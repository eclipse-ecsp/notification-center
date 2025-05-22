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

package org.eclipse.ecsp.notification.config;

import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.platform.IgnitePlatform;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NotificationIgnitePlatformImpl class.
 */
@Component
public class NotificationIgnitePlatformImpl implements IgnitePlatform {

    private static IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationIgnitePlatformImpl.class);

    @Value("${notification.ignite.event.platformId}")
    private String platformId;

    /**
     * Method to get platform Id.
     *
     * @param streamProcessingContext streamProcessingContext
     * @param rec                     rec
     * @return platform Id
     */
    @Override
    public String getPlatformId(StreamProcessingContext<IgniteKey<?>, IgniteEvent> streamProcessingContext,
                                Record<IgniteKey<?>, IgniteEvent> rec) {
        LOGGER.debug("Getting platform Id for the rec {} as {} ", rec, platformId);
        return platformId;
    }
}
