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

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.userprofile.UserProfileIntegrationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for UserProfileEnricher. All the test cases for
 * UserProfileEnricher should also be executed here.
 */
public class UserProfileEnricherTest {

    @InjectMocks
    private UserProfileEnricher userProfileEnricher;

    @Mock
    private UserProfileDAO userProfileDao;

    @Mock
    private UserProfileIntegrationService userService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcess() {
        userProfileEnricher.setIgniteVehicleProfile(true);

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        alertsInfo.setAlertsData(data);

        UserProfile retUserProfile = new UserProfile();
        retUserProfile.setUserId("testUser");
        retUserProfile.setFirstName("vishnu");
        retUserProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        retUserProfile.setDefaultEmail("testUser@harman.com");

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(
            retUserProfile);

        userProfileEnricher.process(alertsInfo);

        assertEquals(alertsInfo.getAlertsData().getUserProfile().getFirstName(), retUserProfile.getFirstName());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getNickName("Vehicle123"),
            retUserProfile.getNickName("Vehicle123"));
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail(), retUserProfile.getDefaultEmail());
        assertNotEquals("MARUTI", alertsInfo.getAlertsData().getVehicleProfile().getName());
    }

    @Test
    public void testProcessWithInactiveIgniteVehicleProfile() {
        userProfileEnricher.setIgniteVehicleProfile(false);

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        alertsInfo.setAlertsData(data);

        UserProfile retUserProfile = new UserProfile();
        retUserProfile.setUserId("testUser");
        retUserProfile.setFirstName("vishnu");
        retUserProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        retUserProfile.setDefaultEmail("testUser@harman.com");

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(
            retUserProfile);

        userProfileEnricher.process(alertsInfo);

        assertEquals(alertsInfo.getAlertsData().getUserProfile().getFirstName(), retUserProfile.getFirstName());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getNickName("Vehicle123"),
            retUserProfile.getNickName("Vehicle123"));
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail(), retUserProfile.getDefaultEmail());
        assertEquals("MARUTI", alertsInfo.getAlertsData().getVehicleProfile().getName());
    }

    @Test
    public void testProcessWithUserIdInData() {

        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("testUser");
        data.setUserProfile(userProfile);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setAlertsData(data);
        alertsInfo.setEventID(EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT.toString());
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        GenericEventData genericEventData = new GenericEventData();
        igniteEvent.setEventData(genericEventData);
        alertsInfo.setIgniteEvent(igniteEvent);

        UserProfile retUserProfile = new UserProfile();
        retUserProfile.setUserId("testUser");
        retUserProfile.setFirstName("vishnu");
        retUserProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        retUserProfile.setDefaultEmail("testUser@harman.com");

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(
            retUserProfile);

        userProfileEnricher.process(alertsInfo);

        assertTrue(((GenericEventData) alertsInfo.getIgniteEvent().getEventData()).getData(NotificationConstants.USERID)
            .isPresent());
        assertEquals("testUser",
            ((GenericEventData) alertsInfo.getIgniteEvent().getEventData()).getData(NotificationConstants.USERID).get()
                .toString());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getFirstName(), retUserProfile.getFirstName());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getNickName("Vehicle123"),
            retUserProfile.getNickName("Vehicle123"));
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail(), retUserProfile.getDefaultEmail());

    }

    @Test
    public void testProcessWithoutUserProfile() {


        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);


        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);
        UserProfile userProf = new UserProfile();

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(userProf);

        userProfileEnricher.process(alertsInfo);
        alertsInfo.getAlertsData().getUserProfile().setNickNames(new HashSet<>());

        assertNull(alertsInfo.getAlertsData().getUserProfile().getFirstName());
        assertTrue(alertsInfo.getAlertsData().getUserProfile().getNickNames().isEmpty());
        assertNull(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail());
    }

    @Test
    public void testProcessUser() {

        userProfileEnricher.setUserProfileExcludedNotifications("dummy");

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        data.setNotificationId("lowFuel");
        alertsInfo.setAlertsData(data);

        UserProfile retUserProfile = new UserProfile();
        retUserProfile.setUserId("testUser");
        retUserProfile.setFirstName("vishnu");
        retUserProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        retUserProfile.setDefaultEmail("testUser@harman.com");

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(null);
        when(userService.processRealTimeUserUpdate(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean()))
            .thenReturn(retUserProfile);
        userProfileEnricher.process(alertsInfo);

        assertEquals(alertsInfo.getAlertsData().getUserProfile().getFirstName(), retUserProfile.getFirstName());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getNickName("Vehicle123"),
            retUserProfile.getNickName("Vehicle123"));
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail(), retUserProfile.getDefaultEmail());
    }

    @Test
    public void testProcessUserWithRefreshUserFlag() {

        userProfileEnricher.setUserProfileExcludedNotifications("dummy");

        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        data.setNotificationId("lowFuel");
        data.set(NotificationConstants.REFRESH_USER, true);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setAlertsData(data);

        UserProfile retUserProfile = new UserProfile();
        retUserProfile.setUserId("testUser");
        retUserProfile.setFirstName("vishnu");
        retUserProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        retUserProfile.setDefaultEmail("testUser@harman.com");

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(
            new UserProfile());
        when(userService.processRealTimeUserUpdate(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean()))
            .thenReturn(retUserProfile);
        userProfileEnricher.process(alertsInfo);

        verify(userService, times(1)).processRealTimeUserUpdate(Mockito.anyString(), Mockito.any(),
            Mockito.anyBoolean());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getFirstName(), retUserProfile.getFirstName());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getNickName("Vehicle123"),
            retUserProfile.getNickName("Vehicle123"));
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail(), retUserProfile.getDefaultEmail());
    }

    @Test
    public void testProcessUserNull() {

        userProfileEnricher.setUserProfileExcludedNotifications("dummy");

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        data.setNotificationId("lowFuel");
        alertsInfo.setAlertsData(data);

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(Mockito.any())).thenReturn(null);
        userProfileEnricher.process(alertsInfo);
        assertNull(alertsInfo.getAlertsData().getUserProfile());
    }

    @Test
    public void testProcessUserNotificationFalse() {

        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, false);
        data.setAlertDataProperties(alertDataProperties);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setAlertsData(data);

        UserProfile retUserProfile = new UserProfile();
        retUserProfile.setUserId("testUser");
        retUserProfile.setFirstName("vishnu");
        retUserProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        retUserProfile.setDefaultEmail("testUser@harman.com");

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(
            retUserProfile);

        userProfileEnricher.process(alertsInfo);

        assertEquals(alertsInfo.getAlertsData().getUserProfile().getFirstName(), retUserProfile.getFirstName());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getNickName("Vehicle123"),
            retUserProfile.getNickName("Vehicle123"));
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail(), retUserProfile.getDefaultEmail());
        assertNotNull(alertsInfo.getAlertsData().getVehicleProfile().getName());

    }

    @Test
    public void testProcessUserNotificationTrue() {

        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        Map<String, Object> alertDataProperties = new HashMap<>();
        alertDataProperties.put(NotificationConstants.USER_NOTIFICATION, true);
        data.setAlertDataProperties(alertDataProperties);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setAlertsData(data);

        UserProfile retUserProfile = new UserProfile();
        retUserProfile.setUserId("testUser");
        retUserProfile.setFirstName("vishnu");
        retUserProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        retUserProfile.setDefaultEmail("testUser@harman.com");

        VehicleProfileAbridged vehicleProfile = new VehicleProfileAbridged();
        vehicleProfile.setVehicleId("Vehicle123");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "Vehicle123");
        attrs.put("model", "Model123");
        attrs.put("userId", "testUser");
        vehicleProfile.setVehicleAttributes(attrs);
        alertsInfo.getAlertsData().setVehicleProfile(vehicleProfile);

        when(userProfileDao.findById(alertsInfo.getAlertsData().getVehicleProfile().getUserId())).thenReturn(
            retUserProfile);

        userProfileEnricher.process(alertsInfo);

        assertEquals(alertsInfo.getAlertsData().getUserProfile().getFirstName(), retUserProfile.getFirstName());
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getNickName("Vehicle123"),
            retUserProfile.getNickName("Vehicle123"));
        assertEquals(alertsInfo.getAlertsData().getUserProfile().getDefaultEmail(), retUserProfile.getDefaultEmail());
        assertNull(alertsInfo.getAlertsData().getVehicleProfile().getName());

    }
}
