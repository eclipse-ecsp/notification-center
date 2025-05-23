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
 * Service class providing operations for alert history, read/unread status, and deletion.
 * Handles alert retrieval by device or user, status updates, and pagination.
 */
@Service
public class AlertsServiceV3 extends AlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertsServiceV3.class);

    private static final String PAGINATION = "pagination";

    private static final String DEVICES = "devices";
    private static final String ALL = "all";

    private final AssociationServiceClient associationServiceClient;

    /**
     * Constructs an instance of AlertsServiceV3.
     *
     * @param associationServiceClient the association service client
     * @param coreVehicleProfileClient the core vehicle profile client
     * @param alertsHistoryDao         the alerts history DAO
     * @param dtcService               the DTC master service
     */
    @Autowired
    public AlertsServiceV3(AssociationServiceClient associationServiceClient,
                           CoreVehicleProfileClient coreVehicleProfileClient, AlertsHistoryDao alertsHistoryDao,
                           DTCMasterService dtcService) {
        super(coreVehicleProfileClient, alertsHistoryDao, dtcService);
        this.associationServiceClient = associationServiceClient;
    }

    /**
     * Retrieves paginated alerts for a specific device.
     *
     * @param params the request parameters containing device ID and filters
     * @return paginated alerts history
     * @throws Exception if validation or data access fails
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
     * Retrieves alerts for all devices associated with a user.
     *
     * @param params the request parameters containing user ID and filters
     * @return a map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
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
     * Retrieves the next page of alerts for a specific device.
     *
     * @param params the request parameters containing device ID and filters
     * @return list of alert history info
     * @throws Exception if validation or data access fails
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
     * Updates the read/unread status of alerts for a device.
     * The request can specify lists of alert IDs to mark as read or unread, or use "all".
     * AlertReadUpdate request data can have three valid format. { "read" :
     * ["2000", "1000","300"], "unread" : ["2001", "1001","301"] }
     *
     * @param deviceId the device (Harman) ID
     * @param reqdata  the read/unread update request
     * @throws IllegalArgumentException if the request is invalid
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
     * Validates and updates the read/unread status of alerts for a device.
     *
     * @param deviceId       the device (Harman) ID
     * @param reqAlertIdlist list of alert IDs to update
     * @param isRead         true to mark as read, false to mark as unread
     * @throws NoSuchEntityException   if no alerts are found for the device
     * @throws IllegalArgumentException if alert IDs are invalid
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
     * Retrieves paginated alerts based on request parameters.
     *
     * @param params the request parameters
     * @return paginated alerts history
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
     * Builds a paginated result from the query response.
     *
     * @param results  the query response
     * @param page     the current page number
     * @param pageSize the page size
     * @return paginated alerts history
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
     * Calculates the total number of pages for pagination.
     *
     * @param totalRecords the total number of records
     * @param pageSize     the page size
     * @return total number of pages
     */
    private int calculateTotalPages(long totalRecords, int pageSize) {
        return ((int) totalRecords / pageSize) + (totalRecords % pageSize == 0 ? 0 : 1);
    }


    /**
     * Groups alerts by device ID for a user.
     *
     * @param alertList the list of alert history info
     * @return a map of device ID to list of alerts
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
     * Deletes alerts for a device, either soft or hard delete.
     *
     * @param deviceId    the device (Harman) ID
     * @param softDelete  true for soft delete, false for hard delete
     * @param alertsDelete the alert IDs to delete (optional)
     * @throws IllegalArgumentException if no alerts are found or IDs are invalid
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
