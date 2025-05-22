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

import java.util.Optional;

/**
 * RetryTemplate class.
 */
public class RetryTemplate {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RetryTemplate.class);

    private static final int THREE = 3;

    private static final int FIVE_HUNDRED = 500;
    private int maxRetries = THREE;

    /**
     * RetryTemplate constructor.
     */
    public RetryTemplate() {
        this(THREE);
    }

    /**
     * RetryTemplate constructor.
     *
     * @param maxRetries Integer
     */
    public RetryTemplate(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Method to perform retry action.
     *
     * @param ra RetryAction
     * @return RA
     */
    public <R> Optional<R> retry(RetryAction<R> ra) {
        int attemptNum = 1;
        while (attemptNum <= maxRetries) {
            try {
                LOGGER.info("RetryTemplate attempting: {}, attempt: {}", ra.getClass().getName(), attemptNum);
                return Optional.of(ra.perform());
            } catch (Exception e) {
                LOGGER.warn("Exception when retrying " + ra.getClass().getName(), e);
                if (attemptNum == maxRetries) {
                    throw e;
                } else {
                    try {
                        LOGGER.info("Sleeping before retrying: {}", ra.getClass().getName());
                        Thread.sleep((long) FIVE_HUNDRED * attemptNum);
                    } catch (InterruptedException e1) {
                        LOGGER.error("Interrupted when waiting for retry of {}", ra.getClass().getName(), e1);
                        Thread.currentThread().interrupt();
                    }
                }
                attemptNum++;
            }
        }
        return Optional.empty();
    }

}
