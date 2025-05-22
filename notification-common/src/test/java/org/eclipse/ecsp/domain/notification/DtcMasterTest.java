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

import org.eclipse.ecsp.notification.entities.DTCMaster;
import org.junit.Test;

/**
 * DtcMasterTest class.
 */
public class DtcMasterTest {

    DTCMaster dtcMaster = new DTCMaster();

    @Test
    public void testgetId() {
        dtcMaster.getId();
    }

    @Test
    public void testsetId() {
        dtcMaster.setId("test123");
    }

    @Test
    public void testgetDescription() {
        dtcMaster.getDescription();
    }

    @Test
    public void testsetDescription() {
        dtcMaster.setDescription("");
    }

    @Test
    public void testgetCategory() {
        dtcMaster.getCategory();
    }

    @Test
    public void testsetCategory() {
        dtcMaster.setCategory("dasdsa");
    }

    @Test
    public void testgetSubcategory() {
        dtcMaster.getSubcategory();
    }

    @Test
    public void testsetSubcategory() {
        dtcMaster.setSubcategory("");
    }

    @Test
    public void testgetSuggestions() {
        dtcMaster.getSuggestions();
    }

    @Test
    public void testsetSuggestions() {
        dtcMaster.setSuggestions(null);
    }

    @Test
    public void testtoString() {
        dtcMaster.toString();
    }
}
