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

package org.eclipse.ecsp.notification.processors;

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * NotificationGroupFinder class.
 *
 * @author AMuraleedhar
 */
@Component
@Order(2)
public class NotificationGroupFinder implements NotificationProcessor {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationGroupFinder.class);

    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;

    /**
     * process method.
     *
     * @param alert AlertsInfo
     */
    @Override
    public void process(AlertsInfo alert) {
        LOGGER.debug("Finding group for the alert {} ", alert);
        NotificationGrouping ng =
                notificationGroupingDao.findFirstByNotificationId(alert.getAlertsData().getNotificationId());
        if (ng == null) {
            throw new IllegalArgumentException("Notification grouping not found for notificationId "
                    + alert.getAlertsData().getNotificationId() + ". Cannot process any further");
        }
        alert.setNotificationGrouping(ng);
    }

}
