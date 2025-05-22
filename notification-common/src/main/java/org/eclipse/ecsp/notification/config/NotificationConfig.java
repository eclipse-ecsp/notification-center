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

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * NotificationConfig entity class.
 */
@Entity(value = NotificationDaoConstants.NOTIFICATION_CONFIG_COLLECTION_NAME, discriminatorKey = "className",
    discriminator = "org.eclipse.ecsp.notification.config.NotificationConfig")
@Indexes({
    @Index(fields = {@Field(value = NotificationDaoConstants.USERID_FIELD),
        @Field(value = NotificationDaoConstants.VEHICLEID_FIELD),
        @Field(value = NotificationDaoConstants.GROUP_FIELD)},
        options = @IndexOptions(name = "NotificationConfig_Index_1"))})
public class NotificationConfig extends AbstractIgniteEntity implements Serializable {
    public static final String USER_ID_FOR_DEFAULT_PREFERENCE = "GENERAL";
    public static final String VEHICLE_ID_FOR_DEFAULT_PREFERENCE = "GENERAL";
    public static final String CONTACT_ID_FOR_DEFAULT_PREFERENCE = "self";
    private static final long serialVersionUID = -5449527514851409827L;

    @Id
    private String id;
    private String userId;
    private String vehicleId;
    private String contactId;
    private String email;
    private String phoneNumber;
    private String group;
    private boolean enabled;
    private List<Channel> channels;
    private String locale;
    private String brand;

    public NotificationConfig() {
        channels = new ArrayList<>();
    }

