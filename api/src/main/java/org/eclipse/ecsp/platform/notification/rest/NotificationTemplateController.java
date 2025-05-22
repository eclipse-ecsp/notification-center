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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.platform.notification.dto.MissingNotificationTemplates;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNotificationIdException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationTemplateDoesNotExistException;
import org.eclipse.ecsp.platform.notification.service.NotificationTemplateService;
import org.eclipse.ecsp.security.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.ecsp.utils.ApiUtils.getHeaders;
import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Controller for read, delete, import and export template.
 *
 * @author Shai
 */


@RestController
public class NotificationTemplateController {


    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * The constant REQUEST_ID.
     */
    public static final String REQUEST_ID = "requestId";

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    @Autowired
    private NotificationTemplateService notificationTemplateService;

    /**
     * Export notification templates based on given filter criteria.
     */

    @PostMapping(value = "v1/notification/template/filter")
    @Operation(summary = "Notification Center Api is used to export notification templates ",
        description = "Notification Center Api is used to export notification templates", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<byte[]> filterNotificationTemplate(
        @RequestHeader(value = "RequestId", required = true) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @RequestBody
        @Valid NotificationTemplateFilterDto filterDto

    ) {
        byte[] notificationTemplates = notificationTemplateService.filter(filterDto);

        return ResponseEntity.ok()
            .header(REQUEST_ID, requestId)
            .header("Content-Disposition", "attachment; filename=ignite_notification_templates_export.zip")
            .header("Content-Type", "application/zip")
            .body(notificationTemplates);

    }

    /**
     * Export notification templates based on given filter criteria.
     */
    @GetMapping(value = "v1/notification/template")
    @Operation(summary = "Get a list of maps, each map contains notification id, notification short and long name.",
        description = "Get a list of maps, each map contains notification id, notification short and long name.",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> filterNotificationTemplate(
        @RequestHeader(value = "RequestId", required = true) String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId
    ) throws JsonProcessingException  {
        List<Map<String, String>> notificationTemplatesMetaData =
            notificationTemplateService.getNotificationTemplates();

        HttpStatus httpStatus = HttpStatus.OK;
        String response = MAPPER.writeValueAsString(notificationTemplatesMetaData);
        return new ResponseEntity<>(response, httpStatus);

    }

    /**
     * Import generic notification templates.
     */
    @PostMapping(value = "v1/notification/template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Notification Center Api is used to import notification templates ",
        description = "Notification Center Api is used to import notification templates", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> importNotificationTemplate(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @RequestParam("file") @Valid MultipartFile file) throws Exception {


        notificationTemplateService.importNotificationTemplate(file, false);

        return ResponseEntity.ok().header(REQUEST_ID, requestId).build();
    }

    /**
     * Delete notification template by notificationId.
     */
    @DeleteMapping(value = "v1/notification/template/{notificationId}")
    @Operation(summary = "Notification Center Api is used to delete notification templates by notification id ",
        description = "Notification Center Api is used to delete notification templates by notification id",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> deleteNotificationTemplate(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("notificationId") @Valid @NotBlank String notificationId) {

        notificationTemplateService.deleteNotificationTemplate(notificationId);

        return ResponseEntity.accepted().header(REQUEST_ID, requestId).build();
    }

    /**
     * Delete notification templates by filter criteria.
     */
    @DeleteMapping(value = "v1/notification/template")
    @Operation(summary = "Delete all notification templates associate with all specified notification IDs",
        description = "Delete all notification templates associate with all specified notification IDs", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<MissingNotificationTemplates> deleteNotificationTemplates(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @RequestBody @Valid NotificationTemplateFilterDto deleteList)  {


        Set<String> nonExistingIds = null;
        if (deleteList != null && !CollectionUtils.isEmpty(deleteList.getNotificationTemplateIds())) {
            nonExistingIds = notificationTemplateService.deleteNotificationTemplates(deleteList);
        }

        return ResponseEntity.accepted().header(REQUEST_ID, requestId)
            .body(new MissingNotificationTemplates(nonExistingIds));
    }

    /**
     * Import notification templates incremental approach.
     */
    @PostMapping(value = "v2/notification/template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Notification Center Api is used to import notification templates ",
        description = "Notification Center Api is used to import notification templates ", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    //@ApiOperation(value = "import notification templates", notes = "import notification templates")
    public ResponseEntity<String> updateNotificationTemplate(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @RequestParam("file") @Valid MultipartFile file) throws Exception {


        notificationTemplateService.importNotificationTemplate(file, true);

        return ResponseEntity.ok().header(REQUEST_ID, requestId).build();
    }

    /**
     * Delete notification template by notificationId ,brand ,local and /or additional lookup property.
     */
    @DeleteMapping(value = "v2/notification/template/{notificationId}/brands/{brand}/locales/{locale}")
    @Operation(summary = "Delete templates by notification ID,brand,locale and an additional lookup property",
        description = "Delete templates by notification ID,brand,locale and an additional lookup property",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"ManageNotifications"})
    public ResponseEntity<String> deleteNotificationTemplateByIdBrandLocale(
        @RequestHeader(value = "RequestId") String requestId,
        @RequestHeader(value = "SessionId", required = false) String sessionId,
        @RequestHeader(value = "ClientRequestId", required = false) String clientRequestId,
        @PathVariable("notificationId") @Valid @NotBlank String notificationId,
        @PathVariable("brand") @Valid @NotBlank String brand,
        @PathVariable("locale") @Valid @NotBlank String locale,
        @RequestParam(value = "additionalLookupPropertyName", required = false) String additionalLookupPropertyName,
        @RequestParam(value = "additionalLookupPropertyValue", required = false) String additionalLookupPropertyValue) {


        if ((StringUtils.isNotBlank(additionalLookupPropertyName) && StringUtils.isBlank(additionalLookupPropertyValue))
            || (StringUtils.isBlank(additionalLookupPropertyName)
            && StringUtils.isNotBlank(additionalLookupPropertyValue))) {
            return new ResponseEntity<>("Missing either additionalLookupPropertyName or additionalLookupPropertyValue",
                getHeaders(clientRequestId, sessionId, requestId), BAD_REQUEST);
        }
        additionalLookupPropertyName = additionalLookupPropertyName.replaceAll("[\r\n]", "");
        additionalLookupPropertyValue = additionalLookupPropertyValue.replaceAll("[\r\n]", "");

        notificationTemplateService.deleteNotificationTemplateByIdBrandLocale(notificationId, brand, locale,
            additionalLookupPropertyName, additionalLookupPropertyValue);

        return ResponseEntity.accepted().header(REQUEST_ID, requestId).build();
    }

    /**
     * Exception handler for invalid input.
     */
    @ExceptionHandler({InvalidNotificationIdException.class, InvalidInputFileException.class,
        NotificationTemplateDoesNotExistException.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseWrapper<Void> invalidNotificationIds(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.badRequest()
            .requestId(request.getHeader("RequestId"))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }
}