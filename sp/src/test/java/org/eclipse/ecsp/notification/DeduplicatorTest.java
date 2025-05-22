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

package org.eclipse.ecsp.notification;

import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.cache.redis.EmbeddedRedisServer;
import org.eclipse.ecsp.cache.redis.RedisConfig;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.notification.duplication.Deduplicator;
import org.eclipse.ecsp.notification.duplication.KeyExtractor;
import org.eclipse.ecsp.notification.duplication.KeyExtractorFactory;
import org.eclipse.ecsp.notification.key.store.RedisStore;
import org.eclipse.ecsp.notification.key.store.StoreUser;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.redisson.api.RedissonClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test deduplicator.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class DeduplicatorTest {
    private static final String KEY_PATTERN = KeyExtractor.DEDUP_KEY_PREFIX + "*";
    @ClassRule
    public static EmbeddedRedisServer redisServer = new EmbeddedRedisServer();
    Map<String, String> redisClientProps = new HashMap<>();
    private RedisStore redisStore;
    private Properties redisProperties;
    private RedissonClient redisClient;

    /**
     * set up before test.
     */
    @Before
    public void setUp() throws IOException {
        redisProperties = NotificationTestUtils.loadProperties("/deduplicator.properties");
        redisProperties.put(PropertyNames.REDIS_SINGLE_ENDPOINT, "127.0.0.1:" + redisServer.getPort());
        redisStore = new RedisStore(redisProperties, StoreUser.NOTIFICATION_ALERT_DEDUPLICATOR);
        redisStore.put("hello");
        assertEquals(true, redisStore.keyExists("hello"));

        redisClientProps = redisProperties.entrySet().stream().collect(Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue())));
        RedisConfig redisConfig = new RedisConfig();
        redisClient = redisConfig.builder().build(redisClientProps);
    }

    /**
     * CLean up after test.
     */
    @After
    public void clearRedis() {
        redisClient.getKeys().deleteByPattern(KEY_PATTERN);
        assertTrue(redisStore.getAllKeys(KEY_PATTERN).isEmpty());
        KeyExtractorFactory.reset();
    }

    @Test
    public void testDeduplicatorWithIntervalZero() throws IOException {
        // Default interval is 0
        Deduplicator deduplicator = new Deduplicator(redisProperties);
        List<AlertsInfo> alerts = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            AlertsInfo geofenceAlert = new AlertsInfo();
            Data d1 = new Data();
            d1.set("id", "geofenceId");
            geofenceAlert.setAlertsData(d1);
            geofenceAlert.setEventID(EventMetadata.EventID.GEOFENCE.toString());
            geofenceAlert.setPdid("H0123");
            geofenceAlert.setTimestamp(i);
            alerts.add(geofenceAlert);
        }
        List<AlertsInfo> filteredAlerts = deduplicator.filterDuplicateAlert(alerts);
        assertEquals(10, filteredAlerts.size());
    }

    @Test
    public void testDeduplicatorRestore() throws IOException {
        redisProperties.put(NotificationProperty.DEDUP_INTERVAL_MS, "0");
        List<AlertsInfo> alerts = new ArrayList<>();
        int interval = Integer.parseInt(this.redisProperties.getProperty(NotificationProperty.DEDUP_INTERVAL_MS, "0"));
        KeyExtractorFactory.init(interval);

        for (int i = 1; i < 11; i++) {
            AlertsInfo geofenceAlert = new AlertsInfo();
            Data d1 = new Data();
            d1.set("id", "geofenceId");
            geofenceAlert.setAlertsData(d1);
            geofenceAlert.setEventID(EventMetadata.EventID.GEOFENCE.toString());
            geofenceAlert.setPdid("H0123");
            geofenceAlert.setTimestamp(i);
            alerts.add(geofenceAlert);
            if (i == 1 || i == 4 || i == 7 || i == 10) {
                redisStore.put(KeyExtractorFactory.getCurrentKey(geofenceAlert), geofenceAlert.getTimestamp());
            }
        }
        Deduplicator deduplicator = new Deduplicator(redisProperties);
        List<AlertsInfo> filteredAlerts = deduplicator.filterDuplicateAlert(alerts);
        assertEquals(6, filteredAlerts.size());
    }

    @Test
    public void testDeduplicatorBloomFilterRestored() throws IOException, InterruptedException {
        for (int i = 0; i < 10000; i++) {
            redisStore.put(KeyExtractor.DEDUP_KEY_PREFIX + i);
        }
        // Deduplicatpr should restore the keys which are already in Redis.
        redisProperties.put(NotificationProperty.DEDUP_INTERVAL_MS, "0");
        Deduplicator deduplicator = new Deduplicator(redisProperties);
        // So this alert should be filtered out.
        assertEquals(false, deduplicator.cacheRestored());
        long intervalForRestore = 5000;
        System.out.println("deduplicator.isBloomFilterRestored(): " + deduplicator.cacheRestored()
                + " Waiting for these many milis to restored it: " + intervalForRestore);
        Thread.sleep(5000);
        assertEquals("Faile to restore cache", true, deduplicator.cacheRestored());
        assertEquals("Failed to restore cache", 10000, redisStore.getAllKeys(KEY_PATTERN).size());
    }

    @Test
    public void testDeduplicatorForBloomFilterInsertionRate() throws IOException {
        List<AlertsInfo> alerts = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            AlertsInfo genericAlert = new AlertsInfo();
            genericAlert.setEventID(EventMetadata.EventID.CURFEW.toString());
            genericAlert.setPdid("I0123");
            genericAlert.setTimestamp(System.nanoTime());
            alerts.add(genericAlert);
        }
        Deduplicator deduplicator = new Deduplicator(redisProperties);
        List<AlertsInfo> filteredAlerts = deduplicator.filterDuplicateAlert(alerts);
        assertEquals("Unique keys filtered", alerts.size(), filteredAlerts.size());
    }
}
