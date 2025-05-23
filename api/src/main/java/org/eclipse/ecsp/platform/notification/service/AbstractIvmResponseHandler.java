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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.IVMResponse;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ExecutionException;


/**
 * Abstract base class for handling IVM (In-Vehicle Messaging) responses.
 *
 * <p>
 * Provides a template for processing vehicle responses, converting them into {@link IgniteEvent} objects,
 * and forwarding these events to a Kafka topic. Subclasses must implement the {@link #doProcess(IVMResponse)}
 * method to define custom response processing logic.
 * </p>
 *
 * <p>
 * This class manages Kafka integration and error handling for event forwarding.
 * </p>
 *
 * @author AMuraleedharan
 */

public abstract class AbstractIvmResponseHandler implements IvmResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIvmResponseHandler.class);

    /**
     * Jackson ObjectMapper instance.
     */
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private KafkaService kafkaService;

    @Value("${kafka.sink.topic}")
    private String topic;

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    /**
     * Do process.
     *
     * @param ivmResponse the ivm response
     * @return the ignite event
     */
    public abstract IgniteEvent doProcess(IVMResponse ivmResponse);

    /**
     * Process ivm response.
     *
     * @param ivmResponse the ivm response
     */
    @Override
    public void processIvmResponse(IVMResponse ivmResponse) {
        IgniteEvent event = doProcess(ivmResponse);
        sendIgniteEvent(event.getVehicleId(), event);
    }

    /**
     * Send ignite event.
     *
     * @param vehicleId the vehicle id
     * @param igniteEvent the ignite event
     */
    private void sendIgniteEvent(String vehicleId, IgniteEvent igniteEvent) {
        try {

            LOGGER.info("Forwarding the ivmresponse {} for vehicleId {} to topic {}",
                igniteEvent, vehicleId, topic);
            kafkaService.sendIgniteEvent(vehicleId, igniteEvent, topic);
        } catch (ExecutionException e) {
            LOGGER.error("Unable to forward the ivmResponse {}", e.getMessage());
        }
    }
}
