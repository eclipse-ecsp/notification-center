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

package org.eclipse.ecsp.notification.db.client;

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.notification.config.CachedChannelServiceProviderConfigDAO;
import org.eclipse.ecsp.notification.config.ChannelServiceProviderConfig;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CachedChannelServiceProviderConfigDaoImpl class.
 */
@Repository
public class CachedChannelServiceProviderConfigDaoImpl
        extends IgniteBaseDAOMongoImpl<String, ChannelServiceProviderConfig>
        implements CachedChannelServiceProviderConfigDAO {

    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(CachedChannelServiceProviderConfigDaoImpl.class);

    private Map<String, ChannelServiceProviderConfig> channelServiceProviderConfigCache = new ConcurrentHashMap<>();

    /**
     * Method to get service provider by channel notification id and region.
     *
     * @param channelType    channelType
     * @param notificationId notificationId
     * @param region         region
     * @return service provider
     */
    @Override
    public Optional<String> getServiceProviderByChannelNotificationIdAndRegion(ChannelType channelType,
                                                                               String notificationId,
                                                                               String region) {
        String id = getId(channelType, notificationId, region);
        ChannelServiceProviderConfig config = channelServiceProviderConfigCache.get(id);
        if (null == config) {
            config = findById(id);
            updateCache(config);
        }

        return config != null ? Optional.of(config.getServiceProvider()) : Optional.empty();
    }

    /**
     * Method to get service provider by channel notification id.
     *
     * @param channelType    channelType
     * @param notificationId notificationId
     * @return service provider
     */
    private String getId(ChannelType channelType, String notificationId, String region) {
        return String.join(NotificationConstants.UNDERSCORE, region.toLowerCase(), channelType.getChannelType(),
                notificationId.toLowerCase());
    }

    /**
     * Method to update cache.
     *
     * @param channelConfig channelConfig
     */
    private void updateCache(final ChannelServiceProviderConfig channelConfig) {

        if (null != channelConfig) {
            LOGGER.debug("Updating Cache for {}", channelConfig.getId());
            channelServiceProviderConfigCache.put(channelConfig.getId(), channelConfig);
        }

    }
}
