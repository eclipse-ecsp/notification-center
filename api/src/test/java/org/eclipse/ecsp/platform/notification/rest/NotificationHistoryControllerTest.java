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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelResponseData;
import org.eclipse.ecsp.domain.notification.utils.UserService;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.platform.notification.dto.CampaignDetail;
import org.eclipse.ecsp.platform.notification.dto.CampaignSummary;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNonRegisteredVehicle;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.eclipse.ecsp.platform.notification.service.NotificationHistoryService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
/**
 * NotificationHistoryControllerTest.
 */

@SuppressWarnings("checkstyle:MagicNumber")
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationHistoryControllerTest {
    private static final String STATUS = "status";
    private static final String CHANNEL_DETAILS_ID = "CHANNEL_DETAILS_ID";
    private static final String CHANNEL_DETAILS_NOTIFICATION_NAME = "CHANNEL_DETAILS_NOTIFICATION_NAME";
    private static final String CHANNEL_DETAILS_GROUP = "CHANNEL_DETAILS_GROUP";
    private static final long CHANNEL_DETAILS_DATE = System.currentTimeMillis();
    private static final String SESSION_ID = "SESSION_ID";
    private static final String PLATFORM_RESPONSE_ID = "PLATFORM_RESPONSE_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";
    private static final String VEHICLE_ID = "VEHICLE_ID";
    private static final String REQUEST_ID = "REQ";
    private static final String USER_ID = "USER_ID";
    public static final String EMAIL = "email";
    public static final String TEST_EMAIL = "test@gmail.com";
    public static final String SUCCESS = "success";

    @InjectMocks
    private NotificationHistoryController notificationHistoryController;

    @Mock
    NotificationHistoryService notificationHistoryService;

    @Mock
    UserService userService;

    @Mock
    VehicleService vehicleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }


    @Test
    public void getVehicleNotificationStatus() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doReturn(new UserProfile()).when(userService).getUser("userId");
        Mockito.doReturn(true).when(vehicleService).isVehicleExist("vehicleId");
        Mockito.doReturn(notificationChannelDetails).when(notificationHistoryService)
            .getNotificationStatus(Mockito.any(), Mockito.any(),
                Mockito.any());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatus(PLATFORM_RESPONSE_ID, SESSION_ID, null,
                "userId", "vehicleId", PLATFORM_RESPONSE_ID, STATUS, false);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME, jsonNode.get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get("group").asText());
    }

    @Test
    public void getNotificationStatusNonRegVehicle() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doReturn(new UserProfile()).when(userService).getUser("userId");
        Mockito.doReturn(false).when(vehicleService).isVehicleExist("vehicleId");
        Mockito.doReturn(notificationChannelDetails).when(notificationHistoryService)
            .getNotificationStatus(Mockito.any(), Mockito.any(),
                Mockito.any());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatus(PLATFORM_RESPONSE_ID, SESSION_ID, null,
                "userId", "vehicleId", PLATFORM_RESPONSE_ID, STATUS, true);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME, jsonNode.get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get("group").asText());
    }


    @Test
    public void getNotificationNonRegVehicleStatusNotFound() throws Exception {
        Mockito.doReturn(new UserProfile()).when(userService).getUser("userId");
        Mockito.doReturn(false).when(vehicleService).isVehicleExist("vehicleId");
        when(notificationHistoryService.getNotificationStatus(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(
                NotFoundException.class);
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatus(PLATFORM_RESPONSE_ID, SESSION_ID, null,
                "userId", "vehicleId", PLATFORM_RESPONSE_ID, STATUS, true);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test(expected = InvalidNonRegisteredVehicle.class)
    public void getNotificationNonRegVehicleNotExists() throws Exception {
        Mockito.doReturn(new UserProfile()).when(userService).getUser("userId");
        Mockito.doReturn(true).when(vehicleService).isVehicleExist("vehicleId");
        when(notificationHistoryService.getNotificationStatus(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(
                NotFoundException.class);
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatus(PLATFORM_RESPONSE_ID, SESSION_ID, null,
                "userId", "vehicleId", PLATFORM_RESPONSE_ID, STATUS, true);
    }

    @Test
    public void getNotificationStatusByVehicleIdAndPlatformIdBadContent() throws Exception {
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doReturn(notificationChannelDetails).when(notificationHistoryService)
            .getNotificationStatus(Mockito.any(), Mockito.any(),
                Mockito.any());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatusByVehicleIdAndPlatformId(REQUEST_ID,
                SESSION_ID, CLIENT_REQUEST_ID, VEHICLE_ID, PLATFORM_RESPONSE_ID, "VIOLA", false);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(StringUtils.EMPTY, responseEntity.getBody());
    }

    @Test
    public void getNotificationStatusByVehicleIdAndPlatformIdNotFound() throws Exception {
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        when(notificationHistoryService.getNotificationStatus(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(
                NotFoundException.class);
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatusByVehicleIdAndPlatformId(REQUEST_ID,
                SESSION_ID, CLIENT_REQUEST_ID, VEHICLE_ID, PLATFORM_RESPONSE_ID, STATUS, false);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void getNotificationStatusByUserIdAndPlatformId() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        Mockito.doReturn(notificationChannelDetails).when(notificationHistoryService)
            .getNotificationStatus(Mockito.any(), Mockito.any(),
                Mockito.any());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatusByUserIdAndPlatformId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID,
                USER_ID, PLATFORM_RESPONSE_ID, STATUS);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME, jsonNode.get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get("group").asText());
    }

    @Test
    public void getNotificationStatusByUserIdAndPlatformIdBadContent() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        Mockito.doReturn(notificationChannelDetails).when(notificationHistoryService)
            .getNotificationStatus(Mockito.any(), Mockito.any(),
                Mockito.any());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatusByUserIdAndPlatformId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, PLATFORM_RESPONSE_ID, "JET");
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(StringUtils.EMPTY, responseEntity.getBody());
    }

    @Test
    public void getNotificationStatusByUserIdAndPlatformIdNotFound() throws Exception {
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        when(notificationHistoryService.getNotificationStatus(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(
                NotFoundException.class);
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationStatusByUserIdAndPlatformId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, USER_ID, PLATFORM_RESPONSE_ID, STATUS);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void getNotificationHistoryUserIdVehicleIdNotFound() {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        String link = "/v1/users/" + USER_ID + "/vehicles/" + VEHICLE_ID + "/notifications/"
            + notificationChannelDetails.getId() + "?content=full";
        notificationChannelDetails.setLink(link);
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        when(notificationHistoryService.getNotificationHistoryUserIdVehicleId(Mockito.any(), Mockito.any(),
            Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationHistoryUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID,
                USER_ID, VEHICLE_ID, 1L, 1L, 1, 1, false);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void getNotificationHistoryUserIdVehicleIdInternalServerError() {
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        when(notificationHistoryService.getNotificationHistoryUserIdVehicleId(Mockito.any(), Mockito.any(),
            Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
            .thenThrow(RuntimeException.class);
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationHistoryUserIdVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID,
                USER_ID, VEHICLE_ID, 1L, 1L, 1, 1, false);
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void getNotificationHistoryUserId() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        String link = "/v1/users/" + USER_ID + "/notifications/" + notificationChannelDetails.getId() + "?content=full";
        notificationChannelDetails.setLink(link);
        List<NotificationChannelDetails> notificationChannelDetailsList = new ArrayList<>();
        notificationChannelDetailsList.add(notificationChannelDetails);
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        Mockito.doReturn(notificationChannelDetailsList).when(notificationHistoryService)
            .getNotificationHistoryUserId(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationHistoryUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, 1L, 1L, 1, 1);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get(0).get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME, jsonNode.get(0).get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get(0).get("group").asText());
        assertEquals(link, jsonNode.get(0).get("link").asText());
    }

    @Test
    public void getNotificationHistoryByPlatformResponseId() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doReturn(notificationChannelDetails).when(notificationHistoryService)
            .getNotificationStatus(Mockito.any(), Mockito.any(),
                Mockito.any());
        ResponseEntity<NotificationChannelDetails> responseEntity = notificationHistoryController
            .getNotificationStatusByPlatformId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                notificationChannelDetails.getId(), STATUS);
        ChannelResponseData emailChanelResponse =
            Objects.requireNonNull(responseEntity.getBody()).getChannelResponses().get(0);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(EMAIL, emailChanelResponse.getChannelType());
        assertEquals(TEST_EMAIL, emailChanelResponse.getDestination());
        assertEquals(SUCCESS, emailChanelResponse.getStatus());
    }

    @Test(expected = NotFoundException.class)
    public void getNotificationHistoryFailurePlatformResponseIdDoseNotExist()
            throws NotFoundException, javassist.NotFoundException {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doThrow(new NotFoundException(null)).when(notificationHistoryService)
            .getNotificationStatus(Mockito.any(), Mockito.any(),
                Mockito.any());
        notificationHistoryController.getNotificationStatusByPlatformId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
            notificationChannelDetails.getId(), STATUS);
    }

    @Test(expected = NotFoundException.class)
    public void getNotificationHistoryFailureContentTypeDoseNotExist() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        notificationHistoryController.getNotificationStatusByPlatformId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
            notificationChannelDetails.getId(), "invalid_content_type");
    }

    @Test
    public void getNotificationHistoryUserIdNotFound() throws Exception {
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        when(
            notificationHistoryService.getNotificationHistoryUserId(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(),
                Mockito.anyInt(), Mockito.anyInt()))
            .thenThrow(RuntimeException.class);
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationHistoryUserId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                USER_ID, 1L, 1L, 1, 1);
        assertEquals(NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void getNotificationHistoryVehicleId() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        String link =
            "/v1/vehicles/" + VEHICLE_ID + "/notifications/" + notificationChannelDetails.getId() + "?content=full";
        notificationChannelDetails.setLink(link);
        List<NotificationChannelDetails> notificationChannelDetailsList = new ArrayList<>();
        notificationChannelDetailsList.add(notificationChannelDetails);
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        Mockito.doReturn(notificationChannelDetailsList).when(notificationHistoryService)
            .getNotificationHistoryVehicleId(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationHistoryVehicleId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                VEHICLE_ID, 1L, 1L, 1, 1, false);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get(0).get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME, jsonNode.get(0).get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get(0).get("group").asText());
        assertEquals(link, jsonNode.get(0).get("link").asText());
    }

    @Test
    public void getNotificationHistoryNonRegVehicleId() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        String link =
            "/v1/vehicles/" + VEHICLE_ID + "/notifications/" + notificationChannelDetails.getId() + "?content=full";
        notificationChannelDetails.setLink(link);
        List<NotificationChannelDetails> notificationChannelDetailsList = new ArrayList<>();
        notificationChannelDetailsList.add(notificationChannelDetails);
        Mockito.doReturn(false).when(vehicleService).isVehicleExist(VEHICLE_ID);
        Mockito.doReturn(notificationChannelDetailsList).when(notificationHistoryService)
            .getNotificationHistoryNonRegVehicleId(
                Mockito.any(),
                Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        ResponseEntity<String> responseEntity =
            notificationHistoryController.getNotificationHistoryVehicleId(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID,
                VEHICLE_ID, 1L, 1L, 1, 1, true);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get(0).get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME, jsonNode.get(0).get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get(0).get("group").asText());
        assertEquals(link, jsonNode.get(0).get("link").asText());
    }

    @Test
    public void getNotificationHistoryVehicleIdNotFound() throws Exception {
        Mockito.doReturn(false).when(vehicleService).isVehicleExist(VEHICLE_ID);
        VehicleIdNotFoundException thrown = assertThrows(VehicleIdNotFoundException.class, () -> {
            notificationHistoryController.getNotificationHistoryVehicleId(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                VEHICLE_ID, 1L, 1L, 1,
                1, false);
        });
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getMessage(), thrown.getMessage());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getReason(), thrown.getReason());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getCode(), thrown.getCode());
    }

    @Test
    public void getNotificationHistoryForCampaignId() throws JsonProcessingException, javassist.NotFoundException {

        Mockito.doReturn(getCampaignChannelSummary()).when(notificationHistoryService).getCampaignHistory(
            Mockito.any(),
            Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
        assertNotNull(notificationHistoryController.getNotificationHistoryForCampaignId(
                CLIENT_REQUEST_ID, SESSION_ID, REQUEST_ID,
            "safdasfsafsa", STATUS, 1, 1));


    }

    private CampaignSummary getCampaignChannelSummary() {
        CampaignSummary ccs = new CampaignSummary();
        ccs.setTotalPages(100L);
        ccs.setTotalRequests(100L);
        ccs.setFailedRequests(10L);
        ccs.setSuccessfulRequests(90L);
        ccs.setCurrentPageNumber(1L);



        CampaignDetail campaignChannelDetails = new CampaignDetail();
        campaignChannelDetails.setUserId(USER_ID);
        campaignChannelDetails.setVehicleId(VEHICLE_ID);

        ChannelResponseData channelResponse = new ChannelResponseData();
        channelResponse.setDestination(TEST_EMAIL);
        channelResponse.setStatus(SUCCESS);
        channelResponse.setChannelType(EMAIL);
        List<ChannelResponseData> channelResponseDataList = new ArrayList<>();
        channelResponseDataList.add(channelResponse);
        campaignChannelDetails.setChannelResponses(channelResponseDataList);
        List<CampaignDetail> campaignChannelDetailsList = new ArrayList<>();
        campaignChannelDetailsList.add(campaignChannelDetails);
        ccs.setCampaignChannelDetails(campaignChannelDetailsList);
        return ccs;
    }

    private NotificationChannelDetails getNotificationChannelDetails() {
        NotificationChannelDetails notificationChannelDetails = new NotificationChannelDetails();
        notificationChannelDetails.setId(CHANNEL_DETAILS_ID);
        notificationChannelDetails.setNotificationName(CHANNEL_DETAILS_NOTIFICATION_NAME);
        notificationChannelDetails.setGroup(CHANNEL_DETAILS_GROUP);
        notificationChannelDetails.setNotificationDate(CHANNEL_DETAILS_DATE);

        ChannelResponseData channelResponse = new ChannelResponseData();
        channelResponse.setDestination(TEST_EMAIL);
        channelResponse.setStatus(SUCCESS);
        channelResponse.setChannelType(EMAIL);
        List<ChannelResponseData> channelResponseDataList = new ArrayList<>();
        channelResponseDataList.add(channelResponse);
        notificationChannelDetails.setChannelResponses(channelResponseDataList);
        return notificationChannelDetails;
    }
}
