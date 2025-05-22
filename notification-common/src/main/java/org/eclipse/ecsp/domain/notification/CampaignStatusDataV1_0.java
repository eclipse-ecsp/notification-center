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

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.AbstractEventData;

/**
 * CampaignStatusDataV1_0 eventdata for CampaignStatus event.
 */
@SuppressWarnings("checkstyle:TypeName")
@EventMapping(id = EventID.CAMPAIGN_STATUS_EVENT, version = Version.V1_0)
public class CampaignStatusDataV1_0 extends AbstractEventData {

    private String campaignId;
    private String type;
    private String status;
    private Boolean graceful;

    /**
     * This method is a getter for campaignid.
     *
     * @return String
     */

    public String getCampaignId() {
        return campaignId;
    }

    /**
     * This method is a setter for campaignid.
     *
     * @param campaignId : String
     */

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    /**
     * This method is a getter for type.
     *
     * @return String
     */

    public String getType() {
        return type;
    }

    /**
     * This method is a setter for type.
     *
     * @param type : String
     */

    public void setType(String type) {
        this.type = type;
    }

    /**
     * This method is a getter for status.
     *
     * @return String
     */

    public String getStatus() {
        return status;
    }

    /**
     * This method is a setter for status.
     *
     * @param status : String
     */

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * This method is a getter for graceful.
     *
     * @return Boolean
     */
    public Boolean isGraceful() {
        return graceful;
    }

    /**
     * This method is a setter for graceful.
     *
     * @param graceful : Boolean
     */

    public void setGraceful(Boolean graceful) {
        this.graceful = graceful;
    }
}