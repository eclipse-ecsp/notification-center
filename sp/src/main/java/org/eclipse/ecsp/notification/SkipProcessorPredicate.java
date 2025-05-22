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
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * This predicate is testing whether the control should be skipped to the next
 * processor.
 */
@Component
@Profile(value = {"!test"})
public class SkipProcessorPredicate implements
        BiPredicate<IgniteEventStreamProcessor,
        StreamProcessingContext<IgniteKey<?>, IgniteEvent>> {

    Map<String[], Set<String>> topicsCache = new HashMap<>();

    /**
     * Test method.
     *
     * @param streamProcessor IgniteEventStreamProcessor
     * @param ctxt StreamProcessingContext
     * @return boolean
     */
    @Override
    public boolean test(IgniteEventStreamProcessor streamProcessor, StreamProcessingContext ctxt) {

        String[] sources = streamProcessor.sources();

        topicsCache.computeIfAbsent(sources, s -> new HashSet<>(Arrays.asList(sources)));

        return !(topicsCache.get(sources).contains(ctxt.streamName()));
    }

}
