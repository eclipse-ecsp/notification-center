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
 * VehicleMessageDispositionPublishData class.
 */
@EventMapping(id = EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH, version = Version.V1_0)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
// @Converters(JsonObjectConverter.class)
public class VehicleMessageDispositionPublishData extends AbstractEventData {

    private static final long serialVersionUID = -4954452081861140037L;


    private int vehicleMessageID;
    private long messageDisplayTimestamp;
    private long messageHMIDispositionEventTimestamp;
    private String failureDiagnostic;
    private String campaignDate;
    private String campaignId;
    private String countryCode;
    private String notificationId;

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

    private String fileName;
    private String harmanId;

    private MessageDispositionEnum disposition;

    /**
     * MessageDispositionEnum class.
     */
    public enum MessageDispositionEnum {
        MESSAGE_CONFIRMED_BY_OPERATOR(NotificationConstants.MESSAGE_CONFIRMED_BY_OPERATOR),
        MESSAGE_DISPLAY_CANCELLED(NotificationConstants.MESSAGE_DISPLAY_CANCELLED),
        MESSAGE_DISPLAY_CONFIRMED_CALL(NotificationConstants.MESSAGE_DISPLAY_CONFIRMED_CALL),
        MESSAGE_VIN_NOT_VALID(NotificationConstants.MESSAGE_VIN_NOT_VALID),
        MESSAGE_TIMED_OUT(NotificationConstants.MESSAGE_TIMED_OUT),
        MESSAGE_LANGUAGES_NOT_SUPPORTED(NotificationConstants.MESSAGE_LANGUAGES_NOT_SUPPORTED),
        MESSAGE_DISPLAY_FAILED(NotificationConstants.MESSAGE_DISPLAY_FAILED),
        MESSAGE_AUTO_DELETE(NotificationConstants.MESSAGE_AUTO_DELETE),
        MESSAGE_DELETE(NotificationConstants.MESSAGE_DELETE),
        CUSTOM_EXTENSION(NotificationConstants.CUSTOM_EXTENSION);

        private String value;

        MessageDispositionEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
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
     * Getter for MessageDisplayTimestamp.
     *
     * @return messagedisplaytimestamp
     */
    public long getMessageDisplayTimestamp() {
        return messageDisplayTimestamp;
    }

    /**
     * Setter for MessageDisplayTimestamp.
     *
     * @param messageDisplayTimestamp the new value
     */
    public void setMessageDisplayTimestamp(long messageDisplayTimestamp) {
        this.messageDisplayTimestamp = messageDisplayTimestamp;
    }

    /**
     * Getter for MessageHMIDispositionEventTimestamp.
     *
     * @return messagehmidispositioneventtimestamp
     */
    public long getMessageHMIDispositionEventTimestamp() {
        return messageHMIDispositionEventTimestamp;
    }

    /**
     * Setter for MessageHMIDispositionEventTimestamp.
     *
     * @param messageHMIDispositionEventTimestamp the new value
     */
    public void setMessageHMIDispositionEventTimestamp(long messageHMIDispositionEventTimestamp) {
        this.messageHMIDispositionEventTimestamp = messageHMIDispositionEventTimestamp;
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
     * Getter for Disposition.
     *
     * @return disposition
     */
    public MessageDispositionEnum getDisposition() {
        return disposition;
    }

    /**
     * Setter for Disposition.
     *
     * @param disposition the new value
     */
    public void setDisposition(MessageDispositionEnum disposition) {
        this.disposition = disposition;
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
     * Equals method.
     *
     * @param obj Object
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * HashCode method.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * To String.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "VehicleMessageDispositionPublishData [vehicleMessageId="
            + vehicleMessageID + ", messageDisplayTimestamp="
            + messageDisplayTimestamp + ", messageHMIDispositionEventTimestamp=" + messageHMIDispositionEventTimestamp
            + ", failureDiagnostic=" + failureDiagnostic + ", campaignDate=" + campaignDate
            + ", campaignId=" + campaignId + ", countryCode=" + countryCode + ", fileName=" + fileName
            + ", harmanId=" + harmanId + ", disposition=" + disposition + "]";
    }
}
