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
 * alerts service.
 */
@Service
public class AlertsServiceV1 extends AlertService {

    /**
     * AlertsServiceV1 constructor.
     */
    public AlertsServiceV1(CoreVehicleProfileClient coreVehicleProfileClient, AlertsHistoryDao alertsHistoryDao,
                           DTCMasterService dtcService) {
        super(coreVehicleProfileClient, alertsHistoryDao, dtcService);
    }

    /**
     * To fetch alert history items based on time stamp provided and also with
     * alertNames if provided. verifyLesserOfTwoNumber method is used to find
     * whether until value is more then since value or not.
     *
     * @return List of AlertHistoryInfo object
     * @throws Exception exception
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
            return new ArrayList<AlertsHistoryInfo>();
        }
        cleanStatusHistoryRecords(resultList);
        addAlertMessageToResponse(resultList);
        return resultList;
    }
}
