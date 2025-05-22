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
import org.eclipse.ecsp.entities.AbstractEventData;

/**
 * IVMFeedbackData_V1 eventdata for IvmFeedback event.
 */
@SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:TypeName"})
@EventMapping(id = EventID.IVM_FEEDBACK, version = Version.V1_0)
public class IVMFeedbackData_V1 extends AbstractEventData {

    private static final long serialVersionUID = -6636527245123014250L;
    private String campaignDate;
    private String campaignId;
    private String countryCode;
    private String notificationId;
    private String fileName;
    private String harmanId;
    private String status;
    private NotificationErrorCode errorCode;
    private String errorDetail;
    private int vehicleMessageID;

    /**
     * This method is a getter for status.
     *
     * @return String
     */
    public String getStatus() {
        return status;
    }

    /**
     * This method is a setter for status.
     *
     * @param status String
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * This method is a getter for errorCode.
     *
     * @return NotificationErrorCode
     */
    public NotificationErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * This method is a setter for errorCode.
     *
     * @param errorCode NotificationErrorCode
     */
    public void setErrorCode(NotificationErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * This method is a getter for campaignDate.
     *
     * @return String
     */
    public String getCampaignDate() {
        return campaignDate;
    }

    /**
     * This method is a setter for campaignDate.
     *
     * @param campaignDate String
     */
    public void setCampaignDate(String campaignDate) {
        this.campaignDate = campaignDate;
    }

    /**
     * This method is a getter for campaignId.
     *
     * @return String
     */
    public String getCampaignId() {
        return campaignId;
    }

    /**
     * This method is a setter for campaignId.
     *
     * @param campaignId String
     */
    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    /**
     * This method is a getter for countryCode.
     *
     * @return String
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * This method is a setter for countryCode.
     *
     * @param countryCode String
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * This method is a getter for notificationId.
     *
     * @return String
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * This method is a setter for notificationId.
     *
     * @param notificationId String
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * This method is a getter for fileName.
     *
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * This method is a setter for fileName.
     *
     * @param fileName String
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * This method is a getter for harmanId.
     *
     * @return String
     */
    public String getHarmanId() {
        return harmanId;
    }

    /**
     * This method is a setter for harmanId.
     *
     * @param harmanId String
     */
    public void setHarmanId(String harmanId) {
        this.harmanId = harmanId;
    }

    /**
     * This method is a getter for errorDetail.
     *
     * @return String
     */
    public String getErrorDetail() {
        return errorDetail;
    }

    /**
     * This method is a setter for errorDetail.
     *
     * @param errorDetail String
     */
    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    /**
     * This method is a getter for vehicleMessageId.
     *
     * @return int
     */
    public int getVehicleMessageID() {
        return vehicleMessageID;
    }

    /**
     * This method is a setter for vehicleMessageId.
     *
     * @param vehicleMessageID int
     */
    public void setVehicleMessageID(int vehicleMessageID) {
        this.vehicleMessageID = vehicleMessageID;
    }

    /**
     * This method is a toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "IVMFeedbackData_V1 [campaignDate=" + campaignDate + ", campaignId="
            + campaignId + ", countryCode=" + countryCode
            + ", notificationId=" + notificationId + ", fileName=" + fileName
            + ", harmanId=" + harmanId + ", status=" + status
            + ", errorCode=" + errorCode + ", errorDetail=" + errorDetail
            + ", vehicleMessageId=" + vehicleMessageID + "]";
    }

}
