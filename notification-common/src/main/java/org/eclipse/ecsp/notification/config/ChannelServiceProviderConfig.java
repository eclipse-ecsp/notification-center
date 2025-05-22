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

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

/**
 * ChannelServiceProviderConfig entity class.
 */
@Entity(value = NotificationConstants.CHANNEL_SERVICE_PROIVDER_CONFIG_COLLECTION_NAME, useDiscriminator = false)
public class ChannelServiceProviderConfig extends AbstractIgniteEntity {

    @Id
    private String id;

    private ChannelType channelType;

    private String region;

    private String notificationId;

    private String serviceProvider;

    /**
     * Get id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get channelType.
     *
     * @return channelType
     */
    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * Set channelType.
     *
     * @param channelType channelType
     */
    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * Get region.
     *
     * @return region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Set region.
     *
     * @param region region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Get notificationId.
     *
     * @return notificationId
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Set notificationId.
     *
     * @param notificationId notificationId
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Get serviceProvider.
     *
     * @return serviceProvider
     */
    public String getServiceProvider() {
        return serviceProvider;
    }

    /**
     * Set serviceProvider.
     *
     * @param serviceProvider serviceProvider
     */
    public void setServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

}
