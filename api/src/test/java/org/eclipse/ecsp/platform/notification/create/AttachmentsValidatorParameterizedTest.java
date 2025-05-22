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
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * AttachmentsValidatorParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class AttachmentsValidatorParameterizedTest {
    public static final int FIVE = 5;
    public static final int TWO = 2;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    /**
     * AttachmentsValidatorParameterizedTest constructor.
     *
     * @param content String
     * @param maxAttachmentCount int
     * @param errorMsg String
     */
    public AttachmentsValidatorParameterizedTest(String content, int maxAttachmentCount, String errorMsg) {
        this.content = content;
        this.maxAttachmentCount = maxAttachmentCount;
        this.errorMsg = errorMsg;
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Errors errors = new MapBindingResult(new HashMap<String, String>(), "errors");


    @InjectMocks
    private AttachmentsValidator attachmentsValidator;

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"{\"fileName\":\"\", \"content\":\"AA==\", \"mimeType\":\"txt\"}",
                    FIVE, "attachment.name.empty"},
                {"{\"fileName\":\"/tmp/test.txt\", \"content\":\"AA==\", \"mimeType\":\"\"}",
                    FIVE, "attachment.type.empty"},
                {"{\"fileName\":\"/tmp/test.txt\", \"mimeType\":\"txt\"}",
                    TWO, "attachment.content.empty"},

        });

    }


    private final String content;
    private final int maxAttachmentCount;
    private final String errorMsg;

    @Test
    public void validateWhenAttachment() throws IOException {
        NotificationCreationRequest ncr = new NotificationCreationRequest();
        Map<String, Object> data = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        AttachmentData attachmentData =
                objectMapper.readValue(content,
                        AttachmentData.class);
        List<AttachmentData> attachmentList = Collections.singletonList(attachmentData);
        data.put("attachments", attachmentList);
        ncr.setData(data);
        attachmentsValidator.setAttachmentsMaxCount(maxAttachmentCount);
        attachmentsValidator.setMapper(objectMapper);
        attachmentsValidator.validate(ncr, errors);
        assertEquals(1, errors.getAllErrors().size());
        assertEquals(errorMsg, errors.getAllErrors().get(0).getDefaultMessage());
    }
}
