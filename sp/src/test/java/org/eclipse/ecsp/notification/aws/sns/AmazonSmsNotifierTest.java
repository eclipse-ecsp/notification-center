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

package org.eclipse.ecsp.notification.aws.sns;

import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.UnsubscribeResult;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AmazonSNSSMSResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm;
import org.eclipse.ecsp.notification.userprofile.UserProfileIntegrationService;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_MISSING_DESTINATION;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.eclipse.ecsp.notification.aws.sns.SnsUtils.IGNITE_HARMAN_COM;
import static org.eclipse.ecsp.notification.aws.sns.SnsUtils.REQUEST_ID;
import static org.eclipse.ecsp.notification.aws.sns.SnsUtils.TEMPLATE_COM;
import static org.eclipse.ecsp.notification.aws.sns.SnsUtils.getAlertsInfo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * AmazonSmsNotifierTest.
 */
public class AmazonSmsNotifierTest {

    @InjectMocks
    AmazonSmsNotifier amazonSmsNotifier;

    @Mock
    AmazonSNSClient snsClient;

    @Mock
    NotificationDao notificationDao;

    @Captor
    private ArgumentCaptor<PublishRequest> publishRequestCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(amazonSmsNotifier, "defaultSender", IGNITE_HARMAN_COM);
    }

    @Test
    public void snsPublishWithoutUser() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().setUserProfile(null);
        ChannelResponse response = amazonSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void sendSmsToSecondaryContactWithoutPhoneNumber() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        SmsChannel smsChannel = alertsInfo.getNotificationConfig().getChannel(ChannelType.SMS);
        smsChannel.setPhones(new ArrayList<>());
        ChannelResponse response = amazonSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_MISSING_DESTINATION, response.getStatus());
    }

    @Test
    public void sendSmsToSecondaryContactPhoneNumber() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        SmsChannel smsChannel = alertsInfo.getNotificationConfig().getChannel(ChannelType.SMS);
        List<String> phoneList = new ArrayList<>();
        phoneList.add("234234234");
        smsChannel.setPhones(phoneList);
        ChannelResponse response = amazonSmsNotifier.doPublish(alertsInfo);
        assertEquals("Failure", response.getStatus());
    }

    @Test
    public void sendSmsToSecondaryContactPhoneNumberEmpty() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        SmsChannel smsChannel = alertsInfo.getNotificationConfig().getChannel(ChannelType.SMS);
        List<String> phoneList = new ArrayList<>();

        smsChannel.setPhones(phoneList);
        ChannelResponse response = amazonSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_MISSING_DESTINATION, response.getStatus());
    }


    @Test
    public void sendTopicSmsSuccessToNumber() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getNotificationTemplate().getSmsTemplate().setSender(TEMPLATE_COM);

        UnsubscribeResult unsubscribeResult = new UnsubscribeResult();
        Map<String, String> map = new HashMap<>();
        map.put("AWS_REQUEST_ID", REQUEST_ID);
        ResponseMetadata responseMetadata = new ResponseMetadata(map);
        unsubscribeResult.setSdkResponseMetadata(responseMetadata);
        doReturn(new ResponseMetadata(map)).when(snsClient).getCachedResponseMetadata(any(PublishRequest.class));
        doReturn(new PublishResult()).when(snsClient).publish(publishRequestCaptor.capture());
        ChannelResponse response = amazonSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
        assertEquals(REQUEST_ID, ((AmazonSNSSMSResponse) response).getTopics().get(0).getPubishTrackingID());
        assertEquals(TEMPLATE_COM,
                publishRequestCaptor.getValue().getMessageAttributes().get("AWS.SNS.SMS.SenderID").getStringValue());

    }


    @Test
    public void sendNonRegisterSmsSuccess() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().getUserProfile().setUserId("");
        Map<String, String> map = new HashMap<>();
        map.put("AWS_REQUEST_ID", REQUEST_ID);
        doReturn(new ResponseMetadata(map)).when(snsClient).getCachedResponseMetadata(any(PublishRequest.class));
        doReturn(new PublishResult()).when(snsClient).publish(publishRequestCaptor.capture());
        ChannelResponse response = amazonSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
        assertEquals("", ((AmazonSNSSMSResponse) response).getTopics().get(0).getTopicArn());
        assertEquals(REQUEST_ID, ((AmazonSNSSMSResponse) response).getTopics().get(0).getPubishTrackingID());
        assertEquals(IGNITE_HARMAN_COM,
                publishRequestCaptor.getValue().getMessageAttributes().get("AWS.SNS.SMS.SenderID").getStringValue());
        verify(snsClient, Mockito.times(0)).subscribe(any());
        verify(snsClient, Mockito.times(0)).createTopic(any(CreateTopicRequest.class));
        verify(snsClient, Mockito.times(0)).unsubscribe(anyString());
        verify(snsClient, Mockito.times(1)).publish(any());
    }

    @Test
    public void sendNonRegisterSmsSuccessWithSenderId() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().getUserProfile().setUserId("");
        alertsInfo.getNotificationTemplate().getSmsTemplate().setSender(TEMPLATE_COM);
        Map<String, String> map = new HashMap<>();
        map.put("AWS_REQUEST_ID", REQUEST_ID);
        doReturn(new ResponseMetadata(map)).when(snsClient).getCachedResponseMetadata(any(PublishRequest.class));
        doReturn(new PublishResult()).when(snsClient).publish(publishRequestCaptor.capture());
        ChannelResponse response = amazonSmsNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
        assertEquals("", ((AmazonSNSSMSResponse) response).getTopics().get(0).getTopicArn());
        assertEquals(REQUEST_ID, ((AmazonSNSSMSResponse) response).getTopics().get(0).getPubishTrackingID());
        assertEquals(TEMPLATE_COM,
                publishRequestCaptor.getValue().getMessageAttributes().get("AWS.SNS.SMS.SenderID").getStringValue());
        verify(snsClient, Mockito.times(0)).subscribe(any());
        verify(snsClient, Mockito.times(0)).createTopic(any(CreateTopicRequest.class));
        verify(snsClient, Mockito.times(0)).unsubscribe(anyString());
        verify(snsClient, Mockito.times(1)).publish(any());
    }

    @Test
    public void getProtocolTest() {
        assertEquals(ChannelType.SMS.getProtocol(), amazonSmsNotifier.getProtocol());
    }

    @Test
    public void sendInit() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(NotificationProperty.AWS_SNS_IOS_PUSH_PLATFORM_NAME, "APNS");
        properties.setProperty(NotificationProperty.AWS_SNS_PUSH_APP_NAME, "saas_notification_app_1");
        properties.setProperty(NotificationProperty.AWS_SNS_PUSH_CERT_KEY_FILENAME, "notification_ack_harman_prod1");
        properties.setProperty(NotificationProperty.AWS_SNS_PUSH_PRIVATE_KEY_FILENAME, "notification_apk_harman_prod1");
        properties.setProperty(NotificationProperty.ANDROID_SERVER_API_KEY_FILENAME, "android_api_key");
        properties.setProperty(NotificationProperty.AWS_SNS_TOPIC_ARN_PREFIX, "arn:aws:sns:us-east-1:381706862408:");
        ReflectionTestUtils.setField(amazonSmsNotifier, "properties", properties);
        ReflectionTestUtils.setField(amazonSmsNotifier, "isSMSAttributeSet", true);

        MetricRegistry metricRegistry = spy(MetricRegistry.class);
        Assertions.assertNotNull(metricRegistry);
        amazonSmsNotifier.init(properties, metricRegistry, notificationDao);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void platformEnums() {

        Platform.APNS.getSaasServiceName();
        Platform.APNS_SANDBOX.getSnsCompatibleName();
        Platform.GCM.toString();
        Platform.getType("apns");
        String str = PropertyNamesForFcm.ALERT_TIME_ZONE;

        UserProfileIntegrationService service = new UserProfileIntegrationService() {

            @Override
            public void processWebHookNotification(IgniteEvent webHookEvent) {

            }

            @Override
            public UserProfile processRealTimeUserUpdate(String uid, boolean persistUserProfile) {
                return null;
            }
        };
        service.processRealTimeUserUpdate(REQUEST_ID, null, false);
    }

}