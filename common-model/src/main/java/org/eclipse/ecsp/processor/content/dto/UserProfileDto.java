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
import java.util.Locale;
import java.util.Map;

/**
 * UserProfileDto class.
 */
@Data
@Accessors(chain = true)
public class UserProfileDto {
    private String userId;
    private String firstName;
    private String lastName;
    private Locale locale;
    private String defaultEmail;
    private String defaultPhoneNumber;
    private boolean consent;
    private String timeZone;
    private long lastModifiedTime;
    private Map<String, Object> customAttributes;

    /**
     * Sets custom attributes.
     *
     * @param customAttributes the custom attributes
     * @return the custom attributes
     */
    public UserProfileDto setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes == null ? new HashMap<>() : customAttributes;
        return this;
    }
}
