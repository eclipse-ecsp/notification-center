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
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * MQTTBrowserResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.MQTTBrowserResponse")
public class MQTTBrowserResponse extends BrowserResponse {

    /**
     * Default constructor.
     */
    public MQTTBrowserResponse() {
        this(null);
    }

    /**
     * Constructor with userId.
     *
     * @param userId userId
     */
    public MQTTBrowserResponse(String userId) {
        this(userId, null);
    }

    /**
     * Constructor with userId and pdid.
     *
     * @param userId userId
     * @param pdid pdid
     */
    public MQTTBrowserResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * Constructor with userId, pdid and eventData.
     *
     * @param userId userId
     * @param pdid pdid
     * @param eventData eventData
     */
    public MQTTBrowserResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
    }


    /**
     * Getter for Provider.
     *
     * @return provider
     */
    @Override
    public String getProvider() {
        return NotificationConstants.MQTT_PROVIDER;
    }

}
