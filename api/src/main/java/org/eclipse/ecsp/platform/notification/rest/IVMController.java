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

package org.eclipse.ecsp.platform.notification.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.eclipse.ecsp.domain.notification.IVMAckResponse;
import org.eclipse.ecsp.domain.notification.IVMDispositionResponse;
import org.eclipse.ecsp.platform.notification.service.IvmResponseHandler;
import org.eclipse.ecsp.platform.notification.service.IvmResponseHandlerFactory;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static org.eclipse.ecsp.platform.notification.v1.common.Constants.IVM_ACK;
import static org.eclipse.ecsp.platform.notification.v1.common.Constants.IVM_RESPONSE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * IVMController class.
 * This controller exposes the APIs to capture IVM response and acks from Ignite
 * NC/third party and process to forward them to NC stream processor
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@RestController
public class IVMController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IVMController.class);
    public static final String REGEX = "[\r\n]";

    @Autowired
    private IvmResponseHandlerFactory ivmResponseHandlerFactory;

    /**
     * This API capture IVM response third party and process to forward them to stream processor.
     */
    @PostMapping(value = "v1/vehicles/{vehicleId}/ivmResponse", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to recieve IVM response",
        description = "Notification Center Api is used to recieve IVM response",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> postIvmResponse(@RequestHeader(value = "RequestId") String requestId,
                                                  @RequestHeader(value = "SessionId", required = false)
                                                  String sessionId,
                                                  @RequestHeader(value = "ClientRequestId", required = false)
                                                  String clientRequestId,
                                                  @PathVariable("vehicleId") String vehicleId,
                                                  @RequestBody @Valid IVMDispositionResponse ivmDispositionResponse) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Received ivmResponse for vehicle %s and Event %s",
                    vehicleId, ivmDispositionResponse));
        }
        ivmDispositionResponse.setVehicleId(vehicleId);
        IvmResponseHandler ivmResponseHandler = ivmResponseHandlerFactory.getIvmService(IVM_RESPONSE);
        ivmResponseHandler.processIvmResponse(ivmDispositionResponse);

        return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId, sessionId, requestId), HttpStatus.ACCEPTED);
    }

    /**
     * This API captures IVM Ack from third party and process to forward them to stream processor.
     */
    @PostMapping(value = "v1/vehicles/{vehicleId}/ivmAck", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to recieve IVM ACK",
        description = "Notification Center Api is used to recieve IVM ACK",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> postIvmAck(@RequestHeader(value = "RequestId") String requestId,
                                             @RequestHeader(value = "SessionId", required = false) String sessionId,
                                             @RequestHeader(value = "ClientRequestId", required = false)
                                             String clientRequestId,
                                             @PathVariable("vehicleId") String vehicleId,
                                             @RequestBody @Valid IVMAckResponse ivmAckResponse) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received ivmAck for vehicle {} {}", vehicleId.replaceAll(REGEX, ""),
                    ivmAckResponse.toString().replaceAll(REGEX, ""));
        }
        ivmAckResponse.setVehicleId(vehicleId);
        IvmResponseHandler ivmResponseHandler = ivmResponseHandlerFactory.getIvmService(IVM_ACK);
        ivmResponseHandler.processIvmResponse(ivmAckResponse);

        return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId, sessionId, requestId), HttpStatus.ACCEPTED);
    }

}
