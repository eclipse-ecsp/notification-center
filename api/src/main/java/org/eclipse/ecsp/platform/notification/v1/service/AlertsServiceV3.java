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
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.nosqldao.IgnitePagingInfoResponse;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.entities.PaginatedAlertsHistory;
import org.eclipse.ecsp.notification.entities.Pagination;
import org.eclipse.ecsp.notification.entities.TotalPagination;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertIdDto;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertReadUpdate;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertsHistoryRequestParams;
import org.eclipse.ecsp.platform.notification.v1.fw.web.AssociationServiceClient;
import org.eclipse.ecsp.platform.notification.v1.fw.web.DeviceAssociation;
import org.eclipse.ecsp.platform.notification.v1.utils.AlertsConstants;
import org.eclipse.ecsp.platform.notification.v1.utils.ReadStatusValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.ecsp.platform.notification.v1.common.Constants.ASSOCIATED;

/**
 * alert services.
 */
@Service
public class AlertsServiceV3 extends AlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertsServiceV3.class);

    private static final String PAGINATION = "pagination";

    private static final String DEVICES = "devices";
    private static final String ALL = "all";

    private final AssociationServiceClient associationServiceClient;

    /**
     * AlertsServiceV3 constructor.
     */
    @Autowired
    public AlertsServiceV3(AssociationServiceClient associationServiceClient,
                           CoreVehicleProfileClient coreVehicleProfileClient, AlertsHistoryDao alertsHistoryDao,
                           DTCMasterService dtcService) {
        super(coreVehicleProfileClient, alertsHistoryDao, dtcService);
        this.associationServiceClient = associationServiceClient;
    }

    /**
     * Get alerts by device ID.
     */
    public PaginatedAlertsHistory getAlertsByDeviceId(AlertsHistoryRequestParams params) throws Exception {
        params.getTimeIntervalInfo().validate();

        String userId = getUserIdFromDevice(params.getDeviceIdAsSingle());
        params.setUserId(userId);
        PaginatedAlertsHistory pageList = getAlerts(params);
        addAlertMessageToResponse(pageList.getAlerts());
        return pageList;
    }

    /**
     * To get alert data from user by fetching list of pdids.
     */
    public Map<String, Object> getAlertsByUserid(AlertsHistoryRequestParams params) throws Exception {
        List<DeviceAssociation> devices =
            associationServiceClient.getDevices(params.getUserId()).orElse(Collections.emptyList());

        for (DeviceAssociation device : devices) {
            if (StringUtils.isNotEmpty(device.getHarmanID())
                && StringUtils.equals(device.getAssociationStatus(), ASSOCIATED)) {
                params.getDeviceIds().add(device.getHarmanID());
            }
        }

        PaginatedAlertsHistory pageList = getAlerts(params);
        Map<String, List<AlertsHistoryInfo>> alertMap = prepareAlertsByUserId(pageList.getAlerts());

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(PAGINATION, pageList.getPagination());
        dataMap.put(DEVICES, alertMap);
        return dataMap;
    }

    /**
     * Get alerts by deviceId.
     */
    public List<AlertsHistoryInfo> getNextAlertsByDeviceId(AlertsHistoryRequestParams params) throws Exception {

        params.getTimeIntervalInfo().validate();

        String userId = getUserIdFromDevice(params.getDeviceIdAsSingle());
        params.setUserId(userId);
        PaginatedAlertsHistory pageList = getAlerts(params);

        addAlertMessageToResponse(pageList.getAlerts());

        return pageList.getAlerts();
    }

    /**
     * AlertReadUpdate request data can have three valid format. { "read" :
     * ["2000", "1000","300"], "unread" : ["2001", "1001","301"] }
     *
     * <p>or { "read" : ["all"] }
     *
     * <p>or { "unread" : ["all"] }
     *
     * @param deviceId harmanId
     *
     * @param reqdata  AlertReadUpdate
     */
    public void saveData(String deviceId, AlertReadUpdate reqdata) {

        LOGGER.debug("Entered saveData method with parameter deviceId: {}", deviceId);
        if (CollectionUtils.containsAny(reqdata.getReadList(), reqdata.getUnreadList())) {
            throw new IllegalArgumentException(AlertsConstants.ALERTS_READ_AND_UNREAD_INVALID_REQUEST);
        }

        if (CollectionUtils.isEmpty(reqdata.getReadList()) && CollectionUtils.isEmpty(reqdata.getUnreadList())) {
            validateAndSaveData(deviceId, reqdata.getUnreadList(), Boolean.FALSE);
        } else if (CollectionUtils.isEmpty(reqdata.getUnreadList())
            && !CollectionUtils.isEmpty(reqdata.getReadList())) {
            validateAndSaveData(deviceId, reqdata.getReadList(), Boolean.TRUE);
        } else {
            validateAndSaveData(deviceId, reqdata.getReadList(), Boolean.TRUE);
            validateAndSaveData(deviceId, reqdata.getUnreadList(), Boolean.FALSE);
        }
        LOGGER.debug("Exited saveData method, data updated successfully for deviceId: {}",
            deviceId);
    }

    /**
     * Validate and save data.
     *
     * @param deviceId       HarmanId
     *
     * @param reqAlertIdlist reqAlertIDlist
     *
     * @param isRead         boolean value
     */
    private void validateAndSaveData(String deviceId, List<String> reqAlertIdlist, boolean isRead) {

        LOGGER.debug("Entered validateAndSaveData method");

        if (!CollectionUtils.isEmpty(reqAlertIdlist)) {
            String userId = getUserIdFromDevice(deviceId);
            List<AlertsHistoryInfo> dbAlertDataList;
            if (ALL.equalsIgnoreCase(reqAlertIdlist.get(0))) {
                dbAlertDataList = alertsHistoryDao.findByPdidAndRead(deviceId, userId, !isRead);
                /*
                 * If no alerts found for the provided deviceId, then 404
                 * NOT_FOUND returned with error message
                 */
                if (CollectionUtils.isEmpty(dbAlertDataList)) {
                    List<AlertsHistoryInfo> dbAlertDataListTotal = alertsHistoryDao.findByPdid(deviceId, userId);
                    if (CollectionUtils.isEmpty(dbAlertDataListTotal)) {
                        throw new NoSuchEntityException(AlertsConstants.ALERTS_NO_DATA_FOUND_ERROR_MSG);
                    }
                }
            } else {
                dbAlertDataList = alertsHistoryDao.findByPdidAndIdIn(deviceId, userId, reqAlertIdlist);
                if (dbAlertDataList.size() != reqAlertIdlist.size()) {
                    throw new IllegalArgumentException(AlertsConstants.INVALID_ALERT_ID);
                }
            }
            dbAlertDataList.forEach(data -> {
                data.setRead(isRead);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("data :: %s", data));
                }
                alertsHistoryDao.save(data);
            });
        }

        LOGGER.debug("Exited validateAndSaveData method");
    }

    /**
     * Get alerts.
     */
    private PaginatedAlertsHistory getAlerts(AlertsHistoryRequestParams params) {
        params.getTimeIntervalInfo().validate();

        ReadStatusValidator.validate(params.getReadStatus());
        Object read = ReadStatusValidator.convert(params.getReadStatus());

        IgnitePagingInfoResponse<AlertsHistoryInfo> alertsHistoryInfo;
        boolean filterByAlertNames = !CollectionUtils.isEmpty(params.getAlertNames());
        if (filterByAlertNames && read != null) {
            alertsHistoryInfo =
                alertsHistoryDao.findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(
                    params.getDeviceIds(), params.getUserId(), (boolean) read, params.getAlertNames(),
                    params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil(),
                    params.getPaginationInfo().getPage(), params.getPaginationInfo().getSize());
        } else if (!filterByAlertNames && read != null) {
            alertsHistoryInfo = alertsHistoryDao.findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc(
                params.getDeviceIds(), params.getUserId(), (boolean) read,
                params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil(),
                params.getPaginationInfo().getPage(), params.getPaginationInfo().getSize());
        } else if (filterByAlertNames) {
            alertsHistoryInfo = alertsHistoryDao.findByPdidAndAlertTypeInAndTimestampBetweenOrderByTimestampDesc(
                params.getDeviceIds(), params.getUserId(), params.getAlertNames(),
                params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil(),
                params.getPaginationInfo().getPage(), params.getPaginationInfo().getSize());
        } else {
            alertsHistoryInfo = alertsHistoryDao.findByPdidInAndTimestampBetweenOrderByTimestampDesc(
                params.getDeviceIds(), params.getUserId(),
                params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil(),
                params.getPaginationInfo().getPage(), params.getPaginationInfo().getSize());
        }

        cleanStatusHistoryRecords(alertsHistoryInfo.getData());
        return getPaginatedResults(alertsHistoryInfo, params.getPaginationInfo().getPage(),
            params.getPaginationInfo().getSize());
    }

    /**
     * Get paginated results.
     */
    private PaginatedAlertsHistory getPaginatedResults(IgnitePagingInfoResponse<AlertsHistoryInfo> results, int page,
                                                       int pageSize) {
        int resultSize = CollectionUtils.isEmpty(results.getData()) ? 0 : results.getData().size();
        return PaginatedAlertsHistory.builder()
            .alerts(results.getData())
            .pagination(Pagination.builder()
                .page(page)
                .size(resultSize)
                .total(TotalPagination.builder()
                    .pages(calculateTotalPages(results.getTotal(), pageSize))
                    .records(results.getTotal()).build()).build()).build();
    }

    /**
     * Calculate total pages.
     */
    private int calculateTotalPages(long totalRecords, int pageSize) {
        return ((int) totalRecords / pageSize) + (totalRecords % pageSize == 0 ? 0 : 1);
    }

    /**
     * Prepare alerts by userId.
     */
    private Map<String, List<AlertsHistoryInfo>> prepareAlertsByUserId(List<AlertsHistoryInfo> alertList) {
        Map<String, List<AlertsHistoryInfo>> deviceIdToAlertListMap = new HashMap<>();
        List<AlertsHistoryInfo> alerts;
        for (AlertsHistoryInfo alert : alertList) {
            alerts = deviceIdToAlertListMap.computeIfAbsent(alert.getPdid(), k -> new ArrayList<>());
            alerts.add(alert);
        }
        return deviceIdToAlertListMap;
    }

    /**
     * Delete alerts.
     */
    public void deleteAlerts(String deviceId, boolean softDelete, AlertIdDto alertsDelete) {

        List<AlertsHistoryInfo> alertsList;
        String userId = getUserIdFromDevice(deviceId);
        if (alertsDelete != null) {
            alertsList = alertsHistoryDao.findByPdidAndIdIn(deviceId, userId, alertsDelete.getAlertIdList());

            if (!CollectionUtils.isEmpty(alertsList) && alertsList.size() != alertsDelete.getAlertIdList().size()) {
                throw new IllegalArgumentException(AlertsConstants.INVALID_ALERT_ID_DELETE);
            }

            LOGGER.debug("retrieved alertsHistory count ::{}", alertsList.size());
        } else {
            alertsList = alertsHistoryDao.findByPdid(deviceId, userId);
        }

        if (CollectionUtils.isEmpty(alertsList)) {
            throw new IllegalArgumentException(AlertsConstants.ALERTS_NO_DATA_FOUND_ERROR_MSG);
        }

        if (softDelete) {
            alertsList.forEach(alert -> {
                alert.setDeleted(true);
                alertsHistoryDao.save(alert);
            });
        } else {
            alertsHistoryDao.deleteByIds(alertsList.stream().map(AlertsHistoryInfo::getId).toArray(String[]::new));
        }
    }
}
