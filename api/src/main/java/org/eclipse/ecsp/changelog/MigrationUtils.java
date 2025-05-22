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

package org.eclipse.ecsp.changelog;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * DB data migration utility class.
 */
public class MigrationUtils {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(MigrationUtils.class);

    private static final String[] TEMPLATE_EXPORT_HEADERS = {"Brand", "Channel", "Attribute"};
    static final String UTF8_BOM = "\uFEFF";
    static final String NOTIFICATION_ID_HEADLINE = "Notification ID";
    static final String NOTIFICATION_SHORT_NAME_HEADLINE = "Notification short name";
    static final String NOTIFICATION_LONG_NAME_HEADLINE = "Notification long name";

    private MigrationUtils()
      {}

    /**
     * Get notification templates byte array.
     *
     * @param dynamicNotificationTemplateList templates list
     *
     * @return bytearray
     */
    public static byte[] getTemplateFileStream(List<DynamicNotificationTemplate> dynamicNotificationTemplateList) {
        if (CollectionUtils.isEmpty(dynamicNotificationTemplateList)) {
            return new byte[] {};
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            OutputStreamWriter outputStreamWriter =
                new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8.newEncoder());

            CSVPrinter csvPrinter = new CSVPrinter(outputStreamWriter, CSVFormat.DEFAULT);
            csvPrinter.printRecord(UTF8_BOM + NOTIFICATION_ID_HEADLINE, NOTIFICATION_SHORT_NAME_HEADLINE,
                NOTIFICATION_LONG_NAME_HEADLINE);
            csvPrinter.printRecord(dynamicNotificationTemplateList.get(0).getNotificationId(),
                dynamicNotificationTemplateList.get(0).getNotificationShortName(),
                dynamicNotificationTemplateList.get(0).getNotificationLongName());

            Set<String> locales = dynamicNotificationTemplateList.stream().map(DynamicNotificationTemplate::getLocale)
                .collect(Collectors.toSet());

            List<String> headers = new ArrayList<>(Arrays.asList(TEMPLATE_EXPORT_HEADERS));
            headers.addAll(locales.stream().sorted().toList());
            csvPrinter.printRecord(headers);

            Map<String, Map<String, String>> csvData =
                convertTemplatesToCsvData(dynamicNotificationTemplateList, locales);

            List<String> sortKeys = csvData.keySet().stream().sorted().toList();

            Map<String, String> translationMap;
            List<String> sortLocales;
            List<String> rowData;

            for (String keyStr : sortKeys) {
                translationMap = csvData.get(keyStr);
                sortLocales = translationMap.keySet().stream().sorted().toList();
                rowData = new ArrayList<>(Arrays.asList(keyStr.split(";")));
                for (String localeStr : sortLocales) {
                    rowData.add(translationMap.get(localeStr));
                }
                csvPrinter.printRecord(rowData);
            }

            csvPrinter.flush();
            byteArrayOutputStream.flush();


            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            LOGGER.error(
                "error in migrating notification ID: " + dynamicNotificationTemplateList.get(0).getNotificationId(), e);
        }
        return new byte[]{};
    }

    /**
     * Convert templates to csv data.
     *
     * @param dynamicNotificationTemplateList templates list
     * @param locales locales
     *
     * @return map
     */
    private static Map<String, Map<String, String>> convertTemplatesToCsvData(
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList, Set<String> locales) {
        final Map<String, Map<String, String>> data = new HashMap<>();
        final Map<String, String> langData = new HashMap<>();
        locales.forEach(s -> langData.put(s, ""));

        for (DynamicNotificationTemplate dynamicNotificationTemplate : dynamicNotificationTemplateList) {
            for (Map.Entry<String, Map<String, Object>> channelEntry : dynamicNotificationTemplate.getChannelTemplates()
                .entrySet()) {
                for (Map.Entry<String, Object> attributeEntry : dynamicNotificationTemplate.getChannelTemplates()
                    .get(channelEntry.getKey()).entrySet()) {
                    String dataKey = dynamicNotificationTemplate.getBrand() + ";" + channelEntry.getKey()
                        + ";" + attributeEntry.getKey();

                    data.computeIfAbsent(dataKey, k -> new HashMap<>(langData));

                    if (attributeEntry.getValue() instanceof String) {
                        Map<String, String> translations = data.get(dataKey);
                        translations.put(dynamicNotificationTemplate.getLocale(), attributeEntry.getValue().toString());
                        data.put(dataKey, translations);
                    } else {
                        data.remove(dataKey);
                        break;
                    }
                }
            }
        }
        return data;
    }
}
