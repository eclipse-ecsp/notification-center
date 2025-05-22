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

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.AbstractEventData;

/**
 *  VehicleMessageAckData class.
 */
@EventMapping(id = EventID.VEHICLE_MESSAGE_ACK, version = Version.V1_0)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
// @Converters(JsonObjectConverter.class)
public class VehicleMessageAckData extends AbstractEventData {

    private static final long serialVersionUID = -4758652148042312104L;

    private String failureDiagnostic;
    private long messageReceiptTimestamp;

    private int vehicleMessageID;
    private String campaignDate;
    private String campaignId;
    private String countryCode;
    private String fileName;
    private String harmanId;
    private String notificationId;

    private ResponseEnum status;

    /**
     *  ResponseEnum class.
     */
    public enum ResponseEnum {
        MESSAGE_STAGED_FOR_DISPLAY(NotificationConstants.MESSAGE_STAGED_FOR_DISPLAY),
        MESSAGE_TYPE_NOT_SUPPORTED(NotificationConstants.MESSAGE_TYPE_NOT_SUPPORTED),
        MESSAGE_LANGUAGES_NOT_SUPPORTED(NotificationConstants.MESSAGE_LANGUAGES_NOT_SUPPORTED),
        MESSAGE_STAGING_FAILED(NotificationConstants.MESSAGE_STAGING_FAILED),
        MESSAGE_DESTINATION_TEMPORARILY_NOT_AVAILABLE(
            NotificationConstants.MESSAGE_DESTINATION_TEMPORARILY_NOT_AVAILABLE),
        MESSAGE_VIN_NOT_VALID(NotificationConstants.MESSAGE_VIN_NOT_VALID),
        MESSAGE_AUTO_DELETE(NotificationConstants.MESSAGE_AUTO_DELETE),
        MESSAGE_DELETE(NotificationConstants.MESSAGE_DELETE),
        CUSTOM_EXTENSION(NotificationConstants.CUSTOM_EXTENSION);

        private String value;

        ResponseEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    /**
     * Getter for FailureDiagnostic.
     *
     * @return failurediagnostic
     */
    public String getFailureDiagnostic() {
        return failureDiagnostic;
    }

    /**
     * Setter for FailureDiagnostic.
     *
     * @param failureDiagnostic the new value
     */
    public void setFailureDiagnostic(String failureDiagnostic) {
        this.failureDiagnostic = failureDiagnostic;
    }

    /**
     * Getter for MessageReceiptTimestamp.
     *
     * @return messagereceipttimestamp
     */
    public long getMessageReceiptTimestamp() {
        return messageReceiptTimestamp;
    }

    /**
     * Setter for MessageReceiptTimestamp.
     *
     * @param messageReceiptTimestamp the new value
     */
    public void setMessageReceiptTimestamp(long messageReceiptTimestamp) {
        this.messageReceiptTimestamp = messageReceiptTimestamp;
    }

    /**
     * Getter for VehicleMessageID.
     *
     * @return vehiclemessageid
     */
    public int getVehicleMessageID() {
        return vehicleMessageID;
    }

    /**
     * Setter for VehicleMessageID.
     *
     * @param vehicleMessageID the new value
     */
    public void setVehicleMessageID(int vehicleMessageID) {
        this.vehicleMessageID = vehicleMessageID;
    }

    /**
     * Getter for Status.
     *
     * @return status
     */
    public ResponseEnum getStatus() {
        return status;
    }

    /**
     * Setter for Status.
     *
     * @param status the new value
     */
    public void setStatus(ResponseEnum status) {
        this.status = status;
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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "VehicleMessageAckData [failureDiagnostic=" + failureDiagnostic + ", messageReceiptTimestamp="
            + messageReceiptTimestamp + ", vehicleMessageID=" + vehicleMessageID + ", campaignDate=" + campaignDate
            + ", campaignId=" + campaignId + ", countryCode=" + countryCode + ", fileName=" + fileName
            + ", harmanId=" + harmanId + ", status=" + status + "]";
    }

}
