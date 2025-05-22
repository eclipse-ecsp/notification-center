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

package org.eclipse.ecsp.domain.notification;

/**
 * Class that consist of the information about the various alert events.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class EventMetadata {
    /**
     * EventIDs.
     */

    public enum EventID {
        COLLISION("Collision"),
        GEOFENCE("GeoFence"),
        DTC_STORED("DTCStored"),
        LOW_FUEL("LowFuel"),
        OVER_SPEED("OverSpeeding"),
        CURFEW("CurfewViolation"),
        IDLING("Idling"),
        TOWING("Tow"),
        DONGLE_STATUS("DongleStatus"),
        SERVICE_REMINDER("ServiceReminder"),
        GENERIC_NOTIFICATION_EVENT("GenericNotificationEvent"),
        NOTIFICATION_SETTINGS("NotificationSettings"),
        PROMOTIONAL_NOTIFICATIONS("PromotionalNotification");

        private String value;

        private EventID(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }

    /**
     * GeoFenceAttrs.
     */
    // Data fields for each eventID
    public enum GeoFenceAttrs {
        POSITION("position"),
        LONGITUDE("longitude"),
        LATITUDE("latitude"),
        TYPE("type"),
        ID("id"),
        GEOFENCE_ID("geofenceId");

        String value;

        GeoFenceAttrs(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    /**
     * As of now we support only generic and valet only. And it comes only in
     * GeoFence event version 1.1
     */
    public enum GeoFenceTypes {
        GENERIC("generic"),
        VALET("valet"),
        PRIVATE_LOC("privatelocation");

        String value;

        GeoFenceTypes(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        /**
         * method returns supportedType.
         *
         * @param type boolean
         * @return boolean
         */
        public static boolean isSupportedType(String type) {
            boolean typeSupported = false;
            for (GeoFenceTypes geoFenceType : GeoFenceTypes.values()) {
                if (geoFenceType.getValue().equals(type)) {
                    typeSupported = true;
                    break;
                }

            }
            return typeSupported;
        }

        /**
         * get geofencetype value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * GeoFencePosition class.
     */
    public enum GeoFencePosition {
        OUT("out"),
        IN("in");
        String value;

        GeoFencePosition(
            String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        /**
         * get geofence position value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * DTCAttrs class.
     */
    public enum DTCAttrs {
        SET("set"),
        CLEARED("cleared");

        String value;

        DTCAttrs(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }

    /**
     * GeoFenceEventVersions enums.
     */
    public enum GeoFenceEventVersions {
        VER1_0("1.0"),
        VER1_1("1.1");

        String value;

        GeoFenceEventVersions(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        /**
         * get geofence event version.
         *
         * @param chType version
         * @return version
         */
        public static GeoFenceEventVersions getVersion(String chType) {
            for (GeoFenceEventVersions type : GeoFenceEventVersions.values()) {
                if (type.getValue().equals(chType)) {
                    return type;
                }
            }
            return null;

        }

        /**
         * get Geofence event version.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }

    }

    /**
     * CurfewEventVersions enums.
     */
    public enum CurfewEventVersions {
        VER1_0("1.0"),
        VER1_1("1.1");

        String value;

        CurfewEventVersions(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        /**
         * get curfew event version.
         *
         * @param chType version
         * @return version
         */
        public static CurfewEventVersions getVersion(String chType) {
            for (CurfewEventVersions type : CurfewEventVersions.values()) {
                if (type.getValue().equals(chType)) {
                    return type;
                }
            }
            return null;

        }

        /**
         * get curfew event version.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }

    }

    /**
     * DongleStatusAttr enums.
     */
    public enum DongleStatusAttr {
        STATUS("status");

        String value;

        DongleStatusAttr(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }

    /**
     * DongleStatus enum.
     */
    public enum DongleStatus {
        ATTACHED("attached"),
        DETACHED("detached");

        String value;

        DongleStatus(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }

    /**
     * Enum for service reminder attributes.
     */

    public enum ServiceReminderAttr {
        SOURCE("source");

        String value;

        ServiceReminderAttr(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }

    /**
     * CollisionDataFieldNames class.
     */
    public enum CollisionDataFieldNames {
        LONGITUDE("longitude"), LATITUDE("latitude"), SPEED("speed");
        String value;

        CollisionDataFieldNames(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
