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

/**
 *
 */

package org.eclipse.ecsp.platform.notification.utility.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author MBadoni
 *
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application-test.properties")
public class NotificationConfigUpdateServiceTest {

    @InjectMocks
    NotificationConfigUpdateService configService;

    @Mock
    private RestTemplate restTemplate;

    @Value("${notification.config.update.url}")
    private String notificationConfigUpdateUrl;

    @Value("${notification.grouping.update.url}")
    private String notificationGroupingUpdateUrl;

    @Value("${notification.template.update.url}")
    private String notificationTemplateUpdateUrl;

    @Value("${notification.template.config.update.url}")
    private String notificationTemplateConfigUpdateUrl;

    @Value("${notification.grouping.delete.url}")
    private String notificationGroupingDeleteUrl;

    @Value("${notification.template.delete.url}")
    private String notificationTemplateDeleteUrl;


    /**
     * setup method.
     *
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(configService, "notificationConfigUpdateUrl", notificationConfigUpdateUrl);
        ReflectionTestUtils.setField(configService, "notificationGroupingUpdateUrl", notificationGroupingUpdateUrl);
        ReflectionTestUtils.setField(configService, "notificationTemplateUpdateUrl", notificationTemplateUpdateUrl);
        ReflectionTestUtils.setField(configService, "notificationTemplateConfigUpdateUrl",
            notificationTemplateConfigUpdateUrl);
        ReflectionTestUtils.setField(configService, "notificationGroupingDeleteUrl", notificationGroupingDeleteUrl);
        ReflectionTestUtils.setField(configService, "notificationTemplateDeleteUrl", notificationTemplateDeleteUrl);

        ReflectionTestUtils.setField(configService, "configFilePath", "src/test/resources/");
        ReflectionTestUtils.setField(configService, "region", "TEST");
        ReflectionTestUtils.setField(configService, "service", "test");
        ResponseEntity<String> response = new ResponseEntity<String>(HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.any(HttpEntity.class),
            ArgumentMatchers.<Class<String>>any())).thenReturn(response);
        ResponseEntity<Void> response2 = new ResponseEntity<Void>(HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.any(HttpEntity.class),
            ArgumentMatchers.<Class<Void>>any(),
            ArgumentMatchers.anyMap())).thenReturn(response2);
    }

    @Test
    public void testUpdateNotificationConfig() throws IOException {
        configService.processConfigUpdate();
        verify(restTemplate, times(4)).exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any());
    }

    @Test
    public void testExceptionUpdateNotificationConfig() throws IOException {

        String errorResponse = "{\r\n"
            + "  \"httpStatusCode\": 404,\r\n"
            + "  \"requestId\": \"18dabe20-3789-11ee-885a-eb11cf3fabaa\",\r\n"
            + "  \"code\": \"nc-1405\",\r\n"
            + "  \"reason\": \"grouping.does.not.exist\",\r\n"
            + "  \"message\": \"Grouping does not exist\",\r\n"
            + "  \"errors\": [\r\n"
            + "    {\r\n"
            + "      \"code\": \"nc-10704\",\r\n"
            + "      \"reason\": \"threesome.group.notificationId.service.doesn't.exist\",\r\n"
            + "      \"message\": \"Threesome of group, notification ID and service doesn't exist\"\r\n"
            + "    }\r\n"
            + "  ]\r\n"
            + "}";
        RestClientResponseException ex =
            new RestClientResponseException("test", 400, "mockstatuscode", null, errorResponse.getBytes(),
                Charset.defaultCharset());
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any())).thenThrow(ex);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<Void>>any())).thenThrow(ex);
        configService.processConfigUpdate();
        verify(restTemplate, times(4)).exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
            ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.<Class<String>>any());
    }
}
