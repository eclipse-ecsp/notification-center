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

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NotificationBufferDaoImpl class.
 */
@Repository
public class NotificationBufferDaoImpl extends IgniteBaseDAOMongoImpl<String, NotificationBuffer>
    implements NotificationBufferDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationBufferDaoImpl.class);

    /**
     * Find by scheduler id.
     *
     * @param schedulerId scheduler id
     * @return NotificationBuffer
     */
    @Override
    public NotificationBuffer findBySchedulerId(String schedulerId) {
        IgniteCriteria schedulerIdCriteria =
            new IgniteCriteria(NotificationDaoConstants.SCHEDULER_ID_FIELD, Operator.EQ, schedulerId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(schedulerIdCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        List<NotificationBuffer> resultSet = find(query);
        NotificationBuffer result = null;
        if (resultSet != null && !resultSet.isEmpty()) {
            result = resultSet.get(0);
        }
        return result;

    }

    /**
     * Find by user id and vehicle id and channel type and group.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @param channelType channel type
     * @param group group
     * @return NotificationBuffer
     */
    @Override
    public NotificationBuffer findByUserIdAndVehicleIdAndChannelTypeAndGroup(String userId, String vehicleId,
                                                                             ChannelType channelType, String group) {
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationDaoConstants.USERID_FIELD, Operator.EQ, userId);
        IgniteCriteria vehicleIdCriteria =
            new IgniteCriteria(NotificationDaoConstants.VEHICLEID_FIELD, Operator.EQ, vehicleId);
        IgniteCriteria channelTypeCriteria =
            new IgniteCriteria(NotificationDaoConstants.CHANNELTYPE_FIELD, Operator.EQ, channelType);
        IgniteCriteria groupCriteria = new IgniteCriteria(NotificationDaoConstants.GROUP_FIELD, Operator.EQ, group);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(userIdCriteria).and(vehicleIdCriteria).and(channelTypeCriteria).and(groupCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        List<NotificationBuffer> resultSet = find(query);
        LOGGER.debug("UserId {}", userId);
        NotificationBuffer result = null;
        if (resultSet != null && !resultSet.isEmpty()) {
            result = resultSet.get(0);
        }
        return result;
    }

    /**
     * Find by user id and vehicle id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @return list of NotificationBuffer
     */
    @Override
    public List<NotificationBuffer> findByUserIdAndVehicleId(String userId, String vehicleId) {
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationDaoConstants.USERID_FIELD, Operator.EQ, userId);
        IgniteCriteria vehicleIdCriteria =
            new IgniteCriteria(NotificationDaoConstants.VEHICLEID_FIELD, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(userIdCriteria).and(vehicleIdCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        return find(query);
    }

    /**
     * Delete by user id and vehicle id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     */
    @Override
    public void deleteByUserIdAndVehicleId(String userId, String vehicleId) {
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationDaoConstants.USERID_FIELD, Operator.EQ, userId);
        IgniteCriteria vehicleIdCriteria =
            new IgniteCriteria(NotificationDaoConstants.VEHICLEID_FIELD, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(userIdCriteria).and(vehicleIdCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        List<NotificationBuffer> resultSet = find(query);
        resultSet.forEach(record -> {
            deleteById(record.getId());
        });
    }

    /**
     * Added this method to get notification information on a contactId level.
     */
    @Override
    public NotificationBuffer findByUserIDVehicleIDChannelTypeGroupContactId(String userId, String vehicleId,
                                                                             ChannelType channelType, String group,
                                                                             String contactId) {
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationDaoConstants.USERID_FIELD, Operator.EQ, userId);
        IgniteCriteria vehicleIdCriteria =
            new IgniteCriteria(NotificationDaoConstants.VEHICLEID_FIELD, Operator.EQ, vehicleId);
        IgniteCriteria channelTypeCriteria =
            new IgniteCriteria(NotificationDaoConstants.CHANNELTYPE_FIELD, Operator.EQ, channelType);
        IgniteCriteria groupCriteria = new IgniteCriteria(NotificationDaoConstants.GROUP_FIELD, Operator.EQ, group);
        IgniteCriteria contactIdCriteria =
            new IgniteCriteria(NotificationDaoConstants.CONTACT_ID_FIELD, Operator.EQ, contactId);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(userIdCriteria).and(vehicleIdCriteria).and(channelTypeCriteria)
                .and(groupCriteria).and(contactIdCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        List<NotificationBuffer> resultSet = find(query);
        LOGGER.debug("UserId {}, ContactId {}", userId, contactId);
        NotificationBuffer result = null;
        if (resultSet != null && !resultSet.isEmpty()) {
            result = resultSet.get(0);
        }
        return result;
    }
}
