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

package org.eclipse.ecsp.notification.config;

/**
 * RedisDbConfig class.
 */
public class RedisDbConfig {
    /**
     * Default Redis database number.
     */
    public static final int NOTIF_DEFAULT_REDIS_DB_NUM = 0;
    /**
     * Notifcation component, Deduplicator is using below database number.
     */
    public static final int NOTIF_DEDUP_REDIS_DB_NUM = 10;
    /**
     * Notifcation component, Association is using below database number.
     */
    public static final int NOTIF_ASSOCIATION_REDIS_DB_NUM = 11;
    /**
     * Notifcation component, BouncedEmail is using below database number.
     */
    public static final int NOTIF_BOUNCED_EMAIL_REDIS_DB_NUM = 12;
    /**
     *  Pulse component database number.
     */
    public static final int PULSE_DB_NUM = 5;

    // Redis database number for the user component
    private RedisDbConfig() {
    }

    /**
     * Create RedisDbConfig object.
     *
     * @return RedisDbConfig object
     */
    public static RedisDbConfig createRedisDbConfig() {
        return new RedisDbConfig();
    }
}
