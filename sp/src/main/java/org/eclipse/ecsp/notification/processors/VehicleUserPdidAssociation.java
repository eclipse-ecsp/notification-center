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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class representig the VehicleAssociation event id.
 */

public class VehicleUserPdidAssociation {

    @JsonProperty(value = "EventID")
    private String eventId;
    @JsonProperty(value = "Data")
    private AssociationData data;

    /**
     * getEventId method.
     *
     * @return String
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * setEventId method.
     *
     * @param eventId String
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * getData method.
     *
     * @return AssociationData
     */
    public AssociationData getData() {
        return data;
    }

    /**
     * setData method.
     *
     * @param data AssociationData
     */
    public void setData(AssociationData data) {
        this.data = data;
    }

    /**
     * AssociationData class.
     */
    public static class AssociationData {

        @JsonProperty(value = "userId")
        private String userId;

        /**
         * getUserId method.
         *
         * @return String
         */
        public String getUserId() {
            return userId;
        }

        /**
         * setUserId method.
         *
         * @param userId String
         */
        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

}
