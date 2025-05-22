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

import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.cache.redis.RedisConfig;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Caching the event keys in Redis server.
 */
public class RedisStore extends AbstractKeyStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisStore.class);
    private static final String REDIS_NOT_INIT_MSG = "RedisStore is not initialized.";
    Map<String, String> redisClientProps = new HashMap<>();
    private int ttl;
    private TimeUnit ttlTimeUnit;
    private boolean isInitialized;
    private RedissonClient redissonClient;

    /**
     * RedisStore parameterised constructor.
     *
     * @param props the given properties
     * @param storeUser the given store user
     */
    public RedisStore(Properties props, StoreUser storeUser) {
        super(storeUser);
        init(props);
    }

    /**
     * init method to initialize redis client.
     *
     * @param redisProps the given properties
     */
    private void init(Properties redisProps) {
        if (!isInitialized) {
            redisProps.put(PropertyNames.REDIS_DATABASE, getUser().getRedisDatabase());
            redisClientProps = redisProps.entrySet().stream().collect(Collectors.toMap(
                    e -> String.valueOf(e.getKey()),
                    e -> String.valueOf(e.getValue())));
            RedisConfig redisConfig = new RedisConfig();
            redissonClient = redisConfig.builder().build(redisClientProps);
            this.isInitialized = true;
            LOGGER.info("RedisConnection initialized");
        }
    }

    /**
     * destroyPool method to shutdown redis client.
     */
    public void destroyPool() {
        if (!isInitialized) {
            throw new IllegalStateException(REDIS_NOT_INIT_MSG);
        }
        redissonClient.shutdown();
    }

    /**
     * Method to put Key.
     *
     * @param key the given key
     */
    @Override
    public void put(String key) {
        if (!isInitialized) {
            throw new IllegalStateException(REDIS_NOT_INIT_MSG);
        }

        redissonClient.getBucket(key).set("");
        if (ttl > 0) {
            redissonClient.getKeys().expire(key, ttl, ttlTimeUnit);
        }
    }

    /**
     * Method to put Key.
     *
     * @param key the given key
     * @param val the given value
     */
    @Override
    public void put(String key, Object val) {
        if (!isInitialized) {
            throw new IllegalStateException(REDIS_NOT_INIT_MSG);
        }

        redissonClient.getBucket(key).set(val);
        if (ttl > 0) {
            redissonClient.getKeys().expire(key, ttl, ttlTimeUnit);
        }
    }

    /**
     * Method to check if Key exists.
     *
     * @param key the given key
     * @return boolean
     */
    @Override
    public boolean keyExists(String key) {
        if (!isInitialized) {
            throw new IllegalStateException(REDIS_NOT_INIT_MSG);
        }

        return redissonClient.getKeys().countExists(key) > 0 ? true : false;
    }

    /**
     * Method to check if Cache is restored.
     *
     * @return boolean
     */
    @Override
    public Set<String> getAllKeys(String pattern) {

        if (!isInitialized) {
            throw new IllegalStateException(REDIS_NOT_INIT_MSG);
        }
        return new HashSet<>(
                StreamSupport.stream(redissonClient.getKeys().getKeysByPattern(pattern).spliterator(), false)
                        .toList());
    }

    /**
     * Method to Set TTL.
     *
     */
    @Override
    public void setTtl(int ttl, TimeUnit ttlTimeUnit) {
        this.ttl = ttl;
        this.ttlTimeUnit = ttlTimeUnit;
    }

    /**
     * Method to get Value.
     *
     * @param key the given key
     * @return Object
     */
    @Override
    public Object get(String key) {
        if (!isInitialized) {
            throw new IllegalStateException(REDIS_NOT_INIT_MSG);
        }
        return redissonClient.getBucket(key).get();
    }
}
