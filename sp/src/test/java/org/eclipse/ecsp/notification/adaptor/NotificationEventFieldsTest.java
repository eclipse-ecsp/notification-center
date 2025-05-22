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

package org.eclipse.ecsp.notification.adaptor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * NotificationEventFieldsTest.
 */
public class NotificationEventFieldsTest {

    @Test
    public void channelTypeIsChannelSupportedTrue() {
        assertTrue(NotificationEventFields.ChannelType.isChannelSupported("sms"));
    }

    @Test
    public void channelTypeIsChannelSupportedFalse() {
        assertFalse(NotificationEventFields.ChannelType.isChannelSupported("tv"));
    }

    @Test
    public void testgetChannelsList() {
        NotificationEventFields n = NotificationEventFields.createNotificationEventFields();
        assertNotNull(n.getChannelsList());
    }

    @Test
    public void channelTypeGetChannel() {
        NotificationEventFields.ChannelType channelType = NotificationEventFields.ChannelType.getChannel("sms");
        assertNotNull(channelType);
        assertEquals("SMS", channelType.toString());
    }

    @Test
    public void channelTypeGetChannelNull() {
        assertNull(NotificationEventFields.ChannelType.getChannel("tv"));
    }

    @Test
    public void channelTypeGetFieldName() {
        assertEquals("sms", NotificationEventFields.ChannelType.SMS.getChannelTypeName());
    }

    @Test
    public void smsFieldsGetFieldName() {
        assertEquals("ph", NotificationEventFields.SmsFields.PHONE_NUM.getFieldName());
    }

    @Test
    public void devicePushFieldsGetFieldName() {
        assertEquals("token", NotificationEventFields.DevicePushFields.DEVICE_TOKEN.getFieldName());
    }

    @Test
    public void emailFieldsGetFieldName() {
        assertEquals("email", NotificationEventFields.EmailFields.EMAIL.getFieldName());
    }

    @Test
    public void portalFieldsGetFieldName() {
        assertEquals("user", NotificationEventFields.PortalFields.USER.getFieldName());
    }

    @Test
    public void getEndPointFieldNamePush() {
        assertEquals("token", NotificationEventFields.getEndPointFieldName("push"));
    }

    @Test
    public void getEndPointFieldNameSms() {
        assertEquals("ph", NotificationEventFields.getEndPointFieldName("sms"));
    }

    @Test
    public void getEndPointFieldNameEmail() {
        assertEquals("email", NotificationEventFields.getEndPointFieldName("email"));
    }

    @Test
    public void getEndPointFieldNameBrowser() {
        assertEquals("user", NotificationEventFields.getEndPointFieldName("browser"));
    }

    @Test
    public void getEndPointFieldNameIvm() {
        assertNull(NotificationEventFields.getEndPointFieldName("ivm"));
    }

    @Test
    public void getEndPointFieldNameNull() {
        assertNull(NotificationEventFields.getEndPointFieldName("tv"));
    }

    @Test
    public void getSnsType() {
        assertEquals("email", NotificationEventFields.getSnsType("email"));
    }
}