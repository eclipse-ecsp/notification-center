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
 * RequestInfoTest class.
 */
public class RequestInfoTest {

    RequestInfo ri = new RequestInfo();
    RequestInfo ri2 = new RequestInfo("test", "test", "test");

    @Test
    public void testgetRequestId() {
        ri.getRequestId();
    }

    @Test
    public void testsetRequestId() {
        ri.setRequestId("");
    }

    @Test
    public void testgetSessionId() {
        ri.getSessionId();
    }

    @Test
    public void testsetSessionId() {
        ri.setSessionId("");
    }

    @Test
    public void testgetClientRequestId() {
        ri.setClientRequestId("");
    }

    @Test
    public void testsetClientRequestId() {
        ri.getClientRequestId();
    }
}
