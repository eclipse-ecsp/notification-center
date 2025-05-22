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

import org.junit.Test;

/**
 * IvmFeedbackDataV1Test class.
 */
public class IvmFeedbackDataV1Test {

    IVMFeedbackData_V1 ivmFeedbackDataV1 = new IVMFeedbackData_V1();

    @Test
    public void testgetStatus() {
        ivmFeedbackDataV1.getStatus();
    }

    @Test
    public void testsetStatus() {
        ivmFeedbackDataV1.setStatus("dummy");
    }

    @Test
    public void testgetErrorCode() {
        ivmFeedbackDataV1.getErrorCode();
    }

    @Test
    public void testsetErrorCode() {
        ivmFeedbackDataV1.setErrorCode(null);
    }

    public void testgetCampaignDate() {
        ivmFeedbackDataV1.getCampaignDate();
    }

    @Test
    public void testsetCampaignDate() {
        ivmFeedbackDataV1.setCampaignDate("");
    }

    @Test
    public void testgetCampaignId() {
        ivmFeedbackDataV1.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        ivmFeedbackDataV1.setCampaignId("test123");
    }

    @Test
    public void testgetCountryCode() {
        ivmFeedbackDataV1.getCountryCode();
    }

    @Test
    public void testsetCountryCode() {
        ivmFeedbackDataV1.setCountryCode("dummy");
    }

    @Test
    public void testgetNotificationId() {
        ivmFeedbackDataV1.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        ivmFeedbackDataV1.setNotificationId("dummy");
    }

    @Test
    public void testgetFileName() {
        ivmFeedbackDataV1.getFileName();
    }

    @Test
    public void testsetFileName() {
        ivmFeedbackDataV1.setFileName("dummy");
    }

    @Test
    public void testgetHarmanId() {
        ivmFeedbackDataV1.getHarmanId();
    }

    @Test
    public void testsetHarmanId() {
        ivmFeedbackDataV1.setHarmanId("");
    }

    @Test
    public void testgetErrorDetail() {
        ivmFeedbackDataV1.getErrorDetail();
    }

    @Test
    public void testsetErrorDetail() {
        ivmFeedbackDataV1.setErrorDetail(null);
    }

    @Test
    public void testgetVehicleMessageId() {
        ivmFeedbackDataV1.getVehicleMessageID();
    }

    @Test
    public void testsetVehicleMessageId() {
        ivmFeedbackDataV1.setVehicleMessageID(0);
    }

    @Test
    public void testtoString() {
        ivmFeedbackDataV1.toString();
    }
}
