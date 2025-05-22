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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.ecsp.utils.Constants;

import java.util.Map;

/**
 * NotificationCreationRequest class.
 */
public class    NotificationCreationRequest extends NotificationRequest {

    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    @JsonProperty(value = "NotificationId")
    private String notificationId;

    @JsonProperty(value = "Version")
    private String version;

    @NotNull(message = Constants.VALIDATION_MESSAGE)
    @JsonProperty(value = "Timestamp")
    private long timestamp;

    @JsonProperty(value = "Timezone")
    private short timezone;

    @JsonProperty(value = "Schedule")
    private String schedule;

    @JsonProperty(value = "Brand")
    private String brand;

    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    @JsonProperty(value = "Data")
    private Map<String, Object> data;

    @JsonIgnore
    @Transient
    private boolean isDynamicNotification;

    @JsonIgnore
    @Transient
    private boolean isUserNotification;

    @JsonIgnore
    @Transient
    private boolean isCampaignNotification;

    /**
     * Getter for NotificationId.
     *
     * @return notificationid
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Setter for NotificationId.
     *
     * @param notificationId the new value
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Getter for Version.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter for Version.
     *
     * @param version the new value
     */
    public void setVersion(String version) {
        this.version = version;
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
     * Getter for Timezone.
     *
     * @return timezone
     */
    public short getTimezone() {
        return timezone;
    }

    /**
     * Setter for Timezone.
     *
     * @param timezone the new value
     */
    public void setTimezone(short timezone) {
        this.timezone = timezone;
    }

    /**
     * Getter for Data.
     *
     * @return data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Setter for Data.
     *
     * @param data the new value
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public boolean isDynamicNotification() {
        return isDynamicNotification;
    }

    /**
     * Setter for DynamicNotification.
     *
     * @param isDynamicNotification the new value
     */
    public void setDynamicNotification(boolean isDynamicNotification) {
        this.isDynamicNotification = isDynamicNotification;
    }

    public boolean isUserNotification() {
        return isUserNotification;
    }

    /**
     * Setter for UserNotification.
     *
     * @param isUserNotification the new value
     */
    public void setUserNotification(boolean isUserNotification) {
        this.isUserNotification = isUserNotification;
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
     * Getter for Brand.
     *
     * @return brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Setter for Brand.
     *
     * @param brand the new value
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isCampaignNotification() {
        return isCampaignNotification;
    }

    /**
     * Setter for CampaignNotification.
     *
     * @param campaignNotification the new value
     */
    public void setCampaignNotification(boolean campaignNotification) {
        isCampaignNotification = campaignNotification;
    }

    @Override
    public String toString() {
        return "NotificationCreationRequest [notificationId=" + notificationId + ", version=" + version + ", timestamp="
            + timestamp + ", schedule=" + schedule + ", brand=" + brand + ", data=" + data + ", toString()="
            + super.toString() + "]";
    }
}
