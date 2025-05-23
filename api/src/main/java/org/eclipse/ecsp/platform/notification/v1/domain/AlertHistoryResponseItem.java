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

package org.eclipse.ecsp.platform.notification.v1.domain;

import java.util.Map;

/**
 * AlertHistoryResponseItem class.
 */
public class AlertHistoryResponseItem {

    private String alertType;
    private String alertName;
    private Map<Object, Object> alertData;

    /**
     * AlertNames enum.
     */
    public enum AlertNames {
        /**
         * Low fuel alert.
         */
        LOWFUEL("LowFuel", "Low Fuel"),
        /**
         * Collision alert.
         */
        COLLISION("Collision", "Collision"),
        /**
         * Geofence breach alert.
         */
        GEOFENCE("GeoFence", "GeoFence Breach"),
        /**
         * Diagnostic trouble code stored alert.
         */
        DTCSTORED("DTCStored", "DTC Set");

        @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
        private String eventID;
        private String alertName;

        /**
         * Constructor.
         *
         * @param eventId   event id
         * @param alertName alert name
         */
        AlertNames(String eventId, String alertName) {
            this.eventID = eventId;
            this.alertName = alertName;
        }

        /**
         * Get event id.
         *
         * @return event id
         */
        @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
        public String getEventID() {
            return eventID;
        }

        /**
         * Get alert name.
         *
         * @return alert name
         */
        public String getAlertName() {
            return alertName;
        }

    }

    /**
     * Get alert type.
     *
     * @return alert type
     */
    public String getAlertType() {
        return alertType;
    }

    /**
     * Set alert type.
     *
     * @param alertType alert type
     */
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    /**
     * Get alert name.
     *
     * @return alert name
     */
    public String getAlertName() {
        return alertName;
    }

    /**
     * Set alert name.
     *
     * @param alertName alert name
     */
    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    /**
     * Get alert data.
     *
     * @return alert data
     */
    public Map<Object, Object> getAlertData() {
        return alertData;
    }

    /**
     * Set alert data.
     *
     * @param alertData alert data
     */
    public void setAlertData(Map<Object, Object> alertData) {
        this.alertData = alertData;
    }

}
