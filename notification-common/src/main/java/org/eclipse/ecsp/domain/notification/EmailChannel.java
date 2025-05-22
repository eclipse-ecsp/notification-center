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
 * EmailChannel class.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.EmailChannel")
public class EmailChannel extends Channel {
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    @JsonProperty("emails")
    private List<String> emails;

    /**
     * Constructor.
     */
    public EmailChannel() {
        setType(ChannelType.EMAIL.getChannelType());
    }

    /**
     * Constructor.
     *
     * @param emails List
     */
    public EmailChannel(List<String> emails) {
        this();
        setEmails(emails);
    }

    /**
     * Getter for emails.
     *
     * @return List
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Setter for emails.
     *
     * @param emails List
     */
    public void setEmails(List<String> emails) {
        this.emails = new ArrayList<>(emails);
    }

    /**
     * This method is a getter for type.
     *
     * @return String
     */
    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((emails == null) ? 0 : emails.hashCode());
        return result;
    }

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
        EmailChannel other = (EmailChannel) obj;
        if (emails == null) {
            return other.emails == null;
        }

        return emails.equals(other.emails);
    }

    private List<String> getMaskedEmails() {
        List<String> maskedEmails = new ArrayList<>();
        if (emails != null && !emails.isEmpty()) {
            emails.forEach(e -> maskedEmails.add(Utils.maskString(e)));
        }
        return maskedEmails;
    }

    @Override
    public String toString() {
        return "EmailChannel [emails=" + getMaskedEmails() + ", toString()=" + super.toString() + "]";
    }

    /**
     * diff .
     *
     *
     */
    @Override
    public void diff(Channel others, List<Channel> deletions, List<Channel> additions) {
        EmailChannel otherEmail = (EmailChannel) others;
        for (String email : emails) {
            if (!otherEmail.getEmails().contains(email)) {
                deletions.add(new EmailChannel(Collections.singletonList(email)));
            }
        }
        for (String email : otherEmail.getEmails()) {
            if (!emails.contains(email)) {
                additions.add(new EmailChannel(Collections.singletonList(email)));
            }
        }
    }

    /**
     * requiresSetup.
     *
     * @return boolean
     */
    @Override
    public boolean requiresSetup() {
        return false;
    }

    /**
     * flatten.
     *
     * @return List
     */
    @Override
    public List<Channel> flatten() {
        List<Channel> ret = new ArrayList<>();

        // Update config api allows to create Email channel without email
        // collection, therefore it might be null.
        if (emails == null) {
            return ret;
        }

        for (String email : emails) {
            ret.add(new EmailChannel(Collections.singletonList(email)));
        }
        return ret;
    }

    /**
     * merge.
     *
     * @param channel Channel
     */
    @Override
    public void merge(Channel channel) {
        super.merge(channel);
        this.emails = ((EmailChannel) channel).emails;
    }

    /**
     * shallowClone.
     *
     * @return Channel
     */
    @Override
    public Channel shallowClone() {
        EmailChannel clone = new EmailChannel();
        if (getEmails() != null) {
            clone.setEmails(new ArrayList<>(getEmails()));
        }
        super.populateClone(clone);
        return clone;
    }
}
