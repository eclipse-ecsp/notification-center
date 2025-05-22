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
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.NotificationPlaceholderDao;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholder;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
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
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * CustomPlaceholderProcessorTest class.
 */
public class CustomPlaceholderProcessorTest {

    private static final String LOW_FUEL = "LowFuel";
    private static final String FITA = "fita";
    private static final String EN_US = "en_US";
    private static final String DEFAULT_BRAND = "default";
    private static final String VEHICLE_ID = "vehicleIdVal";
    private static final String USER_ID = "userIdVal";

    @InjectMocks
    private CustomPlaceholderProcessor customPlaceholderProcessor;

    @Mock
    private NotificationPlaceholderDao notificationPlaceholderDao;

    /**
     * setup method.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(customPlaceholderProcessor, "defaultLocale", "en_US");
        ReflectionTestUtils.setField(customPlaceholderProcessor, "defaultBrand", "default");
    }

    @Test
    public void processNoTemplates() {
        AlertsInfo alertsInfo = initAlertsInfo(null, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        customPlaceholderProcessor.process(alertsInfo);
        assertTrue(CollectionUtils.isEmpty(alertsInfo.getLocaleToPlaceholders()));
    }

    @Test
    public void processNoPlaceholders() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        initAlertInfoTemplate(alertsInfo, "/NotificationTemplatefita.json", "en-US");
        customPlaceholderProcessor.process(alertsInfo);
        assertTrue(CollectionUtils.isEmpty(alertsInfo.getLocaleToPlaceholders()));
    }

    @Test
    public void processNoPlaceholderInMongo() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        initAlertInfoTemplate(alertsInfo, "/NotificationTemplatefitaPlaceholders.json", "en-US");
        when(notificationPlaceholderDao.findByKeysBrandsAndLocales(any(), any(), any())).thenReturn(new ArrayList<>());
        customPlaceholderProcessor.process(alertsInfo);
        assertTrue(CollectionUtils.isEmpty(alertsInfo.getLocaleToPlaceholders().get("en_US")));
    }

    @Test
    public void processNoFitPlaceholder() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        initAlertInfoTemplate(alertsInfo, "/NotificationTemplatefitaPlaceholders.json", "en-US");
        NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
        notificationPlaceholder.setKey("aaa");
        notificationPlaceholder.setLocale(Locale.forLanguageTag("en-US"));
        notificationPlaceholder.setValue("aaaVal");
        when(notificationPlaceholderDao.findByKeysBrandsAndLocales(any(), any(), any()))
            .thenReturn(Collections.singletonList(notificationPlaceholder));
        customPlaceholderProcessor.process(alertsInfo);
        assertTrue(CollectionUtils.isEmpty(alertsInfo.getLocaleToPlaceholders().get("en_US")));
    }

    @Test
    public void processPlaceholderOneTemplateSuccess() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        initAlertInfoTemplate(alertsInfo, "/NotificationTemplatefitaPlaceholders.json", "en-US");
        NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
        notificationPlaceholder.setKey("emergencyPhoneRich");
        notificationPlaceholder.setLocale(Locale.forLanguageTag("en-US"));
        notificationPlaceholder.setValue("emergencyPhoneRichVal");
        notificationPlaceholder.setBrand(FITA);
        NotificationPlaceholder notificationPlaceholder2 = new NotificationPlaceholder();
        notificationPlaceholder2.setKey("emergencyPhoneRich");
        notificationPlaceholder2.setLocale(Locale.forLanguageTag("en-US"));
        notificationPlaceholder2.setValue("emergencyPhoneRichVal2");
        notificationPlaceholder2.setBrand(DEFAULT_BRAND);
        when(notificationPlaceholderDao.findByKeysBrandsAndLocales(any(), any(), any()))
            .thenReturn(Arrays.asList(notificationPlaceholder, notificationPlaceholder2));
        customPlaceholderProcessor.process(alertsInfo);
        assertEquals("emergencyPhoneRichVal",
            (alertsInfo.getLocaleToPlaceholders().get("en_US")).get("emergencyPhoneRich"));
    }

    @Test
    public void processPlaceholderTwoTemplateSuccess() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        initAlertInfoTemplate(alertsInfo, "/NotificationTemplatefitaPlaceholders.json", "en-US");
        NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
        notificationPlaceholder.setKey("emergencyPhoneRich");
        notificationPlaceholder.setLocale(Locale.forLanguageTag("en-US"));
        notificationPlaceholder.setValue("emergencyPhoneRichVal");
        notificationPlaceholder.setBrand(FITA);
        when(notificationPlaceholderDao.findByKeysBrandsAndLocales(any(), any(), any()))
            .thenReturn(Collections.singletonList(notificationPlaceholder));
        customPlaceholderProcessor.process(alertsInfo);
        assertEquals("emergencyPhoneRichVal",
            (alertsInfo.getLocaleToPlaceholders().get("en_US")).get("emergencyPhoneRich"));
    }

    @Test
    public void processPlaceholderIvmAllLocales() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        initAlertInfoTemplate(alertsInfo, "/NotificationTemplatefitaPlaceholders.json", "en-US");
        NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
        notificationPlaceholder.setKey("emergencyPhoneRich");
        notificationPlaceholder.setLocale(Locale.forLanguageTag("en-US"));
        notificationPlaceholder.setValue("emergencyPhoneRichVal");
        notificationPlaceholder.setBrand(FITA);

        NotificationPlaceholder notificationPlaceholder2 = new NotificationPlaceholder();
        notificationPlaceholder2.setKey("key1");
        notificationPlaceholder2.setLocale(Locale.forLanguageTag("fr-FR"));
        notificationPlaceholder2.setValue("key1Val");
        notificationPlaceholder2.setBrand(DEFAULT_BRAND);

        Set<NotificationTemplate> allLanguageTemplates = new HashSet<>();
        allLanguageTemplates.add(
            getCustomTemplate("/placeholders/IvmNotificationTemplatePlaceholders.json", "en_US", FITA));
        allLanguageTemplates.add(
            getCustomTemplate("/placeholders/IvmNotificationTemplatePlaceholders.json", "fr_FR", DEFAULT_BRAND));
        alertsInfo.setAllLanguageTemplates(allLanguageTemplates);
        when(notificationPlaceholderDao.findByKeysBrandsAndLocales(any(), any(), any()))
            .thenReturn(Collections.singletonList(notificationPlaceholder))
            .thenReturn(Collections.singletonList(notificationPlaceholder2));
        customPlaceholderProcessor.process(alertsInfo);
        assertEquals("emergencyPhoneRichVal",
            (alertsInfo.getLocaleToPlaceholders().get("en_US")).get("emergencyPhoneRich"));
        assertEquals("key1Val", (alertsInfo.getLocaleToPlaceholders().get("fr_FR")).get("key1"));
    }

    @Test
    public void processNoPlaceholderIvmAllLocales() throws IOException {
        AlertsInfo alertsInfo = initAlertsInfo(FITA, "en-US");
        alertsInfo.setLocaleToNotificationTemplate(new HashMap<>());
        initAlertInfoTemplate(alertsInfo, "/NotificationTemplatefitaPlaceholders.json", "en-US");
        NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
        notificationPlaceholder.setKey("emergencyPhoneRich");
        notificationPlaceholder.setLocale(Locale.forLanguageTag("en-US"));
        notificationPlaceholder.setValue("emergencyPhoneRichVal");
        notificationPlaceholder.setBrand(FITA);

        NotificationPlaceholder notificationPlaceholder2 = new NotificationPlaceholder();
        notificationPlaceholder2.setKey("key1");
        notificationPlaceholder2.setLocale(Locale.forLanguageTag("fr-FR"));
        notificationPlaceholder2.setValue("key1Val");
        notificationPlaceholder2.setBrand(DEFAULT_BRAND);

        Set<NotificationTemplate> allLanguageTemplates = new HashSet<>();
        allLanguageTemplates.add(
            getCustomTemplate("/placeholders/IvmNotificationTemplatePlaceholders.json", "en_US", FITA));
        NotificationTemplate nt =
            getCustomTemplate("/placeholders/IvmNotificationTemplatePlaceholders.json", "fr_FR", DEFAULT_BRAND);
        nt.setCustomPlaceholders(new HashSet<>());
        allLanguageTemplates.add(nt);
        alertsInfo.setAllLanguageTemplates(allLanguageTemplates);
        when(notificationPlaceholderDao.findByKeysBrandsAndLocales(any(), any(), any()))
            .thenReturn(Collections.singletonList(notificationPlaceholder))
            .thenReturn(Collections.singletonList(notificationPlaceholder2));
        customPlaceholderProcessor.process(alertsInfo);
        assertEquals("emergencyPhoneRichVal",
            (alertsInfo.getLocaleToPlaceholders().get("en_US")).get("emergencyPhoneRich"));
        assertNull(alertsInfo.getLocaleToPlaceholders().get("fr_FR"));
    }

    private AlertsInfo initAlertsInfo(String make, String locale) {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.BRAND, make == null ? null : make.toLowerCase());
        data.setAlertDataProperties(alertDataProperties);
        AlertsInfo alertsInfo = new AlertsInfo();
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

    private void initAlertInfoTemplate(AlertsInfo alertsInfo, String fileName, String locale) throws IOException {
        NotificationTemplate notificationTemplatefitaBrand = getNotificationTemplate(fileName);
        Map<String, NotificationTemplate> localeToNotificationTemplate = new HashMap<>();
        localeToNotificationTemplate.put(locale, notificationTemplatefitaBrand);
        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplate);

    }

    private NotificationTemplate getNotificationTemplate(String fileName) throws IOException {
        return JsonUtils.bindData(
            IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream(fileName),
                StandardCharsets.UTF_8),
            NotificationTemplate.class);
    }

    private NotificationTemplate getCustomTemplate(String fileName, String locale, String brand) throws IOException {

        String templateStr = IOUtils.toString(NotificationTemplateFinderTest.class.getResourceAsStream(fileName),
            StandardCharsets.UTF_8);
        templateStr = templateStr.replace("$locale", locale).replace("$brand", brand);
        return JsonUtils.bindData(templateStr, NotificationTemplate.class);
    }
}