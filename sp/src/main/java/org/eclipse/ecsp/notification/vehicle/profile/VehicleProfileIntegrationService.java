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

/**
 * Abstraction for vehicle profile service to be implemented by any service
 * providers.
 *
 * @author AMuraleedhar
 */
public interface VehicleProfileIntegrationService {

    /**
     * Method to call any vehicle profile service and return the
     * VehicleProfileAbridged.
     *
     * @param alertInfo alertInfo
     * @return VehicleProfileAbridged VehicleProfileAbridged
     */
    public VehicleProfileAbridged getVehicleProfile(AlertsInfo alertInfo);

    /**
     * Get service name.
     *
     * @return service Name
     */
    public String getName();
}
