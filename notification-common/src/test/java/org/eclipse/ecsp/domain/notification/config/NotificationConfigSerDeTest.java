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

package org.eclipse.ecsp.domain.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NotificationConfigSerDeTest class.
 */
public class NotificationConfigSerDeTest {

    @Test
    public void testSerDe() throws IOException {
        List<Channel> channels = new ArrayList<Channel>();
        SmsChannel ch = new SmsChannel();
        ch.setPhones(Arrays.asList("1243434894"));
        ch.setEnabled(true);
        channels.add(ch);
        NotificationConfig original = new NotificationConfig();

        original.setChannels(channels);
        original.setSchemaVersion(Version.V1_0);
        original.setGroup("ParentalControls");
        original.setUserId("noname001");
        original.setVehicleId("HJDSHDUHSG73DHJ");
        original.setEnabled(true);
        ObjectMapper om = new ObjectMapper();
        String ng = om.writeValueAsString(original);
        NotificationConfig reborn = om.readValue(ng, NotificationConfig.class);
        Assert.assertEquals(original, reborn);
        Assert.assertTrue(NotificationConfig.isSimilar(original, reborn));
        Assert.assertEquals(original.getChannels(), reborn.getChannels());
        Assert.assertEquals(original.getSchemaVersion(), reborn.getSchemaVersion());
    }
}
