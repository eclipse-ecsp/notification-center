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

package org.eclipse.ecsp.domain.notification.config;

import org.eclipse.ecsp.notification.config.NotificationSettingsInfo;
import org.junit.Test;

/**
 * NotificationSettingsInfoTest class.
 */
public class NotificationSettingsInfoTest {

    NotificationSettingsInfo notificationSettingsInfo = new NotificationSettingsInfo();

    @Test
    public void testgetEventId() {
        notificationSettingsInfo.getEventID();
    }

    @Test
    public void testsetEventId() {
        notificationSettingsInfo.setEventID("test");
    }

    @Test
    public void testgetVersion() {
        notificationSettingsInfo.getVersion();
    }

    @Test
    public void testsetVersion() {
        notificationSettingsInfo.setVersion("");
    }

    @Test
    public void testgetBenchMode() {
        notificationSettingsInfo.getBenchMode();
    }

    @Test
    public void testsetBenchMode() {
        notificationSettingsInfo.setBenchMode(0);
    }

    @Test
    public void testgetTimestamp() {
        notificationSettingsInfo.getTimestamp();
    }

    @Test
    public void testsetTimestamp() {
        notificationSettingsInfo.setTimestamp(0);
    }

    @Test
    public void testgetTimezone() {
        notificationSettingsInfo.getTimezone();
    }

    @Test
    public void testsetTimezone() {
        notificationSettingsInfo.setTimezone(0);
    }

    @Test
    public void getData() {
        notificationSettingsInfo.getData();
    }

    @Test
    public void setData() {
        notificationSettingsInfo.setData(null);
    }

    @Test
    public void testgetChannelResponses() {
        notificationSettingsInfo.getChannelResponses();
    }

    @Test
    public void testsetChannelResponses() {
        notificationSettingsInfo.setChannelResponses(null);
    }

    @Test
    public void testaddChannelResponse() {
        notificationSettingsInfo.addChannelResponse(null);
    }

    @Test
    public void testtoString() {
        notificationSettingsInfo.toString();
    }

    NotificationSettingsInfo.NotificationData ndt = new NotificationSettingsInfo.NotificationData();

    @Test
    public void testgetNotificationConfigs() {
        ndt.getNotificationConfigs();
    }

    @Test
    public void testsetNotificationConfigs() {
        ndt.setNotificationConfigs(null);
    }

    @Test
    public void tes2ttoString() {
        ndt.toString();
    }

}
