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

import org.eclipse.ecsp.domain.notification.VehicleMessagePublishData.MessageDetailType;
import org.eclipse.ecsp.domain.notification.VehicleMessagePublishData.MessageType;
import org.junit.Assert;
import org.junit.Test;

/**
 * VehicleMessagePublishDataTest class.
 */
public class VehicleMessagePublishDataTest {

    VehicleMessagePublishData vehicleMessagePublishData = new VehicleMessagePublishData();

    @Test
    public void testgetCampaignDate() {
        vehicleMessagePublishData.getCampaignDate();
    }

    @Test
    public void testsetCampaignDate() {
        vehicleMessagePublishData.setCampaignDate(null);
    }

    @Test
    public void testgetHarmanId() {
        vehicleMessagePublishData.getHarmanId();
    }

    @Test
    public void testsetHarmanId() {
        vehicleMessagePublishData.setHarmanId("test");
    }

    @Test
    public void testgetFileName() {
        vehicleMessagePublishData.getFileName();
    }

    @Test
    public void testsetFileName() {
        vehicleMessagePublishData.setFileName("test");
    }

    @Test
    public void testgetCountryCode() {
        vehicleMessagePublishData.getCountryCode();
    }

    @Test
    public void testsetCountryCode() {
        vehicleMessagePublishData.setCountryCode("");
    }

    @Test
    public void testgetVehicleMessageId() {
        vehicleMessagePublishData.getVehicleMessageID();
    }

    @Test
    public void testsetVehicleMessageId() {
        vehicleMessagePublishData.getVehicleMessageID();
    }

    @Test
    public void testgetVin() {
        vehicleMessagePublishData.getVin();
    }

    @Test
    public void testsetVin() {
        vehicleMessagePublishData.setVin(null);
    }

    @Test
    public void testgetMessageTemplate() {
        vehicleMessagePublishData.getMessageTemplate();
    }

    @Test
    public void testsetMessageTemplate() {
        vehicleMessagePublishData.setMessageTemplate(null);
    }

    @Test
    public void testgetServiceMessageEventId() {
        vehicleMessagePublishData.getServiceMessageEventID();
    }

    @Test
    public void testsetServiceMessageEventId() {
        vehicleMessagePublishData.setServiceMessageEventID(null);
    }

    @Test
    public void testgetMessage() {
        vehicleMessagePublishData.getMessage();
    }

    @Test
    public void testsetMessage() {
        vehicleMessagePublishData.setMessage(null);
    }

    @Test
    public void testgetMessageParameters() {
        vehicleMessagePublishData.getMessageParameters();
    }

    @Test
    public void testsetMessageParameters() {
        vehicleMessagePublishData.setMessageParameters(null);
    }

    @Test
    public void testgetMessageType() {
        vehicleMessagePublishData.getMessageType();
    }

    @Test
    public void testsetMessageType() {
        vehicleMessagePublishData.setMessageType(null);
    }

    @Test
    public void testgetMessageDetailType() {
        vehicleMessagePublishData.getMessageDetailType();
    }

    @Test

    public void testsetMessageDetailType() {
        vehicleMessagePublishData.setMessageDetailType(null);
    }

    @Test
    public void testgetUserId() {
        vehicleMessagePublishData.getUserId();
    }

    @Test
    public void testsetUserId() {
        vehicleMessagePublishData.setUserId(null);
    }

    @Test
    public void testgetCampaignId() {
        vehicleMessagePublishData.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        vehicleMessagePublishData.setCampaignId(null);
    }

    @Test
    public void testgetNotificationId() {
        vehicleMessagePublishData.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        vehicleMessagePublishData.setNotificationId(null);
    }

    @Test
    public void testgetButtonActions() {
        vehicleMessagePublishData.getButtonActions();
    }

    @Test
    public void testsetButtonActions() {
        vehicleMessagePublishData.setButtonActions(null);
    }

