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

package org.eclipse.ecsp.notification.processors;

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ApiPushChannel;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.IVMChannel;
import org.eclipse.ecsp.domain.notification.PortalChannel;
import org.eclipse.ecsp.domain.notification.PushChannel;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.entities.APIPushTemplate;
import org.eclipse.ecsp.notification.entities.ChannelTemplates;
import org.eclipse.ecsp.notification.entities.EmailTemplate;
import org.eclipse.ecsp.notification.entities.IVMTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.PortalTemplate;
import org.eclipse.ecsp.notification.entities.PushTemplate;
import org.eclipse.ecsp.notification.entities.SMSTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.CUSTOM_PLACEHOLDERS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * NotificationMsgGeneratorTest class.
 */
public class NotificationMsgGeneratorTest {

    @InjectMocks
    private NotificationMsgGenerator notificationMsgGenerator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void processAllChannelsSuccess() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.set("dynamicKey", "dynamicVal");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("user");
        notificationConfig.setGroup("group");
        notificationConfig.setVehicleId("1234");
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        ApiPushChannel apiPushChannel = new ApiPushChannel();
        apiPushChannel.setEnabled(true);
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(true);
        PortalChannel portalChannel = new PortalChannel();
        portalChannel.setEnabled(true);
        notificationConfig.setChannels(
            Arrays.asList(smsChannel, emailChannel, pushChannel, apiPushChannel, ivmChannel, portalChannel));
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));

        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        ChannelTemplates channelTemplates = new ChannelTemplates();

        getSmsTemplate(channelTemplates);

        getEmailTemplate(channelTemplates);

        getApiPushTemplate(channelTemplates);

        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setTitle("push-title [$.Data.dynamicKey]");
        channelTemplates.setPush(pushTemplate);

        IVMTemplate ivmTemplate = new IVMTemplate();
        ivmTemplate.setTitle("ivm-title [$.Data.dynamicKey]");
        channelTemplates.setIvm(ivmTemplate);

        PortalTemplate portalTemplate = new PortalTemplate();
        portalTemplate.setTitle("portal-title [$.Data.dynamicKey]");
        channelTemplates.setPortal(portalTemplate);

        notificationTemplate.setChannelTemplates(channelTemplates);
        Map<String, NotificationTemplate> localeToNotificationTemplateMap = new HashMap<>();
        localeToNotificationTemplateMap.put("en", notificationTemplate);

        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplateMap);

        notificationMsgGenerator.process(alertsInfo);
        assertEquals("sms-body dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("en").getSmsTemplate().getBody());
        assertEquals("email-title dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("en").getEmailTemplate().getTitle());
        assertEquals("api-push-sub-title dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("en").getApiPushTemplate().getSubtitle());
        assertEquals("push-title dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("en").getPushTemplate().getTitle());
        assertEquals("ivm-title dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("en").getIvmTemplate().getTitle());
        assertEquals("portal-title dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("en").getPortalTemplate().getTitle());
    }

    private void getApiPushTemplate(ChannelTemplates channelTemplates) {
        APIPushTemplate apiPushTemplate = new APIPushTemplate();
        apiPushTemplate.setSubtitle("api-push-sub-title [$.Data.dynamicKey]");
        channelTemplates.setApiPush(apiPushTemplate);
    }

    private void getEmailTemplate(ChannelTemplates channelTemplates) {
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTitle("email-title [$.Data.dynamicKey]");
        channelTemplates.setEmail(emailTemplate);
    }

    private void getSmsTemplate(ChannelTemplates channelTemplates) {
        SMSTemplate smsTemplate = new SMSTemplate();
        smsTemplate.setSender("sms-sender");
        smsTemplate.setBody("sms-body [$.Data.dynamicKey]");
        channelTemplates.setSms(smsTemplate);
    }

    @Test
    public void processSmsChannelsExceptionWithChannel() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.set("dynamicKey", "dynamicVal");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("user");
        notificationConfig.setGroup("group");
        notificationConfig.setVehicleId("1234");
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        notificationConfig.setLocale("en");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);

        notificationConfig.setChannels(Collections.singletonList(smsChannel));
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));

        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        ChannelTemplates channelTemplates = new ChannelTemplates();

        notificationTemplate.setChannelTemplates(channelTemplates);
        Map<String, NotificationTemplate> localeToNotificationTemplateMap = new HashMap<>();
        localeToNotificationTemplateMap.put("en", notificationTemplate);

        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplateMap);

        notificationMsgGenerator.process(alertsInfo);
        assertFalse(alertsInfo.getNotificationConfigs().get(0).getChannel(ChannelType.SMS).getEnabled());
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void processMultiLocaleEmailChannelsExceptionWithChannel() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.set("dynamicKey", "dynamicVal");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("user");
        notificationConfig.setGroup("group");
        notificationConfig.setVehicleId("1234");
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        notificationConfig.setLocale("pt-BR");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        List<Channel> channels = new ArrayList<>();
        channels.add(smsChannel);
        channels.add(emailChannel);
        notificationConfig.setChannels(channels);

        NotificationConfig notificationConfig1 = new NotificationConfig();
        notificationConfig1.setUserId("user");
        notificationConfig1.setGroup("group");
        notificationConfig1.setVehicleId("1234");
        notificationConfig1.setEnabled(true);
        notificationConfig1.setContactId("sec123");
        notificationConfig1.setLocale("en-CA");
        SmsChannel smsChannel1 = new SmsChannel();
        smsChannel1.setEnabled(true);
        EmailChannel emailChannel1 = new EmailChannel();
        emailChannel1.setEnabled(true);
        List<Channel> channels1 = new ArrayList<>();
        channels1.add(smsChannel1);
        channels1.add(emailChannel1);
        notificationConfig1.setChannels(channels1);
        List<NotificationConfig> notificationConfigs = new ArrayList<>();
        notificationConfigs.add(notificationConfig);
        notificationConfigs.add(notificationConfig1);
        alertsInfo.setNotificationConfigs(notificationConfigs);

        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        ChannelTemplates channelTemplates = new ChannelTemplates();

        SMSTemplate smsTemplate = new SMSTemplate();
        smsTemplate.setSender("sms-sender");
        smsTemplate.setBody("sms-body [$.Data.dynamicKey]");
        channelTemplates.setSms(smsTemplate);

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTitle("email-title [$.Data.dynamicKey]");
        channelTemplates.setEmail(emailTemplate);
        notificationTemplate.setChannelTemplates(channelTemplates);

        Map<String, NotificationTemplate> localeToNotificationTemplateMap = new HashMap<>();
        localeToNotificationTemplateMap.put("pt-BR", notificationTemplate);

        NotificationTemplate notificationTemplate1 = new NotificationTemplate();
        notificationTemplate1.setId("id");
        notificationTemplate1.setNotificationId("nid");
        ChannelTemplates channelTemplates1 = new ChannelTemplates();

        APIPushTemplate apiPushTemplate = new APIPushTemplate();
        apiPushTemplate.setSubtitle("api-push-sub-title [$.Data.dynamicKey]");
        channelTemplates1.setApiPush(apiPushTemplate);

        notificationTemplate1.setChannelTemplates(channelTemplates1);
        localeToNotificationTemplateMap.put("en-CA", notificationTemplate1);

        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplateMap);

        notificationMsgGenerator.process(alertsInfo);
        alertsInfo.getNotificationConfigs().forEach(config -> {
            if (config.getLocale().equalsIgnoreCase("pt-BR")) {
                assertTrue(config.getChannel(ChannelType.SMS).getEnabled());
                assertTrue(config.getChannel(ChannelType.EMAIL).getEnabled());
            } else {
                assertFalse(config.getChannel(ChannelType.SMS).getEnabled());
                assertFalse(config.getChannel(ChannelType.EMAIL).getEnabled());
            }
        });

    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void processAllLanguagesSuccess() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.set("dynamicKey", "dynamicVal");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("user");
        notificationConfig.setGroup("group");
        notificationConfig.setVehicleId("1234");
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        notificationConfig.setLocale("en");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        ApiPushChannel apiPushChannel = new ApiPushChannel();
        apiPushChannel.setEnabled(true);
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(true);
        PortalChannel portalChannel = new PortalChannel();
        portalChannel.setEnabled(true);
        notificationConfig.setChannels(
            Arrays.asList(smsChannel, emailChannel, pushChannel, apiPushChannel, ivmChannel, portalChannel));
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));

        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));
        ChannelTemplates channelTemplates = new ChannelTemplates();

        IVMTemplate ivmTemplate = new IVMTemplate();
        ivmTemplate.setTitle("ivm-title [$.Data.dynamicKey]");
        channelTemplates.setIvm(ivmTemplate);

        notificationTemplate.setChannelTemplates(channelTemplates);
        Map<String, NotificationTemplate> localeToNotificationTemplateMap = new HashMap<>();
        localeToNotificationTemplateMap.put("en", notificationTemplate);

        notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        notificationTemplate.setLocale(Locale.forLanguageTag("fr-FR"));
        channelTemplates = new ChannelTemplates();

        ivmTemplate = new IVMTemplate();
        ivmTemplate.setTitle("FR ivm-title [$.Data.dynamicKey]");
        channelTemplates.setIvm(ivmTemplate);
        notificationTemplate.setChannelTemplates(channelTemplates);
        localeToNotificationTemplateMap.put("fr", notificationTemplate);
        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplateMap);
        alertsInfo.setAllLanguageTemplates(
            new HashSet<>(Arrays.asList(alertsInfo.getLocaleToNotificationTemplate().get("en"),
                alertsInfo.getLocaleToNotificationTemplate().get("fr"))));

        notificationMsgGenerator.process(alertsInfo);
        assertEquals("ivm-title dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("en").getIvmTemplate().getTitle());
        assertEquals("FR ivm-title dynamicVal",
            alertsInfo.getLocaleToNotificationTemplate().get("fr").getIvmTemplate().getTitle());
        for (NotificationTemplate nt : alertsInfo.getAllLanguageTemplates()) {
            if (nt.getLocale().toLanguageTag().equals("en-US")) {
                assertEquals("ivm-title dynamicVal", nt.getIvmTemplate().getTitle());
            } else {
                assertEquals("FR ivm-title dynamicVal", nt.getIvmTemplate().getTitle());
            }
        }
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void processWithPlaceholdersWithoutTemplatePlaceholdersSuccess() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.set("dynamicKey", "dynamicVal");
        VehicleProfileAbridged vehicleProfileAbridged = new VehicleProfileAbridged();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("make", "kia");
        attrs.put("model", "sonet");
        vehicleProfileAbridged.setVehicleAttributes(attrs);
        data.setVehicleProfile(vehicleProfileAbridged);
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("user");
        notificationConfig.setGroup("group");
        notificationConfig.setVehicleId("1234");
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        ApiPushChannel apiPushChannel = new ApiPushChannel();
        apiPushChannel.setEnabled(true);
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(true);
        PortalChannel portalChannel = new PortalChannel();
        portalChannel.setEnabled(true);
        notificationConfig.setChannels(
            Arrays.asList(smsChannel, emailChannel, pushChannel, apiPushChannel, ivmChannel, portalChannel));
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));

        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        ChannelTemplates channelTemplates = new ChannelTemplates();

        SMSTemplate smsTemplate = new SMSTemplate();
        smsTemplate.setSender("sms-sender");
        smsTemplate.setBody(
            "sms-body [$.Data.dynamicKey] for [$.Data.vehicleProfile.make] - [$.Data.vehicleProfile.model]");
        channelTemplates.setSms(smsTemplate);

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTitle("email-title [$.Data.dynamicKey]");
        channelTemplates.setEmail(emailTemplate);

        APIPushTemplate apiPushTemplate = new APIPushTemplate();
        apiPushTemplate.setSubtitle("api-push-sub-title [$.Data.dynamicKey]");
        channelTemplates.setApiPush(apiPushTemplate);

        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setTitle("push-title [$.Data.dynamicKey]");
        channelTemplates.setPush(pushTemplate);

        IVMTemplate ivmTemplate = new IVMTemplate();
        ivmTemplate.setTitle("ivm-title [$.Data.dynamicKey]");
        channelTemplates.setIvm(ivmTemplate);

        PortalTemplate portalTemplate = new PortalTemplate();
        portalTemplate.setTitle("portal-title [$.Data.dynamicKey]");
        channelTemplates.setPortal(portalTemplate);

        notificationTemplate.setChannelTemplates(channelTemplates);
        Map<String, NotificationTemplate> localeToNotificationTemplateMap = new HashMap<>();
        localeToNotificationTemplateMap.put("en", notificationTemplate);

        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplateMap);

        Map<String, Map<String, String>> localeToPlaceholders = new HashMap<>();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("key1", "val1");
        localeToPlaceholders.put("en_US", placeholders);
        alertsInfo.setLocaleToPlaceholders(localeToPlaceholders);

        notificationMsgGenerator.process(alertsInfo);
        assertTrue(!alertsInfo.getLocaleToNotificationTemplate().toString().contains("<NOT_FOUND>"));
        assertTrue(CollectionUtils.isEmpty(
            (Collection<?>) alertsInfo.getAlertsData().getAlertDataProperties().get(CUSTOM_PLACEHOLDERS)));

    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void processWithPlaceholdersAndTemplatePlaceholdersSuccess() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.set("dynamicKey", "dynamicVal");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("user");
        notificationConfig.setGroup("group");
        notificationConfig.setVehicleId("1234");
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        ApiPushChannel apiPushChannel = new ApiPushChannel();
        apiPushChannel.setEnabled(true);
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(true);
        PortalChannel portalChannel = new PortalChannel();
        portalChannel.setEnabled(true);
        notificationConfig.setChannels(
            Arrays.asList(smsChannel, emailChannel, pushChannel, apiPushChannel, ivmChannel, portalChannel));
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));

        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        ChannelTemplates channelTemplates = new ChannelTemplates();

        SMSTemplate smsTemplate = new SMSTemplate();
        smsTemplate.setSender("sms-sender");
        smsTemplate.setBody("sms-body [$.Data.dynamicKey]");
        channelTemplates.setSms(smsTemplate);

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTitle("email-title [$.Data.dynamicKey]");
        channelTemplates.setEmail(emailTemplate);

        APIPushTemplate apiPushTemplate = new APIPushTemplate();
        apiPushTemplate.setSubtitle("api-push-sub-title [$.Data.dynamicKey]");
        channelTemplates.setApiPush(apiPushTemplate);

        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setTitle("push-title [$.Data.dynamicKey]");
        channelTemplates.setPush(pushTemplate);

        IVMTemplate ivmTemplate = new IVMTemplate();
        ivmTemplate.setTitle("ivm-title [$.Data.dynamicKey]");
        channelTemplates.setIvm(ivmTemplate);

        PortalTemplate portalTemplate = new PortalTemplate();
        portalTemplate.setTitle("portal-title [$.Data.dynamicKey]");
        channelTemplates.setPortal(portalTemplate);

        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));

        notificationTemplate.setChannelTemplates(channelTemplates);
        notificationTemplate.setCustomPlaceholders(Collections.singleton("key1"));

        Map<String, NotificationTemplate> localeToNotificationTemplateMap = new HashMap<>();
        localeToNotificationTemplateMap.put("en", notificationTemplate);

        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplateMap);

        Map<String, Map<String, String>> localeToPlaceholders = new HashMap<>();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("key1", "val1");
        localeToPlaceholders.put("en_US", placeholders);
        alertsInfo.setLocaleToPlaceholders(localeToPlaceholders);

        notificationMsgGenerator.process(alertsInfo);
        assertEquals("val1",
            ((HashMap<?, ?>) alertsInfo.getAlertsData().getAlertDataProperties().get(CUSTOM_PLACEHOLDERS)).get("key1"));

    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void processWithDynamicPlaceholdersAndTemplatePlaceholdersSuccess() {
        AlertsInfo.Data data = new AlertsInfo.Data();
        data.setNotificationId("GeofenceIn");
        data.set("dynamicKey1", "[$.Data.dynamicKey2]");
        data.set("dynamicKey2", "dynamicVal");
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setAlertsData(data);
        NotificationConfig notificationConfig = new NotificationConfig();
        notificationConfig.setUserId("user");
        notificationConfig.setGroup("group");
        notificationConfig.setVehicleId("1234");
        notificationConfig.setEnabled(true);
        notificationConfig.setContactId("self");
        SmsChannel smsChannel = new SmsChannel();
        smsChannel.setEnabled(true);
        EmailChannel emailChannel = new EmailChannel();
        emailChannel.setEnabled(true);
        PushChannel pushChannel = new PushChannel();
        pushChannel.setEnabled(true);
        ApiPushChannel apiPushChannel = new ApiPushChannel();
        apiPushChannel.setEnabled(true);
        IVMChannel ivmChannel = new IVMChannel();
        ivmChannel.setEnabled(true);
        PortalChannel portalChannel = new PortalChannel();
        portalChannel.setEnabled(true);
        notificationConfig.setChannels(
            Arrays.asList(smsChannel, emailChannel, pushChannel, apiPushChannel, ivmChannel, portalChannel));
        alertsInfo.setNotificationConfigs(Collections.singletonList(notificationConfig));

        NotificationTemplate notificationTemplate = new NotificationTemplate();
        notificationTemplate.setId("id");
        notificationTemplate.setNotificationId("nid");
        ChannelTemplates channelTemplates = new ChannelTemplates();

        SMSTemplate smsTemplate = new SMSTemplate();
        smsTemplate.setSender("sms-sender");
        smsTemplate.setBody("sms-body [$.Data.dynamicKey2]");
        channelTemplates.setSms(smsTemplate);

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTitle("email-title [$.Data.dynamicKey1]");
        channelTemplates.setEmail(emailTemplate);

        APIPushTemplate apiPushTemplate = new APIPushTemplate();
        apiPushTemplate.setSubtitle("api-push-sub-title [$.Data.dynamicKey2]");
        channelTemplates.setApiPush(apiPushTemplate);

        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setTitle("push-title [$.Data.dynamicKey2]");
        channelTemplates.setPush(pushTemplate);

        IVMTemplate ivmTemplate = new IVMTemplate();
        ivmTemplate.setTitle("ivm-title [$.Data.dynamicKey2]");
        channelTemplates.setIvm(ivmTemplate);

        PortalTemplate portalTemplate = new PortalTemplate();
        portalTemplate.setTitle("portal-title [$.Data.dynamicKey2]");
        channelTemplates.setPortal(portalTemplate);

        notificationTemplate.setLocale(Locale.forLanguageTag("en-US"));

        notificationTemplate.setChannelTemplates(channelTemplates);
        notificationTemplate.setCustomPlaceholders(Collections.singleton("key1"));

        Map<String, NotificationTemplate> localeToNotificationTemplateMap = new HashMap<>();
        localeToNotificationTemplateMap.put("en", notificationTemplate);

        alertsInfo.setLocaleToNotificationTemplate(localeToNotificationTemplateMap);

        Map<String, Map<String, String>> localeToPlaceholders = new HashMap<>();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("key1", "val1");
        localeToPlaceholders.put("en_US", placeholders);
        alertsInfo.setLocaleToPlaceholders(localeToPlaceholders);

        notificationMsgGenerator.process(alertsInfo);
        assertEquals("val1",
            ((HashMap<?, ?>) alertsInfo.getAlertsData().getAlertDataProperties().get(CUSTOM_PLACEHOLDERS)).get("key1"));

    }

}