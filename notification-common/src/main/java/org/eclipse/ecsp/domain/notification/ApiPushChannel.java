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
 * ApiPushChannel class.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.ApiPushChannel")
public class ApiPushChannel extends Channel {



    /**
     * This method is a getter for channeltype.
     *
     * @return ChannelType
     */
    @Override
    public ChannelType getChannelType() {
        return ChannelType.API_PUSH;
    }

    /**
     * To String.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "ApiPushChannel [toString()=" + super.toString() + "]";
    }

    /**
     * Diff.
     *
     * @param others Channel
     * @param deletions List
     * @param additions List
     */
    @Override
    public void diff(Channel others, List<Channel> deletions, List<Channel> additions) {
        return;
    }

    /**
     * Requires Setup.
     *
     * @return boolean
     */
    @Override
    public boolean requiresSetup() {
        return false;
    }

    /**
     * Flatten.
     *
     * @return List
     */
    @Override
    public List<Channel> flatten() {
        return null;
    }

    /**
     * Shallow Clone.
     *
     * @return Channel
     */
    @Override
    public Channel shallowClone() {
        ApiPushChannel clone = new ApiPushChannel();
        super.populateClone(clone);
        return clone;
    }

}