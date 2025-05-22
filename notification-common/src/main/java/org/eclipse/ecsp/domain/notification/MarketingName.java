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
import dev.morphia.annotations.Id;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;
import org.eclipse.ecsp.utils.Constants;

/**
 * MarketingName class.
 */
@Entity(value = NotificationDaoConstants.MARKETING_NAME_COLLECTION_NAME)
public class MarketingName extends AbstractIgniteEntity {

    @Id
    private String id;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String brandName;

    private String model;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String marketingName;

    /**
     * Getter for Id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for Id.
     *
     * @param id the new value
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for BrandName.
     *
     * @return brandname
     */
    public String getBrandName() {
        return brandName;
    }

    /**
     * Setter for BrandName.
     *
     * @param brandName the new value
     */
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    /**
     * Getter for Model.
     *
     * @return model
     */
    public String getModel() {
        return model;
    }

    /**
     * Setter for Model.
     *
     * @param model the new value
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Getter for MarketingName.
     *
     * @return marketingname
     */
    public String getMarketingName() {
        return marketingName;
    }

    /**
     * Setter for MarketingName.
     *
     * @param marketingName the new value
     */
    public void setMarketingName(String marketingName) {
        this.marketingName = marketingName;
    }

}
