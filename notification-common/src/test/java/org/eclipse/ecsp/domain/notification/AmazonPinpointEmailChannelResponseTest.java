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

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.AWS_PINPOINT_EMAIL_PROVIDER;

/**
 * AmazonPinpointEmailChannelResponseTest class.
 */
public class AmazonPinpointEmailChannelResponseTest {

    AmazonPinpointEmailChannelResponse response = null;

    private final String twoHundred = "200";


    @Test
    public void testResponse() {
        response = new AmazonPinpointEmailChannelResponse();
        Assert.assertNotNull(response);

        response = new AmazonPinpointEmailChannelResponse("userID");
        Assert.assertNotNull(response);
        Assert.assertEquals("userID", response.getUserID());

        response = new AmazonPinpointEmailChannelResponse("userID", "pdID");
        Assert.assertNotNull(response);
        Assert.assertEquals("userID", response.getUserID());
        Assert.assertEquals("pdID", response.getPdid());

        response = new AmazonPinpointEmailChannelResponse("userID", "pdID", "{\n"
                +
            "                \"default\" : \"\"\n"
                +
            "            }");
        response.setTemplate(new EmailTemplate());
        response.setAlertData(null);
        response.setChannelType(ChannelType.EMAIL);
        response.setDestination("destination");
        response.setErrorCode(NotificationErrorCode.PROCESSING_ERROR);
        response.setProcessedTime("12234234");
        response.setStatus("SUCCESS");
        EndpointResult endpointResult = new EndpointResult();
        endpointResult.setAddress("address");
        endpointResult.setDeliveryStatus("deliveryStatus");
        endpointResult.setMessageId("messageId");
        endpointResult.setStatusCode(Integer.valueOf(twoHundred));
        endpointResult.setStatusMessage("statusMessage");
        endpointResult.setUpdatedToken("updatedToken");
        Map<String, EndpointResult> deliveryStatus = new HashMap<>();
        deliveryStatus.put("endpointId", endpointResult);
        response.setDeliveryStatus(deliveryStatus);

        deliveryStatus = response.getDeliveryStatus();
        Assert.assertNotNull(response.getTemplate());
        Assert.assertNull(response.getAlertData());
        Assert.assertEquals(ChannelType.EMAIL.toString(), response.getChannelType().toString());
        Assert.assertNull(response.getDefaultMessage());
        Assert.assertEquals("destination", response.getDestination());
        Assert.assertEquals(NotificationErrorCode.PROCESSING_ERROR, response.getErrorCode());
        Assert.assertEquals("12234234", response.getProcessedTime());
        Assert.assertEquals("SUCCESS", response.getStatus());
        Assert.assertEquals(AWS_PINPOINT_EMAIL_PROVIDER, response.getProvider());
        Assert.assertNotNull(response.getActualEvent());
        Assert.assertEquals(endpointResult.getAddress(), deliveryStatus.get("endpointId").getAddress());
        Assert.assertEquals(endpointResult.getDeliveryStatus(), deliveryStatus.get("endpointId").getDeliveryStatus());
        Assert.assertEquals(endpointResult.getMessageId(), deliveryStatus.get("endpointId").getMessageId());
        Assert.assertEquals(endpointResult.getStatusCode(), deliveryStatus.get("endpointId").getStatusCode());
        Assert.assertEquals(endpointResult.getStatusMessage(), deliveryStatus.get("endpointId").getStatusMessage());
        Assert.assertEquals(endpointResult.getUpdatedToken(), deliveryStatus.get("endpointId").getUpdatedToken());

    }

}
