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
import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;
import org.eclipse.ecsp.notification.config.NotificationBuffer;

import java.util.List;

/**
 * NotificationBufferDao class.
 */
public interface NotificationBufferDao extends IgniteBaseDAO<String, NotificationBuffer> {

    /**
     * Find by scheduler id.
     *
     * @param schedulerId scheduler id
     * @return NotificationBuffer
     */
    NotificationBuffer findBySchedulerId(String schedulerId);

    /**
     * Find by user id and vehicle id and channel type and group.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @param channelType channel type
     * @param group group
     * @return NotificationBuffer
     */
    NotificationBuffer findByUserIdAndVehicleIdAndChannelTypeAndGroup(String userId, String vehicleId,
                                                                      ChannelType channelType, String group);

    /**
     * Find by user id and vehicle id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @return list of NotificationBuffer
     */
    List<NotificationBuffer> findByUserIdAndVehicleId(String userId, String vehicleId);

    /**
     * Delete by user id and vehicle id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     */
    void deleteByUserIdAndVehicleId(String userId, String vehicleId);

    /**
     * Find by user id and vehicle id and channel type and group and contact id.
     *
     * @param userId user id
     * @param vehicleId vehicle id
     * @param channelType channel type
     * @param group group
     * @param contactId contact id
     * @return NotificationBuffer
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    NotificationBuffer findByUserIDVehicleIDChannelTypeGroupContactId(String userId, String vehicleId,
                                                                      ChannelType channelType, String group,
                                                                      String contactId);
}
