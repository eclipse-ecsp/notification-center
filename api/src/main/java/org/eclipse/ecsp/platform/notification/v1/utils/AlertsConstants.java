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

package org.eclipse.ecsp.platform.notification.v1.utils;

/**
 * Alerts constants.
 */
public class AlertsConstants {

    private AlertsConstants() {}

    /** Channel push constant. */
    public static final String CHANNEL_PUSH = "MOBILE_APP_PUSH";

    /** Channel browser constant. */
    public static final String CHANNEL_BROWSER = "BROWSER";

    /** Channel email constant. */
    public static final String CHANNEL_EMAIL = "EMAIL";

    /** Channel SMS constant. */
    public static final String CHANNEL_SMS = "SMS";

    /** Default message constant. */
    public static final String DEFAULT_MESSAGE = "Default message is unavailable.";

    /** Invalid alert ID constant. */
    public static final String INVALID_ALERT_ID = "Invalid ids provided to mark the alerts as read/unread";

    /** Alerts no data found error message constant. */
    public static final String ALERTS_NO_DATA_FOUND_ERROR_MSG = "No alerts found";

    /** Invalid alert ID delete constant. */
    public static final String INVALID_ALERT_ID_DELETE = "Invalid ids provided to delete alerts";

    /** Alerts read and unread invalid request constant. */
    public static final String ALERTS_READ_AND_UNREAD_INVALID_REQUEST =
            "Invalid request. Ids in the read and unread list should not have a match.";

    /** Alerts null body invalid request constant. */
    public static final String ALERTS_NULL_BODY_INVALID_REQUEST = "Invalid request. Body must not be empty";
}