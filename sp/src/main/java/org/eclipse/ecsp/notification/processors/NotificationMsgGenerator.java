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

import lombok.val;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.BaseTemplate;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.function.Consumer;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.CUSTOM_PLACEHOLDERS;
import static org.eclipse.ecsp.notification.adaptor.NotificationUtils.getDecoratedMsg;

/**
 * NotificationMsgGenerator class.
 */
@Component
@Order(9)
public class NotificationMsgGenerator implements NotificationProcessor {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationMsgGenerator.class);

    /**
     * process method.
     *
     * @param info AlertsInfo
     */
    @Override
    public void process(AlertsInfo info) {
        val channelTypes = info.resolveAvailableChannelTypes();
        val localeToTemplates = info.getLocaleToNotificationTemplate();

        localeToTemplates.forEach((userLocale, template) -> {
            LOGGER.debug("Populating template for {} with info {}", template, info);
            channelTypes.forEach(ct -> populatePlaceholders(ct, template, info, userLocale));
        });

        if (!CollectionUtils.isEmpty(info.getAllLanguageTemplates())) {
            info.getAllLanguageTemplates()
                    .forEach(template -> populatePlaceholders(ChannelType.IVM, template, info,
                            template.getLocale().toLanguageTag()));
        }
    }

    /**
     * populatePlaceholders method.
     *
     * @param channelType          ChannelType
     * @param notificationTemplate NotificationTemplate
     * @param info                 AlertsInfo
     * @param userLocale           String
     */
    private void populatePlaceholders(ChannelType channelType, NotificationTemplate notificationTemplate,
                                      AlertsInfo info,
                                      String userLocale) {
        try {
            if (!CollectionUtils.isEmpty(info.getLocaleToPlaceholders())
                    && !CollectionUtils.isEmpty(notificationTemplate.getCustomPlaceholders())) {
                info.getAlertsData().set(CUSTOM_PLACEHOLDERS,
                        info.getLocaleToPlaceholders().get(notificationTemplate.getLocale().toString()));
            }
            BaseTemplate channelTemplate = notificationTemplate.getChannelTemplate(channelType);
            if (null == channelTemplate) {
                LOGGER.warn("Failed to Populate templates for channel {} ", channelType);
                info.getNotificationConfigs().stream()
                        .filter(conf -> conf.getLocale().equalsIgnoreCase(userLocale))
                        .map(conf -> (Channel) conf.getChannel(channelType))
                        .filter(Objects::nonNull)
                        .forEach(channel -> channel.setEnabled(false));
            } else {
                channelTemplate.getContentFieldsGetter().forEach((contentField, getter) -> {
                    String decoratedMsg = getDecoratedMsg(info, getter.get());
                    Consumer<String> fieldSetter = channelTemplate.getContentFieldsSetter().get(contentField);
                    fieldSetter.accept(decoratedMsg);
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error during populate placeholder for notificationTemplate {} exception {}",
                    notificationTemplate, e);
        }
    }
}
