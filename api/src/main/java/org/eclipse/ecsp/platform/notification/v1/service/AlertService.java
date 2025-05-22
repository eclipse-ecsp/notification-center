/*
 *
 *  *
 *  * ******************************************************************************
 *  *
 *  *  Copyright (c) 2023-24 Harman International
 *  *
 *  *
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *
 *  *  you may not use this file except in compliance with the License.
 *  *
 *  *  You may obtain a copy of the License at
 *  *
 *  *
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  **
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *
 *  *  See the License for the specific language governing permissions and
 *  *
 *  *  limitations under the License.
 *  *
 *  *
 *  *
 *  *  SPDX-License-Identifier: Apache-2.0
 *  *
 *  *  *******************************************************************************
 *  *
 *
 */

package org.eclipse.ecsp.platform.notification.v1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.entities.DTCMaster;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.eclipse.ecsp.platform.notification.v1.common.Constants;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertHistoryResponseItem;
import org.eclipse.ecsp.platform.notification.v1.events.EventMetadataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Alert service for alert history CRUD operations.
 */
public class AlertService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertService.class);

    protected AlertsHistoryDao alertsHistoryDao;

    protected DTCMasterService dtcService;

    @Value("${enable.ignite.vehicle.profile:true}")
    private boolean igniteVehicleProfile;

    private final CoreVehicleProfileClient coreVehicleProfileClient;

    /**
     * constructor.
     */
    public AlertService(CoreVehicleProfileClient coreVehicleProfileClient, AlertsHistoryDao alertsHistoryDao,
                        DTCMasterService dtcService) {
        this.coreVehicleProfileClient = coreVehicleProfileClient;
        this.alertsHistoryDao = alertsHistoryDao;
        this.dtcService = dtcService;
    }

    /**
     * To fetch DTC details from DTC code.
     *
     * @param dtcCode dtc code
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    protected void setDTCDetails(String dtcCode, Map<String, Object> dtcCurrentStateMap) {
        DTCMaster dtcInfo = dtcService.getDtcCode(dtcCode);
        dtcCurrentStateMap.put(Constants.DTC_CODE, dtcCode);
        dtcCurrentStateMap.put(Constants.DTC_DESCRIPTION,
            (null != dtcInfo) ? dtcInfo.getDescription() : Constants.NOT_APPLICABLE);
        dtcCurrentStateMap.put(Constants.DTC_CATEGORY,
            (null != dtcInfo) ? dtcInfo.getCategory() : Constants.NOT_APPLICABLE);
        dtcCurrentStateMap.put(Constants.DTC_SUB_CATEGORY,
            (null != dtcInfo) ? dtcInfo.getSubcategory() : Constants.NOT_APPLICABLE);
        dtcCurrentStateMap.put(Constants.DTC_SUGGESTIONS,
            (null != dtcInfo) ? dtcInfo.getSuggestions() : Constants.NOT_APPLICABLE);
    }

    /**
     * To get existing alertHistory object and add corresponding alertMessage.
     * If alert type is DTC then fetch data contents from DTC code and append it
     * with existing alert history object.
     *
     * @param resultList List of AlertsHistoryInfo
     */
    protected void addAlertMessageToResponse(List<AlertsHistoryInfo> resultList) {
        resultList.forEach(alertData -> {
            if (alertData.getAlertType().equalsIgnoreCase(AlertHistoryResponseItem.AlertNames.DTCSTORED.getEventID())) {
                Map<String, Object> alertsPayLoad = alertData.getPayload().getAlertsData().any();
                List<String> setdtclist = (List<String>) alertsPayLoad.get(EventMetadataConstants.SET);
                Map<String, Object> currentState =
                    (Map<String, Object>) alertsPayLoad.get(EventMetadataConstants.CURRENT_STATE);
                if (!ObjectUtils.isEmpty(setdtclist) && !ObjectUtils.isEmpty(currentState)) {
                    for (String dtcCode : setdtclist) {
                        setDTCDetails(dtcCode, currentState);
                    }
                }
            }
            if (null != alertData.getChannelResponses()) {
                alertData.setAlertMessage(alertData.getAlertMessage());
            }
        });
    }

    /**
     * Get userId available in vehicleprofile.
     *
     * @param pdid pdid
     *
     * @return userId
     *
     * @throws Exception if failed to get VP
     */
    public String getUserIdFromDevice(String pdid) {
        try {
            Optional<String> vehicleProfileOptional =
                coreVehicleProfileClient.getVehicleProfileJSON(pdid, igniteVehicleProfile);
            if (vehicleProfileOptional.isPresent()) {
                JsonNode vehicleProfile = new ObjectMapper().readTree(vehicleProfileOptional.get());
                String userId = vehicleProfile.get("authorizedUsers").get(0).get("userId").asText();
                return userId;
            } else {
                LOGGER.error("Vehicle ID {} does not exist", pdid);
                throw new InvalidVehicleIdInput("Vehicle ID does not exist");
            }
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.error(String.format("Vehicle ID %s does not exist", pdid), e);
            throw new InvalidVehicleIdInput("Vehicle ID does not exist");
        } catch (Exception e) {
            LOGGER.error("Failed getting vehicle profile", e);
            throw new VehicleIdNotFoundException(Collections.singletonList(
                NotificationCenterError.NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.toMessage()));
        }
    }

    /**
     * Clean status history records before flushing history to consumer.
     */
    public void cleanStatusHistoryRecords(List<AlertsHistoryInfo> alertList) {
        if (!CollectionUtils.isEmpty(alertList)) {
            alertList.removeIf(r -> (r.getStatusHistoryRecordList().size() > 0
                && !containsStatus(r.getStatusHistoryRecordList(), AlertsHistoryInfo.Status.DONE)));
            alertList.forEach(r -> r.setStatusHistoryRecordList(null));
        }
    }

    /**
     * containsStatus.
     *
     * @param list list
     * @param status   status
     * @return  boolean
     */
    public boolean containsStatus(final List<StatusHistoryRecord> list, final AlertsHistoryInfo.Status status) {
        return list.stream().anyMatch(o -> o.getStatus().equals(status));
    }
}
