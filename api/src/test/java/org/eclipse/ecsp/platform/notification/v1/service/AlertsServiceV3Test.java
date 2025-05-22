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
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.nosqldao.IgnitePagingInfoResponse;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.entities.PaginatedAlertsHistory;
import org.eclipse.ecsp.platform.notification.v1.common.Constants;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertHistoryResponseItem;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertIdDto;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertReadUpdate;
import org.eclipse.ecsp.platform.notification.v1.domain.AlertsHistoryRequestParams;
import org.eclipse.ecsp.platform.notification.v1.domain.PaginationInfo;
import org.eclipse.ecsp.platform.notification.v1.domain.TimeIntervalInfo;
import org.eclipse.ecsp.platform.notification.v1.events.EventMetadataConstants;
import org.eclipse.ecsp.platform.notification.v1.fw.web.AssociationServiceClient;
import org.eclipse.ecsp.platform.notification.v1.fw.web.DeviceAssociation;
import org.eclipse.ecsp.platform.notification.v1.utils.AlertsConstants;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * AlertsServiceV3Test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("checkstyle:MagicNumber")
public class AlertsServiceV3Test {

    @InjectMocks
    private AlertsServiceV3 alertsServiceV3;

    @Mock
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @Mock
    private AlertsHistoryDao alertsHistoryDao;

    @Mock
    private DTCMasterService dtcMasterService;

    @Mock
    private AssociationServiceClient associationServiceClient;

