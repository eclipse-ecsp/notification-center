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

package org.eclipse.ecsp.platform.notification.dto;

import org.eclipse.ecsp.domain.notification.commons.ChannelResponseData;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;

import java.util.List;

/**
 * NotificationChannelDetails class.
 */
public class NotificationChannelDetails {

    private String id;

    private String link;

    private Long notificationDate;

    private String group;

    private String notificationName;

    private String notificationId;

    private List<ChannelResponseData> channelResponses;

    private List<StatusHistoryRecord> statusHistoryRecordList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(Long notificationDate) {
        this.notificationDate = notificationDate;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getNotificationName() {
        return notificationName;
    }

    public void setNotificationName(String notificationName) {
        this.notificationName = notificationName;
    }

    public List<ChannelResponseData> getChannelResponses() {
        return channelResponses;
    }

    /**
     * Set channelResponses.
     *
     * @param channelResponses List of ChannelResponseData
     */
    public void setChannelResponses(List<ChannelResponseData> channelResponses) {
        this.channelResponses = channelResponses;
    }

    /**
     * Get link.
     *
     * @return link
     */
    public String getLink() {
        return link;
    }

    /**
     * Set link.
     *
     * @param link link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Get statusHistoryRecordList.
     *
     * @return statusHistoryRecordList
     */
    public List<StatusHistoryRecord> getStatusHistoryRecordList() {
        return statusHistoryRecordList;
    }

    /**
     * Set statusHistoryRecordList.
     *
     * @param statusHistoryRecordList List of StatusHistoryRecord
     */
    public void setStatusHistoryRecordList(List<StatusHistoryRecord> statusHistoryRecordList) {
        this.statusHistoryRecordList = statusHistoryRecordList;
    }

    /**
     * Get notificationId.
     *
     * @return notificationId
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Set notificationId.
     *
     * @param notificationId notificationId
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

}