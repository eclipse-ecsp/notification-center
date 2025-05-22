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

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * AmazonSNSChannelResponse entity.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity
public abstract class AmazonSNSChannelResponse extends AbstractChannelResponse {

    @JsonProperty(value = "topics")
    private List<TopicsInfo> topics = new ArrayList<>();

    /**
     * AmazonSNSChannelResponse constructor.
     *
     * @param userID userID
     * @param pdid pdid
     * @param eventData eventData
     */
    public AmazonSNSChannelResponse(String userID, String pdid, String eventData) {
        super(userID, pdid, eventData);
    }

    /**
     * Get topics.
     *
     * @return topics
     */
    public List<TopicsInfo> getTopics() {
        return topics;
    }

    /**
     * Set topics.
     *
     * @param topics topics
     */
    public void setTopics(List<TopicsInfo> topics) {
        this.topics = topics;
    }

    /**
     * form the topic and subscription object.
     *
     *  @param topicARN topicArn
     *
     * @param topicCreateTrackingID topicCreateTrackingID
     *
     * @param subscriptionResponse response
     */
    public void setTopicAndSubscriptionsInfo(String topicARN, String topicCreateTrackingID,
                                             List<Triplet<String, String, String>> subscriptionResponse) {

        createTopicsInfo(topicARN, topicCreateTrackingID, subscriptionResponse);

    }

    /**
     * set delete topic info.
     *
     * @param topicARN topicARN
     * @param deleteTopicTrackingID deleteTopicTrackingID
     */
    public void setDeleteTopicsInfo(String topicARN, String deleteTopicTrackingID) {
        createDeleteTopicInfo(topicARN, deleteTopicTrackingID);
    }

    /**
     * set publish info.
     *
     * @param topicARN topicARN
     * @param  publishTrackingID publishTrackingID
     */
    public void setPublishInfo(String topicARN, String publishTrackingID) {
        createPublishInfo(topicARN, publishTrackingID);
    }

    private void createDeleteTopicInfo(String topicARN, String deleteTopicTrackingID) {
        TopicsInfo info = new TopicsInfo();
        info.setTopicArn(topicARN);
        info.setDeleteTrackingID(deleteTopicTrackingID);
        topics.add(info);

    }

    private void createTopicsInfo(String topicARN, String topicCreateTrackingID,
                                  List<Triplet<String, String, String>> subscriptionResponse) {
        TopicsInfo info = new TopicsInfo();
        info.setTopicArn(topicARN);
        info.setTopicCreationID(topicCreateTrackingID);
        info.setSubscriptionInfo(subscriptionResponse);
        topics.add(info);

    }

    private void createPublishInfo(String topicARN, String trackingID) {
        TopicsInfo info = new TopicsInfo();
        info.setTopicArn(topicARN);
        info.setPubishTrackingID(trackingID);
        topics.add(info);
    }

    /**
     * TopicsInfo class.
     */
    @Entity(useDiscriminator = false)
    public static class TopicsInfo {

        @JsonProperty(value = "topicARN")
        private String topicArn;

        @JsonProperty(value = "creationTrackingID")
        private String topicCreationID;

        @JsonProperty(value = "publishTrackingID")
        private String pubishTrackingID;

        @JsonProperty(value = "deleteTrackingID")
        private String deleteTrackingID;

        @JsonProperty(value = "subscriptions")
        private List<SubscriptionInfo> subscriptions;

        public TopicsInfo() {
            subscriptions = new ArrayList<>();

        }

        /**
         * setter subscription info.
         *
         * @param subscriptionResponse response
         */
        public void setSubscriptionInfo(List<Triplet<String, String, String>> subscriptionResponse) {
            /*
             * Iterate over the subscription and create subscrition object one
             * per request
             */
            for (Triplet<String, String, String> subscriptionInfo : subscriptionResponse) {
                String protocolName = subscriptionInfo.getA();
                String protocolValue = subscriptionInfo.getB();
                String subscriptionTrackingID = subscriptionInfo.getC();
                createSubscriptionInfo(protocolName, protocolValue, subscriptionTrackingID);

            }

        }

        private void createSubscriptionInfo(String protocolName, String protocolValue, String subscriptionTrackingID) {
            SubscriptionInfo info = new SubscriptionInfo();
            info.setProtocolName(protocolName);
            info.setProtocolValue(protocolValue);
            info.setSubscriptionTrackingID(subscriptionTrackingID);
            addSubscriptionToList(info);

        }

