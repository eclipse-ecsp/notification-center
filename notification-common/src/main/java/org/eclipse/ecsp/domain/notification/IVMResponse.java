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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.ecsp.utils.Constants;

/**
 * IVMResponse abstract class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class IVMResponse {

    private static final long serialVersionUID = -470373577484784015L;

    private String vehicleId;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String messageId;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String requestId;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String sessionId;

    private String sourceDeviceId;

    private String correlationId;
    @NotNull(message = Constants.VALIDATION_MESSAGE)
    private long timestamp;


    /**
     * Getter for VehicleId.
     *
     * @return vehicleid
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Setter for VehicleId.
     *
     * @param vehicleId the new value
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Getter for MessageId.
     *
     * @return messageid
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Setter for MessageId.
     *
     * @param messageId the new value
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Getter for SessionId.
     *
     * @return sessionid
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Setter for SessionId.
     *
     * @param sessionId the new value
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Getter for RequestId.
     *
     * @return requestid
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter for RequestId.
     *
     * @param requestId the new value
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter for SourceDeviceId.
     *
     * @return sourcedeviceid
     */
    public String getSourceDeviceId() {
        return sourceDeviceId;
    }

    /**
     * Setter for SourceDeviceId.
     *
     * @param sourceDeviceId the new value
     */
    public void setSourceDeviceId(String sourceDeviceId) {
        this.sourceDeviceId = sourceDeviceId;
    }

    /**
     * Getter for Timestamp.
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Setter for Timestamp.
     *
     * @param timestamp the new value
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Getter for CorrelationId.
     *
     * @return correlationid
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Setter for CorrelationId.
     *
     * @param correlationId the new value
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return String.format(
            "IVMResponse [messageId=%s, requestId=%s, sessionId=%s, sourceDeviceId=%s, correlationId=%s, timestamp=%s]",
            messageId, requestId, sessionId, sourceDeviceId, correlationId, timestamp);
    }

}
