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

import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertsHistoryRequestParams;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for retrieving alert history for a specific device.
 * Provides methods to fetch alert data within a given time range, optionally filtered by alert names.
 */
@Service
public class AlertsServiceV1 extends AlertService {

    /**
     * Constructs an instance of AlertsServiceV1.
     *
     * @param coreVehicleProfileClient the core vehicle profile client
     * @param alertsHistoryDao         the alerts history DAO
     * @param dtcService               the DTC master service
     */
    public AlertsServiceV1(CoreVehicleProfileClient coreVehicleProfileClient, AlertsHistoryDao alertsHistoryDao,
                           DTCMasterService dtcService) {
        super(coreVehicleProfileClient, alertsHistoryDao, dtcService);
    }

    /**
     * Retrieves alert history items for a device within a specified time range.
     * Optionally filters by alert names if provided in the request parameters.
     *
     * @param params the request parameters containing device ID, time interval, and alert names
     * @return a list of alert history information objects
     * @throws Exception if validation or data access fails
     */
    public List<AlertsHistoryInfo> getAlerts(AlertsHistoryRequestParams params) throws Exception {

        params.getTimeIntervalInfo().validate();

        String userId = getUserIdFromDevice(params.getDeviceIdAsSingle());
        List<AlertsHistoryInfo> resultList = CollectionUtils.isEmpty(params.getAlertNames())
            ? alertsHistoryDao.findByPdidAndTimestampBetween(params.getDeviceIdAsSingle(), userId,
                params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil())
            : alertsHistoryDao.findByPdidAndTimestampBetweenAndAlertTypeIn(params.getDeviceIdAsSingle(), userId,
            params.getTimeIntervalInfo().getSince(), params.getTimeIntervalInfo().getUntil(), params.getAlertNames());
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        cleanStatusHistoryRecords(resultList);
        addAlertMessageToResponse(resultList);
        return resultList;
    }
}
