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
import org.eclipse.ecsp.analytics.stream.base.idgen.MessageIdGenerator;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.NotificationRetryEventData;
import org.eclipse.ecsp.domain.notification.NotificationRetryHistory;
import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.dao.NotificationRetryHistoryDao;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * NotificationRetryAssistantTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class NotificationRetryAssistantTest {

    static final String USER_ID = "userId";
    static final String VEHICLE_ID = "vehicleId";

    @InjectMocks
    NotificationRetryAssistant notificationRetryAssistant;

    @Mock
    private NotificationRetryHistoryDao notificationRetryHistoryDao;

    @Mock
    private MessageIdGenerator msgIdGen;

    @Captor
    private ArgumentCaptor<IgniteEventImpl> igniteEventArgumentCaptor;

    AlertsHistoryInfo alertHistory;

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        alertHistory = new AlertsHistoryInfo();
    }

    @Test
    public void scheduleRetryTest() throws JsonProcessingException {
        RetryRecord rc = new RetryRecord("java.lang.RuntimeException", 2, 0, 10000);
        notificationRetryAssistant.updateAlertHistoryForRetry(alertHistory, rc);
        assertEquals(1, alertHistory.getRetryRecordList().size());

        RetryRecord rcExist = new RetryRecord("java.lang.IOException", 2, 1, 10000);
        List<RetryRecord> rcLst = new ArrayList<>();
        rcLst.add(rcExist);
        alertHistory.setRetryRecordList(rcLst);
        notificationRetryAssistant.updateAlertHistoryForRetry(alertHistory, rc);
        assertEquals(2, alertHistory.getRetryRecordList().size());

        notificationRetryAssistant.updateAlertHistoryForRetry(alertHistory, rc);
        assertEquals(2, alertHistory.getRetryRecordList().size());
        assertEquals(2, alertHistory.getRetryRecordList().get(1).getRetryCount());

        notificationRetryAssistant.updateAlertHistoryForRetry(alertHistory, rc);
        assertEquals(2, alertHistory.getRetryRecordList().get(1).getRetryCount());

        assertNotNull(notificationRetryAssistant.createRetryNotificationEvent(getIgniteEvent(), rc, "dummy-topic"));

        assertNotNull(
            notificationRetryAssistant.createCreateScheduleEvent(new IgniteStringKey(VEHICLE_ID), getRetryIgniteEvent(),
                20000,
                "dummy-topic"));

    }

    @Test
    public void updateRetryHistoryTest() {
        String requestId = "77cb7c51-5813-11ec-ab0b-45a84d2299d0";
        RetryRecord rc = new RetryRecord("java.lang.RuntimeException", 2, 2, 10000);
        Mockito.when(notificationRetryHistoryDao.findById(requestId)).thenReturn(null);
        notificationRetryAssistant.updateNotificationRetryHistory(requestId, rc, VEHICLE_ID);


        String requestId1 = "0b303b91-5849-11ec-85a5-f3182ad1e968";
        NotificationRetryHistory history = new NotificationRetryHistory();
        history.setRequestId(requestId1);
        List<RetryRecord> retryRecordsList = new ArrayList<>();
        retryRecordsList.add(new RetryRecord("java.lang.RuntimeException", 2, 1, 10000));
        history.setRetryRecordsList(retryRecordsList);
        Mockito.when(notificationRetryHistoryDao.findById(requestId1)).thenReturn(history);
        notificationRetryAssistant.updateNotificationRetryHistory(requestId1, rc, VEHICLE_ID);

        notificationRetryAssistant.updateNotificationRetryHistory(requestId1,
            new RetryRecord("retryException", 2, 1, 10000), VEHICLE_ID);

        verify(notificationRetryHistoryDao, Mockito.times(3)).save(any());

    }

    @NotNull
    private IgniteEventImpl getRetryIgniteEvent() throws JsonProcessingException {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId(EventID.RETRY_NOTIFICATION_EVENT);
        igniteEvent.setRequestId("requestId");
        igniteEvent.setVersion(Version.V1_0);
        NotificationRetryEventData eventdata = new NotificationRetryEventData();
        eventdata.setOriginalEvent(MAPPER.writeValueAsBytes(getIgniteEvent()));
        eventdata.setOriginalEventTopic("notification");
        eventdata.setRetryRecord(new RetryRecord("java.lang.RuntimeException", 2, 0, 10000));
        igniteEvent.setEventData(eventdata);
        return igniteEvent;
    }

    @NotNull
    private IgniteEventImpl getIgniteEvent() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId("GenericNotificationEvent");
        igniteEvent.setVehicleId(VEHICLE_ID);
        igniteEvent.setRequestId("requestId01");
        igniteEvent.setVersion(Version.V1_0);
        return igniteEvent;
    }
}
