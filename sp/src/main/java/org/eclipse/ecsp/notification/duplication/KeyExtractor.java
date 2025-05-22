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

package org.eclipse.ecsp.notification.duplication;

import org.eclipse.ecsp.domain.notification.AlertsInfo;

/**
 * key Extractor interface.
 */
public interface KeyExtractor {

    /**
     * UNDERSCORE.
     */
    public static final String UNDERSCORE = "_";
    /**
     * DEDUP_KEY_PREFIX.
     */
    public static final String DEDUP_KEY_PREFIX = "DEDUP_";

    /**
     * Extract current key.
     *
     * @param alertInfo AlertsInfo
     * @return String
     */
    String extractCurrentKey(AlertsInfo alertInfo);

    /**
     * Extract previous key.
     *
     * @param alertInfo AlertsInfo
     * @param hops      int
     * @return String
     */
    String extractPreviousKey(AlertsInfo alertInfo, int hops);

}
