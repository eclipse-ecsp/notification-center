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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import dev.morphia.Datastore;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.eclipse.ecsp.analytics.stream.base.Launcher;
import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.discovery.PropBasedDiscoveryServiceImpl;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaStreamsApplicationTestBase;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.IVMRequest;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.EventDataDeSerializer;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.browser.MqttBrowserNotifier;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.dao.IVMRequestDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * IVM test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VehicleProfileClientTestBean.class, Launcher.class})
@TestPropertySource("/ivm-integration-test.properties")
@Ignore
public class IvmIntegrationTest extends KafkaStreamsApplicationTestBase {

    private static final  Logger LOGGER = LoggerFactory.getLogger(IvmIntegrationTest.class);

    ObjectMapper mapper = new ObjectMapper();
    @Mock
    StreamProcessingContext<IgniteKey<?>, IgniteEvent> spc;

    private Properties apiPushProperties;

    private final String notificationTopic = "notification";
    private final String postAlertTopic = "post-alerts";
    private final String internalTopic = "internal";
    private final String notificationSpDlqTopic = "notification-sp-dlq";
    private final String deviceStatusNotificationTopic = "device-status-notification";
    private final String httpsIntegHigh = "https-integ-high";

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
    private IVMRequestDAO ivmRequestDao;

    @Autowired
    private AlertsHistoryDao alertsHistoryDao;

    private static final String PDID = "HUXOIDDN4HUN18";
    private static final String MQTT_TOPIC = "ivm/DEVICE123/notification";

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPropertyInclusion(Include.NON_NULL);

        apiPushProperties = new Properties();
        InputStream inputStream = IvmIntegrationTest.class
            .getResourceAsStream("/apiPush-integration-test.properties");
        apiPushProperties.load(inputStream);

        createTopics(notificationTopic, postAlertTopic, internalTopic, notificationSpDlqTopic,
            deviceStatusNotificationTopic,
            httpsIntegHigh);
        subscibeToMqttTopic(MQTT_TOPIC);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-consumer");

        ksProps.put(PropertyNames.DISCOVERY_SERVICE_IMPL, PropBasedDiscoveryServiceImpl.class.getName());
        ksProps.put(NotificationProperty.SOURCE_TOPIC, notificationTopic);
        ksProps.put(PropertyNames.APPLICATION_ID, "pt");

        ksProps.put(PropertyNames.EVENT_TRANSFORMER_CLASSES,
            apiPushProperties.getProperty(PropertyNames.EVENT_TRANSFORMER_CLASSES));
        ksProps.put(PropertyNames.INGESTION_SERIALIZER_CLASS,
            apiPushProperties.getProperty(PropertyNames.INGESTION_SERIALIZER_CLASS));

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
        dao.init(apiPushProperties);
        ((MongoDbClient) dao).setDatastore(datastore);

