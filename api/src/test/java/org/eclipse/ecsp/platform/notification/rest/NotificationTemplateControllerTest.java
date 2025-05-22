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
import org.eclipse.ecsp.platform.notification.service.NotificationTemplateService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

/**
 * NotificationTemplateControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationTemplateControllerTest {
    private static final String SESSION_ID = "SESSION_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";
    private static final String REQUEST_ID = "requestId";
    public static final String NOTIFICATION_ID = "apple";
    public static final String NOTIFICATION_SHORT_NAME = "iPhone";
    public static final String NOTIFICATION_LONG_NAME = "appleIPhone";

    @InjectMocks
    private NotificationTemplateController notificationTemplateController;

    @Mock
    NotificationTemplateService notificationTemplateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void filterNotificationTemplateForBytes() {
        Mockito.doReturn(new byte[1]).when(notificationTemplateService).filter(Mockito.any());
        ResponseEntity<byte[]> responseEntity =
            notificationTemplateController.filterNotificationTemplate(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        assertEquals(OK, responseEntity.getStatusCode());
        byte[] body = responseEntity.getBody();
        assertNotNull(body);
    }

    @Test
    public void filterNotificationTemplate() throws Exception {
        List<Map<String, String>> notificationTemplatesMetaData = getNotificationTemplatesMetaData();
        Mockito.doReturn(notificationTemplatesMetaData).when(notificationTemplateService).getNotificationTemplates();
        ResponseEntity<String> responseEntity =
            notificationTemplateController.filterNotificationTemplate(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID);
        assertEquals(OK, responseEntity.getStatusCode());
        JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
        assertEquals(NOTIFICATION_ID, jsonNode.get(0).get("notificationId").asText());
        assertEquals(NOTIFICATION_SHORT_NAME, jsonNode.get(0).get("notificationShortName").asText());
        assertEquals(NOTIFICATION_LONG_NAME, jsonNode.get(0).get("notificationLongName").asText());
    }

    @Test
    public void importNotificationTemplate() throws Exception {
        ResponseEntity<String> responseEntity =
            notificationTemplateController.importNotificationTemplate(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(REQUEST_ID, responseEntity.getHeaders().getFirst(REQUEST_ID));
    }

    @Test
    public void importNotificationTemplateV2() throws Exception {
        ResponseEntity<String> responseEntity =
            notificationTemplateController.updateNotificationTemplate(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        assertEquals(OK, responseEntity.getStatusCode());
        assertEquals(REQUEST_ID, responseEntity.getHeaders().getFirst(REQUEST_ID));
    }

    @Test
    public void deleteNotificationTemplate() {
        ResponseEntity<String> responseEntity =
            notificationTemplateController.deleteNotificationTemplate(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        assertEquals(ACCEPTED, responseEntity.getStatusCode());
        assertEquals(REQUEST_ID, responseEntity.getHeaders().getFirst(REQUEST_ID));
    }

    @Test
    public void deleteNotificationTemplates() throws Exception {
        Set<String> missingNotificationTemplateIds = new HashSet<>();
        missingNotificationTemplateIds.add("someId");

        Mockito.doReturn(missingNotificationTemplateIds).when(notificationTemplateService)
            .deleteNotificationTemplates(Mockito.any());
        NotificationTemplateFilterDto deleteList = new NotificationTemplateFilterDto();
        List<String> notificationTemplateIds = new ArrayList<>();
        notificationTemplateIds.add("dummyId");
        deleteList.setNotificationTemplateIds(notificationTemplateIds);
        ResponseEntity<MissingNotificationTemplates> responseEntity =
            notificationTemplateController.deleteNotificationTemplates(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                deleteList);
        assertEquals(ACCEPTED, responseEntity.getStatusCode());
        MissingNotificationTemplates missingNotificationTemplatesAsResponse = responseEntity.getBody();
        assert missingNotificationTemplatesAsResponse != null;
        MissingNotificationTemplates missingNotificationTemplates =
            new MissingNotificationTemplates(missingNotificationTemplateIds);
        assertEquals(missingNotificationTemplates.getMissingNotificationTemplateIds(),
            missingNotificationTemplatesAsResponse.getMissingNotificationTemplateIds());
    }

    @Test
    public void deleteNotificationTemplatesWithNullData() throws Exception {
        Set<String> missingNotificationTemplateIds = new HashSet<>();
        missingNotificationTemplateIds.add("someId");
        Mockito.doReturn(missingNotificationTemplateIds).when(notificationTemplateService)
            .deleteNotificationTemplates(Mockito.any());
        NotificationTemplateFilterDto deleteList = new NotificationTemplateFilterDto();
        List<String> notificationTemplateIds = new ArrayList<>();
        notificationTemplateIds.add("dummyId");
        deleteList.setNotificationTemplateIds(notificationTemplateIds);
        ResponseEntity<MissingNotificationTemplates> responseEntity =
            notificationTemplateController.deleteNotificationTemplates(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        assertEquals(ACCEPTED, responseEntity.getStatusCode());
        MissingNotificationTemplates missingNotificationTemplatesAsResponse = responseEntity.getBody();
        assert missingNotificationTemplatesAsResponse != null;
        assertNull(missingNotificationTemplatesAsResponse.getMissingNotificationTemplateIds());
    }

    @Test
    public void deleteNotificationTemplatesWithEmptyData() throws Exception {
        Set<String> missingNotificationTemplateIds = new HashSet<>();
        missingNotificationTemplateIds.add("someId");
        Mockito.doReturn(missingNotificationTemplateIds).when(notificationTemplateService)
            .deleteNotificationTemplates(Mockito.any());
        NotificationTemplateFilterDto deleteList = new NotificationTemplateFilterDto();
        deleteList.setNotificationTemplateIds(new ArrayList<>());
        ResponseEntity<MissingNotificationTemplates> responseEntity =
            notificationTemplateController.deleteNotificationTemplates(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                deleteList);
        assertEquals(ACCEPTED, responseEntity.getStatusCode());
        MissingNotificationTemplates missingNotificationTemplatesAsResponse = responseEntity.getBody();
        assert missingNotificationTemplatesAsResponse != null;
        assertNull(missingNotificationTemplatesAsResponse.getMissingNotificationTemplateIds());
    }

    @Test
    public void invalidNotificationIds() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        ResponseWrapper<Void> responseWrapper =
            notificationTemplateController.invalidNotificationIds(new InvalidInputException(new ArrayList<>()),
                request);
        assertEquals(BAD_REQUEST.value(), responseWrapper.getHttpStatusCode());
        assertNull(responseWrapper.getRequestId());
    }

    @Test
    public void deleteNotificationTemplateByIdBrandLocale() throws Exception {
        ResponseEntity<String> responseEntity =
            notificationTemplateController.deleteNotificationTemplateByIdBrandLocale(REQUEST_ID,
                SESSION_ID, CLIENT_REQUEST_ID, NOTIFICATION_ID, "deep", "en_US", "model", null);
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());

        responseEntity =
            notificationTemplateController.deleteNotificationTemplateByIdBrandLocale(REQUEST_ID,
                SESSION_ID, CLIENT_REQUEST_ID, NOTIFICATION_ID, "deep", "en_US", " ", "Sonet");
        assertEquals(BAD_REQUEST, responseEntity.getStatusCode());

        responseEntity =
            notificationTemplateController.deleteNotificationTemplateByIdBrandLocale(REQUEST_ID, SESSION_ID,
                CLIENT_REQUEST_ID, NOTIFICATION_ID, "deep", "en_US", "modelCode", "jk147");
        assertEquals(ACCEPTED, responseEntity.getStatusCode());
    }

    private List<Map<String, String>> getNotificationTemplatesMetaData() {
        Map<String, String> map = new HashMap<>();
        map.put("notificationId", NOTIFICATION_ID);
        map.put("notificationShortName", NOTIFICATION_SHORT_NAME);
        map.put("notificationLongName", NOTIFICATION_LONG_NAME);
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        return list;
    }
}