    /**
     * NotificationConfig constructor.
     *
     * @param userId userId
     *
     * @param vehicleId vehicleId
     *
     * @param contactId contactId
     *
     * @param ncr NotificationConfigRequest
     */
    public NotificationConfig(String userId, String vehicleId, String contactId, NotificationConfigRequest ncr) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.contactId = contactId;
        this.group = ncr.getGroup();
        this.enabled = ncr.isEnabled();
        this.channels = ncr.getChannels();
        this.brand = ncr.getBrand();
    }

    /**
     * get defaultNotificationConfig.
     *
     * @param ncr NotificationConfigRequest
     *
     * @param defaultBrand default
     *
     * @return NotificationConfig
     */
    public static NotificationConfig defaultNotificationConfig(NotificationConfigRequest ncr, String defaultBrand) {
        NotificationConfig notificationConfig =
            new NotificationConfig(USER_ID_FOR_DEFAULT_PREFERENCE, VEHICLE_ID_FOR_DEFAULT_PREFERENCE, "self", ncr);
        if (StringUtils.isEmpty(ncr.getBrand())) {
            notificationConfig.setBrand(defaultBrand);
        }
        return notificationConfig;
    }

    /**
     * Get locale.
     *
     * @return locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Set locale.
     *
     * @param locale locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Get userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get vehicleId.
     *
     * @return vehicleId
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Set vehicleId.
     *
     * @param vehicleId vehicleId
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Get group.
     *
     * @return group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set group.
     *
     * @param group group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Get enabled.
     *
     * @return enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set enabled.
     *
     * @param enabled enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get channels.
     *
     * @return channels
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * Set channels.
     *
     * @param channels channels
     */
    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    /**
     * addChannel.
     *
     * @param channel channel
     */
    public void addChannel(Channel channel) {
        if (this.channels == null) {
            this.channels = new ArrayList<>();
        }

        channels.add(channel);
    }

    private void addIfAbsent(Channel newChannel) {
        if (newChannel == null || StringUtils.isEmpty(newChannel.getType())) {
            return;
        }

        for (Channel channel : channels) {
            if (channel.getClass().equals(newChannel.getClass())) {
                return;
            }
        }
        this.addChannel(newChannel);
    }

    /**
     * addAll channel IfAbsent.
     *
     * @param newChannels channels
     */
    public void addAllIfAbsent(List<Channel> newChannels) {
        if (CollectionUtils.isEmpty(newChannels)) {
            return;
        }

        if (CollectionUtils.isEmpty(this.channels)) {
            this.setChannels(newChannels);
        } else {
            newChannels.forEach(this::addIfAbsent);
        }
    }

    /**
     * compare the config.
     *
     * @param current config
     *
     * @param other config
     *
     * @return boolean
     */
    public static boolean isSimilar(NotificationConfig current, NotificationConfig other) {
        return Objects.equals(current.getGroup(), other.getGroup())
            && Objects.equals(current.getUserId(), other.getUserId())
            && Objects.equals(current.getVehicleId(), other.getVehicleId())
            && Objects.equals(current.getBrand(), other.getBrand())
            && Objects.equals(current.getContactId(), other.getContactId());
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
     * Get contactId.
     *
     * @return contactId
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * Set contactId.
     *
     * @param contactId contactId
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    /**
     * Get email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set email.
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get phoneNumber.
     *
     * @return phoneNumber
     */
    private String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Set phoneNumber.
     *
     * @param phoneNumber phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Get brand.
     *
     * @return brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Set brand.
     *
     * @param brand brand
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Hashcode.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * equals.
     *
     * @param obj Object
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotificationConfig other = (NotificationConfig) obj;
        if (id == null) {
            return other.id == null;
        } else {
            return id.equals(other.id);
        }
    }

    /**
     * toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "NotificationConfig [id=" + id + ", userId=" + userId + ", vehicleId=" + vehicleId
            + ", contactId=" + contactId + ", group="
            + group + ", enabled=" + enabled + ", channels=" + channels + ", locale=" + locale
            + ", brand=" + brand + "]";
    }

    /**
     * getEnabledChannels.
     *
     * @return List
     */
    @JsonIgnore
    public List<Channel> getEnabledChannels() {
        return channels.stream().filter(Channel::getEnabled).collect(Collectors.toList());
    }

    /**
     * disable all other channels except channel specified.
     *
     * @param channel channel type (sms, email, ...)
     */
    public void disableChannelsExcept(ChannelType channel) {
        channels.stream().filter(existingChannel -> !existingChannel.getChannelType().equals(channel))
            .forEach(c -> c.setEnabled(false));
    }

    /**
     * diff channels.
     *
     * @param another config
     *
     * @param deletions channels
     *
     * @param additions channels
     */
    public void diffChannels(NotificationConfig another, List<Channel> deletions, List<Channel> additions) {
        for (Channel mine : channels) {
            for (Channel others : another.channels) {
                if (mine.getClass().equals(others.getClass())) {
                    mine.diff(others, deletions, additions);
                }
            }
        }
    }

    /**
     * Adds new channels not present in existing config. Also replaces existing
     * channel with what is in new config for same channel type.
     *
     * @param nc - new config
     */
    public void patch(NotificationConfig nc) {
        setEnabled(nc.isEnabled());
        for (Channel newOne : nc.getChannels()) {
            boolean found = false;
            ListIterator<Channel> existingIter = channels.listIterator();
            // replace existing config for the channel with the new config
            while (existingIter.hasNext()) {
                Channel existing = existingIter.next();
                if (existing.getClass().equals(newOne.getClass())) {
                    found = true;
                    existing.merge(newOne);
                    break;
                }
            }
            // add new channels not present in existing config
            if (!found) {
                existingIter.add(newOne);
            }
        }
    }

    /**
     * getFlattenedChannels.
     *
     * @return List
     */
    @JsonIgnore
    List<Channel> getFlattenedChannels() {
        List<Channel> flattenedChannels = new ArrayList<>();
        for (Channel ch : channels) {
            if (ch.requiresSetup()) {
                flattenedChannels.addAll(ch.flatten());
            }
        }
        return flattenedChannels;
    }

    /**
     * getChannel.
     *
     * @param channelType channelType
     *
     * @param <T> Channel
     *
     * @return Channel
     */
    @JsonIgnore
    public <T extends Channel> T getChannel(ChannelType channelType) {
        return (T) channels.stream().filter(c -> channelType.equals(c.getChannelType())).findFirst().orElse(null);
    }

    public void removeChannel(ChannelType type) {
        channels.removeIf(channel -> channel.getChannelType().equals(type));
    }

    /**
     * deepClone config.
     *
     * @return NotificationConfig
     */
    public NotificationConfig deepClone() {
        NotificationConfig clone = new NotificationConfig();
        clone.setChannels(channels.stream().map(Channel::shallowClone).collect(Collectors.toList()));
        clone.setContactId(getContactId());
        clone.setEmail(getEmail());
        clone.setEnabled(isEnabled());
        clone.setGroup(getGroup());
        clone.setId(getId());
        clone.setPhoneNumber(getPhoneNumber());
        clone.setSchemaVersion(getSchemaVersion());
        clone.setUserId(getUserId());
        clone.setVehicleId(getVehicleId());
        clone.setLocale(getLocale());
        clone.setBrand(getBrand());
        return clone;
    }
}
