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
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateImportedDataDAO;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateImportedData;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNotificationIdException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationTemplateDoesNotExistException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INVALID_NOTIFICATION_ID_EXCEPTION;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_TEMPLATE_DOES_NOT_EXIST_EXCEPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * NotificationTemplateServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationTemplateServiceTest {

    public static final String NOTIFICATION_ID_1 = "notificationId-1";

    @InjectMocks
    private NotificationTemplateService notificationTemplateService;

    @Mock
    private DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    @Mock
    private NotificationTemplateImportedDataDAO notificationTemplateImportedDataDao;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void filter() throws Exception {
        List<NotificationTemplateImportedData> notificationTemplateImportedDataList =
            createNotificationTemplateImportedDataList();
        when(notificationTemplateImportedDataDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            notificationTemplateImportedDataList);
        MockMultipartFile multipartFile = new MockMultipartFile("testFile", "", "", new byte[1]);

        NotificationTemplateImportedData templateImportedData = new NotificationTemplateImportedData(NOTIFICATION_ID_1,
            Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));
        when(notificationTemplateImportedDataDao.findById(Mockito.anyString())).thenReturn(templateImportedData);

        NotificationTemplateFilterDto filterDto = new NotificationTemplateFilterDto();
        filterDto.setNotificationTemplateIds(Collections.singletonList(NOTIFICATION_ID_1));
        byte[] filter = notificationTemplateService.filter(filterDto);
        assertNotNull(filter);
    }

    @Test
    public void filterWithInvalidNotificationIdExceptionOnWhenNotificationIdMismatch() throws IOException {
        List<NotificationTemplateImportedData> notificationTemplateImportedDataList =
            createNotificationTemplateImportedDataList();
        when(notificationTemplateImportedDataDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            notificationTemplateImportedDataList);
        NotificationTemplateFilterDto filterDto = new NotificationTemplateFilterDto();
        List<String> notificationTemplateIds = new ArrayList<>();
        notificationTemplateIds.add("notificationId-2");
        filterDto.setNotificationTemplateIds(notificationTemplateIds);
        InvalidNotificationIdException thrown =
            assertThrows(InvalidNotificationIdException.class,
                () -> notificationTemplateService.filter(filterDto),
                "Expected to throw, but it didn't");
        assertEquals(INVALID_NOTIFICATION_ID_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void filterWhenNotificationIdNull() throws Exception {
        List<NotificationTemplateImportedData> notificationTemplateImportedDataList =
            createNotificationTemplateImportedDataList();
        when(notificationTemplateImportedDataDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            notificationTemplateImportedDataList);
        when(notificationTemplateImportedDataDao.findAll()).thenReturn(notificationTemplateImportedDataList);
        MultipartFile multipartFile = new MockMultipartFile("testFile", "", "", new byte[1]);
        NotificationTemplateImportedData notificationTemplateImportedData =
            new NotificationTemplateImportedData(NOTIFICATION_ID_1,
                Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));
        when(notificationTemplateImportedDataDao.findById(Mockito.anyString())).thenReturn(
            notificationTemplateImportedData);
        NotificationTemplateFilterDto filterDto = new NotificationTemplateFilterDto();
        filterDto.setNotificationTemplateIds(null);
        byte[] filter = notificationTemplateService.filter(filterDto);
        assertNotNull(filter);
    }

    @Test
    public void filterWhenNotificationIdEmpty() throws Exception {
        List<NotificationTemplateImportedData> notificationTemplateImportedDataList =
            createNotificationTemplateImportedDataList();
        when(notificationTemplateImportedDataDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            notificationTemplateImportedDataList);
        when(notificationTemplateImportedDataDao.findAll()).thenReturn(notificationTemplateImportedDataList);
        MultipartFile multipartFile = new MockMultipartFile("testFile", "", "", new byte[1]);
        NotificationTemplateImportedData notificationTemplateImportedData =
            new NotificationTemplateImportedData(NOTIFICATION_ID_1,
                Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));
        when(notificationTemplateImportedDataDao.findById(Mockito.anyString())).thenReturn(
            notificationTemplateImportedData);
        NotificationTemplateFilterDto filterDto = new NotificationTemplateFilterDto();
        filterDto.setNotificationTemplateIds(new ArrayList<>());
        byte[] filter = notificationTemplateService.filter(filterDto);
        assertNotNull(filter);
    }

    @Test
    public void filterForExceptionWhenNotificationIdEmpty() throws Exception {
        List<NotificationTemplateImportedData> notificationTemplateImportedDataList =
            createNotificationTemplateImportedDataList();
        when(notificationTemplateImportedDataDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            notificationTemplateImportedDataList);
        when(notificationTemplateImportedDataDao.findAll()).thenThrow(RuntimeException.class);
        NotificationTemplateFilterDto filterDto = new NotificationTemplateFilterDto();
        filterDto.setNotificationTemplateIds(new ArrayList<>());
        byte[] filter = notificationTemplateService.filter(filterDto);
        assertNull(filter);
    }

    @Test
    public void importNotificationTemplate() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        Resource resource = new ClassPathResource("sample_template.csv");
        InputStream input = resource.getInputStream();
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        when(notificationTemplateImportedDataDao.deleteById(Mockito.anyString())).thenReturn(true);
        when(notificationTemplateImportedDataDao.save(Mockito.any())).thenReturn(null);
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        assertNotNull(multipartFile);
        notificationTemplateService.importNotificationTemplate(multipartFile, false);
    }

    @Test
    public void importNotificationTemplateV2() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        Resource resource = new ClassPathResource("sample_template.csv");
        InputStream input = resource.getInputStream();
        when(dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet())).thenReturn(dynamicNotificationTemplateList);
        when(notificationTemplateImportedDataDao.deleteById(Mockito.anyString())).thenReturn(true);
        when(notificationTemplateImportedDataDao.save(Mockito.any())).thenReturn(null);
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        assertNotNull(multipartFile);
        notificationTemplateService.importNotificationTemplate(multipartFile, true);
    }

    @Test
    public void importNotificationTemplateV2InvalidFile() throws Exception {
        Resource resource = new ClassPathResource("sample_template_with_empty_additional_attribute.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("sample_template_with_empty_additional_attribute", input);
        assertThrows(InvalidInputFileException.class,
            () -> notificationTemplateService.importNotificationTemplate(multipartFile, true));
    }

    @Test
    public void importNotificationTemplateWithAdditionalLookupProperties() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        Resource resource = new ClassPathResource("sample_template_with_additional_attribute.csv");
        InputStream input = resource.getInputStream();
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        assertNotNull(multipartFile);
        notificationTemplateService.importNotificationTemplate(multipartFile, false);
    }

    @Test
    public void importNotificationTemplateWithAdditionalLookupPropertiesV2() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplate.setBrand("default");
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);

        DynamicNotificationTemplate dynamicNotificationTmplate = new DynamicNotificationTemplate();
        dynamicNotificationTmplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTmplate.setBrand("kia");
        List<AdditionalLookupProperty> additionalLookupProperties = new ArrayList<>();
        AdditionalLookupProperty prop = new AdditionalLookupProperty();
        prop.setName("vehicleProfile.model");
        prop.setValues(new HashSet<>(Arrays.asList("rio", "picanto")));
        additionalLookupProperties.add(prop);
        dynamicNotificationTmplate.setAdditionalLookupProperties(additionalLookupProperties);
        List<DynamicNotificationTemplate> dynamicNotificationTemplateLst = new ArrayList<>();
        dynamicNotificationTemplateLst.add(dynamicNotificationTmplate);
        Resource resource = new ClassPathResource("sample_template_with_additional_attribute.csv");
        InputStream input = resource.getInputStream();
        when(dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet())).thenReturn(dynamicNotificationTemplateList);
        when(dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet(), Mockito.anyString(), Mockito.anySet()))
            .thenReturn(dynamicNotificationTemplateLst);
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        notificationTemplateService.importNotificationTemplate(multipartFile, true);
        AdditionalLookupProperty prop1 = new AdditionalLookupProperty();
        prop1.setName("vehicleProfile.modelYear");
        prop1.setValues(new HashSet<>(Arrays.asList("2000", "2020")));
        additionalLookupProperties.add(prop1);
        assertNotNull(multipartFile);
        notificationTemplateService.importNotificationTemplate(multipartFile, true);
    }

    @Test
    public void importNotificationTemplateWithEmptyAdditionalLookupProperties() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        Resource resource = new ClassPathResource("sample_template_with_empty_additional_attribute.csv");
        InputStream input = resource.getInputStream();
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        assertNotNull(multipartFile);
        notificationTemplateService.importNotificationTemplate(multipartFile, false);
    }

    @Test
    public void importNotificationTemplateWithDuplicateAdditionalLookupProperties() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        Resource resource = new ClassPathResource("sample_template_with_duplicate_additional_attribute.csv");
        InputStream input = resource.getInputStream();
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> notificationTemplateService.importNotificationTemplate(multipartFile, false));
        assertEquals(INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void importNotificationTemplateNotUtf8() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        Resource resource = new ClassPathResource("sample_template_no_bom.csv");
        InputStream input = resource.getInputStream();
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        InvalidInputFileException thrown =
            assertThrows(InvalidInputFileException.class,
                () -> notificationTemplateService.importNotificationTemplate(multipartFile, false));
        assertEquals(INVALID_INPUT_FILE_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void deleteNotificationTemplateWithImportedFile() {
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        when(notificationTemplateImportedDataDao.deleteById(NOTIFICATION_ID_1)).thenReturn(true);
        notificationTemplateService.deleteNotificationTemplate(NOTIFICATION_ID_1);
        assertEquals(1, dynamicNotificationTemplateList.size());
    }

    @Test
    public void deleteNotificationTemplateWithoutImportedFile() {
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        doThrow(RuntimeException.class).when(notificationTemplateImportedDataDao).deleteById(NOTIFICATION_ID_1);
        notificationTemplateService.deleteNotificationTemplate(NOTIFICATION_ID_1);
        assertEquals(1, dynamicNotificationTemplateList.size());
    }

    @Test
    public void deleteNotificationTemplateExceptionWhenNotificationIdListEmpty() {
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(new ArrayList<>());
        NotificationTemplateDoesNotExistException thrown =
            assertThrows(NotificationTemplateDoesNotExistException.class,
                () -> notificationTemplateService.deleteNotificationTemplate(null));
        assertEquals(NOTIFICATION_TEMPLATE_DOES_NOT_EXIST_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void deleteNotificationTemplatesWithImportedFile() {
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        NotificationTemplateFilterDto filterDto = new NotificationTemplateFilterDto();
        List<String> notificationTemplateIds = new ArrayList<>();
        notificationTemplateIds.add(NOTIFICATION_ID_1);
        filterDto.setNotificationTemplateIds(notificationTemplateIds);
        when(notificationTemplateImportedDataDao.deleteByIds(
            filterDto.getNotificationTemplateIds().toArray(new String[0]))).thenReturn(1);
        Set<String> strings = notificationTemplateService.deleteNotificationTemplates(filterDto);
        strings.forEach(s -> assertEquals(NOTIFICATION_ID_1, s));
    }

    @Test
    public void deleteNotificationTemplatesWithoutImportedFile() {
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        when(dynamicNotificationTemplateDao.findByNotificationIds(Mockito.anyList())).thenReturn(
            dynamicNotificationTemplateList);
        NotificationTemplateFilterDto filterDto = new NotificationTemplateFilterDto();
        List<String> notificationTemplateIds = new ArrayList<>();
        notificationTemplateIds.add(NOTIFICATION_ID_1);
        filterDto.setNotificationTemplateIds(notificationTemplateIds);
        doThrow(RuntimeException.class).when(notificationTemplateImportedDataDao)
            .deleteByIds(filterDto.getNotificationTemplateIds().toArray(new String[0]));
        Set<String> strings = notificationTemplateService.deleteNotificationTemplates(filterDto);
        strings.forEach(s -> assertEquals(NOTIFICATION_ID_1, s));
    }

    @Test
    public void deleteNotificationTemplatesV2() {

        when(dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(NOTIFICATION_ID_1,
            Collections.singleton("kia"),
            Collections.singleton(Locale.forLanguageTag("en_GB")), "vehicleProfile.modelColor",
            Collections.singleton("red")))
            .thenReturn(null);
        assertThrows(NotificationTemplateDoesNotExistException.class,
            () -> notificationTemplateService.deleteNotificationTemplateByIdBrandLocale(NOTIFICATION_ID_1, "kia",
                "en_GB", "modelColor", "red"));


        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplate.setBrand("deep");
        dynamicNotificationTemplate.setLocale("en_US");
        List<AdditionalLookupProperty> additionalLookupProperties = new ArrayList<>();
        AdditionalLookupProperty prop = new AdditionalLookupProperty();
        prop.setName("vehicleProfile.model");
        prop.setValues(new HashSet<>(Arrays.asList("wagoneer")));
        additionalLookupProperties.add(prop);
        dynamicNotificationTemplate.setAdditionalLookupProperties(additionalLookupProperties);
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        when(dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet(), Mockito.anyString(), Mockito.anySet()))
            .thenReturn(dynamicNotificationTemplateList);
        notificationTemplateService.deleteNotificationTemplateByIdBrandLocale(NOTIFICATION_ID_1, "deep", "en_US",
            "model", "Wagoneer");

        DynamicNotificationTemplate template = new DynamicNotificationTemplate();
        template.setNotificationId(NOTIFICATION_ID_1);
        template.setBrand("deep");
        template.setLocale("en_US");
        List<AdditionalLookupProperty> additionalLookupProperties1 = new ArrayList<>();
        AdditionalLookupProperty prop1 = new AdditionalLookupProperty();
        prop1.setName("vehicleProfile.modelCode");
        prop1.setValues(new HashSet<>(Arrays.asList("ktf12")));
        additionalLookupProperties1.add(prop1);
        template.setAdditionalLookupProperties(additionalLookupProperties1);
        dynamicNotificationTemplateList.add(template);
        notificationTemplateService.deleteNotificationTemplateByIdBrandLocale(NOTIFICATION_ID_1, "deep", "en_US",
            "modelCode", "KTF12");

        prop1.setValues(new HashSet<>(Arrays.asList("ktf12", "jk147")));
        when(dynamicNotificationTemplateDao.update(Mockito.anyString(), Mockito.any())).thenReturn(true);
        notificationTemplateService.deleteNotificationTemplateByIdBrandLocale(NOTIFICATION_ID_1, "deep", "en_US",
            "modelCode", "KTF12");

        when(dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet()))
            .thenReturn(null);
        assertThrows(NotificationTemplateDoesNotExistException.class,
            () -> notificationTemplateService.deleteNotificationTemplateByIdBrandLocale(NOTIFICATION_ID_1, "deep",
                "en_US", null, null));

    }

    @Test
    public void getNotificationTemplates() {

        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplate.setNotificationShortName("wine");
        dynamicNotificationTemplate.setNotificationLongName("red wine");
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        when(dynamicNotificationTemplateDao.findAll()).thenReturn(dynamicNotificationTemplateList);


        Map<String, String> map = new HashMap<>();
        map.put("notificationId", NOTIFICATION_ID_1);
        map.put("notificationShortName", "wine");
        map.put("notificationLongName", "red wine");
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        List<Map<String, String>> notificationTemplates = notificationTemplateService.getNotificationTemplates();
        assertEquals(notificationTemplates, list);
    }

    private List<NotificationTemplateImportedData> createNotificationTemplateImportedDataList() throws IOException {
        List<NotificationTemplateImportedData> notificationTemplateImportedDataList = new ArrayList<>();
        MultipartFile multipartFile = new MockMultipartFile("testFile", "", "", new byte[1]);
        NotificationTemplateImportedData notificationTemplateImportedData =
            new NotificationTemplateImportedData(NOTIFICATION_ID_1,
                Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));

        notificationTemplateImportedDataList.add(notificationTemplateImportedData);
        return notificationTemplateImportedDataList;
    }
}