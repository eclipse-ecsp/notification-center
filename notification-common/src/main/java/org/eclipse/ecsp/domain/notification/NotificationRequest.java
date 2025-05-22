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
 * NotificationRequest class.
 */
@Entity
public abstract class NotificationRequest {
    private String userId;
    private String vehicleId;
    private String requestId;
    private String sessionId;
    private String clientRequestId;
    private String schedule;
    private String campaignId;

    /**
     * Getter for UserId.
     *
     * @return userid
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter for UserId.
     *
     * @param userId the new value
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
     * Getter for ClientRequestId.
     *
     * @return clientrequestid
     */
    public String getClientRequestId() {
        return clientRequestId;
    }

    /**
     * Setter for ClientRequestId.
     *
     * @param clientRequestId the new value
     */
    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    /**
     * Getter for Schedule.
     *
     * @return schedule
     */
    public String getSchedule() {
        return schedule;
    }

    /**
     * Setter for Schedule.
     *
     * @param schedule the new value
     */
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    /**
     * Getter for CampaignId.
     *
     * @return campaignid
     */
    public String getCampaignId() {
        return campaignId;
    }

    /**
     * Setter for CampaignId.
     *
     * @param campaignId the new value
     */
    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    @Override
    public String toString() {
        return "NotificationRequest [userId=" + userId + ", vehicleId=" + vehicleId + ", requestId=" + requestId
            + ", sessionId=" + sessionId + ", clientRequestId=" + clientRequestId + " schedule="
            + getSchedule() + " campaignId=" + getCampaignId() + "]";
    }

}
