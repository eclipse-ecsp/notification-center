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

import org.eclipse.ecsp.notification.entities.NotificationSchedulerPayload;
import org.junit.Test;

/**
 * NotificationSchedulerPayloadTest class.
 */
public class NotificationSchedulerPayloadTest {

    NotificationSchedulerPayload notificationSchedulerPayload = new NotificationSchedulerPayload();
    NotificationSchedulerPayload notificationSchedulerPayload2 = new NotificationSchedulerPayload("test123", "test123",
        null, "test123", "test123");
    NotificationSchedulerPayload notificationSchedulerPayload3 = new NotificationSchedulerPayload("test123", "test123",
        null, "test123", true, "test123", "test123");

    @Test
    public void testgetSchedulerId() {
        notificationSchedulerPayload.getSchedulerId();
    }

    @Test
    public void testsetSchedulerId() {
        notificationSchedulerPayload.setSchedulerId("test");
    }

    @Test
    public void testisSchedulerUpdateFlag() {
        notificationSchedulerPayload.isSchedulerUpdateFlag();
    }

    @Test
    public void testsetSchedulerUpdateFlag() {
        notificationSchedulerPayload.setSchedulerUpdateFlag(false);
    }

    @Test
    public void testgetGroup() {
        notificationSchedulerPayload.getGroup();
    }

    @Test
    public void testsetGroup() {
        notificationSchedulerPayload.setGroup("");
    }

    @Test
    public void testgetUserId() {
        notificationSchedulerPayload.getUserID();
    }

    @Test
    public void testsetUserId() {
        notificationSchedulerPayload.setUserID("");
    }

    @Test
    public void testgetVehicleId() {
        notificationSchedulerPayload.getVehicleID();
    }

    @Test
    public void testsetVehicleId() {
        notificationSchedulerPayload.setVehicleID("");
    }

    @Test
    public void testgetChannelType() {
        notificationSchedulerPayload.getChannelType();
    }

    @Test
    public void testsetChannelType() {
        notificationSchedulerPayload.setChannelType(null);
    }

    @Test
    public void testgetContactId() {
        notificationSchedulerPayload.getContactId();
    }

    @Test
    public void testsetContactId() {
        notificationSchedulerPayload.setContactId("");
    }

    @Test
    public void testtoString() {
        notificationSchedulerPayload.toString();
    }
}
