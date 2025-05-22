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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.redisson.api.RedissonClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * DeduplicatorParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class DeduplicatorParameterizedTest {


    public static final int SIX = 6;
    public static final int THREE = 3;
    public static final int ELEVEN = 11;
    public static final int FOUR = 4;

    /**
     * DeduplicatorParameterizedTest constructor.
     *
     * @param dedupIntvl String
     * @param size int
     * @param alertSize int
     */
    public DeduplicatorParameterizedTest(String dedupIntvl, int size, int alertSize) {
        this.dedupIntvl = dedupIntvl;
        this.size = size;
        this.alertSize = alertSize;
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"1", SIX, THREE},
                {"2", ELEVEN, FOUR},
                {"3", ELEVEN, THREE},

        });

    }

    private final String dedupIntvl;
    private final int size;
    private final int alertSize;
    private RedisStore redisStore;
    private Properties redisProperties;
    private RedissonClient redisClient;
    Map<String, String> redisClientProps = new HashMap<>();
    private static final String KEY_PATTERN = KeyExtractor.DEDUP_KEY_PREFIX + "*";

    @ClassRule
    public static EmbeddedRedisServer redisServer = new EmbeddedRedisServer();

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
    public void testDeduplicatorWithIntervalOne() throws IOException {
        redisProperties.put(NotificationProperty.DEDUP_INTERVAL_MS, dedupIntvl);
        Deduplicator deduplicator = new Deduplicator(redisProperties);
        List<AlertsInfo> alerts = new ArrayList<>();
        for (int i = 1; i < size; i++) {
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
        assertEquals(alertSize, filteredAlerts.size());
    }


}
