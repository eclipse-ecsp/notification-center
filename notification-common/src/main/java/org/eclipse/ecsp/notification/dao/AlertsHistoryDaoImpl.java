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
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteOrderBy;
import org.eclipse.ecsp.nosqldao.IgnitePagingInfoResponse;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * AlertsHistoryDaoImpl class.
 */
@Repository
public class AlertsHistoryDaoImpl extends IgniteBaseDAOMongoImpl<String, AlertsHistoryInfo>
    implements AlertsHistoryDao {

    private List<String> failedStatus =
        Arrays.asList("FAILURE", "Failure", "RETRY_ATTEMPTS_EXCEEDED", "DEVICE_STATUS_INACTIVE",
            "DEVICE_DELIVERY_CUTOFF_EXCEEDED", "MissingDestination");

    /**
     * Find by pdid and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param since since
     * @param until until
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByPdidAndTimestampBetween(String pdid, String userId, long since, long until) {
        IgniteCriteria pdIdCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(pdIdCriteria).and(userIdCriteria).and(fromTimeCriteria).and(toTimeCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        return find(query);
    }

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
    @Override
    public List<AlertsHistoryInfo> findByPdidAndTimestampBetweenAndAlertTypeIn(String pdid, String userId, long since,
                                                                               long until,
                                                                               Collection<String> alertTypes) {
        IgniteCriteria pdIdCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteria alertTypeInCriteria =
            new IgniteCriteria(NotificationConstants.ALERT_TYPE, Operator.IN, alertTypes);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(pdIdCriteria).and(userIdCriteria).and(fromTimeCriteria).and(toTimeCriteria)
                .and(alertTypeInCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        return find(query);
    }

    /**
     * Find by pdid and read and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param idList id list
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByPdidAndIdIn(String pdid, String userId, List<String> idList) {
        IgniteCriteria pdIdCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria idInCriteria = new IgniteCriteria(NotificationConstants.ID, Operator.IN, idList);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(pdIdCriteria).and(userIdCriteria).and(idInCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        return find(query);
    }

    /**
     * Find by pdid.
     *
     * @param pdid pdid
     * @param userId userId
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByPdid(String pdid, String userId) {
        IgniteCriteria pdIdCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(pdIdCriteria).and(userIdCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        return find(query);
    }

    /**
     * Find by pdid and read and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param since since
     * @param until until
     * @param page offset
     * @param pageSize limit
     * @return list of AlertsHistoryInfo
     */
    @Override
    public IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidInAndTimestampBetweenOrderByTimestampDesc(
        List<String> pdid, String userId, long since, long until, int page, int pageSize) {
        IgniteCriteria pdIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.IN, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(pdIdInCriteria).and(userIdCriteria).and(fromTimeCriteria).and(toTimeCriteria);

        IgniteOrderBy orderByTimeStamp = new IgniteOrderBy();
        orderByTimeStamp.byfield(NotificationConstants.TIMESTAMP);
        orderByTimeStamp.desc();
        IgniteQuery query = new IgniteQuery(criteriaGroup).orderBy(orderByTimeStamp);

        query.setPageNumber(page);
        query.setPageSize(pageSize);
        return findWithPagingInfo(query);
    }

    /**
     * Find by pdid and alert type in and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param alertTypes alert types
     * @param since since
     * @param until until
     * @param page offset
     * @param pageSize limit
     * @return list of AlertsHistoryInfo
     */
    @Override
    public IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidAndAlertTypeInAndTimestampBetweenOrderByTimestampDesc(
        List<String> pdid, String userId, Collection<String> alertTypes, long since, long until, int page,
        int pageSize) {
        IgniteCriteria pdIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.IN, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria alertTypeInCriteria =
            new IgniteCriteria(NotificationConstants.ALERT_TYPE, Operator.IN, alertTypes);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(pdIdInCriteria).and(userIdCriteria).and(alertTypeInCriteria).and(fromTimeCriteria)
                .and(toTimeCriteria);

        IgniteOrderBy orderByTimeStamp = new IgniteOrderBy();
        orderByTimeStamp.byfield(NotificationConstants.TIMESTAMP);
        orderByTimeStamp.desc();
        IgniteQuery query = new IgniteQuery(criteriaGroup).orderBy(orderByTimeStamp);

        query.setPageNumber(page);
        query.setPageSize(pageSize);
        return findWithPagingInfo(query);
    }

    /**
     * Find by pdid and read and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param read read
     * @param since since
     * @param until until
     * @param page offset
     * @param pageSize limit
     * @return list of AlertsHistoryInfo
     */
    @Override
    public IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc(
        List<String> pdid, String userId, boolean read, long since, long until, int page, int pageSize) {
        IgniteCriteria pdIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.IN, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria readCriteria = new IgniteCriteria(NotificationConstants.READ, Operator.EQ, read);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(pdIdInCriteria).and(userIdCriteria).and(readCriteria).and(fromTimeCriteria)
                .and(toTimeCriteria);

        IgniteOrderBy orderByTimeStamp = new IgniteOrderBy();
        orderByTimeStamp.byfield(NotificationConstants.TIMESTAMP);
        orderByTimeStamp.desc();
        IgniteQuery query = new IgniteQuery(criteriaGroup).orderBy(orderByTimeStamp);

        query.setPageNumber(page);
        query.setPageSize(pageSize);
        return findWithPagingInfo(query);
    }

    /**
     * Find by pdid read alert type and timestamp between.
     *
     * @param pdid pdid
     * @param userId user id
     * @param read read
     * @param alertTypes alert types
     * @param since since
     * @param until until
     * @param page offset
     * @param pageSize limit
     * @return list of AlertsHistoryInfo
     */
    @Override
    public IgnitePagingInfoResponse<AlertsHistoryInfo> findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(
        List<String> pdid, String userId, boolean read, Collection<String> alertTypes, long since, long until, int page,
        int pageSize) {
        IgniteCriteria pdIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.IN, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria readCriteria = new IgniteCriteria(NotificationConstants.READ, Operator.EQ, read);
        IgniteCriteria alertTypeInCriteria =
            new IgniteCriteria(NotificationConstants.ALERT_TYPE, Operator.IN, alertTypes);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(pdIdInCriteria).and(userIdCriteria).and(readCriteria)
                .and(alertTypeInCriteria)
                .and(fromTimeCriteria)
                .and(toTimeCriteria);

        IgniteOrderBy orderByTimeStamp = new IgniteOrderBy();
        orderByTimeStamp.byfield(NotificationConstants.TIMESTAMP);
        orderByTimeStamp.desc();
        IgniteQuery query = new IgniteQuery(criteriaGroup).orderBy(orderByTimeStamp);

        query.setPageNumber(page);
        query.setPageSize(pageSize);
        return findWithPagingInfo(query);
    }

    /**
     * findByuserIdvehicleIdTimestampBetween.
     *
     * @param pdid vehicleId
     * @param userId userId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByuserIdvehicleIdTimestampBetween(String pdid, String userId, long since,
                                                                         long until, int size, int page) {
        IgniteCriteria vehicleIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, pdid);
        IgniteCriteria userIdInCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(userIdInCriteria).and(vehicleIdInCriteria).and(fromTimeCriteria)
                .and(toTimeCriteria);

        IgniteQuery query = new IgniteQuery(criteriaGroup);
        query.setPageNumber(page);
        query.setPageSize(size);
        return find(query);
    }

    /**
     * findByUserIdTimestampBetween.
     *
     * @param userId userId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByUserIdTimestampBetween(String userId, long since, long until, int size,
                                                                int page) {
        IgniteCriteria userIdInCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(userIdInCriteria).and(fromTimeCriteria).and(toTimeCriteria);

        IgniteQuery query = new IgniteQuery(criteriaGroup);

        query.setPageNumber(page);
        query.setPageSize(size);
        return find(query);
    }

    /**
     * findByVehicleIdTimestampBetween.
     *
     * @param pdid vehicleId
     * @param userId userId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByVehicleIdTimestampBetween(String pdid, String userId, long since, long until,
                                                                   int size, int page) {
        IgniteCriteria vehicleIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, pdid);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(vehicleIdInCriteria).and(userIdCriteria).and(fromTimeCriteria).and(toTimeCriteria);

        IgniteQuery query = new IgniteQuery(criteriaGroup);
        query.setPageNumber(page);
        query.setPageSize(size);
        return find(query);
    }

    /**
     * findByOnlyVehicleIdTimestampBetween.
     *
     * @param pdid vehicleId
     * @param since since
     * @param until until
     * @param size size
     * @param page page
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByOnlyVehicleIdTimestampBetween(String pdid, long since, long until, int size,
                                                                       int page) {
        IgniteCriteria vehicleIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, pdid);
        IgniteCriteria fromTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.GTE, since);
        IgniteCriteria toTimeCriteria = new IgniteCriteria(NotificationConstants.TIMESTAMP, Operator.LTE, until);
        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(vehicleIdInCriteria).and(fromTimeCriteria).and(toTimeCriteria);

        IgniteQuery query = new IgniteQuery(criteriaGroup);
        query.setPageNumber(page);
        query.setPageSize(size);
        return find(query);
    }

    /**
     * findByUserIdAndRead.
     *
     * @param deviceId deviceId
     * @param userId userId
     * @param read read
     * @return list of AlertsHistoryInfo
     */
    @Override
    public List<AlertsHistoryInfo> findByPdidAndRead(String deviceId, String userId, boolean read) {
        IgniteCriteria pdIdCriteria =
            new IgniteCriteria().field(NotificationConstants.PDID).op(Operator.EQ).val(deviceId);
        IgniteCriteria userIdCriteria = new IgniteCriteria(NotificationConstants.USERID, Operator.EQ, userId);
        IgniteCriteria readCriteria = new IgniteCriteria().field(NotificationConstants.READ).op(Operator.EQ).val(read);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(pdIdCriteria).and(userIdCriteria).and(readCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        return find(query);
    }

    /**
     * updateChannel.
     *
     * @param docId docId
     * @param channelResponseIndex channelResponseIndex
     * @param response response
     * @return boolean
     */
    @Override
    public boolean updateChannel(String docId, int channelResponseIndex, AbstractChannelResponse response) {
        Updates channelUpdates = new Updates();
        channelUpdates.addFieldSet("channelResponses." + channelResponseIndex, response);
        IgniteCriteria idCriteria = new IgniteCriteria("_id", Operator.EQ, docId);
        IgniteCriteriaGroup findDocById = new IgniteCriteriaGroup(idCriteria);
        IgniteQuery query = new IgniteQuery(findDocById);
        boolean isUpdated = update(query, channelUpdates);
        return isUpdated;
    }

    /**
     * Find by vehicleId and platformResponseId.
     *
     * @param vehicleId vehicleId
     * @param platformResponseId platformResponseId
     * @return AlertsHistoryInfo
     */
    @Override
    public AlertsHistoryInfo findByVehicleIdAndPlatformResponseId(String vehicleId, String platformResponseId) {
        IgniteCriteria vehicleIdInCriteria = new IgniteCriteria(NotificationConstants.PDID, Operator.EQ, vehicleId);
        IgniteCriteria idCriteria = new IgniteCriteria("_id", Operator.EQ, platformResponseId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(vehicleIdInCriteria).and(idCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        List<AlertsHistoryInfo> alerts = find(query);
        if (!alerts.isEmpty()) {
            return alerts.get(0);
        }
        return null;
    }

    /**
     * findCountByCampaignId.
     *
     * @param campaignId campaignId
     * @return long
     */
    @Override
    public long findCountByCampaignId(String campaignId) {
        IgniteCriteria campaignIdInCriteria =
            new IgniteCriteria(NotificationConstants.CAMPAIGN_ID, Operator.EQ, campaignId);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(campaignIdInCriteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);

        return countByQuery(query);
    }

    /**
     * findCountOfSuccessfulRequests.
     *
     * @param campaignId campaignId
     * @return long
     */
    @Override
    public long findCountOfSuccessfulRequests(String campaignId) {
        IgniteCriteria campaignIdInCriteria =
            new IgniteCriteria(NotificationConstants.CAMPAIGN_ID, Operator.EQ, campaignId);
        IgniteCriteria channelResponsesCriteria =
            new IgniteCriteria(NotificationConstants.CHANNEL_RESPONSES_STATUS, Operator.NOT_IN, failedStatus);
        IgniteCriteria statusHistoryListCriteria =
            new IgniteCriteria(NotificationConstants.STATUS_HISTORY_RECORD_STATUS, Operator.EQ, "DONE");

        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(campaignIdInCriteria).and(channelResponsesCriteria).and(statusHistoryListCriteria);

        IgniteQuery query = new IgniteQuery(criteriaGroup);

        return countByQuery(query);
    }

    /**
     * findByCampaignIdPageAndSizeFailed.
     *
     * @param campaignId campaignId
     * @return long
     */
    @Override
    public List<AlertsHistoryInfo> findByCampaignIdPageAndSizeFailed(String campaignId, int page, int size) {


        IgniteCriteria campaignIdInCriteria =
            new IgniteCriteria(NotificationConstants.CAMPAIGN_ID, Operator.EQ, campaignId);
        IgniteCriteria channelResponsesCriteria =
            new IgniteCriteria(NotificationConstants.CHANNEL_RESPONSES_STATUS, Operator.IN, failedStatus);
        IgniteCriteria statusHistoryListCriteria =
            new IgniteCriteria(NotificationConstants.STATUS_HISTORY_RECORD_STATUS, Operator.NEQ, "DONE");


        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(campaignIdInCriteria);

        IgniteQuery query = new IgniteQuery(criteriaGroup);
        IgniteCriteriaGroup criteriaGroup1 =
            new IgniteCriteriaGroup(statusHistoryListCriteria).or(channelResponsesCriteria);
        query.and(criteriaGroup1);
        query.setPageNumber(page);
        query.setPageSize(size);

        return find(query);
    }

    /**
     * findByCampaignIdPageAndSizeSuccess.
     *
     * @param campaignId campaignId
     * @return long
     */
    @Override
    public List<AlertsHistoryInfo> findByCampaignIdPageAndSizeSuccess(String campaignId, int page, int size) {
        IgniteCriteria campaignIdInCriteria =
            new IgniteCriteria(NotificationConstants.CAMPAIGN_ID, Operator.EQ, campaignId);
        IgniteCriteria channelResponsesCriteria =
            new IgniteCriteria(NotificationConstants.CHANNEL_RESPONSES_STATUS, Operator.NOT_IN, failedStatus);
        IgniteCriteria statusHistoryListCriteria =
            new IgniteCriteria(NotificationConstants.STATUS_HISTORY_RECORD_STATUS, Operator.EQ, "DONE");

        IgniteCriteriaGroup criteriaGroup =
            new IgniteCriteriaGroup(campaignIdInCriteria).and(statusHistoryListCriteria).and(channelResponsesCriteria);

        IgniteQuery query = new IgniteQuery(criteriaGroup);
        query.setPageNumber(page);
        query.setPageSize(size);

        return find(query);
    }

}