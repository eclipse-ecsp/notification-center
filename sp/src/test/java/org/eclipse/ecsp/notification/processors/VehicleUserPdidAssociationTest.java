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

import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * VehicleUserPdidAssociationTest class.
 */
public class VehicleUserPdidAssociationTest {

    VehicleUserPdidAssociation vehicleUserPdidAssociation;

    @Before
    public void init() {
        vehicleUserPdidAssociation = new VehicleUserPdidAssociation();
    }

    @Test
    public void setGetEventId_success() {
        vehicleUserPdidAssociation.setEventId("dummyEventID");
        assertEquals("dummyEventID", vehicleUserPdidAssociation.getEventId());
    }

    @Test
    public void setGetUserIdAssociationData_success() {
        VehicleUserPdidAssociation.AssociationData associationData = new VehicleUserPdidAssociation.AssociationData();
        associationData.setUserId("dummyUserId");
        assertEquals("dummyUserId", associationData.getUserId());
    }

    @Test
    public void setGetData_success() {
        VehicleUserPdidAssociation.AssociationData data = new VehicleUserPdidAssociation.AssociationData();
        vehicleUserPdidAssociation.setData(data);
        assertEquals(vehicleUserPdidAssociation.getData(), data);
    }

}