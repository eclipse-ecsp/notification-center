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
import org.apache.commons.csv.CSVRecord;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.RichContentImportedDataDAOMongoImpl;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.eclipse.ecsp.notification.entities.RichContentImportedData;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.dto.TemplateProcessingParams;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNotificationIdException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationTemplateDoesNotExistException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.csv.CSVFormat.RFC4180;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INPUT_EMPTY_LOCALE;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INPUT_INVALID_LOCALE;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INPUT_MANDATORY_NOTIFICATION_ID;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INVALID_INPUT_FILE_EXCEPTION;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INVALID_NOTIFICATION_ID_EXCEPTION;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_ID_DOES_NOT_EXIST;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.RICH_HTML_INPUT_MISSING_ATTRIBUTE;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.RICH_HTML_INPUT_MISSING_FILE;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.RICH_HTML_INPUT_MISSING_HEADERS;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.RICH_HTML_INPUT_MISSING_PROPERTIES_HEADER;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.RICH_HTML_INPUT_MISSING_REFERENCE_HTML;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.RICH_HTML_INPUT_MISSING_REFERENCE_HTML_VALUE;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.TEMPLATE_INVALID_FORMAT;
import static org.eclipse.ecsp.platform.notification.service.RichContentNotificationTemplateService.ZIP_FILE_NAME_FORMAT;
import static org.eclipse.ecsp.platform.notification.utils.UnitTestUtils.compareException;
import static org.eclipse.ecsp.platform.notification.utils.UnitTestUtils.compareExceptionMultiParams;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * RichContentNotificationTemplateServiceTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
@RunWith(MockitoJUnitRunner.class)
public class RichContentNotificationTemplateServiceTest {

    private static String IMPORT_RICH_CONTENT_RESOURCES_PATH = "src/test/resources/importRichContent/";

    @InjectMocks
    private RichContentNotificationTemplateService richContentNotificationTemplateService;

    @BeforeEach
    public void setup() {
        initMocks(this);
    }

    @Mock
    private RichContentDynamicNotificationTemplateDAO richContentDynamicNotificationTemplateDao;

    @Mock
    private RichContentImportedDataDAOMongoImpl richContentImportedDataDao;

    @Mock
    private DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    private final RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate1 =
        new RichContentDynamicNotificationTemplate();

    private final RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate2 =
        new RichContentDynamicNotificationTemplate();


