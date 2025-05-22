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

import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * RichContentDynamicNotificationTemplateDAOMongoImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class RichContentDynamicNotificationTemplateDAOMongoImpl
    extends IgniteBaseDAOMongoImpl<String, RichContentDynamicNotificationTemplate>
    implements RichContentDynamicNotificationTemplateDAO {
    /**
     * findByNotificationIds method.
     *
     * @param notificationIds notificationIds
     * @return List of RichContentDynamicNotificationTemplate
     */
    @Override
    public List<RichContentDynamicNotificationTemplate> findByNotificationIds(List<String> notificationIds) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.IN, notificationIds);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria));
        return find(igniteQuery);
    }

    /**
     * Find by notification id locale and brand.
     *
     * @param notificationId the notification id
     * @param locale         the locale
     * @param brand          the brand
     * @return the optional
     */
    @Override
    public Optional<RichContentDynamicNotificationTemplate> findByNotificationIdLocaleAndBrand(String notificationId,
                                                                                               Locale locale,
                                                                                               String brand) {
        IgniteCriteria idCriteria = new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ,
            notificationId);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.EQI, brand);

        //implementing compatibility with both versions of locale representation: high - , low _
        String localeStr = locale.toString();
        IgniteCriteria localeCriteriaLow =
            new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.EQI, localeStr);
        IgniteCriteria localeCriteriaHigh =
            new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.EQI, localeStr.replace("_", "-"));
        IgniteCriteriaGroup localeCriteria = new IgniteCriteriaGroup(localeCriteriaHigh).or(localeCriteriaLow);

        IgniteQuery igniteQuery =
            new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(flavorCriteria)).and(localeCriteria);

        List<RichContentDynamicNotificationTemplate> templates = find(igniteQuery);

        if (null != templates && !templates.isEmpty()) {
            return Optional.ofNullable(templates.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Find by notification id and brand.
     *
     * @param notificationId the notification id
     * @param locales       the locales
     * @param brands       the brands
     * @return the list
     */
    @Override
    public List<RichContentDynamicNotificationTemplate> findByNotificationIdLocalesAndBrands(String notificationId,
                                                                                             List<Locale> locales,
                                                                                             List<String> brands) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ, notificationId);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN, brands);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.IN,
            locales.stream().map(Locale::toString).collect(toList()));

        IgniteQuery igniteQuery =
            new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(flavorCriteria).and(localeCriteria));

        return find(igniteQuery);
    }

    /**
     * Find by notification id locales and brands.
     *
     * @param notificationId the notification id
     * @param brands        the brands
     * @param locales      the locales
     * @return the list
     */
    @Override
    public List<RichContentDynamicNotificationTemplate> findByNotificationIdsBrandsLocalesNoAddAttrs(
        String notificationId,
        Set<String> brands, Set<Locale> locales) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ, notificationId);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN, brands);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.IN,
            locales.stream().map(Locale::toString).collect(Collectors.toSet()));
        IgniteCriteria addAttrCriteria =
            new IgniteCriteria(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD, Operator.EQ,
                null);
        IgniteQuery igniteQuery = new IgniteQuery(
            new IgniteCriteriaGroup(idCriteria).and(flavorCriteria).and(localeCriteria).and(addAttrCriteria));

        return find(igniteQuery);
    }

    /**
     * Find by notification id locales and brands.
     *
     * @param notificationId the notification id
     * @param brands the brands
     * @param locales the locales
     * @return the list
     */
    @Override
    public List<RichContentDynamicNotificationTemplate> findByNotificationIdsBrandsLocalesAddAttrs(
        String notificationId,
        Set<String> brands, Set<Locale> locales, String propertyName, Set<String> propertyValues) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ, notificationId);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN, brands);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.IN,
            locales.stream().map(Locale::toString).collect(Collectors.toSet()));
        IgniteCriteria addAttrNameCriteria =
            new IgniteCriteria(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD_NAME, Operator.EQ,
                propertyName);
        IgniteCriteria addAttrValsCriteria =
            new IgniteCriteria(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD_VALUES, Operator.IN,
                propertyValues);
        IgniteQuery igniteQuery = new IgniteQuery(
            new IgniteCriteriaGroup(idCriteria).and(flavorCriteria).and(localeCriteria).and(addAttrNameCriteria)
                .and(addAttrValsCriteria));

        return find(igniteQuery);
    }

}
