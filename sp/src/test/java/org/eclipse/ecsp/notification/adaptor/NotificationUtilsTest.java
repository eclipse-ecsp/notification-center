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

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * NotificationUtilsTest.
 */
public class NotificationUtilsTest {

    @Test
    public void isNotificationSettingsRequestTrue() {
        assertTrue(NotificationUtils.isNotificationSettingsRequest("{\"EventID\":\"NotificationSettings\"}"));
    }

    @Test
    public void isNotificationSettingsRequestFalse() {
        assertFalse(NotificationUtils.isNotificationSettingsRequest("{\"EventID\":\"E1\"}"));
    }

    @Test
    public void isNotificationSettingsRequestFalseNoEventId() {
        assertFalse(NotificationUtils.isNotificationSettingsRequest("{}"));
    }

    @Test
    public void isUserToPdidAssociationRequestTrue() {
        assertTrue(NotificationUtils.isUserToPdidAssociationRequest("{\"EventID\":\"VehicleAssociation\"}"));
    }

    @Test
    public void isUserToPdidAssociationRequestFalse() {
        assertFalse(NotificationUtils.isUserToPdidAssociationRequest("{\"EventID\":\"E1\"}"));
    }

    @Test
    public void isUserToPdidAssociationRequestFalseNoEventId() {
        assertFalse(NotificationUtils.isUserToPdidAssociationRequest("{}"));
    }

    @Test
    public void isUserDissociationRequestTrue() {
        assertTrue(NotificationUtils.isUserDissociationRequest("{\"EventID\":\"VehicleDisAssociation\"}"));
    }

    @Test
    public void isUserDissociationRequestFalse() {
        assertFalse(NotificationUtils.isUserDissociationRequest("{\"EventID\":\"E1\"}"));
    }

    @Test
    public void isUserDissociationRequestFalseNoEventId() {
        assertFalse(NotificationUtils.isUserDissociationRequest("{}"));
    }

    @Test
    public void getUserId() {
        assertEquals("abc", NotificationUtils.getUserId("{\"Data\":{\"userId\":\"abc\"}}"));
    }

    @Test
    public void bindDataSuccess() {
        assertNotNull(NotificationUtils.bindData("{\"Data\":{\"userId\":\"abc\"}}", Map.class));
    }

    @Test
    public void bindDataNull() {
        assertNull(NotificationUtils.bindData("{\"Data\":userId\":\"abc\"}}", Map.class));
    }

    @Test
    public void getListObjectsSuccess() {
        assertNotNull(NotificationUtils.getListObjects("[{\"Data\":{\"userId\":\"abc\"}}]", Map.class));
    }

    @Test
    public void getListObjectsNull() {
        assertTrue(NotificationUtils.getListObjects("{\"Data\":userId\":\"abc\"}}", Map.class).isEmpty());
    }
}