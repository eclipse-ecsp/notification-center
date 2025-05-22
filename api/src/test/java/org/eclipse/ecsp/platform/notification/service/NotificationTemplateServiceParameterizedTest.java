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

package org.eclipse.ecsp.platform.notification.service;

import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * NotificationTemplateServiceParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class NotificationTemplateServiceParameterizedTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    /**
     * NotificationTemplateServiceParameterizedTest constructor.
     *
     * @param res String
     * @param multiPartFile String
     */
    public NotificationTemplateServiceParameterizedTest(String res, String multiPartFile) {
        this.res = res;
        this.multiPartFile = multiPartFile;
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"sample_template_dup_language.csv", "sample_template_dup_language"},
                {"sample_template_without_locale.csv", "sample_template_without_locale"},
                {"sample_template_less_record.csv", "sample_template_less_record"},
                {"sample_template_invalid_header.csv", "sample_template_invalid_header"},
                {"sample_template_invalid_properties.csv", "sample_template_invalid_properties"},
                {"sample_template_empty_brand.csv", "sample_template_empty_brand"},
                {"sample_template_dup_brand.csv", "sample_template_dup_brand"},

        });

    }

    private final String res;
    private final String multiPartFile;


    @InjectMocks
    private NotificationTemplateService notificationTemplateService;

    @Test
    public void importNotificationTemplateWithDupLanguage() throws Exception {
        Resource resource = new ClassPathResource(res);
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile(multiPartFile, input);
        InvalidInputFileException thrown =
                assertThrows(InvalidInputFileException.class,
                        () -> notificationTemplateService.importNotificationTemplate(multipartFile, false));
        assertEquals(INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
    }
}
