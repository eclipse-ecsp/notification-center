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

package org.eclipse.ecsp.platform.notification.service;

import javassist.NotFoundException;
import org.eclipse.ecsp.platform.notification.dto.CampaignSummary;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;

import java.util.List;


/**
 * NotificationHistoryService operations contract.
 *
 * @author AMadan
 */

public interface NotificationHistoryService {

    /**
     * Method to get the notification status.
     *
     * @param platformResponseId Platform response id
     * @param content            Content
     * @param vehicleId          Vehicle id
     * @return NotificationChannelDetails
     * @throws NotFoundException NotFoundException
     */
    NotificationChannelDetails getNotificationStatus(final String platformResponseId, String content, String vehicleId)
        throws NotFoundException;

    /**
     * Method to get the notification history.
     *
     * @param userId userId
     * @param vehicleId vehicleId
     * @param since since
     * @param until until
     * @param size size
     * @param page  page
     * @return List of NotificationChannelDetails
     */
    List<NotificationChannelDetails> getNotificationHistoryUserIdVehicleId(String userId, String vehicleId, long since,
                                                                           long until, int size, int page);

    /**
     * Method to get the notification history.
     *
     * @param userId userId
     * @param since since
     * @param until until
     * @param size size
     * @param page  page
     * @return List of NotificationChannelDetails
     */
    List<NotificationChannelDetails> getNotificationHistoryUserId(String userId, long since, long until, int size,
                                                                  int page);

    /**
     * Method to get the notification history by vehicleId.
     *
     * @param vehicleId vehicleId
     * @param since since
     * @param until until
     * @param size size
     * @param page  page
     * @return List of NotificationChannelDetails
     */
    List<NotificationChannelDetails> getNotificationHistoryVehicleId(String vehicleId, long since, long until, int size,
                                                                     int page);

    /**
     * Method to get the notification history by non reg vehicleId.
     *
     * @param vehicleId vehicleId
     * @param since since
     * @param until until
     * @param size size
     * @param page  page
     * @return List of NotificationChannelDetails
     */
    List<NotificationChannelDetails> getNotificationHistoryNonRegVehicleId(String vehicleId, long since, long until,
                                                                           int size, int page);

    /**
     * Method to get the notification history by campaignId.
     *
     * @param campaignId campaignId
     * @param status status
     * @param page page
     * @param size size
     * @return CampaignSummary
     * @throws NotFoundException NotFoundException
     */
    CampaignSummary getCampaignHistory(String campaignId, String status, int page, int size) throws NotFoundException;
}
