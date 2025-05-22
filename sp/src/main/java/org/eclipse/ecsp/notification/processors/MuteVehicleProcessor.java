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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.notification.dao.MuteVehicleDAO;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * MuteVehicleProcessor class.
 */
@Component
@Order(10)
@Slf4j
public class MuteVehicleProcessor implements NotificationProcessor {

    private final MuteVehicleDAO muteVehicleDao;

    /**
     * MuteVehicleProcessor constructor.
     *
     * @param muteVehicleDao MuteVehicleDAO
     */
    public MuteVehicleProcessor(MuteVehicleDAO muteVehicleDao) {
        this.muteVehicleDao = muteVehicleDao;
    }

    /**
     * process method.
     *
     * @param alertsInfo AlertsInfo
     */
    @Override
    public void process(AlertsInfo alertsInfo) {

        log.debug("start MuteVehicleProcessor");

        if (alertsInfo.getIgniteEvent().getVehicleId() != null) {

            log.debug("looking for vehicle {}", alertsInfo.getIgniteEvent().getVehicleId());

            MuteVehicle muteVehicle = muteVehicleDao.findById(alertsInfo.getIgniteEvent().getVehicleId());

            alertsInfo.setMuteVehicle(muteVehicle);
        }
    }
}
