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

package org.eclipse.ecsp.notification.feedback;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.commons.ChannelFeedback;
import org.eclipse.ecsp.domain.notification.commons.ChannelResponseData;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.commons.NotificationFeedbackEventDataV1_0;
import org.eclipse.ecsp.domain.notification.commons.NotificationLifecycleFeedbackEventData;
import org.eclipse.ecsp.domain.notification.commons.NotificationSendingFeedbackEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.FEEDBACK_KEY;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.FEEDBACK_TOPIC;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.MILESTONE_FEEDBACK_KEY;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.MILESTONE_FEEDBACK_TOPIC;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_CAMPAIGN_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.USERID;

/**
 * NotificationFeedbackHandler class.
 */
@Component
@ConditionalOnProperty(value = "notification.feedback.enabled", havingValue = "true")
public class NotificationFeedbackHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationFeedbackHandler.class);

    private static StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessor;

    private static boolean isFeedBackEnabled;

    private static boolean isDefaultFeedbackTopicEnabled;

    private static String defaultFeedbackTopic;
    
    private NotificationFeedbackHandler() {
    }

    /**
     *  init method.
     *
     * @param streamProcessingContext StreamProcessingContext
     * @param feedBackEnabled boolean
     * @param defaultFeedbackTopicEnabled boolean
     * @param defaultNotificationFeedbackTopic String
     */
    public static void init(StreamProcessingContext<IgniteKey<?>,
            IgniteEvent> streamProcessingContext, boolean feedBackEnabled,
                            boolean defaultFeedbackTopicEnabled, String defaultNotificationFeedbackTopic) {
        streamProcessor = streamProcessingContext;
        isFeedBackEnabled = feedBackEnabled;
        isDefaultFeedbackTopicEnabled = defaultFeedbackTopicEnabled;
        defaultFeedbackTopic = defaultNotificationFeedbackTopic;
    }

    /**
     * sendNotificationChannelLevelFeedback method.
     *
     * @param alert           AlertsInfo
     * @param key             IgniteKey
     * @param sourceEvent     IgniteEvent
     * @param alertHistoryObj AlertsHistoryInfo
     */
    public static void sendNotificationChannelLevelFeedback(AlertsInfo alert, IgniteKey<?> key, IgniteEvent sourceEvent,
                                                            AlertsHistoryInfo alertHistoryObj) {
        LOGGER.debug("Send Channel Level Feedback for alert {} ,alertHistory {}", alert, alertHistoryObj.getId());

        String feedbackTopic = alert != null ? getTopicName(alert) : null;
        IgniteEventImpl feedbackEvent = null;
        LOGGER.debug("feebackTopic: {} isFeedbackEnabled: {}", feedbackTopic, isFeedBackEnabled);
        if (isFeedBackEnabled && StringUtils.isNotEmpty(feedbackTopic)) {

            if (alertHistoryObj.getProcessingStatus() != null
                    && NotificationConstants.FAILURE.equalsIgnoreCase(
                            alertHistoryObj.getProcessingStatus().getStatus())) {

                feedbackEvent = createIgniteEvent(sourceEvent, EventID.NOTIFICATION_FEEDBACK);

                NotificationFeedbackEventDataV1_0 data = new NotificationFeedbackEventDataV1_0();
                data.setNotificationStatus(NotificationConstants.FAILURE);
                data.setNotificationErrorDetail(alertHistoryObj.getProcessingStatus().getErrorMessage());
                feedbackEvent.setEventData(data);
                IgniteKey<?> eventKey = getFeedbackEventKey(alert, key, FEEDBACK_KEY);
                LOGGER.info("Sending Processing Failure feedback to kafka: key={}, event={}, topic={}", eventKey,
                        feedbackEvent, feedbackTopic);

            } else {

                NotificationFeedbackEventDataV1_0 channelFeedback = getNotificationChannelLevelFeedbackEventData(
                        alertHistoryObj);

                feedbackEvent = createIgniteEvent(sourceEvent, EventID.NOTIFICATION_FEEDBACK);
                feedbackEvent.setEventData(channelFeedback);
                LOGGER.info("Sending Channel Level feedback to kafka: key={}, event={}, topic={}", key, feedbackEvent,
                        feedbackTopic);
            }

            streamProcessor.forwardDirectly(key, feedbackEvent, feedbackTopic);
        }
    }

    /**
     * setDefaultFeedbackTopic method.
     *
     * @return defaultFeedbackTopic
     */
    private static String setDefaultFeedbackTopic() {
        if (isDefaultFeedbackTopicEnabled) {
            LOGGER.debug("Default feedback topic is enabled and the topic is {}", defaultFeedbackTopic);
            return defaultFeedbackTopic;
        }
        return null;
    }

    /**
     * getNotificationChannelLevelFeedbackEventData method.
     *
     * @param alertHistoryInfo AlertsHistoryInfo
     * @return NotificationFeedbackEventDataV1_0
     */
    private static NotificationFeedbackEventDataV1_0 getNotificationChannelLevelFeedbackEventData(
            AlertsHistoryInfo alertHistoryInfo) {
        NotificationFeedbackEventDataV1_0 notificationChannelDetails = new NotificationFeedbackEventDataV1_0();
        notificationChannelDetails.setGroup(alertHistoryInfo.getGroup());
        notificationChannelDetails.setNotificationName(alertHistoryInfo.getNotificationLongName());
        notificationChannelDetails.setNotificationId(alertHistoryInfo.getNotificationId());
        for (ChannelResponse response : alertHistoryInfo.getChannelResponses()) {
            ChannelFeedback channel = new ChannelFeedback();
            channel.setChannelType(response.getChannelType());
            channel.setStatus(response.getStatus());
            if (StringUtils.isNotBlank(response.getStatus())
                    && StringUtils.equals(response.getStatus(), NotificationConstants.FAILURE)
                    && response.getErrorCode() != null) {
                channel.setChannelResponse(response.getErrorCode().toString());
            }
            notificationChannelDetails.getChannelFeedbacks().add(channel);
        }
        notificationChannelDetails.setStatusHistoryRecordList(alertHistoryInfo.getStatusHistoryRecordList());
        Map<String, String> skippedChannels = alertHistoryInfo.getSkippedChannels();
        if (!skippedChannels.isEmpty()) {
            for (Map.Entry<String, String> entry : skippedChannels.entrySet()) {
                ChannelFeedback skippedChannel = new ChannelFeedback();

                skippedChannel.setChannelType(ChannelType.getChannelType(entry.getKey()));
                skippedChannel.setStatus(NotificationConstants.NOTIFICATION_STATUS_FAILURE);
                skippedChannel.setChannelResponse(entry.getValue());
                notificationChannelDetails.getChannelFeedbacks().add(skippedChannel);
            }

        }

        return notificationChannelDetails;
    }

    /**
     * sendNotificationSendingFeedback method.
     *
     * @param alert              AlertsInfo
     * @param sourceEvent        IgniteEvent
     * @param channelResponse    ChannelResponse
     * @param platformResponseId String
     */
    public static void sendNotificationSendingFeedback(AlertsInfo alert, IgniteEvent sourceEvent,
                                                       ChannelResponse channelResponse, String platformResponseId) {
        String feedbackTopic = getMilestoneFeedbackTopicName(alert);
        if (isFeedBackEnabled && StringUtils.isNotEmpty(feedbackTopic) && Objects.nonNull(channelResponse)) {
            ChannelResponseData channelResponseData = getChannelResponseData(channelResponse);
            NotificationSendingFeedbackEventData feedbackEventData =
                    getNotificationSendingFeedbackEventData(sourceEvent, alert, channelResponse, channelResponseData);

            IgniteEventImpl feedbackEvent = createIgniteEvent(sourceEvent, EventID.NOTIFICATION_MILESTONE_FEEDBACK);
            feedbackEvent.setEventData(feedbackEventData);
            IgniteKey<?> eventKey =
                    getFeedbackEventKey(alert, new IgniteStringKey(platformResponseId), MILESTONE_FEEDBACK_KEY);
            LOGGER.info("Sending feedback to kafka: key={}, event={}, topic={}", eventKey, feedbackEvent,
                    feedbackTopic);
            streamProcessor.forwardDirectly(eventKey, feedbackEvent, feedbackTopic);
        }
    }

    /**
     * sendCampaignLifecycleFeedback method.
     *
     * @param alert AlertsInfo
     * @param sourceEvent IgniteEvent
     * @param key IgniteKey
     * @param lifecycleStatus String
     * @param code String
     * @param message String
     */
    public static void sendCampaignLifecycleFeedback(AlertsInfo alert, IgniteEvent sourceEvent, IgniteKey<?> key,
                                                     String lifecycleStatus, String code, String message) {
        String feedbackTopic = getTopicName(alert);
        if (isFeedBackEnabled && StringUtils.isNotEmpty(feedbackTopic)) {
            NotificationLifecycleFeedbackEventData lifecycleFeedback =
                    getNotificationLifecycleFeedbackEventData(alert, lifecycleStatus, code, message);
            IgniteEventImpl feedbackEvent = createIgniteEvent(sourceEvent, EventID.NOTIFICATION_LIFECYCLE_FEEDBACK);
            feedbackEvent.setEventData(lifecycleFeedback);
            IgniteKey<?> eventKey = getFeedbackEventKey(alert, key, FEEDBACK_KEY);
            LOGGER.info("Sending lifecycle feedback to kafka: key={}, event={}, topic={}", eventKey, feedbackEvent,
                    feedbackTopic);
            streamProcessor.forwardDirectly(eventKey, feedbackEvent, feedbackTopic);
        }
    }


    /**
     * getMilestoneFeedbackTopicName method.
     *
     * @param alert AlertsInfo
     * @return milestoneFeedbackTopicName
     */
    private static String getMilestoneFeedbackTopicName(AlertsInfo alert) {
        return (String) alert.getAlertsData().any().get(MILESTONE_FEEDBACK_TOPIC);
    }

    /**
     * getTopicName method.
     *
     * @param alert AlertsInfo
     * @return topicName
     */
    private static String getTopicName(AlertsInfo alert) {
        if (EventID.VEHICLE_MESSAGE_PUBLISH.equals(alert.getEventID())
                && alert.getAlertsData() != null
                && alert.getAlertsData().any() != null) {
            Map<String, Object> data = alert.getAlertsData().any();
            Map<String, Object> additionalData = (Map<String, Object>) data.get("additionalData") != null
                    ? (Map<String, Object>) data.get("additionalData") : new HashMap<>();
            return (String) additionalData.get(FEEDBACK_TOPIC) != null
                    ? (String) additionalData.get(FEEDBACK_TOPIC)
                    : setDefaultFeedbackTopic();
        }
        return (String) alert.getAlertsData().any().get(FEEDBACK_TOPIC) != null
                ? (String) alert.getAlertsData().any().get(FEEDBACK_TOPIC)
                : setDefaultFeedbackTopic();
    }

    /**
     * getChannelResponseData method.
     *
     * @param channelResponse ChannelResponse
     * @return channelResponseData
     */
    @NotNull
    private static ChannelResponseData getChannelResponseData(ChannelResponse channelResponse) {
        ChannelResponseData channelResponseData = new ChannelResponseData();
        channelResponseData.setChannelType(channelResponse.getChannelType().getChannelType());
        channelResponseData.setStatus(channelResponse.getStatus());
        channelResponseData.setDestination(channelResponse.getDestination());
        return channelResponseData;
    }

    /**
     * getNotificationSendingFeedbackEventData method.
     *
     * @param sourceEvent IgniteEvent
     * @param alert AlertsInfo
     * @param channelResponse ChannelResponse
     * @param channelResponseData ChannelResponseData
     * @return feedbackEventData
     */
    @NotNull
    private static NotificationSendingFeedbackEventData
          getNotificationSendingFeedbackEventData(IgniteEvent sourceEvent, AlertsInfo alert,
                                                                          ChannelResponse channelResponse,
                                                                          ChannelResponseData channelResponseData) {
        NotificationSendingFeedbackEventData feedbackEventData = new NotificationSendingFeedbackEventData();
        feedbackEventData.setMilestone(channelResponse.getChannelType().toString().toLowerCase(Locale.ROOT)
                .concat(channelResponse.getStatus().toUpperCase(Locale.ROOT)));
        if (Objects.nonNull(alert.getAlertsData().getAlertDataProperties().get(NOTIFICATION_CAMPAIGN_ID))) {
            feedbackEventData.setCampaignId(alert.getAlertsData().any().get(NOTIFICATION_CAMPAIGN_ID).toString());
        }
        feedbackEventData.setVehicleId(sourceEvent.getVehicleId());
        feedbackEventData.setUserId(channelResponse.getUserID());
        feedbackEventData.setChannelResponse(channelResponseData);
        return feedbackEventData;
    }

    /**
     * getNotificationLifecycleFeedbackEventData method.
     *
     * @param alert AlertsInfo
     * @param lifecycleStatus String
     * @param code String
     * @param message String
     * @return NotificationLifecycleFeedbackEventData
     */
    @NotNull
    private static NotificationLifecycleFeedbackEventData
              getNotificationLifecycleFeedbackEventData(AlertsInfo alert,
                                                          String lifecycleStatus,
                                                          String code,
                                                          String message) {
        return NotificationLifecycleFeedbackEventData.builder()
                .campaignId((String) alert.getAlertsData().any().get(NOTIFICATION_CAMPAIGN_ID))
                .vehicleId(alert.getIgniteEvent().getVehicleId())
                .userId((String) alert.getAlertsData().any().get(USERID))
                .milestone(lifecycleStatus).code(code).message(message)
                .build();
    }

    /**
     * createIgniteEvent method.
     *
     * @param event IgniteEvent
     * @param eventId String
     * @return igniteEvent
     */
    private static IgniteEventImpl createIgniteEvent(IgniteEvent event, String eventId) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId(eventId);
        igniteEvent.setVersion(Version.V1_0);
        igniteEvent.setTimestamp(event.getTimestamp());
        igniteEvent.setTimezone(event.getTimezone());
        igniteEvent.setVehicleId(event.getVehicleId());
        igniteEvent.setRequestId(event.getRequestId());
        igniteEvent.setBizTransactionId(event.getBizTransactionId());
        igniteEvent.setCorrelationId(event.getCorrelationId());
        return igniteEvent;
    }

    /**
     * getFeedbackEventKey method.
     *
     * @param alert AlertsInfo
     * @param defaultValue IgniteKey
     * @param feedbackEventKeyName String
     * @return feedbackEventKey
     */
    private static IgniteKey<?> getFeedbackEventKey(AlertsInfo alert, IgniteKey<?> defaultValue,
                                                    String feedbackEventKeyName) {
        if (StringUtils.isEmpty((String) alert.getAlertsData().any().get(feedbackEventKeyName))) {
            return defaultValue;
        }

        return new IgniteStringKey((String) alert.getAlertsData().any().get(feedbackEventKeyName));
    }

}
