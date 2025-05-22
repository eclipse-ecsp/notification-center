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


import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * AmazonSESNotifierSkipPublishTest.
 */
public class AmazonSesNotifierSkipPublishTest {


    @Test
    public void doPublish_success() throws Exception {

        AlertsInfo alert = new AlertsInfo();
        String userId = "dummyUser";
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setUserProfile(userProfile);
        alert.setAlertsData(data);

        String body = "msg_dumy";
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setBody(body);
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setEmail(emailTemplate);
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setChannelTemplates(channelTemplates);

        Map<String, NotificationTemplate> localeToNotificationTemplate = new HashMap<>();
        localeToNotificationTemplate.put("dummyLocale", notificationTemplate);
        alert.setLocaleToNotificationTemplate(localeToNotificationTemplate);

        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setLocale("dummyLocale");
        alert.setNotificationConfig(notificationConfig);
        AmazonSesNotifierSkipPublish amazonSesNotifierSkipPublish = new AmazonSesNotifierSkipPublish();

        ChannelResponse channelResponse = amazonSesNotifierSkipPublish.doPublish(alert);
        assertEquals(channelResponse.getUserID(), alert.getAlertsData().getUserProfile().getUserId());
        assertEquals(channelResponse.getAlertData().getDefaultMessage(),
            alert.getNotificationTemplate().getChannelTemplates().getEmail().getBody());
    }
}