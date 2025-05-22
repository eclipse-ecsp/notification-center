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

package org.eclipse.ecsp.notification.duplication;

import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.EventMetadata;

import java.util.Map;

/**
 * Geofence event key extractor.
 */
public class GeofenceKeyExtractor extends AbstractKeyExtractor {

    /**
     * GeofenceKeyExtractor constructor.
     *
     * @param interval int
     */
    public GeofenceKeyExtractor(int interval) {
        super(interval);
    }

    /**
     * Get data qualifier.
     *
     * @param alertsData AlertsInfo.Data
     * @return String
     */
    @Override
    protected String getDataQualifer(Data alertsData) {
        Map<String, Object> any = alertsData.any();
        if (any != null) {
            Object id = any.get(EventMetadata.GeoFenceAttrs.ID.toString());
            if (id != null) {
                return id.toString();
            }
            Object geofenceId = any.get(EventMetadata.GeoFenceAttrs.GEOFENCE_ID.toString());
            if (geofenceId != null) {
                return geofenceId.toString();
            }
        }
        return null;
    }

}
