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
 * IvmRequestTest class.
 */
public class IvmRequestTest {

    IVMRequest ivmRequest = new IVMRequest();

    @Test
    public void testgetMessageId() {
        ivmRequest.getMessageId();
    }

    @Test
    public void testsetMessageId() {
        ivmRequest.setMessageId("test123");
    }

    @Test
    public void testgetRequestId() {
        ivmRequest.getRequestId();
    }

    @Test
    public void testsetRequestId() {
        ivmRequest.setRequestId("test123");
    }

    @Test
    public void testgetVehicleId() {
        ivmRequest.getVehicleId();
    }

    @Test
    public void testsetVehicleId() {
        ivmRequest.setVehicleId("dummy");
    }

    @Test
    public void testgetSessionId() {
        ivmRequest.getSessionId();
    }

    @Test
    public void testsetSessionId() {
        ivmRequest.setSessionId("test123");
    }

    @Test
    public void testgetCampaignDate() {
        ivmRequest.getCampaignDate();
    }

    @Test
    public void testsetCampaignDate() {
        ivmRequest.setCampaignDate("test");
    }

    @Test
    public void testgetCampaignId() {
        ivmRequest.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        ivmRequest.setCampaignId("");
    }

    @Test
    public void testgetHarmanId() {
        ivmRequest.getHarmanId();
    }

    @Test
    public void testsetHarmanId() {
        ivmRequest.setHarmanId("");
    }

    @Test
    public void testgetFileName() {
        ivmRequest.getFileName();
    }

    @Test
    public void testsetFileName() {
        ivmRequest.setFileName("dummy");
    }

    @Test
    public void testgetCountryCode() {
        ivmRequest.getCountryCode();
    }

    @Test
    public void testsetCountryCode() {
        ivmRequest.setCountryCode("");
    }

    @Test
    public void testgetNotificationId() {
        ivmRequest.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        ivmRequest.setNotificationId("test");
    }

}
