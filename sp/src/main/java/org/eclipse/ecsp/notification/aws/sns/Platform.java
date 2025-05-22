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

import org.eclipse.ecsp.analytics.stream.base.utils.ObjectUtils;

/**
 * Platform enum.
 */
public enum Platform {
    /**
     * Google cloud messaging platform for android devices.
     */
    GCM("GCM", "gcm"),
    /**
     * Apple Push Notification Service for iOS devices.
     */
    APNS("APNS", "apns"),
    // Sandbox version of Apple Push Notification Service
    // Note that there is no service name for APNS_SANDBOX. it will come as
    // apns for APNS and APNS_SANDBOX
    /**
     * Apple Push Notification Service for iOS devices in sandbox mode.
     */
    APNS_SANDBOX("APNS_SANDBOX", "apns");

    /**
     * The first type is of how SNS is expecting the platform name and
     * second is how the MobileClient sends the type in the
     * NotificationSettings data.

     * Example: { "type": "push", "enabled": true, "token":
     * "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
     * "service": "apns/gcm", "alertTypes": ["all"] }
     */
    String snsCompatibleName;
    String saasServiceName;

    /**
     * Constructor.
     *
     * @param snsCompatibleName String
     * @param saasServiceName   String
     */
    Platform(String snsCompatibleName, String saasServiceName) {
        this.snsCompatibleName = snsCompatibleName;
        this.saasServiceName = saasServiceName;
    }

    /**
     * Get the SNS compatible name.
     *
     * @return String
     */
    public String getSnsCompatibleName() {
        return this.snsCompatibleName;
    }

    /**
     * Get the service name.
     *
     * @return String
     */
    public String getSaasServiceName() {
        return this.saasServiceName;
    }

    /**
     * To get the platform you pass the service name. For example : gcm or
     * apns only.
     *
     * @param platform String
     * @return Platform
     */
    public static Platform getType(String platform) {
        ObjectUtils.requireNonEmpty(platform, "Platform / service name cannot be empty.");
        for (Platform pf : Platform.values()) {
            if (pf.getSaasServiceName().equals(platform)) {
                return pf;
            }
        }
        throw new IllegalArgumentException("Platform of type " + platform + " is not supported.");
    }

}