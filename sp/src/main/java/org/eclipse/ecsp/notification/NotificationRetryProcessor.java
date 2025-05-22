
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
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.notification.NotificationRetryEventData;
import org.eclipse.ecsp.domain.notification.NotificationRetryEventDataV2;
import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData.ScheduleOpStatusErrorCode;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.utils.RetryCacheClient;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.function.BiPredicate;

import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.RETRY_SCHEDULED;
import static org.eclipse.ecsp.domain.notification.commons.EventID.RETRY_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.notification.utils.NotificationProperty.SCHEDULER_SOURCE_TOPIC;

/**
 * This processor hears to notification-retry topic and schedules retry
 * of any retry notification event using scheduler and once scheduler
 * event expires ,sends the original event back to the originating topic
 * for replay.
 *
 * @author AMuraleedhar
 */
@Component
public class NotificationRetryProcessor extends IgniteEventStreamProcessorBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRetryProcessor.class);
    @SuppressWarnings("rawtypes")
    @Autowired
    BiPredicate<IgniteEventStreamProcessor, StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;
    @Autowired
    private GenericIgniteEventTransformer igniteEventTransformer;
    @Autowired
    private RetryCacheClient cacheClient;

    @Value("${notification.retry.topic}")
    private String sourceTopic;

    @Value("${" + SCHEDULER_SOURCE_TOPIC + "}")
    private String schedulerSourceTopic;

    @Autowired
    private AlertsHistoryDao alertsHistoryDao;

    @Autowired
    private NotificationRetryAssistant notificationRetryAssistant;

    /**
     * Method to initialize the processor.
     *
     * @param spc StreamProcessingContext
     */
    @Override
    public void init(StreamProcessingContext<IgniteKey<?>, IgniteEvent> spc) {
        LOGGER.info("Initializing notification-retry processor");
        this.ctxt = spc;
    }

    /**
     * Method to get the name of the processor.
     *
     * @return name of the processor
     */
    @Override
    public String name() {
        return "notification-retry-processor";
    }

    /**
     * Method to get the source topics of the processor.
     *
     * @return source topics of the processor
     */
    @Override
    public String[] sources() {
        return new String[]{sourceTopic};
    }

    /**
     * Method to process the event.
     *
     * @param kafkaRecord Record
     */
    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> kafkaRecord) {

        IgniteKey<?> key = kafkaRecord.key();
        IgniteEvent event = kafkaRecord.value();

        // if the target processor is different than this
        if (skipProcessorPredicate.test(this, ctxt)) {
            ctxt.forward(kafkaRecord);
            return;
        }
        LOGGER.info("processing event: {}", event);

        switch (event.getEventId()) {
            case RETRY_NOTIFICATION_EVENT:
                LOGGER.debug("processing notification retry event");
                createRetryScheduleEvent(key, event);
                break;
            case EventID.SCHEDULE_OP_STATUS_EVENT:
                LOGGER.debug("processing schedule op status event");
                handleScheduleOpStatusEvent(event);
                break;
            case EventID.SCHEDULE_NOTIFICATION_EVENT:
                LOGGER.debug("processing schedule notification event");
                handleRetryReadyEvent(key, event);
                break;
            default:
                LOGGER.error("Invalid eventID {}", event.getEventId());
                break;
        }
    }

    /**
     * Method to create retry schedule event.
     *
     * @param key key
     * @param event event
     */
    private void createRetryScheduleEvent(IgniteKey<?> key, IgniteEvent event) {

        boolean isRetryable = false;
        RetryRecord rc = null;
        IgniteEvent originalEvent = null;
        if (((AbstractIgniteEvent) event).getEventData() instanceof NotificationRetryEventDataV2) {
            NotificationRetryEventDataV2 eventData = (NotificationRetryEventDataV2) ((AbstractIgniteEvent) event)
                    .getEventData();
            rc = eventData.getRetryRecord();
            originalEvent = eventData.getOriginalEvent();
        }
        if (rc != null) {
            String requestId = originalEvent.getRequestId();
            RetryRecord existingRecord = cacheClient.getRetryRecordForException(requestId, rc.getRetryException());
            if (null == existingRecord) {
                isRetryable = true;
                rc.setRetryCount(1);
                cacheClient.putRetryRecord(requestId, rc);

            } else if (existingRecord.getRetryCount() < rc.getMaxRetryLimit()) {
                LOGGER.debug("Retry record found in cache {} {} ", requestId, existingRecord);
                isRetryable = true;
                int currentAttempts = existingRecord.getRetryCount();
                rc.setRetryCount(++currentAttempts);
                cacheClient.putRetryRecord(requestId, rc);
            }
            notificationRetryAssistant.updateNotificationRetryHistory(originalEvent.getRequestId(), rc,
                    originalEvent.getVehicleId());

            if (isRetryable) {
                try {
                    IgniteEvent scheduleEvent =
                            notificationRetryAssistant.createCreateScheduleEvent(key, event, rc.getRetryIntervalMs(),
                                    sourceTopic);

                    ctxt.forwardDirectly(key, scheduleEvent, schedulerSourceTopic);
                    LOGGER.debug("Event sent to scheduler: {} topic {}", scheduleEvent, sourceTopic);
                } catch (JsonProcessingException e) {
                    LOGGER.error("Event retry schedule failed {}", e.getMessage());
                }
            } else {
                cacheClient.deleteRetryRecord(requestId, rc.getRetryException());
                LOGGER.error("Retry attempts exhausted ,hence no retry scheduled for event {}", event);
            }
        } else {
            LOGGER.error("Invalid retry event {}", event);
        }

    }

    /**
     * Method to handle schedule op status event.
     *
     * @param event event
     */
    private void handleScheduleOpStatusEvent(IgniteEvent event) {

        ScheduleOpStatusEventData eventData = (ScheduleOpStatusEventData) event.getEventData();
        String scheduleId = eventData.getScheduleId();
        ScheduleStatus status = eventData.getStatus();
        boolean valid = eventData.isValid();
        ScheduleOpStatusErrorCode statusErrorCode = eventData.getStatusErrorCode();

        // schedule created successfully or failed. Or created schedule has
        // invalidated e.g., missed firing
        if (valid && ScheduleStatus.CREATE.equals(status)) {
            if (valid && statusErrorCode == null) {
                val scheduleEventData = (CreateScheduleEventData) eventData.getIgniteEvent().getEventData();
                String notificationPayload =
                        new String(scheduleEventData.getNotificationPayload(), Charset.defaultCharset());
                IgniteEvent retryNotifEvent =
                        NotificationUtils.resolveEventPayload(notificationPayload, igniteEventTransformer);
                /*
                 * Remove this if clause once NotificationRetryEventDataV2
                 * excercised till prod as part of fix for RTC 511741 SonarQube
                 * - notification-common - Code Smells
                 */

                if (((AbstractIgniteEvent) retryNotifEvent).getEventData() instanceof NotificationRetryEventData) {
                    NotificationRetryEventData retryEventData =
                            (NotificationRetryEventData) ((AbstractIgniteEvent) retryNotifEvent)
                                    .getEventData();
                    // Alerthistory update required only for notification events
                    // which maintain such status history and common
                    // retryHistory
                    IgniteEvent originalEvent = NotificationUtils.resolveEventPayload(
                            new String(retryEventData.getOriginalEvent(), Charset.defaultCharset()),
                            igniteEventTransformer);
                    updateScheduleDetailsInHistory(originalEvent.getRequestId(), scheduleId);
                    LOGGER.info("The notification event {} successfully scheduled for retry.",
                            retryEventData.getOriginalEvent());
                } else if (((AbstractIgniteEvent) retryNotifEvent).getEventData()
                        instanceof NotificationRetryEventDataV2) {
                    NotificationRetryEventDataV2 retryEventData =
                            (NotificationRetryEventDataV2) ((AbstractIgniteEvent) retryNotifEvent)
                                    .getEventData();
                    // Alerthistory update required only for notification events
                    // which maintain such status history and common
                    // retryHistory
                    IgniteEvent originalEvent = retryEventData.getOriginalEvent();
                    updateScheduleDetailsInHistory(originalEvent.getRequestId(), scheduleId);
                    LOGGER.info("The notification event {} successfully scheduled for retry.",
                            retryEventData.getOriginalEvent());
                }

            } else {
                LOGGER.error("The scheduler failed to schedule event for retry");
            }
        }
    }

    /**
     * Method to handle retry ready event.
     *
     * @param igniteKey key
     * @param schedulerEvent event
     */
    void handleRetryReadyEvent(IgniteKey<?> igniteKey, IgniteEvent schedulerEvent) {

        val notifRetryEventData = (ScheduleNotificationEventData) schedulerEvent.getEventData();
        String notificationPayload = new String(notifRetryEventData.getPayload(), Charset.defaultCharset());
        IgniteEvent retryNotifEvent =
                NotificationUtils.resolveEventPayload(notificationPayload, igniteEventTransformer);

        /*
         * Remove this if clause once NotificationRetryEventDataV2 excercised
         * till prod as part of fix for RTC 511741 SonarQube -
         * notification-common - Code Smells
         */
        if (((AbstractIgniteEvent) retryNotifEvent).getEventData() instanceof NotificationRetryEventData) {
            NotificationRetryEventData retryEventData =
                    (NotificationRetryEventData) ((AbstractIgniteEvent) retryNotifEvent)
                            .getEventData();
            IgniteEvent originalEvent = NotificationUtils.resolveEventPayload(
                    new String(retryEventData.getOriginalEvent(), Charset.defaultCharset()), igniteEventTransformer);
            String originalEventTopic = retryEventData.getOriginalEventTopic();
            if (StringUtils.isBlank(originalEventTopic) || null == originalEvent) {
                LOGGER.error("Event / topic not found to retry in scheduleNotificationEvent {}", schedulerEvent);
                return;
            }
            // refresh timestamp to pass dedup filter
            ((AbstractIgniteEvent) originalEvent).setTimestamp(Instant.now().toEpochMilli());
            LOGGER.debug("Event {} sent back to topic {} for retry", originalEvent, originalEventTopic);
            ctxt.forwardDirectly(igniteKey, originalEvent, originalEventTopic);
        } else if (((AbstractIgniteEvent) retryNotifEvent).getEventData() instanceof NotificationRetryEventDataV2) {
            NotificationRetryEventDataV2 retryEventData =
                    (NotificationRetryEventDataV2) ((AbstractIgniteEvent) retryNotifEvent)
                            .getEventData();
            IgniteEvent originalEvent = retryEventData.getOriginalEvent();
            String originalEventTopic = retryEventData.getOriginalEventTopic();
            if (StringUtils.isBlank(originalEventTopic) || null == originalEvent) {
                LOGGER.error("Event / topic not found to retry in scheduleNotificationEvent {}", schedulerEvent);
                return;
            }
            // refresh timestamp to pass dedup filter
            ((AbstractIgniteEvent) originalEvent).setTimestamp(Instant.now().toEpochMilli());
            LOGGER.debug("Event {} sent back to topic {} for retry", originalEvent, originalEventTopic);
            ctxt.forwardDirectly(igniteKey, originalEvent, originalEventTopic);
        }

    }

    void updateScheduleDetailsInHistory(String requestId, String scheduleId) {
        // find by requestId
        val alertsHistory = alertsHistoryDao.findById(requestId);
        if (alertsHistory != null) {
            LOGGER.debug("Found AlertHistoryInfo record for id={} :{}", requestId, alertsHistory);
            alertsHistory.addStatus(RETRY_SCHEDULED, scheduleId);
            alertsHistoryDao.save(alertsHistory);
        } else {
            LOGGER.debug("No AlertHistoryInfo found for id={} ", requestId);
        }


    }

    /**
     * Setter created for unit testing.
     *
     * @param sourceTopic string
     */
    public void setSourceTopic(String sourceTopic) {
        this.sourceTopic = sourceTopic;
    }

    /**
     * Setter created for unit testing.
     *
     * @param igniteEventTransformer igniteEventTransformer
     */
    public void setIgniteEventTransformer(GenericIgniteEventTransformer igniteEventTransformer) {
        this.igniteEventTransformer = igniteEventTransformer;
    }

}
