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

import jakarta.validation.constraints.NotNull;
import org.eclipse.ecsp.utils.Constants;

/**
 * IVMDispositionResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class IVMDispositionResponse extends IVMResponse {
    @NotNull(message = Constants.VALIDATION_MESSAGE)
    private VehicleMessageDispositionPublishData vehicleMessageDispositionPublish;

    /**
     * Getter for VehicleMessageDispositionPublish.
     *
     * @return vehiclemessagedispositionpublish
     */
    public VehicleMessageDispositionPublishData getVehicleMessageDispositionPublish() {
        return vehicleMessageDispositionPublish;
    }

    /**
     * Setter for VehicleMessageDispositionPublish.
     *
     * @param vehicleMessageDispositionPublish the new value
     */
    public void setVehicleMessageDispositionPublish(
        VehicleMessageDispositionPublishData vehicleMessageDispositionPublish) {
        this.vehicleMessageDispositionPublish = vehicleMessageDispositionPublish;
    }

    @Override
    public String toString() {
        return String.format(
            "IVMDispositionResponse [vehicleMessageDispositionPublish=%s, getMessageId()=%s, getSessionId()=%s, "
                + "getRequestId()=%s, getSourceDeviceId()=%s, getTimestamp()=%s, getCorrelationId()=%s]",
            vehicleMessageDispositionPublish, getMessageId(), getSessionId(), getRequestId(), getSourceDeviceId(),
            getTimestamp(), getCorrelationId());
    }

}
