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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonSESResponse;
import org.eclipse.ecsp.domain.notification.AmazonSNSPushResponse;
import org.eclipse.ecsp.domain.notification.AssociationDataV1_0;
import org.eclipse.ecsp.domain.notification.CampaignStatusDataV1_0;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.CreateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.DeleteSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.DisAssociationDataV1_0;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.NotificationUserProfileEventDataV1_0;
import org.eclipse.ecsp.domain.notification.PinDataV1_0;
import org.eclipse.ecsp.domain.notification.RefreshSchedulerData;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.UpdateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.RetryableException;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.CompositeIgniteEvent;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.EventDataDeSerializer;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigControlService;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.duplication.Deduplicator;
import org.eclipse.ecsp.notification.entities.BufferedAlertsInfo;
import org.eclipse.ecsp.notification.entities.CloneNotificationConfig;
import org.eclipse.ecsp.notification.entities.CloneNotificationTemplate;
import org.eclipse.ecsp.notification.entities.CloneNotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.notification.entities.NotificationSchedulerPayload;
import org.eclipse.ecsp.notification.processors.AlertProcessorChain;
import org.eclipse.ecsp.notification.user.profile.UserProfileService;
import org.eclipse.ecsp.notification.userprofile.UserProfileIntegrationService;
import org.eclipse.ecsp.notification.utils.MemoryAppender;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiPredicate;

import static org.eclipse.ecsp.domain.EventID.SCHEDULE_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.EventID.SCHEDULE_OP_STATUS_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.ASSOCIATION;
import static org.eclipse.ecsp.domain.notification.commons.EventID.CREATE_SECONDARY_CONTACT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.DELETE_SECONDARY_CONTACT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.DFF_FEEDBACK_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.DISASSOCIATION;
import static org.eclipse.ecsp.domain.notification.commons.EventID.DMA_FEEDBACK_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NON_REGISTERED_USER_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NOTIFICATION_SETTINGS;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NOTIFICATION_USER_PROFILE;
import static org.eclipse.ecsp.domain.notification.commons.EventID.PIN_GENERATED;
import static org.eclipse.ecsp.domain.notification.commons.EventID.REFRESH_NOTIFICATION_SCHEDULER;
import static org.eclipse.ecsp.domain.notification.commons.EventID.UPDATE_SECONDARY_CONTACT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_ACK;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.DISASSOCIATION_TOPIC;
import static org.eclipse.ecsp.notification.VehicleInfoNotification.MAPPER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



