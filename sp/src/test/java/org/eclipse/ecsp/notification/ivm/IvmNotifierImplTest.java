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

package org.eclipse.ecsp.notification.ivm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.idgen.MessageIdGenerator;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.FCMChannelResponse;
import org.eclipse.ecsp.domain.notification.IVMFeedbackData_V1;
import org.eclipse.ecsp.domain.notification.IVMNotifierResponse;
import org.eclipse.ecsp.domain.notification.IVMRequest;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.NotificationErrorCode;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleMessageAckData;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionAckData;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData;
import org.eclipse.ecsp.domain.notification.VehicleMessagePublishData;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.entities.dma.DeviceMessageErrorCode;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.dao.IVMRequestDAO;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.IVMTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.grouping.GroupType;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.eclipse.ecsp.domain.notification.commons.EventID.DMA_FEEDBACK_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_ACK;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_DISPOSITION_ACK;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_PUBLISH;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * IvmNotifierImplTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class IvmNotifierImplTest {

    @InjectMocks
    private IvmNotifierImpl ivmNotifier;

    @Mock
    private StreamProcessingContext ctxt;

    @Mock
    private AlertsHistoryDao alertsHistoryDao;

    @Mock
    private NotificationGroupingDAO notificationGroupingDao;

    @Mock
    private IVMRequestDAO ivmRequestDao;

    @Mock
    private MessageIdGenerator msgIdGen;

    @Mock
    private VehicleService vehicleService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Captor
    ArgumentCaptor<IgniteEventImpl> igniteEventArgumentCaptor;

    @Captor
    ArgumentCaptor<IgniteEventImpl> igniteEventArgumentCaptor2;

    @Captor
    ArgumentCaptor<Record> kafkaRecordArgumentCaptor;


    @Test
    public void testCampaignNotificationSuccess() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doNothing().when(vehicleService).validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forward(kafkaRecordArgumentCaptor.capture());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(true);

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);


        assertEquals(NOTIFICATION_STATUS_SUCCESS, ivmResponse.getStatus());
        assertEquals("ivm body", ivmResponse.getTemplate().getBody());
        assertEquals("ivm title", ivmResponse.getTemplate().getTitle());
        IgniteEventImpl igniteEvent = (IgniteEventImpl) kafkaRecordArgumentCaptor.getValue().value();
        assertEquals("12345", igniteEvent.getMessageId());
        assertEquals(3, ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().size());
        assertEquals("No querrás perderte esta oferta !!!",
            ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().stream()
                .filter(ls -> ls.getLanguage().equals("es-ES")).findFirst().get().getTitle());
        assertEquals(2, ((VehicleMessagePublishData) igniteEvent.getEventData()).getButtonActions().size());
    }

    @Test
    public void testCampaignNotificationWithoutUser() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doNothing().when(vehicleService).validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forward(kafkaRecordArgumentCaptor.capture());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(true);
        alertsInfo.getAlertsData().setUserProfile(null);

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);


        assertEquals(NOTIFICATION_STATUS_SUCCESS, ivmResponse.getStatus());
        assertEquals("ivm body", ivmResponse.getTemplate().getBody());
        assertEquals("ivm title", ivmResponse.getTemplate().getTitle());
        IgniteEventImpl igniteEvent = (IgniteEventImpl) kafkaRecordArgumentCaptor.getValue().value();
        assertEquals("12345", igniteEvent.getMessageId());
        assertEquals(3, ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().size());
        assertEquals("No querrás perderte esta oferta !!!",
            ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().stream()
                .filter(ls -> ls.getLanguage().equals("es-ES")).findFirst().get().getTitle());
        assertEquals(2, ((VehicleMessagePublishData) igniteEvent.getEventData()).getButtonActions().size());
    }

    @Test
    public void testRegularNotificationUserLocaleSuccess() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doNothing().when(vehicleService).validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forward(kafkaRecordArgumentCaptor.capture());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(false);

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);

        assertEquals(NOTIFICATION_STATUS_SUCCESS, ivmResponse.getStatus());
        assertEquals("ivm body", ivmResponse.getTemplate().getBody());
        assertEquals("ivm title", ivmResponse.getTemplate().getTitle());
        IgniteEventImpl igniteEvent = (IgniteEventImpl) kafkaRecordArgumentCaptor.getValue().value();

        assertEquals("12345", igniteEvent.getMessageId());
        assertEquals(1, ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().size());
        assertEquals("ivm title",
            ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().get(0).getTitle());
        assertEquals(2, ((VehicleMessagePublishData) igniteEvent.getEventData()).getButtonActions().size());
        assertEquals(2, ((VehicleMessagePublishData) igniteEvent.getEventData()).getPriority());
        assertEquals("Bluetooth", ((VehicleMessagePublishData) igniteEvent.getEventData()).getCallType());
        assertEquals("adVal1", ((VehicleMessagePublishData) igniteEvent.getEventData()).getAdditionalData().get("ad1"));
    }

    @Test
    public void testRegularNotificationUserLocaleWithoutAllData() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doNothing().when(vehicleService).validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forward(kafkaRecordArgumentCaptor.capture());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(false);
        alertsInfo.getAlertsData().setAlertDataProperties(new HashMap<>());

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);

        assertEquals(NOTIFICATION_STATUS_SUCCESS, ivmResponse.getStatus());
        assertEquals("ivm body", ivmResponse.getTemplate().getBody());
        assertEquals("ivm title", ivmResponse.getTemplate().getTitle());
        IgniteEventImpl igniteEvent = (IgniteEventImpl) kafkaRecordArgumentCaptor.getValue().value();
        assertEquals("12345", igniteEvent.getMessageId());
        assertEquals(1, ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().size());
        assertEquals("ivm title",
            ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().get(0).getTitle());
        assertNull(((VehicleMessagePublishData) igniteEvent.getEventData()).getButtonActions());
        assertNull(((VehicleMessagePublishData) igniteEvent.getEventData()).getCallType());
        assertNull(((VehicleMessagePublishData) igniteEvent.getEventData()).getAdditionalData());
    }

    @Test
    public void testRegularNotificationAllLocaleSuccess() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doNothing().when(vehicleService).validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forward(kafkaRecordArgumentCaptor.capture());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(false);
        alertsInfo.setAllLanguageTemplates(getAllLanguageTemplate());

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);

        assertEquals(NOTIFICATION_STATUS_SUCCESS, ivmResponse.getStatus());
        assertEquals("ivm body", ivmResponse.getTemplate().getBody());
        assertEquals("ivm title", ivmResponse.getTemplate().getTitle());
        IgniteEventImpl igniteEvent = (IgniteEventImpl) kafkaRecordArgumentCaptor.getValue().value();
        assertEquals("12345", igniteEvent.getMessageId());
        assertEquals(2, ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().size());
        assertEquals("es ivm title",
            ((VehicleMessagePublishData) igniteEvent.getEventData()).getMessage().stream()
                .filter(ls -> ls.getLanguage().equals("es-ES")).findFirst().get().getTitle());
        assertEquals(2, ((VehicleMessagePublishData) igniteEvent.getEventData()).getButtonActions().size());
    }

    @Test
    public void testCampaignNotificationFailEntitlementNoGroup() throws IOException {
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(null);
        doNothing().when(vehicleService).validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(true);

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);
        IgniteEventImpl igniteEvent = igniteEventArgumentCaptor.getValue();

        assertEquals(NotificationConstants.FAILURE, ivmResponse.getStatus());
        assertEquals(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED, ivmResponse.getErrorCode());

        assertEquals(NotificationConstants.FAILURE, ((IVMFeedbackData_V1) igniteEvent.getEventData()).getStatus());
        assertEquals(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED,
            ((IVMFeedbackData_V1) igniteEvent.getEventData()).getErrorCode());
        assertEquals("Vehicle Id not provisioned", ((IVMFeedbackData_V1) igniteEvent.getEventData()).getErrorDetail());
    }

    @Test
    public void testCampaignNotificationFailEntitlementNoAuth() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doThrow(new AuthorizationException("auth exception")).when(vehicleService)
            .validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(true);

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);
        IgniteEventImpl igniteEvent = igniteEventArgumentCaptor.getValue();

        assertEquals(NotificationConstants.FAILURE, ivmResponse.getStatus());
        assertEquals(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED, ivmResponse.getErrorCode());

        assertEquals(NotificationConstants.FAILURE, ((IVMFeedbackData_V1) igniteEvent.getEventData()).getStatus());
        assertEquals(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED,
            ((IVMFeedbackData_V1) igniteEvent.getEventData()).getErrorCode());
        assertEquals("Vehicle Id not provisioned", ((IVMFeedbackData_V1) igniteEvent.getEventData()).getErrorDetail());
        assertEquals("HUXOIDDN2HUN11", igniteEvent.getVehicleId());
    }

    @Test
    public void testCampaignNotificationFailEntitlementNoPath() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doThrow(new PathNotFoundException("path exception")).when(vehicleService)
            .validateServiceEnabled(anyString(), any());
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor.capture(), any());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(true);

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);
        IgniteEventImpl igniteEvent = igniteEventArgumentCaptor.getValue();

        assertEquals(NotificationConstants.FAILURE, ivmResponse.getStatus());
        assertEquals(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED, ivmResponse.getErrorCode());

        assertEquals(NotificationConstants.FAILURE, ((IVMFeedbackData_V1) igniteEvent.getEventData()).getStatus());
        assertEquals(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED,
            ((IVMFeedbackData_V1) igniteEvent.getEventData()).getErrorCode());
        assertEquals("Vehicle Id not provisioned", ((IVMFeedbackData_V1) igniteEvent.getEventData()).getErrorDetail());
    }

    @Test
    public void testCampaignNotificationFailForward() throws IOException {
        List<NotificationGrouping> notificationGroupings = Collections.singletonList(getNotificationGrouping());
        when(notificationGroupingDao.findByNotificationId(Mockito.anyString())).thenReturn(notificationGroupings);
        doNothing().when(vehicleService).validateServiceEnabled(anyString(), any());
        doThrow(new RuntimeException("runtime exception")).when(ctxt).forward(any());
        when(ivmRequestDao.save(any())).thenReturn(new IVMRequest());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");

        AlertsInfo alertsInfo = getAlertsInfo(true);

        IVMNotifierResponse ivmResponse = (IVMNotifierResponse) ivmNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, ivmResponse.getStatus());
    }

    @Test
    public void testProcessAckDisposition() {
        IgniteEventImpl igniteEvent = getIgniteEvent(VEHICLE_MESSAGE_DISPOSITION_PUBLISH);
        VehicleMessageDispositionPublishData data = new VehicleMessageDispositionPublishData();
        data.setDisposition(VehicleMessageDispositionPublishData.MessageDispositionEnum.MESSAGE_DELETE);
        long time = System.currentTimeMillis();
        data.setMessageDisplayTimestamp(time);
        igniteEvent.setEventData(data);
        IVMRequest ivmRequest = getIvmRequest(time);

        AlertsHistoryInfo alertsHistory = new AlertsHistoryInfo();
        IVMNotifierResponse ivmNotifierResponse = new IVMNotifierResponse();
        alertsHistory.addChannelResponse(ivmNotifierResponse);

        doNothing().when(ctxt).forward(kafkaRecordArgumentCaptor.capture());
        when(ivmRequestDao.findByVehicleIdSessionId(any(), any())).thenReturn(Optional.of(ivmRequest));
        when(alertsHistoryDao.findById(any())).thenReturn(alertsHistory);
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor2.capture(), any());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");
        when(alertsHistoryDao.update(any())).thenReturn(true);

        ivmNotifier.processAck(igniteEvent);
        IgniteEventImpl forwardIgniteEvent = (IgniteEventImpl) kafkaRecordArgumentCaptor.getValue().value();

        assertEquals(VEHICLE_MESSAGE_DISPOSITION_ACK, forwardIgniteEvent.getEventId());
        assertEquals(VehicleMessageDispositionAckData.DispositionResponseEnum.SUCCESS,
            ((VehicleMessageDispositionAckData) forwardIgniteEvent.getEventData()).getResponse());
        assertEquals("biz12345", forwardIgniteEvent.getBizTransactionId());
        assertEquals(time, forwardIgniteEvent.getTimestamp());

        IgniteEventImpl forwardIgniteEvent2 = igniteEventArgumentCaptor2.getValue();
        VehicleMessageDispositionPublishData eventData =
            (VehicleMessageDispositionPublishData) forwardIgniteEvent2.getEventData();
        assertEquals("file12345", eventData.getFileName());
        assertEquals("HU4IVM0000000", eventData.getHarmanId());
        assertEquals(VehicleMessageDispositionPublishData.MessageDispositionEnum.MESSAGE_DELETE.toString(),
            ((IVMNotifierResponse) alertsHistory.getChannelResponses().get(0)).getUserStatus());
    }

    @Test
    public void testProcessAckDmaFeedback() {
        IgniteEventImpl igniteEvent = getIgniteEvent(DMA_FEEDBACK_EVENT);
        DeviceMessageFailureEventDataV1_0 data = new DeviceMessageFailureEventDataV1_0();
        data.setFailedIgniteEvent(getIgniteEvent(DMA_FEEDBACK_EVENT));
        data.setErrorCode(DeviceMessageErrorCode.MQTT_DISPATCH_FAILED);

        igniteEvent.setEventData(data);

        long time = System.currentTimeMillis();
        IVMRequest ivmRequest = getIvmRequest(time);

        AlertsHistoryInfo alertsHistory = new AlertsHistoryInfo();
        IVMNotifierResponse ivmNotifierResponse = new IVMNotifierResponse();
        alertsHistory.addChannelResponse(ivmNotifierResponse);

        when(ivmRequestDao.findByVehicleIdMessageId(any(), any())).thenReturn(Optional.of(ivmRequest));
        when(alertsHistoryDao.findById(any())).thenReturn(alertsHistory);
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor2.capture(), any());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");
        when(alertsHistoryDao.update(any())).thenReturn(true);

        ivmNotifier.processAck(igniteEvent);
        IgniteEventImpl forwardIgniteEvent2 = igniteEventArgumentCaptor2.getValue();
        IVMFeedbackData_V1 eventData = (IVMFeedbackData_V1) forwardIgniteEvent2.getEventData();
        assertEquals("file12345", eventData.getFileName());
        assertEquals("HU4IVM0000000", eventData.getHarmanId());
        assertEquals("Failure", eventData.getStatus());
        assertEquals(NotificationErrorCode.DELIVERY_CHANNEL_NOT_AVAILABLE.toString(),
            eventData.getErrorCode().toString());
        assertEquals(DeviceMessageErrorCode.MQTT_DISPATCH_FAILED.toString(),
            alertsHistory.getChannelResponses().get(0).getStatus());
        assertEquals(NotificationErrorCode.DELIVERY_CHANNEL_NOT_AVAILABLE,
            (alertsHistory.getChannelResponses().get(0)).getErrorCode());
    }

    @Test
    public void testProcessMessageAck() {

        IgniteEventImpl igniteEvent = getIgniteEvent(VEHICLE_MESSAGE_ACK);
        VehicleMessageAckData data = new VehicleMessageAckData();
        data.setStatus(VehicleMessageAckData.ResponseEnum.MESSAGE_AUTO_DELETE);
        igniteEvent.setEventData(data);

        long time = System.currentTimeMillis();
        IVMRequest ivmRequest = getIvmRequest(time);

        AlertsHistoryInfo alertsHistory = new AlertsHistoryInfo();
        IVMNotifierResponse ivmNotifierResponse = new IVMNotifierResponse();
        alertsHistory.addChannelResponse(ivmNotifierResponse);

        when(ivmRequestDao.findByVehicleIdMessageId(any(), any())).thenReturn(Optional.of(ivmRequest));
        when(alertsHistoryDao.findById(any())).thenReturn(alertsHistory);
        doNothing().when(ctxt).forwardDirectly(any(), igniteEventArgumentCaptor2.capture(), any());
        when(msgIdGen.generateUniqueMsgId(any())).thenReturn("12345");
        when(alertsHistoryDao.update(any())).thenReturn(true);

        ivmNotifier.processAck(igniteEvent);
        IgniteEventImpl forwardIgniteEvent2 = igniteEventArgumentCaptor2.getValue();
        VehicleMessageAckData eventData = (VehicleMessageAckData) forwardIgniteEvent2.getEventData();
        assertEquals("file12345", eventData.getFileName());
        assertEquals("HU4IVM0000000", eventData.getHarmanId());
        assertEquals(VehicleMessageAckData.ResponseEnum.MESSAGE_AUTO_DELETE, eventData.getStatus());
        assertEquals(NotificationConstants.SUCCESS, alertsHistory.getChannelResponses().get(0).getStatus());
        assertEquals(VehicleMessageAckData.ResponseEnum.MESSAGE_AUTO_DELETE.toString(),
            ((IVMNotifierResponse) alertsHistory.getChannelResponses().get(0)).getDeviceStatus());
        assertNull(alertsHistory.getChannelResponses().get(0).getErrorCode());
    }

    @Test
    public void testProcessAckIvmRequestNotPresent() {

        IgniteEventImpl igniteEvent = getIgniteEvent(VEHICLE_MESSAGE_ACK);
        VehicleMessageAckData data = new VehicleMessageAckData();
        data.setStatus(VehicleMessageAckData.ResponseEnum.MESSAGE_AUTO_DELETE);
        igniteEvent.setEventData(data);

        AlertsHistoryInfo alertsHistory = new AlertsHistoryInfo();
        IVMNotifierResponse ivmNotifierResponse = new IVMNotifierResponse();
        alertsHistory.addChannelResponse(ivmNotifierResponse);

        when(ivmRequestDao.findByVehicleIdMessageId(any(), any())).thenReturn(Optional.empty());

        ivmNotifier.processAck(igniteEvent);
        assertEquals(ivmNotifierResponse, alertsHistory.getChannelResponses().get(0));
    }

    @Test
    public void testProcessAckNoIvmChannel() {

        IgniteEventImpl igniteEvent = getIgniteEvent(VEHICLE_MESSAGE_ACK);
        VehicleMessageAckData data = new VehicleMessageAckData();
        data.setStatus(VehicleMessageAckData.ResponseEnum.MESSAGE_AUTO_DELETE);
        igniteEvent.setEventData(data);

        long time = System.currentTimeMillis();
        IVMRequest ivmRequest = getIvmRequest(time);

        AlertsHistoryInfo alertsHistory = new AlertsHistoryInfo();
        FCMChannelResponse fcmChannelResponse = new FCMChannelResponse();
        alertsHistory.addChannelResponse(fcmChannelResponse);

        when(ivmRequestDao.findByVehicleIdMessageId(any(), any())).thenReturn(Optional.of(ivmRequest));
        when(alertsHistoryDao.findById(any())).thenReturn(alertsHistory);
        when(alertsHistoryDao.update(any())).thenReturn(true);

        ivmNotifier.processAck(igniteEvent);
        assertEquals(fcmChannelResponse, alertsHistory.getChannelResponses().get(0));
    }

    @Test
    public void testProcessAckNoChannel() {

        IgniteEventImpl igniteEvent = getIgniteEvent(VEHICLE_MESSAGE_ACK);
        VehicleMessageAckData data = new VehicleMessageAckData();
        data.setStatus(VehicleMessageAckData.ResponseEnum.MESSAGE_AUTO_DELETE);
        igniteEvent.setEventData(data);

        long time = System.currentTimeMillis();
        IVMRequest ivmRequest = getIvmRequest(time);

        AlertsHistoryInfo alertsHistory = new AlertsHistoryInfo();
        alertsHistory.setChannelResponses(new ArrayList<>());

        when(ivmRequestDao.findByVehicleIdMessageId(any(), any())).thenReturn(Optional.of(ivmRequest));
        when(alertsHistoryDao.findById(any())).thenReturn(alertsHistory);
        when(alertsHistoryDao.update(any())).thenReturn(true);

        ivmNotifier.processAck(igniteEvent);
        assertEquals(0, alertsHistory.getChannelResponses().size());
    }

    @Test
    public void testSetters() {
        ivmNotifier.setProcessorContext(null);
        assertNull(ivmNotifier.destroyChannel("a", null));
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("a");
        assertEquals(ChannelType.IVM, ivmNotifier.setupChannel(notificationConfig).getChannelType());
    }

    @NotNull
    private IVMRequest getIvmRequest(long time) {
        IVMRequest ivmRequest = new IVMRequest();
        ivmRequest.setCampaignDate(String.valueOf(time));
        ivmRequest.setRequestId("requestId12345");
        ivmRequest.setCampaignId("campaignId12345");
        ivmRequest.setCountryCode("IL");
        ivmRequest.setNotificationId("ivm12345");
        ivmRequest.setHarmanId("HU4IVM0000000");
        ivmRequest.setFileName("file12345");
        return ivmRequest;
    }

    @NotNull
    private IgniteEventImpl getIgniteEvent(String dmaFeedbackEvent) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventId(dmaFeedbackEvent);
        igniteEvent.setVehicleId("HU4IVM0000000");
        igniteEvent.setMessageId("12345");
        igniteEvent.setRequestId("requestId12345");
        igniteEvent.setBizTransactionId("biz12345");
        igniteEvent.setCorrelationId("12345");
        return igniteEvent;
    }

    private Set<NotificationTemplate> getAllLanguageTemplate() {
        Set<NotificationTemplate> notificationTemplates = new HashSet<>();
        NotificationTemplate notificationTemplate = getNotificationTemplate();
        notificationTemplates.add(notificationTemplate);
        notificationTemplate = getNotificationTemplate();
        notificationTemplate.setLocale(Locale.forLanguageTag("es-ES"));
        notificationTemplate.setBrand("kia");
        notificationTemplate.getIvmTemplate().setTitle("es ivm title");
        notificationTemplates.add(notificationTemplate);
        return notificationTemplates;
    }

    @NotNull
    private AlertsInfo getAlertsInfo(boolean campaignNotification) throws IOException {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(campaignNotification ? VEHICLE_MESSAGE_PUBLISH : GENERIC_NOTIFICATION_EVENT);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setUserProfile(getUserProfile());
        if (!campaignNotification) {
            data.setAlertDataProperties(getGenericEventData());
        }
        alertsInfo.setAlertsData(data);
        alertsInfo.addNotificationTemplate("en-US", getNotificationTemplate());
        alertsInfo.setNotificationConfig(getNotificationConfig());
        alertsInfo.setNotificationConfigs(Collections.singletonList(getNotificationConfig()));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        if (campaignNotification) {
            igniteEvent.setEventData(getVehicleMessagePublishData());
        }

        igniteEvent.setMessageId("12345");
        igniteEvent.setVehicleId("HUXOIDDN2HUN11");
        alertsInfo.setIgniteEvent(igniteEvent);

        return alertsInfo;
    }

    private Map<String, Object> getGenericEventData() {
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", "SERVICE_NOTICE");
        map.put("messageDetailType", "RECALL_ID");
        map.put("message", new ArrayList<>());
        map.put("altPhoneNumber", "+972875489547698");
        map.put("buttonActions", Arrays.asList("Call", "OK"));
        map.put("callType", "Bluetooth");
        map.put("priority", 2);
        Map<String, Object> adMap = new HashMap<>();
        adMap.put("ad1", "adVal1");
        map.put("additionalData", adMap);
        Map<String, String> mpMap = new HashMap<>();
        mpMap.put("mp1", "mpVal1");
        map.put("messageParameters", mpMap);
        map.put("serviceMessageEventID", "se1");
        return map;
    }

    private VehicleMessagePublishData getVehicleMessagePublishData() throws IOException {
        return objectMapper.readValue("{\n"
            + "  \"notificationId\": \"S06_IVM_RECALL_NOTIFICATION\",\n"
            + "  \"campaignId\": \"kdlkdsfj093458\",\n"
            + "  \"messageTemplate\": \"vehicle message template\",\n"
            + "  \"messageType\": \"SERVICE_NOTICE\",\n"
            + "  \"message\": [\n"
            + "    {\n"
            + "      \"title\": \"You do not want to miss this offer !!!\",\n"
            + "      \"subtitle\": \"sub You do not want to miss this offer !!!\",\n"
            + "      \"language\": \"en-US\",\n"
            + "      \"additionalData\": {\"messageText1\":\"text1\"},\n"
            + "      \"messageText\": \"Your Deep Qualifies for a $20 discount on Synthetic Oil Change."
            + "Contact Parkway Chrylr Doge Deep Shyam to schedule an appointment\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"title\": \"Vous ne voulez pas manquer cette offre !!!\",\n"
            + "      \"subtitle\": \"fr sub Vous ne voulez pas manquer cette offre !!!\",\n"
            + "      \"language\": \"fr-FR\",\n"
            + "      \"additionalData\": {\"messageText1\":\"fr text1\"},\n"
            + "      \"messageText\": \"Votre Deep se qualifie pour un rabais de 20 $ sur la vidange synthétique. "
            + "Contactez Parkway Chrylr Doge Deep Shyam pour prendre rendez-vous\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"title\": \"No querrás perderte esta oferta !!!\",\n"
            + "      \"subtitle\": \"es sub No querrás perderte esta oferta !!!\",\n"
            + "      \"language\": \"es-ES\",\n"
            + "      \"additionalData\": {\"messageText1\":\"es text1\"},\n"
            + "      \"messageText\": \"Su Deep califica para un descuento de $ 20 en cambio de aceite sintético. "
            + "Póngase en contacto con Parkway Chyslr godge deep shyam para programar una cita\"\n"
            + "    }],\n"
            + "    \"altPhoneNumber\": \"\",\n"
            + "    \"callType\": \"Bluetooth\",\n"
            + "    \"priority\": 2,\n"
            + "    \"serviceMessageEventID\": \"ev1\",\n"
            + "    \"messageParameters\": {\"key1\":\"val1\"},\n"
            + "    \"additionalData\": {\"k2\":\"v2\"},\n"
            + "    \"buttonActions\": [\n"
            + "      \"Call\",\n"
            + "      \"OK\"\n"
            + "    ]\n"
            + "  }\n"
            + "}", VehicleMessagePublishData.class);
    }

    private NotificationConfig getNotificationConfig() throws IOException {
        NotificationConfig notificationConfig = objectMapper.readValue(
            "{\n"
                + "  \"userId\" : \"testUser\",\n"
                + "  \"vehicleId\" : \"HUXOIDDN2HUN11\",\n"
                + "  \"contactId\" : \"self\",\n"
                + "  \"group\" : \"ivm\",\n"
                + "  \"enabled\" : true,\n"
                + "  \"channels\" : [\n"
                + "    {\n"
                + "      \"emails\" : [\n"
                + "        \"shai.tanchuma@harman.com\"\n"
                + "      ],\n"
                + "      \"type\" : \"email\",\n"
                + "      \"enabled\" : true\n"
                + "    },\n"
                + "    {\n"
                + "      \"phones\" : [\n"
                + "        \" +972528542238\"\n"
                + "      ],\n"
                + "      \"type\" : \"sms\",\n"
                + "      \"enabled\" : true\n"
                + "    },\n"
                + "    {\n"
                + "      \"type\" : \"ivm\",\n"
                + "      \"enabled\" : true\n"
                + "    }\n"
                + "  ]\n"
                + "}", NotificationConfig.class);
        notificationConfig.setLocale("en-US");
        return notificationConfig;
    }

    private UserProfile getUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("testUser");
        userProfile.setFirstName("test");
        userProfile.addNickName(new NickName("MARUTI", "Vehicle123"));
        userProfile.setDefaultEmail("testUser@harman.com");
        return userProfile;
    }

    private NotificationGrouping getNotificationGrouping() {
        NotificationGrouping notificationGrouping = new NotificationGrouping();
        notificationGrouping.setNotificationId("S06_IVM_RECALL_NOTIFICATION");
        notificationGrouping.setGroup("ivm");
        notificationGrouping.setGroupType(GroupType.DEFAULT);
        notificationGrouping.setId("groupId");
        notificationGrouping.setService("service1");
        notificationGrouping.setCheckEntitlement(false);
        notificationGrouping.setMandatory(false);
        return notificationGrouping;
    }

    private NotificationTemplate getNotificationTemplate() {
        IVMTemplate ivmTemplate = new IVMTemplate();
        ivmTemplate.setBody("ivm body");
        ivmTemplate.setTitle("ivm title");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setIvm(ivmTemplate);
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setChannelTemplates(channelTemplates);
        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));
        notificationTemplate.setNotificationId("S06_IVM_RECALL_NOTIFICATION");
        notificationTemplate.setBrand("default");
        return notificationTemplate;
    }

}