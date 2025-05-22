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
import io.prometheus.client.CollectorRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.ecsp.analytics.stream.base.Launcher;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaStreamsApplicationTestBase;
import org.eclipse.ecsp.domain.notification.AmazonSNSSMSResponse;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.config.TestSecurityLib;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAOMongoImpl;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.duplication.Deduplicator;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.PushTemplate;
import org.eclipse.ecsp.notification.entities.SMSTemplate;
import org.eclipse.ecsp.notification.entities.SmsConfig;
import org.eclipse.ecsp.notification.fcm.FcmNotifier;
import org.eclipse.ecsp.notification.processors.AlertProcessorChain;
import org.eclipse.ecsp.notification.processors.NotificationMsgGenerator;
import org.eclipse.ecsp.notification.processors.NotificationTemplateFinder;
import org.eclipse.ecsp.notification.utils.TimeCalculator;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * VehicleInfoNotificationUnitTest.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestSecurityLib.class, Launcher.class})
@Ignore
@SuppressWarnings("checkstyle:MagicNumber")
public class VehicleInfoNotificationUnitTest extends KafkaStreamsApplicationTestBase {
    private VehicleInfoNotification vehicleInfoNotification;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Autowired
    private GenericIgniteEventTransformer transformer;

    @Autowired
    private NotificationConfigDAO notificationConfigDao;

    @Autowired
    private NotificationBufferDao notificationBufferDao;

    @Autowired
    private AlertProcessorChain alertProcessorChain;

    @Autowired
    private ApplicationContext applicationContext;

    private Properties properties;

    IgniteEvent event;

    @Mock
    KeyValueStore<String, String> notificationStateStore;

    @Mock
    NotificationDao notificationDao;

    @Mock
    Deduplicator deduplicator;

    @Mock
    AlertProcessorChain alertProcessor;

    @Mock
    NotificationTemplateDAOMongoImpl templateDao;

    @Mock
    NotificationTemplateConfig configDao;

    @Mock
    NotificationTemplateConfigDAO notificationTemplateConfigDao;

    @Mock
    RichContentDynamicNotificationTemplateDAO richContentDynamicNotificationTemplateDao;

    @Mock
    ChannelNotifierRegistry channelNotifierRegistry;

    @Mock
    ChannelNotifier notifier;

    @Mock
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessingContext;

    @Mock
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @Mock
    private ScheduleNotificationAssistant scheduleNotificationAssistant;

    /**
     * set up.
     */
    @Before
    public void setup() throws Exception {
        CollectorRegistry.defaultRegistry.clear();
        event = new IgniteEventImpl();
        vehicleInfoNotification = new VehicleInfoNotification();
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationStateStore", notificationStateStore);
        ReflectionTestUtils.setField(vehicleInfoNotification, "deduplicator", deduplicator);
        ReflectionTestUtils.setField(vehicleInfoNotification, "alertProcessorChain", alertProcessor);
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationDAO", notificationDao);
        ReflectionTestUtils.setField(vehicleInfoNotification, "channelNotifierRegistry", channelNotifierRegistry);
        ReflectionTestUtils.setField(vehicleInfoNotification, "coreVehicleProfileClient", coreVehicleProfileClient);
        ReflectionTestUtils.setField(vehicleInfoNotification, "scheduleNotificationAssistant",
            coreVehicleProfileClient);
        vehicleInfoNotification.initConfig(NotificationTestUtils.loadProperties("/application.properties"));
    }

    //Tests for currentDateTimeIsInInterval method

    /**
     * suppression case: VACATION - The suppression day is 1/1/2019 - 10/1/2019, 22:00 - 05:00
     * the current date time is 1/1/2019, 23:00
     * date time is in interval so method should return true.
     */
    @Test
    public void dateTimeInInterval() {
        String vacation = SuppressionConfig.SuppressionType.VACATION.toString();
        SuppressionConfig suppressionConfig = new SuppressionConfig(vacation,
            "22:00", "05:00", "2019-01-01", "2019-01-10", Collections.emptyList());

        // Date: 1/1/2019 23:00
        LocalDateTime current = LocalDateTime.of(2019, 1, 1, 23, 0);
        assertTrue(TimeCalculator.currentDateTimeIsInInterval(suppressionConfig, current));
    }


