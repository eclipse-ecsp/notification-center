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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.NonRegisteredUserData;
import org.eclipse.ecsp.domain.notification.NotificationNonRegisteredUser;
import org.eclipse.ecsp.domain.notification.PortalChannel;
import org.eclipse.ecsp.domain.notification.PushChannel;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.ProcessingStatus;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.feedback.NotificationFeedbackHandler;
import org.eclipse.ecsp.notification.processors.AlertProcessorChain;
import org.eclipse.ecsp.notification.processors.ContentTransformersManagerProcessor;
import org.eclipse.ecsp.notification.processors.NotificationMsgGenerator;
import org.eclipse.ecsp.notification.processors.NotificationProcessor;
import org.eclipse.ecsp.notification.processors.NotificationTemplateFinder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.DONE;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.FAILED;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.READY;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.FEEDBACK_TOPIC;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.MAX_NON_REGISTERED_USERS_PER_REQUEST_HARD_LIMIT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_CLIENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_TOKEN;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_ID;
import static org.eclipse.ecsp.notification.VehicleInfoNotification.MAPPER;

@Service
class VehicleInfoNotificationNonRegisteredUser {
    static final String NON_REGISTERED_USERS_GROUP = "nonRegisteredUsers";
    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleInfoNotificationNonRegisteredUser.class);
    /**
     * VEHCILE_PROFILE.
     */
    public static final String VEHICLE_PROFILE = "vehicleProfile";
    /**
     * VEHICLE_ID.
     */
    public static final String VEHICLE_ID = "vehicleId";
    private final List<NotificationProcessor> alertProcessorChainUnregisteredUsers;
    private final AlertsHistoryAssistant alertsHistoryAssistant;
    private final ObjectMapper objectMapper;

    @Value("#{${maxNonRegisteredRecipientsPerRequest}<" + MAX_NON_REGISTERED_USERS_PER_REQUEST_HARD_LIMIT
            + "? ${maxNonRegisteredRecipientsPerRequest} :" + MAX_NON_REGISTERED_USERS_PER_REQUEST_HARD_LIMIT + "}")
    private int maxNonRegisteredRecipientsPerRequestSoftLimit;

    /**
     * VehicleInfoNotificationNonRegisteredUser constructor.
     *
     * @param alertProcessorChain AlertProcessorChain
     * @param alertsHistoryAssistant AlertsHistoryAssistant
     */
    @Autowired
    public VehicleInfoNotificationNonRegisteredUser(AlertProcessorChain alertProcessorChain,
                                                    AlertsHistoryAssistant alertsHistoryAssistant) {
        this.alertsHistoryAssistant = alertsHistoryAssistant;
        List<NotificationProcessor> processors = alertProcessorChain.getProcessors();
        alertProcessorChainUnregisteredUsers = Arrays.asList(
                processors.stream().filter(p -> p instanceof NotificationTemplateFinder).findFirst().get(),
                processors.stream().filter(p -> p instanceof NotificationMsgGenerator).findFirst().get(),
                processors.stream().filter(p -> p instanceof ContentTransformersManagerProcessor).findFirst().get());
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * processNonRegisterUserEvent method.
     *
     * @param key IgniteKey
     * @param value IgniteEvent
     * @param channelsSupported Set
     * @param channelNotifierRegistry ChannelNotifierRegistry
     * @param defaultLocale String
     */
    void processNonRegisterUserEvent(IgniteKey<?> key, IgniteEvent value, Set<ChannelType> channelsSupported,
                                     ChannelNotifierRegistry channelNotifierRegistry,
                                     String defaultLocale) {
        if (value.getEventData() != null) {
            GenericEventData genericEventData = (GenericEventData) value.getEventData();
            List<NonRegisteredUserData> recipients = null;
            try {
                String data = MAPPER.writeValueAsString(genericEventData.getData());
                NotificationNonRegisteredUser notificationNonRegisteredUser =
                        MAPPER.readValue(data, NotificationNonRegisteredUser.class);
                recipients = notificationNonRegisteredUser.getRecipients();
            } catch (IOException e) {
                LOGGER.error("Failed deserialization of non-registered users event", e);
            }

            if (!CollectionUtils.isEmpty(recipients)
                    &&
                    recipients.size() > maxNonRegisteredRecipientsPerRequestSoftLimit) {
                LOGGER.warn("Received {} recipients. Max recipients allowed in one request is {}", recipients.size(),
                        maxNonRegisteredRecipientsPerRequestSoftLimit);
                prepareBasicAlertHistoryAndSendFeedback(
                        key, value, recipients, genericEventData.getData().get(NOTIFICATION_ID).toString(),
                        String.format("Recieved recipients size : %s. Max recipients allowed in one request is %s",
                                recipients.size(), maxNonRegisteredRecipientsPerRequestSoftLimit));
                return;
            }

            if (!CollectionUtils.isEmpty(recipients)) {
                sendNotificationToNonRegisteredUsers(key, recipients, value,
                        genericEventData.getData().get(NOTIFICATION_ID).toString(),
                        channelsSupported, channelNotifierRegistry, defaultLocale);
            }
        }
    }

    /**
     * prepareBasicAlertHistoryAndSendFeedback method.
     *
     * @param key IgniteKey
     * @param value IgniteEvent
     * @param recipients List
     * @param notificationId String
     * @param errMsg String
     */
    private void prepareBasicAlertHistoryAndSendFeedback(
            IgniteKey<?> key, IgniteEvent value,
            List<NonRegisteredUserData> recipients, String notificationId, String errMsg) {
        NotificationConfig notificationConfig = getNonRegisteredUserNotificationConfig();
        AlertsInfo basicAlertsInfo = getBasicAlertInfo(value, notificationConfig);
        for (NonRegisteredUserData nonRegisteredUserData : recipients) {
            AlertsInfo alertsInfo = convertRecipientDataToAlertInfo(nonRegisteredUserData, value, notificationId);
            if (!(basicAlertsInfo.getAlertsData() != null
                    && basicAlertsInfo.getAlertsData().any().containsKey(FEEDBACK_TOPIC))
                    && alertsInfo.getAlertsData().any().containsKey(FEEDBACK_TOPIC)) {
                basicAlertsInfo.setAlertsData(alertsInfo.getAlertsData());
                break;
            }
        }
        AlertsHistoryInfo alertHistoryObj = alertsHistoryAssistant.setEnrichedAlertHistory(basicAlertsInfo,
                new AlertsHistoryInfo());
        alertHistoryObj.addStatus(READY);
        setProcessingStatus(NotificationConstants.FAILURE, alertHistoryObj, errMsg);
        alertHistoryObj.addStatus(FAILED);
        alertsHistoryAssistant.saveAlertHistory("", "", basicAlertsInfo, alertHistoryObj);

        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                basicAlertsInfo, key, value, alertHistoryObj);

    }

    /**
     * sendNotificationToNonRegisteredUsers method.
     *
     * @param key IgniteKey
     * @param recipients List
     * @param event IgniteEvent
     * @param notificationId String
     * @param channelsSupported Set
     * @param channelNotifierRegistry ChannelNotifierRegistry
     * @param defaultLocale String
     */
    private void sendNotificationToNonRegisteredUsers(IgniteKey<?> key,
                                                      List<NonRegisteredUserData> recipients, IgniteEvent event,
                                                      String notificationId,
                                                      Set<ChannelType> channelsSupported,
                                                      ChannelNotifierRegistry channelNotifierRegistry,
                                                      String defaultLocale) {
        NotificationConfig notificationConfig = getNonRegisteredUserNotificationConfig();
        AlertsInfo basicAlertsInfo = getBasicAlertInfo(event, notificationConfig);
        AlertsHistoryInfo alertHistoryObj =
                alertsHistoryAssistant.setEnrichedAlertHistory(basicAlertsInfo, new AlertsHistoryInfo());
        alertHistoryObj.addStatus(READY);
        for (NonRegisteredUserData nonRegisteredUserData : recipients) {
            AlertsInfo alertsInfo = convertRecipientDataToAlertInfo(nonRegisteredUserData, event, notificationId);
            if (!(basicAlertsInfo.getAlertsData() != null
                    && basicAlertsInfo.getAlertsData().any().containsKey(FEEDBACK_TOPIC))
                    && alertsInfo.getAlertsData().any().containsKey(FEEDBACK_TOPIC)) {
                basicAlertsInfo.setAlertsData(alertsInfo.getAlertsData());
            }
            updateNotificationConfigChannelsInfo(notificationConfig, nonRegisteredUserData, defaultLocale);
            alertsInfo.setNotificationConfig(notificationConfig);
            alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));
            try {
                alertProcessorChainUnregisteredUsers.forEach(processor -> processor.process(alertsInfo));
            } catch (Exception e) {
                LOGGER.error("Processing alert for non registered users failed: {}", alertsInfo, e);
                alertHistoryObj.addStatus(FAILED);
                alertsHistoryAssistant.saveAlertHistory("", "", basicAlertsInfo, alertHistoryObj);
                setProcessingStatus(NotificationConstants.FAILURE, alertHistoryObj, e.getMessage());

            }
            LOGGER.debug("sendNotificationToNonRegisteredUsers alert info: {}", alertsInfo);

            getChannelInfoFromConfig(event, notificationId, channelsSupported, channelNotifierRegistry,
                    nonRegisteredUserData, alertsInfo, alertHistoryObj);

        }
        alertHistoryObj.addStatus(DONE);
        alertsHistoryAssistant.saveAlertHistory("", "", basicAlertsInfo, alertHistoryObj);
        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(basicAlertsInfo, key, event, alertHistoryObj);

    }

    /**
     * getChannelInfoFromConfig method.
     *
     * @param event IgniteEvent
     * @param notificationId String
     * @param channelsSupported Set
     * @param channelNotifierRegistry ChannelNotifierRegistry
     * @param nonRegisteredUserData NonRegisteredUserData
     * @param alertsInfo AlertsInfo
     * @param alertHistoryObj AlertsHistoryInfo
     */
    private void getChannelInfoFromConfig(IgniteEvent event, String notificationId,
                                          Set<ChannelType> channelsSupported,
                                          ChannelNotifierRegistry channelNotifierRegistry,
                                          NonRegisteredUserData nonRegisteredUserData, AlertsInfo alertsInfo,
                                          AlertsHistoryInfo alertHistoryObj) {
        for (Channel channel : alertsInfo.getNotificationConfig().getEnabledChannels()) {
            ChannelType channelType = channel.getChannelType();
            if (channelsSupported.contains(channelType)) {
                processSupportedChannels(event, notificationId, channelNotifierRegistry,
                        nonRegisteredUserData, alertsInfo, alertHistoryObj, channelType);
            } else {
                LOGGER.debug("{} channel is not Supported. Alert will not be sent", channelType);
                setSkippedChannelResponse(channelType,
                        alertHistoryObj, "channel is not Supported. Alert will not be sent");
            }
        }
    }

    /**
     * setSkippedChannelResponse method.
     *
     * @param channelType ChannelType
     * @param alertHistoryObj AlertsHistoryInfo
     * @param failureReason String
     */
    private void setSkippedChannelResponse(
            ChannelType channelType, AlertsHistoryInfo alertHistoryObj, String failureReason) {

        Map<String, String> skippedChannels = alertHistoryObj.getSkippedChannels();
        skippedChannels.put(channelType.getChannelType(), failureReason);
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
     * processSupportedChannels method.
     *
     * @param event IgniteEvent
     * @param notificationId String
     * @param channelNotifierRegistry ChannelNotifierRegistry
     * @param nonRegisteredUserData NonRegisteredUserData
     * @param alertsInfo AlertsInfo
     * @param alertHistoryObj AlertsHistoryInfo
     * @param channelType ChannelType
     */
    private void processSupportedChannels(IgniteEvent event, String notificationId,
                                          ChannelNotifierRegistry channelNotifierRegistry,
                                          NonRegisteredUserData nonRegisteredUserData, AlertsInfo alertsInfo,
                                          AlertsHistoryInfo alertHistoryObj, ChannelType channelType) {
        if (channelType.equals(ChannelType.PORTAL)
                &&
                StringUtils.isNotEmpty(nonRegisteredUserData.getPortal())) {
            String mqttTopic = nonRegisteredUserData.getPortal();
            LOGGER.info("mqtt topic from api: {}", mqttTopic);
            alertsInfo.getAlertsData().setMqttTopic(mqttTopic);
        }

        Data data = alertsInfo.getAlertsData();

        String region = "";
        if (data.getVehicleProfile() != null) {
            region = data.getVehicleProfile().getSoldRegion() != null
                    ?
                    data.getVehicleProfile().getSoldRegion() : "";
        } else {
            LOGGER.info("Vehicle profile is not present in data ");
        }
        ChannelNotifier notifier =
                channelNotifierRegistry.channelNotifier(channelType, notificationId, region);
        try {
            VehicleInfoNotification.sendAlert(alertHistoryObj, notifier, alertsInfo, event);
        } catch (Exception e) {
            LOGGER.error(
                    "Exception occurred while publishing "
                            +
                            "alerts {} to channel type {}. Continuing publishing to other channels",
                    alertsInfo, channelType, e);
            setSkippedChannelResponse(channelType, alertHistoryObj, e.getMessage());
        }
    }

    /**
     * getBasicAlertInfo method.
     *
     * @param value IgniteEvent
     * @param notificationConfig NotificationConfig
     * @return AlertsInfo
     */
    private AlertsInfo getBasicAlertInfo(IgniteEvent value, NotificationConfig notificationConfig) {
        AlertsInfo basicAlertsInfo = new AlertsInfo();
        basicAlertsInfo.setIgniteEvent(value);
        basicAlertsInfo.setEventID(value.getEventId());
        basicAlertsInfo.setNotificationConfig(notificationConfig);
        basicAlertsInfo.setTimestamp(value.getTimestamp());
        return basicAlertsInfo;
    }

    /**
     * updateNotificationConfigChannelsInfo method.
     *
     * @param notificationConfig NotificationConfig
     * @param nonRegisteredUserData NonRegisteredUserData
     * @param defaultLocale String
     */
    private void updateNotificationConfigChannelsInfo(NotificationConfig notificationConfig,
                                                      NonRegisteredUserData nonRegisteredUserData,
                                                      String defaultLocale) {
        notificationConfig.setLocale(StringUtils.isNotEmpty(nonRegisteredUserData.getLocale())
                ? Locale.forLanguageTag(nonRegisteredUserData.getLocale().replace('_', '-')).toLanguageTag()
                : defaultLocale);
        notificationConfig.getChannel(ChannelType.EMAIL)
                .setEnabled(!StringUtils.isEmpty(nonRegisteredUserData.getEmail()));
        if (!StringUtils.isEmpty(nonRegisteredUserData.getEmail())) {
            ((EmailChannel) notificationConfig.getChannel(ChannelType.EMAIL))
                    .setEmails(Collections.singletonList(nonRegisteredUserData.getEmail()));
        }

        notificationConfig.getChannel(ChannelType.SMS).setEnabled(!StringUtils.isEmpty(nonRegisteredUserData.getSms()));
        if (!StringUtils.isEmpty(nonRegisteredUserData.getSms())) {
            ((SmsChannel) notificationConfig.getChannel(ChannelType.SMS))
                    .setPhones(Collections.singletonList(nonRegisteredUserData.getSms()));
        }

        notificationConfig.getChannel(ChannelType.MOBILE_APP_PUSH).setEnabled(
                nonRegisteredUserData.getPush() != null
                        && !StringUtils.isEmpty(nonRegisteredUserData.getPush().get(NON_REGISTERED_FIELD_PUSH_TOKEN)));

        notificationConfig.getChannel(ChannelType.PORTAL)
                .setEnabled(!StringUtils.isEmpty(nonRegisteredUserData.getPortal()));
        if (!StringUtils.isEmpty(nonRegisteredUserData.getPortal())) {
            ((PortalChannel) notificationConfig.getChannel(ChannelType.PORTAL))
                    .setMqttTopics(Collections.singletonList(nonRegisteredUserData.getPortal()));
        }
    }

    /**
     * getNonRegisteredUserNotificationConfig method.
     *
     * @return NotificationConfig
     */
    private NotificationConfig getNonRegisteredUserNotificationConfig() {
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        PortalChannel portalChannel = new PortalChannel();
        portalChannel.setEnabled(true);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setChannels(Arrays.asList(emailChannel, pushChannel, smsChannel, portalChannel));
        notificationConfig.setGroup(NON_REGISTERED_USERS_GROUP);
        return notificationConfig;
    }

    /**
     * convertRecipientDataToAlertInfo method.
     *
     * @param nonRegisteredUserData NonRegisteredUserData
     * @param value IgniteEvent
     * @param notificationId String
     * @return AlertsInfo
     */
    private AlertsInfo convertRecipientDataToAlertInfo(NonRegisteredUserData nonRegisteredUserData, IgniteEvent value,
                                                       String notificationId) {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(getIgniteEventForRecipient(value, nonRegisteredUserData));
        alertsInfo.setEventID(value.getEventId());
        AlertsInfo.Data alertData = new AlertsInfo.Data();
        alertData.setNotificationId(notificationId);
        alertsInfo.setTimestamp(value.getTimestamp());
        UserProfile userProfile = new UserProfile();
        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        Map<String, Object> vehicleAttributes = new HashMap<>();

        if (nonRegisteredUserData.getData() != null) {
            if (isPushDataAvlbl(nonRegisteredUserData)) {
                nonRegisteredUserData.getData().putAll(nonRegisteredUserData.getPush());
            }

            Map<String, Object> dataProperties = nonRegisteredUserData.getData();
            if (dataProperties.get("userProfile") != null) {
                userProfile = objectMapper.convertValue(dataProperties.get("userProfile"), UserProfile.class);
            }
            if (!StringUtils.isEmpty(nonRegisteredUserData.getLocale())) {
                userProfile.setLocale(
                        Locale.forLanguageTag(nonRegisteredUserData.getLocale().toLowerCase().replace("_", "-")));
            }

            if (dataProperties.get(VEHICLE_PROFILE) != null) {
                vehicleAttributes = getVehAttr(dataProperties, vehicleProfile);
            }

            alertData.setAlertDataProperties(dataProperties);
        }
        // Without Data block in the request correct brand should be picked
        if (!StringUtils.isEmpty(nonRegisteredUserData.getBrand())) {
            vehicleAttributes.put("make", nonRegisteredUserData.getBrand());
        }
        vehicleProfile.setVehicleAttributes(vehicleAttributes);
        userProfile.setDefaultPhoneNumber(nonRegisteredUserData.getSms());
        userProfile.setDefaultEmail(nonRegisteredUserData.getEmail());
        alertData.setUserProfile(userProfile);
        alertData.setVehicleProfile(vehicleProfile);
        alertsInfo.setAlertsData(alertData);
        alertsInfo.setTimestamp(value.getTimestamp());
        return alertsInfo;
    }

    /**
     * isPushDataAvlbl method.
     *
     * @param nonRegisteredUserData NonRegisteredUserData
     * @return boolean
     */
    private boolean isPushDataAvlbl(NonRegisteredUserData nonRegisteredUserData) {
        return nonRegisteredUserData.getPush() != null
                &&
                !StringUtils.isEmpty(nonRegisteredUserData.getPush().get(NON_REGISTERED_FIELD_PUSH_TOKEN))
                &&
                !StringUtils.isEmpty(nonRegisteredUserData.getPush().get(NON_REGISTERED_FIELD_PUSH_CLIENT));
    }

    /**
     * getVehAttr method.
     *
     * @param dataProperties Map
     * @param vehicleProfile VehicleProfileAbridged
     * @return Map
     */
    @NotNull
    private Map<String, Object> getVehAttr(Map<String, Object> dataProperties, VehicleProfileAbridged vehicleProfile) {
        Map<String, Object> vehicleAttributes;
        vehicleAttributes = objectMapper.convertValue(dataProperties.get(VEHICLE_PROFILE), HashMap.class);
        if (vehicleAttributes.containsKey(VEHICLE_ID) || vehicleAttributes.containsKey("vin")) {
            vehicleProfile.setVehicleId(
                    vehicleAttributes.get(VEHICLE_ID) != null ? (String) vehicleAttributes.get(VEHICLE_ID)
                            : (String) vehicleAttributes.get("vin"));
        }
        Map<String, Object> dataAttrs = new HashMap<>();
        dataAttrs.put("vehicleAttributes", dataProperties.get(VEHICLE_PROFILE));
        dataProperties.put(VEHICLE_PROFILE, dataAttrs);
        return vehicleAttributes;
    }

    /**
     * getIgniteEventForRecipient method.
     *
     * @param value IgniteEvent
     * @param nonRegisteredUserData NonRegisteredUserData
     * @return IgniteEvent
     */
    private IgniteEvent getIgniteEventForRecipient(IgniteEvent value, NonRegisteredUserData nonRegisteredUserData) {
        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setEventId(value.getEventId());
        igniteEventImpl.setVersion(value.getVersion());
        igniteEventImpl.setTimestamp(value.getTimestamp());
        igniteEventImpl.setTimezone(value.getTimezone());
        igniteEventImpl.setRequestId(value.getRequestId());
        igniteEventImpl.setBizTransactionId(value.getBizTransactionId());

        GenericEventData genericEventData = new GenericEventData();
        if (nonRegisteredUserData.getData() != null) {
            for (Entry<String, Object> entry : nonRegisteredUserData.getData().entrySet()) {
                genericEventData.set(entry.getKey(), entry.getValue());
            }
        }

        igniteEventImpl.setEventData(genericEventData);
        return igniteEventImpl;
    }
}
