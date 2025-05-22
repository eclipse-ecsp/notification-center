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

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.eclipse.ecsp.notification.config.ChannelServiceProviderConfig;
import org.eclipse.ecsp.platform.notification.service.ChannelServiceProviderConfigService;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * ChannelServiceProviderConfigController class.
 * API to update the channel service providers.
 */

@RestController
public class ChannelServiceProviderConfigController {

    private static final IgniteLogger LOGGER =
        IgniteLoggerFactory.getLogger(ChannelServiceProviderConfigController.class);

    @Autowired
    private ChannelServiceProviderConfigService configService;

    /**
     * Update channel service provider configs.
     */
    @PostMapping(value = "/v1/channelServiceProvider/configs", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseEntity<Void> updateNotificationStaticConfigs(
        @RequestHeader(value = "RequestId", required = false) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @RequestBody @Valid List<ChannelServiceProviderConfig> configList) {

        boolean isUpdated = configService.configure(configList);
        HttpStatus httpStatus = isUpdated ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;

        LOGGER.info("Updating notification channel serviceprovider configs httpStatus={}", httpStatus);

        return new ResponseEntity<>(httpStatus);

    }
}
