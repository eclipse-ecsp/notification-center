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

package org.eclipse.ecsp.domain.notification.grouping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * NotificationGroupingSerDeTest class.
 */
public class NotificationGroupingSerDeTest {

    @Test
    public void testSerDe() throws IOException {
        NotificationGrouping original = new NotificationGrouping();
        original.setNotificationId("GeoFenceIn");
        original.setSchemaVersion(Version.V1_0);
        original.setGroup("ParentalControls");
        ObjectMapper om = new ObjectMapper();
        String ng = om.writeValueAsString(original);
        NotificationGrouping reborn = om.readValue(ng, NotificationGrouping.class);
        Assert.assertEquals(original, reborn);
        Assert.assertEquals(original.getNotificationId(), reborn.getNotificationId());
        Assert.assertEquals(original.getSchemaVersion(), reborn.getSchemaVersion());
        Assert.assertEquals(original.getGroup(), reborn.getGroup());
    }
}
