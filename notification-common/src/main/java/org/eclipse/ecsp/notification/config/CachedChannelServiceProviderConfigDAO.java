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

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;

import java.util.Optional;

/**
 * CachedChannelServiceProviderConfigDAO interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface CachedChannelServiceProviderConfigDAO extends IgniteBaseDAO<String, ChannelServiceProviderConfig> {
    public Optional<String> getServiceProviderByChannelNotificationIdAndRegion(ChannelType channelType,
                                                                               String notificationId,
                                                                               String region);
}
