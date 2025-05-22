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
import org.eclipse.ecsp.notification.entities.NotificationPlaceholder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * NotificationPlaceholderDaoMongoImpl class.
 */
@Repository
public class NotificationPlaceholderDaoMongoImpl extends IgniteBaseDAOMongoImpl<String, NotificationPlaceholder>
    implements NotificationPlaceholderDao {
    /**
     * Delete by keys.
     *
     * @param placeholdersKeys placeholders keys
     */
    @Override
    public void deleteByKeys(Set<String> placeholdersKeys) {
        IgniteCriteria keyCriteria =
            new IgniteCriteria(NotificationDaoConstants.PLACEHOLDER_KEY_FIELD, Operator.IN, placeholdersKeys);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(keyCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        deleteByQuery(query);
    }

    /**
     * Find by keys.
     *
     * @param keys keys
     * @return list of NotificationPlaceholder
     */
    @Override
    public List<NotificationPlaceholder> findByKeys(Set<String> keys) {
        IgniteCriteria keyCriteria =
            new IgniteCriteria(NotificationDaoConstants.PLACEHOLDER_KEY_FIELD, Operator.IN, keys);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(keyCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        return find(query);
    }

    /**
     * Find by keys, brands and locales.
     *
     * @param keys keys
     * @param locales locales
     * @param brands brands
     * @return list of NotificationPlaceholder
     */
    @Override
    public List<NotificationPlaceholder> findByKeysBrandsAndLocales(Set<String> keys, List<Locale> locales,
                                                                    List<String> brands) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.PLACEHOLDER_KEY_FIELD, Operator.IN, keys);
        IgniteCriteria localeCriteria = new IgniteCriteria(NotificationDaoConstants.LOCALE_FIELD, Operator.IN, locales);
        IgniteCriteria flavorCriteria = new IgniteCriteria(NotificationDaoConstants.BRAND_FIELD, Operator.IN, brands);
        IgniteQuery igniteQuery =
            new IgniteQuery(new IgniteCriteriaGroup(idCriteria).and(localeCriteria).and(flavorCriteria));

        return find(igniteQuery);
    }
}
