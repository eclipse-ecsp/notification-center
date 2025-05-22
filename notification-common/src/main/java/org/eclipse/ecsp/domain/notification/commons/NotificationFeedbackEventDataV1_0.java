
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

package org.eclipse.ecsp.domain.notification.commons;

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.entities.AbstractEventData;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationFeedbackEventDataV1_0 event data class.
 */
@SuppressWarnings("checkstyle:TypeName")
@EventMapping(id = EventID.NOTIFICATION_FEEDBACK, version = Version.V1_0)
public class NotificationFeedbackEventDataV1_0 extends AbstractEventData {
    private static final long serialVersionUID = -8872485191824441941L;
    private String notificationStatus;
    private String notificationErrorDetail;
    private List<ChannelFeedback> channelFeedbacks = new ArrayList<>();
    private String group;
    private String notificationName;
    private String notificationId;
    private List<StatusHistoryRecord> statusHistoryRecordList;

    /**
     * Get group.
     *
     * @return group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set group.
     *
     * @param group string
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Get notification name.
     *
     * @return notification name
     */
    public String getNotificationName() {
        return notificationName;
    }

    /**
     * Set notification name.
     *
     * @param notificationName string
     */
    public void setNotificationName(String notificationName) {
        this.notificationName = notificationName;
    }

    /**
     * Get notification id.
     *
     * @return notification id
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Set notification id.
     *
     * @param notificationId string
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Get status history record list.
     *
     * @return status history record list
     */
    public List<StatusHistoryRecord> getStatusHistoryRecordList() {
        return statusHistoryRecordList;
    }

    /**
     * Set status history record list.
     *
     * @param statusHistoryRecordList list
     */
    public void setStatusHistoryRecordList(List<StatusHistoryRecord> statusHistoryRecordList) {
        this.statusHistoryRecordList = statusHistoryRecordList;
    }

    /**
     * Get notification status.
     *
     * @return notification status
     */
    public String getNotificationStatus() {
        return notificationStatus;
    }

    /**
     * Set notification status.
     *
     * @param notificationStatus string
     */
    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    /**
     * Get notification error detail.
     *
     * @return notification error detail
     */
    public String getNotificationErrorDetail() {
        return notificationErrorDetail;
    }

    /**
     * Set notification error detail.
     *
     * @param notificationErrorDetail string
     */
    public void setNotificationErrorDetail(String notificationErrorDetail) {
        this.notificationErrorDetail = notificationErrorDetail;
    }

    /**
     * Get channel feedbacks.
     *
     * @return channel feedbacks
     */
    public List<ChannelFeedback> getChannelFeedbacks() {
        return channelFeedbacks;
    }

    /**
     * Set channel feedbacks.
     *
     * @param channelFeedbacks list
     */
    public void setChannelFeedbacks(List<ChannelFeedback> channelFeedbacks) {
        this.channelFeedbacks = channelFeedbacks;
    }

    /**
     * Add channel feedback.
     *
     * @param channelReponse channel feedback
     */
    public void addChannelFeedback(ChannelFeedback channelReponse) {
        this.channelFeedbacks.add(channelReponse);
    }

    /**
     * Add status history record.
     *
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotificationFeedbackEventDataV1_0{");
        sb.append("notificationStatus='").append(notificationStatus).append('\'');
        sb.append(", notificationErrorDetail='").append(notificationErrorDetail).append('\'');
        sb.append(", channelFeedbacks=").append(channelFeedbacks);
        sb.append(", group='").append(group).append('\'');
        sb.append(", notificationName='").append(notificationName).append('\'');
        sb.append(", notificationId='").append(notificationId).append('\'');
        sb.append(", statusHistoryRecordList=").append(statusHistoryRecordList);
        sb.append('}');
        return sb.toString();
    }
}
