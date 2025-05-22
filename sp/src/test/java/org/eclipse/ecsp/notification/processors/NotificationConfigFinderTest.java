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

package org.eclipse.ecsp.notification.processors;

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.utils.NotificationConfigCommonService;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.GENERAL;
import static org.eclipse.ecsp.notification.config.NotificationConfig.CONTACT_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.USER_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

/**
 * NotificationConfigFinderTest class.
 */
public class NotificationConfigFinderTest {

    private static final String VEHICLE_ID = "HJKJDHS&78983DJ";
    private static final String USER_ID = "noname001";
    public static final String DEFAULT_BRAND = "default";

    @InjectMocks
    private NotificationConfigFinder notificationConfigFinder;

    @Mock
    private NotificationGroupingDAO notificationGroupingDao;

    @Mock
    private NotificationConfigCommonService notificationConfigCommonService;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(notificationConfigFinder, "defaultBrand", DEFAULT_BRAND);
    }

    @Test
    public void pinGeneratedEvent() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.PIN_GENERATED);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        data.setVehicleProfile(new VehicleProfileAbridged(VEHICLE_ID));
        alertsInfo.setAlertsData(data);
        notificationConfigFinder.process(alertsInfo);
        Assert.assertTrue(CollectionUtils.isEmpty(alertsInfo.getNotificationConfigs()));
    }

    @Test
    public void noConfigForNotificationId() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.API_PUSH_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        VehicleProfileAbridged vp = new VehicleProfileAbridged(VEHICLE_ID);
        vp.setVehicleAttributes(new HashMap<>());
        data.setVehicleProfile(vp);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        Mockito.when(notificationConfigCommonService.getAllConfigsFromDbByGroup(any(), any(), any()))
            .thenReturn(new ArrayList<>());

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> notificationConfigFinder.process(alertsInfo));
        assertEquals(
            String.format("No notification config found for userId %s vehicleId %s serviceName %s", USER_ID, VEHICLE_ID,
                "ParentalControls"), thrown.getMessage());
    }

    @Test
    public void noSelectedConfigForNotificationId() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.API_PUSH_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        VehicleProfileAbridged vp = new VehicleProfileAbridged(VEHICLE_ID);
        vp.setVehicleAttributes(new HashMap<>());
        data.setVehicleProfile(vp);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        List<NotificationConfig> configs = createAllNotificationConfigs();
        Mockito.when(notificationConfigCommonService.getAllConfigsFromDbByGroup(any(), any(), any()))
            .thenReturn(configs);
        NotificationConfig nc = createNotificationConfig(USER_ID, VEHICLE_ID);
        nc.setContactId("contactId");
        Mockito.when(notificationConfigCommonService.getSecondaryContactsConfig(any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(nc));
        Mockito.when(notificationConfigCommonService.selectNotificationConfig(any(), any(), any(), any()))
            .thenReturn(new ArrayList<>());

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> notificationConfigFinder.process(alertsInfo));
        assertEquals(
            String.format("Could not find a notification config for userId %s vehicleId %s serviceName %s", USER_ID,
                VEHICLE_ID,
                "ParentalControls"), thrown.getMessage());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processSuccess() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.API_PUSH_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        data.setVehicleProfile(new VehicleProfileAbridged(VEHICLE_ID));
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "kia");
        data.getVehicleProfile().setVehicleAttributes(attrs);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        List<NotificationConfig> configs = createAllNotificationConfigs();
        Mockito.when(notificationConfigCommonService.getAllConfigsFromDbByGroup(any(), any(), any()))
            .thenReturn(configs);
        NotificationConfig nc = createNotificationConfig(USER_ID, VEHICLE_ID);
        nc.setContactId("contactId");
        Mockito.when(
                notificationConfigCommonService.getSecondaryContactsConfig(any(), any(), any(), stringCaptor.capture()))
            .thenReturn(Collections.singletonList(nc));
        NotificationConfig nc1 = createNotificationConfig(USER_ID, VEHICLE_ID);
        Mockito.when(notificationConfigCommonService.selectNotificationConfig(any(), any(), any(), any()))
            .thenReturn(Arrays.asList(nc1, nc));
        notificationConfigFinder.process(alertsInfo);

        assertEquals("kia", stringCaptor.getValue());

        assertEquals(2, alertsInfo.getNotificationConfigs().size());

        assertEquals(nc1, alertsInfo.getNotificationConfigs().get(0));
        assertEquals(nc1.getChannels(), alertsInfo.getNotificationConfigs().get(0).getChannels());
        assertEquals(nc1.getGroup(), alertsInfo.getNotificationConfigs().get(0).getGroup());
        assertEquals(USER_ID, alertsInfo.getNotificationConfigs().get(0).getUserId());
        assertEquals(VEHICLE_ID, alertsInfo.getNotificationConfigs().get(0).getVehicleId());
        assertEquals(nc1.getSchemaVersion(), alertsInfo.getNotificationConfigs().get(0).getSchemaVersion());

        assertEquals(nc, alertsInfo.getNotificationConfigs().get(1));
        assertEquals(nc.getChannels(), alertsInfo.getNotificationConfigs().get(1).getChannels());
        assertEquals(nc.getGroup(), alertsInfo.getNotificationConfigs().get(1).getGroup());
        assertEquals(USER_ID, alertsInfo.getNotificationConfigs().get(1).getUserId());
        assertEquals(VEHICLE_ID, alertsInfo.getNotificationConfigs().get(1).getVehicleId());
        assertEquals(nc.getSchemaVersion(), alertsInfo.getNotificationConfigs().get(1).getSchemaVersion());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processWithoutUser() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.API_PUSH_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(null);
        data.setVehicleProfile(new VehicleProfileAbridged(VEHICLE_ID));
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "kia");
        data.getVehicleProfile().setVehicleAttributes(attrs);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        List<NotificationConfig> configs = new ArrayList<>();
        configs.add(createNotificationConfig(USER_ID_FOR_DEFAULT_PREFERENCE, VEHICLE_ID_FOR_DEFAULT_PREFERENCE));
        Mockito.when(notificationConfigCommonService.getAllConfigsFromDbByGroup(any(), any(), any()))
            .thenReturn(configs);
        Mockito.when(
                notificationConfigCommonService.getSecondaryContactsConfig(any(), any(), any(), stringCaptor.capture()))
            .thenReturn(new ArrayList<>());
        Mockito.when(notificationConfigCommonService.selectNotificationConfig(any(), any(), any(), any()))
            .thenReturn(configs);
        notificationConfigFinder.process(alertsInfo);

        assertEquals("kia", stringCaptor.getValue());

        assertEquals(1, alertsInfo.getNotificationConfigs().size());
        assertEquals(USER_ID_FOR_DEFAULT_PREFERENCE, alertsInfo.getNotificationConfigs().get(0).getUserId());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processIvmReq() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.VEHICLE_MESSAGE_PUBLISH);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("S06_IVM_OIL_NOTIFICATION");
        data.setUserProfile(null);
        data.setVehicleProfile(new VehicleProfileAbridged(VEHICLE_ID));
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "kia");
        data.getVehicleProfile().setVehicleAttributes(attrs);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("S06_IVM_OIL_NOTIFICATION", "IVM_SERVICE");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        List<NotificationConfig> configs = new ArrayList<>();
        configs.add(createNotificationConfig(USER_ID_FOR_DEFAULT_PREFERENCE, VEHICLE_ID_FOR_DEFAULT_PREFERENCE));
        Mockito.when(notificationConfigCommonService.getAllConfigsFromDbByGroup(any(), any(), any()))
            .thenReturn(configs);
        Mockito.when(
                notificationConfigCommonService.getSecondaryContactsConfig(any(), any(), any(), stringCaptor.capture()))
            .thenReturn(new ArrayList<>());
        Mockito.when(notificationConfigCommonService.selectNotificationConfig(any(), any(), any(), any()))
            .thenReturn(configs);
        notificationConfigFinder.process(alertsInfo);


        assertEquals(1, alertsInfo.getNotificationConfigs().size());
        assertEquals(USER_ID_FOR_DEFAULT_PREFERENCE, alertsInfo.getNotificationConfigs().get(0).getUserId());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processDynamicNotification() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.DYNAMIC_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        VehicleProfileAbridged vp = new VehicleProfileAbridged(VEHICLE_ID);
        vp.setVehicleAttributes(new HashMap<>());
        data.setVehicleProfile(vp);
        data.set("channelType", "sms");
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        List<NotificationConfig> configs = createAllNotificationConfigs();
        Mockito.when(notificationConfigCommonService.getAllConfigsFromDbByGroup(any(), any(), any()))
            .thenReturn(configs);
        NotificationConfig nc = createNotificationConfig(USER_ID, VEHICLE_ID);
        nc.setContactId("contactId");
        Mockito.when(notificationConfigCommonService.getSecondaryContactsConfig(any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(nc));
        NotificationConfig nc1 = createNotificationConfig(USER_ID, VEHICLE_ID);
        nc1.setUserId(GENERAL);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        emailChannel.setEmails(Collections.singletonList("test1@2.com"));
        ArrayList<Channel> channels = new ArrayList<>();
        channels.add(emailChannel);
        nc1.getChannel(ChannelType.SMS).setEnabled(true);
        channels.add(nc1.getChannel(ChannelType.SMS));
        nc1.setChannels(channels);
        Mockito.when(notificationConfigCommonService.selectNotificationConfig(any(), any(), any(), any()))
            .thenReturn(Arrays.asList(nc1, nc));
        notificationConfigFinder.process(alertsInfo);

        assertEquals(2, alertsInfo.getNotificationConfigs().size());

        NotificationConfig selectedConfig = alertsInfo.getNotificationConfigs().get(0);
        Assert.assertTrue(selectedConfig.getChannel(ChannelType.SMS).getEnabled());
        Assert.assertFalse(selectedConfig.getChannel(ChannelType.EMAIL).getEnabled());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void processDynamicNotificationNoChannels() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.DYNAMIC_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        // data.setVehicleProfile(new VehicleProfileAbridged(VEHICLE_ID));
        data.set("channelType", "invalid");
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        List<NotificationConfig> configs = createAllNotificationConfigs();
        Mockito.when(notificationConfigCommonService.getAllConfigsFromDbByGroup(any(), stringCaptor.capture(), any()))
            .thenReturn(configs);
        NotificationConfig nc = createNotificationConfig(USER_ID, VEHICLE_ID);
        nc.setContactId("contactId");
        Mockito.when(notificationConfigCommonService.getSecondaryContactsConfig(any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(nc));
        NotificationConfig nc1 = createNotificationConfig(USER_ID, VEHICLE_ID);
        nc1.setUserId(GENERAL);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        emailChannel.setEmails(Collections.singletonList("test1@2.com"));
        ArrayList<Channel> channels = new ArrayList<>();
        channels.add(emailChannel);
        nc1.getChannel(ChannelType.SMS).setEnabled(true);
        channels.add(nc1.getChannel(ChannelType.SMS));
        nc1.setChannels(channels);
        Mockito.when(notificationConfigCommonService.selectNotificationConfig(any(), any(), any(), any()))
            .thenReturn(Arrays.asList(nc1, nc));
        notificationConfigFinder.process(alertsInfo);

        assertNull(stringCaptor.getValue());

        assertEquals(2, alertsInfo.getNotificationConfigs().size());

        NotificationConfig selectedConfig = alertsInfo.getNotificationConfigs().get(0);
        Assert.assertTrue(selectedConfig.getChannel(ChannelType.SMS).getEnabled());
        Assert.assertTrue(selectedConfig.getChannel(ChannelType.EMAIL).getEnabled());
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
        if (USER_ID_FOR_DEFAULT_PREFERENCE.equals(userId)) {
            nc.setBrand(DEFAULT_BRAND);
        }
        return nc;
    }

    private IgniteEventImpl createAlertEvent() {
        IgniteEventImpl alertEvent = new IgniteEventImpl();
        alertEvent.setMessageId("1234");
        alertEvent.setBizTransactionId("1234");
        alertEvent.setRequestId("1234");
        alertEvent.setTimestamp(System.currentTimeMillis());
        return alertEvent;
    }
}
