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
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.eclipse.ecsp.domain.notification.EventMetadata.EventID.DTC_STORED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * NotificationIdGeneratorTest class.
 */
public class NotificationIdGeneratorTest {

    @InjectMocks
    private NotificationIdGenerator notificationIdGenerator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processWithNotificationId() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals("GeofenceIn", alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processDtcStoreSet() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(EventMetadata.DTCAttrs.SET.toString(), "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(DTC_STORED.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals(DTC_STORED.toString() + NotificationUtils.UNDERSCORE + EventMetadata.DTCAttrs.SET.toString(),
            alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processDtcStoreNullSet() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(EventMetadata.DTCAttrs.CLEARED.toString(), "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(DTC_STORED.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals(DTC_STORED.toString() + NotificationUtils.UNDERSCORE + EventMetadata.DTCAttrs.CLEARED.toString(),
            alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processDtcStoreNullSetAndNullClear() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(DTC_STORED.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertNull(alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processGeoFence() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.GEOFENCE.toString());
        alertsInfo.setVersion(EventMetadata.GeoFenceEventVersions.VER1_1.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertNull(alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processCurfew() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.CURFEW.toString());
        alertsInfo.setVersion(EventMetadata.CurfewEventVersions.VER1_1.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals(EventMetadata.EventID.CURFEW.toString() + NotificationUtils.UNDERSCORE + "time",
            alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processDongleNullStatus() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.DONGLE_STATUS.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertNull(alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processDongleEmptyStatus() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(EventMetadata.DongleStatusAttr.STATUS.toString(), "");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.DONGLE_STATUS.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertNull(alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processDongleWithStatus() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(EventMetadata.DongleStatusAttr.STATUS.toString(), "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.DONGLE_STATUS.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals(EventMetadata.EventID.DONGLE_STATUS.toString() + NotificationUtils.UNDERSCORE + "a",
            alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processServiceReminderNullSource() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.SERVICE_REMINDER.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertNull(alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processServiceReminderWithSource() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(EventMetadata.ServiceReminderAttr.SOURCE.toString(), "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.SERVICE_REMINDER.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals(EventMetadata.EventID.SERVICE_REMINDER.toString() + NotificationUtils.UNDERSCORE + "a",
            alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processGenericNotificationEvent() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(NotificationConstants.NOTIFICATION_ID, "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals("a", alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processVehicleMessagePublish() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(NotificationConstants.NOTIFICATION_ID, "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventID.VEHICLE_MESSAGE_PUBLISH);
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals("a", alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processPromotionalNotification() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(NotificationConstants.NOTIFICATION_ID, "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.PROMOTIONAL_NOTIFICATIONS.toString());
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals("a", alertsInfo.getAlertsData().getNotificationId());
    }

    @Test
    public void processDefault() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.set(NotificationConstants.NOTIFICATION_ID, "a");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID("none");
        alertsInfo.setAlertsData(data);

        notificationIdGenerator.process(alertsInfo);
        assertEquals("none", alertsInfo.getAlertsData().getNotificationId());
    }
}