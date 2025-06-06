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

package org.eclipse.ecsp.platform.notification.v1.rest;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.notification.entities.PaginatedAlertsHistory;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.eclipse.ecsp.platform.notification.v1.annotation.ValidDeviceID;
import org.eclipse.ecsp.platform.notification.v1.common.Constants;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertIdDto;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertReadUpdate;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertsHistoryRequestParams;
import org.eclipse.ecsp.platform.notification.v1.domain.PaginationInfo;
import org.eclipse.ecsp.platform.notification.v1.domain.TimeIntervalInfo;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV1;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV2;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV3;
import org.eclipse.ecsp.platform.notification.v1.utils.AlertsConstants;
import org.eclipse.ecsp.platform.notification.v1.utils.ResponseMsgConstants;
import org.eclipse.ecsp.platform.notification.v1.utils.Utils;
import org.eclipse.ecsp.security.Security;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * REST controller providing APIs to fetch, update, and delete alert history for devices and users.
 * Supports multiple API versions and alert filtering options.
 */
@RestController
@Validated
public class AlertsController {

    private static final String REQUEST_ID_HEADER_KEY = "RequestId";

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertsController.class);
    /**
     * Success message for marking alerts as read/unread.
     */
    public static final String
            ALERTS_ARE_MARKED_READ_UNREAD_SUCCESSFULLY =
            "Alerts are marked read/unread successfully";
    private final AlertsServiceV1 alertServiceV1;
    private final AlertsServiceV2 alertServiceV2;
    private final AlertsServiceV3 alertServiceV3;


    /**
     * Constructs an AlertsController with the required alert services.
     *
     * @param alertServiceV1 alert service v1
     * @param alertServiceV2 alert service v2
     * @param alertServiceV3 alert service v3
     */
    @Autowired
    public AlertsController(AlertsServiceV1 alertServiceV1, AlertsServiceV2 alertServiceV2,
                            AlertsServiceV3 alertServiceV3) {
        this.alertServiceV1 = alertServiceV1;
        this.alertServiceV2 = alertServiceV2;
        this.alertServiceV3 = alertServiceV3;
    }

    /**
     * Retrieves paginated alert history for a device within a time range (v3).
     *
     * @param deviceId   device ID
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @return paginated alerts history
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v3/devices/{deviceId}/alerts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "AlertHistory Api allows to retrieve historical information",
        description = "API to get historical information about Diagnostic Trouble Codes set by Vehicle ECUs",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public PaginatedAlertsHistory getAllAlertsBetweenSinceAndUntilV3(
        @ValidDeviceID @PathVariable("deviceId") String deviceId,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "until")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus)
        throws Exception {
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV3.getAlertsByDeviceId(params);
    }


    /**
     * Retrieves paginated alert history for a device and alert types within a time range (v3).
     *
     * @param deviceId   device ID
     * @param alertTypes set of alert types
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @return paginated alerts history
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v3/devices/{deviceId}/alerts/{alertTypes}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "API to retrieve historical information about Diagnostic Trouble Codes set by Vehicle ECUs",
        description = "API to get historical information about Diagnostic Trouble Codes set by Vehicle ECUs",
        responses = {@ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public PaginatedAlertsHistory getSpecificAlertBetweenSinceAndUntilV3(
        @ValidDeviceID @PathVariable("deviceId") String deviceId,
        @PathVariable("alertTypes") Set<String> alertTypes,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "until")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus)
        throws Exception {
        LOGGER.debug("alertTYpe {}", alertTypes);

        validateAlertTypesList(alertTypes);
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .alertNames(alertTypes)
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV3.getAlertsByDeviceId(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user within a time range (v3).
     *
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @param userId     user ID
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v3/devices/alerts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "API to get historical information about Diagnostic Trouble Codes set by Vehicle ECUs",
        description = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public Map<String, Object> getAlertsBetweenSinceAndUntilV3(
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "until")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus,
        @RequestHeader(Constants.USER_ID) String userId)
        throws Exception {

        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .userId(userId)
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV3.getAlertsByUserid(params);
    }


    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @param alertTypes set of alert types
     * @param userId     user ID
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @GetMapping(value = "/v3/devices/alerts/{alertTypes}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs",
        description = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public Map<String, Object> getAlertsBetweenSinceAndUntilAndAlertTypeV3(
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "until")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus,
        @PathVariable("alertTypes") Set<String> alertTypes,
        @RequestHeader(Constants.USER_ID) String userId)
        throws Exception {

        validateAlertTypesList(alertTypes);
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .userId(userId)
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .alertNames(alertTypes)
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV3.getAlertsByUserid(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param deviceId   device ID
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @param alertTypes set of alert types
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v2/devices/{deviceId}/alerts/{alertTypes}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs",
        description = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public Map<String, Object> getSpecificAlertBetweenSinceAndUntilV2(
        @ValidDeviceID @PathVariable("deviceId") String deviceId,
        @Valid @PathVariable("alertTypes") Set<String> alertTypes,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus)
        throws Exception {

        validateAlertTypesList(alertTypes);
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .alertNames(alertTypes)
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV2.getAlertsByDeviceId(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param deviceId   device ID
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v2/devices/{deviceId}/alerts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs",
        description = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public Map<String, Object> getAllAlertsBetweenSinceAndUntilV2(
        @ValidDeviceID @PathVariable("deviceId") String deviceId,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus)
        throws Exception {
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV2.getAlertsByDeviceId(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @param userId    user ID
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v2/devices/alerts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs",
        description = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public Map<String, Object> getAlertsBetweenSinceAndUntilV2(
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus,
        @RequestHeader(Constants.USER_ID) String userId)
        throws Exception {

        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .userId(userId)
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV2.getAlertsByUserId(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @param alertTypes set of alert types
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @GetMapping(value = "/v2/devices/alerts/{alertTypes}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs",
        description = "API to get information about Diagnostic Trouble Codes set by Vehicle ECUs", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public Map<String, Object> getAlertsBetweenSinceAndUntilAndAlertTypeV2(
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus,
        @PathVariable("alertTypes") Set<String> alertTypes,
        @RequestHeader(Constants.USER_ID) String userId)
        throws Exception {

        validateAlertTypesList(alertTypes);
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .userId(userId)
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .alertNames(alertTypes)
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV2.getAlertsByUserId(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param deviceId   device ID
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v1/devices/{deviceId}/alerts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public List<AlertsHistoryInfo> getAllAlertsBetweenSinceAndUntilV1(
        @ValidDeviceID @PathVariable("deviceId") String deviceId,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus)
        throws Exception {
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV1.getAlerts(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param deviceId   device ID
     * @param alertTypes set of alert types
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v1/devices/{deviceId}/alerts/{alertTypes}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public List<AlertsHistoryInfo> getSpecificAlertBetweenSinceAndUntilV1(
        @ValidDeviceID @PathVariable("deviceId") String deviceId,
        @Valid @PathVariable("alertTypes") Set<String> alertTypes,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus)
        throws Exception {

        validateAlertTypesList(alertTypes);
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .alertNames(alertTypes)
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV1.getAlerts(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @param userId    user ID
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v1/devices/alerts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public Object getAlertsBetweenSinceAndUntilV1(
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus,
        @RequestHeader(Constants.USER_ID) String userId)
        throws Exception {

        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .userId(userId)
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .paginationInfo(paginationInfo)
            .readStatus(readStatus).build();
        return alertServiceV1.getAlerts(params);
    }

    /**
     * Retrieves paginated alert history for all devices of a user and alert
     * types within a time range (v3).
     *
     * @param since      start timestamp (epoch ms)
     * @param until      end timestamp (epoch ms)
     * @param size       page size (optional)
     * @param page       page number (optional)
     * @param readStatus read status filter (optional)
     * @param alertTypes set of alert types
     * @param userId    user ID
     * @return map containing pagination info and device-to-alerts mapping
     * @throws Exception if validation or data access fails
     */
    @GetMapping(value = "/v1/devices/alerts/{alertTypes}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public List<AlertsHistoryInfo> getAlertsBetweenSinceAndUntilAndAlertTypeV1(
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_UNTIL_MSG) @RequestParam(value = "till")
        String until,
        @RequestParam(value = "size", required = false) String size,
        @RequestParam(value = "page", required = false) String page,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus,
        @PathVariable("alertTypes") Set<String> alertTypes,
        @RequestHeader(Constants.USER_ID) String userId)
        throws Exception {

        validateAlertTypesList(alertTypes);
        PaginationInfo paginationInfo = validatePageAndSize(page, size);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .userId(userId)
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until)))
            .paginationInfo(paginationInfo)
            .alertNames(alertTypes)
            .readStatus(readStatus).build();
        return alertServiceV1.getAlerts(params);
    }

    /**
     * Retrieves the next set of alerts for a given device starting from a specific timestamp.
     * Allows filtering by alert types and read status, and limits the number of results.
     *
     * @param since      start timestamp (epoch ms) to fetch alerts from
     * @param count      number of alerts to retrieve
     * @param deviceId   device ID for which alerts are fetched
     * @param alertTypes optional set of alert types to filter results
     * @param readStatus read status filter (optional, defaults to "all")
     * @return list of alert history information objects
     * @throws Exception if validation or data access fails
     */
    @RequestMapping(value = "/v3/nextalerts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Next X api is to get the requested number of alerts for the requested device id",
        description = "Next X api is to get the requested number of alerts for the requested device id", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public List<AlertsHistoryInfo> getNextAlertsV3(
        @Valid @Range(min = 0, message = ResponseMsgConstants.INVALID_ALERT_SINCE_MSG) @RequestParam(value = "since")
        String since,
        @Valid @Range(min = 1, max = Integer.MAX_VALUE, message = ResponseMsgConstants.INVALID_NEXT_COUNT_MSG)
        @RequestParam(value = "count", defaultValue = "10", required = false) String count,
        @ValidDeviceID @RequestParam(value = "deviceid") String deviceId,
        @RequestParam(value = "alerttypes", required = false) Set<String> alertTypes,
        @RequestParam(value = "readstatus", defaultValue = "all", required = false) String readStatus)
        throws Exception {

        validateAlertTypesList(alertTypes);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), System.currentTimeMillis()))
            .alertNames(alertTypes)
            .paginationInfo(new PaginationInfo(1, Integer.parseInt(count)))
            .readStatus(readStatus).build();
        return alertServiceV3.getNextAlertsByDeviceId(params);
    }

    /**
     * Updates the read/unread status of alerts for a device (v3).
     *
     * @param deviceId    device ID
     * @param alertNotify request body containing lists of alert IDs to mark as read or unread
     * @throws IllegalArgumentException if the request body is invalid
     */
    @PutMapping(path = "/v3/devices/{deviceId}/alerts/readupdate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public void saveV3(@ValidDeviceID @PathVariable("deviceId") String deviceId,
                       @RequestBody AlertReadUpdate alertNotify) {
        if (alertNotify == null || (alertNotify.getUnreadList() == null && alertNotify.getReadList() == null)) {
            throw new IllegalArgumentException(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST);
        }
        alertServiceV3.saveData(deviceId, alertNotify);
        LOGGER.info(ALERTS_ARE_MARKED_READ_UNREAD_SUCCESSFULLY);
    }

    /**
     * Updates the read/unread status of alerts for a device (v2).
     *
     * @param deviceId    device ID
     * @param alertNotify request body containing lists of alert IDs to mark as read or unread
     * @throws IllegalArgumentException if the request body is invalid
     */
    @PutMapping(path = "/v2/devices/{deviceId}/alerts/readupdate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public void saveV2(@ValidDeviceID @PathVariable("deviceId") String deviceId,
                       @RequestBody AlertReadUpdate alertNotify) {
        if (alertNotify == null || (alertNotify.getReadList() == null && alertNotify.getUnreadList() == null)) {
            throw new IllegalArgumentException(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST);
        }
        alertServiceV3.saveData(deviceId, alertNotify);
        LOGGER.info(ALERTS_ARE_MARKED_READ_UNREAD_SUCCESSFULLY);
    }

    /**
     * Updates the read/unread status of alerts for a device (v1).
     *
     * @param deviceId    device ID
     * @param alertNotify request body containing lists of alert IDs to mark as read or unread
     * @throws IllegalArgumentException if the request body is invalid
     */
    @PutMapping(path = "/v1/devices/{deviceId}/alerts/readupdate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Mark Alert Api is to mark the alerts read or unread",
        description = "Mark Alert Api is to mark the alerts read or unread", responses = {
          @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {"SelfManage,AssociateMyselfToVehicle"})
    public void saveV1(@ValidDeviceID @PathVariable("deviceId") String deviceId,
                       @RequestBody AlertReadUpdate alertNotify) {
        if (alertNotify == null || (alertNotify.getReadList() == null && alertNotify.getUnreadList() == null)) {
            throw new IllegalArgumentException(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST);
        }
        alertServiceV3.saveData(deviceId, alertNotify);
        LOGGER.info(ALERTS_ARE_MARKED_READ_UNREAD_SUCCESSFULLY);
    }

    /**
     * Retrieves alert history for a device between two timestamps.
     * Used by trip analysis.
     *
     * @param deviceId device ID
     * @param since    start timestamp (epoch ms)
     * @param until    end timestamp (epoch ms)
     * @return list of alert history information objects
     * @throws Exception if validation or data access fails
     */
    @GetMapping(value = "/v1/devices/{deviceId}/alerthistory", produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public List<AlertsHistoryInfo> getAlertHistoryBetweenSinceAndUntil(@PathVariable("deviceId") String deviceId,
                                                                       @RequestParam(value = "since") String since,
                                                                       @RequestParam(value = "till") String until)
        throws Exception {

        since = since != null ? since.replaceAll("[\r\n]", "") : null;
        until = until != null ? until.replaceAll("[\r\n]", "") : null;

        LOGGER.info("Retrieving alert history for device id {} time stand between {} and {} ",
            deviceId, since,
            until);
        AlertsHistoryRequestParams params = AlertsHistoryRequestParams.builder()
            .deviceIds(Collections.singletonList(deviceId))
            .timeIntervalInfo(new TimeIntervalInfo(Long.parseLong(since), Long.parseLong(until))).build();
        List<AlertsHistoryInfo> result = alertServiceV1.getAlerts(params);
        LOGGER.info("Received {} alerts from alert History ", result.size());
        return result;
    }

    /**
     * Deletes alerts for a device, supporting soft delete.
     *
     * @param deviceId   device ID
     * @param deleteList optional list of alert IDs to delete
     * @param deleteType type of delete operation (only "soft" is supported)
     * @throws IllegalArgumentException if deleteType is not supported
     */
    @DeleteMapping(path = "/v1/devices/{deviceId}/alerts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete alerts for a device", description = "Delete alerts for a device", responses = {
        @ApiResponse(responseCode = "200", description = "Success")})
    @SecurityRequirement(name = Security.JWT_AUTH_VALIDATOR, scopes = {
        "SelfManage,AssociateMyselfToVehicle"})
    public void deleteAlertsV1(@ValidDeviceID @PathVariable String deviceId,
                               @RequestBody(required = false) @Valid AlertIdDto deleteList,
                               @RequestParam(value = "deleteType", required = false) @Valid String deleteType) {
        if (deleteType != null && !"soft".equalsIgnoreCase(deleteType)) {
            throw new IllegalArgumentException(ResponseMsgConstants.UNSUPPORTED_API_DELETE_TYPE_MSG);
        }

        alertServiceV3.deleteAlerts(deviceId, deleteType != null, deleteList);
        LOGGER.debug("Alerts are deleted successfully");
    }

    /**
     * Handles requests to unmapped URLs and responds with an unsupported API version message.
     *
     * @throws NoSuchEntityException always thrown to indicate unsupported API version
     */
    @RequestMapping(method = RequestMethod.GET)
    public Object default1() {
        throw new NoSuchEntityException(ResponseMsgConstants.UNSUPPORTED_API_VERSION_MSG);
    }

    /**
     * Validates and parses the pagination parameters for page number and page size.
     * If the provided values are not numeric, defaults are used.
     *
     * @param page page number as a string; defaults to 1 if not numeric
     * @param size page size as a string; defaults to 10 if not numeric
     * @return a {@link PaginationInfo} object containing the parsed page and size values
     */
    private PaginationInfo validatePageAndSize(String page, String size) {
        if (!StringUtils.isNumeric(page)) {
            page = Utils.DEFAULT_PAGINATION_PAGE;
        }
        if (!StringUtils.isNumeric(size)) {
            size = Utils.DEFAULT_PAGINATION_SIZE;
        }
        return new PaginationInfo(Integer.parseInt(page), Integer.parseInt(size));
    }

    /**
     * Validate alert types list.
     */
    private void validateAlertTypesList(Collection<String> alertTypes) {
        if (alertTypes.stream().map(alertType -> alertType.replace("_", ""))
            .anyMatch(alertType -> !StringUtils.isAlphanumeric(alertType))) {
            throw new ValidationException(ResponseMsgConstants.INVALID_ALERTS_SPECIFIED);
        }
    }

    /**
     * Exception handler.
     */
    @ExceptionHandler({InvalidVehicleIdInput.class})
    @ResponseStatus(code = BAD_REQUEST)
    public ResponseEntity<String> invalidInputError(InvalidVehicleIdInput e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    /**
     * Exception handler.
     */
    @ExceptionHandler({VehicleIdNotFoundException.class})
    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    public ResponseWrapper<Void> internalServerError(NotificationCenterExceptionBase e, WebRequest request) {
        return ResponseWrapper.internalServerError()
            .requestId(request.getHeader(REQUEST_ID_HEADER_KEY))
            .rootMessage(ResponseWrapper.Message.of(e.getCode(), e.getReason(), e.getMessage()))
            .errors(e.getErrors())
            .build();
    }
}
