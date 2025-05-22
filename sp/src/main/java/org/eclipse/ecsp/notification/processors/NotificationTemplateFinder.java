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
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NotificationTemplateFinder processor to find correct template.
 */
@Component
@Order(6)
public class NotificationTemplateFinder implements NotificationProcessor {

    private static final int MINUS_ONE = -1;

    // ObjectMapper is thread-safe and can be reused
    /**
     * Mapper.
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationTemplateFinder.class);
    @Autowired
    RichContentDynamicNotificationTemplateDAO richHtmlDao;
    @Autowired
    private NotificationTemplateDAO templateDao;
    @Autowired
    private NotificationTemplateConfigDAO notificationTemplateConfigDao;
    @Value("${locale.default.value}")
    private String defaultLocale;
    @Value("${brand.default.value:default}")
    private String defaultBrand;

    /**
     * process method.
     *
     * @param alert AlertsInfo
     */
    @Override
    public void process(AlertsInfo alert) {
        Data data = alert.getAlertsData();
        String notificationId = data.getNotificationId();

        // Search for the notification template config
        NotificationTemplateConfig notificationTemplateConfig = getNotificationTemplateConfig(notificationId);
        alert.setNotificationTemplateConfig(notificationTemplateConfig);

        // Finding the Notification Template
        Locale defLocale = Locale.forLanguageTag(defaultLocale.replace('_', '-'));
        LOGGER.debug("defaultBrand:: {} , defaultLocale:: {}", defaultBrand, Locale.forLanguageTag(defaultLocale));

        String brand = getBrand(alert, data);

        boolean isIvmEnabled = false;
        try {
            for (NotificationConfig notificationConfig : alert.getNotificationConfigs()) {

                isIvmEnabled = isIvmEnabled || (notificationConfig.getChannel(ChannelType.IVM) != null
                        && notificationConfig.getChannel(ChannelType.IVM).getEnabled());
                LOGGER.debug("isIvmEnabled {}", isIvmEnabled);
                Locale locale = (StringUtils.isEmpty(notificationConfig.getLocale())) ? defLocale
                        : Locale.forLanguageTag(notificationConfig.getLocale().replace('_', '-'));
                LOGGER.debug("Search for template: {}, locale: {}, Brand: {} for user: {}, contact: {}", notificationId,
                        locale, brand,
                        notificationConfig.getUserId(), notificationConfig.getContactId());
                if (!CollectionUtils.isEmpty(alert.getLocaleToNotificationTemplate())
                        && alert.getLocaleToNotificationTemplate().containsKey(locale.toLanguageTag())) {
                    LOGGER.debug("Template for locale {} already found in the alert. Skipping to the next config",
                            locale);
                    break;
                }

                Optional<NotificationTemplate> templateOpt =
                        getNotificationTemplate(notificationId, defLocale, brand, locale, data);

                if (templateOpt.isPresent()) {

                    NotificationTemplate notificationTemplate = findAndMergeRichTemplate(alert,
                            notificationConfig, templateOpt, defLocale,
                            brand, locale, data);

                    alert.addNotificationTemplate(locale.toLanguageTag(), notificationTemplate);
                    LOGGER.debug("Rich HTML Template alert for locale {} : {}", locale, alert.toString());
                } else {
                    LOGGER.error("Template not found for notification id {}, locale {} and brand {}", notificationId,
                            locale, brand);
                }
            }

            if (notificationTemplateConfig.getIvmConfig() != null
                    &&
                    notificationTemplateConfig.getIvmConfig().isSendAllLanguages()
                    && isIvmEnabled) {
                initAllLanguagesTemplate(alert, notificationId, brand);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error occurred during notification template finding" + e.getMessage());
        }
    }

    @NotNull
    private NotificationTemplate findAndMergeRichTemplate(AlertsInfo alert,
                                                          NotificationConfig notificationConfig,
                                                          Optional<NotificationTemplate> templateOpt,
                                                          Locale defLocale, String brand, Locale locale,
                                                          Data data) throws JsonProcessingException {
        NotificationTemplate notificationTemplate = templateOpt.get();
        String notificationId = data.getNotificationId();
        LOGGER.info("Successfully found template {}, brand: {}, locale: {} for user: {}, contact: {}",
                notificationId,
                notificationTemplate.getBrand(),
                notificationTemplate.getLocale(), notificationConfig.getUserId(),
                notificationConfig.getContactId());

        // Setting the Rich-Html in AlertsInfo
        Optional<RichContentDynamicNotificationTemplate> richHtmlTemplate =
                getRichContentTemplate(notificationId, defLocale,
                        brand,
                        locale, data);

        if (richHtmlTemplate.isPresent()) {
            String richHtml = richHtmlTemplate.get().getHtml();
            if (notificationTemplate.getChannelTemplates().getEmail() != null) {
                notificationTemplate.getChannelTemplates().getEmail().setBody(richHtml);
                notificationTemplate.getChannelTemplates().getEmail().setRichContent(true);
                addEmailRichHtmlAttachments(alert, richHtmlTemplate.get(), locale.toLanguageTag());

                mergeCustomPlaceholders(notificationTemplate, richHtmlTemplate.get());
            }
            LOGGER.info("Rich HTML Template saved and replaced old email template");
        }
        return notificationTemplate;
    }

    private void mergeCustomPlaceholders(NotificationTemplate notificationTemplate,
                                         RichContentDynamicNotificationTemplate richHtmlTemplate) {
        if (!CollectionUtils.isEmpty(richHtmlTemplate.getCustomPlaceholders())) {
            if (notificationTemplate.getCustomPlaceholders() == null) {
                notificationTemplate.setCustomPlaceholders(richHtmlTemplate.getCustomPlaceholders());
            } else {
                notificationTemplate.getCustomPlaceholders().addAll(richHtmlTemplate.getCustomPlaceholders());
            }
        }
    }

    private void initAllLanguagesTemplate(AlertsInfo alert, String notificationId, String brand)
            throws JsonProcessingException {
        LOGGER.debug("start  initAllLanguagesTemplate");
        final Map<String, NotificationTemplate> notificationTemplatePerLocale = new HashMap<>();

        List<NotificationTemplate> notificationTemplates = templateDao.findByNotificationIdAndBrands(notificationId,
                Arrays.asList(brand, defaultBrand));
        Set<Locale> locales =
                notificationTemplates.stream().map(NotificationTemplate::getLocale).collect(Collectors.toSet());

        String dataStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(alert.getAlertsData());
        locales.forEach(locale -> {
            NotificationTemplate selectedTemplate = getSelectedTemplate(dataStr, notificationTemplates, brand, locale);
            if (selectedTemplate != null) {
                notificationTemplatePerLocale.put(selectedTemplate.getLocale().toLanguageTag(), selectedTemplate);
            }
        });

        // leave only IVM channel data
        final Set<NotificationTemplate> finalNotificationTemplates = new HashSet<>();
        notificationTemplatePerLocale.values().forEach(notificationTemplate -> {
            if (notificationTemplate.getIvmTemplate() != null) {
                ChannelTemplates channelTemplates = new ChannelTemplates();
                channelTemplates.setIvm(notificationTemplate.getIvmTemplate());
                notificationTemplate.setChannelTemplates(channelTemplates);
                finalNotificationTemplates.add(notificationTemplate);
            }
        });

        if (!CollectionUtils.isEmpty(finalNotificationTemplates)) {
            LOGGER.debug("setAllLanguageTemplates {}", finalNotificationTemplates);
            alert.setAllLanguageTemplates(finalNotificationTemplates);
        }
    }

    @NotNull
    private Optional<NotificationTemplate> getNotificationTemplate(String notificationId, Locale defLocale,
                                                                   String brand, Locale locale,
                                                                   Data data) throws JsonProcessingException {
        List<NotificationTemplate> notificationTemplates =
                templateDao.findByNotificationIdLocalesAndBrands(notificationId,
                        Arrays.asList(locale, defLocale), Arrays.asList(brand, defaultBrand));

        String dataStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        return Optional.ofNullable(getSelectedTemplate(dataStr, notificationTemplates, brand, locale));
    }

    @NotNull
    private Optional<RichContentDynamicNotificationTemplate> getRichContentTemplate(String notificationId,
                                                                                    Locale defLocale, String brand,
                                                                                    Locale locale, Data data)
            throws JsonProcessingException {
        List<RichContentDynamicNotificationTemplate> richHtmlTemplates =
                richHtmlDao.findByNotificationIdLocalesAndBrands(notificationId,
                        Arrays.asList(locale, defLocale), Arrays.asList(brand, defaultBrand));
        RichContentDynamicNotificationTemplate selectedTemplate = null;
        int maxRank = MINUS_ONE;
        int currentRank;

        String dataStr = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        for (RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate : richHtmlTemplates) {
            currentRank = Utils.getRank(richContentDynamicNotificationTemplate, brand, locale, dataStr);
            if (currentRank > maxRank) {
                maxRank = currentRank;
                selectedTemplate = richContentDynamicNotificationTemplate;
            }
        }
        LOGGER.debug("Selected rich template: {}", selectedTemplate);
        return Optional.ofNullable(selectedTemplate);
    }

    private NotificationTemplate getSelectedTemplate(String dataStr, List<NotificationTemplate> notificationTemplates,
                                                     String brand,
                                                     Locale locale) {
        NotificationTemplate selectedTemplate = null;
        int maxRank = MINUS_ONE;
        int currentRank;

        for (NotificationTemplate notificationTemplate : notificationTemplates) {
            currentRank = Utils.getRank(notificationTemplate, brand, locale, dataStr);
            if (currentRank > maxRank) {
                maxRank = currentRank;
                selectedTemplate = notificationTemplate;
            }
        }
        LOGGER.debug("Selected template: {}", selectedTemplate);
        return selectedTemplate;
    }

    private String getBrand(AlertsInfo alert, Data data) {
        String brand = null;
        // if user notification check to see if brand was passed in payload
        if (alert.getAlertsData().getAlertDataProperties().containsKey(NotificationConstants.USER_NOTIFICATION)
                &&
                ((Boolean) (alert.getAlertsData().getAlertDataProperties()
                        .get(NotificationConstants.USER_NOTIFICATION)))
                &&
                alert.getAlertsData().getAlertDataProperties().containsKey(NotificationConstants.BRAND)) {
            brand = (String) alert.getAlertsData().getAlertDataProperties().get(NotificationConstants.BRAND);
            LOGGER.debug("user notification with brand in payload. brand:: {}", brand);
        }

        if (StringUtils.isEmpty(brand)) {
            brand = (data.getVehicleProfile() != null && !StringUtils.isEmpty(data.getVehicleProfile().getMake()))
                    ? data.getVehicleProfile().getMake()
                    : defaultBrand;
        }

        if (brand != null) {
            alert.getAlertsData().getAlertDataProperties().put(NotificationConstants.BRAND, brand.toLowerCase());
        }
        return brand != null ? brand.toLowerCase() : null;
    }

    @NotNull
    private NotificationTemplateConfig getNotificationTemplateConfig(String notificationId) {
        NotificationTemplateConfig config = notificationTemplateConfigDao.findById(notificationId);
        if (config == null) {
            String errMsg = String.format("Template configuration not found for notification id %s", notificationId);
            LOGGER.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        return config;
    }

    private void addEmailRichHtmlAttachments(AlertsInfo alert, RichContentDynamicNotificationTemplate richHtmlTemplate,
                                             String locale) {
        if (!CollectionUtils.isEmpty(richHtmlTemplate.getAttachments())) {
            EventData eventData = alert.getIgniteEvent().getEventData();
            if (!(eventData instanceof GenericEventData)) {
                return;
            }

            GenericEventData genericEventData = (GenericEventData) eventData;
            genericEventData.set(locale, richHtmlTemplate.getAttachments());
            LOGGER.debug("added attachments to alert event-data map: {}:{}", locale,
                    richHtmlTemplate.getAttachments().stream().map(EmailAttachment::getFileName)
                            .collect(Collectors.joining(", ")));
        }
    }

    /**
     * Set Template DAO.
     *
     * @param templateDao the given templateDao
     */
    public void setTemplateDao(NotificationTemplateDAO templateDao) {
        this.templateDao = templateDao;
    }


    /**
     * Set Default Locale.
     *
     * @param locale the given locale
     */
    @Profile("test")
    public void setDefaultLocale(String locale) {
        this.defaultLocale = locale;
    }

    /**
     * Set Default Brand.
     *
     * @param defaultBrand the given defaultBrand
     */
    @Profile("test")
    void setDefaultBrand(String defaultBrand) {
        this.defaultBrand = defaultBrand != null ? defaultBrand.toLowerCase() : null;
    }

    /**
     * Set RichHtml DAO.
     *
     * @param richContentDynamicNotificationTemplateDao the given richContentDynamicNotificationTemplateDao
     */
    public void setRichHtmlDao(RichContentDynamicNotificationTemplateDAO richContentDynamicNotificationTemplateDao) {
        this.richHtmlDao = richContentDynamicNotificationTemplateDao;
    }

    /**
     * Set NotificationTemplateConfig DAO.
     *
     * @param notificationTemplateConfigDao the given notificationTemplateConfigDao
     */
    public void setNotificationTemplateConfigDao(NotificationTemplateConfigDAO notificationTemplateConfigDao) {
        this.notificationTemplateConfigDao = notificationTemplateConfigDao;
    }
}
