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

package org.eclipse.ecsp.notification.config;

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.IVMChannel;
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.dao.NotificationSettingsInfoDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * NotificationConfigControlServiceTest.
 */
public class NotificationConfigControlServiceTest {
    @Mock
    private NotificationConfigDAO notificationConfigDao;
    @Mock
    private NotificationSettingsInfoDAO settingsInfoDao;
    @Mock
    private UserProfileDAO userProfileDao;
    private NotificationConfigControlService controlService;

    private String userId = "noname001";
    private String vehicleId = "JHBDSJDHBJSHJDS";
    private String contactId = "dummyContactId";

    /**
     * set up.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        controlService = new NotificationConfigControlService();
        controlService.setNotificationConfigDao(notificationConfigDao);
        controlService.setNotificationSettingsInfoDao(settingsInfoDao);
    }

    // Test that when group exists and channels exist, new channels are added
    // and existing are replaced
    @Test
    public void testWhenGroupExistsAndSameChannelExistsWithSameContacts() {
        NotificationSettingDataV1_0 configData = new NotificationSettingDataV1_0();
        NotificationConfig nc1 = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724")));

        List<NotificationConfig> configs = new ArrayList<>();
        configs.add(nc1);
        configData.setNotificationConfigs(configs);

        // setup db to have same but with enabled false
        NotificationConfig ncdb = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724")));
        ncdb.setEnabled(false);
        List<NotificationConfig> configsInDb = new ArrayList<>();
        configsInDb.add(ncdb);
        IgniteEventImpl event = createNotificationSettingsEvent(configData);
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();
        Mockito.when(notificationConfigDao.findByUserVehicleContactId(userId, vehicleId, contactId))
            .thenReturn(configsInDb);
        Mockito.when(settingsInfoDao.save(settingsInfo)).thenReturn(settingsInfo);

        controlService.patchUpdateConfig(event);
        ArgumentCaptor<NotificationConfig> eventCaptor = ArgumentCaptor.forClass(NotificationConfig.class);
        // Its a replace at channel level, so we expect an update call
        // nevertheless
        Mockito.verify(notificationConfigDao, Mockito.times(1)).update(eventCaptor.capture());
        NotificationConfig updatedConfig = eventCaptor.getValue();
        Assert.assertEquals(nc1.getId(), updatedConfig.getId());
        Assert.assertTrue(NotificationConfig.isSimilar(nc1, updatedConfig));
        Assert.assertEquals(nc1.isEnabled(), updatedConfig.isEnabled());
        Assert.assertEquals(nc1.getChannels(), updatedConfig.getChannels());
    }

    // when contact exists but has different contacts, then new set of contacts
    // should replace existing
    @Test
    public void testWhenGroupExistsAndSameChannelExistsWithDifferentContacts() {
        NotificationSettingDataV1_0 configData = new NotificationSettingDataV1_0();
        NotificationConfig nc1 = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724", "6264234623874")));

        List<NotificationConfig> configs = new ArrayList<>();
        configs.add(nc1);
        configData.setNotificationConfigs(configs);

        // setup db to have one more contact for same channel
        NotificationConfig ncdb = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724")));
        List<NotificationConfig> configsInDb = new ArrayList<>();
        configsInDb.add(ncdb);
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();
        IgniteEventImpl event = createNotificationSettingsEvent(configData);
        Mockito.when(notificationConfigDao.findByUserVehicleContactId(userId, vehicleId, contactId))
            .thenReturn(configsInDb);
        Mockito.when(settingsInfoDao.save(settingsInfo)).thenReturn(settingsInfo);
        controlService.patchUpdateConfig(event);
        ArgumentCaptor<NotificationConfig> eventCaptor = ArgumentCaptor.forClass(NotificationConfig.class);
        // Its a replace at channel level
        Mockito.verify(notificationConfigDao, Mockito.times(1)).update(eventCaptor.capture());
        NotificationConfig updatedConfig = eventCaptor.getValue();
        Assert.assertEquals(nc1.getId(), updatedConfig.getId());
        Assert.assertTrue(NotificationConfig.isSimilar(nc1, updatedConfig));
        // contacts replaced completely with new contacts for the
        // channel
        Assert.assertEquals(nc1.getChannels(), updatedConfig.getChannels());
    }

    // when existing channel is mentioned and new channel is also there in patch
    // request then existing would be replaced and new should be included
    @Test
    public void testWhenGroupExistsAndSameChannelExistsAndNewChannelsExist() {
        NotificationSettingDataV1_0 configData = new NotificationSettingDataV1_0();
        NotificationConfig nc1 = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724"), new IVMChannel()));
        List<NotificationConfig> configs = new ArrayList<>();

        configs.add(nc1);
        configData.setNotificationConfigs(configs);

        // setup db to have one channel less
        NotificationConfig ncdb = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724")));
        List<NotificationConfig> configsInDb = new ArrayList<>();
        configsInDb.add(ncdb);
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();
        IgniteEventImpl event = createNotificationSettingsEvent(configData);
        Mockito.when(notificationConfigDao.findByUserVehicleContactId(userId, vehicleId, contactId))
            .thenReturn(configsInDb);
        Mockito.when(settingsInfoDao.save(settingsInfo)).thenReturn(settingsInfo);
        controlService.patchUpdateConfig(event);
        ArgumentCaptor<NotificationConfig> eventCaptor = ArgumentCaptor.forClass(NotificationConfig.class);
        Mockito.verify(notificationConfigDao, Mockito.times(1)).update(eventCaptor.capture());
        NotificationConfig updatedConfig = eventCaptor.getValue();
        Assert.assertEquals(nc1.getId(), updatedConfig.getId());
        Assert.assertTrue(NotificationConfig.isSimilar(nc1, updatedConfig));
        // contacts replaced completely with new contacts for the
        // channel
        Assert.assertEquals(nc1.getChannels(), updatedConfig.getChannels());
    }

    @Test(expected = Exception.class)
    public void testSmsEnabledSelf() {
        NotificationSettingDataV1_0 configData = new NotificationSettingDataV1_0();
        SmsChannel sms = new SmsChannel();
        sms.setEnabled(true);
        NotificationConfig nc1 = createNotificationConfig(userId, vehicleId, "self", "ParentalControls",
            createChannels(sms, new IVMChannel()));
        List<NotificationConfig> configs = new ArrayList<>();

        configs.add(nc1);
        configData.setNotificationConfigs(configs);

        // setup db to have one channel less
        NotificationConfig ncdb = createNotificationConfig(userId, vehicleId, "self", "ParentalControls",
            createChannels(sms));
        List<NotificationConfig> configsInDb = new ArrayList<>();
        configsInDb.add(ncdb);
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();

        Mockito.when(notificationConfigDao.findByUserVehicleContactId(userId, vehicleId, "self"))
            .thenReturn(configsInDb);
        Mockito.when(settingsInfoDao.save(settingsInfo)).thenReturn(settingsInfo);
        Mockito.when(userProfileDao.findById(userId)).thenReturn(null);
        IgniteEventImpl event = createNotificationSettingsEvent(configData);
        controlService.patchUpdateConfig(event);
        ArgumentCaptor<NotificationConfig> eventCaptor = ArgumentCaptor.forClass(NotificationConfig.class);
        Mockito.verify(notificationConfigDao, Mockito.times(1)).update(eventCaptor.capture());
        NotificationConfig updatedConfig = eventCaptor.getValue();
        Assert.assertEquals(nc1.getId(), updatedConfig.getId());
        Assert.assertTrue(NotificationConfig.isSimilar(nc1, updatedConfig));
        // contacts replaced completely with new contacts for the
        // channel
        Assert.assertEquals(nc1.getChannels(), updatedConfig.getChannels());
    }

    @Test(expected = Exception.class)
    public void testSmsEnabled() {
        NotificationSettingDataV1_0 configData = new NotificationSettingDataV1_0();
        SmsChannel sms = new SmsChannel("3437634724");
        sms.setEnabled(true);
        NotificationConfig nc1 = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(sms, new IVMChannel()));
        List<NotificationConfig> configs = new ArrayList<>();

        configs.add(nc1);
        configData.setNotificationConfigs(configs);

        // setup db to have one channel less
        NotificationConfig ncdb = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(sms));
        List<NotificationConfig> configsInDb = new ArrayList<>();
        configsInDb.add(ncdb);
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();
        Mockito.when(notificationConfigDao.findByUserVehicleContactId(userId, vehicleId, contactId))
            .thenReturn(configsInDb);
        Mockito.when(settingsInfoDao.save(settingsInfo)).thenReturn(settingsInfo);
        Mockito.when(userProfileDao.findById(userId)).thenReturn(null);
        IgniteEventImpl event = createNotificationSettingsEvent(configData);
        controlService.patchUpdateConfig(event);
        ArgumentCaptor<NotificationConfig> eventCaptor = ArgumentCaptor.forClass(NotificationConfig.class);
        Mockito.verify(notificationConfigDao, Mockito.times(1)).update(eventCaptor.capture());
        NotificationConfig updatedConfig = eventCaptor.getValue();
        Assert.assertEquals(nc1.getId(), updatedConfig.getId());
        Assert.assertTrue(NotificationConfig.isSimilar(nc1, updatedConfig));
        // contacts replaced completely with new contacts for the
        // channel
        Assert.assertEquals(nc1.getChannels(), updatedConfig.getChannels());
    }

    // when existing channel is not mentioned in patch request then it should be
    // retained
    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void testWhenGroupExistsAndDifferentChannelsExist() {
        NotificationSettingDataV1_0 configData = new NotificationSettingDataV1_0();
        NotificationConfig nc1 = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new IVMChannel()));
        List<NotificationConfig> configs = new ArrayList<>();
        configs.add(nc1);
        configData.setNotificationConfigs(configs);
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();

        // setup db to have one channel less
        NotificationConfig ncdb = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724")));
        List<NotificationConfig> configsInDb = new ArrayList<>();
        configsInDb.add(ncdb);
        Mockito.when(notificationConfigDao.findByUserVehicleContactId(userId, vehicleId, contactId))
            .thenReturn(configsInDb);
        Mockito.when(settingsInfoDao.save(settingsInfo)).thenReturn(settingsInfo);
        IgniteEventImpl event = createNotificationSettingsEvent(configData);
        controlService.patchUpdateConfig(event);
        ArgumentCaptor<NotificationConfig> eventCaptor = ArgumentCaptor.forClass(NotificationConfig.class);
        Mockito.verify(notificationConfigDao, Mockito.times(1)).update(eventCaptor.capture());
        NotificationConfig updatedConfig = eventCaptor.getValue();
        Assert.assertEquals(nc1.getId(), updatedConfig.getId());
        Assert.assertTrue(NotificationConfig.isSimilar(nc1, updatedConfig));
        // existing channel would be replaced and new would be added
        Assert.assertEquals(2, updatedConfig.getChannels().size());
    }

    // Test that config for a new group gets added
    @Test
    public void testWhenNewConfigForGroup() {
        NotificationSettingDataV1_0 configData = new NotificationSettingDataV1_0();
        NotificationConfig nc1 = createNotificationConfig(userId, vehicleId, contactId, "ParentalControls",
            createChannels(new SmsChannel("3437634724")));
        configData.setNotificationConfigs(Collections.singletonList(nc1));
        IgniteEventImpl event = createNotificationSettingsEvent(configData);
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();
        // return empty from DB
        Mockito.when(notificationConfigDao.findByUserVehicle(userId, vehicleId)).thenReturn(new ArrayList<>());
        Mockito.when(settingsInfoDao.save(settingsInfo)).thenReturn(settingsInfo);
        controlService.patchUpdateConfig(event);
        Mockito.verify(notificationConfigDao, Mockito.times(1)).save(nc1);
    }

    private IgniteEventImpl createNotificationSettingsEvent(NotificationSettingDataV1_0 configData) {
        IgniteEventImpl event = new IgniteEventImpl();
        event.setEventData(configData);
        event.setEventId(org.eclipse.ecsp.domain.notification.commons.EventID.NOTIFICATION_SETTINGS);
        event.setVersion(Version.V1_0);
        return event;
    }

    private NotificationConfig createNotificationConfig(String userId, String vehicleId, String contactId, String group,
                                                        List<Channel> channels) {
        NotificationConfig nc = new NotificationConfig();
        nc.setChannels(channels);
        nc.setEnabled(true);
        nc.setGroup(group);
        nc.setSchemaVersion(Version.V1_0);
        nc.setUserId(userId);
        nc.setVehicleId(vehicleId);
        nc.setContactId(contactId);
        return nc;
    }

    private List<Channel> createChannels(Channel... chArray) {
        return new ArrayList<>(Arrays.asList(chArray));
    }
}
