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

import io.prometheus.client.CollectorRegistry;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.SchedulerEventsHandlerProcessor.NotificationRequestConstructor;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.config.TestSecurityLib;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiPredicate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


/**
 * SchedulerEventsHandlerProcessorTest.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSecurityLib.class})
public class SchedulerEventsHandlerProcessorTest {

    private SchedulerEventsHandlerProcessor schedulerEventsHandlerProcessor;

    private Properties properties;

    private GenericIgniteEventTransformer igniteEventTransformer = new GenericIgniteEventTransformer();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessingContext;

    @Mock
    private ScheduleNotificationAssistant scheduleNotificationAssistant;

    @Mock
    private BiPredicate<IgniteEventStreamProcessor,
            StreamProcessingContext<IgniteKey<?>,
                    IgniteEvent>> skipProcessorPredicate = (a, b) -> false;

    @Mock
    private AlertsHistoryDao alertsHistoryDao;

    @Mock
    private AlertsHistoryInfo alertsHistoryInfo;

    @Mock
    private AlertsInfo alertsInfo;

    @Mock
    private NotificationRequestConstructor notificationRequestConstructor;

    @Mock
    private IgniteEventImpl igniteEventImpl;

    @Mock
    private GenericEventData genericEventData;

    @Mock
    private Map<Object, Object> dataMap;

    @Mock
    private Data alertsData;

    @Mock
    private VehicleProfileAbridged vehicleProfile;

    /**
     * test set up.
     */
    @Before
    public void setup() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        properties = NotificationTestUtils.loadProperties("/application.properties");

        prepareMocks();
        // set mock objects
        schedulerEventsHandlerProcessor = new SchedulerEventsHandlerProcessor();
        String[] sourceTopics = properties.getProperty("scheduling.processor.topic.scheduler.callback").split(",");
        String notificationTopic = properties.getProperty("source.topic.name").split(",")[0];
        schedulerEventsHandlerProcessor.setAlertsHistoryDao(alertsHistoryDao);
        schedulerEventsHandlerProcessor.setIgniteEventTransformer(igniteEventTransformer);
        schedulerEventsHandlerProcessor.setNotificationTopic(notificationTopic);
        schedulerEventsHandlerProcessor.setScheduleNotificationAssistant(scheduleNotificationAssistant);
        schedulerEventsHandlerProcessor.setSourceTopics(sourceTopics);
        schedulerEventsHandlerProcessor.setNotificationRequestConstructor(notificationRequestConstructor);
        schedulerEventsHandlerProcessor.initConfig(properties);
        schedulerEventsHandlerProcessor.init(streamProcessingContext);
        schedulerEventsHandlerProcessor.setSkipProcessorPredicate(skipProcessorPredicate);
    }

    /**
     * prepare mocks.
     */
    public void prepareMocks() {
        when(alertsHistoryDao.findById("make-costa-great-again")).thenReturn(alertsHistoryInfo);
        when(alertsHistoryDao.findById("valid-requestID")).thenReturn(alertsHistoryInfo);
        when(alertsHistoryDao.findById("5dd6a4d3-1331-43bf-b291-20a3dbb80a01")).thenReturn(alertsHistoryInfo);
        when(alertsHistoryInfo.getPayload()).thenReturn(alertsInfo);
        when(notificationRequestConstructor.apply(alertsHistoryInfo)).thenReturn(igniteEventImpl);
        when(alertsInfo.getIgniteEvent()).thenReturn(igniteEventImpl);
        when(igniteEventImpl.getEventData()).thenReturn(genericEventData);
        when(genericEventData.getData()).thenReturn(dataMap);
        when(dataMap.get("notificationId")).thenReturn("valid-notificationID");
        when(alertsInfo.getAlertsData()).thenReturn(alertsData);
        when(alertsData.getVehicleProfile()).thenReturn(vehicleProfile);
        when(vehicleProfile.getVehicleId()).thenReturn("HBASCVGSDA");
        StatusHistoryRecord statusHistoryRecord = Mockito.mock(StatusHistoryRecord.class);
        when(alertsHistoryInfo.currentStatus()).thenReturn(Optional.of(statusHistoryRecord));
        when(statusHistoryRecord.getStatus()).thenReturn(Status.SCHEDULED);
    }

    @Test
    public void testInit() {
        Assertions.assertNotNull(streamProcessingContext);
        schedulerEventsHandlerProcessor.init(streamProcessingContext);
    }

    @Test
    public void testSources() throws IOException {
        String[] sourceTopics = properties.getProperty("scheduling.processor.topic.scheduler.callback").split(",");
        assertArrayEquals(sourceTopics, schedulerEventsHandlerProcessor.sources());
    }

    @Test
    public void testInitConfig() {
        Assertions.assertNotNull(properties);
        schedulerEventsHandlerProcessor.initConfig(properties);
    }

    @Test
    public void testName() {
        assertEquals("scheduler-events-callback-processor", schedulerEventsHandlerProcessor.name());
    }

    @Test
    public void testRetrieveAlertsHistoryInfoFound() {
        assertEquals(schedulerEventsHandlerProcessor.retrieveAlertsHistoryInfo("valid-requestID"), alertsHistoryInfo);

    }

    @Test
    public void testRetrieveAlertsHistoryInfoNotFound() {
        Exception exception = assertThrows(Exception.class,
            () -> schedulerEventsHandlerProcessor.retrieveAlertsHistoryInfo("invalid-requestID"));
        assertTrue(exception.getMessage().contains("No initial alert history found with id"));
    }

    @Test
    public void testResolveEventPayload() {
        String notificationPayload = new String(
            "{\"EventID\":\"GenericNotificationEvent\",\"Version\":\"1.0\",\"Timestamp\":16022243530,"
                + "\"Data\":{\"customExtension\":null,\"schedule\":\"2020-10-14T07:58:30.996\",\"campaignId\":null,"
                + "\"UserNotification\":false,\"name\":\"moshe\",\"notificationId\":\"aaa1\",\"userId\":\"costa1\","
                + "\"age\":\"43\"},\"RequestId\":\"request1\",\"SourceDeviceId\":null,"
                + "\"VehicleId\":\"HU4COSTA2217012187778492\",\"trcCtx\":null,\"Timezone\":330,\"DFFQualifier\":null,"
                + "\"MessageId\":null,\"CorrelationId\":null,\"BizTransactionId\":\"biztrn1\",\"BenchMode\":null,"
                + "\"UserContext\":null,\"ecuType\":\"\",\"mqttTopic\":\"\",\"DeviceDeliveryCutoff\":-1,"
                + "\"DuplicateMessage\":false}");
        IgniteEvent expectedIgniteEvent = igniteEventTransformer.fromBlob(notificationPayload.getBytes(),
            Optional.empty());

        assertTrue(EqualsBuilder.reflectionEquals(
            NotificationUtils.resolveEventPayload(notificationPayload, igniteEventTransformer), expectedIgniteEvent));
    }

    @Test
    public void testResolveEventPayloadRuntimeError() {
        String notificationPayload = new String(
            "{\"EventID\":\"GenericNotificationEvent\",\"Version\":\"1.0\",\"Timestamp\":16022243530,"
                + "\"Data\":{\"customExtension\":null,\"schedule\":\"2020-10-14T07:58:30.996\",\"campaignId\":null,"
                + "\"UserNotification\":false,\"name\":\"moshe\",\"notificationId\":\"aaa1\",\"userId\":\"costa1\","
                + "\"age\":\"43\"},\"SourceDeviceId\":null,\"VehicleId\":\"HU4COSTA2217012187778492\",\"trcCtx\":null,"
                + "\"Timezone\":330,\"DFFQualifier\":null,\"MessageId\":null,\"CorrelationId\":null,"
                + "\"BizTransactionId\":\"biztr1\",\"BenchMode\":null,\"UserContext\":null,\"ecuType\":\"\","
                + "\"mqttTopic\":\"\",\"DeviceDeliveryCutoff\":-1,\"DuplicateMessage\":false}");
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> NotificationUtils.resolveEventPayload(notificationPayload, igniteEventTransformer));
        assertTrue(exception.getMessage().contains("Error retrieving RequestId from scheduler payload"));
    }

    @Test
    public void handleEventReadyTest() throws IOException {
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/schedule-notification-event.json"), "UTF-8");
        IgniteEvent event = igniteEventTransformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        Assertions.assertNotNull(event);
        schedulerEventsHandlerProcessor.handleEventReady(
                new IgniteStringKey("scheduled-notification-key"), event);
    }

    @Test
    public void handleSchedulerResponseCallbackTest() throws IOException {
        String notificationSettingsData = IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream("/schedule-op-status-event.json"), "UTF-8");
        IgniteEvent event = igniteEventTransformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        Assertions.assertNotNull(event);
        schedulerEventsHandlerProcessor.handleSchedulerResponseCallback(
                new IgniteStringKey("scheduled-notification-key"), event);
    }

    @Test
    public void handleInvalidEventDataTest() throws IOException {
        String notificationSettingsData = IOUtils
                .toString(VehicleInfoNotificationTest.class.getResourceAsStream("/invalid-event-data.json"), "UTF-8");
        IgniteEvent event = igniteEventTransformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        Assertions.assertNotNull(event);
        schedulerEventsHandlerProcessor.handleSchedulerResponseCallback(
                new IgniteStringKey("dummy"), event);
    }

    @Test
    public void testIsEndOfSeriesMessageValid() throws IOException {
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/schedule-op-status-event.json"), "UTF-8");
        IgniteEvent event = igniteEventTransformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        ScheduleOpStatusEventData eventData = (ScheduleOpStatusEventData) event.getEventData();
        assertEquals(false, SchedulerEventsHandlerProcessor.isEndOfSeriesMessage(eventData));
    }

    @Test
    public void testIsEndOfSeriesMessageInValid() throws IOException {
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/schedule-op-status-event.json"), "UTF-8");
        IgniteEvent event = igniteEventTransformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        ScheduleOpStatusEventData eventData = (ScheduleOpStatusEventData) event.getEventData();
        eventData.setValid(false);
        eventData.setStatusErrorCode(ScheduleOpStatusEventData.ScheduleOpStatusErrorCode.EXPIRED_SCHEDULE);
        eventData.setStatus(ScheduleStatus.CREATE);
        assertEquals(true, SchedulerEventsHandlerProcessor.isEndOfSeriesMessage(eventData));
    }

    @Test
    public void notificationRequestConstructorTest() {
        NotificationRequestConstructor notificationRequestConstructor = new NotificationRequestConstructor();
        IgniteEventImpl igniteEvent = notificationRequestConstructor.apply(alertsHistoryInfo);
        assertEquals("HBASCVGSDA", igniteEvent.getVehicleId());
    }
}
