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

package org.eclipse.ecsp.platform.notification.create;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.Campaign;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.NonRegisteredUserData;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.domain.notification.NotificationNonRegisteredUser;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.notification.dao.CampaignDAO;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.ErrorSendingEventException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNotificationIdException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.ScheduledNotificationDeletionException;
import org.eclipse.ecsp.platform.notification.service.NotificationHistoryServiceImpl;
import org.eclipse.ecsp.platform.notification.utils.NotificationUtils;
import org.eclipse.ecsp.platform.notification.v1.fw.web.NotificationGroupingNotFoundException;
import org.eclipse.ecsp.platform.notification.v1.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.MAX_NON_REGISTERED_USERS_PER_REQUEST_HARD_LIMIT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_CLIENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_TOKEN;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_RECIPIENTS;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.PLATFORM_RESPONSE_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.SCHEDULE_NOTIFICATION;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.isValidLocale;

/**
 * NotificationServiceImpl class implements NotificationService.
 *
 * @author kjalawadi
 */

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private UserProfileDAO userProfileDao;

    @Autowired
    private CampaignDAO campaignDao;

    @Value("${kafka.sink.topic}")
    private String topic;

    @Value("${kafka.notification.scheduler.topic}")
    private String notificationScheduleTopic;

    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    @Autowired
    private NotificationHistoryServiceImpl notificationHistoryService;

    @Value("${enable.entitlement.validation:false}")
    private boolean enableEntitlementValidation;

    @Value("#{${maxNonRegisteredRecipientsPerRequest}<" + MAX_NON_REGISTERED_USERS_PER_REQUEST_HARD_LIMIT
        + "? ${maxNonRegisteredRecipientsPerRequest} :" + MAX_NON_REGISTERED_USERS_PER_REQUEST_HARD_LIMIT + "}")
    private int maxNonRegisterUserNotificationsPerRequest;

    @Value("${notification.schedule.max.period.days:365}")
    private int maxScheduleDays;

    private static final SecureRandom RANDOM;

    static {
        RANDOM = new SecureRandom();
    }

    /**
     * Create notification.
     *
     * @param notificationRequest notification request
     *
     * @return platformResponseId
     */
    @Override
    public String createNotification(NotificationCreationRequest notificationRequest) {
        String platformResponseId = null;
        String vehicleId = notificationRequest.getVehicleId();
        String requestId = notificationRequest.getRequestId();
        String userId = notificationRequest.getUserId();
        String topicToUse = topic;
        boolean isNonRegisteredVehicle = NotificationUtils.isNonRegisteredVehicle(notificationRequest);
        if (!isNonRegisteredVehicle && enableEntitlementValidation && notificationRequest.getVehicleId() != null
            && null == notificationRequest.getCampaignId()) {
            checkEntitlementServices(notificationRequest);
        }
        try {
            IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
            setEventId(notificationRequest, igniteEventImpl);
            igniteEventImpl.setVersion(Version.V1_0);
            igniteEventImpl.setTimestamp(notificationRequest.getTimestamp());
            igniteEventImpl.setTimezone(notificationRequest.getTimezone());
            igniteEventImpl.setVehicleId(vehicleId);
            igniteEventImpl.setRequestId(requestId);
            igniteEventImpl.setBizTransactionId(notificationRequest.getSessionId());

            GenericEventData genericEventData = new GenericEventData();
            genericEventData.set(NotificationConstants.USERID, userId);
            genericEventData.set(NotificationConstants.NOTIFICATION_ID, notificationRequest.getNotificationId());
            genericEventData.set(NotificationConstants.CAMPAIGN_ID, notificationRequest.getCampaignId());
            genericEventData.set(NotificationConstants.USER_NOTIFICATION, notificationRequest.isUserNotification());
            genericEventData.set(NotificationConstants.BRAND, notificationRequest.getBrand());
            for (Entry<String, Object> entry : notificationRequest.getData().entrySet()) {
                genericEventData.set(entry.getKey(), entry.getValue());
            }

            igniteEventImpl.setEventData(genericEventData);
            if (notificationRequest.getSchedule() != null) {
                handleScheduledNotification(igniteEventImpl, notificationRequest.getSchedule());
                topicToUse = notificationScheduleTopic;
            }
            kafkaService.sendIgniteEvent(StringUtils.isEmpty(vehicleId) ? userId : vehicleId, igniteEventImpl,
                topicToUse);
            platformResponseId = requestId;

            LOGGER.debug(
                "Create Notification Event sent to Kafka topic: RequestId={} userId={} "
                    + "vehicleId={} NotificationRequest={} topic={} Event={}",
                requestId, userId, vehicleId, notificationRequest, topicToUse, igniteEventImpl);
        } catch (InvalidInputException e) {
            throw e;
        } catch (DateTimeParseException e) {
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.INVALID_TIME_FORMAT.toMessage()));
        } catch (Exception e) {
            LOGGER.error(
                "Error sending Notification Event to Kafka: topic={} RequestId={} userId={} "
                    + "vehicleId={} NotificationRequest={} error={}",
                topicToUse, requestId, userId, vehicleId, notificationRequest, e.getMessage());
        }

        return platformResponseId;
    }

    /**
     * Set event id.
     *
     * @param notificationRequest notification request
     * @param igniteEventImpl ignite event
     */
    private void setEventId(NotificationCreationRequest notificationRequest, IgniteEventImpl igniteEventImpl) {
        if (notificationRequest.isCampaignNotification()) {
            igniteEventImpl.setEventId(EventID.VEHICLE_MESSAGE_PUBLISH);
        } else if (notificationRequest.isDynamicNotification()) {
            igniteEventImpl.setEventId(EventID.DYNAMIC_NOTIFICATION);
        } else {
            igniteEventImpl.setEventId(EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT.toString());
        }
    }

    /**
     * Method to handle the scheduled notification.
     *
     * @param igniteEventImpl igniteevent
     *
     * @param schedule schedule period
     */
    public void handleScheduledNotification(IgniteEventImpl igniteEventImpl, String schedule) {
        if (!isValidDelayPeriod(schedule)) {
            LOGGER.error("Invalid schedule period, maximum allowed {} days", maxScheduleDays);
            throw new InvalidInputException(Collections.singletonList(
                NotificationCenterError.SCHEDULED_NOTIFICATION_MAX_DELAY_INVALID.toMessage(
                    String.valueOf(maxScheduleDays), schedule)));
        }
        GenericEventData genericEventData = (GenericEventData) igniteEventImpl.getEventData();
        genericEventData.set(SCHEDULE_NOTIFICATION, schedule);
    }

    /**
     * Update nick name.
     *
     * @param userId userId
     * @param vehicleId vehicleId
     * @param nickName nickName
     *
     * @return boolean
     */
    @Override
    public boolean updateNickName(String userId, String vehicleId, String nickName) {
        boolean isSaved = false;
        LOGGER.debug("Received update NickName {}", nickName);
        try {
            userProfileDao.updateNickName(userId, vehicleId, nickName);
            isSaved = true;
        } catch (Exception e) {
            LOGGER.error("Error while saving user NickName", e);
        }
        return isSaved;
    }

    /**
     * Update consent.
     *
     * @param userId userId
     * @param consent consent
     *
     * @return boolean
     */
    @Override
    public boolean updateConsent(String userId, boolean consent) {
        boolean isSaved = false;
        LOGGER.debug("Received update consent {}", consent);
        try {
            userProfileDao.updateConsent(userId, consent);
            isSaved = true;
        } catch (Exception e) {
            LOGGER.error("Error while saving user Consent", e);
        }
        return isSaved;
    }

    /**
     * Get nick name by userId and vehicleId.
     *
     * @param userProfile userProfile
     * @param userId userId
     * @param vehicleId vehicleId
     *
     * @return nickName
     */
    @Override
    public String getNickNameByUserIdVehicleId(UserProfile userProfile, String userId, String vehicleId) {
        String nickName = null;
        if (null != userProfile) {
            nickName = userProfile.getNickName(vehicleId);
        }
        return nickName;
    }

    /**
     * Save the campaign.
     */
    public boolean saveCampaign(Campaign campaign) {
        boolean isSaved = false;
        LOGGER.debug("Save campaignId {}", campaign);
        try {
            campaignDao.save(campaign);
            isSaved = true;
        } catch (Exception e) {
            LOGGER.error("Error while saving campaign ", e);
        }
        return isSaved;
    }

    /**
     * Get the campaign by campaign id.
     */
    public Campaign getCampaign(String campaignId) {
        Campaign campaign = null;
        try {
            campaign = campaignDao.findById(campaignId);
            LOGGER.debug("campaign= {}", campaign);
        } catch (Exception e) {
            LOGGER.error("Error occurred while getting campaignId ", e);
        }
        return campaign;
    }

    /**
     * Get the user profile by userId.
     */
    public UserProfile getUserProfile(String userId) {
        return userProfileDao.findById(userId);
    }

    /**
     * Check entitlement services.
     *
     * @param notificationRequest notification request
     *
     * @throws AuthorizationException authorization exception
     * @throws NoSuchEntityException no such entity exception
     */
    @Override
    public void checkEntitlementServices(NotificationCreationRequest notificationRequest)
        throws AuthorizationException, NoSuchEntityException {

        List<NotificationGrouping> notificationGroupingList = notificationGroupingDao
            .findByNotificationId(notificationRequest.getNotificationId());
        if (CollectionUtils.isEmpty(notificationGroupingList)) {
            throw new NotificationGroupingNotFoundException(
                "Notification Grouping not found for " + notificationRequest.getNotificationId());
        }
        vehicleService.validateServiceEnabled(notificationRequest.getVehicleId(), notificationGroupingList);
    }


    /**
     * Create notification for non registered users.
     *
     * @param notificationNonRegisteredUser notificationNonRegisteredUser
     * @param requestId requestId
     * @param sessionId sessionId
     *
     * @throws ExecutionException execution exception
     */
    @Override
    public void createNotificationForNonRegisteredUsers(NotificationNonRegisteredUser notificationNonRegisteredUser,
                                                        String requestId,
                                                        String sessionId) throws ExecutionException {

        validateNonRegisteredUsersRequestData(notificationNonRegisteredUser);
        sendNotificationForNonRegisteredUsers(notificationNonRegisteredUser, requestId, sessionId);
    }

    /**
     * Validate non registered users request data.
     *
     * @param notificationNonRegisteredUser notificationNonRegisteredUser
     */
    private void validateNonRegisteredUsersRequestData(NotificationNonRegisteredUser notificationNonRegisteredUser) {

        if (StringUtils.isEmpty(notificationNonRegisteredUser.getNotificationId())) {
            throw new InvalidNotificationIdException(
                Collections.singletonList(NotificationCenterError.INPUT_MANDATORY_NOTIFICATION_ID.toMessage()));
        }

        if (!dynamicNotificationTemplateDao.isNotificationIdExist(notificationNonRegisteredUser.getNotificationId())) {
            throw new InvalidNotificationIdException(
                Collections.singletonList(NotificationCenterError.NOTIFICATION_ID_DOES_NOT_EXIST.toMessage()));
        }

        if (notificationNonRegisteredUser.getRecipients() == null
            || notificationNonRegisteredUser.getRecipients().isEmpty()) {
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.NON_REGISTERED_INPUT_MISSING_RECIPIENTS.toMessage()));
        }

        if (notificationNonRegisteredUser.getRecipients().size() > maxNonRegisterUserNotificationsPerRequest) {
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.NON_REGISTERED_INPUT_MAX_RECIPIENTS_EXCEEDED
                    .toMessage(String.valueOf(maxNonRegisterUserNotificationsPerRequest),
                        String.valueOf(notificationNonRegisteredUser.getRecipients().size()))));
        }

        List<ResponseWrapper.Message> recipientsErrors =
            validateRecipientsData(notificationNonRegisteredUser.getRecipients());
        if (!CollectionUtils.isEmpty(recipientsErrors)) {
            throw new InvalidInputException(recipientsErrors);
        }
    }

    /**
     * Send notification for non registered users.
     *
     * @param notificationNonRegisteredUser notificationNonRegisteredUser
     * @param requestId requestId
     * @param sessionId sessionId
     *
     * @throws ExecutionException execution exception
     */
    private void sendNotificationForNonRegisteredUsers(NotificationNonRegisteredUser notificationNonRegisteredUser,
                                                       String requestId,
                                                       String sessionId) throws ExecutionException {

        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setEventId(EventID.NON_REGISTERED_USER_NOTIFICATION_EVENT);
        igniteEventImpl.setVersion(Version.V1_0);
        igniteEventImpl.setTimestamp(System.currentTimeMillis());
        igniteEventImpl.setTimezone((short) 0);
        igniteEventImpl.setRequestId(requestId);
        igniteEventImpl.setBizTransactionId(sessionId);

        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set(NotificationConstants.NOTIFICATION_ID, notificationNonRegisteredUser.getNotificationId());
        genericEventData.set(NON_REGISTERED_FIELD_RECIPIENTS, notificationNonRegisteredUser.getRecipients());

        igniteEventImpl.setEventData(genericEventData);
        String randomKey = getRandomKey();
        kafkaService.sendIgniteEvent(randomKey, igniteEventImpl, topic);

        LOGGER.debug(
            "sendNotificationForNonRegisteredUsers Event sent to Kafka topic: RequestId={}  "
                + "notificationNonRegisteredUser={} topic={} Event={}",
            requestId, notificationNonRegisteredUser, topic, igniteEventImpl);
    }

    /**
     * Validate recipients data.
     *
     * @param recipients recipients
     *
     * @return list of errors
     */
    private List<ResponseWrapper.Message> validateRecipientsData(List<NonRegisteredUserData> recipients) {
        List<ResponseWrapper.Message> errors = new ArrayList<>();
        int noChannelRecipientCount = 0;
        List<String> invalidEmails = new ArrayList<>();
        List<String> invalidSms = new ArrayList<>();
        List<String> invalidPortal = new ArrayList<>();
        for (NonRegisteredUserData nonRegisteredUserData : recipients) {

            if (checkIfNoChannel(nonRegisteredUserData)) {
                noChannelRecipientCount++;
            }
            if (checkLocale(nonRegisteredUserData)) {
                errors.add(NotificationCenterError.NON_REGISTERED_INPUT_INVALID_LOCALE.toMessage(
                    nonRegisteredUserData.getUserIdentifier(),
                    nonRegisteredUserData.getLocale()));
            }

            if (!StringUtils.isEmpty(nonRegisteredUserData.getEmail())
                && !Utils.isValidEmail(nonRegisteredUserData.getEmail())) {
                invalidEmails.add(nonRegisteredUserData.getEmail());
            }

            if (!StringUtils.isEmpty(nonRegisteredUserData.getSms())
                && !Utils.isValidSms(nonRegisteredUserData.getSms())) {
                invalidSms.add(nonRegisteredUserData.getSms());
            }
            if (!StringUtils.isEmpty(nonRegisteredUserData.getPortal())
                && !Utils.isValidPortal(nonRegisteredUserData.getPortal())) {
                invalidPortal.add(nonRegisteredUserData.getPortal());
            }
        }

        if (noChannelRecipientCount > 0) {
            errors.add(NotificationCenterError.NON_REGISTERED_INPUT_MISSING_RECIPIENT_CHANNELS
                .toMessage(String.valueOf(noChannelRecipientCount)));
        }
        errors.addAll(updateResponse(invalidEmails, invalidSms, invalidPortal));

        return errors;
    }

    /**
     * Check if locale is valid.
     *
     * @param nonRegisteredUserData nonRegisteredUserData
     *
     * @return boolean
     */
    private static boolean checkLocale(NonRegisteredUserData nonRegisteredUserData) {
        return !StringUtils.isEmpty(nonRegisteredUserData.getLocale())
            && !isValidLocale(nonRegisteredUserData.getLocale());
    }

    /**
     * Update response.
     *
     * @param invalidEmails invalidEmails
     * @param invalidSms invalidSms
     * @param invalidPortal invalidPortal
     *
     * @return list of errors
     */
    private static List<ResponseWrapper.Message> updateResponse(List<String> invalidEmails,
                                                           List<String> invalidSms, List<String> invalidPortal) {
        List<ResponseWrapper.Message> errors = new ArrayList<>();
        if (!invalidEmails.isEmpty()) {
            errors.add(NotificationCenterError.NON_REGISTERED_INPUT_INVALID_EMAIL.toMessage(invalidEmails));
        }
        if (!invalidSms.isEmpty()) {
            errors.add(NotificationCenterError.NON_REGISTERED_INPUT_INVALID_SMS.toMessage(invalidSms));
        }
        if (!invalidPortal.isEmpty()) {
            errors.add(NotificationCenterError.NON_REGISTERED_INPUT_INVALID_PORTAL.toMessage(invalidPortal));
        }
        return errors;
    }

    /**
     * Check if no channel.
     *
     * @param nonRegisteredUserData nonRegisteredUserData
     *
     * @return boolean
     */
    private static boolean checkIfNoChannel(NonRegisteredUserData nonRegisteredUserData) {
        return StringUtils.isEmpty(nonRegisteredUserData.getEmail())
                && StringUtils.isEmpty(nonRegisteredUserData.getSms())
                && (nonRegisteredUserData.getPush() == null
                || !nonRegisteredUserData.getPush().containsKey(NON_REGISTERED_FIELD_PUSH_TOKEN)
                || !nonRegisteredUserData.getPush().containsKey(NON_REGISTERED_FIELD_PUSH_CLIENT))
                && StringUtils.isEmpty(nonRegisteredUserData.getPortal());
    }

    /**
     * Send the delete schedule command.
     *
     * @param igniteEventImpl event
     */
    public void sendDeleteScheduledNotificationCommand(IgniteEventImpl igniteEventImpl) {
        igniteEventImpl.setEventId(EventID.DELETE_SCHEDULED_NOTIFICATION_COMMAND);
        igniteEventImpl.setVersion(Version.V1_0);
        igniteEventImpl.setTimestamp(System.currentTimeMillis());
        igniteEventImpl.setTimezone((short) 0);

        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set(PLATFORM_RESPONSE_ID, igniteEventImpl.getRequestId());
        String randomKey = getRandomKey();
        igniteEventImpl.setEventData(genericEventData);
        try {
            kafkaService.sendIgniteEvent(randomKey, igniteEventImpl, notificationScheduleTopic);
        } catch (Exception e) {
            LOGGER.error("Failed writing event to kafka topic: {} event: {}", notificationScheduleTopic,
                igniteEventImpl);
            throw new ErrorSendingEventException(Collections.emptyList());
        }
    }

    /**
     * Delete scheduled notification.
     *
     * @param igniteEvent igniteEvent
     */
    @Override
    public void deleteScheduledNotification(IgniteEventImpl igniteEvent) {
        String queryParamStatus = "status";
        try {
            NotificationChannelDetails notificationChannelDetails = notificationHistoryService
                .getNotificationStatus(igniteEvent.getRequestId(), queryParamStatus, igniteEvent.getVehicleId());

            List<StatusHistoryRecord> statusHistoryRecords = notificationChannelDetails.getStatusHistoryRecordList();
            if (!isStatusAllowDeletion(statusHistoryRecords)) {
                LOGGER.error("Notification cannot be cancelled at this status {}", statusHistoryRecords);
                throw new ScheduledNotificationDeletionException(Collections.emptyList());
            }
            sendDeleteScheduledNotificationCommand(igniteEvent);
        } catch (javassist.NotFoundException e) {
            LOGGER.error("Scheduled notification was not found for platformResponseId: {}", igniteEvent.getRequestId());
            throw new NotFoundException(Collections.emptyList());
        }
    }

    /**
     * Check status allow deletion.
     *
     * @param statusHistoryRecords statusHistory records
     *
     * @return boolean
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public boolean isStatusAllowDeletion(List<StatusHistoryRecord> statusHistoryRecords) {
        return (null != statusHistoryRecords && statusHistoryRecords.size() == 2
            && statusHistoryRecords.stream().map(StatusHistoryRecord::getStatus)
                .toList()
                .containsAll(
                    Arrays.asList(AlertsHistoryInfo.Status.SCHEDULE_REQUESTED, AlertsHistoryInfo.Status.SCHEDULED)));
    }

    /**
     * Validate schedule time string.
     *
     * @param scheduleTimeStr scheduleTime
     *
     * @return boolean
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public boolean isValidDelayPeriod(String scheduleTimeStr) {
        Instant scheduleTime =
            LocalDateTime.parse(scheduleTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC"))
                .toInstant();
        long delay = (scheduleTime.getEpochSecond() - Instant.now().getEpochSecond()) * 1000;
        long delayDays = TimeUnit.MILLISECONDS.toDays(delay);
        return (delayDays <= maxScheduleDays);
    }

    // for testing
    void setMaxNonRegisterUserNotificationsPerRequest(int maxNonRegisterUserNotificationsPerRequest) {
        this.maxNonRegisterUserNotificationsPerRequest = maxNonRegisterUserNotificationsPerRequest;
    }

    // for testing
    void setEnableEntitlementValidation(boolean enableEntitlementValidation) {
        this.enableEntitlementValidation = enableEntitlementValidation;
    }

    // for testing
    public void setMaxScheduleDays(int maxScheduleDays) {
        this.maxScheduleDays = maxScheduleDays;
    }

    // for testing
    public void setTopic(String topic) {
        this.topic = topic;
    }

    // for testing
    public void setNotificationScheduleTopic(String notificationScheduleTopic) {
        this.notificationScheduleTopic = notificationScheduleTopic;
    }

    // for testing
    @SuppressWarnings("checkstyle:MagicNumber")
    private String getRandomKey() {
        byte[] result = new byte[32];
        RANDOM.nextBytes(result);
        return Hex.encodeHexString(result);
    }
}
