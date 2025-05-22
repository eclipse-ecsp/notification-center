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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Transient;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * POJO class for representing the alerts data.
 */
@Entity(useDiscriminator = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertsInfo {

    /**
     * AlertsInfo constructor.
     */
    public AlertsInfo() {
        alertsData = new Data();
    }

    @JsonIgnore
    @Transient
    private IgniteEvent igniteEvent;

    @JsonIgnore
    @Transient
    private NotificationConfig notificationConfig;

    @JsonIgnore
    @Transient
    private Map<String, NotificationTemplate> localeToNotificationTemplate;

    @JsonIgnore
    @Transient
    Map<String, Map<String, String>> localeToPlaceholders;

    @JsonIgnore
    @Transient
    private NotificationTemplateConfig notificationTemplateConfig;

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @JsonProperty(value = "EventID")
    @Property("EventID")
    private String eventID;

    @JsonProperty(value = "Data")
    private Data alertsData;

    @JsonProperty(value = "Version")
    @Property("Version")
    private String version;

    @JsonProperty(value = "BenchMode")
    @Property("BenchMode")
    private int benchMode;

    @JsonProperty(value = "Timezone")
    @Property("Timezone")
    private int timezone;

    @JsonProperty(value = "Timestamp")
    @Property("Timestamp")
    private long timestamp;

    /**
     * PDID is not a part of alert data. It is populated while we publish the
     * alerts
     */
    @JsonProperty(value = "pdid")
    private String pdid;

    @JsonIgnore
    @Transient
    private List<NotificationConfig> notificationConfigs = new ArrayList<>();

    @JsonIgnore
    @Transient
    private MuteVehicle muteVehicle;

    @JsonIgnore
    @Transient
    Set<NotificationTemplate> allLanguageTemplates;

    @JsonIgnore
    @Transient
    private NotificationGrouping notificationGrouping;

    /**
     * Get alerts data.
     *
     * @return alertsData
     */
    public Data getAlertsData() {
        return alertsData;
    }

    /**
     * Set alerts data.
     *
     * @param alertsData alertsData
     */
    public void setAlertsData(Data alertsData) {
        this.alertsData = alertsData;
    }

    /**
     * Get locale to notification template.
     *
     * @return localeToNotificationTemplate
     */
    public Map<String, NotificationTemplate> getLocaleToNotificationTemplate() {
        return localeToNotificationTemplate;
    }

    /**
     * Set locale to notification template.
     *
     * @param localeToNotificationTemplate localeToNotificationTemplate
     */
    public void setLocaleToNotificationTemplate(Map<String, NotificationTemplate> localeToNotificationTemplate) {
        this.localeToNotificationTemplate = localeToNotificationTemplate;
    }

    /**
     * Get ignite event.
     *
     * @return igniteEvent
     */
    public IgniteEvent getIgniteEvent() {
        return igniteEvent;
    }

    /**
     * Set ignite event.
     *
     * @param igniteEvent igniteEvent
     */
    public void setIgniteEvent(IgniteEvent igniteEvent) {
        this.igniteEvent = igniteEvent;
    }

    /**
     * Get bench mode.
     *
     * @return benchMode
     */
    public int getBenchMode() {
        return benchMode;
    }

    /**
     * Set bench mode.
     *
     * @param benchMode benchMode
     */
    public void setBenchMode(int benchMode) {
        this.benchMode = benchMode;
    }

    /**
     * Get event ID.
     *
     * @return eventID
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public String getEventID() {
        return eventID;
    }

    /**
     * Set event ID.
     *
     * @param eventID eventID
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public void setEventID(String eventID) {
        this.eventID = eventID;
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
     * Get PDID.
     *
     * @return pdid
     */
    public String getPdid() {
        return pdid;
    }

    /**
     * Set PDID.
     *
     * @param pdid pdid
     */
    public void setPdid(String pdid) {
        this.pdid = pdid;
    }

    /**
     * Get notification config.
     *
     * @return notificationConfig
     */
    public NotificationConfig getNotificationConfig() {
        return notificationConfig;
    }

    /**
     * Set notification config.
     *
     * @param notificationConfig notificationConfig
     */
    public void setNotificationConfig(NotificationConfig notificationConfig) {
        this.notificationConfig = notificationConfig;
    }

    /**
     * get notification template.
     *
     * @return NotificationTemplate.
     */
    @JsonIgnore
    public NotificationTemplate getNotificationTemplate() {
        if (CollectionUtils.isEmpty(localeToNotificationTemplate)) {
            return null;
        }

        if (notificationConfig == null) {
            return CollectionUtils.isEmpty(this.notificationConfigs) ? null
                : localeToNotificationTemplate.get(notificationConfigs.get(0).getLocale());
        }

        return localeToNotificationTemplate.get(notificationConfig.getLocale());
    }

    /**
     * add notification template.
     *
     * @param locale               of template.
     * @param notificationTemplate template.
     */
    public void addNotificationTemplate(String locale, NotificationTemplate notificationTemplate) {
        if (CollectionUtils.isEmpty(this.localeToNotificationTemplate)) {
            this.localeToNotificationTemplate = new HashMap<>();
        }

        this.localeToNotificationTemplate.put(locale, notificationTemplate);
    }

    /**
     * get notification template config.
     *
     * @return NotificationTemplateConfig.
     */
    public NotificationTemplateConfig getNotificationTemplateConfig() {
        return notificationTemplateConfig;
    }

    /**
     * set notification template config.
     *
     * @param notificationTemplateConfig template config.
     */
    public void setNotificationTemplateConfig(NotificationTemplateConfig notificationTemplateConfig) {
        this.notificationTemplateConfig = notificationTemplateConfig;
    }

    /**
     * get notification configs.
     *
     * @return List of NotificationConfig.
     */
    public List<NotificationConfig> getNotificationConfigs() {
        return notificationConfigs;
    }

    /**
     * set notification configs.
     *
     * @param notificationConfigs list of notification configs.
     */
    public void setNotificationConfigs(List<NotificationConfig> notificationConfigs) {
        this.notificationConfigs = notificationConfigs;
    }

    /**
     * add notification config.
     *
     * @param notificationConfig notification config.
     */
    public void addNotificationConfig(NotificationConfig notificationConfig) {
        this.notificationConfigs.add(notificationConfig);
    }

    /**
     * get mute vehicle.
     *
     * @return MuteVehicle.
     */
    public MuteVehicle getMuteVehicle() {
        return muteVehicle;
    }

    /**
     * set mute vehicle.
     *
     * @param muteVehicle mute vehicle.
     */
    public void setMuteVehicle(MuteVehicle muteVehicle) {
        this.muteVehicle = muteVehicle;
    }

    /**
     * get all language templates.
     *
     * @return Set of NotificationTemplate.
     */
    public Set<NotificationTemplate> getAllLanguageTemplates() {
        return allLanguageTemplates;
    }

    /**
     * set all language templates.
     *
     * @param allLanguageTemplates set of notification templates.
     */
    public void setAllLanguageTemplates(Set<NotificationTemplate> allLanguageTemplates) {
        this.allLanguageTemplates = allLanguageTemplates;
    }

    /**
     * get locale to placeholders.
     *
     * @return Map of locale to placeholders.
     */
    public Map<String, Map<String, String>> getLocaleToPlaceholders() {
        return localeToPlaceholders;
    }

    /**
     * set locale to placeholders.
     *
     * @param localeToPlaceholders map of locale to placeholders.
     */
    public void setLocaleToPlaceholders(Map<String, Map<String, String>> localeToPlaceholders) {
        this.localeToPlaceholders = localeToPlaceholders;
    }

    /**
     * a set of all available ChannelTypes merged from all channels in the.
     * NotificationConfigs.
     *
     * @return Set of ChannelType
     */
    public Set<ChannelType> resolveAvailableChannelTypes() {
        if (getNotificationConfigs() == null) {
            return Collections.emptySet();
        }

        return getNotificationConfigs().stream()
            .flatMap(nc -> nc.getChannels().stream().map(Channel::getChannelType))
            .collect(toSet());
    }

    /**
     * get notification grouping.
     *
     * @return NotificationGrouping.
     */
    public NotificationGrouping getNotificationGrouping() {
        return notificationGrouping;
    }

    /**
     * set notification grouping.
     *
     * @param notificationGrouping notification grouping.
     */
    public void setNotificationGrouping(NotificationGrouping notificationGrouping) {
        this.notificationGrouping = notificationGrouping;
    }

    /**
     * alertData class.
     */
    @Entity(useDiscriminator = false)
    public static class Data {

        private Map<String, Object> alertDataProperties;
        @Transient
        private UserProfile userProfile;
        @Transient
        private VehicleProfileAbridged vehicleProfile;
        @Transient
        private String notificationId;
        @Transient
        private String marketingName;
        // filed added for DMportal Crash Alert Notifications.
        @Transient
        private String mqttTopic;

        /**
         * Data constructor.
         */
        public Data() {
            alertDataProperties = new HashMap<>();
        }

        /**
         * returns alertdata properties.
         *
         * @return map
         */
        @JsonAnyGetter
        public Map<String, Object> any() {
            return alertDataProperties;
        }

        /**
         * alertdata properties setter.
         *
         * @param name  property name
         * @param value property value
         */
        @JsonAnySetter
        public void set(String name, Object value) {
            alertDataProperties.put(name, value);
        }

        /**
         * get alert data properties.
         *
         * @return alertDataProperties
         */
        public Map<String, Object> getAlertDataProperties() {
            return alertDataProperties;
        }

        /**
         * set alert data properties.
         *
         * @param alertDataProperties alertDataProperties
         */
        public void setAlertDataProperties(Map<String, Object> alertDataProperties) {
            this.alertDataProperties = alertDataProperties;
        }

        /**
         * get user profile.
         *
         * @return userProfile
         */
        public UserProfile getUserProfile() {
            return userProfile;
        }

        /**
         * set user profile.
         *
         * @param userProfile userProfile
         */
        public void setUserProfile(UserProfile userProfile) {
            this.userProfile = userProfile;
        }

        /**
         * get notification id.
         *
         * @return notificationId
         */
        public String getNotificationId() {
            return notificationId;
        }

        /**
         * set notification id.
         *
         * @param notificationId notificationId
         */
        public void setNotificationId(String notificationId) {
            this.notificationId = notificationId;
        }

        /**
         * get vehicle profile.
         *
         * @return vehicleProfile
         */
        public VehicleProfileAbridged getVehicleProfile() {
            return vehicleProfile;
        }

        /**
         * set vehicle profile.
         *
         * @param vehicleProfile vehicleProfile
         */
        public void setVehicleProfile(VehicleProfileAbridged vehicleProfile) {
            this.vehicleProfile = vehicleProfile;
        }

        /**
         * get marketing name.
         *
         * @return marketingName
         */
        public String getMarketingName() {
            return marketingName;
        }

        /**
         * set marketing name.
         *
         * @param marketingName marketingName
         */
        public void setMarketingName(String marketingName) {
            this.marketingName = marketingName;
        }

        /**
         * get mqtt topic.
         *
         * @return mqttTopic
         */
        public String getMqttTopic() {
            return mqttTopic;
        }

        /**
         * set mqtt topic.
         *
         * @param mqttTopic mqttTopic
         */
        public void setMqttTopic(String mqttTopic) {
            this.mqttTopic = mqttTopic;
        }

        /**
         * To string.
         *
         * @return string
         */
        @Override
        public String toString() {
            return "Data [alertDataProperties=" + alertDataProperties + ", userProfile=" + userProfile
                + ", vehicleProfile=" + vehicleProfile + ", notificationId=" + notificationId + ", marketingName="
                + marketingName + ", mqttTopic=" + mqttTopic + "]";
        }

    }

    /**
     * To string.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "AlertsInfo [igniteEvent=" + igniteEvent + ", notificationConfig=" + notificationConfig
            + ", notificationGrouping=" + notificationGrouping + ", localeToNotificationTemplate="
            + localeToNotificationTemplate + ", notificationTemplateConfig="
            + notificationTemplateConfig + ", eventID=" + eventID
            + ", alertsData=" + alertsData + ", version=" + version
            + ", benchMode=" + benchMode + ", timestamp=" + timestamp
            + ", timezone=" + timezone + ", pdid=" + pdid + ", allLanguageTemplates=" + allLanguageTemplates
            + ", localeToPlaceholders=" + localeToPlaceholders + "]";
    }

}
