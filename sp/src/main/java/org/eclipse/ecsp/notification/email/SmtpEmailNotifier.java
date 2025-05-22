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

package org.eclipse.ecsp.notification.email;


import com.codahale.metrics.MetricRegistry;
import jakarta.mail.MessagingException;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.SMTPEmailResponse;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.MESSAGE_ATTACHMENTS;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;

/**
 * SmtpEmailNotifier class.
 */
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@Component
public class SmtpEmailNotifier extends EmailNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(SmtpEmailNotifier.class);
    private static final String SVC_PROVIDER = "EMAIL:SMTP";
    MailService mailService;
    private MimeMessageHelper helper;

    /**
     * Set Mail Service.
     *
     * @param mailService MailService
     */
    @Autowired
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Do Publish.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse doPublish(AlertsInfo alert) {
        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        SMTPEmailResponse smtpResponse = new SMTPEmailResponse(userId);
        if (!validateUserId(smtpResponse, userId, alert)) {
            return smtpResponse;
        }

        EmailChannel emailChannel = alert.getNotificationConfig().getChannel(ChannelType.EMAIL);
        List<String> emailList = emailChannel.getEmails();
        String content = alert.getNotificationTemplate().getChannelTemplates().getEmail().getBody();
        try {
            for (String email : emailList) {
                helper = mailService.prepareMimeMessage(email, alert, content);
                prepareAttachments(helper, alert);
                mailService.sendEmail(helper.getMimeMessage());
                LOG.info("Message sent for event {} for pdid {}:", alert.getEventID(), alert.getPdid());
            }
            prepareSmtpResponse(smtpResponse, emailList, content);
        } catch (MessagingException mex) {
            LOG.error("Failed to send email to {}", userId, mex);
            smtpResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
        }
        return smtpResponse;
    }

    /**
     * Prepare Smtp Response.
     *
     * @param smtpResponse SMTPEmailResponse
     * @param emailList     List of email
     * @param content       String
     */
    private void prepareSmtpResponse(SMTPEmailResponse smtpResponse, List<String> emailList, String content) {
        smtpResponse.setAlertData(getDefaultAlertData(content));
        String destinationsString = emailList.stream().map(Object::toString).collect(Collectors.joining(","));
        smtpResponse.setDestination(destinationsString);
        LOG.info("emailList: {}", emailList);
        EmailTemplate template = new EmailTemplate();
        template.setBody(content);
        smtpResponse.setTemplate(template);
        smtpResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
    }


    /**
     * Prepare Attachments.
     *
     * @param helper MimeMessageHelper
     * @param alert  AlertsInfo
     * @throws MessagingException MessagingException
     */
    private void prepareAttachments(MimeMessageHelper helper, AlertsInfo alert) throws MessagingException {
        List<EmailAttachment> attachmentList = new ArrayList<>();

        EventData eventData = alert.getIgniteEvent().getEventData();
        if (!(eventData instanceof GenericEventData)) {
            return;
        }
        GenericEventData genericEventData = (GenericEventData) eventData;

        String locale = alert.getNotificationConfig().getLocale();
        Object attachments = genericEventData.getData().get(MESSAGE_ATTACHMENTS);
        Object perLocaleAttachments = genericEventData.getData().get(locale);

        attachmentList.addAll(getAttachmentsFromOptionalData(attachments));
        attachmentList.addAll(getAttachmentsFromOptionalData(perLocaleAttachments));

        for (EmailAttachment emailAttachment : attachmentList) {
            EmailAttachment attachment;
            try {
                byte[] attachmentAsByteArray = mapper.writeValueAsBytes(emailAttachment);
                attachment = mapper.readValue(attachmentAsByteArray, EmailAttachment.class);
            } catch (IOException e) {
                LOG.warn("Invalid attachment was received ");
                throw new MessagingException(e.getMessage());
            }
            helper.addAttachment(attachment.getFileName(),
                    new ByteArrayResource(Base64.decodeBase64(attachment.getContent())),
                    attachment.getMimeType());
        }
    }

    /**
     * Init.
     *
     * @param props       Properties
     * @param metricRegistry    MetricRegistry
     * @param notificationDao NotificationDao
     */
    @Override
    public void init(Properties props, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        super.init(props, metricRegistry, notificationDao);

        mailService.init(props);
    }

    /**
     * Setup Channel.
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
     * Destroy Channel.
     *
     * @param key       String
     * @param eventData String
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse destroyChannel(String key, String eventData) {
        // Nothing to do
        return null;
    }

    /**
     * Get Service Provider Name.
     *
     * @return String
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }


}