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
import org.eclipse.ecsp.analytics.stream.base.idgen.MessageIdGenerator;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status;
import org.eclipse.ecsp.domain.notification.NotificationRetryEventDataV2;
import org.eclipse.ecsp.domain.notification.NotificationRetryHistory;
import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.dao.NotificationRetryHistoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.eclipse.ecsp.domain.EventID.CREATE_SCHEDULE_EVENT;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.FAILED;
import static org.eclipse.ecsp.events.scheduler.CreateScheduleEventData.RecurrenceType.CUSTOM_MS;
import static org.eclipse.ecsp.notification.VehicleInfoNotification.MAPPER;
import static org.eclipse.ecsp.notification.adaptor.NotificationUtils.SERVICE_NAME;

/**
 * This assistent class helps to prepare the alertHistory
 * for retry tracking and creating retry event to forward retry topic.
 *
 * @author AMuraleedhar
 *
 */
@Component
public class NotificationRetryAssistant {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRetryAssistant.class);

    private static final int FIRST_RETRY_ATTEMPT = 1;

    @Autowired
    @Qualifier("globalMessageIdGenerator")
    private MessageIdGenerator msgIdGen;

    @Autowired
    private NotificationRetryHistoryDao notificationRetryHistoryDao;

    @Value("${" + SERVICE_NAME + "}")
    private String serviceName;


    /**
     * Updates the alert history for retry event.
     *
     * @param alertHistory alert history
     * @param retryReord retry record
     */
    void updateAlertHistoryForRetry(AlertsHistoryInfo alertHistory, RetryRecord retryReord) {

        RetryRecord existingRecord = null;
        List<RetryRecord> retryRecordList = new ArrayList<>();

        for (RetryRecord rc : alertHistory.getRetryRecordList()) {
            if (rc.getRetryException().equalsIgnoreCase(retryReord.getRetryException())) {
                existingRecord = rc;
            } else {
                retryRecordList.add(rc);
            }
        }

        if (alertHistory.getRetryRecordList().isEmpty() || existingRecord == null) {
            // First retry so need to add retry record
            RetryRecord rc = new RetryRecord();
            rc.setRetryException(retryReord.getRetryException());
            rc.setMaxRetryLimit(retryReord.getMaxRetryLimit());
            rc.setRetryCount(FIRST_RETRY_ATTEMPT);
            rc.setRetryIntervalMs(retryReord.getRetryIntervalMs());
            alertHistory.getRetryRecordList().add(rc);
            alertHistory.addStatus(Status.RETRY_REQUESTED);
        } else {
            // Not a first retry hence check for current retry exception
            // kind and check exhaust and
            // update the record

            int currentRetryCount = existingRecord.getRetryCount();
            if (currentRetryCount < existingRecord.getMaxRetryLimit()) {
                existingRecord.setRetryCount(++currentRetryCount);
                retryRecordList.add(existingRecord);
                alertHistory.setRetryRecordList(retryRecordList);
                alertHistory.addStatus(Status.RETRY_REQUESTED);
            } else {
                alertHistory.addStatus(FAILED);
            }


        }
        LOGGER.debug("ALert history updated for retry event ");
    }

    /**
     * Creates retry notification event.
     *
     * @param originalEvent original event
     * @param retryRecord retry record
     * @param originalEventTopic original event topic
     * @return retry notification event
     * @throws JsonProcessingException JsonProcessingException
     */
    IgniteEvent createRetryNotificationEvent(IgniteEvent originalEvent, RetryRecord retryRecord,
                                             String originalEventTopic)
        throws JsonProcessingException {

        String vehicleId = originalEvent.getVehicleId();

        IgniteEventImpl retryNotificationEvent = new IgniteEventImpl();
        retryNotificationEvent.setVehicleId(vehicleId);
        retryNotificationEvent.setSourceDeviceId(vehicleId);
        retryNotificationEvent.setMessageId(msgIdGen.generateUniqueMsgId(vehicleId));

        retryNotificationEvent.setEventId(EventID.RETRY_NOTIFICATION_EVENT);
        retryNotificationEvent.setTimestamp(System.currentTimeMillis());

        retryNotificationEvent.setVersion(Version.V2_0);
        retryNotificationEvent.setRequestId(randomUUID().toString());
        retryNotificationEvent.setBizTransactionId(randomUUID().toString());

        NotificationRetryEventDataV2 eventData = new NotificationRetryEventDataV2();
        eventData.setRetryRecord(retryRecord);
        eventData.setOriginalEvent((IgniteEventImpl) originalEvent);
        eventData.setOriginalEventTopic(originalEventTopic);
        retryNotificationEvent.setEventData(eventData);
        return retryNotificationEvent;

    }

    /**
     * Updates the notification retry history.
     *
     * @param requestId request id
     * @param rec retry record
     * @param vehicleId vehicle id
     */
    void updateNotificationRetryHistory(String requestId, RetryRecord rec, String vehicleId) {
        NotificationRetryHistory retryHistory = Optional.ofNullable(notificationRetryHistoryDao.findById(requestId))
            .orElse(new NotificationRetryHistory());
        retryHistory.setRequestId(requestId);
        retryHistory.setVehicleId(vehicleId);
        if (retryHistory.getRetryRecordsList() == null || retryHistory.getRetryRecordsList().isEmpty()) {
            List<RetryRecord> retryRcdLst = new ArrayList<>();
            retryRcdLst.add(rec);
            retryHistory.setRetryRecordsList(retryRcdLst);
        } else {
            List<RetryRecord> retryRcdLst = retryHistory.getRetryRecordsList();
            boolean ifExists = false;
            for (RetryRecord r : retryRcdLst) {
                if (r.getRetryException().equalsIgnoreCase(rec.getRetryException())) {
                    r.setRetryCount(rec.getRetryCount());
                    ifExists = true;
                    break;
                }
            }
            if (!ifExists) {
                retryRcdLst.add(rec);
            }
        }
        notificationRetryHistoryDao.save(retryHistory);
    }

    /**
     * Creates an object of SchedulerEvent and sets values.
     */
    IgniteEvent createCreateScheduleEvent(@SuppressWarnings("rawtypes") IgniteKey key, IgniteEvent igniteEvent,
                                          long initialDelay, String source) throws JsonProcessingException {
        LOGGER.debug("Creating schedule create event for key={}, value={}", key, igniteEvent);
        IgniteEventImpl scheduleIgniteEvent = new IgniteEventImpl();
        scheduleIgniteEvent.setSourceDeviceId(igniteEvent.getVehicleId());
        scheduleIgniteEvent.setVehicleId(igniteEvent.getVehicleId());
        scheduleIgniteEvent.setMessageId(msgIdGen.generateUniqueMsgId(igniteEvent.getVehicleId()));

        scheduleIgniteEvent.setEventId(CREATE_SCHEDULE_EVENT);
        scheduleIgniteEvent.setTimestamp(System.currentTimeMillis());

        scheduleIgniteEvent.setVersion(Version.V1_0);
        scheduleIgniteEvent.setRequestId(randomUUID().toString());
        scheduleIgniteEvent.setBizTransactionId(randomUUID().toString());

        CreateScheduleEventData scheduleEventData = new CreateScheduleEventData();
        scheduleEventData.setFiringCount(1);
        scheduleEventData.setServiceName(serviceName);
        byte[] payloadInBytes = MAPPER.writeValueAsBytes(igniteEvent);
        scheduleEventData.setNotificationPayload(payloadInBytes);
        scheduleEventData.setNotificationTopic(source);
        scheduleEventData.setNotificationKey((IgniteStringKey) key);
        scheduleEventData.setInitialDelayMs(initialDelay);
        scheduleEventData.setRecurrenceType(CUSTOM_MS);
        scheduleEventData.setRecurrenceDelayMs(0L);
        scheduleIgniteEvent.setEventData(scheduleEventData);
        return scheduleIgniteEvent;
    }
}
