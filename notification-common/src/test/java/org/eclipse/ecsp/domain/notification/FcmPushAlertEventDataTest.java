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

import org.junit.Test;

/**
 * FcmPushAlertEventDataTest class.
 */
public class FcmPushAlertEventDataTest {

    FcmPushAlertEventData fpaed = new FcmPushAlertEventData();

    @Test
    public void testgetFcmResponse() {
        fpaed.getFcmResponse();
    }

    @Test
    public void testsetFcmResponse() {
        fpaed.setFcmResponse("");
    }

    @Test
    public void testgetFcmPayload() {
        fpaed.getFcmPayload();
    }

    @Test
    public void testsetFcmPayload() {
        fpaed.setFcmPayload("");
    }

    @Test
    public void testgetAppPlatform() {
        fpaed.getAppPlatform();
    }

    @Test
    public void testsetAppPlatform() {
        fpaed.setAppPlatform("");
    }
}
