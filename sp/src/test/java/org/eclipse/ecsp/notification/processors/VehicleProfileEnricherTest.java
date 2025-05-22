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

import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.VehicleAttributes;
import org.eclipse.ecsp.domain.VehicleProfile;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.MarketingName;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.dao.MarketingNameDao;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.vehicle.profile.VehicleProfileIntegrationService;
import org.eclipse.ecsp.notification.vehicle.profile.VehicleProfileServiceProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for VehicleProfileEnricher. All the test cases for
 * VehicleProfileEnricher should also be executed here.
 */
public class VehicleProfileEnricherTest {

    private static final String VIN = "vinVal";

    @InjectMocks
    private VehicleProfileEnricher vehicleProfileEnricher;

    @Mock
    private MarketingNameDao mkDao;

    @Mock
    private IgniteEvent event;

    @Mock
    private VehicleProfileServiceProvider vpServiceProvider;

    @Mock
    private VehicleProfileIntegrationService vpService;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoProcess() {
        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");

        MarketingName mktName = new MarketingName();
        mktName.setMarketingName("MARUTI_NEXA");
        mktName.setModel("Picanto");

        MarketingName mktName1 = new MarketingName();
        mktName1.setMarketingName("KIKO");

        List<MarketingName> foundModels = new LinkedList<>();

        foundModels.add(mktName);
        foundModels.add(mktName1);
        String vehicleId = "MAR_1234";

        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        Mockito.doReturn(foundModels).when(mkDao)
            .findByMakeAndModel(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        Map<String, Object> vpAttrs = new HashMap<>();
        vpAttrs.put("make", vh.getMake());
        vpAttrs.put("model", vh.getModel());
        vpAttrs.put("modelYear", vh.getModelYear());
        vpAttrs.put("name", vh.getMake());
        vpAttrs.put("vin", VIN);
        String userId = "testUser";
        vpAttrs.put("userId", userId);
        VehicleProfileAbridged vp = new VehicleProfileAbridged();

        vp.setVehicleAttributes(vpAttrs);
        vp.setVehicleId(VIN);
        Mockito.when(vpServiceProvider.getVehicleProfileIntegrationService(Mockito.any()))
            .thenReturn(vpService);
        Mockito.when(vpService.getVehicleProfile(Mockito.any())).thenReturn(vp);

        vehicleProfileEnricher.process(alertsInfo);

        assertEquals(vh.getName(), alertsInfo.getAlertsData().getVehicleProfile().getName());
        assertEquals(vh.getModel(), alertsInfo.getAlertsData().getVehicleProfile().getModel());
        assertEquals(vh.getModelYear(), alertsInfo.getAlertsData().getVehicleProfile().getModelYear());
        assertEquals(vh.getMake(), alertsInfo.getAlertsData().getVehicleProfile().getMake());
        assertTrue(StringUtils.isEmpty(alertsInfo.getAlertsData().getVehicleProfile().getEmergencyNumber()));
        assertTrue(StringUtils.isEmpty(alertsInfo.getAlertsData().getVehicleProfile().getPlateNumber()));
        assertEquals(userId, alertsInfo.getAlertsData().getVehicleProfile().getUserId());
        assertEquals(mktName.getMarketingName(), alertsInfo.getAlertsData().getMarketingName());

    }

    @Test
    public void testProcessWithoutMarketingName() {

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();

        // create sample data
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        VehicleProfile vehProf = new VehicleProfile();

        vehProf.setVehicleAttributes(vh);
        String vehicleId = "MAR_1234";
        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        Map<String, Object> vpAttrs = new HashMap<>();
        vpAttrs.put("make", vh.getMake());
        vpAttrs.put("model", vh.getModel());
        vpAttrs.put("modelYear", vh.getModelYear());
        vpAttrs.put("name", vh.getMake());
        vpAttrs.put("vin", VIN);
        String userId = "testUser";

        vpAttrs.put("userId", userId);
        VehicleProfileAbridged vp = new VehicleProfileAbridged();

        vp.setVehicleAttributes(vpAttrs);
        vp.setVehicleId(VIN);
        Mockito.when(vpServiceProvider.getVehicleProfileIntegrationService(Mockito.any()))
            .thenReturn(vpService);
        Mockito.when(vpService.getVehicleProfile(Mockito.any())).thenReturn(vp);


        Mockito.doReturn(null).when(mkDao).find(Mockito.any());
        vehicleProfileEnricher.process(alertsInfo);
        assertEquals(vh.getName(), alertsInfo.getAlertsData().getVehicleProfile().getName());
        assertEquals(vh.getModel(), alertsInfo.getAlertsData().getVehicleProfile().getModel());
        assertEquals(vh.getModelYear(), alertsInfo.getAlertsData().getVehicleProfile().getModelYear());
        assertEquals(vh.getMake(), alertsInfo.getAlertsData().getVehicleProfile().getMake());
        assertEquals(VIN, alertsInfo.getAlertsData().getVehicleProfile().getVin());
        assertEquals(userId, alertsInfo.getAlertsData().getVehicleProfile().getUserId());
        assertEquals(vh.getMake() + " " + vh.getModel(), alertsInfo.getAlertsData().getMarketingName());
        Mockito.verify(mkDao, Mockito.times(1)).findByMakeAndModel(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
    }

    @Test
    public void testProcessWithoutVehicleProfile() {

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        VehicleProfile vehProf = new VehicleProfile();

        vehProf.setVehicleAttributes(vh);
        Mockito.when(event.getVehicleId()).thenReturn(null);

        Mockito.when(vpServiceProvider.getVehicleProfileIntegrationService(Mockito.any()))
            .thenReturn(vpService);
        Mockito.when(vpService.getVehicleProfile(Mockito.any())).thenReturn(null);

        vehicleProfileEnricher.process(alertsInfo);
        assertNull(vh.getName(), alertsInfo.getAlertsData().getVehicleProfile());
        Mockito.verify(mkDao, Mockito.times(0)).find(Mockito.any());
    }


    @Test
    public void testProcessNonRegisteredVehicle() {

        String userId = "testUser01";
        Data data = new Data();
        data.set("id", "geofenceId");
        data.set(NotificationConstants.USERID, userId);
        data.set(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD, true);

        Map<String, Object> vpAttr = new HashMap<>();
        vpAttr.put("make", "Deep");
        vpAttr.put("model", "Chroke");
        vpAttr.put("name", "Deep");
        vpAttr.put("licensePlate", "A15 1947");
        vpAttr.put("emergencyContacts", "+12489879090");
        vpAttr.put("userId", userId);
        String vehicleId = "MAR_1234";

        VehicleProfileAbridged vp = new VehicleProfileAbridged();
        vp.setVehicleId(vehicleId);
        vp.setVehicleAttributes(vpAttr);
        data.setVehicleProfile(vp);
        AlertsInfo alertsInfo = new AlertsInfo();

        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);

        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);
        Mockito.when(vpServiceProvider.getVehicleProfileIntegrationService(Mockito.any()))
            .thenReturn(vpService);
        Mockito.when(vpService.getVehicleProfile(Mockito.any())).thenReturn(vp);

        vehicleProfileEnricher.process(alertsInfo);

        assertEquals("Deep", alertsInfo.getAlertsData().getVehicleProfile().getName());
        assertEquals("Chroke", alertsInfo.getAlertsData().getVehicleProfile().getModel());
        assertEquals(null, alertsInfo.getAlertsData().getVehicleProfile().getModelYear());
        assertEquals("Deep", alertsInfo.getAlertsData().getVehicleProfile().getMake());
        assertEquals(userId, alertsInfo.getAlertsData().getVehicleProfile().getUserId());
        assertEquals(vehicleId, alertsInfo.getAlertsData().getVehicleProfile().getVehicleId());

    }
}
