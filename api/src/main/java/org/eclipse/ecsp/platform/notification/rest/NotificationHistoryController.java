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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.utils.UserService;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.platform.notification.dto.CampaignSummary;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNonRegisteredVehicle;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.exceptions.UserIdNotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.eclipse.ecsp.platform.notification.service.NotificationHistoryService;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.utils.ApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Controller for get notification status details.
 *
 * @author AMadan
 */

@RestController
public class NotificationHistoryController {
    /**
     * The constant NOTIFICATIONS.
     */
    public static final String NOTIFICATIONS = "/notifications/";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHistoryController.class);

    private static final String STATUS = "status";
    private static final String FULL = "full";

    private static final List<String> CONTENT_LIST = new ArrayList<>();

    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * The constant FAILED_TO_GET_NOTIFICATION_STATUS_DETAILS_BY_USER_ID_VEHICLE_ID_AND_PLATFORM_RESPONSE_ID.
     */
    public static final String
            FAILED_TO_GET_NOTIFICATION_STATUS_DETAILS_BY_USER_ID_VEHICLE_ID_AND_PLATFORM_RESPONSE_ID =
            "Failed to get notification status details by user id, vehicle id and platform response id: ";
    /**
     * The constant CONTENT_FULL.
     */
    public static final String CONTENT_FULL = "?content=full";
    /**
     * The constant REGEX.
     */
    public static final String REGEX = "[\r\n]";

    private static final String UNTIL_MUST_BE_GREATER_THEN_SINCE = "until must be greater then since";

    static {
        CONTENT_LIST.add(STATUS);
        CONTENT_LIST.add(FULL);

        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        MAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    private final NotificationHistoryService notificationHistoryService;
    private final UserService userService;
    private final VehicleService vehicleService;

    /**
     * NotificationHistoryController constructor.
     */
    public NotificationHistoryController(NotificationHistoryService notificationHistoryService, UserService userService,
                                         VehicleService vehicleService) {
        this.notificationHistoryService = notificationHistoryService;
        this.userService = userService;
        this.vehicleService = vehicleService;
    }

    /**
     * Get notification status by platformId.
     */
    @GetMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/notifications/{platformResponseId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to get notification",
        description = "Notification Center Api is used to get notification", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNotificationStatus(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("userId") String userId, @PathVariable("vehicleId") String vehicleId,
        @PathVariable("platformResponseId") String platformResponseId,
        @RequestParam(value = "content") String content,
        @RequestParam(value = "isNonRegisteredVehicle", defaultValue = "false")
        boolean isNonRegisteredVehicle) throws JsonProcessingException {

        content = content != null ? content.replaceAll(REGEX, "") : null;

        LOGGER.debug(
            "getNotificationStatus - requestId: {} sessionId: {} clientRequestId: {} userId: {} vehicleId: {} "
                + "platformResponseId: {} content: {} isNonRegisteredVehicle{}",
            requestId, sessionId,
            clientRequestId, userId,
            vehicleId,
            platformResponseId, content,
            isNonRegisteredVehicle);
        validateUserExists(userId);
        validateVehiclesExistence(vehicleId, isNonRegisteredVehicle);

        NotificationChannelDetails channelDetails = null;

        if (!CONTENT_LIST.contains(content)) {
            return new ResponseEntity<>(StringUtils.EMPTY, HttpStatus.BAD_REQUEST);
        }
        try {
            channelDetails = notificationHistoryService.getNotificationStatus(platformResponseId, content, vehicleId);
        } catch (Exception ex) {
            LOGGER.error(FAILED_TO_GET_NOTIFICATION_STATUS_DETAILS_BY_USER_ID_VEHICLE_ID_AND_PLATFORM_RESPONSE_ID,
                ex);
        }

        HttpStatus httpStatus = null == channelDetails ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        String response = MAPPER.writeValueAsString(channelDetails);
        return new ResponseEntity<>(response,
            ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId), httpStatus);
    }


    /**
     * getNotificationStatusByVehicleIdAndPlatformId API.
     */
    @GetMapping(value = "/v1/vehicles/{vehicleId}/notifications/{platformResponseId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to get notification by passing vin and platform id",
        description = "Notification Center Api is used to get notification by passing vin and platform id",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNotificationStatusByVehicleIdAndPlatformId(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("vehicleId") String vehicleId, @PathVariable("platformResponseId") String platformResponseId,
        @RequestParam(value = "content") String content,
        @RequestParam(value = "isNonRegisteredVehicle", defaultValue = "false")
        boolean isNonRegisteredVehicle) throws JsonProcessingException {
        content = content != null ? content.replaceAll(REGEX, "") : null;
        LOGGER.debug(
            "getNotificationStatusByVehicleIdAndPlatformId - requestId: {} sessionId: {} clientRequestId: {} "
                + "vehicleId: {} platformResponseId: {} content: {} isNonRegisteredVehicle{}",
            requestId, sessionId,
            clientRequestId, vehicleId,
            platformResponseId, content,
            isNonRegisteredVehicle);

        validateVehiclesExistence(vehicleId, isNonRegisteredVehicle);

        NotificationChannelDetails channelDetails = null;

        if (!CONTENT_LIST.contains(content)) {
            return new ResponseEntity<>(StringUtils.EMPTY, HttpStatus.BAD_REQUEST);
        }

        try {
            channelDetails = notificationHistoryService.getNotificationStatus(platformResponseId, content, vehicleId);
        } catch (Exception ex) {
            LOGGER.error("Failed to get notification status details by vehicle id and platform response id: ", ex);
        }

        HttpStatus httpStatus = (null == channelDetails) ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        String response = MAPPER.writeValueAsString(channelDetails);
        return new ResponseEntity<>(response,
            ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId), httpStatus);
    }

    /**
     * getNotificationStatusByUserIdAndPlatformId API.
     */
    @GetMapping(value = "/v1/users/{userId}/notifications/{platformResponseId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to get notification status",
        description = "Notification Center Api is used to get notification status", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNotificationStatusByUserIdAndPlatformId(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("userId") String userId,
        @PathVariable("platformResponseId") String platformResponseId,
        @RequestParam(value = "content") String content) throws JsonProcessingException {
        content = content != null ? content.replaceAll(REGEX, "") : null;
        LOGGER.debug(
            "getNotificationStatusByUserIdAndPlatformId - requestId: {} sessionId: {} clientRequestId: {} userId: {} "
                + "platformResponseId: {} content: {} ",
            requestId, sessionId,
            clientRequestId, userId,
            platformResponseId, content);
        validateUserExists(userId);
        NotificationChannelDetails channelDetails = null;

        if (!CONTENT_LIST.contains(content)) {
            return new ResponseEntity<>(StringUtils.EMPTY, HttpStatus.BAD_REQUEST);
        }
        try {
            channelDetails = notificationHistoryService.getNotificationStatus(platformResponseId, content, null);
        } catch (Exception ex) {
            LOGGER.error(
                "Failed to get notification status details by user Id: {} and platform response id:{} . Exception  ",
                userId,
                platformResponseId, ex);
        }

        HttpStatus httpStatus = null == channelDetails ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        String response = MAPPER.writeValueAsString(channelDetails);
        return new ResponseEntity<>(response,
            ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId), httpStatus);
    }

    /**
     * getNotificationStatusByPlatformId API.
     */
    @GetMapping(value = "/v1/notifications/{platformResponseId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseEntity<NotificationChannelDetails> getNotificationStatusByPlatformId(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("platformResponseId") String platformResponseId,
        @RequestParam(value = "content") String content) {
        content = content != null ? content.replaceAll(REGEX, "") : null;
        LOGGER.debug(
            "getNotificationStatusByUserIdAndPlatformId - requestId: {} sessionId: {} "
                + "clientRequestId: {} platformResponseId: {} content: {} ",
            requestId, sessionId,
            clientRequestId, platformResponseId,
            content);
        NotificationChannelDetails channelDetails;

        if (!CONTENT_LIST.contains(content)) {
            throw new NotFoundException(
                Collections.singletonList(NotificationCenterError.CONTENT_TYPE_DOSE_NOT_EXIST.toMessage(content)));
        }
        try {
            channelDetails = notificationHistoryService.getNotificationStatus(platformResponseId, content, null);
        } catch (Exception ex) {
            LOGGER.error("Failed to get notification status details by platform response id: {}",
                platformResponseId, ex);
            throw new NotFoundException(Collections.singletonList(
                NotificationCenterError.PLATFORM_RESPONSE_ID_NOT_FOUND.toMessage(platformResponseId)));
        }

        HttpStatus httpStatus = null == channelDetails ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return new ResponseEntity<>(channelDetails,
            ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId), httpStatus);
    }


    /**
     * getNotificationHistoryUserIdVehicleID class.
     */
    @GetMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/notifications",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to get notification status by vin and user id",
        description = "Notification Center Api is used to get notification status by vin and user id", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNotificationHistoryUserIdVehicleId(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("userId") String userId,
        @PathVariable("vehicleId") String vehicleId,
        @RequestParam(value = "since") long since,
        @RequestParam(value = "until") long until,
        @RequestParam(value = "size") int size,
        @RequestParam(value = "page") int page,
        @RequestParam(value = "isNonRegisteredVehicle", defaultValue = "false") boolean isNonRegisteredVehicle) {
        LOGGER.debug(
            "getNotificationHistoryUserIdVehicleID - requestId: {} userid: {}   vehicleId: {} from: {} "
                + "to: {} with page size: {}  and page: {} isNonRegisteredVehicle {}",
            requestId, userId,
            vehicleId, since, until,
            size,
            page, isNonRegisteredVehicle);
        if (since > until) {
            return new ResponseEntity<>(UNTIL_MUST_BE_GREATER_THEN_SINCE, HttpStatus.BAD_REQUEST);
        }
        validateUserExists(userId);
        validateVehiclesExistence(vehicleId, isNonRegisteredVehicle);

        List<NotificationChannelDetails> channelDetails;
        String link = "/v1/users/" + userId + "/vehicles/" + vehicleId + NOTIFICATIONS;

        try {
            channelDetails =
                notificationHistoryService.getNotificationHistoryUserIdVehicleId(userId, vehicleId, since, until, size,
                    page);
            if (channelDetails != null) {
                channelDetails.forEach(channelDetail -> channelDetail.setLink(
                    link.concat(channelDetail.getId() + CONTENT_FULL)));
            }
            HttpStatus httpStatus = null == channelDetails ? HttpStatus.NOT_FOUND : HttpStatus.OK;
            String response = MAPPER.writeValueAsString(channelDetails);
            return new ResponseEntity<>(response,
                ApiUtils.getHeaders(clientRequestId, sessionId, vehicleId), httpStatus);
        } catch (Exception ex) {
            LOGGER.error("Failed to get notification status details by user id {} , vehicle id: {} .Exception ",
                userId, vehicleId, ex);
            return new ResponseEntity<>(null,
                ApiUtils.getHeaders(clientRequestId, sessionId, vehicleId), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * getNotificationHistoryUserId class.
     */
    @GetMapping(value = "/v1/users/{userId}/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to get notification status by user id",
        description = "Notification Center Api is used to get notification status by user id", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNotificationHistoryUserId(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("userId") String userId,
        @RequestParam(value = "since") long since,
        @RequestParam(value = "until") long until,
        @RequestParam(value = "size") int size,
        @RequestParam(value = "page") int page) throws JsonProcessingException {
        LOGGER.debug(
            "getNotificationHistoryUserId - requestId: {} sessionId: {} clientRequestId: {} userid: {} from: {} "
                + "to: {} with page size: {}  and page: {} ",
            requestId, sessionId,
            clientRequestId, userId, since, until, size,
            page);
        if (since > until) {
            return new ResponseEntity<>(UNTIL_MUST_BE_GREATER_THEN_SINCE, HttpStatus.BAD_REQUEST);
        }
        validateUserExists(userId);
        List<NotificationChannelDetails> channelDetails = null;
        String link = "/v1/users/" + userId + NOTIFICATIONS;
        try {
            channelDetails = notificationHistoryService.getNotificationHistoryUserId(userId, since, until, size, page);
            channelDetails.forEach(channelDetail -> channelDetail.setLink(
                link.concat(channelDetail.getId() + CONTENT_FULL)));
        } catch (Exception ex) {
            LOGGER.error(FAILED_TO_GET_NOTIFICATION_STATUS_DETAILS_BY_USER_ID_VEHICLE_ID_AND_PLATFORM_RESPONSE_ID,
                ex);
        }

        HttpStatus httpStatus = null == channelDetails ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        String response = MAPPER.writeValueAsString(channelDetails);
        return new ResponseEntity<>(response,
            httpStatus);
    }

    /**
     * getNotificationHistoryVehicleId API.
     */

    @GetMapping(value = "/v1/vehicles/{vehicleId}/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to get notification status by vin",
        description = "Notification Center Api is used to get notification status by vin ", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNotificationHistoryVehicleId(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("vehicleId") String vehicleId,
        @RequestParam(value = "since") long since,
        @RequestParam(value = "until") long until,
        @RequestParam(value = "size") int size,
        @RequestParam(value = "page") int page,
        @RequestParam(value = "isNonRegisteredVehicle", defaultValue = "false")
        boolean isNonRegisteredVehicle) throws JsonProcessingException {
        LOGGER.debug(
            "getNotificationHistoryVehicleId - requestId: {} vehicleId: {} from: {} to: {} "
                + "with page size: {}  and page: {} isNonRegisteredVehicle {} ",
            requestId, vehicleId, since, until, size, page,
            isNonRegisteredVehicle);
        if (since > until) {
            return new ResponseEntity<>(UNTIL_MUST_BE_GREATER_THEN_SINCE, HttpStatus.BAD_REQUEST);
        }
        validateVehiclesExistence(vehicleId, isNonRegisteredVehicle);

        List<NotificationChannelDetails> channelDetails = null;
        String link = "/v1/vehicles/" + vehicleId + NOTIFICATIONS;

        try {
            channelDetails = isNonRegisteredVehicle
                ? notificationHistoryService.getNotificationHistoryNonRegVehicleId(vehicleId, since, until, size, page)
                : notificationHistoryService.getNotificationHistoryVehicleId(vehicleId, since, until, size, page);
            channelDetails.forEach(channelDetail -> channelDetail.setLink(
                link.concat(channelDetail.getId() + CONTENT_FULL)));
        } catch (Exception ex) {
            LOGGER.error(FAILED_TO_GET_NOTIFICATION_STATUS_DETAILS_BY_USER_ID_VEHICLE_ID_AND_PLATFORM_RESPONSE_ID,
                ex);
        }

        HttpStatus httpStatus = (null == channelDetails) ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        String response = MAPPER.writeValueAsString(channelDetails);
        return new ResponseEntity<>(response,
            ApiUtils.getHeaders(clientRequestId, sessionId, vehicleId), httpStatus);
    }


    /**
     * [Story 503788] Updated T&C Consent Campaign Notification.
     * This API is used to fetch a campaign's notification details
     */

    @GetMapping(value = "/v1/notification/campaign/{campaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification center API is used to get notification history of a campaign",
        description = "Notification center API is used to get notification history of a campaign", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNotificationHistoryForCampaignId(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("campaignId") String campaignId,
        @RequestParam(value = "status") String status,
        @RequestParam(value = "size") int size,
        @RequestParam(value = "page") int page
    ) throws JsonProcessingException, javassist.NotFoundException {
        status = status != null ? status.replaceAll(REGEX, "") : null;
        LOGGER.debug(
            "getNotificationHistoryForCampaignId - requestId: {} campaignId: {} with status {} ,"
                + "page size: {}  and page number: {} ",
            requestId,
            campaignId, status, size, page);

        if (StringUtils.isEmpty(status) || size <= 0 || page <= 0) {
            return new ResponseEntity<>(StringUtils.EMPTY, HttpStatus.BAD_REQUEST);
        }
        CampaignSummary campaignSummary = null;

        campaignSummary = notificationHistoryService.getCampaignHistory(campaignId, status, page, size);

        HttpStatus httpStatus = (null == campaignSummary) ? HttpStatus.NOT_FOUND : HttpStatus.OK;

        String response = MAPPER.writeValueAsString(campaignSummary);

        return new ResponseEntity<>(response,
            ApiUtils.getHeaders(clientRequestId, sessionId, campaignId), httpStatus);


    }


    /**
     * Validate user exists.
     *
     * @param userId the user id
     */
    private void validateUserExists(String userId) {
        if (StringUtils.isNotEmpty(userId) && userService.getUser(userId) == null) {
            throw new UserIdNotFoundException(Collections.emptyList());
        }
    }

    /**
     * Validate vehicle exists.
     *
     * @param vehicleId the vehicle id
     */
    private void validateVehicleExists(String vehicleId) {
        if (StringUtils.isNotEmpty(vehicleId) && !vehicleService.isVehicleExist(vehicleId)) {
            throw new VehicleIdNotFoundException(Collections.emptyList());
        }
    }

    /**
     * Validate vehicle not exists.
     *
     * @param vehicleId the vehicle id
     */
    private void validateVehicleNotExists(String vehicleId) {
        if (StringUtils.isNotEmpty(vehicleId) && vehicleService.isVehicleExist(vehicleId)) {
            throw new InvalidNonRegisteredVehicle(Collections.emptyList());
        }
    }

    /**
     * Validate vehicles existence.
     *
     * @param vehicleId the vehicle id
     * @param isNonRegisteredVehicle the is non registered vehicle
     */
    private void validateVehiclesExistence(String vehicleId, boolean isNonRegisteredVehicle) {
        if (isNonRegisteredVehicle) {
            validateVehicleNotExists(vehicleId);
        } else {
            validateVehicleExists(vehicleId);
        }
    }

    /**
     * Exception handler for NotFoundException.
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

    /**
     * Exception handler for UserIdNotFoundException or VehicleIdNotFoundException.
     */
    @ExceptionHandler({UserIdNotFoundException.class, VehicleIdNotFoundException.class})
    @ResponseStatus(code = NOT_FOUND)
    public ResponseWrapper<Void> invalidInputException(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.notFound()
            .requestId(request.getHeader("RequestId"))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }
}