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

package org.eclipse.ecsp.notification.fcm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.FCMChannelResponse;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.PushTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NON_REGISTERED_USER_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_CLIENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_TOKEN;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM_ANDROID;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM_IOS;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM_WEB;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * FCMNotifierTest.
 */
public class FcmNotifierTest {

    ObjectMapper objectMapper = new ObjectMapper();

    FcmNotifier fcmNotifier = new FcmNotifier();

    @InjectMocks
    FcmNotifier fcmNotifierSpy = Mockito.spy(fcmNotifier);

    @InjectMocks
    FcmNotifier fcmNotifier1;

    @Mock
    NotificationDao notificationDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void doPublishAndroidSuccess() throws Exception {


        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(APP_PLATFORM, APP_PLATFORM_ANDROID);
        deviceMap.put(ID,
                "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_"
                        + "EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h");

        doReturn(getNewFcmResponse(true)).when(
                fcmNotifierSpy).publishFcmNotification(any(), any());
        when(notificationDao.getFieldsValueByFields(any(), any(), any())).thenReturn(
                Collections.singletonList(deviceMap));
        AlertsInfo alertsInfo = getAlertsInfo(false);
        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }


    @Test
    public void doPublishAndroidSuccess2() throws Exception {


        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(APP_PLATFORM, APP_PLATFORM_ANDROID);
        deviceMap.put(ID,
                "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_"
                        + "EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h");

        doReturn(getNewFcmResponse(false)).when(fcmNotifierSpy).publishFcmNotification(any(), any());
        when(notificationDao.getFieldsValueByFields(any(), any(), any())).thenReturn(
                Collections.singletonList(deviceMap));
        AlertsInfo alertsInfo = getAlertsInfo(false);
        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void doPublishWithoutUser() throws Exception {

        AlertsInfo alertsInfo = getAlertsInfo(false);
        alertsInfo.getAlertsData().setUserProfile(null);

        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void doPublishAndroidFailure() throws Exception {


        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(APP_PLATFORM, APP_PLATFORM_ANDROID);
        deviceMap.put(ID,
                "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_"
                        + "EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h");

        doReturn(getNewFcmResponse(false)).when(fcmNotifierSpy).publishFcmNotification(any(), any());
        when(notificationDao.getFieldsValueByFields(any(), any(), any())).thenReturn(
                Collections.singletonList(deviceMap));
        doNothing().when(notificationDao).deleteSingleDocument(any(), any());
        AlertsInfo alertsInfo = getAlertsInfo(false);
        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void doPublishWebSuccess() throws Exception {


        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(APP_PLATFORM, APP_PLATFORM_WEB);
        deviceMap.put(ID,
                "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_"
                        + "EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h");

        doReturn(getNewFcmResponse(true)).when(fcmNotifierSpy).publishFcmNotification(any(), any());
        when(notificationDao.getFieldsValueByFields(any(), any(), any())).thenReturn(
                Collections.singletonList(deviceMap));
        AlertsInfo alertsInfo = getAlertsInfo(false);
        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }


    @Test
    public void doPublishIosSuccess() throws Exception {


        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(APP_PLATFORM, APP_PLATFORM_IOS);
        deviceMap.put(ID,
                "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_"
                        + "EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h");

        doReturn(getNewFcmResponse(true)).when(fcmNotifierSpy).publishFcmNotification(any(), any());
        when(notificationDao.getFieldsValueByFields(any(), any(), any())).thenReturn(
                Collections.singletonList(deviceMap));
        AlertsInfo alertsInfo = getAlertsInfo(false);
        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void doPublishDefaultSuccess() throws Exception {


        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put(APP_PLATFORM, "default");
        deviceMap.put(ID,
                "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_"
                        + "EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h");

        doReturn(getNewFcmResponse(true)).when(fcmNotifierSpy).publishFcmNotification(any(), any());
        when(notificationDao.getFieldsValueByFields(any(), any(), any())).thenReturn(
                Collections.singletonList(deviceMap));
        AlertsInfo alertsInfo = getAlertsInfo(false);

        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void doPublishAndroidNonRegisterUserSuccess() throws Exception {

        AlertsInfo alertsInfo = getAlertsInfo(true);
        Map<String, Object> props = new HashMap<>();
        props.put(NON_REGISTERED_FIELD_PUSH_CLIENT, APP_PLATFORM_ANDROID);
        props.put(NON_REGISTERED_FIELD_PUSH_TOKEN,
                "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_EgKJ"
                        + "3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h");
        alertsInfo.getAlertsData().setAlertDataProperties(props);

        doReturn(getNewFcmResponse(true)).when(fcmNotifierSpy).publishFcmNotification(any(), any());

        ChannelResponse response = fcmNotifierSpy.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void doPublishNoIds() throws Exception {

        AlertsInfo alertsInfo = getAlertsInfo(false);

        doReturn(getNewFcmResponse(true)).when(fcmNotifierSpy).publishFcmNotification(any(), any());
        when(notificationDao.getFieldsValueByFields(any(), any(), any())).thenReturn(null);

        assertThrows(Exception.class, () -> fcmNotifierSpy.doPublish(alertsInfo));
    }

    @Test
    public void setupChannelEnabled() throws Exception {

        NotificationConfig notificationConfig = getNotificationConfig();

        doNothing().when(notificationDao).deleteSingleDocument(any(), any());
        doNothing().when(notificationDao).insertSingleDocument(any(), any());

        FCMChannelResponse response = (FCMChannelResponse) fcmNotifierSpy.setupChannel(notificationConfig);
        assertEquals(
                "setup Channel for FCM push successful for device edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18"
                        + "j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTOTH0DuC53SLI_EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7j"
                        + "xG7IhhyTqusulNmwnTArt14h and user testUser",
                response.getFcmResponse());
    }

    @Test
    public void setupChannelDisabled() throws Exception {

        NotificationConfig notificationConfig = getNotificationConfig();
        notificationConfig.getChannel(ChannelType.MOBILE_APP_PUSH).setEnabled(false);

        doNothing().when(notificationDao).deleteSingleDocument(any(), any());
        doNothing().when(notificationDao).insertSingleDocument(any(), any());

        FCMChannelResponse response = (FCMChannelResponse) fcmNotifierSpy.setupChannel(notificationConfig);
        assertEquals(
                "Push channel not enabled for "
                        + "edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD1"
                        + "4EmdkNTOTH0DuC53SLI_EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h "
                        + "and user testUser",
                response.getFcmResponse());
    }

    @Test
    public void destroyChannelSuccess() {

        doNothing().when(notificationDao).deleteSingleDocument(any(), any());

        FCMChannelResponse response =
                (FCMChannelResponse) fcmNotifierSpy.destroyChannel("testUser", "{\"Data\": {\"token\":\"tokenId\"}}");
        assertEquals("Destroying Channel for FCM push successful for device tokenId and user testUser",
                response.getFcmResponse());
    }

    @Test
    public void destroyChannelFailure() {

        doThrow(new RuntimeException("error")).when(notificationDao).deleteSingleDocument(any(), any());

        FCMChannelResponse response =
                (FCMChannelResponse) fcmNotifierSpy.destroyChannel("testUser", "{\"Data\": {\"token\":\"tokenId\"}}");
        assertNull(response.getFcmResponse());
    }

    @NotNull
    private AlertsInfo getAlertsInfo(boolean isNonRegisterUser) throws IOException {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(isNonRegisterUser ? NON_REGISTERED_USER_NOTIFICATION_EVENT : GENERIC_NOTIFICATION_EVENT);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setUserProfile(getUserProfile());

        alertsInfo.setAlertsData(data);
        alertsInfo.addNotificationTemplate("en-US", getNotificationTemplate());
        alertsInfo.setNotificationConfig(getNotificationConfig());
        alertsInfo.setNotificationConfigs(Collections.singletonList(getNotificationConfig()));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        igniteEvent.setMessageId("12345");
        igniteEvent.setVehicleId("HUXOIDDN2HUN11");
        alertsInfo.setIgniteEvent(igniteEvent);

        return alertsInfo;
    }

    private NotificationConfig getNotificationConfig() throws IOException {
        NotificationConfig notificationConfig = objectMapper.readValue(
                "{\n"
                        + "  \"userId\" : \"testUser\",\n"
                        + "  \"vehicleId\" : \"HUXOIDDN2HUN11\",\n"
                        + "  \"contactId\" : \"self\",\n"
                        + "  \"group\" : \"push\",\n"
                        + "  \"enabled\" : true,\n"
                        + "  \"channels\" : [\n"
                        + "    {\n"
                        + "      \"emails\" : [\n"
                        + "        \"shai.tanchuma@harman.com\"\n"
                        + "      ],\n"
                        + "      \"type\" : \"email\",\n"
                        + "      \"enabled\" : true\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"phones\" : [\n"
                        + "        \" +972528542238\"\n"
                        + "      ],\n"
                        + "      \"type\" : \"sms\",\n"
                        + "      \"enabled\" : true\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"service\" : \"pushservice\",\n"
                        + "      \"appPlatform\" : \"ANDROID\",\n"
                        + "      \"deviceTokens\" : [\n"
                        + "        \"edzhfG4utqc:"
                        + "APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkNTO"
                        + "TH0DuC53SLI_EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h\"\n"
                        + "      ],\n"
                        + "      \"type\" : \"push\",\n"
                        + "      \"enabled\" : true\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}", NotificationConfig.class);
        notificationConfig.setLocale("en-US");
        return notificationConfig;
    }

    private UserProfile getUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("testUser");
        userProfile.setFirstName("test");
        userProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        userProfile.setDefaultEmail("testUser@harman.com");
        return userProfile;
    }

    private NotificationTemplate getNotificationTemplate() {
        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setBody("push body");
        pushTemplate.setTitle("push title");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setPush(pushTemplate);
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setChannelTemplates(channelTemplates);
        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));
        notificationTemplate.setNotificationId("LOW_FUEL");
        notificationTemplate.setBrand("default");
        return notificationTemplate;
    }

    private String getNewFcmResponse(boolean success) {
        return success ? "{  "
                + "\"name\": \"projects/ivehicle-ccfc8/messages/0:1725350792074371%04d88afcf9fd7ecd\"}" : "{\r\n"
                + "    \"error\": {\r\n"
                + "        \"code\": 400,\r\n"
                + "        \"message\": \"The registration token is not a valid FCM registration token\",\r\n"
                + "        \"status\": \"INVALID_ARGUMENT\",\r\n"
                + "        \"details\": [\r\n"
                + "            {\r\n"
                + "                \"@type\": \"type.googleapis.com/google.firebase.fcm.v1.FcmError\",\r\n"
                + "                \"errorCode\": \"INVALID_ARGUMENT\"\r\n"
                + "            }\r\n"
                + "        ]\r\n"
                + "    }\r\n"
                + "}";
    }

    private String getFcmResponse(boolean success) {
        return success
                ? "{\n"
                + "    \"multicast_id\": 1796314303484991886,\n"
                + "    \"success\": 1,\n"
                + "    \"failure\": 0,\n"
                + "    \"canonical_ids\": 0,\n"
                + "    \"results\": [\n"
                + "        {\n"
                + "            \"message_id\": \"0:1611752964184468%93b410e5f9fd7ecd\"\n"
                + "        }\n"
                + "    ]\n"
                + "}"
                :
                "{\n"
                        + "    \"multicast_id\": 6426863259008167738,\n"
                        + "    \"success\": 0,\n"
                        + "    \"failure\": 1,\n"
                        + "    \"canonical_ids\": 0,\n"
                        + "    \"results\": [\n"
                        + "        {\n"
                        + "            \"error\": \"NotRegistered\"\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}";

    }

}