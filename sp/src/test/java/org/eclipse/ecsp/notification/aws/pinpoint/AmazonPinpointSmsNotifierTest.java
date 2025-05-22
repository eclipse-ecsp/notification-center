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

package org.eclipse.ecsp.notification.aws.pinpoint;

import com.amazonaws.services.pinpoint.AmazonPinpoint;
import com.amazonaws.services.pinpoint.model.EndpointMessageResult;
import com.amazonaws.services.pinpoint.model.EndpointResponse;
import com.amazonaws.services.pinpoint.model.GetEndpointRequest;
import com.amazonaws.services.pinpoint.model.GetEndpointResult;
import com.amazonaws.services.pinpoint.model.MessageBody;
import com.amazonaws.services.pinpoint.model.MessageResponse;
import com.amazonaws.services.pinpoint.model.NumberValidateResponse;
import com.amazonaws.services.pinpoint.model.PhoneNumberValidateRequest;
import com.amazonaws.services.pinpoint.model.PhoneNumberValidateResult;
import com.amazonaws.services.pinpoint.model.SendMessagesRequest;
import com.amazonaws.services.pinpoint.model.SendMessagesResult;
import com.amazonaws.services.pinpoint.model.UpdateEndpointRequest;
import com.amazonaws.services.pinpoint.model.UpdateEndpointResult;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.AmazonPinpointEndpointDAO;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.eclipse.ecsp.notification.aws.sns.SnsUtils.getAlertsInfo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * AmazonPinpointSmsNotifierTest.
 *
 * @author MaKumari
 */
public class AmazonPinpointSmsNotifierTest {

    public static final int THREE = 3;
    @InjectMocks
    AmazonPinpointSmsNotifier amazonPinpointSmsNotifier;

    @Mock
    AmazonPinpoint client;

    @Mock
    Properties properties;

    @Mock
    MetricRegistry metricRegistry;

    @Mock
    NotificationDao notificationDao;

    @Mock
    SendMessagesResult sendMessagesResult;

    @Mock
    private AmazonPinpointEndpointDAO amazonPinpointEndpointDao;

    /**
     * test set up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        amazonPinpointSmsNotifier = new AmazonPinpointSmsNotifier();
        ReflectionTestUtils.setField(amazonPinpointSmsNotifier, "client", client);
        ReflectionTestUtils.setField(amazonPinpointSmsNotifier, "appId", "appId");
        ReflectionTestUtils.setField(amazonPinpointSmsNotifier, "pinpointEndpointDao", amazonPinpointEndpointDao);
        ReflectionTestUtils.setField(amazonPinpointSmsNotifier, "defaultSender", "IGNITE_HARMAN_COM");
        PhoneNumberValidateResult phoneNumberValidateResult = new PhoneNumberValidateResult();
        NumberValidateResponse numberValidateResponse = new NumberValidateResponse();
        numberValidateResponse.setPhoneType("MOBILE");
        phoneNumberValidateResult.setNumberValidateResponse(numberValidateResponse);
        Mockito.when(client.phoneNumberValidate(Mockito.any(PhoneNumberValidateRequest.class)))
            .thenReturn(phoneNumberValidateResult);
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        doReturn(getEndpointResult).when(client).getEndpoint(Mockito.any(GetEndpointRequest.class));
        sendMessagesResult = new SendMessagesResult();

        EndpointMessageResult endpointResult = new EndpointMessageResult();
        endpointResult.setAddress("address");
        endpointResult.setDeliveryStatus("deliveryStatus");
        endpointResult.setMessageId("messageId");
        endpointResult.setStatusCode(HttpStatus.OK.value());
        endpointResult.setStatusMessage("statusMessage");
        Map<String, EndpointMessageResult> result1 = new HashMap<>();
        result1.put("endpoint1", endpointResult);
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setEndpointResult(result1);
        sendMessagesResult.setMessageResponse(messageResponse);
        Mockito.when(client.sendMessages(Mockito.any(SendMessagesRequest.class))).thenReturn(sendMessagesResult);
        UpdateEndpointResult updateEndpointResult = new UpdateEndpointResult();
        MessageBody messageBody = new MessageBody();
        messageBody.setMessage("message");
        messageBody.setRequestID("requestID");
        updateEndpointResult.setMessageBody(messageBody);
        Mockito.when(client.updateEndpoint(Mockito.any(UpdateEndpointRequest.class))).thenReturn(updateEndpointResult);
    }

    @Test
    public void testInit() {
        doReturn("aws.region").when(properties).getProperty(NotificationProperty.AWS_REGION);
        amazonPinpointSmsNotifier.init(properties, metricRegistry, notificationDao);
        Mockito.verify(properties, Mockito.times(THREE)).getProperty(Mockito.any());
    }

    @Test
    public void testChannelSetUp() {
        NotificationConfig notificationConfig = new NotificationConfig();
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        smsChannel.setPhones(Arrays.asList("124324354"));
        notificationConfig.setChannels(Arrays.asList(smsChannel));
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        EndpointResponse resp = new EndpointResponse();
        resp.setId("endpointid");
        getEndpointResult.setEndpointResponse(resp);
        doReturn(getEndpointResult).when(client).getEndpoint(Mockito.any(GetEndpointRequest.class));
        notificationConfig.setChannels(Arrays.asList(smsChannel));
        amazonPinpointSmsNotifier.setupChannel(notificationConfig);
        Mockito.verify(client, Mockito.times(1)).getEndpoint(Mockito.any());
    }

    @Test
    public void getProtocolTest() {
        assertEquals(ChannelType.SMS.getProtocol(), amazonPinpointSmsNotifier.getProtocol());
    }

    @Test
    public void smsPublishWithoutUser() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().setUserProfile(null);
        ChannelResponse response = amazonPinpointSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void smsPublishUser() throws Exception {

        GetEndpointResult getEndpointResult = new GetEndpointResult();
        EndpointResponse resp = new EndpointResponse();
        resp.setId("endpointid");
        getEndpointResult.setEndpointResponse(resp);
        AlertsInfo alertsInfo = getAlertsInfo();
        doReturn(getEndpointResult).when(client).getEndpoint(Mockito.any(GetEndpointRequest.class));
        ChannelResponse response = amazonPinpointSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void sendNonRegisterSmsSuccessWithSenderId() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().getUserProfile().setUserId("");
        alertsInfo.getNotificationTemplate().getSmsTemplate().setSender("TEMPLATE_COM");
        Map<String, String> map = new HashMap<>();
        map.put("AWS_REQUEST_ID", "REQUEST_ID");
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        EndpointResponse resp = new EndpointResponse();
        resp.setId("endpointid");
        getEndpointResult.setEndpointResponse(resp);
        doReturn(getEndpointResult).when(client).getEndpoint(Mockito.any(GetEndpointRequest.class));
        ChannelResponse response = amazonPinpointSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }
}
