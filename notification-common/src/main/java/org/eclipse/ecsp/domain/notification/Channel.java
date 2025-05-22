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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.morphia.annotations.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * channel abstract class.
 */
@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type", visible = true)
@JsonSubTypes({
    @Type(value = PushChannel.class, name = "push"),
    @Type(value = EmailChannel.class, name = "email"),
    @Type(value = SmsChannel.class, name = "sms"),
    @Type(value = PortalChannel.class, name = "portal"),
    @Type(value = ApiPushChannel.class, name = "apiPush"),
    @Type(value = IVMChannel.class, name = "ivm")
})
public abstract class Channel implements Serializable {
    private static final long serialVersionUID = -4969339686745666842L;
    private static final String CHANNEL_TYPE = "email|sms|portal|ivm|push|apiPus";
    private static final String INVALID_VALUE = "Invalid value";
    @Pattern(regexp = CHANNEL_TYPE + "h", message = INVALID_VALUE)
    private String type;
    @NotNull(message = Constants.VALIDATION_MESSAGE)
    private boolean enabled;
    private List<SuppressionConfig> suppressionConfigs = new ArrayList<>();

    /**
     * This method is a setter for type.
     *
     * @param type : String
     */

    public void setType(String type) {
        this.type = type;
    }

    /**
     * This method is a getter for type.
     *
     * @return String
     */

    public String getType() {
        return type;
    }

    /**
     * This method is a setter for enabled.
     *
     * @param enabled : boolean
     */

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * This method is a getter for enabled.
     *
     * @return boolean
     */

    public boolean getEnabled() {
        return enabled;
    }

    /**
     * This method is a getter for suppressionconfigs.
     *
     * @return List
     */
    public List<SuppressionConfig> getSuppressionConfigs() {
        return suppressionConfigs;
    }

    /**
     * This method is a setter for suppressionconfigs.
     *
     * @param suppressionConfigs : List
     */
    public void setSuppressionConfigs(List<SuppressionConfig> suppressionConfigs) {
        this.suppressionConfigs = suppressionConfigs;
    }



    /**
     * This method is a getter for channeltype.
     *
     * @return ChannelType
     */
    @JsonIgnore
    public abstract ChannelType getChannelType();

    @Override
    public String toString() {
        return "Channel [type=" + type + ", enabled=" + enabled + ", suppressionConfigs=" + suppressionConfigs + "]";
    }

    /**
     * Diff.
     *
     * @param others     others
     * @param deletions  deletions
     * @param additions  additions
     */
    public abstract void diff(Channel others, List<Channel> deletions, List<Channel> additions);

    /**
     * If true, this channel requires setting up to be done. For SNS topic
     * creation
     *
     * @return boolean
     */
    public abstract boolean requiresSetup();

    /**
     * Returns a flattened list of Channel with just one touch point in each
     * channel. So an email channel config with 3 email addresses would result
     * in 3 Channel instances
     *
     * @return List
     */
    public abstract List<Channel> flatten();

    /**
     * Merge the channel.
     *
     * @param channel channel
     */
    public void merge(Channel channel) {
        this.setEnabled(channel.getEnabled());
        this.setSuppressionConfigs(channel.getSuppressionConfigs());
    }

    /**
     * Shallow clone.
     *
     * @return Channel
     */
    public abstract Channel shallowClone();

    /**
     * clone channel.
     *
     * @param clone channel
     */
    public void populateClone(Channel clone) {
        clone.setEnabled(getEnabled());
        clone.setSuppressionConfigs(new ArrayList<>(getSuppressionConfigs()));
        clone.setType(getType());
    }
}