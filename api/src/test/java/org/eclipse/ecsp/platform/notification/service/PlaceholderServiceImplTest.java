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

import org.apache.commons.codec.binary.Base64;
import org.eclipse.ecsp.notification.dao.NotificationPlaceholderDao;
import org.eclipse.ecsp.notification.dao.NotificationPlaceholderImportedDataDao;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholder;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholderImportedData;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * PlaceholderServiceImplTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
@RunWith(SpringJUnit4ClassRunner.class)
public class PlaceholderServiceImplTest {
    @InjectMocks
    private PlaceholderServiceImpl placeholderService;

    @Mock
    private NotificationPlaceholderDao notificationPlaceholderDao;

    @Mock
    private NotificationPlaceholderImportedDataDao notificationPlaceholderImportedDataDao;

    @Captor
    private ArgumentCaptor<NotificationPlaceholder> placeholderArrayCaptor;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void importPlaceholdersInvalidCsv() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_invalid_csv.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_invalid_csv", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_INVALID_CSV.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersNoBom() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_no_bom.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_no_bom", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.TEMPLATE_INVALID_FORMAT.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersNoMinimumRows() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_1_line.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_1_line", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_LINES.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersNoMinimumColumns() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_2_columns.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_2_columns", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_COLUMNS.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersNoLocaleColumn() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_no_locale_columns.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_no_locale_columns", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_COLUMNS.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }


    @Test
    public void importPlaceholdersNoKeyColumns() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_no_key_column.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_no_key_column", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_HEADERS.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersNoBrandColumns() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_no_brand_column.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_no_brand_column", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_HEADERS.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersEmptyLocale() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_empty_locale.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_empty_locale", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.INPUT_EMPTY_LOCALE.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersDuplicateLocale() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_duplicate_locale.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_duplicate_locale", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.INPUT_DUPLICATE_LOCALE_PREFIX.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersInvalidLocale() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_invalid_locale.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_invalid_locale", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.INPUT_INVALID_LOCALE.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersHeadersWrongOrder() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_wrong_columns_order.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_wrong_columns_order", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_WRONG_HEADERS_ORDER.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void importPlaceholdersDuplicateKey() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_duplicate_key.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("placeholder_duplicate_key", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> placeholderService.importPlaceholders(multipartFile));
        assertEquals(NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_INPUT_DUPLICATE_DATA.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }


    @Test
    public void importPlaceholdersNoPropsSuccess() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_no_props_success.csv");
        InputStream input = resource.getInputStream();

        doNothing().when(notificationPlaceholderDao).deleteByKeys(Mockito.any());
        when(notificationPlaceholderDao.saveAll(placeholderArrayCaptor.capture())).thenReturn(new ArrayList<>());
        when(notificationPlaceholderImportedDataDao.save(any())).thenReturn(null);
        MultipartFile multipartFile = new MockMultipartFile("placeholder_no_props_success", input);
        placeholderService.importPlaceholders(multipartFile);

