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

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.cache.redis.EmbeddedRedisServer;
import org.eclipse.ecsp.cache.redis.RedisConfig;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.CampaignStatusDataV1_0;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.duplication.Deduplicator;
import org.eclipse.ecsp.notification.duplication.KeyExtractorFactory;
import org.eclipse.ecsp.notification.feedback.NotificationFeedbackHandler;
import org.eclipse.ecsp.notification.key.store.RedisStore;
import org.eclipse.ecsp.notification.key.store.StoreUser;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.CANCELED;
import static org.eclipse.ecsp.domain.notification.commons.EventID.CAMPAIGN_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.CAMPAIGN_STATUS_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_TYPE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.STATUS;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * VehicleCampaignNotificationTest.
 */
public class VehicleCampaignNotificationTest {

    @InjectMocks
    private VehicleInfoNotification vehicleInfoNotification;

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;

    @Mock
    private BiPredicate<IgniteEventStreamProcessor,
            StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;

    @ClassRule
    public static EmbeddedRedisServer redisServer = new EmbeddedRedisServer();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Deduplicator deduplicator;

    @Value("${is.feedback.enabled}")
    private boolean isFeedBackEnabled;
    
    @Mock
    private AlertsHistoryAssistant alertsHistoryAssistant;

    private RedisStore redisStore;
    private RedissonClient redisClient;
    Map<String, String> redisClientProps = new HashMap<>();

    private static final String KEY_PATTERN = "*" + UNDERSCORE + STATUS;
    private static final String VEHICLE_ID = "testVehicle";
    private Map<String, String> eventMap = new HashMap<String, String>();

    /**
     * set up test.
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        Properties properties = NotificationTestUtils.loadProperties("/application.properties");
        properties.put(PropertyNames.REDIS_SINGLE_ENDPOINT, "127.0.0.1:" + redisServer.getPort());
        redisStore = new RedisStore(properties, StoreUser.NOTIFICATION_DEFAULT_USER);

        redisClientProps = properties.entrySet().stream().collect(Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue())));
        redisClient = new RedisConfig().builder().build(redisClientProps);
        ReflectionTestUtils.setField(vehicleInfoNotification, "campaignStore", new CampaignStore(properties));
        eventMap.put("CAMPAIGN_EVENT", "CAMPAIGN_EVENT");
        eventMap.put("CampaignStatus", "CampaignStatus");

        ReflectionTestUtils.setField(vehicleInfoNotification, "eventIdMap", eventMap);

        NotificationFeedbackHandler.init(ctxt,
                isFeedBackEnabled, false, "notification-feedback");
    }

    /**
     * clean up after test.
     */
    @After
    public void clearRedis() {
        redisClient.getKeys().deleteByPattern(KEY_PATTERN);
        assertTrue(redisStore.getAllKeys(KEY_PATTERN).isEmpty());
        KeyExtractorFactory.reset();
    }

    @Test
    public void processCancelCampaignEvent() {
        cancelCampaign(randomUUID().toString());
    }

