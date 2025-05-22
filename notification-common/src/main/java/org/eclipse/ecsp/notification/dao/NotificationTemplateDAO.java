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

package org.eclipse.ecsp.notification.dao;

import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * NotificationTemplateDAO interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface NotificationTemplateDAO extends IgniteBaseDAO<String, NotificationTemplate> {
    /**
     * Find by notification id and locale.
     *
     * @param notificationId the notification id
     * @param locale         the locale
     * @return the optional
     */
    Optional<NotificationTemplate> findByNotificationIdAndLocale(String notificationId, Locale locale);

    /**
     * Find by notification id locale and brand.
     *
     * @param notificationId the notification id
     * @param locale         the locale
     * @param brand          the brand
     * @return the optional
     */
    Optional<NotificationTemplate> findByNotificationIdLocaleAndBrand(String notificationId, Locale locale,
                                                                      String brand);

    /**
     * Find by notification id and brand.
     *
     * @param notificationId the notification id
     * @param brand         the brand
     * @return the list
     */
    List<NotificationTemplate> findByNotificationIdAndBrand(String notificationId, String brand);

    /**
     * Find by notification id locales and brands.
     *
     * @param notificationId the notification id
     * @param locales        the locales
     * @param brands         the brands
     * @return the list
     */
    List<NotificationTemplate> findByNotificationIdLocalesAndBrands(String notificationId, List<Locale> locales,
                                                                    List<String> brands);

    /**
     * Find by notification id and brands.
     *
     * @param notificationId the notification id
     * @param brands the brands
     * @return the list
     */
    List<NotificationTemplate> findByNotificationIdAndBrands(String notificationId, List<String> brands);
}