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

import java.util.ArrayList;
import java.util.List;

/**
 * PortalChannelTest class.
 */
public class PortalChannelTest {

    PortalChannel portalChannel = new PortalChannel();

    List<String> mqttTopics = new ArrayList<String>();

    PortalChannel portalChannel2 = new PortalChannel(mqttTopics);

    @Test
    public void testgetMqttTopics() {
        portalChannel.getMqttTopics();
    }

    @Test
    public void testsetMqttTopics() {
        portalChannel.setMqttTopics(null);
    }

    @Test
    public void testgetChannelType() {
        portalChannel.getChannelType();
    }

    @Test
    public void testtoString() {
        portalChannel.toString();
    }

    @Test
    public void testdiff() {
        portalChannel.diff(portalChannel, null, null);
    }

    @Test
    public void testrequiresSetup() {
        portalChannel.requiresSetup();
    }

    @Test
    public void testflatten() {
        portalChannel.flatten();
    }

    @Test
    public void testshallowClone() {
        portalChannel.shallowClone();
    }

}