/**
 * VehicleInfoNotification test.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class VehicleInfoNotification2Test {

    private static final String VEHICLE_ID = "vehicleIdVal";
    private static final String USER_ID = "userIdVal";
    private static final String GROUP = "groupVal";
    private static final String SCHEDULER_ID = "80458302-abda-4110-b330-52318af56705";


    @InjectMocks
    private VehicleInfoNotification vehicleInfoNotification;

    @Mock
    private BiPredicate<IgniteEventStreamProcessor,
            StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;

    @Mock
    private Deduplicator deduplicator;

    @Mock
    private AlertsHistoryAssistant alertsHistoryAssistant;

    @Mock
    private AlertProcessorChain alertProcessorChain;

    @Mock
    private ScheduleNotificationAssistant scheduleNotificationAssistant;

    @Mock
    private ChannelNotifierRegistry channelNotifierRegistry;

    @Mock
    private ChannelNotifier amazonsnssmsnotifier;

    @Mock
    private ChannelNotifier amazonsesnotifier;

    @Mock
    private ChannelNotifier apiPushNotifier;

    @Mock
    private ChannelNotifier ivmNotifier;

    @Mock
    private NotificationConfigDAO notificationConfigDao;

    @Mock
    private NotificationBufferDao notificationBufferDao;

    @Mock
    private NotificationConfigControlService configService;

    @Mock
    private UserProfileService profileService;

    @Mock
    private UserProfileIntegrationService userService;

    @Mock
    private KeyValueStore<String, String> notificationStateStore;

    @Mock
    private VehicleInfoNotificationNonRegisteredUser vehicleInfoNotificationNonRegisteredUser;

    @Mock
    private NotificationRetryAssistant notificationRetryAssistant;

    @Captor
    private ArgumentCaptor<String> vehicleIdCaptor;

    @Captor
    private ArgumentCaptor<List<AlertsInfo>> alertInfosCaptor;

    private MemoryAppender memoryAppender;

    private AlertsInfo alertsInfo;
    private AlertsHistoryInfo alertsHistoryInfo;
    private Properties properties;

    /**
     * test set up.
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(skipProcessorPredicate.test(any(), any())).thenReturn(false);
        Mockito.when(ctxt.streamName()).thenReturn("notification");
        properties = NotificationTestUtils.loadProperties("/application.properties");
        vehicleInfoNotification.initConfig(properties);

        alertsHistoryInfo = new AlertsHistoryInfo();
        Mockito.when(alertsHistoryAssistant.createBasicAlertHistory(any())).thenReturn(alertsHistoryInfo);
        Mockito.doNothing().when(alertProcessorChain).process(any());
        Mockito.doNothing().when(scheduleNotificationAssistant).init(any());

        Mockito.doNothing().when(notificationRetryAssistant).updateAlertHistoryForRetry(any(), any());
        Mockito.when(notificationRetryAssistant.createRetryNotificationEvent(any(), any(), any()))
                .thenReturn(new IgniteEventImpl());

        AmazonSNSPushResponse amazonSnsPushResponse = new AmazonSNSPushResponse();
        amazonSnsPushResponse.setStatus("success");
        Mockito.when(amazonsnssmsnotifier.publish(any())).thenReturn(amazonSnsPushResponse);
        when(channelNotifierRegistry.channelNotifier(ChannelType.SMS)).thenReturn(amazonsnssmsnotifier);
        when(channelNotifierRegistry.channelNotifier(ChannelType.SMS, "lowFuel", "")).thenReturn(amazonsnssmsnotifier);

        AmazonSESResponse sesResponse = new AmazonSESResponse("userId");
        sesResponse.setStatus("success");
        Mockito.when(amazonsesnotifier.publish(any())).thenReturn(sesResponse);
        when(channelNotifierRegistry.channelNotifier(ChannelType.EMAIL)).thenReturn(amazonsesnotifier);
        when(channelNotifierRegistry.channelNotifier(ChannelType.EMAIL, null, "")).thenReturn(amazonsesnotifier);

        Logger logger = (Logger) LoggerFactory.getLogger(VehicleInfoNotification.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();

    }

    @Test
    public void processScheduleNotification() {
        IgniteEventImpl igniteEvent = getIgniteEvent();
        Mockito.when(skipProcessorPredicate.test(any(), any())).thenReturn(true);
        Mockito.doNothing().when(ctxt).forward(any(), any());
        VehicleInfoNotification vehicleInfoNotificationSpy = spy(vehicleInfoNotification);
        vehicleInfoNotificationSpy.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        verify(vehicleInfoNotificationSpy, Mockito.times(0)).process(any(), any(), any());
    }

    @Test
    public void processEmptyKey() {
        memoryAppender.reset();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), null, "notification");
        assertEquals(1, memoryAppender.countEventsForLogger("org.eclipse.ecsp.notification.VehicleInfoNotification"));
        assertEquals("ERROR", memoryAppender.getLoggedEvents().get(0).getLevel().toString());
    }

    @Test
    public void processEmptyValue() {
        memoryAppender.reset();
        IgniteEventImpl igniteEvent = getIgniteEvent();
        vehicleInfoNotification.process(null, igniteEvent, "notification");
        assertEquals(1, memoryAppender.countEventsForLogger("org.eclipse.ecsp.notification.VehicleInfoNotification"));
        assertEquals("ERROR", memoryAppender.getLoggedEvents().get(0).getLevel().toString());
    }

    @Test
    public void processRefreshNotificationSchedulerResend() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());


        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(ctxt, times(1)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(1)).deleteById(any());
        verify(scheduleNotificationAssistant, times(1)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(1)).publish(any());
        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Resend snoozed Notifications", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("DefaultAlertMessage from channel", Level.DEBUG).size());
    }

    @Test
    public void processRefreshNotificationSchedulerResendEmptyUser() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);
        notificationConfig.setUserId(null);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any())).thenReturn(
                Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());


        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        verify(ctxt, times(0)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(0)).deleteById(any());
        verify(scheduleNotificationAssistant, times(0)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(0)).publish(any());

        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("UserProfile is null for the request", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("DefaultAlertMessage from channel", Level.DEBUG).size());
    }


    @Test(expected = RuntimeException.class)
    public void processIgniteExceptionEvent() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, "IgniteExceptionEvent");
        igniteEvent.setVersion(Version.V1_1);
        ReflectionTestUtils.setField(vehicleInfoNotification, "reprocessingEnabled", true);
        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);
        notificationConfig.setUserId(null);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());


        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

    }

    @Test
    public void processCompositeEvent() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, "CompositeEvent");
        Assertions.assertNotNull(igniteEvent);
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

    }

    @Test
    public void processcampaignEvent() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, "CAMPAIGN_EVENT");
        Assertions.assertNotNull(igniteEvent);
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

    }

    @Test
    public void processCampaignStatus() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        Assertions.assertNotNull(igniteEvent);
        setEventId(igniteEvent, "CampaignStatus");

        CampaignStatusDataV1_0 campaignStatusDataV10 = new CampaignStatusDataV1_0();
        campaignStatusDataV10.setCampaignId("343746378246");
        campaignStatusDataV10.setType("NOTIFICATION");
        campaignStatusDataV10.setStatus("Live");
        igniteEvent.setEventData(campaignStatusDataV10);

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

    }


    @Test
    public void processRefreshNotificationSchedulerResendEmptyScheduleId() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);
        nb.setSchedulerId(null);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());


        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        verify(ctxt, times(0)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(0)).deleteById(any());
        verify(scheduleNotificationAssistant, times(0)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(0)).publish(any());

        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Resend snoozed Notifications", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("SchedulerId not found", Level.ERROR).size());
    }

    @Test
    public void processRefreshNotificationSchedulerResendScheduleIdNotFound() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(null);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());


        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        verify(ctxt, times(0)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(0)).deleteById(any());
        verify(scheduleNotificationAssistant, times(0)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(0)).publish(any());

        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Resend snoozed Notifications", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("SchedulerId not found", Level.ERROR).size());
    }

    @Test
    public void processRefreshNotificationSchedulerResendSendAlertException() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        doThrow(new RuntimeException("error while sendAlert")).when(amazonsesnotifier).publish(any());
        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());


        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        verify(ctxt, times(1)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(1)).deleteById(any());
        verify(scheduleNotificationAssistant, times(1)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(1)).publish(any());

        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Resend snoozed Notifications", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Exception occurred while publishing alerts", Level.ERROR).size());
    }

    @Test
    public void processRefreshNotificationSchedulerNotInAffectSuppression() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);
        SuppressionConfig suppressionConfig = new SuppressionConfig();
        notificationConfig.getChannel(ChannelType.EMAIL)
                .setSuppressionConfigs(Collections.singletonList(suppressionConfig));

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());
        when(scheduleNotificationAssistant.enforceSuppression(any(), any())).thenReturn(null);

        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        verify(ctxt, times(1)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(1)).deleteById(any());
        verify(scheduleNotificationAssistant, times(1)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(1)).publish(any());
        verify(scheduleNotificationAssistant, times(1)).enforceSuppression(any(), any());
        verify(scheduleNotificationAssistant, times(0)).updateScheduler(any(), any(), any(), any(), any(), any(),
                any());


        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("DefaultAlertMessage from channel", Level.DEBUG).size());
    }

    @Test
    public void processRefreshNotificationSchedulerNoBuffer() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);
        SuppressionConfig suppressionConfig = new SuppressionConfig();
        notificationConfig.getChannel(ChannelType.EMAIL)
                .setSuppressionConfigs(Collections.singletonList(suppressionConfig));

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(null);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());
        when(scheduleNotificationAssistant.enforceSuppression(any(), any())).thenReturn(new SuppressionConfig());

        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        verify(ctxt, times(0)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(0)).deleteById(any());
        verify(scheduleNotificationAssistant, times(0)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(0)).publish(any());
        verify(scheduleNotificationAssistant, times(0)).enforceSuppression(any(), any());
        verify(scheduleNotificationAssistant, times(0)).updateScheduler(any(), any(), any(), any(), any(), any(),
                any());


        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("DefaultAlertMessage from channel", Level.DEBUG).size());
    }

    @Test
    public void processRefreshNotificationSchedulerUpdateScheduler() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, REFRESH_NOTIFICATION_SCHEDULER);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);
        SuppressionConfig suppressionConfig = new SuppressionConfig();
        notificationConfig.getChannel(ChannelType.EMAIL)
                .setSuppressionConfigs(Collections.singletonList(suppressionConfig));

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());
        when(scheduleNotificationAssistant.enforceSuppression(any(), any())).thenReturn(new SuppressionConfig());

        RefreshSchedulerData refreshSchedulerData = RefreshSchedulerData.builder()
                .contactId("self")
                .userId(USER_ID)
                .groups(Collections.singleton(GROUP))
                .build();
        igniteEvent.setEventData(refreshSchedulerData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(1)).findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        verify(ctxt, times(0)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        verify(notificationBufferDao, times(0)).deleteById(any());
        verify(scheduleNotificationAssistant, times(0)).createDeleteScheduleEventData(any(), any(), any());
        verify(amazonsesnotifier, times(0)).publish(any());
        verify(scheduleNotificationAssistant, times(1)).enforceSuppression(any(), any());
        verify(scheduleNotificationAssistant, times(1)).updateScheduler(any(), any(), any(), any(), any(), any(),
                any());


        assertEquals(1, memoryAppender.search("Refresh scheduler for user", Level.INFO).size());
        assertEquals(1,
                memoryAppender.search("Updating Scheduler before updating NotificationConfig", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("Alert History:", Level.DEBUG).size());
        assertEquals(0, memoryAppender.search("DefaultAlertMessage from channel", Level.DEBUG).size());
    }


    @Test
    public void processNotificationSetting() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, NOTIFICATION_SETTINGS);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        doNothing().when(configService).decryptNotificationConfig(any());
        doNothing().when(configService).patchUpdateConfig(any());
        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        when(notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(),
                any())).thenReturn(nb);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());

        NotificationSettingDataV1_0 notificationSettingData = new NotificationSettingDataV1_0();
        notificationSettingData.setNotificationConfigs(Collections.singletonList(notificationConfig));
        igniteEvent.setEventData(notificationSettingData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(configService, times(1)).decryptNotificationConfig(any(NotificationConfig.class));
        verify(configService, times(1)).patchUpdateConfig(any(IgniteEventImpl.class));
        assertEquals(1, memoryAppender.search("Scheduler updated successfully", Level.DEBUG).size());
    }

    @Test
    public void processNotificationSettingException() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, NOTIFICATION_SETTINGS);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        NotificationBuffer nb = getNotificationBuffer(igniteEvent);

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        doNothing().when(configService).decryptNotificationConfig(any());
        doNothing().when(configService).patchUpdateConfig(any());
        when(
                notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(notificationConfig));
        doThrow(new RuntimeException("rt error")).when(notificationBufferDao)
                .findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(), any());
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(new AlertsHistoryInfo());
        when(notificationBufferDao.findBySchedulerId(any())).thenReturn(nb);
        when(alertsHistoryAssistant.getAlertHistory(any())).thenReturn(alertsHistoryInfo);
        when(alertsHistoryAssistant.setEnrichedAlertHistory(any(), any())).thenReturn(alertsHistoryInfo);
        doNothing().when(alertsHistoryAssistant).saveAlertHistory(any(), any(), any(), any());
        when(notificationBufferDao.deleteById(any())).thenReturn(true);
        when(scheduleNotificationAssistant.createDeleteScheduleEventData(any(), any(), any())).thenReturn(igniteEvent);
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());

        NotificationSettingDataV1_0 notificationSettingData = new NotificationSettingDataV1_0();
        notificationSettingData.setNotificationConfigs(Collections.singletonList(notificationConfig));
        igniteEvent.setEventData(notificationSettingData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(configService, times(1)).decryptNotificationConfig(any(NotificationConfig.class));
        verify(configService, times(1)).patchUpdateConfig(any(IgniteEventImpl.class));
        assertEquals(0, memoryAppender.search("Scheduler updated successfully", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Unable to update scheduler. Exception occurred", Level.ERROR).size());
    }

    @Test
    public void processUserProfile() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, NOTIFICATION_USER_PROFILE);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        doNothing().when(profileService).updateUserProfile(any(IgniteEventImpl.class));

        NotificationUserProfileEventDataV1_0 notificationUserProfileEventData =
                new NotificationUserProfileEventDataV1_0();
        notificationUserProfileEventData.setUserProfile(new UserProfile(USER_ID));
        igniteEvent.setEventData(notificationUserProfileEventData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(profileService, times(1)).updateUserProfile(any(IgniteEventImpl.class));
    }

    @Test
    public void processCreateSecondaryUser() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, CREATE_SECONDARY_CONTACT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        doNothing().when(profileService).createSecondaryContact(any(IgniteEventImpl.class));

        CreateSecondaryContactEventDataV1_0 createSecondaryContactEventData = new CreateSecondaryContactEventDataV1_0();
        createSecondaryContactEventData.setSecondaryContact(new SecondaryContact());
        igniteEvent.setEventData(createSecondaryContactEventData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(profileService, times(1)).createSecondaryContact(any(IgniteEventImpl.class));
    }

    @Test
    public void processUpdateSecondaryUser() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, UPDATE_SECONDARY_CONTACT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        doNothing().when(profileService).updateSecondaryContact(any(IgniteEventImpl.class));

        UpdateSecondaryContactEventDataV1_0 updateSecondaryContactEventData = new UpdateSecondaryContactEventDataV1_0();
        updateSecondaryContactEventData.setSecondaryContact(new SecondaryContact());
        igniteEvent.setEventData(updateSecondaryContactEventData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(profileService, times(1)).updateSecondaryContact(any(IgniteEventImpl.class));
    }

    @Test
    public void processDeleteSecondaryUser() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, DELETE_SECONDARY_CONTACT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        doNothing().when(profileService).deleteSecondaryContact(any(IgniteEventImpl.class));

        DeleteSecondaryContactEventDataV1_0 deleteSecondaryContactEventData = new DeleteSecondaryContactEventDataV1_0();
        deleteSecondaryContactEventData.setSecondaryContact(new SecondaryContact());
        igniteEvent.setEventData(deleteSecondaryContactEventData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(profileService, times(1)).deleteSecondaryContact(any(IgniteEventImpl.class));
    }

    @Test
    public void processScheduleOpNonCreate() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_OP_STATUS_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleOpStatusEventData scheduleOpStatusEventData = new ScheduleOpStatusEventData();
        scheduleOpStatusEventData.setStatus(ScheduleStatus.DELETE);
        igniteEvent.setEventData(scheduleOpStatusEventData);

        initConfig();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        assertEquals(1,
                memoryAppender.search("NotificationStreamProcessor has received schedule op status event", Level.DEBUG)
                        .size());
    }

    @Test
    public void processScheduleOpInvalid() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_OP_STATUS_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleOpStatusEventData scheduleOpStatusEventData = new ScheduleOpStatusEventData();
        scheduleOpStatusEventData.setStatus(ScheduleStatus.CREATE);
        scheduleOpStatusEventData.setValid(false);
        IgniteEventImpl igniteScheduleEvent = getIgniteEvent();
        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        NotificationSchedulerPayload notificationSchedulerPayload = getNotificationSchedulerPayload();
        createScheduleEventData.setNotificationPayload(MAPPER.writeValueAsBytes(notificationSchedulerPayload));
        igniteScheduleEvent.setEventData(createScheduleEventData);
        scheduleOpStatusEventData.setIgniteEvent(igniteScheduleEvent);
        igniteEvent.setEventData(scheduleOpStatusEventData);

        initConfig();

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        assertEquals(1, memoryAppender.search("Error saving ScheduledId. valid field is false", Level.ERROR).size());
    }

    @Test
    public void processScheduleOpSuccess() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_OP_STATUS_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleOpStatusEventData scheduleOpStatusEventData = new ScheduleOpStatusEventData();
        scheduleOpStatusEventData.setStatus(ScheduleStatus.CREATE);
        scheduleOpStatusEventData.setValid(true);
        scheduleOpStatusEventData.setScheduleId(SCHEDULER_ID);

        IgniteEventImpl igniteScheduleEvent = getIgniteEvent();
        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        NotificationSchedulerPayload notificationSchedulerPayload = getNotificationSchedulerPayload();
        createScheduleEventData.setNotificationPayload(MAPPER.writeValueAsBytes(notificationSchedulerPayload));
        igniteScheduleEvent.setEventData(createScheduleEventData);
        scheduleOpStatusEventData.setIgniteEvent(igniteScheduleEvent);
        igniteEvent.setEventData(scheduleOpStatusEventData);

        initConfig();

        doReturn(new NotificationBuffer()).when(scheduleNotificationAssistant)
                .getNotificationBuffer(any(), any(), any(), any(), any());
        doReturn(true).when(notificationBufferDao).update(any());
        doReturn(new IgniteEventImpl()).when(scheduleNotificationAssistant)
                .createDeleteScheduleEventData(any(), any(), any());
        doReturn("topic").when(scheduleNotificationAssistant).getSchedulerSourceTopic();
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(scheduleNotificationAssistant, times(1)).getNotificationBuffer(any(), any(), any(), any(), any());
        verify(notificationBufferDao, times(1)).update(any());
        verify(scheduleNotificationAssistant, times(1)).createDeleteScheduleEventData(any(), any(), any());
        verify(scheduleNotificationAssistant, times(3)).getSchedulerSourceTopic();
        verify(ctxt, times(1)).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());
    }

    @Test
    public void processScheduleOpNoBufferNoFlag() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_OP_STATUS_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleOpStatusEventData scheduleOpStatusEventData = new ScheduleOpStatusEventData();
        scheduleOpStatusEventData.setStatus(ScheduleStatus.CREATE);
        scheduleOpStatusEventData.setValid(true);
        scheduleOpStatusEventData.setScheduleId(SCHEDULER_ID);

        IgniteEventImpl igniteScheduleEvent = getIgniteEvent();
        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        NotificationSchedulerPayload notificationSchedulerPayload = getNotificationSchedulerPayload();
        notificationSchedulerPayload.setSchedulerUpdateFlag(false);
        createScheduleEventData.setNotificationPayload(MAPPER.writeValueAsBytes(notificationSchedulerPayload));
        igniteScheduleEvent.setEventData(createScheduleEventData);
        scheduleOpStatusEventData.setIgniteEvent(igniteScheduleEvent);
        igniteEvent.setEventData(scheduleOpStatusEventData);

        initConfig();

        doReturn(null).when(scheduleNotificationAssistant).getNotificationBuffer(any(), any(), any(), any(), any());
        doReturn(true).when(notificationBufferDao).update(any());
        doReturn(new IgniteEventImpl()).when(scheduleNotificationAssistant)
                .createDeleteScheduleEventData(any(), any(), any());
        doReturn("topic").when(scheduleNotificationAssistant).getSchedulerSourceTopic();
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(scheduleNotificationAssistant, times(1)).getNotificationBuffer(any(), any(), any(), any(), any());
        verify(notificationBufferDao, times(0)).update(any());
        verify(scheduleNotificationAssistant, times(0)).createDeleteScheduleEventData(any(), any(), any());
        verify(scheduleNotificationAssistant, times(2)).getSchedulerSourceTopic();
        verify(ctxt, times(0)).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());
    }

    @Test
    public void processScheduleOpIoException() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_OP_STATUS_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleOpStatusEventData scheduleOpStatusEventData = new ScheduleOpStatusEventData();
        scheduleOpStatusEventData.setStatus(ScheduleStatus.CREATE);
        scheduleOpStatusEventData.setValid(true);
        scheduleOpStatusEventData.setScheduleId(SCHEDULER_ID);

        IgniteEventImpl igniteScheduleEvent = getIgniteEvent();
        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        NotificationSchedulerPayload notificationSchedulerPayload = getNotificationSchedulerPayload();
        notificationSchedulerPayload.setSchedulerUpdateFlag(false);
        createScheduleEventData.setNotificationPayload("fgdf".getBytes(StandardCharsets.UTF_8));
        igniteScheduleEvent.setEventData(createScheduleEventData);
        scheduleOpStatusEventData.setIgniteEvent(igniteScheduleEvent);
        igniteEvent.setEventData(scheduleOpStatusEventData);

        initConfig();

        doReturn(new NotificationBuffer()).when(scheduleNotificationAssistant)
                .getNotificationBuffer(any(), any(), any(), any(), any());
        doReturn(true).when(notificationBufferDao).update(any());
        doReturn(new IgniteEventImpl()).when(scheduleNotificationAssistant)
                .createDeleteScheduleEventData(any(), any(), any());
        doReturn("topic").when(scheduleNotificationAssistant).getSchedulerSourceTopic();
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(scheduleNotificationAssistant, times(0)).getNotificationBuffer(any(), any(), any(), any(), any());
        verify(notificationBufferDao, times(0)).update(any());
        verify(scheduleNotificationAssistant, times(0)).createDeleteScheduleEventData(any(), any(), any());
        verify(scheduleNotificationAssistant, times(2)).getSchedulerSourceTopic();
        verify(ctxt, times(0)).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());
        assertEquals(1,
                memoryAppender.search("Exception occurred while saving notification SchedulerId to buffer", Level.ERROR)
                        .size());
    }

    @Test
    public void processScheduleNoScheduleId() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleNotificationEventData scheduleNotificationEventData = new ScheduleNotificationEventData();
        igniteEvent.setEventData(scheduleNotificationEventData);

        initConfig();

        doReturn(new NotificationBuffer()).when(notificationBufferDao).findBySchedulerId(any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(0)).findByUserVehicleContactId(any(), any(), any());
    }

    @Test
    public void processScheduleNoNotificationBuffer() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleNotificationEventData scheduleNotificationEventData = new ScheduleNotificationEventData();
        igniteEvent.setEventData(scheduleNotificationEventData);

        initConfig();

        doReturn(null).when(notificationBufferDao).findBySchedulerId(any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(0)).findByUserVehicleContactId(any(), any(), any());
    }

    @Test
    public void processScheduleNoSuppression() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleNotificationEventData scheduleNotificationEventData = new ScheduleNotificationEventData();
        scheduleNotificationEventData.setScheduleIdId(SCHEDULER_ID);
        scheduleNotificationEventData.setTriggerTimeMs(System.currentTimeMillis());
        igniteEvent.setEventData(scheduleNotificationEventData);

        initConfig();

        NotificationBuffer notificationBuffer = getNotificationBuffer(getIgniteEvent());
        doReturn(notificationBuffer, (NotificationBuffer) null).when(notificationBufferDao).findBySchedulerId(any());
        doReturn(Collections.singletonList(notificationConfig)).when(notificationConfigDao)
                .findByUserVehicleContactId(any(), vehicleIdCaptor.capture(), any());
        doReturn(null).when(scheduleNotificationAssistant).enforceSuppression(any(), any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        assertEquals(VEHICLE_ID + "FromAlert", vehicleIdCaptor.getValue());
        verify(notificationBufferDao, times(2)).findBySchedulerId(any());
        verify(notificationConfigDao, times(1)).findByUserVehicleContactId(any(), any(), any());
        verify(scheduleNotificationAssistant, times(0)).updateScheduler(any(), any(), any(), any(), any(), any(),
                any());
        verify(scheduleNotificationAssistant, times(1)).enforceSuppression(any(), any());
    }

    @Test
    public void processScheduleWithSuppression() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleNotificationEventData scheduleNotificationEventData = new ScheduleNotificationEventData();
        scheduleNotificationEventData.setScheduleIdId(SCHEDULER_ID);
        scheduleNotificationEventData.setTriggerTimeMs(System.currentTimeMillis());
        igniteEvent.setEventData(scheduleNotificationEventData);

        initConfig();

        NotificationBuffer notificationBuffer = getNotificationBuffer(getIgniteEvent());
        notificationBuffer.setVehicleId(VEHICLE_ID);
        doReturn(notificationBuffer, (NotificationBuffer) null).when(notificationBufferDao).findBySchedulerId(any());
        doReturn(Collections.singletonList(notificationConfig)).when(notificationConfigDao)
                .findByUserVehicleContactId(any(), vehicleIdCaptor.capture(), any());
        doReturn(new SuppressionConfig()).when(scheduleNotificationAssistant).enforceSuppression(any(), any());
        doNothing().when(scheduleNotificationAssistant)
                .updateScheduler(any(), any(), any(), any(), any(), any(), any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        assertEquals(VEHICLE_ID, vehicleIdCaptor.getValue());
        verify(notificationBufferDao, times(1)).findBySchedulerId(any());
        verify(notificationConfigDao, times(1)).findByUserVehicleContactId(any(), any(), any());
        verify(scheduleNotificationAssistant, times(1)).updateScheduler(any(), any(), any(), any(), any(), any(),
                any());
        verify(scheduleNotificationAssistant, times(1)).enforceSuppression(any(), any());
    }

    @Test
    public void processScheduleException() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, SCHEDULE_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        ScheduleNotificationEventData scheduleNotificationEventData = new ScheduleNotificationEventData();
        scheduleNotificationEventData.setScheduleIdId(SCHEDULER_ID);
        scheduleNotificationEventData.setTriggerTimeMs(System.currentTimeMillis());
        igniteEvent.setEventData(scheduleNotificationEventData);

        initConfig();

        NotificationBuffer notificationBuffer = getNotificationBuffer(getIgniteEvent());
        doReturn(notificationBuffer, (NotificationBuffer) null).when(notificationBufferDao).findBySchedulerId(any());
        doThrow(new RuntimeException("rt exception")).when(notificationConfigDao)
                .findByUserVehicleContactId(any(), vehicleIdCaptor.capture(), any());
        doReturn(new SuppressionConfig()).when(scheduleNotificationAssistant).enforceSuppression(any(), any());
        doNothing().when(scheduleNotificationAssistant)
                .updateScheduler(any(), any(), any(), any(), any(), any(), any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        assertEquals(1,
                memoryAppender.search("Exception occurred while handling scheduled notification event", Level.ERROR)
                        .size());
    }

    @Test
    public void processAckApiPush() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, DFF_FEEDBACK_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        initConfig();

        doNothing().when(apiPushNotifier).processAck(any());
        doNothing().when(ivmNotifier).processAck(any());
        doReturn(apiPushNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.API_PUSH);
        doReturn(ivmNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.IVM);

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(apiPushNotifier, times(1)).processAck(any());
        verify(ivmNotifier, times(0)).processAck(any());
    }

    @Test
    public void processAckIvmVehicleMsg() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, VEHICLE_MESSAGE_ACK);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        initConfig();

        doNothing().when(apiPushNotifier).processAck(any());
        doNothing().when(ivmNotifier).processAck(any());
        doReturn(apiPushNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.API_PUSH);
        doReturn(ivmNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.IVM);

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(apiPushNotifier, times(0)).processAck(any());
        verify(ivmNotifier, times(1)).processAck(any());
    }

    @Test
    public void processAckIvmVehicleMsgDisposition() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, VEHICLE_MESSAGE_DISPOSITION_PUBLISH);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        initConfig();

        doNothing().when(apiPushNotifier).processAck(any());
        doNothing().when(ivmNotifier).processAck(any());
        doReturn(apiPushNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.API_PUSH);
        doReturn(ivmNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.IVM);

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(apiPushNotifier, times(0)).processAck(any());
        verify(ivmNotifier, times(1)).processAck(any());
    }

    @Test
    public void processAckIvmVehicleMsgDma() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, DMA_FEEDBACK_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        initConfig();

        doNothing().when(apiPushNotifier).processAck(any());
        doNothing().when(ivmNotifier).processAck(any());
        doReturn(apiPushNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.API_PUSH);
        doReturn(ivmNotifier).when(channelNotifierRegistry).channelNotifier(ChannelType.IVM);

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(apiPushNotifier, times(0)).processAck(any());
        verify(ivmNotifier, times(1)).processAck(any());
    }

    @Test
    public void processAssociation() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, ASSOCIATION);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        AssociationDataV1_0 associationData = new AssociationDataV1_0();
        associationData.setUserId(USER_ID);
        igniteEvent.setEventData(associationData);
        initConfig();

        doNothing().when(notificationStateStore).put(any(), any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationStateStore, times(1)).put(any(), any());
        assertEquals(1, memoryAppender.search("Received the user-id", Level.DEBUG).size());
        assertEquals(1, memoryAppender.search("Received user", Level.DEBUG).size());
    }

    @Test
    public void processDisassociation() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, DISASSOCIATION);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        DisAssociationDataV1_0 disAssociationData = new DisAssociationDataV1_0();
        disAssociationData.setUserId(USER_ID);
        igniteEvent.setEventData(disAssociationData);
        initConfig();

        doReturn(VEHICLE_ID).when(notificationStateStore).delete(any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationStateStore, times(1)).delete(any());
        assertEquals(1,
                memoryAppender.search("Received DELETE request to remove from state store for the pdid", Level.DEBUG)
                        .size());
        assertEquals(1, memoryAppender.search("Deleted the key", Level.INFO).size());
    }

    @Test
    public void processProfileChangedSuccess() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);


        VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription =
                new VehicleProfileNotificationEventDataV1_1.ChangeDescription();

        Map<String, String> map = new HashMap<>();
        map.put("userId", USER_ID);
        List<Map<String, String>> oldData = Collections.singletonList(map);
        changeDescription.setOld(oldData);
        changeDescription.setChanged(null);
        VehicleProfileNotificationEventDataV1_1 vehicleProfileNotificationEventData =
                new VehicleProfileNotificationEventDataV1_1();
        vehicleProfileNotificationEventData.setChangeDescriptions(Collections.singletonList(changeDescription));
        igniteEvent.setEventData(vehicleProfileNotificationEventData);
        initConfig();

        doReturn(true).when(notificationConfigDao).deleteNotificationConfigByUserAndVehicle(any(), any());
        doNothing().when(scheduleNotificationAssistant).deleteScheduleNotifications(any(), any(), any());
        doNothing().when(profileService).deleteUserVehicleNickNames(any(), any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, DISASSOCIATION_TOPIC);

        verify(notificationConfigDao, times(1)).deleteNotificationConfigByUserAndVehicle(any(), any());
        verify(scheduleNotificationAssistant, times(1)).deleteScheduleNotifications(any(), any(), any());
        verify(profileService, times(1)).deleteUserVehicleNickNames(any(), any());
    }

    @Test
    public void processProfileChangedNoChange() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);


        VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription =
                new VehicleProfileNotificationEventDataV1_1.ChangeDescription();

        Map<String, String> map = new HashMap<>();
        map.put("userId", USER_ID);
        List<Map<String, String>> oldData = Collections.singletonList(map);
        changeDescription.setOld(oldData);
        changeDescription.setChanged("changed");
        VehicleProfileNotificationEventDataV1_1 vehicleProfileNotificationEventData =
                new VehicleProfileNotificationEventDataV1_1();
        vehicleProfileNotificationEventData.setChangeDescriptions(Collections.singletonList(changeDescription));
        igniteEvent.setEventData(vehicleProfileNotificationEventData);
        initConfig();

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, DISASSOCIATION_TOPIC);

        verify(notificationConfigDao, times(0)).deleteNotificationConfigByUserAndVehicle(any(), any());
        verify(scheduleNotificationAssistant, times(0)).deleteScheduleNotifications(any(), any(), any());
        verify(profileService, times(0)).deleteUserVehicleNickNames(any(), any());
    }

    @Test
    public void processProfileChangedNoOld() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        VehicleProfileNotificationEventDataV1_1 vehicleProfileNotificationEventData =
                new VehicleProfileNotificationEventDataV1_1();
        VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription =
                new VehicleProfileNotificationEventDataV1_1.ChangeDescription();

        changeDescription.setOld(null);
        changeDescription.setChanged("changed");
        vehicleProfileNotificationEventData.setChangeDescriptions(Collections.singletonList(changeDescription));
        igniteEvent.setEventData(vehicleProfileNotificationEventData);
        initConfig();

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, DISASSOCIATION_TOPIC);

        verify(notificationConfigDao, times(0)).deleteNotificationConfigByUserAndVehicle(any(), any());
        verify(scheduleNotificationAssistant, times(0)).deleteScheduleNotifications(any(), any(), any());
        verify(profileService, times(0)).deleteUserVehicleNickNames(any(), any());
    }

    @Test
    public void processProfileChangedWrongTopic() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        VehicleProfileNotificationEventDataV1_1 vehicleProfileNotificationEventData =
                new VehicleProfileNotificationEventDataV1_1();
        VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription =
                new VehicleProfileNotificationEventDataV1_1.ChangeDescription();

        changeDescription.setOld(null);
        changeDescription.setChanged("changed");
        vehicleProfileNotificationEventData.setChangeDescriptions(Collections.singletonList(changeDescription));
        igniteEvent.setEventData(vehicleProfileNotificationEventData);
        initConfig();

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(notificationConfigDao, times(0)).deleteNotificationConfigByUserAndVehicle(any(), any());
        verify(scheduleNotificationAssistant, times(0)).deleteScheduleNotifications(any(), any(), any());
        verify(profileService, times(0)).deleteUserVehicleNickNames(any(), any());
    }

    @Test
    public void processNonRegisteredUsers() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, NON_REGISTERED_USER_NOTIFICATION_EVENT);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        initConfig();

        doNothing().when(vehicleInfoNotificationNonRegisteredUser)
                .processNonRegisterUserEvent(any(), any(), any(), any(), any());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(vehicleInfoNotificationNonRegisteredUser,
                times(1)).processNonRegisterUserEvent(
                any(), any(), any(), any(), any());
    }

    @Test
    public void processPinGenerated() throws Exception {

        IgniteEventImpl igniteEvent = getIgniteEvent();
        setEventId(igniteEvent, PIN_GENERATED);

        memoryAppender.reset();
        NotificationConfig notificationConfig = getNotificationConfig();
        addEmailChannelToConfig(notificationConfig);

        PinDataV1_0 pinData = new PinDataV1_0();
        pinData.setEmails(Arrays.asList("a@b.com", "c@d.com"));
        pinData.setPhones(Arrays.asList("+97231111111", "+97232222222"));
        igniteEvent.setEventData(pinData);

        initConfig();

        doReturn(new ArrayList<>()).when(deduplicator).filterDuplicateAlert(alertInfosCaptor.capture());

        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        List<String> emails = ((EmailChannel) alertInfosCaptor.getValue().get(0).getNotificationConfigs().get(0)
                .getChannel(ChannelType.EMAIL)).getEmails();
        List<String> phones = ((SmsChannel) alertInfosCaptor.getValue().get(0).getNotificationConfigs().get(0)
                .getChannel(ChannelType.SMS)).getPhones();
        assertEquals(2, emails.size());
        assertEquals(2, phones.size());
        assertTrue(phones.containsAll(Arrays.asList("+97231111111", "+97232222222")));
        assertTrue(emails.containsAll(Arrays.asList("a@b.com", "c@d.com")));
    }

    @Test
    public void processMuteVehicleNull() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            return list;
        });

        initConfig();

        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateNotMuted();
    }

    @Test
    public void processMuteVehicleEmptyAll() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateMuted();
    }

    @Test
    public void processMuteVehicleContainChannel() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setChannels(Collections.singleton(ChannelType.SMS));
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateMuted();
    }

    @Test
    public void processMuteVehicleNotContainChannel() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setChannels(Collections.singleton(ChannelType.EMAIL));
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateNotMuted();
    }

    @Test
    public void processMuteVehicleContainGroup() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setGroups(Collections.singleton(GROUP));
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateMuted();
    }

    @Test
    public void processMuteVehicleNotContainGroup() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setGroups(Collections.singleton("group"));
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();

        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateNotMuted();
    }


    @Test
    public void processMuteVehicleStartTimeZero() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setStartTime(0L);
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateMuted();
    }

    @Test
    public void processMuteVehicleStartTimeLessNow() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setStartTime(System.currentTimeMillis() - 100000);
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateMuted();
    }


    @Test
    public void processMuteVehicleStartTimeGreaterNow() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setStartTime(System.currentTimeMillis() + 100000);
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateNotMuted();
    }

    @Test
    public void processMuteVehicleEndTimeZero() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setEndTime(0L);
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateMuted();
    }

    @Test
    public void processMuteVehicleEndTimeGreaterNow() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setEndTime(System.currentTimeMillis() + 100000);
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateMuted();
    }


    @Test
    public void processMuteVehicleEndTimeLessNow() throws IOException {

        IgniteEventImpl igniteEvent = getIgniteEvent();

        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            MuteVehicle muteVehicle = new MuteVehicle();
            muteVehicle.setVehicleId(VEHICLE_ID);
            muteVehicle.setEndTime(System.currentTimeMillis() - 100000);
            alertsInfo.setMuteVehicle(muteVehicle);
            return list;
        });

        initConfig();
        vehicleInfoNotification.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        validateNotMuted();
    }

    @Test
    public void testAlertsData() throws IOException {
        ObjectMapper mapper = getCustomMapper();

        String eventAsString = IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-data-2.json"),
                StandardCharsets.UTF_8);

        CompositeIgniteEvent compositeEvent = getCompositeEvent(mapper, eventAsString);

        String notificationEvent = mapper.writeValueAsString(compositeEvent.getNestedEvents());

        List<AlertsInfo> infoList = NotificationUtils.getListObjects(notificationEvent,
                AlertsInfo.class);
        assertNotNull(infoList);
        assertEquals("Number of elements should be 3", 3, infoList.size());
    }

    @Test
    public void testRetryNotification() {
        memoryAppender.reset();
        Mockito.doThrow(new RetryableException(2, 60000, "Retryable Exception")).when(alertProcessorChain)
                .process(any());
        doNothing().when(ctxt).forwardDirectly(any(IgniteKey.class), any(IgniteEvent.class), anyString());
        Mockito.when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            Object[] args = invocation.getArguments();
            List<AlertsInfo> list = (List<AlertsInfo>) args[0];
            initAlertInfo(list);
            return list;
        });
        IgniteEventImpl igniteEvent = getIgniteEvent();
        vehicleInfoNotification.process(new IgniteStringKey(VEHICLE_ID), igniteEvent, "notification");

        verify(ctxt, times(1)).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());

    }

    @Test
    public void processAlertsWithVehicleProfile() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(VEHICLE_ID);
        igniteEvent.setEventId("GenericNotificationEvent");
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", "lowFuel");
        genericEventData.set("userId", USER_ID);
        genericEventData.set("UserNotification", false);
        genericEventData.set("isNonRegisteredVehicle", true);

        Map<String, Object> vp = new HashMap<>();
        vp.put("make", "Deep");
        genericEventData.set("vehicleProfile", vp);
        igniteEvent.setEventData(genericEventData);

        Mockito.when(skipProcessorPredicate.test(any(), any())).thenReturn(false);
        Mockito.doNothing().when(ctxt).forward(any(), any());
        VehicleInfoNotification vehicleInfoNotificationSpy = spy(vehicleInfoNotification);
        vehicleInfoNotificationSpy.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        verify(vehicleInfoNotificationSpy, Mockito.times(1)).process(any(), any(), any());
    }

    @Test
    public void processGenericEventWithCampaignId() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(VEHICLE_ID);
        igniteEvent.setEventId("GenericNotificationEvent");
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", "lowFuel");
        genericEventData.set("userId", USER_ID);
        genericEventData.set("UserNotification", false);
        genericEventData.set(NotificationConstants.CAMPAIGN_ID, "c5ea759c-8dab-4292-8f5f-9545d94fd246");
        Map<String, Object> vp = new HashMap<>();
        vp.put("make", "Deep");
        genericEventData.set("vehicleProfile", vp);
        igniteEvent.setEventData(genericEventData);

        Mockito.when(skipProcessorPredicate.test(any(), any())).thenReturn(false);
        Mockito.doNothing().when(ctxt).forward(any(), any());
        VehicleInfoNotification vehicleInfoNotificationSpy = spy(vehicleInfoNotification);
        vehicleInfoNotificationSpy.process(
                new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        verify(vehicleInfoNotificationSpy, Mockito.times(1)).process(any(), any(), any());
    }

    private ObjectMapper getCustomMapper() {
        ObjectMapper mapper = new ObjectMapper();
        EventDataDeSerializer eventDataSerializer = new EventDataDeSerializer();
        SimpleModule module = new SimpleModule("PolymorphicEventDataModule");
        module.addDeserializer(EventData.class, eventDataSerializer);
        mapper.registerModule(module);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        return mapper;
    }

    private CompositeIgniteEvent getCompositeEvent(ObjectMapper mapper, String eventAsString) throws IOException {
        CompositeIgniteEvent compositeEvent;
        JsonNode node = mapper.readTree(eventAsString);
        if (node.isObject()) {
            IgniteEventImpl event = mapper.readValue(eventAsString, IgniteEventImpl.class);
            List<IgniteEvent> eventAsList = new ArrayList<IgniteEvent>(0);
            eventAsList.add(event);
            eventAsList.forEach(e -> {
                GenericEventData genericEventData = (GenericEventData) ((AbstractIgniteEvent) e).getEventData();
                if (genericEventData.getData(NotificationConstants.VEHICLE_DATA).isPresent()) {
                    Map<String, Object> vehicleAttributes = new HashMap<String, Object>();
                    vehicleAttributes.put(NotificationConstants.VEHICLE_ATTRIBUTES_DATA,
                            genericEventData.getData(NotificationConstants.VEHICLE_DATA).get());
                    genericEventData.set(NotificationConstants.VEHICLE_DATA, vehicleAttributes);
                }
            });
            compositeEvent = new CompositeIgniteEvent();
            compositeEvent.setNestedEvents(eventAsList);
        } else {
            List<IgniteEvent> list = mapper.readValue(eventAsString,
                    mapper.getTypeFactory().constructCollectionType(List.class, IgniteEventImpl.class));
            list.forEach(e -> {
                GenericEventData genericEventData = (GenericEventData) ((AbstractIgniteEvent) e).getEventData();
                if (genericEventData.getData(NotificationConstants.VEHICLE_DATA).isPresent()) {
                    Map<String, Object> vehicleAttributes = new HashMap<String, Object>();
                    vehicleAttributes.put(NotificationConstants.VEHICLE_ATTRIBUTES_DATA,
                            genericEventData.getData(NotificationConstants.VEHICLE_DATA).get());
                    genericEventData.set(NotificationConstants.VEHICLE_DATA, vehicleAttributes);
                }
            });
            compositeEvent = new CompositeIgniteEvent();
            compositeEvent.setNestedEvents(list);
        }
        return compositeEvent;
    }


    private void initConfig() throws IOException {
        Properties properties = NotificationTestUtils.loadProperties("/application.properties");
        properties.setProperty("check.user.pdid.association", "false");
        vehicleInfoNotification.initConfig(properties);
    }

    @NotNull
    private IgniteEventImpl getIgniteEvent() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(VEHICLE_ID);
        igniteEvent.setEventId("GenericNotificationEvent");
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", "lowFuel");
        genericEventData.set("userId", USER_ID);
        genericEventData.set("UserNotification", false);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("soldRegion", "");
        vehicleProfileAbridged.setVehicleAttributes(attr);
        genericEventData.set("vehicleProfile", vehicleProfileAbridged);
        igniteEvent.setEventData(genericEventData);
        return igniteEvent;
    }

    private void initAlertInfo(List<AlertsInfo> list) {
        alertsInfo = list.get(0);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(USER_ID);
        userProfile.setConsent(true);
        alertsInfo.getAlertsData().setUserProfile(userProfile);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("soldRegion", "");
        vehicleProfileAbridged.setVehicleAttributes(attr);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfileAbridged);
        NotificationConfig notificationConfig = getNotificationConfig();
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        notificationConfig.setChannels(Collections.singletonList(smsChannel));
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));
    }

    @NotNull
    private NotificationConfig getNotificationConfig() {
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId(USER_ID);
        notificationConfig.setGroup(GROUP);
        notificationConfig.setVehicleId(VEHICLE_ID);
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        return notificationConfig;
    }

    private void validateNotMuted() {
        assertEquals(1, alertsHistoryInfo.getChannelResponses().size());
        assertEquals("success", alertsHistoryInfo.getChannelResponses().get(0).getStatus());
    }

    private void validateMuted() {
        assertEquals(0, alertsHistoryInfo.getChannelResponses().size());
    }

    private void setEventId(IgniteEventImpl igniteEvent, String eventId) {
        igniteEvent.setEventId(eventId);
        igniteEvent.setRequestId("requestId");
    }

    private void addEmailChannelToConfig(NotificationConfig notificationConfig) {
        List<Channel> channels = new ArrayList<>();
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("test@1.com"));
        emailChannel.setEnabled(true);
        channels.add(emailChannel);
        notificationConfig.setChannels(channels);
    }

    @NotNull
    private NotificationBuffer getNotificationBuffer(IgniteEventImpl igniteEvent) {
        NotificationBuffer nb = new NotificationBuffer();
        nb.setSchedulerId("schedulerId");
        nb.setChannelType(ChannelType.EMAIL);
        BufferedAlertsInfo bufferedAlertsInfo = new BufferedAlertsInfo();
        CloneNotificationTemplate cloneNotificationTemplate = new CloneNotificationTemplate();
        cloneNotificationTemplate.setNotificationId("nid");
        bufferedAlertsInfo.addCloneNotificationTemplate("en-US", cloneNotificationTemplate);
        CloneNotificationTemplateConfig cloneNotificationTemplateConfig = new CloneNotificationTemplateConfig();
        cloneNotificationTemplateConfig.setNotificationId("nid");
        bufferedAlertsInfo.setCloneNotificationTemplateConfig(cloneNotificationTemplateConfig);
        CloneNotificationConfig cloneNotificationConfig = new CloneNotificationConfig();
        cloneNotificationConfig.setLocale("en-US");
        cloneNotificationConfig.setVehicleId(VEHICLE_ID + "FromAlert");
        cloneNotificationConfig.setGroup(GROUP);
        bufferedAlertsInfo.setCloneNotificationConfig(cloneNotificationConfig);
        bufferedAlertsInfo.setIgniteEvent(igniteEvent);
        nb.setAlertsInfo(Collections.singletonList(bufferedAlertsInfo));
        return nb;
    }

    @NotNull
    private NotificationSchedulerPayload getNotificationSchedulerPayload() {
        NotificationSchedulerPayload notificationSchedulerPayload = new NotificationSchedulerPayload();
        notificationSchedulerPayload.setSchedulerId(SCHEDULER_ID);
        notificationSchedulerPayload.setSchedulerUpdateFlag(true);
        notificationSchedulerPayload.setVehicleID(VEHICLE_ID);
        notificationSchedulerPayload.setUserID(USER_ID);
        notificationSchedulerPayload.setGroup(GROUP);
        notificationSchedulerPayload.setChannelType(ChannelType.EMAIL);
        notificationSchedulerPayload.setContactId("self");
        return notificationSchedulerPayload;
    }
}