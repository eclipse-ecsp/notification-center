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

import dev.morphia.annotations.Entity;

/**
 * EndpointResult class.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.EndpointResult")
public class EndpointResult {
    /**
     * Address that endpoint message was delivered to.
     */
    private String address;
    /**
     * The delivery status of the message. Possible values:
     *
     * <p>SUCCESS - The message was successfully delivered to the endpoint.
     *
     * <p>TRANSIENT_FAILURE - A temporary error occurred. Amazon Pinpoint will
     * attempt to deliver the message again later.
     *
     * <p>FAILURE_PERMANENT - An error occurred when delivering the message to the
     * endpoint. Amazon Pinpoint won't attempt to send the message again.
     *
     * <p>TIMEOUT - The message couldn't be sent within the timeout period.
     *
     * <p>QUIET_TIME - The local time for the endpoint was within the QuietTime for
     * the campaign or app.
     *
     * <p>DAILY_CAP - The endpoint has received the maximum number of messages it
     * can receive within a 24-hour period.
     *
     * <p>HOLDOUT - The endpoint was in a hold out treatment for the campaign.
     *
     * <p>THROTTLED - Amazon Pinpoint throttled sending to this endpoint.
     *
     * <p>EXPIRED - The endpoint address is expired.
     *
     * <p>CAMPAIGN_CAP - The endpoint received the maximum number of messages
     * allowed by the campaign.
     *
     * <p>SERVICE_FAILURE - A service-level failure prevented Amazon Pinpoint from
     * delivering the message.
     *
     * <p>UNKNOWN - An unknown error occurred.
     */
    private String deliveryStatus;
    /**
     * Unique message identifier associated with the message that was sent.
     */
    private String messageId;
    /**
     * Downstream service status code.
     */
    private Integer statusCode;
    /**
     * Status message for message delivery.
     */
    private String statusMessage;
    /**
     * If token was updated as part of delivery. (This is GCM Specific)
     */
    private String updatedToken;

    /**
     * Get Address.
     *
     * @return String
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set Address.
     *
     * @param address String
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get DeliveryStatus.
     *
     * @return String
     */
    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    /**
     * Set DeliveryStatus.
     *
     * @param deliveryStatus String
     */
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * Get MessageId.
     *
     * @return String
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Set MessageId.
     *
     * @param messageId String
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Get StatusCode.
     *
     * @return Integer
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Set StatusCode.
     *
     * @param statusCode Integer
     */
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get StatusMessage.
     *
     * @return String
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Set StatusMessage.
     *
     * @param statusMessage String
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Get UpdatedToken.
     *
     * @return String
     */
    public String getUpdatedToken() {
        return updatedToken;
    }

    /**
     * Set UpdatedToken.
     *
     * @param updatedToken String
     */
    public void setUpdatedToken(String updatedToken) {
        this.updatedToken = updatedToken;
    }

    /**
     * This method is a toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "EndpointMessageResult [address=" + address + ", deliveryStatus=" + deliveryStatus + ", messageId="
            + messageId + ", statusCode=" + statusCode + ", statusMessage="
            + statusMessage + ", updatedToken=" + updatedToken + "]";
    }

}
