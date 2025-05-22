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

/**
 * Attachment class.
 */
public class Attachment {

    private String id;
    private String name;
    private String url;

    /**
     * This is a default constructor.
     */
    public Attachment() {
        super();
    }

    /**
     * This method is a getter for id.
     *
     * @return String
     */

    public String getId() {
        return id;
    }

    /**
     * This method is a setter for id.
     *
     * @param id : String
     */

    public void setId(String id) {
        this.id = id;
    }

    /**
     * This method is a getter for name.
     *
     * @return String
     */

    public String getName() {
        return name;
    }

    /**
     * This method is a setter for name.
     *
     * @param name : String
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method is a getter for url.
     *
     * @return String
     */

    public String getUrl() {
        return url;
    }

    /**
     * This method is a setter for url.
     *
     * @param url : String
     */

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Attachment [id=" + id + ", name=" + name + ", url=" + url + "]";
    }

}