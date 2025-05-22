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

package org.eclipse.ecsp.platform.notification.dto;

import java.util.Set;

/**
 * TemplateProcessingParams class.
 *
 * @author AMuraleedhar
 */
public class TemplateProcessingParams {

    private Set<String> brandsNoAddAttrs;

    private Set<String> brandsWithAddAttrs;

    private Set<String> addAttrs;

    private boolean isAdditionalAttrPatch;

    private String propertyName;

    /**
     * Get the brandsNoAddAttrs.
     *
     * @return brandsNoAddAttrs
     */
    public Set<String> getBrandsNoAddAttrs() {
        return brandsNoAddAttrs;
    }

    /**
     * Set the brandsNoAddAttrs.
     *
     * @param brandsNoAddAttrs Set of brands with no additional attributes
     */
    public void setBrandsNoAddAttrs(Set<String> brandsNoAddAttrs) {
        this.brandsNoAddAttrs = brandsNoAddAttrs;
    }

    /**
     * Get the brandsWithAddAttrs.
     *
     * @return brandsWithAddAttrs
     */
    public Set<String> getBrandsWithAddAttrs() {
        return brandsWithAddAttrs;
    }

    /**
     * Set the brandsWithAddAttrs.
     *
     * @param brandsWithAddAttrs Set of brands with additional attributes
     */
    public void setBrandsWithAddAttrs(Set<String> brandsWithAddAttrs) {
        this.brandsWithAddAttrs = brandsWithAddAttrs;
    }

    /**
     * Get the addAttrs.
     *
     * @return addAttrs
     */
    public Set<String> getAddAttrs() {
        return addAttrs;
    }

    /**
     * Set the addAttrs.
     *
     * @param addAttrs Set of additional attributes
     */
    public void setAddAttrs(Set<String> addAttrs) {
        this.addAttrs = addAttrs;
    }

    /**
     * Get the isAdditionalAttrPatch.
     *
     * @return isAdditionalAttrPatch
     */
    public boolean isAdditionalAttrPatch() {
        return isAdditionalAttrPatch;
    }

    /**
     * Set the isAdditionalAttrPatch.
     *
     * @param isAdditionalAttrPatch isAdditionalAttrPatch
     */
    public void setAdditionalAttrPatch(boolean isAdditionalAttrPatch) {
        this.isAdditionalAttrPatch = isAdditionalAttrPatch;
    }

    /**
     * Get the propertyName.
     *
     * @return propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Set the propertyName.
     *
     * @param propertyName propertyName
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * To string.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "TemplateProcessingParams [brandsNoAddAttrs=" + brandsNoAddAttrs + ", brandsWithAddAttrs="
            + brandsWithAddAttrs + ", addAttrs=" + addAttrs + ", isAdditionalAttrPatch=" + isAdditionalAttrPatch
            + ", propertyName=" + propertyName + "]";
    }

}
