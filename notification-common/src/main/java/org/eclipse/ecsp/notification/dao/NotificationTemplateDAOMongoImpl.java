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
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * NotificationTemplateDAOMongoImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class NotificationTemplateDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, NotificationTemplate>
    implements NotificationTemplateDAO {

    /**
     * Find by notification id and locale.
     *
     * @param notificationId the notification id
     * @param locale         the locale
     * @return the optional
     */
    @Override
    public Optional<NotificationTemplate> findByNotificationIdAndLocale(String notificationId, Locale locale) {
        IgniteCriteria idCriteria = new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ,
            notificationId);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.EQ,
            locale);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(localeCriteria));

        List<NotificationTemplate> templates = find(igniteQuery);

        if (null != templates && !templates.isEmpty()) {
            return Optional.ofNullable(templates.get(0));
        } else {
            return Optional.empty();
        }
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
    public Optional<NotificationTemplate> findByNotificationIdLocaleAndBrand(String notificationId, Locale locale,
                                                                             String brand) {
        IgniteCriteria idCriteria = new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ,
            notificationId);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.EQ,
            locale);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.EQI, brand);
        IgniteQuery igniteQuery =
            new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(localeCriteria).and(flavorCriteria));

        List<NotificationTemplate> templates = find(igniteQuery);

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
     * @param brand         the brand
     * @return the list
     */
    @Override
    public List<NotificationTemplate> findByNotificationIdAndBrand(String notificationId, String brand) {
        IgniteCriteria idCriteria = new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ,
            notificationId);
        IgniteCriteria brandCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.EQI,
            brand);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(brandCriteria));

        return find(igniteQuery);
    }

    /**
     * Find by notification id locales and brands.
     *
     * @param notificationId the notification id
     * @param locales        the locales
     * @param brands         the brands
     * @return the list
     */
    @Override
    public List<NotificationTemplate> findByNotificationIdLocalesAndBrands(String notificationId, List<Locale> locales,
                                                                           List<String> brands) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ, notificationId);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.IN, locales);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN, brands);
        IgniteQuery igniteQuery =
            new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(localeCriteria).and(flavorCriteria));

        return find(igniteQuery);
    }

    /**
     * Find by notification id and brands.
     *
     * @param notificationId the notification id
     * @param brands         the brands
     * @return the list
     */
    @Override
    public List<NotificationTemplate> findByNotificationIdAndBrands(String notificationId, List<String> brands) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ, notificationId);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN, brands);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(flavorCriteria));

        return find(igniteQuery);
    }


}