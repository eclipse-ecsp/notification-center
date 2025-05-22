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

import junit.framework.Assert;
import org.eclipse.ecsp.domain.notification.SuppressionConfig.SuppressionType;
import org.junit.Test;

import java.util.ArrayList;

/**
 * SuppressionConfigTest class.
 *.
 */
public class SuppressionConfigTest {

    SuppressionConfig sc = new SuppressionConfig();
    SuppressionConfig sc2 =
        new SuppressionConfig("RECURRING", "22:22", "22:45", "2022-02-01", "2022-02-22", new ArrayList<Integer>());
    SuppressionConfig sc3 = new SuppressionConfig("RECURRING", "", "", "", "", new ArrayList<Integer>());

    SuppressionConfig.LocalDateDeserializer sldtdeser = new SuppressionConfig.LocalDateDeserializer();
    SuppressionConfig.LocalDateSerializer sldtser = new SuppressionConfig.LocalDateSerializer();

    SuppressionConfig.LocalTimeSerializer sltmser = new SuppressionConfig.LocalTimeSerializer();

    SuppressionConfig.LocalTimeDeserializer sltmdeser = new SuppressionConfig.LocalTimeDeserializer();


    @Test
    public void testMethod() {
        Assert.assertEquals(SuppressionType.RECURRING, SuppressionType.RECURRING);
        Assert.assertEquals(SuppressionType.VACATION, SuppressionType.VACATION);


    }

}
