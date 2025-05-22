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

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * VehicleService class.
 */
@Slf4j
@Service
public class VehicleService {
    private final CoreVehicleProfileClient coreVehicleProfileClient;

    @NotNull
    @Value("${path.for.getting.services}")
    private String pathForGettingServices;

    @Value("${enable.ignite.vehicle.profile:true}")
    private boolean igniteVehicleProfile;

    /**
     * Constructor.
     *
     * @param coreVehicleProfileClient coreVehicleProfileClient
     */
    @Autowired
    public VehicleService(CoreVehicleProfileClient coreVehicleProfileClient) {
        this.coreVehicleProfileClient = coreVehicleProfileClient;
    }

    /**
     * check isVehicleExist.
     *
     * @param id vehicleId
     *
     * @return boolean
     */
    public boolean isVehicleExist(String id) {
        try {
            Optional<String> vehicleProfile =
                coreVehicleProfileClient.getVehicleProfileJSON(id, igniteVehicleProfile);
            return vehicleProfile.isPresent();
        } catch (Exception ex) {
            log.debug("Error occurred while fetching vehicleProfileClient", ex);
            return false;
        }
    }

    /**
     * validateServiceEnabled.
     *
     * @param vehicleId vehicleId
     *
     * @param notificationGroupingList groups
     */
    public void validateServiceEnabled(String vehicleId, List<NotificationGrouping> notificationGroupingList) {
        Set<String> enabledServices = getEnabledServices(vehicleId);
        for (NotificationGrouping notificationGrouping : notificationGroupingList) {
            String serviceId = notificationGrouping.getService();
            if (!StringUtils.isEmpty(serviceId) && notificationGrouping.isCheckEntitlement()
                && !enabledServices.contains(serviceId)) {
                log.info("Vehicle {} is not subscribed to service {}. Continue searching another services", vehicleId,
                    serviceId);
            } else {
                log.info("Vehicle {} is subscribed to service {}. Continue processing notification", vehicleId,
                    serviceId);
                return;
            }
        }

        log.info(
            "Not processing notification as vehicle {} is not subscribed to any of the services. "
                + "Vehicle is subscribed to these services: {}",
            vehicleId, enabledServices);
        throw new AuthorizationException("Vehicle is not subscribed to any of the services");
    }

    /**
     * getEnabledServices for vehicleId.
     *
     * @param vehicleId vehicleID
     *
     * @return services enabled
     */
    public Set<String> getEnabledServices(String vehicleId) {
        Optional<String> vehicleProfileOp;
        try {
            vehicleProfileOp = coreVehicleProfileClient.getVehicleProfileJSON(vehicleId, igniteVehicleProfile);
        } catch (Exception e) {
            log.error("Failed to retrieve vehicle {} from vehicle profile", vehicleId, e);
            vehicleProfileOp = Optional.empty();
        }

        if (vehicleProfileOp.isPresent()) {
            log.info("Vehicle profile json received: {} for vehicleId {}", vehicleProfileOp, vehicleId);
            return fetchEnabledServices(vehicleProfileOp.get(), vehicleId);
        } else {
            throw new NoSuchEntityException("Vehicle profile not found for vehicle " + vehicleId);
        }
    }

    /**
     * fetchEnabledServices.
     *
     * @param vehicleProfile vehicleProfile
     *
     * @param vehicleId vehicleId
     *
     * @return services enabled
     */
    private Set<String> fetchEnabledServices(String vehicleProfile, String vehicleId) {
        Set<String> enabledServices = Collections.EMPTY_SET;
        try {
            List<String> apps = JsonPath.read(vehicleProfile, pathForGettingServices);
            enabledServices = new LinkedHashSet<>(apps);
            log.info("Provisioned Services for the vehicle {} are: {}", vehicleId, enabledServices);
        } catch (PathNotFoundException ex) {
            log.warn("Provisioned services path not found for vehicle {}", vehicleId);
        }
        return enabledServices;
    }
}