    @Test(expected = Exception.class)
    public void testdestroyPool() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RedisStore is not initialized.");
        ReflectionTestUtils.setField(redisStore, "isInitialized", false);
        redisStore.destroyPool();
    }

    @Test(expected = Exception.class)
    public void testput() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RedisStore is not initialized.");
        ReflectionTestUtils.setField(redisStore, "isInitialized", false);
        redisStore.put("ewq");
    }

    @Test(expected = Exception.class)
    public void testkeyExistsl() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RedisStore is not initialized.");
        ReflectionTestUtils.setField(redisStore, "isInitialized", false);
        redisStore.keyExists("we");
    }

    @Test(expected = Exception.class)
    public void testgetAllKeys() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RedisStore is not initialized.");
        ReflectionTestUtils.setField(redisStore, "isInitialized", false);
        redisStore.getAllKeys("dqwd");
    }

    @Test(expected = Exception.class)
    public void testput2() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RedisStore is not initialized.");
        ReflectionTestUtils.setField(redisStore, "isInitialized", false);
        redisStore.put("", null);
    }

    @Test(expected = Exception.class)
    public void testget() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RedisStore is not initialized.");
        ReflectionTestUtils.setField(redisStore, "isInitialized", false);
        redisStore.get("ewqe");
    }

    @Test
    public void testputCamp() throws IOException {
        Properties properties = NotificationTestUtils.loadProperties("/application.properties");
        Assertions.assertNotNull(properties);
        properties.put(PropertyNames.REDIS_SINGLE_ENDPOINT, "127.0.0.1:" + redisServer.getPort());
        CampaignStore c = new CampaignStore(properties);
        c.put("ewqe");
        c.keyExists("dsad");
        c.cacheRestored();
        c.getCacheUser();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void processEventsForCanceledCampaign_withoutFeedback() {
        String campaignId = randomUUID().toString();
        cancelCampaign(campaignId);

        when(ctxt.streamName()).thenReturn("notification");
        when(alertsHistoryAssistant.createBasicAlertHistory(any())).thenReturn(new AlertsHistoryInfo());
        when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer((Answer<List<AlertsInfo>>) invocation -> {
            return (List<AlertsInfo>) invocation.getArguments()[0];
        });
        vehicleInfoNotification.process(
            new Record(new IgniteStringKey(VEHICLE_ID), getAlertIgniteEvent(campaignId), System.currentTimeMillis()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void processEventsForCanceledCampaign_withFeedback() {
        String campaignId = randomUUID().toString();
        cancelCampaign(campaignId);

        when(ctxt.streamName()).thenReturn("notification");
        doNothing().when(ctxt).forwardDirectly(any(IgniteStringKey.class), any(IgniteEventImpl.class), any());
        when(alertsHistoryAssistant.createBasicAlertHistory(any())).thenReturn(new AlertsHistoryInfo());
        when(deduplicator.filterDuplicateAlert(anyList())).thenAnswer(
            (Answer<List<AlertsInfo>>) invocation -> (List<AlertsInfo>) invocation.getArguments()[0]);

        IgniteEvent event = getAlertIgniteEvent(campaignId);
        ((GenericEventData) event.getEventData()).set("feedbackTopic", "testFeedbackTopic");
        vehicleInfoNotification.process(new Record(new IgniteStringKey(VEHICLE_ID), event, System.currentTimeMillis()));
    }

    private void cancelCampaign(String campaignId) {
        when(ctxt.streamName()).thenReturn("campaign-status");
        IgniteEventImpl igniteStatusEvent = getCancelStatusEvent(campaignId);
        vehicleInfoNotification.process(
            new Record(new IgniteStringKey(campaignId), igniteStatusEvent, System.currentTimeMillis()));
        assertTrue(redisStore.keyExists(
            ((CampaignStatusDataV1_0) igniteStatusEvent.getEventData()).getCampaignId() + UNDERSCORE + STATUS));
    }

    @NotNull
    private IgniteEventImpl getCancelStatusEvent(String campaignId) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId(CAMPAIGN_STATUS_EVENT);
        CampaignStatusDataV1_0 campaignData = new CampaignStatusDataV1_0();
        campaignData.setCampaignId(campaignId);
        campaignData.setStatus(CANCELED.name());
        campaignData.setType(NOTIFICATION_TYPE);
        igniteEvent.setEventData(campaignData);
        return igniteEvent;
    }

    @NotNull
    private IgniteEventImpl getAlertIgniteEvent(String campaignId) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId(VEHICLE_ID);
        igniteEvent.setEventId(CAMPAIGN_EVENT);
        GenericEventData genericEventData = new GenericEventData();
        genericEventData.set("notificationId", "lowFuel");
        genericEventData.set("userId", "testUser");
        genericEventData.set("campaignId", campaignId);
        genericEventData.set("UserNotification", false);
        igniteEvent.setEventData(genericEventData);
        return igniteEvent;
    }

}