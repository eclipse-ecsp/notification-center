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
import org.apache.commons.codec.binary.Base64;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.idgen.MessageIdGenerator;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.DeleteScheduleEventData;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.entities.BufferedAlertsInfo;
import org.eclipse.ecsp.notification.entities.CloneNotificationConfig;
import org.eclipse.ecsp.notification.entities.CloneNotificationTemplate;
import org.eclipse.ecsp.notification.entities.CloneNotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.NotificationSchedulerPayload;
import org.eclipse.ecsp.notification.utils.TimeCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.eclipse.ecsp.domain.EventID.CREATE_SCHEDULE_EVENT;
import static org.eclipse.ecsp.domain.EventID.DELETE_SCHEDULE_EVENT;
import static org.eclipse.ecsp.events.scheduler.CreateScheduleEventData.RecurrenceType.CUSTOM_MS;
import static org.eclipse.ecsp.notification.VehicleInfoNotification.MAPPER;
import static org.eclipse.ecsp.notification.adaptor.NotificationUtils.SERVICE_NAME;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.utils.NotificationProperty.SCHEDULER_SOURCE_TOPIC;

/**
 * ScheduleNotificationAssistant class.
 */
@Component
public class ScheduleNotificationAssistant {
    private static final int THOUSAND = 1000;

    private static final int FORTY_FIVE = 45;

