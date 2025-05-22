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

package org.eclipse.ecsp.domain.notification.dao;

import org.eclipse.ecsp.notification.dao.AlertsHistoryDaoImpl;
import org.junit.Test;

/**
 * AlertsHistoryDaoImplTest class.
 */
public class AlertsHistoryDaoImplTest {

    AlertsHistoryDaoImpl alertsHistoryDaoImpl = new AlertsHistoryDaoImpl();

    private String two = "2";

    @Test(expected = Exception.class)
    public void findByPdidAndTimestampBetween() {
        alertsHistoryDaoImpl.findByPdidAndTimestampBetween("", "", 0, 0);
    }

    @Test(expected = Exception.class)
    public void findByPdidAndTimestampBetweenAndAlertTypeIn() {
        alertsHistoryDaoImpl.findByPdidAndTimestampBetweenAndAlertTypeIn(
            "", "", 0, 0, null);
    }

    @Test(expected = Exception.class)
    public void findByPdidAndIdIn() {
        alertsHistoryDaoImpl.findByPdidAndIdIn("", "", null);
    }

    @Test(expected = Exception.class)
    public void findByPdid() {
        alertsHistoryDaoImpl.findByPdid("", "");
    }

    @Test(expected = Exception.class)
    public void findByPdidInAndTimestampBetweenOrderByTimestampDesc() {
        alertsHistoryDaoImpl.findByPdidInAndTimestampBetweenOrderByTimestampDesc(
            null, null, 0, 0, 1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByPdidInAndAlertTypeInAndTimestampBetweenOrderByTimestampDesc() {
        alertsHistoryDaoImpl.findByPdidAndAlertTypeInAndTimestampBetweenOrderByTimestampDesc(
            null, null, null, 0, 0,
            1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc() {
        alertsHistoryDaoImpl.findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc(
            null, null, false, 0, 0, 1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByPdidInAndReadAndAlertTypeInAndTimestampBetweenOrderByTimestampDesc() {
        alertsHistoryDaoImpl.findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(
            null, null, false,
                null, 0, 0, 1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByuserIdvehicleIdTimestampBetween() {
        alertsHistoryDaoImpl.findByuserIdvehicleIdTimestampBetween(
            null, null, 0, 0, 1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByUserIdTimestampBetween() {
        alertsHistoryDaoImpl.findByUserIdTimestampBetween(
            null, 0, 0, 1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByVehicleIdTimestampBetween() {
        alertsHistoryDaoImpl.findByVehicleIdTimestampBetween(
            null, null, 0, 0, 1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByOnlyVehicleIdTimestampBetween() {
        alertsHistoryDaoImpl.findByOnlyVehicleIdTimestampBetween(
            null, 0, 0, 1, Integer.parseInt(two));
    }

    @Test(expected = Exception.class)
    public void findByPdidAndRead() {
        alertsHistoryDaoImpl.findByPdidAndRead(null, null, false);
    }

    @Test(expected = Exception.class)
    public void updateChannel() {
        alertsHistoryDaoImpl.updateChannel(null, 0, null);
    }

    @Test(expected = Exception.class)
    public void findCountByCampaignId() {
        alertsHistoryDaoImpl.findCountByCampaignId(null);
    }

    @Test(expected = Exception.class)
    public void findCountOfSuccessfulRequests() {
        alertsHistoryDaoImpl.findCountOfSuccessfulRequests(null);
    }

    @Test(expected = Exception.class)
    public void findByCampaignIdPageAndSizeFailed() {
        alertsHistoryDaoImpl.findByCampaignIdPageAndSizeFailed(
            null, 0, 0);
    }

    @Test(expected = Exception.class)
    public void findByCampaignIdPageAndSizeSuccess() {
        alertsHistoryDaoImpl.findByCampaignIdPageAndSizeSuccess(null, 0, 0);
    }
}
