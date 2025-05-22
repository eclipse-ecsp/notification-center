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

/**
 * AmazonSnsSmsResponseTest class.
 */
public class AmazonSnsSmsResponseTest {

    AmazonSNSSMSResponse amznresp = new AmazonSNSSMSResponse();

    AmazonSNSSMSResponse amznresp2 = new AmazonSNSSMSResponse("testuser", "testpdid");

    AmazonSNSSMSResponse amznresp3 = new AmazonSNSSMSResponse("testuser", "testpdid", "dummy");

    @Test
    public void testgetChannelType() {
        amznresp.getChannelType();
    }

    @Test
    public void testgetProvider() {
        amznresp.getProvider();
    }

    @Test
    public void testgetTopics() {
        amznresp.getTopics();
    }

    @Test
    public void setTopics() {
        amznresp.setTopics(null);
    }

    @Test
    public void testsetTopicAndSubscriptionsInfo() {
        amznresp.setTopicAndSubscriptionsInfo("test", "dummy", new ArrayList<Triplet<String, String, String>>());
    }

    @Test
    public void testsetDeleteTopicsInfo() {
        amznresp.setDeleteTopicsInfo("test123", "dummy123");
    }

    @Test
    public void testsetPublishInfo() {
        amznresp.setPublishInfo("test123", "dummy123");
    }

    AmazonSNSSMSResponse.TopicsInfo topicinf = new AmazonSNSSMSResponse.TopicsInfo();

    @Test
    public void testsetSubscriptionInfo() {
        topicinf.setSubscriptionInfo(new ArrayList<Triplet<String, String, String>>());
    }

    @Test
    public void testgetTopicArn() {
        topicinf.getTopicArn();
    }

    @Test
    public void testsetTopicArn() {
        topicinf.setTopicArn("test123");
    }

    @Test
    public void testgetTopicCreationId() {
        topicinf.getTopicCreationID();
    }

    @Test
    public void testsetTopicCreationId() {
        topicinf.setTopicCreationID("test");
    }

    @Test
    public void testgetSubscriptions() {
        topicinf.getSubscriptions();
    }

    @Test
    public void testsetSubscriptions() {
        topicinf.setSubscriptions(null);
    }

    @Test
    public void testgetPubishTrackingId() {
        topicinf.getPubishTrackingID();
    }

    @Test
    public void testsetPubishTrackingId() {
        topicinf.setPubishTrackingID("dummy");
    }

    @Test
    public void testgetDeleteTrackingId() {
        topicinf.getDeleteTrackingID();
    }

    @Test
    public void testsetDeleteTrackingId() {
        topicinf.setDeleteTrackingID("");
    }

    @Test
    public void testtoString() {
        topicinf.toString();
    }

    AmazonSNSSMSResponse.SubscriptionInfo subscriptionInfo = new AmazonSNSSMSResponse.SubscriptionInfo();

    @Test
    public void testgetSubscriptionTrackingId() {
        subscriptionInfo.getSubscriptionTrackingID();
    }

    @Test
    public void testsetSubscriptionTrackingId() {
        subscriptionInfo.setSubscriptionTrackingID("test");
    }

    @Test
    public void testgetProtocolName() {
        subscriptionInfo.getProtocolName();
    }

    @Test
    public void testsetProtocolName() {
        subscriptionInfo.setProtocolName("Test");
    }

    @Test
    public void testgetProtocolValue() {
        subscriptionInfo.getProtocolValue();
    }

    @Test
    public void testsetProtocolValue() {
        subscriptionInfo.setProtocolValue("test123");
    }

    @Test
    public void toStringtest() {
        subscriptionInfo.toString();

    }

}
