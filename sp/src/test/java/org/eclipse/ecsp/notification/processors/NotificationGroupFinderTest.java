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
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * NotificationGroupFinderTest class.
 */
public class NotificationGroupFinderTest {
    private static final String VEHICLE_ID = "HJKJDHS&78983DJ";
    private static final String USER_ID = "noname001";

    @InjectMocks
    private NotificationGroupFinder notificationGroupFinder;

    @Mock
    private NotificationGroupingDAO notificationGroupingDao;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void noGroupForNotificationId() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.API_PUSH_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        data.setVehicleProfile(new VehicleProfileAbridged(VEHICLE_ID));
        alertsInfo.setAlertsData(data);
        Mockito.when(notificationGroupingDao.findFirstByNotificationId(Mockito.any())).thenReturn(null);

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> notificationGroupFinder.process(alertsInfo));
        assertEquals("Notification grouping not found for notificationId GeofenceIn. Cannot process any further",
            thrown.getMessage());
    }

    @Test
    public void testGroupForNotificationId() {
        IgniteEventImpl alertEvent = createAlertEvent();
        alertEvent.setEventId(EventID.API_PUSH_NOTIFICATION);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(alertEvent);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.setUserProfile(new UserProfile(USER_ID));
        data.setVehicleProfile(new VehicleProfileAbridged(VEHICLE_ID));
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        geofenceGrouping.setCheckAssociation(true);
        geofenceGrouping.setCheckEntitlement(false);
        Mockito.when(notificationGroupingDao.findFirstByNotificationId(Mockito.any())).thenReturn(geofenceGrouping);
        notificationGroupFinder.process(alertsInfo);
        assertNotNull(alertsInfo.getNotificationGrouping());
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
