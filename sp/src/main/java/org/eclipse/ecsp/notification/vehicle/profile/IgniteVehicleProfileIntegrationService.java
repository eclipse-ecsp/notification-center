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

package org.eclipse.ecsp.notification.vehicle.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.exception.UserNotAssociatedException;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.utils.PropertyUtils;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.entities.VehicleProfileOnDemandAttribute;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * IgniteVehicleProfileIntegrationService class for core.
 */
@Service
public class IgniteVehicleProfileIntegrationService implements VehicleProfileIntegrationService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory
            .getLogger(IgniteVehicleProfileIntegrationService.class);

    @Autowired
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @Autowired
    private PropertyUtils propertyUtils;

    @Value("${enable.ignite.vehicle.profile:true}")
    private boolean igniteVehicleProfile;

    /**
     * Method to get vehicle profile.
     *
     * @param alertInfo AlertsInfo
     * @return VehicleProfileAbridged
     */
    @Override
    public VehicleProfileAbridged
        getVehicleProfile(AlertsInfo alertInfo) {
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();

        Map<String, Object> attributes = new HashMap<String, Object>();
        String userId = null;
        Map<String, VehicleProfileOnDemandAttribute> vpAttributes = propertyUtils.getVehicleAttributes();
        NotificationGrouping ng = alertInfo.getNotificationGrouping();
        LOGGER.debug("Fetching vehicle attributes {}", vpAttributes.keySet());
        // Add car name,model and year to notification.
        IgniteEvent igniteEvent = alertInfo.getIgniteEvent();
        String vehicleId = igniteEvent.getVehicleId();
        if (StringUtils.isNotEmpty(vehicleId)) {
            return getVehicleProfileAbridged(alertInfo, vehicleId, vpAttributes,
                    userId, ng, attributes, vehicleProfileAbridged);
        }
        return null;
    }

    /**
     * Method to get vehicle profile abridged.
     *
     * @param alertInfo AlertsInfo
     * @param vehicleId String
     * @param vpAttributes Map
     * @param userId String
     * @param ng NotificationGrouping
     * @param attributes Map
     * @param vehicleProfileAbridged VehicleProfileAbridged
     * @return VehicleProfileAbridged
     */
    @NotNull
    private VehicleProfileAbridged getVehicleProfileAbridged(AlertsInfo alertInfo, String vehicleId,
                                                             Map<String, VehicleProfileOnDemandAttribute> vpAttributes,
                                                             String userId, NotificationGrouping ng,
                                                             Map<String, Object> attributes,
                                                             VehicleProfileAbridged vehicleProfileAbridged) {
        Map<String, Optional<?>> attrs = coreVehicleProfileClient.getVehicleProfileAttributes(vehicleId,
                igniteVehicleProfile,
                vpAttributes.values().toArray(new VehicleProfileOnDemandAttribute[vpAttributes.size()]));
        userId = getUserIdFromVehProf(alertInfo, vehicleId, userId, ng);
        String licensePlate = null;
        String contact = null;
        if (!igniteVehicleProfile) {
            LOGGER.debug("Ignite vehicle profile disabled, getting vehicle profile json");
            attributes.put("vehicleId", vehicleId);
            try {
                Optional<String> vehicleProfileOptional = coreVehicleProfileClient.getVehicleProfileJSON(vehicleId,
                        igniteVehicleProfile);
                if (vehicleProfileOptional.isPresent()) {

                    JsonNode vehicleProfile = new ObjectMapper().readTree(vehicleProfileOptional.get());
                    JsonNode soldRegionNode = vehicleProfile.get("soldRegion");
                    if (null != soldRegionNode && !soldRegionNode.isNull()) {
                        attributes.put("soldRegion", soldRegionNode.asText());
                    }
                    licensePlate = vehicleProfile.get("authorizedUsers").get(0).get("licensePlate").asText();
                    contact = vehicleProfile.get("authorizedUsers").get(0).get("emergencyContacts").get(0)
                            .get("phone").asText();

                }
            } catch (Exception e) {
                LOGGER.error("Failed to get vehicle Profile Json.", e);
            }
        }
        for (String key : vpAttributes.keySet()) {
            attributes.put(key, attrs.get(key).orElse(null));

        }

        attributes.put("userId", userId);
        attributes.put("emergencyContacts", contact);
        attributes.put("licensePlate", licensePlate);
        vehicleProfileAbridged.setVehicleAttributes(attributes);
        vehicleProfileAbridged.setVehicleId(vehicleId);
        return vehicleProfileAbridged;
    }

    /**
     * Method to get user id from vehicle profile.
     *
     * @param alertInfo AlertsInfo
     * @param vehicleId String
     * @param userId String
     * @param ng NotificationGrouping
     * @return String
     */
    @Nullable
    private String getUserIdFromVehProf(AlertsInfo alertInfo, String vehicleId, String userId,
                                        NotificationGrouping ng) {
        if (!alertInfo.getAlertsData().getAlertDataProperties().containsKey(NotificationConstants.USERID)
                || alertInfo.getAlertsData().getAlertDataProperties().get(NotificationConstants.USERID) == null
                || alertInfo.getAlertsData().getAlertDataProperties().get(NotificationConstants.USERID).toString()
                .isEmpty()) {
            LOGGER.debug("userId not present in payload");
            try {
                Map<VehicleProfileAttribute, Optional<String>> attr = coreVehicleProfileClient
                        .getVehicleProfileAttributes(vehicleId, igniteVehicleProfile,
                                VehicleProfileAttribute.USERID);
                userId = attr.get(VehicleProfileAttribute.USERID).orElse(null);
            } catch (Exception ex) {
                if (!ng.isCheckAssociation()) {
                    LOGGER.warn("No user is associated to the vehicle");
                } else {
                    throw new UserNotAssociatedException("No user associated to the vehicle");
                }
            }

        }
        return userId;
    }

    /**
     * Method to get name.
     *
     * @return String
     */
    @Override
    public String getName() {
        return "ignite";
    }

}
