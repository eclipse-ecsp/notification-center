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


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * UtilsTest class.
 */
public class UtilsTest {

    private static final String LOW_FUEL = "LowFuel";
    private static final String FITA = "fita";
    private static final String VEHICLE_ID = "vehicleIdVal";
    private static final String USER_ID = "userIdVal";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void getRankWithAdditionalLookupProperties() throws IOException {

        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, true);
        data.setAlertDataProperties(alertDataProperties);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", FITA);
        attrs.put("model", "fiesta");
        attrs.put("modelYear", "2004");
        vehicleProfileAbridged.setVehicleAttributes(attrs);
        data.setVehicleProfile(vehicleProfileAbridged);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);

        setAlertInfoConfig(alertsInfo, "en-US");


        AdditionalLookupProperty additionalLookupProperties = new AdditionalLookupProperty();
        additionalLookupProperties.setName("vehicleProfile.model");
        additionalLookupProperties.setOrder((short) 1);
        additionalLookupProperties.setValues(Collections.singleton("fiesta"));
        NotificationTemplate notificationTemplateDefaultBrand = JsonUtils.bindData(
                IOUtils.toString(
                        NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefault.json"),
                        StandardCharsets.UTF_8),
                NotificationTemplate.class);
        notificationTemplateDefaultBrand.setAdditionalLookupProperties(Arrays.asList(additionalLookupProperties));

        String dataStr = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);

        int rank = Utils.getRank(notificationTemplateDefaultBrand, FITA, Locale.forLanguageTag("en-US"), dataStr);
        assertEquals(5, rank);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void getRankWithAdditionalLookupPropertiesNoValue() throws IOException {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, true);
        data.setAlertDataProperties(alertDataProperties);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", FITA);
        attrs.put("model", "fiesta");
        vehicleProfileAbridged.setVehicleAttributes(attrs);
        data.setVehicleProfile(vehicleProfileAbridged);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setAlertsData(data);

        setAlertInfoConfig(alertsInfo, "en-US");


        AdditionalLookupProperty additionalLookupProperty1 = new AdditionalLookupProperty();
        additionalLookupProperty1.setName("vehicleProfile.model");
        additionalLookupProperty1.setOrder((short) 1);
        additionalLookupProperty1.setValues(Collections.singleton("fiesta"));
        AdditionalLookupProperty additionalLookupProperty2 = new AdditionalLookupProperty();
        additionalLookupProperty2.setName("vehicleProfile.modelYear");
        additionalLookupProperty2.setOrder((short) 2);
        additionalLookupProperty2.setValues(Collections.singleton("2004"));
        NotificationTemplate notificationTemplateDefaultBrand = JsonUtils.bindData(
                IOUtils.toString(
                        NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefault.json"),
                        StandardCharsets.UTF_8),
                NotificationTemplate.class);
        notificationTemplateDefaultBrand.setAdditionalLookupProperties(
            Arrays.asList(additionalLookupProperty1, additionalLookupProperty2));

        String dataStr = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);

        int rank = Utils.getRank(notificationTemplateDefaultBrand, FITA, Locale.forLanguageTag("en-US"), dataStr);
        assertEquals(-1, rank);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void getRankWithAdditionalLookupPropertiesNoMatch() throws IOException {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId(LOW_FUEL);
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, true);
        data.setAlertDataProperties(alertDataProperties);
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        vehicleProfileAbridged.setVehicleId(VEHICLE_ID);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", FITA);
        attrs.put("model", "focus");
        vehicleProfileAbridged.setVehicleAttributes(attrs);
        data.setVehicleProfile(vehicleProfileAbridged);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setAlertsData(data);

        setAlertInfoConfig(alertsInfo, "fr-FR");



        AdditionalLookupProperty additionalLookupProperty1 = new AdditionalLookupProperty();
        additionalLookupProperty1.setName("vehicleProfile.model");
        additionalLookupProperty1.setOrder((short) 1);
        additionalLookupProperty1.setValues(Collections.singleton("fiesta"));
        AdditionalLookupProperty additionalLookupProperty2 = new AdditionalLookupProperty();
        additionalLookupProperty2.setName("vehicleProfile.modelYear");
        additionalLookupProperty2.setOrder((short) 2);
        additionalLookupProperty2.setValues(Collections.singleton("2004"));
        NotificationTemplate notificationTemplateDefaultBrand = JsonUtils.bindData(
                IOUtils.toString(
                        NotificationTemplateFinderTest.class.getResourceAsStream("/NotificationTemplateDefault.json"),
                        StandardCharsets.UTF_8),
                NotificationTemplate.class);
        notificationTemplateDefaultBrand.setAdditionalLookupProperties(
            Arrays.asList(additionalLookupProperty1, additionalLookupProperty2));

        String dataStr = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);

        int rank = Utils.getRank(notificationTemplateDefaultBrand, FITA, Locale.forLanguageTag("fr-FR"), dataStr);
        assertEquals(-1, rank);
    }

    private void setAlertInfoConfig(AlertsInfo alertsInfo, String locale) {
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId(USER_ID);
        notificationConfig.setLocale(locale);
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));
    }
}