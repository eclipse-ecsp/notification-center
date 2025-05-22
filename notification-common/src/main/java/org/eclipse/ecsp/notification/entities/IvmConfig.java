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
 * IvmConfig class.
 */
@Entity(useDiscriminator = false)
public class IvmConfig {

    boolean sendAllLanguages;

    /**
     * IvmConfig constructor.
     */
    public boolean isSendAllLanguages() {
        return sendAllLanguages;
    }

    /**
     * This method is a setter for sendalllanguages.
     *
     * @param sendAllLanguages : boolean
     */

    public void setSendAllLanguages(boolean sendAllLanguages) {
        this.sendAllLanguages = sendAllLanguages;
    }

    @Override
    public String toString() {
        return "IvmConfig{" + "sendAllLanguages=" + sendAllLanguages + '}';
    }
}