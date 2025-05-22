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
 * AmazonSNSSMSResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.domain.notification.AmazonSNSSMSResponse")
public class AmazonSNSSMSResponse extends AmazonSNSChannelResponse {

    @SuppressWarnings("checkstyle:ConstantName")
    private static final ChannelType channelType = ChannelType.SMS;

    /**
     * Constructor.
     */
    public AmazonSNSSMSResponse() {
        this(null, null, null);
    }

    /**
     * Constructor.
     *
     * @param userID userID
     * @param pdid   pdid
     */
    public AmazonSNSSMSResponse(String userID, String pdid) {
        this(userID, pdid, null);
    }

    /**
     * Constructor.
     *
     * @param userID    userID
     * @param pdid      pdid
     * @param eventData eventData
     */
    public AmazonSNSSMSResponse(String userID, String pdid, String eventData) {
        super(userID, pdid, eventData);
        setChannelType(ChannelType.SMS);
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
        return NotificationConstants.AWS_SMS_PROVIDER;
    }

}