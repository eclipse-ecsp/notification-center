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

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;

/**
 * IVMRequest class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(value = NotificationDaoConstants.IVM_REQUEST_COLLECTION_NAME)
@Indexes({@Index(fields = {@Field(NotificationDaoConstants.VEHICLEID_FIELD),
    @Field(NotificationDaoConstants.SESSION_ID_FIELD)}, options = @IndexOptions(name = "IVMRequest_Index_1")),
    @Index(fields = {@Field(NotificationDaoConstants.VEHICLEID_FIELD),
        @Field(NotificationDaoConstants.MESSAGEID_FIELD)}, options = @IndexOptions(name = "IVMRequest_Index_2"))})
public class IVMRequest extends AbstractIgniteEntity {

    @Id
    private String requestId;
    private String vehicleId;
    private String messageId;
    private String sessionId;
    private String campaignDate;
    private String campaignId;
    private String harmanId;
    private String fileName;
    private String countryCode;
    private String notificationId;

    /**
     * Getter for MessageId.
     *
     * @return messageid
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Setter for MessageId.
     *
     * @param messageId the new value
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Getter for RequestId.
     *
     * @return requestid
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter for RequestId.
     *
     * @param requestId the new value
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter for SessionId.
     *
     * @return sessionid
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Setter for SessionId.
     *
     * @param sessionId the new value
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Getter for VehicleId.
     *
     * @return vehicleid
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Setter for VehicleId.
     *
     * @param vehicleId the new value
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Getter for CampaignDate.
     *
     * @return campaigndate
     */
    public String getCampaignDate() {
        return campaignDate;
    }

    /**
     * Setter for CampaignDate.
     *
     * @param campaignDate the new value
     */
    public void setCampaignDate(String campaignDate) {
        this.campaignDate = campaignDate;
    }

    /**
     * Getter for HarmanId.
     *
     * @return harmanid
     */
    public String getHarmanId() {
        return harmanId;
    }

    /**
     * Setter for HarmanId.
     *
     * @param harmanId the new value
     */
    public void setHarmanId(String harmanId) {
        this.harmanId = harmanId;
    }

    /**
     * Getter for CampaignId.
     *
     * @return campaignid
     */
    public String getCampaignId() {
        return campaignId;
    }

    /**
     * Setter for CampaignId.
     *
     * @param campaignId the new value
     */
    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    /**
     * Getter for FileName.
     *
     * @return filename
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter for FileName.
     *
     * @param fileName the new value
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter for CountryCode.
     *
     * @return countrycode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Setter for CountryCode.
     *
     * @param countryCode the new value
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Getter for NotificationId.
     *
     * @return notificationid
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Setter for NotificationId.
     *
     * @param notificationId the new value
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

}
