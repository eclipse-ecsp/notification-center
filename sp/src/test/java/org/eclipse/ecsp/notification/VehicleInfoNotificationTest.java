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

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.eclipse.ecsp.analytics.stream.base.Launcher;
import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.discovery.StreamProcessorDiscoveryService;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaStreamsApplicationTestBase;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaTestUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.entities.CompositeIgniteEvent;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.EventDataDeSerializer;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.browser.MqttBrowserNotifier;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.config.TestSecurityLib;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.PortalTemplate;
import org.eclipse.ecsp.notification.fcm.FcmNotifier;
import org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.notification.processors.AlertProcessorChain;
import org.eclipse.ecsp.notification.processors.GeoFenceFieldGetter11;
import org.eclipse.ecsp.notification.processors.VehicleUserPdidAssociation;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM_ANDROID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * VehicleInfoNotificationTest.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSecurityLib.class})
@Ignore
@SuppressWarnings("checkstyle:MagicNumber")
public class VehicleInfoNotificationTest extends KafkaStreamsApplicationTestBase {
    private static final  Logger LOGGER = LoggerFactory.getLogger(VehicleInfoNotificationTest.class.getName());
    private static final  String PDID = "HUNCJVKNQHA750";
    private static final  String USER_ID = "test123";
    private static final String DTC_COLLECTION_NAME = "dTCMaster";
    public static final int TEN_THOUSAND = 10000;
    public static final int EIGHT = 8;
    private NotificationDao dao;
    @Autowired
    private AlertProcessorChain alertProcessor;
    @Autowired
    private Datastore datastore;
    @Autowired
    private GenericIgniteEventTransformer transformer;
    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;
    @Autowired
    private NotificationTemplateDAO templateDao;
    @Autowired
    private NotificationTemplateConfigDAO templateConfigDao;
    @Autowired
    private NotificationConfigDAO notificationConfigDao;
    @Autowired
    private NotificationBufferDao notificationBufferDao;
    @Autowired
    private AlertsHistoryAssistant alertsHistoryAssistant;
    @Autowired
    private ScheduleNotificationAssistant scheduleNotificationAssistant;

    @Autowired
    private AlertProcessorChain alertProcessorChain;

    @Autowired
    private ApplicationContext applicationContext;

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessingContext;

    private String alertTopic;
    private String alertsCollectionName;
    private String dtcCollectionName;
    private Properties snsProperties;

