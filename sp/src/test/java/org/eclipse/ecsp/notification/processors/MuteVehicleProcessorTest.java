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
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.dao.MuteVehicleDAO;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

/**
 * MuteVehicleProcessorTest class.
 */
public class MuteVehicleProcessorTest {

    @InjectMocks
    private MuteVehicleProcessor muteVehicleProcessor;

    @Mock
    private MuteVehicleDAO muteVehicleDao;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processSuccess() {
        AlertsInfo alertsInfo = new AlertsInfo();
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setVehicleId("vehicleIdVal");
        alertsInfo.setIgniteEvent(igniteEvent);
        MuteVehicle muteVehicle = new MuteVehicle();
        muteVehicle.setVehicleId("vehicleIdVal");
        Mockito.when(muteVehicleDao.findById(any())).thenReturn(muteVehicle);
        muteVehicleProcessor.process(alertsInfo);
        assertEquals("vehicleIdVal", alertsInfo.getMuteVehicle().getVehicleId());
    }

    @Test
    public void processVehicleIdNull() {
        AlertsInfo alertsInfo = new AlertsInfo();
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        alertsInfo.setIgniteEvent(igniteEvent);
        muteVehicleProcessor.process(alertsInfo);
        assertNull(alertsInfo.getMuteVehicle());
    }
}