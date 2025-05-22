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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.NotificationRetryEventDataV2;
import org.eclipse.ecsp.domain.notification.NotificationRetryHistory;
import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.config.TestSecurityLib;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.dao.NotificationRetryHistoryDao;
import org.eclipse.ecsp.notification.utils.RetryCacheClient;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiPredicate;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * NotificationRetryProcessorTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSecurityLib.class})
public class NotificationRetryProcessorTest {

    private static final String VEHICLE_ID = "vehicleIdVal";
    private static final String USER_ID = "userIdVal";

    private Properties properties;

    private GenericIgniteEventTransformer igniteEventTransformer = new GenericIgniteEventTransformer();

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    @InjectMocks
    private NotificationRetryProcessor notificationRetryProcessor;

    @Mock
    BiPredicate<IgniteEventStreamProcessor, StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;

    @Mock
    private RetryCacheClient cacheClient;

    @Mock
    private NotificationRetryHistoryDao notificationRetryHistoryDao;

    @Mock
    private AlertsHistoryDao alertsHistoryDao;

    @Mock
    private NotificationRetryAssistant notificationRetryAssistant;

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;

    /**
     * test set up.
     */
    @Before
    public void setup() throws Exception {
        properties = NotificationTestUtils.loadProperties("/application.properties");
        MockitoAnnotations.initMocks(this);
        Mockito.when(skipProcessorPredicate.test(any(), any())).thenReturn(false);
        Mockito.when(ctxt.streamName()).thenReturn("notification-retry-processor");
        notificationRetryProcessor.setSourceTopic(properties.getProperty("notification.retry.topic"));

        Mockito.doNothing().when(notificationRetryAssistant).updateAlertHistoryForRetry(any(), any());
        Mockito.when(cacheClient.getRetryRecordForException(any(), any())).thenReturn(null);
        // Mockito.when(igniteEventTransformer.fromBlob(any(),
        // any())).thenReturn(getIgniteEvent());
        notificationRetryProcessor.setIgniteEventTransformer(igniteEventTransformer);
        notificationRetryProcessor.init(ctxt);
        Mockito.when(alertsHistoryDao.findById(any())).thenReturn(new AlertsHistoryInfo());
        Mockito.when(notificationRetryHistoryDao.findById(any())).thenReturn(new NotificationRetryHistory());

    }

    @Test
    public void processRetryNotification() throws JsonProcessingException {
        IgniteEventImpl igniteEvent = getRetryIgniteEvent();
        Mockito.doNothing().when(ctxt).forward(any(), any());
        notificationRetryProcessor.process(
            new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));

        Mockito.when(cacheClient.getRetryRecordForException(any(), any()))
            .thenReturn(new RetryRecord("org.eclipse.UserException", 2, 1, 20000));
        notificationRetryProcessor.process(
            new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        Mockito.verify(cacheClient, Mockito.times(2)).getRetryRecordForException(any(), any());
        NotificationRetryEventDataV2 data = (NotificationRetryEventDataV2) igniteEvent.getEventData();
        data.setRetryRecord(null);
        igniteEvent.setEventData(data);
        notificationRetryProcessor.process(
            new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));

    }

    @Test
    public void processRetryExhaust() throws JsonProcessingException {

        Mockito.when(cacheClient.getRetryRecordForException(any(), any()))
            .thenReturn(new RetryRecord("org.eclipse.UserException", 2, 2, 20000));
        IgniteEventImpl igniteEvent = getRetryIgniteEvent();
        Mockito.doNothing().when(ctxt).forward(any(), any());
        notificationRetryProcessor.process(
            new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
        verify(cacheClient, Mockito.times(1)).deleteRetryRecord(any(), any());
    }

    @Test
    public void processScheduleOpsEvent() throws IOException {
        String notificationSettingsData = IOUtils.toString(
            NotificationRetryProcessorTest.class.getResourceAsStream("/schedule-op-status-retry-event.json"), "UTF-8");
        GenericIgniteEventTransformer transformer = new GenericIgniteEventTransformer();
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        notificationRetryProcessor.process(
            new Record(new IgniteStringKey(VEHICLE_ID), event, System.currentTimeMillis()));

        String notificationSettingsData1 = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/schedule-notification-invalid-event.json"),
            "UTF-8");
        IgniteEvent event1 = igniteEventTransformer.fromBlob(notificationSettingsData1.getBytes(), Optional.empty());
        Assertions.assertNotNull(event1);
        notificationRetryProcessor.process(
            new Record(new IgniteStringKey(VEHICLE_ID), event1, System.currentTimeMillis()));
    }

    @Test
    public void testSources() throws IOException {
        String[] sourceTopics = properties.getProperty("notification.retry.topic").split(",");
        assertArrayEquals(sourceTopics, notificationRetryProcessor.sources());
    }

    @Test
    public void testName() {
        assertEquals("notification-retry-processor", notificationRetryProcessor.name());
    }

    @Test
    public void processScheduleNotification() {
        IgniteEventImpl igniteEvent = getIgniteEvent();
        Mockito.when(skipProcessorPredicate.test(any(), any())).thenReturn(true);
        Mockito.doNothing().when(ctxt).forward(any(), any());
        NotificationRetryProcessor notificationRetryProcessorSpy = spy(notificationRetryProcessor);
        notificationRetryProcessorSpy.process(
            new Record(new IgniteStringKey(VEHICLE_ID), igniteEvent, System.currentTimeMillis()));
    }

    @NotNull
    private IgniteEventImpl getRetryIgniteEvent() throws JsonProcessingException {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId(EventID.RETRY_NOTIFICATION_EVENT);
        igniteEvent.setVersion(Version.V2_0);
        igniteEvent.setRequestId("requestid001");
        NotificationRetryEventDataV2 eventdata = new NotificationRetryEventDataV2();
        eventdata.setOriginalEvent(getIgniteEvent());
        eventdata.setOriginalEventTopic("notification");
        eventdata.setRetryRecord(new RetryRecord("java.lang.RuntimeException", 2, 0, 10000));
        igniteEvent.setEventData(eventdata);
        return igniteEvent;
    }

    @NotNull
    private IgniteEventImpl getIgniteEvent() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(VEHICLE_ID);
        igniteEvent.setRequestId("requestId");
        igniteEvent.setEventId("GenericNotificationEvent");
        igniteEvent.setVersion(Version.V1_0);
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", "lowFuel");
        genericEventData.set("userId", USER_ID);
        genericEventData.set("UserNotification", false);
        igniteEvent.setEventData(genericEventData);
        return igniteEvent;
    }

}
