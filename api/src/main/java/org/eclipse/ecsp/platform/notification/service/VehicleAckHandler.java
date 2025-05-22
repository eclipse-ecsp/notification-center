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
import org.eclipse.ecsp.domain.notification.IVMAckResponse;
import org.eclipse.ecsp.domain.notification.IVMResponse;
import org.eclipse.ecsp.domain.notification.VehicleMessageAckData;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * VehicleAckHandler class.
 *
 * @author AMuraleedhar
 *
 *     <p>This is a vehicle acknowledgement handler which process the ack and
 *     converts to ignite event to forward to the stream processor</p>
 */
@Service
public class VehicleAckHandler extends AbstractIvmResponseHandler {

    /**
     * This method is used to process the vehicle ack response and convert to ignite event.
     *
     * @param ivmResponse the ivm response
     * @return the ignite event
     */
    @Override
    public IgniteEvent doProcess(IVMResponse ivmResponse) {
        String vehicleId = ivmResponse.getVehicleId();

        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setEventId(EventID.VEHICLE_MESSAGE_ACK);
        igniteEventImpl.setVersion(Version.V1_0);
        igniteEventImpl.setTimestamp(ivmResponse.getTimestamp());
        igniteEventImpl.setVehicleId(vehicleId);
        igniteEventImpl.setRequestId(ivmResponse.getRequestId());
        igniteEventImpl.setBizTransactionId(ivmResponse.getSessionId());
        igniteEventImpl.setMessageId(ivmResponse.getMessageId());
        igniteEventImpl.setSourceDeviceId(ivmResponse.getSourceDeviceId());
        igniteEventImpl.setCorrelationId(ivmResponse.getCorrelationId());

        VehicleMessageAckData vehicleMessageAck = ((IVMAckResponse) ivmResponse).getVehicleMessageAck();
        vehicleMessageAck.setCustomExtension(MAPPER.convertValue(vehicleMessageAck, Map.class));
        igniteEventImpl.setEventData(vehicleMessageAck);
        return igniteEventImpl;

    }

}
