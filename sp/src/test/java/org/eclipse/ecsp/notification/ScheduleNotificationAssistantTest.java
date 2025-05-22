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

import org.apache.commons.codec.binary.Base64;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.idgen.MessageIdGenerator;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.DeleteScheduleEventData;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.aws.sns.SnsUtils;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.entities.BufferedAlertsInfo;
import org.eclipse.ecsp.notification.entities.CloneNotificationConfig;
import org.eclipse.ecsp.notification.entities.CloneNotificationTemplate;
import org.eclipse.ecsp.notification.entities.CloneNotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.ecsp.domain.EventID.CREATE_SCHEDULE_EVENT;
import static org.eclipse.ecsp.domain.EventID.DELETE_SCHEDULE_EVENT;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


/**
 * ScheduleNotificationAssistantTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class ScheduleNotificationAssistantTest {

    static final String USER_ID = "userId";
    static final String VEHICLE_ID = "vehicleId";
    static final String SCHEDULER_ID = "schedulerId";
    static final String MSG_ID_GEN = "msgIdGen";
    static final String GROUP = "group";

    @InjectMocks
    ScheduleNotificationAssistant scheduleNotificationAssistant;

    @Mock
    NotificationBufferDao notificationBufferDao;

    @Mock
    MessageIdGenerator msgIdGen;

    @Mock
    StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;

    @Mock
    UserProfileDAO userProfileDao;

    @Captor
    private ArgumentCaptor<IgniteEventImpl> igniteEventArgumentCaptor;

    @Captor
    private ArgumentCaptor<NotificationBuffer> notificationBufferArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deleteScheduleNotificationsNotFound() {
        doReturn(new ArrayList<NotificationBuffer>()).when(notificationBufferDao)
            .findByUserIdAndVehicleId(any(), any());
        IgniteEventImpl igniteEvent = getIgniteEvent();
        scheduleNotificationAssistant.deleteScheduleNotifications(USER_ID, VEHICLE_ID, igniteEvent);
        verify(notificationBufferDao, Mockito.times(0)).deleteByUserIdAndVehicleId(any(), any());
        verify(notificationBufferDao, Mockito.times(1)).findByUserIdAndVehicleId(any(), any());
    }

    @NotNull
    private IgniteEventImpl getIgniteEvent() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(VEHICLE_ID);
        return igniteEvent;
    }

    @Test
    public void deleteScheduleNotificationsSuccess() {
        IgniteEventImpl igniteEvent = getIgniteEvent();
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);

        doReturn(Collections.singletonList(notificationBuffer)).when(notificationBufferDao)
            .findByUserIdAndVehicleId(any(), any());
        doReturn(MSG_ID_GEN).when(msgIdGen).generateUniqueMsgId(any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        doNothing().when(notificationBufferDao).deleteByUserIdAndVehicleId(any(), any());

        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.deleteScheduleNotifications(USER_ID, VEHICLE_ID, igniteEvent);

        verify(notificationBufferDao, Mockito.times(1)).findByUserIdAndVehicleId(any(), any());
        verify(ctxt, Mockito.times(1)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(1)).deleteByUserIdAndVehicleId(any(), any());
        igniteEvent = igniteEventArgumentCaptor.getValue();
        assertEquals(DELETE_SCHEDULE_EVENT, igniteEvent.getEventId());
        assertEquals(VEHICLE_ID, igniteEvent.getVehicleId());
        assertEquals(VEHICLE_ID, igniteEvent.getSourceDeviceId());
        assertEquals(MSG_ID_GEN, igniteEvent.getMessageId());
        assertEquals(SCHEDULER_ID, ((DeleteScheduleEventData) igniteEvent.getEventData()).getScheduleId());
    }

    @Test
    public void snoozeAlertWithExistingScheduleEmailAttachment() throws IOException {

        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());

        GenericEventData genericEventData = new GenericEventData();
        ArrayList<EmailAttachment> emailAttachments = new ArrayList<>();
        EmailAttachment emailAttachment = new EmailAttachment("dummyFile", "dummyContent", "dummyMimeType", false);
        emailAttachments.add(emailAttachment);
        genericEventData.set("en-US", emailAttachments);
        IgniteEventImpl igniteEvent = getIgniteEvent();
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);
        IgniteEventImpl igniteEventBuffer = getIgniteEvent();
        igniteEventBuffer.setEventData(genericEventData);
        alertsInfo.setIgniteEvent(igniteEventBuffer);
        doReturn(notificationBuffer).when(notificationBufferDao)
                .findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(), any());
        doReturn(true).when(notificationBufferDao).update(notificationBufferArgumentCaptor.capture());
        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.snoozeAlert(new IgniteStringKey("schedule-notification-key"), igniteEvent,
                alertsInfo, "contactId", ChannelType.EMAIL, null, "temp@a.com");

        verify(notificationBufferDao, Mockito.times(1)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
                any(), any(), any());
        verify(ctxt, Mockito.times(0)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(1)).update(any());
        verify(notificationBufferDao, Mockito.times(0)).save(any());

        NotificationBuffer notificationBufferCaptor = notificationBufferArgumentCaptor.getValue();
        assertEquals(2, notificationBufferCaptor.getAlertsInfo().size());
        assertEquals(ChannelType.EMAIL, notificationBufferCaptor.getChannelType());
    }

    @Test
    public void snoozeAlertWithExistingSchedule() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();

        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());

        GenericEventData genericEventData = new GenericEventData();
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);
        IgniteEventImpl igniteEventBuffer = getIgniteEvent();
        igniteEventBuffer.setEventData(genericEventData);
        alertsInfo.setIgniteEvent(igniteEventBuffer);
        doReturn(notificationBuffer).when(notificationBufferDao)
            .findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(), any());
        doReturn(true).when(notificationBufferDao).update(notificationBufferArgumentCaptor.capture());
        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.snoozeAlert(new IgniteStringKey("schedule-notification-key"), igniteEvent,
            alertsInfo, "contactId", ChannelType.EMAIL, null, "temp@a.com");

        verify(notificationBufferDao, Mockito.times(1)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
            any(), any(), any());
        verify(ctxt, Mockito.times(0)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(1)).update(any());
        verify(notificationBufferDao, Mockito.times(0)).save(any());

        NotificationBuffer notificationBufferCaptor = notificationBufferArgumentCaptor.getValue();
        assertEquals(2, notificationBufferCaptor.getAlertsInfo().size());
        assertEquals(ChannelType.EMAIL, notificationBufferCaptor.getChannelType());
    }

    @Test
    public void snoozeAlertWithExistingScheduleEmptyId() throws IOException {
        IgniteEventImpl igniteEvent = getIgniteEvent();
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);
        notificationBuffer.setSchedulerId(null);
        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());

        doReturn(notificationBuffer).when(notificationBufferDao)
            .findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(), any());
        doReturn(true).when(notificationBufferDao).update(notificationBufferArgumentCaptor.capture());
        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.snoozeAlert(new IgniteStringKey("schedule-notification-key"), igniteEvent,
            alertsInfo, "contactId", ChannelType.EMAIL, null, "temp@a.com");

        verify(notificationBufferDao, Mockito.times(1)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
            any(), any(), any());
        verify(ctxt, Mockito.times(0)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(0)).update(any());
        verify(notificationBufferDao, Mockito.times(0)).save(any());
    }

    @Test
    public void snoozeAlertWithNewSchedule() throws IOException {

        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone("America/New_York");

        List<EmailAttachment> emailAttachments = new ArrayList<>();
        EmailAttachment emailAttachment = new EmailAttachment("/tmp/fff.txt",
            Base64.encodeBase64String("aaa".getBytes(StandardCharsets.UTF_8)), "MP3", false);
        emailAttachments.add(emailAttachment);
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("en-US", emailAttachments);
        IgniteEventImpl igniteEventBuffer = getIgniteEvent();
        igniteEventBuffer.setEventData(genericEventData);
        alertsInfo.setIgniteEvent(igniteEventBuffer);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndDate(LocalDate.now());
        suppressionConfig.setEndTime(LocalTime.now());

        IgniteEventImpl igniteEvent = getIgniteEvent();
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);

        doReturn(null).when(notificationBufferDao)
            .findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(), any());
        doReturn(notificationBuffer).when(notificationBufferDao).save(notificationBufferArgumentCaptor.capture());
        doReturn(userProfile).when(userProfileDao).findById(any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());

        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.snoozeAlert(new IgniteStringKey("schedule-notification-key"), igniteEvent,
            alertsInfo, "contactId", ChannelType.EMAIL, suppressionConfig, "temp@a.com");

        verify(notificationBufferDao, Mockito.times(1)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
            any(), any(), any());
        verify(ctxt, Mockito.times(1)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(0)).update(any());
        verify(notificationBufferDao, Mockito.times(1)).save(any());
        verify(userProfileDao, Mockito.times(1)).findById(any());
        assertEquals("/tmp/fff.txt",
            ((List<Map>) ((GenericEventData) notificationBufferArgumentCaptor.getValue().getAlertsInfo().get(0)
                .getIgniteEvent().getEventData()).getData().get("en-US")).get(0).get("fileName"));
        assertEquals(VEHICLE_ID, notificationBufferArgumentCaptor.getValue().getVehicleId());
    }

    @Test
    public void snoozeAlertWithNewScheduleWithoutAttachmentAndVehicleId() throws IOException {

        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone("America/New_York");
        GenericEventData genericEventData = new GenericEventData();
        IgniteEventImpl igniteEvent = getIgniteEvent();
        IgniteEventImpl igniteEventBuffer = getIgniteEvent();
        igniteEventBuffer.setEventData(genericEventData);
        alertsInfo.setIgniteEvent(igniteEventBuffer);
        igniteEvent.setVehicleId(null);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndDate(LocalDate.now());
        suppressionConfig.setEndTime(LocalTime.now());
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);

        doReturn(null).when(notificationBufferDao)
            .findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(), any());
        doReturn(notificationBuffer).when(notificationBufferDao).save(notificationBufferArgumentCaptor.capture());
        doReturn(userProfile).when(userProfileDao).findById(any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());

        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.snoozeAlert(new IgniteStringKey("schedule-notification-key"), igniteEvent,
            alertsInfo, "contactId", ChannelType.EMAIL, suppressionConfig, "temp@a.com");

        verify(notificationBufferDao, Mockito.times(1)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
            any(), any(), any());
        verify(ctxt, Mockito.times(1)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(0)).update(any());
        verify(notificationBufferDao, Mockito.times(1)).save(any());
        verify(userProfileDao, Mockito.times(1)).findById(any());
        assertNull("/tmp/fff.txt",
            ((GenericEventData) notificationBufferArgumentCaptor.getValue().getAlertsInfo().get(0).getIgniteEvent()
                .getEventData()).getData().get("en-US"));
        assertEquals(VEHICLE_ID_FOR_DEFAULT_PREFERENCE, notificationBufferArgumentCaptor.getValue().getVehicleId());
        assertEquals(VEHICLE_ID_FOR_DEFAULT_PREFERENCE, igniteEventArgumentCaptor.getValue().getVehicleId());
        assertEquals(VEHICLE_ID_FOR_DEFAULT_PREFERENCE, igniteEventArgumentCaptor.getValue().getSourceDeviceId());
    }

    @Test
    public void snoozeAlertWithNewScheduleUserNotification() throws IOException {

        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("testUser");
        userProfile.setTimeZone("America/New_York");
        GenericEventData genericEventData = new GenericEventData();

        IgniteEventImpl igniteEvent = getIgniteEvent();
        IgniteEventImpl igniteEventBuffer = getIgniteEvent();
        igniteEventBuffer.setEventData(genericEventData);
        alertsInfo.setIgniteEvent(igniteEventBuffer);
        igniteEvent.setVehicleId("testUser");

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndDate(LocalDate.now());
        suppressionConfig.setEndTime(LocalTime.now());
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);

        doReturn(null).when(notificationBufferDao)
            .findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(), any(), any(), any());
        doReturn(notificationBuffer).when(notificationBufferDao).save(notificationBufferArgumentCaptor.capture());
        doReturn(userProfile).when(userProfileDao).findById(any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());

        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.snoozeAlert(new IgniteStringKey("schedule-notification-key"), igniteEvent,
            alertsInfo, "contactId", ChannelType.EMAIL, suppressionConfig, "temp@a.com");

        verify(notificationBufferDao, Mockito.times(1)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
            any(), any(), any());
        verify(ctxt, Mockito.times(1)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(0)).update(any());
        verify(notificationBufferDao, Mockito.times(1)).save(any());
        verify(userProfileDao, Mockito.times(1)).findById(any());
        assertNull("/tmp/fff.txt",
            ((GenericEventData) notificationBufferArgumentCaptor.getValue().getAlertsInfo().get(0).getIgniteEvent()
                .getEventData()).getData().get("en-US"));
        assertEquals(VEHICLE_ID_FOR_DEFAULT_PREFERENCE, notificationBufferArgumentCaptor.getValue().getVehicleId());
        assertEquals("testUser", igniteEventArgumentCaptor.getValue().getVehicleId());
        assertEquals("testUser", igniteEventArgumentCaptor.getValue().getSourceDeviceId());
    }

    @Test
    public void updateScheduler() throws IOException {

        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone("America/New_York");

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndDate(LocalDate.now());
        suppressionConfig.setEndTime(LocalTime.now());

        IgniteEventImpl igniteEvent = getIgniteEvent();

        doReturn(userProfile).when(userProfileDao).findById(any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());

        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);

        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.updateScheduler(suppressionConfig, SnsUtils.getNotificationConfig(),
            ChannelType.EMAIL, notificationBuffer, new IgniteStringKey("schedule-notification-key"), igniteEvent,
            "temp@a.com");

        verify(notificationBufferDao, Mockito.times(0)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
            any(), any(), any());
        verify(ctxt, Mockito.times(1)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(0)).update(any());
        verify(notificationBufferDao, Mockito.times(0)).save(any());
        verify(userProfileDao, Mockito.times(1)).findById(any());
        assertEquals(CREATE_SCHEDULE_EVENT, igniteEventArgumentCaptor.getValue().getEventId());
        assertTrue(10828000
            <  ((CreateScheduleEventData) igniteEventArgumentCaptor.getValue().getEventData()).getInitialDelayMs());
    }

    @Test
    public void updateSchedulerDefaultTimezone() throws IOException {

        AlertsInfo alertsInfo = SnsUtils.getAlertsInfo();
        alertsInfo.setNotificationTemplateConfig(new NotificationTemplateConfig());

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndDate(LocalDate.now());
        suppressionConfig.setEndTime(LocalTime.now());
        UserProfile userProfile = new UserProfile();

        doReturn(userProfile).when(userProfileDao).findById(any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());

        IgniteEventImpl igniteEvent = getIgniteEvent();
        NotificationBuffer notificationBuffer = getNotificationBuffer(igniteEvent);

        scheduleNotificationAssistant.init(ctxt);
        scheduleNotificationAssistant.updateScheduler(suppressionConfig, SnsUtils.getNotificationConfig(),
            ChannelType.EMAIL, notificationBuffer, new IgniteStringKey("schedule-notification-key"), igniteEvent,
            "temp@a.com");

        verify(notificationBufferDao, Mockito.times(0)).findByUserIDVehicleIDChannelTypeGroupContactId(any(), any(),
            any(), any(), any());
        verify(ctxt, Mockito.times(1)).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        verify(notificationBufferDao, Mockito.times(0)).update(any());
        verify(notificationBufferDao, Mockito.times(0)).save(any());
        verify(userProfileDao, Mockito.times(1)).findById(any());
        assertEquals(CREATE_SCHEDULE_EVENT, igniteEventArgumentCaptor.getValue().getEventId());
        assertTrue(25509000
            > ((CreateScheduleEventData) igniteEventArgumentCaptor.getValue().getEventData()).getInitialDelayMs());
    }

    @Test
    public void enforceSuppressionVacationInAffect() {
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone("America/New_York");
        userProfile.setUserId(USER_ID);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndDate(LocalDate.now().plusWeeks(1));
        suppressionConfig.setEndTime(LocalTime.now().plusHours(1));
        suppressionConfig.setStartDate(LocalDate.now().minusWeeks(1));
        suppressionConfig.setStartTime(LocalTime.now().minusHours(1));

        doReturn(userProfile).when(userProfileDao).findById(any());

        scheduleNotificationAssistant.init(ctxt);
        SuppressionConfig result =
            scheduleNotificationAssistant.enforceSuppression(Collections.singletonList(suppressionConfig), USER_ID);

        assertNotNull(result);
    }

    @Test
    public void enforceSuppressionVacationNotInAffect() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(USER_ID);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndDate(LocalDate.now().plusWeeks(1));
        suppressionConfig.setEndTime(LocalTime.now().plusHours(1));
        suppressionConfig.setStartDate(LocalDate.now().plusDays(2));
        suppressionConfig.setStartTime(LocalTime.now().plusHours(2));

        doReturn(userProfile).when(userProfileDao).findById(any());

        scheduleNotificationAssistant.init(ctxt);
        SuppressionConfig result =
            scheduleNotificationAssistant.enforceSuppression(Collections.singletonList(suppressionConfig), USER_ID);

        assertNull(result);
    }

    @Test
    public void enforceSuppressionRecurringInAffect() {
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone(ZoneId.systemDefault().toString());
        userProfile.setUserId(USER_ID);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.RECURRING);
        suppressionConfig.setDays(Arrays.asList(DayOfWeek.values()));
        suppressionConfig.setEndTime(LocalTime.now().plusHours(1));
        suppressionConfig.setStartTime(LocalTime.now().minusHours(1));

        doReturn(userProfile).when(userProfileDao).findById(any());

        scheduleNotificationAssistant.init(ctxt);
        SuppressionConfig result =
            scheduleNotificationAssistant.enforceSuppression(Collections.singletonList(suppressionConfig), USER_ID);

        assertNotNull(result);
    }

    @Test
    public void enforceSuppressionRecurringNotInAffect() {
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone(ZoneId.systemDefault().toString());
        userProfile.setUserId(USER_ID);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.RECURRING);
        suppressionConfig.setDays(Arrays.asList(DayOfWeek.values()));
        suppressionConfig.setEndTime(LocalTime.now().plusHours(2));
        suppressionConfig.setStartTime(LocalTime.now().plusHours(1));

        doReturn(userProfile).when(userProfileDao).findById(any());

        scheduleNotificationAssistant.init(ctxt);
        SuppressionConfig result =
            scheduleNotificationAssistant.enforceSuppression(Collections.singletonList(suppressionConfig), USER_ID);

        assertNull(result);
    }

    @Test
    public void calculateQpDurationRecurring() {
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone(ZoneId.systemDefault().toString());
        userProfile.setUserId(USER_ID);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.RECURRING);
        suppressionConfig.setDays(Arrays.asList(DayOfWeek.values()));
        suppressionConfig.setEndTime(LocalTime.of(23, 0));
        suppressionConfig.setStartTime(LocalTime.of(20, 0));

        long result =
            ScheduleNotificationAssistant.calculateQpDuration(LocalDateTime.of(2021, 1, 1, 22, 0), suppressionConfig);

        assertEquals(3645, result);
    }

    @Test
    public void calculateQpDurationRecurringOverNight() {
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone(ZoneId.systemDefault().toString());
        userProfile.setUserId(USER_ID);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.RECURRING);
        suppressionConfig.setDays(Arrays.asList(DayOfWeek.values()));
        suppressionConfig.setEndTime(LocalTime.of(6, 0));
        suppressionConfig.setStartTime(LocalTime.of(20, 0));

        long result =
            ScheduleNotificationAssistant.calculateQpDuration(LocalDateTime.of(2021, 1, 1, 22, 0), suppressionConfig);

        assertEquals(8 * 3600 + 45, result);
    }

    @Test
    public void calculateQpDurationVacation() {
        UserProfile userProfile = new UserProfile();
        userProfile.setTimeZone(ZoneId.systemDefault().toString());
        userProfile.setUserId(USER_ID);

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        suppressionConfig.setEndTime(LocalTime.of(23, 0));
        suppressionConfig.setEndDate(LocalDate.of(2021, 1, 1));
        suppressionConfig.setStartTime(LocalTime.of(20, 0));
        suppressionConfig.setStartDate(LocalDate.of(2021, 1, 1));

        long result =
            ScheduleNotificationAssistant.calculateQpDuration(LocalDateTime.of(2021, 1, 1, 22, 0), suppressionConfig);

        assertEquals(3645, result);
    }

    @NotNull
    private NotificationBuffer getNotificationBuffer(IgniteEventImpl igniteEvent) {
        NotificationBuffer notificationBuffer = new NotificationBuffer();
        notificationBuffer.setSchedulerId(SCHEDULER_ID);
        notificationBuffer.setChannelType(ChannelType.EMAIL);
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
        ArrayList<BufferedAlertsInfo> bufferedAlertsInfoArrayList = new ArrayList<>();
        bufferedAlertsInfoArrayList.add(bufferedAlertsInfo);
        notificationBuffer.setAlertsInfo(bufferedAlertsInfoArrayList);
        return notificationBuffer;
    }
}