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
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.domain.notification.utils.NotificationConfigCommonService;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.entities.DefaultIgniteEvent;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.config.NotificationConfigRequest;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
import org.eclipse.ecsp.notification.dao.TokenUserMapDao;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.TokenUserMap;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.NotificationConfigResponse;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidContactInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidGroupException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidUserIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationGroupingNotAllowedException;
import org.eclipse.ecsp.platform.notification.exceptions.ServiceNameNotFoundException;
import org.eclipse.ecsp.security.utils.HmacSignatureGenerator;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.eclipse.ecsp.notification.grouping.GroupType.DEFAULT;
import static org.eclipse.ecsp.notification.grouping.GroupType.USER_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * NotificationConfigServiceV1Test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationConfigServiceV1Test {
    private static final String USER_ID = "USER_ID";
    private static final String VEHICLE_ID = "VEHICLE_ID";

    @InjectMocks
    private NotificationConfigServiceV1_0 notificationConfigServiceV1;

    @Mock
    private NotificationTemplateDAO notificationTemplateDao;

    @Mock
    private NotificationTemplateConfigDAO notificationTemplateConfigDao;

    @Mock
    private KafkaService kafkaService;

    @Mock
    private NotificationGroupingDAO notificationGroupingDao;

    @Mock
    private NotificationConfigDAO notificationConfigDao;

    @Mock
    private UserProfileDAO userProfileDao;

    @Mock
    private SecondaryContactDAO secondaryContactDao;

    @Mock
    private HmacSignatureGenerator hmacSignatureGenerator;

    @Mock
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @Mock
    TokenUserMapDao tokenUserMapDao;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private NotificationEncryptionServiceImpl encryptionDecryptionService;

    @Mock
    private NotificationConfigCommonService notificationConfigCommonService;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void processNotificationPreference() throws Exception {
        Mockito.doThrow(ExecutionException.class).when(kafkaService).sendIgniteEvent(any(), any(), any());
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> notificationConfigServiceV1.processNotificationPreference(USER_ID, VEHICLE_ID,
                new DefaultIgniteEvent(new byte[1])));
        assertTrue(thrown.getMessage().contains("Failed to send the request to kafka"));
    }

    @Test
    public void processNotificationPreferenceWhenVehicleIdNull() throws Exception {
        Mockito.doThrow(ExecutionException.class).when(kafkaService).sendIgniteEvent(any(), any(), any());
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> notificationConfigServiceV1.processNotificationPreference(USER_ID, null,
                new DefaultIgniteEvent(new byte[1])));
        assertTrue(thrown.getMessage().contains("Failed to send the request to kafka"));
    }

    @Test
    public void processNotificationPreferenceSuccess() throws ExecutionException {
        Mockito.doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());
        notificationConfigServiceV1.processNotificationPreference(USER_ID, null, new DefaultIgniteEvent(new byte[1]));
        Mockito.verify(kafkaService, Mockito.times(1)).sendIgniteEvent(any(), any(), any());
    }

    @Test
    public void processUserPreferenceFailure() throws Exception {
        Mockito.doThrow(ExecutionException.class).when(kafkaService).sendIgniteEvent(any(), any(), any());
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> notificationConfigServiceV1.processUserPreference(USER_ID, new DefaultIgniteEvent(new byte[1])));
        assertTrue(thrown.getMessage().contains("Failed to send the request to kafka"));
    }

    @Test
    public void processUserPreferenceSuccess() throws Exception {
        Mockito.doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());
        notificationConfigServiceV1.processUserPreference(USER_ID, new DefaultIgniteEvent(new byte[1]));
        Mockito.verify(kafkaService, Mockito.times(1)).sendIgniteEvent(any(), any(), any());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void getNotificationPreferenceSuccess() {

        initVpClientMock();
        initUserProfileMock();
        initNotificationGroupingMock();
        initNotificationCommonServiceMock();
        initSecondaryDaoMock();

        List<NotificationConfigResponse> notificationConfigResponses =
            notificationConfigServiceV1.getNotificationPreference(USER_ID,
                VEHICLE_ID, null);
        assertEquals(2, notificationConfigResponses.size());
        Optional<NotificationConfigResponse> optional = notificationConfigResponses.stream()
            .filter(ncr -> ncr.getContactId().equals("6062b3a3af27df1c46259519")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test2@test.com", optional.get().getEmail());
        optional = notificationConfigResponses.stream().filter(ncr -> ncr.getContactId().equals("self")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test@test.com", optional.get().getEmail());
    }

    @Test
    public void getNotificationPreferenceDefaultVehicle() {

        initUserProfileMock();
        initNotificationGroupingMock();
        initNotificationCommonServiceMock();
        initSecondaryDaoMock();

        ReflectionTestUtils.setField(notificationConfigServiceV1, "enableEntitlementValidation", true);

        ServiceNameNotFoundException thrown = assertThrows(ServiceNameNotFoundException.class,
            () -> notificationConfigServiceV1.getNotificationPreference(USER_ID, "GENERAL", "s1"));
        assertTrue(thrown.getMessage().contains("service name not found"));

    }

    @Test
    public void getNotificationPreferenceSecondaryNull() {

        initVpClientMock();
        initUserProfileMock();
        initNotificationGroupingMock();
        initNotificationCommonServiceMock();

        Mockito.doReturn(null).when(secondaryContactDao).findById(any());

        List<NotificationConfigResponse> notificationConfigResponses =
            notificationConfigServiceV1.getNotificationPreference(USER_ID,
                VEHICLE_ID, null);
        assertEquals(1, notificationConfigResponses.size());
        Optional<NotificationConfigResponse> optional = notificationConfigResponses.stream()
            .filter(ncr -> ncr.getContactId().equals("self")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test@test.com", optional.get().getEmail());
    }

    @Test
    public void getNotificationPreferenceNoConfigs() {

        initVpClientMock();
        initUserProfileMock();
        initNotificationGroupingMock();
        Mockito.doReturn(new ArrayList<>()).when(notificationConfigCommonService)
            .getAllConfigsFromDbByGroup(any(), any(), any());

        List<NotificationConfigResponse> notificationConfigResponses =
            notificationConfigServiceV1.getNotificationPreference(USER_ID,
                VEHICLE_ID, null);
        assertEquals(0, notificationConfigResponses.size());
    }

    @Test
    public void getNotificationPreferenceNoSelected() {

        initVpClientMock();
        initUserProfileMock();
        initNotificationGroupingMock();


        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("u1");
        notificationConfig.setGroup("g1");
        notificationConfig.setContactId("self");
        List<Channel> channels = new ArrayList<>();
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("test@1.com"));
        emailChannel.setEnabled(true);
        channels.add(emailChannel);
        notificationConfig.setChannels(channels);

        List<NotificationConfig> userNotificationConfigs = new ArrayList<>();
        userNotificationConfigs.add(notificationConfig);
        Mockito.doReturn(userNotificationConfigs).when(notificationConfigCommonService)
            .getAllConfigsFromDbByGroup(any(), any(), any());


        NotificationConfig nc = new NotificationConfig();
        nc.setUserId("u1");
        nc.setGroup("g1");
        nc.setContactId("6062b3a3af27df1c46259519");
        nc.setChannels(channels);

        List<NotificationConfig> secondaryNotificationConfigs = new ArrayList<>();
        secondaryNotificationConfigs.add(nc);
        Mockito.doReturn(secondaryNotificationConfigs).when(notificationConfigCommonService)
            .getSecondaryContactsConfig(any(), any(), any(),
                any());

        Mockito.doReturn(new ArrayList<>()).when(notificationConfigCommonService)
            .selectNotificationConfig(any(), any(), any(), any());

        List<NotificationConfigResponse> notificationConfigResponses =
            notificationConfigServiceV1.getNotificationPreference(USER_ID,
                VEHICLE_ID, null);
        assertEquals(0, notificationConfigResponses.size());
    }

    @Test
    public void getNotificationPreferenceApiPush() {

        initVpClientMock();

        UserProfile userProfile = new UserProfile();
        userProfile.setDefaultEmail("test@test.com");
        userProfile.setDefaultPhoneNumber("+1 000 000 000");
        userProfile.setUserId("userId");
        Mockito.doReturn(userProfile).when(userProfileDao).findById(any());

        initNotificationGroupingMock();


        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("GENERAL");
        notificationConfig.setGroup("g1");
        notificationConfig.setContactId("self");
        List<Channel> channels = new ArrayList<>();
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("test@1.com"));
        emailChannel.setEnabled(true);
        channels.add(emailChannel);
        notificationConfig.setChannels(channels);

        List<NotificationConfig> userNotificationConfigs = new ArrayList<>();
        userNotificationConfigs.add(notificationConfig);
        Mockito.doReturn(userNotificationConfigs).when(notificationConfigCommonService)
            .getAllConfigsFromDbByGroup(any(), any(), any());


        NotificationConfig nc = new NotificationConfig();
        nc.setUserId("u1");
        nc.setGroup("g1");
        nc.setContactId("6062b3a3af27df1c46259519");
        channels = new ArrayList<>();
        ApiPushChannel apiPushChannel = new ApiPushChannel();
        apiPushChannel.setEnabled(true);
        channels.add(apiPushChannel);
        nc.setChannels(channels);

        List<NotificationConfig> secondaryNotificationConfigs = new ArrayList<>();
        secondaryNotificationConfigs.add(nc);
        Mockito.doReturn(secondaryNotificationConfigs).when(notificationConfigCommonService)
            .getSecondaryContactsConfig(any(), any(), any(),
                any());

        List<NotificationConfig> selectedNotificationConfigs = new ArrayList<>();
        selectedNotificationConfigs.addAll(userNotificationConfigs);
        selectedNotificationConfigs.addAll(secondaryNotificationConfigs);
        Mockito.doReturn(selectedNotificationConfigs).when(notificationConfigCommonService)
            .selectNotificationConfig(any(), any(), any(),
                any());

        initSecondaryDaoMock();

        List<NotificationConfigResponse> notificationConfigResponses =
            notificationConfigServiceV1.getNotificationPreference(USER_ID,
                VEHICLE_ID, null);
        assertEquals(1, notificationConfigResponses.size());
        Optional<NotificationConfigResponse> optional = notificationConfigResponses.stream()
            .filter(ncr -> ncr.getContactId().equals("self")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test@test.com", optional.get().getEmail());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void getNotificationPreferenceWithEntitlement() {

        initVpClientMock();
        initUserProfileMock();


        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setMandatory(false);
        notificationGrouping.setNotificationId("n1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setGroupType(DEFAULT);
        notificationGrouping.setService("s1");

        List<NotificationGrouping> groupSet = new ArrayList<>();
        groupSet.add(notificationGrouping);
        notificationGrouping = new NotificationGrouping();
        notificationGrouping.setMandatory(false);
        notificationGrouping.setNotificationId("n2");
        notificationGrouping.setGroup("g2");
        notificationGrouping.setGroupType(DEFAULT);
        notificationGrouping.setService("s2");
        notificationGrouping.setCheckEntitlement(true);
        groupSet.add(notificationGrouping);
        Mockito.doReturn(groupSet).when(notificationGroupingDao).findByMandatory(Mockito.anyBoolean());

        initNotificationCommonServiceMock();
        initSecondaryDaoMock();

        Mockito.doReturn(Collections.singleton("s1")).when(vehicleService).getEnabledServices(any());

        ReflectionTestUtils.setField(notificationConfigServiceV1, "enableEntitlementValidation", true);

        List<NotificationConfigResponse> notificationConfigResponses =
            notificationConfigServiceV1.getNotificationPreference(USER_ID,
                VEHICLE_ID, null);
        assertEquals(2, notificationConfigResponses.size());
        Optional<NotificationConfigResponse> optional = notificationConfigResponses.stream()
            .filter(ncr -> ncr.getContactId().equals("6062b3a3af27df1c46259519")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test2@test.com", optional.get().getEmail());
        optional = notificationConfigResponses.stream().filter(ncr -> ncr.getContactId().equals("self")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test@test.com", optional.get().getEmail());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void getNotificationPreferenceWithEntitlementNoEnabledServices() {

        initVpClientMock();
        initUserProfileMock();


        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setMandatory(false);
        notificationGrouping.setNotificationId("n1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setGroupType(DEFAULT);
        notificationGrouping.setService("s1");

        List<NotificationGrouping> groupSet = new ArrayList<>();
        groupSet.add(notificationGrouping);
        notificationGrouping = new NotificationGrouping();
        notificationGrouping.setMandatory(false);
        notificationGrouping.setNotificationId("n2");
        notificationGrouping.setGroup("g2");
        notificationGrouping.setGroupType(DEFAULT);
        notificationGrouping.setService("s2");
        notificationGrouping.setCheckEntitlement(true);
        groupSet.add(notificationGrouping);
        Mockito.doReturn(groupSet).when(notificationGroupingDao).findByMandatory(Mockito.anyBoolean());

        initNotificationCommonServiceMock();
        initSecondaryDaoMock();

        Mockito.doReturn(Collections.EMPTY_SET).when(vehicleService).getEnabledServices(any());

        ReflectionTestUtils.setField(notificationConfigServiceV1, "enableEntitlementValidation", true);

        List<NotificationConfigResponse> notificationConfigResponses =
            notificationConfigServiceV1.getNotificationPreference(USER_ID,
                VEHICLE_ID, null);
        assertEquals(2, notificationConfigResponses.size());
        Optional<NotificationConfigResponse> optional = notificationConfigResponses.stream()
            .filter(ncr -> ncr.getContactId().equals("6062b3a3af27df1c46259519")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test2@test.com", optional.get().getEmail());
        optional = notificationConfigResponses.stream().filter(ncr -> ncr.getContactId().equals("self")).findFirst();
        assertTrue(optional.isPresent());
        assertEquals("test@test.com", optional.get().getEmail());
    }

    @Test
    public void deleteNotificationGrouping() {
        Mockito.doReturn(1).when(notificationGroupingDao).deleteByGroupNotificationIdAndService(any(), any(), any());
        Assertions.assertNotNull(notificationGroupingDao);
        notificationConfigServiceV1.deleteNotificationGrouping(null, null, null);
    }

    @Test
    public void saveNotificationTemplates() {
        assertTrue(notificationConfigServiceV1.saveNotificationTemplates(new ArrayList<>()));
    }

    @Test
    public void saveNotificationTemplatesWithTrueResponse() {
        List<NotificationTemplate> notificationTemplatesList = new ArrayList<>();
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setBrand("Kia");
        notificationTemplatesList.add(notificationTemplate);
        Mockito.doReturn(null).when(notificationTemplateDao).saveAll(any());
        assertTrue(notificationConfigServiceV1.saveNotificationTemplates(notificationTemplatesList));
    }

    @Test
    public void saveNotificationTemplatesWithException() {
        assertFalse(notificationConfigServiceV1.saveNotificationTemplates(null));
    }

    @Test
    public void saveNotificationTemplatesConfigs() {
        assertTrue(notificationConfigServiceV1.saveNotificationTemplatesConfigs(new ArrayList<>()));
    }

    @Test
    public void saveNotificationTemplatesConfigsWhenException() {
        assertFalse(notificationConfigServiceV1.saveNotificationTemplatesConfigs(null));
    }

    @Test
    public void saveNotificationGrouping() {
        List<NotificationGrouping> notificationTemplatesList = getNotificationGrouping("id1", "g1", "s1");
        assertTrue(notificationConfigServiceV1.saveNotificationGrouping(notificationTemplatesList));
    }

    @Test
    public void saveNotificationGroupingWhenException() {
        Mockito.doThrow(RuntimeException.class).when(notificationGroupingDao).saveAll(any());
        List<NotificationGrouping> notificationTemplatesList = getNotificationGrouping("id1", "g1", "s1");
        assertFalse(notificationConfigServiceV1.saveNotificationGrouping(notificationTemplatesList));
    }

    @Test
    public void testGetNotificationGrouping() {
        List<NotificationGrouping> groups = getNotificationGrouping("id1", "g1", "s1");
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        assertEquals(groups, notificationConfigServiceV1.getNotificationGrouping(null));
    }

    @Test
    public void getNotificationGroupingWithException() {
        List<NotificationGrouping> groups = new ArrayList<>();
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        InvalidGroupException thrown = assertThrows(InvalidGroupException.class,
            () -> notificationConfigServiceV1.getNotificationGrouping(null));
        assertTrue(thrown.getMessage().contains("Grouping does not exist"));
    }

    @Test
    public void deleteNotificationGroupingWhenDeleteFailed() {
        Mockito.doReturn(0).when(notificationGroupingDao).deleteByGroupNotificationIdAndService(any(), any(), any());
        InvalidGroupException thrown = assertThrows(InvalidGroupException.class,
            () -> notificationConfigServiceV1.deleteNotificationGrouping(null, null, null));
        assertTrue(thrown.getMessage().contains("Grouping does not exist"));
    }

    @Test
    public void saveDefaultNotificationConfig() {
        List<NotificationConfig> notificationConfigList = getNotificationConfigList("u1", "v1", "g1");
        notificationConfigList.get(0).setBrand("default");

        Mockito.doReturn(notificationConfigList).when(notificationConfigDao).findDefaultByGroups(any());
        Mockito.doReturn(notificationConfigList).when(notificationConfigDao).saveAll(any());

        ReflectionTestUtils.setField(notificationConfigServiceV1, "defaultBrand", "default");
        List<NotificationConfig> notificationConfigListActual = notificationConfigServiceV1
            .saveDefaultNotificationConfig(notificationConfigList);

        assertEquals(notificationConfigList, notificationConfigListActual);
    }

    @Test
    public void saveDefaultNotificationConfigMismatchGroup() {
        List<NotificationConfig> notificationConfigList = getNotificationConfigList("u1", "v1", "g1");
        notificationConfigList.get(0).setBrand("default");

        Mockito.doReturn(notificationConfigList).when(notificationConfigDao).findDefaultByGroups(any());
        Mockito.doReturn(notificationConfigList).when(notificationConfigDao).saveAll(any());
        ReflectionTestUtils.setField(notificationConfigServiceV1, "defaultBrand", "default");


        NotificationConfig notificationConfig1 = new NotificationConfig();
        notificationConfig1.setUserId("u1");
        notificationConfig1.setVehicleId("v1");
        notificationConfig1.setGroup("g2");
        notificationConfig1.setBrand("default");

        List<NotificationConfig> notificationConfigListDto = new ArrayList<>();
        notificationConfigListDto.add(notificationConfig1);
        List<NotificationConfig> notificationConfigListActual = notificationConfigServiceV1
            .saveDefaultNotificationConfig(notificationConfigListDto);

        assertEquals(notificationConfigList, notificationConfigListActual);
    }

    @Test
    public void saveDefaultNotificationConfigWithoutDefault() {
        List<NotificationConfig> notificationConfigList = getNotificationConfigList("u1", "v1", "g1");
        notificationConfigList.get(0).setBrand("kia");

        Mockito.doReturn(notificationConfigList).when(notificationConfigDao).findDefaultByGroups(any());
        Mockito.doReturn(notificationConfigList).when(notificationConfigDao).saveAll(any());

        ReflectionTestUtils.setField(notificationConfigServiceV1, "defaultBrand", "default");

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.saveDefaultNotificationConfig(notificationConfigList));
        assertEquals("nc-10612", thrown.getErrors().iterator().next().getCode());

    }

    @Test
    public void saveNotificationConfig() {

        NotificationConfig notificationConfigDto = new NotificationConfig();
        notificationConfigDto.setUserId("u1");
        notificationConfigDto.setVehicleId("v1");
        notificationConfigDto.setGroup("g1");

        List<NotificationConfig> notificationConfigListDto = new ArrayList<>();
        notificationConfigListDto.add(notificationConfigDto);


        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("u1");
        notificationConfig.setVehicleId("v1");
        notificationConfig.setGroup("g1");

        List<NotificationConfig> existingConfigs = new ArrayList<>();
        existingConfigs.add(notificationConfig);
        Mockito.doReturn(existingConfigs).when(notificationConfigDao)
            .findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        Mockito.doReturn(existingConfigs).when(notificationConfigDao).saveAll(any());
        List<NotificationConfig> notificationConfigListActual =
            notificationConfigServiceV1.saveNotificationConfig(USER_ID, VEHICLE_ID,
                null, notificationConfigListDto);
        assertEquals(existingConfigs, notificationConfigListActual);
    }

    @Test
    public void saveNotificationConfigGroupMismatch() {

        NotificationConfig notificationConfigDto = new NotificationConfig();
        notificationConfigDto.setUserId("u1");
        notificationConfigDto.setVehicleId("v1");
        notificationConfigDto.setGroup("g1");
        List<NotificationConfig> notificationConfigListDto = new ArrayList<>();
        notificationConfigListDto.add(notificationConfigDto);


        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("u1");
        notificationConfig.setVehicleId("v1");
        notificationConfig.setGroup("g2");

        List<NotificationConfig> existingConfigs = new ArrayList<>();
        existingConfigs.add(notificationConfig);
        Mockito.doReturn(existingConfigs).when(notificationConfigDao).saveAll(any());
        Mockito.doReturn(existingConfigs).when(notificationConfigDao)
            .findByUserIdAndVehicleIdAndContactIdAndGroups(any(), any(), any(),
                any());
        List<NotificationConfig> notificationConfigListActual =
            notificationConfigServiceV1.saveNotificationConfig(USER_ID, VEHICLE_ID,
                null, notificationConfigListDto);
        assertEquals(existingConfigs, notificationConfigListActual);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void sanitizeConfig() {

        NotificationConfig notificationConfigDto = new NotificationConfig();
        notificationConfigDto.setUserId("u1");
        notificationConfigDto.setVehicleId("v1");
        notificationConfigDto.setGroup("g1");



        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.VACATION);
        LocalDate start = LocalDate.of(2020, 1, 8);
        suppressionConfig.setStartDate(start);
        LocalTime time = LocalTime.now();
        suppressionConfig.setStartTime(time);
        LocalTime endTime = LocalTime.now();
        suppressionConfig.setEndDate(start);
        suppressionConfig.setEndTime(endTime);

        List<SuppressionConfig> suppressionConfigs = new ArrayList<>();
        suppressionConfigs.add(suppressionConfig);

        Channel channel = new ApiPushChannel();
        channel.setSuppressionConfigs(suppressionConfigs);
        channel.setEnabled(true);
        channel.setType("t1");
        List<Channel> channels = new ArrayList<>();
        channels.add(channel);
        notificationConfigDto.setChannels(channels);

        List<NotificationConfig> notificationConfigListDto = new ArrayList<>();
        notificationConfigListDto.add(notificationConfigDto);

        List<NotificationConfig> notificationConfigListActual =
            notificationConfigServiceV1.sanitizeConfig(notificationConfigListDto);
        assertEquals(notificationConfigListDto, notificationConfigListActual);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void sanitizeConfigWithRecurringSuppressionType() {

        NotificationConfig notificationConfigDto = new NotificationConfig();
        notificationConfigDto.setUserId("u1");
        notificationConfigDto.setVehicleId("v1");
        notificationConfigDto.setGroup("g1");

        SuppressionConfig suppressionConfig = new SuppressionConfig();
        suppressionConfig.setSuppressionType(SuppressionConfig.SuppressionType.RECURRING);
        LocalDate start = LocalDate.of(2020, 1, 8);
        suppressionConfig.setStartDate(start);
        LocalTime time = LocalTime.now();
        suppressionConfig.setStartTime(time);
        LocalTime endTime = LocalTime.now();
        suppressionConfig.setEndDate(start);
        suppressionConfig.setEndTime(endTime);


        Channel channel = new ApiPushChannel();
        List<SuppressionConfig> suppressionConfigs = new ArrayList<>();
        suppressionConfigs.add(suppressionConfig);
        channel.setSuppressionConfigs(suppressionConfigs);
        channel.setEnabled(true);
        channel.setType("t1");
        List<Channel> channels = new ArrayList<>();
        channels.add(channel);
        notificationConfigDto.setChannels(channels);
        List<NotificationConfig> notificationConfigListDto = new ArrayList<>();
        notificationConfigListDto.add(notificationConfigDto);

        List<NotificationConfig> notificationConfigListActual =
            notificationConfigServiceV1.sanitizeConfig(notificationConfigListDto);
        assertEquals(notificationConfigListDto, notificationConfigListActual);
    }

    @Test
    public void encryptNotificationConfig() {
        assertNotNull(notificationConfigServiceV1);
        notificationConfigServiceV1.encryptNotificationConfig(null);
    }



    @Test
    public void getUserProfile() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        UserProfile userProfileActual = notificationConfigServiceV1.getUserProfile(null);
        assertEquals(up, userProfileActual);
    }

    @Test
    public void getSecondaryContact() {
        SecondaryContact sc = new SecondaryContact();
        sc.setUserId("ben");
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        SecondaryContact secondaryContactActual = notificationConfigServiceV1.getSecondaryContact(null);
        assertEquals(sc, secondaryContactActual);
    }

    @Test
    public void isUserExists() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        assertTrue(notificationConfigServiceV1.isUserExists("boom"));
    }

    @Test
    public void validateInputWhenUserDoesNotExist() {
        Mockito.doReturn(null).when(userProfileDao).findById(any());
        NotificationConfigRequest configRequest = new NotificationConfigRequest();
        Collection<NotificationConfigRequest> configs = Collections.singleton(configRequest);
        RuntimeException thrown = assertThrows(InvalidUserIdInput.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", configs));
        assertEquals("User doesn't exist", thrown.getMessage());
    }

    @Test
    public void validateInputWhenVehicleDoesNotExist() {
        NotificationConfigRequest configRequest = new NotificationConfigRequest();

        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Optional<String> optional = Optional.empty();
        Collection<NotificationConfigRequest> configs = Collections.singleton(configRequest);
        Mockito.doThrow(new RuntimeException("Error while querying json path")).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.anyString(), Mockito.anyBoolean());
        RuntimeException thrown = assertThrows(InvalidVehicleIdInput.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", configs));
        assertEquals("Vehicle doesn't exist", thrown.getMessage());
    }

    @Test
    public void validateInputWithNonExistingSecondaryContact() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        Mockito.doReturn(null).when(secondaryContactDao).findById(any());
        NotificationConfigRequest configRequest = new NotificationConfigRequest();
        Collection<NotificationConfigRequest> configs = Collections.singleton(configRequest);
        RuntimeException thrown = assertThrows(InvalidContactInput.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", configs));
        assertEquals("Invalid contact id", thrown.getMessage());
    }

    @Test
    public void validateInputInvalidGroupDoesNotExist() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("gk1");
        Mockito.doReturn(null).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest request1 = new NotificationConfigRequest();
        request1.setGroup("g1");
        NotificationConfigRequest request2 = new NotificationConfigRequest();
        request2.setGroup("g2");
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(request1);
        notificationConfigRequests.add(request2);
        RuntimeException thrown = assertThrows(NoSuchEntityException.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests));
        assertEquals("No such group(s): [g1, g2]", thrown.getMessage());
    }

    @Test
    public void validateInputInvalidGroupWhenOneGroupDoesNotExist() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g1");
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest request1 = new NotificationConfigRequest();
        request1.setGroup("g1");
        NotificationConfigRequest request2 = new NotificationConfigRequest();
        request2.setGroup("g2");
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(request1);
        notificationConfigRequests.add(request2);
        RuntimeException thrown = assertThrows(NoSuchEntityException.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests));
        assertEquals("No such group(s): [g2]", thrown.getMessage());
    }

    @Test
    public void validateInputInvalidGroupWhenGroupCountSame() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        Collection<NotificationGrouping> groups = new ArrayList<>();
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("gk1");
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        RuntimeException thrown = assertThrows(NotificationGroupingNotAllowedException.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests));
        assertEquals("The following groups: [g2] are not allowed for API of type USER_VEHICLE", thrown.getMessage());
    }

    @Test
    public void validateInputWithGroupTypeDefault() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        Collection<NotificationGrouping> groups = new ArrayList<>();
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        ReflectionTestUtils.setField(notificationConfigServiceV1, "enableEntitlementValidation", true);
        assertTrue((Boolean) ReflectionTestUtils.getField(notificationConfigServiceV1, "enableEntitlementValidation"));

        Set<String> services = new HashSet<>();
        Mockito.doReturn(services).when(vehicleService).getEnabledServices(any());
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
            .findDefaultConfigByBrand(any(), any());
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateInputWithGroupTypeUserOnly() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        Collection<NotificationGrouping> groups = new ArrayList<>();
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(USER_ONLY);
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        ReflectionTestUtils.setField(notificationConfigServiceV1, "enableEntitlementValidation", true);
        RuntimeException thrown = assertThrows(NotificationGroupingNotAllowedException.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests));
        assertEquals("The following groups: [g2] are not allowed for API of type USER_VEHICLE", thrown.getMessage());
    }

    @Test
    public void validateInputWhenDefaultConfigDoesNotExist() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new EmailChannel()));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        Mockito.doReturn(new ArrayList<>()).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Mockito.doReturn(null).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateInputWhenConfigRequestDoesNotContainChannels() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        assertNotNull(up);
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
            .findDefaultConfigByBrand(any(), any());
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateInputWhenDefaultConfigDoesNotHaveChannels() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new EmailChannel()));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
            .findDefaultConfigByBrand(any(), any());
        assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests));
    }

    @Test
    public void validateInputWhenDefaultConfigDoesNotHaveTheSpecifiedChannel() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Optional<String> optional = Optional.of("");
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new EmailChannel()));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        config.setChannels(Collections.singletonList(new SmsChannel()));
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
            .findDefaultConfigByBrand(any(), any());
        assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests));
    }

    @Test
    public void validateInputSucceed() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        assertNotNull(up);
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new EmailChannel()));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        config.setChannels(Collections.singletonList(new EmailChannel()));
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
            .findDefaultConfigByBrand(any(), any());
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateInputEmailSuccess() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        assertNotNull(up);
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        List<String> lsem = new ArrayList<String>();
        lsem.add("dasdsa@sdfdsf.com");
        Channel ch = new EmailChannel(lsem);
        ch.setType("email");
        ch.setEnabled(false);
        List<Channel> lsch = new ArrayList<Channel>();
        lsch.add(ch);


        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new EmailChannel(lsem)));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        config.setChannels(lsch);
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
            .findDefaultConfigByBrand(any(), any());
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateInputInvalidEmail() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        assertNotNull(up);
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        List<String> lsem = new ArrayList<String>();
        lsem.add("8989100769");
        Channel ch = new EmailChannel(lsem);
        ch.setType("email");
        ch.setEnabled(false);
        List<Channel> lsch = new ArrayList<Channel>();
        lsch.add(ch);


        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new EmailChannel(lsem)));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        config.setChannels(lsch);
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
                .findDefaultConfigByBrand(any(), anyString());
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateInputPhNoSuccess() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        assertNotNull(up);
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());

        Channel ch = new SmsChannel("+3131", "43243-2");
        ch.setType("sms");
        ch.setEnabled(false);
        List<Channel> lsch = new ArrayList<Channel>();
        lsch.add(ch);


        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new SmsChannel("+3131", "43243-2")));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        config.setChannels(lsch);
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
            .findDefaultConfigByBrand(any(), any());
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateInputPhNoException() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        assertNotNull(up);
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist(VEHICLE_ID);
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        Collection<NotificationGrouping> groups = new ArrayList<>();
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());

        Channel ch = new SmsChannel("+3131", "efsdf-2");
        ch.setType("sms");
        ch.setEnabled(false);
        List<Channel> lsch = new ArrayList<Channel>();
        lsch.add(ch);


        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        notificationConfigRequest.setChannels(Collections.singletonList(new SmsChannel("+3131", "efsdf-2")));
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        NotificationConfig config = new NotificationConfig();
        config.setGroup("g2");
        config.setChannels(lsch);
        List<NotificationConfig> configs = Collections.singletonList(config);
        Mockito.doReturn(configs).when(notificationConfigDao).findDefaultByGroups(Mockito.anySet());
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.MAKE, Optional.empty());
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
        Mockito.doReturn(Optional.of(configs.get(0))).when(notificationConfigCommonService)
                .findDefaultConfigByBrand(any(), anyString());
        notificationConfigServiceV1.validateInput(USER_ID, VEHICLE_ID, "c1", notificationConfigRequests);
    }

    @Test
    public void validateContactUniquenessContactNameNotEmpty() {

        SecondaryContact secondaryContact1 = new SecondaryContact();
        secondaryContact1.setContactId("cid1");
        secondaryContact1.setContactName("n1");

        SecondaryContact secondaryContact2 = new SecondaryContact();
        secondaryContact2.setContactId("cid2");
        secondaryContact2.setContactName("n1");

        List<SecondaryContact> existingContacts = new ArrayList<>();
        existingContacts.add(secondaryContact1);
        existingContacts.add(secondaryContact2);

        Mockito.doReturn(existingContacts).when(secondaryContactDao).findByIds(any());

        List<String> contacts = new ArrayList<>();
        Mockito.doReturn(contacts).when(secondaryContactDao).getContactIds(any(), any());
        SecondaryContact secondaryContactDto = new SecondaryContact();
        secondaryContactDto.setContactId("cid1");
        secondaryContactDto.setContactName("n1");
        InvalidContactInput thrown = assertThrows(InvalidContactInput.class,
            () -> notificationConfigServiceV1.validateContactUniqueness(secondaryContactDto));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void validateContactUniquenessEmailNotEmpty() {

        SecondaryContact secondaryContact1 = new SecondaryContact();
        secondaryContact1.setContactId("cid1");
        secondaryContact1.setEmail("abc@yomail.com");

        SecondaryContact secondaryContact2 = new SecondaryContact();
        secondaryContact2.setContactId("cid2");
        secondaryContact2.setEmail("abc@yomail.com");

        List<SecondaryContact> existingContacts = new ArrayList<>();
        existingContacts.add(secondaryContact1);
        existingContacts.add(secondaryContact2);

        Mockito.doReturn(existingContacts).when(secondaryContactDao).findByIds(any());

        List<String> contacts = new ArrayList<>();
        Mockito.doReturn(contacts).when(secondaryContactDao).getContactIds(any(), any());
        SecondaryContact secondaryContactDto = new SecondaryContact();
        secondaryContactDto.setContactId("cid1");
        secondaryContactDto.setEmail("abc@yomail.com");
        InvalidContactInput thrown = assertThrows(InvalidContactInput.class,
            () -> notificationConfigServiceV1.validateContactUniqueness(secondaryContactDto));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void validateContactUniquenessPhoneExist() {

        SecondaryContact secondaryContact1 = new SecondaryContact();
        secondaryContact1.setContactId("cid1");
        secondaryContact1.setPhoneNumber("+91 1111111111");

        SecondaryContact secondaryContact2 = new SecondaryContact();
        secondaryContact2.setContactId("cid2");
        secondaryContact2.setPhoneNumber("+91 1111111111");

        List<SecondaryContact> existingContacts = new ArrayList<>();
        existingContacts.add(secondaryContact1);
        existingContacts.add(secondaryContact2);

        Mockito.doReturn(existingContacts).when(secondaryContactDao).findByIds(any());

        List<String> contacts = new ArrayList<>();
        Mockito.doReturn(contacts).when(secondaryContactDao).getContactIds(any(), any());
        SecondaryContact secondaryContactDto = new SecondaryContact();
        secondaryContactDto.setContactId("cid1");
        secondaryContactDto.setPhoneNumber("+91 1111111111");
        InvalidContactInput thrown = assertThrows(InvalidContactInput.class,
            () -> notificationConfigServiceV1.validateContactUniqueness(secondaryContactDto));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void validateUserIdAndVehicleIdWhenUserIdNotRegistered() {
        Mockito.doReturn(null).when(userProfileDao).findById(any());
        InvalidUserIdInput thrown = assertThrows(InvalidUserIdInput.class,
            () -> notificationConfigServiceV1.validateUserIdAndVehicleId(null, null));
        assertTrue(thrown.getMessage().contains("User Id not registered"));
    }

    @Test
    public void validateUserIdAndVehicleIdWhenVehicleIdNotRegistered() {
        UserProfile uc = new UserProfile();
        Mockito.doReturn(uc).when(userProfileDao).findById(any());
        Optional<String> optional = Optional.of("");
        Mockito.doReturn(optional).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.anyString(), Mockito.anyBoolean());
        InvalidVehicleIdInput thrown = assertThrows(InvalidVehicleIdInput.class,
            () -> notificationConfigServiceV1.validateUserIdAndVehicleId("u1", null));
        assertTrue(thrown.getMessage().contains("Vehicle Id not registered"));
    }

    @Test
    public void validateUserIdAndVehicleId() {
        UserProfile uc = new UserProfile();
        assertNotNull(uc);
        Mockito.doReturn(uc).when(userProfileDao).findById(any());
        Mockito.doReturn(true).when(vehicleService).isVehicleExist("v1");
        notificationConfigServiceV1.validateUserIdAndVehicleId("u1", "v1");
    }

    @Test
    public void validategroupinginputgroupingMultiGroupForNotificationId() {
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setNotificationId("nid1");
        notificationGrouping.setGroup("g2");
        notificationGroupingList.add(notificationGrouping);


        NotificationGrouping notificationGroupingOfDb = new NotificationGrouping();
        notificationGroupingOfDb.setNotificationId("nid1");
        notificationGroupingOfDb.setGroup("g1");
        notificationGroupingOfDb.setGroupType(DEFAULT);

        NotificationGrouping notificationGroupingOfDb2 = new NotificationGrouping();
        notificationGroupingOfDb2.setNotificationId("nid1");
        notificationGroupingOfDb2.setGroup("g1");
        notificationGroupingOfDb2.setGroupType(DEFAULT);

        List<NotificationGrouping> notificationGroupingDbList = new ArrayList<>();
        notificationGroupingDbList.add(notificationGroupingOfDb);
        notificationGroupingDbList.add(notificationGroupingOfDb2);
        Mockito.doReturn(notificationGroupingDbList).when(notificationGroupingDao).findAll();

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.validateGroupingInput(notificationGroupingList));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void validategroupinginputgroupingDuplicateKey() {

        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setNotificationId("nid1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setService("s1");
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        notificationGroupingList.add(notificationGrouping);


        NotificationGrouping notificationgroupingofdb = new NotificationGrouping();
        notificationgroupingofdb.setNotificationId("nid1");
        notificationgroupingofdb.setGroup("g1");
        notificationgroupingofdb.setGroupType(DEFAULT);
        notificationgroupingofdb.setService("s1");

        NotificationGrouping notificationgroupingofdb2 = new NotificationGrouping();
        notificationgroupingofdb2.setNotificationId("nid1");
        notificationgroupingofdb2.setGroup("g1");
        notificationgroupingofdb2.setGroupType(DEFAULT);
        notificationgroupingofdb2.setService("s1");

        List<NotificationGrouping> notificationGroupingDbList = new ArrayList<>();
        notificationGroupingDbList.add(notificationgroupingofdb);
        notificationGroupingDbList.add(notificationgroupingofdb2);
        Mockito.doReturn(notificationGroupingDbList).when(notificationGroupingDao).findAll();

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.validateGroupingInput(notificationGroupingList));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void validategroupinginputgroupingDuplicateKeynoexception() {

        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setNotificationId("nid1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setService("s1");

        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        notificationGroupingList.add(notificationGrouping);


        NotificationGrouping notificationGroupingOfDb = new NotificationGrouping();
        notificationGroupingOfDb.setNotificationId("nid1");
        notificationGroupingOfDb.setGroup("g1");
        notificationGroupingOfDb.setGroupType(DEFAULT);
        notificationGroupingOfDb.setService("s2");

        NotificationGrouping notificationGroupingOfDb2 = new NotificationGrouping();
        notificationGroupingOfDb2.setNotificationId("nid1");
        notificationGroupingOfDb2.setGroup("g1");
        notificationGroupingOfDb2.setGroupType(DEFAULT);
        notificationGroupingOfDb2.setService("s3");

        List<NotificationGrouping> notificationGroupingDbList = new ArrayList<>();
        notificationGroupingDbList.add(notificationGroupingOfDb);
        notificationGroupingDbList.add(notificationGroupingOfDb2);
        Mockito.doReturn(notificationGroupingDbList).when(notificationGroupingDao).findAll();

        assertNotNull(notificationGroupingList);
        notificationConfigServiceV1.validateGroupingInput(notificationGroupingList);
    }

    @Test
    public void validategroupinginputgroupingTypenull() {

        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setNotificationId("nid1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setService("s1");
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        notificationGroupingList.add(notificationGrouping);


        NotificationGrouping notificationGroupingOfDb = new NotificationGrouping();
        notificationGroupingOfDb.setNotificationId("nid1");
        notificationGroupingOfDb.setGroup("g1");
        notificationGroupingOfDb.setGroupType(null);
        notificationGroupingOfDb.setService("s1");

        NotificationGrouping notificationGroupingOfDb2 = new NotificationGrouping();
        notificationGroupingOfDb2.setNotificationId("nid1");
        notificationGroupingOfDb2.setGroup("g1");
        notificationGroupingOfDb2.setGroupType(null);
        notificationGroupingOfDb2.setService("s1");

        List<NotificationGrouping> notificationGroupingDbList = new ArrayList<>();
        notificationGroupingDbList.add(notificationGroupingOfDb);
        notificationGroupingDbList.add(notificationGroupingOfDb2);
        Mockito.doReturn(notificationGroupingDbList).when(notificationGroupingDao).findAll();

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.validateGroupingInput(notificationGroupingList));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void notifyNotificationConfigUpdate() {
        assertNotNull(notificationConfigServiceV1);
        notificationConfigServiceV1.notifyNotificationConfigUpdate(null, null, null, null, null);
    }

    @Test
    public void notifyNotificationConfigUpdateWhenException() throws Exception {
        Mockito.doThrow(ExecutionException.class).when(kafkaService).sendIgniteEvent(any(), any(), any());
        assertNotNull(notificationConfigServiceV1);
        notificationConfigServiceV1.notifyNotificationConfigUpdate(null, null, null, null, null);
    }

    @Test
    public void deleteFcmToken() {
        TokenUserMap tokenMapping = new TokenUserMap();
        tokenMapping.setUserID("u1");
        tokenMapping.setToken("t1");
        Mockito.doReturn(tokenMapping).when(tokenUserMapDao).findById(any());
        assertNotNull(tokenMapping);
        notificationConfigServiceV1.deleteFcmToken("t1", "u1");
    }

    @Test
    public void deletefcmtokenwhenfcmTokenDoesNotExist() {
        Mockito.doReturn(null).when(tokenUserMapDao).findById(any());
        InvalidInputException thrown = assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.deleteFcmToken("t1", "u1"));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void deletefcmtokenwheninvalidUseridException() {
        TokenUserMap tokenMapping = new TokenUserMap();
        tokenMapping.setUserID("u1");
        tokenMapping.setToken("t1");
        Mockito.doReturn(tokenMapping).when(tokenUserMapDao).findById(any());
        InvalidInputException thrown = assertThrows(InvalidInputException.class,
            () -> notificationConfigServiceV1.deleteFcmToken("t1", "u2"));
        assertTrue(thrown.getMessage().contains("Invalid input"));
    }

    @Test
    public void getdefaultconfigwhengroupingNotificationIdNotFound() {
        List<NotificationGrouping> notificationGroupings = new ArrayList<>();
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByNotificationId(any());
        NotFoundException thrown = assertThrows(NotFoundException.class,
            () -> notificationConfigServiceV1.getDefaultConfig(null, null));
        assertTrue(thrown.getMessage().contains("Not found"));
    }

    @Test
    public void getdefaultconfigwhennotificationConfigNotFound() {

        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setNotificationId("nid1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setService("s1");
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        notificationGroupingList.add(notificationGrouping);
        Mockito.doReturn(notificationGroupingList).when(notificationGroupingDao).findByNotificationId(any());

        List<NotificationConfig> notificationConfigs = new ArrayList<>();
        Mockito.doReturn(notificationConfigs).when(notificationConfigDao).findDefaultByGroupsAndBrand(any(), any());
        NotFoundException thrown = assertThrows(NotFoundException.class,
            () -> notificationConfigServiceV1.getDefaultConfig(null, null));
        assertTrue(thrown.getMessage().contains("Not found"));
    }

    @Test
    public void getDefaultConfig() {
        List<NotificationGrouping> notificationGroupingList = getNotificationGrouping("nid1", "g1", "s1");
        Mockito.doReturn(notificationGroupingList).when(notificationGroupingDao).findByNotificationId(any());
        List<NotificationConfig> notificationConfigs = new ArrayList<>();
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setGroup("g1");
        notificationConfigs.add(notificationConfig);
        Mockito.doReturn(notificationConfigs).when(notificationConfigDao).findDefaultByGroupsAndBrand(any(), any());
        assertNotNull(notificationConfigs);
        notificationConfigServiceV1.getDefaultConfig("id1", "b1");
    }

    @Test
    public void getalldefaultconfigswhengroupingNotificationIdNotFound() {
        List<NotificationGrouping> notificationGroupings = new ArrayList<>();
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByNotificationId(any());
        NotFoundException thrown = assertThrows(NotFoundException.class,
            () -> notificationConfigServiceV1.getAllDefaultConfigs(null));
        assertTrue(thrown.getMessage().contains("Not found"));
    }

    @Test
    public void getalldefaultconfigswhennotificationConfigNotFound() {

        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setNotificationId("nid1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setService("s1");

        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        notificationGroupingList.add(notificationGrouping);
        Mockito.doReturn(notificationGroupingList).when(notificationGroupingDao).findByNotificationId(any());

        List<NotificationConfig> notificationConfigs = new ArrayList<>();
        Mockito.doReturn(notificationConfigs).when(notificationConfigDao).findDefaultByGroups(any());
        NotFoundException thrown = assertThrows(NotFoundException.class,
            () -> notificationConfigServiceV1.getAllDefaultConfigs(null));
        assertTrue(thrown.getMessage().contains("Not found"));
    }

    @Test
    public void getAllDefaultConfigs() {
        List<NotificationGrouping> notificationGroupingList = getNotificationGrouping("nid1", "g1", "s1");
        Mockito.doReturn(notificationGroupingList).when(notificationGroupingDao).findByNotificationId(any());
        List<NotificationConfig> notificationConfigs = new ArrayList<>();
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setGroup("g1");
        notificationConfigs.add(notificationConfig);
        Mockito.doReturn(notificationConfigs).when(notificationConfigDao).findDefaultByGroups(any());
        assertNotNull(notificationGroupingList);
        notificationConfigServiceV1.getAllDefaultConfigs("id1");
    }

    @Test
    public void validateInputWithGroupTypeDefaultForUserOnlyConfig() {
        UserProfile up = new UserProfile();
        up.setFirstName("ben");
        assertNotNull(up);
        Mockito.doReturn(up).when(userProfileDao).findById(any());
        SecondaryContact sc = new SecondaryContact();
        Mockito.doReturn(sc).when(secondaryContactDao).findById(any());
        Collection<NotificationGrouping> groups = new ArrayList<>();
        NotificationGrouping grouping = new NotificationGrouping();
        grouping.setGroup("g2");
        grouping.setGroupType(DEFAULT);
        groups.add(grouping);
        Mockito.doReturn(groups).when(notificationGroupingDao).findByGroups(any());
        NotificationConfigRequest notificationConfigRequest = new NotificationConfigRequest();
        notificationConfigRequest.setGroup("g2");
        List<NotificationConfigRequest> notificationConfigRequests = new ArrayList<>();
        notificationConfigRequests.add(notificationConfigRequest);
        ReflectionTestUtils.setField(notificationConfigServiceV1, "enableEntitlementValidation", true);
        ReflectionTestUtils.setField(notificationConfigServiceV1, "defaultBrand", "default");
        notificationConfigServiceV1.validateInput(USER_ID, "GENERAL", "c1", notificationConfigRequests);
        Mockito.verify(vehicleService, Mockito.times(0)).isVehicleExist(anyString());
        Mockito.verify(notificationGroupingDao, Mockito.times(1)).findByGroups(any());
    }

    private List<NotificationConfig> getNotificationConfigList(String userid, String vehicleId, String groupName) {

        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId(userid);
        notificationConfig.setVehicleId(vehicleId);
        notificationConfig.setGroup(groupName);
        List<NotificationConfig> notificationConfigList = new ArrayList<>();
        notificationConfigList.add(notificationConfig);
        return notificationConfigList;
    }

    private List<NotificationGrouping> getNotificationGrouping(String notificationId, String groupId,
                                                               String serviceId) {

        NotificationGrouping ng = new NotificationGrouping();
        ng.setNotificationId("id1");
        ng.setGroup("g1");
        ng.setService("s1");
        List<NotificationGrouping> notificationGroupings = new ArrayList<>();
        notificationGroupings.add(ng);
        return notificationGroupings;
    }

    private void initVpClientMock() {
        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        VehicleProfileAttribute[] vehicleProfileAttributes =
            new VehicleProfileAttribute[] {VehicleProfileAttribute.MAKE};
        attr.put(vehicleProfileAttributes[0], Optional.of("default"));
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(any(), anyBoolean(),
            any(VehicleProfileAttribute.class));
    }

    private void initUserProfileMock() {
        UserProfile userProfile = new UserProfile();
        userProfile.setDefaultEmail("test@test.com");
        userProfile.setDefaultPhoneNumber("+1 000 000 000");
        Mockito.doReturn(userProfile).when(userProfileDao).findById(any());
    }

    private void initNotificationGroupingMock() {

        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setMandatory(false);
        notificationGrouping.setNotificationId("n1");
        notificationGrouping.setGroup("g1");
        notificationGrouping.setGroupType(DEFAULT);
        List<NotificationGrouping> groupSet = new ArrayList<>();
        groupSet.add(notificationGrouping);
        Mockito.doReturn(groupSet).when(notificationGroupingDao).findByMandatory(Mockito.anyBoolean());
    }

    private void initNotificationCommonServiceMock() {

        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("u1");
        notificationConfig.setGroup("g1");
        notificationConfig.setContactId("self");
        List<Channel> channels = new ArrayList<>();
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("test@1.com"));
        emailChannel.setEnabled(true);
        channels.add(emailChannel);
        notificationConfig.setChannels(channels);

        List<NotificationConfig> userNotificationConfigs = new ArrayList<>();
        userNotificationConfigs.add(notificationConfig);
        Mockito.doReturn(userNotificationConfigs).when(notificationConfigCommonService)
            .getAllConfigsFromDbByGroup(any(), any(), any());


        NotificationConfig nc = new NotificationConfig();
        nc.setUserId("u1");
        nc.setGroup("g1");
        nc.setContactId("6062b3a3af27df1c46259519");
        nc.setChannels(channels);

        List<NotificationConfig> secondaryNotificationConfigs = new ArrayList<>();
        secondaryNotificationConfigs.add(nc);
        Mockito.doReturn(secondaryNotificationConfigs).when(notificationConfigCommonService)
            .getSecondaryContactsConfig(any(), any(), any(),
                any());

        List<NotificationConfig> selectedNotificationConfigs = new ArrayList<>();
        selectedNotificationConfigs.addAll(userNotificationConfigs);
        selectedNotificationConfigs.addAll(secondaryNotificationConfigs);
        Mockito.doReturn(selectedNotificationConfigs).when(notificationConfigCommonService)
            .selectNotificationConfig(any(), any(), any(),
                any());
    }

    private void initSecondaryDaoMock() {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setLocale(new Locale("EN", "US"));
        secondaryContact.setEmail("test2@test.com");
        secondaryContact.setPhoneNumber("+1 000 000 002");
        secondaryContact.setContactName("s1");
        Mockito.doReturn(secondaryContact).when(secondaryContactDao).findById(any());
    }
}