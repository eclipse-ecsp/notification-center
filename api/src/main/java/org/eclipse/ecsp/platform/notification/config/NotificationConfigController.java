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

package org.eclipse.ecsp.platform.notification.config;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.CreateContactResponse;
import org.eclipse.ecsp.domain.notification.CreateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.DeleteSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.NotificationUserProfileEventDataV1_0;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.UpdateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.config.NotificationConfigRequest;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.NotificationConfigResponse;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.DisableApiPushChannelException;
import org.eclipse.ecsp.platform.notification.exceptions.EmptyNotificationConfig;
import org.eclipse.ecsp.platform.notification.exceptions.FcmTokenNotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidContactInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidGroupException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidGroupsException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidUserIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.MandatoryGroupsNotAllowedException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationGroupingNotAllowedException;
import org.eclipse.ecsp.platform.notification.exceptions.ServiceNameNotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.SuppressApiPushChannelException;
import org.eclipse.ecsp.platform.notification.exceptions.UserIdNotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static org.eclipse.ecsp.domain.Version.V1_0;
import static org.eclipse.ecsp.domain.notification.commons.ChannelType.API_PUSH;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NOTIFICATION_USER_PROFILE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.CONTACT_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.FCM_TOKEN_DELETE_SUCCESS;
import static org.eclipse.ecsp.utils.ApiUtils.getHeaders;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * NotificationConfigController class for all notification config CRUD operations.
 */
