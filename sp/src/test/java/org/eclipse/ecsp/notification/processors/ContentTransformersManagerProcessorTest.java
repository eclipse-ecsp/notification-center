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


import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.BaseTemplate;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.NotificationTemplateProcessorTest;
import org.eclipse.ecsp.notification.config.Configurations;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.processors.transformers.AlertsInfoToDtoConverter;
import org.eclipse.ecsp.notification.processors.transformers.CustomDtoConverter;
import org.eclipse.ecsp.notification.processors.transformers.DemoToUpperCaseContentTransformer;
import org.eclipse.ecsp.notification.processors.transformers.DemoUserNameContentTransformer;
import org.eclipse.ecsp.notification.processors.transformers.PlaceholderResolver;
import org.eclipse.ecsp.notification.processors.transformers.SlowTransformer;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * ContentTransformersManagerProcessorTest class.
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ContentTransformersManagerProcessor.class,
    DemoToUpperCaseContentTransformer.class, DemoUserNameContentTransformer.class, PlaceholderResolver.class,
    SlowTransformer.class,
    Configurations.class, ConfigBeanLoader.class})
@TestPropertySource("/notification-dao-test.properties")
public class ContentTransformersManagerProcessorTest {
    @Autowired
    ContentTransformersManagerProcessor contentTransformersManagerProcessor;

    @Autowired
    AlertsInfoToDtoConverter alertsInfoToDtoConverter;

    @Test
    public void testConverterOverloaded() {
        Class<? extends AlertsInfoToDtoConverter> converterClass = alertsInfoToDtoConverter.getClass();
        assertEquals(CustomDtoConverter.class, converterClass);
    }

    @Test
    public void testProcessNormal() throws IOException {
        AlertsInfo alertsInfo = getAlertsInfo();

        contentTransformersManagerProcessor.process(alertsInfo);

        Set<ChannelType> availableChannelTypes = alertsInfo.resolveAvailableChannelTypes();
        alertsInfo.getLocaleToNotificationTemplate().values().stream()
            .filter(f -> f.getNotificationId().equals("ContentTransformersTest")).forEach(template -> {
                for (ChannelType ct : availableChannelTypes) {
                    BaseTemplate channelTemplate = template.getChannelTemplate(ct);
                    assertEquals("This is a unit test for the BEST "
                                    +
                                    "notification content transformer for "
                                    +
                                    "Thomas Anderson",
                        channelTemplate.getBody());
                    if (ct == ChannelType.API_PUSH || ct == ChannelType.MOBILE_APP_PUSH) {
                        assertEquals("Goodbye Thomas Anderson", channelTemplate.getSubtitle());
                        assertEquals("Hello Thomas (Neo) Anderson", channelTemplate.getTitle());
                    }
                }
            });
    }

    @Test
    public void testProcessMissingTransformer() throws IOException {
        AlertsInfo alertsInfo = getAlertsInfo();

        contentTransformersManagerProcessor.process(alertsInfo);

        alertsInfo.getLocaleToNotificationTemplate().values().stream()
            .filter(f -> f.getNotificationId().equals("ContentTransformersTest-unknownTransformer"))
            .forEach(template -> {
                BaseTemplate channelTemplate = template.getChannelTemplate(ChannelType.MOBILE_APP_PUSH);
                assertEquals("This is a unit test for the best notification "
                                +
                                "content transformer for Thomas Anderson",
                    channelTemplate.getBody());
                assertEquals("Goodbye cat", channelTemplate.getSubtitle());
                assertEquals("Hello Neo", channelTemplate.getTitle());
            });
    }


