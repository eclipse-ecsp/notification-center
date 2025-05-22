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

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.morphia.annotations.Entity;

import java.time.Instant;
import java.util.Date;

import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status;

/**
 * StatusHistoryRecord class.
 */
@Entity(useDiscriminator = false)
public class StatusHistoryRecord {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date time;

    private Status status;

    private String correlationId;

    /**
     * StatusHistoryRecord constructor.
     *
     * @param value status
     */
    public StatusHistoryRecord(Status value) {
        this.time = Date.from(Instant.now());
        this.status = value;
    }

    /**
     * StatusHistoryRecord constructor.
     *
     * @param value status
     *
     * @param correlationId correlationId
     */
    public StatusHistoryRecord(Status value, String correlationId) {
        this.time = Date.from(Instant.now());
        this.status = value;
        this.correlationId = correlationId;
    }

    /**
     * Constructor.
     */
    public StatusHistoryRecord() {
    }

    /**
     * Get status.
     *
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Get time.
     *
     * @return time
     */
    public Date getTime() {
        return (Date) time.clone();
    }

    /**
     * Get correlationId.
     *
     * @return correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * To string.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "StatusHistoryRecord [Date=" + time + ", Status=" + status + ", CorrelationId=" + correlationId + "]";
    }
}
