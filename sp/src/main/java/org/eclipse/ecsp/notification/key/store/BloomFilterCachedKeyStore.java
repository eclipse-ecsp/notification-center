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

import org.eclipse.ecsp.notification.utils.CustomBloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * BloomFilterCachedKeyStore class.
 */
public abstract class BloomFilterCachedKeyStore extends AbstractCachedKeyStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(BloomFilterCachedKeyStore.class);
    private static final String ALL_PATTERN = "*";
    private volatile boolean bloomFilterRestored = false;
    private CustomBloomFilter bloomFilter;

    /**
     * BloomFilterCachedKeyStore parameterised constructor.
     *
     * @param properties Properties
     * @param keyStore KeyStore
     * @param prefix String
     */
    protected BloomFilterCachedKeyStore(Properties properties, KeyStore keyStore, String prefix) {
        super(properties, keyStore);
        bloomFilter = new CustomBloomFilter(getInsertionCount());
        // Bloom filter is not restored, so directly query to Store
        Executors.newSingleThreadExecutor(runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.setName("BloomFilterRestore:" + getCacheUser() + ":" + Thread.currentThread().getName());
            return t;
        }).submit(() -> {
            // Restore all the keys from store to Bloom filter
            Set<String> keysInStore = keyStore.getAllKeys(prefix + ALL_PATTERN);
            if (keysInStore != null) {
                LOGGER.info("{} keys found in Store DB to restore in Bloom filter.", keysInStore.size());
                keysInStore.forEach(key -> bloomFilter.put(key));
            }
            LOGGER.info("Bloom filter is restored. Keys In Store {}", keysInStore.size());
            bloomFilterRestored = true;
        });
    }

    /**
     * Method to get Insertion Count.
     *
     * @return int
     */
    protected abstract int getInsertionCount();

    /**
     * Method to get Cache User.
     */
    @Override
    public void put(String key) {
        keyStore.put(key);
        bloomFilter.put(key);
    }

    /**
     * Method to get Cache User.
     */
    @Override
    public boolean keyExists(String key) {
        // Bloom filter is not restored, so directly query to Store
        if (!bloomFilterRestored) {
            LOGGER.debug("bloom filter is not restore, so directly query to Store. Key {}", key);
            return keyStore.keyExists(key);
        }
        boolean keyExists = bloomFilter.mightContain(key);
        if (keyExists) {
            keyExists = keyStore.keyExists(key);
        }
        return keyExists;
    }

    /**
     * Method to get Cache User.
     */
    @Override
    public boolean cacheRestored() {
        return bloomFilterRestored;
    }

}
