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
import dev.morphia.annotations.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;
import org.eclipse.ecsp.utils.Constants;
import org.eclipse.ecsp.utils.Utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * UserProfile class.
 */
@Entity(value = NotificationDaoConstants.USER_PREF_COLLECTION_NAME)
public class UserProfile extends AbstractIgniteEntity implements Serializable {


    /**
     * DEFAULT_USER_TIME_ZONE.
     */
    public static final String DEFAULT_USER_TIME_ZONE = "UTC";
    private static final long serialVersionUID = 4061910684472541493L;

    @Id
    private String userId;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String firstName;
    private String lastName;
    @NotNull(message = Constants.VALIDATION_MESSAGE)
    private Locale locale;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String defaultEmail;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String defaultPhoneNumber;
    private boolean consent;
    private Set<NickName> nickNames;
    private String timeZone;
    private long lastModifiedTime;
    private Map<String, Object> customAttributes;

    /**
     * UserProfile constructor.
     */
    public UserProfile() {
        this(null);

    }

    /**
     * UserProfile constructor.
     *
     * @param userId String
     */
    public UserProfile(String userId) {
        this.userId = userId;
        nickNames = new HashSet<>();
        timeZone = DEFAULT_USER_TIME_ZONE;
    }

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
     * Getter for FirstName.
     *
     * @return firstname
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setter for FirstName.
     *
     * @param firstName the new value
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Getter for LastName.
     *
     * @return lastname
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setter for LastName.
     *
     * @param lastName the new value
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Getter for Locale.
     *
     * @return locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Setter for Locale.
     *
     * @param language the new value
     */
    public void setLocale(Locale language) {
        this.locale = language;
    }

    /**
     * Getter for DefaultEmail.
     *
     * @return defaultemail
     */
    public String getDefaultEmail() {
        return defaultEmail;
    }

    /**
     * Setter for DefaultEmail.
     *
     * @param defaultEmail the new value
     */
    public void setDefaultEmail(String defaultEmail) {
        this.defaultEmail = defaultEmail;
    }

    /**
     * Getter for DefaultPhoneNumber.
     *
     * @return defaultphonenumber
     */
    public String getDefaultPhoneNumber() {
        return defaultPhoneNumber;
    }

    /**
     * Setter for DefaultPhoneNumber.
     *
     * @param defaultPhone the new value
     */
    public void setDefaultPhoneNumber(String defaultPhone) {
        this.defaultPhoneNumber = defaultPhone;
    }

    /**
     * Getter for Consent.
     *
     * @return consent
     */
    public boolean isConsent() {
        return consent;
    }

    /**
     * Setter for Consent.
     *
     * @param consent the new value
     */
    public void setConsent(boolean consent) {
        this.consent = consent;
    }

    /**
     * Getter for NickNames.
     *
     * @return nicknames
     */
    public Set<NickName> getNickNames() {
        return nickNames;
    }

    /**
     * Setter for NickNames.
     *
     * @param nickNames the new value
     */
    public void setNickNames(Set<NickName> nickNames) {
        this.nickNames = nickNames;
    }

    public void addNickName(NickName nickName) {
        nickNames.remove(nickName);
        nickNames.add(nickName);
    }

    public void removeNickNames(String vehicleId) {
        nickNames.removeIf(n -> n.getVehicleId().equals(vehicleId));
    }

    /**
     * Method to get nickname of a vehicle.
     *
     * @param vehicleId String.
     *
     * @return nickname string.
     */
    public String getNickName(String vehicleId) {
        String nickName = null;
        for (NickName name : nickNames) {
            if (name.getVehicleId().equals(vehicleId)) {
                nickName = name.getNickName();
            }
        }
        return nickName;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    /**
     * Equals method.
     *
     * @param obj Object
     *
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
        UserProfile other = (UserProfile) obj;
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    /**
     * Getter for TimeZone.
     *
     * @return timezone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Setter for TimeZone.
     *
     * @param timeZone the new value
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Getter for LastModifiedTime.
     *
     * @return lastmodifiedtime
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Setter for LastModifiedTime.
     *
     * @param lastModifiedTime the new value
     */
    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    /**
     * Setter for CustomAttributes.
     *
     * @param customAttributes the new value
     */
    public void setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
    }

    /**
     * To String.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "UserProfile{"
            + "userId='" + userId + '\'' + ", firstName='" + Utils.maskString(firstName) + '\''
            + ", lastName='" + Utils.maskString(lastName) + '\''
            + ", locale=" + locale + ", defaultEmail='" + Utils.maskString(defaultEmail) + '\''
            + ", defaultPhoneNumber='" + Utils.maskString(defaultPhoneNumber) + '\''
            + ", consent=" + consent + ", nickNames=" + nickNames
            + ", timeZone='" + timeZone + '\'' + ", lastModifiedTime="
            + lastModifiedTime + ", customAttributes=" + customAttributes + '}';
    }
}