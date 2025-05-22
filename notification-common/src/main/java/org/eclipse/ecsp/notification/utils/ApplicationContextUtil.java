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

package org.eclipse.ecsp.notification.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * ApplicationContextUtil class.
 */
@Component("NotificationApplicationContextUtil")
public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext appContext;

    /**
     * setApplicationContext method.
     *
     * @param applicationContext ApplicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        setAppContext(applicationContext);
    }

    /**
     * getApplicationContext method.
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    /**
     * setAppContext method.
     *
     * @param applicationContext ApplicationContext
     */
    private static void setAppContext(ApplicationContext applicationContext) {
        appContext = applicationContext;
    }
}
