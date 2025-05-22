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
import org.eclipse.ecsp.domain.notification.Channel;

import java.util.List;

/**
 * CloneNotificationConfig class.
 */
@Entity(useDiscriminator = false)
public class CloneNotificationConfig {

    private String id;
    private String userId;
    private String vehicleId;
    private String email;
    private String contactId;
    private String phoneNumber;
    private String group;
    private List<Channel> channels;
    private boolean enabled;
    private String locale;

    /**
     * CloneNotificationConfig constructor.
     *
     * @param email email
     *
     * @param phoneNumber phone
     *
     * @param channels channels list
     *
     * @param contactId contactId
     */
    public CloneNotificationConfig(String email, String phoneNumber, List<Channel> channels, String contactId) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.channels = channels;
        this.contactId = contactId;
    }

    /**
     * Constructor.
     */
    public CloneNotificationConfig() {
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
     * This method is a getter for userid.
     *
     * @return String
     */

    public String getUserId() {
        return userId;
    }

    /**
     * This method is a setter for userid.
     *
     * @param userId : String
     */

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * This method is a getter for vehicleid.
     *
     * @return String
     */

    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * This method is a setter for vehicleid.
     *
     * @param vehicleId : String
     */

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * This method is a getter for contactid.
     *
     * @return String
     */

    public String getContactId() {
        return contactId;
    }

    /**
     * This method is a setter for contactid.
     *
     * @param contactId : String
     */

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    /**
     * This method is a getter for email.
     *
     * @return String
     */

    public String getEmail() {
        return email;
    }

    /**
     * This method is a setter for email.
     *
     * @param email : String
     */

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * This method is a getter for phonenumber.
     *
     * @return String
     */

    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * This method is a setter for phonenumber.
     *
     * @param phoneNumber : String
     */

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * This method is a getter for channels.
     *
     * @return List
     */

    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * This method is a setter for channels.
     *
     * @param channels : List
     */

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    /**
     * This method is a getter for group.
     *
     * @return String
     */

    public String getGroup() {
        return group;
    }

    /**
     * This method is a setter for group.
     *
     * @param group : String
     */

    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * This method is a getter for enabled.
     *
     * @return boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * This method is a setter for enabled.
     *
     * @param enabled : boolean
     */

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "CloneNotificationConfig{" + "id='" + id + '\''
            + ", userId='" + userId + '\'' + ", vehicleId='" + vehicleId + '\''
            + ", contactId='" + contactId + '\'' + ", email='" + email + '\''
            + ", phoneNumber='" + phoneNumber + '\'' + ", group='" + group + '\''
            + ", enabled=" + enabled + ", channels=" + channels + ", locale=" + locale + '}';
    }
}