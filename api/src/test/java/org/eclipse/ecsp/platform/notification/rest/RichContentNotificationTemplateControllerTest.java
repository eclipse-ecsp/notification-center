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

package org.eclipse.ecsp.platform.notification.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.platform.notification.dto.MissingNotificationTemplates;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.service.RichContentNotificationTemplateService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * RichContentNotificationTemplateControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RichContentNotificationTemplateControllerTest {
    private static final String SESSION_ID = "SESSION_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";
    private static final String REQUEST_ID = "REQ";
    public static final String NOTIFICATION_ID = "apple";

    @InjectMocks
    private RichContentNotificationTemplateController richContentNotificationTemplateController;

    @Mock
    RichContentNotificationTemplateService richContentNotificationTemplateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void importNotificationTemplate() throws Exception {
        String message = "Hello";
        Mockito.doReturn(message).when(richContentNotificationTemplateService)
            .importNotificationTemplate(Mockito.any(), Mockito.any(Boolean.class));
        ResponseEntity<String> responseEntity =
            richContentNotificationTemplateController.importNotificationTemplate(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, null);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody());
        List<String> requestId = responseEntity.getHeaders().get("requestId");
        assert requestId != null;
        assertEquals(REQUEST_ID, requestId.get(0));
    }

    @Test
    public void importNotificationTemplateV2() throws Exception {
        String message = "Hello";
        Mockito.doReturn(message).when(richContentNotificationTemplateService)
            .importNotificationTemplate(Mockito.any(), Mockito.any(Boolean.class));
        ResponseEntity<String> responseEntity =
            richContentNotificationTemplateController.importNotificationTemplateByIdLocaleBrand(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, null);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody());
        List<String> requestId = responseEntity.getHeaders().get("requestId");
        assert requestId != null;
        assertEquals(REQUEST_ID, requestId.get(0));
    }

    @Test
    public void filterNotificationTemplate() throws Exception {
        String message = "Hello";
        Mockito.doReturn(message.getBytes()).when(richContentNotificationTemplateService).filter(Mockito.any());
        ResponseEntity<byte[]> responseEntity =
            richContentNotificationTemplateController.filterNotificationTemplate(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, null);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(message, responseEntity.getBody());
        List<String> requestId = responseEntity.getHeaders().get("requestId");
        assert requestId != null;
        assertEquals(REQUEST_ID, requestId.get(0));

        List<String> contentDisposition = responseEntity.getHeaders().get("Content-Disposition");
        assert contentDisposition != null;
        assertEquals("attachment; filename=Ignite_Notification_Center_Rich_HTML_Templates_Export.zip",
            contentDisposition.get(0));

        List<String> contentType = responseEntity.getHeaders().get("Content-Type");
        assert contentType != null;
        assertEquals("application/zip", contentType.get(0));
    }

    @Test
    public void downloadNotificationTemplate() {
        assertNull(richContentNotificationTemplateController.downloadNotificationTemplate(REQUEST_ID, SESSION_ID,
            CLIENT_REQUEST_ID, null));
    }

    @Test
    public void filterNotificationTemplateWithoutDto() throws Exception {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("notificationId", NOTIFICATION_ID);
        map.put("lastUpdateTime", "123");
        list.add(map);
        Mockito.doReturn(list).when(richContentNotificationTemplateService).getRichHtmlNotificationTemplates();
        ResponseEntity<String> responseEntity =
            richContentNotificationTemplateController.filterNotificationTemplate(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(NOTIFICATION_ID, jsonNode.get(0).get("notificationId").asText());
        assertEquals("123", jsonNode.get(0).get("lastUpdateTime").asText());
    }

    @Test
    public void deleteNotificationTemplates() throws Exception {
        Set<String> nonExistingIds = new HashSet<>();
        nonExistingIds.add("007");
        Mockito.doReturn(nonExistingIds).when(richContentNotificationTemplateService)
            .deleteRichContentNotificationTemplates(Mockito.any());
        NotificationTemplateFilterDto dto = new NotificationTemplateFilterDto();

        List<String> notificationIds = new ArrayList<>();
        notificationIds.add("009");
        dto.setNotificationTemplateIds(notificationIds);

        ResponseEntity<MissingNotificationTemplates> responseEntity =
            richContentNotificationTemplateController.deleteNotificationTemplates(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, dto);
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        List<String> requestId = responseEntity.getHeaders().get("requestId");
        assert requestId != null;
        assertEquals(REQUEST_ID, requestId.get(0));

        assert responseEntity.getBody() != null;
        assertEquals(nonExistingIds, responseEntity.getBody().getMissingNotificationTemplateIds());
    }

    @Test
    public void deleteNotificationTemplatesWithEmptyDto() throws Exception {
        Set<String> nonExistingIds = new HashSet<>();
        nonExistingIds.add("007");
        Mockito.doReturn(nonExistingIds).when(richContentNotificationTemplateService)
            .deleteRichContentNotificationTemplates(Mockito.any());
        NotificationTemplateFilterDto dto = new NotificationTemplateFilterDto();
        dto.setNotificationTemplateIds(new ArrayList<>());

        ResponseEntity<MissingNotificationTemplates> responseEntity =
            richContentNotificationTemplateController.deleteNotificationTemplates(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, dto);
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        List<String> requestId = responseEntity.getHeaders().get("requestId");
        assert requestId != null;
        assertEquals(REQUEST_ID, requestId.get(0));

        assert responseEntity.getBody() != null;
        assertNull(responseEntity.getBody().getMissingNotificationTemplateIds());
    }

    @Test
    public void deleteNotificationTemplatesWithNullDto() throws Exception {
        Set<String> nonExistingIds = new HashSet<>();
        nonExistingIds.add("007");
        Mockito.doReturn(nonExistingIds).when(richContentNotificationTemplateService)
            .deleteRichContentNotificationTemplates(Mockito.any());
        NotificationTemplateFilterDto dto = new NotificationTemplateFilterDto();
        dto.setNotificationTemplateIds(new ArrayList<>());

        ResponseEntity<MissingNotificationTemplates> responseEntity =
            richContentNotificationTemplateController.deleteNotificationTemplates(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, null);
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        List<String> requestId = responseEntity.getHeaders().get("requestId");
        assert requestId != null;
        assertEquals(REQUEST_ID, requestId.get(0));

        assert responseEntity.getBody() != null;
        assertNull(responseEntity.getBody().getMissingNotificationTemplateIds());
    }

    @Test
    public void deleteNotificationTemplatesV2() throws Exception {

        ResponseEntity<String> responseEntity = richContentNotificationTemplateController
            .deleteRichNotificationTemplateByIdBrandLocale(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "Geo_fencing",
                "kia", "en_US", "seltos", null);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        responseEntity = richContentNotificationTemplateController
            .deleteRichNotificationTemplateByIdBrandLocale(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, NOTIFICATION_ID, "deep", "en_US", " ", "Sonet");
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());

        responseEntity =
            richContentNotificationTemplateController.deleteRichNotificationTemplateByIdBrandLocale(REQUEST_ID,
                SESSION_ID, CLIENT_REQUEST_ID, NOTIFICATION_ID, "deep", "en_US", "modelYear", "2020");
        assertEquals(ACCEPTED, responseEntity.getStatusCode());

    }

    @Test
    public void invalidNotificationIds() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper = richContentNotificationTemplateController.invalidNotificationIds(
            new InvalidInputException(new ArrayList<>()), request);
        assertEquals(BAD_REQUEST.value(), responseWrapper.getHttpStatusCode());
        assertNull(responseWrapper.getRequestId());
    }
}
