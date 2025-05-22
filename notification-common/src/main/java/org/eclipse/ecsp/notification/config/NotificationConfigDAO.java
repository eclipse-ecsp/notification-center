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

package org.eclipse.ecsp.notification.config;

import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;

import java.util.List;
import java.util.Set;

/**
 * NotificationConfigDAO interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface NotificationConfigDAO extends IgniteBaseDAO<String, NotificationConfig> {
    /**
     * Find notification config by user id , vehicle id and group.
     *
     * @param userIds user id
     * @param vehicleIds vehicle id
     * @param group group
     * @return list of notification config
     */
    List<NotificationConfig> findByUserVehicleGroup(List<String> userIds, List<String> vehicleIds, String group);

    /**
     * Find notification config by user id and vehicle id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @return list of notification config
     */
    List<NotificationConfig> findByUserVehicle(String userId, String vehicleId);

    /**
     * Find notification config by user id, vehicle id and contact id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @param contactId contact id
     * @return list of notification config
     */
    List<NotificationConfig> findByUserVehicleContactId(String userId, String vehicleId, String contactId);

    /**
     * Find notification config.
     *
     * @param group group
     * @return list of notification config
     */
    List<NotificationConfig> findDefaultByGroups(Set<String> group);

    /**
     * Find notification config.
     *
     * @param groups group
     * @param brand brand
     * @return list of notification config
     */
    List<NotificationConfig> findDefaultByGroupsAndBrand(Set<String> groups, String brand);

    /**
     * Find notification config by user id, vehicle id, contact id and group.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @param contactId contact id
     * @param group group
     * @return list of notification config
     */
    List<NotificationConfig> findByUserIdAndVehicleIdAndContactIdAndGroups(String userId, String vehicleId,
                                                                           String contactId, Set<String> group);

    /**
     * Delete notification config by contact id and vehicle id.
     *
     * @param contactId contact id
     * @param vehicleId vehicle id
     * @return boolean
     */
    boolean deleteConfigForContact(String contactId, String vehicleId);

    /**
     * Delete notification config by user id and vehicle id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @return boolean
     */
    boolean deleteNotificationConfigByUserAndVehicle(String userId, String vehicleId);
}
