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

/**
 * SmsConfig class.
 */
@Entity(useDiscriminator = false)
public class SmsConfig {

    private String smsType;

    /**
     * SmsConfig default constructor.
     */
    public SmsConfig() {
        super();
    }

    /**
     * Get smsType.
     *
     * @return smsType
     */
    public String getSmsType() {
        return smsType;
    }

    /**
     * Set smsType.
     *
     * @param smsType smsType
     */
    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "SmsConfig [smsType=" + smsType + "]";
    }
}
