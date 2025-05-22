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

package org.eclipse.ecsp.processor.content.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a light weight version of the notification-commons.AlertsInfo.
 * It contains data reasonable for dynamic content processing. If more data is needed,
 * please implement custom AlertsInfo to Dto transformer and put all additional values into the
 * additional properties [String,Object] map.
 */
@Data
@Accessors(chain = true)
public class ContentProcessingContextDto {
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private String eventID;
    private String version;
    private long timestamp;
    private int timezone;
    private String pdid;
    private String notificationId;
    private String marketingName;
    private String locale;
    private UserProfileDto userProfile;
    private VehicleProfileDto vehicleProfile;
    private Map<String, Object> additionalProperties;

    /**
     * Sets additional properties.
     *
     * @param additionalProperties the additional properties
     * @return the additional properties
     */
    public ContentProcessingContextDto setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties == null ? new HashMap<>() : additionalProperties;
        return this;
    }
}
