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
import org.eclipse.ecsp.notification.entities.NotificationTemplateImportedData;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NotificationTemplateImportedDataDAOMongoImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class NotificationTemplateImportedDataDAOMongoImpl
    extends IgniteBaseDAOMongoImpl<String, NotificationTemplateImportedData>
    implements NotificationTemplateImportedDataDAO {

    /**
     * findByNotificationIds method.
     *
     * @param notificationIds notificationIds
     * @return List of NotificationTemplateImportedData
     */
    @Override
    public List<NotificationTemplateImportedData> findByNotificationIds(List<String> notificationIds) {
        IgniteCriteria idCriteria =
            new IgniteCriteria(NotificationDaoConstants.NOTIFICATION_ID_FIELD, Operator.IN, notificationIds);
        IgniteQuery igniteQuery = new IgniteQuery(new IgniteCriteriaGroup(idCriteria));
        return find(igniteQuery);
    }

}
