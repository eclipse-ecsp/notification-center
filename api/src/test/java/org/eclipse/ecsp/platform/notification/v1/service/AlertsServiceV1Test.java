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

package org.eclipse.ecsp.platform.notification.v1.service;

import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.entities.DTCMaster;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertHistoryResponseItem;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertsHistoryRequestParams;
import org.eclipse.ecsp.platform.notification.v1.domain.TimeIntervalInfo;
import org.eclipse.ecsp.platform.notification.v1.events.EventMetadataConstants;
import org.eclipse.ecsp.platform.notification.v1.utils.ResponseMsgConstants;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * AlertsServiceV1_Test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("checkstyle:MagicNumber")
public class AlertsServiceV1Test {

    @InjectMocks
    private AlertsServiceV1 alertsServiceV1;

    @Mock
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @Mock
    private AlertsHistoryDao alertsHistoryDao;

    @Mock
    private DTCMasterService dtcMasterService;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    private static final String VP =
        "{\"_id\":\"5db96bc50d55260007a62519\","
            + "\"className\":\"org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile\",\"schemaVersion\":\"V1_0\","
            + "\"vin\":\"HCPHUHHFMOSDZXM42\",\"createdOn\":\"0\",\"updatedOn\":\"0\","
            + "\"vehicleAttributes\":{\"make\":\"NA\",\"model\":\"NA\",\"modelYear\":\"NA\",\"name\":\"My Car\","
            + "\"type\":\"UNAVAILABLE\"},\"authorizedUsers\":[{\"role\":\"VEHICLE_OWNER\",\"userId\":\"12345\"}],"
            + "\"vehicleArchType\":\"dongle\",\"ecus\":{\"dongle\":{\"clientId\":\"HUHHFMOSDZXM42\"}},\"dummy\":true,"
            + "\"eolValidationInProgress\":false}";

    @Test
    public void getAlerts() throws Exception {
        List<AlertsHistoryInfo> alertsHistoryInfos = getAlertsHistoryInfos();

        Mockito.doReturn(alertsHistoryInfos).when(alertsHistoryDao)
            .findByPdidAndTimestampBetweenAndAlertTypeIn(Mockito.any(), Mockito.any(), Mockito.anyLong(),
                Mockito.anyLong(), Mockito.any());
        DTCMaster dtcInfo = getDtcMaster();
        Mockito.doReturn(dtcInfo).when(dtcMasterService).getDtcCode(Mockito.any());

        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Collection<String> alertNames = getAlertNames();
        AlertsHistoryRequestParams params = createRequestParams(alertNames, 1L, 2L);
        List<AlertsHistoryInfo> alerts = alertsServiceV1.getAlerts(params);
        assertEquals(1, alerts.size());
    }

    @Test
    public void getAlertsWithEmptyAlertNames() throws Exception {
        List<AlertsHistoryInfo> alertsHistoryInfos = getAlertsHistoryInfos();

        Mockito.doReturn(alertsHistoryInfos).when(alertsHistoryDao)
            .findByPdidAndTimestampBetween(Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());
        DTCMaster dtcInfo = getDtcMaster();
        Mockito.doReturn(dtcInfo).when(dtcMasterService).getDtcCode(Mockito.any());

        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        AlertsHistoryRequestParams params = createRequestParams(null, 1L, 2L);
        List<AlertsHistoryInfo> alerts = alertsServiceV1.getAlerts(params);
        assertEquals(1, alerts.size());
    }

