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
import dev.morphia.annotations.Entity;
import org.eclipse.ecsp.utils.Utils;

import java.util.Map;

/**
 * VehicleProfileAbridged class.
 */
@Entity(useDiscriminator = false)
public class VehicleProfileAbridged {

    private String vehicleId;

    private Map<String, Object> vehicleAttributes;


    /**
     * Getter for UserId.
     *
     * @return userid
     */
    @JsonIgnore
    public String getUserId() {
        return vehicleAttributes.containsKey("userId") ? (String) vehicleAttributes.get("userId") : null;
    }

    /**
     * Constructor.
     */
    public VehicleProfileAbridged() {

    }

    /**
     * Constructor.
     *
     * @param vehicleId vehicleid
     */
    public VehicleProfileAbridged(String vehicleId) {
        setVehicleId(vehicleId);
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
     * Getter for Name.
     *
     * @return name
     */
    @JsonIgnore
    public String getName() {
        return vehicleAttributes.containsKey("name") ? (String) vehicleAttributes.get("name") : null;
    }


    /**
     * Getter for ModelYear.
     *
     * @return modelyear
     */
    @JsonIgnore
    public String getModelYear() {
        return vehicleAttributes.containsKey("modelYear") ? (String) vehicleAttributes.get("modelYear") : null;
    }


    /**
     * Getter for Model.
     *
     * @return model
     */
    @JsonIgnore
    public String getModel() {
        return vehicleAttributes.containsKey("model") ? (String) vehicleAttributes.get("model") : null;
    }


    /**
     * Getter for Make.
     *
     * @return make
     */
    @JsonIgnore
    public String getMake() {
        return vehicleAttributes.containsKey("make") ? (String) vehicleAttributes.get("make") : null;
    }


    /**
     * Getter for PlateNumber.
     *
     * @return platenumber
     */
    @JsonIgnore
    public String getPlateNumber() {
        return vehicleAttributes.containsKey("licensePlate") ? (String) vehicleAttributes.get("licensePlate") : null;
    }


    /**
     * Getter for EmergencyNumber.
     *
     * @return emergencynumber
     */
    @JsonIgnore
    public String getEmergencyNumber() {
        return vehicleAttributes.containsKey("emergencyContacts")
            ? (String) vehicleAttributes.get("emergencyContacts") : null;
    }


    /**
     * Getter for Vin.
     *
     * @return vin
     */
    @JsonIgnore
    public String getVin() {
        return vehicleAttributes.containsKey("vin") ? (String) vehicleAttributes.get("vin") : null;
    }


    /**
     * Getter for SoldRegion.
     *
     * @return soldregion
     */
    @JsonIgnore
    public String getSoldRegion() {
        return vehicleAttributes.containsKey("soldRegion") ? (String) vehicleAttributes.get("soldRegion") : null;
    }

    public Map<String, Object> getVehicleAttributes() {
        return vehicleAttributes;
    }

    /**
     * Setter for VehicleAttributes.
     *
     * @param vehicleAttributes the new value
     */
    public void setVehicleAttributes(Map<String, Object> vehicleAttributes) {
        this.vehicleAttributes = vehicleAttributes;
    }

    /**
     * Getter for VehicleAttributeByName.
     *
     * @return vehicleattributebyname
     */
    public String getVehicleAttributeByName(String name) {
        return vehicleAttributes.containsKey(name) ? (String) vehicleAttributes.get(name) : null;
    }


    /**
     * Getter for VehicleAttributeLog.
     *
     * @return vehicleattributelog
     */
    public String getVehicleAttributeLog() {

        return vehicleAttributes != null ? String.format(
            "vehicleattribues [userId = %s , modelYear = %s , make = %s , model = %s ,name = %s ,"
                + "soldRegion = %s ,emergencyNumber = %s ,vin = %s, plateNumber = %s]",
            getUserId(), getModelYear(), getMake(), getModel(),
            getName(), getSoldRegion(), Utils.maskString(getEmergencyNumber()), getVin(), getPlateNumber()) : null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        return result;
    }

    /**
     * Equals method.
     *
     * @param obj Object
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
        VehicleProfileAbridged other = (VehicleProfileAbridged) obj;
        if (vehicleId == null) {
            if (other.vehicleId != null) {
                return false;
            }
        } else if (!vehicleId.equals(other.vehicleId)) {
            return false;
        }
        return true;
    }


    /**
     * To string method.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "VehicleProfileAbridged [vehicleId=" + vehicleId + ", " + getVehicleAttributeLog() + "]";
    }
}
