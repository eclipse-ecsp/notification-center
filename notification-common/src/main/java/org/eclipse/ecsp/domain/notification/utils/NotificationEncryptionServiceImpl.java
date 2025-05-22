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

package org.eclipse.ecsp.domain.notification.utils;

import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NotificationEncryptionServiceImpl class.
 */
@Component
public class NotificationEncryptionServiceImpl implements NotificationEncryptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEncryptionServiceImpl.class);
    @Autowired
    private EncryptDecryptInterface encryptDecryptInterface;

    /**
     * Encrypt NotificationConfig.
     *
     * @param notificationConfig NotificationConfig
     */
    @Override
    public void encryptNotificationConfig(NotificationConfig notificationConfig) {
        EmailChannel emailChannel = notificationConfig.getChannel(ChannelType.EMAIL);
        int size = 0;
        if (null != emailChannel && null != emailChannel.getEmails() && (size = emailChannel.getEmails().size()) > 0) {
            List<String> emails = emailChannel.getEmails();
            String email = null;
            for (int i = 0; i < size; i++) {
                email = emails.get(i);
                emails.add(i, encryptDecryptInterface.encrypt(email));
                emails.remove(email);
            }
        }
        SmsChannel smsChannel = notificationConfig.getChannel(ChannelType.SMS);
        if (null != smsChannel && null != smsChannel.getPhones() && (size = smsChannel.getPhones().size()) > 0) {
            List<String> phones = smsChannel.getPhones();
            String phone = null;
            for (int i = 0; i < size; i++) {
                phone = phones.get(i);
                phones.add(i, encryptDecryptInterface.encrypt(phone));
                phones.remove(phone);
            }
        }

    }

    /**
     * Decrypt NotificationConfig.
     *
     * @param notificationConfig NotificationConfig
     */
    @Override
    public void decryptNotificationConfig(NotificationConfig notificationConfig) {
        LOGGER.debug("decrypting NotificationConfig {}  ", notificationConfig);
        EmailChannel emailChannel = notificationConfig.getChannel(ChannelType.EMAIL);
        int size = 0;
        if (null != emailChannel && null != emailChannel.getEmails() && (size = emailChannel.getEmails().size()) > 0) {
            List<String> emails = emailChannel.getEmails();
            String email = null;
            for (int i = 0; i < size; i++) {
                email = emails.get(i);
                emails.add(i, encryptDecryptInterface.decrypt(email));
                emails.remove(email);
            }
        }
        SmsChannel smsChannel = notificationConfig.getChannel(ChannelType.SMS);
        if (null != smsChannel && null != smsChannel.getPhones() && (size = smsChannel.getPhones().size()) > 0) {
            List<String> phones = smsChannel.getPhones();
            String phone = null;
            for (int i = 0; i < size; i++) {
                phone = phones.get(i);
                phones.add(i, encryptDecryptInterface.decrypt(phone));
                phones.remove(phone);
            }
        }
        LOGGER.debug("decrypted NotificationConfig {}  ", notificationConfig);

    }
}
