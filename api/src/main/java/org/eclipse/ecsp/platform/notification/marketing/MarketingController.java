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

package org.eclipse.ecsp.platform.notification.marketing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.eclipse.ecsp.domain.notification.MarketingName;
import org.eclipse.ecsp.security.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * MarketingController class.
 * APIs for marketing names import.
 */
@RestController
public class MarketingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketingController.class);

    @Autowired
    private MarketingService service;


    /**
     * Import marketing names.
     */
    @PatchMapping(value = "/v1/notification/marketingNames", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to update marketing names",
        description = "Notification Center Api is used to update marketing names", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> updateMarketingNames(
            @RequestHeader(value = "RequestId", required = false) String requestId,
            @RequestHeader(value = "SessionId", required = false) String sessionId,
            @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
            @RequestBody @Valid List<MarketingName> marketingNameList) {

        boolean isUpdated = service.updateMarketingName(marketingNameList);

        HttpStatus httpStatus = isUpdated ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

        LOGGER.debug("Updating marketing Names  httpStatus={}", httpStatus);

        return new ResponseEntity<>(httpStatus);

    }

}
