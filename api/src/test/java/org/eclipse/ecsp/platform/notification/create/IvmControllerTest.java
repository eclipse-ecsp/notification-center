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

package org.eclipse.ecsp.platform.notification.create;

import org.eclipse.ecsp.domain.notification.IVMAckResponse;
import org.eclipse.ecsp.domain.notification.IVMDispositionResponse;
import org.eclipse.ecsp.domain.notification.VehicleMessageAckData;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData.MessageDispositionEnum;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.platform.notification.rest.IVMController;
import org.eclipse.ecsp.platform.notification.service.IvmResponseHandlerFactory;
import org.eclipse.ecsp.platform.notification.service.VehicleAckHandler;
import org.eclipse.ecsp.platform.notification.service.VehicleDispositionHandler;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

/**
 * IVMControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class IvmControllerTest {

    @Mock
    private IvmResponseHandlerFactory ivmHandler;

    @InjectMocks
    private IVMController ivmController;

    @Mock
    private KafkaService kafkaService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test(expected = NullPointerException.class)
    public void postIvmResponse() throws ExecutionException {
        IVMDispositionResponse response = new IVMDispositionResponse();
        response.setSessionId("bizid123");
        response.setMessageId("10021");
        response.setRequestId("requestid123");
        VehicleMessageDispositionPublishData data = new VehicleMessageDispositionPublishData();
        data.setVehicleMessageID(110023);
        data.setDisposition(MessageDispositionEnum.MESSAGE_AUTO_DELETE);
        data.setCampaignId("campaign-123");
        response.setVehicleMessageDispositionPublish(data);

        Mockito.when(ivmHandler.getIvmService(Mockito.anyString())).thenReturn(new VehicleDispositionHandler());
        ivmController.postIvmResponse("requestid123", null, null, "vehicleId123", response);
    }

    @Test(expected = NullPointerException.class)
    public void testPostIvmAck() {
        IVMAckResponse ack = new IVMAckResponse();
        ack.setSessionId("bizid123");
        ack.setMessageId("10021");
        ack.setRequestId("requestid123");
        ack.setVehicleMessageAck(new VehicleMessageAckData());
        Mockito.when(ivmHandler.getIvmService(Mockito.anyString())).thenReturn(new VehicleAckHandler());
        ivmController.postIvmAck("requestid123", null, null, "vehicleId123", ack);
    }
}
