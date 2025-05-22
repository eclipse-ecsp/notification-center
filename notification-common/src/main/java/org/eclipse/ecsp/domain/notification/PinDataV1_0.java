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

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.AbstractEventData;

import java.util.List;

/**
 * PinDataV1_0 eventdata class for PinGenerated event.
 */
@SuppressWarnings("checkstyle:TypeName")
@EventMapping(id = EventID.PIN_GENERATED, version = Version.V1_0)
public class PinDataV1_0 extends AbstractEventData {
    private static final long serialVersionUID = 7739753996146783504L;
    private String pin;
    private List<String> emails;
    private List<String> phones;
    private String imei;
    private String duration;
    private String productName;

    /**
     * Getter for Pin.
     *
     * @return pin
     */
    public String getPin() {
        return pin;
    }

    /**
     * Setter for Pin.
     *
     * @param pin the new value
     */
    public void setPin(String pin) {
        this.pin = pin;
    }

    /**
     * Getter for Emails.
     *
     * @return emails
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Setter for Emails.
     *
     * @param emails the new value
     */
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    /**
     * Getter for Phones.
     *
     * @return phones
     */
    public List<String> getPhones() {
        return phones;
    }

    /**
     * Setter for Phones.
     *
     * @param phones the new value
     */
    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    /**
     * Getter for Imei.
     *
     * @return imei
     */
    public String getImei() {
        return imei;
    }

    /**
     * Setter for Imei.
     *
     * @param imei the new value
     */
    public void setImei(String imei) {
        this.imei = imei;
    }

    /**
     * Getter for Duration.
     *
     * @return duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Setter for Duration.
     *
     * @param duration the new value
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Getter for ProductName.
     *
     * @return productname
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Setter for ProductName.
     *
     * @param productName the new value
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }
}
