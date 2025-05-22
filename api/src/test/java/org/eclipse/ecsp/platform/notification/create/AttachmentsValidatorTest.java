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

package org.eclipse.ecsp.platform.notification.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * AttachmentsValidatorTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
@RunWith(SpringJUnit4ClassRunner.class)
public class AttachmentsValidatorTest {

    @InjectMocks
    private AttachmentsValidator attachmentsValidator;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    private Errors errors = new MapBindingResult(new HashMap<String, String>(), "errors");

    @Test
    public void supports() {
        assertTrue(attachmentsValidator.supports(NotificationCreationRequest.class));
    }

    @Test
    public void supportsWhenDiffClass() {
        assertFalse(attachmentsValidator.supports(Exception.class));
    }

    @Test
    public void validateWhenNotificationCreationRequestDoesNotContainsAttachmentKey() {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();
        data.put("", 1);
        ncr.setData(data);
        attachmentsValidator.validate(ncr, errors);
    }

    @Test
    public void validateWhenAttachmentKeyValueNotList() {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();
        data.put("attachments", 1);
        ncr.setData(data);
        attachmentsValidator.validate(ncr, errors);
        assertEquals(1, errors.getAllErrors().size());
        assertEquals("attachments.is.not.collection", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void validateWhenAttachmentKeyEmptyList() {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();
        data.put("attachments", new ArrayList<String>());
        ncr.setData(data);
        attachmentsValidator.validate(ncr, errors);
    }


    @Test
    public void validateWhenAttachmentKeyMaxSizeList() {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();
        List<String> attachmentList = Arrays.asList("1", "2", "3");
        data.put("attachments", attachmentList);
        ncr.setData(data);
        attachmentsValidator.setAttachmentsMaxCount(2);
        attachmentsValidator.validate(ncr, errors);
        assertEquals(1, errors.getAllErrors().size());
        assertEquals("attachments.exceeded.2", errors.getAllErrors().get(0).getDefaultMessage());
    }


    @Test
    public void validateWhenAttachmentExceedMaxSize() throws IOException {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        AttachmentData attachmentData =
            objectMapper.readValue("{\"fileName\":\"/tmp/test.txt\", \"content\":\"AA==\", \"mimeType\":\"txt\"}",
                AttachmentData.class);
        List<AttachmentData> attachmentList = Collections.singletonList(attachmentData);
        data.put("attachments", attachmentList);
        ncr.setData(data);
        attachmentsValidator.setAttachmentsMaxCount(2);
        attachmentsValidator.setMapper(objectMapper);
        attachmentsValidator.setMaxSize(0);
        attachmentsValidator.validate(ncr, errors);
        assertEquals(1, errors.getAllErrors().size());
        assertEquals("attachment.content.exceeded.0", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void validateWhenAttachmentSuccess() throws IOException {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        AttachmentData attachmentData =
            objectMapper.readValue("{\"fileName\":\"/tmp/test.txt\", \"content\":\"AA==\", \"mimeType\":\"txt\"}",
                AttachmentData.class);
        List<AttachmentData> attachmentList = Collections.singletonList(attachmentData);
        data.put("attachments", attachmentList);
        ncr.setData(data);
        attachmentsValidator.setAttachmentsMaxCount(2);
        attachmentsValidator.setMapper(objectMapper);
        attachmentsValidator.setMaxSize(100);
        attachmentsValidator.validate(ncr, errors);
        assertEquals(0, errors.getAllErrors().size());
    }

    @Test
    public void validateWhenAttachmentInvalidStructure() throws IOException {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();

        List<String> attachmentList = Collections.singletonList("test");
        data.put("attachments", attachmentList);
        ncr.setData(data);
        attachmentsValidator.setAttachmentsMaxCount(2);
        ObjectMapper objectMapper = new ObjectMapper();
        attachmentsValidator.setMapper(objectMapper);
        attachmentsValidator.setMaxSize(100);
        attachmentsValidator.validate(ncr, errors);
        assertEquals(1, errors.getAllErrors().size());
        assertEquals("attachment.structure.invalid", errors.getAllErrors().get(0).getDefaultMessage());
    }
}