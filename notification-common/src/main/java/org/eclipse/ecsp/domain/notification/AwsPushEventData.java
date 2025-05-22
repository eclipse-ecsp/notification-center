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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * AwsPushEventData class.
 */
public class AwsPushEventData extends AlertEventData {
    @JsonProperty(NotificationConstants.APNS)
    private String apns;
    @JsonProperty(NotificationConstants.GCM)
    private String gcm;

    /**
     * This method is a getter for apns.
     *
     * @return String
     */

    public String getApns() {
        return apns;
    }

    /**
     * This method is a setter for apns.
     *
     * @param apns : String
     */

    public void setApns(String apns) {
        this.apns = apns;
    }

    /**
     * This method is a getter for gcm.
     *
     * @return String
     */

    public String getGcm() {
        return gcm;
    }

    /**
     * This method is a setter for gcm.
     *
     * @param gcm : String
     */

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }
}