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

package org.eclipse.ecsp.notification.entities;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Transient;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BufferedAlertsInfo class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(useDiscriminator = false)
public class BufferedAlertsInfo {
    /**
     * BufferedAlertsInfo default constructor.
     */
    public BufferedAlertsInfo() {
        alertsData = new Data();
        this.cloneNotificationConfig = new CloneNotificationConfig();
        this.cloneLocaleToNotificationTemplates = new HashMap<>();
        this.cloneNotificationTemplateConfig = new CloneNotificationTemplateConfig();
    }

    private IgniteEvent igniteEvent;

    private CloneNotificationConfig cloneNotificationConfig;

    private Map<String, CloneNotificationTemplate> cloneLocaleToNotificationTemplates;

    private CloneNotificationTemplateConfig cloneNotificationTemplateConfig;

    /**
     * This method is a getter for clonelocaletonotificationtemplates.
     *
     * @return Map
     */

    public Map<String, CloneNotificationTemplate> getCloneLocaleToNotificationTemplates() {
        return cloneLocaleToNotificationTemplates;
    }

    /**
     * This method is a setter for clonelocaletonotificationtemplates.
     *
     * @param cloneLocaleToNotificationTemplates : Map
     */

    public void setCloneLocaleToNotificationTemplates(
            Map<String, CloneNotificationTemplate> cloneLocaleToNotificationTemplates) {
        this.cloneLocaleToNotificationTemplates = cloneLocaleToNotificationTemplates;
    }


    @JsonProperty(value = "EventID")
    @Property("EventID")
    private String eventID;

    @JsonProperty(value = "Data")
    private Data alertsData;

    @JsonProperty(value = "Version")
    @Property("Version")
    private String version;

    @JsonProperty(value = "pdid")
    private String pdid;

    @JsonProperty(value = "BenchMode")
    @Property("BenchMode")
    private int benchMode;

    @JsonProperty(value = "Timestamp")
    @Property("Timestamp")
    private long timestamp;

    @JsonProperty(value = "Timezone")
    @Property("Timezone")
    private int timezone;

    @Transient
    private List<NotificationConfig> notificationConfigs = new ArrayList<>();

    /**
     * This method is a getter for clonenotificationconfig.
     *
     * @return CloneNotificationConfig
     */

    public CloneNotificationConfig getCloneNotificationConfig() {
        return cloneNotificationConfig;
    }

    /**
     * This method is a getter for clonenotificationtemplate.
     *
     * @return CloneNotificationTemplate
     */
    public CloneNotificationTemplate getCloneNotificationTemplate() {
        if (CollectionUtils.isEmpty(cloneLocaleToNotificationTemplates)) {
            return null;
        }

        if (cloneNotificationConfig == null) {
            return CollectionUtils.isEmpty(this.notificationConfigs) ? null :
                    cloneLocaleToNotificationTemplates.get(notificationConfigs.get(0).getLocale());
        }

        return cloneLocaleToNotificationTemplates.get(cloneNotificationConfig.getLocale());
    }

    /**
     * addCloneNotificationTemplate to cloneLocaleToNotificationTemplates.
     *
     * @param locale locale
     *
     * @param notificationTemplate template
     */
    public void addCloneNotificationTemplate(String locale, CloneNotificationTemplate notificationTemplate) {
        if (CollectionUtils.isEmpty(this.cloneLocaleToNotificationTemplates)) {
            this.cloneLocaleToNotificationTemplates = new HashMap<>();
        }

        this.cloneLocaleToNotificationTemplates.put(locale, notificationTemplate);
    }

    /**
     * This method is a setter for clonenotificationconfig.
     *
     * @param cloneNotificationConfig : CloneNotificationConfig
     */

    public void setCloneNotificationConfig(CloneNotificationConfig cloneNotificationConfig) {
        this.cloneNotificationConfig = cloneNotificationConfig;
    }

    /**
     * This method is a getter for clonenotificationtemplateconfig.
     *
     * @return CloneNotificationTemplateConfig
     */

    public CloneNotificationTemplateConfig getCloneNotificationTemplateConfig() {
        return cloneNotificationTemplateConfig;
    }

    /**
     * This method is a setter for clonenotificationtemplateconfig.
     *
     * @param cloneNotificationTemplateConfig : CloneNotificationTemplateConfig
     */

    public void setCloneNotificationTemplateConfig(CloneNotificationTemplateConfig cloneNotificationTemplateConfig) {
        this.cloneNotificationTemplateConfig = cloneNotificationTemplateConfig;
    }

    /**
     * This method is a getter for alertsdata.
     *
     * @return Data
     */

    public Data getAlertsData() {
        return alertsData;
    }

    /**
     * This method is a setter for alertsdata.
     *
     * @param alertsData : Data
     */

    public void setAlertsData(Data alertsData) {
        this.alertsData = alertsData;
    }

    /**
     * This method is a getter for igniteevent.
     *
     * @return IgniteEvent
     */


    public IgniteEvent getIgniteEvent() {
        return igniteEvent;
    }

    /**
     * This method is a setter for igniteevent.
     *
     * @param igniteEvent : IgniteEvent
     */

    public void setIgniteEvent(IgniteEvent igniteEvent) {
        this.igniteEvent = igniteEvent;
    }

    /**
     * This method is a getter for eventid.
     *
     * @return String
     */

    public String getEventID() {
        return eventID;
    }

    /**
     * This method is a setter for eventid.
     *
     * @param eventID : String
     */

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * This method is a getter for benchmode.
     *
     * @return int
     */

