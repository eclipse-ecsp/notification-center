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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.Campaign;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.NonRegisteredUserData;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.domain.notification.NotificationNonRegisteredUser;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.StatusHistoryRecord;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.notification.dao.CampaignDAO;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.NotificationChannelDetails;
import org.eclipse.ecsp.platform.notification.exceptions.ErrorSendingEventException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNotificationIdException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.exceptions.ScheduledNotificationDeletionException;
import org.eclipse.ecsp.platform.notification.service.NotificationHistoryServiceImpl;
import org.eclipse.ecsp.platform.notification.v1.fw.web.NotificationGroupingNotFoundException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.DONE;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.READY;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.SCHEDULED;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.SCHEDULE_REQUESTED;
import static org.eclipse.ecsp.platform.notification.utils.UnitTestUtils.compareException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * NotificationServiceTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationServiceTest {

    private static final String VEHICLE_ID = "HU4TEST112233";
    private static final String NOTIFICATION_ID = "low_fuel";
    private static final String PLATFORM_RESPONSE_ID = "98ba6991-032c-11eb-a74d-6f1d4556d17c";
    private static final String USER_ID = "testUser001";
    private static final String TOPIC = "topic";
    private static final String SCHEDULE_TOPIC = "scheduleTopic";

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private DynamicNotificationTemplateDAO dynamicnotificationtemplatedao;

    @Mock
    private KafkaService kafkaService;

    @Mock
    NotificationGroupingDAO notificationGroupingDao;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private UserProfileDAO userProfileDao;

    @Mock
    private CampaignDAO campaignDao;

    @Mock
    private NotificationHistoryServiceImpl notificationHistoryService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * set up method before test.
     */
    @BeforeEach
    public void setup() {

        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createNotificationNotificationGroupingNotFoundException() {

        notificationService.setEnableEntitlementValidation(true);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setVehicleId(VEHICLE_ID);
        notificationCreationRequest.setNotificationId(NOTIFICATION_ID);

        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);

        NotificationGroupingNotFoundException thrown =
            assertThrows(NotificationGroupingNotFoundException.class,
                () -> notificationService.createNotification(notificationCreationRequest),
                "Expected to throw, but it didn't");

        assertEquals("Notification Grouping not found for " + NOTIFICATION_ID, thrown.getMessage());
    }

    @Test
    public void createNotificationVehicleMessagePublic() throws ExecutionException {

        notificationService.setEnableEntitlementValidation(true);

        NotificationCreationRequest notificationCreationRequest = getNotificationCreationRequest();

        notificationCreationRequest.setCampaignId("cid");

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertEquals(PLATFORM_RESPONSE_ID, response);
    }

    @Test
    public void createNotificationThrowInvalidTimeFormat() throws ExecutionException {

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setUserId(VEHICLE_ID);
        notificationCreationRequest.setNotificationId(NOTIFICATION_ID);
        notificationCreationRequest.setRequestId(PLATFORM_RESPONSE_ID);
        notificationCreationRequest.setSchedule("invalid-format");
        InvalidInputException expectedException = new InvalidInputException(
            Collections.singletonList(NotificationCenterError.INVALID_TIME_FORMAT.toMessage()));

        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        notificationCreationRequest.setData(data);

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
            () -> notificationService.createNotification(notificationCreationRequest));
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.INVALID_TIME_FORMAT);
    }


    @Test
    public void createNotificationDynamicNotification() throws ExecutionException {

        notificationService.setEnableEntitlementValidation(true);

        NotificationCreationRequest notificationCreationRequest = getNotificationCreationRequest();

        notificationCreationRequest.setDynamicNotification(true);

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertEquals(PLATFORM_RESPONSE_ID, response);
    }

    @Test
    public void createNotificationGenericEvent() throws ExecutionException {

        notificationService.setEnableEntitlementValidation(true);

        NotificationCreationRequest notificationCreationRequest = getNotificationCreationRequest();

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertEquals(PLATFORM_RESPONSE_ID, response);
    }

    @Test
    public void createNotificationGenericEventEntitlementValidationFalse() throws ExecutionException {

        notificationService.setEnableEntitlementValidation(false);

        NotificationCreationRequest notificationCreationRequest = getNotificationCreationRequest();

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertEquals(PLATFORM_RESPONSE_ID, response);
    }

    @Test
    public void createNotificationGenericEventEmptyDataMap() throws ExecutionException {

        notificationService.setEnableEntitlementValidation(false);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setVehicleId(VEHICLE_ID);
        notificationCreationRequest.setNotificationId(NOTIFICATION_ID);
        notificationCreationRequest.setRequestId(PLATFORM_RESPONSE_ID);

        Map<String, Object> data = new HashMap<>();
        notificationCreationRequest.setData(data);

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertEquals(PLATFORM_RESPONSE_ID, response);
    }

    @Test
    public void createNotificationGenericEventException() throws ExecutionException {

        notificationService.setEnableEntitlementValidation(true);

        NotificationCreationRequest notificationCreationRequest = getNotificationCreationRequest();

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doThrow(new RuntimeException("error")).when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertNull(response);
    }

    @Test
    public void createNotificationGenericEventEmptyVehicle() throws ExecutionException {

        notificationService.setEnableEntitlementValidation(true);

        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setUserId(VEHICLE_ID);
        notificationCreationRequest.setNotificationId(NOTIFICATION_ID);
        notificationCreationRequest.setRequestId(PLATFORM_RESPONSE_ID);

        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        notificationCreationRequest.setData(data);

        List<NotificationGrouping> notificationGroupingList = getNotificationGroupings();

        when(notificationGroupingDao.findByNotificationId(any())).thenReturn(notificationGroupingList);
        doNothing().when(vehicleService).validateServiceEnabled(any(), any());
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertEquals(PLATFORM_RESPONSE_ID, response);
    }

    @Test
    public void updateNickNameSuccess() {

        when(userProfileDao.updateNickName(any(), any(), any())).thenReturn(new UserProfile());

        boolean response = notificationService.updateNickName(USER_ID, VEHICLE_ID, "nickName");

        assertTrue(response);
    }

    @Test
    public void updateNickNameFailure() {

        doThrow(new RuntimeException("error")).when(userProfileDao).updateNickName(any(), any(), any());

        boolean response = notificationService.updateNickName(USER_ID, VEHICLE_ID, "nickName");

        assertFalse(response);
    }

    @Test
    public void updateConsentSuccess() {

        when(userProfileDao.updateConsent(any(), anyBoolean())).thenReturn(new UserProfile());

        boolean response = notificationService.updateConsent(USER_ID, true);

        assertTrue(response);
    }

    @Test
    public void updateConsentFailure() {

        doThrow(new RuntimeException("error")).when(userProfileDao).updateConsent(any(), anyBoolean());

        boolean response = notificationService.updateConsent(USER_ID, true);

        assertFalse(response);
    }

    @Test
    public void getNickNameByUserIdVehicleIdSuccess() {

        UserProfile userProfile = new UserProfile();
        userProfile.setNickNames(Collections.singleton(new NickName("nick1", VEHICLE_ID)));

        String response = notificationService.getNickNameByUserIdVehicleId(userProfile, USER_ID, VEHICLE_ID);

        assertEquals("nick1", response);
    }

    @Test
    public void getNickNameByUserIdVehicleIdFailure() {

        String response = notificationService.getNickNameByUserIdVehicleId(null, USER_ID, VEHICLE_ID);

        assertNull(response);
    }

    @Test
    public void saveCampaignSuccess() {

        when(campaignDao.save(any())).thenReturn(new Campaign());

        boolean response = notificationService.saveCampaign(new Campaign());

        assertTrue(response);
    }

    @Test
    public void saveCampaignFailure() {

        doThrow(new RuntimeException("error")).when(campaignDao).save(any());

        boolean response = notificationService.saveCampaign(new Campaign());

        assertFalse(response);
    }

    @Test
    public void getCampaignSuccess() {

        Campaign campaign = new Campaign();
        campaign.setId("cid");
        when(campaignDao.findById(any())).thenReturn(campaign);

        Campaign response = notificationService.getCampaign("cid");

        assertEquals("cid", response.getId());
    }

    @Test
    public void getCampaignFailure() {

        doThrow(new RuntimeException("error")).when(campaignDao).findById(any());

        Campaign response = notificationService.getCampaign("cid");

        assertNull(response);
    }

    @Test
    public void getUserProfile() {

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(USER_ID);

        when(userProfileDao.findById(any())).thenReturn(userProfile);

        UserProfile response = notificationService.getUserProfile(USER_ID);

        assertEquals(USER_ID, response.getUserId());
    }

    @Test
    public void nonRegisteredUserFailNullNotificationId() throws Exception {

        String requestData =
            IOUtils.toString(NotificationServiceTest.class.getResourceAsStream("/non-register-user-data.json"),
                "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        notificationNonRegisteredUser.setNotificationId(null);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidNotificationIdException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_NOTIFICATION_ID_EXCEPTION,
            NotificationCenterError.INPUT_MANDATORY_NOTIFICATION_ID);
    }

    @Test
    public void nonRegisteredUserFailEmptyNotificationId() throws Exception {

        String requestData =
            IOUtils.toString(NotificationServiceTest.class.getResourceAsStream("/non-register-user-data.json"),
                "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        notificationNonRegisteredUser.setNotificationId("");

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidNotificationIdException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_NOTIFICATION_ID_EXCEPTION,
            NotificationCenterError.INPUT_MANDATORY_NOTIFICATION_ID);
    }

    @Test
    public void nonRegisteredUserFailNotificationIdNotFound() throws Exception {

        String requestData =
            IOUtils.toString(NotificationServiceTest.class.getResourceAsStream("/non-register-user-data.json"),
                "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(false);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidNotificationIdException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_NOTIFICATION_ID_EXCEPTION,
            NotificationCenterError.NOTIFICATION_ID_DOES_NOT_EXIST);
    }

    @Test
    public void nonRegisteredUserFailNullRecipients() throws Exception {

        String requestData =
            IOUtils.toString(NotificationServiceTest.class.getResourceAsStream("/non-register-user-data.json"),
                "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        notificationNonRegisteredUser.setRecipients(null);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidInputException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.NON_REGISTERED_INPUT_MISSING_RECIPIENTS);
    }

    @Test
    public void nonRegisteredUserFailNoRecipients() throws Exception {

        String requestData =
            IOUtils.toString(NotificationServiceTest.class.getResourceAsStream("/non-register-user-data.json"),
                "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        notificationNonRegisteredUser.setRecipients(new ArrayList<>());

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidInputException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.NON_REGISTERED_INPUT_MISSING_RECIPIENTS);
    }


    @Test
    public void nonRegisteredUserFailMaxRecipientsExceeded() throws Exception {

        String requestData =
            IOUtils.toString(NotificationServiceTest.class.getResourceAsStream("/non-register-user-data.json"),
                "UTF-8");


        List<NonRegisteredUserData> recipients = new ArrayList<>();
        recipients.add(new NonRegisteredUserData());
        recipients.add(new NonRegisteredUserData());
        recipients.add(new NonRegisteredUserData());
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);
        notificationNonRegisteredUser.setRecipients(recipients);

        notificationService.setMaxNonRegisterUserNotificationsPerRequest(2);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidInputException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.NON_REGISTERED_INPUT_MAX_RECIPIENTS_EXCEEDED, "2", "3");
    }

    @Test
    public void nonRegisteredUserWarningInvalidLocale() throws Exception {

        String requestData = IOUtils.toString(
            NotificationServiceTest.class.getResourceAsStream("/non-register-user-invalid-locale-data.json"), "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);
        notificationService.setMaxNonRegisterUserNotificationsPerRequest(6);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidInputException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.NON_REGISTERED_INPUT_INVALID_LOCALE, "+9725233335", "xx_YY");
    }

    @Test
    public void nonRegisteredUserInvalidEmail() throws Exception {
        String requestData = IOUtils.toString(
            NotificationServiceTest.class.getResourceAsStream("/non-register-user-data-invalid-email.json"), "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);
        notificationService.setMaxNonRegisterUserNotificationsPerRequest(2);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidInputException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.NON_REGISTERED_INPUT_INVALID_EMAIL, "[shai.tanchumaharman.com]");
    }

    @Test
    public void nonRegisteredUserInvalidSms() throws Exception {
        String requestData = IOUtils.toString(
            NotificationServiceTest.class.getResourceAsStream("/non-register-user-data-invalid-sms.json"), "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);
        notificationService.setMaxNonRegisterUserNotificationsPerRequest(2);

        NotificationCenterExceptionBase thrown =
            assertThrows(InvalidInputException.class,
                () -> notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId",
                    "sId"),
                "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.NON_REGISTERED_INPUT_INVALID_SMS, "[+972523333]");
    }

    @Test
    public void nonRegisteredUserSuccess() throws Exception {

        String requestData =
            IOUtils.toString(NotificationServiceTest.class.getResourceAsStream("/non-register-user-data.json"),
                "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
            mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);
        notificationService.setMaxNonRegisterUserNotificationsPerRequest(2);
        assertNotNull(notificationNonRegisteredUser);
        notificationService.createNotificationForNonRegisteredUsers(notificationNonRegisteredUser, "rId", "sId");
    }

    @Test
    public void handleScheduledNotificationScheduleTopic() throws Exception {
        notificationService.setTopic(TOPIC);
        notificationService.setNotificationScheduleTopic(SCHEDULE_TOPIC);
        notificationService.setMaxScheduleDays(100);

        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        GenericEventData genericEventData = new GenericEventData();
        igniteEvent.setEventData(genericEventData);
        String schedule = LocalDateTime.now().plusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        notificationService.handleScheduledNotification(igniteEvent, schedule);

        assertEquals(schedule, genericEventData.getData(NotificationConstants.SCHEDULE_NOTIFICATION).get());
    }

    @Test
    public void handleScheduledNotificationInvalidPeriod() throws Exception {
        notificationService.setTopic(TOPIC);
        notificationService.setNotificationScheduleTopic(SCHEDULE_TOPIC);
        notificationService.setMaxScheduleDays(10);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        GenericEventData genericEventData = new GenericEventData();
        igniteEvent.setEventData(genericEventData);
        String schedule = LocalDateTime.now().plusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        assertThrows(InvalidInputException.class,
               () -> notificationService.handleScheduledNotification(igniteEvent, schedule));
    }

    @Test
    public void isValidDelayPeriodValid() throws Exception {
        notificationService.setMaxScheduleDays(5);
        String scheduleDate =
            Instant.now().atZone(ZoneId.of("UTC")).plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        assertTrue(notificationService.isValidDelayPeriod(scheduleDate));
    }

    @Test
    public void isValidDelayPeriodValidMinutes() throws Exception {
        notificationService.setMaxScheduleDays(5);
        String scheduleDate =
            Instant.now().atZone(ZoneId.of("UTC")).plusMinutes(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        assertTrue(notificationService.isValidDelayPeriod(scheduleDate));
    }

    @Test
    public void isValidDelayPeriodInvalidDays() throws Exception {
        notificationService.setMaxScheduleDays(5);
        String scheduleDate =
            Instant.now().atZone(ZoneId.of("UTC")).plusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        assertFalse(notificationService.isValidDelayPeriod(scheduleDate));
    }

    @Test
    public void isStatusAllowDeletionTrue() throws Exception {
        List<StatusHistoryRecord> statusHistoryRecords =
            createStatusHistoryRecords(Arrays.asList(SCHEDULE_REQUESTED, SCHEDULED));

        assertTrue(notificationService.isStatusAllowDeletion(statusHistoryRecords));
    }

    @Test
    public void isStatusAllowDeletionNullStatusList() throws Exception {
        assertFalse(notificationService.isStatusAllowDeletion(null));
    }


    @Test
    public void deleteScheduledNotificationThrowScheduledNotificationDeletionException() throws Exception {
        List<StatusHistoryRecord> statusHistoryRecordsAlreadySent =
            createStatusHistoryRecords(Arrays.asList(SCHEDULE_REQUESTED, SCHEDULED, READY, DONE));
        NotificationChannelDetails notificationChannelDetails = new NotificationChannelDetails();
        notificationChannelDetails.setStatusHistoryRecordList(statusHistoryRecordsAlreadySent);
        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setRequestId("123");

        when(notificationHistoryService.getNotificationStatus(any(), any(), any())).thenReturn(
            notificationChannelDetails);
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        assertThrows(ScheduledNotificationDeletionException.class,
                () -> notificationService.deleteScheduledNotification(igniteEventImpl));
    }

    @Test
    public void deleteScheduledNotificationThrowNotFoundException() throws Exception {
        List<StatusHistoryRecord> statusHistoryRecordsAlreadySent =
            createStatusHistoryRecords(Arrays.asList(SCHEDULE_REQUESTED, SCHEDULED, READY, DONE));
        NotificationChannelDetails notificationChannelDetails = new NotificationChannelDetails();
        notificationChannelDetails.setStatusHistoryRecordList(statusHistoryRecordsAlreadySent);
        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setRequestId("123");

        when(notificationHistoryService.getNotificationStatus(any(), any(), any())).thenThrow(
            new javassist.NotFoundException(""));
        assertThrows(NotFoundException.class,
                () -> notificationService.deleteScheduledNotification(igniteEventImpl));
    }

    @Test
    public void deleteScheduledNotificationThrowErrorSendingEventException() throws Exception {
        List<StatusHistoryRecord> statusHistoryRecordsAlreadySent =
            createStatusHistoryRecords(Arrays.asList(SCHEDULE_REQUESTED, SCHEDULED));
        NotificationChannelDetails notificationChannelDetails = new NotificationChannelDetails();
        notificationChannelDetails.setStatusHistoryRecordList(statusHistoryRecordsAlreadySent);
        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setRequestId("123");

        when(notificationHistoryService.getNotificationStatus(any(), any(), any())).thenReturn(
            notificationChannelDetails);
        doThrow(new RuntimeException()).when(kafkaService).sendIgniteEvent(any(), any(), any());

        assertThrows(ErrorSendingEventException.class,
               () -> notificationService.deleteScheduledNotification(igniteEventImpl));
    }

    @Test
    public void deleteScheduledNotificationSuccess() throws Exception {
        List<StatusHistoryRecord> statusHistoryRecordsAlreadySent =
            createStatusHistoryRecords(Arrays.asList(SCHEDULE_REQUESTED, SCHEDULED));
        NotificationChannelDetails notificationChannelDetails = new NotificationChannelDetails();
        notificationChannelDetails.setStatusHistoryRecordList(statusHistoryRecordsAlreadySent);
        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setRequestId("123");

        when(notificationHistoryService.getNotificationStatus(any(), any(), any())).thenReturn(
            notificationChannelDetails);
        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());
        assertNotNull(igniteEventImpl);
        notificationService.deleteScheduledNotification(igniteEventImpl);
    }

    @Test
    public void isStatusAllowDeletionFalse() throws Exception {
        List<StatusHistoryRecord> statusHistoryRecordsAlreadySent =
            createStatusHistoryRecords(Arrays.asList(SCHEDULE_REQUESTED, SCHEDULED, READY, DONE));
        List<StatusHistoryRecord> statusHistoryRecords = createStatusHistoryRecords(Arrays.asList(SCHEDULE_REQUESTED));

        assertFalse(notificationService.isStatusAllowDeletion(statusHistoryRecordsAlreadySent));
        assertFalse(notificationService.isStatusAllowDeletion(statusHistoryRecords));
    }

    @Test
    public void createNotificationNonRegVehicle() throws ExecutionException {

        NotificationCreationRequest notificationCreationRequest = getNotificationCreationRequest();
        notificationCreationRequest.getData().put(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD, true);

        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());

        String response = notificationService.createNotification(notificationCreationRequest);

        assertEquals(PLATFORM_RESPONSE_ID, response);
    }

    private List<StatusHistoryRecord> createStatusHistoryRecords(List<AlertsHistoryInfo.Status> statuses) {
        List<StatusHistoryRecord> statusHistoryRecords = new ArrayList<>();
        statuses.forEach(s -> statusHistoryRecords.add(new StatusHistoryRecord(s)));
        return statusHistoryRecords;
    }

    private NotificationCreationRequest getNotificationCreationRequest() {
        NotificationCreationRequest notificationCreationRequest = new NotificationCreationRequest();
        notificationCreationRequest.setVehicleId(VEHICLE_ID);
        notificationCreationRequest.setNotificationId(NOTIFICATION_ID);
        notificationCreationRequest.setRequestId(PLATFORM_RESPONSE_ID);

        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        notificationCreationRequest.setData(data);
        return notificationCreationRequest;
    }

    private List<NotificationGrouping> getNotificationGroupings() {
        NotificationGrouping notificationGrouping = new NotificationGrouping(NOTIFICATION_ID, "all");
        return Collections.singletonList(notificationGrouping);
    }

}
