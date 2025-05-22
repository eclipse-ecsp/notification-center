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

import com.amazonaws.util.CollectionUtils;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DynamicNotificationTemplateDAOimpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class DynamicNotificationTemplateDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, DynamicNotificationTemplate>
    implements DynamicNotificationTemplateDAO {

    /**
     * Find by notification ids.
     *
     * @param notificationIds notification ids
     * @return list of DynamicNotificationTemplate
     */
    @Override
    public List<DynamicNotificationTemplate> findByNotificationIds(List<String> notificationIds) {
        IgniteCriteria idCriteria = new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.IN,
            notificationIds);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria));

        return find(igniteQuery);
    }

    /**
     * Find by notification id.
     *
     * @param notificationId notification id
     * @return list of DynamicNotificationTemplate
     */
    @Override
    public boolean isNotificationIdExist(String notificationId) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ, notificationId);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria));

        List<DynamicNotificationTemplate> notificationEntities = find(igniteQuery);
        return !CollectionUtils.isNullOrEmpty(notificationEntities);
    }

    /**
     * findByNotificationIdsBrandsLocalesNoAddAttrs.
     *
     * @param notificationId notificationId
     * @param brands brands
     * @param locales locales
     * @return list of DynamicNotificationTemplate
     */
    @Override
    public List<DynamicNotificationTemplate> findByNotificationIdsBrandsLocalesNoAddAttrs(String notificationId,
                                                                                          Set<String> brands,
                                                                                          Set<Locale> locales) {
        IgniteCriteria idCriteria = new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ,
            notificationId);
        IgniteCriteria brandCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN,
            brands);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.IN,
            locales.stream().map(Locale::toString).collect(Collectors.toSet()));
        IgniteCriteria addAttrCriteria =
            new IgniteCriteria(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD, Operator.EQ,
                null);
        IgniteQuery igniteQuery = new IgniteQuery(
            new IgniteCriteriaGroup(idCriteria).and(brandCriteria).and(localeCriteria).and(addAttrCriteria));

        return find(igniteQuery);
    }

    /**
     * findByNotificationIdsBrandsLocalesAddAttrs.
     *
     * @param notificationId notificationId
     * @param brands brands
     * @param locales locales
     * @param propertyName propertyName
     * @param propertyValues propertyValues
     * @return list of DynamicNotificationTemplate
     */
    @Override
    public List<DynamicNotificationTemplate> findByNotificationIdsBrandsLocalesAddAttrs(String notificationId,
                                                                                        Set<String> brands,
                                                                                        Set<Locale> locales,
                                                                                        String propertyName,
                                                                                        Set<String> propertyValues) {
        IgniteCriteria idCriteria = new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.EQ,
            notificationId);
        IgniteCriteria brandCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN,
            brands);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.IN,
            locales.stream().map(Locale::toString).collect(Collectors.toSet()));
        IgniteCriteria addAttrNameCriteria =
            new IgniteCriteria(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD_NAME, Operator.EQ,
                propertyName);
        IgniteCriteria addAttrValsCriteria =
            new IgniteCriteria(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD_VALUES, Operator.IN,
                propertyValues);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(brandCriteria)
            .and(localeCriteria).and(addAttrNameCriteria).and(addAttrValsCriteria));

        return find(igniteQuery);
    }
}