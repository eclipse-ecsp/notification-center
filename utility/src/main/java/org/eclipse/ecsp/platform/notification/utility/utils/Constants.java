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

package org.eclipse.ecsp.platform.notification.utility.utils;

/**
 * Constants declarations, variables used in notification-utility.
 *
 * @author MBadoni
 */
public class Constants {
    private Constants() {

    }

    /** Notification ID constant. */
    public static final String NOTIFICATION_ID = "notificationId";
    /** Service constant. */
    public static final String SERVICE = "service";
    /** Group constant. */
    public static final String GROUP = "group";
    /** Brand constant. */
    public static final String BRAND = "brand";
    /** Locale constant. */
    public static final String LOCALE = "locale";
    /** Request ID constant. */
    public static final String REQUEST_ID = "RequestId";
    /** Utility request ID constant. */
    public static final String UTILITY_REQUEST_ID = "notification-config-utility";
    /** Default brand constant. */
    public static final String DEFAULT_BRAND = "default";
    /** JSON extension constant. */
    public static final String JSON_EXTN = ".json";

    /** Notification config file constant. */
    public static final String NOTIFICATION_CONFIG_FILE = "notification-config.json";
    /** Notification template file constant. */
    public static final String NOTIFICATION_TEMPLATE_FILE = "notification-template.json";
    /** Notification template config file constant. */
    public static final String NOTIFICATION_TEMPLATE_CONFIG_FILE = "notification-template-config.json";
    /** Notification grouping config file constant. */
    public static final String NOTIFICATION_GROUPING_CONFIG_FILE = "notification-grouping-config.json";
    /** Error message constant. */
    public static final String ERROR_MSG = "message";
    /** Success message constant. */
    public static final String SUCCESS_MSG = "Processed Successfully";
    /** Non-default notification template cannot be processed message constant. */
    public static final String NON_DEFAULT_NOTIFICATION_TEMPLATE_CAN_NOT_PROCESSED =
            "Notification template config, brand without default is not allowed";
}