    static final String DEFAULT_TIMEZONE = "UTC";
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleNotificationAssistant.class);
    private final NotificationBufferDao notificationBufferDao;
    private final UserProfileDAO userProfileDao;
    private final MessageIdGenerator msgIdGen;
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;
    @Value("${" + SERVICE_NAME + "}")
    private String serviceName;

    @Value("${" + SCHEDULER_SOURCE_TOPIC + "}")
    private String schedulerSourceTopic;

    /**
     * ScheduleNotificationAssistant constructor.
     *
     * @param notificationBufferDao NotificationBufferDao
     * @param userProfileDao UserProfileDAO
     * @param msgIdGen MessageIdGenerator
     */
    @Autowired
    public ScheduleNotificationAssistant(NotificationBufferDao notificationBufferDao, UserProfileDAO userProfileDao,
                                         @Qualifier("globalMessageIdGenerator") MessageIdGenerator msgIdGen) {
        this.notificationBufferDao = notificationBufferDao;
        this.userProfileDao = userProfileDao;
        this.msgIdGen = msgIdGen;
    }

    /**
     * Calculates duration in Seconds between now and EndTime of QuietPeriod.
     */
    static long calculateQpDuration(LocalDateTime now, SuppressionConfig suppression) {
        long snoozePeriod = 0L;
        switch (suppression.getSuppressionType()) {
            case VACATION: {
                LocalDateTime endDateTime = LocalDateTime.of(suppression.getEndDate(), suppression.getEndTime());
                snoozePeriod = getTimeDiffSeconds(now, endDateTime);
            }
            break;
            case RECURRING: {
                LocalDateTime endDateTime = now.with(suppression.getEndTime());
                boolean isOverNight = now.toLocalTime().isAfter(suppression.getEndTime());
                if (isOverNight) {
                    endDateTime = now.plusDays(1).with(suppression.getEndTime());
                }
                snoozePeriod = getTimeDiffSeconds(now, endDateTime);
            }
            break;
            default:
                return snoozePeriod + FORTY_FIVE; // adding additional time in order to
        }
        return snoozePeriod + FORTY_FIVE; // adding additional time in order to
        // overreach any system caused delays
    }

    /**
     * Calculates the difference in seconds between two LocalDateTime objects.
     */
    public static long getTimeDiffSeconds(LocalDateTime now, LocalDateTime target) {
        Duration duration = Duration.between(now, target);
        return Math.abs(duration.getSeconds());
    }

    /**
     * Initializes the StreamProcessingContext.
     */
    void init(StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessingContext) {
        this.ctxt = streamProcessingContext;
    }

    /**
     * Deletes all the Schedule Notifications for a given user and vehicle.
     */
    void deleteScheduleNotifications(String userId, String vehicleId, IgniteEvent igniteEvent) {
        List<NotificationBuffer> notificationBufferList =
                notificationBufferDao.findByUserIdAndVehicleId(userId, vehicleId);
        if (!CollectionUtils.isEmpty(notificationBufferList)) {
            IgniteKey<String> key = new IgniteStringKey(vehicleId);
            for (NotificationBuffer notificationBuffer : notificationBufferList) {
                IgniteEvent deleteScheduleEvent =
                        createDeleteScheduleEventData(key, igniteEvent, notificationBuffer.getSchedulerId());
                submitSchedulerEvent(key, deleteScheduleEvent);
            }
            notificationBufferDao.deleteByUserIdAndVehicleId(userId, vehicleId);
        }
    }

    /**
     * Creates an object of SchedulerEvent and sets values.
     */
    <T extends Object> IgniteEvent createCreateScheduleEventData(@SuppressWarnings("rawtypes") IgniteKey key,
                                                                 IgniteEvent igniteEvent,
                                                                 T payload, long durationInMs, String source)
            throws JsonProcessingException {
        LOGGER.debug("Creating schedule create event for key={}, value={}", key, igniteEvent);
        Optional<String> vehicleIdOptional = ofNullable(igniteEvent.getVehicleId());
        IgniteEventImpl createScheduleIgniteEvent = new IgniteEventImpl();
        if (vehicleIdOptional.isPresent()) {
            createScheduleIgniteEvent.setSourceDeviceId(vehicleIdOptional.get());
            createScheduleIgniteEvent.setVehicleId(vehicleIdOptional.get());
            createScheduleIgniteEvent.setMessageId(vehicleIdOptional.get());
        } else {
            createScheduleIgniteEvent.setSourceDeviceId(VEHICLE_ID_FOR_DEFAULT_PREFERENCE);
            createScheduleIgniteEvent.setVehicleId(VEHICLE_ID_FOR_DEFAULT_PREFERENCE);
            createScheduleIgniteEvent.setMessageId(randomUUID().toString());
        }

        createScheduleIgniteEvent.setEventId(CREATE_SCHEDULE_EVENT);
        createScheduleIgniteEvent.setTimestamp(System.currentTimeMillis());

        createScheduleIgniteEvent.setVersion(Version.V1_0);
        createScheduleIgniteEvent.setRequestId(randomUUID().toString());
        createScheduleIgniteEvent.setBizTransactionId(randomUUID().toString());

        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        createScheduleEventData.setFiringCount(1);
        createScheduleEventData.setServiceName(serviceName);
        byte[] payloadInBytes = MAPPER.writeValueAsBytes(payload);
        createScheduleEventData.setNotificationPayload(payloadInBytes);
        createScheduleEventData.setNotificationTopic(source);
        createScheduleEventData.setNotificationKey((IgniteStringKey) key);
        createScheduleEventData.setInitialDelayMs(durationInMs); // Duration
        // after which
        // Scheduler
        // sends First
        // Schedule_Notification_Event
        createScheduleEventData.setRecurrenceType(CUSTOM_MS);
        LOGGER.debug("recurrenceDelay {}", durationInMs);
        createScheduleEventData.setRecurrenceDelayMs(0L);
        createScheduleIgniteEvent.setEventData(createScheduleEventData);
        return createScheduleIgniteEvent;
    }

    /**
     * Creates an Object of Delete Scheduler Event.
     */
    IgniteEvent createDeleteScheduleEventData(@SuppressWarnings("rawtypes") IgniteKey key, IgniteEvent igniteEvent,
                                              String schedulerId) {
        LOGGER.debug("Creating schedule delete event for key={}, value={}", key, igniteEvent);

        String vehicleId = igniteEvent.getVehicleId();
        IgniteEventImpl deleteScheduleIgniteEvent = new IgniteEventImpl();
        deleteScheduleIgniteEvent.setEventId(DELETE_SCHEDULE_EVENT);
        deleteScheduleIgniteEvent.setTimestamp(System.currentTimeMillis());
        deleteScheduleIgniteEvent.setSourceDeviceId(vehicleId);
        deleteScheduleIgniteEvent.setVehicleId(vehicleId);
        deleteScheduleIgniteEvent.setVersion(Version.V1_0);
        deleteScheduleIgniteEvent.setRequestId(randomUUID().toString());
        deleteScheduleIgniteEvent.setBizTransactionId(randomUUID().toString());
        deleteScheduleIgniteEvent.setMessageId(msgIdGen.generateUniqueMsgId(igniteEvent.getVehicleId()));

        DeleteScheduleEventData deleteScheduleEventData = new DeleteScheduleEventData();
        deleteScheduleEventData.setScheduleId(schedulerId);
        deleteScheduleIgniteEvent.setEventData(deleteScheduleEventData);
        LOGGER.debug("Created schedule delete event for key={}, value={}, deleteScheduleIgniteEvent={}", key,
                igniteEvent,
                deleteScheduleIgniteEvent);

        return deleteScheduleIgniteEvent;
    }

    /**
     * Snoozes the Alert Notification.
     */
    void snoozeAlert(IgniteKey<?> key, IgniteEvent igniteEvent, AlertsInfo alert, String contactId,
                     ChannelType channelType, SuppressionConfig suppression, String source)
            throws JsonProcessingException {
        LOGGER.info("Quiet time in effect for the following contact id {}, snoozing notification", contactId);
        String userId =
                alert.getAlertsData().getUserProfile() != null ? alert.getAlertsData().getUserProfile().getUserId()
                        : null;
        NotificationBuffer buffer = getNotificationBuffer(channelType, userId, igniteEvent.getVehicleId(),
                alert.getNotificationConfig().getGroup(), contactId);
        if (buffer != null) {
            LOGGER.debug("Scheduler available, the buffer is: {}", buffer);
            String schedulerId = buffer.getSchedulerId();
            if (schedulerId != null) {

                // Persist delayed notification to buffer
                BufferedAlertsInfo bufferedAlert = setBufferedAlertInfo(alert);

                convertEmailAttachmentDataForUpdate(bufferedAlert, alert);

                List<BufferedAlertsInfo> bufferedAlertList = buffer.getAlertsInfo();
                bufferedAlertList.add(bufferedAlert);
                buffer.setAlertsInfo(bufferedAlertList);
                LOGGER.debug("Notification updating to buffer in  snoozeAlert {}", buffer);

                notificationBufferDao.update(buffer);
                LOGGER.info("Notification updated to buffer successfully in snoozeAlert");
            }
        } else {
            LOGGER.debug("Scheduler not available");
            String vehicleId = igniteEvent.getVehicleId();
            if (!StringUtils.hasText(vehicleId)
                    ||
                    (vehicleId.equals(userId) && alert.getAlertsData().getVehicleProfile() == null)) {
                vehicleId = VEHICLE_ID_FOR_DEFAULT_PREFERENCE;
            }
            // saving Alert into Notification Buffer
            saveSnoozedNotification(channelType, alert, userId, vehicleId, contactId);

            // create payload for Notification Scheduler
            NotificationSchedulerPayload payload = new NotificationSchedulerPayload(userId, vehicleId,
                    channelType, alert.getNotificationConfig().getGroup(), contactId);
            String timeZone = Optional.ofNullable(userProfileDao.findById(userId))
                    .map(u -> u.getTimeZone()).orElse(DEFAULT_TIMEZONE);
            LOGGER.debug("Processing TimeZone {}", timeZone);
            LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));

            long durationInSecs = calculateQpDuration(now, suppression);

            // create Scheduler
            IgniteEvent createScheduleEvent =
                    createCreateScheduleEventData(key, igniteEvent, payload, durationInSecs * THOUSAND, source);
            submitSchedulerEvent(key, createScheduleEvent);
        }
    }

    /**
     * Saves the snoozed notification to buffer in MongoDB.
     */
    void updateScheduler(SuppressionConfig suppression, NotificationConfig notificationConfig, ChannelType channelType,
                         NotificationBuffer notificationBuffer, IgniteKey<?> key, IgniteEvent event, String source)
            throws JsonProcessingException {

        String timeZone = Optional.ofNullable(userProfileDao.findById(notificationConfig.getUserId()))
                .map(u -> u.getTimeZone()).orElse(DEFAULT_TIMEZONE);

        LOGGER.debug("Processing TimeZone {}", timeZone);
        LocalDateTime now = LocalDateTime.now(ZoneId.of(timeZone));
        long durationInSecs = calculateQpDuration(now, suppression);
        LOGGER.info("Update updateScheduler for user: {}, set it in time: {}", notificationConfig.getUserId(),
                durationInSecs * THOUSAND);
        NotificationSchedulerPayload payload = new NotificationSchedulerPayload(notificationConfig.getUserId(),
                notificationConfig.getVehicleId(), channelType, notificationConfig.getGroup(), true,
                notificationBuffer.getSchedulerId(),
                notificationConfig.getContactId());
        IgniteEvent createScheduleEvent =
                createCreateScheduleEventData(key, event, payload, durationInSecs * THOUSAND, source);
        submitSchedulerEvent(key, createScheduleEvent);
    }

    /**
     * iterates through the given SuppressionConfig list. returns true in case
     * the suppression needs to be enforced. otherwise, returns false.
     */
    SuppressionConfig enforceSuppression(List<SuppressionConfig> suppressionConfigs, String userId) {
        String timeZone =
                Optional.ofNullable(userProfileDao.findById(userId)).map(u -> u.getTimeZone()).orElse(DEFAULT_TIMEZONE);
        LOGGER.debug("Processing TimeZone{}", timeZone);
        LocalDateTime now = LocalDateTime.now((TimeZone.getTimeZone(timeZone).toZoneId()));
        for (SuppressionConfig suppression : suppressionConfigs) {
            switch (suppression.getSuppressionType()) {
                case VACATION:
                    LOGGER.debug("Vacation: current time is: {}, suppression: {} - {}", now, suppression.getStartDate(),
                            suppression.getEndDate());

                    if (TimeCalculator.currentDateTimeIsInInterval(suppression, now)) {
                        LOGGER.debug("Quiet time is in effect, suppressing notification.");
                        return suppression;
                    }
                    break;
                case RECURRING:
                    LOGGER.debug("Recurring: current time is: {}, suppression: {}", now, suppression.getDays());

                    if (TimeCalculator.currentDateTimeIsInRecurringInterval(suppression, now)) {
                        LOGGER.debug("Quiet time is in effect, suppressing notification.");
                        return suppression;
                    }
                    break;
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * Overloaded getNotificationBuffer method, returns a NotificationBuffer.
     * Object from mongo based on ChannelType, userId and VehicleId
     */
    NotificationBuffer getNotificationBuffer(ChannelType channelType, String userId, String vehicleId, String group,
                                             String contactId) {
        LOGGER.debug("finding NotificationBuffer based on userId, VehicleId and channelType");
        return notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(userId, vehicleId, channelType,
                group, contactId);
    }

    /**
     * saves the snoozed notification to buffer in MongoDB.

     * @param channelType ChannelType
     * @param alert AlertsInfo
     * @param userId String
     * @param vehicleId String
     * @param contactId String
     */
    private void saveSnoozedNotification(ChannelType channelType, AlertsInfo alert, String userId, String vehicleId,
                                         String contactId) {
        NotificationBuffer buffer = new NotificationBuffer();
        List<BufferedAlertsInfo> notifications = new ArrayList<>();
        BufferedAlertsInfo bufferedAlert = setBufferedAlertInfo(alert);
        notifications.add(bufferedAlert);
        buffer.setAlertsInfo(notifications);
        buffer.setChannelType(channelType);
        buffer.setUserId(userId);
        buffer.setVehicleId(vehicleId);
        buffer.setGroup(alert.getNotificationConfig().getGroup());
        buffer.setId(randomUUID().toString());
        buffer.setContactId(contactId);
        convertEmailAttachmentData(buffer, alert);
        LOGGER.info("Notification saving to buffer in saveSnoozedNotification {}", buffer);

        notificationBufferDao.save(buffer);
        LOGGER.info("Notification saved to buffer Successfully");
    }

    /**
     * Converts the AlertsInfo object to BufferedAlertsInfo object.
     */
    private BufferedAlertsInfo setBufferedAlertInfo(AlertsInfo alert) {
        BufferedAlertsInfo bufferedAlert = new BufferedAlertsInfo();
        IgniteEventImpl event = ((IgniteEventImpl) alert.getIgniteEvent()).headerClone();
        Map<Object, Object> originalEventData =
                ((GenericEventData) ((IgniteEventImpl) alert.getIgniteEvent()).getEventData()).getData();
        GenericEventData eventDataCopy = new GenericEventData();
        for (Map.Entry<?, ?> me : originalEventData.entrySet()) {
            eventDataCopy.set(me.getKey(), me.getValue());
        }
        event.setEventData(eventDataCopy);
        bufferedAlert.setIgniteEvent(event);
        bufferedAlert.setEventID(alert.getEventID());
        bufferedAlert.setVersion(alert.getVersion());
        bufferedAlert.setBenchMode(alert.getBenchMode());
        bufferedAlert.setTimestamp(alert.getTimestamp());
        bufferedAlert.setTimezone(alert.getTimezone());

        BufferedAlertsInfo.Data data = new BufferedAlertsInfo.Data();
        BeanUtils.copyProperties(alert.getAlertsData(), data);
        bufferedAlert.setAlertsData(data);

        CloneNotificationConfig cnc = new CloneNotificationConfig();
        BeanUtils.copyProperties(alert.getNotificationConfig(), cnc);
        bufferedAlert.setCloneNotificationConfig(cnc);

        CloneNotificationTemplate cnt = new CloneNotificationTemplate();
        BeanUtils.copyProperties(alert.getNotificationTemplate(), cnt);
        bufferedAlert.addCloneNotificationTemplate(cnc.getLocale(), cnt);

        CloneNotificationTemplateConfig cntc = new CloneNotificationTemplateConfig();
        BeanUtils.copyProperties(alert.getNotificationTemplateConfig(), cntc);
        bufferedAlert.setCloneNotificationTemplateConfig(cntc);

        return bufferedAlert;
    }

    /**
     * Converts the EmailAttachment object to LinkedHashMap object.
     */
    private void convertEmailAttachmentData(NotificationBuffer buffer, AlertsInfo alert) {

        Map<Object, Object> locale = new HashMap<>();
        alert.getNotificationConfigs().forEach(c -> locale.put(c.getLocale(), "locale"));

        LOGGER.debug("NotificationBuffer and locale  in convertEmailAttachmentData {} {}", buffer, locale);

        for (Map.Entry<?, ?> me : locale.entrySet()) {
            buffer.getAlertsInfo().forEach(bufferedAlertsInfo -> {
                if (((GenericEventData) bufferedAlertsInfo.getIgniteEvent().getEventData()).getData()
                        .get(me.getKey()) != null) {
                    List<EmailAttachment> emailAttachments =
                            (ArrayList<EmailAttachment>) ((GenericEventData) bufferedAlertsInfo
                                    .getIgniteEvent().getEventData()).getData().get(me.getKey());
                    List<LinkedHashMap<String, Object>> localeAttachments = new ArrayList<>();
                    emailAttachments.forEach(emailAttachment -> {
                        LinkedHashMap<String, Object> attachmentProperties = new LinkedHashMap<>();
                        attachmentProperties.put("fileName", emailAttachment.getFileName());
                        attachmentProperties.put("content", Base64.decodeBase64(emailAttachment.getContent()));
                        attachmentProperties.put("mimeType", emailAttachment.getMimeType());
                        attachmentProperties.put("inline", emailAttachment.isInline());
                        localeAttachments.add(attachmentProperties);
                    });
                    ((GenericEventData) bufferedAlertsInfo.getIgniteEvent().getEventData()).getData()
                            .put(me.getKey(), localeAttachments);
                }
            });
        }

    }

    /**
     * Converts the EmailAttachment object to LinkedHashMap object.
     */
    private void convertEmailAttachmentDataForUpdate(BufferedAlertsInfo bufferedAlertsInfo, AlertsInfo alert) {
        Map<Object, Object> locale = new HashMap<>();
        alert.getNotificationConfigs().forEach(c -> locale.put(c.getLocale(), "locale"));
        LOGGER.debug("BufferedAlertsInfo and locale  in convertEmailAttachmentDataForUpdate {} {}", bufferedAlertsInfo,
                locale);

        for (Map.Entry<?, ?> me : locale.entrySet()) {

            if (((GenericEventData) bufferedAlertsInfo.getIgniteEvent().getEventData()).getData().get(me.getKey())
                    !=
                    null) {
                List<EmailAttachment> emailAttachments =
                        (ArrayList<EmailAttachment>) ((GenericEventData) bufferedAlertsInfo
                                .getIgniteEvent().getEventData()).getData().get(me.getKey());
                List<LinkedHashMap<String, Object>> localeAttachments = new ArrayList<>();
                emailAttachments.forEach(emailAttachment -> {
                    LinkedHashMap<String, Object> attachmentProperties = new LinkedHashMap<>();
                    attachmentProperties.put("fileName", emailAttachment.getFileName());
                    attachmentProperties.put("mimeType", emailAttachment.getMimeType());
                    attachmentProperties.put("content", Base64.decodeBase64(emailAttachment.getContent()));
                    attachmentProperties.put("inline", emailAttachment.isInline());
                    localeAttachments.add(attachmentProperties);
                });
                ((GenericEventData) bufferedAlertsInfo.getIgniteEvent().getEventData()).getData()
                        .put(me.getKey(), localeAttachments);
            }
        }
    }

    /**
     * Submits the Scheduler Event.
     */
    void submitSchedulerEvent(IgniteKey<?> key, IgniteEvent schedulerEvent) {
        ctxt.forwardDirectly(key, schedulerEvent, schedulerSourceTopic);
    }

    /**
     * Sets the Scheduler Source Topic.
     */
    String getSchedulerSourceTopic() {
        return this.schedulerSourceTopic;
    }
}
