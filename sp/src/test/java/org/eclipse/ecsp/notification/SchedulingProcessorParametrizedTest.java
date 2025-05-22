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
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.processors.UserProfileEnricher;
import org.eclipse.ecsp.notification.processors.VehicleProfileEnricher;
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
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiPredicate;

import static org.mockito.Mockito.when;


/**
 * SchedulingProcessorParametrizedTest class.
 */
@RunWith(Parameterized.class)
public class SchedulingProcessorParametrizedTest {

    private SchedulingProcessor schedulingProcessor;

    private Properties properties;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private GenericIgniteEventTransformer transformer = new GenericIgniteEventTransformer();

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessingContext;

    @Mock
    private ScheduleNotificationAssistant scheduleNotificationAssistant;

    @Mock
    private BiPredicate<IgniteEventStreamProcessor,
            StreamProcessingContext<IgniteKey<?>,
                    IgniteEvent>> skipProcessorPredicate = (a, b) -> false;

    @Mock
    private AlertsHistoryAssistant alertsHistoryAssistant;

    @Mock
    private UserProfileEnricher userProfileEnricher;

    @Mock
    private VehicleProfileEnricher vehicleProfileEnricher;

    @Mock
    private Data alertsData;

    @Mock
    private UserProfile userProfile;

    @Mock
    AlertsInfo alertsInfo = Mockito.mock(AlertsInfo.class);

    @Mock
    AlertsHistoryDao alertHistoryDao;

    @Mock
    AlertsHistoryInfo alertsHistoryInfo;

    @Mock
    IgniteEventImpl igniteEventImpl;

    private final String payload;

    /**
     * SchedulingProcessorParametrizedTest constructor.
     *
     * @param payload String
     */
    public SchedulingProcessorParametrizedTest(String payload) {
        this.payload = payload;
    }


    /**
     * set up.
     */
    @Before
    public void setup() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        properties = NotificationTestUtils.loadProperties("/application.properties");
        // set mock objects
        prepareMocks();
        schedulingProcessor = new SchedulingProcessor();
        String[] sourceTopics = properties.getProperty("scheduling.processor.topic.sources").split(",");
        String schedulerCallbackTopic = properties.getProperty("scheduling.processor.topic.scheduler.callback");
        int maxScheduleDays = Integer.parseInt(properties.getProperty("notification.schedule.max.period.days"));
        schedulingProcessor.setSourceTopics(sourceTopics);
        schedulingProcessor.setSkipProcessorPredicate(skipProcessorPredicate);
        schedulingProcessor.setMaxScheduleDays(maxScheduleDays);
        schedulingProcessor.setAlertsHistoryAssistant(alertsHistoryAssistant);
        schedulingProcessor.setUserProfileEnricher(userProfileEnricher);
        schedulingProcessor.setVehicleProfileEnricher(vehicleProfileEnricher);
        schedulingProcessor.setScheduleNotificationAssistant(scheduleNotificationAssistant);
        schedulingProcessor.setSchedulerCallbackTopic(schedulerCallbackTopic);
        schedulingProcessor.setDefaultTimezone("UTC");
        schedulingProcessor.setAlertHistoryDao(alertHistoryDao);
        schedulingProcessor.initConfig(properties);
        schedulingProcessor.init(streamProcessingContext);
    }

    private void prepareMocks() {
        when(scheduleNotificationAssistant.getSchedulerSourceTopic()).thenReturn("SomeRandomTopic");
        when(alertHistoryDao.findById("scheduler-id-001")).thenReturn(alertsHistoryInfo);
        StatusHistoryRecord statusHistoryRecord = Mockito.mock(StatusHistoryRecord.class);
        when(alertsHistoryInfo.currentStatus()).thenReturn(Optional.of(statusHistoryRecord));
        when(statusHistoryRecord.getStatus()).thenReturn(Status.SCHEDULED);
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"/scheduled-notification-data.json"},
                {"/scheduled-nonreg-vehiclenotification-data.json"},
                {"/invalid-scheduled-notification.json"},

        });

    }

    @Test
    public void testCreateProcess() throws IOException {
        String notificationSettingsData = IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream(payload), "UTF-8");
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        Assertions.assertNotNull(event);
        schedulingProcessor.process(
                new Record(new IgniteStringKey("scheduled-notification-key"), event, System.currentTimeMillis()));
    }
}
