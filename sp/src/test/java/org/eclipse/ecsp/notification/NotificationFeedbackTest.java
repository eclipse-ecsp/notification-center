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
import org.eclipse.ecsp.analytics.stream.base.discovery.PropBasedDiscoveryServiceImpl;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaStreamsApplicationTestBase;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.notification.config.TestSecurityLib;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.eclipse.ecsp.notification.utils.PropertyUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * NotificationFeedbackTest.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {TestSecurityLib.class, CoreVehicleProfileClient.class, Launcher.class})
@TestPropertySource("/apiPush-integration-test.properties")
public class NotificationFeedbackTest extends KafkaStreamsApplicationTestBase {

    private static final  String[] PDID = {"HUXOIDDN4HUN18", "HUXOIDDN4HUN19", "HUXOIDDN4HUN20", "HUXOIDDN4HUN21",
        "HUXOIDDN4HUN22"};
    private static final  Logger LOGGER = LoggerFactory.getLogger(NotificationFeedbackTest.class);

    ObjectMapper mapper = new ObjectMapper();

    private Properties apiPushProperties;

    private final String notificationTopic = "notification";
    private final String dffEventFeedbackTopic = "DffFeedBackTopic";


    @Autowired
    private Datastore datastore;

    @Autowired
    private NotificationTemplateDAO notificationTemplateDao;

    @Autowired
    private NotificationTemplateConfigDAO notificationTemplateConfigDao;

    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;

    @Autowired
    private UserProfileDAO userProfileDao;

    @Autowired
    PropertyUtils utils;

    @Value("${service.name}")
    private String serviceName;

    private MongoDbClient dao;

    @Override
    @BeforeEach
    public void setup() throws Exception {
        super.setup();


        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPropertyInclusion(Include.NON_NULL);

        apiPushProperties = new Properties();
        InputStream inputStream = NotificationFeedbackTest.class
            .getResourceAsStream("/apiPush-integration-test.properties");
        apiPushProperties.load(inputStream);

        String internalTopic = "internal";
        String notificationSpDlqTopic = "notification-sp-dlq";
        String deviceStatusNotificationSpTopic = "device-status-notification-sp";
        String httpsIntegHigh = "https-integ-high";
        createTopics(notificationTopic, internalTopic, notificationSpDlqTopic, deviceStatusNotificationSpTopic,
            httpsIntegHigh, dffEventFeedbackTopic);
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

        dao = new MongoDbClient();
        dao.init(apiPushProperties);
        dao.setDatastore(datastore);

        saveToDao();
    }

    private void saveToDao() throws IOException {
        String notificationTemp = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/NotificationTemplate.json"),
            StandardCharsets.UTF_8);

        NotificationTemplate[] notificationTemplate = mapper.readValue(notificationTemp, NotificationTemplate[].class);
        notificationTemplateDao.saveAll(notificationTemplate);

        String notificationTemplateConfigData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/NotificationTemplateConfig.json"),
            StandardCharsets.UTF_8);

        NotificationTemplateConfig[] notificationTemplateConfig = mapper.readValue(notificationTemplateConfigData,
            NotificationTemplateConfig[].class);
        notificationTemplateConfigDao.saveAll(notificationTemplateConfig);

        String notificationGroupData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/NotificationGroup.json"),
            StandardCharsets.UTF_8);

        NotificationGrouping[] notificationGroup = mapper.readValue(notificationGroupData,
            NotificationGrouping[].class);
        notificationGroupingDao.saveAll(notificationGroup);

        String userProfileData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/UserProfile.json"),
            StandardCharsets.UTF_8);

        UserProfile userProfile = mapper.readValue(userProfileData,
            UserProfile.class);
        userProfile.setUserId("HUXOIDDN4HUN18");
        userProfileDao.save(userProfile);
    }


    @Test
    public void testGetDataBaseInstance() throws IOException {
        Assertions.assertNotNull(dao);
        dao.getDataBaseInstance();
        dao.getRecord(notificationTopic, dffEventFeedbackTopic);
        dao.getRecordsByCollections(serviceName, notificationTopic, dffEventFeedbackTopic);
        dao.getFieldValueWithPrimaryKey(serviceName, notificationTopic, dffEventFeedbackTopic);
        dao.insertOneDocument("{\n"
            + "  \"NotificationId\": \"C04_EN_COMPLETEACCOUNT_NON_CONNECTED\"}");
        dao.getFieldsValueByFields(null, dffEventFeedbackTopic, null);
        dao.getFieldValuesByField(dffEventFeedbackTopic, serviceName, notificationTopic, dffEventFeedbackTopic);
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void testPropertyUtils() {

        assertEquals(2, utils.getHeaderMap().size());
        assertEquals(5, utils.getVehicleAttributes().size());
    }


}
