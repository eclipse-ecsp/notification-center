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

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.notification.entities.AmazonPinpointEndpoint;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AmazonPinpointEndpointTest class.
 */
public class AmazonPinpointEndpointTest {

    @Test
    public void testEntity() {
        AmazonPinpointEndpoint amazonPinpointEndpoint = new AmazonPinpointEndpoint();
        amazonPinpointEndpoint.setUserId("userId");
        Map<String, String> channelEndpoints = new HashMap<>();
        channelEndpoints.put("address1", "endpoint1");
        channelEndpoints.put("address2", "endpoint2");
        channelEndpoints.put("address3", "endpoint3");
        Map<String, Map<String, String>> endpoints = new HashMap<>();
        endpoints.put(com.amazonaws.services.pinpoint.model.ChannelType.SMS.toString(), channelEndpoints);
        channelEndpoints = new HashMap<>();
        channelEndpoints.put("address11", "endpoint11");
        channelEndpoints.put("address22", "endpoint22");
        channelEndpoints.put("address33", "endpoint33");
        endpoints.put(com.amazonaws.services.pinpoint.model.ChannelType.EMAIL.toString(), channelEndpoints);
        amazonPinpointEndpoint.setEndpoints(endpoints);
        amazonPinpointEndpoint.setLastUpdatedTime(LocalDateTime.now());
        amazonPinpointEndpoint.setSchemaVersion(Version.V1_0);

        Assert.assertNotNull(amazonPinpointEndpoint);
        Assert.assertEquals("userId", amazonPinpointEndpoint.getUserId());
        Assert.assertNotNull(amazonPinpointEndpoint.getLastUpdatedTime());
        Assert.assertEquals(Version.V1_0, amazonPinpointEndpoint.getSchemaVersion());
        Assert.assertNotNull(amazonPinpointEndpoint.getEndpoints());
        Assert.assertNotNull(amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.SMS.toString()));
        Assert.assertNotNull(amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.EMAIL.toString()));
        Assert.assertEquals("endpoint1", amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.SMS.toString()).get("address1"));
        Assert.assertEquals("endpoint2", amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.SMS.toString()).get("address2"));
        Assert.assertEquals("endpoint3", amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.SMS.toString()).get("address3"));

        Assert.assertEquals("endpoint11", amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.EMAIL.toString()).get("address11"));
        Assert.assertEquals("endpoint22", amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.EMAIL.toString()).get("address22"));
        Assert.assertEquals("endpoint33", amazonPinpointEndpoint.getEndpoints()
            .get(com.amazonaws.services.pinpoint.model.ChannelType.EMAIL.toString()).get("address33"));

    }
}
