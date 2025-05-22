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

import org.eclipse.ecsp.domain.notification.IVMRequest;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * IVMRequestDAOImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class IVMRequestDAOImpl extends IgniteBaseDAOMongoImpl<String, IVMRequest> implements IVMRequestDAO {

    /**
     * Find IVMRequest by vehicleId and messageId.
     *
     * @param vehicleId vehicleId
     * @param messageId messageId
     * @return IVMRequest
     */
    @Override
    public Optional<IVMRequest> findByVehicleIdMessageId(String vehicleId, String messageId) {
        IgniteCriteria criteria1 = new IgniteCriteria(NotificationDaoConstants.MESSAGEID_FIELD, Operator.EQ, messageId);
        IgniteCriteria criteria2 = new IgniteCriteria(NotificationDaoConstants.VEHICLEID_FIELD, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria1).and(criteria2);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        List<IVMRequest> requests = find(igniteQuery);
        IVMRequest request = null;
        if (null != requests && !requests.isEmpty()) {
            request = requests.get(0);
        }
        return Optional.ofNullable(request);
    }

    /**
     * Find IVMRequest by vehicleId and sessionId.
     *
     * @param vehicleId vehicleId
     * @param sessionId sessionId
     * @return IVMRequest
     */
    @Override
    public Optional<IVMRequest> findByVehicleIdSessionId(String vehicleId, String sessionId) {
        IgniteCriteria criteria1 =
            new IgniteCriteria(NotificationDaoConstants.SESSION_ID_FIELD, Operator.EQ, sessionId);
        IgniteCriteria criteria2 = new IgniteCriteria(NotificationDaoConstants.VEHICLEID_FIELD, Operator.EQ, vehicleId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria1).and(criteria2);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        List<IVMRequest> requests = find(igniteQuery);
        IVMRequest request = null;
        if (null != requests && !requests.isEmpty()) {
            request = requests.get(0);
        }
        return Optional.ofNullable(request);
    }
}
