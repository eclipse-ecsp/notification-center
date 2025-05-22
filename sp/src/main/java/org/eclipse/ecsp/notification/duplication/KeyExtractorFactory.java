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

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.EventMetadata.EventID;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory of Key extractor.
 */
public class KeyExtractorFactory {

    private static Map<Key, KeyExtractor> extractors = new EnumMap<>(Key.class);
    private static boolean isInitialized;

    private KeyExtractorFactory() {
    }

    /**
     * init method.
     *
     * @param interval Integer
     */
    public static synchronized void init(int interval) {
        if (!isInitialized) {
            extractors.put(Key.GEOFENCE, new GeofenceKeyExtractor(interval));
            extractors.put(Key.GENERIC, new GenericKeyExtractor(interval));
            isInitialized = true;
        }
    }

    /**
     * getCurrentKey method.
     *
     * @param alertInfo AlertsInfo
     * @return String
     */
    public static String getCurrentKey(AlertsInfo alertInfo) {
        if (!isInitialized) {
            throw new IllegalStateException("KeyExtractFactory is not initialized.");
        }
        if (alertInfo.getEventID().equals(EventMetadata.EventID.GEOFENCE.toString())) {
            return extractors.get(Key.GEOFENCE).extractCurrentKey(alertInfo);
        } else {
            return extractors.get(Key.GENERIC).extractCurrentKey(alertInfo);
        }
    }

    /**
     * getPreviousKey method.
     *
     * @param alertInfo AlertsInfo
     * @param hops Integer
     * @return String
     */
    public static String getPreviousKey(AlertsInfo alertInfo, int hops) {
        if (!isInitialized) {
            throw new IllegalStateException("KeyExtractFactory is not initialized.");
        }
        if (alertInfo.getEventID().equals(EventMetadata.EventID.GEOFENCE.toString())) {
            return extractors.get(Key.GEOFENCE).extractPreviousKey(alertInfo, hops);
        } else {
            return extractors.get(Key.GENERIC).extractPreviousKey(alertInfo, hops);
        }
    }

    // Support multiple test cases in DuplicatorTest class
    /**
     * reset method.
     */
    public static void reset() {
        isInitialized = false;
    }

    /**
     * createKeyExtractorFactory method.
     *
     * @return KeyExtractorFactory
     */
    public static KeyExtractorFactory createKeyExtractorFactory() {
        return new KeyExtractorFactory();
    }

    /**
     * Key enum.
     */
    public enum Key {
        /**
         * GEOFENCE.
         */
        GEOFENCE(EventID.GEOFENCE.toString()), GENERIC("Generic");

        private String value;

        private Key(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }
}
