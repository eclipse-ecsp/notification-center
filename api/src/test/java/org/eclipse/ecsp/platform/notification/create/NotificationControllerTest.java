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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.Campaign;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.domain.notification.NotificationNonRegisteredUser;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.v1.fw.web.NotificationGroupingNotFoundException;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.eclipse.ecsp.platform.notification.utils.UnitTestUtils.compareMessage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

/**
 * NotificationControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("checkstyle:MagicNumber")
public class NotificationControllerTest {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationControllerTest.class);

    private static final String PLATFORM_RESPONSE_ID = "98ba6991-032c-11eb-a74d-6f1d4556d17c";
    private static final String SESSION_ID = "SESSION_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String USER_ID = "testUser001";
    private static final String VEHICLE_ID = "HU4TEST112233";
    private static final String SESSION_ID_HEADER = "SessionId";
    private static final String PLATFORM_RESPONSE_ID_HEADER = "PlatformResponseId";
    private static final String CLIENT_REQUEST_ID_HEADER = "ClientRequestId";

    @Mock
    private NotificationServiceImpl notificationService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createNotificationUserIdVehicleIdSuccess() {

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<String> response =
            notificationController.createNotificationUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, VEHICLE_ID, notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdVehicleIdWithCampaignIdSuccess() {

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setCampaignId("743657436");

        ResponseEntity<String> response =
            notificationController.createNotificationUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, VEHICLE_ID, notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdVehicleIdAuthorizationException() {

        when(notificationService.createNotification(any())).thenThrow(new AuthorizationException("auth error"));

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<String> response =
            notificationController.createNotificationUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, VEHICLE_ID, notificationCreationRequest);

        assertEquals(FORBIDDEN, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdVehicleIdNoSuchEntityException() {

        when(notificationService.createNotification(any())).thenThrow(new NoSuchEntityException("no entity error"));

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<String> response =
            notificationController.createNotificationUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, VEHICLE_ID, notificationCreationRequest);

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdVehicleIdGroupingNotFoundException() {

        when(notificationService.createNotification(any())).thenThrow(
            new NotificationGroupingNotFoundException("grouping not found"));

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<String> response =
            notificationController.createNotificationUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, VEHICLE_ID, notificationCreationRequest);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdVehicleIdInternalError() {

        when(notificationService.createNotification(any())).thenReturn(null);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<String> response =
            notificationController.createNotificationUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, VEHICLE_ID, notificationCreationRequest);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationVehicleIdSuccess() {

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);
        when(vehicleService.isVehicleExist(any())).thenReturn(true);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<Void> response =
            notificationController.createNotificationVehicleId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                VEHICLE_ID, notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationVehicleIdWithCampaignIdSuccess() {

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);
        when(vehicleService.isVehicleExist(any())).thenReturn(true);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setCampaignId("4564385634");

        ResponseEntity<Void> response =
            notificationController.createNotificationVehicleId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                VEHICLE_ID, notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationVehicleIdAuthorizationException() {

        when(notificationService.createNotification(any())).thenThrow(new AuthorizationException("auth error"));
        when(vehicleService.isVehicleExist(any())).thenReturn(true);
        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<Void> response =
            notificationController.createNotificationVehicleId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                VEHICLE_ID, notificationCreationRequest);

        assertEquals(FORBIDDEN, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationVehicleIdNoSuchEntityException() {

        when(notificationService.createNotification(any())).thenThrow(new NoSuchEntityException("no entity error"));
        when(vehicleService.isVehicleExist(any())).thenReturn(true);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<Void> response =
            notificationController.createNotificationVehicleId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                VEHICLE_ID, notificationCreationRequest);

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationVehicleIdInternalError() {

        when(notificationService.createNotification(any())).thenReturn(null);
        when(vehicleService.isVehicleExist(any())).thenReturn(true);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<Void> response =
            notificationController.createNotificationVehicleId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                VEHICLE_ID, notificationCreationRequest);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdSuccess() {

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<Void> response =
            notificationController.createNotificationUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdWithCampignIdSuccess() {

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setCampaignId("c5ea759c-8dab-4292-8f5f-9545d94fd246");

        ResponseEntity<Void> response =
            notificationController.createNotificationUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createNotificationUserIdInternalError() {

        when(notificationService.createNotification(any())).thenReturn(null);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        ResponseEntity<Void> response =
            notificationController.createNotificationUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                notificationCreationRequest);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void updateNickNameSuccess() throws JSONException {

        UserProfile userProfile = new UserProfile();
        long currentTime = System.currentTimeMillis();
        userProfile.setLastModifiedTime(currentTime);

        when(notificationService.getUserProfile(any())).thenReturn(userProfile);
        when(notificationService.getNickNameByUserIdVehicleId(any(), any(), any())).thenReturn(null);
        when(notificationService.updateNickName(any(), any(), any())).thenReturn(true);

        ResponseEntity<Void> response = notificationController.updateNickName(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
            "\"" + currentTime + "\"", USER_ID, VEHICLE_ID, "{\"nickName\":\"nickName\"}");

        assertEquals(OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void updateNickNameSuccessEtag() throws JSONException {

        UserProfile userProfile = new UserProfile();
        long currentTime = System.currentTimeMillis();
        userProfile.setLastModifiedTime(currentTime);

        when(notificationService.getUserProfile(any())).thenReturn(userProfile);
        when(notificationService.getNickNameByUserIdVehicleId(any(), any(), any())).thenReturn("nick");
        when(notificationService.updateNickName(any(), any(), any())).thenReturn(true);

        ResponseEntity<Void> response = notificationController.updateNickName(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
            "\"" + currentTime + "\"", USER_ID, VEHICLE_ID, "{\"nickName\":\"nickName\"}");

        assertEquals(OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void updateNickNameInternalError() throws JSONException {

        UserProfile userProfile = new UserProfile();
        long currentTime = System.currentTimeMillis();
        userProfile.setLastModifiedTime(currentTime);

        when(notificationService.getUserProfile(any())).thenReturn(userProfile);
        when(notificationService.getNickNameByUserIdVehicleId(any(), any(), any())).thenReturn("nick");
        when(notificationService.updateNickName(any(), any(), any())).thenReturn(false);

        ResponseEntity<Void> response = notificationController.updateNickName(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
            "\"" + currentTime + "\"", USER_ID, VEHICLE_ID, "{\"nickName\":\"nickName\"}");

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void updateNickNamePreconditionFailed()throws JSONException {

        UserProfile userProfile = new UserProfile();
        long currentTime = System.currentTimeMillis();
        userProfile.setLastModifiedTime(currentTime);

        when(notificationService.getUserProfile(any())).thenReturn(userProfile);
        when(notificationService.getNickNameByUserIdVehicleId(any(), any(), any())).thenReturn("nick");
        when(notificationService.updateNickName(any(), any(), any())).thenReturn(false);

        ResponseEntity<Void> response = notificationController.updateNickName(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
            "\"" + (currentTime + 1) + "\"", USER_ID, VEHICLE_ID, "{\"nickName\":\"nickName\"}");

        assertEquals(PRECONDITION_FAILED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void getNickNameSuccess()throws JSONException {

        UserProfile userProfile = new UserProfile();
        long currentTime = System.currentTimeMillis();
        userProfile.setLastModifiedTime(currentTime);

        when(notificationService.getUserProfile(any())).thenReturn(userProfile);
        when(notificationService.getNickNameByUserIdVehicleId(any(), any(), any())).thenReturn("nickName");

        ResponseEntity<String> response =
            notificationController.getNickName(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID);

        assertEquals(OK, response.getStatusCode());
        assertEquals(NotificationConstants.DOUBLE_QUOTES + currentTime + NotificationConstants.DOUBLE_QUOTES,
            response.getHeaders().getETag());
        assertEquals("{\"nickName\":\"nickName\"}", response.getBody());
    }

    @Test
    public void getNickNameNotFound()throws JSONException {

        UserProfile userProfile = new UserProfile();
        long currentTime = System.currentTimeMillis();
        userProfile.setLastModifiedTime(currentTime);

        when(notificationService.getUserProfile(any())).thenReturn(userProfile);
        when(notificationService.getNickNameByUserIdVehicleId(any(), any(), any())).thenReturn(null);

        ResponseEntity<String> response =
            notificationController.getNickName(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID);

        assertEquals(NOT_FOUND, response.getStatusCode());
        assertEquals(0, response.getHeaders().size());
        assertNull(response.getBody());
    }

    @Test
    public void updateConsentSuccess()throws JSONException {

        when(notificationService.updateConsent(any(), anyBoolean())).thenReturn(true);

        ResponseEntity<Void> response =
            notificationController.updateConsent(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                "{\"consent\": true}");

        assertEquals(OK, response.getStatusCode());
    }

    @Test
    public void updateConsentInternalError()throws JSONException {

        when(notificationService.updateConsent(any(), anyBoolean())).thenReturn(false);

        ResponseEntity<Void> response =
            notificationController.updateConsent(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                "{\"consent\": true}");

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createCampaignSuccess() {

        when(notificationService.saveCampaign(any())).thenReturn(true);

        ResponseEntity<Campaign> response = notificationController.createCampaign();

        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }

    @Test
    public void createCampaignInternalError() {

        when(notificationService.saveCampaign(any())).thenReturn(false);

        ResponseEntity<Campaign> response = notificationController.createCampaign();

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createCampaignNotificationsSuccess() {

        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID().toString());

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        when(notificationService.getCampaign(any())).thenReturn(campaign);

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);

        ResponseEntity<String> response =
            notificationController.createCampaignNotifications(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, campaign.getId(), notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
        assertNull(response.getBody());
    }

    @Test
    public void createCampaignNotificationsInternalError() {

        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID().toString());

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        when(notificationService.getCampaign(any())).thenReturn(campaign);

        when(notificationService.createNotification(any())).thenReturn(null);

        ResponseEntity<String> response =
            notificationController.createCampaignNotifications(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, campaign.getId(), notificationCreationRequest);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
        assertNull(response.getBody());
    }

    @Test
    public void createCampaignNotificationsNullCampaign() {

        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID().toString());

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        when(notificationService.getCampaign(any())).thenReturn(null);

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);

        ResponseEntity<String> response =
            notificationController.createCampaignNotifications(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, campaign.getId(), notificationCreationRequest);

        assertEquals(BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid CampaignId ", response.getBody());
    }

    @Test
    public void createDynamicNotificationUserIdSuccess() {

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);

        ResponseEntity<Void> response =
            notificationController.createDynamicNotificationUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, notificationCreationRequest);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void createDynamicNotificationUserIdInternalError() {

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();

        when(notificationService.createNotification(any())).thenReturn(null);

        ResponseEntity<Void> response =
            notificationController.createDynamicNotificationUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, notificationCreationRequest);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, null, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);
    }

    @Test
    public void testCreateNotificationNonRegisterUser() throws Exception {
        validateCreateNotificationForNonRegisteredUsers("a", "b", "c", new NotificationNonRegisteredUser());
    }


    private void validateCreateNotificationForNonRegisteredUsers(String requestId, String sessionId,
                                                                 String clientRequestId,
                                                                 NotificationNonRegisteredUser nonRegisteredUser)
        throws Exception {
        ResponseWrapper<Void> response =
            notificationController.createNotificationForNonRegisteredUsers(requestId, sessionId,
                clientRequestId, nonRegisteredUser);

        compareMessage(response.getRootMessage(), NotificationCenterError.NON_REGISTERED_SUCCESS);

        assertEquals(200, response.getHttpStatusCode());
        assertEquals(requestId, response.getRequestId());
        assertTrue(CollectionUtils.isEmpty(response.getErrors()));
    }

    @Test
    public void testCreateNotificationNonRegisterUserWithAdditionalAttributes() throws Exception {
        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        UserProfile userProfile = new UserProfile();
        NotificationNonRegisteredUser nonRegisteredUser = new ObjectMapper().readValue(
            new File("../sp/src/test/resources/non-registered-with-additional-attributes.json"),
            NotificationNonRegisteredUser.class);
        Map<String, Object> dataProperties = nonRegisteredUser.getRecipients().get(0).getData();
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        userProfile = mapper.convertValue(dataProperties.get("userProfile"), UserProfile.class);
        Map<String, Object> attrs = mapper.convertValue(dataProperties.get("vehicleProfile"), HashMap.class);
        vehicleProfile.setVehicleAttributes(attrs);
        validateCreateNotificationForNonRegisteredUsers("reqId", "sesId", "cliId", nonRegisteredUser);
        assertEquals("focus", vehicleProfile.getModel());
        assertEquals("userFirst", userProfile.getFirstName());
    }

    @Test
    public void testCreateNotificationUserOnlyWithAdditionalAttributes() throws Exception {
        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        UserProfile userProfile = new UserProfile();
        NotificationCreationRequest userOnly = new ObjectMapper().readValue(
            new File("../sp/src/test/resources/user-only-with-additional-attributes.json"),
            NotificationCreationRequest.class);
        Map<String, Object> dataProperties = userOnly.getData();

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        userProfile = mapper.convertValue(dataProperties.get("userProfile"), UserProfile.class);
        Map<String, Object> attrs = mapper.convertValue(dataProperties.get("vehicleProfile"), HashMap.class);
        vehicleProfile.setVehicleAttributes(attrs);
        when(notificationService.createNotification(any())).thenReturn(PLATFORM_RESPONSE_ID);
        ResponseEntity<Void> response =
            notificationController.createDynamicNotificationUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, userOnly);

        assertEquals(ACCEPTED, response.getStatusCode());
        assertHeader(response, SESSION_ID, SESSION_ID_HEADER);
        assertHeader(response, PLATFORM_RESPONSE_ID, PLATFORM_RESPONSE_ID_HEADER);
        assertHeader(response, CLIENT_REQUEST_ID, CLIENT_REQUEST_ID_HEADER);

        assertEquals("mondeo", vehicleProfile.getModel());
        assertEquals("adam", userProfile.getFirstName());
    }

    @Test
    public void testDeleteScheduledNotificationSuccess() throws Exception {

        ResponseWrapper<Void> response = notificationController.deleteScheduledNotification("a", "b", "c");

        compareMessage(response.getRootMessage(), NotificationCenterError.DELETE_SCHEDULED_NOTIFICATION_SUCCESS);

        assertEquals(202, response.getHttpStatusCode());
        assertEquals("c", response.getRequestId());
        assertTrue(CollectionUtils.isEmpty(response.getErrors()));
    }

    @Test
    public void invalidNotificationIds() {

        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            notificationController.invalidNotificationIds(new InvalidInputException(new ArrayList<>()),
                request);

        assertEquals(BAD_REQUEST.value(), responseWrapper.getHttpStatusCode());
        assertNull(responseWrapper.getRequestId());
    }

    private void assertHeader(ResponseEntity<?> responseEntity, String expectedHeaderValue, String headerName) {
        List<String> headerList = responseEntity.getHeaders().get(headerName);
        assertFalse(CollectionUtils.isEmpty(headerList));
        assertEquals(expectedHeaderValue, headerList.get(0));
    }
}