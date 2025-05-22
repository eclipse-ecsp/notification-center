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
 * RetryRecordTest class.
 */
public class RetryRecordTest {

    RetryRecord retryRecord = new RetryRecord();

    RetryRecord retryRecord2 = new RetryRecord("e", 2, 3, 4L);

    @Test
    public void testgetRetryException() {
        retryRecord.getRetryException();
    }

    @Test
    public void testsetRetryException() {
        retryRecord.setRetryException(null);
    }

    @Test
    public void testgetMaxRetryLimit() {
        retryRecord.getMaxRetryLimit();
    }

    @Test
    public void testsetMaxRetryLimit() {
        retryRecord.setMaxRetryLimit(0);
    }

    @Test
    public void testgetRetryCount() {
        retryRecord.getRetryCount();
    }

    @Test
    public void testsetRetryCount() {
        retryRecord.setRetryCount(0);
    }

    @Test
    public void getRetryIntervalMs() {
        retryRecord.getRetryIntervalMs();
    }

    @Test
    public void testsetRetryIntervalMs() {
        retryRecord.setRetryIntervalMs(0);
    }

    @Test
    public void testtoString() {
        retryRecord.toString();
    }
}
