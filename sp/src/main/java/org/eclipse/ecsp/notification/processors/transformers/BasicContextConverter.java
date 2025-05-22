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

package org.eclipse.ecsp.notification.processors.transformers;

import com.google.common.collect.ImmutableMap;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.processor.content.dto.ContentProcessingContextDto;
import org.eclipse.ecsp.processor.content.dto.UserProfileDto;
import org.eclipse.ecsp.processor.content.dto.VehicleProfileDto;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * This bean is default and be initiated only if there is no other bean
 * implementation present. This allows to implement other conversion logic and
 * include more data in the additional properties map. NOTE: the maps here are
 * wrapped into ImmutableMap to prevent data modification, however, no deep
 * cloning is done!
 */
public class BasicContextConverter implements AlertsInfoToDtoConverter {

    /**
     * Converts UserProfile to UserProfileDto.
     *
     * @param userProfile the given UserProfile
     * @return UserProfileDto
     */
    private static UserProfileDto toDto(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        Map<String, Object> customAttributes = userProfile.getCustomAttributes();
        return new UserProfileDto()
                .setUserId(userProfile.getUserId())
                .setFirstName(userProfile.getFirstName())
                .setLastName(userProfile.getLastName())
                .setLocale(userProfile.getLocale())
                .setDefaultEmail(userProfile.getDefaultEmail())
                .setDefaultPhoneNumber(userProfile.getDefaultPhoneNumber())
                .setConsent(userProfile.isConsent())
                .setTimeZone(userProfile.getTimeZone())
                .setLastModifiedTime(userProfile.getLastModifiedTime())
                .setCustomAttributes(toImmutableMap(customAttributes));
    }

    /**
     * Converts VehicleProfileAbridged to VehicleProfileDto.
     *
     * @param vehicleProfile the given VehicleProfileAbridged
     * @return VehicleProfileDto
     */
    private static VehicleProfileDto toDto(VehicleProfileAbridged vehicleProfile) {
        if (vehicleProfile == null) {
            return null;
        }

        return new VehicleProfileDto()
                .setVehicleId(vehicleProfile.getVehicleId())
                .setVehicleAttributes(vehicleProfile.getVehicleAttributes());
    }

    /**
     * Converts mutable map to immutable map.
     *
     * @param mutableMap the given mutable map
     * @return ImmutableMap
     */
    private static ImmutableMap<String, Object> toImmutableMap(Map<String, Object> mutableMap) {
        return mutableMap == null ? ImmutableMap.of()
                : ImmutableMap.copyOf(mutableMap.entrySet().stream() // filtering
                // out all
                // the null
                // key or
                // value
                // entries,
                .filter(entry -> entry.getKey() != null
                        && // because
                        // guava has
                        // a
                        // precondition
                        // agains
                        entry.getValue() != null)
                .collect(Collectors.toSet()));
    }

    /**
     * Method to apply Transformation.
     */
    @Override
    public ContentProcessingContextDto apply(AlertsInfo alertsInfo) {
        ContentProcessingContextDto contextDto = new ContentProcessingContextDto()
                .setEventID(alertsInfo.getEventID())
                .setVersion(alertsInfo.getVersion())
                .setTimestamp(alertsInfo.getTimestamp())
                .setTimezone(alertsInfo.getTimezone())
                .setPdid(alertsInfo.getPdid());

        if (alertsInfo.getAlertsData() != null) {
            AlertsInfo.Data alertsData = alertsInfo.getAlertsData();
            UserProfile userProfile = alertsData.getUserProfile();
            VehicleProfileAbridged vehicleProfile = alertsData.getVehicleProfile();
            contextDto.setNotificationId(alertsData.getNotificationId())
                    .setMarketingName(alertsData.getMarketingName())
                    .setUserProfile(toDto(userProfile))
                    .setVehicleProfile(toDto(vehicleProfile))
                    .setAdditionalProperties(toImmutableMap(alertsData.getAlertDataProperties()));
        }
        return contextDto;
    }
}
