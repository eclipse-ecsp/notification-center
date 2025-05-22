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

import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutEntityRequest;
import org.eclipse.ecsp.cache.redis.IgniteCacheRedisImpl;
import org.eclipse.ecsp.notification.utils.ApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * IgniteCacheRedisStore class.
 */
public class IgniteCacheRedisStore implements KeyStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteCacheRedisStore.class);

    private static final int THOUSAND  = 1000;
    private int ttl;

    private IgniteCache cache;

    /**
     * IgniteCacheRedisStore constructor.
     *
     * @param props Properties
     * @param storeUser storeUser
     */
    public IgniteCacheRedisStore(Properties props, StoreUser storeUser) {
        super();
        ApplicationContext appContext = ApplicationContextUtil.getApplicationContext();
        if (appContext == null) {
            throw new IllegalStateException("Application context not initialized");
        }
        cache = appContext.getBean(IgniteCacheRedisImpl.class);

        if (cache == null) {
            throw new IllegalStateException("Application context not initialized");
        }
    }

    /**
     * Method to put Key.
     *
     * @param key the given key
     */
    @Override
    public void put(String key) {
        LOGGER.debug("Put key to ignite cache");
        IgniteCacheEntity entity = new IgniteCacheEntity();
        entity.setValue("");
        PutEntityRequest<IgniteCacheEntity> req = new PutEntityRequest<>();
        req.withKey(key);
        req.withValue(entity);
        req.withTtlMs(ttl);
        cache.putEntity(req);
    }

    /**
     * Method to put Key.
     *
     * @param key the given key
     * @param val the given value
     */
    @Override
    public void put(String key, Object val) {
        IgniteCacheEntity entity = new IgniteCacheEntity();
        entity.setValue(val);
        PutEntityRequest<IgniteCacheEntity> req = new PutEntityRequest<>();
        req.withKey(key);
        req.withValue(entity);
        req.withTtlMs(ttl);
        cache.putEntity(req);
    }

    /**
     * Method to check if Key exists.
     *
     * @param key the given key
     * @return boolean
     */
    @Override
    public boolean keyExists(String key) {

        return get(key) != null ? true : false;
    }

    /**
     * Method to check if Cache is restored.
     *
     * @param pattern the given pattern
     * @return boolean
     */
    @Override
    public Set<String> getAllKeys(String pattern) {

        return cache.getKeyValuePairsForRegex(pattern, Optional.of(true)).keySet();
    }

    /**
     * Method to set TTL.
     *
     * @param ttl the given ttl
     * @param ttlTimeUnit the given ttlTimeUnit
     */
    @Override
    public void setTtl(int ttl, TimeUnit ttlTimeUnit) {
        this.ttl = ttl * THOUSAND;

    }

    /**
     * Method to get.
     *
     * @param key the given key
     * @return Object
     */
    @Override
    public Object get(String key) {
        LOGGER.debug("Getting from ignite cache for key {}", key);
        IgniteCacheEntity entity = cache.getEntity(key);
        return entity != null ? entity.getValue() : null;
    }

    /**
     * Method to get User.
     *
     * @return StoreUser
     */
    @Override
    public StoreUser getUser() {
        // The store user is not used in Ignite Cache
        return null;
    }

}
