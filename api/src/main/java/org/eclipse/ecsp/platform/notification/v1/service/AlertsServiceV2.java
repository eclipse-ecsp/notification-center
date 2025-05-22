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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.platform.notification.v1.common.Constants;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertReadUpdate;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertsHistoryRequestParams;
import org.eclipse.ecsp.platform.notification.v1.fw.web.AssociationServiceClient;
import org.eclipse.ecsp.platform.notification.v1.fw.web.DeviceAssociation;
import org.eclipse.ecsp.platform.notification.v1.utils.AlertsConstants;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alert service.
 */
@Service
public class AlertsServiceV2 extends AlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertsServiceV2.class);
    private static final String ALERTS = "alerts";
    private static final String TOTAL_ALERTS = "totalAlerts";
    private final AssociationServiceClient associationServiceClient;

    /**
     * AlertsServiceV2 constructor.
     */
    public AlertsServiceV2(CoreVehicleProfileClient coreVehicleProfileClient, AlertsHistoryDao alertsHistoryDao,
                           AssociationServiceClient associationServiceClient, DTCMasterService dtcService) {
        super(coreVehicleProfileClient, alertsHistoryDao, dtcService);
        this.associationServiceClient = associationServiceClient;
    }

    /**
     * To fetch alert history items based on time stamp provided and also with
     * alertNames if provided. verifyLesserOfTwoNumber method is used to find
     * whether until value is more then since value or not.
     */
    public Map<String, Object> getAlertsByDeviceId(AlertsHistoryRequestParams params) throws Exception {

        params.getTimeIntervalInfo().validate();
        String userId = getUserIdFromDevice(params.getDeviceIdAsSingle());
        List<AlertsHistoryInfo> resultList = CollectionUtils.isEmpty(params.getAlertNames())
            ? alertsHistoryDao.findByPdidAndTimestampBetween(params.getDeviceIdAsSingle(), userId,
            params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil())
            : alertsHistoryDao.findByPdidAndTimestampBetweenAndAlertTypeIn(params.getDeviceIdAsSingle(), userId,
            params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil(), params.getAlertNames());

        if (CollectionUtils.isEmpty(resultList)) {
            return new HashMap<String, Object>();
        }

        cleanStatusHistoryRecords(resultList);
        addAlertMessageToResponse(resultList);

        List<JSONObject> result = new ArrayList<>();
        resultList.forEach(alert -> {
            JSONObject alertHistory = new JSONObject();
            alertHistory.put(NotificationConstants.ID, alert.getId());
            alertHistory.put(NotificationConstants.PAYLOAD, alert.getPayload());
            alertHistory.put(NotificationConstants.ALERT_TYPE, alert.getAlertType());
            alertHistory.put(NotificationConstants.ALERT_MESSAGE, alert.getAlertMessage());
            result.add(alertHistory);
        });
        Map<String, Object> alertMap = new HashMap<>();
        alertMap.put(TOTAL_ALERTS, resultList.size());
        alertMap.put(ALERTS, result);
        return alertMap;
    }

    /**
     * To get alert data from user by fetching list of pdids.
     */
    public Map<String, Object> getAlertsByUserId(AlertsHistoryRequestParams params) throws Exception {
        List<DeviceAssociation> devices =
            associationServiceClient.getDevices(params.getUserId()).orElse(Collections.emptyList());
        Map<String, Object> dataMap = new HashMap<>();
        int totalNoOfAlerts = 0;
        for (DeviceAssociation device : devices) {
            if (StringUtils.isNotEmpty(device.getHarmanID())
                && StringUtils.equals(device.getAssociationStatus(), Constants.ASSOCIATED)) {
                List<AlertsHistoryInfo> resultList = CollectionUtils.isEmpty(params.getAlertNames())
                    ? alertsHistoryDao.findByPdidAndTimestampBetween(device.getHarmanID(), params.getUserId(),
                        params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil())
                    : alertsHistoryDao.findByPdidAndTimestampBetweenAndAlertTypeIn(device.getHarmanID(),
                    params.getUserId(), params.getTimeIntervalInfo().getSince(),
                    params.getTimeIntervalInfo().getUntil(), params.getAlertNames());
                addAlertMessageToResponse(resultList);
                if (!resultList.isEmpty()) {
                    totalNoOfAlerts += resultList.size();
                    dataMap.put(device.getHarmanID(), resultList);
                }
            }
        }

        if (CollectionUtils.isEmpty(dataMap)) {
            return new HashMap<String, Object>();
        }

        dataMap.put(TOTAL_ALERTS, totalNoOfAlerts);
        return dataMap;
    }

    /**
     * Save data.
     *
     * @param deviceId deviceId
     *
     * @param reqdata regdata
     */
    public void saveData(String deviceId, String userId, AlertReadUpdate reqdata) {
        LOGGER.info("Entered saveData method");
        validateAndSaveData(deviceId, userId, reqdata.getReadList(), Boolean.TRUE);
        validateAndSaveData(deviceId, userId, reqdata.getUnreadList(), Boolean.FALSE);
        LOGGER.info("Exited saveData method , data updated successfully");

    }

    /**
     * Validate and save data.
     *
     * @param deviceId deviceId
     *
     * @param reqAlertIdlist alertList
     *
     * @param isRead boolean
     */
    private void validateAndSaveData(String deviceId, String userId, List<String> reqAlertIdlist, boolean isRead) {
        LOGGER.info("Entered validateAndSaveData method");
        if (!CollectionUtils.isEmpty(reqAlertIdlist)) {
            List<AlertsHistoryInfo> dbAlertDataList =
                alertsHistoryDao.findByPdidAndIdIn(deviceId, userId, reqAlertIdlist);
            if (dbAlertDataList.size() != reqAlertIdlist.size()) {
                throw new NoSuchEntityException(AlertsConstants.INVALID_ALERT_ID);
            }
            dbAlertDataList.forEach(data -> {
                data.setRead(isRead);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("data is %s ", data));
                }
                alertsHistoryDao.save(data);
            });
        }
        LOGGER.info("Exited validateAndSaveData method");
    }
}
