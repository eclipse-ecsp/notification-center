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

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;

/**
 * APIPushConfig defining criticality , android and apns.
 */
@Entity(useDiscriminator = false)
public class ApiPushConfig {

    @JsonProperty("android")
    private AndroidParam androidParam;

    @JsonProperty("apns")
    private ApnsParam apnsParam;

    private String criticality;

    /**
     * This is a default constructor.
     */
    public ApiPushConfig() {
        super();
    }

    /**
     * This method is a getter for androidparam.
     *
     * @return AndroidParam
     */

    public AndroidParam getAndroidParam() {
        return androidParam;
    }

    /**
     * This method is a setter for androidparam.
     *
     * @param androidParam : AndroidParam
     */

    public void setAndroidParam(AndroidParam androidParam) {
        this.androidParam = androidParam;
    }

    /**
     * This method is a getter for apnsparam.
     *
     * @return ApnsParam
     */

    public ApnsParam getApnsParam() {
        return apnsParam;
    }

    /**
     * This method is a setter for apnsparam.
     *
     * @param apnsParam : ApnsParam
     */

    public void setApnsParam(ApnsParam apnsParam) {
        this.apnsParam = apnsParam;
    }

    /**
     * This method is a getter for criticality.
     *
     * @return String
     */

    public String getCriticality() {
        return criticality;
    }

    /**
     * This method is a setter for criticality.
     *
     * @param criticality : String
     */

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    @Override
    public String toString() {
        return "APIPushConfig [androidParam=" + androidParam + ", apnsParam=" + apnsParam + "]";
    }
}