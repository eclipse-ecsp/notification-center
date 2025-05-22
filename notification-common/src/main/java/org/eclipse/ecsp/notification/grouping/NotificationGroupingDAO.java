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

import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;

import java.util.Collection;
import java.util.List;

/**
 * NotificationGroupingDAO interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface NotificationGroupingDAO extends IgniteBaseDAO<String, NotificationGrouping> {

    /**
     * Find by mandatory.
     *
     * @param mandatory mandatory
     * @return List of NotificationGrouping
     */
    List<NotificationGrouping> findByMandatory(boolean mandatory);

    /**
     * Find by group.
     *
     * @param groupName group name
     * @return List of NotificationGrouping
     */
    List<NotificationGrouping> findByGroups(Collection<String> groupName);

    /**
     * Find First By Notification Id.
     *
     * @param notificationId notificationId
     * @return NotificationGrouping
     */
    NotificationGrouping findFirstByNotificationId(String notificationId);

    /**
     * Find by notification id.
     *
     * @param notificationId notificationId
     * @return List of NotificationGrouping
     */
    List<NotificationGrouping> findByNotificationId(String notificationId);

    /**
     * Delete by group notification id and service.
     *
     * @param group group
     * @param notificationId notificationId
     * @param service service
     * @return int
     */
    int deleteByGroupNotificationIdAndService(String group, String notificationId, String service);
}
