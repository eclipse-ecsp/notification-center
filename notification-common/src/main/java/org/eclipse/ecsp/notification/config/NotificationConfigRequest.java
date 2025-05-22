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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.utils.Constants;

import java.util.List;

/**
 * NotificationConfigRequest class.
 */
public class NotificationConfigRequest {
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String group;
    @NotNull(message = Constants.VALIDATION_MESSAGE)
    private boolean enabled;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private List<Channel> channels;
    private String brand;

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
     * toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "NotificationConfigRequest [group=" + group + ", enabled=" + enabled
            + ", brand=" + brand + ", channels=" + channels + "]";
    }

}
