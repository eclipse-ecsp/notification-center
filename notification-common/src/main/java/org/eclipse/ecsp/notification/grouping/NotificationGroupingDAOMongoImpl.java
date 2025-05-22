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

package org.eclipse.ecsp.notification.grouping;

import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * NotificationGroupingDAOMongoImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class NotificationGroupingDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, NotificationGrouping>
    implements NotificationGroupingDAO {
    /**
     * find groups by mandatory clause.
     *
     * @param mandatory mandatory boolen flag.
     *
     * @return list of groups
     */
    public List<NotificationGrouping> findByMandatory(boolean mandatory) {

        IgniteCriteria c = new IgniteCriteria("mandatory", Operator.EQ, mandatory);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c);
        IgniteQuery query = new IgniteQuery(cg);
        return find(query);
    }

    /**
     * find groups by group name.
     *
     * @param groupsName group name.
     *
     * @return list of groups
     */
    @Override
    public List<NotificationGrouping> findByGroups(Collection<String> groupsName) {
        IgniteCriteria c = new IgniteCriteria("group", Operator.IN, groupsName);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c);
        IgniteQuery query = new IgniteQuery(cg);
        return find(query);
    }

    /**
     * find first by notification id.
     *
     * @param notificationId notification id.
     *
     * @return NotificationGrouping
     */
    @Override
    public NotificationGrouping findFirstByNotificationId(String notificationId) {
        IgniteCriteria c = new IgniteCriteria("notificationId", Operator.EQ, notificationId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c);
        IgniteQuery query = new IgniteQuery(cg);

        List<NotificationGrouping> list = find(query);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * find by notification id.
     *
     * @param notificationId notification id.
     *
     * @return list of groups
     */
    @Override
    public List<NotificationGrouping> findByNotificationId(String notificationId) {
        IgniteCriteria c = new IgniteCriteria("notificationId", Operator.EQ, notificationId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c);
        IgniteQuery query = new IgniteQuery(cg);
        return find(query);
    }

    /**
     * delete by group notification id and service.
     *
     * @param group group.
     * @param notificationId notification id.
     * @param service service.
     *
     * @return number of records deleted.
     */
    @Override
    public int deleteByGroupNotificationIdAndService(String group, String notificationId, String service) {
        IgniteCriteria groupCriterion = new IgniteCriteria("group", Operator.EQ, group);
        IgniteCriteria notificationIdCriterion = new IgniteCriteria("notificationId", Operator.EQ, notificationId);
        IgniteCriteria serviceCriterion = new IgniteCriteria("service", Operator.EQ, service);
        IgniteCriteriaGroup criteria = new IgniteCriteriaGroup(groupCriterion)
            .and(notificationIdCriterion)
            .and(serviceCriterion);
        IgniteQuery query = new IgniteQuery(criteria);
        return deleteByQuery(query);
    }
}