    @Test
    public void testgetAltPhoneNumber() {
        vehicleMessagePublishData.getAltPhoneNumber();
    }

    @Test
    public void testsetAltPhoneNumber() {
        vehicleMessagePublishData.setAltPhoneNumber(null);
    }

    @Test
    public void testgetCallType() {
        vehicleMessagePublishData.getCallType();
    }

    @Test
    public void testsetCallType() {
        vehicleMessagePublishData.setCallType(null);
    }

    @Test
    public void testgetPriority() {
        vehicleMessagePublishData.getPriority();
    }

    @Test
    public void testsetPriority() {
        vehicleMessagePublishData.setPriority(0);
    }

    @Test
    public void testgetAdditionalData() {
        vehicleMessagePublishData.getAdditionalData();
    }

    @Test
    public void testsetAdditionalData() {
        vehicleMessagePublishData.setAdditionalData(null);
    }

    @Test
    public void testtoString() {
        vehicleMessagePublishData.toString();
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(MessageType.RECALL_NOTICE, MessageType.RECALL_NOTICE);
        Assert.assertEquals(MessageType.SERVICE_NOTICE, MessageType.SERVICE_NOTICE);
        Assert.assertEquals(MessageType.PC_NOTIFICATION, MessageType.PC_NOTIFICATION);
        Assert.assertEquals(MessageType.ENROLLMENT, MessageType.ENROLLMENT);
        Assert.assertEquals(MessageType.SUBSCRIPTION_NOTICE, MessageType.SUBSCRIPTION_NOTICE);
        Assert.assertEquals(MessageType.MARKETING, MessageType.MARKETING);
        Assert.assertEquals(MessageType.GENERAL, MessageType.GENERAL);
        Assert.assertEquals(MessageType.CUSTOM_EXTENSION, MessageType.CUSTOM_EXTENSION);

        Assert.assertEquals(MessageDetailType.RECALL_ID, MessageDetailType.RECALL_ID);
        Assert.assertEquals(MessageDetailType.RECALL_COMPONENT, MessageDetailType.RECALL_COMPONENT);
        Assert.assertEquals(MessageDetailType.RECALL_SUMMARY, MessageDetailType.RECALL_SUMMARY);
        Assert.assertEquals(MessageDetailType.RECALL_CONSEQUENCE, MessageDetailType.RECALL_CONSEQUENCE);
        Assert.assertEquals(MessageDetailType.OWNER_NEXT_STEPS, MessageDetailType.OWNER_NEXT_STEPS);
        Assert.assertEquals(MessageDetailType.SERVICE_DUE_DATE, MessageDetailType.SERVICE_DUE_DATE);
        Assert.assertEquals(MessageDetailType.SERVICE_SUMMARY, MessageDetailType.SERVICE_SUMMARY);
        Assert.assertEquals(MessageDetailType.SUBSCRIPTION_TO_EXPIRE, MessageDetailType.SUBSCRIPTION_TO_EXPIRE);
        Assert.assertEquals(MessageDetailType.SUBSCRIPTION_EXPIRED, MessageDetailType.SUBSCRIPTION_EXPIRED);
        Assert.assertEquals(MessageDetailType.CUSTOM_EXTENSION, MessageDetailType.CUSTOM_EXTENSION);
        Assert.assertEquals(MessageDetailType.OTHER, MessageDetailType.OTHER);
        Assert.assertEquals(MessageDetailType.SUBSCRIPTION_END_DATE, MessageDetailType.SUBSCRIPTION_END_DATE);
        Assert.assertEquals(MessageDetailType.SUBSCRIPTION_TO_RENEW, MessageDetailType.SUBSCRIPTION_TO_RENEW);
        Assert.assertEquals(MessageDetailType.SUBSCRIPTION_DAYS_TO_EXPIRE,
            MessageDetailType.SUBSCRIPTION_DAYS_TO_EXPIRE);
    }
}
