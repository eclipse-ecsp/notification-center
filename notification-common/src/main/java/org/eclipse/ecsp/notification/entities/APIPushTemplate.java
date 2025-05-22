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
import org.eclipse.ecsp.domain.notification.BaseTemplate;

/**
 * APIPushTemplate for api push notifications.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.notification.entities.APIPushTemplate")
public class APIPushTemplate extends BaseTemplate {
    private String category;
    private String bannertitle;
    private String bannerdesc;

    /**
     * APIPushTemplate  constructor.
     */
    public APIPushTemplate() {
        super();
        contentFieldsGetter.put("bannertitle", this::getBannertitle);
        contentFieldsGetter.put("bannerdesc", this::getBannerdesc);

        contentFieldsSetter.put("bannertitle", this::setBannertitle);
        contentFieldsSetter.put("bannerdesc", this::setBannerdesc);
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
     * This method is a getter for bannertitle.
     *
     * @return String
     */

    public String getBannertitle() {
        return bannertitle;
    }

    /**
     * This method is a setter for bannertitle.
     *
     * @param bannertitle : String
     */

    public void setBannertitle(String bannertitle) {
        this.bannertitle = bannertitle;
    }

    /**
     * This method is a getter for bannerdesc.
     *
     * @return String
     */

    public String getBannerdesc() {
        return bannerdesc;
    }

    /**
     * This method is a setter for bannerdesc.
     *
     * @param bannerdesc : String
     */

    public void setBannerdesc(String bannerdesc) {
        this.bannerdesc = bannerdesc;
    }

}