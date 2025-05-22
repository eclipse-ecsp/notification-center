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

import org.eclipse.ecsp.domain.notification.commons.NotificationFeedbackEventDataV1_0;
import org.junit.Test;

/**
 * NotificationFeedbackEventDataV1Test class.
 */
public class NotificationFeedbackEventDataV1Test {

    NotificationFeedbackEventDataV1_0 nfed = new NotificationFeedbackEventDataV1_0();

    @Test
    public void testgetterssetters() {
        nfed.addChannelFeedback(null);
        nfed.getChannelFeedbacks();
        nfed.setChannelFeedbacks(null);
        nfed.getCustomExtension();
        nfed.setCustomExtension(nfed);
        nfed.getNotificationErrorDetail();
        nfed.setNotificationErrorDetail(null);
        nfed.getNotificationStatus();
        nfed.setNotificationStatus(null);
    }
}
