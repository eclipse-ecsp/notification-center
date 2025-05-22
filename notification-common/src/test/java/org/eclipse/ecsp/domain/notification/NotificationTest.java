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

import junit.framework.Assert;
import org.junit.Test;

/**
 * NotificationTest class.
 */
public class NotificationTest {

    Notification notification = new Notification();

    @Test
    public void testgetTitle() {
        notification.getTitle();
    }

    @Test
    public void testsetTitle() {
        notification.setTitle("test123");
    }

    @Test
    public void testgetSubtitle() {
        notification.getSubtitle();
    }

    @Test
    public void testsetSubtitle() {
        notification.setSubtitle("");
    }

    @Test
    public void testgetBody() {
        notification.getBody();
    }

    @Test
    public void testsetBody() {
        notification.setBody("test");
    }

    @Test
    public void testgetClickAction() {
        notification.getClickAction();
    }

    @Test
    public void testsetClickAction() {
        notification.setClickAction("");
    }

    @Test
    public void testgetBannerTitle() {
        notification.getBannerTitle();
    }

    @Test
    public void testsetBannerTitle() {
        notification.setBannerTitle("dummy");
    }

    @Test
    public void testgetBannerDesc() {
        notification.getBannerDesc();
    }

    @Test
    public void testsetBannerDesc() {
        notification.setBannerDesc("");
    }

    @Test
    public void testNotificationErrorCode() {
        Assert.assertEquals(NotificationErrorCode.DB_ERROR, NotificationErrorCode.DB_ERROR);
        Assert.assertEquals(NotificationErrorCode.DELIVERY_CHANNEL_NOT_AVAILABLE,
            NotificationErrorCode.DELIVERY_CHANNEL_NOT_AVAILABLE);
        Assert.assertEquals(NotificationErrorCode.DELIVERY_INFO_NOT_AVAILABLE,
            NotificationErrorCode.DELIVERY_INFO_NOT_AVAILABLE);
        Assert.assertEquals(NotificationErrorCode.NOTIFICATION_ID_NOT_AVAILABLE,
            NotificationErrorCode.NOTIFICATION_ID_NOT_AVAILABLE);
        Assert.assertEquals(NotificationErrorCode.PROCESSING_ERROR, NotificationErrorCode.PROCESSING_ERROR);
        Assert.assertEquals(NotificationErrorCode.TEMPLATE_NOT_AVAILABLE, NotificationErrorCode.TEMPLATE_NOT_AVAILABLE);
        Assert.assertEquals(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED,
            NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED);
    }

    @Test
    public void testtoString() {
        notification.toString();
    }
}
