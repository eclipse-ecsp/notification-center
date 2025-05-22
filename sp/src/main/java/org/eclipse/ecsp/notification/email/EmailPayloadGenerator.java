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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.entities.AbstractIgniteEventBase;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EmailPayloadGenerator class.
 */
@Component
public class EmailPayloadGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailPayloadGenerator.class);
    private static final String CHARSET_UTF_8 = " charset=UTF-8";
    private static final String BODY_TEXT_TYPE = "text/plain;";
    private static final String BODY_HTML_TYPE = "text/html;";
    private static final String ATTACHMENTS = "attachments";
    private ObjectMapper mapper;

    /**
     * Set Mapper.
     *
     * @param mapper ObjectMapper
     */
    @Autowired
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Build email payload.
     *
     * @param messageBody String
     * @param isRichContent boolean
     * @param messageHtmlInline List of attachments
     * @param attachments List of attachments
     * @return Multipart
     * @throws MessagingException Exception
     */
    public Multipart build(String messageBody, boolean isRichContent, List<EmailAttachment> messageHtmlInline,
                           List<EmailAttachment> attachments) throws MessagingException {
        final Multipart mpMixed = new MimeMultipart("mixed");
          {
            if (!StringUtils.isEmpty(messageBody)) {
                final Multipart mpMixedAlternative = newChild(mpMixed, "alternative");
                  { // Note: MUST RENDER HTML LAST otherwise iPad mail client
                      // only renders the last image and no email
                      if (isRichContent) {
                          addHtmlVersion(mpMixedAlternative, messageBody, messageHtmlInline);
                      } else {
                          addTextVersion(mpMixedAlternative, messageBody);
                      }
                  }
            }
            addImages(mpMixed, attachments);
          }
        return mpMixed;
    }

    /**
     * Adds text to payload.
     *
     * @param mpRelatedAlternative Multipart
     * @param messageText String
     * @throws MessagingException Exception
     */
    private void addTextVersion(Multipart mpRelatedAlternative, String messageText) throws MessagingException {
        final MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(messageText, BODY_TEXT_TYPE + CHARSET_UTF_8);
        mpRelatedAlternative.addBodyPart(textPart);
    }

    /**
     *  Builds email body.
     *
     * @param parent Multipart
     * @param alternative String
     * @return Multipart
     * @throws MessagingException Exception
     */
    private Multipart newChild(Multipart parent, String alternative) throws MessagingException {
        MimeMultipart child = new MimeMultipart(alternative);
        final MimeBodyPart mbp = new MimeBodyPart();
        parent.addBodyPart(mbp);
        mbp.setContent(child);
        return child;
    }

    /**
     * Adds image(s) to payload.
     *
     * @param parent Multipart
     * @param attachments List of Email attachments
     * @throws MessagingException Exception class
     */
    private void addImages(Multipart parent, List<EmailAttachment> attachments) throws MessagingException {
        if (attachments != null) {
            for (EmailAttachment emailAttachment : attachments) {
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                ByteArrayDataSource byteArrayDataSource =
                        new ByteArrayDataSource(Base64.decodeBase64(emailAttachment.getContent()),
                                emailAttachment.getMimeType());
                mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));
                mimeBodyPart.setFileName(emailAttachment.getFileName());
                mimeBodyPart.setDisposition(emailAttachment.isInline() ? Part.INLINE : Part.ATTACHMENT);
                mimeBodyPart.setContentID(
                        emailAttachment.isInline() ? "<" + emailAttachment.getFileName() + ">" :
                                emailAttachment.getFileName());
                parent.addBodyPart(mimeBodyPart);
                LOGGER.debug("added {} image to HTML, filename={}", mimeBodyPart.getDisposition(),
                        emailAttachment.getFileName());
            }
        }
    }

    /**
     * Adds Html Version to payload.
     *
     * @param parent Multipart
     * @param messageHtml String
     * @param embedded List of email attachments
     * @throws MessagingException Exception
     */
    private void addHtmlVersion(Multipart parent, String messageHtml, List<EmailAttachment> embedded)
            throws MessagingException {
        final Multipart mpRelated = newChild(parent, "related");
        final MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(messageHtml, BODY_HTML_TYPE + CHARSET_UTF_8);
        mpRelated.addBodyPart(htmlPart);
        addImages(mpRelated, embedded);
    }

    /**
     * Extracts attachment(s) info from AlertsInfo.
     *
     * @param obj Object
     * @return List of attachments
     */
    private List<EmailAttachment> getAttachmentsFromOptionalData(Object obj) {
        List<EmailAttachment> emailAttachments = new ArrayList<>();
        if (obj == null) {
            return emailAttachments;
        }
        List<Object> attachments = (List<Object>) obj;

        for (Object attachment : attachments) {
            EmailAttachment emailAttachment;
            try {
                byte[] attachmentAsByteArray = mapper.writeValueAsBytes(attachment);
                emailAttachment = mapper.readValue(attachmentAsByteArray, EmailAttachment.class);
            } catch (IOException e) {

                throw new IllegalArgumentException();
            }
            emailAttachments.add(emailAttachment);
        }
        return emailAttachments;
    }

    /**
     * Extracts attachment(s) info from AlertsInfo.
     *
     * @param alert AlertsInfo
     * @return List of attachments
     */
    public List<EmailAttachment> getAttachmentsFromMessage(AlertsInfo alert) {
        List<EmailAttachment> attachmentList = new ArrayList<>();
        if (!(alert.getIgniteEvent() instanceof AbstractIgniteEventBase)) {
            return attachmentList;
        }
        String locale = alert.getNotificationConfig().getLocale();
        AbstractIgniteEventBase igniteEvent = (AbstractIgniteEventBase) alert.getIgniteEvent();
        LOGGER.debug("ignite event to get attachments {}", igniteEvent);
        GenericEventData genericEventData = (GenericEventData) igniteEvent.getEventData();
        Object attachments = genericEventData.getData().get(ATTACHMENTS);
        Object perLocaleAttachments = genericEventData.getData().get(locale);
        attachmentList.addAll(getAttachmentsFromOptionalData(attachments));
        attachmentList.addAll(getAttachmentsFromOptionalData(perLocaleAttachments));
        return attachmentList;
    }
}
