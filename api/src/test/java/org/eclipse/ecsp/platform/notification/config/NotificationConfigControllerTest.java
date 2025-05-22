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

import org.eclipse.ecsp.domain.notification.ApiPushChannel;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.CreateContactResponse;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.config.NotificationConfigRequest;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.NotificationConfigResponse;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.DisableApiPushChannelException;
import org.eclipse.ecsp.platform.notification.exceptions.EmptyNotificationConfig;
import org.eclipse.ecsp.platform.notification.exceptions.FcmTokenNotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidContactInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidGroupsException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidUserIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.MandatoryGroupsNotAllowedException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationGroupingNotAllowedException;
import org.eclipse.ecsp.platform.notification.exceptions.SuppressApiPushChannelException;
import org.eclipse.ecsp.platform.notification.exceptions.UserIdNotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.FCM_TOKEN_DELETE_SUCCESS;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.FCM_TOKEN_DOES_NOT_EXIST;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_MANDATORY_GROUP_NOT_ALLOWED;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_USER_ID_NOT_FOUND;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * NotificationConfigControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationConfigControllerTest {
    private static final String SESSION_ID = "SESSION_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";
    private static final String REQUEST_ID = "REQ";
    private static final String CONTACT_ID = "contactId";

    public static final String GRP_NAME_GRP1 = "grp1";
    public static final String GRP_NAME_GRP2 = "grp2";
    public static final String GRP_NAME_G1 = "g1";
    public static final String GRP_NAME_G2 = "g2";
    public static final String USER_ID = "userId";
    public static final String VEHICLE_ID = "vehicleId";
    public static final String SELF = "self";

    @InjectMocks
    private NotificationConfigController notificationConfigController;

    @Mock
    NotificationConfigServiceV1_0 notificationConfigServiceV1;

    @Mock
    NotificationGroupingDAO notificationGroupingDao;

    @Mock
    NotificationConfigDAO configDao;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void updateNotificationConfigValidateInputResponseNotNull() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("APIPush should not be allowed for secondary contact", responseEntity.getBody());
    }

    @Test
    public void updateNotificationConfigModifyingMandatoryGroupNotAllowed() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        List<NotificationGrouping> set = getNotificationGrouping(GRP_NAME_GRP1);
        Mockito.doReturn(set).when(notificationGroupingDao).findByMandatory(true);
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Modifying mandatory notification group grp1 not allowed", responseEntity.getBody());
        assertHeader(responseEntity, CLIENT_REQUEST_ID, "ClientRequestId");
        assertHeader(responseEntity, SESSION_ID, "SessionId");
        assertHeader(responseEntity, REQUEST_ID, "PlatformResponseId");
    }

    @Test
    public void updateNotificationConfigModifyingMandatoryGroupAllowed() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);

        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        List<NotificationGrouping> set = getNotificationGrouping(GRP_NAME_GRP2);
        Mockito.doReturn(set).when(notificationGroupingDao).findByMandatory(true);
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("APIPush should not be allowed for secondary contact", responseEntity.getBody());
        assertHeader(responseEntity, CLIENT_REQUEST_ID, "ClientRequestId");
        assertHeader(responseEntity, SESSION_ID, "SessionId");
        assertHeader(responseEntity, REQUEST_ID, "PlatformResponseId");
    }

    @Test
    public void updateNotificationConfigEmailChannel() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.EMAIL, null);

        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        List<NotificationGrouping> set = getNotificationGrouping(GRP_NAME_GRP2);
        Mockito.doReturn(set).when(notificationGroupingDao).findByMandatory(true);
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(OK, responseEntity.getStatusCode());
        assertHeader(responseEntity, CLIENT_REQUEST_ID, "ClientRequestId");
        assertHeader(responseEntity, SESSION_ID, "SessionId");
        assertHeader(responseEntity, REQUEST_ID, "PlatformResponseId");
    }

    @Test
    public void updateNotificationConfigForSelfContact() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, false, ChannelType.API_PUSH, null);
        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        List<NotificationGrouping> set = getNotificationGrouping(GRP_NAME_GRP2);
        Mockito.doReturn(set).when(notificationGroupingDao).findByMandatory(true);
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, SELF, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("APIPush cannot be disabled", responseEntity.getBody());

        assertHeader(responseEntity, CLIENT_REQUEST_ID, "ClientRequestId");
        assertHeader(responseEntity, SESSION_ID, "SessionId");
        assertHeader(responseEntity, REQUEST_ID, "PlatformResponseId");
    }

    @Test
    public void updateNotificationConfigForSelfContactWithEnabledChannel() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);

        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        List<NotificationGrouping> set = getNotificationGrouping(GRP_NAME_GRP2);
        Mockito.doReturn(set).when(notificationGroupingDao).findByMandatory(true);
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, SELF, requestConfigs);
        assertEquals(OK, responseEntity.getStatusCode());

        assertHeader(responseEntity, CLIENT_REQUEST_ID, "ClientRequestId");
        assertHeader(responseEntity, SESSION_ID, "SessionId");
        assertHeader(responseEntity, REQUEST_ID, "PlatformResponseId");
    }

    @Test
    public void updateNotificationConfigWithSuppressionConfig() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH,
                SuppressionConfig.SuppressionType.RECURRING);

        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        List<NotificationGrouping> set = getNotificationGrouping(GRP_NAME_GRP2);
        Mockito.doReturn(set).when(notificationGroupingDao).findByMandatory(true);
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, SELF, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("APIPush cannot be Suppressed", responseEntity.getBody());
        assertHeader(responseEntity, CLIENT_REQUEST_ID, "ClientRequestId");
        assertHeader(responseEntity, SESSION_ID, "SessionId");
        assertHeader(responseEntity, REQUEST_ID, "PlatformResponseId");
    }

    @Test
    public void updateNotificationConfigThrowInvalidUserIdInput() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doThrow(new InvalidUserIdInput("InvalidUserIdInput")).when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("InvalidUserIdInput", responseEntity.getBody());
    }

    @Test
    public void updateNotificationConfigThrowInvalidVehicleIdInput() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doThrow(new InvalidVehicleIdInput("InvalidVehicleIdInput")).when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("InvalidVehicleIdInput", responseEntity.getBody());
    }

    @Test
    public void updateNotificationConfigThrowInvalidContactInput() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doThrow(new InvalidContactInput("InvalidContactInput")).when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("InvalidContactInput", responseEntity.getBody());
    }

    @Test
    public void updateNotificationConfigThrowAuthorizationException() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doThrow(new AuthorizationException("AuthorizationException")).when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("AuthorizationException", responseEntity.getBody());
    }

    @Test
    public void updateNotificationConfigThrowNoSuchEntityException() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doThrow(new NoSuchEntityException("NoSuchEntityException")).when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("NoSuchEntityException", responseEntity.getBody());
    }

    @Test
    public void updateNotificationConfigThrowInvalidInputException() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doThrow(new InvalidInputException(null)).when(notificationConfigServiceV1)
            .validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void updateNotificationConfigThrowNotificationGroupingNotAllowedException() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_GRP1, true, ChannelType.API_PUSH, null);
        Mockito.doThrow(new NotificationGroupingNotAllowedException(Collections.emptyList(), "ff", "gg"))
            .when(notificationConfigServiceV1).validateInput(any(), any(), any(), Mockito.anyCollection());
        ResponseEntity<String> responseEntity =
            notificationConfigController.updateNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID, CONTACT_ID, requestConfigs);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void getNotificationConfig() {
        List<NotificationConfigResponse> notificationConfigResponses = new ArrayList<>();
        NotificationConfigResponse notificationConfigResponse = new NotificationConfigResponse();
        notificationConfigResponse.setContactId(CONTACT_ID);
        Mockito.doReturn(notificationConfigResponses).when(notificationConfigServiceV1)
            .getNotificationPreference(any(), any(), any());
        ResponseEntity<List<NotificationConfigResponse>> responseEntity =
            notificationConfigController.getNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                VEHICLE_ID);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(notificationConfigResponses, responseEntity.getBody());
    }

    @Test
    public void getNotificationConfigOfService() {
        List<NotificationConfigResponse> notificationConfigResponses = new ArrayList<>();
        NotificationConfigResponse notificationConfigResponse = new NotificationConfigResponse();
        notificationConfigResponse.setContactId(CONTACT_ID);
        Mockito.doReturn(notificationConfigResponses).when(notificationConfigServiceV1)
            .getNotificationPreference(any(), any(), any());
        ResponseEntity<List<NotificationConfigResponse>> responseEntity =
            notificationConfigController.getNotificationConfigOfService(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, VEHICLE_ID, null);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(notificationConfigResponses, responseEntity.getBody());
    }

    @Test
    public void getUserNotificationConfig() {
        List<NotificationConfigResponse> notificationConfigResponses = new ArrayList<>();
        NotificationConfigResponse notificationConfigResponse = new NotificationConfigResponse();
        notificationConfigResponse.setContactId(CONTACT_ID);
        Mockito.doReturn(true).when(notificationConfigServiceV1).isUserExists(any());
        Mockito.doReturn(notificationConfigResponses).when(notificationConfigServiceV1)
            .getNotificationPreference(any(), any(), any());
        List<NotificationConfigResponse> notificationConfigResponsesActual =
            notificationConfigController.getUserNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID);
        assertEquals(notificationConfigResponses, notificationConfigResponsesActual);
    }

    @Test
    public void getUserNotificationConfigWithInvalidUser() {
        UserIdNotFoundException thrown =
            assertThrows(UserIdNotFoundException.class,
                () -> notificationConfigController.getUserNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                    USER_ID));
        Assert.assertEquals("user id not found", thrown.getMessage());
    }

    @Test
    public void updateUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName("Ocean");
        ResponseEntity<Void> responseEntity =
            notificationConfigController.updateUserProfile(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, USER_ID,
                userProfile);
        assertHeader(responseEntity, CLIENT_REQUEST_ID, "ClientRequestId");
        assertHeader(responseEntity, SESSION_ID, "SessionId");
        assertHeader(responseEntity, REQUEST_ID, "PlatformResponseId");
    }

    @Test
    public void postNotificationTemplate() {
        Mockito.doReturn(true).when(notificationConfigServiceV1).saveNotificationTemplates(any());
        ResponseEntity<String> responseEntity = notificationConfigController.postNotificationTemplate(null);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals("Notification Templates successfully processed", responseEntity.getBody());
    }

    @Test
    public void postNotificationTemplateWhenSaveFailed() {
        Mockito.doReturn(false).when(notificationConfigServiceV1).saveNotificationTemplates(any());
        ResponseEntity<String> responseEntity = notificationConfigController.postNotificationTemplate(null);
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Notification Templates not processed", responseEntity.getBody());
    }

    @Test
    public void postNotificationTemplateConfig() {
        Mockito.doReturn(true).when(notificationConfigServiceV1).saveNotificationTemplatesConfigs(any());
        ResponseEntity<String> responseEntity = notificationConfigController.postNotificationTemplateConfig(null);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals("Template Config successfully processed", responseEntity.getBody());
    }

    @Test
    public void postNotificationTemplateConfigWhenSaveFailed() {
        Mockito.doReturn(false).when(notificationConfigServiceV1).saveNotificationTemplatesConfigs(any());
        ResponseEntity<String> responseEntity = notificationConfigController.postNotificationTemplateConfig(null);
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Template Config not processed", responseEntity.getBody());
    }

    @Test
    public void postNotificationGrouping() {
        Mockito.doReturn(true).when(notificationConfigServiceV1).saveNotificationGrouping(any());
        ResponseEntity<String> responseEntity = notificationConfigController.postNotificationGrouping(null);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals("Notification Grouping successfully processed", responseEntity.getBody());
    }

    @Test
    public void postNotificationGroupingWhenSaveFailed() {
        Mockito.doReturn(false).when(notificationConfigServiceV1).saveNotificationGrouping(any());
        ResponseEntity<String> responseEntity = notificationConfigController.postNotificationGrouping(null);
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Notification Grouping not processed", responseEntity.getBody());
    }

    @Test
    public void getNotificationGrouping() {
        List<NotificationGrouping> notificationGroupings = new ArrayList<>();
        Mockito.doReturn(notificationGroupings).when(notificationConfigServiceV1).getNotificationGrouping(any());
        ResponseEntity<List<NotificationGrouping>> responseEntity =
            notificationConfigController.getNotificationGrouping(null);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(notificationGroupings, responseEntity.getBody());
    }

    private List<NotificationGrouping> getNotificationGrouping(String grpName) {
        List<NotificationGrouping> notificationGroupings = new ArrayList<>();
        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setGroup(grpName);
        notificationGrouping.setMandatory(true);
        notificationGroupings.add(notificationGrouping);
        return notificationGroupings;
    }

    @Test
    public void deleteNotificationGrouping() {
        ResponseEntity<Void> responseEntity = notificationConfigController.deleteNotificationGrouping(null, null, null);
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void createContact() {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setContactId("007");
        ResponseEntity<CreateContactResponse> responseEntity =
            notificationConfigController.createContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null, null,
                secondaryContact);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(secondaryContact.getContactId(), Objects.requireNonNull(responseEntity.getBody()).getContactId());
    }

    @Test
    public void updateContact() {
        SecondaryContact existingContact = new SecondaryContact();
        existingContact.setContactId("007");
        existingContact.setUserId("abc");
        Mockito.doReturn(existingContact).when(notificationConfigServiceV1).getSecondaryContact(any());
        ResponseEntity<CreateContactResponse> responseEntity =
            notificationConfigController.updateContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "abc", null,
                existingContact);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(existingContact.getContactId(),
            ((CreateContactResponse) Objects.requireNonNull(responseEntity.getBody())).getContactId());
    }

    @Test
    public void updateContactWhenExistingContactNull() {
        Mockito.doReturn(null).when(notificationConfigServiceV1).getSecondaryContact(any());
        ResponseEntity<CreateContactResponse> responseEntity =
            notificationConfigController.updateContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "abc", null, null);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Invalid contact id", responseEntity.getBody().getMessage());
    }

    @Test
    public void updateContactWhenMisMatchUserId() {
        SecondaryContact existingContact = new SecondaryContact();
        existingContact.setContactId("007");
        existingContact.setUserId("me");
        ResponseEntity<CreateContactResponse> responseEntity =
            notificationConfigController.updateContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "you", null,
                existingContact);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Invalid contact id", responseEntity.getBody().getMessage());
    }

    @Test
    public void deleteContact() {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setContactId("007");
        secondaryContact.setUserId("abc");
        Mockito.doReturn(secondaryContact).when(notificationConfigServiceV1).getSecondaryContact(any());
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "abc",
                secondaryContact.getContactId());
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void deleteContactWhenSecondaryContactNull() {
        Mockito.doReturn(null).when(notificationConfigServiceV1).getSecondaryContact(any());
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "abc", null);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Invalid contact id", responseEntity.getBody());
    }

    @Test
    public void deleteContactWhenUserMismatch() {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setContactId("007");
        secondaryContact.setUserId("hey");
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "you",
                secondaryContact.getContactId());
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Invalid contact id", responseEntity.getBody());
    }

    @Test
    public void getUserProfile() {
        UserProfile userProfile = new UserProfile();
        Mockito.doReturn(userProfile).when(notificationConfigServiceV1).getUserProfile(any());
        ResponseEntity<UserProfile> responseEntity = notificationConfigController.getUserProfile(null);
        assertEquals(OK, responseEntity.getStatusCode());
    }



    @Test
    public void deleteContact2WhenSecondaryContactUserIdMismatch() {
        SecondaryContact secondaryContact = getSecondaryContact("c1", "u1");
        Mockito.doReturn(secondaryContact).when(notificationConfigServiceV1).getSecondaryContact(any());
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "u11", null,
                secondaryContact.getContactId());
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Invalid contact id", responseEntity.getBody());
    }

    @Test
    public void deleteContact2WhenSecondaryContactNull() {
        SecondaryContact secondaryContact = getSecondaryContact("c1", "u1");
        Mockito.doReturn(null).when(notificationConfigServiceV1).getSecondaryContact(any());
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "u11", null,
                secondaryContact.getContactId());
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Invalid contact id", responseEntity.getBody());
    }

    @Test
    public void deleteContact2WhenContactIdSelf() {
        SecondaryContact secondaryContact = getSecondaryContact("self", "u1");
        Mockito.doReturn(secondaryContact).when(notificationConfigServiceV1).getSecondaryContact(any());
        List<NotificationConfig> configs = new ArrayList<>();
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setContactId(secondaryContact.getContactId());
        configs.add(notificationConfig);
        Mockito.doReturn(configs).when(configDao).findByUserVehicleContactId(any(), any(), any());
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "u11", null,
                secondaryContact.getContactId());
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void deleteContact2WhenContactIdSelfAndNotificationConfigNull() {
        SecondaryContact secondaryContact = getSecondaryContact("self", "u1");
        Mockito.doReturn(secondaryContact).when(notificationConfigServiceV1).getSecondaryContact(any());
        Mockito.doReturn(null).when(configDao).findByUserVehicleContactId(any(), any(), any());
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "u11", null,
                secondaryContact.getContactId());
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void deleteContact2WhenContactIdSelfAndNotificationConfigEmpty() {
        SecondaryContact secondaryContact = getSecondaryContact("self", "u1");
        Mockito.doReturn(secondaryContact).when(notificationConfigServiceV1).getSecondaryContact(any());
        List<NotificationConfig> configs = new ArrayList<>();
        Mockito.doReturn(configs).when(configDao).findByUserVehicleContactId(any(), any(), any());
        ResponseEntity<String> responseEntity =
            notificationConfigController.deleteContact(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "u11", null,
                secondaryContact.getContactId());
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void createDefaultNotificationConfig() {
        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_G1, true, ChannelType.API_PUSH,
                SuppressionConfig.SuppressionType.RECURRING);
        ResponseEntity<String> responseEntity =
            notificationConfigController.createDefaultNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                requestConfigs);
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void createDefaultNotificationConfigWhenRequestConfigEmpty() {
        List<NotificationConfigRequest> requestConfigs = new ArrayList<>();
        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class,
                () -> notificationConfigController.createDefaultNotificationConfig(REQUEST_ID, SESSION_ID,
                    CLIENT_REQUEST_ID, requestConfigs));
        Assert.assertEquals("expecting array of configs", thrown.getMessage());
    }

    @Test
    public void getDefaultConfigWhenBrandIsNull() {
        ReflectionTestUtils.setField(notificationConfigController, "defaultBrand", "default");
        NotificationConfig notificationConfig = new NotificationConfig();
        Mockito.doReturn(notificationConfig).when(notificationConfigServiceV1).getDefaultConfig(null, "default");

        ResponseEntity<NotificationConfig> responseEntity =
            notificationConfigController.getDefaultConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null, null);
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void getDefaultConfigWhenBrandSpecified() {
        ReflectionTestUtils.setField(notificationConfigController, "defaultBrand", "default");
        NotificationConfig notificationConfig = new NotificationConfig();
        Mockito.doReturn(notificationConfig).when(notificationConfigServiceV1).getDefaultConfig(null, "brand1");

        ResponseEntity<NotificationConfig> responseEntity =
            notificationConfigController.getDefaultConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null, "brand1");
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void getAllDefaultConfigs() {
        List<NotificationConfig> notificationConfigs = new ArrayList<>();
        notificationConfigs.add(new NotificationConfig());
        Mockito.doReturn(notificationConfigs).when(notificationConfigServiceV1).getAllDefaultConfigs(any());
        ResponseEntity<List<NotificationConfig>> responseEntity =
            notificationConfigController.getAllDefaultConfigs(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void createUserNotificationConfigWhenRequestEmpty() {
        Mockito.doThrow(IllegalArgumentException.class).when(notificationConfigServiceV1)
            .validateInput(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
        assertThrows(EmptyNotificationConfig.class,
            () -> notificationConfigController.createUserNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, new ArrayList<>()));
    }

    @Test
    public void createUserNotificationConfigWhenUserDoesNotExists() {
        Mockito.doThrow(new InvalidUserIdInput("User doesn't exist")).when(notificationConfigServiceV1)
            .validateInput(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
        assertThrows(UserIdNotFoundException.class,
            () -> notificationConfigController.createUserNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, new ArrayList<>()));
    }

    @Test
    public void createUserNotificationConfigWithInvalidGroup() {
        Mockito.doThrow(NoSuchEntityException.class).when(notificationConfigServiceV1)
            .validateInput(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList());
        assertThrows(InvalidGroupsException.class,
            () -> notificationConfigController.createUserNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                "u1", new ArrayList<>()));
    }

    @Test
    public void createUserNotificationConfigMandatoryGroupNotAllowed() {
        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList());

        List<NotificationGrouping> notificationGroupings = getNotificationGrouping(GRP_NAME_G1);
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByMandatory(true);

        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_G1, true, ChannelType.API_PUSH,
                SuppressionConfig.SuppressionType.RECURRING);

        RuntimeException thrown =
            assertThrows(MandatoryGroupsNotAllowedException.class,
                () -> notificationConfigController.createUserNotificationConfig(REQUEST_ID, SESSION_ID,
                    CLIENT_REQUEST_ID, "u1", requestConfigs));
        Assert.assertTrue(thrown.getMessage()
            .startsWith("Notification config can not contains mandatory groups, the invalid group is: "));
    }

    @Test
    public void createUserNotificationConfigWithApiPushChannelDisabled() {
        Mockito.doNothing().when(notificationConfigServiceV1)
            .validateInput(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList());

        List<NotificationGrouping> notificationGroupings = getNotificationGrouping(GRP_NAME_G1);
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByMandatory(true);

        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_G2, false, ChannelType.API_PUSH, null);

        DisableApiPushChannelException thrown =
            assertThrows(DisableApiPushChannelException.class,
                () -> notificationConfigController.createUserNotificationConfig(REQUEST_ID, SESSION_ID,
                    CLIENT_REQUEST_ID, "u1", requestConfigs));
        Assert.assertEquals("Can not disable  api push channel", thrown.getMessage());
    }


    @Test
    public void createUserNotificationConfigWithSuppressionConfig() {
        Mockito.doReturn(true).when(notificationConfigServiceV1).isUserExists(Mockito.anyString());

        List<NotificationGrouping> notificationGroupings = getNotificationGrouping(GRP_NAME_G1);
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByMandatory(true);

        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_G2, true, ChannelType.API_PUSH,
                SuppressionConfig.SuppressionType.RECURRING);

        SuppressApiPushChannelException thrown =
            assertThrows(SuppressApiPushChannelException.class,
                () -> notificationConfigController.createUserNotificationConfig(REQUEST_ID, SESSION_ID,
                    CLIENT_REQUEST_ID, "u1", requestConfigs));
        Assert.assertEquals("Can not suppress api push channel", thrown.getMessage());
    }

    @Test
    public void createUserNotificationConfig() {
        Mockito.doReturn(true).when(notificationConfigServiceV1).isUserExists(Mockito.anyString());

        List<NotificationGrouping> notificationGroupings = getNotificationGrouping(GRP_NAME_G1);
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByMandatory(true);

        List<NotificationConfig> notificationConfigList = new ArrayList<>();
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("u1");
        notificationConfigList.add(notificationConfig);
        Mockito.doReturn(notificationConfigList).when(notificationConfigServiceV1)
            .saveNotificationConfig(any(), any(), any(), any());

        List<NotificationConfigRequest> requestConfigs =
            getNotificationConfigRequestList(GRP_NAME_G2, true, ChannelType.API_PUSH, null);
        List<NotificationConfig> notificationConfigListActual =
            notificationConfigController.createUserNotificationConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "u1",
                requestConfigs);
        assertEquals(notificationConfigList, notificationConfigListActual);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void deleteToken() {
        ResponseWrapper<Void> responseWrapper =
            notificationConfigController.deleteToken(REQUEST_ID, SESSION_ID, null, CLIENT_REQUEST_ID, null);
        assertEquals(200, responseWrapper.getHttpStatusCode());
        assertEquals(REQUEST_ID, responseWrapper.getRequestId());
        assertEquals(FCM_TOKEN_DELETE_SUCCESS.getCode(), responseWrapper.getRootMessage().getCode());
        assertEquals(FCM_TOKEN_DELETE_SUCCESS.getReason(), responseWrapper.getRootMessage().getReason());
        assertEquals(FCM_TOKEN_DELETE_SUCCESS.getMessage(), responseWrapper.getRootMessage().getMsg());

    }

    @Test
    public void invalidUserIdException() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            notificationConfigController.invalidUserIdException(new FcmTokenNotFoundException(new ArrayList<>()),
                request);
        assertEquals(BAD_REQUEST.value(), responseWrapper.getHttpStatusCode());
        assertEquals(FCM_TOKEN_DOES_NOT_EXIST.getCode(), responseWrapper.getRootMessage().getCode());
        assertEquals(FCM_TOKEN_DOES_NOT_EXIST.getReason(), responseWrapper.getRootMessage().getReason());
        assertEquals(FCM_TOKEN_DOES_NOT_EXIST.getMessage(), responseWrapper.getRootMessage().getMsg());
    }

    @Test
    public void mandatoryGroupsNotAllowedException() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper = notificationConfigController.mandatoryGroupsNotAllowedException(
            new MandatoryGroupsNotAllowedException(new ArrayList<>(), ""), request);
        assertEquals(BAD_REQUEST.value(), responseWrapper.getHttpStatusCode());
        assertEquals(NOTIFICATION_CONFIG_MANDATORY_GROUP_NOT_ALLOWED.getCode(),
            responseWrapper.getRootMessage().getCode());
        assertEquals(NOTIFICATION_CONFIG_MANDATORY_GROUP_NOT_ALLOWED.getReason(),
            responseWrapper.getRootMessage().getReason());
        assertEquals(NOTIFICATION_CONFIG_MANDATORY_GROUP_NOT_ALLOWED.getMessage(),
            responseWrapper.getRootMessage().getMsg());
    }

    @Test
    public void userIdNotFoundException() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            notificationConfigController.notFound(new UserIdNotFoundException(new ArrayList<>()), request);
        assertEquals(NOT_FOUND.value(), responseWrapper.getHttpStatusCode());
        assertEquals(NOTIFICATION_CONFIG_USER_ID_NOT_FOUND.getCode(), responseWrapper.getRootMessage().getCode());
        assertEquals(NOTIFICATION_CONFIG_USER_ID_NOT_FOUND.getReason(), responseWrapper.getRootMessage().getReason());
        assertEquals(NOTIFICATION_CONFIG_USER_ID_NOT_FOUND.getMessage(), responseWrapper.getRootMessage().getMsg());
    }

    @Test
    public void vehicleIdNotFoundException() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            notificationConfigController.notFound(new VehicleIdNotFoundException(new ArrayList<>()), request);
        assertEquals(NOT_FOUND.value(), responseWrapper.getHttpStatusCode());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getCode(), responseWrapper.getRootMessage().getCode());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getReason(),
            responseWrapper.getRootMessage().getReason());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getMessage(),
            responseWrapper.getRootMessage().getMsg());
    }

    @Test
    public void invalidContactInput() {
        ResponseEntity<String> responseEntity =
            notificationConfigController.invalidContactInput(new InvalidContactInput(""));
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void invalidInput() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            notificationConfigController.invalidInput(new EmptyNotificationConfig(new ArrayList<>()), request);
        assertEquals(BAD_REQUEST.value(), responseWrapper.getHttpStatusCode());
        assertEquals(NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST.getCode(),
            responseWrapper.getRootMessage().getCode());
        assertEquals(NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST.getReason(),
            responseWrapper.getRootMessage().getReason());
        assertEquals(NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST.getMessage(),
            responseWrapper.getRootMessage().getMsg());
    }

    @Test
    public void notFound() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            notificationConfigController.notFound(new EmptyNotificationConfig(new ArrayList<>()), request);
        assertEquals(NOT_FOUND.value(), responseWrapper.getHttpStatusCode());
        assertEquals(NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST.getCode(),
            responseWrapper.getRootMessage().getCode());
        assertEquals(NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST.getReason(),
            responseWrapper.getRootMessage().getReason());
        assertEquals(NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST.getMessage(),
            responseWrapper.getRootMessage().getMsg());
    }

    private void assertHeader(ResponseEntity<?> responseEntity, String expectedHeader, String actualHeader) {
        List<String> clientRequestId = responseEntity.getHeaders().get(actualHeader);
        assert clientRequestId != null;
        assertEquals(expectedHeader, clientRequestId.get(0));
    }

    private List<NotificationConfigRequest> getNotificationConfigRequestList(String grpName, boolean channelEnabled,
                 ChannelType channelType,
                 SuppressionConfig.SuppressionType suppressionType) {

        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup(grpName);
        List<Channel> channels = new ArrayList<>();
        Channel channel = ChannelType.API_PUSH.equals(channelType) ? new ApiPushChannel() : new EmailChannel();
        channel.setEnabled(channelEnabled);
        if (suppressionType != null) {
            List<SuppressionConfig> suppressionConfigs = new ArrayList<>();
            SuppressionConfig suppressionConfig = new SuppressionConfig();
            suppressionConfig.setSuppressionType(suppressionType);
            suppressionConfigs.add(suppressionConfig);
            channel.setSuppressionConfigs(suppressionConfigs);
        }
        List<NotificationConfigRequest> requestConfigs = new ArrayList<>();
        channels.add(channel);
        notificationConfigRequest.setChannels(channels);

        requestConfigs.add(notificationConfigRequest);
        return requestConfigs;
    }



    private SecondaryContact getSecondaryContact(String contactId, String userId) {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setContactId(contactId);
        secondaryContact.setUserId(userId);
        return secondaryContact;
    }
}
