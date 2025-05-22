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
import com.amazonaws.services.pinpoint.model.AmazonPinpointException;
import com.amazonaws.services.pinpoint.model.DeleteEndpointRequest;
import com.amazonaws.services.pinpoint.model.DeleteEndpointResult;
import com.amazonaws.services.pinpoint.model.EndpointMessageResult;
import com.amazonaws.services.pinpoint.model.EndpointResponse;
import com.amazonaws.services.pinpoint.model.EndpointUser;
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
import com.amazonaws.util.IOUtils;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.EndpointResult;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.AmazonPinpointEndpointDAO;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.email.EmailPayloadGenerator;
import org.eclipse.ecsp.notification.entities.AmazonPinpointEndpoint;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.PushTemplate;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.FILE_NAME;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_MISSING_DESTINATION;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

/**
 * AmazonPinpointEmailNotifierTest.
 *
 * @author MaKumari
 */
public class AmazonPinpointEmailNotifierTest {

    public static final int FOUR = 4;
    @Mock
    AmazonPinpointEmailNotifier amazonPinpointEmailNotifier;

    @Mock
    Properties properties;

    @Mock
    MetricRegistry metricRegistry;

    @Mock
    NotificationDao notificationDao;

    ObjectMapper objectMapper = new ObjectMapper();

    public static final String FILE_NAME_ATTACHMENTS = "unit_test_images.csv";

    @Mock
    ObjectMapper mapper;

    @Mock
    SendMessagesResult sendMessagesResult;

    @Mock
    private AmazonPinpoint client;

    @Mock
    private AmazonPinpointEndpointDAO amazonPinpointEndpointDao;

    private AmazonPinpointEndpoint amazonPinpointEndpoint = null;
    private Map<String, Map<String, String>> endpoints = new HashMap<>();
    private Map<String, String> channelEndpoints = new HashMap<>();

    private EmailPayloadGenerator emailPayloadGen = new EmailPayloadGenerator();

