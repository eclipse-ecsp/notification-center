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

import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateImportedDataDAO;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * DateFormatTransformerParameterizedTemplateTest class.
 */
@RunWith(Parameterized.class)
public class DateFormatTransformerParameterizedTemplateTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    /**
     * DateFormatTransformerParameterizedTemplateTest constructor.
     *
     * @param res String
     */
    public DateFormatTransformerParameterizedTemplateTest(String res) {
        this.res = res;
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
                {"sample_dateFormat_template_failure.csv"},
                {"sample_dateFormat_template_failure.csv"},
                {"sample_template_invalid_locale_date_format.csv"},

        });

    }

    private final String res;

    @InjectMocks
    NotificationTemplateService notificationTemplateService;

    @Mock
    private DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    @Mock
    private NotificationTemplateImportedDataDAO notificationTemplateImportedDataDao;

    public static final String NOTIFICATION_ID_1 = "notificationId-1";

    @Test(expected = InvalidInputFileException.class)
    public void importRichNotificationTemplateFailure() throws Exception {
        Resource resource = new ClassPathResource(res);
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        notificationTemplateService.importNotificationTemplate(multipartFile, false);
    }
}
