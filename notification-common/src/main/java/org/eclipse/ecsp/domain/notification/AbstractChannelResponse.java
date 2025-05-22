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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.json.JSONObject;

/**
 * AbstractChannelResponse class.
 */
@Entity
public abstract class AbstractChannelResponse implements ChannelResponse {

    @JsonProperty(value = "alertEventData")
    private AlertEventData alertEventData;

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @JsonProperty(value = "userID")
    private String userID;

    @JsonProperty(value = "alertsObject")
    private AlertsInfo alertObject;

    @JsonProperty(value = "pdid")
    private String pdid;

    @JsonProperty(value = "processedTime")
    String processedTime;

    @JsonProperty(value = "eventData")
    Object actualEvent;

    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "errorCode")
    private NotificationErrorCode errorCode;

    @JsonProperty(value = "template")
    private BaseTemplate template;

    @JsonProperty(value = "provider")
    private String provider;

    @JsonProperty(value = "destination")
    private String destination;

    private ChannelType channelType;

    /**
     * AbstractChannelResponse constructor.
     *
     * @param userId    param
     * @param pdid      param
     * @param eventData param
     */
    public AbstractChannelResponse(String userId, String pdid, String eventData) {
        super();
        /*
         * Add the userid, processed time and event data
         */
        this.processedTime = String.valueOf(System.currentTimeMillis());
        this.userID = userId;
        this.pdid = pdid;
        if (null != eventData) {
            this.actualEvent = JsonUtils.getJsonAsMap(eventData);
        }
    }

    /**
     * Get AlertEventData.
     *
     * @return alertEventData
     */
    @Override
    public AlertEventData getAlertData() {
        return alertEventData;
    }

    /**
     * Set AlertEventData.
     *
     * @param alertData alertData
     */
    @Override
    public void setAlertData(AlertEventData alertData) {
        this.alertEventData = alertData;
    }

    /**
     * Set userID.
     *
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @Override
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Get userID.
     *
     * @return userID
     */
    @Override
    public String getUserID() {
        return this.userID;
    }

    /**
     * Get getAlertObject.
     *
     * @return AlertsInfo
     */
    @Override
    public AlertsInfo getAlertObject() {
        return this.alertObject;
    }

    /**
     * Set AlertObject.
     *
     * @param alertsObject alertsObject
     */
    @Override
    public void setAlertObject(AlertsInfo alertsObject) {
        this.alertObject = alertsObject;
    }

    /**
     * Get pdid.
     *
     * @return pdid
     */
    @Override
    public String getPdid() {
        return this.pdid;
    }

    /**
     * Set pdid.
     *
     * @param pdid pdid
     */
    @Override
    public void setPdid(String pdid) {
        this.pdid = pdid;

    }

    /**
     * getProcessedTime.
     *
     * @return getProcessedTime
     */
    @Override
    public String getProcessedTime() {
        return this.processedTime;
    }

    /**
     * setProcessedTime.
     *
     * @param processedTime processedTime
     */
    @Override
    public void setProcessedTime(String processedTime) {
        this.processedTime = processedTime;
    }

    /**
     * Get actualEvent.
     *
     * @return actualEvent
     */
    @Override
    public Object getActualEvent() {
        return actualEvent;
    }

    /**
     * Set actualEvent.
     *
     * @param actualEvent actualEvent
     */
    @Override
    public void setActualEvent(Object actualEvent) {
        this.actualEvent = actualEvent;
    }

    /**
     * Set status.
     *
     * @param status status
     */
    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get ErrorCode.
     *
     * @return errorCode
     */
    @Override
    public NotificationErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Set ErrorCode.
     *
     * @param errorCode errorCode
     */
    @Override
    public void setErrorCode(NotificationErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Get Status.
     *
     * @return status
     */
    @Override
    public String getStatus() {
        return status;
    }

    /*
     * used in api.Added from saas-api
     */
    @Override
    public String getDefaultMessage() {
        if (this.alertEventData != null) {
            JSONObject jsonObj = new JSONObject(alertEventData);
            return jsonObj.getString("defaultMessage");
        }
        return null;
    }

    /**
     * Get Template.
     *
     * @return template
     */
    @Override
    public BaseTemplate getTemplate() {
        return template;
    }

    /**
     * Set Template.
     *
     * @param template template
     */
    public void setTemplate(BaseTemplate template) {
        this.template = template;
    }

    /**
     * Set ChannelType.
     *
     * @param channelType channelType
     */
    @Override
    @JsonIgnore
    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * Get ChannelType.
     *
     * @return ChannelType
     */
    @Override
    @JsonIgnore
    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * Set Destination.
     *
     * @param destination destination
     */
    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Get Destination.
     *
     * @return destination
     */
    @Override
    public String getDestination() {
        return destination;
    }

    /**
     * toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "AbstractChannelResponse [alertData=" + alertEventData + ", userID=" + userID + ", alertObject="
            + alertObject + ", pdid=" + pdid
            + ", processedTime=" + processedTime + ", actualEvent=" + actualEvent + "]";
    }

}
