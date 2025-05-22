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

import java.io.Serializable;

/**
 * NickName class.
 */
@Entity(useDiscriminator = false)
public class NickName implements Serializable {
    private static final long serialVersionUID = -5900264093656110372L;
    private String nickName;
    private String vehicleId;

    public NickName() {
    }

    /**
     * nickname constructor.
     *
     * @param nickName  parameter
     * @param vehicleId parameter
     */
    public NickName(String nickName, String vehicleId) {
        super();
        this.nickName = nickName;
        this.vehicleId = vehicleId;
    }

    /**
     * Getter for NickName.
     *
     * @return nickname
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Setter for NickName.
     *
     * @param nickName the new value
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * Getter for VehicleId.
     *
     * @return vehicleid
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Setter for VehicleId.
     *
     * @param vehicleId the new value
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Equals method.
     *
     * @param obj object
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
        NickName other = (NickName) obj;
        if (vehicleId == null) {
            if (other.vehicleId != null) {
                return false;
            }
        } else if (!vehicleId.equals(other.vehicleId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "NickName [nickName=" + nickName + ", vehicleId=" + vehicleId + "]";
    }

}
