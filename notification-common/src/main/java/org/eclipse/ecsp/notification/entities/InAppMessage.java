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
 * InAppMessage template.
 */
public class InAppMessage {

    private String category;
    private String title;
    private String subtitle;
    private String body;

    /**
     * InAppMessage default constructor.
     */
    public InAppMessage() {
        super();
    }

    /**
     * This method is a getter for category.
     *
     * @return String
     */

    public String getCategory() {
        return category;
    }

    /**
     * This method is a setter for category.
     *
     * @param category : String
     */

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * This method is a getter for body.
     *
     * @return String
     */

    public String getBody() {
        return body;
    }

    /**
     * This method is a setter for body.
     *
     * @param body : String
     */

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * This method is a getter for title.
     *
     * @return String
     */

    public String getTitle() {
        return title;
    }

    /**
     * This method is a setter for title.
     *
     * @param title : String
     */

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * This method is a getter for subtitle.
     *
     * @return String
     */

    public String getSubtitle() {
        return subtitle;
    }

    /**
     * This method is a setter for subtitle.
     *
     * @param subtitle : String
     */

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public String toString() {
        return "InAppMessage [category=" + category + ", title=" + title
            + ", subtitle=" + subtitle + ", body=" + body + "]";
    }
}