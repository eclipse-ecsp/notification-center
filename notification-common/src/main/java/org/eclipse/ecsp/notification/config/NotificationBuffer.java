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
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;
import org.eclipse.ecsp.notification.entities.BufferedAlertsInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationBuffer entity class.
 */
@Entity(value = NotificationDaoConstants.NOTIFICATION_BUFFER)
@Indexes({
    @Index(fields = {@Field(value = NotificationDaoConstants.USERID_FIELD),
        @Field(value = NotificationDaoConstants.VEHICLEID_FIELD)}),
    @Index(fields = {@Field(value = NotificationDaoConstants.SCHEDULER_ID_FIELD)})
})
public class NotificationBuffer extends AbstractIgniteEntity {

    @Id
    private String id;
    private List<BufferedAlertsInfo> alertsInfo;
    private ChannelType channelType;
    private String schedulerId;
    private String userId;
    private String group;
    private String vehicleId;
    private String contactId;

    public NotificationBuffer() {
        this.alertsInfo = new ArrayList<>();
    }

    /**
     * NotificationBuffer constructor.
     *
     * @param alertsInfo alertsInfo
     *
     * @param channelType channelType
     *
     * @param schedulerId scheduleId
     *
     * @param vehicleId vehicleId
     *
     * @param userId userId
     */
    public NotificationBuffer(List<BufferedAlertsInfo> alertsInfo, ChannelType channelType, String schedulerId,
                              String vehicleId, String userId) {
        this.vehicleId = vehicleId;
        this.alertsInfo = alertsInfo;
        this.channelType = channelType;
        this.schedulerId = schedulerId;
        this.userId = userId;
    }

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
     * Get contactId.
     *
     * @return contactId
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * Set contactId.
     *
     * @param contactId contactId
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

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
     * Get alertsInfo.
     *
     * @return alertsInfo
     */
    public List<BufferedAlertsInfo> getAlertsInfo() {
        return alertsInfo;
    }

    /**
     * Set alertsInfo.
     *
     * @param alertsInfo alertsInfo
     */
    public void setAlertsInfo(List<BufferedAlertsInfo> alertsInfo) {
        this.alertsInfo = alertsInfo;
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
     * Get schedulerId.
     *
     * @return schedulerId
     */
    public String getSchedulerId() {
        return schedulerId;
    }

    /**
     * Set schedulerId.
     *
     * @param schedulerId schedulerId
     */
    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    /**
     * Get userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get vehicleId.
     *
     * @return vehicleId
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Set vehicleId.
     *
     * @param vehicleId vehicleId
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "NotificationBuffer{" + "id='" + id + '\''
            + ", alertsInfo=" + alertsInfo + ", channelType=" + channelType
            + ", schedulerId='" + schedulerId + '\'' + ", userId='" + userId + '\''
            + ", group='" + group + '\'' + ", vehicleId='" + vehicleId + '\'' + '}';
    }
}
