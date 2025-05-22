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
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;

/**
 * Campaign class.
 */
@Entity(value = NotificationDaoConstants.CAMPAIGNIDS_COLLECTION_NAME)
public class Campaign extends AbstractIgniteEntity {

    @Id
    private String campaignId;

    /**
     * This method is a getter for id.
     *
     * @return String
     */

    public String getId() {
        return campaignId;
    }

    /**
     * This method is a setter for id.
     *
     * @param campaignId : String
     */

    public void setId(String campaignId) {
        this.campaignId = campaignId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Campaign [id=");
        builder.append(campaignId);
        builder.append("]");
        return builder.toString();
    }

}