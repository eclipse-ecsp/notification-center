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

package org.eclipse.ecsp.notification.aws.ses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AmazonBounceNotification class.
 */
public class AmazonBounceNotification {
    @JsonProperty("Type")
    private String type;

    @JsonProperty("MessageId")
    private String messageId;

    @JsonProperty("TopicArn")
    private String topicArn;

    @JsonProperty("Message")
    private String bounceMessage;

    /**
     * BounceMessage.
     */
    public String getType() {
        return type;
    }

    /**
     * BounceMessage.
     *
     * @param type the given type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * MessageId.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * MessageId.
     *
     * @param messageId the given messageId
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * TopicArn.
     */
    public String getTopicArn() {
        return topicArn;
    }

    /**
     * TopicArn.
     *
     * @param topicArn the given topicArn
     */
    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    /**
     * BounceMessage.
     */
    public String getBounceMessage() {
        return bounceMessage;
    }

    /**
     * BounceMessage.
     *
     * @param bounceMessage the given bounceMessage
     */
    public void setBounceMessage(String bounceMessage) {
        this.bounceMessage = bounceMessage;
    }

    /**
     * BounceMessage class.
     */
    public static class BounceMessage {
        private String notificationType;
        private Bounce bounce;

        /**
         * NotificationType.
         */
        public String getNotificationType() {
            return notificationType;
        }

        /**
         * NotificationType.
         *
         * @param notificationType the given notificationType
         */
        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        /**
         * Bounce.
         */
        public Bounce getBounce() {
            return bounce;
        }

        /**
         * Bounce.
         *
         * @param bounce the given bounce
         */
        public void setBounce(Bounce bounce) {
            this.bounce = bounce;
        }

    }

    /**
     * Bounce static class.
     */
    public static class Bounce {
        private String bounceType;
        private String bounceSubType;
        private List<BouncedRecipient> bouncedRecipients;

        /**
         * BounceType.
         */
        public String getBounceType() {
            return bounceType;
        }

        /**
         * BounceType.
         *
         * @param bounceType the given bounceType
         */
        public void setBounceType(String bounceType) {
            this.bounceType = bounceType;
        }

        /**
         * BounceSubType.
         */
        public String getBounceSubType() {
            return bounceSubType;
        }

        /**
         * BounceSubType.
         *
         * @param bounceSubType the given bounceSubType
         */
        public void setBounceSubType(String bounceSubType) {
            this.bounceSubType = bounceSubType;
        }

        /**
         * BouncedRecipients.
         */
        public List<BouncedRecipient> getBouncedRecipients() {
            return bouncedRecipients;
        }

        /**
         * BouncedRecipients.
         *
         * @param bouncedRecipients the given bouncedRecipients
         */
        public void setBouncedRecipients(List<BouncedRecipient> bouncedRecipients) {
            this.bouncedRecipients = bouncedRecipients;
        }

    }

    /**
     * BouncedRecipient class.
     */
    public static class BouncedRecipient {
        private String emailAddress;
        private String action;
        private String status;
        private String diagnosticCode;

        /**
         * EmailAddress.
         */
        public String getEmailAddress() {
            return emailAddress;
        }

        /**
         * EmailAddress.
         *
         * @param emailAddress the given emailAddress
         */
        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        /**
         * Action.
         */
        public String getAction() {
            return action;
        }

        /**
         * Action.
         *
         * @param action the given action
         */
        public void setAction(String action) {
            this.action = action;
        }

        /**
         * Status.
         */
        public String getStatus() {
            return status;
        }

        /**
         * Status.
         *
         * @param status the given status
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * DiagnosticCode.
         */
        public String getDiagnosticCode() {
            return diagnosticCode;
        }

        /**
         * DiagnosticCode.
         *
         * @param diagnosticCode the given diagnosticCode
         */
        public void setDiagnosticCode(String diagnosticCode) {
            this.diagnosticCode = diagnosticCode;
        }

        /**
         * BouncedRecipient.
         */
        @Override
        public String toString() {
            return "BouncedRecipient [emailAddress=" + emailAddress + ", action=" + action + ", status=" + status
                    +
                    ", diagnosticCode="
                    + diagnosticCode + "]";
        }

    }
}
