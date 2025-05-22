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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.PushTemplate;
import org.eclipse.ecsp.notification.entities.SMSTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;

/**
 * SNS utils class.
 */
public class SnsUtils {

    static final String REQUEST_ID = "reqId";
    static final String SNS_TOPIC_ARN = "snsTopicArn";
    static final String IGNITE_HARMAN_COM = "ignite@harman.com";
    static final String TEMPLATE_COM = "template@com";

    private SnsUtils() {
    }

    /**
     * get alertsinfo.
     */
    public static AlertsInfo getAlertsInfo() throws IOException {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(GENERIC_NOTIFICATION_EVENT);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setUserProfile(getUserProfile());

        alertsInfo.setAlertsData(data);
        alertsInfo.addNotificationTemplate("en-US", getNotificationTemplate());
        alertsInfo.setNotificationConfig(getNotificationConfig());
        alertsInfo.setNotificationConfigs(Collections.singletonList(getNotificationConfig()));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        igniteEvent.setMessageId("12345");
        igniteEvent.setVehicleId("HUXOIDDN2HUN11");
        alertsInfo.setIgniteEvent(igniteEvent);

        return alertsInfo;
    }

    /**
     * get notif config.
     */
    public static NotificationConfig getNotificationConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationConfig notificationConfig = objectMapper.readValue(
            "{\n"
                + "  \"userId\" : \"testUser\",\n"
                + "  \"vehicleId\" : \"HUXOIDDN2HUN11\",\n"
                + "  \"contactId\" : \"self\",\n"
                + "  \"group\" : \"push\",\n"
                + "  \"enabled\" : true,\n"
                + "  \"channels\" : [\n"
                + "    {\n"
                + "      \"emails\" : [\n"
                + "        \"shai.tanchuma@harman.com\"\n"
                + "      ],\n"
                + "      \"type\" : \"email\",\n"
                + "      \"enabled\" : true\n"
                + "    },\n"
                + "    {\n"
                + "      \"phones\" : [\n"
                + "        \" +972528542238\"\n"
                + "      ],\n"
                + "      \"type\" : \"sms\",\n"
                + "      \"enabled\" : true\n"
                + "    },\n"
                + "    {\n"
                + "      \"service\" : \"apns\",\n"
                + "      \"appPlatform\" : \"ANDROID\",\n"
                + "      \"deviceTokens\" : [\n"
                + "        \"edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0"
                + "DuC53SLI_EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h\"\n"
                + "      ],\n"
                + "      \"type\" : \"push\",\n"
                + "      \"enabled\" : true\n"
                + "    }\n"
                + "  ]\n"
                + "}", NotificationConfig.class);
        notificationConfig.setLocale("en-US");
        return notificationConfig;
    }

    static UserProfile getUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("testUser");
        userProfile.setFirstName("test");
        userProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        userProfile.setDefaultEmail("testUser@harman.com");
        return userProfile;
    }

    static NotificationTemplate getNotificationTemplate() {
        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setBody("push body");
        pushTemplate.setTitle("push title");
        SMSTemplate smsTemplate = new SMSTemplate();
        smsTemplate.setBody("sms body");
        smsTemplate.setTitle("sms title");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setPush(pushTemplate);
        channelTemplates.setSms(smsTemplate);
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setChannelTemplates(channelTemplates);
        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));
        notificationTemplate.setNotificationId("LOW_FUEL");
        notificationTemplate.setBrand("default");
        return notificationTemplate;
    }
}
