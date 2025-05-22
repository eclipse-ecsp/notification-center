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
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.PinDataV1_0;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * NotificationSettings class.
 */
public class NotificationSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationSettings.class);

    private NotificationSettings() {
    }

    /**
     * createNotificationSettingEvent.
     *
     * @param pinData pinData
     *
     * @return IgniteEvent
     */
    public static IgniteEvent createNotificationSettingEvent(PinDataV1_0 pinData) {
        NotificationConfig config = new NotificationConfig();
        config.setGroup("all");
        config.setEnabled(true);
        List<String> emails = pinData.getEmails();
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(false);
        config.addChannel(emailChannel);
        LOGGER.info("Creating email channel with {}", emails);
        if (emails != null && !emails.isEmpty()) {
            emailChannel.setEmails(emails);
            emailChannel.setEnabled(true);
        }

        LOGGER.info("Creating sms channel ");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(false);
        config.addChannel(smsChannel);
        List<String> phones = pinData.getPhones();
        if (phones != null && !phones.isEmpty()) {
            smsChannel.setPhones(phones);
            smsChannel.setEnabled(true);
        }
        LOGGER.info("Created sms channel {} ", smsChannel);
        IgniteEventImpl setting = null;
        if (!config.getChannels().isEmpty()) {
            setting = new IgniteEventImpl();
            setting.setEventId(EventID.NOTIFICATION_SETTINGS);
            NotificationSettingDataV1_0 data = new NotificationSettingDataV1_0();
            data.addNotificationConfig(config);
            setting.setEventData(data);
        }
        return setting;
    }
}
