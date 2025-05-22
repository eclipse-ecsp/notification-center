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
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PlaceholderServiceImplParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class PlaceholderServiceImplParameterizedTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    /**
     * PlaceholderServiceImplParameterizedTest constructor.
     *
     * @param res String
     * @param multiPartFile String
     */
    public PlaceholderServiceImplParameterizedTest(String res, String multiPartFile) {
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
                {"placeholderCsv/placeholder_missing_columns.csv", "placeholder_missing_columns"},
                {"placeholderCsv/placeholder_empty_brand.csv", "placeholder_empty_brand"},
                {"placeholderCsv/placeholder_empty_key.csv", "placeholder_empty_key"},

        });

    }

    @InjectMocks
    private PlaceholderServiceImpl placeholderService;

    private final String res;
    private final String multiPartFile;


    @Test
    public void importPlaceholdersMissingColumns() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_missing_columns.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_missing_columns", input);
        InvalidInputFileException thrown =
                assertThrows(InvalidInputFileException.class,
                        () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_DATA_FIELD.getCode(),
                thrown.getErrors().iterator().next().getCode());
    }
}
