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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import static org.eclipse.ecsp.platform.notification.v1.common.Constants.IVM_ACK;
import static org.eclipse.ecsp.platform.notification.v1.common.Constants.IVM_RESPONSE;

/**
 * Factory to return the IVM response handlers.
 *
 * @author AMuraleedhar
 */
@Component
public class IvmResponseHandlerFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * Set application context.
     *
     * @param applicationContext applicationContext
     *
     * @throws BeansException BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Get IVM service handler.
     *
     * @param eventName eventName
     *
     * @return handler
     */
    public IvmResponseHandler getIvmService(String eventName) {

        if (IVM_RESPONSE.equalsIgnoreCase(eventName)) {
            return applicationContext.getBean(VehicleDispositionHandler.class);
        } else if (IVM_ACK.equalsIgnoreCase(eventName)) {
            return applicationContext.getBean(VehicleAckHandler.class);
        }
        throw new IllegalArgumentException(String.format("IVM Handler not found for the eventname %s", eventName));
    }

}
