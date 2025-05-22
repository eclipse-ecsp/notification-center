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
 * AmazonSNSPushResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.AmazonSNSPushResponse")
public class AmazonSNSPushResponse extends AmazonSNSChannelResponse {

    @SuppressWarnings("checkstyle:ConstantName")
    private static final ChannelType channelType = ChannelType.MOBILE_APP_PUSH;

    /**
     * Constructor.
     */
    public AmazonSNSPushResponse() {
        this(null, null, null);
    }

    /**
     * Constructor.
     *
     * @param userId userId
     * @param pdid   pdid
     */
    public AmazonSNSPushResponse(String userId, String pdid) {
        this(userId, pdid, null);
    }

    /**
     * Constructor.
     *
     * @param userId    userId
     * @param pdid      pdid
     * @param eventData eventData
     */
    public AmazonSNSPushResponse(String userId, String pdid, String eventData) {
        super(userId, pdid, eventData);
        setChannelType(ChannelType.MOBILE_APP_PUSH);
    }



    /**
     * This method is a getter for channeltype.
     *
     * @return ChannelType
     */
    @Override
    public ChannelType getChannelType() {
        return channelType;
    }



    /**
     * This method is a getter for provider.
     *
     * @return String
     */
    @Override
    public String getProvider() {
        return NotificationConstants.AWS_PUSH_PROVIDER;
    }

}