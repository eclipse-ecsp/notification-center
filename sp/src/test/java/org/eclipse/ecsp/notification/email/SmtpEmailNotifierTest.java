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

package org.eclipse.ecsp.notification.email;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleMessageAckData;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * SMTPEmailNotifierTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class SmtpEmailNotifierTest {
    public static final String FILE_NAME = "unit_test_images.csv";

    ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    SmtpEmailNotifier smtpEmailNotifier;

    @Mock
    ObjectMapper mapper;
    @Mock
    MimeMessageHelper helperMock;
    @Mock
    MailService mailServiceImpl;
    @Mock
    Properties props;
    @Mock
    MetricRegistry metricRegistry;
    @Mock
    NotificationDao notificationDao;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoPublishWithoutUser() throws IOException, MessagingException {
        AlertsInfo alertsInfo = getAlertsInfo();
        alertsInfo.getAlertsData().setUserProfile(null);
        ChannelResponse response = smtpEmailNotifier.doPublish(alertsInfo);
        assertEquals(NOTIFICATION_STATUS_FAILURE, response.getStatus());
    }


    @Test
    public void testDoPublishAttachmentsSuccess() throws IOException, MessagingException {
        AlertsInfo alertsInfo = getAlertsInfo();
        doReturn(extractFileFromZip(getZipFile(), FILE_NAME)).when(mapper).writeValueAsBytes(any());
        doReturn(getEmailAttachment(extractFileFromZip(getZipFile(), FILE_NAME))).when(mapper)
            .readValue((byte[]) any(), (Class<EmailAttachment>) any());
        doReturn(helperMock).when(mailServiceImpl).prepareMimeMessage(any(), any(), any());
        doNothing().when(helperMock).addAttachment(any(), any(), any());
        doNothing().when(mailServiceImpl).sendEmail(any());
        ChannelResponse response = smtpEmailNotifier.doPublish(alertsInfo);
        assertEquals("shai.tanchuma@harman.com,efrat.sadomsky@harman.com,yonatan.frank@harman.com",
            response.getDestination());
        assertEquals("email body", response.getTemplate().getBody());
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void testDoPublishMimeMessageFails() throws IOException, MessagingException {
        AlertsInfo alertsInfo = getAlertsInfo();
        doThrow(new MessagingException()).when(mailServiceImpl).prepareMimeMessage(any(), any(), any());
        ChannelResponse response = smtpEmailNotifier.doPublish(alertsInfo);
        assertEquals("Failure", response.getStatus());
        verify(mapper, times(0)).writeValueAsBytes(any());
        verify(mapper, times(0)).readValue((byte[]) any(), (Class<EmailAttachment>) any());
        verify(mailServiceImpl, times(0)).sendEmail(any());

    }

    @Test
    public void testDoPublishAttachmentsFails() throws IOException, MessagingException {
        AlertsInfo alertsInfo = getAlertsInfo();
        doReturn(extractFileFromZip(getZipFile(), FILE_NAME)).when(mapper).writeValueAsBytes(any());
        doReturn(getEmailAttachment(extractFileFromZip(getZipFile(), FILE_NAME))).when(mapper)
            .readValue((byte[]) any(), (Class<EmailAttachment>) any());
        doReturn(helperMock).when(mailServiceImpl).prepareMimeMessage(any(), any(), any());
        doThrow(new MessagingException()).when(helperMock).addAttachment(any(), any(), any());
        ChannelResponse response = smtpEmailNotifier.doPublish(alertsInfo);
        assertEquals("Failure", response.getStatus());
        verify(mapper, times(2)).writeValueAsBytes(any());
        verify(mapper, times(2)).readValue((byte[]) any(), (Class<EmailAttachment>) any());
        verify(mailServiceImpl, times(0)).sendEmail(any());

    }

    @Test
    public void testDoPublishAttachmentsNotGeneralEvent() throws IOException, MessagingException {
        AlertsInfo alertsInfo = getAlertsInfoNotGenericEvent();
        doReturn(helperMock).when(mailServiceImpl).prepareMimeMessage(any(), any(), any());
        doNothing().when(mailServiceImpl).sendEmail(any());
        ChannelResponse response = smtpEmailNotifier.doPublish(alertsInfo);
        assertEquals("Success", response.getStatus());
    }

    @Test
    public void testDoPublishInit() {
        doReturn("nothing").when(props).getProperty(any());
        doNothing().when(mailServiceImpl).init(new Properties());
        smtpEmailNotifier.init(props, metricRegistry, notificationDao);
        Mockito.verify(mailServiceImpl, Mockito.times(1)).init(Mockito.any());

    }

    @Test
    public void testDoPublishSetupChannel() {
        ChannelResponse response = smtpEmailNotifier.setupChannel(new NotificationConfig());
        assertEquals(null, response);
    }

    @Test
    public void testDoPublishDestroyChannel() {
        ChannelResponse response = smtpEmailNotifier.destroyChannel(new String(), new String());
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
        igniteEvent.setEventData(new GenericEventData());
        alertsInfo.setIgniteEvent(igniteEvent);
        EventData eventData = alertsInfo.getIgniteEvent().getEventData();
        GenericEventData genericEventData = (GenericEventData) eventData;
        ZipFile zipFile = getZipFile();
        byte[] image = extractFileFromZip(zipFile, FILE_NAME);
        EmailAttachment newAttachment = getEmailAttachment(image);
        genericEventData.set("attachments", Collections.singletonList(newAttachment));

        return alertsInfo;
    }

    @NotNull
    private AlertsInfo getAlertsInfoNotGenericEvent() throws IOException, MessagingException {
        AlertsInfo alertsInfo = new AlertsInfo();
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setUserProfile(getUserProfile());
        alertsInfo.setAlertsData(data);
        alertsInfo.addNotificationTemplate("en-US", getNotificationTemplate());
        alertsInfo.setNotificationConfig(getNotificationConfig());
        alertsInfo.setNotificationConfigs(Collections.singletonList(getNotificationConfig()));
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(new VehicleMessageAckData());
        alertsInfo.setIgniteEvent(igniteEvent);
        EventData eventData = alertsInfo.getIgniteEvent().getEventData();
        VehicleMessageAckData notGenericEventData = (VehicleMessageAckData) eventData;
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
                + "        \"shai.tanchuma@harman.com\",\n"
                + "       \"efrat.sadomsky@harman.com\",\n"
                + "       \"yonatan.frank@harman.com\"\n"
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
                + "}", NotificationConfig.class);
        notificationConfig.setLocale("en-US");
        return notificationConfig;
    }


    private UserProfile getUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("testUser");
        return userProfile;
    }

    private NotificationTemplate getNotificationTemplate() {
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setBody("email body");
        ChannelTemplates channelTemplates = new ChannelTemplates();
        channelTemplates.setEmail(emailTemplate);
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setChannelTemplates(channelTemplates);
        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));
        notificationTemplate.setNotificationId("LOW_FUEL");
        notificationTemplate.setBrand("default");
        return notificationTemplate;
    }
}