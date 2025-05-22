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
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionAckData.DispositionResponseEnum;
import org.junit.Test;

/**
 * VehicleMessageDispositionAckDataTest class.
 */
public class VehicleMessageDispositionAckDataTest {

    VehicleMessageDispositionAckData vad = new VehicleMessageDispositionAckData();

    @Test
    public void testgetResponse() {
        vad.getResponse();
    }

    @Test
    public void testsetResponse() {
        vad.setResponse(null);
    }

    @Test
    public void testequals() {
        vad.equals(vad);
    }

    @Test
    public void testhashCode() {
        vad.hashCode();
    }

    @Test
    public void testtoString() {
        vad.toString();
    }

    @Test
    public void testEnum() {
        Assert.assertEquals(DispositionResponseEnum.SUCCESS, DispositionResponseEnum.SUCCESS);

        Assert.assertEquals(DispositionResponseEnum.FAILURE, DispositionResponseEnum.FAILURE);
        Assert.assertEquals(DispositionResponseEnum.CUSTOM_EXTENSION, DispositionResponseEnum.CUSTOM_EXTENSION);
    }


}
