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

package org.eclipse.ecsp.platform.notification.utility;

import org.eclipse.ecsp.platform.notification.utility.service.NotificationConfigUpdateService;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * NotificationConfigUtility launcher class.
 *
 * @author MBadoni
 */
@Component
@PropertySource(value = {"classpath:application.properties"})
@ComponentScan(basePackages = {"org.eclipse"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*[DAO](.*?)"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.eclipse.ecsp.domain.notification.utils.*"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.eclipse.ecsp.notification.dao.*")})
public class NotificationConfigUtility {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationConfigUtility.class);

    @Autowired
    private NotificationConfigUpdateService configUpdateService;

    private void process() {
        configUpdateService.processConfigUpdate();
    }

    /**
     * NotificationConfigUtility main method.
     *
     * @param args args
     */
    public static void main(String[] args) {
        LOGGER.info("Notification Config Utility started");
        try (ConfigurableApplicationContext cnxt = new AnnotationConfigApplicationContext(
            NotificationConfigUtility.class);) {
            NotificationConfigUtility notificationConfigUtility = cnxt.getBean(NotificationConfigUtility.class);
            notificationConfigUtility.process();
        } catch (Exception ex) {
            LOGGER.error("Exception while updating notification config ", ex);
        }
        LOGGER.info("Notification Config Utility completed");
    }

}
