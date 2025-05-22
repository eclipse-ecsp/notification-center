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

package org.eclipse.ecsp.notification.entities;

import org.eclipse.ecsp.domain.notification.commons.ChannelType;

/**
 * NotificationSchedulerPayload class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class NotificationSchedulerPayload {

    private String userID;
    private String vehicleID;
    private ChannelType channelType;
    private String group;
    private boolean schedulerUpdateFlag;
    private String schedulerId;
    private String contactId;

    /**
     * NotificationSchedulerPayload default constructor.
     */
    public NotificationSchedulerPayload() {
    }

    /**
     * NotificationSchedulerPayload constructor.
     *
     * @param userID userId
     *
     * @param vehicleID vehicleId
     *
     * @param channelType channelType
     *
     * @param group group
     *
     * @param contactId contactId
     */
    public NotificationSchedulerPayload(String userID, String vehicleID, ChannelType channelType, String group,
                                        String contactId) {
        this.userID = userID;
        this.vehicleID = vehicleID;
        this.channelType = channelType;
        this.group = group;
        this.contactId = contactId;
        this.schedulerUpdateFlag = false;
    }

    /**
     * NotificationSchedulerPayload constructor.
     *
     * @param userID userId
     *
     * @param vehicleID vehicleId
     *
     * @param channelType channelType
     *
     * @param group group
     *
     * @param schedulerUpdateFlag updateflag
     *
     * @param schedulerId schedulerId
     *
     * @param contactId contactId
     */
    public NotificationSchedulerPayload(String userID, String vehicleID, ChannelType channelType, String group,
                                        boolean schedulerUpdateFlag, String schedulerId, String contactId) {
        this.userID = userID;
        this.vehicleID = vehicleID;
        this.channelType = channelType;
        this.group = group;
        this.schedulerUpdateFlag = schedulerUpdateFlag;
        this.schedulerId = schedulerId;
        this.contactId = contactId;
    }

    /**
     * This method is a getter for schedulerid.
     *
     * @return String
     */

    public String getSchedulerId() {
        return schedulerId;
    }

    /**
     * This method is a setter for schedulerid.
     *
     * @param schedulerId : String
     */

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    /**
     * This method is a getter for schedulerupdateflag.
     *
     * @return boolean
     */
    public boolean isSchedulerUpdateFlag() {
        return schedulerUpdateFlag;
    }

    /**
     * This method is a setter for schedulerupdateflag.
     *
     * @param schedulerUpdateFlag : boolean
     */

    public void setSchedulerUpdateFlag(boolean schedulerUpdateFlag) {
        this.schedulerUpdateFlag = schedulerUpdateFlag;
    }

    /**
     * This method is a getter for group.
     *
     * @return String
     */

    public String getGroup() {
        return group;
    }

    /**
     * This method is a setter for group.
     *
     * @param group : String
     */

    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * This method is a getter for userid.
     *
     * @return String
     */

    public String getUserID() {
        return userID;
    }

    /**
     * This method is a setter for userid.
     *
     * @param userID : String
     */

    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * This method is a getter for vehicleid.
     *
     * @return String
     */

    public String getVehicleID() {
        return vehicleID;
    }

    /**
     * This method is a setter for vehicleid.
     *
     * @param vehicleID : String
     */

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    /**
     * This method is a getter for channeltype.
     *
     * @return ChannelType
     */

    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * This method is a setter for channeltype.
     *
     * @param channelType : ChannelType
     */

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * This method is a getter for contactid.
     *
     * @return String
     */

    public String getContactId() {
        return contactId;
    }

    /**
     * This method is a setter for contactid.
     *
     * @param contactId : String
     */

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    @Override
    public String toString() {
        return "NotificationSchedulerPayload{" + "userID='" + userID + '\''
            + ", vehicleID='" + vehicleID + '\'' + ", channelType=" + channelType
            + ", group='" + group + '\'' + ", schedulerUpdateFlag="
            + schedulerUpdateFlag + ", schedulerId='" + schedulerId + '\''
            + ", contactId='" + contactId + '\'' + '}';
    }
}