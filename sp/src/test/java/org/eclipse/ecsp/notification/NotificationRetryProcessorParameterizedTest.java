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

import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.utils.RetryCacheClient;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiPredicate;

import static org.mockito.ArgumentMatchers.any;

/**
 * NotificationRetryProcessorParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class NotificationRetryProcessorParameterizedTest {


    private final String payload;
    @InjectMocks
    private NotificationRetryProcessor notificationRetryProcessor;
    private static final String VEHICLE_ID = "vehicleIdVal";
    @Mock
    BiPredicate<IgniteEventStreamProcessor, StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;
    private Properties properties;
    @Mock
    private RetryCacheClient cacheClient;
    @Mock
    private NotificationRetryAssistant notificationRetryAssistant;
    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;
    private GenericIgniteEventTransformer igniteEventTransformer = new GenericIgniteEventTransformer();

    /**
     * NotificationRetryProcessorParameterizedTest constructor.
     *
     * @param payload String
     */
    public NotificationRetryProcessorParameterizedTest(String payload) {
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
                {"/schedule-notification-retry-event.json"},
                {"/schedule-op-status-retry-eventV2.json"},
                {"/schedule-notification-retry-eventV2.json"},

        });

    }

    /**
     * setup method.
     *
     * @throws Exception exception
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
    }

    @Test
    public void processRetryReadyEvent() throws IOException {
        String notificationSettingsData = IOUtils.toString(
                NotificationRetryProcessorTest.class.getResourceAsStream("/schedule-notification-retry-event.json"),
                "UTF-8");
        GenericIgniteEventTransformer transformer = new GenericIgniteEventTransformer();
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        Assertions.assertNotNull(event);
        notificationRetryProcessor.process(
                new Record(new IgniteStringKey(VEHICLE_ID), event, System.currentTimeMillis()));
    }
}
