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

package org.eclipse.ecsp.notification.mobile.platforms;

import com.amazonaws.services.sns.AmazonSNSClient;

/**
 * Interface for mobile app on different platforms.
 */
public interface PushPlatform {

    /**
     * Each platform should create their platform application.
     *
     * @param snsClient AmazonSNSClient
     * @param applicationName String
     * @param principalFileName String
     * @param credentialFileName String
     * @param serverApiKeyFileName String
     * @param topicArnPrefix String
     *
     */
    public void createPlatformApplication(AmazonSNSClient snsClient, String applicationName,
                                          String principalFileName,
                                          String credentialFileName, String serverApiKeyFileName,
                                          String topicArnPrefix);

    /**
     * Return the associated service name. This name is specified in the
     * NotificationSettings events
     * Example: { "type": "push", "enabled": true, "token":
     * "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
     * "service": "apns_sandbox", "alertTypes": ["all"] },
     *
     * @return String
     */
    public String getSnsPlatformName();

    /**
     * Return the platform applicaton arn name.
     *
     * @return String
     */
    public String getPlatformApplicationArn();
}
