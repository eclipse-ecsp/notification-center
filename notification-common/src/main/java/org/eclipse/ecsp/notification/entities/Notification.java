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

/**
 * Notification with action for push notification.
 */
public class Notification {

    @JsonProperty("click_action")
    private String clickAction;

    /**
     * Notification default constructor.
     */
    public Notification() {
        super();
    }

    /**
     * This method is a getter for clickaction.
     *
     * @return String
     */

    public String getClickAction() {
        return clickAction;
    }

    /**
     * This method is a setter for clickaction.
     *
     * @param clickAction : String
     */

    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    @Override
    public String toString() {
        return "Notification [clickAction=" + clickAction + "]";
    }
}