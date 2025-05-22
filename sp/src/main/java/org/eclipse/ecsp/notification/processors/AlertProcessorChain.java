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

package org.eclipse.ecsp.notification.processors;

import lombok.Getter;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Processes the alerts through a chain of filters that transform the alert in
 * some way or other, typically enriching it.
 *
 * @author ssasidharan
 */
@Component
@Getter
public class AlertProcessorChain {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(AlertProcessorChain.class);

    private final List<NotificationProcessor> processors;

    /**
     * Constructor.
     *
     * @param processors List of NotificationProcessor
     */
    @Autowired
    public AlertProcessorChain(List<NotificationProcessor> processors) {
        this.processors = processors;
    }

    /**
     * entrypoint method for the processor.
     *
     * @param alert AlertsInfo
     */
    public void process(AlertsInfo alert) {
        LOGGER.debug("running processors " + processors.toString());
        for (NotificationProcessor p : processors) {
            p.process(alert);
        }
    }

}