        saveToDao();

    }

    private void saveToDao() throws IOException {
        String notificationTemp = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/NotificationTemplate.json"),
            "UTF-8");

        NotificationTemplate[] notificationTemplate = mapper.readValue(notificationTemp, NotificationTemplate[].class);
        notificationTemplateDao.saveAll(notificationTemplate);

        String notificationTemplateConfigData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/NotificationTemplateConfig.json"),
            "UTF-8");

        NotificationTemplateConfig[] notificationTemplateConfig = mapper.readValue(notificationTemplateConfigData,
            NotificationTemplateConfig[].class);
        notificationTemplateConfigDao.saveAll(notificationTemplateConfig);

        String notificationGroupData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/NotificationGroup.json"),
            "UTF-8");

        NotificationGrouping[] notificationGroup = mapper.readValue(notificationGroupData,
            NotificationGrouping[].class);
        notificationGroupingDao.saveAll(notificationGroup);

        String notificationConfigData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/ivm-notification-config.json"),
            "UTF-8");

        NotificationConfig notificationConfig = mapper.readValue(notificationConfigData,
            NotificationConfig.class);
        notificationConfigDao.save(notificationConfig);

        String userProfileData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/UserProfile.json"),
            "UTF-8");

        UserProfile userProfile = mapper.readValue(userProfileData,
            UserProfile.class);
        userProfile.setUserId("HUXOIDDN4HUN18");
        userProfileDao.save(userProfile);
    }

    /**
     * Test to create a single feed in DB and verify there is no exception while
     * saving the data to MongoDB.
     *
     * @throws InterruptedException ex
     * @throws ExecutionException ex
     * @throws IOException ex
     * @throws TimeoutException ex
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    @Ignore("failing reason unclear")
    public void testIvmNotification() throws Exception {

        ksProps.put(PropertyNames.EVENT_TRANSFORMER_CLASSES,
            apiPushProperties.getProperty(PropertyNames.EVENT_TRANSFORMER_CLASSES));
        ksProps.put(PropertyNames.SERVICE_STREAM_PROCESSORS,
            apiPushProperties.getProperty(PropertyNames.SERVICE_STREAM_PROCESSORS));
        ksProps.put(PropertyNames.APPLICATION_ID, "chaining" + System.currentTimeMillis());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);


        ObjectMapper mapper = new ObjectMapper();
        EventDataDeSerializer eventDataSerializer = new EventDataDeSerializer();
        SimpleModule module = new SimpleModule("PolymorphicEventDataModule", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(EventData.class, eventDataSerializer);
        mapper.registerModule(module);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        String deviceConnectionStatusEvent = IOUtils
            .toString(VehicleInfoNotificationTest.class.getResourceAsStream("/ivm-device-status.json"), "UTF-8");
        //launchApplication();

        String inputData = IOUtils.toString(IvmIntegrationTest.class.getResourceAsStream("/ivmPush.json"),
            "UTF-8");
        LOGGER.debug("The input event file is : " + inputData);

        sendMessages(deviceStatusNotificationTopic, producerProps,
            Arrays.asList(PDID.getBytes(),
                deviceConnectionStatusEvent.getBytes()));

        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            Serdes.String().serializer().getClass().getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            Serdes.String().serializer().getClass().getName());

        sendMessages(notificationTopic, producerProps, PDID, inputData);

        // ivm/DEVICE123/notification
        List<byte[]> mqttAck = getMessagesFromMqttTopic(MQTT_TOPIC, 1, 65000);
        LOGGER.debug("igniteEventString {}", new String(mqttAck.get(0)));
        IgniteEvent igniteEvent = mapper.readValue(new String(mqttAck.get(0)), IgniteEventImpl.class);

        assertEquals("Received IgniteEvent vehicle id is not matching ", true, PDID.equals(igniteEvent.getVehicleId()));

        Optional<IVMRequest> optionalRequest = ivmRequestDao.findByVehicleIdMessageId(PDID, "1");
        LOGGER.debug("IVMRequest.tostring() in test {}", optionalRequest.toString());

        assertEquals("IVMRequest vehicle id and IgniteEvent vehicle id are not equal", true,
            PDID.equals(optionalRequest.get().getVehicleId().trim()));

        String vehicleMessageAck = IOUtils.toString(
            IvmIntegrationTest.class.getResourceAsStream("/ivm-vehicle-message-disposition-publish.json"),
            "UTF-8");
        sendMessages(notificationTopic, producerProps, PDID,
            vehicleMessageAck);
        List<byte[]> dispositionAck = getMessagesFromMqttTopic(MQTT_TOPIC, 1, 45000);
        LOGGER.debug("disposition {}", dispositionAck);
        igniteEvent = mapper.readValue(new String(dispositionAck.get(0)),
            IgniteEventImpl.class);

        assertEquals("Vehicle message disposition details are not matched",
            true, PDID.equals(igniteEvent.getVehicleId()));

        AlertsHistoryInfo ivmHistory = alertsHistoryDao.findById(igniteEvent.getRequestId());
        ChannelResponse channelResponse = ivmHistory.getChannelResponses().get(0);
        Optional<NotificationTemplate> notificationTemplate = notificationTemplateDao.findByNotificationIdAndLocale(
            "S06_IVM_SUBS_ENDED_NOTIF", Locale.US);
        assertEquals("IVM Templates are not equal", true,
            notificationTemplate.get().getIvmTemplate().getBody().equals(channelResponse.getTemplate().getBody()));

        shutDownApplication();
    }

    @BeforeClass
    public static void updateMqttBrokerUrl() {
        MqttBrowserNotifier.setOverRiddenBrokerUrl(MQTT_SERVER.getConnectionString());
    }

}
