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

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.utils.Constants;
import org.eclipse.ecsp.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entitly class for SmsChannel.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.SmsChannel")
public class SmsChannel extends Channel {
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    @JsonProperty("phones")
    private List<String> phones;

    /**
     * SmsChannel constructor.
     */
    public SmsChannel() {
        setType(ChannelType.SMS.getChannelType());
    }

    /**
     * SmsChannel constructor.
     *
     * @param phones phonenumbers
     */
    public SmsChannel(List<String> phones) {
        this();
        this.phones = phones;
    }

    /**
     * SmsChannel constructor.
     *
     * @param phone phonenumbers
     */
    public SmsChannel(String... phone) {
        this();
        phones = new ArrayList<>();
        Collections.addAll(phones, phone);
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
        this.phones = new ArrayList<>(phones);
    }


    /**
     * Getter for ChannelType.
     *
     * @return channeltype
     */
    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }


    /**
     * HashCode method.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((phones == null) ? 0 : phones.hashCode());
        return result;
    }

    /**
     * Equals method.
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
        SmsChannel other = (SmsChannel) obj;
        if (phones == null) {
            return other.phones == null;
        }

        return phones.equals(other.phones);
    }

    private List<String> getMaskedPhones() {
        List<String> maskedPhs = new ArrayList<>();
        if (phones != null && !phones.isEmpty()) {
            phones.forEach(e -> maskedPhs.add(Utils.maskString(e)));
        }
        return maskedPhs;
    }

    /**
     * To string method.
     *
     * @return string
     */
    @Override
    public String toString() {

        return "SmsChannel [phones=" + getMaskedPhones() + ", toString()=" + super.toString() + "]";
    }

    /**
     * Diff method.
     *
     * @param others Channel
     * @param deletions List
     * @param additions List
     */
    @Override
    public void diff(Channel others, List<Channel> deletions, List<Channel> additions) {
        SmsChannel otherSms = (SmsChannel) others;
        for (String phone : phones) {
            if (!otherSms.getPhones().contains(phone)) {
                deletions.add(new SmsChannel(Collections.singletonList(phone)));
            }
        }
        for (String phone : otherSms.getPhones()) {
            if (!phones.contains(phone)) {
                additions.add(new SmsChannel(Collections.singletonList(phone)));
            }
        }
    }

    /**
     * RequiresSetup method.
     *
     * @return boolean
     */
    @Override
    public boolean requiresSetup() {
        return true;
    }

    /**
     * Flatten method.
     *
     * @return List
     */
    @Override
    public List<Channel> flatten() {
        List<Channel> ret = new ArrayList<>();

        // Update config api allows to create SMS channel without phones
        // collection, therefore it might be null.
        if (phones == null) {
            return ret;
        }
        for (String phone : phones) {
            ret.add(new SmsChannel(Collections.singletonList(phone)));
        }
        return ret;
    }

    /**
     * Merge method.
     *
     * @param channel Channel
     */
    @Override
    public void merge(Channel channel) {
        super.merge(channel);
        this.phones = ((SmsChannel) channel).phones;
    }

    /**
     * ShallowClone method.
     *
     * @return Channel
     */
    @Override
    public Channel shallowClone() {
        SmsChannel clone = new SmsChannel();
        if (getPhones() != null) {
            clone.setPhones(new ArrayList<>(getPhones()));
        }
        super.populateClone(clone);
        return clone;
    }

}
