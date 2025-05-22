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


import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * VehicleServiceTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class VehicleServiceTest {

    @InjectMocks
    private VehicleService vehicleService;

    @Mock
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    private static final String VP_JSON =
        "{\"_id\":\"HU4YOOI0000000\",\"createdOn\":\"\",\"updatedOn\":\"\","
                +
                "\"productionDate\":\"\",\"soldRegion\":\"string\",\"vehicleAttributes\":"
                +
                "{\"make\":\"string\",\"model\":\"string\",\"marketingColor\":\"string\","
                +
                "\"baseColor\":\"string\",\"modelYear\":\"string\",\"destinationCountry\":"
                +
                "\"string\",\"engineType\":\"string\",\"bodyStyle\":\"string\","
                +
                "\"bodyType\":\"string\",\"name\":\"string\",\"trim\":\"string\"},"
                +
                "\"authorizedUsers\":[{\"userId\":\"yoni\",\"role\":\"VEHICLE_OWNER\","
                +
                "\"source\":\"string\",\"status\":\"string\",\"tc\":{},\"createdOn\":\"\","
                +
                "\"updatedOn\":\"\",\"licensePlate\":\"111-11-111\",\"emergencyContacts\":"
                +
                "[{\"phone\":\"+972111111111\"}]}],\"modemInfo\":{\"eid\":\"string\",\"iccid"
                +
                "\":\"string\",\"imei\":\"string\",\"msisdn\":\"string\",\"imsi\":\"string\"},"
                +
                "\"vehicleArchType\":\"string\",\"ecus\":{\"additionalProp1\":{\"swVersion\":"
                +
                "\"string\",\"hwVersion\":\"string\",\"partNumber\":\"string\",\"os\":\"string"
                +
                "\",\"screenSize\":\"string\",\"manufacturer\":\"string\",\"ecuType\":\"string\""
                +
                ",\"serialNo\":\"string\",\"clientId\":\"string\",\"capabilities\":{\"services\":"
                +
                "[{\"applicationId\":\"string\",\"version\":\"string\"}]},\"provisionedServices\""
                +
                ":{\"services\":[{\"applicationId\":\"string\",\"version\":\"string\"}]}},\"additionalProp2\""
                +
                ":{\"swVersion\":\"string\",\"hwVersion\":\"string\",\"partNumber\":\"string\",\"os\":"
                +
                "\"string\",\"screenSize\":\"string\",\"manufacturer\":\"string\",\"ecuType\":\"string\","
                +
                "\"serialNo\":\"string\",\"clientId\":\"string\",\"capabilities\":{\"services\":"
                +
                "[{\"applicationId\":\"string\",\"version\":\"string\"}]},\"provisionedServices\":"
                +
                "{\"services\":[{\"applicationId\":\"string\",\"version\":\"string\"}]}},\"additionalProp3"
                +
                "\":{\"swVersion\":\"string\",\"hwVersion\":\"string\",\"partNumber\":\"string\",\"os\":"
                +
                "\"string\",\"screenSize\":\"string\",\"manufacturer\":\"string\",\"ecuType\":\"string\","
                +
                "\"serialNo\":\"string\",\"clientId\":\"string\",\"capabilities\":{\"services\":"
                +
                "[{\"applicationId\":\"string\",\"version\":\"string\"}]},\"provisionedServices\":"
                +
                "{\"services\":[{\"applicationId\":\"string\",\"version\":\"string\"}]}}},\"dummy\":true,"
                +
                "\"events\":{},\"customParams\":{},\"vehicleCapabilities\":{\"sourceType\":\"string\","
                +
                "\"pids\":[\"string\"]},\"saleAttributes\":{\"dealerCode\":\"string\",\"saleDate\":\"\","
                +
                "\"eventType\":\"string\",\"eventDate\":\"\",\"warrantyStartDate\":\"\",\"marketCode\":"
                +
                "\"string\",\"salesChannel\":\"string\",\"customerSegment\":\"string\"},\"provisionedServices"
                +
                "\":[{\"applicationId\":\"string\",\"version\":\"string\"}],\""
                +
                "_class\":\"org.eclipse.ecsp.vehiclemanagement.entities.VehicleProfile\"}";
    private static final String VP_NONCONNECTED_JSON =
        "{\"_id\":\"HU4YOOI0000000\",\"createdOn\":\"\",\"updatedOn\":\"\",\"productionDate\":\"\""
                +
                ",\"soldRegion\":\"string\",\"vehicleAttributes\":{\"make\":\"string\","
                +
                "\"model\":\"string\",\"marketingColor\":\"string\",\"baseColor\":\"string\""
                +
                ",\"modelYear\":\"string\",\"destinationCountry\":\"string\",\"engineType\":"
                +
                "\"string\",\"bodyStyle\":\"string\",\"bodyType\":\"string\",\"name\":\"string\""
                +
                ",\"trim\":\"string\"},\"authorizedUsers\":[{\"userId\":\"yoni\",\"role\":"
                +
                "\"VEHICLE_OWNER\",\"source\":\"string\",\"status\":\"string\",\"tc\":{},"
                +
                "\"createdOn\":\"\",\"updatedOn\":\"\",\"licensePlate\":\"111-11-111\","
                +
                "\"emergencyContacts\":[{\"phone\":\"+972111111111\"}]}],\"modemInfo\":"
                +
                "{\"eid\":\"string\",\"iccid\":\"string\",\"imei\":\"string\",\"msisdn\":"
                +
                "\"string\",\"imsi\":\"string\"},\"vehicleArchType\":\"string\",\"dummy\":true,"
                +
                "\"events\":{},\"customParams\":{},\"vehicleCapabilities\":{\"sourceType\":"
                +
                "\"string\",\"pids\":[\"string\"]},\"saleAttributes\":{\"dealerCode\":\"string\","
                +
                "\"saleDate\":\"\",\"eventType\":\"string\",\"eventDate\":\"\",\"warrantyStartDate\""
                +
                ":\"\",\"marketCode\":\"string\",\"salesChannel\":\"string\",\"customerSegment\":"
                +
                "\"string\"},\"provisionedServices\":[{\"applicationId\":\"string\",\"version\":\""
                +
                "string\"}],"
                +
                "\"_class\":\"org.eclipse.ecsp.vehiclemanagement.entities.VehicleProfile\"}";
    private static final String PATH_FOR_GETTING_SERVICES =
        "ecus.additionalProp1.provisionedServices.services[*].applicationId";

    @Test
    public void validateServiceEnabledWhenException() {
        ReflectionTestUtils.setField(vehicleService, "pathForGettingServices", PATH_FOR_GETTING_SERVICES);
        Mockito.doReturn(Optional.of(VP_JSON)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();

        AuthorizationException thrown =
            assertThrows(AuthorizationException.class,
                () -> vehicleService.validateServiceEnabled(null, notificationGroupingList));
        assertEquals("Vehicle is not subscribed to any of the services", thrown.getMessage());
    }

    @Test
    public void validateServiceEnabledWhenNoSuchEntityException() {
        ReflectionTestUtils.setField(vehicleService, "pathForGettingServices", PATH_FOR_GETTING_SERVICES);
        Mockito.doReturn(Optional.empty()).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();

        NoSuchEntityException thrown =
            assertThrows(NoSuchEntityException.class,
                () -> vehicleService.validateServiceEnabled(null, notificationGroupingList));
        assertTrue(thrown.getMessage().contains("Vehicle profile not found for vehicle"));
    }

    @Test
    public void getEnabledServicesWhenNoSuchEntityException() {
        Mockito.doThrow(RuntimeException.class).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());

        NoSuchEntityException thrown =
            assertThrows(NoSuchEntityException.class,
                () -> vehicleService.getEnabledServices("vehicleId"));
        assertTrue(thrown.getMessage().contains("Vehicle profile not found for vehicle vehicleId"));
    }

    @Test
    public void validateServiceEnabledWithEmptyService() {
        ReflectionTestUtils.setField(vehicleService, "pathForGettingServices", PATH_FOR_GETTING_SERVICES);
        Mockito.doReturn(Optional.of(VP_JSON)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        NotificationGrouping ng = new NotificationGrouping();
        ng.setService("");
        notificationGroupingList.add(ng);
        vehicleService.validateServiceEnabled(null, notificationGroupingList);
    }

    @Test
    public void validateServiceEnabledWithFalseCheckEntitlement() {
        ReflectionTestUtils.setField(vehicleService, "pathForGettingServices", PATH_FOR_GETTING_SERVICES);
        Mockito.doReturn(Optional.of(VP_JSON)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        NotificationGrouping ng = new NotificationGrouping();
        ng.setService("s1");
        ng.setCheckEntitlement(false);
        notificationGroupingList.add(ng);
        vehicleService.validateServiceEnabled(null, notificationGroupingList);
    }

    @Test
    public void validateServiceEnabledWithNoEnabledService() {
        ReflectionTestUtils.setField(vehicleService, "pathForGettingServices", PATH_FOR_GETTING_SERVICES);
        Mockito.doReturn(Optional.of(VP_JSON)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        NotificationGrouping ng = new NotificationGrouping();
        ng.setService("string");
        ng.setCheckEntitlement(true);
        notificationGroupingList.add(ng);
        vehicleService.validateServiceEnabled(null, notificationGroupingList);
    }

    @Test
    public void validateServiceEnabledWithNoEcusEnabledService() {
        ReflectionTestUtils.setField(vehicleService, "pathForGettingServices", PATH_FOR_GETTING_SERVICES);
        Mockito.doReturn(Optional.of(VP_NONCONNECTED_JSON)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(),
                Mockito.anyBoolean());
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        NotificationGrouping ng = new NotificationGrouping();
        ng.setService("string");
        ng.setCheckEntitlement(true);
        notificationGroupingList.add(ng);
        AuthorizationException thrown = assertThrows(AuthorizationException.class,
            () -> vehicleService.validateServiceEnabled(null, notificationGroupingList));
        assertEquals("Vehicle is not subscribed to any of the services", thrown.getMessage());
    }

    @Test
    public void validateServiceEnabledWithEntitlement() {
        ReflectionTestUtils.setField(vehicleService, "pathForGettingServices", PATH_FOR_GETTING_SERVICES);
        Mockito.doReturn(Optional.of(VP_JSON)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<NotificationGrouping> notificationGroupingList = new ArrayList<>();
        NotificationGrouping ng = new NotificationGrouping();
        ng.setService("s1");
        ng.setCheckEntitlement(true);
        notificationGroupingList.add(ng);
        AuthorizationException thrown =
            assertThrows(AuthorizationException.class,
                () -> vehicleService.validateServiceEnabled(null, notificationGroupingList));
        assertEquals("Vehicle is not subscribed to any of the services", thrown.getMessage());
    }
}