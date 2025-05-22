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

import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VehicleProfileServiceProvider class.
 */
@Component
public class VehicleProfileServiceProvider implements ApplicationContextAware {

    private static final IgniteLogger LOGGER
            = IgniteLoggerFactory.getLogger(VehicleProfileServiceProvider.class);

    private ApplicationContext applicationContext;

    private ConcurrentHashMap<String, VehicleProfileIntegrationService> services
            = new ConcurrentHashMap<>();

    @Value("${ignite.vehicle.profile.service}")
    private String igniteVehicleProfileService;

    @Value("${external.vehicle.profile.service}")
    private String externalVehicleProfileService;

    /**
     * init method.
     */
    @PostConstruct
    private void init() {

        Map<String, VehicleProfileIntegrationService> serviceBeans = applicationContext
                .getBeansOfType(VehicleProfileIntegrationService.class);

        LOGGER.info("Registering all the available vehicle profile services {}", serviceBeans.keySet());
        serviceBeans.values().forEach(bean -> {
            registerVehicleProfileIntegrationService(bean);
        });
    }

    /**
     * Register Vehicle Profile Integration service.
     *
     * @param service VehicleProfileIntegrationService
     */
    private void registerVehicleProfileIntegrationService(VehicleProfileIntegrationService service) {
        services.put(service.getName(), service);
    }

    /**
     * Getting Vehicle Profile Integration service based on Alert Data.
     *
     * @param alertInfo alertInfo
     * @return VehicleProfileIntegrationService
     */
    public VehicleProfileIntegrationService
           getVehicleProfileIntegrationService(AlertsInfo alertInfo) {
        if ((alertInfo.getAlertsData().getAlertDataProperties()
                .containsKey(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD)
                && ((Boolean) (alertInfo.getAlertsData().getAlertDataProperties()
                .get(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD))))
                || (alertInfo.getAlertsData().getAlertDataProperties()
                .containsKey(NotificationConstants.USER_NOTIFICATION)
                && ((Boolean) (alertInfo.getAlertsData().getAlertDataProperties()
                .get(NotificationConstants.USER_NOTIFICATION))))) {
            return services.get(externalVehicleProfileService);
        }
        return services.get(igniteVehicleProfileService);

    }

    /**
     * Set application context.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    // For test
    void setServices(ConcurrentHashMap<String, VehicleProfileIntegrationService> services) {
        this.services = services;
    }

    // For test
    void setIgniteVehicleProfileService(String igniteVehicleProfileService) {
        this.igniteVehicleProfileService = igniteVehicleProfileService;
    }

    // For test
    void setPayloadBasedVehicleProfileService(String payloadBasedVehicleProfileService) {
        this.externalVehicleProfileService = payloadBasedVehicleProfileService;
    }

}
