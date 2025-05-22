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
 * PinDataV1Test class.
 */
public class PinDataV1Test {

    PinDataV1_0 pd = new PinDataV1_0();

    @Test
    public void testgetPin() {
        pd.getPin();
    }

    @Test
    public void testsetPin() {
        pd.setPin("");
    }

    @Test
    public void testgetEmails() {
        pd.getEmails();
    }

    @Test
    public void testsetEmails() {
        pd.setEmails(null);
    }

    @Test
    public void testgetPhones() {
        pd.getPhones();
    }

    @Test
    public void setPhones() {
        pd.setPhones(null);
    }

    @Test
    public void testgetImei() {
        pd.getImei();
    }

    @Test
    public void testsetImei() {
        pd.setImei("");
    }

    @Test
    public void testgetDuration() {
        pd.getDuration();
    }

    @Test
    public void testsetDuration() {
        pd.setDuration("");
    }

    @Test
    public void testgetProductName() {
        pd.getProductName();
    }

    @Test
    public void testsetProductName() {
        pd.setProductName(null);
    }
}
