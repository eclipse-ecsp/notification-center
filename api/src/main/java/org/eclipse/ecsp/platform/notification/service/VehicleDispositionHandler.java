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

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.IVMDispositionResponse;
import org.eclipse.ecsp.domain.notification.IVMResponse;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * Handles vehicle disposition responses by converting them into {@link IgniteEvent} objects
 * for further processing in the notification stream.
 *
 * <p>
 * This service processes incoming {@link IVMDispositionResponse} messages, extracts relevant disposition
 * data, and maps it to an {@link IgniteEventImpl} instance. The resulting event is then forwarded
 * to the stream processor for downstream handling.
 * </p>
 *
 * <p>
 * Used as part of the notification platform to ensure reliable tracking and processing of
 * vehicle message disposition events.
 * </p>
 *
 * @author AMuraleedhar
 */
@Service
public class VehicleDispositionHandler extends AbstractIvmResponseHandler {

    /**
     * This method is used to process the IVM response and convert to the ignite event.
     *
     * @param ivmResponse ivmResponse
     * @return IgniteEvent
     */
    @Override
    public IgniteEvent doProcess(IVMResponse ivmResponse) {
        String vehicleId = ivmResponse.getVehicleId();

        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setEventId(EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH);
        igniteEventImpl.setVersion(Version.V1_0);
        igniteEventImpl.setTimestamp(ivmResponse.getTimestamp());
        igniteEventImpl.setVehicleId(vehicleId);
        igniteEventImpl.setRequestId(ivmResponse.getRequestId());
        igniteEventImpl.setBizTransactionId(ivmResponse.getSessionId());
        igniteEventImpl.setMessageId(ivmResponse.getMessageId());
        igniteEventImpl.setSourceDeviceId(ivmResponse.getSourceDeviceId());
        igniteEventImpl.setCorrelationId(ivmResponse.getCorrelationId());

        VehicleMessageDispositionPublishData data = ((IVMDispositionResponse) ivmResponse)
            .getVehicleMessageDispositionPublish();
        data.setCustomExtension(MAPPER.convertValue(data, Map.class));
        igniteEventImpl.setEventData(data);
        return igniteEventImpl;

    }

}