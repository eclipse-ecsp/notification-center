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

import org.eclipse.ecsp.notification.entities.BufferedAlertsInfo;
import org.junit.Test;

/**
 * BufferedAlertsInfoTest class.
 */
public class BufferedAlertsInfoTest {

    BufferedAlertsInfo bufferedAlertsInfo = new BufferedAlertsInfo();

    @Test
    public void testgetCloneNotificationConfig() {
        bufferedAlertsInfo.getCloneNotificationConfig();
    }

    @Test
    public void testgetCloneNotificationTemplate() {
        bufferedAlertsInfo.getCloneLocaleToNotificationTemplates();
    }

    @Test
    public void testaddCloneNotificationTemplate() {
        bufferedAlertsInfo.addCloneNotificationTemplate("test", null);
    }

    @Test
    public void testsetCloneNotificationConfig() {
        bufferedAlertsInfo.setCloneNotificationConfig(null);
    }

    @Test
    public void testgetCloneNotificationTemplateConfig() {
        bufferedAlertsInfo.getCloneLocaleToNotificationTemplates();
    }

    @Test
    public void testsetCloneNotificationTemplateConfig() {
        bufferedAlertsInfo.setCloneNotificationTemplateConfig(null);
    }

    @Test
    public void testgetAlertsData() {
        bufferedAlertsInfo.getAlertsData();
    }

    @Test
    public void testsetAlertsData() {
        bufferedAlertsInfo.setAlertsData(null);
    }

    @Test
    public void testgetIgniteEvent() {
        bufferedAlertsInfo.getIgniteEvent();
    }

    @Test
    public void testsetIgniteEvent() {
        bufferedAlertsInfo.setIgniteEvent(null);
    }

    @Test
    public void testgetEventId() {
        bufferedAlertsInfo.getEventID();
    }

    @Test
    public void testsetEventId() {
        bufferedAlertsInfo.setEventID("dummy");
    }

    @Test
    public void testgetBenchMode() {
        bufferedAlertsInfo.getBenchMode();
    }

    @Test
    public void testsetBenchMode() {
        bufferedAlertsInfo.setBenchMode(0);
    }

    @Test
    public void testgetTimestamp() {
        bufferedAlertsInfo.getTimestamp();
    }

    @Test
    public void testsetTimestamp() {
        bufferedAlertsInfo.setTimestamp(0);
    }

    @Test
    public void testgetVersion() {
        bufferedAlertsInfo.getVersion();
    }

    @Test
    public void testsetVersion() {
        bufferedAlertsInfo.setVersion("1.1");
    }

    @Test
    public void testgetTimezone() {
        bufferedAlertsInfo.getTimezone();
    }

    @Test
    public void testsetTimezone() {
        bufferedAlertsInfo.setTimezone(0);
    }

    @Test
    public void testgetPdid() {
        bufferedAlertsInfo.getPdid();
    }

    @Test
    public void testsetPdid() {
        bufferedAlertsInfo.setPdid("dummy");
    }

    @Test
    public void testgetNotificationConfigs() {
        bufferedAlertsInfo.getNotificationConfigs();
    }

    @Test
    public void testsetNotificationConfigs() {
        bufferedAlertsInfo.setNotificationConfigs(null);
    }

    @Test
    public void testaddNotificationConfig() {
        bufferedAlertsInfo.addNotificationConfig(null);
    }

    BufferedAlertsInfo.Data data = new BufferedAlertsInfo.Data();

    @Test
    public void getAlertDataProperties() {
        data.getAlertDataProperties();
    }

    @Test
    public void setAlertDataProperties() {
        data.setAlertDataProperties(null);
    }

    @Test
    public void testany() {
        data.any();
    }

    @Test
    public void testset() {
        data.set("dummy", null);
    }

    @Test
    public void testgetUserProfile() {
        data.getUserProfile();
    }

    @Test
    public void testsetUserProfile() {
        data.setUserProfile(null);
    }

    @Test
    public void testgetVehicleProfile() {
        data.getVehicleProfile();
    }

    @Test
    public void setVehicleProfile() {
        data.setVehicleProfile(null);
    }

    @Test
    public void testgetNotificationId() {
        data.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        data.setNotificationId("dummyId");
    }

    @Test
    public void testgetMarketingName() {
        data.getMarketingName();
    }

    @Test
    public void testsetMarketingName() {
        data.setMarketingName("test");
    }

    @Test
    public void testtoString() {
        data.toString();
    }

    @Test
    public void toStringTest() {
        bufferedAlertsInfo.toString();
    }
}
