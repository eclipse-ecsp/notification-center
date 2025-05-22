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

package org.eclipse.ecsp.notification.grouping;

/**
 * GroupType enum.
 * GroupType is added to NotificationGrouping to determine whether current group targeted for:
    USER_ONLY - preference for account level api only
    USER_VEHICLE - preference for userId and vehicleId api only
    DEFAULT - both api's
*/
public enum GroupType {
    DEFAULT("default"), USER_ONLY("user_only"), USER_VEHICLE("user_vehicle");
    private final String type;

    /**
     * GroupType constructor.
     *
     * @param type type
     */
    GroupType(String type) {
        this.type = type;
    }

    /**
     * Get type.
     *
     * @return type
     */
    String getType() {
        return type;
    }
}