        List<NotificationPlaceholder> placeholders = placeholderArrayCaptor.getAllValues();
        assertEquals(4, placeholders.size());
        assertTrue(CollectionUtils.isEmpty(placeholders.get(0).getAdditionalLookupProperties()));

    }


    @Test
    public void importPlaceholdersSuccess() throws IOException {
        Resource resource = new ClassPathResource("placeholderCsv/placeholder_success.csv");
        InputStream input = resource.getInputStream();

        doNothing().when(notificationPlaceholderDao).deleteByKeys(Mockito.any());
        when(notificationPlaceholderDao.saveAll(placeholderArrayCaptor.capture())).thenReturn(new ArrayList<>());
        when(notificationPlaceholderImportedDataDao.save(any())).thenReturn(null);
        MultipartFile multipartFile = new MockMultipartFile("placeholder_success", input);
        placeholderService.importPlaceholders(multipartFile);

        List<NotificationPlaceholder> placeholders = placeholderArrayCaptor.getAllValues();
        assertEquals(10, placeholders.size());
        assertEquals("default", placeholders.get(0).getBrand());
        assertEquals("en_US", placeholders.get(0).getLocale().toString());
        assertTrue(CollectionUtils.isEmpty(placeholders.get(0).getAdditionalLookupProperties()));
        assertEquals("ford", placeholders.get(2).getBrand());
        assertEquals("alert Dina: +9721700432322", placeholders.get(2).getValue());
        assertFalse(CollectionUtils.isEmpty(placeholders.get(2).getAdditionalLookupProperties()));
        assertTrue(placeholders.get(2).getAdditionalLookupProperties().get(0).getValues().contains("mondeo"));
        assertTrue(placeholders.get(2).getAdditionalLookupProperties().get(0).getValues().contains("focus"));
        assertEquals(1, placeholders.get(2).getAdditionalLookupProperties().get(0).getOrder());
        assertTrue(placeholders.get(2).getAdditionalLookupProperties().get(1).getValues().contains("2000"));
        assertTrue(placeholders.get(2).getAdditionalLookupProperties().get(1).getValues().contains("2001"));
        assertTrue(placeholders.get(2).getAdditionalLookupProperties().get(1).getValues().contains("2002"));
        assertEquals(2, placeholders.get(2).getAdditionalLookupProperties().get(1).getOrder());
    }

    @Test
    public void exportPlaceholdersNotFound() {
        when(notificationPlaceholderImportedDataDao.findById(any())).thenReturn(null);
        NotFoundException thrown =
            assertThrows(NotFoundException.class,
                () -> placeholderService.exportPlaceholders("key1"));
        assertEquals(NotificationCenterError.NOT_FOUND_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_NOT_FOUND.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void exportPlaceholdersSuccess() {
        byte[] data = "1245".getBytes(StandardCharsets.UTF_8);
        NotificationPlaceholderImportedData notificationPlaceholderImportedData =
            new NotificationPlaceholderImportedData("key1",
                Base64.encodeBase64String(data));
        when(notificationPlaceholderImportedDataDao.findById(any())).thenReturn(notificationPlaceholderImportedData);
        byte[] returnedData = placeholderService.exportPlaceholders("key1");
        assertArrayEquals(Base64.decodeBase64(notificationPlaceholderImportedData.getFile()), returnedData);
    }

    @Test
    public void deletePlaceholderNotFound() {
        when(notificationPlaceholderImportedDataDao.findById(any())).thenReturn(null);
        when(notificationPlaceholderDao.findById(any())).thenReturn(null);
        NotFoundException thrown =
            assertThrows(NotFoundException.class,
                () -> placeholderService.deletePlaceholder("key1"));
        assertEquals(NotificationCenterError.NOT_FOUND_EXCEPTION.getMessage(), thrown.getMessage());
        assertEquals(NotificationCenterError.PLACEHOLDERS_NOT_FOUND.getCode(),
            thrown.getErrors().iterator().next().getCode());
    }

    @Test
    public void deletePlaceholderOnlyData() {
        NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
        notificationPlaceholder.setKey("key1");
        notificationPlaceholder.setValue("val1");
        when(notificationPlaceholderImportedDataDao.findById(any())).thenReturn(null);
        when(notificationPlaceholderDao.findByKeys(any())).thenReturn(
            Collections.singletonList(notificationPlaceholder));
        doNothing().when(notificationPlaceholderDao).deleteByKeys(any());
        doReturn(true).when(notificationPlaceholderImportedDataDao).deleteById(any());
        placeholderService.deletePlaceholder("key1");
        verify(notificationPlaceholderDao, times(1)).deleteByKeys(any());
        verify(notificationPlaceholderImportedDataDao, times(0)).deleteById(any());
    }

    @Test
    public void deletePlaceholderOnlyImportedData() {
        NotificationPlaceholderImportedData notificationPlaceholderImportedData =
            new NotificationPlaceholderImportedData("key1",
                "77u/a2V5LGJyYW5kLHByb3AudmVoaWNsZVByb2ZpbGUubW9kZWxDb2RlLGVuX1VTDQpjdXN0Q2FyZVBob25lTnVt"
                    + "YmVyLGplZXAsLDE5NzIxOTAwMTExDQpjdXN0Q2FyZVBob25lTnVtYmVyLGplZXAsV0xKVDc1LDExMS1tb2RlbENvZGUtMT"
                    + "ExDQpjdXN0Q2FyZVBob25lTnVtYmVyLGRlZmF1bHQsLDAwMC0xMTEtMjIyLTMzMw0K");
        when(notificationPlaceholderImportedDataDao.findById(any())).thenReturn(notificationPlaceholderImportedData);
        when(notificationPlaceholderDao.findById(any())).thenReturn(null);
        doNothing().when(notificationPlaceholderDao).deleteByKeys(any());
        doReturn(true).when(notificationPlaceholderImportedDataDao).deleteById(any());
        placeholderService.deletePlaceholder("key1");
        verify(notificationPlaceholderDao, times(0)).deleteByKeys(any());
        verify(notificationPlaceholderImportedDataDao, times(1)).deleteById(any());
    }

    @Test
    public void deletePlaceholderSuccess() {
        NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
        notificationPlaceholder.setKey("key1");
        notificationPlaceholder.setValue("val1");
        NotificationPlaceholderImportedData notificationPlaceholderImportedData =
            new NotificationPlaceholderImportedData("key1",
                "77u/a2V5LGJyYW5kLHByb3AudmVoaWNsZVByb2ZpbGUubW9kZWxDb2RlLGVuX1VTDQpjdXN0Q2FyZVBob25lTnVtYmVyLGplZX"
                    + "AsLDE5NzIxOTAwMTExDQpjdXN0Q2FyZVBob25lTnVtYmVyLGplZXAsV0xKVDc1LDExMS1tb2RlbENvZGUtMTExDQpjdXN0Q"
                    + "2FyZVBob25lTnVtYmVyLGRlZmF1bHQsLDAwMC0xMTEtMjIyLTMzMw0K");

        when(notificationPlaceholderImportedDataDao.findById(any())).thenReturn(notificationPlaceholderImportedData);
        when(notificationPlaceholderDao.findByKeys(any())).thenReturn(
            Collections.singletonList(notificationPlaceholder));
        doNothing().when(notificationPlaceholderDao).deleteByKeys(any());
        doReturn(true).when(notificationPlaceholderImportedDataDao).deleteById(any());
        placeholderService.deletePlaceholder("key1");
        verify(notificationPlaceholderDao, times(1)).deleteByKeys(any());
        verify(notificationPlaceholderImportedDataDao, times(1)).deleteById(any());
    }
}