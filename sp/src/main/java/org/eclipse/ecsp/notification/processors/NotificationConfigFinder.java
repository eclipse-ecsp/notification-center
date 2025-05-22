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
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.NotificationConfigCommonService;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NotificationConfigFinder class.
 */
@Component
@Order(5)
public class NotificationConfigFinder implements NotificationProcessor {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationConfigFinder.class);
    private static final String IVM = "IVM";
    @Autowired
    private NotificationConfigCommonService notificationConfigCommonService;

    @Value("${brand.default.value:default}")
    private String defaultBrand;

    /**
     * process method.
     *
     * @param alert AlertsInfo
     */
    @Override
    public void process(AlertsInfo alert) {
        if (EventID.PIN_GENERATED.equals(alert.getIgniteEvent().getEventId())) {
            LOGGER.debug("Not processing NotificationConfigFinder for Pin Generated event: {}", alert);
            return;
        }
        LOGGER.debug("Finding notification config for alert info: {}", alert);
        final String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;

        String vehicleId = (alert.getAlertsData().getVehicleProfile() != null)
                ?
                alert.getAlertsData().getVehicleProfile().getVehicleId()
                : null;
        String brand = (alert.getAlertsData().getVehicleProfile() != null
                && StringUtils.isNotEmpty(alert.getAlertsData().getVehicleProfile().getMake()))
                ? alert.getAlertsData().getVehicleProfile().getMake()
                : defaultBrand;

        NotificationGrouping ng = alert.getNotificationGrouping();

        List<NotificationConfig> configs =
                notificationConfigCommonService.getAllConfigsFromDbByGroup(userId, vehicleId, ng.getGroup());
        if (configs.isEmpty()) {
            LOGGER.error(
                    "No notification config found "
                            +
                            "for userId {} vehicleId {} serviceName {} "
                            +
                            "Cannot process any further. expecting at least GENERAL/GENERAL",
                    userId, vehicleId, ng.getGroup());
            throw new IllegalStateException(
                    String.format("No notification config found for "
                                    +
                                    "userId %s vehicleId %s serviceName %s", userId,
                            vehicleId, ng.getGroup()));
        }

        if (!ng.getGroup().startsWith(IVM)) {
            List<NotificationConfig> secondaryContactsDefaultConfigs =
                    notificationConfigCommonService.getSecondaryContactsConfig(userId,
                            vehicleId, configs, brand);
            configs.addAll(secondaryContactsDefaultConfigs);
        }
        List<NotificationConfig> selectedConfigs =
                notificationConfigCommonService.selectNotificationConfig(configs, vehicleId,
                        alert.getAlertsData().getUserProfile(), brand);
        LOGGER.info("selected config size {} and config {}", selectedConfigs.size(), selectedConfigs);
        if (selectedConfigs.isEmpty()) {
            LOGGER.error(
                    "Could not find a notification config for "
                            +
                            "userId {} vehicleId {} serviceName {} Fetch returned configs: {}",
                    userId, vehicleId, ng.getGroup(), configs);
            throw new IllegalStateException(
                    String.format("Could not find a notification config for userId %s vehicleId %s serviceName %s",
                            userId, vehicleId, ng.getGroup()));
        }

        sanitizeSelectedConfigs(alert, selectedConfigs, userId);
        alert.setNotificationConfigs(selectedConfigs);
    }

    /**
     * Sanitize selected configs.
     *
     * @param alert           AlertsInfo
     * @param selectedConfigs List
     * @param userId          String
     */
    private void sanitizeSelectedConfigs(AlertsInfo alert, List<NotificationConfig> selectedConfigs, String userId) {
        selectedConfigs.forEach(config -> {
            if (NotificationConstants.GENERAL.equals(config.getUserId()) && StringUtils.isNotEmpty(userId)) {
                config.setUserId(userId);
            }
            if (alert.getIgniteEvent().getEventId().equals(EventID.DYNAMIC_NOTIFICATION)) {
                String channelTypeStr = (String) alert.getAlertsData().getAlertDataProperties().get("channelType");
                ChannelType channelType = ChannelType.getChannelType(channelTypeStr);
                if (channelType != null) {
                    config.disableChannelsExcept(channelType);
                }
            }
        });
    }
}
