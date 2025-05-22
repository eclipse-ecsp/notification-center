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

import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.junit.Test;

/**
 * NotificationTemplateConfigTest class.
 */
public class NotificationTemplateConfigTest {

    NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();

    @Test
    public void testgetNotificationId() {
        notificationTemplateConfig.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        notificationTemplateConfig.setNotificationId("test");
    }

    @Test
    public void testgetApiPushConfig() {
        notificationTemplateConfig.getApiPushConfig();
    }

    @Test
    public void testsetApiPushConfig() {
        notificationTemplateConfig.setApiPushConfig(null);
    }

    @Test
    public void testgetEmailConfig() {
        notificationTemplateConfig.getEmailConfig();
    }

    @Test
    public void testsetEmailConfig() {
        notificationTemplateConfig.setEmailConfig(null);
    }

    @Test
    public void testgetSmsConfig() {
        notificationTemplateConfig.getSmsConfig();
    }

    @Test
    public void testsetSmsConfig() {
        notificationTemplateConfig.setSmsConfig(null);
    }

    @Test
    public void testgetIvmConfig() {
        notificationTemplateConfig.getIvmConfig();
    }

    @Test
    public void testsetIvmConfig() {
        notificationTemplateConfig.setIvmConfig(null);
    }

    @Test
    public void testtoString() {
        notificationTemplateConfig.toString();
    }
}
