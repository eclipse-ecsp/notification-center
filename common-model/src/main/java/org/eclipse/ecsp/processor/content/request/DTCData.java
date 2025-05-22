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

package org.eclipse.ecsp.processor.content.request;

import java.util.Set;

/**
 * DTCData class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class DTCData {
    private String locale;
    private String description;
    private String category;
    private String subcategory;
    private Set<String> suggestions;

    /**
     * DTCData constructor.
     */
    public DTCData() {

    }

    /**
     * DTCData constructor.
     *
     * @param locale locale
     *
     * @param description description
     *
     * @param category category
     *
     * @param subcategory subcategory
     *
     * @param suggestions suggestions
     */
    public DTCData(String locale, String description, String category, String subcategory, Set<String> suggestions) {
        super();
        this.locale = locale;
        this.description = description;
        this.category = category;
        this.subcategory = subcategory;
        this.suggestions = suggestions;
    }

    /**
     * Get locale.
     *
     * @return locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Set locale.
     *
     * @param locale locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Get description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get Category.
     *
     * @return category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Set category.
     *
     * @param category category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get subcategory.
     *
     * @return subcategory
     */
    public String getSubcategory() {
        return subcategory;
    }

    /**
     * Set subcategory.
     *
     * @param subcategory subcategory
     */
    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    /**
     * Get suggestions.
     *
     * @return suggestions
     */
    public Set<String> getSuggestions() {
        return suggestions;
    }

    /**
     * Set suggestions.
     *
     * @param suggestions suggestions
     */
    public void setSuggestions(Set<String> suggestions) {
        this.suggestions = suggestions;
    }

    /**
     * toString.
     *
     * @return toString
     */
    @Override
    public String toString() {
        return "DTCData [locale=" + locale + ", description=" + description + ", category=" + category
            + ", subcategory=" + subcategory + ", suggestions=" + suggestions + "]";
    }


}
