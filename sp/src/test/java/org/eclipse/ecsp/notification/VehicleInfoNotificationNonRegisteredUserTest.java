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

package org.eclipse.ecsp.notification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonSESResponse;
import org.eclipse.ecsp.domain.notification.AmazonSNSPushResponse;
import org.eclipse.ecsp.domain.notification.AmazonSNSSMSResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.MQTTBrowserResponse;
import org.eclipse.ecsp.domain.notification.NonRegisteredUserData;
import org.eclipse.ecsp.domain.notification.NotificationNonRegisteredUser;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.processors.AlertProcessorChain;
import org.eclipse.ecsp.notification.processors.ContentTransformersManagerProcessor;
import org.eclipse.ecsp.notification.processors.NotificationMsgGenerator;
import org.eclipse.ecsp.notification.processors.NotificationProcessor;
import org.eclipse.ecsp.notification.processors.NotificationTemplateFinder;
import org.eclipse.ecsp.notification.utils.MemoryAppender;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * VehicleInfoNotificationNonRegisteredUserTest.
 */
@SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:MagicNumber"})
public class VehicleInfoNotificationNonRegisteredUserTest {

    private VehicleInfoNotificationNonRegisteredUser vehicleInfoNotificationNonRegisteredUser;

    @Mock
    private ChannelNotifierRegistry channelNotifierRegistry;

    @Mock
    NotificationTemplateFinder notificationTemplateFinder;

    @Mock
    NotificationMsgGenerator notificationMsgGenerator;

    @Mock
    ContentTransformersManagerProcessor contentTransformersManagerProcessor;

    @Mock
    private AlertsHistoryAssistant alertsHistoryAssistant;

    @Captor
    ArgumentCaptor<AlertsHistoryInfo> alertsHistoryInfoArgumentCaptor;

    @Captor
    ArgumentCaptor<AlertsInfo> alertsInfoArgumentCaptor;

    private MemoryAppender memoryAppender;

    private final ObjectMapper mapper = new ObjectMapper();

    ChannelNotifier mySmsChannelNotifier = new ChannelNotifier() {
        @Override
        public ChannelResponse publish(AlertsInfo alert) {
            AmazonSNSSMSResponse amazonSnsSmsResponse = new AmazonSNSSMSResponse();
            amazonSnsSmsResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
            return amazonSnsSmsResponse;
        }


        @Override
        public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
            return null;
        }

        @Override
        public ChannelResponse destroyChannel(String userId, String eventData) {
            return null;
        }

        @Override
        public void init(Properties notificationProps, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        }

        @Override
        public String getProtocol() {
            return null;
        }