    @Test
    public void testProcessTimeoutTransformer() throws IOException {
        AlertsInfo alertsInfo = getAlertsInfo();

        contentTransformersManagerProcessor.process(alertsInfo);

        alertsInfo.getLocaleToNotificationTemplate().values().stream()
            .filter(f -> f.getNotificationId().equals("ContentTransformersTest-timeouts")).forEach(template -> {
                BaseTemplate channelTemplate = template.getChannelTemplate(ChannelType.MOBILE_APP_PUSH);
                assertEquals(
                    "This is a timeout failed on timeout 70 MS unit"
                            +
                            " test for the BEST notification content transformer "
                            +
                            "for Thomas Anderson",
                    channelTemplate.getBody());
                assertEquals("Goodbye Thomas Anderson, "
                                +
                                "now we will throw an exception :"
                                +
                                " failed on timeout NaN MS",
                    channelTemplate.getSubtitle());
                assertEquals(
                    "Hello Thomas (Neo) Anderson, "
                            +
                            "now we will wait done sleeping 20 MS, done sleeping 40 MS,"
                            +
                            " failed on timeout 50 MS",
                    channelTemplate.getTitle());
            });
    }

    @Test
    public void testProcessExceptionsTransformer() throws IOException {
        AlertsInfo alertsInfo = getAlertsInfo();

        contentTransformersManagerProcessor.process(alertsInfo);

        alertsInfo.getLocaleToNotificationTemplate().values().stream()
            .filter(f -> f.getNotificationId().equals("ContentTransformersTest-exceptions")).forEach(template -> {
                BaseTemplate channelTemplate = template.getChannelTemplate(ChannelType.MOBILE_APP_PUSH);
                assertEquals(
                    "Hello Thomas (Neo) Anderson, now we will wait "
                            +
                            "failed on exception because Nana is NaN, failed on "
                            +
                            "exception because Banana is NaN, failed on exception "
                            +
                            "because Janana is NaN",
                    channelTemplate.getTitle());
                assertEquals(
                    "This is a timeout failed on timeout 100 MS unit test "
                            +
                            "for the BEST notification content transformer for "
                            +
                            "Thomas Anderson. failed on exception because NaN-2 is NaN, "
                            +
                            "failed on exception because NaN-3 is NaN",
                    channelTemplate.getBody());
                assertEquals(
                    "Goodbye Thomas Anderson, now we will throw an"
                            +
                            " exception : failed on exception because "
                            +
                            "NaN is NaN",
                    channelTemplate.getSubtitle());
            });
    }

    @Test
    public void testProcessDisabledTransformer() throws IOException {
        AlertsInfo alertsInfo = getAlertsInfo();

        contentTransformersManagerProcessor.process(alertsInfo);

        alertsInfo.getLocaleToNotificationTemplate().values().stream()
            .filter(f -> f.getNotificationId().equals("ContentTransformersTest-disabled")).forEach(template -> {
                BaseTemplate channelTemplate = template.getChannelTemplate(ChannelType.MOBILE_APP_PUSH);
                assertEquals("Hello Thomas (Neo) Anderson", channelTemplate.getTitle());
                assertEquals(
                    "This is a unit test for the dog and cat notification "
                            +
                            "content transformer for Thomas Anderson.",
                    channelTemplate.getBody());
                assertEquals("Goodbye Thomas Anderson, now we will all die", channelTemplate.getSubtitle());
            });
    }

    @NotNull
    private AlertsInfo getAlertsInfo() throws IOException {
        List<NotificationTemplate> templates = JsonUtils
            .getListObjects(
                IOUtils.toString(
                        NotificationTemplateProcessorTest
                            .class
                                .getResourceAsStream("/NotificationTemplateContentTransformers.json"),
                        "UTF-8"),
                NotificationTemplate.class);

        AlertsInfo alertsInfo = new AlertsInfo();
        templates.forEach(
            template -> alertsInfo.addNotificationTemplate(template.getLocale().toLanguageTag(), template));



        UserProfile profile = new UserProfile();
        profile.setFirstName("Thomas");
        profile.setLastName("Anderson");
        profile.addNickName(new NickName("Neo", "Trinity"));
        profile.setLocale(Locale.US);
        profile.setUserId("The One");
        NotificationConfig config = JsonUtils.bindData(
                IOUtils.toString(NotificationTemplateProcessorTest
                                     .class
                                          .getResourceAsStream("/NotificationConfig.json"),
                        "UTF-8"),
                NotificationConfig.class);
        alertsInfo.addNotificationConfig(config);
        alertsInfo.getAlertsData().setUserProfile(profile);
        return alertsInfo;
    }

}