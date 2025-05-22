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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAOMongoImpl;
import org.eclipse.ecsp.notification.config.TestSecurityLib;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAOMongoImpl;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAOMongoImpl;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAOMongoImpl;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAOImpl;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAOMongoImpl;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.processors.NotificationIdGenerator;
import org.eclipse.ecsp.notification.processors.NotificationMsgGenerator;
import org.eclipse.ecsp.notification.processors.NotificationProcessor;
import org.eclipse.ecsp.notification.processors.NotificationTemplateFinder;
import org.eclipse.ecsp.notification.processors.UserProfileEnricher;
import org.eclipse.ecsp.notification.user.profile.UserProfileService;
import org.eclipse.ecsp.testutils.CommonTestBase;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * Template processor test.
 */

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {SecondaryContactDAOImpl.class, UserProfileService.class,
    NotificationEncryptionServiceImpl.class, NotificationConfigDAOMongoImpl.class,
    IgniteDAOMongoConfigWithProps.class,
    NotificationTemplateConfigDAOMongoImpl.class,
    NotificationTemplateDAOMongoImpl.class, NotificationIdGenerator.class, NotificationTemplateFinder.class,
    NotificationMsgGenerator.class, MongoDbClient.class,
    UserProfileDAOMongoImpl.class, UserProfileEnricher.class,
    TestMockSaaaApi.class, TestSecurityLib.class,
    RestTemplate.class, NotificationConfigDAOMongoImpl.class, NotificationEncryptionServiceImpl.class,
    RichContentDynamicNotificationTemplateDAOMongoImpl.class, ObjectMapper.class})
@TestPropertySource("/notification-dao-test.properties")
public class NotificationTemplateProcessorTest extends CommonTestBase {

    @Autowired
    private NotificationTemplateDAO templateDao;

    @Autowired
    private NotificationTemplateConfigDAO templateConfigDao;


    @Autowired
    List<NotificationProcessor> sps;

    @Autowired
    private UserProfileDAO userProfileDao;

