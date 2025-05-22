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

import java.util.List;
import java.util.Map;

/**
 * VehicleMessagePublishData class.
 */
@EventMapping(id = EventID.VEHICLE_MESSAGE_PUBLISH, version = Version.V1_0)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
// @Converters(JsonObjectConverter.class)
public class VehicleMessagePublishData extends AbstractEventData {

    private static final long serialVersionUID = -4269499686968357997L;

    private int vehicleMessageID;
    private String vin;
    private String messageTemplate;
    private String serviceMessageEventID;
    private List<LanguageString> message;
    private Map<String, String> messageParameters;
    private MessageType messageType;
    private List<String> buttonActions;
    private String altPhoneNumber;
    private String callType;
    private int priority;
    private Map<String, Object> additionalData;

    private String userId;
    private String campaignId;
    private String notificationId;
    private String campaignDate;
    private String harmanId;
    private String fileName;
    private String countryCode;

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
     * MessageType enum.
     */
    public enum MessageType {
        RECALL_NOTICE(NotificationConstants.RECALL_NOTICE),
        SERVICE_NOTICE(NotificationConstants.SERVICE_NOTICE),
        PC_NOTIFICATION(NotificationConstants.PC_NOTIFICATION),
        ENROLLMENT(NotificationConstants.ENROLLMENT),
        SUBSCRIPTION_NOTICE(NotificationConstants.SUBSCRIPTION_NOTICE),
        MARKETING(NotificationConstants.MARKETING),
        GENERAL(NotificationConstants.GENERAL),
        CUSTOM_EXTENSION(NotificationConstants.CUSTOM_EXTENSION);

        private String value;

        MessageType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    private MessageDetailType messageDetailType;

    /**
     * MessageDetailType enum.
     */
    public enum MessageDetailType {
        RECALL_ID(NotificationConstants.RECALL_ID),
        RECALL_COMPONENT(NotificationConstants.RECALL_COMPONENT),
        RECALL_SUMMARY(NotificationConstants.RECALL_SUMMARY),
        RECALL_CONSEQUENCE(NotificationConstants.RECALL_CONSEQUENCE),
        OWNER_NEXT_STEPS(NotificationConstants.OWNER_NEXT_STEPS),
        SERVICE_DUE_DATE(NotificationConstants.SERVICE_DUE_DATE),
        SERVICE_SUMMARY(NotificationConstants.SERVICE_SUMMARY),
        SUBSCRIPTION_TO_EXPIRE(NotificationConstants.SUBSCRIPTION_TO_EXPIRE),
        SUBSCRIPTION_EXPIRED(NotificationConstants.SUBSCRIPTION_EXPIRED),
        OTHER(NotificationConstants.OTHER),
        SUBSCRIPTION_END_DATE(NotificationConstants.SUBSCRIPTION_END_DATE),
        SUBSCRIPTION_TO_RENEW(NotificationConstants.SUBSCRIPTION_TO_RENEW),
        SUBSCRIPTION_DAYS_TO_EXPIRE(NotificationConstants.SUBSCRIPTION_DAYS_TO_EXPIRE),
        CUSTOM_EXTENSION(NotificationConstants.CUSTOM_EXTENSION);

        private String value;

        MessageDetailType(String value) {
            this.value = value;
        }

        /**
         * Getter for Value.
         *
         * @return value
         */
        public String getValue() {
            return value;
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
     * Getter for Vin.
     *
     * @return vin
     */
    public String getVin() {
        return vin;
    }

    /**
     * Setter for Vin.
     *
     * @param vin the new value
     */
    public void setVin(String vin) {
        this.vin = vin;
    }

    /**
     * Getter for MessageTemplate.
     *
     * @return messagetemplate
     */
    public String getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * Setter for MessageTemplate.
     *
     * @param messageTemplate the new value
     */
    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /**
     * Getter for ServiceMessageEventID.
     *
     * @return servicemessageeventid
     */
    public String getServiceMessageEventID() {
        return serviceMessageEventID;
    }

    /**
     * Setter for ServiceMessageEventID.
     *
     * @param serviceMessageEventID the new value
     */
    public void setServiceMessageEventID(String serviceMessageEventID) {
        this.serviceMessageEventID = serviceMessageEventID;
    }

    /**
     * Getter for Message.
     *
     * @return message
     */
    public List<LanguageString> getMessage() {
        return message;
    }

    /**
     * Setter for Message.
     *
     * @param message the new value
     */
    public void setMessage(List<LanguageString> message) {
        this.message = message;
    }

    /**
     * Getter for MessageParameters.
     *
     * @return messageparameters
     */
    public Map<String, String> getMessageParameters() {
        return messageParameters;
    }

    /**
     * Setter for MessageParameters.
     *
     * @param messageParameters the new value
     */
    public void setMessageParameters(Map<String, String> messageParameters) {
        this.messageParameters = messageParameters;
    }

    /**
     * Getter for MessageType.
     *
     * @return messagetype
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Setter for MessageType.
     *
     * @param messageType the new value
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Getter for MessageDetailType.
     *
     * @return messagedetailtype
     */
    public MessageDetailType getMessageDetailType() {
        return messageDetailType;
    }

    /**
     * Setter for MessageDetailType.
     *
     * @param messageDetailType the new value
     */
    public void setMessageDetailType(MessageDetailType messageDetailType) {
        this.messageDetailType = messageDetailType;
    }

    /**
     * Getter for UserId.
     *
     * @return userid
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter for UserId.
     *
     * @param userId the new value
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
     * Getter for ButtonActions.
     *
     * @return buttonactions
     */
    public List<String> getButtonActions() {
        return buttonActions;
    }

    /**
     * Setter for ButtonActions.
     *
     * @param buttonActions the new value
     */
    public void setButtonActions(List<String> buttonActions) {
        this.buttonActions = buttonActions;
    }

    /**
     * Getter for AltPhoneNumber.
     *
     * @return altphonenumber
     */
    public String getAltPhoneNumber() {
        return altPhoneNumber;
    }

    /**
     * Setter for AltPhoneNumber.
     *
     * @param altPhoneNumber the new value
     */
    public void setAltPhoneNumber(String altPhoneNumber) {
        this.altPhoneNumber = altPhoneNumber;
    }

    /**
     * Getter for CallType.
     *
     * @return calltype
     */
    public String getCallType() {
        return callType;
    }

    /**
     * Setter for CallType.
     *
     * @param callType the new value
     */
    public void setCallType(String callType) {
        this.callType = callType;
    }

    /**
     * Getter for Priority.
     *
     * @return priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Setter for Priority.
     *
     * @param priority the new value
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    /**
     * Setter for AdditionalData.
     *
     * @param additionalData the new value
     */
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    /**
     * To String.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "VehicleMessagePublishData [vehicleMessageId=" + vehicleMessageID + ", vin=" + vin
            + ", messageTemplate=" + messageTemplate + ", serviceMessageEventID=" + serviceMessageEventID
            + ", notificationId=" + notificationId + ", message=" + message
            + ", messageParameters=" + messageParameters + ", messageType=" + messageType
            + ", messageDetailType=" + messageDetailType + ", buttonActions="
            + buttonActions + ", altPhoneNumber=" + altPhoneNumber
            + ", callType=" + callType + ", priority=" + priority
            + ", additionalData=" + additionalData + "]";
    }

}
