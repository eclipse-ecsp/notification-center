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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelResponseData;
import org.eclipse.ecsp.domain.notification.utils.UserService;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;
import org.eclipse.ecsp.platform.notification.service.NotificationHistoryService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;

/**
 * NotificationHistoryControllerParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class NotificationHistoryControllerParameterizedTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

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
    public static final String EMAIL = "email";
    public static final String TEST_EMAIL = "test@gmail.com";
    public static final String SUCCESS = "success";
    private static final String USER_ID = "USER_ID";


    @Mock
    UserService userService;

    @InjectMocks
    private NotificationHistoryController notificationHistoryController;

    @Mock
    NotificationHistoryService notificationHistoryService;

    @Mock
    VehicleService vehicleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * NotificationHistoryControllerParameterizedTest constructor.
     *
     * @param isVehicleExists boolean
     * @param isNonRegVeh boolean
     */
    public NotificationHistoryControllerParameterizedTest(boolean isVehicleExists, boolean isNonRegVeh) {
        this.isVehicleExists = isVehicleExists;
        this.isNonRegVeh = isNonRegVeh;
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {true, false},
                {true, false},
                {false, true},

        });

    }

    private final boolean isVehicleExists;
    private final boolean isNonRegVeh;

    @Test
    public void getNotificationStatus() throws Exception {
        Mockito.doReturn(isVehicleExists).when(vehicleService).isVehicleExist(VEHICLE_ID);
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        Mockito.doReturn(notificationChannelDetails).when(notificationHistoryService)
                .getNotificationStatus(Mockito.any(), Mockito.any(),
                        Mockito.any());
        ResponseEntity<String> responseEntity =
                notificationHistoryController.getNotificationStatusByVehicleIdAndPlatformId(REQUEST_ID,
                        SESSION_ID, CLIENT_REQUEST_ID, VEHICLE_ID, PLATFORM_RESPONSE_ID, STATUS, isNonRegVeh);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME,
                jsonNode.get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get("group").asText());
    }

    @Test
    public void getNotificationHistoryUserIdVehicleId() throws Exception {
        NotificationChannelDetails notificationChannelDetails = getNotificationChannelDetails();
        String link = "/v1/users/" + USER_ID + "/vehicles/" + VEHICLE_ID + "/notifications/"
                + notificationChannelDetails.getId() + "?content=full";
        notificationChannelDetails.setLink(link);
        List<NotificationChannelDetails> notificationChannelDetailsList = new ArrayList<>();
        notificationChannelDetailsList.add(notificationChannelDetails);
        Mockito.doReturn(new UserProfile()).when(userService).getUser(USER_ID);
        Mockito.doReturn(isVehicleExists).when(vehicleService).isVehicleExist(VEHICLE_ID);
        Mockito.doReturn(notificationChannelDetailsList).when(notificationHistoryService)
                .getNotificationHistoryUserIdVehicleId(Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.anyInt(), Mockito.anyInt());
        ResponseEntity<String> responseEntity =
                notificationHistoryController.getNotificationHistoryUserIdVehicleId(REQUEST_ID, SESSION_ID,
                        CLIENT_REQUEST_ID,
                        USER_ID, VEHICLE_ID, 1L, 1L, 1, 1, isNonRegVeh);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(CHANNEL_DETAILS_ID, jsonNode.get(0).get("id").asText());
        assertEquals(CHANNEL_DETAILS_NOTIFICATION_NAME, jsonNode.get(0).get("notificationName").asText());
        assertEquals(CHANNEL_DETAILS_GROUP, jsonNode.get(0).get("group").asText());
        assertEquals(link, jsonNode.get(0).get("link").asText());
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
