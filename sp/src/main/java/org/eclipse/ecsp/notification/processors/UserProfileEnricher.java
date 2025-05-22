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

package org.eclipse.ecsp.notification.processors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.userprofile.UserProfileIntegrationService;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * UserProfileEnricher processor to add user details to payload.
 */
@Component
@Order(4)
public class UserProfileEnricher implements NotificationProcessor {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(UserProfileEnricher.class);

    @Autowired
    private UserProfileDAO userProfileDao;

    @Autowired(required = false)
    private UserProfileIntegrationService userService;

    @Value("${user.profile.excluded.notification.ids}")
    private String userProfileExcludedNotifications;

    @Value("${enable.ignite.vehicle.profile:true}")
    private boolean igniteVehicleProfile;

    /**
     * process method.
     *
     * @param alertInfo AlertsInfo
     */
    @Override
    public void process(AlertsInfo alertInfo) {
        LOGGER.debug("Finding user profile for alert info {}", alertInfo);
        Data alertsData = alertInfo.getAlertsData();
        String userId = (String) alertsData.any().get(NotificationConstants.USERID);
        userId = getUserIdFromVehProf(alertInfo, userId, alertsData);

        if (StringUtils.isNotEmpty(userId)) {
            UserProfile userProfile;
            userProfile = userProfileDao.findById(userId);
            if ((null == userProfile
                    ||
                    alertsData.getAlertDataProperties().containsKey(NotificationConstants.REFRESH_USER))
                    && userService != null) {
                // Added for RTC Task 163636: Send email to the user if User
                // deleted from Ignite
                String notificationId = alertInfo.getAlertsData().getNotificationId();
                if (null != notificationId && userProfileExcludedNotifications != null) {
                    String trimmedExcludedNotifications = userProfileExcludedNotifications.replaceAll("\\s+", "");
                    userProfile = userService.processRealTimeUserUpdate(userId, alertsData.getVehicleProfile(),
                            Arrays.stream(trimmedExcludedNotifications.split(","))
                                    .noneMatch(t -> t.equals(notificationId)));
                }
            }
            populateUserInfoInEvent(alertInfo, userProfile, userId);
            alertInfo.getAlertsData().setUserProfile(userProfile);
            LOGGER.debug("Got user id from vehicle profile {}", userId);
        }
    }

    /**
     * populateUserInfoInEvent method.
     *
     * @param alertInfo   AlertsInfo
     * @param userProfile UserProfile
     * @param userId      String
     */
    private void populateUserInfoInEvent(AlertsInfo alertInfo, UserProfile userProfile, String userId) {
        if (null != userProfile) {
            if (!alertInfo.getAlertsData().getAlertDataProperties()
                    .containsKey(NotificationConstants.USER_NOTIFICATION)
                    || (!(Boolean) (alertInfo.getAlertsData().getAlertDataProperties()
                    .get(NotificationConstants.USER_NOTIFICATION)))) {
                setName(alertInfo, userProfile);

            }
        } else {

            LOGGER.info("User profile not found for user {}", userId);
        }
    }

    /**
     * getUserIdFromVehProf method.
     *
     * @param alertInfo  AlertsInfo
     * @param userId     String
     * @param alertsData Data
     * @return String
     */
    @Nullable
    private String getUserIdFromVehProf(AlertsInfo alertInfo, String userId, Data alertsData) {
        if (StringUtils.isEmpty(userId)) {
            VehicleProfileAbridged vehProf = alertsData.getVehicleProfile();
            if (null != vehProf) {
                userId = alertsData.getUserProfile() != null ? alertsData.getUserProfile().getUserId()
                        : vehProf.getUserId();
            }
            if (EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT.toString().equals(alertInfo.getEventID())) {
                ((GenericEventData) alertInfo.getIgniteEvent().getEventData()).set(NotificationConstants.USERID,
                        userId);
            }
        }
        return userId;
    }

    /**
     * setName method.
     *
     * @param alertInfo   AlertsInfo
     * @param userProfile UserProfile
     */
    private void setName(AlertsInfo alertInfo, UserProfile userProfile) {
        VehicleProfileAbridged vehicleProfileAbridged = alertInfo.getAlertsData().getVehicleProfile();
        String name = userProfile.getNickName(vehicleProfileAbridged.getVehicleId());
        if (StringUtils.isEmpty(name)) {
            name = vehicleProfileAbridged.getMake() + " " + vehicleProfileAbridged.getModel();
            NickName nickName = new NickName();
            nickName.setNickName(name);
            nickName.setVehicleId(vehicleProfileAbridged.getVehicleId());
            LOGGER.debug("Nick name not found in User profile.Default nick name : {} ", nickName);
            userProfile.addNickName(nickName);
        }

        if (!igniteVehicleProfile) {
            vehicleProfileAbridged.getVehicleAttributes()
                    .put("name", userProfile.getNickName(vehicleProfileAbridged.getVehicleId()));
        }
    }

    /**
     * setUserProfileExcludedNotifications method.
     *
     * @param userProfileExcludedNotifications String
     */
    @Profile("test")
    void setUserProfileExcludedNotifications(String userProfileExcludedNotifications) {
        this.userProfileExcludedNotifications = userProfileExcludedNotifications;
    }

    /**
     * setIgniteVehicleProfile method.
     *
     * @param igniteVehicleProfile boolean
     */
    @Profile("test")
    void setIgniteVehicleProfile(boolean igniteVehicleProfile) {
        this.igniteVehicleProfile = igniteVehicleProfile;
    }
}
