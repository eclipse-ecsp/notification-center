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

package org.eclipse.ecsp.notification.key.store;

import org.eclipse.ecsp.notification.config.RedisDbConfig;

/**
 * StoreUser enum.
 */
public enum StoreUser {
    NOTIFICATION_ASSOCIATION {
        @Override
        public String getRedisDatabase() {
            return String.valueOf(RedisDbConfig.NOTIF_ASSOCIATION_REDIS_DB_NUM);
        }
    },
    NOTIFICATION_ALERT_DEDUPLICATOR {
        @Override
        public String getRedisDatabase() {
            return String.valueOf(RedisDbConfig.NOTIF_DEDUP_REDIS_DB_NUM);
        }
    },
    NOTIFICATION_EMAIL_BOUNCE_HANDLER {
        @Override
        public String getRedisDatabase() {
            return String.valueOf(RedisDbConfig.NOTIF_BOUNCED_EMAIL_REDIS_DB_NUM);
        }
    },
    NOTIFICATION_DEFAULT_USER {
        @Override
        public String getRedisDatabase() {
            return String.valueOf(RedisDbConfig.NOTIF_DEFAULT_REDIS_DB_NUM);
        }
    };

    /**
     * Method to get Redis Database.
     *
     * @return String
     */
    public abstract String getRedisDatabase();
}
