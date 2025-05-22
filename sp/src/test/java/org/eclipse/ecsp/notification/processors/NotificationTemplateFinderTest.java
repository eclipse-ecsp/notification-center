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

package org.eclipse.ecsp.notification.processors;

import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.IVMChannel;
import org.eclipse.ecsp.domain.notification.RefreshSchedulerData;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.entities.IvmConfig;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

/**
 * NotificationTemplateFinderTest class.
 */
public class NotificationTemplateFinderTest {

    private static final String LOW_FUEL = "LowFuel";
    private static final String FITA = "fita";
    private static final String EN_US = "en_US";
    private static final String DEFAULT_BRAND = "default";
    private static final String VEHICLE_ID = "vehicleIdVal";
    private static final String USER_ID = "userIdVal";

    @InjectMocks
    private NotificationTemplateFinder notificationTemplateFinder;

    @Mock
    private NotificationTemplateDAO templateDao;

    @Mock
    private NotificationTemplateConfigDAO notificationTemplateConfigDao;

    @Mock
    private RichContentDynamicNotificationTemplateDAO richHtmlDao;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processNullConfig() {
        AlertsInfo alertsInfo = new AlertsInfo();
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        alertsInfo.setAlertsData(data);
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> notificationTemplateFinder.process(alertsInfo));
        assertEquals("Template configuration not found for notification id LowFuel", thrown.getMessage());
    }

    @Test
    public void processUserNotificationWithBrand() throws IOException {

        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, true);
        alertDataProperties.put(NotificationConstants.BRAND, FITA);
        data.setAlertDataProperties(alertDataProperties);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);

        setAlertInfoConfig(alertsInfo, "en-US");

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals("fita " + LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processUserNotificationWithoutBrand() throws IOException {
        AlertsInfo.Data data = new AlertsInfo.Data();

        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, true);
        data.setAlertDataProperties(alertDataProperties);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setAlertsData(data);

        setAlertInfoConfig(alertsInfo, "en-US");

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals(LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processUserNotificationFalse() throws IOException {

        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, false);
        data.setAlertDataProperties(alertDataProperties);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);

        setAlertInfoConfig(alertsInfo, "en-US");

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals(LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationWithVehicleProfile() throws IOException {

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");

        notificationTemplateFinder.process(alertsInfo);
        assertEquals("fita " + LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationWithVehicleProfileWithoutMake() throws IOException {
        AlertsInfo alertsInfo = new AlertsInfo();
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        data.setAlertDataProperties(alertDataProperties);
        alertsInfo.setAlertsData(data);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        vehicleProfileAbridged.setVehicleAttributes(new HashMap<>());
        data.setVehicleProfile(vehicleProfileAbridged);

        setAlertInfoConfig(alertsInfo, "en-US");

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals(LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationWithoutLocaleInConfig() throws IOException {
        AlertsInfo alertsInfo = new AlertsInfo();
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        data.setAlertDataProperties(alertDataProperties);
        alertsInfo.setAlertsData(data);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", FITA);
        vehicleProfileAbridged.setVehicleAttributes(attrs);
        data.setVehicleProfile(vehicleProfileAbridged);

        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId(USER_ID);
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals("fita " + LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationWithNotificationTemplate() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");

        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Map<String, NotificationTemplate> localeToNotificationTemplate = new HashMap<>();
        localeToNotificationTemplate.put("en-US", notificationTemplatefitaBrand);
        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplate);

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals("fita " + LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationWithNotificationTemplateNoLocale() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo("volvo", "en-US");

        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Map<String, NotificationTemplate> localeToNotificationTemplate = new HashMap<>();
        localeToNotificationTemplate.put("fr-FR", notificationTemplatefitaBrand);
        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplate);

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals(LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("en-US").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationUserLocaleDefaultBrand() throws IOException {

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        NotificationTemplate notificationTemplateDefaultBrandFr = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefaultFr.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(notificationTemplateDefaultBrandFr));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        notificationTemplateFinder.process(alertsInfo);
        assertEquals("FR " + LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationDefaultLocaleWithBrand() throws IOException {

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(notificationTemplatefitaBrand));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");
        notificationTemplateFinder.process(alertsInfo);
        assertEquals("fita " + LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationDefaultLocaleDefaultBrand() throws IOException {

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        NotificationTemplate notificationTemplateDefaultBrand = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefault.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);

        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(notificationTemplateDefaultBrand));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        notificationTemplateFinder.process(alertsInfo);
        assertEquals(LOW_FUEL + " Notification",
            alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getEmailTemplate().getSubject());
    }

    @Test
    public void processNotificationNoTemplateFound() {

        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();
        notificationTemplateConfig.setNotificationId(LOW_FUEL);
        Mockito.when(notificationTemplateConfigDao.findById(any())).thenReturn(notificationTemplateConfig);

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(CollectionUtils.isEmpty(alertsInfo.getLocaleToNotificationTemplate()));
    }

    @Test
    public void processRichContentUserLocaleUserBrand() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);
        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Arrays.asList(setLocaleAndBrand("fr-FR", FITA), setLocaleAndBrand("en-US", FITA),
                setLocaleAndBrand("fr-FR", DEFAULT_BRAND), setLocaleAndBrand("en-US", DEFAULT_BRAND)));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(
            alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getEmailTemplate().getBody()
                .contains("fr-FR " + FITA + " body"));
    }

    @Test
    public void processRichContentUserLocaleDefaultBrand() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);
        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Arrays.asList(setLocaleAndBrand("en-US", FITA), setLocaleAndBrand("fr-FR", DEFAULT_BRAND),
                setLocaleAndBrand("en-US", DEFAULT_BRAND)));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getEmailTemplate().getBody()
            .contains("fr-FR " + DEFAULT_BRAND + " body"));
    }

    @Test
    public void processRichContentDefaultLocaleUserBrand() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);
        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Arrays.asList(setLocaleAndBrand("en-US", FITA), setLocaleAndBrand("en-US", DEFAULT_BRAND)));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(
            alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getEmailTemplate().getBody()
                .contains("en-US " + FITA + " body"));
    }

    @Test
    public void processRichContentDefaultLocaleDefaultBrand() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);
        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(setLocaleAndBrand("en-US", DEFAULT_BRAND)));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getEmailTemplate().getBody()
            .contains("en-US " + DEFAULT_BRAND + " body"));
    }

    @Test
    public void processRichContentUserLocaleUserBrandNullEmail() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);

        NotificationTemplate notificationTemplatefitaBrandFr = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefitaFr.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);

        notificationTemplatefitaBrandFr.getChannelTemplates().setEmail(null);

        NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();
        notificationTemplateConfig.setNotificationId(LOW_FUEL);
        Mockito.when(notificationTemplateConfigDao.findById(any())).thenReturn(notificationTemplateConfig);

        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(notificationTemplatefitaBrandFr));
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(setLocaleAndBrand("fr-FR", FITA)));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertEquals("FR fita Low fuel detected. Current fuel level is [$.Data.fuelLevel].",
            alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getSmsTemplate().getBody());
    }

    @Test
    public void processRichContentWithoutAttachments() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);
        initDaoMocks();
        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            setLocaleAndBrand("fr-FR", FITA);
        richContentDynamicNotificationTemplate.setAttachments(null);
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(richContentDynamicNotificationTemplate));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertFalse(((GenericEventData) alertsInfo.getIgniteEvent().getEventData()).getData("fr-FR").isPresent());
    }

    @Test
    public void processRichContentNoGenericData() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        RefreshSchedulerData refreshSchedulerData = new RefreshSchedulerData();
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(refreshSchedulerData);
        alertsInfo.setIgniteEvent(igniteEvent);

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(setLocaleAndBrand("fr-FR", FITA)));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertFalse(alertsInfo.getIgniteEvent().getEventData() instanceof GenericEventData);
    }

    @Test
    public void processNotificationIvmDisabled() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");

        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Map<String, NotificationTemplate> localeToNotificationTemplate = new HashMap<>();
        localeToNotificationTemplate.put("en-US", notificationTemplatefitaBrand);
        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplate);

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        setAlertInfoConfig(alertsInfo, "en-US");
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(false);
        alertsInfo.getNotificationConfigs().get(0).setChannels(Collections.singletonList(ivmChannel));
        IvmConfig ivmConfig = new IvmConfig();
        ivmConfig.setSendAllLanguages(true);
        NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();
        notificationTemplateConfig.setNotificationId(LOW_FUEL);
        notificationTemplateConfig.setIvmConfig(ivmConfig);
        Mockito.when(notificationTemplateConfigDao.findById(any())).thenReturn(notificationTemplateConfig);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(CollectionUtils.isEmpty(alertsInfo.getAllLanguageTemplates()));
    }

    @Test
    public void processNotificationIvmEnabledNotAllLang() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");

        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Map<String, NotificationTemplate> localeToNotificationTemplate = new HashMap<>();
        localeToNotificationTemplate.put("en-US", notificationTemplatefitaBrand);
        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplate);

        initDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        setAlertInfoConfig(alertsInfo, "en-US");
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(true);
        alertsInfo.getNotificationConfigs().get(0).setChannels(Collections.singletonList(ivmChannel));
        IvmConfig ivmConfig = new IvmConfig();
        ivmConfig.setSendAllLanguages(false);
        NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();
        notificationTemplateConfig.setNotificationId(LOW_FUEL);
        notificationTemplateConfig.setIvmConfig(ivmConfig);
        Mockito.when(notificationTemplateConfigDao.findById(any())).thenReturn(notificationTemplateConfig);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(CollectionUtils.isEmpty(alertsInfo.getAllLanguageTemplates()));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processNotificationAllLang() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");

        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Map<String, NotificationTemplate> localeToNotificationTemplate = new HashMap<>();
        localeToNotificationTemplate.put("en-US", notificationTemplatefitaBrand);
        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplate);

        initDaoMocks();
        initAllLangDaoMocks();
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        setAlertInfoConfig(alertsInfo, "en-US");
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(true);
        alertsInfo.getNotificationConfigs().get(0).setChannels(Collections.singletonList(ivmChannel));
        IvmConfig ivmConfig = new IvmConfig();
        ivmConfig.setSendAllLanguages(true);
        NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();
        notificationTemplateConfig.setNotificationId(LOW_FUEL);
        notificationTemplateConfig.setIvmConfig(ivmConfig);
        Mockito.when(notificationTemplateConfigDao.findById(any())).thenReturn(notificationTemplateConfig);

        notificationTemplateFinder.process(alertsInfo);
        assertFalse(CollectionUtils.isEmpty(alertsInfo.getAllLanguageTemplates()));
        assertNull(alertsInfo.getAllLanguageTemplates().iterator().next().getEmailTemplate());
        assertNull(alertsInfo.getAllLanguageTemplates().iterator().next().getApiPushTemplate());
        assertNull(alertsInfo.getAllLanguageTemplates().iterator().next().getSmsTemplate());
        assertNull(alertsInfo.getAllLanguageTemplates().iterator().next().getPushTemplate());
        assertNull(alertsInfo.getAllLanguageTemplates().iterator().next().getPortalTemplate());
        assertNotNull(alertsInfo.getAllLanguageTemplates().iterator().next().getIvmTemplate());
        assertEquals(2, alertsInfo.getAllLanguageTemplates().size());
        assertEquals("fita LowFuel Notification",
            (alertsInfo.getAllLanguageTemplates().stream()
                .filter(template -> template.getLocale().toLanguageTag().equals("en-US"))
                .findFirst().get().getIvmTemplate().getTitle()));
        assertEquals("FR LowFuel Notification",
            (alertsInfo.getAllLanguageTemplates().stream()
                .filter(template -> template.getLocale().toLanguageTag().equals("fr-FR"))
                .findFirst().get().getIvmTemplate().getTitle()));

    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processWithPlaceholderInRichAndNotInTemplate() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);
        initDaoMocks();
        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            setLocaleAndBrand("fr-FR", FITA);
        richContentDynamicNotificationTemplate.setCustomPlaceholders(Collections.singleton("emergencyPhone"));
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(richContentDynamicNotificationTemplate));

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getCustomPlaceholders()
            .contains("emergencyPhone"));
        assertEquals(1, alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getCustomPlaceholders().size());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processWithPlaceholderInRichAndTemplate() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "fr-FR");

        initIgniteEvent(alertsInfo);
        NotificationTemplate notificationTemplatefitaBrandFr = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefitaFr.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        HashSet<String> placeholdersSet = new HashSet<>();
        placeholdersSet.add("color");
        notificationTemplatefitaBrandFr.setCustomPlaceholders(placeholdersSet);

        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            setLocaleAndBrand("fr-FR", FITA);
        richContentDynamicNotificationTemplate.setCustomPlaceholders(Collections.singleton("emergencyPhone"));

        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(any(), any(), any()))
            .thenReturn(Collections.singletonList(notificationTemplatefitaBrandFr));
        Mockito.when(richHtmlDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(richContentDynamicNotificationTemplate));

        NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();
        notificationTemplateConfig.setNotificationId(LOW_FUEL);
        Mockito.when(notificationTemplateConfigDao.findById(any())).thenReturn(notificationTemplateConfig);

        notificationTemplateFinder.setDefaultLocale(EN_US);
        notificationTemplateFinder.setDefaultBrand(DEFAULT_BRAND);

        notificationTemplateFinder.process(alertsInfo);
        assertTrue(alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getCustomPlaceholders()
            .contains("emergencyPhone"));
        assertTrue(alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getCustomPlaceholders().contains("color"));
        assertEquals(2, alertsInfo.getLocaleToNotificationTemplate().get("fr-FR").getCustomPlaceholders().size());
    }

    @NotNull
    private AlertsInfo initAlertsInfo(String make, String locale) {
        AlertsInfo alertsInfo = new AlertsInfo();
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        data.setAlertDataProperties(alertDataProperties);
        alertsInfo.setAlertsData(data);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", make);
        vehicleProfileAbridged.setVehicleAttributes(attrs);
        data.setVehicleProfile(vehicleProfileAbridged);

        setAlertInfoConfig(alertsInfo, locale);
        return alertsInfo;
    }

    private void setAlertInfoConfig(AlertsInfo alertsInfo, String locale) {
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId(USER_ID);
        notificationConfig.setLocale(locale);
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));
    }

    private void initDaoMocks() throws IOException {
        NotificationTemplate notificationTemplateDefaultBrand = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefault.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        NotificationTemplate notificationTemplatefitaBrandFr = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefitaFr.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        NotificationTemplate notificationTemplateDefaultBrandFr = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefaultFr.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("en-us"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Arrays.asList(notificationTemplatefitaBrand, notificationTemplateDefaultBrand));
        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("en-us"), Locale.forLanguageTag("en-us")),
                Arrays.asList("volvo", DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(notificationTemplateDefaultBrand));
        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("en-us")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Arrays.asList(notificationTemplatefitaBrand, notificationTemplateDefaultBrand,
                notificationTemplatefitaBrandFr,
                notificationTemplateDefaultBrandFr));
        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("fr-fr"), Locale.forLanguageTag("fr-fr")),
                Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Arrays.asList(notificationTemplatefitaBrandFr, notificationTemplateDefaultBrandFr));
        Mockito.when(templateDao.findByNotificationIdLocalesAndBrands(LOW_FUEL,
                Arrays.asList(Locale.forLanguageTag("en-us"), Locale.forLanguageTag("en-us")),
                Arrays.asList(DEFAULT_BRAND, DEFAULT_BRAND)))
            .thenReturn(Collections.singletonList(notificationTemplateDefaultBrand));

        NotificationTemplateConfig notificationTemplateConfig = new NotificationTemplateConfig();
        notificationTemplateConfig.setNotificationId(LOW_FUEL);
        Mockito.when(notificationTemplateConfigDao.findById(any())).thenReturn(notificationTemplateConfig);
    }

    private void initAllLangDaoMocks() throws IOException {
        NotificationTemplate notificationTemplateDefaultBrand = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefault.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        NotificationTemplate notificationTemplatefitaBrand = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplatefita.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        NotificationTemplate notificationTemplateDefaultBrandEs = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefaultNoIvmEs.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
        NotificationTemplate notificationTemplateDefaultBrandFr = JsonUtils.bindData(
            IOUtils.toString(
                NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefaultFr.json"),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);

        Mockito.when(templateDao.findByNotificationIdAndBrands(LOW_FUEL, Arrays.asList(FITA, DEFAULT_BRAND)))
            .thenReturn(Arrays.asList(notificationTemplateDefaultBrand, notificationTemplateDefaultBrandFr,
                notificationTemplateDefaultBrandEs, notificationTemplatefitaBrand));
    }

    private RichContentDynamicNotificationTemplate setLocaleAndBrand(String locale, String brand) throws IOException {
        RichContentDynamicNotificationTemplate template = JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream("/RichContentTemplate.json"),
                StandardCharsets.UTF_8),
            RichContentDynamicNotificationTemplate.class);
        template.setId(brand + "_" + locale + "_" + LOW_FUEL);
        template.setBrand(brand);
        template.setLocale(locale);
        template.setHtml(template.getHtml().replace("$brand$", brand).replace("$locale$", locale));
        return template;
    }

    private void initIgniteEvent(AlertsInfo alertsInfo) {
        GenericEventData genericEventData = new GenericEventData();
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(genericEventData);
        alertsInfo.setIgniteEvent(igniteEvent);
    }
}