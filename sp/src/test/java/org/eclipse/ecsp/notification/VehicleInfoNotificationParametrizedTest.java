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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import dev.morphia.Datastore;
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaStreamsApplicationTestBase;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.FCMChannelResponse;
import org.eclipse.ecsp.domain.notification.FcmPushAlertEventData;
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.PushTemplate;
import org.eclipse.ecsp.notification.fcm.FcmNotifier;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.notification.processors.AlertProcessorChain;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertTrue;


/**
 * VehicleInfoNotificationParametrizedTest class.
 */
@RunWith(Parameterized.class)
@Ignore
public class VehicleInfoNotificationParametrizedTest extends KafkaStreamsApplicationTestBase {

    private static final  String PDID = "HUNCJVKNQHA750";
    private static final  String USER_ID = "test123";
    private static final String DTC_COLLECTION_NAME = "dTCMaster";
    public static final int THOUSAND = 10000;
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

    private final String payload;

    /**
     * VehicleInfoNotificationParametrizedTest constructor.
     *
     * @param payload String
     */
    public VehicleInfoNotificationParametrizedTest(String payload) {
        this.payload = payload;
    }

    private void pushToMongo(String data, String collectionName, NotificationDao dao2)
            throws JsonParseException, JsonMappingException, IOException {
        dao.insertMultiDocument(data, collectionName);
    }

    @Override
    @Before
    public void setup() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        Thread.sleep(THOUSAND);
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
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"/configuration-settings.json"},
                {"/configuration-settings-ios.json"},
                {"/configuration-settings-appAndroid.json"},

        });

    }

    @Test
    public void testDoPublishAndroidForFcm() throws Exception {
        FcmNotifier notifier = new FcmNotifier();
        notifier.init(snsProperties, new MetricRegistry(), dao);
        String notificationSettingsData = IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream(payload),
                StandardCharsets.UTF_8);
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        NotificationSettingDataV1_0 v10 = (NotificationSettingDataV1_0) event.getEventData();
        NotificationConfig config = v10.getNotificationConfigs().get(0);
        String user1 = USER_ID + "ONE";
        config.setUserId(user1);
        notifier.setupChannel(config);
        String alertsData = IOUtils.toString(
                VehicleInfoNotificationTest.class.getResourceAsStream("/alerts-data-collision.json"),
                StandardCharsets.UTF_8);

        List<AlertsInfo> infoList = NotificationUtils.getListObjects(alertsData, AlertsInfo.class);

        IgniteEvent events = transformer.fromBlob(alertsData.getBytes(), Optional.empty());

        int i = 0;
        for (IgniteEvent igEvent : events.getNestedEvents()) {
            AlertsInfo info = infoList.get(i);
            info.getAlertsData().setUserProfile(new UserProfile(user1));
            info.setIgniteEvent(igEvent);
            NotificationTemplate template = new NotificationTemplate();
            ChannelTemplates templates = new ChannelTemplates();
            PushTemplate pushTemplate = new PushTemplate();
            pushTemplate.setBody("Test Message");
            templates.setPush(pushTemplate);
            template.setChannelTemplates(templates);
            info.addNotificationTemplate("en-US", template);
            NotificationConfig notificationConfig = new NotificationConfig();
            notificationConfig.setLocale("en-US");
            info.setNotificationConfig(notificationConfig);
            i++;
        }

        AlertsInfo alert = infoList.get(0);
        FCMChannelResponse channelResponse = (FCMChannelResponse) notifier.publish(alert);
        @SuppressWarnings("unchecked")
        String fcmresponse = ((FcmPushAlertEventData) channelResponse.getAlertData()).getFcmResponse();
        assertTrue("Message id is there : ", fcmresponse.contains("multicast_id"));
        dao.deleteSingleDocument("1a13055e69d4e916fc43fa9b18f6931e5bcbf8a82b1ffe698a24f3e8e253ade9",
                "notnTokenUserMap");

    }
}
