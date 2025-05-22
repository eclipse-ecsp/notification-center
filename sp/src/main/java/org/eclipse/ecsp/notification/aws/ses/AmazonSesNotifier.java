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

package org.eclipse.ecsp.notification.aws.ses;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;
import com.codahale.metrics.MetricRegistry;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonSESResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.utils.AwsUtils;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.email.EmailNotifier;
import org.eclipse.ecsp.notification.email.EmailPayloadGenerator;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_MISSING_DESTINATION;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;


/**
 * AmazonSesNotifier class.
 */
@Component
@ConditionalOnProperty(name = "email.default.sp", havingValue = "EMAIL:AWS_SES")
public class AmazonSesNotifier extends EmailNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(AmazonSesNotifier.class);
    private static final String SVC_PROVIDER = "EMAIL:AWS_SES";
    private AmazonSimpleEmailService sesService;
    private AmazonSesBounceHandler bounceHandler;
    private boolean enableSesBounceHandler;
    private Session session;
    @Autowired
    private EmailPayloadGenerator emailPayloadGen;
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
     * Incase of cross account, temporary credentials are fetched from assumed
     * role in different aws account.
     *
     * @return AWSCredentialsProvider
     */
    private AWSCredentialsProvider loadCredentials(String awsRegion) {
        AWSCredentialsProviderChain credProviderChain = new AWSCredentialsProviderChain(
                AwsUtils.getCredentialsProviderList(getProperties()));
        if (isCrossAccountEnabled) {
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceAsyncClientBuilder.standard()
                    .withCredentials(credProviderChain)
                    .withRegion(awsRegion)
                    .build();
            STSAssumeRoleSessionCredentialsProvider.Builder builder =
                    new STSAssumeRoleSessionCredentialsProvider.Builder(crossArnRole,
                            crossRoleSessionName).withRoleSessionDurationSeconds(Integer.parseInt(durationInSec))
                            .withStsClient(stsClient);
            stsAssumeRoleSessionCredentialsProvider = builder.build();
            LOG.info("Initialised cross account credentials provider {} for AmazonSesNotifier",
                    stsAssumeRoleSessionCredentialsProvider);
            return stsAssumeRoleSessionCredentialsProvider;
        }
        return credProviderChain;
    }

    /**
     * shutdown method.
     */
    @PreDestroy
    public void shutdown() {
        if (ObjectUtils.isNotEmpty(sesService)) {
            sesService.shutdown();
        }
        if (ObjectUtils.isNotEmpty(stsAssumeRoleSessionCredentialsProvider)) {
            stsAssumeRoleSessionCredentialsProvider.close();
        }
    }

    /**
     * Method to initialize the AmazonSesNotifier.
     *
     * @param properties       Properties
     * @param metricRegistry   MetricRegistry
     * @param notificationDao  NotificationDao
     */
    @Override
    public void init(Properties properties, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        super.init(properties, metricRegistry, notificationDao);
        session = Session.getDefaultInstance(properties);
        String awsRegion = properties.getProperty(NotificationProperty.AWS_REGION);
        String sesEndpoint = properties.getProperty(NotificationProperty.AWS_SES_ENDPOINT_NAME);
        if (StringUtils.isEmpty(awsRegion)) {
            throw new IllegalArgumentException("Property " + NotificationProperty.AWS_REGION + " not defined");
        }
        if (StringUtils.isEmpty(sesEndpoint)) {
            throw new IllegalArgumentException(
                    "Property " + NotificationProperty.AWS_SES_ENDPOINT_NAME + " not defined");
        }
        sesService = AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(loadCredentials(awsRegion))
                .withEndpointConfiguration(new EndpointConfiguration(sesEndpoint, awsRegion))
                .build();
        enableSesBounceHandler =
                Boolean.parseBoolean(properties.getProperty(NotificationProperty.AWS_SES_BOUCE_HANDLER_ENABLE, "true"));
        if (enableSesBounceHandler) {
            bounceHandler = AmazonSesBounceHandler.getInstance(properties);
        }
    }

    /**
     * Method to setup channel.
     *
     * @param notificationConfig Channel configuration
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
        // Nothing to do
        return null;
    }

    /**
     * Method to destroy channel.
     *
     * @param key       Key
     * @param eventData Event data
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse destroyChannel(String key, String eventData) {
        // Nothing to do
        return null;
    }

    /**
     * Method to publish message to appropriate service.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {

        // Get email list and remove bounced emails
        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        AmazonSESResponse sesResponse = new AmazonSESResponse(userId);
        if (!validateUserId(sesResponse, userId, alert)) {
            return sesResponse;
        }
        EmailChannel emailChannel = alert.getNotificationConfig().getChannel(ChannelType.EMAIL);
        List<String> emailList = emailChannel.getEmails();
        if (CollectionUtils.isEmpty(emailList)) {
            LOG.error(
                    "Failed sending notification to secondary contact for "
                            +
                            "userId {} and vehicleId {} , email address is missing",
                    userId,
                    alert.getNotificationConfig().getVehicleId());
            sesResponse.setStatus(NOTIFICATION_STATUS_MISSING_DESTINATION);
            return sesResponse;
        }
        String destinationsString = emailList.stream().map(Object::toString).collect(Collectors.joining(","));
        sesResponse.setDestination(destinationsString);

        // Set response that will be send in any case
        String alertMsg = alert.getNotificationTemplate().getChannelTemplates().getEmail().getBody();
        sesResponse.setAlertData(getDefaultAlertData(alertMsg));

        // Start sending-email flow
        if (!CollectionUtils.isEmpty(emailList) && !emailList.stream().allMatch(StringUtils::isEmpty)) {
            try {
                emailList.removeIf(StringUtils::isEmpty);
                checkIfEmailAddressBounced(userId, emailList);

                MimeMessage message = new MimeMessage(session);
                String content = alert.getNotificationTemplate().getChannelTemplates().getEmail().getBody();

                List<EmailAttachment> attachmentLists = emailPayloadGen.getAttachmentsFromMessage(alert);
                List<EmailAttachment> inlineAttachments =
                        attachmentLists.stream().filter(EmailAttachment::isInline).toList();
                List<EmailAttachment> attachments =
                        attachmentLists.stream().filter(a -> !a.isInline()).toList();
                LOG.debug("Adding files to the HTML email by type: inline={}, attachment={}", inlineAttachments.size(),
                        attachments.size());
                Multipart multipartParent = emailPayloadGen.build(content,
                        alert.getNotificationTemplate().getChannelTemplates().getEmail().isRichContent(),
                        inlineAttachments, attachments);

                message.setContent(multipartParent);

                // Set email subject
                setEmailSubject(alert, message);

                LOG.debug("Finished settings email message");

                SendRawEmailResult sesResult = sendRawEmail(alert, emailList, message);
                // Adds more info to response after successful email sending
                enrichResponse(sesResponse, content, sesResult.getMessageId());
            } catch (RuntimeException e) {
                LOG.error("Failed sending notification via AmazonSesNotifier", e);
                sesResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
            } catch (Exception e) {
                LOG.error("Failed sending notification via AmazonSesNotifier", e);
                sesResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
            }
        } else {
            sesResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
        }
        LOG.info("Send email action for event: {}, pdid: {} done with response: {} and status: {}", alert.getEventID(),
                alert.getPdid(),
                sesResponse, sesResponse.getStatus());
        return sesResponse;
    }

    /**
     * Enriches the response with more information.
     *
     * @param sesResponse sesResponse
     * @param content    content
     * @param messageId messageId
     * @throws IOException IOException
     * @throws MessagingException MessagingException
     */
    private void enrichResponse(AmazonSESResponse sesResponse,
                                String content, String messageId)
            throws IOException, MessagingException {
        sesResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
        EmailTemplate template = new EmailTemplate();
        template.setBody(content);
        sesResponse.setTemplate(template);
        sesResponse.setMessageId(messageId);
    }

    /**
     * Method to set email subject.
     *
     * @param alert   AlertsInfo
     * @param message MimeMessage
     * @throws MessagingException MessagingException
     */
    private void setEmailSubject(AlertsInfo alert, MimeMessage message) throws MessagingException {
        String alertSubject = alert.getNotificationTemplate().getChannelTemplates().getEmail().getSubject();
        message.setSubject(alertSubject, "utf-8");
        message.setFrom(alert.getNotificationTemplate().getChannelTemplates().getEmail().getFrom());
    }

    /**
     * Method to send raw email.
     *
     * @param alert     AlertsInfo
     * @param emailList List of email
     * @param message   MimeMessage
     * @return SendRawEmailResult
     * @throws IOException         IOException
     * @throws MessagingException MessagingException
     */
    private SendRawEmailResult sendRawEmail(AlertsInfo alert, List<String> emailList, MimeMessage message)
            throws IOException, MessagingException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);
        RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
        SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage)
                .withDestinations(emailList)
                .withSource(alert.getNotificationTemplate().getChannelTemplates().getEmail().getFrom());

        return sesService.sendRawEmail(rawEmailRequest);
    }

    /**
     * Method to check if email address is bounced.
     *
     * @param userId   User id
     * @param emailList List of email
     */
    private void checkIfEmailAddressBounced(String userId, List<String> emailList) {
        if (enableSesBounceHandler) {
            Iterator<String> emailIterator = emailList.iterator();
            while (emailIterator.hasNext()) {
                boolean bouncedEmail = bounceHandler.bounced(emailIterator.next());
                if (bouncedEmail) {
                    emailIterator.remove();
                    LOG.error("Bounced email id for user {}. Email will not be sent.", userId);
                }
            }
        }
    }

    /**
     * Method to get Service Provider Name.
     *
     * @return Service provider name
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }

}
