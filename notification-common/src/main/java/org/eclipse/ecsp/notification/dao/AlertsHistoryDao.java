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

import org.eclipse.ecsp.domain.notification.AbstractChannelResponse;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;
import org.eclipse.ecsp.nosqldao.IgnitePagingInfoResponse;

import java.util.Collection;
import java.util.List;

/**
 * AlertsHistoryDao interface.
 */
public interface AlertsHistoryDao extends IgniteBaseDAO<String, AlertsHistoryInfo> {
    /**
     * Find by pdid and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param since since
     * @param until until
     * @return list of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByPdidAndTimestampBetween(String pdid, String userId, long since, long until);

    /**
     * Find by pdid and timestamp between and alert type in.
     *
     * @param pdid pdid
     * @param userId user id
     * @param since since
     * @param until until
     * @param alertTypes alert types
     * @return list of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByPdidAndTimestampBetweenAndAlertTypeIn(String pdid, String userId, long since,
                                                                        long until, Collection<String> alertTypes);

    /**
     * Find by pdid and read and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param idList id list
     * @return list of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByPdidAndIdIn(String pdid, String userId, List<String> idList);

    /**
     * Find by pdid and read and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param since since
     * @param until until
     * @param offset offset
     * @param limit limit
     * @return list of AlertsHistoryInfo
     */
    IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidInAndTimestampBetweenOrderByTimestampDesc(List<String> pdid,
                                                                                                    String userId,
                                                                                                    long since,
                                                                                                    long until,
                                                                                                    int offset,
                                                                                                    int limit);

    /**
     * Find by pdid and read and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param alertTypes alert types
     * @param since since
     * @param until until
     * @param offset offset
     * @param limit limit
     * @return list of AlertsHistoryInfo
     */
    IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidAndAlertTypeInAndTimestampBetweenOrderByTimestampDesc(
        List<String> pdid, String userId, Collection<String> alertTypes,
        long since,
        long until,
        int offset, int limit);

    /**
     * findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc.
     *
     * @param pdid pdid
     * @param userId userId
     * @param read read
     * @param since since
     * @param until until
     * @param offset offset
     * @param limit limit
     * @return IgnitePagingInfoResponse
     */
    IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc(
        List<String> pdid, String userId, boolean read, long since,
        long until,
        int offset, int limit);

    /**
     * findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc.
     *
     * @param pdid pdid
     * @param userId userId
     * @param read read
     * @param alertTypes alert types
     * @param since since
     * @param until until
     * @param offset offset
     * @param limit limit
     * @return IgnitePagingInfoResponse
     */
    IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(
        List<String> pdid, String userId, boolean read,
        Collection<String> alertTypes,
        long since, long until, int offset, int limit);

    /**
     * findByuserIdvehicleIdTimestampBetween.
     *
     * @param vehicleId vehicleId
     * @param userId userId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return List of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByuserIdvehicleIdTimestampBetween(String vehicleId, String userId,
                                                                  long since,
                                                                  long until,
                                                                  int size, int page);

    /**
     * findByUserIdTimestampBetween.
     *
     * @param userId userId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return List of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByUserIdTimestampBetween(String userId, long since, long until, int size, int page);

    /**
     * findByVehicleIdTimestampBetween.
     *
     * @param vehicleId vehicleId
     * @param userId userId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return List of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByVehicleIdTimestampBetween(String vehicleId, String userId, long since, long until,
                                                            int size, int page);

    /**
     * findByOnlyVehicleIdTimestampBetween.
     *
     * @param vehicleId vehicleId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return List of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByOnlyVehicleIdTimestampBetween(String vehicleId, long since, long until, int size,
                                                                int page);

    /**
     * findByUserIdAndRead.
     *
     * @param deviceId deviceId
     * @param userId userId
     * @param read read
     * @return List of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByPdidAndRead(String deviceId, String userId, boolean read);

    /**
     * findByPdid.
     *
     * @param pdid pdid
     * @param userId userId
     * @return List of AlertsHistoryInfo
     */
    List<AlertsHistoryInfo> findByPdid(String pdid, String userId);

    /**
     * updateChannel.
     *
     * @param docId docId
     * @param channelResponseIndex channelResponseIndex
     * @param response response
     * @return boolean
     */
    boolean updateChannel(String docId, int channelResponseIndex, AbstractChannelResponse response);

    /**
     * findByVehicleIdAndPlatformResponseId.
     *
     * @param vehicleId vehicleId
     * @param platformResponseId platformResponseId
     * @return AlertsHistoryInfo
     */
    AlertsHistoryInfo findByVehicleIdAndPlatformResponseId(String vehicleId, String platformResponseId);

    /**
     * findCountByCampaignId.
     *
     * @param campaignId campaignId
     * @return long
     */
    long findCountByCampaignId(String campaignId);

    /**
     * findCountOfSuccessfulRequests.
     *
     * @param campaignId campaignId
     * @return long
     */
    long findCountOfSuccessfulRequests(String campaignId);

    /**
     * findByCampaignIdPageAndSizeFailed.
     *
     * @param campaignId campaignId
     * @param page page
     * @return long
     */
    List<AlertsHistoryInfo> findByCampaignIdPageAndSizeFailed(String campaignId, int page, int size);

    /**
     * findByCampaignIdPageAndSizeSuccess.
     *
     * @param campaignId campaignId
     * @param page page
     * @param size size
     * @return long
     */
    List<AlertsHistoryInfo> findByCampaignIdPageAndSizeSuccess(String campaignId, int page, int size);

}
