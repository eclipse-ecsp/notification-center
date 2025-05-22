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
import jakarta.validation.constraints.NotBlank;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.service.PlaceholderServiceImpl;
import org.eclipse.ecsp.security.Security;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.PLACEHOLDER_SUCCESS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Notifiation placeholder CRUD APIs.
 */
@RestController
public class PlaceholderController {

    final PlaceholderServiceImpl placeholderService;

    /**
     * PlaceholderController constructor.
     */
    public PlaceholderController(PlaceholderServiceImpl placeholderService) {
        this.placeholderService = placeholderService;
    }

    /**
     * Import notification placeholder data.
     */
    @PostMapping(value = "v1/notification/placeholder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Notification Center Api is used to import notification placeholders ",
        description = "Notification Center Api is used to import notification placeholders ", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseWrapper<Void> importPlaceholders(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @RequestParam("file") @Valid MultipartFile file) throws IOException {

        placeholderService.importPlaceholders(file);

        return ResponseWrapper.ok()
            .requestId(requestId)
            .rootMessage(ResponseWrapper.Message.of(PLACEHOLDER_SUCCESS.getCode(), PLACEHOLDER_SUCCESS.getReason(),
                PLACEHOLDER_SUCCESS.getMessage()))
            .build();
    }

    /**
     * Export notification placeholder data.
     */
    @GetMapping(value = "v1/notification/placeholder/{key}")
    @Operation(summary = "Notification Center Api is used to export notification placeholders for the specified key",
        description = "Notification Center Api is used to export notification placeholders for the specified key",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<byte[]> exportPlaceholders(
        @RequestHeader(value = "RequestId", required = true) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("key") @Valid @NotBlank String key) {
        byte[] notificationPlaceholders = placeholderService.exportPlaceholders(key);

        return ResponseEntity.ok()
            .header("requestId", requestId)
            .header("Content-Disposition", "attachment; filename=ignite_notification_placeholder_export.csv")
            .header("Content-Type", "application/csv")
            .body(notificationPlaceholders);

    }

    /**
     * Delet notification placeholder data by key.
     */
    @DeleteMapping(value = "v1/notification/placeholder/{key}")
    @Operation(summary = "API to delete all notification placeholders associated with the specified key ",
        description = "API to delete all notification placeholders associated with the specified key ",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseWrapper<Void> deletePlaceholder(
        @RequestHeader(value = "RequestId", required = true) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("key") @Valid @NotBlank String key) {
        placeholderService.deletePlaceholder(key);

        return ResponseWrapper.ok()
            .requestId(requestId)
            .rootMessage(ResponseWrapper.Message.of(PLACEHOLDER_SUCCESS.getCode(), PLACEHOLDER_SUCCESS.getReason(),
                PLACEHOLDER_SUCCESS.getMessage()))
            .build();
    }

    /**
     * InvalidInputFileException handler.
     */
    @ExceptionHandler({InvalidInputFileException.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseWrapper<Void> invalidInput(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader("RequestId"))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * NotFoundException handler.
     */
    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(code = NOT_FOUND)
    public ResponseWrapper<Void> notFoundException(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.notFound()
            .requestId(request.getHeader("RequestId"))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }
}
