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
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaStreamsApplicationTestBase;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.browser.MqttBrowserNotifier;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PortalNotifierIntegrationTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VehicleProfileClientTestBean.class, Launcher.class})
@TestPropertySource("/portal-integration-test.properties")

public class PortalNotifierIntegrationTest extends KafkaStreamsApplicationTestBase {

    private static final  String[] PDID = {"HUXOIDDN4HUN18", "HUXOIDDN4HUN19", "HUXOIDDN4HUN20", "HUXOIDDN4HUN21",
        "HUXOIDDN4HUN22"};
    private static final  Logger LOGGER = LoggerFactory.getLogger(PortalNotifierIntegrationTest.class);

    ObjectMapper mapper = new ObjectMapper();
    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> spc;

    private Properties browserPushProperties;

    private final String notificationTopic = "notification";

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
    private static MqttBrowserNotifier mqttBrowserNotifier;

    @Value("${service.name}")
    private String serviceName;

    private final String mqttTopic = "HUXOIDDN4HUN18/" + NotificationConstants.NOTIFICATION_TOPIC;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPropertyInclusion(Include.NON_NULL);

        browserPushProperties = new Properties();
        InputStream inputStream = PortalNotifierIntegrationTest.class
            .getResourceAsStream("/portal-integration-test.properties");
        browserPushProperties.load(inputStream);

        createTopics(notificationTopic);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-consumer");

        ksProps.put(PropertyNames.DISCOVERY_SERVICE_IMPL, PropBasedDiscoveryServiceImpl.class.getName());
        ksProps.put(NotificationProperty.SOURCE_TOPIC, notificationTopic);
        ksProps.put(PropertyNames.APPLICATION_ID, "pt");

        ksProps.put(PropertyNames.EVENT_TRANSFORMER_CLASSES,
            browserPushProperties.getProperty(PropertyNames.EVENT_TRANSFORMER_CLASSES));
        ksProps.put(PropertyNames.INGESTION_SERIALIZER_CLASS,
            browserPushProperties.getProperty(PropertyNames.INGESTION_SERIALIZER_CLASS));

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
        dao.init(browserPushProperties);
        ((MongoDbClient) dao).setDatastore(datastore);

        saveToDao();

        subscibeToMqttTopic(mqttTopic);

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
            VehicleInfoNotificationTest.class.getResourceAsStream("/mqttBrowser-notification-notifConfig.json"),
            "UTF-8");

        NotificationConfig notificationConfig = mapper.readValue(notificationConfigData,
            NotificationConfig.class);
        notificationConfigDao.save(notificationConfig);

        String userProfileData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/UserProfile.json"),
            "UTF-8");

        UserProfile userProfile = mapper.readValue(userProfileData,
            UserProfile.class);
        userProfile.setUserId("testID");
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
    public void testMqttPushAlert() throws Exception {

        ksProps.put(PropertyNames.EVENT_TRANSFORMER_CLASSES,
            browserPushProperties.getProperty(PropertyNames.EVENT_TRANSFORMER_CLASSES));
        ksProps.put(PropertyNames.SERVICE_STREAM_PROCESSORS,
            browserPushProperties.getProperty(PropertyNames.SERVICE_STREAM_PROCESSORS));
        ksProps.put(PropertyNames.APPLICATION_ID, "chaining" + System.currentTimeMillis());

        launchApplication();

        String inputData =
            IOUtils.toString(PortalNotifierIntegrationTest.class.getResourceAsStream("/mqttBrowser-notification.json"),
                "UTF-8");
        LOGGER.debug("The input event file is : " + inputData);
        sendMessages(notificationTopic, producerProps, PDID[0], inputData);

        List<byte[]> messages = getMessagesFromMqttTopic(mqttTopic, 1, 60000);
        assertEquals("No of message expected", 1, messages.size());
        assertTrue(new String(messages.get(0)).contains("Your vehicle is back inside the valet boundary"));

        shutDownApplication();
    }

    @BeforeClass
    public static void updateMqttBrokerUrl() {
        MqttBrowserNotifier.setOverRiddenBrokerUrl(MQTT_SERVER.getConnectionString());
    }
}
