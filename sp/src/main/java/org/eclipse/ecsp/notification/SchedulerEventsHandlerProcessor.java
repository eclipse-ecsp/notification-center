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

import com.google.common.base.Preconditions;
import lombok.val;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.feedback.NotificationFeedbackHandler;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.SCHEDULED;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.CAMPAIGN_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.USERID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.USER_NOTIFICATION;
import static org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData.ScheduleOpStatusErrorCode.EXPIRED_SCHEDULE;

/**
 * SchedulerEventsHandlerProcessor class.
 */
@Component
public class SchedulerEventsHandlerProcessor extends IgniteEventStreamProcessorBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerEventsHandlerProcessor.class);
    @SuppressWarnings("rawtypes")
    @Autowired
    BiPredicate<IgniteEventStreamProcessor,
            StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;
    private Map<String[], String[]> sourceCache;
    @Autowired
    private ScheduleNotificationAssistant scheduleNotificationAssistant;
    @Autowired
    private GenericIgniteEventTransformer igniteEventTransformer;
    @Autowired
    private AlertsHistoryDao alertsHistoryDao;
    @Value("${scheduling.processor.topic.scheduler.callback}")
    private String[] sourceTopics;
    @Value("#{'${source.topic.name}'.split(',')[0]}")
    private String notificationTopic; // this topic is for outputting the ready notification event
    private NotificationRequestConstructor notificationRequestConstructor = new NotificationRequestConstructor();


    /**
     * actually valid notification for "end-of-series".
     *
     * @param eventData event data
     * @return boolean
     */
    static boolean isEndOfSeriesMessage(ScheduleOpStatusEventData eventData) {
        return !eventData.isValid() && ScheduleStatus.CREATE.equals(eventData.getStatus())
                && EXPIRED_SCHEDULE.equals(eventData.getStatusErrorCode());
    }

    /**
     * Method to get the source topics.
     *
     * @return source topics
     */
    @Override
    public String[] sources() {
        return sourceCache.get(sourceTopics);
    }

    /**
     * Method to initialize the processor.
     *
     * @param spc stream processing context
     */
    @Override
    public void init(StreamProcessingContext<IgniteKey<?>, IgniteEvent> spc) {
        LOGGER.info("Initializing notification-schedule-command processor");
        this.ctxt = spc;

        scheduleNotificationAssistant.init(ctxt);

    }

    /**
     * Method to initialize the configuration.
     *
     * @param props properties
     */
    @Override
    public void initConfig(Properties props) {
        sourceCache = new HashMap<>();

        sourceCache.computeIfAbsent(sourceTopics, s -> Arrays.stream(s).toArray(String[]::new));
    }

    /**
     * Method to get the name of the processor.
     *
     * @return name
     */
    @Override
    public String name() {
        return "scheduler-events-callback-processor";
    }

    /**
     * Method to process the record.
     *
     * @param kafkaRecord kafka record
     */
    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> kafkaRecord) {
        if (skipProcessorPredicate.test(this, ctxt)) { // if the target processor is different than this
            LOGGER.trace("SchedulerEventsHandlerProcessor processes events which match its source topic.");
            ctxt.forward(kafkaRecord);
            return;
        }
        IgniteEvent schedulerEvent = kafkaRecord.value();
        LOGGER.debug("processing event: {}", schedulerEvent);

        switch (schedulerEvent.getEventId()) {
            case EventID.SCHEDULE_OP_STATUS_EVENT:
                handleSchedulerResponseCallback(kafkaRecord.key(), schedulerEvent);
                break;
            case EventID.SCHEDULE_NOTIFICATION_EVENT:
                handleEventReady(kafkaRecord.key(), schedulerEvent);
                break;
            default:
                LOGGER.error("EventID {} is not valid scheduler eventID", schedulerEvent.getEventId());
                break;
        }
    }

    /**
     * Method to handle the event ready.
     *
     * @param igniteKey      ignite key
     * @param schedulerEvent scheduler event
     */
    void handleEventReady(IgniteKey<?> igniteKey, IgniteEvent schedulerEvent) {
        // retrive the data from alert history using "requestId" in the payload
        val scheduleEventData = (ScheduleNotificationEventData) schedulerEvent.getEventData();
        String notificationPayload = new String(scheduleEventData.getPayload(), Charset.defaultCharset());
        IgniteEvent igniteEvent = NotificationUtils.resolveEventPayload(notificationPayload, igniteEventTransformer);
        AlertsHistoryInfo alertsHistory = retrieveAlertsHistoryInfo(igniteEvent.getRequestId());
        // the original event field is transient , so we need to re-introduce it after
        // deserialization
        alertsHistory.getPayload().setIgniteEvent(igniteEvent);

        IgniteEventImpl igniteEventImpl = notificationRequestConstructor.apply(alertsHistory);

        // Forward it to notification topic
        ctxt.forwardDirectly(igniteKey, igniteEventImpl, notificationTopic);
    }

    /**
     * Method to handle the scheduler response callback.
     *
     * @param igniteKey      ignite key
     * @param schedulerEvent scheduler event
     */
    void handleSchedulerResponseCallback(IgniteKey<?> igniteKey, IgniteEvent schedulerEvent) {
        val eventData = (ScheduleOpStatusEventData) schedulerEvent.getEventData();
        String scheduleId = eventData.getScheduleId();
        ScheduleStatus status = eventData.getStatus();

        if (isEndOfSeriesMessage(eventData)) {
            return;
        }

        if (!eventData.isValid()) {
            LOGGER.error("Scheduler returned error indication {} for event: {}", eventData.getStatusErrorCode(),
                    schedulerEvent);
            return;
        }

        switch (status) {
            case CREATE:
                val scheduleEventData = (CreateScheduleEventData) eventData.getIgniteEvent().getEventData();
                String notificationPayload =
                        new String(scheduleEventData.getNotificationPayload(), Charset.defaultCharset());
                IgniteEvent igniteEvent =
                        NotificationUtils.resolveEventPayload(notificationPayload, igniteEventTransformer);
                AlertsHistoryInfo alertsHistory = retrieveAlertsHistoryInfo(igniteEvent.getRequestId());
                alertsHistory.addStatus(SCHEDULED, scheduleId);
                alertsHistoryDao.save(alertsHistory);
                NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                        alertsHistory.getPayload(), igniteKey, igniteEvent, alertsHistory);
                break;

            case DELETE:
                IgniteEventImpl event = (IgniteEventImpl) schedulerEvent;
                AlertsHistoryInfo alertsHistoryInfo = retrieveAlertsHistoryInfo(event.getRequestId());
                Optional<StatusHistoryRecord> currentStatusOp = alertsHistoryInfo.currentStatus();
                Preconditions.checkArgument(currentStatusOp.isPresent());
                StatusHistoryRecord currentStatus = currentStatusOp.get();
                Preconditions.checkArgument(currentStatus.getStatus() == Status.SCHEDULED);
                alertsHistoryInfo.addStatus(Status.CANCELED, scheduleId);
                alertsHistoryDao.save(alertsHistoryInfo);
                NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                        alertsHistoryInfo.getPayload(), igniteKey, event,  alertsHistoryInfo);
                break;

            default:
                LOGGER.error("Invalid status {}", status);
                break;
        }
    }

    /**
     * Method to retrieve the alerts history info.
     *
     * @param requestId request id
     * @return alerts history info
     */
    @NotNull
    AlertsHistoryInfo retrieveAlertsHistoryInfo(String requestId) {
        // find by requestId
        val alertsHistory = alertsHistoryDao.findById(requestId);
        Preconditions.checkNotNull(alertsHistory, "No initial alert history found with id:" + requestId);
        LOGGER.debug("Found AlertHistoryInfo record for id={} :{}", requestId, alertsHistory);
        return alertsHistory;
    }

    /**
     * Setter created for unit testing.
     */
    public void setScheduleNotificationAssistant(ScheduleNotificationAssistant scheduleNotificationAssistant) {
        this.scheduleNotificationAssistant = scheduleNotificationAssistant;
    }

    /**
     * Setter created for unit testing.
     */
    public void setAlertsHistoryDao(AlertsHistoryDao alertsHistoryDao) {
        this.alertsHistoryDao = alertsHistoryDao;
    }

    /**
     * Setter created for unit testing.
     */
    public void setIgniteEventTransformer(GenericIgniteEventTransformer igniteEventTransformer) {
        this.igniteEventTransformer = igniteEventTransformer;
    }

    /**
     * Setter created for unit testing.
     */
    public void setSourceTopics(String[] sourceTopics) {
        this.sourceTopics = Arrays.stream(sourceTopics).toArray(String[]::new);
    }

    /**
     * Setter created for unit testing.
     */
    public void setNotificationTopic(String notificationTopic) {
        this.notificationTopic = notificationTopic;
    }

    /**
     * Setter created for unit testing.
     */
    public void setNotificationRequestConstructor(NotificationRequestConstructor notificationRequestConstructor) {
        this.notificationRequestConstructor = notificationRequestConstructor;
    }

    /**
     * Setter created for unit testing.
     */
    @SuppressWarnings("rawtypes")
    public void setSkipProcessorPredicate(
            BiPredicate<IgniteEventStreamProcessor,
                    StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate) {
        this.skipProcessorPredicate = skipProcessorPredicate;
    }

    /**
     * This class encapsulates logic of transforming ALertHistory to IgniteEvent. It
     * is needed because a gap can develop in event model while the original event
     * is waiting in storage at scheduler service. In theory, this class can be a
     * spring component and implementation will be injected per context.
     */
    static class NotificationRequestConstructor implements Function<AlertsHistoryInfo, IgniteEventImpl> {
        @Override
        public IgniteEventImpl apply(AlertsHistoryInfo alertsHistory) {
            val alertInfo = alertsHistory.getPayload();
            val originalIgniteEvent = alertInfo.getIgniteEvent();
            val originalData = (GenericEventData) originalIgniteEvent.getEventData();

            Map<Object, Object> dataMap = originalData.getData();
            String notificationId = (String) dataMap.get(NOTIFICATION_ID);
            Preconditions.checkNotNull(notificationId, NOTIFICATION_ID + " is null or missing");

            // 2, build the notification request event
            IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
            igniteEventImpl.setEventId(EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT.toString());
            igniteEventImpl.setVersion(Version.V1_0); // maybe dynamic version selector?
            igniteEventImpl.setTimestamp(Instant.now().getEpochSecond());
            short timezone = originalIgniteEvent.getTimezone();
            igniteEventImpl.setTimezone(timezone);
            val alertsData = alertInfo.getAlertsData();
            String vehicleId = alertsData.getVehicleProfile() == null ? null
                    : alertsData.getVehicleProfile().getVehicleId();
            igniteEventImpl.setVehicleId(vehicleId);
            String requestId = alertsHistory.getId();
            igniteEventImpl.setRequestId(requestId);
            String bizTransactionId = originalIgniteEvent.getBizTransactionId();
            igniteEventImpl.setBizTransactionId(bizTransactionId);
            String userId = alertsHistory.getUserId();
            GenericEventData genericEventData = new GenericEventData();
            genericEventData.set(USERID, userId);
            genericEventData.set(NOTIFICATION_ID, notificationId);
            String campaignId = (String) dataMap.getOrDefault(CAMPAIGN_ID, null);
            genericEventData.set(CAMPAIGN_ID, campaignId);
            Boolean isUserNotification = (Boolean) dataMap.get(USER_NOTIFICATION);
            genericEventData.set(USER_NOTIFICATION, isUserNotification);

            dataMap.keySet().stream().filter(key -> !genericEventData.getData().containsKey(key))
                    .forEach(key -> genericEventData.set(key, dataMap.get(key)));

            igniteEventImpl.setEventData(genericEventData);

            LOGGER.debug("built Notification Request event: {} ", igniteEventImpl);
            return igniteEventImpl;
        }
    }
}
