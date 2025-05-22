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

package org.eclipse.ecsp.notification.aws.sns;

import com.amazonaws.ResponseMetadata;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SetSMSAttributesRequest;
import com.codahale.metrics.MetricRegistry;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.ecsp.domain.notification.AlertEventData;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonSNSChannelResponse;
import org.eclipse.ecsp.domain.notification.AmazonSNSSMSResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.AwsUtils;
import org.eclipse.ecsp.notification.adaptor.AbstractChannelNotifier;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.SMSTemplate;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;

/**
 * AmazonSmsNotifier class.
 */
@Component
@ConditionalOnProperty(name = "sms.default.sp", havingValue = "SMS:AWS_SNS")
public class AmazonSmsNotifier extends AbstractChannelNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSmsNotifier.class);
    private static final String PROTOCOL = ChannelType.SMS.getProtocol();
    private static final String SVC_PROVIDER = "SMS:AWS_SNS";
    private static boolean isSMSAttributeSet = false;
    private AmazonSNSClient snsClient;
    @Value("${sender.id.default:NOTICE}")
    private String defaultSender;
    @Value("${aws.cross.account.enabled: false}")
    private boolean isCrossAccountEnabled;
    @Value("${aws.cross.account.arn.role}")
    private String crossArnRole;
    @Value("${aws.cross.account.session.name}")
    private String crossRoleSessionName;
    @Value("${aws.cross.account.token.expiry.sec: 3600}")
    private String durationInSec;
    private STSAssumeRoleSessionCredentialsProvider stsAssumeRoleSessionCredentialsProvider;

    /**
     * Constructor for AmazonSmsNotifier.
     */
    public AmazonSmsNotifier() {
    }

    private static void setSmsAttribute(boolean smsAtt) {
        isSMSAttributeSet = smsAtt;
    }

    /**
     * Incase of cross account, temporary credentials are fetched from assumed
     * role in different aws account.
     *
     * @return loadCredentials
     */
    private AWSCredentialsProvider loadCredentials() {
        AWSCredentialsProviderChain credProviderChain = new AWSCredentialsProviderChain(
            AwsUtils.getCredentialsProviderList(getProperties()));
        if (isCrossAccountEnabled) {
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceAsyncClientBuilder.standard()
                    .withCredentials(credProviderChain)
                    .build();
            STSAssumeRoleSessionCredentialsProvider.Builder builder =
                    new STSAssumeRoleSessionCredentialsProvider.Builder(crossArnRole,
                            crossRoleSessionName).withRoleSessionDurationSeconds(Integer.parseInt(durationInSec))
                            .withStsClient(stsClient);
            stsAssumeRoleSessionCredentialsProvider = builder.build();
            LOGGER.info("Initialised cross account credentials provider {} for AmazonSmsNotifier",
                    stsAssumeRoleSessionCredentialsProvider);
            return stsAssumeRoleSessionCredentialsProvider;
        }
        return credProviderChain;
    }

    /**
     * shutdown sns client.
     */
    @PreDestroy
    public void shutdown() {
        if (ObjectUtils.isNotEmpty(snsClient)) {
            snsClient.shutdown();
        }
        if (ObjectUtils.isNotEmpty(stsAssumeRoleSessionCredentialsProvider)) {
            stsAssumeRoleSessionCredentialsProvider.close();
        }
    }

    /**
     * init method.
     *
     * @param properties        Properties
     * @param metricRegistry    MetricRegistry
     * @param notificationDao   NotificationDao
     */
    @Override
    public void init(Properties properties, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        super.init(properties, metricRegistry, notificationDao);
        boolean isRunningOnLambda = Boolean
                .parseBoolean(properties.getProperty(NotificationProperty.AWS_LAMBDA_EXECUTION_PROP, "false"));
        if (!isRunningOnLambda) {
            snsClient = new AmazonSNSClient(loadCredentials());
        } else {
            /*
             * When running on lambda, we dont have to give any credentials, it
             * will automatically use the lambda role
             */
            snsClient = new AmazonSNSClient();
        }
        String endPoint = properties.getProperty(NotificationProperty.AWS_SNS_ENDPOINT_NAME,
                "https://sns.us-east-1.amazonaws.com");
        snsClient.setEndpoint(endPoint);
        synchronized (AmazonSmsNotifier.class) {
            if (!isSMSAttributeSet) {
                setDefaultSmsAttributes();
                setSmsAttribute(true);
            }
        }
    }

    /**
     * setting the default sms attribute type always to transactional.
     */
    private void setDefaultSmsAttributes() {
        SetSMSAttributesRequest setRequest = new SetSMSAttributesRequest().addAttributesEntry("DefaultSMSType",
                "Transactional");
        snsClient.setSMSAttributes(setRequest);
        LOGGER.debug("Response for setting the default sms attribute to transactions:{}",
                snsClient.getCachedResponseMetadata(setRequest));
    }

    /**
     * setupChannel method.
     *
     * @param notificationConfig NotificationConfig
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
        return null;
    }

    /**
     * destroyChannel method.
     *
     * @param userId    String
     * @param eventData String
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse destroyChannel(String userId, String eventData) {
        return null;
    }

    /**
     * getProtocol method.
     *
     * @return String
     */
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * Method to publish message to appropriate service.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {
        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        AmazonSNSSMSResponse response = new AmazonSNSSMSResponse(userId, alert.getPdid());
        if (!validateUserId(response, userId, alert)) {
            return response;
        }
        SMSTemplate template = new SMSTemplate();
        SmsChannel smsChannel = alert.getNotificationConfig().getChannel(ChannelType.SMS);
        List<String> phoneList = smsChannel.getPhones();
        if (CollectionUtils.isEmpty(phoneList)) {
            LOGGER.error(
                    "Failed sending notification to secondary contact for "
                            +
                            "userId {} and vehicleId {} , phone number is missing",
                    userId, alert.getNotificationConfig().getVehicleId());
            response.setStatus(NotificationConstants.NOTIFICATION_STATUS_MISSING_DESTINATION);
            return response;
        }

        String destinationsString = phoneList.stream().map(Object::toString).collect(Collectors.joining(","));
        response.setDestination(destinationsString);

        try {
            String alertsMsg = alert.getNotificationTemplate().getChannelTemplates().getSms().getBody();
            AlertEventData alertEventData = getDefaultAlertData(alertsMsg);
            LOGGER.debug("msg body: {}", alertEventData.getDefaultMessage());
            response.setAlertData(alertEventData);
            template.setBody(alertsMsg);
            response.setTemplate(template);
            if (StringUtils.isEmpty(userId)) {
                publishToNonRegisteredUser(alert, response);
            } else {

                publishToMobile(alert, response, phoneList);

            }
            response.setStatus(NOTIFICATION_STATUS_SUCCESS);
        } catch (Exception e) {
            LOGGER.error("Failed sending notification via AmazonSNSSMSNotifier", e);
            response.setStatus(NOTIFICATION_STATUS_FAILURE);
        }
        LOGGER.info("Published sms message to user {} with response status {}", userId, response.getStatus());
        return response;
    }

    /**
     * Method to publish message to appropriate service.
     *
     * @param response AmazonSNSChannelResponse
     * @param info    AlertsInfo
     * @param alertMsg String
     * @param senderId String
     */
    protected void doPublish(AmazonSNSChannelResponse response, AlertsInfo info, String alertMsg, String senderId) {
        LOGGER.info("Publishing the message {} to the user {} ", alertMsg,
                info.getAlertsData().getUserProfile().getUserId());
        PublishRequest request = new PublishRequest()
                .withPhoneNumber(info.getAlertsData().getUserProfile().getDefaultPhoneNumber()).withMessage(alertMsg);
        if (StringUtils.isNotEmpty(senderId)) {
            request.setMessageAttributes(getSmsAttributes(senderId));
        }
        snsClient.publish(request);
        ResponseMetadata metaData = snsClient.getCachedResponseMetadata(request);
        LOGGER.info("Response ID: {}", metaData);
        response.setPublishInfo("", metaData.getRequestId());
        // publish info
        // without topic
    }

    /**
     * Method to publish message to a specific Mobile number.
     *
     * @param alert AlertsInfo
     * @param response AmazonSNSSMSResponse
     * @param phoneList List
     */
    private void publishToMobile(AlertsInfo alert, AmazonSNSSMSResponse response, List<String> phoneList) {


        SMSTemplate smsTemplate = alert.getNotificationTemplate().getChannelTemplate(ChannelType.SMS);
        String senderId = smsTemplate.getSender();
        senderId = StringUtils.isNotEmpty(senderId) ? senderId : defaultSender;
        doPublishSmstoMobile(response, alert, response.getAlertData().getDefaultMessage(), senderId, phoneList);
        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        String contactId = alert.getNotificationConfig() != null ? alert.getNotificationConfig().getContactId() : null;

        LOGGER.info("Sender ID {} ", senderId);

        LOGGER.info("Published publishToMobile message to user {} with contact ID {} response is {}", userId, contactId,
                response);
        response.setStatus(NOTIFICATION_STATUS_SUCCESS);
    }

    /**
     * Method to publish message to a specific Mobile number.
     *
     * @param response AmazonSNSSMSResponse
     * @param info    AlertsInfo
     * @param alertMsg String
     * @param senderId String
     * @param phoneList List
     */
    protected void doPublishSmstoMobile(AmazonSNSChannelResponse response, AlertsInfo info, String alertMsg,
                                        String senderId, List<String> phoneList) {
        String userId =
                info.getAlertsData().getUserProfile() != null
                        ? info.getAlertsData().getUserProfile().getUserId() : null;
        String contactId = info.getNotificationConfig() != null ? info.getNotificationConfig().getContactId() : null;
        phoneList.stream().forEach(ph -> {
            LOGGER.info("Publishing the message {} to user {} having contactId {} ", alertMsg, userId, contactId);

            PublishRequest request = new PublishRequest().withPhoneNumber(ph).withMessage(alertMsg);
            if (StringUtils.isNotEmpty(senderId)) {
                request.setMessageAttributes(getSmsAttributes(senderId));
            }
            request.setSubject("HAA-Alerts");
            snsClient.publish(request);
            ResponseMetadata metaData = snsClient.getCachedResponseMetadata(request);
            LOGGER.info("Response ID: {}", metaData);
            response.setPublishInfo("", metaData.getRequestId());
            // publish
            // info
            // without
            // topic

        });

    }

    /**
     * Method to publish message to a non registered user.
     *
     * @param alert AlertsInfo
     * @param response AmazonSNSSMSResponse
     */
    private void publishToNonRegisteredUser(AlertsInfo alert, AmazonSNSSMSResponse response) {
        SMSTemplate smsTemplate = alert.getNotificationTemplate().getChannelTemplate(ChannelType.SMS);
        String senderId = smsTemplate.getSender();
        senderId = StringUtils.isNotEmpty(senderId) ? senderId : defaultSender;
        doPublish(response, alert, response.getAlertData().getDefaultMessage(), senderId);
        LOGGER.info("Published message to {} response is {}", alert.getAlertsData().getUserProfile().getUserId(),
                response);
        response.setStatus(NOTIFICATION_STATUS_SUCCESS);
    }


    /**
     * getSmsAttributes method.
     *
     * @param senderId String
     * @return Map
     */
    public Map<String, MessageAttributeValue> getSmsAttributes(String senderId) {
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue().withStringValue(senderId) // The
                // sender
                // ID
                // shown
                // on
                // the
                // device.
                .withDataType("String"));
        return smsAttributes;
    }

    /**
     * Get service provider name.
     *
     * @return Service provider name
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }

}
