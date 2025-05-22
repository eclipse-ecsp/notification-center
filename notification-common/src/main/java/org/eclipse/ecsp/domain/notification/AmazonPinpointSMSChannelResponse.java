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

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.AWS_PINPOINT_SMS_PROVIDER;

/**
 * Amazon Pinpoint SMS Response Entity.
 *
 * @author MaKumari
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className",
    discriminator = "org.eclipse.ecsp.domain.notification.AmazonPinpointSMSChannelResponse")
public class AmazonPinpointSMSChannelResponse extends AbstractChannelResponse {

    @JsonProperty(value = "deliveryStatus")
    private java.util.Map<String, EndpointResult> deliveryStatus;

    @SuppressWarnings("checkstyle:ConstantName")
    private static final ChannelType channelType = ChannelType.SMS;

    /**
     * Constructor.
     */
    public AmazonPinpointSMSChannelResponse() {
        this(null, null, null);
    }

    /**
     * Constructor.
     *
     * @param userId user id
     * @param pdid   pdid
     */
    public AmazonPinpointSMSChannelResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * Constructor.
     *
     * @param userId    user id
     * @param pdid      pdid
     * @param eventData event data
     */
    public AmazonPinpointSMSChannelResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
        setChannelType(ChannelType.SMS);
    }

    /**
     * Get channel type.
     *
     * @return channel type
     */
    @Override
    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * Get channel type.
     *
     * @return channel type
     */
    public static ChannelType getChanneltype() {
        return channelType;
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
        return AWS_PINPOINT_SMS_PROVIDER;
    }
}
