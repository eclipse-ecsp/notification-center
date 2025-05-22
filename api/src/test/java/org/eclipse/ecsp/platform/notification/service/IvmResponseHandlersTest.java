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

package org.eclipse.ecsp.platform.notification.service;

import org.eclipse.ecsp.domain.notification.IVMAckResponse;
import org.eclipse.ecsp.domain.notification.IVMDispositionResponse;
import org.eclipse.ecsp.domain.notification.VehicleMessageAckData;
import org.eclipse.ecsp.domain.notification.VehicleMessageAckData.ResponseEnum;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData.MessageDispositionEnum;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

/**
 * IVMResponseHandlersTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("checkstyle:MagicNumber")
public class IvmResponseHandlersTest {

    @InjectMocks
    private VehicleDispositionHandler service;

    @InjectMocks
    private VehicleAckHandler ackHandlerService;

    @Mock
    private KafkaService kafkaService;

    @BeforeEach
    public void setup() {

        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void forwardIvmResponseTest() throws ExecutionException {

        IVMDispositionResponse response = new IVMDispositionResponse();
        response.setSessionId("bizid123");
        response.setMessageId("10021");
        response.setRequestId("requestid123");
        response.setVehicleId("vehicleId123");
        VehicleMessageDispositionPublishData data = new VehicleMessageDispositionPublishData();
        data.setVehicleMessageID(110023);
        data.setDisposition(MessageDispositionEnum.MESSAGE_AUTO_DELETE);
        data.setCampaignId("campaign-123");
        response.setVehicleMessageDispositionPublish(data);

        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());
        Assert.assertNotNull(response);
        service.processIvmResponse(response);
    }

    @Test
    public void forwardIvmAckTest() throws ExecutionException {
        IVMAckResponse ack = new IVMAckResponse();
        ack.setSessionId("bizid123");
        ack.setMessageId("10021");
        ack.setRequestId("requestid123");
        ack.setCorrelationId("10020");
        ack.setSourceDeviceId("devicemno1");
        VehicleMessageAckData ackData = new VehicleMessageAckData();
        ackData.setCampaignId("campaign-123");
        ackData.setStatus(ResponseEnum.CUSTOM_EXTENSION);
        ack.setVehicleMessageAck(ackData);

        doNothing().when(kafkaService).sendIgniteEvent(any(), any(), any());
        Assert.assertNotNull(ack);
        ackHandlerService.processIvmResponse(ack);
    }

    @Test
    public void forwardIvmResponseTestException() throws ExecutionException {

        IVMDispositionResponse response = new IVMDispositionResponse();
        response.setSessionId("bizid123");
        response.setMessageId("10021");
        response.setRequestId("requestid123");
        response.setVehicleId("vehicleId123");
        VehicleMessageDispositionPublishData data = new VehicleMessageDispositionPublishData();
        data.setVehicleMessageID(110023);
        data.setDisposition(MessageDispositionEnum.MESSAGE_AUTO_DELETE);
        data.setCampaignId("campaign-123");
        response.setVehicleMessageDispositionPublish(data);

        doThrow(new ExecutionException("exception", null)).when(kafkaService).sendIgniteEvent(any(), any(), any());
        Assert.assertNotNull(response);
        service.processIvmResponse(response);
    }

}
