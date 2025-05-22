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

package org.eclipse.ecsp.notification.aws.pinpoint;


import com.amazonaws.services.pinpoint.model.DirectMessageConfiguration;
import com.amazonaws.services.pinpoint.model.EndpointSendConfiguration;
import com.amazonaws.services.pinpoint.model.MessageRequest;
import com.amazonaws.services.pinpoint.model.SMSMessage;
import com.amazonaws.services.pinpoint.model.SendMessagesRequest;
import com.amazonaws.services.pinpoint.model.SendMessagesResult;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.ecsp.domain.notification.AlertEventData;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonPinpointSMSChannelResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.entities.SMSTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.TRANSACTIONAL_MESSAGE_TYPE;


/**
 * Amazon Pinpoint SMS Service.
 *
 * @author MaKumari
 */
@Component
public class AmazonPinpointSmsNotifier extends AmazonPinpointNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonPinpointSmsNotifier.class);
    private static final String PROTOCOL = ChannelType.SMS.getProtocol();
    private static final String SVC_PROVIDER = "SMS:AWS_PINPOINT";

    @Value("${sender.id.default:NOTICE}")
    private String defaultSender;

    @Value("${aws.pinpoint.application.id}")
    private String appId;

    /**
     * Instantiates a new Amazon pinpoint sms notifier.
     */
    public AmazonPinpointSmsNotifier() {
    }

    /**
     * Gets service provider name.
     *
     * @return the service provider name
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }

    /**
     * Setup channel.
     *
     * @param notificationConfig Notification configuration
     * @return Channel response
     */
    @Override
    public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
        if (ObjectUtils.isNotEmpty(notificationConfig)) {
            updateEndpoints(notificationConfig, com.amazonaws.services.pinpoint.model.ChannelType.SMS, null);
            LOGGER.info(
                    "SMS setup channel called for userId: {} vehicleId: {} contactId: {} group: {}",
                    notificationConfig.getUserId(),
                    notificationConfig.getVehicleId(), notificationConfig.getContactId(),
                    notificationConfig.getGroup());
        }
        return null;
    }


    /**
     * Get protocol.
     *
     * @return Protocol
     */
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * Destroy channel.
     *
     * @param userId    User id
     * @param eventData Event data
     * @return Channel response
     */
    @Override
    public ChannelResponse destroyChannel(String userId, String eventData) {
        return null;
    }

    /**
     * Publish notification.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {
        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        AmazonPinpointSMSChannelResponse smsResponse = new AmazonPinpointSMSChannelResponse(userId, alert.getPdid());
        if (!validateUserId(smsResponse, userId, alert)) {
            return smsResponse;
        }
        SMSTemplate smsTemplate = new SMSTemplate();
        SmsChannel smsChannel = alert.getNotificationConfig().getChannel(ChannelType.SMS);
        List<String> phoneList = smsChannel.getPhones();
        if (CollectionUtils.isEmpty(phoneList)) {
            LOGGER.error(
                    "Failed sending notification to secondary "
                            +
                            "contact for userId {} and vehicleId {} , phone number is missing",
                    userId, alert.getNotificationConfig().getVehicleId());
            smsResponse.setStatus(NotificationConstants.NOTIFICATION_STATUS_MISSING_DESTINATION);
            return smsResponse;
        }
        String destinationsString = phoneList.stream().map(Object::toString).collect(Collectors.joining(","));
        smsResponse.setDestination(destinationsString);
        try {
            String alertsMsg = alert.getNotificationTemplate().getChannelTemplates().getSms().getBody();
            AlertEventData alertEventData = getDefaultAlertData(alertsMsg);
            LOGGER.debug("msg body: {}", alertEventData.getDefaultMessage());
            smsResponse.setAlertData(alertEventData);
            smsTemplate.setBody(alertsMsg);
            smsResponse.setTemplate(smsTemplate);
            if (StringUtils.isEmpty(userId)) {
                publishToNonRegisteredUser(alert, smsResponse);
            } else {
                publishToMobile(alert, smsResponse, phoneList);
            }
            smsResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
        } catch (Exception e) {
            LOGGER.error("Failed sending notification via AmazonPinpointSmsNotifier", e);
            smsResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
        }
        LOGGER.info("Published sms message to user {} with response status {}", userId, smsResponse.getStatus());

        return smsResponse;
    }

    /**
     * Publish pinpoint message to phone numbers of user.
     *
     * @param info     AlertsInfo
     * @param alertMsg Alert message
     * @param senderId Sender ID
     * @param phNumberList Phone number list
     * @return SendMessagesResult
     */
    protected SendMessagesResult doPublish(AlertsInfo info, String alertMsg, String senderId,
                                           List<String> phNumberList) {
        LOGGER.info("Publishing pinpoint message {} to phone numbers of user {}", alertMsg,
                info.getAlertsData().getUserProfile().getUserId());
        SMSMessage smsMessage = new SMSMessage().withBody(alertMsg).withMessageType(TRANSACTIONAL_MESSAGE_TYPE)
                .withSenderId(StringUtils.isEmpty(senderId) ? defaultSender : senderId);
        DirectMessageConfiguration directMsgConfig = new DirectMessageConfiguration().withSMSMessage(smsMessage);
        Map<String, String> endPointMap = updateEndpoints(info.getNotificationConfig(),
                com.amazonaws.services.pinpoint.model.ChannelType.SMS, phNumberList);
        Map<String, EndpointSendConfiguration> endpointsConfig = new HashMap<>();
        for (String phNumber : phNumberList) {
            if (endPointMap.containsKey(phNumber)) {
                endpointsConfig.put(endPointMap.get(phNumber), new EndpointSendConfiguration());
            }
        }
        MessageRequest msgReq =
                new MessageRequest().withMessageConfiguration(directMsgConfig).withEndpoints(endpointsConfig);
        SendMessagesRequest smsPinpointRequest =
                new SendMessagesRequest().withApplicationId(appId).withMessageRequest(msgReq);
        SendMessagesResult smsPinpointResponse = sendMessage(smsPinpointRequest);
        return smsPinpointResponse;
    }

    /**
     * Publish pinpoint message to mobile.
     *
     * @param alert AlertsInfo
     * @param response AmazonPinpointSMSChannelResponse
     * @param phoneList Phone number list
     */
    private void publishToMobile(AlertsInfo alert, AmazonPinpointSMSChannelResponse response, List<String> phoneList) {

        SMSTemplate smsTemplate = alert.getNotificationTemplate().getChannelTemplate(ChannelType.SMS);
        String senderId = smsTemplate.getSender();
        senderId = StringUtils.isNotEmpty(senderId) ? senderId : defaultSender;
        SendMessagesResult sendMessagesResult =
                doPublish(alert, response.getAlertData().getDefaultMessage(), senderId, phoneList);
        response.setDeliveryStatus(getDeliveryStatus(sendMessagesResult.getMessageResponse().getEndpointResult()));
        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        String contactId = alert.getNotificationConfig() != null ? alert.getNotificationConfig().getContactId() : null;
        LOGGER.info("Sender ID {} ", senderId);
        LOGGER.info("Published publishToMobile pinpoint message to user {} with contact ID {} response is {}", userId,
                contactId, response);
        response.setStatus(NOTIFICATION_STATUS_SUCCESS);
    }

    /**
     * Publish pinpoint message to non registered user.
     *
     * @param alert AlertsInfo
     * @param response AmazonPinpointSMSChannelResponse
     */
    private void publishToNonRegisteredUser(AlertsInfo alert, AmazonPinpointSMSChannelResponse response) {
        SMSTemplate smsTemplate = alert.getNotificationTemplate().getChannelTemplate(ChannelType.SMS);
        String senderId = smsTemplate.getSender();
        senderId = StringUtils.isNotEmpty(senderId) ? senderId : defaultSender;
        SendMessagesResult sendMessagesResult = doPublish(alert, response.getAlertData().getDefaultMessage(), senderId,
                Arrays.asList(alert.getAlertsData().getUserProfile().getDefaultPhoneNumber()));
        response.setDeliveryStatus(getDeliveryStatus(sendMessagesResult.getMessageResponse().getEndpointResult()));
        LOGGER.info("Published pinpoint message to {} response is {}",
                alert.getAlertsData().getUserProfile().getUserId(), response);
        response.setStatus(NOTIFICATION_STATUS_SUCCESS);
    }



}