    @Test
    public void successfulBuildValidation() throws IOException {
        try {
            InputStream csv = getClass().getClassLoader().getResourceAsStream("rich_content_template_example.csv");
            byte[] bytes = new byte[csv.available()];
            csv.read(bytes);
            StringReader stringReader = new StringReader(new String(bytes));
            List<CSVRecord> csvRecords = RFC4180.parse(stringReader).getRecords();
            File file = new File(getClass().getClassLoader().getResource("unit_test_example.zip").getFile());
            ZipFile zipFile = new ZipFile(file);
            List<RichContentDynamicNotificationTemplate> result =
                richContentNotificationTemplateService.buildHtmlVariants(csvRecords,
                    zipFile, new TemplateProcessingParams());
            assertEquals(4, result.size());
            long englishCount = result.stream()
                .filter(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n"
                    + "<html>\n" +  "  <head> \n"
                    + "      <p style=\"font-family: sans-serif; font-size: 14px; "
                        + "font-weight: normal; margin: 0; Margin-bottom: 15px;\">English Notification</p> \n"
                    +                        "  </head>\n" + "  <body>\n"
                    + "      <p style=\"font-family: sans-serif; font-size: 14px; "
                        + "font-weight: normal; margin: 0; Margin-bottom: 15px;\">Drive Alert</p> \n"
                    + "  </body>\n" + "</html>\n")))
                .count();
            long frenchCount = result.stream()
                .filter(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n"
                        + "<html>\n" + "  <head> \n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; "
                        + "font-weight: normal; margin: 0; Margin-bottom: 15px;\">French Notification</p> \n"
                        + "  </head>\n" + "  <body>\n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; "
                        + "margin: 0; Margin-bottom: 15px;\">Drive Alert</p> \n"
                        + "  </body>\n" + "</html>\n")))
                .count();
            assertEquals(2L, englishCount);
            assertEquals(2L, frenchCount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void successfulBuildValidationWithImages() throws IOException {
        try {
            InputStream csv =
                RichContentNotificationTemplateServiceTest.class.getResourceAsStream("/unit_test_images.csv");
            byte[] bytes = new byte[csv.available()];
            csv.read(bytes);
            StringReader stringReader = new StringReader(new String(bytes));
            List<CSVRecord> csvRecords = RFC4180.parse(stringReader).getRecords();
            File file = new File(getClass().getClassLoader().getResource("unit_test_images_example.zip").getFile());
            ZipFile zipFile = new ZipFile(file);
            List<RichContentDynamicNotificationTemplate> result =
                richContentNotificationTemplateService.buildHtmlVariants(csvRecords,
                    zipFile, new TemplateProcessingParams());
            assertEquals(4, result.size());
            assertTrue(result.stream()
                .anyMatch(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n" + "<html>\n" +  "  <head> \n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; "
                        + "margin: 0; Margin-bottom: 15px;\">English Notification</p> \n"
                        + "  </head>\n" + "  <body>\n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; "
                        + "margin: 0; Margin-bottom: 15px;\">Drive Alert</p> \n"
                        + "  </body>\n" + "</html>\n"))));
            assertEquals(0, result.stream()
                    .filter(r -> r.getHtml().contains("English Notification")
                            && !r.getHtml().contains("cid:images-logo_deep.png"))
                    .findFirst().get().getAttachments().size());
            assertTrue(result.stream()
                .anyMatch(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n" + "<html>\n" + "  <head> \n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; "
                        + "margin: 0; Margin-bottom: 15px;\">French Notification</p> \n"
                        + "  </head>\n" + "  <body>\n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; "
                        + "margin: 0; Margin-bottom: 15px;\">Drive Alert</p> \n"
                        + "  </body>\n" + "</html>\n"))));
            assertEquals(0, result.stream()
                    .filter(r -> r.getHtml().contains("French Notification")
                            && !r.getHtml().contains("cid:images-logo_deep.png"))
                    .findFirst().get().getAttachments().size());
            assertTrue(result.stream()
                .anyMatch(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n" + "<html>\n" + "  <head> \n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0;"
                        + " Margin-bottom: 15px;\">English Notification</p> \n"
                        + "  </head>\n" + "  <body>\n" + "      <p style=\"font-family: sans-serif; font-size: 14px;"
                        + " font-weight: normal; margin: 0; Margin-bottom: 15px;\">Drive Alert</p> \n"
                        + "      <img src=\"cid:images-logo_deep.png\" height=\"60\" style=\"margin: 0; padding: 0; "
                        + "border: none;\" alt=\"Image-hdr_brand\">\n"
                        + "  </body>\n" + "</html>\n"))));
            assertEquals(1, result.stream()
                    .filter(r -> r.getHtml().contains("English Notification")
                            && r.getHtml().contains("cid:images-logo_deep.png"))
                    .findFirst().get().getAttachments().size());
            assertTrue(result.stream()
                .anyMatch(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n" + "<html>\n" + "  <head> \n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0; "
                        + "Margin-bottom: 15px;\">French Notification</p> \n"
                        + "  </head>\n" + "  <body>\n" + "      <p style=\"font-family: sans-serif; font-size: 14px; "
                        + "font-weight: normal; margin: 0; Margin-bottom: 15px;\">Drive Alert</p> \n"
                        + "      <img src=\"cid:images-logo_deep.png\" height=\"60\" style=\"margin: 0; padding: 0;"
                        + " border: none;\" alt=\"Image-hdr_brand\">\n" + "  </body>\n" + "</html>\n"))));
            assertEquals(1, result.stream()
                    .filter(r -> r.getHtml().contains("French Notification")
                            && r.getHtml().contains("cid:images-logo_deep.png"))
                    .findFirst().get().getAttachments().size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void successfulBuildValidationWithAdditionalLookupProperties() throws IOException {
        try {
            InputStream csv = RichContentNotificationTemplateServiceTest.class
                .getResourceAsStream("/ProperFileWithgetAdditionalLookupProperties.csv");
            byte[] bytes = new byte[csv.available()];
            csv.read(bytes);
            StringReader stringReader = new StringReader(new String(bytes));
            List<CSVRecord> csvRecords = RFC4180.parse(stringReader).getRecords();
            File file = new File(
                getClass().getClassLoader()
                    .getResource("importRichContent/ProperFileWithAdditionalLookupProperties.zip").getFile());
            ZipFile zipFile = new ZipFile(file);
            List<RichContentDynamicNotificationTemplate> result =
                richContentNotificationTemplateService.buildHtmlVariants(csvRecords,
                    zipFile, new TemplateProcessingParams());
            long englishCount = result.stream()
                .filter(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n" + "<html>\n" + "  <head> \n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0;"
                        + " Margin-bottom: 15px;\">English Notification</p> \n" + "  </head>\n" + "  <body>\n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0;"
                        + " Margin-bottom: 15px;\">Drive Alert</p> \n"
                        + "  </body>\n" + "</html>\n")))
                .count();
            long frenchCount = result.stream()
                .filter(r -> StringUtils.trimAllWhitespace(r.getHtml())
                    .equals(StringUtils.trimAllWhitespace("<!doctype html>\n" + "<html>\n" + "  <head> \n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; margin: 0;"
                        + " Margin-bottom: 15px;\">French Notification</p> \n"
                        + "  </head>\n" + "  <body>\n"
                        + "      <p style=\"font-family: sans-serif; font-size: 14px; font-weight: normal; "
                        + "margin: 0; Margin-bottom: 15px;\">Drive Alert</p> \n"
                        + "  </body>\n" + "</html>\n")))
                .count();

            assertEquals(4, result.size());
            assertEquals(2L, englishCount);
            assertEquals(2L, frenchCount);
            long additionalLookupPropertiesF =
                result.stream().filter(r -> r.getAdditionalLookupProperties().size() == 1
                    && r.getAdditionalLookupProperties().get(0).getName().equals("vehicleProfile.model")
                    && r.getAdditionalLookupProperties().get(0).getValues().contains("duna")
                    && r.getAdditionalLookupProperties().get(0).getValues().contains("mobi")
                    && r.getAdditionalLookupProperties().get(0).getOrder() == 1).count();
            long additionalLookupPropertiesAlfa =
                result.stream().filter(r -> r.getAdditionalLookupProperties().size() == 1
                    && r.getAdditionalLookupProperties().get(0).getName().equals("vehicleProfile.model")
                    && r.getAdditionalLookupProperties().get(0).getValues().contains("romeo")
                    && r.getAdditionalLookupProperties().get(0).getOrder() == 1).count();
            assertEquals(2L, additionalLookupPropertiesF);
            assertEquals(2L, additionalLookupPropertiesAlfa);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void failedExtractImageValidation() throws IOException {
        ZipFile zipFile = mock(ZipFile.class);
        ZipEntry zipEntry = mock(ZipEntry.class);
        when(zipFile.getEntry(any())).thenReturn(zipEntry);
        when(zipFile.getInputStream(zipEntry)).thenThrow(new RuntimeException());
        try {
            richContentNotificationTemplateService.extractImage(zipFile, "images/fita_logo.gif");
        } catch (Exception e) {
            assertEquals("Invalid image", e.getMessage());
        }
    }

    @Test
    public void failedConvertZipEntryToStringValidation() throws IOException {
        ZipFile zipFile = mock(ZipFile.class);
        BufferedReader br = mock(BufferedReader.class);
        Mockito.lenient().when(br.readLine()).thenThrow(new RuntimeException());
        try {
            richContentNotificationTemplateService.convertZipEntryToString(zipFile, "htmls/generic.html");
        } catch (Exception e) {
            assertEquals("Invalid image", e.getMessage());
        }
    }

    @Test
    public void getRichHtmlNotificationTemplatesNoTemplates() {
        when(richContentDynamicNotificationTemplateDao.findAll()).thenReturn(new ArrayList<>());

        List<Map<String, String>> data = richContentNotificationTemplateService.getRichHtmlNotificationTemplates();

        assertThat(data.size()).isEqualTo(0);
    }

    @Test
    public void getRichHtmlNotificationTemplatesTwoTemplates() {
        List<RichContentDynamicNotificationTemplate> list = new ArrayList<>(2);
        richContentDynamicNotificationTemplate1.setNotificationId("x");
        richContentDynamicNotificationTemplate2.setNotificationId("y");
        list.add(richContentDynamicNotificationTemplate1);
        list.add(richContentDynamicNotificationTemplate2);

        when(richContentDynamicNotificationTemplateDao.findAll()).thenReturn(list);

        List<Map<String, String>> data = richContentNotificationTemplateService.getRichHtmlNotificationTemplates();

        assertThat(data.size()).isEqualTo(2);
    }

    @Test
    public void getRichHtmlNotificationTemplatesTwoSameIdsTemplates() {
        List<RichContentDynamicNotificationTemplate> list = new ArrayList<>(2);
        richContentDynamicNotificationTemplate1.setNotificationId("x");
        richContentDynamicNotificationTemplate2.setNotificationId("x");
        list.add(richContentDynamicNotificationTemplate1);
        list.add(richContentDynamicNotificationTemplate2);

        when(richContentDynamicNotificationTemplateDao.findAll()).thenReturn(list);

        List<Map<String, String>> data = richContentNotificationTemplateService.getRichHtmlNotificationTemplates();

        assertThat(data.size()).isEqualTo(1);
    }

    @Test
    public void exportValidationOneMissingId() {
        NotificationTemplateFilterDto notificationTemplateFilterDto = new NotificationTemplateFilterDto();
        notificationTemplateFilterDto.setNotificationTemplateIds(Collections.singletonList("low_tire"));
        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(Mockito.any())).thenReturn(
            new ArrayList<>());
        NotificationCenterExceptionBase thrown = assertThrows(InvalidNotificationIdException.class,
            () -> richContentNotificationTemplateService.filter(notificationTemplateFilterDto),
            "Expected to throw, but it didn't");

        compareException(thrown, INVALID_NOTIFICATION_ID_EXCEPTION, NOTIFICATION_ID_DOES_NOT_EXIST, "low_tire");
    }

    @Test
    public void exportValidationOneOutOfTwoMissingId() {
        NotificationTemplateFilterDto notificationTemplateFilterDto = new NotificationTemplateFilterDto();
        notificationTemplateFilterDto.setNotificationTemplateIds(Arrays.asList("low_tire", "low_tire2"));
        richContentDynamicNotificationTemplate1.setNotificationId("low_tire2");
        List<RichContentDynamicNotificationTemplate> returnedList =
            Collections.singletonList(richContentDynamicNotificationTemplate1);
        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(Mockito.any())).thenReturn(returnedList);
        NotificationCenterExceptionBase thrown = assertThrows(InvalidNotificationIdException.class,
            () -> richContentNotificationTemplateService.filter(notificationTemplateFilterDto),
            "Expected to throw, but it didn't");

        compareException(thrown, INVALID_NOTIFICATION_ID_EXCEPTION, NOTIFICATION_ID_DOES_NOT_EXIST, "low_tire");
    }

    @Test
    public void exportValidationTwoOutOfFourMissingId() {
        NotificationTemplateFilterDto notificationTemplateFilterDto = new NotificationTemplateFilterDto();
        notificationTemplateFilterDto.setNotificationTemplateIds(
            Arrays.asList("alow_tire", "blow_tire2", "clow_tire3", "dlow_tire4"));
        richContentDynamicNotificationTemplate1.setNotificationId("dlow_tire4");
        richContentDynamicNotificationTemplate2.setNotificationId("blow_tire2");
        List<RichContentDynamicNotificationTemplate> returnedList =
            Arrays.asList(richContentDynamicNotificationTemplate1,
                richContentDynamicNotificationTemplate2);
        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(Mockito.any())).thenReturn(returnedList);
        NotificationCenterExceptionBase thrown = assertThrows(InvalidNotificationIdException.class,
            () -> richContentNotificationTemplateService.filter(notificationTemplateFilterDto),
            "Expected to throw, but it didn't");

        compareException(thrown, INVALID_NOTIFICATION_ID_EXCEPTION, NOTIFICATION_ID_DOES_NOT_EXIST,
            Arrays.asList("alow_tire", "clow_tire3"));
    }

    @Test
    public void exportEmptyRequest() throws Exception {
        String notificationId = "S04_DRIVE_ALRT_VALETBREACH";

        richContentDynamicNotificationTemplate1.setNotificationId(notificationId);
        List<RichContentDynamicNotificationTemplate> returnedList =
            Collections.singletonList(richContentDynamicNotificationTemplate1);
        when(richContentDynamicNotificationTemplateDao.findAll()).thenReturn(returnedList);

        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "ProperFile.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        RichContentImportedData importedData = new RichContentImportedData(notificationId,
            Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));
        when(richContentImportedDataDao.findById(notificationId)).thenReturn(importedData);

        NotificationTemplateFilterDto notificationTemplateFilterDto = new NotificationTemplateFilterDto();
        byte[] response = richContentNotificationTemplateService.filter(notificationTemplateFilterDto);
        MultipartFile responseFile = new MockMultipartFile("testFile", "", "", response);
        ZipFile zipFile = richContentNotificationTemplateService.getZipContent(responseFile);
        ZipEntry zipEntry = zipFile.getEntry(String.format(ZIP_FILE_NAME_FORMAT, notificationId));
        assertEquals(zipEntry.getSize(), multipartFile.getSize());
    }

    @Test
    public void exportNotificationId() throws Exception {
        String notificationId = "S04_DRIVE_ALRT_VALETBREACH";

        richContentDynamicNotificationTemplate1.setNotificationId(notificationId);
        List<RichContentDynamicNotificationTemplate> returnedList =
            Collections.singletonList(richContentDynamicNotificationTemplate1);
        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(Collections.singletonList(notificationId)))
            .thenReturn(returnedList);

        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "ProperFile.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        RichContentImportedData importedData = new RichContentImportedData(notificationId,
            Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));
        when(richContentImportedDataDao.findById(notificationId)).thenReturn(importedData);

        NotificationTemplateFilterDto notificationTemplateFilterDto = new NotificationTemplateFilterDto();
        notificationTemplateFilterDto.setNotificationTemplateIds(Collections.singletonList(notificationId));
        byte[] response = richContentNotificationTemplateService.filter(notificationTemplateFilterDto);
        MultipartFile responseFile = new MockMultipartFile("testFile", "", "", response);
        ZipFile zipFile = richContentNotificationTemplateService.getZipContent(responseFile);
        ZipEntry zipEntry = zipFile.getEntry(String.format(ZIP_FILE_NAME_FORMAT, notificationId));
        assertEquals(zipEntry.getSize(), multipartFile.getSize());
    }

    @Test
    public void importRichContentProperFile() throws Exception {
        when(dynamicNotificationTemplateDao.isNotificationIdExist(Mockito.any())).thenReturn(true);
        RichContentDynamicNotificationTemplate template1 = new RichContentDynamicNotificationTemplate();
        template1.setId("Fita_en-us_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template2 = new RichContentDynamicNotificationTemplate();
        template2.setId("Fita_fr-ca_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template3 = new RichContentDynamicNotificationTemplate();
        template3.setId("Alfa_en-us_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template4 = new RichContentDynamicNotificationTemplate();
        template4.setId("Alfa_fr-ca_S04_DRIVE_ALRT_VALETBREACH");
        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(
            Collections.singletonList("S04_DRIVE_ALRT_VALETBREACH")))
            .thenReturn(Arrays.asList(template1, template2, template3, template4));
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "ProperFile.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        RichContentImportedData newEntity = new RichContentImportedData("S04_DRIVE_ALRT_VALETBREACH",
            Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));

        String warning = richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false);
        assertTrue(StringUtils.isEmpty(warning));
        verify(richContentImportedDataDao, times(1)).deleteById("S04_DRIVE_ALRT_VALETBREACH");
        verify(richContentImportedDataDao, times(1)).save(Mockito.any());
        verify(richContentDynamicNotificationTemplateDao, times(1)).deleteByIds(template1.getId(), template2.getId(),
            template3.getId(),
            template4.getId());

        verify(richContentDynamicNotificationTemplateDao, times(1)).saveAll(
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class));


    }

    @Test
    public void importRichContentFileV2() throws Exception {

        RichContentDynamicNotificationTemplate template1 = new RichContentDynamicNotificationTemplate();
        template1.setId("Fita_en-us_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template2 = new RichContentDynamicNotificationTemplate();
        template2.setId("Fita_fr-ca_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template3 = new RichContentDynamicNotificationTemplate();
        template3.setId("Alfa_en-us_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template4 = new RichContentDynamicNotificationTemplate();
        template4.setId("Alfa_fr-ca_S04_DRIVE_ALRT_VALETBREACH");
        List<RichContentDynamicNotificationTemplate> notificationTemplates = new ArrayList<>();
        notificationTemplates.addAll(Arrays.asList(template1, template2, template3, template4));
        when(richContentDynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet()))
            .thenReturn(notificationTemplates);
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "ProperFile.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        RichContentImportedData newEntity = new RichContentImportedData("S04_DRIVE_ALRT_VALETBREACH",
            Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));
        String warning = richContentNotificationTemplateService.importNotificationTemplate(multipartFile, true);

        verify(richContentImportedDataDao, times(1)).deleteById("S04_DRIVE_ALRT_VALETBREACH");
        verify(richContentImportedDataDao, times(1)).save(Mockito.any());
        verify(richContentDynamicNotificationTemplateDao, times(1)).deleteByIds(template1.getId(), template2.getId(),
            template3.getId(),
            template4.getId());
        verify(richContentDynamicNotificationTemplateDao, times(1)).saveAll(
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class));
        //        assertTrue(StringUtils.isEmpty(warning));
    }

    @Test
    public void importRichContentFileV2InvalidFile() throws Exception {

        String filePath = RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH
            + "properFileWithMulAdditionalLookupProperties.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, true),
            "Expected to throw, but it didn't");

    }


    @Test
    public void importRichContentProperFileWithEmptyAdditionalLookupProperties() throws Exception {
        when(dynamicNotificationTemplateDao.isNotificationIdExist(Mockito.any())).thenReturn(true);
        RichContentDynamicNotificationTemplate template1 = new RichContentDynamicNotificationTemplate();
        template1.setId("Fita_en-us_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template2 = new RichContentDynamicNotificationTemplate();
        template2.setId("Fita_fr-ca_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template3 = new RichContentDynamicNotificationTemplate();
        template3.setId("Alfa_en-us_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template4 = new RichContentDynamicNotificationTemplate();
        template4.setId("Alfa_fr-ca_S04_DRIVE_ALRT_VALETBREACH");
        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(
            Collections.singletonList("S04_DRIVE_ALRT_VALETBREACH")))
            .thenReturn(Arrays.asList(template1, template2, template3, template4));
        String filePath = RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH
            + "properFileWithEmptyAdditionalLookupProperties.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        RichContentImportedData newEntity = new RichContentImportedData("S04_DRIVE_ALRT_VALETBREACH",
            Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));

        String warning = richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false);
        assertTrue(StringUtils.isEmpty(warning));
        verify(richContentImportedDataDao, times(1)).deleteById("S04_DRIVE_ALRT_VALETBREACH");
        verify(richContentImportedDataDao, times(1)).save(Mockito.any());
        verify(richContentDynamicNotificationTemplateDao, times(1)).deleteByIds(template1.getId(), template2.getId(),
            template3.getId(),
            template4.getId());
        verify(richContentDynamicNotificationTemplateDao, times(1)).saveAll(
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class));


    }

    @Test
    public void importRichContentProperFileWithAdditionalLookupPropertiesV2() throws Exception {

        RichContentDynamicNotificationTemplate template1 = new RichContentDynamicNotificationTemplate();
        template1.setId("Fita_en-us_S04_DRIVE_ALRT_VALETBREACH");
        List<AdditionalLookupProperty> additionalLookupProperties = new ArrayList<>();
        AdditionalLookupProperty prop = new AdditionalLookupProperty();
        prop.setName("vehicleProfile.model");
        prop.setValues(new HashSet<>(Arrays.asList("rio", "picanto")));
        additionalLookupProperties.add(prop);
        template1.setAdditionalLookupProperties(additionalLookupProperties);
        RichContentDynamicNotificationTemplate template2 = new RichContentDynamicNotificationTemplate();
        template2.setId("Fita_fr-ca_S04_DRIVE_ALRT_VALETBREACH");

        AdditionalLookupProperty prop1 = new AdditionalLookupProperty();
        prop1.setName("vehicleProfile.modelYear");
        prop1.setValues(new HashSet<>(Arrays.asList("2000", "2020")));
        AdditionalLookupProperty prop11 = new AdditionalLookupProperty();
        prop11.setName("vehicleProfile.modelCode");
        prop11.setValues(new HashSet<>(Arrays.asList("rio", "picanto")));
        List<AdditionalLookupProperty> additionalLookupPropertiesMul = new ArrayList<>();
        additionalLookupPropertiesMul.add(prop11);
        template2.setAdditionalLookupProperties(additionalLookupPropertiesMul);
        RichContentDynamicNotificationTemplate template3 = new RichContentDynamicNotificationTemplate();
        template3.setId("Alfa_en-us_S04_DRIVE_ALRT_VALETBREACH");
        RichContentDynamicNotificationTemplate template4 = new RichContentDynamicNotificationTemplate();
        template4.setId("Alfa_fr-ca_S04_DRIVE_ALRT_VALETBREACH");

        List<RichContentDynamicNotificationTemplate> notificationTemplates = new ArrayList<>();
        List<RichContentDynamicNotificationTemplate> notificationTemplatesLst = new ArrayList<>();
        notificationTemplates.addAll(Arrays.asList(template3, template4));
        notificationTemplatesLst.addAll(Arrays.asList(template1, template2));
        when(richContentDynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet()))
            .thenReturn(notificationTemplates);
        when(richContentDynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(Mockito.anyString(),
            Mockito.anySet(),
            Mockito.anySet(), Mockito.anyString(), Mockito.anySet())).thenReturn(notificationTemplatesLst);
        String filePath = RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH
            + "properFileWithEmptyAdditionalLookupProperties.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        RichContentImportedData newEntity = new RichContentImportedData("S04_DRIVE_ALRT_VALETBREACH",
            Base64.encodeBase64String(IOUtils.toByteArray(multipartFile.getInputStream())));
        String warning = richContentNotificationTemplateService.importNotificationTemplate(multipartFile, true);

        verify(richContentImportedDataDao, times(1)).deleteById("S04_DRIVE_ALRT_VALETBREACH");
        verify(richContentImportedDataDao, times(1)).save(Mockito.any());
        verify(richContentDynamicNotificationTemplateDao, times(1)).deleteByIds(template3.getId(), template4.getId(),
            template1.getId(),
            template2.getId());
        verify(richContentDynamicNotificationTemplateDao, times(1)).saveAll(
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class),
            Mockito.any(RichContentDynamicNotificationTemplate.class));
        //        assertTrue(StringUtils.isEmpty(warning));
    }

    @Test
    public void importRichContentProperFileWithWarning() throws Exception {
        when(dynamicNotificationTemplateDao.isNotificationIdExist(Mockito.any())).thenReturn(false);
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "ProperFile.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        String warning = richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false);

        assertEquals(RichContentNotificationTemplateService.WARNING_MISSING_NOTIFICATION_ID, warning);
    }

    @Test
    public void importRichContentInvalidLocale() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "InvalidLocale.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION,
            Arrays.asList(INPUT_INVALID_LOCALE, RICH_HTML_INPUT_MISSING_FILE),
            Arrays.asList("[eu-it]", "[htmls/generic.html, images/alfa_logo.gif, images/fita_logo.gif]"));
    }

    @Test
    public void importRichContentNotUtf8() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "MissingUtf8Bom.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION, Collections.singletonList(TEMPLATE_INVALID_FORMAT),
            Collections.emptyList());
    }

    @Test
    public void importRichContentNoLocales() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "NoLanguages.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION, RICH_HTML_INPUT_MISSING_PROPERTIES_HEADER);
    }

    @Test
    public void importRichContentMissingHtml() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "MissingHtml.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION, Collections.singletonList(RICH_HTML_INPUT_MISSING_FILE),
            Collections.singletonList("[htmls/generic.html]"));
    }

    @Test
    public void importRichContentMissingImage() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "MissingImage.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION, Collections.singletonList(RICH_HTML_INPUT_MISSING_FILE),
            Collections.singletonList("[images/fita_logo.gif]"));
    }

    @Test
    public void importRichContentMissingLanguage() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "MissingLanguage.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION,
            Arrays.asList(INPUT_EMPTY_LOCALE, RICH_HTML_INPUT_MISSING_FILE, INPUT_INVALID_LOCALE),
            Collections.singletonList("[htmls/generic.html, images/alfa_logo.gif, images/fita_logo.gif]"));
    }

    @Test
    public void importRichContentMissingNotifcationIdHeader() throws Exception {
        String filePath = RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH
            + "MissingNotificationIDHeader.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION,
            Arrays.asList(RICH_HTML_INPUT_MISSING_HEADERS, RICH_HTML_INPUT_MISSING_FILE, INPUT_INVALID_LOCALE),
            Arrays.asList("[eu-it]", "[htmls/generic.html, images/alfa_logo.gif, images/fita_logo.gif]"));
    }

    @Test
    public void importRichContentMissingNotifcationId() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "MissingNotificationID.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION,
            Arrays.asList(INPUT_MANDATORY_NOTIFICATION_ID, RICH_HTML_INPUT_MISSING_FILE, INPUT_INVALID_LOCALE),
            Arrays.asList("[eu-it]", "[htmls/generic.html, images/alfa_logo.gif, images/fita_logo.gif]"));
    }

    @Test
    public void importRichContentMissingPlaceHolder() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "MissingPlaceHolder.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareExceptionMultiParams(thrown, INVALID_INPUT_FILE_EXCEPTION, RICH_HTML_INPUT_MISSING_ATTRIBUTE,
            Arrays.asList(Arrays.asList("body-paragraph3-text", "htmls/generic.html", "Alfa"),
                Arrays.asList("hdr-text-css", "htmls/generic.html", "Fita"),
                Arrays.asList("bg-css", "htmls/generic.html", "Alfa")));
    }

    @Test
    public void importRichContentMissingReferenceHtml() throws Exception {
        String filePath =
            RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "MissingReferenceHtml.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION,
            Arrays.asList(RICH_HTML_INPUT_MISSING_FILE, RICH_HTML_INPUT_MISSING_REFERENCE_HTML),
            Arrays.asList("[htmls/generic.html, images/alfa_logo.gif, images/fita_logo.gif]", "[Fita]"));
    }

    @Test
    public void importRichContentMissingReferenceHtmlAndValue() throws Exception {
        String filePath = RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH
            + "MissingReferenceHtmlAndValue.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION,
            Arrays.asList(RICH_HTML_INPUT_MISSING_REFERENCE_HTML, RICH_HTML_INPUT_MISSING_REFERENCE_HTML_VALUE,
                RICH_HTML_INPUT_MISSING_FILE),
            Arrays.asList("{Fita=[en-us, fr-ca]}", "[Alfa]",
                "[htmls/generic.html, images/alfa_logo.gif, images/fita_logo.gif]"));
    }

    @Test
    public void importRichContentMissingReferenceHtmlValue() throws Exception {
        String filePath = RichContentNotificationTemplateServiceTest.IMPORT_RICH_CONTENT_RESOURCES_PATH
            + "MissingReferenceHtmlValue.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));

        NotificationCenterExceptionBase thrown = assertThrows(InvalidInputFileException.class,
            () -> richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false),
            "Expected to throw, but it didn't");
        compareException(thrown, INVALID_INPUT_FILE_EXCEPTION,
            Arrays.asList(RICH_HTML_INPUT_MISSING_REFERENCE_HTML_VALUE, RICH_HTML_INPUT_MISSING_FILE),
            Arrays.asList("{Alfa=[fr-ca]}", "[htmls/generic.html, images/alfa_logo.gif, images/fita_logo.gif]"));
    }

    @Test
    public void deleteRichContent() {
        String notificationId = "notificationId";
        List<String> notificationIds = Collections.singletonList(notificationId);
        RichContentDynamicNotificationTemplate templateEntity = new RichContentDynamicNotificationTemplate();
        templateEntity.setId(notificationId);
        templateEntity.setNotificationId(notificationId);

        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(notificationIds))
            .thenReturn(Collections.singletonList(templateEntity));
        when(richContentDynamicNotificationTemplateDao.deleteByIds(notificationIds.toArray(new String[0]))).thenReturn(
            1);
        when(richContentImportedDataDao.deleteByIds(notificationIds.toArray(new String[0]))).thenReturn(1);

        NotificationTemplateFilterDto deleteRequest = new NotificationTemplateFilterDto();
        deleteRequest.setNotificationTemplateIds(notificationIds);
        Set<String> results =
            richContentNotificationTemplateService.deleteRichContentNotificationTemplates(deleteRequest);
        assertEquals(0, results.size());
        verify(richContentImportedDataDao, times(1)).deleteByIds(notificationIds.toArray(new String[0]));
        verify(richContentDynamicNotificationTemplateDao, times(1)).deleteByIds(notificationIds.toArray(new String[0]));
    }

    @Test
    public void deleteRichContentV2() {

        String notificationId = "notificationId";

        RichContentDynamicNotificationTemplate templateEntity = new RichContentDynamicNotificationTemplate();
        templateEntity.setId(notificationId);
        templateEntity.setNotificationId(notificationId);
        templateEntity.setBrand("kia");
        templateEntity.setLocale("en_US");
        List<AdditionalLookupProperty> additionalLookupProperties = new ArrayList<>();
        AdditionalLookupProperty prop = new AdditionalLookupProperty();
        prop.setName("vehicleProfile.model");
        prop.setValues(new HashSet<>(Arrays.asList("sonet")));
        additionalLookupProperties.add(prop);
        templateEntity.setAdditionalLookupProperties(additionalLookupProperties);
        List<RichContentDynamicNotificationTemplate> notificationTemplates = new ArrayList<>();
        notificationTemplates.add(templateEntity);
        List<String> notificationIds = Collections.singletonList(notificationId);
        when(richContentDynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(Mockito.anyString(),
            Mockito.anySet(),
            Mockito.anySet(), Mockito.anyString(), Mockito.anySet())).thenReturn(notificationTemplates);
        when(richContentDynamicNotificationTemplateDao.deleteByIds(notificationIds.toArray(new String[0]))).thenReturn(
            1);

        richContentNotificationTemplateService.deleteNotificationTemplateByIdBrandLocale(notificationId, "kia", "en_US",
            "make", "sonet");

        List<AdditionalLookupProperty> additionalLookupProperties1 = new ArrayList<>();
        AdditionalLookupProperty prop1 = new AdditionalLookupProperty();
        prop1.setName("vehicleProfile.modelCode");
        prop1.setValues(new HashSet<>(Arrays.asList("jwt56")));
        additionalLookupProperties1.add(prop1);
        templateEntity.setAdditionalLookupProperties(additionalLookupProperties1);

        richContentNotificationTemplateService.deleteNotificationTemplateByIdBrandLocale(notificationId, "kia", "en_US",
            "modelCode", "JWT56");

        prop1.setValues(new HashSet<>(Arrays.asList("ktf12", "jwt56")));
        when(richContentDynamicNotificationTemplateDao.update(Mockito.anyString(), Mockito.any())).thenReturn(true);
        richContentNotificationTemplateService.deleteNotificationTemplateByIdBrandLocale(notificationId, "kia", "en_US",
            "modelCode", "JWT56");

        additionalLookupProperties1.add(prop);
        richContentNotificationTemplateService.deleteNotificationTemplateByIdBrandLocale(notificationId, "kia", "en_US",
            "modelCode", "JWT56");

        when(richContentDynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(Mockito.anyString(),
            Mockito.anySet(), Mockito.anySet()))
            .thenReturn(null);

        assertThrows(NotificationTemplateDoesNotExistException.class,
            () -> richContentNotificationTemplateService.deleteNotificationTemplateByIdBrandLocale(notificationId,
                "kia", "en_US", null, null));
        verify(richContentDynamicNotificationTemplateDao, times(2)).deleteByIds(notificationIds.toArray(new String[0]));
    }

    @Test
    public void deleteRichContentMissingNotification() {
        String notificationId1 = "notificationId1";
        String notificationId2 = "notificationId2";
        List<String> notificationIds = Arrays.asList(notificationId1, notificationId2);

        RichContentDynamicNotificationTemplate templateEntity = new RichContentDynamicNotificationTemplate();
        templateEntity.setId(notificationId1);
        templateEntity.setNotificationId(notificationId1);

        when(richContentDynamicNotificationTemplateDao.findByNotificationIds(notificationIds))
            .thenReturn(Collections.singletonList(templateEntity));
        when(richContentDynamicNotificationTemplateDao.deleteByIds(notificationId1)).thenReturn(1);
        when(richContentImportedDataDao.deleteByIds(notificationIds.toArray(new String[0]))).thenReturn(1);

        NotificationTemplateFilterDto deleteRequest = new NotificationTemplateFilterDto();
        deleteRequest.setNotificationTemplateIds(notificationIds);
        Set<String> results =
            richContentNotificationTemplateService.deleteRichContentNotificationTemplates(deleteRequest);
        assertEquals(1, results.size());
        List<String> existingIds = Collections.singletonList(notificationId1);
        verify(richContentImportedDataDao, times(1)).deleteByIds(notificationIds.toArray(new String[0]));
        verify(richContentDynamicNotificationTemplateDao, times(1)).deleteByIds(existingIds.toArray(new String[0]));
    }
}