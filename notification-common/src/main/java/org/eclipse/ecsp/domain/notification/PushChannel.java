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
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PushChannel class.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.PushChannel")
public class PushChannel extends Channel {
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String service;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String appPlatform;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private List<String> deviceTokens;

    /**
     * Default constructor.
     */
    public PushChannel() {

    }

    /**
     * PushChannel constructor.
     *
     * @param service service
     *
     * @param appPlatform platform
     *
     * @param deviceTokens token
     */
    public PushChannel(String service, String appPlatform, List<String> deviceTokens) {
        super();
        this.service = service;
        this.appPlatform = appPlatform;
        this.deviceTokens = deviceTokens;
    }

    /**
     * Getter for Service.
     *
     * @return service
     */
    public String getService() {
        return service;
    }

    /**
     * Setter for Service.
     *
     * @param service the new value
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Getter for AppPlatform.
     *
     * @return appplatform
     */
    public String getAppPlatform() {
        return appPlatform;
    }

    /**
     * Setter for AppPlatform.
     *
     * @param appPlatform the new value
     */
    public void setAppPlatform(String appPlatform) {
        this.appPlatform = appPlatform;
    }

    /**
     * Getter for DeviceTokens.
     *
     * @return devicetokens
     */
    public List<String> getDeviceTokens() {
        return deviceTokens;
    }

    /**
     * Setter for DeviceTokens.
     *
     * @param deviceTokens the new value
     */
    public void setDeviceTokens(List<String> deviceTokens) {
        this.deviceTokens = deviceTokens;
    }


    /**
     * Getter for ChannelType.
     *
     * @return channeltype
     */
    @Override
    public ChannelType getChannelType() {
        return ChannelType.MOBILE_APP_PUSH;
    }

    @Override
    public String toString() {
        return "PushChannel [service=" + service + ", appPlatform=" + appPlatform + ", deviceTokens="
            + deviceTokens + ", toString()=" + super.toString() + "]";
    }

    /**
     * Hashcode method.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appPlatform == null) ? 0 : appPlatform.hashCode());
        result = prime * result + ((deviceTokens == null) ? 0 : deviceTokens.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    /**
     * Equals method.
     *
     * @param obj object
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
        PushChannel other = (PushChannel) obj;
        if (appPlatform == null) {
            if (other.appPlatform != null) {
                return false;
            }
        } else if (!appPlatform.equals(other.appPlatform)) {
            return false;
        }
        if (deviceTokens == null) {
            if (other.deviceTokens != null) {
                return false;
            }
        } else if (!deviceTokens.equals(other.deviceTokens)) {
            return false;
        }
        if (service == null) {
            if (other.service != null) {
                return false;
            }
        } else if (!service.equals(other.service)) {
            return false;
        }
        return true;
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
        PushChannel otherPush = (PushChannel) others;
        for (String token : deviceTokens) {
            if (!otherPush.getDeviceTokens().contains(token)) {
                deletions.add(new PushChannel(service, appPlatform, Arrays.asList(token)));
            }
        }
        for (String token : otherPush.getDeviceTokens()) {
            if (!deviceTokens.contains(token)) {
                additions.add(
                    new PushChannel(otherPush.getService(), otherPush.getAppPlatform(), Arrays.asList(token)));
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

        // Update config api allows to create Push channel without deviceTokens
        // collection, therefore it might be null.
        if (deviceTokens == null) {
            return ret;
        }
        for (String token : deviceTokens) {
            ret.add(new PushChannel(service, appPlatform, Arrays.asList(token)));
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
        PushChannel pushChannel = (PushChannel) channel;
        this.deviceTokens = pushChannel.deviceTokens;
        this.appPlatform = pushChannel.appPlatform;
        this.service = pushChannel.service;
    }

    /**
     * ShallowClone method.
     *
     * @return Channel
     */
    @Override
    public Channel shallowClone() {
        PushChannel clone = new PushChannel();
        clone.setService(getService());
        clone.setAppPlatform(getAppPlatform());
        if (getDeviceTokens() != null) {
            clone.setDeviceTokens(new ArrayList<>(getDeviceTokens()));
        }
        super.populateClone(clone);
        return clone;
    }

}
