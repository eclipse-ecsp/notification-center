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

package org.eclipse.ecsp.notification.utils;

import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.services.entities.VehicleProfileOnDemandAttribute;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PropertyUtils class.
 */
@Component
public class PropertyUtils {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(PropertyUtils.class);
    Map<String, VehicleProfileOnDemandAttribute> attributes;
    @Value("${outbound.api.additional.headers:#{null}}")
    private String[] additionalHeaders;
    @Value("${vehicle.profile.attributes:#{null}}")
    private String[] vehicleAttributes;

    private static final int TWO = 2;

    /**
     * init method to initialize the attributes.
     */
    @PostConstruct
    void init() {
        attributes = new HashMap<>();
        if (null != vehicleAttributes) {
            for (String attr : vehicleAttributes) {
                String[] arr = attr.split(":");
                try {
                    VehicleProfileOnDemandAttribute vp =
                            new VehicleProfileOnDemandAttribute(arr[0], arr[1], Class.forName(arr[TWO]));
                    attributes.put(arr[0], vp);
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Vehicle attribute {} type {} is incorrect,hence configuration failed", arr[0],
                            arr[TWO]);
                    throw new IllegalArgumentException(
                            String.format("vehicle profile attribute configuration is incorrect  %s", e.getMessage()));
                }

            }
        }
    }

    /**
     * getHeaderMap method to fetch the headers.
     *
     * @return map of headers
     */
    public Map<String, String> getHeaderMap() {
        Map<String, String> headerMap = new HashMap<String, String>();
        if (null != additionalHeaders) {
            for (String header : additionalHeaders) {
                String[] arr = header.split(":");
                headerMap.put(arr[0], arr[1]);
            }
        }
        return headerMap;
    }

    /**
     * getVehicleAttributes method to fetch the vehicle attributes.
     *
     * @return map of vehicle attributes
     */
    public Map<String, VehicleProfileOnDemandAttribute> getVehicleAttributes() {
        return attributes;
    }
}
