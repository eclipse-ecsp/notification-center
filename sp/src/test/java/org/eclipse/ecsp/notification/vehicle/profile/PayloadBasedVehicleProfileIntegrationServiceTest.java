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

package org.eclipse.ecsp.notification.vehicle.profile;

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * PayloadBasedVehicleProfileIntegrationServiceTest.
 */
public class PayloadBasedVehicleProfileIntegrationServiceTest {

    @InjectMocks
    private PayloadBasedVehicleProfileIntegrationService vpService;

    @Mock
    private IgniteEvent event;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessNonRegisteredVehicle() {

        vpService.getName();


        String userId = "testUser01";

        Data data = new Data();
        data.set("id", "geofenceId");
        data.set(NotificationConstants.USERID, userId);
        data.set(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD, true);

        IgniteEventImpl im = new IgniteEventImpl();

        String vehicleId = "MAR_1234";
        im.setVehicleId(vehicleId);
        Map<String, Object> vpAttr = new HashMap<>();
        vpAttr.put("make", "Deep");
        vpAttr.put("model", "Chroke");
        vpAttr.put("name", "Deep");

        vpAttr.put("licensePlate", "A15 1947");
        vpAttr.put("emergencyContacts", "+12489879090");
        GenericEventData ge = new GenericEventData();
        ge.set("vehicleProfile", vpAttr);
        im.setEventData(ge);
        AlertsInfo alertsInfo = new AlertsInfo();
        VehicleProfileAbridged vp = new VehicleProfileAbridged();
        vp.setVehicleAttributes(vpAttr);
        data.setVehicleProfile(vp);
        alertsInfo.setIgniteEvent(im);
        alertsInfo.setAlertsData(data);

        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        vpService.getVehicleProfile(alertsInfo);

        assertEquals("Deep", alertsInfo.getAlertsData().getVehicleProfile().getName());
        assertEquals("Chroke", alertsInfo.getAlertsData().getVehicleProfile().getModel());
        assertEquals(null, alertsInfo.getAlertsData().getVehicleProfile().getModelYear());
        assertEquals("Deep", alertsInfo.getAlertsData().getVehicleProfile().getMake());
        assertEquals(userId, alertsInfo.getAlertsData().getVehicleProfile().getUserId());
        assertEquals(vehicleId, alertsInfo.getAlertsData().getVehicleProfile().getVehicleId());

    }

    @Test(expected = RuntimeException.class)
    public void testProcessNonRegisteredVehicleNoData() {


        String userId = "testUser01";
        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        data.set(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD, true);
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        String vehicleId = "MAR_1234";
        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        vpService.getVehicleProfile(alertsInfo);
    }

    @Test(expected = RuntimeException.class)
    public void testProcessNonRegisteredVehicleNoVehicleId() {

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        data.set(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD, true);
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);

        Mockito.when(event.getVehicleId()).thenReturn(null);

        vpService.getVehicleProfile(alertsInfo);
    }

    @Test(expected = RuntimeException.class)
    public void testProcessNonRegisteredVehicleNoVehicleAttrs() {


        Data data = new Data();
        data.set("id", "geofenceId");
        data.set(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD, true);
        VehicleProfileAbridged vp = new VehicleProfileAbridged();
        data.setVehicleProfile(vp);
        String vehicleId = "MAR_1234";
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);

        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        vpService.getVehicleProfile(alertsInfo);
    }

}
