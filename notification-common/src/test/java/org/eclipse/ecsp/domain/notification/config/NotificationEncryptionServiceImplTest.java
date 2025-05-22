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

package org.eclipse.ecsp.domain.notification.config;

import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationEncryptionServiceImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationEncryptionServiceImplTest {

    private NotificationEncryptionServiceImpl notificationEncryptionServiceImpl =
        new NotificationEncryptionServiceImpl();

    @Mock
    private EncryptDecryptInterface encryptDecryptInterface;

    @Test
    public void encryptNotificationConfig() {
        final String group = "noname003";
        final String brand = "brand1";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setBrand(brand);
        config.setEnabled(true);
        config.setUserId("test123");
        config.setVehicleId("test123");
        config.setContactId("test123");
        EmailChannel emailChannel = new EmailChannel();
        SmsChannel smsChannel = new SmsChannel();
        List<String> phones = new ArrayList<String>();
        phones.add("rerwer123");
        emailChannel.setEmails(phones);
        smsChannel.setPhones(phones);

        List<Channel> ls = new ArrayList<Channel>();
        ls.add(emailChannel);
        ls.add(smsChannel);
        config.setChannels(ls);
        ReflectionTestUtils.setField(notificationEncryptionServiceImpl, "encryptDecryptInterface",
            encryptDecryptInterface);
        notificationEncryptionServiceImpl.encryptNotificationConfig(config);
    }

    @Test
    public void decryptNotificationConfig() {
        final String group = "noname003";
        final String brand = "brand1";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setBrand(brand);
        config.setEnabled(true);
        config.setUserId("test123");
        config.setVehicleId("test123");
        config.setContactId("test123");
        EmailChannel emailChannel = new EmailChannel();
        SmsChannel smsChannel = new SmsChannel();
        List<String> phones = new ArrayList<String>();
        phones.add("rerwer123");
        emailChannel.setEmails(phones);
        smsChannel.setPhones(phones);

        List<Channel> ls = new ArrayList<Channel>();
        ls.add(emailChannel);
        ls.add(smsChannel);
        config.setChannels(ls);
        ReflectionTestUtils.setField(notificationEncryptionServiceImpl, "encryptDecryptInterface",
            encryptDecryptInterface);
        notificationEncryptionServiceImpl.decryptNotificationConfig(config);
    }
}
