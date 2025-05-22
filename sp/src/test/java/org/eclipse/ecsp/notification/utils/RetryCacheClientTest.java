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

package org.eclipse.ecsp.notification.utils;

import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.notification.entities.RetryRecordEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * RetryCacheClientTest.
 */
public class RetryCacheClientTest {

    @InjectMocks
    RetryCacheClient retryCacheClient;

    @Mock
    IgniteCache igniteCache;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void testgetRetryRecordFromCache() {
        RetryRecordEntity rc = new RetryRecordEntity();
        List<RetryRecord> retryRecordList = new ArrayList<RetryRecord>();
        RetryRecord rrr = new RetryRecord();
        rrr.setMaxRetryLimit(3);
        retryRecordList.add(rrr);
        rc.setRetryRecordList(retryRecordList);
        Mockito.when(igniteCache.getEntity("NOTIFICATIONRETRY:dsdsad")).thenReturn(rc);
        retryCacheClient.getRetryRecordFromCache("dasdas");
        retryCacheClient.getRetryRecordForException("dsadas", "dsad");
        retryCacheClient.putRetryRecord("dsadas", new RetryRecord());
        retryCacheClient.deleteRetryRecord("dsdsad", "dsdas");

        List<RetryRecord> retryRecordList2 = new ArrayList<RetryRecord>();
        RetryRecordEntity rc2 = new RetryRecordEntity();
        rc2.setRetryRecordList(retryRecordList2);
        Mockito.when(igniteCache.getEntity("NOTIFICATIONRETRY:dsdsad")).thenReturn(rc2);
        retryCacheClient.deleteRetryRecord("dsdsad", "dsdas");
        Mockito.when(igniteCache.getEntity("NOTIFICATIONRETRY:dsadas")).thenReturn(rc2);
        retryCacheClient.putRetryRecord("dsadas", new RetryRecord());
        Mockito.verify(igniteCache, Mockito.times(6)).getEntity(Mockito.anyString());
    }
}
