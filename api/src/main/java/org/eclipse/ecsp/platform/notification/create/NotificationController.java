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

package org.eclipse.ecsp.platform.notification.create;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.Campaign;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.domain.notification.NotificationNonRegisteredUser;
import org.eclipse.ecsp.domain.notification.NotificationRequest;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.annotation.VersionValidator;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.ErrorSendingEventException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNotificationIdException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.exceptions.ScheduledNotificationDeletionException;
import org.eclipse.ecsp.platform.notification.v1.fw.web.NotificationGroupingNotFoundException;
import org.eclipse.ecsp.platform.notification.v1.utils.Utils;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.utils.ApiUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.DELETE_SCHEDULED_NOTIFICATION_SUCCESS;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NON_REGISTERED_SUCCESS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Api for notifications sending.
 */
@RestController
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);
    /**
     * WITH_REQUEST_ID.
     */
    public static final String WITH_REQUEST_ID = " with requestId ";
    /**
     * AND_SESSION_ID.
     */
    public static final String AND_SESSION_ID = " and sessionId ";
    /**
     * NOTIFICATION_REQUEST_PLATFORM_RESPONSE_ID_HTTP_STATUS.
     */
    public static final String NOTIFICATION_REQUEST_PLATFORM_RESPONSE_ID_HTTP_STATUS =
            "NotificationRequest={} platformResponseId={} httpStatus={}";
    /**
     * REQUEST_ID.
     */
    public static final String REQUEST_ID = "RequestId";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    private AttachmentsValidator attachmentsValidator;

    @Autowired
    private VersionValidator versionValidator;

    /**
     * InitBinder.
     *
     * @param binder WebDataBinder
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        if ("notificationCreationRequest".equals(binder.getObjectName())) {
            binder.addValidators(attachmentsValidator);
            binder.addValidators(versionValidator);
        }
    }

    /**
     * Create notification for a user and vehicle.
     */
    @PostMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/notifications", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to create notification",
        description = "Notification Center Api is used to create notification", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> createNotificationUserIdVehicleId(
        @RequestHeader(value = REQUEST_ID) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("userId") String userId,
        @PathVariable("vehicleId") String vehicleId,
        @RequestBody @Valid NotificationCreationRequest notificationRequest) {

        fillRequestHeaders(notificationRequest, requestId, sessionId, userId, vehicleId);

        String platformResponseId;

        try {
            platformResponseId = notificationService.createNotification(notificationRequest);
        } catch (AuthorizationException e) {
            LOGGER.error(
                "Authorization exception for vehicleId " + notificationRequest.getVehicleId() + WITH_REQUEST_ID
                    + notificationRequest.getRequestId() + AND_SESSION_ID + notificationRequest.getSessionId(),
                e);
            return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId,
                sessionId, null),
                HttpStatus.FORBIDDEN);
        } catch (NoSuchEntityException e) {
            LOGGER.error(
                "NoSuchEntityException for vehicleId " + notificationRequest.getVehicleId() + WITH_REQUEST_ID
                    + notificationRequest.getRequestId() + AND_SESSION_ID + notificationRequest.getSessionId(),
                e);
            return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId,
                sessionId, null),
                HttpStatus.NOT_FOUND);
        } catch (NotificationGroupingNotFoundException ex) {
            LOGGER.error("Notification Grouping not found  for notificationId {}",
                notificationRequest.getNotificationId(), ex);
            return new ResponseEntity<>(
                "No notification grouping found for notification id " + notificationRequest.getNotificationId(),
                ApiUtils.getHeaders(clientRequestId, sessionId, null),
                HttpStatus.BAD_REQUEST);
        }

        HttpStatus httpStatus =
            !StringUtils.isEmpty(platformResponseId) ? HttpStatus.ACCEPTED : INTERNAL_SERVER_ERROR;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Creating Notification for userId:{} and vehicleId:{} RequestId:{}  "
                            + NOTIFICATION_REQUEST_PLATFORM_RESPONSE_ID_HTTP_STATUS,
                    userId, vehicleId,
                    requestId, notificationRequest.toString(),
                    platformResponseId, httpStatus);
        }
        return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId),
            httpStatus);
    }

    /**
     * Delete a scheduled notification.
     */
    @DeleteMapping(path = "/v1/notifications/scheduled/{platformResponseId}", produces = APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseWrapper<Void> deleteScheduledNotification(
        @PathVariable("platformResponseId") String platformResponseId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId) throws Exception {

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setBizTransactionId(sessionId);
        igniteEvent.setRequestId(platformResponseId);
        notificationService.deleteScheduledNotification(igniteEvent);
        return ResponseWrapper.accepted()
            .requestId(clientRequestId)
            .rootMessage(ResponseWrapper.Message.of(DELETE_SCHEDULED_NOTIFICATION_SUCCESS.getCode(),
                DELETE_SCHEDULED_NOTIFICATION_SUCCESS.getReason(),
                DELETE_SCHEDULED_NOTIFICATION_SUCCESS.getMessage()))
            .build();
    }

    /**
     * Send notification to vehicle.
     */
    @PostMapping(value = "/v1/vehicles/{vehicleId}/notifications", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to create notification",
        description = "Notification Center Api is used to create notification", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> createNotificationVehicleId(@RequestHeader(value = REQUEST_ID) String
                                                                requestId,
                                                            @RequestHeader(value = "SessionId", required = false)
                                                            String sessionId,
                                                            @RequestHeader(value = "ClientRequestId", required = false)
                                                            String clientRequestId,
                                                            @PathVariable("vehicleId") String vehicleId,
                                                            @RequestBody @Valid
                                                            NotificationCreationRequest notificationRequest) {

        fillRequestHeaders(notificationRequest, requestId, sessionId, null, vehicleId);
        if (!vehicleService.isVehicleExist(vehicleId)) {
            LOGGER.error("Vehicle not found for vehicleId {}", vehicleId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String platformResponseId;
        try {
            platformResponseId = notificationService.createNotification(notificationRequest);
        } catch (AuthorizationException e) {
            LOGGER.error(
                "Authorization exception for vehicleId " + notificationRequest.getVehicleId()
                    + WITH_REQUEST_ID
                    + notificationRequest.getRequestId() + AND_SESSION_ID + notificationRequest.getSessionId(), e);
            return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId,
                sessionId, null),
                HttpStatus.FORBIDDEN);
        } catch (NoSuchEntityException e) {
            LOGGER.error(
                "NoSuchEntityException for vehicleId " + notificationRequest.getVehicleId() + WITH_REQUEST_ID
                    + notificationRequest.getRequestId() + AND_SESSION_ID + notificationRequest.getSessionId(), e);
            return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId,
                sessionId, null),
                HttpStatus.NOT_FOUND);
        }

        HttpStatus httpStatus =
            !StringUtils.isEmpty(platformResponseId) ? HttpStatus.ACCEPTED : INTERNAL_SERVER_ERROR;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Creating Notification for vehicleId: RequestId={} vehicleId={} NotificationRequest={} "
                            + "platformResponseId={} httpStatus={}",
                    requestId, vehicleId,
                    notificationRequest.toString(), platformResponseId, httpStatus);
        }
        return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId),
            httpStatus);
    }

    /**
     * Send notification to user only.
     */
    @PostMapping(value = "/v1/users/{userId}/notifications", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to create notification",
        description = "Notification Center Api is used to create notification", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> createNotificationUserId(@RequestHeader(value = REQUEST_ID) String requestId,
                                                         @RequestHeader(value = "SessionId", required = false)
                                                         String sessionId,
                                                         @RequestHeader(value = "ClientRequestId", required = false)
                                                         String clientRequestId,
                                                         @PathVariable("userId") String userId,
                                                         @RequestBody @Valid
                                                         NotificationCreationRequest notificationRequest) {

        fillRequestHeaders(notificationRequest, requestId, sessionId, userId, null);
        notificationRequest.setUserNotification(true);

        String platformResponseId = notificationService.createNotification(notificationRequest);
        HttpStatus httpStatus =
            !StringUtils.isEmpty(platformResponseId) ? HttpStatus.ACCEPTED : INTERNAL_SERVER_ERROR;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "Creating Notification for userId: RequestId={} userId={} NotificationRequest={} "
                    + "platformResponseId={} httpStatus={}",
                requestId, userId,
                notificationRequest.toString(), platformResponseId, httpStatus);
        }
        return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId),
            httpStatus);
    }

    /**
     * Update the nickname of a vehicle.
     */
    @PatchMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/nickname", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to update nicknames",
        description = "Notification Center Api is used to update nicknames", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> updateNickName(@RequestHeader(value = REQUEST_ID, required = false) String
                                                   requestId,
                                               @RequestHeader(value = "SessionId", required = false) String sessionId,
                                               @RequestHeader(value = "ClientRequestId", required = false)
                                               String clientRequestId,
                                               @RequestHeader(value = "If-Match") String etag,
                                               @PathVariable("userId") String userId,
                                               @PathVariable("vehicleId") String vehicleId,
                                               @RequestBody @Valid @NotBlank String nickName) throws JSONException {

        JSONObject jsonObj = new JSONObject(nickName);
        UserProfile profile = notificationService.getUserProfile(userId);
        String existingNickName = notificationService.getNickNameByUserIdVehicleId(profile, userId, vehicleId);
        if ((null == existingNickName)
            || (profile.getLastModifiedTime() == Long.parseLong(etag.replace("\"", "")))) {
            boolean isUpdated =
                notificationService.updateNickName(userId, vehicleId, jsonObj.getString("nickName"));
            HttpStatus httpStatus = isUpdated ? HttpStatus.OK : INTERNAL_SERVER_ERROR;
            LOGGER.debug("Updating NickName with  httpStatus={}", httpStatus);
            return new ResponseEntity<>(httpStatus);
        } else {
            LOGGER.debug("cannot update nick name for userId {} ,vehicleId={} .Pre-condition failed ",
                userId, vehicleId);
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }
    }

    /**
     * Get the nickname of the vehicle.
     */
    @GetMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/nickname", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get nicknames for user and vin", description = "Get nicknames for user and vin", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> getNickName(@RequestHeader(value = REQUEST_ID, required = false) String
                                                  requestId,
                                              @RequestHeader(value = "SessionId", required = false) String sessionId,
                                              @RequestHeader(value = "ClientRequestId", required = false)
                                              String clientRequestId,
                                              @PathVariable("userId") String userId,
                                              @PathVariable("vehicleId") String vehicleId) throws JSONException {

        UserProfile profile = notificationService.getUserProfile(userId);
        String nickName = notificationService.getNickNameByUserIdVehicleId(profile, userId, vehicleId);
        HttpStatus httpStatus;
        HttpHeaders header = new HttpHeaders();
        if (null != nickName) {
            String etag = NotificationConstants.DOUBLE_QUOTES + profile.getLastModifiedTime()
                + NotificationConstants.DOUBLE_QUOTES;
            header.setETag(etag);
            httpStatus = HttpStatus.OK;
            JSONObject nickNameJson = new JSONObject();
            nickNameJson.put("nickName", nickName);
            nickName = nickNameJson.toString();
        } else {
            httpStatus = HttpStatus.NOT_FOUND;
        }

        LOGGER.debug("NickName={},httpStatus={}", nickName, httpStatus);
        return new ResponseEntity<>(nickName, header, httpStatus);
    }

    /**
     * Update the user consent for receiving the notification.
     */
    @PatchMapping(value = "/v1/users/{userId}/consent", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update user consent", description = "Update user consent", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> updateConsent(@RequestHeader(value = REQUEST_ID, required = false) String
                                                  requestId,
                                              @RequestHeader(value = "SessionId", required = false) String sessionId,
                                              @RequestHeader(value = "ClientRequestId", required = false)
                                              String clientRequestId,
                                              @PathVariable("userId") String userId,
                                              @RequestBody @Valid @NotBlank String consent) throws JSONException {

        JSONObject jsonObj = new JSONObject(consent);
        boolean isUpdated = notificationService.updateConsent(userId, jsonObj.getBoolean("consent"));
        HttpStatus httpStatus = isUpdated ? HttpStatus.OK : INTERNAL_SERVER_ERROR;
        LOGGER.debug("Updating NickName with  httpStatus={}", httpStatus);
        return new ResponseEntity<>(httpStatus);
    }

    /**
     * Create a campaign.
     */
    @PostMapping(path = "/v1/notifications/campaigns", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to create campaign id",
        description = "Notification Center Api is used to create campaign id", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Campaign> createCampaign() {
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID().toString());
        LOGGER.debug("campaignId = {}", campaign);
        boolean isSave = notificationService.saveCampaign(campaign);
        HttpStatus httpStatus = (isSave) ? HttpStatus.OK : INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(campaign, httpStatus);
    }

    /**
     * Send a campaign notification to the vehicle.
     */
    @PostMapping(path = "/v1/vehicles/{vehicleId}/notifications/campaigns/{campaignId}",
        consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to send notification",
        description = "Notification Center Api is used to send notification", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> createCampaignNotifications(@RequestHeader(value = REQUEST_ID) String
          requestId,
          @RequestHeader(value = "SessionId", required = false)
          String sessionId,
          @RequestHeader(value = "ClientRequestId", required = false)
          String clientRequestId,
          @PathVariable("vehicleId") String vehicleId,
          @PathVariable("campaignId") String campaignId,
          @RequestBody @Valid
          NotificationCreationRequest notificationRequest) {

        fillRequestHeaders(notificationRequest, requestId, sessionId, null, vehicleId);

        if (!verifyCampaignId(campaignId)) {
            return new ResponseEntity<>("Invalid CampaignId ", HttpStatus.BAD_REQUEST);
        }

        notificationRequest.setCampaignId(campaignId);
        notificationRequest.setCampaignNotification(true);
        String platformResponseId = notificationService.createNotification(notificationRequest);
        HttpStatus httpStatus =
            !StringUtils.isEmpty(platformResponseId) ? HttpStatus.ACCEPTED : INTERNAL_SERVER_ERROR;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Creating Notification for vehicleId: RequestId={} vehicleId={} "
                            + NOTIFICATION_REQUEST_PLATFORM_RESPONSE_ID_HTTP_STATUS,
                    requestId, vehicleId,
                    notificationRequest.toString(), platformResponseId, httpStatus);
        }
        return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId),
            httpStatus);
    }

    /**
     * Send user only notification.
     */
    @PostMapping(value = "/v2/users/{userId}/notifications", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to send notification",
        description = "Notification Center Api is used to send notification", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> createDynamicNotificationUserId(@RequestHeader(value = REQUEST_ID) String
                requestId,
            @RequestHeader(value = "SessionId", required = false)
            String sessionId,
            @RequestHeader(value = "ClientRequestId", required = false)
            String clientRequestId,
            @PathVariable("userId") String userId,
            @RequestBody @Valid
            NotificationCreationRequest notificationRequest) {

        fillRequestHeaders(notificationRequest, requestId, sessionId, userId, null);
        notificationRequest.setDynamicNotification(true);
        notificationRequest.setUserNotification(true);
        String platformResponseId = notificationService.createNotification(notificationRequest);

        HttpStatus httpStatus =
            !StringUtils.isEmpty(platformResponseId) ? HttpStatus.ACCEPTED : INTERNAL_SERVER_ERROR;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Creating Dynamic  Notification for userId: RequestId={} userId={} "
                            + NOTIFICATION_REQUEST_PLATFORM_RESPONSE_ID_HTTP_STATUS,
                    requestId, userId,
                    notificationRequest.toString(), platformResponseId, httpStatus);
        }
        return new ResponseEntity<>(ApiUtils.getHeaders(clientRequestId, sessionId, platformResponseId),
            httpStatus);
    }

    /**
     * Send notification to user not registered in the ignite system.
     */
    @PostMapping(path = "/v1/notifications/nonRegisteredUsers", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Notification Center Api is used to send notification for non registered users",
        description = "Notification Center Api is used to send notification for non registered users", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {
        "ManageNonRegisteredUsersNotification"})
    public ResponseWrapper<Void> createNotificationForNonRegisteredUsers(
        @RequestHeader(value = REQUEST_ID) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @RequestBody @Valid NotificationNonRegisteredUser notificationNonRegisteredUser) throws Exception {
        requestId = Utils.generateRandomId(requestId);
        notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, requestId,
            sessionId);
        return ResponseWrapper.ok()
            .requestId(requestId)
            .rootMessage(
                ResponseWrapper.Message.of(NON_REGISTERED_SUCCESS.getCode(), NON_REGISTERED_SUCCESS.getReason(),
                    NON_REGISTERED_SUCCESS.getMessage()))
            .build();
    }

    /**
     * Exception handler for BAD_REQUEST.
     */
    @ExceptionHandler({InvalidNotificationIdException.class, InvalidInputException.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseWrapper<Void> invalidNotificationIds(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * Exception handler for ScheduledNotificationDeletionException.
     */
    @ExceptionHandler({ScheduledNotificationDeletionException.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseWrapper<Void> scheduledNotificationDeletionException(NotificationCenterExceptionBase e,
                                                                        WebRequest request) {
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
     * Exception handler for ErrorSendingEventException.
     */
    @ExceptionHandler({ErrorSendingEventException.class})
    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    public ResponseWrapper<Void> errorSendingEventException(NotificationCenterExceptionBase e, WebRequest
        request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * fillRequestHeaders.
     *
     * @param notificationRequest notificationRequest
     * @param requestId requestId
     * @param sessionId sessionId
     * @param userId userId
     * @param vehicleId vehicleId
     */
    private void fillRequestHeaders(NotificationRequest notificationRequest, String requestId, String
        sessionId,
                                    String userId, String vehicleId) {
        notificationRequest.setRequestId(Utils.generateRandomId(requestId));
        notificationRequest.setSessionId(Utils.generateRandomId(sessionId));
        notificationRequest.setUserId(userId);
        notificationRequest.setVehicleId(vehicleId);
    }

    /**
     * verifyCampaignId.
     *
     * @param campaignId campaignId
     * @return boolean
     */
    private boolean verifyCampaignId(String campaignId) {
        Campaign campaign = notificationService.getCampaign(campaignId);
        return campaign != null;
    }
}