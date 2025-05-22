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

package org.eclipse.ecsp.domain.notification;

import org.junit.Assert;
import org.junit.Test;

/**
 * EventMetadataTest class.
 */
public class EventMetadataTest {

    EventMetadata eventMetadata = new EventMetadata();


    @Test
    public void testEventId() {

        Assert.assertEquals(EventMetadata.EventID.COLLISION, EventMetadata.EventID.COLLISION);
        Assert.assertEquals(EventMetadata.EventID.CURFEW, EventMetadata.EventID.CURFEW);
        Assert.assertEquals(EventMetadata.EventID.GEOFENCE, EventMetadata.EventID.GEOFENCE);
        Assert.assertEquals(EventMetadata.EventID.DTC_STORED, EventMetadata.EventID.DTC_STORED);
        Assert.assertEquals(EventMetadata.EventID.LOW_FUEL, EventMetadata.EventID.LOW_FUEL);
        Assert.assertEquals(EventMetadata.EventID.OVER_SPEED, EventMetadata.EventID.OVER_SPEED);
        Assert.assertEquals(EventMetadata.EventID.IDLING, EventMetadata.EventID.IDLING);
        Assert.assertEquals(EventMetadata.EventID.TOWING, EventMetadata.EventID.TOWING);
        Assert.assertEquals(EventMetadata.EventID.DONGLE_STATUS, EventMetadata.EventID.DONGLE_STATUS);
        Assert.assertEquals(EventMetadata.EventID.SERVICE_REMINDER, EventMetadata.EventID.SERVICE_REMINDER);
        Assert.assertEquals(EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT,
            EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT);
        Assert.assertEquals(EventMetadata.EventID.NOTIFICATION_SETTINGS, EventMetadata.EventID.NOTIFICATION_SETTINGS);
        Assert.assertEquals(EventMetadata.EventID.PROMOTIONAL_NOTIFICATIONS,
            EventMetadata.EventID.PROMOTIONAL_NOTIFICATIONS);

    }

    @Test
    public void testGeoFenceAttrs() {
        Assert.assertEquals(EventMetadata.GeoFenceAttrs.GEOFENCE_ID, EventMetadata.GeoFenceAttrs.GEOFENCE_ID);
        Assert.assertEquals(EventMetadata.GeoFenceAttrs.POSITION, EventMetadata.GeoFenceAttrs.POSITION);
        Assert.assertEquals(EventMetadata.GeoFenceAttrs.LONGITUDE, EventMetadata.GeoFenceAttrs.LONGITUDE);
        Assert.assertEquals(EventMetadata.GeoFenceAttrs.LATITUDE, EventMetadata.GeoFenceAttrs.LATITUDE);
        Assert.assertEquals(EventMetadata.GeoFenceAttrs.ID, EventMetadata.GeoFenceAttrs.ID);
        Assert.assertEquals(EventMetadata.GeoFenceAttrs.GEOFENCE_ID, EventMetadata.GeoFenceAttrs.GEOFENCE_ID);
    }

    @Test
    public void testGeoFencePosition() {
        Assert.assertEquals(EventMetadata.GeoFencePosition.IN, EventMetadata.GeoFencePosition.IN);
        Assert.assertEquals(EventMetadata.GeoFencePosition.OUT, EventMetadata.GeoFencePosition.OUT);
    }

    @Test
    public void testDtcAttrs() {
        Assert.assertEquals(EventMetadata.DTCAttrs.CLEARED, EventMetadata.DTCAttrs.CLEARED);
        Assert.assertEquals(EventMetadata.DTCAttrs.SET, EventMetadata.DTCAttrs.SET);
    }

    @Test
    public void testGeoFenceEventVersions() {
        Assert.assertEquals(EventMetadata.GeoFenceEventVersions.VER1_0, EventMetadata.GeoFenceEventVersions.VER1_0);
        Assert.assertEquals(EventMetadata.GeoFenceEventVersions.VER1_1, EventMetadata.GeoFenceEventVersions.VER1_1);
        EventMetadata.GeoFenceEventVersions.getVersion("1.0");
    }

    @Test
    public void testCurfewEventVersions() {
        Assert.assertEquals(EventMetadata.CurfewEventVersions.VER1_0, EventMetadata.CurfewEventVersions.VER1_0);
        Assert.assertEquals(EventMetadata.CurfewEventVersions.VER1_1, EventMetadata.CurfewEventVersions.VER1_1);
        EventMetadata.CurfewEventVersions.getVersion("1.1");
    }

    @Test
    public void testDongleStatus() {
        Assert.assertEquals(EventMetadata.DongleStatus.ATTACHED, EventMetadata.DongleStatus.ATTACHED);
        Assert.assertEquals(EventMetadata.DongleStatus.DETACHED, EventMetadata.DongleStatus.DETACHED);
    }

    @Test
    public void testDongleStatusAttr() {
        Assert.assertEquals(EventMetadata.DongleStatusAttr.STATUS, EventMetadata.DongleStatusAttr.STATUS);
    }

    @Test
    public void testGeoFenceTypes() {
        Assert.assertEquals(EventMetadata.GeoFenceTypes.GENERIC, EventMetadata.GeoFenceTypes.GENERIC);
        Assert.assertEquals(EventMetadata.GeoFenceTypes.VALET, EventMetadata.GeoFenceTypes.VALET);
        Assert.assertEquals(EventMetadata.GeoFenceTypes.PRIVATE_LOC, EventMetadata.GeoFenceTypes.PRIVATE_LOC);
        EventMetadata.GeoFenceTypes.isSupportedType("valet");
    }

    @Test
    public void testServiceReminderAttr() {
        Assert.assertEquals(EventMetadata.ServiceReminderAttr.SOURCE, EventMetadata.ServiceReminderAttr.SOURCE);
    }

    @Test
    public void testCollisionDataFieldNames() {
        Assert.assertEquals(EventMetadata.CollisionDataFieldNames.LONGITUDE,
            EventMetadata.CollisionDataFieldNames.LONGITUDE);
        Assert.assertEquals(EventMetadata.CollisionDataFieldNames.LATITUDE,
            EventMetadata.CollisionDataFieldNames.LATITUDE);
        Assert.assertEquals(EventMetadata.CollisionDataFieldNames.SPEED, EventMetadata.CollisionDataFieldNames.SPEED);
    }
}
