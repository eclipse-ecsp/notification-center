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
 * NotificationRetryEventDataTest class.
 */
public class NotificationRetryEventDataTest {

    NotificationRetryEventData nred = new NotificationRetryEventData();
    NotificationRetryEventDataV2 retryEventData = new NotificationRetryEventDataV2();

    @Test
    public void testgetsetters() {
        nred.getCustomExtension();
        nred.setCustomExtension(null);
        nred.getOriginalEvent();
        nred.setOriginalEvent(null);
        nred.getOriginalEventTopic();
        nred.setOriginalEventTopic(null);
        nred.getRetryRecord();
        nred.setRetryRecord(null);
        nred.toString();

        retryEventData.getCustomExtension();
        retryEventData.setCustomExtension(null);
        retryEventData.getOriginalEvent();
        retryEventData.setOriginalEvent(null);
        retryEventData.getOriginalEventTopic();
        retryEventData.setOriginalEventTopic(null);
        retryEventData.getRetryRecord();
        retryEventData.setRetryRecord(null);
        retryEventData.toString();

    }

}
