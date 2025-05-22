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

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import java.nio.charset.StandardCharsets;

/**
 * CustomBloomFilter class.
 */
public class CustomBloomFilter {

    private BloomFilter<String> bloomFilter;

    /**
     * CustomBloomFilter constructor.
     *
     * @param expectedInsertion int
     */
    public CustomBloomFilter(int expectedInsertion) {
        bloomFilter = BloomFilter.create(new StringFunnel(), expectedInsertion);
    }

    // Create the custom filter
    private static class StringFunnel implements Funnel<String> {
        @Override
        public void funnel(String arg0, PrimitiveSink arg1) {
            arg1.putBytes(arg0.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * put method.
     *
     * @param key String
     */
    public void put(String key) {
        bloomFilter.put(key);
    }

    /**
     * mightContain method.
     *
     * @param key String
     * @return boolean
     */
    public boolean mightContain(String key) {
        return bloomFilter.mightContain(key);
    }
}
