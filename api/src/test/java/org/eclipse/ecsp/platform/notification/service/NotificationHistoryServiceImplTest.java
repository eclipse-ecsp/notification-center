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

package org.eclipse.ecsp.platform.notification.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AmazonSESResponse;
import org.eclipse.ecsp.domain.notification.NotificationErrorCode;
import org.eclipse.ecsp.domain.notification.commons.ChannelResponseData;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV3;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * NotificationHistoryServiceImplTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationHistoryServiceImplTest {

    private static final String STATUS = "status";
    private static final String FULL = "full";
    private static final String PLATFORM_RESPONSE_ID = "fffcf3a1-f2a3-11ea-8d97-4765ef04ab7e";
    private static final String PLATFORM_RESPONSE_ID2 = "fffcf3a1-f2a3-11ea-8d97-4765ef04ab7f";
    private static final String USER_ID = "userId";
    private static final String VEHICLE_ID = "HU4VHCL000000";
    private static final String MESSAGE_ID = "01010174732b2b58-9d964567-73b3-44e8-88f7-835d99efc246-000000";

    private ObjectMapper objectMapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @InjectMocks
    private NotificationHistoryServiceImpl notificationHistoryServiceImpl;

    @Mock
    private AlertsHistoryDao alertsHistoryDao;

    @Mock
    private AlertsServiceV3 alertService;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void getNotificationStatusNotFoundNullInfoTest() {

        when(alertsHistoryDao.findById(Mockito.any())).thenReturn(null);

        NotFoundException thrown =
            assertThrows(NotFoundException.class,
                () -> notificationHistoryServiceImpl.getNotificationStatus(PLATFORM_RESPONSE_ID, STATUS, null),
                "Expected to throw, but it didn't");
        assertEquals("Alert history information not found", thrown.getMessage());
    }

    @Test
    public void getNotificationStatusNoDetailsSuccessStatus() throws Exception {

        AlertsHistoryInfo alertsHistoryInfo = getDefaultAlertHistoryInfo("Success");
        alertsHistoryInfo.setNotificationLongName("Geofence Notification");
        alertsHistoryInfo.setNotificationId("Geofence_Notification_id");
        alertsHistoryInfo.setId(PLATFORM_RESPONSE_ID);
        when(alertsHistoryDao.findById(Mockito.any())).thenReturn(alertsHistoryInfo);

        NotificationChannelDetails notificationChannelDetails =
            notificationHistoryServiceImpl.getNotificationStatus(PLATFORM_RESPONSE_ID,
                STATUS, null);

        assertEquals(PLATFORM_RESPONSE_ID, notificationChannelDetails.getId());
        assertEquals(1, notificationChannelDetails.getChannelResponses().size());
        ChannelResponseData emailData = notificationChannelDetails.getChannelResponses().stream()
            .filter(data -> data.getChannelType().equals("email")).findFirst().orElse(null);
        assertNotNull(emailData);
        assertEquals("Success", emailData.getStatus());
        assertEquals("Geofence_Notification_id", notificationChannelDetails.getNotificationId());
    }

    @Test
    public void getNotificationStatusNoDetailsFailureStatus() throws Exception {

        AlertsHistoryInfo alertsHistoryInfo = getDefaultAlertHistoryInfo("Failure");
        alertsHistoryInfo.setId(PLATFORM_RESPONSE_ID);
        alertsHistoryInfo.getChannelResponses().get(0).setErrorCode(NotificationErrorCode.DB_ERROR);
        when(alertsHistoryDao.findById(Mockito.any())).thenReturn(alertsHistoryInfo);

        NotificationChannelDetails notificationChannelDetails =
            notificationHistoryServiceImpl.getNotificationStatus(PLATFORM_RESPONSE_ID,
                STATUS, null);

        assertEquals(PLATFORM_RESPONSE_ID, notificationChannelDetails.getId());
        assertEquals(1, notificationChannelDetails.getChannelResponses().size());
        ChannelResponseData emailData = notificationChannelDetails.getChannelResponses().stream()
            .filter(data -> data.getChannelType().equals("email")).findFirst().orElse(null);
        assertNotNull(emailData);
        assertEquals("Failure", emailData.getStatus());
        assertEquals(NotificationErrorCode.DB_ERROR, emailData.getErrorCode());
    }

    @Test
    public void getNotificationStatusDetailsSuccessStatus() throws Exception {

        AlertsHistoryInfo alertsHistoryInfo = getDefaultAlertHistoryInfo("Success");
        alertsHistoryInfo.setId(PLATFORM_RESPONSE_ID);
        when(alertsHistoryDao.findById(Mockito.any())).thenReturn(alertsHistoryInfo);

        NotificationChannelDetails notificationChannelDetails =
            notificationHistoryServiceImpl.getNotificationStatus(PLATFORM_RESPONSE_ID,
                FULL, null);

        assertEquals(PLATFORM_RESPONSE_ID, notificationChannelDetails.getId());
        assertEquals(1, notificationChannelDetails.getChannelResponses().size());
        ChannelResponseData emailData = notificationChannelDetails.getChannelResponses().stream()
            .filter(data -> data.getChannelType().equals("email")).findFirst().orElse(null);
        assertNotNull(emailData);
        assertEquals("Success", emailData.getStatus());
        assertEquals("body", emailData.getBody());
        assertEquals("title", emailData.getTitle());
    }

    @Test
    public void getNotificationStatusDetailsSuccessStatusWithVehicleId() throws Exception {

        AlertsHistoryInfo alertsHistoryInfo = getDefaultAlertHistoryInfo("Success");
        alertsHistoryInfo.setId(PLATFORM_RESPONSE_ID);
        when(alertsHistoryDao.findByVehicleIdAndPlatformResponseId(Mockito.any(), Mockito.any())).thenReturn(
            alertsHistoryInfo);

        NotificationChannelDetails notificationChannelDetails =
            notificationHistoryServiceImpl.getNotificationStatus(PLATFORM_RESPONSE_ID,
                FULL, VEHICLE_ID);

        assertEquals(PLATFORM_RESPONSE_ID, notificationChannelDetails.getId());
        assertEquals(1, notificationChannelDetails.getChannelResponses().size());
        ChannelResponseData emailData = notificationChannelDetails.getChannelResponses().stream()
            .filter(data -> data.getChannelType().equals("email")).findFirst().orElse(null);
        assertNotNull(emailData);
        assertEquals("Success", emailData.getStatus());
        assertEquals("body", emailData.getBody());
        assertEquals("title", emailData.getTitle());
    }

    @Test
    public void getNotificationStatusDetailsFailureStatus() throws Exception {

        AlertsHistoryInfo alertsHistoryInfo = getDefaultAlertHistoryInfo("Failure");
        alertsHistoryInfo.setId(PLATFORM_RESPONSE_ID);
        alertsHistoryInfo.getChannelResponses().get(0).setErrorCode(NotificationErrorCode.DB_ERROR);
        when(alertsHistoryDao.findById(Mockito.any())).thenReturn(alertsHistoryInfo);

        NotificationChannelDetails notificationChannelDetails =
            notificationHistoryServiceImpl.getNotificationStatus(PLATFORM_RESPONSE_ID,
                FULL, null);

        assertEquals(PLATFORM_RESPONSE_ID, notificationChannelDetails.getId());
        assertEquals(1, notificationChannelDetails.getChannelResponses().size());
        ChannelResponseData emailData = notificationChannelDetails.getChannelResponses().stream()
            .filter(data -> data.getChannelType().equals("email")).findFirst().orElse(null);
        assertNotNull(emailData);
        assertEquals("Failure", emailData.getStatus());
        assertEquals(NotificationErrorCode.DB_ERROR, emailData.getErrorCode());
    }


    @Test
    public void getNotificationHistoryUserIdVehicleId() throws IOException {

        List<AlertsHistoryInfo> alertsHistory = getAlertsHistory();

        when(alertsHistoryDao.findByuserIdvehicleIdTimestampBetween(Mockito.any(), Mockito.any(), Mockito.anyLong(),
            Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(alertsHistory);

        List<NotificationChannelDetails> result =
            notificationHistoryServiceImpl.getNotificationHistoryUserIdVehicleId(USER_ID, VEHICLE_ID, 0, 344324234, 20,
                1);
        assertEquals(2, result.size());
        assertTrue(
            PLATFORM_RESPONSE_ID.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID.equals(result.get(1).getId()));
        assertTrue(
            PLATFORM_RESPONSE_ID2.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID2.equals(result.get(1).getId()));
    }

    @Test
    public void getNotificationHistoryUserId() throws IOException {

        List<AlertsHistoryInfo> alertsHistory = getAlertsHistory();

        when(alertsHistoryDao.findByUserIdTimestampBetween(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyInt(), Mockito.anyInt())).thenReturn(alertsHistory);

        List<NotificationChannelDetails> result =
            notificationHistoryServiceImpl.getNotificationHistoryUserId(USER_ID, 0, 344324234, 20, 1);
        assertEquals(2, result.size());
        assertTrue(
            PLATFORM_RESPONSE_ID.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID.equals(result.get(1).getId()));
        assertTrue(
            PLATFORM_RESPONSE_ID2.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID2.equals(result.get(1).getId()));
    }

    @Test
    public void getNotificationHistoryVehicleId() throws Exception {

        List<AlertsHistoryInfo> alertsHistory = getAlertsHistory();

        when(alertService.getUserIdFromDevice(Mockito.any())).thenReturn(USER_ID);
        when(alertsHistoryDao.findByVehicleIdTimestampBetween(Mockito.any(), Mockito.any(), Mockito.anyLong(),
            Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(alertsHistory);

        List<NotificationChannelDetails> result =
            notificationHistoryServiceImpl.getNotificationHistoryVehicleId(USER_ID, 0, 344324234, 20, 1);
        assertEquals(2, result.size());
        assertTrue(
            PLATFORM_RESPONSE_ID.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID.equals(result.get(1).getId()));
        assertTrue(
            PLATFORM_RESPONSE_ID2.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID2.equals(result.get(1).getId()));
    }

    @Test
    public void getNotificationHistoryNonRegVehicleId() throws Exception {

        List<AlertsHistoryInfo> alertsHistory = getAlertsHistory();

        when(alertsHistoryDao.findByOnlyVehicleIdTimestampBetween(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyInt(),
            Mockito.anyInt())).thenReturn(alertsHistory);

        List<NotificationChannelDetails> result =
            notificationHistoryServiceImpl.getNotificationHistoryNonRegVehicleId(VEHICLE_ID, 0,
                344324234, 20,
                1);
        assertEquals(2, result.size());
        assertTrue(
            PLATFORM_RESPONSE_ID.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID.equals(result.get(1).getId()));
        assertTrue(
            PLATFORM_RESPONSE_ID2.equals(result.get(0).getId()) || PLATFORM_RESPONSE_ID2.equals(result.get(1).getId()));
    }

    @Test
    public void getNotificationHistoryCampaignIdFailed() throws Exception {
        when(alertsHistoryDao.findCountByCampaignId(Mockito.any())).thenReturn(new Long(100));
        when(alertsHistoryDao.findCountOfSuccessfulRequests(Mockito.any())).thenReturn(new Long(90));
        when(alertsHistoryDao.findByCampaignIdPageAndSizeFailed(Mockito.any(), Mockito.anyInt(),
            Mockito.anyInt())).thenReturn(getAlertsHistoryFailed());
        assertNotNull(notificationHistoryServiceImpl.getCampaignHistory("dsadasddsa", "FAILURE", 1, 1));
    }

    @Test
    public void getNotificationHistoryCampaignIdZero() throws Exception {
        when(alertsHistoryDao.findCountByCampaignId(Mockito.any())).thenReturn(new Long(0));
        assertNotNull(alertsHistoryDao);
        notificationHistoryServiceImpl.getCampaignHistory("dsadasddsa", "FAILURE", 1, 1);
    }

    @Test(expected = Exception.class)
    public void getNotificationHistoryCampaignIdNull() throws Exception {
        when(alertsHistoryDao.findCountByCampaignId(Mockito.any())).thenReturn(new Long(100));
        when(alertsHistoryDao.findCountOfSuccessfulRequests(Mockito.any())).thenReturn(new Long(90));
        when(alertsHistoryDao.findByCampaignIdPageAndSizeFailed(Mockito.any(), Mockito.anyInt(),
            Mockito.anyInt())).thenReturn(new ArrayList<AlertsHistoryInfo>());
        assertNotNull(notificationHistoryServiceImpl.getCampaignHistory("dsadasddsa", "FAILURE", 1, 1));
    }

    @Test
    public void getNotificationHistoryCampaignIdSuccess() throws Exception {
        when(alertsHistoryDao.findCountByCampaignId(Mockito.any())).thenReturn(new Long(100));
        when(alertsHistoryDao.findCountOfSuccessfulRequests(Mockito.any())).thenReturn(new Long(90));
        when(alertsHistoryDao.findByCampaignIdPageAndSizeSuccess(Mockito.any(), Mockito.anyInt(),
            Mockito.anyInt())).thenReturn(getAlertsHistoryFailed());
        assertNotNull(notificationHistoryServiceImpl.getCampaignHistory("dsadasddsa", "Success", 1, 1));
    }

    private AlertsHistoryInfo getDefaultAlertHistoryInfo(String status) throws IOException {

        AmazonSESResponse amazonsesresponse = new AmazonSESResponse();
        amazonsesresponse.setMessageId(MESSAGE_ID);
        amazonsesresponse.setStatus(status);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setFrom("from");
        emailTemplate.setSubject("subject");
        emailTemplate.setTitle("title");
        emailTemplate.setBody("body");
        amazonsesresponse.setTemplate(emailTemplate);

        String alertHistoryData =
            IOUtils.toString(NotificationHistoryServiceImpl.class.getResourceAsStream("/alertHistory_data.json"),
                StandardCharsets.UTF_8);
        AlertsHistoryInfo alertsHistoryInfo = objectMapper.readValue(alertHistoryData, AlertsHistoryInfo.class);
        alertsHistoryInfo.addChannelResponse(amazonsesresponse);
        return alertsHistoryInfo;
    }

    private List<AlertsHistoryInfo> getAlertsHistoryFailed() throws IOException {
        AlertsHistoryInfo alertsHistoryInfo1 = getDefaultAlertHistoryInfo("Failure");
        alertsHistoryInfo1.setId(PLATFORM_RESPONSE_ID);
        alertsHistoryInfo1.setTimestamp(1528977276688L);
        alertsHistoryInfo1.getChannelResponses().get(0).setErrorCode(NotificationErrorCode.DB_ERROR);
        return Arrays.asList(alertsHistoryInfo1);
    }

    private List<AlertsHistoryInfo> getAlertsHistorySuccess() throws IOException {
        AlertsHistoryInfo alertsHistoryInfo1 = getDefaultAlertHistoryInfo("Success");
        alertsHistoryInfo1.setTimestamp(1528977276688L);
        alertsHistoryInfo1.setId(PLATFORM_RESPONSE_ID);

        return Arrays.asList(alertsHistoryInfo1);
    }

    private List<AlertsHistoryInfo> getAlertsHistory() throws IOException {
        AlertsHistoryInfo alertsHistoryInfo1 = getDefaultAlertHistoryInfo("Failure");
        alertsHistoryInfo1.setId(PLATFORM_RESPONSE_ID);
        alertsHistoryInfo1.getChannelResponses().get(0).setErrorCode(NotificationErrorCode.DB_ERROR);

        AlertsHistoryInfo alertsHistoryInfo2 = getDefaultAlertHistoryInfo("Success");
        alertsHistoryInfo2.setId(PLATFORM_RESPONSE_ID2);

        return Arrays.asList(alertsHistoryInfo1, alertsHistoryInfo2);
    }
}