        private void addSubscriptionToList(SubscriptionInfo info) {
            this.subscriptions.add(info);
        }

        /**
         * Get topicArn.
         *
         * @return topicArn
         */
        public String getTopicArn() {
            return topicArn;
        }

        /**
         * Set topicArn.
         *
         * @param topicArn topicArn
         */
        public void setTopicArn(String topicArn) {
            this.topicArn = topicArn;
        }

        /**
         * Get topicCreationID.
         *
         * @return topicCreationID
         */
        public String getTopicCreationID() {
            return topicCreationID;
        }

        /**
         * Set topicCreationID.
         *
         * @param topicCreationID topicCreationID
         */
        public void setTopicCreationID(String topicCreationID) {
            this.topicCreationID = topicCreationID;
        }

        /**
         * Get subscriptions.
         *
         * @return subscriptions
         */
        public List<SubscriptionInfo> getSubscriptions() {
            return subscriptions;
        }

        /**
         * Set subscriptions.
         *
         * @param subscriptions subscriptions
         */
        public void setSubscriptions(List<SubscriptionInfo> subscriptions) {
            this.subscriptions = subscriptions;
        }

        /**
         * Get pubishTrackingID.
         *
         * @return pubishTrackingID
         */
        public String getPubishTrackingID() {
            return pubishTrackingID;
        }

        /**
         * Set pubishTrackingID.
         *
         * @param pubishTrackingID pubishTrackingID
         */
        public void setPubishTrackingID(String pubishTrackingID) {
            this.pubishTrackingID = pubishTrackingID;
        }

        /**
         * Get deleteTrackingID.
         *
         * @return deleteTrackingID
         */
        public String getDeleteTrackingID() {
            return deleteTrackingID;
        }

        /**
         * Set deleteTrackingID.
         *
         * @param deleteTrackingID deleteTrackingID
         */
        public void setDeleteTrackingID(String deleteTrackingID) {
            this.deleteTrackingID = deleteTrackingID;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TopicsInfo [topicArn=");
            builder.append(topicArn);
            builder.append(", topicCreationID=");
            builder.append(topicCreationID);
            builder.append(", pubishTrackingID=");
            builder.append(pubishTrackingID);
            builder.append(", deleteTrackingID=");
            builder.append(deleteTrackingID);
            builder.append(", subscriptions=");
            builder.append(subscriptions);
            builder.append(", toString()=");
            builder.append(super.toString());
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * SubscriptionInfo static class.
     */
    @Entity(useDiscriminator = false)
    public static class SubscriptionInfo {

        @JsonProperty(value = "subscriptionTrackingID")
        private String subscriptionTrackingID;

        @JsonProperty(value = "protocolName")
        private String protocolName;

        @JsonProperty(value = "endPoint")
        private String protocolValue;

        /**
         * Constructor.
         */
        public SubscriptionInfo() {

        }

        /**
         * Get subscriptionTrackingID.
         *
         * @return subscriptionTrackingID
         */
        public String getSubscriptionTrackingID() {
            return subscriptionTrackingID;
        }

        /**
         * Set subscriptionTrackingID.
         *
         * @param subscriptionTrackingID subscriptionTrackingID
         */
        public void setSubscriptionTrackingID(String subscriptionTrackingID) {
            this.subscriptionTrackingID = subscriptionTrackingID;
        }

        /**
         * Get protocolName.
         *
         * @return protocolName
         */
        public String getProtocolName() {
            return protocolName;
        }

        /**
         * Set protocolName.
         *
         * @param protocolName protocolName
         */
        public void setProtocolName(String protocolName) {
            this.protocolName = protocolName;
        }

        /**
         * Get protocolValue.
         *
         * @return protocolValue
         */
        public String getProtocolValue() {
            return protocolValue;
        }

        /**
         * Set protocolValue.
         *
         * @param protocolValue protocolValue
         */
        public void setProtocolValue(String protocolValue) {
            this.protocolValue = protocolValue;
        }

        @Override
        public String toString() {
            return "SubscriptionInfo [subscriptionTrackingID=" + subscriptionTrackingID
                + ", protocolName=" + protocolName + ", protocolValue=" + protocolValue + "]";
        }

    }

}