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

import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.junit.Test;

/**
 * NotificationBufferTest class.
 */
public class NotificationBufferTest {

    NotificationBuffer nb = new NotificationBuffer();
    NotificationBuffer nb1 = new NotificationBuffer(null, null, null, null, null);

    @Test
    public void testgetGroup() {
        nb.getGroup();
    }

    @Test
    public void testgetContactId() {
        nb.getContactId();
    }

    @Test
    public void testsetId() {
        nb.setId(null);
    }

    @Test
    public void testgetAlertsInfo() {
        nb.getAlertsInfo();
    }

    @Test
    public void testsetAlertsInfo() {
        nb.setAlertsInfo(null);
    }

    @Test
    public void testmethods() {
        nb.getChannelType();
        nb.getSchedulerId();
        nb.getUserId();
        nb.getVehicleId();
        nb.toString();
    }
}
