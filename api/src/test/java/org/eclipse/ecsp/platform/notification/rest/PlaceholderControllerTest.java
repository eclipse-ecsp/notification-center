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

import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.service.PlaceholderServiceImpl;
import org.junit.Assert;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.PLACEHOLDERS_INPUT_INVALID_CSV;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.PLACEHOLDERS_NOT_FOUND;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.PLACEHOLDER_SUCCESS;
import static org.eclipse.ecsp.platform.notification.utils.UnitTestUtils.compareMessage;
import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * PlaceholderControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings("checkstyle:MagicNumber")
public class PlaceholderControllerTest {

    private static final String REQUEST_ID = "REQ";
    private static final String SESSION_ID = "SESSION_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";

    @InjectMocks
    private PlaceholderController placeholderController;

    @Mock
    private PlaceholderServiceImpl placeholderService;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }


    @Test
    public void importPlaceholders() throws IOException {

        Mockito.doNothing().when(placeholderService).importPlaceholders(Mockito.any());
        ResponseWrapper<Void> response =
            placeholderController.importPlaceholders(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, null);
        assertEquals(200, response.getHttpStatusCode());
        compareMessage(response.getRootMessage(), PLACEHOLDER_SUCCESS);
    }

    @Test
    public void exportPlaceholders() {

        byte[] data = "1234".getBytes(StandardCharsets.UTF_8);
        Mockito.doReturn(data).when(placeholderService).exportPlaceholders(Mockito.any());
        ResponseEntity<byte[]> response =
            placeholderController.exportPlaceholders(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "key1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(data, response.getBody());
    }

    @Test
    public void deletePlaceholder() {

        Mockito.doNothing().when(placeholderService).deletePlaceholder(Mockito.any());
        ResponseWrapper<Void> response =
            placeholderController.deletePlaceholder(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID, "key1");
        assertEquals(200, response.getHttpStatusCode());
        compareMessage(response.getRootMessage(), PLACEHOLDER_SUCCESS);
    }

    @Test
    public void invalidInput() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        NotificationCenterExceptionBase e =
            new InvalidInputException(Collections.singletonList(PLACEHOLDERS_INPUT_INVALID_CSV.toMessage()));
        ResponseWrapper<Void> response = placeholderController.invalidInput(e, request);
        Assert.assertEquals(400, response.getHttpStatusCode());
        compareMessage(response.getErrors().iterator().next(), PLACEHOLDERS_INPUT_INVALID_CSV);
    }

    @Test
    public void notFoundException() {
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        NotificationCenterExceptionBase e =
            new NotFoundException(Collections.singletonList(PLACEHOLDERS_NOT_FOUND.toMessage()));
        ResponseWrapper<Void> response = placeholderController.notFoundException(e, request);
        Assert.assertEquals(404, response.getHttpStatusCode());
        compareMessage(response.getErrors().iterator().next(), PLACEHOLDERS_NOT_FOUND);
    }
}