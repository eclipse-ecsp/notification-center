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

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * PayloadBasedVehicleProfileIntegrationService class.
 *
 * @author AMuraleedhar
 */
@Service
public class PayloadBasedVehicleProfileIntegrationService
        implements VehicleProfileIntegrationService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory
            .getLogger(PayloadBasedVehicleProfileIntegrationService.class);

    /**
     * getVehicleProfile method.
     *
     * @param alertInfo AlertsInfo
     * @return VehicleProfileAbridged
     */
    @Override
    public VehicleProfileAbridged getVehicleProfile(AlertsInfo alertInfo) {
        String vehicleId = alertInfo.getIgniteEvent().getVehicleId();
        GenericEventData genericEventData =
                (GenericEventData) alertInfo.getIgniteEvent().getEventData();

        if ((!alertInfo.getAlertsData().getAlertDataProperties()
                .containsKey(NotificationConstants.USER_NOTIFICATION)
                || (!(Boolean) (alertInfo.getAlertsData().getAlertDataProperties()
                .get(NotificationConstants.USER_NOTIFICATION))))
                && (vehicleId == null || genericEventData == null
                || !genericEventData.getData(NotificationConstants.VEHICLE_DATA).isPresent())) {
            LOGGER.error("No vehicle details found in alerts data {}", alertInfo);
            throw new IllegalArgumentException("No vehicle details found in alerts data for vehicle" + vehicleId);
        }
        VehicleProfileAbridged vehicleProfileAbridged = alertInfo.getAlertsData().getVehicleProfile();
        Map<String, Object> vehicleAttributes = new HashMap<String, Object>();

        if (genericEventData.getData(NotificationConstants.VEHICLE_DATA).isPresent()) {

            LOGGER.debug("Enriching vehicleprofile present in payload {}", genericEventData.toString());

            Map<String, Object> data = (Map<String, Object>) genericEventData
                    .getData(NotificationConstants.VEHICLE_DATA).get();

            Set<Map.Entry<String, Object>> entries = data.entrySet();


            for (Map.Entry<String, Object> entry : entries) {
                vehicleAttributes.put(entry.getKey(), entry.getValue());
            }
            if (!vehicleAttributes.containsKey(NotificationConstants.USERID)) {
                vehicleAttributes.put(NotificationConstants.USERID,
                        alertInfo.getAlertsData().getAlertDataProperties()
                                .containsKey(NotificationConstants.USERID)
                                ? alertInfo.getAlertsData().getAlertDataProperties().get(NotificationConstants.USERID)
                                : null);
            }

            vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
            vehicleProfileAbridged.setVehicleId(vehicleId);
        }
        return vehicleProfileAbridged;
    }

    /**
     * Method to get name.
     *
     * @return String
     */
    @Override
    public String getName() {
        return "payloadbased";
    }

}
