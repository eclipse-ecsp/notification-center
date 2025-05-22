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
 * CampaignStatusDataV1Test class.
 */
public class CampaignStatusDataV1Test {

    CampaignStatusDataV1_0 cd = new CampaignStatusDataV1_0();

    @Test
    public void testgetCampaignId() {
        cd.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        cd.setCampaignId("");
    }

    @Test
    public void testgetType() {
        cd.getType();
    }

    @Test
    public void testsetType() {
        cd.setType("");
    }

    @Test
    public void testgetStatus() {
        cd.getStatus();
    }

    @Test
    public void testsetStatus() {
        cd.setStatus("");
    }

    @Test
    public void testisGraceful() {
        cd.isGraceful();
    }

    @Test
    public void testsetGraceful() {
        cd.setGraceful(false);
    }

}
