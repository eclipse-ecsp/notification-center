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

package org.eclipse.ecsp.platform.notification.service;

import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.config.ChannelServiceProviderConfig;
import org.eclipse.ecsp.notification.config.ChannelServiceProviderConfigDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ChannelServiceProviderConfigServiceImpl class.
 */
@Service
public class ChannelServiceProviderConfigServiceImpl implements ChannelServiceProviderConfigService {

    @Autowired
    private ChannelServiceProviderConfigDAO channelServiceProviderConfigDao;

    /**
     * Method to configure the channel service provider.
     *
     * @param configList list of channel service provider config
     * @return boolean
     */
    @Override
    public boolean configure(List<ChannelServiceProviderConfig> configList) {
        boolean isUpdated = false;

        configList.forEach(config -> {
            ChannelServiceProviderConfig channelConfig = (ChannelServiceProviderConfig) config;
            channelConfig.setId(String.join(NotificationConstants.UNDERSCORE, channelConfig.getRegion().toLowerCase(),
                channelConfig.getChannelType().getChannelType(), channelConfig.getNotificationId().toLowerCase()));
        });
        channelServiceProviderConfigDao.saveAll(
            configList.toArray(new ChannelServiceProviderConfig[configList.size()]));
        isUpdated = true;
        return isUpdated;
    }

}
