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

import org.eclipse.ecsp.notification.entities.CloneNotificationConfig;
import org.junit.Test;

/**
 * CloneNotificationConfigTest class.
 */
public class CloneNotificationConfigTest {

    CloneNotificationConfig cloneNotificationConfig = new CloneNotificationConfig();

    CloneNotificationConfig cloneNotificationConfig2 = new CloneNotificationConfig("sdasd", "3123", null, "dsadsa");

    @Test
    public void testgetLocale() {
        cloneNotificationConfig.getLocale();
    }

    @Test
    public void testsetLocale() {
        cloneNotificationConfig.setLocale("en-US");
    }

    @Test
    public void testgetId() {
        cloneNotificationConfig.getId();
    }

    @Test
    public void testsetId() {
        cloneNotificationConfig.setId("");
    }

    @Test
    public void testgetUserId() {
        cloneNotificationConfig.getUserId();
    }

    @Test
    public void testsetUserId() {
        cloneNotificationConfig.setUserId("");
    }

    @Test
    public void testgetVehicleId() {
        cloneNotificationConfig.getVehicleId();
    }

    @Test
    public void testsetVehicleId() {
        cloneNotificationConfig.setVehicleId("");
    }

    @Test
    public void testgetContactId() {
        cloneNotificationConfig.getContactId();
    }

    @Test
    public void testsetContactId() {
        cloneNotificationConfig.setContactId("");
    }

    public void testgetEmail() {
        cloneNotificationConfig.getEmail();
    }

    @Test
    public void testsetEmail() {
        cloneNotificationConfig.setEmail("");
    }

    @Test
    public void testgetPhoneNumber() {
        cloneNotificationConfig.getPhoneNumber();
    }

    @Test
    public void testsetPhoneNumber() {
        cloneNotificationConfig.setPhoneNumber("");
    }

    @Test
    public void testgetGroup() {
        cloneNotificationConfig.getGroup();
    }

    @Test
    public void testsetGroup() {
        cloneNotificationConfig.setGroup("");
    }

    @Test
    public void testisEnabled() {
        cloneNotificationConfig.isEnabled();
    }

    @Test
    public void testsetEnabled() {
        cloneNotificationConfig.setEnabled(false);
    }

    @Test
    public void testgetChannels() {
        cloneNotificationConfig.getChannels();
    }

    @Test
    public void testsetChannels() {
        cloneNotificationConfig.setChannels(null);
    }

    @Test
    public void testtoString() {
        cloneNotificationConfig.toString();
    }
}
