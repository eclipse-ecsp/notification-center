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

/**
 * Android pojo.
 */
public class Android {

    @JsonProperty(value = "ttl")
    private String ttl;

    @JsonProperty(value = "notification")
    private Notification notification;

    @JsonProperty(value = "priority")
    private String priority;

    /**
     * This method is a getter for priority.
     *
     * @return String
     */

    public String getPriority() {
        return priority;
    }

    /**
     * This method is a setter for priority.
     *
     * @param priority : String
     */

    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * This method is a getter for notification.
     *
     * @return Notification
     */

    public Notification getNotification() {
        return notification;
    }

    /**
     * This method is a setter for notification.
     *
     * @param notification : Notification
     */

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    /**
     * This method is a getter for ttl.
     *
     * @return String
     */

    public String getTtl() {
        return ttl;
    }

    /**
     * This method is a setter for ttl.
     *
     * @param ttl : String
     */

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "Android{" + "ttl='" + ttl + '\''
            + ", notification=" + notification
            + ", priority='" + priority + '\'' + '}';
    }
}