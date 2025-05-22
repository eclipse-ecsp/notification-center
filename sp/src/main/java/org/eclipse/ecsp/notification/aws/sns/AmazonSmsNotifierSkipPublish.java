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

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonSNSSMSResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AmazonSmsNotifierSkipPublish class.
 */
@Component
@ConditionalOnProperty(name = "sms.default.sp", havingValue = "SMS:AWS_DUMMY")
public class AmazonSmsNotifierSkipPublish extends AmazonSmsNotifier {


    /**
     * Method to publish message to appropriate service.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {
        String userId = alert.getAlertsData().getUserProfile().getUserId();
        String alertMsg = alert.getNotificationTemplate().getChannelTemplates().getSms().getBody();
        AmazonSNSSMSResponse smsResponse = new AmazonSNSSMSResponse(userId, alert.getPdid());
        smsResponse.setAlertData(getDefaultAlertData(alertMsg));
        return smsResponse;
    }
}
