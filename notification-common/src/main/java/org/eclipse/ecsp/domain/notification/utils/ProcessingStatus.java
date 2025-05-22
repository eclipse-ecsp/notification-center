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

package org.eclipse.ecsp.domain.notification.utils;

import dev.morphia.annotations.Entity;

/**
 * ProcessingStatus class.
 */
@Entity(useDiscriminator = false)
public class ProcessingStatus {

    /**
     * StatusType enum.
     */
    public enum StatusType {
            RECURRING, VACATION
    }

    private String status;
    private String errorMessage;

    /**
     * ProcessingStatus constructor.
     *
     * @param status status
     * @param errorMessage errorMessage
     */
    public ProcessingStatus(String status, String errorMessage) {
        super();
        this.status = status;
        this.errorMessage = errorMessage;
    }

    /**
     * Get status.
     *
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set status.
     *
     * @param status status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get errorMessage.
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set errorMessage.
     *
     * @param errorMessage errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * To string.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProcessingStatus [status=");
        builder.append(status);
        builder.append(", errorMessage=");
        builder.append(errorMessage);
        builder.append("]");
        return builder.toString();
    }
}
