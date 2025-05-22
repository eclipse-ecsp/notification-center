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

package org.eclipse.ecsp.notification.entities;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.io.Serializable;
import java.util.Map;

/**
 * Endpoint(s) per userId.
 *
 * @author MaKumari
 *
 */
@Entity(value = "pinpointEndpoint", useDiscriminator = false)
public class AmazonPinpointEndpoint extends AbstractIgniteEntity implements Serializable {

    private static final long serialVersionUID = -7002988707510303730L;
    @Id
    private String userId;
    private Map<String, Map<String, String>> endpoints;

    /**
     * This method is a getter for userid.
     *
     * @return String
     */

    public String getUserId() {
        return userId;
    }

    /**
     * This method is a setter for userid.
     *
     * @param userId : String
     */

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * This method is a getter for endpoints.
     *
     * @return Map
     */

    public Map<String, Map<String, String>> getEndpoints() {
        return endpoints;
    }

    /**
     * This method is a setter for endpoints.
     *
     * @param endpoints : Map
     */

    public void setEndpoints(Map<String, Map<String, String>> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public String toString() {
        return "AmazonPinpointEndpoint [userId=" + userId + ", endpoints=" + endpoints + "]";
    }

}