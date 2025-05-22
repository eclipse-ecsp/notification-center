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
import dev.morphia.annotations.Entity;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * FCMChannelResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.FCMChannelResponse")
public class FCMChannelResponse extends AbstractChannelResponse {

    @JsonProperty(value = "fcmResponse")
    private String fcmResponse;

    /**
     * This method is a constructor for FCMChannelResponse.
     */
    public FCMChannelResponse() {
        this(null);
    }

    /**
     * This method is a constructor for FCMChannelResponse.
     *
     * @param userId String
     */
    public FCMChannelResponse(String userId) {
        this(userId, null);
    }

    /**
     * This method is a constructor for FCMChannelResponse.
     *
     * @param userId String
     * @param pdid   String
     */
    public FCMChannelResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * This method is a constructor for FCMChannelResponse.
     *
     * @param userId    String
     * @param pdid      String
     * @param eventData String
     */
    public FCMChannelResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
        setChannelType(ChannelType.MOBILE_APP_PUSH);
    }

    /**
     * Get FCM Response.
     *
     * @return String
     */
    public String getFcmResponse() {
        return fcmResponse;
    }

    /**
     * Set FCM Response.
     *
     * @param fcmResponse String
     */
    public void setFcmResponse(String fcmResponse) {
        this.fcmResponse = fcmResponse;
    }

    /**
     * This method is a getter for provider.
     *
     * @return String
     */
    @Override
    public String getProvider() {
        return NotificationConstants.FCM_PROVIDER;
    }

}
