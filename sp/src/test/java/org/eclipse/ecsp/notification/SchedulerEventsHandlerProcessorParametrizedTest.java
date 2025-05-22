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
import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.processor.api.Record;
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
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiPredicate;

import static org.mockito.Mockito.when;

/**
 * SchedulerEventsHandlerProcessorParametrizedTest class.
 */
@RunWith(Parameterized.class)
public class SchedulerEventsHandlerProcessorParametrizedTest {

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
    private SchedulerEventsHandlerProcessor.NotificationRequestConstructor notificationRequestConstructor;

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

    private final String payload;

    /**
     * SchedulerEventsHandlerProcessorParametrizedTest constructor.
     *
     * @param payload String
     */
    public SchedulerEventsHandlerProcessorParametrizedTest(String payload) {
        this.payload = payload;
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"/schedule-op-status-event.json"},
                {"/schedule-notification-event.json"},
                {"/schedule-notification-invalid-event.json"},
                {"/delete-op-scheduled-notification.json"},

        });

    }

    /**
     * setup method.
     *
     * @throws Exception generic exception
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
    public void testScheduleOpStatusEventProcess() throws IOException {
        String notificationSettingsData = IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream(payload), "UTF-8");
        IgniteEvent event = igniteEventTransformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        Assertions.assertNotNull(event);
        schedulerEventsHandlerProcessor.process(
                new Record(new IgniteStringKey("scheduled-notification-key"), event, System.currentTimeMillis()));
    }
}
