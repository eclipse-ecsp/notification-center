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
import jakarta.mail.internet.MimeMessage;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * MailService interface.
 */
@Service
public interface MailService {

    /**
     * Initialize mail service.
     *
     * @param props Properties
     */
    void init(Properties props);

    /**
     * Prepare MimeMessage.
     *
     * @param email  String
     * @param alert  AlertsInfo
     * @param content String
     * @return MimeMessageHelper
     * @throws MessagingException MessagingException
     */
    MimeMessageHelper prepareMimeMessage(String email, AlertsInfo alert, String content) throws MessagingException;

    /**
     * Send email.
     *
     * @param message MimeMessage
     */
    void sendEmail(MimeMessage message);
}
