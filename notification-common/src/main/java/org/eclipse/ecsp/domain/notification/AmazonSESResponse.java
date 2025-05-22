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
import dev.morphia.annotations.Entity;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;

/**
 * AmazonSESResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.AmazonSESResponse")
public class AmazonSESResponse extends EmailResponse {

    @JsonProperty(value = "messageId")
    private String messageId;

    /**
     * Constructor.
     */
    public AmazonSESResponse() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param userId userId
     */
    public AmazonSESResponse(String userId) {
        this(userId, null);
    }

    /**
     * Constructor.
     *
     * @param userId userId
     * @param pdid pdid
     */
    public AmazonSESResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * Constructor.
     *
     * @param userId userId
     * @param pdid pdid
     * @param eventData eventData
     */
    public AmazonSESResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
    }

    /**
     * Set message id.
     *
     * @param messageId message id
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Get message id.
     *
     * @return message id
     */
    public String getMessageId() {
        return this.messageId;
    }

    /**
     * Get provider.
     *
     * @return provider
     */
    @Override
    public String getProvider() {
        return NotificationConstants.AWS_SES_PROVIDER;
    }
}
