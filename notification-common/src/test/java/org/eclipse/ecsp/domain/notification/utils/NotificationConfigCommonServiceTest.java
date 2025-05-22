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

package org.eclipse.ecsp.domain.notification.utils;

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.PushChannel;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.eclipse.ecsp.notification.config.NotificationConfig.CONTACT_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.USER_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * NotificationConfigCommonServiceTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationConfigCommonServiceTest {

    private static final String VEHICLE_ID = "HJKJDHS78983DJ";
    private static final String USER_ID = "noname001";
    private static final String DEFAULT_EMAIL = "default@email.com";
    private static final String DEFAULT_PHONE = "123456789";
    public static final String DEFAULT_BRAND = "default";

    @InjectMocks
    private NotificationConfigCommonService notificationConfigCommonService;

    @Mock
    private NotificationConfigDAO notificationConfigDao;

    @Mock
    private SecondaryContactDAO secondaryContactDao;

    @Captor
    private ArgumentCaptor<List<String>> fetchUserIdsCaptor;

    @Captor
    private ArgumentCaptor<List<String>> fetchVehicleIdsCaptor;

    public String two = "2";

    public String three = "3";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllConfigsFromDbByGroupWithoutVehicle() {

        List<NotificationConfig> configs = createAllNotificationConfigs();
        doReturn(configs).when(notificationConfigDao)
            .findByUserVehicleGroup(fetchUserIdsCaptor.capture(), fetchVehicleIdsCaptor.capture(), any());
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getAllConfigsFromDbByGroup(USER_ID, null, "g1");
        assertEquals(Integer.parseInt(two), fetchUserIdsCaptor.getValue().size());
        assertEquals(Integer.parseInt(two), fetchVehicleIdsCaptor.getValue().size());
        assertEquals(configs, returnedConfigs);
    }

    @Test
    public void getAllConfigsFromDbByGroupWithoutUser() {

        List<NotificationConfig> configs = createAllNotificationConfigs();
        doReturn(configs).when(notificationConfigDao)
            .findByUserVehicleGroup(fetchUserIdsCaptor.capture(), fetchVehicleIdsCaptor.capture(), any());
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getAllConfigsFromDbByGroup(null, VEHICLE_ID, "g1");
        assertEquals(1, fetchUserIdsCaptor.getValue().size());
        assertEquals(1, fetchVehicleIdsCaptor.getValue().size());
        assertEquals(USER_ID_FOR_DEFAULT_PREFERENCE, fetchUserIdsCaptor.getValue().get(0));
        assertEquals(VEHICLE_ID_FOR_DEFAULT_PREFERENCE, fetchVehicleIdsCaptor.getValue().get(0));
    }

    @Test
    public void getAllConfigsFromDbByGroupWithVehicle() {

        List<NotificationConfig> configs = createAllNotificationConfigs();
        when(notificationConfigDao.findByUserVehicleGroup(fetchUserIdsCaptor.capture(), fetchVehicleIdsCaptor.capture(),
            any())).thenReturn(configs);
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getAllConfigsFromDbByGroup(USER_ID, VEHICLE_ID, "g1");
        assertEquals(Integer.parseInt(three), fetchUserIdsCaptor.getValue().size());
        assertEquals(Integer.parseInt(three), fetchVehicleIdsCaptor.getValue().size());
        assertEquals(configs, returnedConfigs);
    }

    @Test
    public void getSecondaryContactsConfigEmptyUser() {

        List<NotificationConfig> configs = createAllNotificationConfigs();
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getSecondaryContactsConfig(null, VEHICLE_ID, configs, "kia");
        assertEquals(0, returnedConfigs.size());
    }

    @Test
    public void getSecondaryContactsConfigEmptyVehicle() {

        List<NotificationConfig> configs = createAllNotificationConfigs();
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getSecondaryContactsConfig(USER_ID, null, configs, "kia");
        assertEquals(0, returnedConfigs.size());
    }

    @Test
    public void getSecondaryContactsConfigNoSecondaryWithoutConfig() {

        List<NotificationConfig> configs = createAllNotificationConfigs();
        configs.get(0).setContactId("contactId");
        when(secondaryContactDao.getContacts(any(), any())).thenReturn(new ArrayList<>());
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getSecondaryContactsConfig(USER_ID, VEHICLE_ID, configs, "kia");
        assertEquals(0, returnedConfigs.size());
    }

    @Test
    public void getSecondaryContactsConfigSuccess() {

        List<NotificationConfig> configs = createAllNotificationConfigs();
        configs.get(0).setContactId("contactId");
        SecondaryContact secondaryContact = createSecondaryContact();
        secondaryContact.setContactId("c2");
        when(secondaryContactDao.getContacts(any(), any())).thenReturn(Collections.singletonList(secondaryContact));
        ReflectionTestUtils.setField(notificationConfigCommonService, "defaultBrand", DEFAULT_BRAND);
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getSecondaryContactsConfig(USER_ID, VEHICLE_ID, configs, "kia");
        assertEquals(1, returnedConfigs.size());
        assertEquals("c2", returnedConfigs.get(0).getContactId());
        assertEquals(USER_ID, returnedConfigs.get(0).getUserId());
    }

    @Test
    public void getSecondaryContactsConfigWithBrandAndLocale() {
        List<NotificationConfig> configs = createAllNotificationConfigs();
        configs.get(0).setContactId("contactId");
        configs.get(Integer.parseInt(two)).setBrand("kia");
        SecondaryContact secondaryContact = createSecondaryContact();
        secondaryContact.setContactId("c2");
        secondaryContact.setLocale(Locale.forLanguageTag("fr-FR"));
        when(secondaryContactDao.getContacts(any(), any())).thenReturn(Collections.singletonList(secondaryContact));
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getSecondaryContactsConfig(USER_ID, VEHICLE_ID, configs, "kia");
        assertEquals(1, returnedConfigs.size());
        assertEquals("c2", returnedConfigs.get(0).getContactId());
        assertEquals(USER_ID, returnedConfigs.get(0).getUserId());
        assertEquals("kia", returnedConfigs.get(0).getBrand());
        assertEquals("fr-FR", returnedConfigs.get(0).getLocale());
    }

    @Test
    public void getSecondaryContactsConfigWithDefaultBrand() {
        List<NotificationConfig> configs = createAllNotificationConfigs();
        configs.get(0).setContactId("contactId");
        configs.get(Integer.parseInt(two)).setUserId(USER_ID);
        SecondaryContact secondaryContact = createSecondaryContact();
        secondaryContact.setLocale(Locale.forLanguageTag("fr-FR"));
        when(secondaryContactDao.getContacts(any(), any())).thenReturn(Collections.singletonList(secondaryContact));
        ReflectionTestUtils.setField(notificationConfigCommonService, "defaultBrand", DEFAULT_BRAND);
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.getSecondaryContactsConfig(USER_ID, VEHICLE_ID, configs, "default");
        assertEquals(0, returnedConfigs.size());
    }

    @Test
    public void selectNotificationConfigNoDefaultConfig() {
        List<NotificationConfig> configs = createAllNotificationConfigs();
        configs.get(Integer.parseInt(two)).setUserId(USER_ID);
        ReflectionTestUtils.setField(notificationConfigCommonService, "defaultBrand", DEFAULT_BRAND);
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, VEHICLE_ID, createUserProfile(),
                "default");
        assertEquals(0, returnedConfigs.size());
    }

    @Test
    public void selectNotificationConfigSuccess() {
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("primary@a.com"));
        emailChannel.setEnabled(true);
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+112345678"));
        smsChannel.setEnabled(false);
        //user config
        List<NotificationConfig> configs = createAllNotificationConfigs();
        configs.get(0).setChannels(Arrays.asList(emailChannel, smsChannel));
        emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("default@a.com"));
        emailChannel.setEnabled(true);
        smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+10000000"));
        smsChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        //default config
        configs.get(Integer.parseInt(two)).setChannels(Arrays.asList(emailChannel, smsChannel, pushChannel));
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, VEHICLE_ID, createUserProfile(),
                "default");
        assertEquals(1, returnedConfigs.size());
        assertEquals("primary@a.com",
            ((EmailChannel) returnedConfigs.get(0).getChannel(ChannelType.EMAIL)).getEmails().get(0));
        assertEquals("+112345678",
            ((SmsChannel) returnedConfigs.get(0).getChannel(ChannelType.SMS)).getPhones().get(0));
        assertFalse(returnedConfigs.get(0).getChannel(ChannelType.SMS).getEnabled());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
    }

    @Test
    public void selectNotificationConfigWithoutVehicle() {
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("primary@a.com"));
        emailChannel.setEnabled(true);
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+112345678"));
        smsChannel.setEnabled(false);
        //user config
        List<NotificationConfig> configs = createAllNotificationConfigs();

        configs.get(1).setChannels(Arrays.asList(emailChannel, smsChannel));
        emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("default@a.com"));
        emailChannel.setEnabled(true);
        smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+10000000"));
        smsChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        //default config
        configs.get(Integer.parseInt(two)).setChannels(Arrays.asList(emailChannel, smsChannel, pushChannel));
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, null, createUserProfile(), "default");
        assertEquals(1, returnedConfigs.size());
        assertEquals("primary@a.com",
            ((EmailChannel) returnedConfigs.get(0).getChannel(ChannelType.EMAIL)).getEmails().get(0));
        assertEquals("+112345678",
            ((SmsChannel) returnedConfigs.get(0).getChannel(ChannelType.SMS)).getPhones().get(0));
        assertFalse(returnedConfigs.get(0).getChannel(ChannelType.SMS).getEnabled());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
    }

    @Test
    public void selectNotificationConfigWithSecondary() {
        NotificationConfig secondaryConfig = createNotificationConfig(USER_ID, VEHICLE_ID);
        secondaryConfig.setContactId("contact1");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+22222222"));
        smsChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(false);
        secondaryConfig.setChannels(Arrays.asList(smsChannel, pushChannel));
        List<NotificationConfig> configs = createAllNotificationConfigs();

        configs.add(secondaryConfig);

        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("primary@a.com"));
        emailChannel.setEnabled(true);
        smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+112345678"));
        smsChannel.setEnabled(false);
        //user config
        configs.get(0).setChannels(Arrays.asList(emailChannel, smsChannel));
        emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        //default config
        configs.get(Integer.parseInt(two)).setChannels(Arrays.asList(emailChannel, smsChannel, pushChannel));

        SecondaryContact secondaryContact = createSecondaryContact();
        secondaryContact.setEmail("sec@b.com");
        secondaryContact.setPhoneNumber("+3333333");
        when(secondaryContactDao.findById(any())).thenReturn(secondaryContact);
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, VEHICLE_ID, createUserProfile(),
                "default");
        assertEquals(Integer.parseInt(two), returnedConfigs.size());
        assertEquals("primary@a.com",
            ((EmailChannel) returnedConfigs.get(0).getChannel(ChannelType.EMAIL)).getEmails().get(0));
        assertEquals("+112345678",
            ((SmsChannel) returnedConfigs.get(0).getChannel(ChannelType.SMS)).getPhones().get(0));
        assertFalse(returnedConfigs.get(0).getChannel(ChannelType.SMS).getEnabled());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
        assertEquals("sec@b.com",
            ((EmailChannel) returnedConfigs.get(1).getChannel(ChannelType.EMAIL)).getEmails().get(0));
        assertEquals("+22222222", ((SmsChannel) returnedConfigs.get(1).getChannel(ChannelType.SMS)).getPhones().get(0));
        assertFalse(returnedConfigs.get(1).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
    }

    @Test
    public void selectNotificationConfigWithEmptySecondary() {
        NotificationConfig secondaryConfig = createNotificationConfig(USER_ID, VEHICLE_ID);
        secondaryConfig.setContactId("contact1");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+22222222"));
        smsChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(false);
        secondaryConfig.setChannels(Arrays.asList(smsChannel, pushChannel));
        List<NotificationConfig> configs = createAllNotificationConfigs();

        configs.add(secondaryConfig);

        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEmails(Collections.singletonList("primary@a.com"));
        emailChannel.setEnabled(true);
        smsChannel = new SmsChannel();
        smsChannel.setPhones(Collections.singletonList("+112345678"));
        smsChannel.setEnabled(false);
        //user config
        configs.get(0).setChannels(Arrays.asList(emailChannel, smsChannel));
        emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        //default config
        configs.get(Integer.parseInt(two)).setChannels(Arrays.asList(emailChannel, smsChannel, pushChannel));

        when(secondaryContactDao.findById(any())).thenReturn(null);
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, VEHICLE_ID, createUserProfile(),
                "default");
        assertEquals(1, returnedConfigs.size());
        assertEquals("primary@a.com",
            ((EmailChannel) returnedConfigs.get(0).getChannel(ChannelType.EMAIL)).getEmails().get(0));
        assertEquals("+112345678",
            ((SmsChannel) returnedConfigs.get(0).getChannel(ChannelType.SMS)).getPhones().get(0));
        assertFalse(returnedConfigs.get(0).getChannel(ChannelType.SMS).getEnabled());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
    }

    @Test
    public void selectNotificationConfigWithoutUserConfig() {
        List<NotificationConfig> configs = createAllNotificationConfigs();
        configs.remove(0);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        //default config
        configs.get(1).setChannels(Arrays.asList(emailChannel, smsChannel, pushChannel));
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, VEHICLE_ID, createUserProfile(),
                "default");
        assertEquals(1, returnedConfigs.size());
        assertEquals(DEFAULT_EMAIL,
            ((EmailChannel) returnedConfigs.get(0).getChannel(ChannelType.EMAIL)).getEmails().get(0));
        assertEquals(DEFAULT_PHONE,
            ((SmsChannel) returnedConfigs.get(0).getChannel(ChannelType.SMS)).getPhones().get(0));
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.SMS).getEnabled());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
    }

    @Test
    public void selectNotificationConfigWithoutUser() {
        ReflectionTestUtils.setField(notificationConfigCommonService, "defaultLocale", "en-US");
        List<NotificationConfig> configs = new ArrayList<>();
        configs.add(createNotificationConfig(USER_ID_FOR_DEFAULT_PREFERENCE, VEHICLE_ID_FOR_DEFAULT_PREFERENCE));
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        //default config
        configs.get(0).setChannels(Arrays.asList(emailChannel, smsChannel, pushChannel));
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, VEHICLE_ID, null, "default");
        assertEquals(1, returnedConfigs.size());
        assertNull(((EmailChannel) returnedConfigs.get(0).getChannel(ChannelType.EMAIL)).getEmails());
        assertNull(((SmsChannel) returnedConfigs.get(0).getChannel(ChannelType.SMS)).getPhones());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.SMS).getEnabled());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
        assertEquals("en-US", returnedConfigs.get(0).getLocale());
    }

    @Test
    public void selectNotificationConfigWithLocaleAndWithoutUser() {
        ReflectionTestUtils.setField(notificationConfigCommonService, "defaultLocale", "en-US");
        List<NotificationConfig> configs = new ArrayList<>();
        configs.add(createNotificationConfig(USER_ID_FOR_DEFAULT_PREFERENCE, VEHICLE_ID_FOR_DEFAULT_PREFERENCE));
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        //default config
        configs.get(0).setChannels(Arrays.asList(emailChannel, smsChannel, pushChannel));
        configs.get(0).setLocale("fr-Fr");
        List<NotificationConfig> returnedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, VEHICLE_ID, null, "default");
        assertEquals(1, returnedConfigs.size());
        assertNull(((EmailChannel) returnedConfigs.get(0).getChannel(ChannelType.EMAIL)).getEmails());
        assertNull(((SmsChannel) returnedConfigs.get(0).getChannel(ChannelType.SMS)).getPhones());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.SMS).getEnabled());
        assertTrue(returnedConfigs.get(0).getChannel(ChannelType.MOBILE_APP_PUSH).getEnabled());
        assertEquals("fr-Fr", returnedConfigs.get(0).getLocale());
    }

    private List<NotificationConfig> createAllNotificationConfigs() {
        List<String> userIds = Arrays.asList(USER_ID, USER_ID, USER_ID_FOR_DEFAULT_PREFERENCE);
        List<String> vehicleIds = Arrays.asList(VEHICLE_ID, VEHICLE_ID_FOR_DEFAULT_PREFERENCE,
            VEHICLE_ID_FOR_DEFAULT_PREFERENCE);
        return createNotificationConfigs(userIds, vehicleIds);
    }

    private List<NotificationConfig> createNotificationConfigs(List<String> userIds, List<String> vehicleIds) {
        List<NotificationConfig> originalConfigs = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            NotificationConfig nc = createNotificationConfig(userIds.get(i), vehicleIds.get(i));
            originalConfigs.add(nc);
        }
        originalConfigs.get(0).setContactId("self");
        return originalConfigs;
    }

    private NotificationConfig createNotificationConfig(String userId, String vehicleId) {
        NotificationConfig nc = new NotificationConfig();
        nc.setChannels(Arrays.asList(new SmsChannel(Arrays.asList("74748484748"))));
        nc.setEnabled(true);
        nc.setGroup("ParentalControls");
        nc.setSchemaVersion(Version.V1_0);
        nc.setUserId(userId);
        nc.setVehicleId(vehicleId);
        nc.setContactId(CONTACT_ID_FOR_DEFAULT_PREFERENCE);
        nc.setBrand(DEFAULT_BRAND);
        return nc;
    }

    private SecondaryContact createSecondaryContact() {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setContactId("contactId");
        secondaryContact.setUserId("userId");
        secondaryContact.setVehicleId("vehicleId");
        return secondaryContact;
    }

    private UserProfile createUserProfile() {
        UserProfile userProfile = new UserProfile(USER_ID);
        userProfile.setDefaultEmail(DEFAULT_EMAIL);
        userProfile.setDefaultPhoneNumber(DEFAULT_PHONE);
        return userProfile;
    }
}