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

package org.eclipse.ecsp.platform.notification.rest;

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.platform.notification.dto.DataResponseWrapper;
import org.eclipse.ecsp.platform.notification.dto.MuteVehicleDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.service.MuteVehicleServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * MuteVehicleControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("checkstyle:MagicNumber")
public class MuteVehicleControllerTest {
    private static final String REQUEST_ID = "REQ";
    private static final String SESSION_ID = "SESSION_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";

    @InjectMocks
    private MuteVehicleController muteVehicleController;

    @Mock
    private MuteVehicleServiceImpl muteVehicleService;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }


    @Test
    public void createMuteConfig() {
        MuteVehicle muteVehicle = getMuteVehicle();
        Mockito.doReturn(muteVehicle).when(muteVehicleService).createMuteConfig(Mockito.any());
        DataResponseWrapper<MuteVehicleDto> response =
            muteVehicleController.createMuteConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        Assert.assertEquals(200, response.getHttpStatusCode());
        Assert.assertEquals("2020-11-04T12:14:33", response.getData().iterator().next().getStartTime());
    }

    @Test
    public void findMuteConfig() {
        MuteVehicle muteVehicle = getMuteVehicle();
        Mockito.doReturn(muteVehicle).when(muteVehicleService).getMuteConfigById(Mockito.any());
        DataResponseWrapper<MuteVehicleDto> response =
            muteVehicleController.findMuteConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        Assert.assertEquals(200, response.getHttpStatusCode());
        Assert.assertEquals("2020-11-04T12:14:33", response.getData().iterator().next().getStartTime());
    }

    @Test
    public void deleteMuteConfig() {
        ResponseWrapper<Void> response =
            muteVehicleController.deleteMuteConfig(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        Assert.assertEquals(200, response.getHttpStatusCode());
    }

    @Test
    public void invalidInput() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        NotificationCenterExceptionBase e = new InvalidInputException(
            Collections.singletonList(NotificationCenterError.MUTE_CONFIG_FAILURE_GETTING_VEHICLE.toMessage()));
        ResponseWrapper<Void> response = muteVehicleController.invalidInput(e, request);
        Assert.assertEquals(400, response.getHttpStatusCode());
    }

    @Test
    public void notFoundException() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        NotificationCenterExceptionBase e = new NotFoundException(
            Collections.singletonList(NotificationCenterError.MUTE_CONFIG_VEHICLE_ID_DOES_NOT_EXIST.toMessage()));
        ResponseWrapper<Void> response = muteVehicleController.notFoundException(e, request);
        Assert.assertEquals(404, response.getHttpStatusCode());
    }

    @Test
    public void internalServerError() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        NotificationCenterExceptionBase e = new NotFoundException(
            Collections.singletonList(NotificationCenterError.MUTE_CONFIG_VEHICLE_ID_DOES_NOT_EXIST.toMessage()));
        ResponseWrapper<Void> response = muteVehicleController.internalServerError(e, request);
        Assert.assertEquals(400, response.getHttpStatusCode());
    }

    private MuteVehicle getMuteVehicle() {
        MuteVehicle muteVehicle = new MuteVehicle();
        muteVehicle.setVehicleId("");
        muteVehicle.setGroups(Collections.singleton("GRP1"));
        muteVehicle.setChannels(Collections.singleton(ChannelType.API_PUSH));
        muteVehicle.setStartTime(1604492073000L);
        muteVehicle.setEndTime(1604492074000L);
        return muteVehicle;
    }
}
