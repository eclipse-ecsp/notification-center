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
import org.eclipse.ecsp.domain.notification.AmazonSESResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AmazonSesNotifierSkipPublish class.
 */
@Component
@ConditionalOnProperty(name = "email.default.sp", havingValue = "EMAIL:AWS_DUMMY")
public class AmazonSesNotifierSkipPublish extends AmazonSesNotifier {

    private static final String SVC_PROVIDER = "EMAIL:AWS_DUMMY";

    /**
     * Method to publish message to appropriate service.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {

        String userId = alert.getAlertsData().getUserProfile().getUserId();
        String alertMsg = alert.getNotificationTemplate().getChannelTemplates().getEmail().getBody();
        AmazonSESResponse sesResponse = new AmazonSESResponse(userId);
        sesResponse.setAlertData(getDefaultAlertData(alertMsg));
        return sesResponse;
    }

    /**
     * Get service provider name.
     *
     * @return Service provider name
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }
}
