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

package org.eclipse.ecsp.domain.notification;

import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * NotificationConstantsTest class.
 */
public class NotificationConstantsTest {

    @Test
    public void testConstants() {
        Assert.assertEquals(NotificationConstants.ADDITIONAL_ATTRIBUTE_MODEL_CODE_HEADER,
            NotificationConstants.ADDITIONAL_ATTRIBUTE_MODEL_CODE_HEADER);
        Assert.assertEquals(NotificationConstants.ADDITIONAL_ATTRIBUTE_MODEL_HEADER,
            NotificationConstants.ADDITIONAL_ATTRIBUTE_MODEL_HEADER);
        Assert.assertEquals(NotificationConstants.ALERT, NotificationConstants.ALERT);
        Assert.assertEquals(NotificationConstants.ALERT_MESSAGE, NotificationConstants.ALERT_MESSAGE);
        Assert.assertEquals(NotificationConstants.ALERT_TYPE, NotificationConstants.ALERT_TYPE);
        Assert.assertEquals(NotificationConstants.ANDROID_MSG_TTL, NotificationConstants.ANDROID_MSG_TTL);
        Assert.assertEquals(NotificationConstants.API_PUSH_PROVIDER, NotificationConstants.API_PUSH_PROVIDER);
        Assert.assertEquals(NotificationConstants.APNS, NotificationConstants.APNS);
        Assert.assertEquals(NotificationConstants.APP_PLATFORM, NotificationConstants.APP_PLATFORM);
        Assert.assertEquals(NotificationConstants.APS, NotificationConstants.APS);
        Assert.assertEquals(NotificationConstants.AWS_SES_PROVIDER, NotificationConstants.AWS_SES_PROVIDER);
        Assert.assertEquals(NotificationConstants.AWS_PUSH_PROVIDER, NotificationConstants.AWS_PUSH_PROVIDER);

        Assert.assertEquals(NotificationConstants.AWS_SMS_PROVIDER, NotificationConstants.AWS_SMS_PROVIDER);
        Assert.assertEquals(NotificationConstants.BRAND, NotificationConstants.BRAND);
        Assert.assertEquals(NotificationConstants.CAMPAIGN_DATE, NotificationConstants.CAMPAIGN_DATE);
        Assert.assertEquals(NotificationConstants.CAMPAIGN_ID, NotificationConstants.CAMPAIGN_ID);
        Assert.assertEquals(NotificationConstants.CLIENT_REQUEST_ID, NotificationConstants.CLIENT_REQUEST_ID);
        Assert.assertEquals(NotificationConstants.COLUMN, NotificationConstants.COLUMN);
        Assert.assertEquals(NotificationConstants.COMMA, NotificationConstants.COMMA);
        Assert.assertEquals(NotificationConstants.COUNTRY_CODE, NotificationConstants.COUNTRY_CODE);
        Assert.assertEquals(NotificationConstants.CUSTOM_EXTENSION, NotificationConstants.CUSTOM_EXTENSION);
        Assert.assertEquals(NotificationConstants.CUSTOM_PLACEHOLDERS, NotificationConstants.CUSTOM_PLACEHOLDERS);

        Assert.assertEquals(NotificationConstants.DATA, NotificationConstants.DATA);
        Assert.assertEquals(NotificationConstants.DEFAULT, NotificationConstants.DEFAULT);
        Assert.assertEquals(NotificationConstants.DEFAULT_MESSAGE, NotificationConstants.DEFAULT_MESSAGE);
        Assert.assertEquals(NotificationConstants.DISASSOCIATION_TOPIC, NotificationConstants.DISASSOCIATION_TOPIC);
        Assert.assertEquals(NotificationConstants.DOUBLE_QUOTES, NotificationConstants.DOUBLE_QUOTES);
        Assert.assertEquals(NotificationConstants.DRY_RUN, NotificationConstants.DRY_RUN);
        Assert.assertEquals(NotificationConstants.ENROLLMENT, NotificationConstants.ENROLLMENT);
        Assert.assertEquals(NotificationConstants.FAILURE, NotificationConstants.FAILURE);
        Assert.assertEquals(NotificationConstants.FCM_PAYLOAD, NotificationConstants.FCM_PAYLOAD);


    }

}
