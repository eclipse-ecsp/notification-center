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
import org.eclipse.ecsp.domain.notification.MarketingName;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.notification.dao.MarketingNameDao;
import org.eclipse.ecsp.notification.vehicle.profile.VehicleProfileIntegrationService;
import org.eclipse.ecsp.notification.vehicle.profile.VehicleProfileServiceProvider;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * VehicleProfileEnricher adds vehicle name, model and model year to alert data.
 */
@Component
@Order(3)
public class VehicleProfileEnricher implements NotificationProcessor {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileEnricher.class);

    @Autowired
    private MarketingNameDao marketingNameDao;

    @Autowired
    private VehicleProfileServiceProvider vpServiceProvider;

    /**
     * process method.
     *
     * @param alertInfo AlertsInfo
     */
    @Override
    public void process(AlertsInfo alertInfo) {

        doProcess(alertInfo);
    }

    /**
     *  entry method for this processor.
     *
     * @param alertInfo alerts info payload
     */
    public void doProcess(AlertsInfo alertInfo) {
        LOGGER.debug("Adding Vehicle Profile Enricher for Alert info {}", alertInfo);
        VehicleProfileIntegrationService vpIntegrationService = vpServiceProvider
                .getVehicleProfileIntegrationService(alertInfo);
        VehicleProfileAbridged vehicleProfileAbridged = vpIntegrationService.getVehicleProfile(alertInfo);
        LOGGER.debug("VehicleProfile integration service returned vehicleprofile {}", vehicleProfileAbridged);
        alertInfo.getAlertsData().setVehicleProfile(vehicleProfileAbridged);
        if (vehicleProfileAbridged != null) {
            setMarketingName(alertInfo, vehicleProfileAbridged);
        }

    }

    /**
     * setMarketingName method.
     *
     * @param alertInfo AlertsInfo
     * @param vehicleProfileAbridged VehicleProfileAbridged
     */
    private void setMarketingName(AlertsInfo alertInfo, VehicleProfileAbridged vehicleProfileAbridged) {
        String modelLocal = (StringUtils.isNotBlank(vehicleProfileAbridged.getModel()))
                ?
                vehicleProfileAbridged.getModel().toLowerCase()
                : null;
        String brandLocal =
                (StringUtils.isNotBlank(vehicleProfileAbridged.getMake()))
                        ? vehicleProfileAbridged.getMake().toLowerCase()
                        : null;
        List<MarketingName> marketingNames = marketingNameDao.findByMakeAndModel(brandLocal, modelLocal, true);
        if (CollectionUtils.isEmpty(marketingNames)) {
            alertInfo.getAlertsData()
                    .setMarketingName(vehicleProfileAbridged.getMake() + " " + vehicleProfileAbridged.getModel());
        } else {
            Optional<MarketingName> optionalMarketingName = marketingNames.stream()
                    .filter(marketingName -> marketingName.getModel() != null)
                    .findFirst();
            String finalMarketingName =
                    optionalMarketingName.isPresent() ? optionalMarketingName.get().getMarketingName()
                            : marketingNames.get(0).getMarketingName();
            alertInfo.getAlertsData().setMarketingName(finalMarketingName);
        }
    }

}