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

package org.eclipse.ecsp.domain.notification;

import org.junit.Test;

/**
 * NotificationCreationRequestTest class.
 */
public class NotificationCreationRequestTest {

    NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

    NotificationRequest nr = new NotificationCreationRequest();

    @Test
    public void testgetNotificationId() {
        notificationCreationRequest.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        notificationCreationRequest.setNotificationId("");
    }

    @Test
    public void testgetVersion() {
        notificationCreationRequest.getVersion();
    }

    @Test
    public void testsetVersion() {
        notificationCreationRequest.setVersion("");
    }

    @Test
    public void testgetTimestamp() {
        notificationCreationRequest.getTimestamp();
    }

    @Test
    public void testsetTimestamp() {
        notificationCreationRequest.setTimestamp(0);
    }

    @Test
    public void testgetTimezone() {
        notificationCreationRequest.getTimezone();
    }

    @Test
    public void testsetTimezone() {
        notificationCreationRequest.setTimezone((short) 0);
    }

    @Test
    public void testgetData() {
        notificationCreationRequest.getData();
    }

    @Test
    public void testsetData() {
        notificationCreationRequest.setData(null);
    }

    @Test
    public void testgetCampaignId() {
        notificationCreationRequest.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        notificationCreationRequest.setCampaignId("sas");
    }

    @Test
    public void testisDynamicNotification() {
        notificationCreationRequest.isDynamicNotification();
    }

    @Test
    public void testsetDynamicNotification() {
        notificationCreationRequest.setDynamicNotification(true);
    }

    @Test
    public void testisUserNotification() {
        notificationCreationRequest.isUserNotification();
    }

    @Test
    public void testsetUserNotification() {
        notificationCreationRequest.setUserNotification(true);
    }

    @Test
    public void testgetSchedule() {
        notificationCreationRequest.getSchedule();
    }

    @Test
    public void testsetSchedule() {
        notificationCreationRequest.setSchedule("test123");
    }

    @Test

    public void testgetBrand() {
        notificationCreationRequest.getBrand();
    }

    @Test
    public void testsetBrand() {
        notificationCreationRequest.setBrand("");
    }

    @Test
    public void testisCampaignNotification() {
        notificationCreationRequest.isCampaignNotification();
    }

    @Test
    public void testsetCampaignNotification() {
        notificationCreationRequest.setCampaignNotification(true);
    }

    @Test
    public void testtoString() {
        notificationCreationRequest.toString();
    }

    @Test
    public void testgetsetinparentclass() {
        notificationCreationRequest.getUserId();
        notificationCreationRequest.setUserId(null);
        notificationCreationRequest.getVehicleId();
        notificationCreationRequest.setVehicleId(null);
        notificationCreationRequest.getRequestId();
        notificationCreationRequest.setRequestId(null);
        notificationCreationRequest.getSessionId();
        notificationCreationRequest.setSessionId(null);
        notificationCreationRequest.getClientRequestId();
        notificationCreationRequest.setClientRequestId(null);

    }

}