    @Override
    @Before
    public void setup() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        Thread.sleep(TEN_THOUSAND);
        super.setup();
        datastore.getDatabase().getCollection(DTC_COLLECTION_NAME).drop();
        snsProperties = new Properties();
        InputStream inStream = VehicleInfoNotificationTest.class.getResourceAsStream("/application.properties");
        snsProperties.load(inStream);
        dao = new MongoDbClient();
        dao.init(snsProperties);
        ((MongoDbClient) dao).setDatastore(datastore);
        String dtcTemplateData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/mongodb-dtc-alerts-template-message.json"),
            StandardCharsets.UTF_8);
        // dtcCollectionName =
        // this.snsProperties.getProperty(DTC_COLLECTION_NAME);
        pushToMongo(dtcTemplateData, DTC_COLLECTION_NAME, dao);
        String alertsTemplateData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/mongodb-alerts-template-message.json"),
            StandardCharsets.UTF_8);
        alertsCollectionName = this.snsProperties
            .getProperty(NotificationProperty.RESOURCE_BUNDLE_COLLECTION_NAME);
        pushToMongo(alertsTemplateData, alertsCollectionName, dao);
        alertTopic = this.snsProperties
            .getProperty(NotificationProperty.SOURCE_TOPIC);

        alertsHistoryAssistant.init("alerts", "userAlerts");
        scheduleNotificationAssistant.init(streamProcessingContext);
    }

    /**
     * populate template.
     */
    @Before
    public void populateTemplate() throws IOException {
        CollectorRegistry.defaultRegistry.clear();
        NotificationTemplate[] templates = new NotificationTemplate[EIGHT];
        templates = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream(
                            "/NotificationTemplate.json"),
                    StandardCharsets.UTF_8),
                NotificationTemplate.class)
            .toArray(templates);
        templateDao.saveAll(templates);
        NotificationTemplateConfig[] templateConfigs = new NotificationTemplateConfig[EIGHT];
        templateConfigs = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream(
                            "/NotificationTemplateConfig.json"),
                    StandardCharsets.UTF_8),
                NotificationTemplateConfig.class)
            .toArray(templateConfigs);
        templateConfigDao.saveAll(templateConfigs);

        List<NotificationGrouping> notificationGroups = JsonUtils
            .getListObjects(IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream("/NotificationGroup.json"),
                StandardCharsets.UTF_8), NotificationGrouping.class);
        notificationGroupingDao.saveAll(
            notificationGroups.toArray(new NotificationGrouping[notificationGroups.size()]));

        List<NotificationConfig> notificationConfigs = JsonUtils
            .getListObjects(IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream(
                        "/disassociation-notification-configs.json"),
                StandardCharsets.UTF_8), NotificationConfig.class);
        notificationConfigDao.saveAll(notificationConfigs.toArray(new NotificationConfig[notificationConfigs.size()]));
    }

    /**
     * populate config.
     */
    @Before
    public void populateConfig() throws IOException {
        CollectorRegistry.defaultRegistry.clear();
        NotificationConfig notificationConfig = JsonUtils.bindData(IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/browser-payload-config.json"),
            StandardCharsets.UTF_8), NotificationConfig.class);
        notificationConfigDao.save(notificationConfig);
    }

    /**
     * clear mongo.
     */
    @After
    public void clearMongo() {
        dao.deleteSingleDocument("en_FR", alertsCollectionName);
        dao.deleteSingleDocument("en_US", alertsCollectionName);
        dao.deleteSingleDocument("P0310", dtcCollectionName);
        dao.deleteSingleDocument("P0598", dtcCollectionName);
        notificationConfigDao.deleteAll();
    }

    /**
     * SimpleTestServiceDiscoveryImpl.
     */
    public static final class SimpleTestServiceDiscoveryImpl implements StreamProcessorDiscoveryService {
        @Override
        public List<StreamProcessor<?, ?, ?, ?>> discoverProcessors() {
            return Collections.singletonList(new VehicleInfoNotification());
        }
    }

    private ObjectMapper getCustomMapper() {
        ObjectMapper mapper = new ObjectMapper();
        EventDataDeSerializer eventDataSerializer = new EventDataDeSerializer();
        SimpleModule module = new SimpleModule("PolymorphicEventDataModule");
        module.addDeserializer(EventData.class, eventDataSerializer);
        mapper.registerModule(module);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        return mapper;
    }

    private CompositeIgniteEvent getCompositeEvent(ObjectMapper mapper, String eventAsString) throws IOException {
        CompositeIgniteEvent compositeEvent;
        JsonNode node = mapper.readTree(eventAsString);
        if (node.isObject()) {
            IgniteEventImpl event = mapper.readValue(eventAsString, IgniteEventImpl.class);
            List<IgniteEvent> eventAsList = new ArrayList<IgniteEvent>(0);
            eventAsList.add(event);
            compositeEvent = new CompositeIgniteEvent();
            compositeEvent.setNestedEvents(eventAsList);
        } else {
            List<IgniteEvent> list = mapper.readValue(eventAsString,
                mapper.getTypeFactory().constructCollectionType(List.class, IgniteEventImpl.class));
            compositeEvent = new CompositeIgniteEvent();
            compositeEvent.setNestedEvents(list);
        }
        return compositeEvent;
    }

    @Test
    public void testDisassociationDeletesNotificationConfig() throws Exception {
        VehicleInfoNotification vehicleInfoNotification = new VehicleInfoNotification();
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationConfigDAO", notificationConfigDao);
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationBufferDao", notificationBufferDao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/disassociation-data.json"), StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        vehicleInfoNotification.process(new IgniteStringKey("fdsdsf"), event,
            "vehicle-profile-modified-authorized-users");

        List<NotificationConfig> list = notificationConfigDao.findByUserVehicle("disUser1", "disVehicle1");
        assertEquals("Number of elements should be 0", 0, list.size());

        list = notificationConfigDao.findByUserVehicle("disUser2", "disVehicle2");
        assertEquals("Number of elements should be 1", 1, list.size());
    }

    @Test
    public void testAssociationNotDeletesNotificationConfig() throws Exception {
        VehicleInfoNotification vehicleInfoNotification = new VehicleInfoNotification();
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationConfigDAO", notificationConfigDao);
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationBufferDao", notificationBufferDao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/association-data.json"), StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());

        vehicleInfoNotification.process(new IgniteStringKey("fdsdsf"), event,
            "vehicle-profile-modified-authorized-users");

        List<NotificationConfig> list = notificationConfigDao.findByUserVehicle("disUser2", "disVehicle2");
        assertEquals("Number of elements should be 1", 1, list.size());
    }

    @Test
    @Ignore("testAssociationDeletesBuffer")
    public void testAssociationDeletesBuffer() throws Exception {

        MockitoAnnotations.initMocks(this);
        VehicleInfoNotification vehicleInfoNotification = new VehicleInfoNotification();
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationConfigDAO", notificationConfigDao);
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationBufferDao", notificationBufferDao);
        ReflectionTestUtils.setField(vehicleInfoNotification, "ctxt", streamProcessingContext);

        String notificationSettingsData = IOUtils
            .toString(VehicleInfoNotificationTest.class.getResourceAsStream("/disassociation-data.json"),
                StandardCharsets.UTF_8);
        notificationSettingsData =
            notificationSettingsData.replace("disUser1", "disUser3").replace("disVehicle1", "disVehicle3");
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());

        ObjectMapper mapper = new ObjectMapper();
        String notificationBufferData = IOUtils.toString(
            SnoozeNotificationTest.class.getResourceAsStream("/disassociation-buffer-notification.json"),
            StandardCharsets.UTF_8);
        NotificationBuffer notificationBuffer = mapper.readValue(notificationBufferData, NotificationBuffer.class);
        notificationBufferDao.save(notificationBuffer);

        List<NotificationBuffer> list = notificationBufferDao.findByUserIdAndVehicleId("disUser3", "disVehicle3");
        assertEquals("Number of elements should be 1", 1, list.size());

        vehicleInfoNotification.process(new IgniteStringKey("fdsdsf"), event,
            "vehicle-profile-modified-authorized-users");

        list = notificationBufferDao.findByUserIdAndVehicleId("disUser3", "disVehicle3");
        assertEquals("Number of elements should be 0", 0, list.size());
    }

    /**
     * testAWSSNSTopicCreation.
     */
    public void testAwsSnsTopicCreation() throws Exception {
        ksProps.put(PropertyNames.DISCOVERY_SERVICE_IMPL, SimpleTestServiceDiscoveryImpl.class.getName());
        ksProps.put(PropertyNames.APPLICATION_ID, "pt");
        new Launcher().launch();

        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings.json"),
            StandardCharsets.UTF_8);

        /*
         * First send the settings data.
         */
        KafkaTestUtils.sendMessages(alertTopic, producerProps, USER_ID, notificationSettingsData);

        Thread.sleep(10000000);

        /*
         * Now send the mapping of pdid to user id data. It will be keyed with
         * PDID.
         */

        String pdidToUserMappingData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/user-association.json"), StandardCharsets.UTF_8);

        KafkaTestUtils.sendMessages(alertTopic, producerProps, PDID, pdidToUserMappingData);

        Thread.sleep(1000);

        /*
         * Now send the alerts data, which is keyed with PDID.
         */

        String alertsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-data.json"), StandardCharsets.UTF_8);

        KafkaTestUtils.sendMessages(alertTopic, producerProps, PDID, alertsData);

        Thread.sleep(1000);

        List<String[]> message1 = KafkaTestUtils.getMessages(alertTopic, consumerProps, 1, TEN_THOUSAND);
        LOGGER.info(message1.get(0)[0]);
        LOGGER.info(message1.get(0)[1]);

        Thread.sleep(1000);

        List<String[]> message2 = KafkaTestUtils.getMessages(alertTopic, consumerProps, 1, TEN_THOUSAND);
        LOGGER.info(message2.get(0)[0]);
        LOGGER.info(message2.get(0)[1]);
    }

    /**
     * Test whether the pojo AlertsInfo is binding properly to the data or not.
     *
     */
    @Test
    public void testAlertsData() throws IOException {
        ObjectMapper mapper = getCustomMapper();

        String eventAsString = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-data-2.json"),
            StandardCharsets.UTF_8);

        CompositeIgniteEvent compositeEvent = getCompositeEvent(mapper, eventAsString);

        String notificationEvent = mapper.writeValueAsString(compositeEvent.getNestedEvents());

        System.out.println("notificationEvent : " + notificationEvent);

        List<AlertsInfo> infoList = NotificationUtils.getListObjects(notificationEvent,
            AlertsInfo.class);
        System.out.println("infoList : " + infoList);
        assertNotNull(infoList);
        assertEquals("Number of elements should be 3", 3, infoList.size());

        for (AlertsInfo info : infoList) {
            LOGGER.debug(info.getEventID());
        }

    }

    @Test
    public void testNotificationSettingsData2() throws IOException {
        ObjectMapper mapper = getCustomMapper();
        String eventAsString = IOUtils.toString(VehicleInfoNotificationTest.class
            .getResourceAsStream("/configuration-settings.json"), StandardCharsets.UTF_8);

        CompositeIgniteEvent compositeEvent = getCompositeEvent(mapper, eventAsString);

        String notificationEvent = mapper.writeValueAsString(compositeEvent.getNestedEvents());

        System.out.println("notificationEvent : " + notificationEvent);

        List<AlertsInfo> infoList = NotificationUtils.getListObjects(notificationEvent,
            AlertsInfo.class);

        assertNotNull(infoList);
        assertEquals("Number of elements should be 1", 1, infoList.size());

        for (AlertsInfo info : infoList) {
            LOGGER.debug(info.getEventID());
        }

    }

    private void pushToMongo(String data, String collectionName, NotificationDao dao2)
        throws JsonParseException, JsonMappingException, IOException {
        dao.insertMultiDocument(data, collectionName);
    }

    /**
     * Vehicle Association is coming as Arrays. Testing whether it properly
     * binding to the pojo or not.
     *
     * @throws IOException ex
     */
    @Test
    public void testVehicleAssociationData() throws IOException {

        ObjectMapper mapper = getCustomMapper();
        String eventAsString = IOUtils.toString(VehicleInfoNotificationTest.class
            .getResourceAsStream("/user-association.json"), StandardCharsets.UTF_8);

        CompositeIgniteEvent compositeEvent = getCompositeEvent(mapper, eventAsString);

        String associationData = mapper.writeValueAsString(compositeEvent.getNestedEvents());

        List<VehicleUserPdidAssociation> userPdidAssociationList = NotificationUtils
            .getListObjects(associationData, VehicleUserPdidAssociation.class);

        assertNotNull(userPdidAssociationList);
        assertEquals("Size of the arrays are not same", 1, userPdidAssociationList.size());

        VehicleUserPdidAssociation obj = userPdidAssociationList.get(0);
        assertEquals("user-ids are not same", "12345", obj.getData().getUserId());

    }

    /**
     *
     * <p> HCP-7314 Accident and notification: Alert coming to app without vehicle
     * association.
     * When a different user logs on to the same device, push notifications for
     * the other users (users which are logged in previously) should not come on
     * to the device.
     * USER1 logs in with device token D1, when other user USER2 logs in, the
     * topic for the USER1 and its end point should be deleted. Also we are
     * keeping the mapping of devicetoken to user id in the mongodb, it should
     * also be deleted.
     *
     * The final result should be: 1) End point for the corresponding device
     * token should exist for USER2 only 2) Mapping of device token should be
     * done for USER2. 3) No push topic for USER1 and no mapping in MongoDB</p>
     *
     * @throws InterruptedException ex
     * @throws IOException ex
     */

    public void testDataToBeSentToMongo() throws IOException {
        /*
         *
         * Read the alerts data and form the List<AlertsInfo> object
         */

        /*
         * In this test case the dtc are cleared
         */
        String alertsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-data-collision.json"),
            StandardCharsets.UTF_8);

        List<AlertsInfo> infoList = NotificationUtils.getListObjects(alertsData, AlertsInfo.class);

        // response.setPublishInfo(this.snsProperties.getProperty(TOPIC_ARN),
        // "Hello How are you", "AWS_REQUEST-ID", infoList.get(0));
        String msg = null;

        // for (TopicsInfo info : response.getTopics()) {

        // AlertsHistoryInfo alerts = new AlertsHistoryInfo();
        /*
         * Since this object is going to be send to MongoDB, we need to set the
         * TTL. Also we have added the serialization of JodaTime to ISODate
         * which is required by MongoDB
         */
        // alerts.setCreateDts(new DateTime());
        /*
         * alerts.setPdid(response.getPdid()); Map<String, Object> alertPayLoad
         * = (Map<String, Object>) info.getAlertData();
         *
         * alerts.setAlertType(info.getAlertObject().getEventID());
         *
         * alerts.setTimestamp(info.getAlertObject().getTimestamp());
         *
         * alerts.setPayload(info.getAlertObject());
         * alerts.setChannelResponse(info); msg =
         * JsonUtils.getObjectValueAsString(alerts); }
         */
        System.out.println(msg);

    }

    /**
     * testJodaTimeSerialization.
     */
    public void testJodaTimeSerialization() {
        DateTime date = new DateTime();
        String str = JsonUtils.getObjectValueAsString(date);
        System.out.println(str);
    }

    @Test
    public void testInvalidGeoFenceType() throws IOException {
        GeoFenceFieldGetter11 geoFenceMongoMsgFetcher1 = new GeoFenceFieldGetter11();
        String alertsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-data-6.json"), StandardCharsets.UTF_8);
        List<AlertsInfo> infoList = NotificationUtils.getListObjects(alertsData, AlertsInfo.class);
        for (AlertsInfo alertsInfo : infoList) {
            assertNull(geoFenceMongoMsgFetcher1.getNotificationId(alertsInfo));
        }

    }

    @Test
    public void testSetupChannelForFcm() throws Exception {
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        config.setUserId(user1);
        notifier.setupChannel(config);
        List<String> tokens = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Value of tokens : ", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            tokens.get(0));
        dao.deleteSingleDocument("1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            "notnTokenUserMap");

    }

    @Test
    public void testSetupChannelForFcmTokenForMultiUser() throws Exception {
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        String user2 = USER_ID + "TWO";
        config.setUserId(user1);
        notifier.setupChannel(config);
        config.setUserId(user2);
        notifier.setupChannel(config);

        List<String> tokens = dao.getFieldValuesByField("userID", user2, "notnTokenUserMap", "_id");
        List<String> user1tokens = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Value of tokens : ", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            tokens.get(0));
        assertEquals("Size of token list for earlier user should be 0 :", 0, user1tokens.size());
        dao.deleteSingleDocument("1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            "notnTokenUserMap");

    }

    @Test
    @Ignore("testBrowserPayload")
    public void testBrowserPayload() throws Exception {

        String alertsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-data-collision.json"),
            StandardCharsets.UTF_8);
        List<AlertsInfo> infoList = NotificationUtils.getListObjects(alertsData, AlertsInfo.class);
        IgniteEvent events = transformer.fromBlob(alertsData.getBytes(), Optional.empty());
        int i = 0;
        for (IgniteEvent event : events.getNestedEvents()) {
            AlertsInfo info = infoList.get(i);
            info.setIgniteEvent(event);
            NotificationTemplate template = new NotificationTemplate();
            ChannelTemplates templates = new ChannelTemplates();
            PortalTemplate portalTemplate = new PortalTemplate();
            portalTemplate.setBody("Test Message");
            templates.setPortal(portalTemplate);
            template.setChannelTemplates(templates);
            info.addNotificationTemplate("en-US", template);
            NotificationConfig notificationConfig = new NotificationConfig();
            notificationConfig.setLocale("en-US");
            info.setNotificationConfig(notificationConfig);
            info.getAlertsData().setUserProfile(new UserProfile("User123"));
            VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged("Vehicle123");
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("make", "default");
            vehicleProfileAbridged.setVehicleAttributes(attrs);
            info.getAlertsData().setVehicleProfile(vehicleProfileAbridged);

            i++;
        }
        assertNotNull(infoList);
        assertEquals("Number of elements should be 1", 1, infoList.size());
        MqttBrowserNotifier notifier = new MqttBrowserNotifier();
        for (AlertsInfo a : infoList) {
            alertProcessor.process(a);
        }
        infoList.get(0).setNotificationConfig(infoList.get(0).getNotificationConfigs().get(0));
        String payload = notifier.getAlertMessage(infoList.get(0));
        String browserExpected = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/collision-browser-response.json"),
            StandardCharsets.UTF_8);

        assertEquals("Browser payload does not match",
            payload.substring(0, payload.indexOf("notificationTemplate\":{\"id\":\"")),
            browserExpected.substring(0, browserExpected.indexOf("notificationTemplate\":{\"id\":\"")));
        assertEquals("Browser payload does not match",
            payload.substring(payload.indexOf(",\"notificationId\":\"Collision\"")),
            browserExpected.substring(browserExpected.indexOf(",\"notificationId\":\"Collision\"")));
    }

    @Test
    public void testPersistResponse() throws Exception {

        VehicleInfoNotification vehicleInfo = new VehicleInfoNotification();
        vehicleInfo.initNotificationDao(snsProperties);
        ((MongoDbClient) ReflectionTestUtils.getField(vehicleInfo, "notificationDAO")).setDatastore(datastore);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotification.class.getResourceAsStream(
                "/settings-response.json"),
            StandardCharsets.UTF_8);

        vehicleInfo.persistResponse(JsonUtils.getJsonAsMap((notificationSettingsData)), "notificationSetUps");

        List<String> channelType = dao.getFieldValuesByField("userID", "testID", "notificationSetUps", "channelType");
        List<String> responseTime =
            dao.getFieldValuesByField("userID", "testID", "notificationSetUps", "processedTime");
        assertEquals("Channel Type value matched: ", "BROWSER", channelType.get(0));
        assertEquals("Processed time value matched: ", "1507113492880", responseTime.get(0));
        dao.deleteSingleDocument("testID", "notificationSetUps");
    }

    /**
     * Test to verify whether the field createDTS is properly getting pushed to
     * Mongo or not.
     *
     * @throws IOException ex
     */
    @Test
    public void testAlertsHistoryResponse() throws IOException {
        // read the alerts data
        final String sampleMongoCollectionName = "alerts";

        VehicleInfoNotification vehicleInfo = new VehicleInfoNotification();
        vehicleInfo.initNotificationDao(snsProperties);
        ((MongoDbClient) ReflectionTestUtils.getField(vehicleInfo, "notificationDAO")).setDatastore(datastore);

        String alertsData = IOUtils.toString(
            VehicleInfoNotification.class.getResourceAsStream(
                "/alerts-data-6.json"),
            StandardCharsets.UTF_8);
        List<AlertsInfo> infoList = NotificationUtils.getListObjects(alertsData, AlertsInfo.class);

        IgniteEvent events = transformer.fromBlob(alertsData.getBytes(), Optional.empty());
        int i = 0;
        for (IgniteEvent event : events.getNestedEvents()) {
            AlertsInfo info = infoList.get(i);
            info.setIgniteEvent(event);
            info.addNotificationTemplate("en-US", new NotificationTemplate());
            NotificationConfig notificationConfig = new NotificationConfig();
            notificationConfig.setLocale("en-US");
            info.setNotificationConfig(notificationConfig);
            info.getAlertsData().setUserProfile(new UserProfile("User" + i));
            i++;
        }
        assertNotNull(infoList);
        assertEquals("Number of elements should be 1", 1, infoList.size());

        AlertsInfo alertObj = infoList.get(0);

        AlertsHistoryInfo alertsHistoryObj =
            alertsHistoryAssistant.setEnrichedAlertHistory(alertObj, new AlertsHistoryInfo());

        // Save this response

        Map<String, Object> responsePersist = JsonUtils.getObjectAsMap(alertsHistoryObj);
        vehicleInfo.persistResponse(responsePersist, sampleMongoCollectionName);

        MongoDbClient db = (MongoDbClient) dao;
        db.setDatastore(datastore);
        MongoDatabase mongoDb = db.getDataBaseInstance();

        MongoCollection<Document> collection = mongoDb.getCollection(sampleMongoCollectionName);
        // get all the documents in the collection
        FindIterable<Document> result = collection.find();

        // get the iterator, so that you can iterate over all the documents
        MongoCursor<Document> cursor = result.iterator();
        // We will store the documents retrieved in an array list
        List<Document> resultantDocumentList = new ArrayList<>();

        while (cursor.hasNext()) {
            resultantDocumentList.add(cursor.next());
        }

        // once all the documents are added to the list, check the number of
        // documents.

        LOGGER.info("Alert history object from mongo:{}", resultantDocumentList.get(0));
        Assert.assertEquals("Number of documents not matching", 1, resultantDocumentList.size());

    }

    @Test
    public void testHandleFcmResponseInvalidRegistration() throws Exception {

        List<String> deviceIds = new ArrayList<String>();
        deviceIds.add("1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9");
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        config.setUserId(user1);
        notifier.setupChannel(config);
        String responseString =
            "{ \"multicast_id\": 111, \"success\": 0, \"failure\": 1, \"canonical_ids\": 0, "
                + "\"results\": [    { \"error\": \"InvalidRegistration\" } ]}";

        List<String> tokens = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Value of tokens : ", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            tokens.get(0));
        notifier.handleFcmResponse(responseString, deviceIds, user1, APP_PLATFORM_ANDROID, false);
        List<String> tokensAfterFcmResponse = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Token should be deleted and size of list should be Zero : ", 0, tokensAfterFcmResponse.size());

    }

    /**
     * This test handles scenarios when application has been uninstalled and
     * still push initiated.
     *
     * @throws Exception ex
     */
    @Test
    public void testHandleFcmResponseNotRegistered() throws Exception {

        List<String> deviceIds = new ArrayList<String>();
        deviceIds.add("1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9");
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        config.setUserId(user1);
        notifier.setupChannel(config);
        String responseString =
            "{ \"multicast_id\": 111, \"success\": 0, \"failure\": 1, \"canonical_ids\": 0, \"results\": [    { "
                + "\"error\": \"NotRegistered\" } ]}";

        List<String> tokens = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Value of tokens : ", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            tokens.get(0));
        notifier.handleFcmResponse(responseString, deviceIds, user1, APP_PLATFORM_ANDROID, true);
        List<String> tokensAfterFcmResponse = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Token should be deleted and size of list should be Zero : ", 0, tokensAfterFcmResponse.size());

    }

    /**
     * This test handles scenarios when FCM updated device token and returned a
     * canonical token id.
     *
     * @throws Exception ex
     */
    @Test
    public void testHandleFcmResponseCanonical() throws Exception {

        List<String> deviceIds = new ArrayList<String>();
        deviceIds.add("1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9");
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        config.setUserId(user1);
        notifier.setupChannel(config);
        String responseString =
            "{ \"multicast_id\": 111, \"success\": 1, \"failure\": 0, \"canonical_ids\": 1, \"results\": "
                + "[    {\"message_id\": \"1:2342\", \"registration_id\": \"32\" } ]}";
        List<String> tokens = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Value of tokens : ", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            tokens.get(0));
        notifier.handleFcmResponse(responseString, deviceIds, user1, APP_PLATFORM_ANDROID, false);
        List<String> tokensAfterFcmResponse = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Value of tokens : ", "32", tokensAfterFcmResponse.get(0));
        dao.deleteSingleDocument("32", "notnTokenUserMap");

    }

    /**
     * This test handles FCM response with multiple error and success or success
     * with canonical ids.
     *
     * @throws Exception ex
     */
    @Test
    public void testHandleFcmResponseMultiErrorSuccess() throws Exception {


        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);

        String user1 = USER_ID + "ONE";
        Map<String, Object> tokenUserId = new HashMap<String, Object>();
        tokenUserId.put(PropertyNamesForFcm.USER_ID, user1);
        for (int i = 0; i < 6; i++) {
            tokenUserId.put(PropertyNamesForFcm.ID, "token" + i);
            dao.insertSingleDocument(tokenUserId, "notnTokenUserMap");
        }

        String responseString =
            "{ \"multicast_id\": 111, \"success\": 3, \"failure\": 3, \"canonical_ids\": 1, \"results\": "
                + "[   { \"message_id\": \"1:0408\" },   { \"error\": \"Unavailable\" },   "
                + "{ \"error\": \"InvalidRegistration\" },   "
                + "{ \"message_id\": \"1:1516\" },   { \"message_id\": \"1:2342\", \"registration_id\": \"32\" },   "
                + "{ \"error\": \"NotRegistered\"} ]}";
        List<String> tokens = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Total Tokens: ", 6, tokens.size());
        notifier.handleFcmResponse(responseString, tokens, user1, APP_PLATFORM_ANDROID, false);
        List<String> tokensAfterFcmResponse = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Total token after FCM response :", 4, tokensAfterFcmResponse.size());

        assertEquals("Value of tokens : ", "32", tokensAfterFcmResponse.get(3));
        for (String token : tokensAfterFcmResponse) {
            dao.deleteSingleDocument(token, "notnTokenUserMap");
        }
        List<String> tokensAfterCleanUp = dao.getFieldValuesByField("userID", user1, "notnTokenUserMap", "_id");
        assertEquals("Total tokens after cleanup : ", 0, tokensAfterCleanUp.size());

    }

    @Test
    public void testSetupChannelForFcmForAndroid() throws Exception {
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings-appAndroid.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        config.setUserId(user1);
        notifier.setupChannel(config);

        Map<String, Object> whereMap = new HashMap<String, Object>();
        whereMap.put("userID", user1);

        Map<String, Object> fieldsMap = new HashMap<String, Object>();
        fieldsMap.put("_id", 1);
        fieldsMap.put("appPlatform", "1");

        List<Map<String, Object>> documentList = dao.getFieldsValueByFields(whereMap, "notnTokenUserMap", fieldsMap);
        assertEquals("Value of tokens : ", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            documentList.get(0).get("_id"));
        assertEquals("Value of appPlatform : ", "ANDROID", documentList.get(0).get("appPlatform"));

        dao.deleteSingleDocument("1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
            "notnTokenUserMap");
    }

    @Test
    @Ignore("testFilterAlerts")
    public void testFilterAlerts() throws IOException {
        ObjectMapper mapper = getCustomMapper();
        String eventAsString = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-filter.json"),
            StandardCharsets.UTF_8);
        VehicleInfoNotification notification = new VehicleInfoNotification();
        snsProperties.setProperty(NotificationProperty.FILTERED_EVENT_IDS, "LowFuel,Idle");
        notification.initConfig(snsProperties);
        List<IgniteEvent> igniteEvents = getCompositeEvent(mapper, eventAsString).getNestedEvents();
        assertEquals("Number of elements should be 4", 4, igniteEvents.size());
        List<IgniteEvent> filteredEvents = notification.filterAndGroupEvents(igniteEvents, alertTopic)
            .get(VehicleInfoNotification.OTHER_EVENTS);
        assertEquals("Number of elements should be 2", 2, filteredEvents.size());
    }

    @Test
    @Ignore("testNoFilter")
    public void testNoFilter() throws IOException {
        ObjectMapper mapper = getCustomMapper();
        String eventAsString = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-filter.json"),
            StandardCharsets.UTF_8);
        VehicleInfoNotification notification = new VehicleInfoNotification();
        notification.initConfig(snsProperties);
        List<IgniteEvent> igniteEvents = getCompositeEvent(mapper, eventAsString).getNestedEvents();
        assertEquals("Number of elements should be 4", 4, igniteEvents.size());
        List<IgniteEvent> filteredEvents = notification
            .filterAndGroupEvents(igniteEvents, alertTopic).get(VehicleInfoNotification.OTHER_EVENTS);
        assertEquals("Number of elements should be 4", 4, filteredEvents.size());
    }

    @Test
    public void testDisablingPushNotificationSettings() throws Exception {
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings-appAndroid.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        config.setUserId(user1);
        notifier.setupChannel(config);
        List<String> userIdList =
            dao.getFieldValuesByField("_id", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
                "notnTokenUserMap", "userID");
        assertEquals("One Id should be present after setup: ", 1, userIdList.size());
        String notificationSettingsDataDisable = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/configuration-settings-disable.json"),
            StandardCharsets.UTF_8);
        IgniteEvent event1 = transformer.fromBlob(notificationSettingsDataDisable.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 disabled = (NotificationSettingDataV1_0) event1.getEventData();
        NotificationConfig configDisabled = disabled.getNotificationConfigs().get(0);
        configDisabled.setUserId(user1);
        notifier.setupChannel(configDisabled);
        notifier.setupChannel(configDisabled);
        userIdList =
            dao.getFieldValuesByField("_id", "1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
                "notnTokenUserMap", "userID");
        assertEquals("After calling notificationSettings with disable flag ,Id should not be there in mongo: ", 0,
            userIdList.size());
    }

    @BeforeClass
    public static void updateMqttBrokerUrl() {
        MqttBrowserNotifier.setOverRiddenBrokerUrl(MQTT_SERVER.getConnectionString());
    }

}
