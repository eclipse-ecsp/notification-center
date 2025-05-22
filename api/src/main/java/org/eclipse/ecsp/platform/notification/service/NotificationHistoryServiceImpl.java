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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.BaseTemplate;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.commons.ChannelResponseData;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.platform.notification.dto.CampaignDetail;
import org.eclipse.ecsp.platform.notification.dto.CampaignSummary;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Notifiattion history operation services.
 */
@Service
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHistoryServiceImpl.class);

    private static final String QUERY_PARAM_STATUS = "status";
    private static final String FAILURE = "Failure";
    /**
     * Alert history information not found.
     */
    public static final String ALERT_HISTORY_INFORMATION_NOT_FOUND = "Alert history information not found";
    private Map<String, CampaignSummary> campaignSummaryCache = new ConcurrentHashMap<>();

    @Autowired
    private AlertsHistoryDao alertsHistoryDao;

    @Autowired
    private AlertsServiceV3 alertService;

    /**
     * Service for get notification status details based on platform response id and
     * content will constant value i.e status,full. if content is "status" then
     * return only status of channel and on another case if content is "full" then
     * return whole notification history details.
     *
     * @param platformResponseId platformResponseId
     * @param content            content
     */
    @Override
    public NotificationChannelDetails getNotificationStatus(final String platformResponseId, String content,
                                                            String vehicleId) throws NotFoundException {
        LOGGER.info("Request to get notification status details");
        AlertsHistoryInfo alertHistoryInfo = null;
        if (vehicleId == null) {
            alertHistoryInfo = alertsHistoryDao.findById(platformResponseId);
        } else {
            alertHistoryInfo = alertsHistoryDao.findByVehicleIdAndPlatformResponseId(vehicleId, platformResponseId);
        }

        if (null == alertHistoryInfo || null == alertHistoryInfo.getChannelResponses()) {
            LOGGER.info(ALERT_HISTORY_INFORMATION_NOT_FOUND);
            throw new NotFoundException(ALERT_HISTORY_INFORMATION_NOT_FOUND);
        }

        if (content.equals(QUERY_PARAM_STATUS)) {
            return getChannelHistoryStatus(alertHistoryInfo);
        } else {
            return getChannelHistoryDetails(alertHistoryInfo);
        }
    }

    /**
     * Method to get the notification history.
     *
     * @param alertHistoryInfo  alertHistoryInfo
     * @return NotificationChannelDetails
     */
    private NotificationChannelDetails getChannelHistoryStatus(AlertsHistoryInfo alertHistoryInfo) {
        NotificationChannelDetails notificationChannelDetails = new NotificationChannelDetails();
        notificationChannelDetails.setId(alertHistoryInfo.getId());
        List<ChannelResponseData> channelResponseDataList = new ArrayList<>();

        for (ChannelResponse response : alertHistoryInfo.getChannelResponses()) {
            ChannelResponseData channel = new ChannelResponseData();
            channel.setChannelType(response.getChannelType().getChannelType());
            channel.setStatus(response.getStatus());
            channel.setDestination(response.getDestination());
            if (StringUtils.isNotBlank(response.getStatus()) && StringUtils.equals(response.getStatus(), FAILURE)) {
                channel.setErrorCode(response.getErrorCode());
            }
            channelResponseDataList.add(channel);
        }
        notificationChannelDetails.setStatusHistoryRecordList(alertHistoryInfo.getStatusHistoryRecordList());
        notificationChannelDetails.setChannelResponses(channelResponseDataList);
        notificationChannelDetails.setGroup(alertHistoryInfo.getGroup());
        notificationChannelDetails.setNotificationDate(alertHistoryInfo.getTimestamp());
        notificationChannelDetails.setNotificationName(alertHistoryInfo.getNotificationLongName());
        notificationChannelDetails.setNotificationId(alertHistoryInfo.getNotificationId());
        LOGGER.debug("NotificationChannelDetails Response {}", notificationChannelDetails);
        return notificationChannelDetails;
    }

    /**
     * Method to get the notification history.
     *
     * @param alertHistoryInfo alertHistoryInfo
     * @return NotificationChannelDetails
     */
    private NotificationChannelDetails getChannelHistoryDetails(AlertsHistoryInfo alertHistoryInfo) {
        NotificationChannelDetails notificationChannelDetails = new NotificationChannelDetails();
        notificationChannelDetails.setId(alertHistoryInfo.getId());
        notificationChannelDetails.setNotificationDate(alertHistoryInfo.getTimestamp());
        notificationChannelDetails.setGroup(alertHistoryInfo.getGroup());
        notificationChannelDetails.setNotificationName(alertHistoryInfo.getNotificationLongName());
        notificationChannelDetails.setNotificationId(alertHistoryInfo.getNotificationId());
        List<ChannelResponseData> channelResponseDataList = new ArrayList<>();
        for (ChannelResponse response : alertHistoryInfo.getChannelResponses()) {
            ChannelResponseData channel = new ChannelResponseData();
            channel.setChannelType(response.getChannelType().getChannelType());
            channel.setDestination(response.getDestination());
            BaseTemplate template = response.getTemplate();
            if (template != null) {
                channel.setTitle(template.getTitle());
                channel.setSubtitle(template.getSubtitle());
                channel.setBody(template.getBody());
            }
            channel.setStatus(response.getStatus());
            if (StringUtils.isNotBlank(response.getStatus()) && StringUtils.equals(response.getStatus(), FAILURE)) {
                channel.setErrorCode(response.getErrorCode());
            }
            channelResponseDataList.add(channel);
        }
        notificationChannelDetails.setStatusHistoryRecordList(alertHistoryInfo.getStatusHistoryRecordList());
        notificationChannelDetails.setChannelResponses(channelResponseDataList);
        return notificationChannelDetails;
    }

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
    @Override
    public List<NotificationChannelDetails> getNotificationHistoryUserIdVehicleId(String userId, String vehicleId,
                                                                                  long since, long until, int size,
                                                                                  int page) {
        List<AlertsHistoryInfo> alerts = alertsHistoryDao.findByuserIdvehicleIdTimestampBetween(vehicleId, userId,
            since, until, size, page);
        alertService.cleanStatusHistoryRecords(alerts);
        List<NotificationChannelDetails> alertDetails = new ArrayList<>();
        alerts.forEach(alert -> alertDetails.add(getChannelHistoryStatus(alert)));
        return alertDetails;
    }

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
    @Override
    public List<NotificationChannelDetails> getNotificationHistoryUserId(String userId, long since, long until,
                                                                         int size, int page) {
        List<AlertsHistoryInfo> alerts = alertsHistoryDao.findByUserIdTimestampBetween(userId, since, until, size,
            page);
        alertService.cleanStatusHistoryRecords(alerts);
        List<NotificationChannelDetails> alertDetails = new ArrayList<>();

        alerts.forEach(alert -> alertDetails.add(getChannelHistoryStatus(alert)));
        return alertDetails;
    }

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
    @Override
    public List<NotificationChannelDetails> getNotificationHistoryVehicleId(String vehicleId, long since, long until,
                                                                            int size, int page)  {
        String userId = alertService.getUserIdFromDevice(vehicleId);
        List<AlertsHistoryInfo> alerts = alertsHistoryDao.findByVehicleIdTimestampBetween(vehicleId, userId, since,
            until, size, page);
        alertService.cleanStatusHistoryRecords(alerts);
        List<NotificationChannelDetails> alertDetails = new ArrayList<>();

        alerts.forEach(alert -> alertDetails.add(getChannelHistoryStatus(alert)));
        return alertDetails;
    }

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
    @Override
    public List<NotificationChannelDetails> getNotificationHistoryNonRegVehicleId(String vehicleId, long since,
                                                                                  long until, int size, int page) {
        List<AlertsHistoryInfo> alerts = alertsHistoryDao.findByOnlyVehicleIdTimestampBetween(vehicleId, since, until,
            size, page);
        alertService.cleanStatusHistoryRecords(alerts);
        List<NotificationChannelDetails> alertDetails = new ArrayList<>();

        alerts.forEach(alert -> alertDetails.add(getChannelHistoryStatus(alert)));
        return alertDetails;
    }

    /**
     * Method to get the notification history by campaignId.
     *
     * @param campaignId campaignId
     * @param status status
     * @param page page
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    @Override
    public CampaignSummary getCampaignHistory(String campaignId, String status, int page, int size)
        throws NotFoundException {

        LOGGER.info("Request to get campaign status details for campaign Id {}", campaignId);

        CampaignSummary campaignSummary = null;
        List<AlertsHistoryInfo> alertHistoryInfo = null;
        long totalRequests;
        long successRequests;
        long failedRequests;

        if (campaignSummaryCache.get(campaignId) == null) {
            LOGGER.info("Campaign {} details are not present in cache fetching from DB", campaignId);

            totalRequests = alertsHistoryDao.findCountByCampaignId(campaignId);

            if (totalRequests != 0) {
                successRequests = alertsHistoryDao.findCountOfSuccessfulRequests(campaignId);
                failedRequests = totalRequests - successRequests;

                campaignSummary = new CampaignSummary();
                campaignSummary.setCampaignId(campaignId);
                campaignSummary.setTotalRequests(totalRequests);
                campaignSummary.setFailedRequests(failedRequests);
                campaignSummary.setSuccessfulRequests(successRequests);
                campaignSummaryCache.put(campaignId, campaignSummary);

            } else {
                return null;
            }

        } else {
            LOGGER.info("Campaign {} details are present in cache fetching from map", campaignId);
            campaignSummary = campaignSummaryCache.get(campaignId);
            LOGGER.debug("CampaignSummary for campaign {} fetched from cache", campaignId);

        }
        //Fetch Based on whether the user wants to see failed requests or successful ones
        if (status.equalsIgnoreCase(FAILURE)) {
            alertHistoryInfo = alertsHistoryDao.findByCampaignIdPageAndSizeFailed(campaignId, page, size);
            campaignSummary.setTotalPages(campaignSummary.getFailedRequests() % size == 0
                ? campaignSummary.getFailedRequests() / size
                : campaignSummary.getFailedRequests() / size + 1);
        } else {
            alertHistoryInfo = alertsHistoryDao.findByCampaignIdPageAndSizeSuccess(campaignId, page, size);
            campaignSummary.setTotalPages(campaignSummary.getSuccessfulRequests() % size == 0
                ? campaignSummary.getSuccessfulRequests() / size
                : campaignSummary.getSuccessfulRequests() / size + 1);

        }

        if (null == alertHistoryInfo || alertHistoryInfo.isEmpty()) {
            LOGGER.info(ALERT_HISTORY_INFORMATION_NOT_FOUND);
            throw new NotFoundException(ALERT_HISTORY_INFORMATION_NOT_FOUND);
        }

        // Build Response

        campaignSummary.setCampaignDate(
            LocalDateTime.ofEpochSecond(alertHistoryInfo.get(0).getTimestamp() / 1000, 0, ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        campaignSummary.setCurrentPageNumber((long) page);
        campaignSummary.setCampaignChannelDetails(getCampaignDetails(status, alertHistoryInfo));


        return campaignSummary;
    }

    /**
     * Method to get campaign details.
     *
     * @param status          status
     * @param alertHistoryInfo alertHistoryInfo
     * @return List of CampaignDetail
     */
    @NotNull
    private static List<CampaignDetail> getCampaignDetails(String status, List<AlertsHistoryInfo> alertHistoryInfo) {
        List<CampaignDetail> campaignChannelDetailsList = new ArrayList<>();
        for (AlertsHistoryInfo alertHistory : alertHistoryInfo) {

            CampaignDetail campaignChannelDetails = new CampaignDetail();

            List<ChannelResponseData> channelResponseDataList = new ArrayList<>();
            for (ChannelResponse response : alertHistory.getChannelResponses()) {
                ChannelResponseData ch = new ChannelResponseData();
                ch.setChannelType(response.getChannelType().getChannelType());
                ch.setDestination(response.getDestination());
                BaseTemplate template = response.getTemplate();
                if (template != null) {
                    ch.setTitle(template.getTitle());
                    ch.setSubtitle(template.getSubtitle());
                    ch.setBody(template.getBody());
                }
                ch.setStatus(response.getStatus());
                if (StringUtils.isNotBlank(response.getStatus()) && StringUtils.equals(response.getStatus(), FAILURE)) {
                    ch.setErrorCode(response.getErrorCode());
                }

                channelResponseDataList.add(ch);

            }
            campaignChannelDetails.setChannelResponses(channelResponseDataList);
            campaignChannelDetails.setStatus(status);
            campaignChannelDetails.setUserId(alertHistory.getUserId());
            campaignChannelDetails.setVehicleId(alertHistory.getPdid());
            campaignChannelDetailsList.add(campaignChannelDetails);
        }
        return campaignChannelDetailsList;
    }

}