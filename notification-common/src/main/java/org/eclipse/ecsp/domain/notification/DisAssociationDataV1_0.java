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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.AbstractEventData;

/**
 * DisAssociationDataV1_0eventdata class.
 */
@SuppressWarnings("checkstyle:TypeName")
@EventMapping(id = EventID.DISASSOCIATION, version = Version.V1_0)
public class DisAssociationDataV1_0 extends AbstractEventData {
    @JsonProperty("PDID")
    private String pdId;
    @JsonProperty("userId")
    private String userId;

    /**
     * Getter for pdId.
     *
     * @return String
     */
    public String getPdId() {
        return pdId;
    }

    /**
     * Setter for pdId.
     *
     * @param pdId String
     */
    public void setPdId(String pdId) {
        this.pdId = pdId;
    }

    /**
     * Getter for userId.
     *
     * @return String
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter for userId.
     *
     * @param userId String
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