@FieldDefaults(level = PRIVATE)
@RestController
@Validated
public class NotificationConfigController {
    static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationConfigController.class);
    public static final String INVALID_CONTACT_ID = "Invalid contact id";
    public static final String REQUEST_ID = "RequestId";

    NotificationConfigServiceV1_0 service;

    NotificationConfigDAO configDao;

    NotificationGroupingDAO notificationGroupingDao;

    @Value("${brand.default.value:default}")
    private String defaultBrand;

    @Autowired
    public void setService(NotificationConfigServiceV1_0 service) {
        this.service = service;
    }

    @Autowired
    public void setConfigDao(NotificationConfigDAO configDao) {
        this.configDao = configDao;
    }

    @Autowired
    public void setNotificationGroupingDao(NotificationGroupingDAO notificationGroupingDao) {
        this.notificationGroupingDao = notificationGroupingDao;
    }

    /**
     * API to update the notification config for a user-vehicle-contact.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @param vehicleId       vehicleId
     * @param contactId       contactId
     * @param requestConfigs  requestConfigs
     * @return ResponseEntity
     */
    @PatchMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/contacts/{contactId}/notifications/config",
        consumes = APPLICATION_JSON_VALUE, produces = {"text/plain", APPLICATION_JSON_VALUE})
    @Operation(summary = "Create\\Update notification configuration of a contact",
        description = "Create\\Update notification configuration of a contact", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> updateNotificationConfig(@RequestHeader(value = REQUEST_ID) String requestId,
                                                           @RequestHeader(value = "SessionId", required = false)
                                                           String sessionId,
                                                           @RequestHeader(value = "ClientRequestId", required = false)
                                                           String clientRequestId,
                                                           @PathVariable(value = "userId") String userId,
                                                           @PathVariable(value = "vehicleId") String vehicleId,
                                                           @PathVariable(value = "contactId") String contactId,
                                                           @RequestBody
                                                           List<NotificationConfigRequest> requestConfigs) {

        try {
            service.validateInput(userId, vehicleId, contactId, requestConfigs);
        } catch (InvalidUserIdInput | InvalidVehicleIdInput | InvalidContactInput e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (AuthorizationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (NoSuchEntityException | InvalidInputException | NotificationGroupingNotAllowedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        IgniteEventImpl event = new IgniteEventImpl();
        event.setBizTransactionId(sessionId);
        List<NotificationConfig> configs =
            requestConfigs.stream().map(rc -> new NotificationConfig(userId, vehicleId, contactId, rc))
                .toList();
        Set<String> mandatoryGroup =
            notificationGroupingDao.findByMandatory(true).stream().filter(NotificationGrouping::isMandatory)
                .map(NotificationGrouping::getGroup).collect(toSet());
        for (NotificationConfig config : configs) {
            service.encryptNotificationConfig(config);
            if (mandatoryGroup.contains(config.getGroup())) {
                return new ResponseEntity<>(
                    "Modifying mandatory notification group " + config.getGroup() + " not allowed",
                    getHeaders(clientRequestId, sessionId, requestId), BAD_REQUEST);
            }
            ResponseEntity<String> responseEntity =
                validateForApiPushChannel(requestId, sessionId, clientRequestId, config);
            if (responseEntity != null) {
                return responseEntity;
            }
        }
        event.setEventData(new NotificationSettingDataV1_0(service.sanitizeConfig(configs)));
        event.setEventId(EventID.NOTIFICATION_SETTINGS);
        event.setVehicleId(vehicleId);
        event.setMessageId(randomUUID().toString());
        event.setRequestId(requestId);
        event.setVersion(V1_0);
        event.setTimestamp(System.currentTimeMillis());
        LOGGER.debug(event, "Processing update notification preference with payload {}", requestConfigs);
        service.processNotificationPreference(userId, vehicleId, event);
        return new ResponseEntity<>(getHeaders(clientRequestId, sessionId, requestId), OK);
    }

    /**
     * API to validate the notification config for a user-vehicle.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @param vehicleId       vehicleId
     * @param requestConfigs  requestConfigs
     * @return ResponseEntity
     */
    @Nullable
    private static ResponseEntity<String> validateForApiPushChannel(String requestId, String sessionId,
                                                                    String clientRequestId, NotificationConfig config) {
        for (Channel channel : config.getChannels()) {
            if (!"self".equals(config.getContactId()) && API_PUSH == channel.getChannelType()) {
                return new ResponseEntity<>("APIPush should not be allowed for secondary contact",
                    getHeaders(clientRequestId, sessionId, requestId), BAD_REQUEST);
            }
            if (API_PUSH == channel.getChannelType()) {
                if (!channel.getEnabled()) {
                    return new ResponseEntity<>("APIPush cannot be disabled",
                        getHeaders(clientRequestId, sessionId, requestId), BAD_REQUEST);
                }
                if (!channel.getSuppressionConfigs().isEmpty()) {
                    return new ResponseEntity<>("APIPush cannot be Suppressed",
                        getHeaders(clientRequestId, sessionId, requestId), BAD_REQUEST);
                }
            }
        }
        return null;
    }

    /**
     * Get notification configs for a user and vehicle association.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @param vehicleId       vehicleId
     * @return ResponseEntity with list of configs
     */
    @GetMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/notifications/config",
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get notification configuration", description = "Get notification configuration", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<List<NotificationConfigResponse>> getNotificationConfig(
        @RequestHeader(value = REQUEST_ID) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable(value = "userId") String userId,
        @PathVariable(value = "vehicleId") String vehicleId) {
        service.validateUserIdAndVehicleId(userId, vehicleId);
        LOGGER.debug("Processing get notification preference with userid  {} and vehicleid {}", userId, vehicleId);
        return new ResponseEntity<>(service.getNotificationPreference(userId, vehicleId, null), OK);
    }


    /**
     * Get notification configs for a user ,vehicle and service.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @param vehicleId       vehicleId
     * @param serviceName     serviceName
     * @return ResponseEntity with list of configs
     */
    @GetMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/services/{serviceName}/notifications/config",
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get notification configuration with service",
        description = "Get notification configuration with service", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<List<NotificationConfigResponse>> getNotificationConfigOfService(
        @RequestHeader(value = REQUEST_ID) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable(value = "userId") String userId,
        @PathVariable(value = "vehicleId") String vehicleId,
        @PathVariable(value = "serviceName") String serviceName) {
        service.validateUserIdAndVehicleId(userId, vehicleId);
        LOGGER.debug("Processing get notification preference with userId {}, vehicleId {} and serviceName {}", userId,
            vehicleId, serviceName);

        return new ResponseEntity<>(service.getNotificationPreference(userId, vehicleId, serviceName), OK);
    }

    /**
     * Get notification configs for a user.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @return ResponseEntity with list of configs
     */
    @GetMapping(value = "/v1/notifications/users/{userId}/config", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get notification configuration for a user",
        description = "Get notification configuration for a user", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage"})
    public List<NotificationConfigResponse> getUserNotificationConfig(
        @RequestHeader(value = REQUEST_ID) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable(value = "userId") String userId) {
        if (service.isUserExists(userId)) {
            LOGGER.debug("Processing get notification preference with userId  {}", userId);
            return service.getNotificationPreference(userId, VEHICLE_ID_FOR_DEFAULT_PREFERENCE, null);
        }
        throw new UserIdNotFoundException(emptyList());
    }


    /**
     * Update user profile.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @param userProfile     userProfile updated
     * @return ResponseEntity headers
     */
    @PatchMapping(value = "/v1/users/{userId}/notifications/user/profile", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Patch user profile", description = "Patch user profile", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> updateUserProfile(@RequestHeader(value = REQUEST_ID) String requestId,
                                                  @RequestHeader(value = "SessionId", required = false)
                                                  String sessionId,
                                                  @RequestHeader(value = "ClientRequestId", required = false)
                                                  String clientRequestId,
                                                  @PathVariable("userId") String userId,
                                                  @RequestBody UserProfile userProfile) {
        userProfile.setUserId(userId);
        IgniteEventImpl event = new IgniteEventImpl();
        event.setBizTransactionId(sessionId);
        event.setEventData(new NotificationUserProfileEventDataV1_0(userProfile));
        event.setEventId(NOTIFICATION_USER_PROFILE);
        event.setMessageId(randomUUID().toString());
        event.setRequestId(requestId);
        event.setVersion(V1_0);
        event.setTimestamp(System.currentTimeMillis());

        LOGGER.debug(event, "Processing update user preference");
        service.processUserPreference(userId, event);
        return new ResponseEntity<>(getHeaders(clientRequestId, sessionId, requestId), OK);
    }

    /**
     * Import default notification template.
     *
     * @param notificationTemplateList template json
     * @return response message
     */
    @PostMapping(value = "/v1/notification/default/template", consumes = APPLICATION_JSON_VALUE,
        produces = TEXT_PLAIN_VALUE)
    @Operation(summary = "Upload default template", description = "Upload default template", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> postNotificationTemplate(
        @RequestBody List<NotificationTemplate> notificationTemplateList) {
        return getResponseEntity(service.saveNotificationTemplates(notificationTemplateList), "Notification Templates");
    }

    /**
     * Import notification template config.
     *
     * @param notificationTemplateConfigs notificationTemplateConfigs json
     * @return response message
     */
    @PostMapping(value = "/v1/notification/templateConfig", consumes = APPLICATION_JSON_VALUE,
        produces = TEXT_PLAIN_VALUE)
    @Operation(summary = "Upload notification template config", description = "Upload notification template config",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> postNotificationTemplateConfig(
        @RequestBody List<NotificationTemplateConfig> notificationTemplateConfigs) {
        return getResponseEntity(service.saveNotificationTemplatesConfigs(notificationTemplateConfigs),
            "Template Config");
    }


    /**
     * Import notification grouping.
     *
     * @param notificationGroupingList grouping list json
     * @return response message
     */
    @PostMapping(value = "/v1/notification/grouping", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    @Operation(summary = "Upload notification grouping", description = "Upload notification grouping", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> postNotificationGrouping(
        @RequestBody List<NotificationGrouping> notificationGroupingList) {

        service.validateGroupingInput(notificationGroupingList);
        return getResponseEntity(service.saveNotificationGrouping(notificationGroupingList), "Notification Grouping");
    }


    /**
     * Get notification grouping details for a group.
     *
     * @param group groupname
     * @return List of notification grouping
     */
    @GetMapping(value = "/v1/notification/grouping/{group}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get notification grouping", description = "Get notification grouping", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<List<NotificationGrouping>> getNotificationGrouping(
        @PathVariable("group") @NotBlank String group) {

        return new ResponseEntity<>(service.getNotificationGrouping(group), HttpStatus.OK);
    }

    /**
     * Delete a notification group.
     *
     * @param groupName      groupName
     * @param notificationId notificationId
     * @param serviceName    serviceName
     * @return Httpstatus
     */
    @DeleteMapping(value = {"/v1/notification/grouping/{group}/notifications/{notificationId}/services/{service}",
        "/v1/notification/grouping/{group}/notifications/{notificationId}/services"})
    @Operation(summary = "Delete notification grouping", description = "Delete notification grouping", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<Void> deleteNotificationGrouping(@PathVariable("group") String groupName,
                                                           @PathVariable("notificationId") String notificationId,
                                                           @PathVariable(value = "service", required = false)
                                                           String serviceName) {
        service.deleteNotificationGrouping(groupName, notificationId, serviceName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Get all notification grouping details.
     *
     * @return List of notification grouping
     */
    private ResponseEntity<String> getResponseEntity(boolean isSaved, String name) {
        if (!isSaved) {
            return new ResponseEntity<>(name + " not processed", INTERNAL_SERVER_ERROR);
        }
        LOGGER.debug(name + "saved in mongoDb httpStatus={}", OK);

        return new ResponseEntity<>(name + " successfully processed", OK);
    }


    /**
     * Create a secondary contact for a user-vehicle.
     *
     * @param requestId        requestId
     * @param sessionId        sessionId
     * @param clientRequestId  clientRequestId
     * @param userId           userId
     * @param vehicleId        vehicleId
     * @param secondaryContact secondaryContact
     * @return response message
     */
    @PostMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/contacts", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create secondary contact", description = "Create secondary contact", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<CreateContactResponse> createContact(
        @RequestHeader(value = REQUEST_ID, required = true) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("userId") String userId,
        @PathVariable("vehicleId") String vehicleId,
        @RequestBody @Valid SecondaryContact secondaryContact) {

        secondaryContact.setContactId(new ObjectId().toString());
        secondaryContact.setUserId(userId);
        secondaryContact.setVehicleId(vehicleId);
        service.validateContactUniqueness(secondaryContact);
        IgniteEventImpl event = new IgniteEventImpl();
        event.setBizTransactionId(sessionId);
        event.setEventData(new CreateSecondaryContactEventDataV1_0(secondaryContact));
        event.setEventId(EventID.CREATE_SECONDARY_CONTACT);
        event.setRequestId(requestId);
        event.setVersion(V1_0);
        event.setTimestamp(System.currentTimeMillis());
        LOGGER.debug(event, "Processing create secondary contact");
        service.processUserPreference(userId, event);
        CreateContactResponse contact = new CreateContactResponse();
        contact.setContactId(secondaryContact.getContactId());

        return new ResponseEntity<>(contact, OK);
    }

    /**
     * Update secondary contact.
     *
     * @param requestId        requestId
     * @param sessionId        sessionId
     * @param clientRequestId  clientRequestId
     * @param userId           userId
     * @param contactId        contactId
     * @param secondaryContact secondaryContact
     * @return response message
     */
    @PutMapping(value = "/v1/users/{userId}/contacts/{contactId}",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update secondary contact", description = "Update secondary contact", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<CreateContactResponse> updateContact(
                                                @RequestHeader(value = REQUEST_ID, required = true) String requestId,
                                                @RequestHeader(value = "SessionId", required = false) String sessionId,
                                                @RequestHeader(value = "ClientRequestId", required = false)
                                                String clientRequestId,
                                                @PathVariable("userId") String userId,
                                                @PathVariable("contactId") String contactId,
                                                @RequestBody SecondaryContact secondaryContact) {

        SecondaryContact existingContact = service.getSecondaryContact(contactId);

        if (existingContact == null || !userId.equals(existingContact.getUserId())) {
            CreateContactResponse message = new CreateContactResponse();
            message.setMessage(INVALID_CONTACT_ID);
            return new ResponseEntity<>(message,
                HttpStatus.NOT_FOUND);
        }

        secondaryContact.setContactId(contactId);
        secondaryContact.setUserId(userId);
        secondaryContact.setVehicleId(existingContact.getVehicleId());

        service.validateContactUniqueness(secondaryContact);

        IgniteEventImpl event = new IgniteEventImpl();
        event.setBizTransactionId(sessionId);
        event.setEventData(new UpdateSecondaryContactEventDataV1_0(secondaryContact));
        event.setEventId(EventID.UPDATE_SECONDARY_CONTACT);
        event.setRequestId(requestId);
        event.setVersion(V1_0);
        event.setTimestamp(System.currentTimeMillis());
        LOGGER.debug(event, "Processing update secondary contact");
        service.processUserPreference(userId, event);
        CreateContactResponse contact = new CreateContactResponse();
        contact.setContactId(secondaryContact.getContactId());

        return new ResponseEntity<>(contact, OK);

    }

    /**
     * Delete a secondary contact.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @param contactId       contactId
     * @return response message
     */
    @DeleteMapping(value = "/v1/users/{userId}/contacts/{contactId}", produces = TEXT_PLAIN_VALUE)
    @Operation(summary = "Delete secondary contact", description = "Delete secondary contact", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> deleteContact(@RequestHeader(value = REQUEST_ID, required = true) String requestId,
                                                @RequestHeader(value = "SessionId", required = false) String sessionId,
                                                @RequestHeader(value = "ClientRequestId", required = false)
                                                String clientRequestId,
                                                @PathVariable("userId") String userId,
                                                @PathVariable("contactId") String contactId) {
        SecondaryContact secondaryContact = service.getSecondaryContact(contactId);

        if (secondaryContact == null || !userId.equals(secondaryContact.getUserId())) {
            return new ResponseEntity<>(INVALID_CONTACT_ID, HttpStatus.NOT_FOUND);
        }

        IgniteEventImpl event = new IgniteEventImpl();
        event.setBizTransactionId(sessionId);
        event.setEventData(new DeleteSecondaryContactEventDataV1_0(secondaryContact));
        event.setEventId(EventID.DELETE_SECONDARY_CONTACT);
        event.setRequestId(requestId);
        event.setVersion(V1_0);
        event.setTimestamp(System.currentTimeMillis());
        LOGGER.debug(event, "Processing delete secondary contact");
        service.processUserPreference(userId, event);
        return new ResponseEntity<>(OK);
    }

    /**
     * Delete config by user vehicle contactId.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param userId          userId
     * @param vehicleId       vehicleId
     * @param contactId       contactId
     * @return response
     */
    @DeleteMapping(value = "/v1/users/{userId}/vehicles/{vehicleId}/contacts/{contactId}/notifications/config",
        produces = TEXT_PLAIN_VALUE)
    @Operation(summary = "Delete config for self or secondary contact",
        description = "Delete config for self or secondary contact",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> deleteContact(@RequestHeader(value = REQUEST_ID, required = true) String requestId,
                                                @RequestHeader(value = "SessionId", required = false) String sessionId,
                                                @RequestHeader(value = "ClientRequestId", required = false)
                                                String clientRequestId,
                                                @PathVariable("userId") String userId,
                                                @PathVariable("vehicleId") String vehicleId,
                                                @PathVariable("contactId") String contactId) {


        if (!contactId.equals("self")) {
            SecondaryContact secondaryContact = service.getSecondaryContact(contactId);
            if (secondaryContact == null || !userId.equals(secondaryContact.getUserId())) {
                return new ResponseEntity<>(INVALID_CONTACT_ID, HttpStatus.NOT_FOUND);
            }
        }

        List<NotificationConfig> configs = configDao.findByUserVehicleContactId(userId, vehicleId, contactId);
        if (null != configs && !configs.isEmpty()) {
            configs.forEach(config -> configDao.deleteById(config.getId()));
        }
        return new ResponseEntity<>(OK);
    }


    /**
     * Get userprofile by userId.
     *
     * @param userId userId
     * @return UserProfile
     */
    @GetMapping(value = "/v1/users/profile/{id}", produces = APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseEntity<UserProfile> getUserProfile(
        @PathVariable("id") String userId) {

        return new ResponseEntity<>(service.getUserProfile(userId), OK);
    }

    /**
     * Get user secondary contact.
     *
     * @param contactId contactId
     * @return Secondary contact
     */
    @GetMapping(value = "/v1/users/contact/{id}", produces = APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseEntity<SecondaryContact> getUserContact(
        @PathVariable("id") String contactId) {
        return new ResponseEntity<>(service.getSecondaryContact(contactId), OK);
    }



    /**
     * create default notification config.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param requestConfigs  requestConfigs
     * @return response
     */
    @PatchMapping(value = "/v1/notifications/defaultConfig", consumes = APPLICATION_JSON_VALUE,
        produces = TEXT_PLAIN_VALUE)
    @Operation(summary = "Create default notification channel config",
        description = "Create default notification channel config",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> createDefaultNotificationConfig(@RequestHeader(value = REQUEST_ID) String requestId,
              @RequestHeader(value = "SessionId", required = false)
              String sessionId,
              @RequestHeader(value = "ClientRequestId", required = false)
              String clientRequestId,
              @RequestBody
              List<@Valid NotificationConfigRequest> requestConfigs) {

        if (isEmpty(requestConfigs)) {
            throw new IllegalArgumentException("expecting array of configs");
        }

        List<NotificationConfig> configs = requestConfigs.stream()
            .map(ncr -> NotificationConfig.defaultNotificationConfig(ncr, defaultBrand))
            .toList();

        LOGGER.debug("Creating default notification Config: {}", configs);
        service.saveDefaultNotificationConfig(configs);
        return new ResponseEntity<>(OK);
    }

    /**
     * Get default config.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param notificationId  notificationId
     * @param brand           brand
     * @return NotificationConfig
     */
    @GetMapping(value = "/v1/notifications/{notificationId}/defaultConfig", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get default notification channel config",
        description = "Get default notification channel config",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<NotificationConfig> getDefaultConfig(@RequestHeader(value = REQUEST_ID) String requestId,
           @RequestHeader(value = "SessionId", required = false)
           String sessionId,
           @RequestHeader(value = "ClientRequestId", required = false)
           String clientRequestId,
           @PathVariable(value = "notificationId")
           String notificationId,
           @RequestParam(value = "brand", required = false)
           String brand) {
        if (!StringUtils.hasText(brand)) {
            brand = defaultBrand;
        }
        return new ResponseEntity<>(service.getDefaultConfig(notificationId, brand), OK);
    }

    /**
     * Get all default notification configs.
     *
     * @param requestId       requestId
     * @param sessionId       sessionId
     * @param clientRequestId clientRequestId
     * @param notificationId  notificationId
     * @return List of NotificationConfig
     */
    @GetMapping(value = "/v1/notifications/{notificationId}/allDefaultConfigs", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get default notification channel config",
        description = "Get default notification channel config",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<List<NotificationConfig>> getAllDefaultConfigs(
        @RequestHeader(value = REQUEST_ID) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable(value = "notificationId") String notificationId) {

        return new ResponseEntity<>(service.getAllDefaultConfigs(notificationId), OK);
    }

    /**
     * Api for creating a notification config for a user with a default vehicle.
     *
     * @param userId         the user to create a config to
     * @param requestConfigs - the config to create for th user
     */

    @PatchMapping(value = "/v1/notifications/users/{userId}/config", consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create \\update user config", description = "Create \\update user config", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public List<NotificationConfig> createUserNotificationConfig(@RequestHeader(value = REQUEST_ID) String requestId,
         @RequestHeader(value = "SessionId", required = false)
         String sessionId,
         @RequestHeader(value = "ClientRequestId", required = false)
         String clientRequestId,
         @PathVariable(value = "userId") String userId,
         @RequestBody
         List<NotificationConfigRequest> requestConfigs) {

        try {
            service.validateInput(userId, VEHICLE_ID_FOR_DEFAULT_PREFERENCE, CONTACT_ID_FOR_DEFAULT_PREFERENCE,
                requestConfigs);
        } catch (IllegalArgumentException e) {
            throw new EmptyNotificationConfig(emptyList());
        } catch (NoSuchEntityException ex) {
            throw new InvalidGroupsException(emptyList());
        } catch (InvalidUserIdInput e) {
            throw new UserIdNotFoundException(emptyList());
        }

        List<NotificationConfig> configs = requestConfigs.stream()
            .map(rc -> new NotificationConfig(userId, VEHICLE_ID_FOR_DEFAULT_PREFERENCE,
                CONTACT_ID_FOR_DEFAULT_PREFERENCE, rc))
            .toList();
        Set<String> mandatoryGroup =
            notificationGroupingDao.findByMandatory(true).stream().filter(NotificationGrouping::isMandatory)
                .map(NotificationGrouping::getGroup).collect(toSet());
        for (NotificationConfig config : configs) {
            if (mandatoryGroup.contains(config.getGroup())) {
                throw new MandatoryGroupsNotAllowedException(emptyList(), config.getGroup());
            }

            for (Channel channel : config.getChannels()) {
                validateApiPushChannel(channel);
            }
        }

        configs = service.sanitizeConfig(configs);
        List<NotificationConfig> response =
            service.saveNotificationConfig(userId, VEHICLE_ID_FOR_DEFAULT_PREFERENCE, CONTACT_ID_FOR_DEFAULT_PREFERENCE,
                configs);
        Set<String> groups = requestConfigs.stream().map(NotificationConfigRequest::getGroup).collect(toSet());
        service.notifyNotificationConfigUpdate(userId, VEHICLE_ID_FOR_DEFAULT_PREFERENCE,
            CONTACT_ID_FOR_DEFAULT_PREFERENCE, groups, requestId);
        return response;
    }

    /**
     * Api for validating config for a user with a default vehicle.
     *
     * @param userId         the user to create a config to
     * @param vehicleId      the vehicle to create a config to
     * @param requestConfigs - the config to create for th user
     */
    private static void validateApiPushChannel(Channel channel) {
        if (API_PUSH == channel.getChannelType()) {
            if (!channel.getEnabled()) {
                throw new DisableApiPushChannelException(emptyList());
            }
            if (!channel.getSuppressionConfigs().isEmpty()) {
                throw new SuppressApiPushChannelException(emptyList());
            }
        }
    }

    /**
     * Delete the fcm token of the user.
     */
    @DeleteMapping(value = "/v1/user/fcm-token/{token}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseWrapper<Void> deleteToken(@RequestHeader(value = REQUEST_ID, required = false) String requestId,
                                             @RequestHeader(value = "SessionId", required = false) String sessionId,
                                             @RequestHeader(value = "user-id", required = true) String userId,
                                             @RequestHeader(value = "ClientRequestId", required = false)
                                             String clientRequestId,
                                             @PathVariable("token") @NotBlank String token) {

        service.deleteFcmToken(token, userId);
        return ResponseWrapper.ok()
            .requestId(requestId)
            .rootMessage(
                ResponseWrapper.Message.of(FCM_TOKEN_DELETE_SUCCESS.getCode(), FCM_TOKEN_DELETE_SUCCESS.getReason(),
                    FCM_TOKEN_DELETE_SUCCESS.getMessage()))
            .build();
    }

    /**
     * Exception handler for type FcmTokenNotFoundException.
     */
    @ExceptionHandler({FcmTokenNotFoundException.class})
    @ResponseStatus(code = NOT_FOUND)
    public ResponseWrapper<Void> invalidUserIdException(FcmTokenNotFoundException e, WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * Exception handler for type MandatoryGroupsNotAllowedException.
     */
    @ExceptionHandler({MandatoryGroupsNotAllowedException.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseWrapper<Void> mandatoryGroupsNotAllowedException(MandatoryGroupsNotAllowedException e,
                                                                    WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage() + e.getGroup()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * Exception handler of type InvalidContactInput.
     */
    @ExceptionHandler({InvalidContactInput.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseEntity<String> invalidContactInput(InvalidContactInput e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    /**
     * Exception handler of type NotificationCenterExceptionBase.
     */
    @ExceptionHandler({NotificationCenterExceptionBase.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseWrapper<Void> invalidInput(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }

    /**
     * Exception handler.
     */
    @ExceptionHandler({InvalidGroupException.class, NotFoundException.class, UserIdNotFoundException.class,
        VehicleIdNotFoundException.class, ServiceNameNotFoundException.class})
    @ResponseStatus(code = NOT_FOUND)
    public ResponseWrapper<Void> notFound(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.notFound()
            .requestId(request.getHeader(REQUEST_ID))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }
}
