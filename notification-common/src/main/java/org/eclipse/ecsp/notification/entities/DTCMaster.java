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
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.util.Set;

/**
 * DTCMaster entity.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(value = "dtcMaster")
public class DTCMaster extends AbstractIgniteEntity {
    @Id
    private String id; // dtc code
    private String description;
    private String category;
    private String subcategory;

    private Set<String> suggestions;

    /**
     * DTCMaster constructor.
     */
    public DTCMaster() {

    }

    /**
     * DTCMaster constructor.
     *
     * @param id id
     *
     * @param description desc
     *
     * @param category category
     *
     * @param subcategory subcategory
     *
     * @param suggestions suggestions
     */
    public DTCMaster(String id, String description, String category, String subcategory, Set<String> suggestions) {
        super();
        this.id = id;
        this.description = description;
        this.category = category;
        this.subcategory = subcategory;
        this.suggestions = suggestions;
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
     * This method is a getter for description.
     *
     * @return String
     */

    public String getDescription() {
        return description;
    }

    /**
     * This method is a setter for description.
     *
     * @param description : String
     */

    public void setDescription(String description) {
        this.description = description;
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
     * This method is a getter for subcategory.
     *
     * @return String
     */

    public String getSubcategory() {
        return subcategory;
    }

    /**
     * This method is a setter for subcategory.
     *
     * @param subcategory : String
     */

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    /**
     * This method is a getter for suggestions.
     *
     * @return Set
     */

    public Set<String> getSuggestions() {
        return suggestions;
    }

    /**
     * This method is a setter for suggestions.
     *
     * @param suggestions : Set
     */

    public void setSuggestions(Set<String> suggestions) {
        this.suggestions = suggestions;
    }

    @Override
    public String toString() {
        return "DTCMaster [id=" + id + ", description=" + description + ", category=" + category
            + ", subcategory=" + subcategory + ", suggestions=" + suggestions + "]";
    }

}