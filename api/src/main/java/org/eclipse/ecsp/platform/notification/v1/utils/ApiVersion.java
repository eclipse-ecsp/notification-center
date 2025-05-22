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

package org.eclipse.ecsp.platform.notification.v1.utils;

import org.eclipse.ecsp.platform.notification.v1.events.EventMetadataConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * APIVersion class.
 */
public class ApiVersion {

    private static final String VERSION_1 = "v1";
    private static final String VERSION_2 = "v2";
    private static final String VERSION_3 = "v3";
    private static final String VERSION_1_1 = "v1.1";

    private static final Map<String, String> VERSION_MAPPING = new HashMap<String, String>();

    static {
        VERSION_MAPPING.put(VERSION_1, EventMetadataConstants.VERSION_1_0);
        VERSION_MAPPING.put(VERSION_1_1, EventMetadataConstants.VERSION_1_1);
        VERSION_MAPPING.put(VERSION_2, EventMetadataConstants.VERSION_2_0);
        VERSION_MAPPING.put(VERSION_3, EventMetadataConstants.VERSION_3_0);
    }

    private ApiVersion()
      {}

    /**
     * Get API version.
     *
     * @param version version
     * @return api version
     */
    public static Optional<String> getApiVersion(String version) {
        String apiVersion = VERSION_MAPPING.get(version);
        return Optional.ofNullable(apiVersion);
    }


}
