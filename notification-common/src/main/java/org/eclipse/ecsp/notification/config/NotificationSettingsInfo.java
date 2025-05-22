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

package org.eclipse.ecsp.notification.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO class for representing the notification configuration settings.
 */
@Entity(value = NotificationDaoConstants.NOTIFICATION_SETTINGS_COLLECTION_NAME)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class NotificationSettingsInfo extends AbstractIgniteEntity {

    @JsonProperty(value = "EventID")
    private String eventID;

    @JsonProperty(value = "Version")
    private String version;

    @JsonProperty(value = "BenchMode")
    private int benchMode;

    @JsonProperty(value = "Timestamp")
    private long timestamp;

    @JsonProperty(value = "Timezone")
    private int timezone;

    @JsonProperty(value = "Data")
    private NotificationData data;

    private List<ChannelResponse> channelResponses;

    @Id
    private String id;

    public NotificationSettingsInfo() {
        data = new NotificationData();
        channelResponses = new ArrayList<>();
    }

    /**
     * Get id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get eventID.
     *
     * @return eventID
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * Set eventID.
     *
     * @param eventID eventID
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Get version.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set version.
     *
     * @param version version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get benchMode.
     *
     * @return benchMode
     */
    public int getBenchMode() {
        return benchMode;
    }

    /**
     * Set benchMode.
     *
     * @param benchMode benchMode
     */
    public void setBenchMode(int benchMode) {
        this.benchMode = benchMode;
    }

    /**
     * Get timestamp.
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp.
     *
     * @param timestamp timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get timezone.
     *
     * @return timezone
     */
    public int getTimezone() {
        return timezone;
    }

    /**
     * Set timezone.
     *
     * @param timezone timezone
     */
    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    /**
     * Get data.
     *
     * @return data
     */
    public NotificationData getData() {
        return data;
    }

    /**
     * Set data.
     *
     * @param data data
     */
    public void setData(NotificationData data) {
        this.data = data;
    }

    /**
     * Get channelResponses.
     *
     * @return channelResponses
     */
    public List<ChannelResponse> getChannelResponses() {
        return channelResponses;
    }

    /**
     * Set channelResponses.
     *
     * @param channelResponses channelResponses
     */
    public void setChannelResponses(List<ChannelResponse> channelResponses) {
        this.channelResponses = channelResponses;
    }

    /**
     * Add channelResponse.
     *
     * @param channelResponse channelResponse
     */
    public void addChannelResponse(ChannelResponse channelResponse) {
        channelResponses.add(channelResponse);
    }

    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "NotificationSettingsInfo [eventID=" + eventID + ", version=" + version + ", benchMode=" + benchMode
            + ", timestamp=" + timestamp + ", timezone=" + timezone + ", data=" + data + ", id=" + id + "]";
    }

    /**
     * NotificationData class.
     */
    @Entity(useDiscriminator = false)
    public static class NotificationData {

        private List<NotificationConfig> notificationConfigs;

        public NotificationData() {
            notificationConfigs = new ArrayList<>();
        }

        /**
         * Get notificationConfigs.
         *
         * @return notificationConfigs
         */
        public List<NotificationConfig> getNotificationConfigs() {
            return notificationConfigs;
        }

        /**
         * Set notificationConfigs.
         *
         * @param notificationConfigs notificationConfigs
         */
        public void setNotificationConfigs(List<NotificationConfig> notificationConfigs) {
            this.notificationConfigs = notificationConfigs;
        }

        /**
         * To String.
         *
         * @return string
         */
        @Override
        public String toString() {
            return "NotificationData [notificationConfigs=" + notificationConfigs + "]";
        }

    }
}
