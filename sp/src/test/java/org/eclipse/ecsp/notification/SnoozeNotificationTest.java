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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.eclipse.ecsp.analytics.stream.base.Launcher;
import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.discovery.PropBasedDiscoveryServiceImpl;
import org.eclipse.ecsp.analytics.stream.base.idgen.MessageIdGenerator;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaStreamsApplicationTestBase;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.browser.MqttBrowserNotifier;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.config.TestSecurityLib;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.NotificationSchedulerPayload;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * SnoozeNotificationTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSecurityLib.class, Launcher.class})
@TestPropertySource("/snoozeNotification-integration-test.properties")
@SuppressWarnings("checkstyle:MagicNumber")
public class SnoozeNotificationTest extends KafkaStreamsApplicationTestBase {
    private static final  String[] PDID = {"HUR902N3KHU7U7", "HUXOIDDN4HUN19", "HUXOIDDN4HUN20", "HUXOIDDN4HUN21",
        "HUXOIDDN4HUN22"};
    private static final  Logger LOGGER = LoggerFactory.getLogger(SnoozeNotificationTest.class);

    ObjectMapper mapper = new ObjectMapper();
    @Mock
    StreamProcessingContext<IgniteKey<?>, IgniteEvent> spc;

    private Properties snoozeNotificationProperties;

    private final String notificationTopic = "notification";
    private final String internalTopic = "internal";
    private final String notificationSpDlqTopic = "notification-sp-dlq";
    private final String deviceStatusNotificationSpTopic = "device-status-notification-sp";
    private final String httpsIntegHigh = "https-integ-high";
    private final String dffEventFeedbackTopic = "DffFeedBackTopic";
    private final String schedulerSourceTopic = "scheduler";


    @Autowired
    private Datastore datastore;

    @Autowired
    private NotificationTemplateDAO notificationTemplateDao;

    @Autowired
    private NotificationTemplateConfigDAO notificationTemplateConfigDao;

    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;

    @Autowired
    private NotificationConfigDAO notificationConfigDao;

    @Autowired
    private UserProfileDAO userProfileDao;

    @Autowired
    private NotificationBufferDao notificationBufferDao;

    @Autowired
    private MessageIdGenerator msgIdGenerator;

    @Value("${service.name}")
    private String serviceName;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();


        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        snoozeNotificationProperties = new Properties();
        InputStream inputStream = SnoozeNotificationTest.class
            .getResourceAsStream("/snoozeNotification-integration-test.properties");
        snoozeNotificationProperties.load(inputStream);

        createTopics(notificationTopic, internalTopic, notificationSpDlqTopic, deviceStatusNotificationSpTopic,
            httpsIntegHigh, dffEventFeedbackTopic, schedulerSourceTopic);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-consumer");

        ksProps.put(PropertyNames.DISCOVERY_SERVICE_IMPL, PropBasedDiscoveryServiceImpl.class.getName());
        ksProps.put(NotificationProperty.SOURCE_TOPIC, notificationTopic);
        ksProps.put(PropertyNames.APPLICATION_ID, "pt");

