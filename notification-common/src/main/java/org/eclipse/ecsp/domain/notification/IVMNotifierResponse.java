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
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * IVMNotifierResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity
public class IVMNotifierResponse extends AbstractChannelResponse {

    private String deviceStatus;

    private String userStatus;

    /**
     * Default constructor.
     */
    public IVMNotifierResponse() {
        this(null, null);
    }

    /**
     * Constructor with userId and pdid.
     *
     * @param userId userId
     * @param pdid pdid
     */
    public IVMNotifierResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * Constructor with userId, pdid and eventData.
     *
     * @param userId userId
     * @param pdid pdid
     * @param eventData eventData
     */
    public IVMNotifierResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
        setChannelType(ChannelType.IVM);
    }

    /**
     * Getter for DeviceStatus.
     *
     * @return devicestatus
     */
    public String getDeviceStatus() {
        return deviceStatus;
    }

    /**
     * Setter for DeviceStatus.
     *
     * @param deviceStatus the new value
     */
    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    /**
     * Getter for UserStatus.
     *
     * @return userstatus
     */
    public String getUserStatus() {
        return userStatus;
    }

    /**
     * Setter for UserStatus.
     *
     * @param userStatus the new value
     */
    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }


    /**
     * Getter for Provider.
     *
     * @return provider
     */
    @Override
    public String getProvider() {
        return NotificationConstants.IVM_PROVIDER;
    }

    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IVMNotifierResponse [deviceStatus=");
        builder.append(deviceStatus);
        builder.append(", userStatus=");
        builder.append(userStatus);
        builder.append(", toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
