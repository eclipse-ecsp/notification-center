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

package org.eclipse.ecsp.platform.notification.dto;

import org.eclipse.ecsp.notification.config.NotificationConfig;

import java.util.List;
import java.util.Locale;

/**
 * NotificationConfigResponse class.
 */
public class NotificationConfigResponse {
    private String contactId;
    private String email;
    private String phoneNumber;
    private String contactName;
    private Locale locale;
    private List<NotificationConfig> preferences;

    /**
     * Get the contact id.
     *
     * @return contactId
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * Set the contact id.
     *
     * @param contactId contactId
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    /**
     * Get the contact name.
     *
     * @return contactName
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * Set the contact name.
     *
     * @param contactName contactName
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * Get the locale.
     *
     * @return locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Set the locale.
     *
     * @param locale locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Get the preferences.
     *
     * @return preferences
     */
    public List<NotificationConfig> getPreferences() {
        return preferences;
    }

    /**
     * Set the preferences.
     *
     * @param preferences preferences
     */
    public void setPreferences(List<NotificationConfig> preferences) {
        this.preferences = preferences;
    }

    /**
     * Get the email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email.
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the phone number.
     *
     * @return phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Set the phone number.
     *
     * @param phoneNumber phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
