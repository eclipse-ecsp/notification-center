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
 * SMTPEmailResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.SMTPEmailResponse")
public class SMTPEmailResponse extends EmailResponse {

    /**
     * Default constructor.
     */
    public SMTPEmailResponse() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param userId String
     */
    public SMTPEmailResponse(String userId) {
        this(userId, null);
    }

    /**
     * Constructor.
     *
     * @param userID String
     * @param pdid String
     */
    public SMTPEmailResponse(String userID, String pdid) {
        this(userID, pdid, null);
    }

    /**
     * Constructor.
     *
     * @param userID String
     * @param pdid String
     * @param eventData String
     */
    public SMTPEmailResponse(String userID, String pdid, String eventData) {
        super(userID, pdid, eventData);
    }


    /**
     * Getter for Provider.
     *
     * @return provider
     */
    @Override
    public String getProvider() {
        return NotificationConstants.SMTP_PROVIDER;
    }
}