    private static final String VP =
        "{\"_id\":\"5db96bc50d55260007a62519\",\"className\":\"org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile\""
            + ",\"schemaVersion\":\"V1_0\",\"vin\":\"HCPHUHHFMOSDZXM42\",\"createdOn\":\"0\",\"updatedOn\":\"0\","
            + "\"vehicleAttributes\":{\"make\":\"NA\",\"model\":\"NA\",\"modelYear\":\"NA\",\"name\":\"My Car\","
            + "\"type\":\"UNAVAILABLE\"},\"authorizedUsers\":[{\"role\":\"VEHICLE_OWNER\",\"userId\":\"12345\"}],"
            + "\"vehicleArchType\":\"dongle\",\"ecus\":{\"dongle\":{\"clientId\":\"HUHHFMOSDZXM42\"}},\"dummy\":true,"
            + "\"eolValidationInProgress\":false}";

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void getAlertsByDeviceIdWhenSinceGreaterThanUntil() {
        AlertsHistoryRequestParams params = createRequestParams(null, null, null, 0, 0, 0L, null);
        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> alertsServiceV3.getAlertsByDeviceId(params));
        assertEquals(ResponseMsgConstants.INVALID_ALERT_SINCE_OR_UNTIL_SPECIFIED, thrown.getMessage());
    }

    @Test
    public void getAlertsByDeviceId() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(getAlertsHistoryInfos(2)).when(alertsHistoryDao)
            .findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyCollection(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        AlertsHistoryRequestParams params =
            createRequestParams(Collections.singletonList("H1"), null, getAlertNames(), 1, 4, 2L, "read");
        PaginatedAlertsHistory alertsByDeviceId = alertsServiceV3.getAlertsByDeviceId(params);
        assertEquals(2, alertsByDeviceId.getAlerts().size());
    }

    @Test
    public void getAlertsByDeviceIdWhenCountEqualsWithAlertCount() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(getAlertsHistoryInfos(2)).when(alertsHistoryDao)
            .findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyCollection(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        AlertsHistoryRequestParams params =
            createRequestParams(Collections.singletonList("H1"), null, getAlertNames(), 1, 2, 2L, "read");
        PaginatedAlertsHistory alertsByDeviceId = alertsServiceV3.getAlertsByDeviceId(params);
        assertEquals(2, alertsByDeviceId.getAlerts().size());
    }

    @Test
    public void getAlertsByDeviceIdWhenReadNull() {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(getAlertsHistoryInfos(1)).when(alertsHistoryDao)
            .findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyCollection(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        AlertsHistoryRequestParams params =
            createRequestParams(Collections.singletonList("H1"), null, getAlertNames(), 1, 2, 2L, null);
        NoSuchEntityException thrown =
            assertThrows(NoSuchEntityException.class, () -> alertsServiceV3.getAlertsByDeviceId(params));
        assertEquals(ResponseMsgConstants.UNSUPPORTED_API_READ_MSG, thrown.getMessage());
    }

    @Test
    public void getAlertsByDeviceIdWhenAlertNamesEmpty() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(getAlertsHistoryInfos(2)).when(alertsHistoryDao)
            .findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        AlertsHistoryRequestParams params =
            createRequestParams(Collections.singletonList("H1"), null, null, 1, 2, 2L, "read");
        PaginatedAlertsHistory alertsByDeviceId = alertsServiceV3.getAlertsByDeviceId(params);
        assertEquals(2, alertsByDeviceId.getAlerts().size());
    }

    @Test
    public void getAlertsByDeviceIdWhenDtcNull() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(getAlertsHistoryInfos(2)).when(alertsHistoryDao)
            .findByPdidInAndReadAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(null).when(dtcMasterService).getDtcCode(Mockito.any());
        AlertsHistoryRequestParams params =
            createRequestParams(Collections.singletonList("H1"), null, null, 1, 2, 2L, "read");
        PaginatedAlertsHistory alertsByDeviceId = alertsServiceV3.getAlertsByDeviceId(params);
        assertEquals(2, alertsByDeviceId.getAlerts().size());
    }

    @Test
    public void getAlertsByDeviceIdWhenReadValueAll() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(getAlertsHistoryInfos(2)).when(alertsHistoryDao)
            .findByPdidAndAlertTypeInAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyCollection(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(null).when(dtcMasterService).getDtcCode(Mockito.any());
        AlertsHistoryRequestParams params =
            createRequestParams(Collections.singletonList("H1"), null, getAlertNames(), 1, 2, 2L, "all");
        PaginatedAlertsHistory alertsByDeviceId = alertsServiceV3.getAlertsByDeviceId(params);
        assertEquals(2, alertsByDeviceId.getAlerts().size());
    }

    @Test
    public void getAlertsByUserid() throws Exception {
        Mockito.doReturn(Optional.of(getDeviceAssociations())).when(associationServiceClient).getDevices(Mockito.any());
        Mockito.doReturn(getAlertsHistoryInfos(2)).when(alertsHistoryDao)
            .findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyCollection(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        AlertsHistoryRequestParams params = createRequestParams(null, "U1", getAlertNames(), 1, 4, 2L, "read");
        Map<String, Object> alertsByDeviceId = alertsServiceV3.getAlertsByUserid(params);
        assertEquals(2, alertsByDeviceId.size());
    }

    @Test
    public void getNextAlertsByDeviceId() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(Optional.of(getDeviceAssociations())).when(associationServiceClient).getDevices(Mockito.any());
        Mockito.doReturn(getAlertsHistoryInfos(1)).when(alertsHistoryDao)
            .findByPdidReadAlertTypeAndTimestampBetweenOrderByTimestampDesc(Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.anyCollection(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        AlertsHistoryRequestParams params =
            createRequestParams(Collections.singletonList("H1"), null, getAlertNames(), 1, 4, 2L, "read");
        List<AlertsHistoryInfo> nextAlertsByDeviceId = alertsServiceV3.getNextAlertsByDeviceId(params);
        assertEquals(1, nextAlertsByDeviceId.size());
    }

    @Test
    public void saveDataWhenReadUnreadMismatch() {
        AlertReadUpdate alertReadUpdate = getAlertReadUpdateWhenReadListContainsUnReadList();
        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> alertsServiceV3.saveData("H1", alertReadUpdate));
        assertEquals(AlertsConstants.ALERTS_READ_AND_UNREAD_INVALID_REQUEST, thrown.getMessage());
    }

    @Test
    public void saveDataWhenReadUnreadListEmpty() throws Exception {
        assertNotNull(alertsServiceV3);
        alertsServiceV3.saveData("H1", new AlertReadUpdate());
    }

    @Test
    public void saveDataWhenReadListNotEmptyAndValueAllAndAlertNotFoundInDb() {
        AlertReadUpdate alertReadUpdate = getAlertReadUpdate();
        alertReadUpdate.getReadList().set(0, "ALL");
        alertReadUpdate.setUnreadList(null);
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<AlertsHistoryInfo> dbAlertDataList = getAlertsHistoryInfoList();
        Mockito.doReturn(dbAlertDataList).when(alertsHistoryDao)
            .findByPdidAndIdIn(Mockito.any(), Mockito.any(), Mockito.anyList());
        NoSuchEntityException thrown =
            assertThrows(NoSuchEntityException.class, () -> alertsServiceV3.saveData("H1", alertReadUpdate));
        assertEquals(AlertsConstants.ALERTS_NO_DATA_FOUND_ERROR_MSG, thrown.getMessage());
    }

    @Test
    public void saveDataWhenReadListNotEmptyAndValueAll() throws Exception {
        AlertReadUpdate alertReadUpdate = getAlertReadUpdate();
        alertReadUpdate.getReadList().set(0, "ALL");
        alertReadUpdate.setUnreadList(null);
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<AlertsHistoryInfo> dbAlertDataList = getAlertsHistoryInfoList();
        Mockito.doReturn(dbAlertDataList).when(alertsHistoryDao)
            .findByPdidAndRead(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        assertNotNull(alertReadUpdate);
        alertsServiceV3.saveData("H1", alertReadUpdate);
    }

    @Test
    public void saveDataWhenReadListNotEmptyAndValueNotAll() throws Exception {
        AlertReadUpdate alertReadUpdate = getAlertReadUpdate();
        alertReadUpdate.setUnreadList(null);
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<AlertsHistoryInfo> dbAlertDataList = getAlertsHistoryInfoList();
        Mockito.doReturn(dbAlertDataList).when(alertsHistoryDao)
            .findByPdidAndIdIn(Mockito.any(), Mockito.any(), Mockito.anyList());
        assertNotNull(alertReadUpdate);
        alertsServiceV3.saveData("H1", alertReadUpdate);
    }

    @Test
    public void saveDataWhenReadListEmpty() throws Exception {
        AlertReadUpdate alertReadUpdate = getAlertReadUpdate();
        alertReadUpdate.setReadList(null);
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<AlertsHistoryInfo> dbAlertDataList = getAlertsHistoryInfoList();
        Mockito.doReturn(dbAlertDataList).when(alertsHistoryDao)
            .findByPdidAndIdIn(Mockito.any(), Mockito.any(), Mockito.anyList());
        assertNotNull(alertReadUpdate);
        alertsServiceV3.saveData("H1", alertReadUpdate);
    }

    @Test
    public void deleteAlerts() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<AlertsHistoryInfo> dbAlertDataList = getAlertsHistoryInfoList();
        Mockito.doReturn(dbAlertDataList).when(alertsHistoryDao)
            .findByPdidAndIdIn(Mockito.any(), Mockito.any(), Mockito.anyList());
        AlertIdDto alertIdDto = new AlertIdDto();
        ArrayList<String> alertIdList = new ArrayList<>();
        alertIdList.add("l1");
        alertIdDto.setAlertIdList(alertIdList);
        assertNotNull(alertIdList);
        alertsServiceV3.deleteAlerts("H1", true, alertIdDto);
    }

    @Test
    public void deleteAlertsWhenAlertIdDtoNull() throws Exception {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        List<AlertsHistoryInfo> dbAlertDataList = getAlertsHistoryInfoList();
        Mockito.doReturn(dbAlertDataList).when(alertsHistoryDao).findByPdid(Mockito.any(), Mockito.any());
        assertNotNull(dbAlertDataList);
        alertsServiceV3.deleteAlerts("H1", true, null);
    }

    @Test
    public void deleteAlertsWhenException() {
        Mockito.doReturn(Optional.of(VP)).when(coreVehicleProfileClient)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> alertsServiceV3.deleteAlerts("H1", true, null));
        assertEquals(AlertsConstants.ALERTS_NO_DATA_FOUND_ERROR_MSG, thrown.getMessage());
    }

    private List<DeviceAssociation> getDeviceAssociations() {
        List<DeviceAssociation> deviceAssociations = new ArrayList<>();
        DeviceAssociation da = new DeviceAssociation();
        da.setHarmanID("H1");
        da.setAssociationStatus(Constants.ASSOCIATED);
        deviceAssociations.add(da);
        return deviceAssociations;
    }

    private IgnitePagingInfoResponse<AlertsHistoryInfo> getAlertsHistoryInfos(int numOfAlerts) {
        List<AlertsHistoryInfo> alertsHistoryInfos = new ArrayList<>();
        for (int i = 0; i < numOfAlerts; i++) {
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
            alertsHistoryInfos.add(alertsHistoryInfo);
        }
        return new IgnitePagingInfoResponse(alertsHistoryInfos, numOfAlerts);
    }

    private Collection<String> getAlertNames() {
        Collection<String> alertNames = new ArrayList<>();
        alertNames.add("a1");
        return alertNames;
    }

    private AlertReadUpdate getAlertReadUpdateWhenReadListContainsUnReadList() {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        List<String> readList = new ArrayList<>();
        readList.add("read1");
        alertReadUpdate.setReadList(readList);
        List<String> unReadList = new ArrayList<>();
        unReadList.add("read1");
        alertReadUpdate.setUnreadList(unReadList);
        return alertReadUpdate;
    }

    private AlertReadUpdate getAlertReadUpdate() {
        AlertReadUpdate alertReadUpdate = new AlertReadUpdate();
        List<String> readList = new ArrayList<>();
        readList.add("read1");
        alertReadUpdate.setReadList(readList);
        List<String> unReadList = new ArrayList<>();
        unReadList.add("unRead1");
        alertReadUpdate.setUnreadList(unReadList);
        return alertReadUpdate;
    }

    private List<AlertsHistoryInfo> getAlertsHistoryInfoList() {
        List<AlertsHistoryInfo> dbAlertDataList = new ArrayList<>();
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        dbAlertDataList.add(alertsHistoryInfo);
        return dbAlertDataList;
    }

    private AlertsHistoryRequestParams createRequestParams(List<String> deviceIds, String userId,
                                                           Collection<String> alertNames,
                                                           Integer page, Integer size, Long until, String readStatus) {
        return AlertsHistoryRequestParams.builder()
            .deviceIds(deviceIds)
            .userId(userId)
            .alertNames(alertNames)
            .paginationInfo(new PaginationInfo(page, size))
            .timeIntervalInfo(new TimeIntervalInfo(1, until))
            .readStatus(readStatus).build();
    }
}