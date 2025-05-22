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

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * BaseTemplate class.
 */
@Entity
public abstract class BaseTemplate {
    private String title;
    private String subtitle;
    private String body;

    /**
     * contentFieldsGetter.
     */
    @Transient
    protected Map<String, Supplier<String>> contentFieldsGetter;
    /**
     * contentFieldsSetter.
     */
    @Transient
    protected Map<String, Consumer<String>> contentFieldsSetter;

    /**
     * BaseTemplate constructor.
     */
    public BaseTemplate() {
        contentFieldsGetter = new HashMap<>();
        contentFieldsGetter.put("title", this::getTitle);
        contentFieldsGetter.put("subtitle", this::getSubtitle);
        contentFieldsGetter.put("body", this::getBody);

        contentFieldsSetter = new HashMap<>();
        contentFieldsSetter.put("title", this::setTitle);
        contentFieldsSetter.put("subtitle", this::setSubtitle);
        contentFieldsSetter.put("body", this::setBody);
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

    /**
     * This method is a getter for contentfieldsgetter.
     *
     * @return Map
     */


    public Map<String, Supplier<String>> getContentFieldsGetter() {
        return contentFieldsGetter;
    }

    /**
     * This method is a getter for contentfieldssetter.
     *
     * @return Map
     */

    public Map<String, Consumer<String>> getContentFieldsSetter() {
        return contentFieldsSetter;
    }

    @Override
    public String toString() {
        return "BaseTemplate [title=" + title + ", subtitle=" + subtitle + ", body=" + body + "]";
    }

}