    /**
     * init before test.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    @BeforeEach
    public void init() throws IOException {
        UserProfile profile = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"), "UTF-8"),
            UserProfile.class);
        userProfileDao.save(profile);
        NotificationTemplate[] templates = new NotificationTemplate[8];
        templates = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationTemplate.json"),
                    "UTF-8"),
                NotificationTemplate.class)
            .toArray(templates);
        templateDao.saveAll(templates);
        NotificationTemplateConfig[] templateConfigs = new NotificationTemplateConfig[8];
        templateConfigs = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationTemplateConfig.json"),
                    "UTF-8"),
                NotificationTemplateConfig.class)
            .toArray(templateConfigs);
        templateConfigDao.saveAll(templateConfigs);

        NotificationTemplateFinder notificationTemplateFinder =
            (NotificationTemplateFinder) sps.stream().filter(p -> p instanceof NotificationTemplateFinder).findAny()
                .get();
        notificationTemplateFinder.setDefaultLocale("en-US");
    }

    @Test
    public void testGeofenceTemplate() throws IOException {
        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/Geofence.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        verifyTemplates(alerts);
    }

    @SuppressWarnings("checkstyle:MethodLength")
    private static void verifyTemplates(List<AlertsInfo> alerts) {
        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("GeoFence_in".equals(notificationId)) {
                Assert.assertEquals("Your vehicle is back inside the set boundary.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle is back inside the set boundary.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle is back inside the set boundary.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle is back inside the set boundary.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle is back inside the set boundary.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle is back inside the set boundary.", templates.getApiPush().getBody());
            }
            if ("GeoFence_out".equals(notificationId)) {
                Assert.assertEquals("Your vehicle is outside of the set boundary.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle is outside of the set boundary.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle is outside of the set boundary.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle is outside of the set boundary.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle is outside of the set boundary.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle is outside of the set boundary.", templates.getApiPush().getBody());
            }
            if ("GeoFence_valet_in".equals(notificationId)) {
                Assert.assertEquals("Your vehicle is back inside the valet boundary.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle is back inside the valet boundary.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle is back inside the valet boundary.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle is back inside the valet boundary.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle is back inside the valet boundary.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle is back inside the valet boundary.",
                    templates.getApiPush().getBody());
            }

            if ("GeoFence_valet_out".equals(notificationId)) {
                Assert.assertEquals("Your vehicle has gone outside of the valet boundary.",
                    templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle has gone outside of the valet boundary.",
                    templates.getSms().getBody());
                Assert.assertEquals("Your vehicle has gone outside of the valet boundary.",
                    templates.getPush().getBody());
                Assert.assertEquals("Your vehicle has gone outside of the valet boundary.",
                    templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle has gone outside of the valet boundary.",
                    templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle has gone outside of the valet boundary.",
                    templates.getApiPush().getBody());
            }

            if ("GeoFence_generic_in".equals(notificationId)) {
                Assert.assertEquals("Your vehicle is back inside the home boundary.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle is back inside the home boundary.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle is back inside the home boundary.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle is back inside the home boundary.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle is back inside the home boundary.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle is back inside the home boundary.", templates.getApiPush().getBody());
            }
            if ("GeoFence_generic_out".equals(notificationId)) {
                Assert.assertEquals("Your vehicle is outside of the home boundary.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle is outside of the home boundary.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle is outside of the home boundary.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle is outside of the home boundary.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle is outside of the home boundary.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle is outside of the home boundary.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testLowFuelTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/Lowfuel.json"), "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("LowFuel".equals(notificationId)) {
                Assert.assertEquals("Low fuel detected. Current fuel level is 10.", templates.getEmail().getBody());
                Assert.assertEquals("Low fuel detected. Current fuel level is 10.", templates.getSms().getBody());
                Assert.assertEquals("Low fuel detected. Current fuel level is 10.", templates.getPush().getBody());
                Assert.assertEquals("Low fuel detected. Current fuel level is 10.", templates.getPortal().getBody());
                Assert.assertEquals("Low fuel detected. Current fuel level is 10.", templates.getIvm().getBody());
                Assert.assertEquals("Low fuel detected. Current fuel level is 10.", templates.getApiPush().getBody());

            }
        });
    }

    @Test
    public void testCollisionTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/Collision.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("Collision".equals(notificationId)) {
                Assert.assertEquals(
                    "Vehicle crash detected at location http://maps.google.com/maps?q=loc:15.92458,79.852421. Your emergency contacts have been notified.",
                    templates.getEmail().getBody());
                Assert.assertEquals(
                    "Vehicle crash detected at location http://maps.google.com/maps?q=loc:15.92458,79.852421. Your emergency contacts have been notified.",
                    templates.getSms().getBody());
                Assert.assertEquals(
                    "Vehicle crash detected at location http://maps.google.com/maps?q=loc:15.92458,79.852421. Your emergency contacts have been notified.",
                    templates.getPush().getBody());
                Assert.assertEquals(
                    "Vehicle crash detected at location http://maps.google.com/maps?q=loc:15.92458,79.852421. Your emergency contacts have been notified.",
                    templates.getPortal().getBody());
                Assert.assertEquals(
                    "Vehicle crash detected at location http://maps.google.com/maps?q=loc:15.92458,79.852421. Your emergency contacts have been notified.",
                    templates.getIvm().getBody());
                Assert.assertEquals(
                    "Vehicle crash detected at location http://maps.google.com/maps?q=loc:15.92458,79.852421. Your emergency contacts have been notified.",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testOverSpeedingTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/OverSpeeding.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("OverSpeeding".equals(notificationId)) {
                Assert.assertEquals("Your vehicle has gone over the speed limit.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle has gone over the speed limit.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle has gone over the speed limit.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle has gone over the speed limit.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle has gone over the speed limit.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle has gone over the speed limit.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testCurfewViolationTimeTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/CurfewViolation_time.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("CurfewViolation_time".equals(notificationId)) {
                Assert.assertEquals("Your vehicle is being used past 14:00 curfew.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle is being used past 14:00 curfew.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle is being used past 14:00 curfew.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle is being used past 14:00 curfew.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle is being used past 14:00 curfew.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle is being used past 14:00 curfew.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testCurfewViolationTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/CurfewViolation.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("CurfewViolation".equals(notificationId)) {
                Assert.assertEquals("Your vehicle is being used past curfew.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle is being used past curfew.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle is being used past curfew.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle is being used past curfew.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle is being used past curfew.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle is being used past curfew.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testIdleTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/idle.json"), "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("Idle".equals(notificationId)) {
                Assert.assertEquals("Your vehicle has been idling for 50 minutes.", templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle has been idling for 50 minutes.", templates.getSms().getBody());
                Assert.assertEquals("Your vehicle has been idling for 50 minutes.", templates.getPush().getBody());
                Assert.assertEquals("Your vehicle has been idling for 50 minutes.", templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle has been idling for 50 minutes.", templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle has been idling for 50 minutes.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testTowTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/Tow.json"), "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("Tow".equals(notificationId)) {
                Assert.assertEquals(
                    "Your vehicle is being towed.Current location is http://maps.google.com/maps?q=loc:12.9716S,77.5946E.",
                    templates.getEmail().getBody());
                Assert.assertEquals(
                    "Your vehicle is being towed.Current location is http://maps.google.com/maps?q=loc:12.9716S,77.5946E.",
                    templates.getSms().getBody());
                Assert.assertEquals(
                    "Your vehicle is being towed.Current location is http://maps.google.com/maps?q=loc:12.9716S,77.5946E.",
                    templates.getPush().getBody());
                Assert.assertEquals(
                    "Your vehicle is being towed.Current location is http://maps.google.com/maps?q=loc:12.9716S,77.5946E.",
                    templates.getPortal().getBody());
                Assert.assertEquals(
                    "Your vehicle is being towed.Current location is http://maps.google.com/maps?q=loc:12.9716S,77.5946E.",
                    templates.getIvm().getBody());
                Assert.assertEquals(
                    "Your vehicle is being towed.Current location is http://maps.google.com/maps?q=loc:12.9716S,77.5946E.",
                    templates.getApiPush().getBody());

            }
        });
    }

    @Test
    public void testDongleStatusDetachedTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/DongleStatus_Detached.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("DongleStatus_detached".equals(notificationId)) {
                Assert.assertEquals("The dongle has been disconnected from your vehicle.",
                    templates.getEmail().getBody());
                Assert.assertEquals("The dongle has been disconnected from your vehicle.",
                    templates.getSms().getBody());
                Assert.assertEquals("The dongle has been disconnected from your vehicle.",
                    templates.getPush().getBody());
                Assert.assertEquals("The dongle has been disconnected from your vehicle.",
                    templates.getPortal().getBody());
                Assert.assertEquals("The dongle has been disconnected from your vehicle.",
                    templates.getIvm().getBody());
                Assert.assertEquals("The dongle has been disconnected from your vehicle.",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testDongleStatusAttachedTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/DongleStatus_Attached.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("DongleStatus_attached".equals(notificationId)) {
                Assert.assertEquals("The dongle has been reattached to your vehicle.", templates.getEmail().getBody());
                Assert.assertEquals("The dongle has been reattached to your vehicle.", templates.getSms().getBody());
                Assert.assertEquals("The dongle has been reattached to your vehicle.", templates.getPush().getBody());
                Assert.assertEquals("The dongle has been reattached to your vehicle.", templates.getPortal().getBody());
                Assert.assertEquals("The dongle has been reattached to your vehicle.", templates.getIvm().getBody());
                Assert.assertEquals("The dongle has been reattached to your vehicle.",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testDocumentExpiryTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/DocumentExpiry.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("DocumentExpiry".equals(notificationId)) {
                Assert.assertEquals("Your Driving license document will be expired in 3 day(s).",
                    templates.getEmail().getBody());
                Assert.assertEquals("Your Driving license document will be expired in 3 day(s).",
                    templates.getSms().getBody());
                Assert.assertEquals("Your Driving license document will be expired in 3 day(s).",
                    templates.getPush().getBody());
                Assert.assertEquals("Your Driving license document will be expired in 3 day(s).",
                    templates.getPortal().getBody());
                Assert.assertEquals("Your Driving license document will be expired in 3 day(s).",
                    templates.getIvm().getBody());
                Assert.assertEquals("Your Driving license document will be expired in 3 day(s).",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testServiceReminder_OdometerReadingTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(IOUtils.toString(
                    NotificationTemplateProcessorTest.class
                        .getResourceAsStream("/ServiceReminder_OdometerReading.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("ServiceReminder_OdometerReading".equals(notificationId)) {
                Assert.assertEquals("Your service is due based on odometer reading. Please visit a service station.",
                    templates.getEmail().getBody());
                Assert.assertEquals("Your service is due based on odometer reading. Please visit a service station.",
                    templates.getSms().getBody());
                Assert.assertEquals("Your service is due based on odometer reading. Please visit a service station.",
                    templates.getPush().getBody());
                Assert.assertEquals("Your service is due based on odometer reading. Please visit a service station.",
                    templates.getPortal().getBody());
                Assert.assertEquals("Your service is due based on odometer reading. Please visit a service station.",
                    templates.getIvm().getBody());
                Assert.assertEquals("Your service is due based on odometer reading. Please visit a service station.",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testServiceReminder_ReminderEngineTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/ServiceReminder_ReminderEngine.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("ServiceReminder_ReminderEngine".equals(notificationId)) {
                Assert.assertEquals("Your service is due on 2017-10-12. Please visit a service station.",
                    templates.getEmail().getBody());
                Assert.assertEquals("Your service is due on 2017-10-12. Please visit a service station.",
                    templates.getSms().getBody());
                Assert.assertEquals("Your service is due on 2017-10-12. Please visit a service station.",
                    templates.getPush().getBody());
                Assert.assertEquals("Your service is due on 2017-10-12. Please visit a service station.",
                    templates.getPortal().getBody());
                Assert.assertEquals("Your service is due on 2017-10-12. Please visit a service station.",
                    templates.getIvm().getBody());
                Assert.assertEquals("Your service is due on 2017-10-12. Please visit a service station.",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testPinGeneratedTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/PinGenerated.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("PinGenerated".equals(notificationId)) {
                Assert.assertEquals(
                    "Verification Code for registering your device with IMEI 4444 is 12345. "
                        + "This Code is valid for 5 minutes.", templates.getEmail().getBody());
                Assert.assertEquals(
                    "Verification Code for registering your device with IMEI 4444 is 12345. "
                        + "This Code is valid for 5 minutes.", templates.getSms().getBody());
                Assert.assertEquals(
                    "Verification Code for registering your device with IMEI 4444 is 12345. "
                        + "This Code is valid for 5 minutes.", templates.getPush().getBody());
                Assert.assertEquals(
                    "Verification Code for registering your device with IMEI 4444 is 12345. "
                        + "This Code is valid for 5 minutes.", templates.getPortal().getBody());
                Assert.assertEquals(
                    "Verification Code for registering your device with IMEI 4444 is 12345. "
                        + "This Code is valid for 5 minutes.", templates.getIvm().getBody());
                Assert.assertEquals(
                    "Verification Code for registering your device with IMEI 4444 is 12345. "
                        + "This Code is valid for 5 minutes.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testDisturbanceTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/Disturbance.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.setNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);

            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("Disturbance".equals(notificationId)) {
                Assert.assertEquals("Disturbance detected at location http://maps.google.com/maps?q=loc:12.54,67.44.",
                    templates.getEmail().getBody());
                Assert.assertEquals("Disturbance detected at location http://maps.google.com/maps?q=loc:12.54,67.44.",
                    templates.getSms().getBody());
                Assert.assertEquals("Disturbance detected at location http://maps.google.com/maps?q=loc:12.54,67.44.",
                    templates.getPush().getBody());
                Assert.assertEquals("Disturbance detected at location http://maps.google.com/maps?q=loc:12.54,67.44.",
                    templates.getPortal().getBody());
                Assert.assertEquals("Disturbance detected at location http://maps.google.com/maps?q=loc:12.54,67.44.",
                    templates.getIvm().getBody());
                Assert.assertEquals("Disturbance detected at location http://maps.google.com/maps?q=loc:12.54,67.44.",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testmmytemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/MMY.json"), "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("MMY".equals(notificationId)) {
                Assert.assertEquals(
                    "Your vehicle's Make, Model, Year and Vehicle Name could not be determined. Please set now.",
                    templates.getEmail().getBody());
                Assert.assertEquals(
                    "Your vehicle's Make, Model, Year and Vehicle Name could not be determined. Please set now.",
                    templates.getSms().getBody());
                Assert.assertEquals(
                    "Your vehicle's Make, Model, Year and Vehicle Name could not be determined. Please set now.",
                    templates.getPush().getBody());
                Assert.assertEquals(
                    "Your vehicle's Make, Model, Year and Vehicle Name could not be determined. Please set now.",
                    templates.getPortal().getBody());
                Assert.assertEquals(
                    "Your vehicle's Make, Model, Year and Vehicle Name could not be determined. Please set now.",
                    templates.getIvm().getBody());
                Assert.assertEquals(
                    "Your vehicle's Make, Model, Year and Vehicle Name could not be determined. Please set now.",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testActivationTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/Activation.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("Activation".equals(notificationId)) {
                Assert.assertEquals("Congratulations!  Your device is activated and ready to use!",
                    templates.getEmail().getBody());
                Assert.assertEquals("Congratulations!  Your device is activated and ready to use!",
                    templates.getSms().getBody());
                Assert.assertEquals("Congratulations!  Your device is activated and ready to use!",
                    templates.getPush().getBody());
                Assert.assertEquals("Congratulations!  Your device is activated and ready to use!",
                    templates.getPortal().getBody());
                Assert.assertEquals("Congratulations!  Your device is activated and ready to use!",
                    templates.getIvm().getBody());
                Assert.assertEquals("Congratulations!  Your device is activated and ready to use!",
                    templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testFirmwareDownloadedTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/FirmwareDownloaded.json"), "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("FirmwareDownloaded".equals(notificationId)) {
                Assert.assertEquals("Your device software is updating.", templates.getEmail().getBody());
                Assert.assertEquals("Your device software is updating.", templates.getSms().getBody());
                Assert.assertEquals("Your device software is updating.", templates.getPush().getBody());
                Assert.assertEquals("Your device software is updating.", templates.getPortal().getBody());
                Assert.assertEquals("Your device software is updating.", templates.getIvm().getBody());
                Assert.assertEquals("Your device software is updating.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testFirmwareUpgradedTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/FirmwareUpgraded.json"),
                    "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("FirmwareUpgraded".equals(notificationId)) {
                Assert.assertEquals("Software update for your device is complete.", templates.getEmail().getBody());
                Assert.assertEquals("Software update for your device is complete.", templates.getSms().getBody());
                Assert.assertEquals("Software update for your device is complete.", templates.getPush().getBody());
                Assert.assertEquals("Software update for your device is complete.", templates.getPortal().getBody());
                Assert.assertEquals("Software update for your device is complete.", templates.getIvm().getBody());
                Assert.assertEquals("Software update for your device is complete.", templates.getApiPush().getBody());
            }
        });
    }

    @Test
    public void testNonIdleTemplate() throws IOException {

        List<AlertsInfo> alerts = JsonUtils
            .getListObjects(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/NonIdle.json"), "UTF-8"),
                AlertsInfo.class);
        for (AlertsInfo alert : alerts) {
            NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(
                    NotificationTemplateProcessorTest.class.getResourceAsStream("/NotificationConfig.json"), "UTF-8"),
                NotificationConfig.class);
            UserProfile profile = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest.class.getResourceAsStream("/UserProfile.json"),
                    "UTF-8"),
                UserProfile.class);
            alert.addNotificationConfig(config);
            alert.getAlertsData().setUserProfile(profile);
            sps.forEach(sp -> sp.process(alert));
        }

        alerts.forEach(alert -> {
            String notificationId = alert.getAlertsData().getNotificationId();
            ChannelTemplates templates = alert.getNotificationTemplate().getChannelTemplates();
            if ("NonIdle".equals(notificationId)) {
                Assert.assertEquals("Your vehicle has started to move after idling for 50 minutes.",
                    templates.getEmail().getBody());
                Assert.assertEquals("Your vehicle has started to move after idling for 50 minutes.",
                    templates.getSms().getBody());
                Assert.assertEquals("Your vehicle has started to move after idling for 50 minutes.",
                    templates.getPush().getBody());
                Assert.assertEquals("Your vehicle has started to move after idling for 50 minutes.",
                    templates.getPortal().getBody());
                Assert.assertEquals("Your vehicle has started to move after idling for 50 minutes.",
                    templates.getIvm().getBody());
                Assert.assertEquals("Your vehicle has started to move after idling for 50 minutes.",
                    templates.getApiPush().getBody());

                Assert.assertEquals("NonIdle Notification for test-user", templates.getEmail().getTitle());
                Assert.assertEquals("NonIdle Notification for test-user", templates.getSms().getTitle());
                Assert.assertEquals("NonIdle Notification for test-user", templates.getPush().getTitle());
                Assert.assertEquals("NonIdle Notification for test-user", templates.getPortal().getTitle());
                Assert.assertEquals("NonIdle Notification for test-user", templates.getIvm().getTitle());
                Assert.assertEquals("NonIdle Notification for test-user", templates.getApiPush().getTitle());


                Assert.assertEquals("NonIdle Notification for test-vehicle", templates.getEmail().getSubtitle());
                Assert.assertEquals("NonIdle Notification for test-vehicle", templates.getSms().getSubtitle());
                Assert.assertEquals("NonIdle Notification for test-vehicle", templates.getPush().getSubtitle());
                Assert.assertEquals("NonIdle Notification for test-vehicle", templates.getPortal().getSubtitle());
                Assert.assertEquals("NonIdle Notification for test-vehicle", templates.getIvm().getSubtitle());
                Assert.assertEquals("NonIdle Notification for test-vehicle", templates.getApiPush().getSubtitle());
            }
        });
    }



}
