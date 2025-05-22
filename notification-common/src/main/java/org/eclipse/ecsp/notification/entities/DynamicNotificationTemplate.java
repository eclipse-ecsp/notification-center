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

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.ecsp.notification.dao.NotificationDaoConstants.NOTIFICATION_TEMPLATES_COLLECTION_NAME;

/**
 * DynamicNotificationTemplate entity.
 */
@Entity(value = NOTIFICATION_TEMPLATES_COLLECTION_NAME, useDiscriminator = false)
public class DynamicNotificationTemplate extends AbstractIgniteEntity implements PlaceholderContainer {
    @Id
    private String id;
    private String notificationId;
    private String notificationShortName;
    private String notificationLongName;
    private String locale;
    private String brand;
    private Set<String> customPlaceholders;
    private List<AdditionalLookupProperty> additionalLookupProperties;
    private Map<String, Map<String, Object>> channelTemplates;

    /**
     * This method is a getter for id.
     *
     * @return String
     */

    public String getId() {
        return id;
    }

    /**
     * This method is a setter for id.
     *
     * @param id : String
     */

    public void setId(String id) {
        this.id = id;
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
     * This method is a getter for notificationshortname.
     *
     * @return String
     */

    public String getNotificationShortName() {
        return notificationShortName;
    }

    /**
     * This method is a setter for notificationshortname.
     *
     * @param notificationShortName : String
     */

    public void setNotificationShortName(String notificationShortName) {
        this.notificationShortName = notificationShortName;
    }

    /**
     * This method is a getter for notificationlongname.
     *
     * @return String
     */

    public String getNotificationLongName() {
        return notificationLongName;
    }

    /**
     * This method is a setter for notificationlongname.
     *
     * @param notificationLongName : String
     */

    public void setNotificationLongName(String notificationLongName) {
        this.notificationLongName = notificationLongName;
    }

    /**
     * This method is a getter for locale.
     *
     * @return String
     */

    public String getLocale() {
        return locale;
    }

    /**
     * This method is a setter for locale.
     *
     * @param locale : String
     */

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * This method is a getter for brand.
     *
     * @return String
     */

    public String getBrand() {
        return brand;
    }

    /**
     * This method is a setter for brand.
     *
     * @param brand : String
     */

    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * This method is a getter for customplaceholders.
     *
     * @return Set
     */

    public Set<String> getCustomPlaceholders() {
        return customPlaceholders;
    }

    /**
     * This method is a setter for customplaceholders.
     *
     * @param customPlaceholders : Set
     */

    public void setCustomPlaceholders(Set<String> customPlaceholders) {
        this.customPlaceholders = customPlaceholders;
    }

    /**
     * This method is a getter for channeltemplates.
     *
     * @return Map
     */

    public Map<String, Map<String, Object>> getChannelTemplates() {
        return channelTemplates;
    }

    /**
     * This method is a setter for channeltemplates.
     *
     * @param channelTemplates : Map
     */

    public void setChannelTemplates(Map<String, Map<String, Object>> channelTemplates) {
        this.channelTemplates = channelTemplates;
    }

    /**
     * This method is a getter for additionallookupproperties.
     *
     * @return List
     */

    public List<AdditionalLookupProperty> getAdditionalLookupProperties() {
        return additionalLookupProperties;
    }

    /**
     * This method is a setter for additionallookupproperties.
     *
     * @param additionalLookupProperties : List
     */

    public void setAdditionalLookupProperties(List<AdditionalLookupProperty> additionalLookupProperties) {
        this.additionalLookupProperties = additionalLookupProperties;
    }

    /**
     * addAttributeToChannel.
     *
     * @param channel channel
     *
     * @param attribute attribute
     *
     * @param value value
     *
     * @throws Exception if channel or attribute is empty
     */
    public void addAttributeToChannel(String channel, String attribute, Object value) throws Exception {
        if (StringUtils.isEmpty(channel) || StringUtils.isEmpty(attribute)) {
            throw new Exception("Channel and attribute must not be empty");
        }

        channel = channel.toLowerCase();
        attribute = attribute.toLowerCase();

        if (channelTemplates == null) {
            channelTemplates = new HashMap<>();
        }

        Map<String, Object> currentChannel = channelTemplates.get(channel);
        if (currentChannel == null) {
            Map<String, Object> newAttribute = new HashMap<>();
            newAttribute.put(attribute, value);
            channelTemplates.put(channel, newAttribute);
        } else {
            currentChannel.put(attribute, value);
        }
    }
}