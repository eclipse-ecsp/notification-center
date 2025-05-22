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

package org.eclipse.ecsp.domain.notification.utils;


import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.entities.VehicleProfileOnDemandAttribute;
import org.eclipse.ecsp.services.utils.VehicleProfileClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * CoreVehicleProfileClient class for vehicle profile interactions.
 */
@Component
public class CoreVehicleProfileClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreVehicleProfileClient.class);

    @Autowired
    VehicleProfileClient vehicleProfileClient;

    /**
     * getVehicleProfileAttributes for the given vehicle and attribute.
     *
     * @param vehicleId vehicleId
     *
     * @param igniteProfile            True when Ignite VehicleProfile is required and False when
     *                                 Vehicle Profile is required
     * @param vehicleProfileAttributes vehicleProfileAttributes
     *
     * @return map of vehicleattribute name and value.
     */
    public Map<VehicleProfileAttribute, Optional<String>> getVehicleProfileAttributes(String vehicleId,
                              boolean igniteProfile, VehicleProfileAttribute... vehicleProfileAttributes) {
        LOGGER.debug("Get VehicleProfileAttributes for vehicle {}", vehicleId);
        return igniteProfile
            ? vehicleProfileClient.getVehicleProfileAttributesWithClientId(vehicleId, true, vehicleProfileAttributes)
            : vehicleProfileClient.getVehicleProfileAttributes(vehicleId, false, vehicleProfileAttributes);
    }

    /**
     * getVehicleProfileAttributes.
     *
     * @param vehicleId String vehicleId
     *
     * @param igniteProfile boolean
     *
     * @param vehicleProfileAttributes VehicleProfileOnDemandAttribute attributes
     *
     * @return attributes
     */
    public Map<String, Optional<?>> getVehicleProfileAttributes(String vehicleId, boolean igniteProfile,
        VehicleProfileOnDemandAttribute... vehicleProfileAttributes) {
        LOGGER.debug("Get VehicleProfileOnDemandAttributes for vehicle {}",
            vehicleId);
        return igniteProfile ? vehicleProfileClient.getVehicleProfileAttributesWithClientId(vehicleId,
            igniteProfile, vehicleProfileAttributes)
            : vehicleProfileClient.getVehicleProfileAttributes(vehicleId, false,
            vehicleProfileAttributes);
    }

    /**
     * getVehicleProfileJSON for the given vehicleId.
     *
     * @param vehicleId string vehicleId
     *
     * @param igniteProfile True when Ignite VehicleProfile is required and False when
     *                      Vehicle Profile is required
     * @return Option vehicleprofile
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public Optional<String> getVehicleProfileJSON(String vehicleId, boolean igniteProfile) {
        LOGGER.debug("Get VehicleProfileJSON for vehicle {}", vehicleId);
        return igniteProfile ? vehicleProfileClient.getVehicleProfileJsonWithClientId(vehicleId)
            : vehicleProfileClient.getVehicleProfileJson(vehicleId);
    }

}
