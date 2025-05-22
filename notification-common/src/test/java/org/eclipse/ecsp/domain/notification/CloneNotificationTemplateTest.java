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

import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.CloneNotificationTemplate;
import org.junit.Test;

/**
 * CloneNotificationTemplateTest class.
 */
public class CloneNotificationTemplateTest {

    CloneNotificationTemplate cloneNotificationTemplate = new CloneNotificationTemplate();

    ChannelTemplates channelTemplates = new ChannelTemplates();

    @Test
    public void testgetId() {
        cloneNotificationTemplate.getId();
    }

    @Test
    public void testsetId() {
        cloneNotificationTemplate.setId("dsds");
    }

    @Test
    public void testgetNotificationId() {
        cloneNotificationTemplate.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        cloneNotificationTemplate.setNotificationId("dummy");
    }

    @Test
    public void testgetNotificationShortName() {
        cloneNotificationTemplate.getNotificationShortName();
    }

    @Test
    public void testsetNotificationShortName() {
        cloneNotificationTemplate.setNotificationShortName("dummy");
    }

    @Test
    public void testgetNotificationLongName() {
        cloneNotificationTemplate.getNotificationLongName();
    }

    @Test
    public void testsetNotificationLongName() {
        cloneNotificationTemplate.setNotificationLongName("test");
    }

    @Test
    public void testgetLocale() {
        cloneNotificationTemplate.getLocale();
    }

    @Test
    public void testsetLocale() {
        cloneNotificationTemplate.setLocale(null);
    }

    @Test
    public void testgetChannelTemplates() {
        cloneNotificationTemplate.getChannelTemplates();
    }

    @Test
    public void testsetChannelTemplates() {
        cloneNotificationTemplate.setChannelTemplates(channelTemplates);
    }

    @Test
    public void testgetEmailTemplate() {
        cloneNotificationTemplate.setChannelTemplates(channelTemplates);
        cloneNotificationTemplate.getEmailTemplate();
    }

    @Test
    public void testgetApiPushTemplate() {
        cloneNotificationTemplate.setChannelTemplates(channelTemplates);
        cloneNotificationTemplate.getApiPushTemplate();
    }

    @Test
    public void testgetSmsTemplate() {
        cloneNotificationTemplate.setChannelTemplates(channelTemplates);
        cloneNotificationTemplate.getSmsTemplate();
    }

    @Test
    public void testgetIvmTemplate() {
        cloneNotificationTemplate.setChannelTemplates(channelTemplates);
        cloneNotificationTemplate.getIvmTemplate();
    }

    @Test
    public void testgetPushTemplate() {
        cloneNotificationTemplate.setChannelTemplates(channelTemplates);
        cloneNotificationTemplate.getPortalTemplate();
        cloneNotificationTemplate.getPushTemplate();
    }

    @Test
    public void testgetPortalTemplate() {
        cloneNotificationTemplate.setChannelTemplates(channelTemplates);
        cloneNotificationTemplate.getPortalTemplate();

    }

    @Test
    public void testtoString() {
        cloneNotificationTemplate.toString();
    }
}
