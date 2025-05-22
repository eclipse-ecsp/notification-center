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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * FcmPushAlertEventData class.
 */
public class FcmPushAlertEventData extends AlertEventData {

    @JsonProperty(NotificationConstants.FCM_RESPONSE)
    private String fcmResponse;
    @JsonProperty(NotificationConstants.FCM_PAYLOAD)
    private String fcmPayload;
    @JsonProperty(NotificationConstants.APP_PLATFORM)
    private String appPlatform;

    /**
     * This method is a getter for fcmResponse.
     *
     * @return String
     */
    public String getFcmResponse() {
        return fcmResponse;
    }

    /**
     * This method is a setter for fcmResponse.
     *
     * @param fcmResponse String
     */
    public void setFcmResponse(String fcmResponse) {
        this.fcmResponse = fcmResponse;
    }

    /**
     * This method is a getter for fcmPayload.
     *
     * @return String
     */
    public String getFcmPayload() {
        return fcmPayload;
    }

    /**
     * This method is a setter for fcmPayload.
     *
     * @param fcmPayload String
     */
    public void setFcmPayload(String fcmPayload) {
        this.fcmPayload = fcmPayload;
    }

    /**
     * This method is a getter for appPlatform.
     *
     * @return String
     */
    public String getAppPlatform() {
        return appPlatform;
    }

    /**
     * This method is a setter for appPlatform.
     *
     * @param appPlatform String
     */
    public void setAppPlatform(String appPlatform) {
        this.appPlatform = appPlatform;
    }

}
