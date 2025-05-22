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

import dev.morphia.annotations.Entity;

import java.io.Serializable;

/**
 * RetryRecord class.
 *
 * @author AMuraleedhar.
 *
 *<p>This class to keep track of the exception that caused retry.It has
 *     retry attempts , max allowed retry and retry interval detail</p>
 */
@Entity(useDiscriminator = false)
public class RetryRecord implements Serializable {

    private static final long serialVersionUID = 4483756764983625454L;

    private String retryException;

    private int maxRetryLimit;

    private int retryCount;

    private long retryIntervalMs;

    /**
     * RetryRecord constructor.
     *
     * @param retryException exception of kind RetryException
     *
     * @param maxRetryLimit int max retry limit
     *
     * @param retryCount int current retry count
     *
     * @param retryIntervalMs long retry interval in ms
     */
    public RetryRecord(String retryException, int maxRetryLimit, int retryCount, long retryIntervalMs) {
        super();
        this.retryException = retryException;
        this.maxRetryLimit = maxRetryLimit;
        this.retryCount = retryCount;
        this.retryIntervalMs = retryIntervalMs;
    }

    public RetryRecord() {
        // Auto-generated constructor stub
    }

    /**
     * Getter for RetryException.
     *
     * @return retryexception
     */
    public String getRetryException() {
        return retryException;
    }

    /**
     * Setter for RetryException.
     *
     * @param retryException the new value
     */
    public void setRetryException(String retryException) {
        this.retryException = retryException;
    }

    /**
     * Getter for MaxRetryLimit.
     *
     * @return maxretrylimit
     */
    public int getMaxRetryLimit() {
        return maxRetryLimit;
    }

    /**
     * Setter for MaxRetryLimit.
     *
     * @param maxRetryLimit the new value
     */
    public void setMaxRetryLimit(int maxRetryLimit) {
        this.maxRetryLimit = maxRetryLimit;
    }

    /**
     * Getter for RetryCount.
     *
     * @return retrycount
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Setter for RetryCount.
     *
     * @param retryCount the new value
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * Getter for RetryIntervalMs.
     *
     * @return retryintervalms
     */
    public long getRetryIntervalMs() {
        return retryIntervalMs;
    }

    /**
     * Setter for RetryIntervalMs.
     *
     * @param retryIntervalMs the new value
     */
    public void setRetryIntervalMs(long retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    @Override
    public String toString() {
        return "RetryRecord [retryException=" + retryException + ", maxRetryLimit=" + maxRetryLimit
            + ", retryCount=" +  retryCount
            + ", retryIntervalMs=" + retryIntervalMs + "]";
    }

}
