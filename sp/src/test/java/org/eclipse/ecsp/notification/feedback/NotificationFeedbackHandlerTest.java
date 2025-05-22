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

package org.eclipse.ecsp.notification.feedback;

import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonSESResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.ProcessingStatus;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.FEEDBACK_KEY;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.MILESTONE_FEEDBACK_KEY;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.MILESTONE_FEEDBACK_TOPIC;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_CAMPAIGN_ID;

/**
 * NotificationFeedbackHandlerTest.
 */
public class NotificationFeedbackHandlerTest {

    private final String notificationTopic = "notification";
    private final String feedbackKeyValue = "key1";

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessor;

    /**
     * setup method.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        NotificationFeedbackHandler.init(
                streamProcessor, true, false, "notification-feedback");
    }

    @Test
    public void sendNotificationSendingFeedback_whenNoDataInAlert_thenNoFeedbackIsSent() {
        AlertsInfo alertsInfo = new AlertsInfo();
        NotificationFeedbackHandler.sendNotificationSendingFeedback(alertsInfo, null, getChannelResponseData(), "");
        Mockito.verifyNoInteractions(streamProcessor);
    }

    @Test
    public void sendNotificationSendingFeedback_whenFeedbackDontRequired_thenNoFeedbackIsSent() {
        AlertsInfo alertsInfo = getAlertsInfo("k", "v", FEEDBACK_KEY, null);
        NotificationFeedbackHandler.sendNotificationSendingFeedback(
                alertsInfo, null, getChannelResponseData(), "");
        Mockito.verifyNoInteractions(streamProcessor);
    }

    @Test
    public void sendNotificationSendingFeedback_whenChannelResponseIsNull_thenNoFeedbackIsSent() {
        AlertsInfo alertsInfo = getAlertsInfo(MILESTONE_FEEDBACK_TOPIC, notificationTopic, FEEDBACK_KEY, null);
        NotificationFeedbackHandler.sendNotificationSendingFeedback(
                alertsInfo, null, null, "");
        Mockito.verifyNoInteractions(streamProcessor);
    }

    @Test
    public void sendNotificationSendingFeedback_whenCampaignIdDoesntExist_thenFeedbackIsSent() {
        AlertsInfo alertsInfo = getAlertsInfo(MILESTONE_FEEDBACK_TOPIC, notificationTopic, FEEDBACK_KEY, null);
        IgniteEventImpl igniteEvent = createIgniteEvent();
        NotificationFeedbackHandler.sendNotificationSendingFeedback(
                alertsInfo, igniteEvent, getChannelResponseData(),
                "");
        Mockito.doNothing().when(streamProcessor)
                .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                        Mockito.anyString());
        Mockito.verify(streamProcessor, Mockito.times(1))
                .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                        Mockito.anyString());
    }

    @Test
    public void sendNotificationSendingFeedback_whenEventKeySpecified_thenFeedbackIsSent() {
        AlertsInfo alertsInfo =
                getAlertsInfo(MILESTONE_FEEDBACK_TOPIC, notificationTopic, MILESTONE_FEEDBACK_KEY, feedbackKeyValue);
        IgniteEventImpl igniteEvent = createIgniteEvent();
        NotificationFeedbackHandler.sendNotificationSendingFeedback(
                alertsInfo, igniteEvent, getChannelResponseData(),
                "");
        Mockito.doNothing().when(streamProcessor)
                .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                        Mockito.anyString());

        ArgumentCaptor<IgniteStringKey> objectCaptured = ArgumentCaptor.forClass(IgniteStringKey.class);
        Mockito.verify(streamProcessor, Mockito.times(1))
                .forwardDirectly(objectCaptured.capture(), Mockito.any(IgniteEventImpl.class), Mockito.anyString());
        Assert.assertEquals(new IgniteStringKey(feedbackKeyValue), objectCaptured.getValue());
    }

    @Test
    public void sendNotificationSendingFeedback_whenCampaignIdExists_thenFeedbackIsSent() {
        AlertsInfo alertsInfo =
            getAlertsInfo(MILESTONE_FEEDBACK_TOPIC, notificationTopic, MILESTONE_FEEDBACK_KEY, null);
        alertsInfo.getAlertsData().set(NOTIFICATION_CAMPAIGN_ID, UUID.randomUUID());
        IgniteEventImpl igniteEvent = createIgniteEvent();
        NotificationFeedbackHandler.sendNotificationSendingFeedback(alertsInfo, igniteEvent, getChannelResponseData(),
            "");
        Mockito.doNothing().when(streamProcessor)
            .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                Mockito.anyString());
        Mockito.verify(streamProcessor, Mockito.times(1))
            .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                Mockito.anyString());
    }

    @Test
    public void sendCampaignLifecycleFeedback() {
        AlertsInfo alertsInfo =
            getAlertsInfo("feedbackTopic", notificationTopic, "feedbackKey", feedbackKeyValue);
        IgniteEventImpl igniteEvent = createIgniteEvent();
        alertsInfo.setIgniteEvent(igniteEvent);
        NotificationFeedbackHandler.sendCampaignLifecycleFeedback(
                alertsInfo, igniteEvent, null, feedbackKeyValue, notificationTopic, feedbackKeyValue);
        Mockito.doNothing().when(streamProcessor)
            .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                Mockito.anyString());

        ArgumentCaptor<IgniteStringKey> objectCaptured = ArgumentCaptor.forClass(IgniteStringKey.class);
        Mockito.verify(streamProcessor, Mockito.times(1))
            .forwardDirectly(objectCaptured.capture(), Mockito.any(IgniteEventImpl.class), Mockito.anyString());
        Assert.assertEquals(new IgniteStringKey(feedbackKeyValue), objectCaptured.getValue());
    }
    
    @Test
    public void sendNotificationChannelLevelFeedback() {
        AlertsInfo alertsInfo =
                getAlertsInfo("feedbackTopic", notificationTopic, "feedbackKey", feedbackKeyValue);
        IgniteEventImpl igniteEvent = createIgniteEvent();
        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                alertsInfo, new IgniteStringKey("key1"), igniteEvent, getAlertsHistoryInfo());
        Mockito.doNothing().when(streamProcessor)
                .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                        Mockito.anyString());

        ArgumentCaptor<IgniteStringKey> objectCaptured = ArgumentCaptor.forClass(IgniteStringKey.class);
        Mockito.verify(streamProcessor, Mockito.times(1))
                .forwardDirectly(objectCaptured.capture(), Mockito.any(IgniteEventImpl.class), Mockito.anyString());
        Assert.assertEquals(new IgniteStringKey(feedbackKeyValue), objectCaptured.getValue());
    }
    
    @Test
    public void sendNotificationChannelLevelFeedbackProcessingFailure() {
        AlertsInfo alertsInfo =
                getAlertsInfo("feedbackTopic", notificationTopic, "feedbackKey", feedbackKeyValue);
        IgniteEventImpl igniteEvent = createIgniteEvent();
        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                alertsInfo, new IgniteStringKey("key1"), igniteEvent, getAlertsHistoryInfo_withProcessingFailure());
        Mockito.doNothing().when(streamProcessor)
                .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                        Mockito.anyString());

        ArgumentCaptor<IgniteStringKey> objectCaptured = ArgumentCaptor.forClass(IgniteStringKey.class);
        Mockito.verify(streamProcessor, Mockito.times(1))
                .forwardDirectly(objectCaptured.capture(), Mockito.any(IgniteEventImpl.class), Mockito.anyString());
        Assert.assertEquals(new IgniteStringKey(feedbackKeyValue), objectCaptured.getValue());
    }
    
    @Test
    public void sendNotificationChannelLevelFeedbackChannleSkipped() {
        AlertsInfo alertsInfo =
                getAlertsInfo("feedbackTopic", notificationTopic, "feedbackKey", feedbackKeyValue);
        IgniteEventImpl igniteEvent = createIgniteEvent();
        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                alertsInfo, new IgniteStringKey("key1"), igniteEvent, getAlertsHistoryInfo_withChannelSkipped());
        Mockito.doNothing().when(streamProcessor)
                .forwardDirectly(Mockito.any(IgniteStringKey.class), Mockito.any(IgniteEventImpl.class),
                        Mockito.anyString());

        ArgumentCaptor<IgniteStringKey> objectCaptured = ArgumentCaptor.forClass(IgniteStringKey.class);
        Mockito.verify(streamProcessor, Mockito.times(1))
                .forwardDirectly(objectCaptured.capture(), Mockito.any(IgniteEventImpl.class), Mockito.anyString());
        Assert.assertEquals(new IgniteStringKey(feedbackKeyValue), objectCaptured.getValue());
    }
    
    @NotNull
    private AlertsInfo getAlertsInfo(String topicKey, String topicValue, String feedbackEventKeyKey,
                                     String feedbackEventKeyValue) {
        AlertsInfo.Data alertData = new AlertsInfo.Data();
        alertData.set(topicKey, topicValue);
        alertData.set(feedbackEventKeyKey, feedbackEventKeyValue);
        alertData.set("userId", feedbackEventKeyValue);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(alertData);
        return alertsInfo;
    }

    @NotNull
    private AlertsHistoryInfo getAlertsHistoryInfo() {
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        alertsHistoryInfo.setId("Test123");
        alertsHistoryInfo.addChannelResponse(getChannelResponseData());
        alertsHistoryInfo.setGroup("xyz");
        alertsHistoryInfo.setNotificationId("abc");
        return alertsHistoryInfo;
    }

    @NotNull
    private AlertsHistoryInfo getAlertsHistoryInfo_withProcessingFailure() {
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        alertsHistoryInfo.setId("Test123");
        ProcessingStatus processingStatus = new ProcessingStatus(NotificationConstants.FAILURE, "exception");
        alertsHistoryInfo.setProcessingStatus(processingStatus);
        alertsHistoryInfo.addChannelResponse(getChannelResponseData());
        alertsHistoryInfo.setGroup("xyz");
        alertsHistoryInfo.setNotificationId("abc");
        return alertsHistoryInfo;
    }

    @NotNull
    private AlertsHistoryInfo getAlertsHistoryInfo_withChannelSkipped() {
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        alertsHistoryInfo.setId("Test123");
        ProcessingStatus processingStatus = new ProcessingStatus(NotificationConstants.SUCCESS, null);
        alertsHistoryInfo.setProcessingStatus(processingStatus);
        alertsHistoryInfo.setSkippedChannels(getskippedChannels());
        alertsHistoryInfo.setGroup("xyz");
        alertsHistoryInfo.setNotificationId("abc");
        return alertsHistoryInfo;
    }

    @NotNull
    private Map<String, String> getskippedChannels() {
        Map<String, String> skippedChannels = new HashMap<String, String>();
        skippedChannels.put("email", "exception");
        return skippedChannels;
    }
    
    @NotNull
    private ChannelResponse getChannelResponseData() {
        ChannelResponse channelResponse = new AmazonSESResponse();
        channelResponse.setStatus("SUCCESS");
        channelResponse.setDestination("email@mail.com");
        return channelResponse;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @NotNull
    private IgniteEventImpl createIgniteEvent() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId("eventId");
        igniteEvent.setVersion(Version.V1_0);
        igniteEvent.setTimestamp(1L);
        igniteEvent.setTimezone((short) 330);
        igniteEvent.setVehicleId("VehicleId");
        igniteEvent.setRequestId("RequestId");
        igniteEvent.setBizTransactionId("BizTransactionId");
        return igniteEvent;
    }
}
