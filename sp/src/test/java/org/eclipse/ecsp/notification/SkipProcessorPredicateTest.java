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

package org.eclipse.ecsp.notification;

import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * SkipProcessorPredicateTest.
 */
public class SkipProcessorPredicateTest {
    @Mock
    private StreamProcessingContext streamProcessingContext;

    @Mock
    private IgniteEventStreamProcessor streamProcessor;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void test_true() {
        SkipProcessorPredicate skipProcessorPredicate = new SkipProcessorPredicate();
        doReturn("dummyTopic").when(streamProcessingContext).streamName();
        doReturn(new String[] {"sourceA", "sourceB"}).when(streamProcessor).sources();
        boolean result = skipProcessorPredicate.test(streamProcessor, streamProcessingContext);
        assertTrue(result);
        boolean resultAfterCache = skipProcessorPredicate.test(streamProcessor, streamProcessingContext);
        assertTrue(resultAfterCache);
    }

    @Test
    public void test_false() {
        SkipProcessorPredicate skipProcessorPredicate = new SkipProcessorPredicate();
        doReturn("dummyTopic").when(streamProcessingContext).streamName();
        doReturn(new String[] {"dummyTopic", "sourceB"}).when(streamProcessor).sources();
        boolean result = skipProcessorPredicate.test(streamProcessor, streamProcessingContext);
        assertFalse(result);
    }

}