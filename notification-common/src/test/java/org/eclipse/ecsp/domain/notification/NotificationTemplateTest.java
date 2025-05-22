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

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * NotificationTemplateTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationTemplateTest {


    NotificationTemplate notificationTemplate = new NotificationTemplate();


    ChannelTemplates channelTemplates = new ChannelTemplates();


    @Before
    public void beforeEach() {
        notificationTemplate.setChannelTemplates(channelTemplates);
    }


    @Test
    public void testgetId() {
        notificationTemplate.getId();
    }

    @Test
    public void testsetId() {
        notificationTemplate.setId("test123");
    }

    @Test
    public void testgetNotificationId() {
        notificationTemplate.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        notificationTemplate.setNotificationId("dumm");
    }

    @Test
    public void testgetNotificationShortName() {
        notificationTemplate.getNotificationShortName();
    }

    @Test
    public void testsetNotificationShortName() {
        notificationTemplate.setNotificationShortName("test");
    }

    @Test
    public void testgetNotificationLongName() {
        notificationTemplate.getNotificationLongName();
    }

    @Test
    public void setNotificationLongName() {
        notificationTemplate.setNotificationLongName("");
    }

    @Test
    public void testgetLocale() {
        notificationTemplate.getLocale();
    }

    @Test
    public void testsetLocale() {
        notificationTemplate.setLocale(null);
    }

    @Test
    public void testgetCustomPlaceholders() {
        notificationTemplate.getCustomPlaceholders();
    }

    @Test
    public void testsetCustomPlaceholders() {
        notificationTemplate.setCustomPlaceholders(null);
    }

    @Test
    public void testgetChannelTemplates() {
        notificationTemplate.getChannelTemplates();
    }

    @Test
    public void testsetChannelTemplates() {
        notificationTemplate.setChannelTemplates(channelTemplates);
    }

    @Test
    public void testgetAdditionalLookupProperties() {
        notificationTemplate.getAdditionalLookupProperties();
    }

    @Test
    public void setAdditionalLookupProperties() {
        notificationTemplate.setAdditionalLookupProperties(null);
    }

    @Test
    public void testgetLocaleAsLocale() {
        notificationTemplate.getLocaleAsLocale();
    }

    @Test
    public void testgetEmailTemplate() {
        notificationTemplate.getEmailTemplate();
    }

    @Test
    public void testgetApiPushTemplate() {
        notificationTemplate.getApiPushTemplate();
    }

    @Test
    public void testgetSmsTemplate() {
        notificationTemplate.getSmsTemplate();
    }

    @Test
    public void testgetIvmTemplate() {
        notificationTemplate.getIvmTemplate();
    }

    @Test
    public void testgetPushTemplate() {
        notificationTemplate.getPushTemplate();
    }

    @Test
    public void getPortalTemplate() {
        notificationTemplate.getPortalTemplate();
    }

    @Test
    public void getChannelTemplate() {

        notificationTemplate
            .getChannelTemplate(ChannelType.MOBILE_APP_PUSH);
        notificationTemplate.getChannelTemplate(ChannelType.EMAIL);
        notificationTemplate.getChannelTemplate(ChannelType.SMS);

        notificationTemplate.getChannelTemplate(ChannelType.PORTAL);
        notificationTemplate.getChannelTemplate(ChannelType.IVM);
        notificationTemplate.getChannelTemplate(ChannelType.API_PUSH);

    }

    @Test
    public void testgetBrand() {
        notificationTemplate.getBrand();
    }

    @Test
    public void setBrand() {
        notificationTemplate.setBrand("ass");
    }

    @Test
    public void testtoString() {
        notificationTemplate.toString();
    }
}
