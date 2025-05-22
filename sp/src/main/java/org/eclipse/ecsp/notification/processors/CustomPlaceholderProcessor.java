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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.dao.NotificationPlaceholderDao;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholder;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CustomPlaceholderProcessor to replace custom placeholders in a template.
 */
@Component
@Order(7)
public class CustomPlaceholderProcessor implements NotificationProcessor {

    /**
     * ObjectMapper.
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int MINUS_ONE = -1;

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(CustomPlaceholderProcessor.class);
    private final NotificationPlaceholderDao notificationPlaceholderDao;
    @Value("${locale.default.value}")
    private String defaultLocale;
    @Value("${brand.default.value:default}")
    private String defaultBrand;

    /**
     * Constructor.
     *
     * @param notificationPlaceholderDao NotificationPlaceholderDao
     */
    public CustomPlaceholderProcessor(NotificationPlaceholderDao notificationPlaceholderDao) {
        this.notificationPlaceholderDao = notificationPlaceholderDao;
    }

    /**
     * Process method.
     *
     * @param alert AlertsInfo
     */
    @Override
    public void process(AlertsInfo alert) {
        LOGGER.debug("start process");

        Map<String, Map<String, String>> localeToPlaceholders = new HashMap<>();
        String brand = (String) alert.getAlertsData().getAlertDataProperties().get(NotificationConstants.BRAND);
        if (brand != null) {
            brand = brand.toLowerCase();
        }
        try {
            for (NotificationTemplate notificationTemplate : alert.getLocaleToNotificationTemplate().values()) {
                if (!CollectionUtils.isEmpty(notificationTemplate.getCustomPlaceholders())) {
                    localeToPlaceholders.put(notificationTemplate.getLocale().toString(),
                            getPlaceholders(notificationTemplate,
                                    Locale.forLanguageTag(defaultLocale.replace("_", "-")),
                                    brand, alert));
                }
            }

            // all locales for ivm
            if (!CollectionUtils.isEmpty(alert.getAllLanguageTemplates())) {
                for (NotificationTemplate notificationTemplate : alert.getAllLanguageTemplates()) {
                    if (!CollectionUtils.isEmpty(notificationTemplate.getCustomPlaceholders())
                            && !localeToPlaceholders.containsKey(notificationTemplate.getLocale().toString())) {
                        localeToPlaceholders.put(notificationTemplate.getLocale().toString(),
                                getPlaceholders(notificationTemplate,
                                        Locale.forLanguageTag(defaultLocale.replace("_", "-")), brand,
                                        alert));
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Error occurred during custom placeholder processing " + ex.getMessage());
        }

        LOGGER.debug("localeToPlaceholders = {}", localeToPlaceholders);
        alert.setLocaleToPlaceholders(localeToPlaceholders);
    }

    /**
     * Get placeholders.
     *
     * @param notificationTemplate NotificationTemplate
     * @param defLocale            Locale
     * @param brand                String
     * @param alertsInfo           AlertsInfo
     * @return Map
     * @throws JsonProcessingException JsonProcessingException
     */
    private Map<String, String> getPlaceholders(NotificationTemplate notificationTemplate, Locale defLocale,
                                                String brand,
                                                AlertsInfo alertsInfo) throws JsonProcessingException {
        List<NotificationPlaceholder> notificationPlaceholders;

        String dataStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(alertsInfo.getAlertsData());

        NotificationPlaceholder notificationPlaceholder;
        Map<String, String> placeholderMap = new HashMap<>();
        notificationPlaceholders =
                notificationPlaceholderDao.findByKeysBrandsAndLocales(notificationTemplate.getCustomPlaceholders(),
                        Arrays.asList(notificationTemplate.getLocale(), defLocale), Arrays.asList(brand, defaultBrand));
        LOGGER.debug("found {} placeholders in db: locale = {}, brand = {}, placeholders = {}",
                notificationPlaceholders.size(),
                notificationTemplate.getLocale(), brand, notificationTemplate.getCustomPlaceholders());
        for (String key : notificationTemplate.getCustomPlaceholders()) {
            notificationPlaceholder = getSelectedPlaceholder(dataStr,
                    notificationPlaceholders.stream().filter(p -> p.getKey().equals(key)).toList(),
                    brand,
                    notificationTemplate.getLocale());
            if (notificationPlaceholder != null) {
                LOGGER.debug("adding custom Placeholder: locale = {}, key = {}, value = {}",
                        notificationTemplate.getLocale(),
                        notificationPlaceholder.getKey(), notificationPlaceholder.getValue());
                placeholderMap.put(notificationPlaceholder.getKey(), notificationPlaceholder.getValue());
            }
        }
        return placeholderMap;
    }

    /**
     * Get selected placeholder.
     *
     * @param dataStr      String
     * @param placeholders List
     * @param brand        String
     * @param locale       Locale
     * @return NotificationPlaceholder
     */
    private NotificationPlaceholder getSelectedPlaceholder(String dataStr, List<NotificationPlaceholder> placeholders,
                                                           String brand,
                                                           Locale locale) {
        NotificationPlaceholder selectedPlaceholder = null;
        int maxRank = MINUS_ONE;
        int currentRank;

        for (NotificationPlaceholder notificationPlaceholder : placeholders) {
            currentRank = Utils.getRank(notificationPlaceholder, brand, locale, dataStr);
            if (currentRank > maxRank) {
                maxRank = currentRank;
                selectedPlaceholder = notificationPlaceholder;
            }
        }
        LOGGER.debug("Selected Placeholder: {}", selectedPlaceholder);
        return selectedPlaceholder;
    }
}
