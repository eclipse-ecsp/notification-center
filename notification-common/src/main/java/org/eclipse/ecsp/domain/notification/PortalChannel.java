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
import org.eclipse.ecsp.domain.notification.commons.ChannelType;

import java.util.List;

/**
 * PortalChannel class.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.PortalChannel")
public class PortalChannel extends Channel {

    private List<String> mqttTopics;

    /**
     * Getter for MqttTopics.
     *
     * @return mqtttopics
     */
    public List<String> getMqttTopics() {
        return mqttTopics;
    }

    /**
     * Setter for MqttTopics.
     *
     * @param mqttTopics the new value
     */
    public void setMqttTopics(List<String> mqttTopics) {
        this.mqttTopics = mqttTopics;
    }


    /**
     * PortalChannel constructor.
     */
    public PortalChannel() {
        setType(ChannelType.PORTAL.getChannelType());
    }

    /**
     * PortalChannel constructor.
     *
     * @param mqttTopics mqtttopics
     */
    public PortalChannel(List<String> mqttTopics) {
        this();
        setMqttTopics(mqttTopics);
    }


    /**
     * Getter for ChannelType.
     *
     * @return channeltype
     */
    @Override
    public ChannelType getChannelType() {
        return ChannelType.PORTAL;
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
    }

    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "PortalChannel [toString()=" + super.toString() + "]";
    }

    /**
     * RequiresSetup method.
     *
     * @return boolean
     */
    @Override
    public boolean requiresSetup() {
        return false;
    }

    /**
     * Flatten method.
     *
     * @return List
     */
    @Override
    public List<Channel> flatten() {
        return null;
    }

    /**
     * ShallowClone method.
     *
     * @return Channel
     */
    @Override
    public Channel shallowClone() {
        PortalChannel clone = new PortalChannel();
        super.populateClone(clone);
        return clone;
    }

}
