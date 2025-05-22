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
import org.eclipse.ecsp.domain.notification.AlertsInfo;

/**
 * NotificationMessage class.
 */
public class NotificationMessage {

    @JsonProperty("payload")
    private AlertsInfo alertsInfo;

    private String message;

    /**
     * getMessage method.
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * setMessage method.
     *
     * @param alertMessage String
     */
    public void setMessage(String alertMessage) {
        this.message = alertMessage;
    }

    /**
     * getAlertsInfo method.
     *
     * @return AlertsInfo
     */
    public AlertsInfo getAlertsInfo() {
        return this.alertsInfo;
    }

    /**
     * setAlertsInfo method.
     *
     * @param alertsInfo AlertsInfo
     */
    public void setAlertsInfo(AlertsInfo alertsInfo) {
        this.alertsInfo = alertsInfo;
    }

}