        @Override
        public String getServiceProviderName() {
            return null;
        }

    };

    ChannelNotifier myPushChannelNotifier = new ChannelNotifier() {
        @Override
        public ChannelResponse publish(AlertsInfo alert) {
            if (alert.getBenchMode() == 5) {
                throw new RuntimeException("RT exception");
            }
            AmazonSNSPushResponse amazonSnsPushResponse = new AmazonSNSPushResponse();
            amazonSnsPushResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
            return amazonSnsPushResponse;
        }


        @Override
        public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
            return null;
        }


        @Override
        public ChannelResponse destroyChannel(String userID, String eventData) {
            return null;
        }

        @Override
        public void init(Properties notificationProps, MetricRegistry metricRegistry, NotificationDao notificationDAO) {
        }

        @Override
        public String getProtocol() {
            return null;
        }


        @Override
        public String getServiceProviderName() {
            return null;
        }


    };

    ChannelNotifier myPortalChannelNotifier = new ChannelNotifier() {
        @Override
        public ChannelResponse publish(AlertsInfo alert) {
            MQTTBrowserResponse mqttBrowserResponse = new MQTTBrowserResponse();
            mqttBrowserResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
            return mqttBrowserResponse;
        }


        @Override
        public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
            return null;
        }

        @Override
        public ChannelResponse destroyChannel(String userID, String eventData) {
            return null;
        }

        @Override
        public void init(Properties notificationProps, MetricRegistry metricRegistry, NotificationDao notificationDAO) {
        }

        @Override
        public String getProtocol() {
            return null;
        }


        @Override
        public String getServiceProviderName() {
            return null;
        }


    };

    ChannelNotifier myEmailChannelNotifier = new ChannelNotifier() {
        @Override
        public ChannelResponse publish(AlertsInfo alert) {
            AmazonSESResponse sesResponse = new AmazonSESResponse();
            sesResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
            return sesResponse;
        }


        @Override
        public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
            return null;
        }

        @Override
        public ChannelResponse destroyChannel(String userID, String eventData) {
            return null;
        }

        @Override
        public void init(Properties notificationProps, MetricRegistry metricRegistry, NotificationDao notificationDAO) {
        }

        @Override
        public String getProtocol() {
            return null;
        }


        @Override
        public String getServiceProviderName() {
            return null;
        }


    };

    /**
     * test set up.
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        List<NotificationProcessor> alertProcessorChainUnregisteredUsers =
            Arrays.asList(notificationTemplateFinder, notificationMsgGenerator, contentTransformersManagerProcessor);
        vehicleInfoNotificationNonRegisteredUser =
            new VehicleInfoNotificationNonRegisteredUser(new AlertProcessorChain(alertProcessorChainUnregisteredUsers),
                alertsHistoryAssistant);

        ReflectionTestUtils.setField(vehicleInfoNotificationNonRegisteredUser,
            "maxNonRegisteredRecipientsPerRequestSoftLimit", 100);

        Logger logger = (Logger) LoggerFactory.getLogger(VehicleInfoNotificationNonRegisteredUser.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    public void processNonRegisterUserEventNoEventData() {

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        Set<ChannelType> channelsSupported = getChannelsSupported();

        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        verify(alertsHistoryAssistant, times(
                0)).setEnrichedAlertHistory(any(), any());
    }

    @Test
    public void processNonRegisterUserEventEmptyRecipients() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        ((GenericEventData) igniteEvent.getEventData()).set("recipients", new ArrayList<NonRegisteredUserData>());
        Set<ChannelType> channelsSupported = getChannelsSupported();

        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        verify(alertsHistoryAssistant,
                times(0)).setEnrichedAlertHistory(any(), any());
    }

    @Test
    public void processNonRegisterUserEventInvalidJsonData() throws Exception {
        memoryAppender.reset();
        IgniteEventImpl igniteEvent = getIgniteEvent();
        ((GenericEventData) igniteEvent.getEventData()).set("recipients", "string");
        Set<ChannelType> channelsSupported = getChannelsSupported();

        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        assertEquals(1,
                memoryAppender.search("Failed deserialization of non-registered users event", Level.ERROR).size());
        verify(alertsHistoryAssistant,
                times(0)).setEnrichedAlertHistory(any(), any());
    }

    @Test
    public void processNonRegisterUserEventExceedMaxRecipients() throws Exception {
        ReflectionTestUtils.setField(vehicleInfoNotificationNonRegisteredUser,
                "maxNonRegisteredRecipientsPerRequestSoftLimit", 1);
        memoryAppender.reset();
        IgniteEventImpl igniteEvent = getIgniteEvent();
        Set<ChannelType> channelsSupported = getChannelsSupported();
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(
                alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        assertEquals(1,
                memoryAppender.search("Received {} recipients. Max recipients allowed in one request is {}", Level.WARN)
                        .size());
        verify(alertsHistoryAssistant, times(
                1)).setEnrichedAlertHistory(any(), any());
    }

    @Test
    public void processNonRegisterUserEventSuccess() throws Exception {

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.SMS, "LowFuel", "");
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.MOBILE_APP_PUSH, "LowFuel", "");
        IgniteEventImpl igniteEvent = getIgniteEvent();
        Set<ChannelType> channelsSupported = getChannelsSupported();
        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        verify(alertsHistoryAssistant, times(
                1)).setEnrichedAlertHistory(any(), any());
        AlertsHistoryInfo returnedAlertsHistoryInfo = alertsHistoryInfoArgumentCaptor.getValue();
        assertEquals(3, returnedAlertsHistoryInfo.getChannelResponses().size());
        assertEquals(NOTIFICATION_STATUS_SUCCESS,
                returnedAlertsHistoryInfo.getChannelResponses().get(0).getStatus());
        assertEquals(ChannelType.MOBILE_APP_PUSH,
                returnedAlertsHistoryInfo.getChannelResponses().get(0).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS,
                returnedAlertsHistoryInfo.getChannelResponses().get(1).getStatus());
        assertEquals(ChannelType.SMS,
                returnedAlertsHistoryInfo.getChannelResponses().get(1).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS,
                returnedAlertsHistoryInfo.getChannelResponses().get(2).getStatus());
        assertEquals(ChannelType.SMS,
                returnedAlertsHistoryInfo.getChannelResponses().get(2).getChannelType());
        assertEquals(2, returnedAlertsHistoryInfo.getStatusHistoryRecordList().size());
        assertEquals(AlertsHistoryInfo.Status.READY,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(0).getStatus());
        assertEquals(AlertsHistoryInfo.Status.DONE,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(1).getStatus());
    }

    @Test
    public void processNonRegisterUserEventProcessorFailure() throws Exception {
        memoryAppender.reset();


        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.SMS);
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.MOBILE_APP_PUSH);
        doThrow(new RuntimeException("Runtime exception")).doNothing().when(notificationTemplateFinder).process(any());
        IgniteEventImpl igniteEvent = getIgniteEvent();
        Set<ChannelType> channelsSupported = getChannelsSupported();
        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        verify(alertsHistoryAssistant, times(1)).setEnrichedAlertHistory(any(), any());
        verify(alertsHistoryAssistant, times(2)).saveAlertHistory(any(), any(), any(),
                alertsHistoryInfoArgumentCaptor.capture());
        AlertsHistoryInfo returnedAlertsHistoryInfo = alertsHistoryInfoArgumentCaptor.getValue();
        assertEquals(3, returnedAlertsHistoryInfo.getStatusHistoryRecordList().size());
        assertEquals(AlertsHistoryInfo.Status.READY,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(0).getStatus());
        assertEquals(AlertsHistoryInfo.Status.FAILED,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(1).getStatus());
        assertEquals(AlertsHistoryInfo.Status.DONE,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(2).getStatus());
        assertEquals(1,
                memoryAppender.search("Processing alert for non registered users failed: {}", Level.ERROR).size());
    }

    @Test
    public void processNonRegisterUserEventNonSupportedChannel() throws Exception {
        memoryAppender.reset();

        Set<ChannelType> channelsSupported = getChannelsSupported();
        channelsSupported.remove(ChannelType.MOBILE_APP_PUSH);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.SMS, "LowFuel", "");
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.MOBILE_APP_PUSH, "LowFuel", "");
        IgniteEventImpl igniteEvent = getIgniteEvent();
        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        assertEquals(1,
                memoryAppender.search("{} channel is not Supported. Alert will not be sent", Level.DEBUG).size());
        verify(alertsHistoryAssistant, times(1)).setEnrichedAlertHistory(any(), any());
        AlertsHistoryInfo returnedAlertsHistoryInfo = alertsHistoryInfoArgumentCaptor.getValue();
        assertEquals(2, returnedAlertsHistoryInfo.getChannelResponses().size());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(0).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(0).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getChannelType());
        assertEquals(2, returnedAlertsHistoryInfo.getStatusHistoryRecordList().size());
        assertEquals(AlertsHistoryInfo.Status.READY,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(0).getStatus());
        assertEquals(AlertsHistoryInfo.Status.DONE,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(1).getStatus());
    }

    @Test
    public void processNonRegisterUserEventWithPortal() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        memoryAppender.reset();
        IgniteEventImpl igniteEvent = getIgniteEvent();
        String data = mapper.writeValueAsString(igniteEvent.getEventData());
        NotificationNonRegisteredUser notificationNonRegisteredUser =
                mapper.readValue(data, NotificationNonRegisteredUser.class);
        List<NonRegisteredUserData> recipients = notificationNonRegisteredUser.getRecipients();
        recipients.get(1).setPortal("portal");
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", notificationNonRegisteredUser.getNotificationId());
        genericEventData.set("version", notificationNonRegisteredUser.getVersion());
        genericEventData.set("recipients", notificationNonRegisteredUser.getRecipients());
        igniteEvent.setEventData(genericEventData);
        Set<ChannelType> channelsSupported = getChannelsSupported();
        channelsSupported.add(ChannelType.PORTAL);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.SMS, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.MOBILE_APP_PUSH, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myPortalChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.PORTAL, notificationNonRegisteredUser.getNotificationId(), "");

        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        verify(alertsHistoryAssistant, times(1)).setEnrichedAlertHistory(any(), any());
        assertEquals(1, memoryAppender.search("mqtt topic from api: {}", Level.INFO).size());
        AlertsHistoryInfo returnedAlertsHistoryInfo = alertsHistoryInfoArgumentCaptor.getValue();
        assertEquals(4, returnedAlertsHistoryInfo.getChannelResponses().size());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(0).getStatus());
        assertEquals(ChannelType.MOBILE_APP_PUSH,
                returnedAlertsHistoryInfo.getChannelResponses().get(0).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(2).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(2).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(3).getStatus());
        assertEquals(ChannelType.PORTAL, returnedAlertsHistoryInfo.getChannelResponses().get(3).getChannelType());
        assertEquals(2, returnedAlertsHistoryInfo.getStatusHistoryRecordList().size());
        assertEquals(AlertsHistoryInfo.Status.READY,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(0).getStatus());
        assertEquals(AlertsHistoryInfo.Status.DONE,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(1).getStatus());
    }

    @Test
    public void processNonRegisterUserEventSendAlertException() throws Exception {
        memoryAppender.reset();


        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.SMS, "LowFuel", "");
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.MOBILE_APP_PUSH, "LowFuel", "");
        Mockito.doAnswer((Answer<AlertsInfo>) invocation -> {
            Object[] args = invocation.getArguments();
            AlertsInfo alertsInfo = (AlertsInfo) args[0];
            alertsInfo.setBenchMode(5);
            return alertsInfo;
        }).when(contentTransformersManagerProcessor).process(any());

        IgniteEventImpl igniteEvent = getIgniteEvent();
        Set<ChannelType> channelsSupported = getChannelsSupported();

        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        assertEquals(1, memoryAppender.search(
                "Exception occurred while publishing alerts {} to"
                        + " channel type {}. Continuing publishing to other channels",
                Level.ERROR).size());
        verify(alertsHistoryAssistant, times(1)).setEnrichedAlertHistory(any(), any());
        AlertsHistoryInfo returnedAlertsHistoryInfo = alertsHistoryInfoArgumentCaptor.getValue();
        assertEquals(2, returnedAlertsHistoryInfo.getChannelResponses().size());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(0).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(0).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getChannelType());
        assertEquals(2, returnedAlertsHistoryInfo.getStatusHistoryRecordList().size());
        assertEquals(AlertsHistoryInfo.Status.READY,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(0).getStatus());
        assertEquals(AlertsHistoryInfo.Status.DONE,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(1).getStatus());
    }

    @Test
    public void processNonRegisterUserEventWithEmptyPortalAndPush() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        memoryAppender.reset();
        IgniteEventImpl igniteEvent = getIgniteEvent();
        String data = mapper.writeValueAsString(igniteEvent.getEventData());
        NotificationNonRegisteredUser notificationNonRegisteredUser =
                mapper.readValue(data, NotificationNonRegisteredUser.class);
        List<NonRegisteredUserData> recipients = notificationNonRegisteredUser.getRecipients();
        recipients.get(1).setPortal("");
        recipients.get(0).setPush(new HashMap<>());
        recipients.get(0).setEmail("a@b.com");
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", notificationNonRegisteredUser.getNotificationId());
        genericEventData.set("version", notificationNonRegisteredUser.getVersion());
        genericEventData.set("recipients", notificationNonRegisteredUser.getRecipients());
        igniteEvent.setEventData(genericEventData);
        Set<ChannelType> channelsSupported = getChannelsSupported();
        channelsSupported.add(ChannelType.PORTAL);
        channelsSupported.add(ChannelType.EMAIL);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.SMS, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.MOBILE_APP_PUSH, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myPortalChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.PORTAL, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myEmailChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.EMAIL, notificationNonRegisteredUser.getNotificationId(), "");


        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        verify(alertsHistoryAssistant, times(1)).setEnrichedAlertHistory(any(), any());
        assertEquals(0, memoryAppender.search("mqtt topic from api: {}", Level.INFO).size());
        AlertsHistoryInfo returnedAlertsHistoryInfo = alertsHistoryInfoArgumentCaptor.getValue();
        assertEquals(3, returnedAlertsHistoryInfo.getChannelResponses().size());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(0).getStatus());
        assertEquals(ChannelType.EMAIL, returnedAlertsHistoryInfo.getChannelResponses().get(0).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(2).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(2).getChannelType());
        assertEquals(2, returnedAlertsHistoryInfo.getStatusHistoryRecordList().size());
        assertEquals(AlertsHistoryInfo.Status.READY,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(0).getStatus());
        assertEquals(AlertsHistoryInfo.Status.DONE,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(1).getStatus());
    }

    @Test
    public void processNonRegisterUserEventWithEmptyEmailAndSms() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        memoryAppender.reset();
        IgniteEventImpl igniteEvent = getIgniteEvent();
        String data = mapper.writeValueAsString(igniteEvent.getEventData());
        NotificationNonRegisteredUser notificationNonRegisteredUser =
                mapper.readValue(data, NotificationNonRegisteredUser.class);
        List<NonRegisteredUserData> recipients = notificationNonRegisteredUser.getRecipients();
        recipients.get(1).setEmail("");
        recipients.get(0).setSms("");
        recipients.get(0).setLocale(null);
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", notificationNonRegisteredUser.getNotificationId());
        genericEventData.set("version", notificationNonRegisteredUser.getVersion());
        genericEventData.set("recipients", notificationNonRegisteredUser.getRecipients());
        igniteEvent.setEventData(genericEventData);
        Set<ChannelType> channelsSupported = getChannelsSupported();
        channelsSupported.add(ChannelType.EMAIL);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.SMS, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.MOBILE_APP_PUSH, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myPortalChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.PORTAL, notificationNonRegisteredUser.getNotificationId(), "");
        doReturn(myEmailChannelNotifier).when(channelNotifierRegistry)
                .channelNotifier(ChannelType.EMAIL, notificationNonRegisteredUser.getNotificationId(), "");

        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        verify(alertsHistoryAssistant, times(1)).setEnrichedAlertHistory(any(), any());
        assertEquals(0, memoryAppender.search("mqtt topic from api: {}", Level.INFO).size());
        AlertsHistoryInfo returnedAlertsHistoryInfo = alertsHistoryInfoArgumentCaptor.getValue();
        assertEquals(2, returnedAlertsHistoryInfo.getChannelResponses().size());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(0).getStatus());
        assertEquals(ChannelType.MOBILE_APP_PUSH,
                returnedAlertsHistoryInfo.getChannelResponses().get(0).getChannelType());
        assertEquals(NOTIFICATION_STATUS_SUCCESS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getStatus());
        assertEquals(ChannelType.SMS, returnedAlertsHistoryInfo.getChannelResponses().get(1).getChannelType());
        assertEquals(2, returnedAlertsHistoryInfo.getStatusHistoryRecordList().size());
        assertEquals(AlertsHistoryInfo.Status.READY,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(0).getStatus());
        assertEquals(AlertsHistoryInfo.Status.DONE,
                returnedAlertsHistoryInfo.getStatusHistoryRecordList().get(1).getStatus());
    }

    @Test
    public void processNonRegisterUserEventWithUserAndVehicle() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        memoryAppender.reset();

        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName("testName");
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userProfile", userProfile);
        Map<String, Object> map = new HashMap<>();
        map.put("make", "kia");
        dataMap.put("vehicleProfile", map);

        IgniteEventImpl igniteEvent = getIgniteEvent();
        String data = mapper.writeValueAsString(igniteEvent.getEventData());
        NotificationNonRegisteredUser notificationNonRegisteredUser =
                mapper.readValue(data, NotificationNonRegisteredUser.class);

        List<NonRegisteredUserData> recipients = notificationNonRegisteredUser.getRecipients();
        recipients.get(1).setData(dataMap);
        recipients.get(1).setBrand(null);
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", notificationNonRegisteredUser.getNotificationId());
        genericEventData.set("version", notificationNonRegisteredUser.getVersion());
        genericEventData.set("recipients", notificationNonRegisteredUser.getRecipients());
        igniteEvent.setEventData(genericEventData);
        Set<ChannelType> channelsSupported = getChannelsSupported();
        channelsSupported.add(ChannelType.PORTAL);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        doReturn(alertsHistoryInfo).when(alertsHistoryAssistant).setEnrichedAlertHistory(any(), any());
        doNothing().when(alertsHistoryAssistant)
                .saveAlertHistory(any(), any(), any(), alertsHistoryInfoArgumentCaptor.capture());
        doReturn(mySmsChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.SMS);
        doReturn(myPushChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.MOBILE_APP_PUSH);
        doReturn(myPortalChannelNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.PORTAL);
        doNothing().when(notificationTemplateFinder).process(alertsInfoArgumentCaptor.capture());

        vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(
                new IgniteStringKey("dummy"), igniteEvent, channelsSupported,
                channelNotifierRegistry, "en-US");

        AlertsInfo alertsInfo = alertsInfoArgumentCaptor.getValue();
        assertEquals("testName", alertsInfo.getAlertsData().getUserProfile().getFirstName());
        assertEquals("kia", alertsInfo.getAlertsData().getVehicleProfile().getVehicleAttributeByName("make"));
    }

    @NotNull
    private IgniteEventImpl getIgniteEvent() throws IOException {
        String requestData = IOUtils.toString(Objects.requireNonNull(
                VehicleInfoNotificationNonRegisteredUserTest.class.getResourceAsStream("/non-register-user-data.json")),
            StandardCharsets.UTF_8);
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);


        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", notificationNonRegisteredUser.getNotificationId());
        genericEventData.set("version", notificationNonRegisteredUser.getVersion());
        genericEventData.set("recipients", notificationNonRegisteredUser.getRecipients());

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(genericEventData);
        return igniteEvent;
    }

    @NotNull
    private Set<ChannelType> getChannelsSupported() {
        Set<ChannelType> channelsSupported = new HashSet<>();
        channelsSupported.add(ChannelType.MOBILE_APP_PUSH);
        channelsSupported.add(ChannelType.SMS);
        return channelsSupported;
    }
}