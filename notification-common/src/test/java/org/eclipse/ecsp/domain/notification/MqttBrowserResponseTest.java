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
 * MqttBrowserResponseTest class.
 */
public class MqttBrowserResponseTest {

    MQTTBrowserResponse mq = new MQTTBrowserResponse();
    MQTTBrowserResponse mq1 = new MQTTBrowserResponse("");
    MQTTBrowserResponse mq2 = new MQTTBrowserResponse("", "");
    MQTTBrowserResponse mq3 = new MQTTBrowserResponse("", "", "");


    @Test
    public void getstetest() {
        mq.getProvider();
        mq.toString();
    }
}
