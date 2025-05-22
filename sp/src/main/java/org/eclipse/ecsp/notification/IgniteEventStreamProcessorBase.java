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
import org.eclipse.ecsp.analytics.stream.base.stores.HarmanPersistentKVStore;

import java.util.Properties;

/**
 * IgniteEventStreamProcessorBase abstract class.
 */
public abstract class IgniteEventStreamProcessorBase implements IgniteEventStreamProcessor {

    /**
     * Punctuate method.
     *
     * @param l long
     */
    @Override
    public void punctuate(long l) {

    }

    /**
     * Close method.
     */
    @Override
    public void close() {

    }

    /**
     * Config changed method.
     *
     * @param properties properties
     */
    @Override
    public void configChanged(Properties properties) {

    }

    /**
     * Create state store method.
     *
     * @return HarmanPersistentKVStore
     */
    @Override
    public HarmanPersistentKVStore createStateStore() {
        return null;
    }
}
