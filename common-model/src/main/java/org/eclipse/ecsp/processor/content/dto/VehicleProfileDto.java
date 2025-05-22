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

package org.eclipse.ecsp.processor.content.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * VehicleProfileDto class to access vehicleprofile in the plugins.
 */
@Data
@Accessors(chain = true)
public class VehicleProfileDto {
    private String vehicleId;

    private Map<String, Object> vehicleAttributes;

    /**
     * Get the userId of the vehicle.
     *
     * @return userId
     */
    public String getUserId() {
        return vehicleAttributes.containsKey("userId") ? (String) vehicleAttributes.get("userId") : null;
    }

    /**
     * Get the name of the vehicle.
     *
     * @return name
     */
    public String getName() {
        return vehicleAttributes.containsKey("name") ? (String) vehicleAttributes.get("name") : null;
    }

    /**
     * Get the model year of the vehicle.
     *
     * @return modelYear
     */
    public String getModelYear() {
        return vehicleAttributes.containsKey("modelYear") ? (String) vehicleAttributes.get("modelYear") : null;
    }

    /**
     * Get the model of the vehicle.
     *
     * @return model
     */
    public String getModel() {
        return vehicleAttributes.containsKey("model") ? (String) vehicleAttributes.get("model") : null;
    }

    /**
     * Get the make of the vehicle.
     *
     * @return make
     */
    public String getMake() {
        return vehicleAttributes.containsKey("make") ? (String) vehicleAttributes.get("make") : null;
    }

    /**
     * getPlateNumber of the vehicle.
     *
     * @return getPlateNumber
     */
    public String getPlateNumber() {
        return vehicleAttributes.containsKey("licensePlate") ? (String) vehicleAttributes.get("licensePlate") : null;
    }

    /**
     * getEmergencyNumber of the vehicle.
     *
     * @return getEmergencyNumber
     */
    public String getEmergencyNumber() {
        return vehicleAttributes.containsKey("emergencyContacts")
            ? (String) vehicleAttributes.get("emergencyContacts") : null;
    }

    /**
     * getVin of the vehicle.
     *
     * @return getVin
     */
    public String getVin() {
        return vehicleAttributes.containsKey("vin") ? (String) vehicleAttributes.get("vin") : null;
    }

    /**
     * getVehicleAttributeByName of the vehicle.
     *
     * @param name name
     * @return getVehicleAttributeByName
     */
    public String getVehicleAttributeByName(String name) {
        return vehicleAttributes.containsKey(name) ? (String) vehicleAttributes.get(name) : null;
    }

}
