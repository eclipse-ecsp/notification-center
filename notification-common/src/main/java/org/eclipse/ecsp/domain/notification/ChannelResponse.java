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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.morphia.annotations.Entity;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * ChannelResponse interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "provider", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FCMChannelResponse.class, name = NotificationConstants.FCM_PROVIDER),
    @JsonSubTypes.Type(value = SMTPEmailResponse.class, name = NotificationConstants.SMTP_PROVIDER),
    @JsonSubTypes.Type(value = MQTTBrowserResponse.class, name = NotificationConstants.MQTT_PROVIDER),
    @JsonSubTypes.Type(value = ApiPushResponse.class, name = NotificationConstants.API_PUSH_PROVIDER),
    @JsonSubTypes.Type(value = AmazonSNSSMSResponse.class, name = NotificationConstants.AWS_SMS_PROVIDER),
    @JsonSubTypes.Type(value = AmazonSNSPushResponse.class, name = NotificationConstants.AWS_PUSH_PROVIDER),
    @JsonSubTypes.Type(value = AmazonSESResponse.class, name = NotificationConstants.AWS_SES_PROVIDER),
    @JsonSubTypes.Type(value = AmazonPinpointSMSChannelResponse.class,
        name = NotificationConstants.AWS_PINPOINT_SMS_PROVIDER),
    @JsonSubTypes.Type(value = AmazonPinpointEmailChannelResponse.class,
        name = NotificationConstants.AWS_PINPOINT_EMAIL_PROVIDER)
})
public interface ChannelResponse {

    /**
     * Set Alert Data.
     *
     * @param alertData AlertEventData
     */
    public void setAlertData(AlertEventData alertData);

    /**
     * Get Alert Data.
     *
     * @return AlertEventData
     */
    public AlertEventData getAlertData();

    /**
     * Get Alert Object.
     *
     * @return AlertsInfo
     */
    public AlertsInfo getAlertObject();

    /**
     * Set Alert Object.
     *
     * @param alertsObject AlertsInfo
     */
    public void setAlertObject(AlertsInfo alertsObject);


    /**
     * Get User ID.
     *
     * @return String
     */
    public String getUserID();

    /**
     * Set User ID.
     *
     * @param userID String
     */
    public void setUserID(String userID);

    /**
     * Get PDID.
     *
     * @return String
     */
    public String getPdid();

    /**
     * Set PDID.
     *
     * @param pdid String
     */
    public void setPdid(String pdid);

    /**
     * Get Event Data.
     *
     * @return String
     */
    public String getProcessedTime();

    /**
     * Set Event Data.
     *
     * @param processedTime processedTime
     */
    public void setProcessedTime(String processedTime);

    /**
     * Get Event Data.
     *
     * @return String
     */
    public Object getActualEvent();

    /**
     * Set Event Data.
     *
     * @param actualEvent actualEvent
     */
    public void setActualEvent(Object actualEvent);

    /**
     * Get ChannelType.
     *
     * @return ChannelType
     */
    @JsonIgnore
    public ChannelType getChannelType();

    /**
     * Set ChannelType.
     *
     * @param channelType ChannelType
     */
    @JsonIgnore
    public void setChannelType(ChannelType channelType);

    /**
     * Get ErrorCode.
     *
     * @return NotificationErrorCode
     */
    public NotificationErrorCode getErrorCode();

    /**
     * Set ErrorCode.
     *
     * @param errorCode NotificationErrorCode
     */
    public void setErrorCode(NotificationErrorCode errorCode);

    /**
     * Get getStatus.
     *
     * @return String
     */
    public String getStatus();

    /**
     * Set Status.
     *
     * @param status String
     */
    public void setStatus(String status);

    /**
     * Get Default Message.
     *
     * @return String
     */
    public String getDefaultMessage();

    /**
     * Get Template.
     *
     * @return BaseTemplate
     */
    public BaseTemplate getTemplate();

    /**
     * getProvider.
     *
     * @return String
     */
    public String getProvider();

    /**
     * destination.
     *
     * @param destination String
     */
    public void setDestination(String destination);

    /**
     * destination.
     *
     * @return String
     */
    public String getDestination();

}
