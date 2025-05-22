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

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.AbstractEventData;
import org.eclipse.ecsp.notification.config.NotificationConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationSettingDataV1_0 class.
 */
@SuppressWarnings("checkstyle:TypeName")
@EventMapping(id = EventID.NOTIFICATION_SETTINGS, version = Version.V1_0)
public class NotificationSettingDataV1_0 extends AbstractEventData {
    private static final long serialVersionUID = 1L;
    private List<NotificationConfig> notificationConfigs;

    /**
     * Default constructor.
     */
    public NotificationSettingDataV1_0() {
        notificationConfigs = new ArrayList<>();
    }

    /**
     * Constructor with notificationConfigs.
     *
     * @param notificationConfigs the notificationConfigs
     */
    public NotificationSettingDataV1_0(List<NotificationConfig> notificationConfigs) {
        super();
        this.notificationConfigs = notificationConfigs;
    }

    /**
     * Getter for NotificationConfigs.
     *
     * @return notificationconfigs
     */
    public List<NotificationConfig> getNotificationConfigs() {
        return notificationConfigs;
    }

    /**
     * Setter for NotificationConfigs.
     *
     * @param notificationConfigs the new value
     */
    public void setNotificationConfigs(List<NotificationConfig> notificationConfigs) {
        this.notificationConfigs = notificationConfigs;
    }

    /**
     * Add NotificationConfig.
     *
     * @param config the config
     */
    public void addNotificationConfig(NotificationConfig config) {
        notificationConfigs.add(config);
    }

}