    //Tests for currentDateTimeIsInRecurringInterval method
    @Test
    public void testEmptyDays() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "", "", "", "", Collections.emptyList());
        assertFalse(TimeCalculator.currentDateTimeIsInRecurringInterval(suppressionConfig, LocalDateTime.now()));
    }

    /**
     * suppression case: RECURRING
     * # - tested point in time.
     * <p>
     * Start Time          Midnight            End time
     * -----------------------|---------------#---|-------------------|-------------</p>
     */
    @Test
    public void currentDateTimeInIntervalOverNightCase() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        List<Integer> daysList = Collections.singletonList(3); // 3 for Wednesday
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "22:00", "05:00", "", "", daysList);

        // Date: 13/02/2019 23:30
        LocalDateTime wednesdayNight = LocalDateTime.of(2019, 2, 13, 23, 30);
        assertTrue(TimeCalculator.currentDateTimeIsInRecurringInterval(suppressionConfig, wednesdayNight));
    }

    /**
     * suppression case: RECURRING
     * # - tested point in time.
     * <p>
     * Start Time          Midnight             End time
     * -----------------------|-------------------|----------------#---|-------------</p>
     */
    @Test
    public void nextDateTimeInIntervalOverNightCase() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        List<Integer> daysList = Collections.singletonList(3); // 3 for Wednesday
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "22:00", "05:00", "", "", daysList);

        // Date: 14/02/2019 04:30
        LocalDateTime wednesdayNight = LocalDateTime.of(2019, 2, 14, 4, 30);
        assertTrue(TimeCalculator.currentDateTimeIsInRecurringInterval(suppressionConfig, wednesdayNight));
    }

    /**
     * suppression case: RECURRING - The suppression day is Wednesday 22:00 - 05:00
     * the current time is the day before at the 22:30
     * although the hour is in Interval, the day is not and method should return false.
     */
    @Test
    public void dayNotInListOverNightCase() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        List<Integer> daysList = Collections.singletonList(3); // 3 for Wednesday
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "22:00", "05:00", "", "", daysList);

        // Date: 12/02/2019 21:30
        LocalDateTime tuesdayNight = LocalDateTime.of(2019, 2, 12, 22, 30);
        assertFalse(TimeCalculator.currentDateTimeIsInRecurringInterval(suppressionConfig, tuesdayNight));
    }

    /**
     * suppression case: RECURRING - The suppression day is Wednesday 22:00 - 05:00
     * the current time is the same day at the 06:30
     * although it is the right day, the hour is not in interval and method should return false.
     */
    @Test
    public void hourNotInIntervalOverNightCase() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        List<Integer> daysList = Collections.singletonList(3); // 3 for Wednesday
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "22:00", "05:00", "", "", daysList);

        // Date: 15/02/2019 04:30
        LocalDateTime fridayMorning = LocalDateTime.of(2019, 2, 13, 6, 30);
        assertFalse(TimeCalculator.currentDateTimeIsInRecurringInterval(suppressionConfig, fridayMorning));
    }

    /**
     * suppression case: VACATION - Test to verify duration between StartDateTime
     * and EndDateTime.
     */
    @Test
    public void testCalculateQpDurationForVacation() {
        String vacation = SuppressionConfig.SuppressionType.VACATION.toString();
        SuppressionConfig suppressionConfig = new SuppressionConfig(vacation,
            "21:00", "00:01", "2019-01-01", "2019-01-02", Collections.emptyList());

        // Date: 1/1/2019 22:00
        LocalDateTime current = LocalDateTime.of(2019, 1, 1, 22, 0);
        assertEquals(7305000, ScheduleNotificationAssistant.calculateQpDuration(current, suppressionConfig) * 1000);
    }

    /**
     * suppression case: RECURRING - Test to verify duration between StartTime
     * and EndDime when QP is over Midnight and is after midnight.
     */
    @Test
    public void testCalculateQpDurationForRecurringAfterMidNight() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        List<Integer> daysList = Collections.singletonList(3); // 3 for Wednesday
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "22:00", "05:00", "", "", daysList);

        // Date: 14/02/2019 04:30
        LocalDateTime wednesdayNight = LocalDateTime.of(2019, 2, 14, 4, 30);
        assertEquals(1845, ScheduleNotificationAssistant.calculateQpDuration(wednesdayNight, suppressionConfig));
    }

    /**
     * suppression case: RECURRING - Test to verify duration between StartTime
     * and EndDime when QP is over Midnight and is before midnight.
     */
    @Test
    public void testCalculateQpDurationForRecurringBeforeMidNight() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        List<Integer> daysList = Collections.singletonList(3); // 3 for Wednesday
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "22:00", "05:00", "", "", daysList);

        // Date: 13/02/2019 23:30
        LocalDateTime wednesdayNight = LocalDateTime.of(2019, 2, 13, 23, 30);
        assertEquals(19845, ScheduleNotificationAssistant.calculateQpDuration(wednesdayNight, suppressionConfig));
    }

    /**
     * suppression case: RECURRING - Test to verify duration between StartTime
     * and EndDime when QP is non over Midnight scenario.
     */
    @Test
    public void testCalculateQpDurationForRecurring() {
        String recurring = SuppressionConfig.SuppressionType.RECURRING.toString();
        List<Integer> daysList = Collections.singletonList(3); // 3 for Wednesday
        SuppressionConfig suppressionConfig = new SuppressionConfig(recurring,
            "06:00", "08:00", "", "", daysList);

        // Date: 13/02/2019 07:00
        LocalDateTime wednesdayNight = LocalDateTime.of(2019, 2, 13, 7, 0);
        assertEquals(3645, ScheduleNotificationAssistant.calculateQpDuration(wednesdayNight, suppressionConfig));
    }

    @Test
    public void sendSmsGreenPath() throws Exception {
        MockitoAnnotations.initMocks(this);
        VehicleInfoNotification vehicleInfoNotification = initVehicleInfoNotificationForNonRegisteredUser();

        SMSTemplate smsTemplate = new SMSTemplate();
        smsTemplate.setBody("body");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setSms(smsTemplate);
        initNotificationData(vehicleInfoNotification, channelTemplates);
        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/non-register-user-event-sms.json"), "UTF-8");

        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());
        Assertions.assertNotNull(event);
        vehicleInfoNotification.process(new IgniteStringKey("fdsdsf"), event, "notification");
    }

    @Test
    public void sendEmailGreenPath() throws Exception {
        MockitoAnnotations.initMocks(this);

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setFrom("me");
        emailTemplate.setSubject("subject");
        emailTemplate.setBody("body");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setEmail(emailTemplate);

        VehicleInfoNotification vehicleInfoNotification = initVehicleInfoNotificationForNonRegisteredUser();

        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/non-register-user-event-email.json"), "UTF-8");
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());

        initNotificationData(vehicleInfoNotification, channelTemplates);
        Assertions.assertNotNull(event);
        vehicleInfoNotification.process(new IgniteStringKey("fdsdsf"), event, "notification");
    }

    @Test
    public void sendPushGreenPath() throws Exception {
        MockitoAnnotations.initMocks(this);

        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setTitle("title push");
        pushTemplate.setBody("web push body test");
        pushTemplate.setSubtitle("subtitle");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setPush(pushTemplate);
        VehicleInfoNotification vehicleInfoNotification = initVehicleInfoNotificationForNonRegisteredUser();

        String notificationSettingsData = IOUtils.toString(
            VehicleInfoNotificationTest.class.getResourceAsStream("/non-register-user-event-push.json"), "UTF-8");
        IgniteEvent event = transformer.fromBlob(notificationSettingsData.getBytes(), Optional.empty());

        initNotificationData(vehicleInfoNotification, channelTemplates);
        Assertions.assertNotNull(event);
        vehicleInfoNotification.process(new IgniteStringKey("fdsdsf"), event, "notification");
    }

    private void initNotificationData(VehicleInfoNotification vehicleInfoNotification,
                                      ChannelTemplates channelTemplates) throws Exception {
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setNotificationId("LowFuel");
        notificationTemplate.setBrand("default");
        notificationTemplate.setLocale(Locale.forLanguageTag("fr_FR"));
        notificationTemplate.setChannelTemplates(channelTemplates);
        NotificationTemplateFinder notificationTemplateFinder = new NotificationTemplateFinder();
        notificationTemplateFinder.setTemplateDao(templateDao);
        notificationTemplateFinder.setDefaultLocale("fr_FR");
        SmsConfig smsConfig = new SmsConfig();
        smsConfig.setSmsType("EMAIL");
        NotificationTemplateConfig config = new NotificationTemplateConfig();
        config.setSmsConfig(smsConfig);
        notificationTemplateFinder.setRichHtmlDao(richContentDynamicNotificationTemplateDao);
        notificationTemplateFinder.setNotificationTemplateConfigDao(notificationTemplateConfigDao);
        NotificationMsgGenerator notificationMsgGenerator = new NotificationMsgGenerator();
        when(templateDao.findByNotificationIdLocaleAndBrand(Mockito.anyString(), Mockito.any(),
            Mockito.anyString())).thenReturn(Optional.of(notificationTemplate));
        when(alertProcessor.getProcessors()).thenReturn(
            Arrays.asList(notificationTemplateFinder, notificationMsgGenerator));
        when(configDao.getSmsConfig()).thenReturn(smsConfig);
        when(richContentDynamicNotificationTemplateDao.findByNotificationIdLocaleAndBrand(Mockito.anyString(),
            Mockito.any(), Mockito.anyString())).thenReturn(Optional.empty());
        when(notificationTemplateConfigDao.findById(Mockito.anyString())).thenReturn(config);
        when(notifier.publish(Mockito.any())).thenReturn(new AmazonSNSSMSResponse());
        when(channelNotifierRegistry.channelNotifier(ChannelType.SMS)).thenReturn(notifier);
        FcmNotifier fcmNotifier = new FcmNotifier();
        fcmNotifier.init(properties, new MetricRegistry(), notificationDao);
        when(channelNotifierRegistry.channelNotifier(ChannelType.MOBILE_APP_PUSH)).thenReturn(fcmNotifier);

        ReflectionTestUtils.setField(vehicleInfoNotification, "alertProcessorChain", alertProcessor);
        ReflectionTestUtils.setField(vehicleInfoNotification, "channelNotifierRegistry", channelNotifierRegistry);
    }

    private VehicleInfoNotification initVehicleInfoNotificationForNonRegisteredUser() throws IOException {
        MockitoAnnotations.initMocks(this);
        properties = NotificationTestUtils.loadProperties("/application.properties");
        VehicleInfoNotification vehicleInfoNotification = new VehicleInfoNotification();
        ReflectionTestUtils.setField(vehicleInfoNotification, "ctxt", streamProcessingContext);
        ReflectionTestUtils.setField(vehicleInfoNotification, "applicationContext", applicationContext);
        ReflectionTestUtils.setField(vehicleInfoNotification, "scheduleNotificationAssistant",
            scheduleNotificationAssistant);
        vehicleInfoNotification.initConfig(properties);
        vehicleInfoNotification.getChannelsSupported().remove(ChannelType.API_PUSH);
        vehicleInfoNotification.getChannelsSupported().remove(ChannelType.IVM);
        vehicleInfoNotification.getChannelsSupported().remove(ChannelType.PORTAL);
        try {
            vehicleInfoNotification.init(streamProcessingContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationConfigDAO", notificationConfigDao);
        ReflectionTestUtils.setField(vehicleInfoNotification, "notificationBufferDao", notificationBufferDao);
        ReflectionTestUtils.setField(vehicleInfoNotification, "alertProcessorChain", alertProcessorChain);
        return vehicleInfoNotification;
    }

    @Test
    public void testKafkaStateStoreGetUserIdMethod() {
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.USERID, Optional.of("admin"));
        when(coreVehicleProfileClient.getVehicleProfileAttributes("HUOFDZHR4GDO39", false,
            VehicleProfileAttribute.USERID)).thenReturn(attr);
        when(notificationStateStore.get("HUOFDZHR4GDO39")).thenReturn(null);
        assertEquals("admin", vehicleInfoNotification.getUserId("HUOFDZHR4GDO39"));
    }
    //End
}