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

package org.eclipse.ecsp.notification.aws.ses;

import org.eclipse.ecsp.notification.key.store.BloomFilterCachedKeyStore;
import org.eclipse.ecsp.notification.key.store.KeyStore;
import org.eclipse.ecsp.notification.key.store.StoreUser;
import org.eclipse.ecsp.notification.utils.NotificationProperty;

import java.util.Properties;

/**
 * BouncedEmailStore class.
 */
public class BouncedEmailStore extends BloomFilterCachedKeyStore {
    private static final String PREFIX = "BOUNCED_EMAIL_";

    /**
     * Constructor for BouncedEmailStore.
     *
     * @param properties the given properties
     * @param keyStore   the given keyStore
     */
    public BouncedEmailStore(Properties properties, KeyStore keyStore) {
        super(properties, keyStore, PREFIX);

    }

    /**
     * Insertion count.
     *
     * @return count
     */
    @Override
    protected int getInsertionCount() {
        return Integer.parseInt(properties.getProperty(NotificationProperty.EMAIL_BOUNCE_BLOOM_FILTER_COUNT, "100"));
    }

    /**
     * Get cache user.
     *
     * @return StoreUser
     */
    @Override
    public StoreUser getCacheUser() {
        return StoreUser.NOTIFICATION_EMAIL_BOUNCE_HANDLER;
    }

}
