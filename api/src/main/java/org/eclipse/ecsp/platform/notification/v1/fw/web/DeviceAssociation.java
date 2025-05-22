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

package org.eclipse.ecsp.platform.notification.v1.fw.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DeviceAssociation class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceAssociation {

    @JsonProperty("associationId")
    private String associationId;

    @JsonProperty("associationStatus")
    private String associationStatus;

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @JsonProperty("deviceId")
    private String harmanID;

    /**
     * Get associationId.
     *
     * @return associationId
     */
    public String getAssociationId() {
        return associationId;
    }

    /**
     * Set associationId.
     *
     * @param associationId associationId
     */
    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    /**
     * Get associationStatus.
     *
     * @return associationStatus
     */
    public String getAssociationStatus() {
        return associationStatus;
    }

    /**
     * Set associationStatus.
     *
     * @param associationStatus associationStatus
     */
    public void setAssociationStatus(String associationStatus) {
        this.associationStatus = associationStatus;
    }

    /**
     * Get harmanID.
     *
     * @return harmanID
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public String getHarmanID() {
        return harmanID;
    }

    /**
     * Set harmanID.
     *
     * @param harmanID harmanID
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public void setHarmanID(String harmanID) {
        this.harmanID = harmanID;
    }

    /**
     * toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "DeviceAssociation [associationId=" + associationId + ", associationStatus=" + associationStatus
            + ", harmanID=" + harmanID + "]";
    }

    /**
     * hashCode.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((associationId == null) ? 0 : associationId.hashCode());
        result = prime * result + ((associationStatus == null) ? 0 : associationStatus.hashCode());
        result = prime * result + ((harmanID == null) ? 0 : harmanID.hashCode());
        return result;
    }

    /**
     * equals.
     *
     * @param obj obj
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DeviceAssociation other = (DeviceAssociation) obj;
        if (associationId == null) {
            if (other.associationId != null) {
                return false;
            }
        } else if (!associationId.equals(other.associationId)) {
            return false;
        }
        if (associationStatus == null) {
            if (other.associationStatus != null) {
                return false;
            }
        } else if (!associationStatus.equals(other.associationStatus)) {
            return false;
        }
        if (harmanID == null) {
            if (other.harmanID != null) {
                return false;
            }
        } else if (!harmanID.equals(other.harmanID)) {
            return false;
        }
        return true;
    }

}
