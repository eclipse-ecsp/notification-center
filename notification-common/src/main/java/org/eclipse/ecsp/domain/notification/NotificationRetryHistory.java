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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.util.List;

/**
 * NotificationRetryHistory class.
 */
@Entity(value = "notificationRetryHistory")
@Indexes(value = {@Index(fields = {@Field(value = "vehicleId"), @Field(value = "requestId")}),
    @Index(fields = @Field(value = "lastUpdatedTime"), options = @IndexOptions(expireAfterSeconds = 16000000))})
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRetryHistory extends AbstractIgniteEntity {

    @Id
    private String requestId;

    private String vehicleId;

    private List<RetryRecord> retryRecordsList;


    /**
     * Getter for VehicleId.
     *
     * @return vehicleid
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Setter for VehicleId.
     *
     * @param vehicleId the new value
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Getter for RequestId.
     *
     * @return requestid
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter for RequestId.
     *
     * @param requestId the new value
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter for RetryRecordsList.
     *
     * @return retryrecordslist
     */
    public List<RetryRecord> getRetryRecordsList() {
        return retryRecordsList;
    }

    /**
     * Setter for RetryRecordsList.
     *
     * @param retryRecordsList the new value
     */
    public void setRetryRecordsList(List<RetryRecord> retryRecordsList) {
        this.retryRecordsList = retryRecordsList;
    }

    @Override
    public String toString() {
        return String.format("NotificationRetryHistory [requestId=%s, vehicleId=%s, retryRecordsList=%s]", requestId,
            vehicleId,
            retryRecordsList);
    }

}
