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
 * AndroidTest class.
 */
public class AndroidTest {

    Android a1 = new Android();

    @Test
    public void testgetPriority() {
        a1.getPriority();
    }

    @Test
    public void testsetPriority() {
        a1.setPriority("");
    }

    @Test
    public void testgetNotification() {
        a1.getNotification();
    }

    @Test
    public void testsetNotification() {
        a1.setNotification(null);
    }

    @Test
    public void testgetTtl() {
        a1.getTtl();
    }

    @Test
    public void testsetTtl() {
        a1.setTtl("");
    }

    @Test
    public void testtoString() {
        a1.toString();
    }
}
