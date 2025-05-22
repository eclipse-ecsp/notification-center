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

package org.eclipse.ecsp.notification.aws.ses;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * AmazonBounceNotificationTest.
 */
public class AmazonBounceNotificationTest {
    private AmazonBounceNotification amazonBounceNotification;
    private AmazonBounceNotification.BounceMessage bounceMessage;
    private AmazonBounceNotification.Bounce bounce;
    private AmazonBounceNotification.BouncedRecipient bouncedRecipient;

    @Before
    public void setup() {
        initMocks(this);
        amazonBounceNotification = new AmazonBounceNotification();
    }

    @Test
    public void setGetType_success() {
        amazonBounceNotification.setType("dummy_type");
        String typeResult = amazonBounceNotification.getType();
        assertEquals("dummy_type", typeResult);
    }

    @Test
    public void setGetMessageId_success() {
        amazonBounceNotification.setMessageId("dummy_messageId");
        String messageIdResult = amazonBounceNotification.getMessageId();
        assertEquals("dummy_messageId", messageIdResult);
    }

    @Test
    public void setGetTopicArn_success() {
        amazonBounceNotification.setTopicArn("dummy_topicArn");
        String topicArnResult = amazonBounceNotification.getTopicArn();
        assertEquals("dummy_topicArn", topicArnResult);
    }

    @Test
    public void setGetBounceMessage_success() {
        amazonBounceNotification.setBounceMessage("dummy_bounceMessage");
        String bounceMessage = amazonBounceNotification.getBounceMessage();
        assertEquals("dummy_bounceMessage", bounceMessage);
    }

    @Test
    public void setNotificationType_success() {
        bounceMessage = new AmazonBounceNotification.BounceMessage();
        bounceMessage.setNotificationType("dummy_notificationType");
        assertEquals("dummy_notificationType", bounceMessage.getNotificationType());
    }

    @Test
    public void setGetBounce_success() {
        bounceMessage = new AmazonBounceNotification.BounceMessage();
        bounceMessage.setBounce(bounce);
        assertEquals(bounce, bounceMessage.getBounce());
    }

    @Test
    public void setGetBounceType_success() {
        bounce = new AmazonBounceNotification.Bounce();
        bounce.setBounceType("dummy_bounceType");
        assertEquals("dummy_bounceType", bounce.getBounceType());
    }

    @Test
    public void setGetBounceSubType_success() {
        bounce = new AmazonBounceNotification.Bounce();
        bounce.setBounceSubType("dummy_bounceSubType");
        assertEquals("dummy_bounceSubType", bounce.getBounceSubType());
    }

    @Test
    public void setGetBouncedRecipients_success() {
        bounce = new AmazonBounceNotification.Bounce();
        List<AmazonBounceNotification.BouncedRecipient> bouncedRecipients = new ArrayList<>();
        AmazonBounceNotification.BouncedRecipient bouncedRecipient1 = new AmazonBounceNotification.BouncedRecipient();
        AmazonBounceNotification.BouncedRecipient bouncedRecipient2 = new AmazonBounceNotification.BouncedRecipient();
        bouncedRecipients.add(bouncedRecipient1);
        bouncedRecipients.add(bouncedRecipient2);
        bounce.setBouncedRecipients(bouncedRecipients);
        assertEquals(bouncedRecipients, bounce.getBouncedRecipients());
    }

    @Test
    public void setGetBouncedRecipient_success() {
        bouncedRecipient = new AmazonBounceNotification.BouncedRecipient();
        bouncedRecipient.setEmailAddress("dummy_emailAddress@gmail.com");
        assertEquals("dummy_emailAddress@gmail.com", bouncedRecipient.getEmailAddress());
    }

    @Test
    public void setGetAction_success() {
        bouncedRecipient = new AmazonBounceNotification.BouncedRecipient();
        bouncedRecipient.setAction("dummy_action");
        assertEquals("dummy_action", bouncedRecipient.getAction());
    }

    @Test
    public void setGetStatus_success() {
        bouncedRecipient = new AmazonBounceNotification.BouncedRecipient();
        bouncedRecipient.setStatus("dummy_status");
        assertEquals("dummy_status", bouncedRecipient.getStatus());
    }

    @Test
    public void setGetDiagnosticCode_success() {
        bouncedRecipient = new AmazonBounceNotification.BouncedRecipient();
        bouncedRecipient.setDiagnosticCode("dummy_diagnosticCode");
        assertEquals("dummy_diagnosticCode", bouncedRecipient.getDiagnosticCode());
    }

    @Test
    public void toString_success() {
        bouncedRecipient = new AmazonBounceNotification.BouncedRecipient();
        String stringResult = bouncedRecipient.toString();
        assertEquals("BouncedRecipient [emailAddress=null, action=null, status=null, diagnosticCode=null]",
            stringResult);
    }

}
