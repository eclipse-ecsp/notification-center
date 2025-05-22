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

import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.junit.Test;

/**
 * MarketingNameTest class.
 */
public class MarketingNameTest {

    MarketingName mn = new MarketingName();

    ChannelTemplates ct = new ChannelTemplates();

    @Test
    public void testgetset() {
        mn.getBrandName();
        mn.setBrandName(null);
        mn.getId();
        mn.setId(null);
        mn.getLastUpdatedTime();
        mn.setLastUpdatedTime(null);
        mn.getMarketingName();
        mn.setMarketingName(null);
        mn.getModel();
        mn.setModel(null);
        mn.getSchemaVersion();
        mn.setSchemaVersion(null);
        mn.hashCode();
        mn.toString();
    }

    @Test
    public void testsetCt() {
        ct.setPush(null);
        ct.setApiPush(null);
        ct.setEmail(null);
        ct.setIvm(null);
        ct.setPortal(null);
        ct.setSms(null);
    }


}