        ksProps.put(PropertyNames.EVENT_TRANSFORMER_CLASSES,
            snoozeNotificationProperties.getProperty(PropertyNames.EVENT_TRANSFORMER_CLASSES));
        // ksProps.put(PropertyNames.IGNITE_KEY_TRANSFORMER,
        // snoozeNotificationProperties.getProperty(PropertyNames.IGNITE_KEY_TRANSFORMER));
        ksProps.put(PropertyNames.INGESTION_SERIALIZER_CLASS,
            snoozeNotificationProperties.getProperty(PropertyNames.INGESTION_SERIALIZER_CLASS));

        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-consumer");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            Serdes.String().deserializer().getClass().getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            Serdes.String().deserializer().getClass().getName());

        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            Serdes.String().serializer().getClass().getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            Serdes.String().serializer().getClass().getName());

        NotificationDao dao = new MongoDbClient();
        dao.init(snoozeNotificationProperties);
        ((MongoDbClient) dao).setDatastore(datastore);

        saveToDao();

    }

    private void saveToDao() throws IOException {
        String notificationTemplateData = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/snooz-apiPush-notification-template.json"),
            "UTF-8");

        String notificationTemp = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/SnoozNotificationTemplate.json"),
            "UTF-8");

        NotificationTemplate[] notificationTemplate = mapper.readValue(notificationTemp, NotificationTemplate[].class);
        notificationTemplateDao.saveAll(notificationTemplate);

        String notificationTemplateConfigData = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/SnoozNotificationTemplateConfig.json"),
            "UTF-8");

        NotificationTemplateConfig[] notificationTemplateConfig = mapper.readValue(notificationTemplateConfigData,
            NotificationTemplateConfig[].class);
        notificationTemplateConfigDao.saveAll(notificationTemplateConfig);

        String notificationGroupData = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/SnoozNotificationGroup.json"),
            "UTF-8");

        NotificationGrouping[] notificationGroup = mapper.readValue(notificationGroupData,
            NotificationGrouping[].class);
        notificationGroupingDao.saveAll(notificationGroup);

        String notificationConfigData = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/snoozeNotification-notification-config.json"),
            "UTF-8");

        NotificationConfig notificationConfig = mapper.readValue(notificationConfigData,
            NotificationConfig.class);
        notificationConfigDao.save(notificationConfig);

        String userProfileData = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/snoozeNotification-user-profile.json"),
            "UTF-8");

        UserProfile userProfile = mapper.readValue(userProfileData,
            UserProfile.class);
        userProfile.setUserId("HUR902N3KHU7U7");
        userProfileDao.save(userProfile);
    }

    @Test
    @Ignore("failing reason unclear")
    public void testSnoozeNotification() throws Exception {

        ksProps.put(PropertyNames.EVENT_TRANSFORMER_CLASSES,
            snoozeNotificationProperties.getProperty(PropertyNames.EVENT_TRANSFORMER_CLASSES));
        ksProps.put(PropertyNames.SERVICE_STREAM_PROCESSORS,
            snoozeNotificationProperties.getProperty(PropertyNames.SERVICE_STREAM_PROCESSORS));
        ksProps.put(PropertyNames.APPLICATION_ID, "chaining" + System.currentTimeMillis());
        launchApplication();

        String inputData =
            IOUtils.toString(SnoozeNotificationTest.class.getResourceAsStream("/geoFence_in.json"), "UTF-8");
        LOGGER.debug("The input event file is : " + inputData);
        sendMessages(notificationTopic, producerProps, PDID[0], inputData);
        List<String[]> igniteEventResponse = getMessages(schedulerSourceTopic, consumerProps, 1, 120000);
        assertEquals(1, igniteEventResponse.size());
        assertEquals(1, notificationBufferDao.findAll().size());
    }

    @Ignore("testSchedulerOpStatusEventSaveSchedulerId")
    @Test
    public void testSchedulerOpStatusEventSaveSchedulerId() throws Exception {

        String notificationBufferData = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/sn_BufferNotification.json"),
            "UTF-8");
        NotificationBuffer notificationBuffer = mapper.readValue(notificationBufferData, NotificationBuffer.class);
        notificationBufferDao.save(notificationBuffer);
        byte[] input = createScheduleOpsStatusIgniteEvent();
        // sendMessages(SCHEDULER_CALLBACK_TOPIC, producerProps, PDID[0],
        // input);
        sendMessages(schedulerSourceTopic, producerProps, Arrays.asList(PDID[0].getBytes(), input));
        assertEquals("scheduleId123", notificationBufferDao.findBySchedulerId(
                "scheduleId123").getSchedulerId());
    }

    private byte[] createScheduleOpsStatusIgniteEvent() throws JsonProcessingException {


        String vehicleId = "HUR902N3KHU7U7";
        IgniteEventImpl createScheduleIgniteEvent = new IgniteEventImpl();
        createScheduleIgniteEvent.setEventId(org.eclipse.ecsp.domain.EventID.CREATE_SCHEDULE_EVENT);
        createScheduleIgniteEvent.setTimestamp(System.currentTimeMillis());
        createScheduleIgniteEvent.setSourceDeviceId(vehicleId);
        createScheduleIgniteEvent.setVehicleId(vehicleId);
        createScheduleIgniteEvent.setVersion(Version.V1_0);
        createScheduleIgniteEvent.setRequestId(UUID.randomUUID().toString());
        createScheduleIgniteEvent.setBizTransactionId(UUID.randomUUID().toString());
        createScheduleIgniteEvent.setMessageId(msgIdGenerator.generateUniqueMsgId(vehicleId));
        CreateScheduleEventData createScheduleEventData = new CreateScheduleEventData();
        createScheduleEventData.setFiringCount(1);
        createScheduleEventData.setServiceName(serviceName);
        ObjectMapper mapper1 = new ObjectMapper();
        NotificationSchedulerPayload payload =
            new NotificationSchedulerPayload("AWh_YZbs3JWC8U6WROnj", "HUR902N3KHU7U7",
                ChannelType.API_PUSH, "all", "self");
        IgniteStringKey key = new IgniteStringKey("HUR902N3KHU7U7");
        byte[] payloadInBytes = mapper1.writeValueAsBytes(payload);
        createScheduleEventData.setNotificationPayload(payloadInBytes);
        createScheduleEventData.setNotificationTopic(schedulerSourceTopic);
        createScheduleEventData.setNotificationKey(key);
        createScheduleEventData.setInitialDelayMs(0);
        createScheduleEventData.setRecurrenceType(CreateScheduleEventData.RecurrenceType.CUSTOM_MS);
        createScheduleEventData.setRecurrenceDelayMs(60000);
        createScheduleIgniteEvent.setEventData(createScheduleEventData);

        ScheduleOpStatusEventData scheduleOpStatusEventData =
            new ScheduleOpStatusEventData("scheduleId123", ScheduleStatus.CREATE,
                createScheduleIgniteEvent, true);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId(EventID.SCHEDULE_OP_STATUS_EVENT);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        igniteEvent.setRequestId(createScheduleIgniteEvent.getRequestId());
        igniteEvent.setBizTransactionId(createScheduleIgniteEvent.getBizTransactionId());
        igniteEvent.setMessageId(msgIdGenerator.generateUniqueMsgId(serviceName));
        igniteEvent.setEventData(scheduleOpStatusEventData);
        igniteEvent.setVehicleId(createScheduleIgniteEvent.getVehicleId());
        igniteEvent.setVersion(Version.V1_0);

        byte[] input = getEventBlob(igniteEvent);
        return input;
    }

    private byte[] getEventBlob(IgniteEventImpl event) throws JsonProcessingException {
        GenericIgniteEventTransformer eventTransformer = new GenericIgniteEventTransformer();
        byte[] eventData = eventTransformer.toBlob(event);

        return eventData;
    }

    @BeforeClass
    public static void updateMqttBrokerUrl() {
        MqttBrowserNotifier.setOverRiddenBrokerUrl(MQTT_SERVER.getConnectionString());
    }

    /**
     * post test cleanup.
     */
    @After
    public void tearDown() {
        notificationTemplateDao.deleteAll();
        notificationTemplateConfigDao.deleteAll();
        notificationGroupingDao.deleteAll();
        notificationConfigDao.deleteAll();
        notificationConfigDao.deleteAll();
    }
}
