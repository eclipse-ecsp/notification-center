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

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.AWS_PINPOINT_EMAIL_PROVIDER;

/**
 * AmazonPinpointEmailChannelResponse class.
 */
@Entity(discriminatorKey = "className",
    discriminator = "org.eclipse.ecsp.domain.notification.AmazonPinpointEmailChannelResponse")
public class AmazonPinpointEmailChannelResponse extends EmailResponse {

    @JsonProperty(value = "deliveryStatus")
    private java.util.Map<String, EndpointResult> deliveryStatus;

    /**
     * Constructor.
     */
    public AmazonPinpointEmailChannelResponse() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param userId user id
     */
    public AmazonPinpointEmailChannelResponse(String userId) {
        this(userId, null);
    }

    /**
     * Constructor.
     *
     * @param userId user id
     * @param pdid   pdid
     */
    public AmazonPinpointEmailChannelResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * Constructor.
     *
     * @param userId    user id
     * @param pdid      pdid
     * @param eventData event data
     */
    public AmazonPinpointEmailChannelResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
    }

    /**
     * Get delivery status.
     *
     * @return delivery status
     */
    public java.util.Map<String, EndpointResult> getDeliveryStatus() {
        return deliveryStatus;
    }

    /**
     * Set delivery status.
     *
     * @param deliveryStatus delivery status
     */
    public void setDeliveryStatus(java.util.Map<String, EndpointResult> deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * Get provider.
     *
     * @return provider
     */
    @Override
    public String getProvider() {
        return AWS_PINPOINT_EMAIL_PROVIDER;
    }
}
