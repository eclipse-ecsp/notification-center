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

package org.eclipse.ecsp.notification.aws.ses;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.email.EmailPayloadGenerator;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.PushTemplate;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.FILE_NAME;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_MISSING_DESTINATION;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * SES test.
 */
public class AmazonSesNotifierTest {
    public static final String FILE_NAME_ATTACHMENTS = "unit_test_images.csv";
    public static final int FOUR = 4;

    ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    ObjectMapper mapper;

    @Mock
    AmazonSimpleEmailService sesService;

    @Mock
    SendRawEmailResult sendRawEmailResult;

    @Mock
    Properties properties;

    @Mock
    MetricRegistry metricRegistry;

    @Mock
    NotificationDao notificationDao;

    @Mock
    AmazonSesBounceHandler bounceHandler;

    private EmailPayloadGenerator emailPayloadGen = new EmailPayloadGenerator();

    @InjectMocks
    AmazonSesNotifier amazonSesNotifier;

    /**
     * set up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(amazonSesNotifier, "emailPayloadGen", emailPayloadGen);
        ReflectionTestUtils.setField(emailPayloadGen, "mapper", objectMapper);
    }

    @Test
    public void doPublishWithoutUser() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().setUserProfile(null);
        ChannelResponse response = amazonSesNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void doInit() {
        doReturn("aws.region").when(properties).getProperty(NotificationProperty.AWS_REGION);
        doReturn("aws.ses.endpoint.name").when(properties).getProperty(NotificationProperty.AWS_SES_ENDPOINT_NAME);
        doReturn("aws.ses.bounce.handler.enable").when(properties)
            .getProperty(NotificationProperty.AWS_SES_BOUCE_HANDLER_ENABLE);
        amazonSesNotifier.init(properties, metricRegistry, notificationDao);
        Mockito.verify(properties, Mockito.times(FOUR)).getProperty(Mockito.any());

    }

    @Test
    public void doInitSesEndpointIsEmpty() {
        doReturn("aws.region").when(properties).getProperty(NotificationProperty.AWS_REGION);
        doReturn(null).when(properties).getProperty(NotificationProperty.AWS_SES_ENDPOINT_NAME);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> amazonSesNotifier.init(properties, metricRegistry, notificationDao));
        assertTrue(exception.getMessage().contains("Property aws.ses.endpoint.name not defined"));
    }

    @Test
    public void doInitAwsRegionIsEmpty() {
        doReturn(null).when(properties).getProperty(NotificationProperty.AWS_REGION);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> amazonSesNotifier.init(properties, metricRegistry, notificationDao));
        assertTrue(exception.getMessage().contains("Property aws.region not defined"));
    }

    @Test
    public void sendEmailToSecondaryContactWithoutEmailAddress() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfo();
        EmailChannel emailChannel = alertsInfo.getNotificationConfig().getChannel(ChannelType.EMAIL);
        emailChannel.setEmails(new ArrayList<>());
        ChannelResponse response = amazonSesNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_MISSING_DESTINATION, response.getStatus());
    }

    @Test
    public void sendEmailToSecondaryContactSuccess() throws Exception {

        doReturn(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS)).when(mapper).writeValueAsBytes(any());
        doReturn(getEmailAttachment(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS))).when(mapper)
            .readValue((byte[]) any(),
                (Class<EmailAttachment>) any());
        doReturn(sendRawEmailResult).when(sesService).sendRawEmail(any());
        ReflectionTestUtils.setField(amazonSesNotifier, "enableSesBounceHandler", Boolean.TRUE);
        AlertsInfo alertsInfo = getAlertsInfo();
        when(bounceHandler.bounced(any())).thenReturn(true);
        ChannelResponse response = amazonSesNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void sendEmailToSecondaryContactFail() throws Exception {
        ReflectionTestUtils.setField(emailPayloadGen, "mapper", mapper);

        doReturn(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS)).when(mapper).writeValueAsBytes(any());
        doThrow(new IOException()).when(mapper).readValue((byte[]) any(), (Class<EmailAttachment>) any());
        doReturn(sendRawEmailResult).when(sesService).sendRawEmail(any());
        ReflectionTestUtils.setField(amazonSesNotifier, "enableSesBounceHandler", Boolean.TRUE);
        AlertsInfo alertsInfo = getAlertsInfo();
        when(bounceHandler.bounced(any())).thenReturn(true);
        ChannelResponse response = amazonSesNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }

    @Test
    public void sendEmailToSecondaryContactWithReachContent() throws Exception {
        AlertsInfo alertsInfo = getAlertsInfoReachContent();
        doReturn(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS)).when(mapper).writeValueAsBytes(any());
        doReturn(getEmailAttachment(extractFileFromZip(getZipFile(), FILE_NAME_ATTACHMENTS))).when(mapper)
            .readValue((byte[]) any(),
                (Class<EmailAttachment>) any());
        doReturn(sendRawEmailResult).when(sesService).sendRawEmail(any());
        ChannelResponse response = amazonSesNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_SUCCESS, response.getStatus());
    }

    @Test
    public void testDoPublishSetupChannel() {
        ChannelResponse response = amazonSesNotifier.setupChannel(new NotificationConfig());
        assertEquals(null, response);
    }

    @Test
    public void testDoPublishDestroyChannel() {
        ChannelResponse response = amazonSesNotifier.destroyChannel(new String(), new String());
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
                + "        \"edzhfG4utqc:APA91bFXzG6azHDy5RW-95cz42A6pA18j0LlqA7SsSGXPaMGJGv9PvTW8RQBojD14Emdk"
                + "NTOTH0DuC53SLI_EgKJ3B25Plh-WT1VT9lunjhcXYV5pU7jxG7IhhyTqusulNmwnTArt14h\"\n"
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
}