    @Test
    public void getAlertsWhenSinceIsGreaterThanUntil() {
        List<AlertsHistoryInfo> alertsHistoryInfos = getAlertsHistoryInfos();

        Mockito.doReturn(alertsHistoryInfos).when(alertsHistoryDao)
            .findByPdidAndTimestampBetweenAndAlertTypeIn(Mockito.any(), Mockito.any(), Mockito.anyLong(),
                Mockito.anyLong(), Mockito.any());
        DTCMaster dtcInfo = getDtcMaster();
        Mockito.doReturn(dtcInfo).when(dtcMasterService).getDtcCode(Mockito.any());

        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Collection<String> alertNames = getAlertNames();
        AlertsHistoryRequestParams params = createRequestParams(alertNames, 2L, 1L);
        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> alertsServiceV1.getAlerts(params));
        assertEquals(ResponseMsgConstants.INVALID_ALERT_SINCE_OR_UNTIL_SPECIFIED, thrown.getMessage());
    }

    @Test
    public void getAlertsWhenInvalidVehicle() {
        List<AlertsHistoryInfo> alertsHistoryInfos = getAlertsHistoryInfos();

        Mockito.doReturn(alertsHistoryInfos).when(alertsHistoryDao)
            .findByPdidAndTimestampBetweenAndAlertTypeIn(Mockito.any(), Mockito.any(), Mockito.anyLong(),
                Mockito.anyLong(), Mockito.any());
        DTCMaster dtcInfo = getDtcMaster();
        Mockito.doReturn(dtcInfo).when(dtcMasterService).getDtcCode(Mockito.any());

        Mockito.doReturn(Optional.of("{}")).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Collection<String> alertNames = getAlertNames();
        AlertsHistoryRequestParams params = createRequestParams(alertNames, 1L, 2L);
        Exception thrown = assertThrows(Exception.class, () -> alertsServiceV1.getAlerts(params));
        assertTrue(thrown.getMessage().contains("vehicle id not found"));
    }

    @Test
    public void getAlertsWithDtcInfoNull() throws Exception {
        List<AlertsHistoryInfo> alertsHistoryInfos = getAlertsHistoryInfos();

        Mockito.doReturn(alertsHistoryInfos).when(alertsHistoryDao)
            .findByPdidAndTimestampBetweenAndAlertTypeIn(Mockito.any(), Mockito.any(), Mockito.anyLong(),
                Mockito.anyLong(), Mockito.any());
        Mockito.doReturn(getNullDtcInfo()).when(dtcMasterService).getDtcCode(Mockito.any());

        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Collection<String> alertNames = getAlertNames();
        AlertsHistoryRequestParams params = createRequestParams(alertNames, 1L, 2L);
        List<AlertsHistoryInfo> alerts = alertsServiceV1.getAlerts(params);
        assertEquals(1, alerts.size());

    }

    private List<AlertsHistoryInfo> getAlertsHistoryInfos() {

        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        alertsHistoryInfo.setAlertType(AlertHistoryResponseItem.AlertNames.DTCSTORED.name());


        Map<String, Object> alertsPayLoad = new HashMap<>();
        List<String> list = new ArrayList<>();
        list.add("s1");
        alertsPayLoad.put(EventMetadataConstants.SET, list);
        HashMap<String, Object> currentStateMap = new HashMap<>();
        currentStateMap.put("A", "A1");
        alertsPayLoad.put(EventMetadataConstants.CURRENT_STATE, currentStateMap);

        AlertsInfo alertInfo = new AlertsInfo();
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setAlertDataProperties(alertsPayLoad);
        alertInfo.setAlertsData(data);
        alertsHistoryInfo.setPayload(alertInfo);
        List<AlertsHistoryInfo> alertsHistoryInfos = new ArrayList<>();
        alertsHistoryInfos.add(alertsHistoryInfo);
        return alertsHistoryInfos;
    }

    private Object getNullDtcInfo() {
        return null;
    }

    private Collection<String> getAlertNames() {
        Collection<String> alertNames = new ArrayList<>();
        alertNames.add("a1");
        return alertNames;
    }

    private DTCMaster getDtcMaster() {
        DTCMaster dtcInfo = new DTCMaster();
        dtcInfo.setDescription("d1");
        dtcInfo.setCategory("c1");
        dtcInfo.setSubcategory("sub1");
        Set<String> suggestions = new HashSet<>();
        suggestions.add("suggestion1");
        dtcInfo.setSuggestions(suggestions);
        return dtcInfo;
    }

    private AlertsHistoryRequestParams createRequestParams(Collection<String> alertNames, Long since, Long until) {
        return AlertsHistoryRequestParams.builder()
            .deviceIds(null)
            .userId(null)
            .alertNames(alertNames)
            .paginationInfo(null)
            .timeIntervalInfo(new TimeIntervalInfo(since, until))
            .readStatus(null).build();
    }
}