    /**
     * test set up.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        amazonPinpointEmailNotifier = new AmazonPinpointEmailNotifier();
        ReflectionTestUtils.setField(amazonPinpointEmailNotifier, "client", client);
        ReflectionTestUtils.setField(amazonPinpointEmailNotifier, "appId", "appId");
        ReflectionTestUtils.setField(amazonPinpointEmailNotifier, "pinpointEndpointDao", amazonPinpointEndpointDao);
        ReflectionTestUtils.setField(emailPayloadGen, "mapper", objectMapper);
        ReflectionTestUtils.setField(amazonPinpointEmailNotifier, "emailPayloadGen", emailPayloadGen);
        amazonPinpointEndpoint = new AmazonPinpointEndpoint();
        amazonPinpointEndpoint.setUserId("userId");
        channelEndpoints.put("address1", "endpoint1");
        channelEndpoints.put("address2", "endpoint2");
        channelEndpoints.put("address3", "endpoint3");
        endpoints.put(ChannelType.SMS.toString(), channelEndpoints);
        channelEndpoints = new HashMap<>();
        channelEndpoints.put("address11", "endpoint11");
        channelEndpoints.put("address22", "endpoint22");
        channelEndpoints.put("address33", "endpoint33");
        endpoints.put(ChannelType.EMAIL.toString(), channelEndpoints);
        amazonPinpointEndpoint.setEndpoints(endpoints);
        Mockito.when(amazonPinpointEndpointDao.findById(Mockito.anyString())).thenReturn(amazonPinpointEndpoint);
        Mockito.when(amazonPinpointEndpointDao.save(Mockito.any(AmazonPinpointEndpoint.class)))
            .thenReturn(amazonPinpointEndpoint);
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        Mockito.when(client.getEndpoint(Mockito.any(GetEndpointRequest.class))).thenReturn(getEndpointResult);
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
    }

    @Test
    public void doInit() {
        doReturn("aws.region").when(properties).getProperty(NotificationProperty.AWS_REGION);
        amazonPinpointEmailNotifier.init(properties, metricRegistry, notificationDao);
        Mockito.verify(properties, Mockito.times(FOUR)).getProperty(Mockito.any());

    }

    @Test
    public void doPublishWithoutUser() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().setUserProfile(null);
        ChannelResponse response = amazonPinpointEmailNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void sendEmailToSecondaryContactWithoutEmailAddress() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        EmailChannel emailChannel = alertsInfo.getNotificationConfig().getChannel(ChannelType.EMAIL);
        emailChannel.setEmails(new ArrayList<>());
        ChannelResponse response = amazonPinpointEmailNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_MISSING_DESTINATION, response.getStatus());
    }

    @Test
    public void sendEmailToSecondaryContactSuccess() throws Exception {

        doReturn(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS)).when(mapper).writeValueAsBytes(any());
        doReturn(getEmailAttachment(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS))).when(mapper)
            .readValue((byte[]) any(),
                (Class<EmailAttachment>) any());
        doReturn(sendMessagesResult).when(client).sendMessages(Mockito.any(SendMessagesRequest.class));
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        EndpointResponse resp = new EndpointResponse();
        resp.setId("endpointid");
        getEndpointResult.setEndpointResponse(resp);
        doReturn(getEndpointResult).when(client).getEndpoint(Mockito.any(GetEndpointRequest.class));
        AlertsInfo alertsInfo = getAlertsInfo();
        ChannelResponse response = amazonPinpointEmailNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void sendEmailToSecondaryContactFail() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        doReturn(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS)).when(mapper).writeValueAsBytes(any());
        doThrow(new IOException()).when(mapper).readValue((byte[]) any(), (Class<EmailAttachment>) any());
        doReturn(null).when(client).sendMessages(Mockito.any(SendMessagesRequest.class));
        ChannelResponse response = amazonPinpointEmailNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void sendEmailToSecondaryContactWithReachContent() throws Exception {

        doReturn(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS)).when(mapper).writeValueAsBytes(any());
        doReturn(getEmailAttachment(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS))).when(mapper)
            .readValue((byte[]) any(),
                (Class<EmailAttachment>) any());
        doReturn(sendMessagesResult).when(client).sendMessages(Mockito.any(SendMessagesRequest.class));
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        EndpointResponse resp = new EndpointResponse();
        resp.setId("endpointid");
        getEndpointResult.setEndpointResponse(resp);
        doReturn(getEndpointResult).when(client).getEndpoint(Mockito.any(GetEndpointRequest.class));
        AlertsInfo alertsInfo = getAlertsInfoReachContent();
        ChannelResponse response = amazonPinpointEmailNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void testDoPublishSetupChannel() {
        NotificationConfig notificationConfig = new NotificationConfig();
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        smsChannel.setPhones(Arrays.asList("124324354"));
        notificationConfig.setChannels(Arrays.asList(smsChannel));
        ChannelResponse response = amazonPinpointEmailNotifier.setupChannel(notificationConfig);
        assertEquals(null, response);
    }

    @Test
    public void testDoPublishDestroyChannel() {
        ChannelResponse response = amazonPinpointEmailNotifier.destroyChannel(new String(), new String());
        assertEquals(null, response);
    }

    @NotNull
    private AlertsInfo getAlertsInfo() throws IOException, MessagingException {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(GENERIC_NOTIFICATION_EVENT);
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setUserProfile(getUserProfile());

        alertsInfo.setAlertsData(data);
        alertsInfo.addNotificationTemplate("en-US", getNotificationTemplate());
        alertsInfo.setNotificationConfig(getNotificationConfig());
        alertsInfo.setNotificationConfigs(Collections.singletonList(getNotificationConfig()));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        igniteEvent.setMessageId("12345");
        igniteEvent.setVehicleId("HUXOIDDN2HUN11");
        igniteEvent.setEventData(new GenericEventData()); // ef
        alertsInfo.setIgniteEvent(igniteEvent);
        EventData eventData = alertsInfo.getIgniteEvent().getEventData();
        GenericEventData genericEventData = (GenericEventData) eventData;
        ZipFile zipFile = getZipFile();
        byte[] image = extractFileFromZip(zipFile, FILE_NAME_ATTACHMENTS);
        EmailAttachment newAttachment = getEmailAttachment(image);
        genericEventData.set("attachments", Collections.singletonList(newAttachment));

        return alertsInfo;
    }

    private ZipFile getZipFile() throws IOException {
        File file = new File(getClass().getClassLoader().getResource("images_example_unit_test.zip").getFile());
        return new ZipFile(file);
    }

    private byte[] extractFileFromZip(ZipFile zipFile, String fileName) throws MessagingException {
        try {
            ZipEntry zipEntry = zipFile.getEntry(fileName);
            InputStream is = zipFile.getInputStream(zipEntry);
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            throw new MessagingException();
        }
    }

    private EmailAttachment getEmailAttachment(byte[] image) {
        return new EmailAttachment(FILE_NAME, Base64.encodeBase64String(image), "image/*", true);
    }

    private NotificationConfig getNotificationConfig() throws IOException {
        NotificationConfig notificationConfig = objectMapper.readValue(
            "{\n"
                + "  \"userId\" : \"testUser\",\n"
                + "  \"vehicleId\" : \"HUXOIDDN2HUN11\",\n"
                + "  \"contactId\" : \"self\",\n"
                + "  \"group\" : \"push\",\n"
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
                + "      \"service\" : \"pushservice\",\n"
                + "      \"appPlatform\" : \"ANDROID\",\n"
                + "      \"deviceTokens\" : [\n"
                + "        \"edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14EmdkN"
                + "TOTH0DuC53SLI_EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h\"\n"
                + "      ],\n"
                + "      \"type\" : \"push\",\n"
                + "      \"enabled\" : true\n"
                + "    }\n"
                + "  ]\n"
                + "}",
            NotificationConfig.class);
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

    private NotificationTemplate getNotificationTemplate() {
        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setBody("push body");
        pushTemplate.setTitle("push title");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setPush(pushTemplate);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setBody("email body");
        emailTemplate.setTitle("email title");
        channelTemplates.setEmail(emailTemplate);
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setChannelTemplates(channelTemplates);
        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));
        notificationTemplate.setNotificationId("LOW_FUEL");
        notificationTemplate.setBrand("default");
        return notificationTemplate;
    }

    private NotificationTemplate getNotificationTemplateReachContent() {
        NotificationTemplate notificationTemplate = getNotificationTemplate();
        EmailTemplate emailTemplate = notificationTemplate.getEmailTemplate();
        emailTemplate.setRichContent(true);
        return notificationTemplate;
    }

    private AlertsInfo getAlertsInfoReachContent() throws IOException, MessagingException {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.addNotificationTemplate("en-US", getNotificationTemplateReachContent());
        return alertsInfo;
    }

    @Test
    public void testGetDeliveryStatus() {

        EndpointMessageResult endpointMessageResult = new EndpointMessageResult();
        endpointMessageResult.setAddress("address");
        endpointMessageResult.setDeliveryStatus("SUCCESSFUL");
        endpointMessageResult.setMessageId("messageId");
        endpointMessageResult.setStatusCode(HttpStatus.OK.value());
        endpointMessageResult.setStatusMessage("OK");
        endpointMessageResult.setUpdatedToken("");
        Map<String, EndpointMessageResult> result = new HashMap<>();
        result.put("endpointId", endpointMessageResult);
        Map<String, EndpointResult> channelResponse = amazonPinpointEmailNotifier.getDeliveryStatus(result);
        Assert.assertEquals(channelResponse.get("endpointId").getAddress(), endpointMessageResult.getAddress());
        Assert.assertEquals(channelResponse.get("endpointId").getDeliveryStatus(),
            endpointMessageResult.getDeliveryStatus());
        Assert.assertEquals(channelResponse.get("endpointId").getMessageId(), endpointMessageResult.getMessageId());
        Assert.assertEquals(channelResponse.get("endpointId").getStatusCode(), endpointMessageResult.getStatusCode());
        Assert.assertEquals(channelResponse.get("endpointId").getStatusMessage(),
            endpointMessageResult.getStatusMessage());
        Assert.assertEquals(channelResponse.get("endpointId").getUpdatedToken(),
            endpointMessageResult.getUpdatedToken());
    }

    @Test
    public void testUpdateEndpoint() {
        UpdateEndpointResult updateEndpointResult = new UpdateEndpointResult();
        MessageBody messageBody = new MessageBody();
        messageBody.setMessage("message");
        messageBody.setRequestID("requestID");
        updateEndpointResult.setMessageBody(messageBody);
        Mockito.when(client.updateEndpoint(Mockito.any(UpdateEndpointRequest.class))).thenReturn(updateEndpointResult);
        String endpointId = amazonPinpointEmailNotifier.updateEndpoint("userId",
            com.amazonaws.services.pinpoint.model.ChannelType.EMAIL,
            "abc@gmail.com");
        Assert.assertNotNull(endpointId);
        Mockito.when(client.updateEndpoint(Mockito.any(UpdateEndpointRequest.class)))
            .thenThrow(AmazonPinpointException.class);
        endpointId = amazonPinpointEmailNotifier.updateEndpoint("userId",
            com.amazonaws.services.pinpoint.model.ChannelType.EMAIL,
            "abc@gmail.com");
        Assert.assertNull(endpointId);
    }

    @Test
    public void testGetEndpoint() {

        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setAddress("abc@gmail.com");
        endpointResponse.setApplicationId("addId");
        endpointResponse.setChannelType(com.amazonaws.services.pinpoint.model.ChannelType.EMAIL);
        endpointResponse.setCohortId("83");
        EndpointUser user = new EndpointUser();
        user.setUserId("userId123");
        endpointResponse.setUser(user);
        endpointResponse.setEndpointStatus("ACTIVE");
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        getEndpointResult.setEndpointResponse(endpointResponse);
        Mockito.when(client.getEndpoint(Mockito.any(GetEndpointRequest.class))).thenReturn(getEndpointResult);
        EndpointResponse response = amazonPinpointEmailNotifier.getEndpoint("endpointId");
        Assert.assertEquals(response.getAddress(), endpointResponse.getAddress());
        Assert.assertEquals(response.getApplicationId(), endpointResponse.getApplicationId());
        Assert.assertEquals(response.getChannelType(), endpointResponse.getChannelType());
        Assert.assertEquals(response.getCohortId(), endpointResponse.getCohortId());
        Assert.assertEquals(response.getEndpointStatus(), endpointResponse.getEndpointStatus());
        Assert.assertEquals(response.getUser().getUserId(), endpointResponse.getUser().getUserId());
        Mockito.when(client.getEndpoint(Mockito.any(GetEndpointRequest.class)))
            .thenThrow(AmazonPinpointException.class);
        response = amazonPinpointEmailNotifier.getEndpoint("endpointId");
        Assert.assertNull(response);
    }

    @Test
    public void testDeleteEndpoint() {
        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setAddress("abc@gmail.com");
        endpointResponse.setApplicationId("addId");
        endpointResponse.setChannelType(com.amazonaws.services.pinpoint.model.ChannelType.EMAIL);
        EndpointUser user = new EndpointUser();
        user.setUserId("userId123");
        endpointResponse.setUser(user);
        endpointResponse.setCohortId("83");
        DeleteEndpointResult deleteEndpointResult = new DeleteEndpointResult();
        deleteEndpointResult.setEndpointResponse(endpointResponse);
        deleteEndpointResult.setEndpointResponse(endpointResponse);
        Mockito.when(client.deleteEndpoint(Mockito.any(DeleteEndpointRequest.class))).thenReturn(deleteEndpointResult);
        EndpointResponse response = amazonPinpointEmailNotifier.deleteEndpoint("endpointId");
        Assert.assertEquals(response.getAddress(), endpointResponse.getAddress());
        Assert.assertEquals(response.getApplicationId(), endpointResponse.getApplicationId());
        Assert.assertEquals(response.getChannelType(), endpointResponse.getChannelType());
        Assert.assertEquals(response.getCohortId(), endpointResponse.getCohortId());
        Assert.assertEquals(response.getUser().getUserId(), endpointResponse.getUser().getUserId());
        Mockito.when(client.deleteEndpoint(Mockito.any(DeleteEndpointRequest.class)))
            .thenThrow(AmazonPinpointException.class);
        response = amazonPinpointEmailNotifier.deleteEndpoint("endpointId");
        Assert.assertNull(response);
    }

    @Test
    public void testCreateEndpoint() {
        PhoneNumberValidateResult phoneNumberValidateResult = new PhoneNumberValidateResult();
        NumberValidateResponse numberValidateResponse = new NumberValidateResponse();
        numberValidateResponse.setPhoneType("MOBILE");
        phoneNumberValidateResult.setNumberValidateResponse(numberValidateResponse);
        Mockito.when(client.phoneNumberValidate(Mockito.any(PhoneNumberValidateRequest.class)))
            .thenReturn(phoneNumberValidateResult);

        UpdateEndpointResult updateEndpointResult = new UpdateEndpointResult();
        MessageBody messageBody = new MessageBody();
        messageBody.setMessage("message");
        messageBody.setRequestID("requestID");
        updateEndpointResult.setMessageBody(messageBody);
        Mockito.when(client.updateEndpoint(Mockito.any(UpdateEndpointRequest.class))).thenReturn(updateEndpointResult);

        EndpointResponse endpointResponse = new EndpointResponse();
        endpointResponse.setAddress("abc@gmail.com");
        endpointResponse.setApplicationId("addId");
        endpointResponse.setChannelType(com.amazonaws.services.pinpoint.model.ChannelType.SMS);
        endpointResponse.setCohortId("83");
        EndpointUser user = new EndpointUser();
        user.setUserId("userId123");
        endpointResponse.setUser(user);
        endpointResponse.setEndpointStatus("ACTIVE");
        GetEndpointResult getEndpointResult = new GetEndpointResult();
        getEndpointResult.setEndpointResponse(endpointResponse);
        Mockito.when(client.getEndpoint(Mockito.any(GetEndpointRequest.class))).thenReturn(getEndpointResult);
        PhoneNumberValidateResult phoneNumberValidateResult0 = new PhoneNumberValidateResult();
        NumberValidateResponse numberValidateResponse0 = new NumberValidateResponse();
        numberValidateResponse0.setPhoneType("MOBILE");
        phoneNumberValidateResult0.setNumberValidateResponse(numberValidateResponse0);
        Mockito.when(client.phoneNumberValidate(Mockito.any(PhoneNumberValidateRequest.class)))
            .thenReturn(phoneNumberValidateResult0);
        Mockito.when(amazonPinpointEndpointDao.findById(Mockito.anyString())).thenReturn(null);

        assertResponse(numberValidateResponse, phoneNumberValidateResult, updateEndpointResult, getEndpointResult);

    }

    private void assertResponse(NumberValidateResponse numberValidateResponse,
                           PhoneNumberValidateResult phoneNumberValidateResult,
                           UpdateEndpointResult updateEndpointResult, GetEndpointResult getEndpointResult) {
        Map<String, String> endpointMap = amazonPinpointEmailNotifier.updateEndpoints("userId",
            com.amazonaws.services.pinpoint.model.ChannelType.SMS,
            Arrays.asList("231231784634"));
        Assert.assertNotNull(endpointMap.get("231231784634"));

        Mockito.when(client.getEndpoint(Mockito.any(GetEndpointRequest.class)))
            .thenThrow(AmazonPinpointException.class);
        endpointMap =
            amazonPinpointEmailNotifier.updateEndpoints("userId", com.amazonaws.services.pinpoint.model.ChannelType.SMS,
                Arrays.asList("231231784634"));
        Assert.assertNull(endpointMap.get("231231784634"));

        Mockito.when(client.updateEndpoint(Mockito.any(UpdateEndpointRequest.class)))
            .thenThrow(AmazonPinpointException.class);
        endpointMap =
            amazonPinpointEmailNotifier.updateEndpoints("userId", com.amazonaws.services.pinpoint.model.ChannelType.SMS,
                Arrays.asList("231231784634"));
        Assert.assertNull(endpointMap.get("231231784634"));

        numberValidateResponse.setPhoneType("INVALID");
        phoneNumberValidateResult.setNumberValidateResponse(numberValidateResponse);
        Mockito.when(client.phoneNumberValidate(Mockito.any(PhoneNumberValidateRequest.class)))
            .thenReturn(phoneNumberValidateResult);
        endpointMap =
            amazonPinpointEmailNotifier.updateEndpoints("userId", com.amazonaws.services.pinpoint.model.ChannelType.SMS,
                Arrays.asList("231231784634"));
        Assert.assertNull(endpointMap.get("231231784634"));

        AmazonPinpointEndpoint amazonPinpointEndpoint = new AmazonPinpointEndpoint();
        amazonPinpointEndpoint.setUserId("userId");
        Map<String, Map<String, String>> channelMaps = new HashMap<>();
        endpointMap.put("231231784635", "12347393asda");
        channelMaps.put(ChannelType.SMS.toString(), endpointMap);
        amazonPinpointEndpoint.setEndpoints(channelMaps);
        Mockito.when(amazonPinpointEndpointDao.findById(Mockito.anyString())).thenReturn(amazonPinpointEndpoint);

        numberValidateResponse.setPhoneType("MOBILE");
        phoneNumberValidateResult.setNumberValidateResponse(numberValidateResponse);
        Mockito.when(client.phoneNumberValidate(Mockito.any(PhoneNumberValidateRequest.class)))
            .thenReturn(phoneNumberValidateResult);
        Mockito.when(client.updateEndpoint(Mockito.any(UpdateEndpointRequest.class))).thenReturn(updateEndpointResult);
        Mockito.when(client.getEndpoint(Mockito.any(GetEndpointRequest.class))).thenReturn(getEndpointResult);
        Map<String, String> endpoint = amazonPinpointEmailNotifier.updateEndpoints("userId",
            com.amazonaws.services.pinpoint.model.ChannelType.SMS,
            Arrays.asList("231231784634"));
        Assert.assertNotNull(endpoint.get("231231784634"));
        Assert.assertNotNull(endpoint.get("231231784635"));

        endpoint = amazonPinpointEmailNotifier.updateEndpoints("userId",
            com.amazonaws.services.pinpoint.model.ChannelType.EMAIL,
            Arrays.asList("test@gmail.com"));
        Assert.assertNotNull(endpoint);
        Assert.assertTrue(endpoint.containsKey("test@gmail.com"));
    }

    @Test
    public void testGetEndpointIdUserChannelAddr() {
        amazonPinpointEmailNotifier.getEndpointIdUserChannelAddr(ChannelType.EMAIL.toString(),
            "userId", "address");
        AmazonPinpointEndpoint amazonPinpointEndpoint = new AmazonPinpointEndpoint();
        amazonPinpointEndpoint.setUserId("userId");
        Map<String, Map<String, String>> channelMaps = new HashMap<>();
        Map<String, String> endpointMap = new HashMap<>();
        endpointMap.put("231231784635", "12347393asda");
        channelMaps.put(ChannelType.EMAIL.toString(), endpointMap);
        amazonPinpointEndpoint.setEndpoints(channelMaps);
        Mockito.when(amazonPinpointEndpointDao.findById(Mockito.anyString())).thenReturn(amazonPinpointEndpoint);
        String endpointId = amazonPinpointEmailNotifier.getEndpointIdUserChannelAddr(ChannelType.EMAIL.toString(),
            "userId", "231231784635");
        Assert.assertEquals("12347393asda", endpointId);
        notificationDao.getFieldsValueByFields(null, endpointId, null);
    }
}
