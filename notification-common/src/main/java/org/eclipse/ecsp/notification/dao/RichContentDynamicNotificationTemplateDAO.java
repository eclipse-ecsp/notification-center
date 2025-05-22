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
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * RichContentDynamicNotificationTemplateDAO interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface RichContentDynamicNotificationTemplateDAO
    extends IgniteBaseDAO<String, RichContentDynamicNotificationTemplate> {
    /**
     * Find by notification ids.
     *
     * @param notificationIds the notification ids
     * @return the list
     */
    List<RichContentDynamicNotificationTemplate> findByNotificationIds(List<String> notificationIds);

    /**
     * Find by notification id locale and brand.
     *
     * @param notificationId the notification id
     * @param locale         the locale
     * @param brand          the brand
     * @return the optional
     */
    Optional<RichContentDynamicNotificationTemplate> findByNotificationIdLocaleAndBrand(String notificationId,
                                                                                        Locale locale, String brand);

    /**
     * Find by notification id and brand.
     *
     * @param notificationId the notification id
     * @param locales       the locales
     * @param brands       the brands
     * @return the list
     */
    List<RichContentDynamicNotificationTemplate> findByNotificationIdLocalesAndBrands(String notificationId,
                                                                                      List<Locale> locales,
                                                                                      List<String> brands);

    /**
     * Find by notification id locales and brands.
     *
     * @param notificationId the notification id
     * @param brands        the brands
     * @param locales      the locales
     * @return the list
     */
    List<RichContentDynamicNotificationTemplate> findByNotificationIdsBrandsLocalesNoAddAttrs(String notificationId,
                                                                                              Set<String> brands,
                                                                                              Set<Locale> locales);

    /**
     * Find by notification id locales and brands.
     *
     * @param notificationId the notification id
     * @param brands the brands
     * @param locales the locales
     * @param propertyName the property name
     * @param propertyValues the property values
     * @return the list
     */
    List<RichContentDynamicNotificationTemplate> findByNotificationIdsBrandsLocalesAddAttrs(String notificationId,
                                                                                            Set<String> brands,
                                                                                            Set<Locale> locales,
                                                                                            String propertyName,
                                                                                            Set<String> propertyValues);

}
