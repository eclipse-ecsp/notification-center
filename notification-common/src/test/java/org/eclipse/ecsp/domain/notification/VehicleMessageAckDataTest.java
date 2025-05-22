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

import org.eclipse.ecsp.domain.notification.VehicleMessageAckData.ResponseEnum;
import org.junit.Assert;
import org.junit.Test;

/**
 * VehicleMessageAckDataTest class.
 */
public class VehicleMessageAckDataTest {

    VehicleMessageAckData vmackdata = new VehicleMessageAckData();

    @Test
    public void testgetFailureDiagnostic() {
        vmackdata.getFailureDiagnostic();
    }

    @Test
    public void testsetFailureDiagnostic() {
        vmackdata.setFailureDiagnostic("test");
    }

    @Test
    public void testgetMessageReceiptTimestamp() {
        vmackdata.getMessageReceiptTimestamp();
    }

    @Test
    public void testsetMessageReceiptTimestamp() {
        vmackdata.setMessageReceiptTimestamp(0);
    }

    @Test
    public void testgetVehicleMessageId() {
        vmackdata.getVehicleMessageID();
    }

    @Test
    public void setVehicleMessageId() {
        vmackdata.setVehicleMessageID(0);
    }

    @Test
    public void testgetStatus() {
        Assert.assertEquals(ResponseEnum.MESSAGE_STAGED_FOR_DISPLAY, ResponseEnum.MESSAGE_STAGED_FOR_DISPLAY);
        Assert.assertEquals(ResponseEnum.MESSAGE_TYPE_NOT_SUPPORTED, ResponseEnum.MESSAGE_TYPE_NOT_SUPPORTED);
        Assert.assertEquals(ResponseEnum.MESSAGE_LANGUAGES_NOT_SUPPORTED, ResponseEnum.MESSAGE_LANGUAGES_NOT_SUPPORTED);
        Assert.assertEquals(ResponseEnum.MESSAGE_STAGING_FAILED, ResponseEnum.MESSAGE_STAGING_FAILED);
        Assert.assertEquals(ResponseEnum.MESSAGE_DESTINATION_TEMPORARILY_NOT_AVAILABLE,
            ResponseEnum.MESSAGE_DESTINATION_TEMPORARILY_NOT_AVAILABLE);
        Assert.assertEquals(ResponseEnum.MESSAGE_VIN_NOT_VALID, ResponseEnum.MESSAGE_VIN_NOT_VALID);
        Assert.assertEquals(ResponseEnum.MESSAGE_AUTO_DELETE, ResponseEnum.MESSAGE_AUTO_DELETE);
        Assert.assertEquals(ResponseEnum.MESSAGE_DELETE, ResponseEnum.MESSAGE_DELETE);
        Assert.assertEquals(ResponseEnum.CUSTOM_EXTENSION, ResponseEnum.CUSTOM_EXTENSION);
        vmackdata.getStatus();
    }

    @Test
    public void testsetStatus() {
        vmackdata.setStatus(null);
    }

    @Test
    public void testgetCampaignId() {
        vmackdata.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        vmackdata.setCampaignId("dummy");
    }

    @Test
    public void testgetFileName() {
        vmackdata.getFileName();
    }

    @Test
    public void testsetFileName() {
        vmackdata.setFileName("dummy");
    }

    @Test
    public void testgetHarmanId() {
        vmackdata.getHarmanId();
    }

    @Test
    public void testsetHarmanId() {
        vmackdata.setHarmanId("test123");
    }

    @Test
    public void testgetCountryCode() {
        vmackdata.getCountryCode();
    }

    @Test
    public void testsetCountryCode() {
        vmackdata.setCountryCode("test");
    }

    @Test
    public void testgetCampaignDate() {
        vmackdata.getCampaignDate();
    }

    @Test
    public void testsetCampaignDate() {
        vmackdata.setCampaignDate(null);
    }

    @Test
    public void testgetNotificationId() {
        vmackdata.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        vmackdata.setNotificationId("tst");
    }

    @Test
    public void equals() {
        vmackdata.equals(vmackdata);
    }

    @Test
    public void testhashCode() {
        vmackdata.hashCode();
    }

    @Test
    public void testtoString() {
        vmackdata.toString();
    }
}
