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

package org.eclipse.ecsp.domain.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Transient;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.ProcessingStatus;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Simple pojo class for creating the alerts history. This class will be
 * populated from Channel Response object.
 *
 * <p>The json data from this class will be pushed to kafka and later pushed to
 * mongo db which will be used for creating the alerts history
 *
 * <p>Refer : HCP-6796
 */
@Entity(value = "alertHistory")
@Indexes(@Index(fields = {@Field("pdid"), @Field("timestamp"),
    @Field("alertType")}, options = @IndexOptions(name = "AlertsHistoryInfo_Index_1")))
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertsHistoryInfo extends AbstractIgniteEntity {

    @Id
    private String id;

    @JsonProperty(value = "pdid")
    private String pdid;

    @JsonProperty(value = "userId")
    private String userId;

    @JsonProperty(value = "alertType")
    private String alertType;

    private long timestamp;

    @JsonProperty(value = "payload")
    private AlertsInfo payload;

    @JsonIgnore
    private LocalDateTime createDts;

    @JsonIgnore
    private List<ChannelResponse> channelResponses = new ArrayList<>();

    @JsonIgnore
    @Transient
    private String defaultMessage;

    @JsonProperty(value = "alertMessage")
    private String alertMessage;

    @JsonProperty(value = "read")
    private boolean read;

    @JsonIgnore
    private String group;

    @JsonIgnore
    private String notificationLongName;

    @JsonIgnore
    private String notificationId;

    @JsonProperty(value = "deleted")
    private boolean deleted;

    @JsonProperty(value = "campaignId")
    private String campaignId;

    private ProcessingStatus processingStatus;

    private Map<String, String> skippedChannels = new HashMap<>();

    private List<RetryRecord> retryRecordList = new ArrayList<>();

    private List<StatusHistoryRecord> statusHistoryRecordList = new ArrayList<>();


    /**
     * Get the processing status.
     *
     * @return processing status
     */
    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    /**
     * Set the processing status.
     *
     * @param processingStatus processing status
     */
    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    /**
     * Get the skipped channels.
     *
     * @return skipped channels
     */
    public Map<String, String> getSkippedChannels() {
        return skippedChannels;
    }

    /**
     * Set the skipped channels.
     *
     * @param skippedChannels skipped channels
     */
    public void setSkippedChannels(Map<String, String> skippedChannels) {
        this.skippedChannels = skippedChannels;
    }

    /**
     * Get the pdid.
     *
     * @return pdid
     */
    public String getPdid() {
        return pdid;
    }

    /**
     * Set the pdid.
     *
     * @param pdid pdid
     */
    public void setPdid(String pdid) {
        this.pdid = pdid;
    }

    /**
     * Get the user id.
     *
     * @return user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user id.
     *
     * @param userId user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the alert type.
     *
     * @return alert type
     */
    public String getAlertType() {
        return alertType;
    }

    /**
     * Set the alert type.
     *
     * @param alertType alert type
     */
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    /**
     * Get the timestamp.
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp.
     *
     * @param timestamp timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the payload.
     *
     * @return payload
     */
    public AlertsInfo getPayload() {
        return payload;
    }

    /**
     * Set the payload.
     *
     * @param payload payload
     */
    public void setPayload(AlertsInfo payload) {
        this.payload = payload;
    }

    /**
     * Get the channel responses.
     *
     * @return channel responses
     */
    public List<ChannelResponse> getChannelResponses() {
        return channelResponses;
    }

    /**
     * Set the channel responses.
     *
     * @param channelResponses channel responses
     */
    public void setChannelResponses(List<ChannelResponse> channelResponses) {
        this.channelResponses.addAll(channelResponses);
    }

    /**
     * Add channel response.
     *
     * @param channelResponse channel response
     */
    public void addChannelResponse(ChannelResponse channelResponse) {
        channelResponses.add(channelResponse);
    }

    /**
     * Get the create date time.
     *
     * @return create date time
     */
    public LocalDateTime getCreateDts() {
        return createDts;
    }

    /**
     * Set the create date time.
     *
     * @param createDts create date time
     */
    public void setCreateDts(LocalDateTime createDts) {
        this.createDts = createDts;
    }

    /**
     * Set the default message.
     *
     * @param defaultMessage default message
     */
    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    /**
     * Get the id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getAlertMessage.
     *
     * @return alertMessage
     */
    public String getAlertMessage() {
        return alertMessage;
    }

    /**
     * setAlertMessage.
     *
     * @param alertMessage alertMessage
     */
    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    /**
     * getGroup.
     *
     * @return group
     */
    public String getGroup() {
        return group;
    }

    /**
     * setGroup.
     *
     * @param group group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * get default message.
     *
     * @return default message.
     */
    public String getDefaultMessage() {
        if (null == this.defaultMessage) {
            for (ChannelResponse info : channelResponses) {
                String defaultMessage = info.getDefaultMessage();
                if (defaultMessage != null && !defaultMessage.isEmpty()) {
                    this.defaultMessage = info.getDefaultMessage();
                    return this.defaultMessage;
                }
            }
            this.defaultMessage = NotificationConstants.DEFAULT_MESSAGE;
        }
        return this.defaultMessage;
    }

    /**
     * set read.
     *
     * @param read read
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * is read.
     *
     * @return read
     */
    public boolean isRead() {
        return read;
    }

    /**
     * get notification long name.
     *
     * @return notification long name
     */
    public String getNotificationLongName() {
        return notificationLongName;
    }

    /**
     * set notification long name.
     *
     * @param notificationLongName notification long name
     */
    public void setNotificationLongName(String notificationLongName) {
        this.notificationLongName = notificationLongName;
    }


    /**
     * get notification id.
     *
     * @return notification id
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * set notification id.
     *
     * @param notificationId notification id
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * is deleted.
     *
     * @return deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * set deleted.
     *
     * @param deleted deleted
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * get campaign id.
     *
     * @return campaign id
     */
    public String getCampaignId() {
        return campaignId;
    }

    /**
     * set campaign id.
     *
     * @param campaignId campaign id
     */
    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }


    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AlertsHistoryInfo [id=");
        builder.append(id);
        builder.append(", pdid=");
        builder.append(pdid);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", alertType=");
        builder.append(alertType);
        builder.append(", timestamp=");
        builder.append(timestamp);
        builder.append(", payload=");
        builder.append(payload);
        builder.append(", createDts=");
        builder.append(createDts);
        builder.append(", channelResponses=");
        builder.append(channelResponses);
        builder.append(", defaultMessage=");
        builder.append(defaultMessage);
        builder.append(", alertMessage=");
        builder.append(alertMessage);
        builder.append(", read=");
        builder.append(read);
        builder.append(", group=");
        builder.append(group);
        builder.append(", notificationLongName=");
        builder.append(notificationLongName);
        builder.append(", notificationId=");
        builder.append(notificationId);
        builder.append(", deleted=");
        builder.append(deleted);
        builder.append(", campaignId=");
        builder.append(campaignId);
        builder.append(", processingStatus=");
        builder.append(processingStatus);
        builder.append(", skippedChannels=");
        builder.append(skippedChannels);
        builder.append(", retryRecordList=");
        builder.append(retryRecordList);
        builder.append(", statusHistoryRecordList=");
        builder.append(statusHistoryRecordList);
        builder.append("]");
        return builder.toString();
    }

    /**
     * get status history record list.
     *
     * @return status history record list
     */
    public List<StatusHistoryRecord> getStatusHistoryRecordList() {
        return statusHistoryRecordList;
    }

    /**
     * set status history record list.
     *
     * @param statusHistoryRecordList status history record list
     */
    public void setStatusHistoryRecordList(List<StatusHistoryRecord> statusHistoryRecordList) {
        this.statusHistoryRecordList = statusHistoryRecordList;
    }

    /**
     * add status.
     *
     * @param status status
     */
    public void addStatus(Status status) {
        statusHistoryRecordList.add(new StatusHistoryRecord(status));
    }

    /**
     * add status.
     *
     * @param status status
     * @param correlationId correlation id
     */
    public void addStatus(Status status, String correlationId) {
        statusHistoryRecordList.add(new StatusHistoryRecord(status, correlationId));
    }

    /**
     * get retry record list.
     *
     * @return retry record list
     */
    public List<RetryRecord> getRetryRecordList() {
        return retryRecordList;
    }

    /**
     * set retry record list.
     *
     * @param retryRecordList retry record list
     */
    public void setRetryRecordList(List<RetryRecord> retryRecordList) {
        this.retryRecordList = retryRecordList;
    }

    /**
     * alert processing status.
     */
    public enum Status {
        /**
         * SCHEDULE_REQUESTED.
         */
        SCHEDULE_REQUESTED,
        /**
         * SCHEDULED.
         */
        SCHEDULED,
        /**
         * CANCELED.
         */
        CANCELED,
        /**
         * FAILED.
         */
        FAILED,
        /**
         * DONE.
         */
        DONE,
        /**
         * READY.
         */
        READY,
        /**
         * STOPPED_BY_CONFIG.
         */
        STOPPED_BY_CONFIG,
        /**
         * RETRY_REQUESTED.
         */
        RETRY_REQUESTED,
        /**
         * RETRY_SCHEDULED.
         */
        RETRY_SCHEDULED;
        private static Map<String, Status> namesMap = new HashMap<>(NotificationConstants.ALERT_STATUS_INIT_SIZE);

        static {
            namesMap.put("SCHEDULED", SCHEDULED);
            namesMap.put("SCHEDULE_REQUESTED", SCHEDULE_REQUESTED);
            namesMap.put("CANCELED", CANCELED);
            namesMap.put("FAILED", FAILED);
            namesMap.put("DONE", DONE);
            namesMap.put("READY", READY);
            namesMap.put("STOPPED_BY_CONFIG", STOPPED_BY_CONFIG);
            namesMap.put("RETRY_REQUESTED", RETRY_REQUESTED);
            namesMap.put("RETRY_SCHEDULED", RETRY_SCHEDULED);
        }

        /**
         * get status.
         *
         * @param value value
         * @return status
         */
        @JsonCreator
        public static Status forValue(String value) {
            return namesMap.get(StringUtils.upperCase(value));
        }

        /**
         * get status value.
         *
         * @return string.
         */
        @JsonValue
        public String toValue() {
            for (Map.Entry<String, Status> entry : namesMap.entrySet()) {
                if (entry.getValue() == this) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }

    /**
     * get current status.
     *
     * @return status history record.
     */
    public Optional<StatusHistoryRecord> currentStatus() {
        return Optional.ofNullable(getStatusHistoryRecordList())
                .flatMap(list -> list.stream().min(LARGEST_DATE_FIRST_COMPARATOR));
    }

    @Transient
    private static final Comparator<StatusHistoryRecord> LARGEST_DATE_FIRST_COMPARATOR =
            (status1, status2) -> status2.getTime().compareTo(status1.getTime());

}