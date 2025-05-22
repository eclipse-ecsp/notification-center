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
import com.amazonaws.services.pinpoint.model.EmailMessage;
import com.amazonaws.services.pinpoint.model.EndpointSendConfiguration;
import com.amazonaws.services.pinpoint.model.MessageRequest;
import com.amazonaws.services.pinpoint.model.RawEmail;
import com.amazonaws.services.pinpoint.model.SendMessagesRequest;
import com.amazonaws.services.pinpoint.model.SendMessagesResult;
import com.codahale.metrics.MetricRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonPinpointEmailChannelResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.aws.ses.AmazonSesBounceHandler;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.email.EmailPayloadGenerator;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_MISSING_DESTINATION;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;

/**
 * Amazon Pinpoint Email Service.
 *
 * @author MaKumari
 *
 */
@Component
public class AmazonPinpointEmailNotifier extends AmazonPinpointNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonPinpointEmailNotifier.class);
    private static final String CHANNEL_TYPE_NAME =
            org.eclipse.ecsp.notification.adaptor.NotificationEventFields.ChannelType.EMAIL
                    .getChannelTypeName();
    private static final String SVC_PROVIDER = "EMAIL:AWS_PINPOINT";
    private boolean enableSesBounceHandler;
    private Session session;
    private AmazonSesBounceHandler bounceHandler;

    @Autowired
    private EmailPayloadGenerator emailPayloadGen;

    @Value("${aws.pinpoint.application.id}")
    private String appId;

    /**
     * Get protocol.
     *
     * @return String
     */
    @Override
    public String getProtocol() {
        return CHANNEL_TYPE_NAME;
    }

    /**
     * Get service provider name.
     *
     * @return String
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }

    /**
     * Initialize Amazon Pinpoint Email Notifier.
     *
     * @param properties       Properties
     * @param metricRegistry   MetricRegistry
     * @param notificationDao  NotificationDao
     */
    @Override
    public void init(Properties properties, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        super.init(properties, metricRegistry, notificationDao);
        session = Session.getDefaultInstance(properties);
        enableSesBounceHandler =
                Boolean.parseBoolean(properties.getProperty(NotificationProperty.AWS_SES_BOUCE_HANDLER_ENABLE, "true"));
        if (enableSesBounceHandler) {
            bounceHandler = AmazonSesBounceHandler.getInstance(properties);
        }
    }

    /**
     * Set up channel.
     *
     * @param notificationConfig Channel configuration
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
        if (ObjectUtils.isNotEmpty(notificationConfig)) {
            EmailChannel emailChannel = notificationConfig.getChannel(ChannelType.EMAIL);
            if (ObjectUtils.isNotEmpty(emailChannel) && emailChannel.getEnabled()
                    &&
                    !CollectionUtils.isEmpty(emailChannel.getEmails())) {
                List<String> emailList =
                        emailChannel.getEmails().stream().filter(StringUtils::isNotEmpty).toList();
                checkIfEmailAddressBounced(notificationConfig.getUserId(), emailList);
                updateEndpoints(notificationConfig, com.amazonaws.services.pinpoint.model.ChannelType.EMAIL, emailList);
                LOGGER.info(
                        "Email setup channel called for userId: {} vehicleId: {} contactId: {} group: {}",
                        notificationConfig.getUserId(),
                        notificationConfig.getVehicleId(), notificationConfig.getContactId(),
                        notificationConfig.getGroup());
            }
        }
        return null;
    }

    /**
     * Destroy channel.
     *
     * @param userId    User id
     * @param eventData Event data
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse destroyChannel(String userId, String eventData) {
        return null;
    }

    /**
     * Sends Email(s) notification.
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {

        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        AmazonPinpointEmailChannelResponse pinpointEmailResponse = new AmazonPinpointEmailChannelResponse(userId);
        if (!validateUserId(pinpointEmailResponse, userId, alert)) {
            return pinpointEmailResponse;
        }

        EmailChannel emailChannel = alert.getNotificationConfig().getChannel(ChannelType.EMAIL);
        List<String> emailList = emailChannel.getEmails();
        if (CollectionUtils.isEmpty(emailList)) {
            return getAmazonPinpointEmailChannelFailureResponse(alert, userId, pinpointEmailResponse);
        }

        String destinationsString = emailList.stream().map(Object::toString).collect(Collectors.joining(","));
        pinpointEmailResponse.setDestination(destinationsString);
        String alertMsg = alert.getNotificationTemplate().getChannelTemplates().getEmail().getBody();
        pinpointEmailResponse.setAlertData(getDefaultAlertData(alertMsg));

        if (!CollectionUtils.isEmpty(emailList) && !emailList.stream().allMatch(StringUtils::isEmpty)) {
            try {
                // validates email address(es)
                emailList.removeIf(StringUtils::isEmpty);
                checkIfEmailAddressBounced(userId, emailList);

                // set email message payload ,from address and subject.
                MimeMessage message = new MimeMessage(session);
                String content = alert.getNotificationTemplate().getChannelTemplates().getEmail().getBody();
                Multipart multipartParent = getMultipart(alert, content);
                message.setContent(multipartParent);
                String alertSubject = alert.getNotificationTemplate().getChannelTemplates().getEmail().getSubject();
                message.setSubject(alertSubject, "utf-8");
                message.setFrom(alert.getNotificationTemplate().getChannelTemplates().getEmail().getFrom());
                LOGGER.debug("Finished settings email message");

                // send email
                SendMessagesResult pinpointEmailResult = sendRawEmail(alert, emailList, message);
                pinpointEmailResponse
                        .setDeliveryStatus(
                                getDeliveryStatus(pinpointEmailResult.getMessageResponse().getEndpointResult()));

                // set channel response
                pinpointEmailResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
                EmailTemplate template = new EmailTemplate();
                template.setBody(content);
                pinpointEmailResponse.setTemplate(template);
            } catch (RuntimeException e) {
                LOGGER.error("Failed sending notification "
                        +
                        "via AmazonPinpointEmailNotifier {}", e.getMessage());
                pinpointEmailResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
            } catch (Exception e) {
                LOGGER.error("Failed sending notification via AmazonPinpointEmailNotifier {}", e.getMessage());
                pinpointEmailResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
            }
        } else {
            pinpointEmailResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
        }
        LOGGER.info("Send email action for event: {}, pdid: {} done with response: {} and status: {}",
                alert.getEventID(), alert.getPdid(),
                pinpointEmailResponse, pinpointEmailResponse.getStatus());

        return pinpointEmailResponse;
    }

    /**
     * Get Multipart.
     *
     * @param alert   AlertsInfo
     * @param content String
     * @return Multipart
     * @throws MessagingException Exception
     */
    private Multipart getMultipart(AlertsInfo alert, String content) throws MessagingException {
        List<EmailAttachment> attachmentLists = emailPayloadGen.getAttachmentsFromMessage(alert);
        List<EmailAttachment> inlineAttachments =
                attachmentLists.stream().filter(EmailAttachment::isInline).toList();
        List<EmailAttachment> attachments =
                attachmentLists.stream().filter(a -> !a.isInline()).toList();
        LOGGER.debug("Adding files to the HTML email by type: inline={}, attachment={}",
                inlineAttachments.size(),
                attachments.size());
        Multipart multipartParent = emailPayloadGen.build(content,
                alert.getNotificationTemplate().getChannelTemplates().getEmail().isRichContent(),
                inlineAttachments, attachments);
        return multipartParent;
    }

    /**
     * Get Amazon Pinpoint Email Channel Failure Response.
     *
     * @param alert AlertsInfo
     * @param userId String
     * @param pinpointEmailResponse AmazonPinpointEmailChannelResponse
     * @return AmazonPinpointEmailChannelResponse
     */
    @NotNull
    private AmazonPinpointEmailChannelResponse getAmazonPinpointEmailChannelFailureResponse(
            AlertsInfo alert, String userId, AmazonPinpointEmailChannelResponse pinpointEmailResponse) {
        LOGGER.error(
                "Failed sending notification to "
                        +
                        "secondary contact for userId {} and vehicleId {} , email address is missing",
                userId,
                alert.getNotificationConfig().getVehicleId());
        pinpointEmailResponse.setStatus(NOTIFICATION_STATUS_MISSING_DESTINATION);
        return pinpointEmailResponse;
    }


    /**
     * Sends pinpoint raw email(s).
     *
     * @param alert     AlertsInfo
     * @param emailList list of emails
     * @param message   MimeMessage
     * @return SendMessagesResult
     * @throws IOException        Exception
     * @throws MessagingException Exception
     */
    private SendMessagesResult sendRawEmail(AlertsInfo alert, List<String> emailList, MimeMessage message)
            throws IOException, MessagingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);
        RawEmail rawEmail = new RawEmail().withData(ByteBuffer.wrap(outputStream.toByteArray()));
        EmailMessage emailMessage = new EmailMessage().withRawEmail(rawEmail)
                .withFromAddress(alert.getNotificationTemplate().getChannelTemplates().getEmail().getFrom());
        DirectMessageConfiguration directMsgConfig = new DirectMessageConfiguration().withEmailMessage(emailMessage);
        Map<String, EndpointSendConfiguration> endpointsConfig = new HashMap<>();
        Map<String, String> endPointMap = updateEndpoints(alert.getNotificationConfig(),
                com.amazonaws.services.pinpoint.model.ChannelType.EMAIL, emailList);
        for (String email : emailList) {
            if (endPointMap.containsKey(email)) {
                endpointsConfig.put(endPointMap.get(email), new EndpointSendConfiguration());
            }
        }
        MessageRequest msgReq =
                new MessageRequest().withMessageConfiguration(directMsgConfig).withEndpoints(endpointsConfig);
        SendMessagesRequest emailPinpointRequest =
                new SendMessagesRequest().withApplicationId(appId).withMessageRequest(msgReq);
        SendMessagesResult emailPinpointResponse = sendMessage(emailPinpointRequest);
        return emailPinpointResponse;
    }

    /**
     * Verifies email address(es).
     *
     * @param userId String
     * @param emailList List of String
     */
    private void checkIfEmailAddressBounced(String userId, List<String> emailList) {
        if (enableSesBounceHandler) {
            Iterator<String> emailIterator = emailList.iterator();
            while (emailIterator.hasNext()) {
                boolean bouncedEmail = bounceHandler.bounced(emailIterator.next());
                if (bouncedEmail) {
                    emailIterator.remove();
                    LOGGER.error("Bounced email id for user {}. Email will not be sent.", userId);
                }
            }
        }
    }

}
