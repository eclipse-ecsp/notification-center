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
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * ApiPushResponse class.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.ApiPushResponse")
public class ApiPushResponse extends AbstractChannelResponse {

    /**
     * This method is a constructor for ApiPushResponse.
     */
    public ApiPushResponse() {
        this(null, null);
    }

    /**
     * This method is a constructor for ApiPushResponse.
     *
     * @param userId String
     * @param pdid String
     */
    public ApiPushResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * This method is a constructor for ApiPushResponse.
     *
     * @param userId String
     * @param pdid String
     * @param eventData String
     */
    public ApiPushResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
        setChannelType(ChannelType.API_PUSH);
    }



    /**
     * This method is a getter for provider.
     *
     * @return String
     */
    @Override
    public String getProvider() {
        return NotificationConstants.API_PUSH_PROVIDER;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ApiPushResponse [toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}