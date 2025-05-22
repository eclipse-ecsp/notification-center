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

import org.eclipse.ecsp.events.vehicleprofile.ChangeDescription;
import org.junit.Test;

/**
 * ChangeDescriptionTest class.
 */
public class ChangeDescriptionTest {

    ChangeDescription cd = new ChangeDescription();

    @Test
    public void testgetPath() {
        cd.getPath();
    }

    @Test
    public void setPath() {
        cd.setPath("");
    }

    @Test
    public void testgetKey() {
        cd.getKey();
    }

    @Test
    public void testsetKey() {
        cd.setKey("");
    }

    @Test
    public void testgetOld() {
        cd.getOld();
    }

    @Test
    public void testsetOld() {
        cd.setOld("");
    }

    @Test
    public void testgetChanged() {
        cd.getChanged();
    }

    @Test
    public void testsetChanged() {
        cd.setChanged(null);
    }

    @Test
    public void testtoString() {
        cd.toString();
    }

}
