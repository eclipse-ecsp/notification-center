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

package org.eclipse.ecsp.platform.notification.v1.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AlertReadUpdate class.
 */
public class AlertReadUpdate {
    @JsonProperty(value = "read")
    private List<String> readList;
    @JsonProperty(value = "unread")
    private List<String> unreadList;

    /**
     * Get readlist.
     *
     * @return the readList
     */
    public List<String> getReadList() {
        return readList;
    }

    /**
     * Set readList.
     *
     * @param readList the readList to set
     */
    public void setReadList(List<String> readList) {
        this.readList = readList;
    }

    /**
     * Get unreadlist.
     *
     * @return the unreadList
     */
    public List<String> getUnreadList() {
        return unreadList;
    }

    /**
     * set unread list.
     *
     * @param unreadList the unreadList to set
     */
    public void setUnreadList(List<String> unreadList) {
        this.unreadList = unreadList;
    }

}
