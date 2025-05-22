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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * VehicleProfileAbridgedTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class VehicleProfileAbridgedTest {

    VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
    VehicleProfileAbridged vehicleProfileAbridged2 = new VehicleProfileAbridged("testVeh");
    Map<String, Object> vehicleAttributes;

    /**
     * beforeEach method.
     */
    @Before
    public void beforeEach() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleAttributes.put("name", "dummy");
        vehicleAttributes.put("modelYear", "dummy");
        vehicleAttributes.put("model", "dummy");
        vehicleAttributes.put("make", "dummy");
        vehicleAttributes.put("licensePlate", "dummy");
        vehicleAttributes.put("emergencyContacts", "dummy");
        vehicleAttributes.put("vin", "dummy");
        vehicleAttributes.put("userId", "dummy");

        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
    }

    @Test
    public void testgetUserId() {
        vehicleProfileAbridged.getUserId();
    }

    @Test
    public void testgetVehicleId() {
        vehicleProfileAbridged.getVehicleId();
    }

    @Test
    public void testsetVehicleId() {
        vehicleProfileAbridged.setVehicleId("dummy");
    }

    @Test
    public void testgetName() {
        vehicleProfileAbridged.getName();
    }

    @Test
    public void testgetModelYear() {
        vehicleProfileAbridged.getModelYear();
    }

    @Test
    public void testgetModel() {
        vehicleProfileAbridged.getModel();
    }

    @Test
    public void testgetMake() {
        vehicleProfileAbridged.getMake();
    }

    @Test
    public void testgetPlateNumber() {
        vehicleProfileAbridged.getPlateNumber();
    }

    @Test
    public void testgetEmergencyNumber() {
        vehicleProfileAbridged.getEmergencyNumber();
    }

    @Test
    public void testgetVin() {
        vehicleProfileAbridged.getVin();
    }

    //get null
    @Test
    public void testgetUserIdnull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getUserId();
    }

    @Test
    public void testgetNamenull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getName();
    }

    @Test
    public void testgetModelYearnull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getModelYear();
    }

    @Test
    public void testgetModelnull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getModel();
    }

    @Test
    public void testgetMakenull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getMake();
    }

    @Test
    public void testgetPlateNumbernull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getPlateNumber();
    }

    @Test
    public void testgetEmergencyNumbernull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getEmergencyNumber();
    }

    @Test
    public void testgetVinnull() {
        vehicleAttributes = new HashMap<String, Object>();
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
        vehicleProfileAbridged.getVin();
    }


    @Test
    public void testgetVehicleAttributes() {
        vehicleProfileAbridged.getVehicleAttributes();
    }

    @Test
    public void atestsetVehicleAttributes() {
        vehicleProfileAbridged.setVehicleAttributes(vehicleAttributes);
    }

    @Test
    public void testgetVehicleAttributeByName() {
        vehicleProfileAbridged.getVehicleAttributeByName("dummy");
    }

    @Test
    public void testhashCode() {
        vehicleProfileAbridged.hashCode();
    }

    @Test
    public void testequals() {

        vehicleProfileAbridged.equals(vehicleProfileAbridged);

    }

    @Test
    public void testtoString() {
        vehicleProfileAbridged.toString();
    }

}
