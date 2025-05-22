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
import com.google.common.base.Preconditions;
import lombok.val;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.RetryableException;
import org.eclipse.ecsp.domain.notification.utils.ProcessingStatus;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.feedback.NotificationFeedbackHandler;
import org.eclipse.ecsp.notification.processors.UserProfileEnricher;
import org.eclipse.ecsp.notification.processors.VehicleProfileEnricher;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.eclipse.ecsp.domain.notification.commons.EventID.DELETE_SCHEDULED_NOTIFICATION_COMMAND;
import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.PLATFORM_RESPONSE_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.SCHEDULE_NOTIFICATION;

/**
 * SchedulingProcessor class.
 */
@Component
public class SchedulingProcessor extends IgniteEventStreamProcessorBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingProcessor.class);
    static ObjectMapper mapper;

    private static final int THOUSAND = 1000;

    static {
        mapper = new ObjectMapper();
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    @SuppressWarnings("rawtypes")
    @Autowired
    BiPredicate<IgniteEventStreamProcessor,
            StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;
    @Autowired
    UserProfileEnricher userProfileEnricher;
    @Autowired
    VehicleProfileEnricher vehicleProfileEnricher;
    @Value("${timezone_default_value:GMT}")
    String defaultTimezone;
    @Autowired
    AlertsHistoryDao alertHistoryDao;
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;
    private Map<String[], String[]> sourceTopicCache;
    @Autowired
    private ScheduleNotificationAssistant scheduleNotificationAssistant;
    @Autowired
    private AlertsHistoryAssistant alertsHistoryAssistant;
    @Value("${scheduling.processor.topic.scheduler.callback}")
    private String schedulerCallbackTopic;
    @Value("#{'${scheduling.processor.topic.sources}'.split(',')}")
    private String[] sourceTopics;
    @Value("${notification.schedule.max.period.days}")
    private int maxScheduleDays;


    /**
     * sources method.
     *
     * @return String[]
     */
    @Override
    public String[] sources() {
        return sourceTopicCache.get(sourceTopics);
    }

    /**
     * init method.
     *
     * @param context StreamProcessingContext
     */
    @Override
    public void init(StreamProcessingContext<IgniteKey<?>, IgniteEvent> context) {
        LOGGER.info("Initializing notification-schedule-command processor");
        this.ctxt = context;
        scheduleNotificationAssistant.init(ctxt);
    }

    /**
     * initConfig method.
     *
     * @param props Properties
     */
    @Override
    public void initConfig(Properties props) {
        sourceTopicCache = new HashMap<>();
        sourceTopicCache.put(sourceTopics, Arrays.stream(sourceTopics).toArray(String[]::new));
        LOGGER.debug("Scheduler Source Topic Name : {}", scheduleNotificationAssistant.getSchedulerSourceTopic());
    }

    /**
     * name method.
     *
     * @return String
     */
    @Override
    public String name() {
        return "scheduling-processor";
    }

    /**
     * process method.
     *
     * @param kafkaRecord Record
     */
    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> kafkaRecord) {

        if (skipProcessorPredicate.test(this, ctxt)) { // if the target
            // processor is different
            // than this
            ctxt.forward(kafkaRecord);
            return;
        }
        IgniteKey<?> igniteKey = kafkaRecord.key();
        IgniteEvent igniteEvent = kafkaRecord.value();
        LOGGER.debug("processing event: {}", igniteEvent);

        switch (igniteEvent.getEventId()) {
            case DELETE_SCHEDULED_NOTIFICATION_COMMAND:
                LOGGER.debug("processing delete scheduled notification command");
                deleteScheduledNotification(igniteKey, igniteEvent);
                break;

            case GENERIC_NOTIFICATION_EVENT:
                LOGGER.debug("processing create scheduled notification event");
                createScheduledNotification(igniteKey, igniteEvent);
                break;
            default:
                LOGGER.error("Invalid eventID {}", igniteEvent.getEventId());
                break;
        }
    }

    /**
     * createScheduledNotification method.
     *
     * @param igniteKey IgniteKey
     * @param igniteEvent IgniteEvent
     */
    void createScheduledNotification(IgniteKey<?> igniteKey, IgniteEvent igniteEvent) {
        AlertsHistoryInfo alertHistoryInfo = new AlertsHistoryInfo();
        AlertsInfo alertInfo = null;
        try {
            alertInfo = resolveAlertsInfo(igniteEvent);

            val alertsData = alertInfo.getAlertsData();
            long scheduledTime = resolveScheduledPeriodMs(alertsData);

            scheduleNotification(igniteKey, igniteEvent, schedulerCallbackTopic, scheduledTime);

            alertHistoryInfo = alertsHistoryAssistant.createBasicAlertHistory(alertInfo);
            alertsHistoryAssistant.setEnrichedAlertHistory(alertInfo, alertHistoryInfo);
            alertHistoryInfo.addStatus(AlertsHistoryInfo.Status.SCHEDULE_REQUESTED);

            String pdid = alertInfo.getIgniteEvent().getVehicleId();
            String userId = alertsData.getUserProfile().getUserId();
            alertsHistoryAssistant.saveAlertHistory(userId, pdid, alertInfo, alertHistoryInfo);


        } catch (RetryableException e) {
            LOGGER.error("Schedule notification retry is not allowed ,ignoring the retryable exception {}", igniteEvent,
                    e);
        } catch (Exception e) {
            LOGGER.error("Failed scheduling a notification based on event: {}", igniteEvent, e);
            setProcessingStatus(NotificationConstants.FAILURE, alertHistoryInfo, e.getMessage());
        }
        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                alertInfo, igniteKey, igniteEvent, alertHistoryInfo);

    }

    /**
     * setProcessingStatus method.
     *
     * @param status String
     * @param alertHistoryObj AlertsHistoryInfo
     * @param errorMsg String
     */
    private void setProcessingStatus(String status, AlertsHistoryInfo alertHistoryObj, String errorMsg) {
        ProcessingStatus processingStatus = alertHistoryObj.getProcessingStatus();
        if (processingStatus == null) {
            processingStatus = new ProcessingStatus(status, errorMsg);
        } else {
            processingStatus.setStatus(status);
            processingStatus.setErrorMessage(errorMsg);
        }
        alertHistoryObj.setProcessingStatus(processingStatus);

    }

    /**
     * resolveAlertsInfo method.
     *
     * @param igniteEvent IgniteEvent
     * @return AlertsInfo
     * @throws JsonProcessingException Exception
     */
    @NotNull
    AlertsInfo resolveAlertsInfo(IgniteEvent igniteEvent) throws JsonProcessingException {

        IgniteEventImpl igniteEventEnriched = null;
        if (((AbstractIgniteEvent) igniteEvent).getEventData() instanceof GenericEventData) {
            GenericEventData genericEventData = (GenericEventData) ((AbstractIgniteEvent) igniteEvent).getEventData();
            if (genericEventData.getData(NotificationConstants.VEHICLE_DATA).isPresent()) {
                LOGGER.debug("Enriching vehicleprofile present in payload {}", genericEventData);
                GenericEventData genericEventDataEnriched = new GenericEventData();
                igniteEventEnriched = ((IgniteEventImpl) igniteEvent).headerClone();
                Map<Object, Object> dataMap = genericEventData.getData();
                dataMap.keySet().stream().forEach(key -> genericEventDataEnriched.set(key, dataMap.get(key)));

                Map<String, Object> vehicleAttributes = new HashMap<String, Object>();
                vehicleAttributes.put(NotificationConstants.VEHICLE_ATTRIBUTES_DATA,
                        genericEventData.getData(NotificationConstants.VEHICLE_DATA).get());
                genericEventDataEnriched.set(NotificationConstants.VEHICLE_DATA, vehicleAttributes);
                igniteEventEnriched.setEventData(genericEventDataEnriched);
                LOGGER.debug("Ignite event after enrich {}", igniteEventEnriched);
            }
        }
        List<IgniteEvent> igniteEventList = igniteEventEnriched != null ? Collections.singletonList(igniteEventEnriched)
                : Collections.singletonList(igniteEvent);
        AlertsInfo alertInfo = NotificationUtils
                .getListObjects(mapper.writeValueAsString(igniteEventList), AlertsInfo.class).get(0);
        // Always set the original event not the enriched one
        alertInfo.setIgniteEvent(igniteEvent);
        vehicleProfileEnricher.process(alertInfo);
        userProfileEnricher.process(alertInfo);
        return alertInfo;
    }

    /**
     * resolveScheduledPeriodMs method.
     *
     * @param alertsData Data
     * @return long
     */
    long resolveScheduledPeriodMs(Data alertsData) {
        Map<String, Object> dataProperties = alertsData.getAlertDataProperties();
        Preconditions.checkArgument(dataProperties.containsKey(SCHEDULE_NOTIFICATION),
                "Missing required property:\"%s\"", SCHEDULE_NOTIFICATION);
        String scheduleTarget = (String) dataProperties.get(SCHEDULE_NOTIFICATION);
        String timeZone = defaultIfBlank(alertsData.getUserProfile().getTimeZone(), defaultTimezone);
        Instant scheduleTime = LocalDateTime.parse(scheduleTarget, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.of(timeZone)).toInstant();
        long delay = (scheduleTime.getEpochSecond() - Instant.now().getEpochSecond()) * THOUSAND;
        validateDelayPeriod(delay);
        return delay;
    }

    /**
     * validateDelayPeriod method.
     *
     * @param delay long
     */
    void validateDelayPeriod(long delay) {
        if (delay <= 0) {
            throw new DateTimeException("Past schedule target date is illegal");
        }
        long days = TimeUnit.MILLISECONDS.toDays(delay);
        boolean valid = days <= maxScheduleDays;
        if (!valid) {
            String msg = String.format("Request scheduling period (%s) is out of configured limit: %s", days,
                    maxScheduleDays);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * scheduleNotification method.
     *
     * @param key IgniteKey
     * @param event IgniteEvent
     * @param callbackTopic String
     * @param durationMs long
     * @throws JsonProcessingException Exception
     */
    public void scheduleNotification(IgniteKey<?> key, IgniteEvent event, String callbackTopic, long durationMs)
            throws JsonProcessingException {

        // create IgniteEvent using createCreateScheduleEventData
        IgniteEvent createScheduleEvent = scheduleNotificationAssistant.createCreateScheduleEventData(key, event, event,
                durationMs, callbackTopic);
        // forward it to scheduler using ctxt.forwad
        scheduleNotificationAssistant.submitSchedulerEvent(key, createScheduleEvent);
        LOGGER.debug("Event sent to scheduler: {}", createScheduleEvent);
    }

    /**
     * Sends a cancellation request to scheduler-sp for canceling the.
     * scheduledNotification
     *
     * @param igniteKey IgniteKey
     * @param igniteEvent IgniteEvent
     */
    public void deleteScheduledNotification(IgniteKey<?> igniteKey, IgniteEvent igniteEvent) {
        GenericEventData eventData = (GenericEventData) igniteEvent.getEventData();
        Optional<Object> platformIdOpt = eventData.getData(PLATFORM_RESPONSE_ID);
        Preconditions.checkArgument(platformIdOpt.isPresent());
        String alertHistoryId = platformIdOpt.get().toString();
        AlertsHistoryInfo alertHistory = alertHistoryDao.findById(alertHistoryId);
        Preconditions.checkNotNull(alertHistory, "AlertHistory Object is null");
        Optional<StatusHistoryRecord> statusHistoryRecordOpt = alertHistory.currentStatus();
        Preconditions.checkArgument(statusHistoryRecordOpt.isPresent(), "Missing status history record");
        StatusHistoryRecord statusHistoryRecord = statusHistoryRecordOpt.get();
        Preconditions.checkArgument(statusHistoryRecord.getStatus() == Status.SCHEDULED,
                "Scheduled Notification can't be cancelled, Status of the record is not :\"%s\"", Status.SCHEDULED);
        String schedulerId = statusHistoryRecord.getCorrelationId();
        IgniteEventImpl igniteEventImpl = (IgniteEventImpl) igniteEvent;
        igniteEventImpl.setVehicleId(defaultIfEmpty(alertHistory.getPdid(), alertHistory.getUserId()));
        IgniteEventImpl createDeleteScheduleEventData = (IgniteEventImpl) scheduleNotificationAssistant
                .createDeleteScheduleEventData(igniteKey, igniteEvent, schedulerId);
        createDeleteScheduleEventData.setRequestId(alertHistoryId);
        LOGGER.debug("Sending Cancellation request to scheduler service for schedulerId {} of alertHistoryId {}",
                schedulerId, alertHistoryId);
        scheduleNotificationAssistant.submitSchedulerEvent(igniteKey, createDeleteScheduleEventData);
       
        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                alertHistory.getPayload(), igniteKey, createDeleteScheduleEventData, alertHistory);

    }

    //Test methods
    @Profile("test")
    void setScheduleNotificationAssistant(ScheduleNotificationAssistant scheduleNotificationAssistant) {
        this.scheduleNotificationAssistant = scheduleNotificationAssistant;
    }

    // region Test methods
    @Profile("test")
    public void setSourceTopics(String[] sourceTopics) {
        this.sourceTopics = Arrays.stream(sourceTopics).toArray(String[]::new);
    }

    //Test methods
    @Profile("test")
    public void setAlertsHistoryAssistant(AlertsHistoryAssistant alertsHistoryAssistant) {
        this.alertsHistoryAssistant = alertsHistoryAssistant;
    }

    //Test methods
    @Profile("test")
    public void setUserProfileEnricher(UserProfileEnricher userProfileEnricher) {
        this.userProfileEnricher = userProfileEnricher;
    }

    //Test methods
    @Profile("test")
    public void setVehicleProfileEnricher(VehicleProfileEnricher vehicleProfileEnricher) {
        this.vehicleProfileEnricher = vehicleProfileEnricher;
    }

    //Test methods
    @Profile("test")
    public void setMaxScheduleDays(int maxScheduleDays) {
        this.maxScheduleDays = maxScheduleDays;
    }

    //Test methods
    @SuppressWarnings("rawtypes")
    @Profile("test")
    public void setSkipProcessorPredicate(
            BiPredicate<IgniteEventStreamProcessor,
                    StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate) {
        this.skipProcessorPredicate = skipProcessorPredicate;
    }

    //Test methods
    @Profile("test")
    public void setSchedulerCallbackTopic(String schedulerCallbackTopic) {
        this.schedulerCallbackTopic = schedulerCallbackTopic;
    }

    //Test methods
    public void setDefaultTimezone(String defaultTimezone) {
        this.defaultTimezone = defaultTimezone;
    }

    //Test methods
    public void setAlertHistoryDao(AlertsHistoryDao alertHistoryDao) {
        this.alertHistoryDao = alertHistoryDao;
    }
    // endregion
}
