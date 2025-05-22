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
 * AlertsInfoTest class.
 */
public class AlertsInfoTest {

    AlertsInfo alertsInfo = new AlertsInfo();

    AlertsInfo.Data data = new AlertsInfo.Data();

    @Test
    public void testgetAlertsData() {
        alertsInfo.getAlertsData();
    }

    @Test
    public void testsetAlertsData() {
        alertsInfo.setAlertsData(data);
    }

    @Test
    public void testgetLocaleToNotificationTemplate() {
        alertsInfo.getLocaleToNotificationTemplate();
    }

    @Test
    public void testsetLocaleToNotificationTemplate() {
        alertsInfo.setLocaleToNotificationTemplate(null);
    }

    @Test
    public void testgetIgniteEvent() {
        alertsInfo.getIgniteEvent();
    }

    @Test
    public void testsetIgniteEvent() {
        alertsInfo.setIgniteEvent(null);
    }

    @Test
    public void testgetEventId() {
        alertsInfo.getEventID();
    }

    @Test
    public void testsetEventId() {
        alertsInfo.setEventID("dsdasda");
    }

    @Test
    public void testgetBenchMode() {
        alertsInfo.getBenchMode();
    }

    @Test
    public void testsetBenchMode() {
        alertsInfo.setBenchMode(0);
    }

    @Test
    public void testgetTimestamp() {
        alertsInfo.getTimestamp();
    }

    @Test
    public void testsetTimestamp() {
        alertsInfo.setTimestamp(0);
    }

    @Test
    public void getVersion() {
        alertsInfo.getVersion();
    }

    @Test
    public void testsetVersion() {
        alertsInfo.setVersion("dummy");
    }

    @Test
    public void testgetTimezone() {
        alertsInfo.getTimezone();
    }

    @Test
    public void testsetTimezone() {
        alertsInfo.setTimezone(0);
    }

    @Test
    public void testgetPdid() {
        alertsInfo.getPdid();
    }

    @Test
    public void testsetPdid() {
        alertsInfo.setPdid("dsadasd");
    }

    @Test
    public void testgetNotificationConfig() {
        alertsInfo.getNotificationConfig();
    }

    @Test
    public void testsetNotificationConfig() {
        alertsInfo.setNotificationConfig(null);
    }

    @Test
    public void testgetNotificationTemplate() {
        alertsInfo.getNotificationTemplate();
    }

    @Test
    public void testaddNotificationTemplate() {
        alertsInfo.addNotificationTemplate(null, null);
    }

    @Test
    public void testgetNotificationTemplateConfig() {
        alertsInfo.getNotificationTemplateConfig();
    }

    @Test
    public void testsetNotificationTemplateConfig() {
        alertsInfo.setNotificationTemplateConfig(null);
    }

    @Test
    public void testgetNotificationConfigs() {
        alertsInfo.getNotificationConfigs();
    }

    @Test
    public void testsetNotificationConfigs() {
        alertsInfo.setNotificationConfigs(null);
    }

    @Test
    public void testaddNotificationConfig() {
        alertsInfo.addNotificationConfig(null);
    }

    @Test
    public void testgetMuteVehicle() {
        alertsInfo.getMuteVehicle();
    }

    @Test
    public void testsetMuteVehicle() {
        alertsInfo.setMuteVehicle(null);
    }

    @Test
    public void testgetAllLanguageTemplates() {
        alertsInfo.getAllLanguageTemplates();
    }

    @Test
    public void testsetAllLanguageTemplates() {
        alertsInfo.setAllLanguageTemplates(null);
    }

    @Test
    public void testgetLocaleToPlaceholders() {
        alertsInfo.getLocaleToPlaceholders();
    }

    @Test
    public void testsetLocaleToPlaceholders() {
        alertsInfo.setLocaleToPlaceholders(null);
    }

    /**
     * a set of all available ChannelTypes merged from all channels in the
     * NotificationConfigs.
     *
     */
    @Test
    public void testresolveAvailableChannelTypes() {
        alertsInfo.resolveAvailableChannelTypes();
    }

    @Test
    public void testgetNotificationGrouping() {
        alertsInfo.getNotificationGrouping();
    }

    @Test
    public void testsetNotificationGrouping() {
        alertsInfo.setNotificationGrouping(null);
    }

    @Test
    public void testgetAlertDataProperties() {
        alertsInfo.getAlertsData();
    }

    @Test
    public void testsetAlertDataProperties() {
        data.setAlertDataProperties(null);
    }

    /**
     * testany test method.
     */
    @Test
    public void testany() {
        data.any();
    }

    /**
     * testset test method.
     *
     */
    @Test
    public void testset() {
        data.set("dummy", alertsInfo);
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
    public void testsetVehicleProfile() {
        data.setVehicleProfile(null);
    }

    @Test
    public void testgetNotificationId() {
        data.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        data.setNotificationId("dumy");
    }

    @Test
    public void testgetMarketingName() {
        data.getMarketingName();
    }

    @Test
    public void testsetMarketingName() {
        data.setMarketingName("dummyMarket");
    }

    @Test
    public void testgetMqttTopic() {
        data.getMqttTopic();
    }

    @Test
    public void testsetMqttTopic() {
        data.setMqttTopic("test");
    }

    @Test
    public void testtoString() {
        data.toString();
    }

    @Test
    public void testtoStringOtr() {
        alertsInfo.toString();
    }
}