    public int getBenchMode() {
        return benchMode;
    }

    /**
     * This method is a setter for benchmode.
     *
     * @param benchMode : int
     */

    public void setBenchMode(int benchMode) {
        this.benchMode = benchMode;
    }

    /**
     * This method is a getter for timestamp.
     *
     * @return long
     */

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * This method is a setter for timestamp.
     *
     * @param timestamp : long
     */

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * This method is a getter for version.
     *
     * @return String
     */

    public String getVersion() {
        return version;
    }

    /**
     * This method is a setter for version.
     *
     * @param version : String
     */

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * This method is a getter for timezone.
     *
     * @return int
     */

    public int getTimezone() {
        return timezone;
    }

    /**
     * This method is a setter for timezone.
     *
     * @param timezone : int
     */

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    /**
     * This method is a getter for pdid.
     *
     * @return String
     */

    public String getPdid() {
        return pdid;
    }

    /**
     * This method is a setter for pdid.
     *
     * @param pdid : String
     */

    public void setPdid(String pdid) {
        this.pdid = pdid;
    }

    /**
     * This method is a getter for notificationconfigs.
     *
     * @return List
     */

    public List<NotificationConfig> getNotificationConfigs() {
        return notificationConfigs;
    }

    /**
     * This method is a setter for notificationconfigs.
     *
     * @param notificationConfigs : List
     */

    public void setNotificationConfigs(List<NotificationConfig> notificationConfigs) {
        this.notificationConfigs = notificationConfigs;
    }

    /**
     * This method is a getter for notificationconfig.
     *
     * @param notificationConfig : NotificationConfig
     */
    public void addNotificationConfig(NotificationConfig notificationConfig) {
        this.notificationConfigs.add(notificationConfig);
    }

    /**
     * static Data class.
     */
    @Entity(useDiscriminator = false)
    public static class Data {

        private Map<String, Object> alertDataProperties;
        private UserProfile userProfile;
        private VehicleProfileAbridged vehicleProfile;
        private String notificationId;
        private String marketingName;

        /**
         * Data default constructor.
         */
        public Data() {
            alertDataProperties = new HashMap<>();
        }

        /**
         * getAlertDataProperties.
         *
         * @return map
         */
        public Map<String, Object> getAlertDataProperties() {
            return alertDataProperties;
        }

        /**
         * setAlertDataProperties.
         *
         * @param alertDataProperties map
         */
        public void setAlertDataProperties(Map<String, Object> alertDataProperties) {
            this.alertDataProperties = alertDataProperties;
        }

        /**
         * This method is a getter for alertdataproperties.
         *
         * @return Map
         */
        @JsonAnyGetter
        public Map<String, Object> any() {
            return alertDataProperties;
        }

        /**
         * This method is a setter for alertdataproperties.
         *
         * @param name : String
         * @param value : Object
         */
        @JsonAnySetter
        public void set(String name, Object value) {
            alertDataProperties.put(name, value);
        }

        /**
         * This method is a getter for userprofile.
         *
         * @return UserProfile
         */
        public UserProfile getUserProfile() {
            return userProfile;
        }

        /**
         * This method is a setter for userprofile.
         *
         * @param userProfile : UserProfile
         */
        public void setUserProfile(UserProfile userProfile) {
            this.userProfile = userProfile;
        }

        /**
         * This method is a getter for vehicleprofile.
         *
         * @return VehicleProfileAbridged
         */
        public VehicleProfileAbridged getVehicleProfile() {
            return vehicleProfile;
        }

        /**
         * This method is a setter for vehicleprofile.
         *
         * @param vehicleProfile : VehicleProfileAbridged
         */
        public void setVehicleProfile(VehicleProfileAbridged vehicleProfile) {
            this.vehicleProfile = vehicleProfile;
        }

        /**
         * This method is a getter for notificationid.
         *
         * @return String
         */
        public String getNotificationId() {
            return notificationId;
        }

        /**
         * This method is a setter for notificationid.
         *
         * @param notificationId : String
         */
        public void setNotificationId(String notificationId) {
            this.notificationId = notificationId;
        }

        /**
         * This method is a getter for marketingname.
         *
         * @return String
         */
        public String getMarketingName() {
            return marketingName;
        }

        /**
         * This method is a setter for marketingname.
         *
         * @param marketingName : String
         */
        public void setMarketingName(String marketingName) {
            this.marketingName = marketingName;
        }

        /**
         * This method is a getter for alertdataproperties.
         *
         * @return String
         */
        @Override
        public String toString() {
            return "Data [alertDataProperties=" + alertDataProperties + ", userProfile=" + userProfile
                    + ", vehicleProfile=" + vehicleProfile + ", notificationId=" + notificationId
                    + ", marketingName=" + marketingName + "]";
        }

    }

    /**
     * This method is a toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "BufferedAlertsInfo{"
                + "igniteEvent=" + igniteEvent + ", cloneNotificationConfig=" + cloneNotificationConfig
                + ", cloneNotificationTemplate=" + cloneLocaleToNotificationTemplates
                + ", cloneNotificationTemplateConfig=" + cloneNotificationTemplateConfig
                + ", eventID='" + eventID + '\'' + ", alertsData=" + alertsData
                + ", version='" + version + '\'' + ", benchMode=" + benchMode
                + ", timestamp=" + timestamp + ", timezone=" + timezone
                + ", pdid='" + pdid + '\'' + ", notificationConfigs=" + notificationConfigs + '}';
    }
}