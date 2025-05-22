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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Apns config class.
 */
public class Apns {

    @JsonProperty(value = "headers")
    private Map<String, String> headers = new HashMap<String, String>();

    @JsonProperty(value = "payload")
    private Payload payload;

    /**
     * This method is a getter for payload.
     *
     * @return Payload
     */

    public Payload getPayload() {
        return payload;
    }

    /**
     * This method is a setter for payload.
     *
     * @param payload : Payload
     */

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    /**
     * This method is a getter for headers.
     *
     * @return Map
     */

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * This method is a setter for headers.
     *
     * @param headers : Map
     */

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}