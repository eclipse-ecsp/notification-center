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

import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData.MessageDispositionEnum;
import org.junit.Assert;
import org.junit.Test;

/**
 * VehicleMessageDispositionPublishDataTest class.
 */
public class VehicleMessageDispositionPublishDataTest {
    VehicleMessageDispositionPublishData vehicleMessageDispositionPublishData =
        new VehicleMessageDispositionPublishData();

    @Test
    public void testgetCampaignId() {
        vehicleMessageDispositionPublishData.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        vehicleMessageDispositionPublishData.setCampaignId("dasdas");
    }

    @Test
    public void testgetCampaignDate() {
        vehicleMessageDispositionPublishData.getCampaignDate();
    }

    @Test
    public void testsetCampaignDate() {
        vehicleMessageDispositionPublishData.setCampaignDate("");
    }

    @Test
    public void testgetCountryCode() {
        vehicleMessageDispositionPublishData.getCountryCode();
    }

    @Test
    public void testsetCountryCode() {
        vehicleMessageDispositionPublishData.setCountryCode("");
    }

    @Test
    public void testgetFileName() {
        vehicleMessageDispositionPublishData.getFileName();
    }

    @Test
    public void testsetFileName() {
        vehicleMessageDispositionPublishData.setFileName("test123");
    }

    @Test
    public void testgetHarmanId() {
        vehicleMessageDispositionPublishData.getHarmanId();
    }

    @Test
    public void testsetHarmanId() {
        vehicleMessageDispositionPublishData.setHarmanId("");
    }

    @Test
    public void testgetVehicleMessageId() {
        vehicleMessageDispositionPublishData.getVehicleMessageID();
    }

    @Test
    public void testsetVehicleMessageId() {
        vehicleMessageDispositionPublishData.setVehicleMessageID(0);
    }

    @Test
    public void testgetMessageDisplayTimestamp() {
        vehicleMessageDispositionPublishData.getMessageDisplayTimestamp();
    }

    @Test
    public void testsetMessageDisplayTimestamp() {
        vehicleMessageDispositionPublishData.setMessageDisplayTimestamp(0);
    }

    @Test
    public void testgetMessageHmiDispositionEventTimestamp() {
        vehicleMessageDispositionPublishData.getMessageHMIDispositionEventTimestamp();
    }

    @Test
    public void testsetMessageHmiDispositionEventTimestamp() {
        vehicleMessageDispositionPublishData.setMessageHMIDispositionEventTimestamp(0);
    }

    @Test
    public void testgetFailureDiagnostic() {
        vehicleMessageDispositionPublishData.getFailureDiagnostic();
    }

    @Test
    public void testsetFailureDiagnostic() {
        vehicleMessageDispositionPublishData.setFailureDiagnostic("");
    }

    @Test
    public void testgetDisposition() {
        vehicleMessageDispositionPublishData.getDisposition();
    }

    @Test
    public void testsetDisposition() {
        vehicleMessageDispositionPublishData.setDisposition(null);
    }

    @Test
    public void testgetNotificationId() {
        vehicleMessageDispositionPublishData.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        vehicleMessageDispositionPublishData.setNotificationId("test123");
    }

    @Test
    public void testequals() {
        vehicleMessageDispositionPublishData.equals(vehicleMessageDispositionPublishData);
    }

    @Test
    public void testhashCode() {
        vehicleMessageDispositionPublishData.hashCode();
    }

    @Test
    public void testtoString() {
        vehicleMessageDispositionPublishData.toString();
    }

    @Test
    public void testEventId() {

        Assert.assertEquals(MessageDispositionEnum.MESSAGE_CONFIRMED_BY_OPERATOR,
            MessageDispositionEnum.MESSAGE_CONFIRMED_BY_OPERATOR);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_DISPLAY_CANCELLED,
            MessageDispositionEnum.MESSAGE_DISPLAY_CANCELLED);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_DISPLAY_CONFIRMED_CALL,
            MessageDispositionEnum.MESSAGE_DISPLAY_CONFIRMED_CALL);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_VIN_NOT_VALID, MessageDispositionEnum.MESSAGE_VIN_NOT_VALID);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_TIMED_OUT, MessageDispositionEnum.MESSAGE_TIMED_OUT);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_LANGUAGES_NOT_SUPPORTED,
            MessageDispositionEnum.MESSAGE_LANGUAGES_NOT_SUPPORTED);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_DISPLAY_FAILED,
            MessageDispositionEnum.MESSAGE_DISPLAY_FAILED);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_AUTO_DELETE, MessageDispositionEnum.MESSAGE_AUTO_DELETE);
        Assert.assertEquals(MessageDispositionEnum.MESSAGE_DELETE, MessageDispositionEnum.MESSAGE_DELETE);
        Assert.assertEquals(MessageDispositionEnum.CUSTOM_EXTENSION, MessageDispositionEnum.CUSTOM_EXTENSION);


    }
}
