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

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * MailServiceImpl class.
 */
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@Service
public class MailServiceImpl implements MailService {

    private JavaMailSender mailSender;
    private String from;
    private String subject;

    /**
     * Set mail sender.
     *
     * @param mailSender JavaMailSender
     */
    @Autowired
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * prepareMimeMessage method.
     *
     * @param email String
     * @param alert AlertsInfo
     * @param content String
     * @return MimeMessageHelper
     *
     * @throws MessagingException Exception class
     */
    public MimeMessageHelper prepareMimeMessage(String email, AlertsInfo alert, String content)
            throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(new InternetAddress(from));
        mimeMessageHelper.setTo(new InternetAddress(email));
        String alertSubject = alert.getEventID() + NotificationConstants.SPACE + subject;
        mimeMessageHelper.setSubject(alertSubject);
        mimeMessageHelper.setText(content, "text/html");
        return mimeMessageHelper;
    }

    /**
     * sendEmail method.
     *
     * @param message MimeMessage
     */
    public void sendEmail(MimeMessage message) {
        mailSender.send(message);
    }

    /**
     * Initialize mail service.
     *
     * @param props Properties
     */
    @Override
    public void init(Properties props) {
        from = props.getProperty(NotificationProperty.EMAIL_FROM);
        subject = props.getProperty(NotificationProperty.EMAIL_SUBJECT);
    }


}
