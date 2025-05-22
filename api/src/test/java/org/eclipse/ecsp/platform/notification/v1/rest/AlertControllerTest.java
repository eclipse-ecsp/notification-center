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

package org.eclipse.ecsp.platform.notification.v1.rest;

import jakarta.validation.ValidationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.notification.entities.PaginatedAlertsHistory;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertReadUpdate;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertsHistoryRequestParams;
import org.eclipse.ecsp.platform.notification.v1.domain.PaginationInfo;
import org.eclipse.ecsp.platform.notification.v1.domain.TimeIntervalInfo;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV1;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV2;
import org.eclipse.ecsp.platform.notification.v1.service.AlertsServiceV3;
import org.eclipse.ecsp.platform.notification.v1.utils.AlertsConstants;
import org.eclipse.ecsp.platform.notification.v1.utils.ResponseMsgConstants;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND;
import static org.eclipse.ecsp.platform.notification.v1.utils.ResponseMsgConstants.INVALID_ALERTS_SPECIFIED;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * AlertControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AlertControllerTest {

    @InjectMocks
    private AlertsController alertsController;

    @Mock
    private AlertsServiceV1 alertsServiceV1;

    @Mock
    private AlertsServiceV2 alertsServiceV2;

    @Mock
    private AlertsServiceV3 alertsServiceV3;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void getAllAlertsBetweenSinceAndUntilV3WhenPageAndSizeNull() throws Exception {
        AlertsHistoryRequestParams params = createRequestParams(0, 0, 0L, 0L);
        Mockito.doReturn(PaginatedAlertsHistory.builder().build()).when(alertsServiceV3).getAlertsByDeviceId(params);
        assertNull(alertsController.getAllAlertsBetweenSinceAndUntilV3(null, "0", "0", null, null, null));
    }

    @Test
    public void getAllAlertsBetweenSinceAndUntilV3WhenPageAndSizeEmpty() throws Exception {
        AlertsHistoryRequestParams params = createRequestParams(0, 0, 0L, 0L);
        Mockito.doReturn(PaginatedAlertsHistory.builder().build()).when(alertsServiceV3).getAlertsByDeviceId(params);
        assertNull(alertsController.getAllAlertsBetweenSinceAndUntilV3(null, "0", "0", "", "", null));
    }

    @Test
    public void getSpecificAlertBetweenSinceAndUntilV3WhenInvalidAlertType() {
        Set<String> alertTypes = new HashSet<>();
        alertTypes.add("@");
        ValidationException thrown =
            assertThrows(ValidationException.class,
                () -> alertsController.getSpecificAlertBetweenSinceAndUntilV3(null, alertTypes, "0", "0", "0", "0",
                    null));
        assertEquals(INVALID_ALERTS_SPECIFIED, thrown.getMessage());
    }

    @Test
    public void getSpecificAlertBetweenSinceAndUntilV3() throws Exception {
        Set<String> alertTypes = getAlertType();
        Mockito.doReturn(null).when(alertsServiceV3).getAlertsByDeviceId(Mockito.any());
        assertNull(alertsController.getSpecificAlertBetweenSinceAndUntilV3(null, alertTypes, "0", "0", "1", "1", null));
    }

    @Test
    public void getAlertsBetweenSinceAndUntilV3() throws Exception {
        Mockito.doReturn(null).when(alertsServiceV3).getAlertsByUserid(Mockito.any());
        assertNull(alertsController.getAlertsBetweenSinceAndUntilV3("0", "0", "0", "0", "1", "1"));
    }

    @Test
    public void getAlertsBetweenSinceAndUntilAndAlertTypeV3() throws Exception {
        Set<String> alertTypes = getAlertType();
        Mockito.doReturn(null).when(alertsServiceV3).getAlertsByUserid(Mockito.any());
        assertNull(
            alertsController.getAlertsBetweenSinceAndUntilAndAlertTypeV3("0", "0", "0", "1", "1", alertTypes, null));
    }

    @Test
    public void getSpecificAlertBetweenSinceAndUntilV2() throws Exception {
        Mockito.when(alertsServiceV2.getAlertsByDeviceId(Mockito.any())).thenReturn(null);

        Set<String> alertTypes = getAlertType();
        assertNull(alertsController.getSpecificAlertBetweenSinceAndUntilV2("", alertTypes, "0", "0", "0", "1", "1"));
    }

    @Test
    public void getAllAlertsBetweenSinceAndUntilV2() throws Exception {
        Mockito.when(alertsServiceV2.getAlertsByDeviceId(Mockito.any())).thenReturn(null);
        assertNull(alertsController.getAllAlertsBetweenSinceAndUntilV2("0", "0", "0", "0", "0", "1"));
    }

    @Test
    public void getAlertsBetweenSinceAndUntilV2() throws Exception {
        Mockito.when(alertsServiceV2.getAlertsByUserId(Mockito.any())).thenReturn(null);
        assertNull(alertsController.getAlertsBetweenSinceAndUntilV2("0", "0", "0", "0", "0", "1"));
    }

    @Test
    public void getAlertsBetweenSinceAndUntilAndAlertTypeV2() throws Exception {
        Set<String> alertTypes = getAlertType();
        Mockito.when(alertsServiceV2.getAlertsByUserId(Mockito.any())).thenReturn(null);
        assertNull(
            alertsController.getAlertsBetweenSinceAndUntilAndAlertTypeV2("0", "0", "0", "0", "0", alertTypes, "1"));
    }

    @Test
    public void getAllAlertsBetweenSinceAndUntilV1() throws Exception {
        Mockito.when(alertsServiceV1.getAlerts(Mockito.any())).thenReturn(null);
        assertNull(alertsController.getAllAlertsBetweenSinceAndUntilV1("", "0", "0", "0", "0", "0"));
    }

    @Test
    public void getSpecificAlertBetweenSinceAndUntilV1() throws Exception {
        Mockito.when(alertsServiceV1.getAlerts(Mockito.any())).thenReturn(null);

        Set<String> alertTypes = getAlertType();
        assertNull(alertsController.getSpecificAlertBetweenSinceAndUntilV1("", alertTypes, "0", "0", "0", "0", "0"));
    }

    @Test
    public void getAlertsBetweenSinceAndUntilV1() throws Exception {
        Mockito.when(alertsServiceV1.getAlerts(Mockito.any())).thenReturn(null);
        assertNull(alertsController.getAlertsBetweenSinceAndUntilV1("0", "0", "0", "0", "0", "0"));
    }

    @Test
    public void getAlertsBetweenSinceAndUntilAndAlertTypeV1() throws Exception {
        Set<String> alertTypes = getAlertType();
        Mockito.when(alertsServiceV1.getAlerts(Mockito.any())).thenReturn(null);
        assertNull(
            alertsController.getAlertsBetweenSinceAndUntilAndAlertTypeV1("0", "0", "0", "0", "0", alertTypes, "0"));
    }

    @Test
    public void getNextAlertsV3() throws Exception {
        Mockito.when(alertsServiceV3.getNextAlertsByDeviceId(Mockito.any())).thenReturn(null);

        Set<String> alertTypes = getAlertType();
        assertNull(alertsController.getNextAlertsV3("0", "0", "0", alertTypes, "0"));
    }

    @Test
    public void saveV3WhenAlertNotifyNull() {
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> alertsController.saveV3("0", null));
        assertEquals(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST, illegalArgumentException.getMessage());
    }

    @Test
    public void saveV3WhenReadUnreadListNull() {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(null);
        alertReadUpdate.setUnreadList(null);
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> alertsController.saveV3("0", null));
        assertEquals(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST, illegalArgumentException.getMessage());
    }

    @Test
    public void saveV3WhenReadListNotNull() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(new ArrayList<>());
        alertReadUpdate.setUnreadList(null);
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());
        alertsController.saveV3("H123", alertReadUpdate);
    }

    @Test
    public void saveV3WhenReadListNull() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(null);
        alertReadUpdate.setUnreadList(new ArrayList<>());
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());
        alertsController.saveV3("H123", alertReadUpdate);
    }

    @Test
    public void saveV3() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());
        alertsController.saveV3("H123", alertReadUpdate);
    }

    @Test
    public void saveV2WhenAlertNotifyNull() {
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> alertsController.saveV2("0", null));
        assertEquals(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST, illegalArgumentException.getMessage());
    }

    @Test
    public void saveV2WhenReadUnreadListNull() {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(null);
        alertReadUpdate.setUnreadList(null);
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> alertsController.saveV2("0", null));
        assertEquals(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST, illegalArgumentException.getMessage());
    }

    @Test
    public void save2WhenReadListNull() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(null);
        alertReadUpdate.setUnreadList(new ArrayList<>());
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getUnreadList());
        alertsController.saveV2("H123", alertReadUpdate);
    }

    @Test
    public void save2WhenReadListNotNull() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(new ArrayList<>());
        alertReadUpdate.setUnreadList(null);
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());

        alertsController.saveV2("H123", alertReadUpdate);
    }

    @Test
    public void save2() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());
        alertsController.saveV2("H123", alertReadUpdate);
    }

    @Test
    public void saveV1WhenAlertNotifyNull() {
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> alertsController.saveV1("0", null));
        assertEquals(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST, illegalArgumentException.getMessage());
    }

    @Test
    public void saveV1WhenReadUnreadListNull() {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(null);
        alertReadUpdate.setUnreadList(null);
        IllegalArgumentException illegalArgumentException =
            assertThrows(IllegalArgumentException.class, () -> alertsController.saveV1("0", null));
        assertEquals(AlertsConstants.ALERTS_NULL_BODY_INVALID_REQUEST, illegalArgumentException.getMessage());
    }

    @Test
    public void save1WhenReadListNull() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(null);
        alertReadUpdate.setUnreadList(new ArrayList<>());
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());
        alertsController.saveV1("H123", alertReadUpdate);
    }

    @Test
    public void save1WhenReadListNotNull() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(null);
        alertReadUpdate.setUnreadList(new ArrayList<>());
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());
        alertsController.saveV1("H123", alertReadUpdate);
    }

    @Test
    public void save1() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(new ArrayList<>());
        assertNotNull(alertReadUpdate.getReadList());
        alertsController.saveV1("H123", alertReadUpdate);
    }

    @Test
    public void getAlertHistoryBetweenSinceAndUntil() throws Exception {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        alertReadUpdate.setReadList(new ArrayList<>());
        Mockito.doReturn(new ArrayList<>()).when(alertsServiceV1).getAlerts(Mockito.any());
        assertNotNull(alertsController.getAlertHistoryBetweenSinceAndUntil("H123", "0", "0"));
    }

    @Test
    public void deleteAlertsV1() throws Exception {
        assertNotNull(alertsController);
        alertsController.deleteAlertsV1(null, null, null);
    }

    @Test
    public void default1() {
        NoSuchEntityException thrown = assertThrows(NoSuchEntityException.class, () -> alertsController.default1());
        assertEquals(ResponseMsgConstants.UNSUPPORTED_API_VERSION_MSG, thrown.getMessage());
    }

    @Test
    public void getAlertsThrowVehicleIdNotFound() throws Exception {
        Mockito.doThrow(new VehicleIdNotFoundException(
            Collections.singletonList(NotificationCenterError.NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.toMessage())))
            .when(alertsServiceV1).getAlerts(any());
        VehicleIdNotFoundException thrown =
            assertThrows(VehicleIdNotFoundException.class,
                () -> alertsController.getAllAlertsBetweenSinceAndUntilV1("", "0", "0", "0", "0", "0"));
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getMessage(), thrown.getMessage());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getCode(), thrown.getCode());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getReason(), thrown.getReason());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getMessage(), thrown.getMessage());
    }

    @Test
    public void vehicleIdNotFoundException() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            alertsController.internalServerError(new VehicleIdNotFoundException(new ArrayList<>()), request);
        assertEquals(INTERNAL_SERVER_ERROR.value(), responseWrapper.getHttpStatusCode());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getCode(), responseWrapper.getRootMessage().getCode());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getReason(),
            responseWrapper.getRootMessage().getReason());
        assertEquals(NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.getMessage(),
            responseWrapper.getRootMessage().getMsg());
    }

    @Test
    public void getAlertsThrowInvalidVehicleIdInput() throws Exception {
        Mockito.doThrow(new InvalidVehicleIdInput("Vehicle ID does not exist")).when(alertsServiceV1).getAlerts(any());
        InvalidVehicleIdInput thrown =
            assertThrows(InvalidVehicleIdInput.class,
                () -> alertsController.getAllAlertsBetweenSinceAndUntilV1("", "0", "0", "0", "0", "0"));
        assertEquals("Vehicle ID does not exist", thrown.getMessage());

    }

    @Test
    public void invalidVehicleIdInput() {
        ResponseEntity<String> responseEntity = alertsController.invalidInputError(new InvalidVehicleIdInput(""));
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
    }

    private Set<String> getAlertType() {
        Set<String> alertTypes = new HashSet<>();
        alertTypes.add("A1");
        return alertTypes;
    }

    private AlertsHistoryRequestParams createRequestParams(Integer page, Integer size, Long since, Long until) {
        return AlertsHistoryRequestParams.builder()
            .deviceIds(null)
            .userId(null)
            .alertNames(null)
            .paginationInfo(new PaginationInfo(page, size))
            .timeIntervalInfo(new TimeIntervalInfo(since, until))
            .readStatus(null).build();
    }
}
