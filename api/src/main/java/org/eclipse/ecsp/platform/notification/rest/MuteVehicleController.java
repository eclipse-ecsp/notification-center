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
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.platform.notification.dto.DataResponseWrapper;
import org.eclipse.ecsp.platform.notification.dto.MuteVehicleDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.service.MuteVehicleService;
import org.eclipse.ecsp.security.Security;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.MUTE_CONFIG_CREATE_SUCCESS;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.MUTE_CONFIG_DELETE_SUCCESS;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.MUTE_CONFIG_FIND_SUCCESS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * Mute Notification controller.
 *
 * <p>There are cases when system admin would like to mute notification for a specific
 * vehicle by group or by channel for specific period or until further notice.
 *
 * <p>For example when a vehicle is not active for a long time system admin might want to stop curtain type of
 * notification until vehicle will become active again.
 *
 * <p>The mute is admin only operation and will override any configuration done by the user.
 *
 * @author Agrahari, Ayush
 * @since 2.24
 */
@RestController
@Slf4j
public class MuteVehicleController {

    /**
     * RequestId for header.
     */
    private static final String REQUEST_ID = "RequestId";

    /**
     * {@link MuteVehicleService}.
     */
    private final MuteVehicleService muteVehicleService;

    /**
     * Parameterized c'tor.
     *
     * @param muteVehicleService {@link MuteVehicleService}
     */
    public MuteVehicleController(MuteVehicleService muteVehicleService) {
        this.muteVehicleService = muteVehicleService;
    }

    /**
     * Create or Update mute notification configuration.
     *
     * @param muteVehicleDto {@link MuteVehicleDto}
     * @return DataResponseWrapper of MuteVehicleDto
     */
    @PutMapping(value = "/v1/notifications/mute", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add or Update Mute Notification Config",
        description = "Add or Update Mute Notification Config",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public DataResponseWrapper<MuteVehicleDto> createMuteConfig(@RequestHeader(value = "RequestId") String requestId,
                @RequestHeader(value = "SessionId", required = false)
                String sessionId,
                @RequestHeader(value = "ClientRequestId", required = false)
                String clientRequestId,
                @RequestBody @Valid @NotNull MuteVehicleDto muteVehicleDto) {
        log.info("## createMuteConfig - requestId: {}, sessionId: {},  clientRequestId: {}, muteVehicleDto: {}",
            StringUtils.normalizeSpace(requestId), StringUtils.normalizeSpace(sessionId),
            StringUtils.normalizeSpace(clientRequestId), StringUtils.normalizeSpace(String.valueOf(muteVehicleDto)));
        MuteVehicle muteVehicle = muteVehicleService.createMuteConfig(muteVehicleDto);
        return DataResponseWrapper.ok(MuteVehicleDto.from(muteVehicle))
            .requestId(requestId)
            .rootResponseWrapperMessage(
                ResponseWrapper.Message.of(MUTE_CONFIG_CREATE_SUCCESS.getCode(), MUTE_CONFIG_CREATE_SUCCESS.getReason(),
                    MUTE_CONFIG_CREATE_SUCCESS.getMessage()))
            .build();
    }

    /**
     * Find mute notification configuration by VehicleId (a.k.a VIN).
     *
     * @param vehicleId VehicleId a.k.a VIN
     * @return DataResponseWrapper of MuteVehicleDto
     */
    @GetMapping(value = "/v1/vehicles/{vehicleId}/notifications/mute")
    @Operation(summary = "Get Mute Notification Config for vehicle",
        description = "Get Mute Notification Config for vehicle", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public DataResponseWrapper<MuteVehicleDto> findMuteConfig(@RequestHeader(value = "RequestId") String requestId,
          @RequestHeader(value = "SessionId", required = false)
          String sessionId,
          @RequestHeader(value = "ClientRequestId", required = false)
          String clientRequestId,
          @PathVariable String vehicleId) {
        log.info("## findMuteConfig - requestId: {}, sessionId: {},  clientRequestId: {}, muteVehicleDto: {}",
            StringUtils.normalizeSpace(requestId), StringUtils.normalizeSpace(sessionId),
            StringUtils.normalizeSpace(clientRequestId), StringUtils.normalizeSpace(vehicleId));
        MuteVehicle muteVehicle = muteVehicleService.getMuteConfigById(vehicleId);
        return DataResponseWrapper.ok(MuteVehicleDto.from(muteVehicle))
            .requestId(requestId)
            .rootResponseWrapperMessage(
                ResponseWrapper.Message.of(MUTE_CONFIG_FIND_SUCCESS.getCode(), MUTE_CONFIG_FIND_SUCCESS.getReason(),
                    MUTE_CONFIG_FIND_SUCCESS.getMessage()))
            .build();
    }

    /**
     * Delete mute notification configuration by VehicleId (a.k.a VIN).
     *
     * <p>It return 404 if vehicle is not valid or does not exist
     *
     * @param vehicleId VehicleId a.k.a VIN.
     * @return ResponseWrapper
     */
    @DeleteMapping(value = "/v1/vehicles/{vehicleId}/notifications/mute")
    @Operation(summary = "Delete Mute Notification Config for vehicle",
        description = "Delete Mute Notification Config for vehicle", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseWrapper<Void> deleteMuteConfig(@RequestHeader(value = "RequestId") String requestId,
                                                  @RequestHeader(value = "SessionId", required = false)
                                                  String sessionId,
                                                  @RequestHeader(value = "ClientRequestId", required = false)
                                                  String clientRequestId,
                                                  @PathVariable String vehicleId) {
        log.info("## deleteMuteConfig - requestId: {}, sessionId: {},  clientRequestId: {}, vehicleId: {}",
            StringUtils.normalizeSpace(requestId), StringUtils.normalizeSpace(sessionId),
            StringUtils.normalizeSpace(clientRequestId), StringUtils.normalizeSpace(vehicleId));
        muteVehicleService.deleteMuteConfigById(vehicleId);
        return ResponseWrapper.ok()
            .requestId(requestId)
            .rootMessage(
                ResponseWrapper.Message.of(MUTE_CONFIG_DELETE_SUCCESS.getCode(), MUTE_CONFIG_DELETE_SUCCESS.getReason(),
                    MUTE_CONFIG_DELETE_SUCCESS.getMessage()))
            .build();
    }

    /**
     * Exception handler for InvalidInputException.
     */
    @ExceptionHandler({InvalidInputException.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseWrapper<Void> invalidInput(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * Exception handler for NotFoundException.
     */
    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(code = NOT_FOUND)
    public ResponseWrapper<Void> notFoundException(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.notFound()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * Exception handler for Exception.
     */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    public ResponseWrapper<Void> internalServerError(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }
}
