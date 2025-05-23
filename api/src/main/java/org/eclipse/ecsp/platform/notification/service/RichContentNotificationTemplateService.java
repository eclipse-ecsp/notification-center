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

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.RichContentImportedDataDAO;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.eclipse.ecsp.notification.entities.RichContentImportedData;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.dto.TemplateProcessingParams;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidImageException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationTemplateDoesNotExistException;
import org.eclipse.ecsp.platform.notification.utils.NotificationUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.csv.CSVFormat.RFC4180;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.addCustomPlaceHolder;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.createAdditionalLookupProperties;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.findNumOfAdditionalLookupProperties;

/**
 * RichContentNotificationTemplateService class.
 */
@Service
public class RichContentNotificationTemplateService extends BaseNotificationTemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RichContentNotificationTemplateService.class);

    private static final String[] RICH_HTML_HEADERS = {"Brand", "Attribute"};
    public static final String MEDIA_TYPE = "image/jpeg";
    private static final Map<String, String> IMAGE_EXTENSION_TO_MIME_TYPE = new HashMap<String, String>();

    static {

        IMAGE_EXTENSION_TO_MIME_TYPE.put(".apng", "image/apng");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".bmp", "image/bmp");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".gif", "image/gif");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".ico", "image/x-icon");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".cur", "image/x-icon");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".jpg", MEDIA_TYPE);
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".jpeg", MEDIA_TYPE);
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".jfif", MEDIA_TYPE);
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".pjpeg", MEDIA_TYPE);
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".pjp", MEDIA_TYPE);
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".png", "image/png");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".svg", "image/svg+xml");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".tif", "image/tiff");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".tiff", "image/tiff");
        IMAGE_EXTENSION_TO_MIME_TYPE.put(".webp", "image/webp");
    }

    static final String ZIP_FILE_NAME_FORMAT = "Ignite_Notification_Center_Rich_HTML_Attributes_%s.zip";
    private static final String REFERENCE_HTML_ATTRIBUTE_NAME = "Reference-HTML";
    private static final String IMAGE_ATTRIBUTE_PREFIX = "Image-";
    private static final String CSV_FILE_EXTENSION = ".csv";
    private static final String CID_FORMAT = "cid:%s";
    private static final int NOTIFICATION_ID_COLUMN = 0;
    private static final int BRAND_COLUMN = 0;
    private static final int ATTRIBUTE_COLUMN = 1;
    private static final int ATTRIBUTE_VALUE_COLUMN = 2;
    private static final int RICH_HTML_START_ROW = 3;
    static final String WARNING_MISSING_NOTIFICATION_ID =
        "Notification ID of imported rich HTML content is not found "
            + "within the notification center, but content has been uploaded";
    /**
     * BRAND_AND_ADDITIONAL_LOOKUP_PROPERTIES_SEPARATOR.
     */
    public static final String BRAND_AND_ADDITIONAL_LOOKUP_PROPERTIES_SEPARATOR = " additional lookup properties ";

    private final RichContentDynamicNotificationTemplateDAO richContentDynamicNotificationTemplateDao;

    private final DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    private final RichContentImportedDataDAO richContentImportedDataDao;

    /**
     * RichContentNotificationTemplateService constructor.
     */
    @Autowired
    public RichContentNotificationTemplateService(
        RichContentDynamicNotificationTemplateDAO richContentDynamicNotificationTemplateDao,
        DynamicNotificationTemplateDAO dynamicNotificationTemplateDao,
        RichContentImportedDataDAO richContentImportedDataDao) {
        this.richContentDynamicNotificationTemplateDao = richContentDynamicNotificationTemplateDao;
        this.dynamicNotificationTemplateDao = dynamicNotificationTemplateDao;
        this.richContentImportedDataDao = richContentImportedDataDao;
    }

    /**
     * <p>Save the data regarding a specific notification ID including the imported
     * file itself (as zip file).</p>
     *
     * @param file the zip file with all the notification data
     *
     * @return warning message if the notification ID specified by the imported file does not exist as
     *     notification template in the notification center
     *
     * @throws IOException validation exception or unexpected
     */
    public String importNotificationTemplate(MultipartFile file, boolean isPatchUpdate) throws IOException {
        ZipFile zipFile = getZipContent(file);
        List<CSVRecord> csvRecords = getCsvRecords(zipFile);

        if (CollectionUtils.isEmpty(csvRecords)) {
            LOGGER.error("Zip file does not contain a csv file");
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.RICH_HTML_INPUT_MISSING_CSV_FILE.toMessage()));
        }
        TemplateProcessingParams templateProcessingParams = new TemplateProcessingParams();
        validateZipContent(zipFile, csvRecords, isPatchUpdate);
        List<RichContentDynamicNotificationTemplate> richContentEntities =
            buildHtmlVariants(csvRecords, zipFile, templateProcessingParams);
        if (isPatchUpdate) {
            saveRichTemplateByIdLocaleBrand(richContentEntities, templateProcessingParams);
        } else {
            saveRichHtmlNotificationTemplate(richContentEntities);
        }

        String notificationId = csvRecords.get(NOTIFICATION_DESCRIPTION_DATA_ROW).get(NOTIFICATION_ID_COLUMN);
        saveZipFile(file, notificationId);

        if (!dynamicNotificationTemplateDao.isNotificationIdExist(notificationId)) {
            return WARNING_MISSING_NOTIFICATION_ID;
        }
        return null;
    }

    /**
     * Get RichHtml Notification Templates.
     *
     */
    public List<Map<String, String>> getRichHtmlNotificationTemplates() {
        List<RichContentDynamicNotificationTemplate> richHtmlNotificationTemplateList =
            richContentDynamicNotificationTemplateDao.findAll();
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map;
        Set<String> notificationIds = new HashSet<>();
        for (RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate
            : richHtmlNotificationTemplateList) {
            if (!notificationIds.contains(richContentDynamicNotificationTemplate.getNotificationId())) {
                map = new HashMap<>();
                map.put("notificationId", richContentDynamicNotificationTemplate.getNotificationId());
                map.put("lastUpdateTime", richContentDynamicNotificationTemplate.getLastUpdateTime());
                data.add(map);
                notificationIds.add(richContentDynamicNotificationTemplate.getNotificationId());
            }
        }
        return data;
    }

    /**
     * Delete rich content template.
     *
     * @param notificationIds notificationIds
     *
     * @return notificationIds of missing templates
     */
    public Set<String> deleteRichContentNotificationTemplates(NotificationTemplateFilterDto notificationIds) {
        List<RichContentDynamicNotificationTemplate> notificationTemplates = richContentDynamicNotificationTemplateDao
            .findByNotificationIds(notificationIds.getNotificationTemplateIds());
        richContentDynamicNotificationTemplateDao.deleteByIds(
            notificationTemplates.stream().map(RichContentDynamicNotificationTemplate::getId).distinct()
                .toArray(String[]::new));

        try {
            richContentImportedDataDao.deleteByIds(notificationIds.getNotificationTemplateIds().toArray(new String[0]));
        } catch (Exception e) {
            LOGGER.error("Failed to delete zip files " + notificationIds.getNotificationTemplateIds() + " from DB", e);
        }

        Set<String> existingNotificationIds =
            notificationTemplates.stream().map(RichContentDynamicNotificationTemplate::getNotificationId)
                .collect(toSet());
        return notificationIds.getNotificationTemplateIds().stream().filter(n -> !existingNotificationIds.contains(n))
            .collect(toSet());
    }

    /**
     * Delete template by filters.
     */
    public void deleteNotificationTemplateByIdBrandLocale(String notificationId, String brand, String locale,
                                                          String propertyName,
                                                          String propertyValue) {
        List<RichContentDynamicNotificationTemplate> notificationTemplates = null;
        LOGGER.info(
            "Delete notification template for notificationId {} , brand {} locale {} property {} propertyValue {} ",
            notificationId, brand,
            locale, propertyName,
            propertyValue);

        if (StringUtils.isNotBlank(propertyName) && StringUtils.isNotBlank(propertyValue)) {
            String lookUpname = NotificationConstants.VEHICLE_DATA.concat(".").concat(propertyName);
            notificationTemplates =
                richContentDynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(notificationId,
                    Collections.singleton(brand.toLowerCase()),
                    Collections.singleton(Locale.forLanguageTag(locale.replace('_', '-'))),
                    lookUpname, Collections.singleton(propertyValue.toLowerCase()));
        } else {
            notificationTemplates =
                richContentDynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(notificationId,
                    Collections.singleton(brand.toLowerCase()),
                    Collections.singleton(Locale.forLanguageTag(locale.replace('_', '-'))));
        }

        if (CollectionUtils.isEmpty(notificationTemplates)) {
            throw new NotificationTemplateDoesNotExistException(
                Collections.singletonList(
                    NotificationCenterError.TEMPLATE_INPUT_NOTIFICATION_NOT_EXIST.toMessage(notificationId, brand,
                        locale, propertyName, propertyValue)));
        }

        if (StringUtils.isNotBlank(propertyValue)) {
            notificationTemplates =
                findTemplatesToDeleteOrUpdate(filterTemplatesWithMultipleAddAttrs(notificationTemplates),
                    propertyValue.toLowerCase());
        }

        if (!CollectionUtils.isEmpty(notificationTemplates)) {
            richContentDynamicNotificationTemplateDao
                .deleteByIds(notificationTemplates.stream().map(RichContentDynamicNotificationTemplate::getId)
                    .toArray(String[]::new));
        }
    }

    /**
     * Update rich content notification template.
     */
    private List<RichContentDynamicNotificationTemplate> filterTemplatesWithMultipleAddAttrs(
        List<RichContentDynamicNotificationTemplate> notificationTemplates) {

        List<RichContentDynamicNotificationTemplate> deleteTemplates = new ArrayList<>();
        notificationTemplates.forEach(t -> {
            if (t.getAdditionalLookupProperties().size() <= 1) {
                deleteTemplates.add(t);
            }
        });
        return deleteTemplates;

    }

    /**
     * Find templates to delete or update.
     *
     * @param richContentNotificationTemplates richContentNotificationTemplates
     * @param propValue propValue
     * @return List of RichContentDynamicNotificationTemplate
     */
    private List<RichContentDynamicNotificationTemplate> findTemplatesToDeleteOrUpdate(
        List<RichContentDynamicNotificationTemplate> richContentNotificationTemplates, String propValue) {
        List<RichContentDynamicNotificationTemplate> deleteRichContentTemplates = new ArrayList<>();
        if (!richContentNotificationTemplates.isEmpty()) {
            deleteRichContentTemplates.addAll(richContentNotificationTemplates);
            richContentNotificationTemplates.forEach(t -> {
                if (!t.getAdditionalLookupProperties().isEmpty()
                    && t.getAdditionalLookupProperties().get(0).getValues().size() > 1) {
                    deleteRichContentTemplates.remove(t);
                    AdditionalLookupProperty prop = new AdditionalLookupProperty();
                    AdditionalLookupProperty propExist = t.getAdditionalLookupProperties().get(0);
                    prop.setName(propExist.getName());
                    prop.setOrder(propExist.getOrder());
                    propExist.getValues().remove(propValue);
                    prop.setValues(propExist.getValues());
                    Updates update = new Updates();
                    update.addFieldSet(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD,
                        Collections.singletonList(prop));
                    richContentDynamicNotificationTemplateDao.update(t.getId(), update);
                }
            });
        }
        return deleteRichContentTemplates;

    }

    /**
     * Filter templates.
     */
    public byte[] filter(NotificationTemplateFilterDto filterDto) throws IOException {
        if (!CollectionUtils.isEmpty(filterDto.getNotificationTemplateIds())) {
            validateNotificationIds(filterDto);
        }

        byte[] result;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream, UTF_8)) {

            result = getZipEntryFileStream(filterDto, byteArrayOutputStream, zipOutputStream);
        } catch (IOException e) {
            throw e;
        }

        return result;
    }

    /**
     * Get rich content notification template.
     */
    @Override
    protected Set<String> getAllTemplateNotificationIds() {
        return richContentDynamicNotificationTemplateDao.findAll().stream()
            .map(RichContentDynamicNotificationTemplate::getNotificationId)
            .collect(Collectors.toSet());
    }

    /**
     * Get Zip Entry.
     *
     * @param notificationId notificationId
     * @return ZipEntry
     */
    @Override
    protected ZipEntry getZipEntry(String notificationId) {
        return new ZipEntry(String.format(ZIP_FILE_NAME_FORMAT, notificationId));
    }

    /**
     * Get Headers Record Length.
     *
     * @return int
     */
    @Override
    protected int getHeadersRecordLength() {
        return RICH_HTML_HEADERS.length;
    }

    /**
     * Get TemplateFile.
     *
     * @return byte[]
     */
    @Override
    protected byte[] getTemplateFile(String notificationId) {
        String encodedFile = richContentImportedDataDao.findById(notificationId).getFile();
        if (StringUtils.isNotBlank(encodedFile)) {
            return Base64.decodeBase64(encodedFile);
        }
        return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    /**
     * getTemplateNotificationIds.
     *
     * @param filterDto filterDto
     * @return Set of String
     */
    @Override
    protected Set<String> getTemplateNotificationIds(NotificationTemplateFilterDto filterDto) {
        List<RichContentDynamicNotificationTemplate> richContentDynamicNotificationTemplateList =
            richContentDynamicNotificationTemplateDao
                .findByNotificationIds(filterDto.getNotificationTemplateIds());
        return richContentDynamicNotificationTemplateList.stream()
            .map(RichContentDynamicNotificationTemplate::getNotificationId)
            .collect(Collectors.toSet());
    }

    /**
     * Get Zip content.
     *
     * @param multipartFile multipartFile
     * @return ZipFile
     * @throws IOException IOException
     */
    ZipFile getZipContent(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".import");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            IOUtils.copy(multipartFile.getInputStream(), fileOutputStream);
            return new ZipFile(file);
        } catch (ZipException e) {
            e.printStackTrace();
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.RICH_HTML_INPUT_INVALID_FILE_TYPE.toMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            Files.delete(file.toPath());
            LOGGER.error("Deleted {}", file.getAbsolutePath());

        }
    }

    /**
     * Get CSV Records.
     *
     * @param zipFile zipFile
     * @return List of CSVRecord
     * @throws IOException IOException
     */
    private List<CSVRecord> getCsvRecords(ZipFile zipFile) throws IOException {
        File tmpFile = null;
        try {

            Enumeration<? extends ZipEntry> zipContent = zipFile.entries();
            while (zipContent.hasMoreElements()) {
                ZipEntry currentEntry = zipContent.nextElement();
                if (currentEntry.getName().endsWith(CSV_FILE_EXTENSION)) {
                    tmpFile = File.createTempFile(UUID.randomUUID().toString(), CSV_FILE_EXTENSION);
                    try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
                        FileReader reader = new FileReader(tmpFile)) {
                        IOUtils.copy(zipFile.getInputStream(currentEntry), fileOutputStream);
                        return CSVParser.parse(tmpFile, UTF_8, RFC4180).getRecords().stream()
                                .filter(rec -> !NotificationUtils.isEmptyRecord(rec)
                                        || rec.getRecordNumber()
                                         < RICH_HTML_START_ROW)
                                .toList();

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw e;
                    }
                }

            }
        } finally {

            if (tmpFile != null) {
                Files.delete(tmpFile.toPath());
                LOGGER.warn("Deleted file: {}", tmpFile.getName());

            }
        }
        return new ArrayList<>();
    }

    /**
     * Validate Zip Content.
     *
     *
     * @param zip zip
     * @param records records
     * @param isPatchUpdate isPatchUpdate
     */
    private void validateZipContent(ZipFile zip, List<CSVRecord> records, boolean isPatchUpdate) {
        List<ResponseWrapper.Message> errors = new ArrayList<>();
        if (records.size() < RICH_HTML_START_ROW) {
            errors.add(NotificationCenterError.INPUT_NUMBER_OF_ROWS.toMessage());
            throw new InvalidInputFileException(errors);
        }

        errors.addAll(validateCsvHeaders(records, isPatchUpdate));
        errors.addAll(validateUniqueTemplates(records));
        errors.addAll(validateResources(zip, records));
        errors.addAll(validateHtmlFiles(zip, records));

        if (!CollectionUtils.isEmpty(errors)) {
            throw new InvalidInputFileException(errors);
        }
    }

    /**
     * Validate Csv Headers.
     *
     * @param records records
     * @param isPatchUpdate isPatchUpdate
     * @return Set of ResponseWrapper.Message
     */
    private Set<ResponseWrapper.Message> validateCsvHeaders(List<CSVRecord> records, boolean isPatchUpdate) {
        Set<ResponseWrapper.Message> errors = new HashSet<>();

        NotificationUtils.validateUtf8File(records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW));

        if (records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW).size() < 1
            || !records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW).get(0).toLowerCase()
                .endsWith(NOTIFICATION_ID_HEADLINE.toLowerCase())) {
            errors.add(NotificationCenterError.RICH_HTML_INPUT_MISSING_HEADERS.toMessage());
        }

        if (StringUtils.isEmpty(records.get(NOTIFICATION_DESCRIPTION_DATA_ROW).get(NOTIFICATION_ID_COLUMN))) {
            errors.add(NotificationCenterError.INPUT_MANDATORY_NOTIFICATION_ID.toMessage());
        }

        int numOfAdditionalLookupProperties =
            findNumOfAdditionalLookupProperties(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW));
        if (isPatchUpdate && numOfAdditionalLookupProperties > 1) {
            errors.add(NotificationCenterError.INPUT_NUMBER_OF_LOOKUP_PROPERTIES.toMessage());
        }
        if (records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).size() < (RICH_HTML_HEADERS.length + 1)
            || !records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(BRAND_COLUMN).equalsIgnoreCase(RICH_HTML_HEADERS[0])
            || !records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties)
                .equalsIgnoreCase(RICH_HTML_HEADERS[1])) {
            errors.add(NotificationCenterError.RICH_HTML_INPUT_MISSING_PROPERTIES_HEADER.toMessage());
        }

        for (int i = RICH_HTML_HEADERS.length + numOfAdditionalLookupProperties;
             i < records.get(NOTIFICATION_PROPERTIES_HEADER_ROW)
                 .size(); i++) {
            if (StringUtils.isEmpty(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(i))) {
                errors.add(NotificationCenterError.INPUT_EMPTY_LOCALE.toMessage());
            }
        }

        errors.addAll(
            validateLanguages(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW), numOfAdditionalLookupProperties));
        return errors;
    }

    /**
     * Validate Languages.
     *
     * @param propertiesHeaderRecord propertiesHeaderRecord
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @return Set of ResponseWrapper.Message
     */
    private Set<ResponseWrapper.Message> validateLanguages(CSVRecord propertiesHeaderRecord,
                                                           int numOfAdditionalLookupProperties) {
        Set<ResponseWrapper.Message> errors = new HashSet<>();
        Map<String, Integer> countedLanguages = countLanguages(propertiesHeaderRecord, numOfAdditionalLookupProperties);

        if (countedLanguages.entrySet().stream().anyMatch(lang -> lang.getValue() > 1)) {
            errors.add(NotificationCenterError.INPUT_DUPLICATE_LOCALE_PREFIX
                .toMessage(countedLanguages.entrySet().stream().filter(lang -> lang.getValue() > 1).toList()
                    .toString()));
        }

        errors.addAll(NotificationUtils.validateLocaleExistence(countedLanguages.keySet()));
        return errors;
    }

    /**
     * Validate Unique Templates.
     *
     * @param records records
     * @return Set of ResponseWrapper.Message
     */
    private Set<ResponseWrapper.Message> validateUniqueTemplates(List<CSVRecord> records) {
        Set<ResponseWrapper.Message> errors = new HashSet<>();
        Set<String> recordUniqueIds = new HashSet<>();
        Set<String> duplicateRecordIds = new HashSet<>();

        CSVRecord headersRecord = records.get(NOTIFICATION_PROPERTIES_HEADER_ROW);
        int numOfAdditionalLookupProperties = findNumOfAdditionalLookupProperties(headersRecord);
        for (CSVRecord rec : records.stream().skip(NOTIFICATION_PROPERTIES_HEADER_ROW + 1L)
            .toList()) {
            if (rec.size() < RICH_HTML_HEADERS.length + numOfAdditionalLookupProperties) {
                errors.add(NotificationCenterError.RICH_HTML_INPUT_MISSING_DATA_FIELD.toMessage());
            } else {
                String brand = rec.get(BRAND_COLUMN).trim().toLowerCase(Locale.ROOT);
                String attribute =
                    rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties).trim().toLowerCase(Locale.ROOT);

                validateNumOfAdditionalLookUpProps(rec, attribute, numOfAdditionalLookupProperties, errors);
                if (brand.isEmpty() || attribute.isEmpty()) {
                    errors.add(NotificationCenterError.RICH_HTML_INPUT_EMPTY_MANDATORY_DATA.toMessage());
                }

                List<AdditionalLookupProperty> additionalLookupProperties =
                    createAdditionalLookupProperties(rec, headersRecord,
                        ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX, numOfAdditionalLookupProperties);
                String additionalLookupPropertiesUniqueKey =
                    getAdditionalLookupPropertiesValuesSignature(additionalLookupProperties);
                String recordUniqueId = brand + additionalLookupPropertiesUniqueKey + attribute;
                if (recordUniqueIds.contains(recordUniqueId)) {
                    duplicateRecordIds.add(recordUniqueId);
                } else {
                    recordUniqueIds.add(recordUniqueId);
                }

            }
        }

        if (!CollectionUtils.isEmpty(duplicateRecordIds)) {
            errors.add(
                NotificationCenterError.RICH_HTML_INPUT_DUPLICATE_ROW_PREFIX.toMessage(duplicateRecordIds.toString()));
        }

        return errors;
    }

    /**
     * Validate Num Of Additional LookUp Props.
     *
     * @param rec rec
     * @param attribute attribute
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @param errors errors
     */
    private void validateNumOfAdditionalLookUpProps(CSVRecord rec, String attribute,
                            int numOfAdditionalLookupProperties, Set<ResponseWrapper.Message> errors) {
        if (!attribute.equalsIgnoreCase(REFERENCE_HTML_ATTRIBUTE_NAME)
            && !(attribute.startsWith(IMAGE_ATTRIBUTE_PREFIX.toLowerCase()))) {
            for (int i = ATTRIBUTE_VALUE_COLUMN + numOfAdditionalLookupProperties; i < rec.size(); i++) {
                errors.addAll(validateDateTransformerTemplate(rec.get(i).trim()));
            }
        }
    }

    /**
     * Validate Resources.
     *
     * @param zip zip
     * @param records records
     * @return Set of ResponseWrapper.Message
     */
    private Set<ResponseWrapper.Message> validateResources(ZipFile zip, List<CSVRecord> records) {
        Set<ResponseWrapper.Message> errors = new HashSet<>();
        Set<String> missingResources = new HashSet<>();
        Map<String, List<String>> missingReferenceHtmlValue = new HashMap<>();
        Map<String, Boolean> brandsToReferenceHtmlExist = new HashMap<>();

        int numOfAdditionalLookupProperties =
            findNumOfAdditionalLookupProperties(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW));
        for (CSVRecord rec : records.stream().skip(NOTIFICATION_PROPERTIES_HEADER_ROW + 1L)
            .toList()) {
            if (rec.size() >= RICH_HTML_HEADERS.length) {
                String brand = rec.get(BRAND_COLUMN);
                boolean isReferenceHtml = rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties)
                    .equals(REFERENCE_HTML_ATTRIBUTE_NAME);

                checkBrandToReferenceHtml(brandsToReferenceHtmlExist, brand, isReferenceHtml);

                if (isReferenceHtml) {
                    validateReferenceHtml(records, missingReferenceHtmlValue, rec, numOfAdditionalLookupProperties);
                }
                boolean isImage =
                    rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties).startsWith(IMAGE_ATTRIBUTE_PREFIX);
                if (isReferenceHtml || isImage) {
                    missingResources.addAll(validateResourceExists(zip, rec, numOfAdditionalLookupProperties));
                }
            }
        }

        List<String> brandsWithoutReferenceHtml =
            brandsToReferenceHtmlExist.entrySet().stream().filter(btr -> !btr.getValue())
                .map(Map.Entry::getKey).toList();

        if (!CollectionUtils.isEmpty(brandsWithoutReferenceHtml)) {
            LOGGER.error("Reference-HTML is not defined for brand(s): {} ", brandsWithoutReferenceHtml);
            errors.add(NotificationCenterError.RICH_HTML_INPUT_MISSING_REFERENCE_HTML.toMessage(
                brandsWithoutReferenceHtml.toString()));
        }
        if (!CollectionUtils.isEmpty(missingReferenceHtmlValue)) {
            LOGGER.error("Reference-HTML is not defined the following brand(s) and locale(s): {}",
                    missingReferenceHtmlValue);
            errors.add(
                NotificationCenterError.RICH_HTML_INPUT_MISSING_REFERENCE_HTML_VALUE.toMessage(
                    missingReferenceHtmlValue.toString()));
        }
        if (!CollectionUtils.isEmpty(missingResources)) {
            LOGGER.error("Reference-HTML is not defined for brand(s): {}", missingResources);
            errors.add(NotificationCenterError.RICH_HTML_INPUT_MISSING_FILE.toMessage(missingResources.toString()));
        }
        return errors;
    }

    /**
     * checkBrandToReferenceHtml.
     *
     * @param brandsToReferenceHtmlExist brandsToReferenceHtmlExist
     * @param brand brand
     * @param isReferenceHtml isReferenceHtml
     */
    private static void checkBrandToReferenceHtml(Map<String, Boolean> brandsToReferenceHtmlExist, String brand,
                                  boolean isReferenceHtml) {

        brandsToReferenceHtmlExist.computeIfAbsent(brand, k -> false);

        if (isReferenceHtml && !brandsToReferenceHtmlExist.get(brand)) {
            brandsToReferenceHtmlExist.put(brand, true);
        }
    }

    /**
     * Validate Html Files.
     *
     * @param zip zip
     * @param records records
     * @return Set ResponseWrapper.Message
     */
    private Set<ResponseWrapper.Message> validateHtmlFiles(ZipFile zip, List<CSVRecord> records) {
        int numOfAdditionalLookupProperties =
            findNumOfAdditionalLookupProperties(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW));
        Map<String, Set<String>> brandToHtmlFilePaths =
            mapBrandToHtmlFilePaths(records, numOfAdditionalLookupProperties);
        Map<String, Map<String, String>> brandToAttributes =
            mapBrandToAttributes(records, numOfAdditionalLookupProperties);

        Set<ResponseWrapper.Message> missingAttributes = new HashSet<>();
        for (Map.Entry<String, Set<String>> brandToFilePath : brandToHtmlFilePaths.entrySet()) {
            Writer writer = new StringWriter();
            for (String htmlFilePath : brandToFilePath.getValue()) {
                String html = convertZipEntryToString(zip, htmlFilePath);
                if (html != null) { // HTML file is missing. This error was
                    // already added to the error aggregator
                    // list
                    while (true) {
                        try {
                            Mustache.compiler().compile(new StringReader(html))
                                .execute(brandToAttributes.get(brandToFilePath.getKey()),
                                    writer);
                            break;
                        } catch (MustacheException.Context e) {
                            missingAttributes.add(
                                NotificationCenterError.RICH_HTML_INPUT_MISSING_ATTRIBUTE.toMessage(e.key, htmlFilePath,
                                    brandToFilePath.getKey()));
                            Map<String, String> currentBrandAttributes =
                                brandToAttributes.get(brandToFilePath.getKey());
                            currentBrandAttributes.put(e.key, "");
                        }
                    }
                    missingAttributes.addAll(validateDateTransformerTemplate(html));
                }
            }
        }

        return missingAttributes;
    }

    /**
     * mapBrandToHtmlFilePaths.
     *
     * @param records records
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @return Map of String and Set of String
     */
    private Map<String, Set<String>> mapBrandToHtmlFilePaths(List<CSVRecord> records,
                                                             int numOfAdditionalLookupProperties) {
        Map<String, Set<String>> brandToHtmlFilePaths = new HashMap<>();
        CSVRecord headersRecord = records.get(NOTIFICATION_PROPERTIES_HEADER_ROW);
        records.stream()
            .filter(rec -> (rec.size() >= RICH_HTML_HEADERS.length + numOfAdditionalLookupProperties
                &&
                rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties).equals(REFERENCE_HTML_ATTRIBUTE_NAME)))
            .forEach(referenceHtmlRecord -> {
                List<AdditionalLookupProperty> additionalLookupProperties =
                    createAdditionalLookupProperties(referenceHtmlRecord,
                        headersRecord, ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX, numOfAdditionalLookupProperties);
                String additionalLookupPropertiesUniqueKey =
                    getAdditionalLookupPropertiesValuesSignature(additionalLookupProperties);
                additionalLookupPropertiesUniqueKey = StringUtils.isEmpty(additionalLookupPropertiesUniqueKey) ? ""
                    : BRAND_AND_ADDITIONAL_LOOKUP_PROPERTIES_SEPARATOR + additionalLookupPropertiesUniqueKey;
                Set<String> htmlFilePaths = new HashSet<>(
                    csvRecordToList(referenceHtmlRecord, RICH_HTML_HEADERS.length + numOfAdditionalLookupProperties));
                brandToHtmlFilePaths.put(referenceHtmlRecord.get(BRAND_COLUMN) + additionalLookupPropertiesUniqueKey,
                    htmlFilePaths);
            });
        return brandToHtmlFilePaths;
    }

    /**
     * mapBrandToAttributes.
     *
     * @param records records
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @return Map of String and Map of String and String
     */
    private Map<String, Map<String, String>> mapBrandToAttributes(List<CSVRecord> records,
                                                                  int numOfAdditionalLookupProperties) {
        Map<String, Map<String, String>> brandToAttributes = new HashMap<>();
        CSVRecord headersRecord = records.get(NOTIFICATION_PROPERTIES_HEADER_ROW);
        records.stream()
            .skip(NOTIFICATION_PROPERTIES_HEADER_ROW + 1L)
            .filter(rec -> (rec.size() >= RICH_HTML_HEADERS.length + numOfAdditionalLookupProperties
                &&
                !rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties).equals(REFERENCE_HTML_ATTRIBUTE_NAME)))
            .forEach(r -> {
                List<AdditionalLookupProperty> additionalLookupProperties =
                    createAdditionalLookupProperties(r, headersRecord,
                        ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX, numOfAdditionalLookupProperties);
                String additionalLookupPropertiesUniqueKey =
                    getAdditionalLookupPropertiesValuesSignature(additionalLookupProperties);
                additionalLookupPropertiesUniqueKey = StringUtils.isEmpty(additionalLookupPropertiesUniqueKey) ? ""
                    : BRAND_AND_ADDITIONAL_LOOKUP_PROPERTIES_SEPARATOR + additionalLookupPropertiesUniqueKey;
                Map<String, String> currentUniqueBrandAttributes = brandToAttributes
                    .get(r.get(BRAND_COLUMN) + additionalLookupPropertiesUniqueKey);
                if (CollectionUtils.isEmpty(currentUniqueBrandAttributes)) {
                    currentUniqueBrandAttributes = new HashMap<>();
                    brandToAttributes.put(r.get(BRAND_COLUMN) + additionalLookupPropertiesUniqueKey,
                        currentUniqueBrandAttributes);
                }
                currentUniqueBrandAttributes.put(r.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties), "");
            });
        return brandToAttributes;
    }

    /**
     * validateReferenceHtml.
     *
     * @param records records
     * @param missingReferenceHtmlValue missingReferenceHtmlValue
     * @param currentRecord currentRecord
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     */
    private void validateReferenceHtml(List<CSVRecord> records, Map<String, List<String>> missingReferenceHtmlValue,
                                       CSVRecord currentRecord, int numOfAdditionalLookupProperties) {
        String brand = currentRecord.get(BRAND_COLUMN);
        for (int i = RICH_HTML_HEADERS.length + numOfAdditionalLookupProperties; i < currentRecord.size(); i++) {
            if (StringUtils.isEmpty(currentRecord.get(i))) {
                String currentLanguage = records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(i);
                if (missingReferenceHtmlValue.get(brand) == null) {
                    missingReferenceHtmlValue.put(brand, Collections.singletonList(currentLanguage));
                } else {
                    List<String> currentBrandLocales = new ArrayList<>(missingReferenceHtmlValue.get(brand));
                    currentBrandLocales.add(currentLanguage);
                    missingReferenceHtmlValue.put(brand, currentBrandLocales);
                }
            }
        }
    }

    /**
     * validateResourceExists.
     *
     * @param zip zip
     * @param rec rec
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @return Collection<? extends String>
     */
    private Collection<? extends String> validateResourceExists(ZipFile zip, CSVRecord rec,
                                                                int numOfAdditionalLookupProperties) {
        Set<String> missingFiles = new HashSet<>();
        for (int i = RICH_HTML_HEADERS.length + numOfAdditionalLookupProperties; i < rec.size(); i++) {
            if (!StringUtils.isEmpty(rec.get(i)) && zip.getEntry(rec.get(i)) == null) {
                missingFiles.add(rec.get(i));
            }
        }

        return missingFiles;
    }

    /**
     * saveZipFile.
     *
     * @param importedFile importedFile
     * @param notificationId notificationId
     * @throws IOException IOException
     */
    private void saveZipFile(MultipartFile importedFile, String notificationId) throws IOException {
        richContentImportedDataDao.deleteById(notificationId);
        RichContentImportedData newEntity = new RichContentImportedData(notificationId,
            Base64.encodeBase64String(IOUtils.toByteArray(importedFile.getInputStream())));
        richContentImportedDataDao.save(newEntity);
    }

    /**
     * Creates html variants for each brand and locale from the csv file.
     */
    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:MethodLength"})
    List<RichContentDynamicNotificationTemplate> buildHtmlVariants(List<CSVRecord> records, ZipFile zipContent,
                                                                   TemplateProcessingParams templateProcessingParams) {

        // Get the notification id, the id will always be present on second row
        // first column
        List<CSVRecord> orgRecords = new ArrayList<>();
        orgRecords.addAll(records);
        CSVRecord headersRecord = records.get(NOTIFICATION_PROPERTIES_HEADER_ROW);
        records = records.stream().skip(RICH_HTML_START_ROW).toList();

        // Make a unique list of all brands
        Set<String> brands = records.stream().map(r -> r.get(BRAND_COLUMN).toLowerCase(Locale.ROOT)).collect(toSet());

        // Make a list of all locales
        int numOfAdditionalLookupProperties = findNumOfAdditionalLookupProperties(headersRecord);
        String[] locales =
            IntStream.range(ATTRIBUTE_VALUE_COLUMN + numOfAdditionalLookupProperties, headersRecord.size())
                .mapToObj(headersRecord::get)
                .map(i -> i.replace("_", "-"))
                .toArray(String[]::new);

        if (numOfAdditionalLookupProperties == 1) {
            templateProcessingParams.setAdditionalAttrPatch(true);
            templateProcessingParams.setPropertyName(
                headersRecord.get(1).substring(ADDITIONAL_ATTRIBUTE_HEADER_PREFIX.length()));
            templateProcessingParams.setAddAttrs(new HashSet<>());
            templateProcessingParams.setBrandsNoAddAttrs(new HashSet<>());
            templateProcessingParams.setBrandsWithAddAttrs(new HashSet<>());
        }
        Map<String, Map<String, String>> all = new HashMap<>();
        Map<String, Set<EmailAttachment>> attachments = new HashMap<>();

        // Extract and collect all attributes files per brand, additional
        // attributes and locale
        for (String brand : brands) {
            List<CSVRecord> brandRecords =
                records.stream().filter(r -> r.get(BRAND_COLUMN).equalsIgnoreCase(brand)).toList();
            Map<String, String> htmlMap = extractHtmlForBrand(brand, brandRecords, headersRecord, locales, zipContent,
                numOfAdditionalLookupProperties);
            for (CSVRecord rec : brandRecords) {
                int count = 0;
                List<AdditionalLookupProperty> additionalLookupProperties =
                    createAdditionalLookupProperties(rec, headersRecord,
                        ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX, numOfAdditionalLookupProperties);
                String additionalLookupPropertiesUniqueKey =
                    getAdditionalLookupPropertiesValuesSignature(additionalLookupProperties);
                for (String locale : locales) {
                    String recordUniqueKey = brand + additionalLookupPropertiesUniqueKey + locale;
                    all.putIfAbsent(recordUniqueKey, new HashMap<>());
                    attachments.putIfAbsent(recordUniqueKey, new HashSet<>());
                    String currentAttributeValue =
                        rec.get(ATTRIBUTE_VALUE_COLUMN + numOfAdditionalLookupProperties + count);
                    if (rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties)
                        .startsWith(IMAGE_ATTRIBUTE_PREFIX)) {
                        if (isImageInHtml(rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties),
                            htmlMap.get(recordUniqueKey))) {
                            String fileName = currentAttributeValue.replace("/", "-");
                            all.get(recordUniqueKey).put(rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties),
                                String.format(CID_FORMAT, fileName));

                            byte[] image = extractImage(zipContent, currentAttributeValue);
                            String fileNameSuffix = fileName
                                .substring(
                                    fileName.lastIndexOf('.') != -1 ? fileName.lastIndexOf('.') : fileName.length());
                            EmailAttachment newAttachment =
                                new EmailAttachment(fileName, Base64.encodeBase64String(image),
                                    IMAGE_EXTENSION_TO_MIME_TYPE.getOrDefault(fileNameSuffix.toLowerCase(), "image/*"),
                                    true);
                            attachments.get(recordUniqueKey).add(newAttachment);
                        }
                    } else {
                        all.get(recordUniqueKey)
                            .put(rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties), currentAttributeValue);
                    }
                    count++;
                }
            }
        }


        // Build html variants
        Map<String, RichContentDynamicNotificationTemplate> result = buildRichHtml(orgRecords, zipContent,
            templateProcessingParams, numOfAdditionalLookupProperties, locales, attachments, all
            );

        return new ArrayList<>(result.values());
    }

    /**
     * Build rich html.
     *
     * @param records records
     * @param zipContent zipContent
     * @param templateProcessingParams templateProcessingParams
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @param locales locales
     * @param attachments attachments
     * @param all all
     * @return Map of String and RichContentDynamicNotificationTemplate
     */
    private Map<String, RichContentDynamicNotificationTemplate> buildRichHtml(List<CSVRecord> records,
                                                                              ZipFile zipContent,
                                                                              TemplateProcessingParams
                                                                                      templateProcessingParams,
                                                                              int numOfAdditionalLookupProperties,
                                                                              String[] locales,
                                                                              Map<String,
                                                                                      Set<EmailAttachment>> attachments,
                                                                              Map<String,
                                                                                      Map<String, String>> all
    ) {
        String notificationId = records.get(NOTIFICATION_DESCRIPTION_DATA_ROW).get(NOTIFICATION_DESCRIPTION_HEADER_ROW);
        CSVRecord headersRecord = records.get(NOTIFICATION_PROPERTIES_HEADER_ROW);
        records = records.stream().skip(RICH_HTML_START_ROW).toList();

        Map<String, RichContentDynamicNotificationTemplate> result = new HashMap<>();

        for (CSVRecord rec : records) {
            String brand = rec.get(0).toLowerCase(Locale.ROOT);
            List<AdditionalLookupProperty> additionalLookupProperties =
                    createAdditionalLookupProperties(rec, headersRecord,
                            ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX, numOfAdditionalLookupProperties);
            String additionalLookupPropertiesUniqueKey =
                    getAdditionalLookupPropertiesValuesSignature(additionalLookupProperties);
            if (templateProcessingParams.isAdditionalAttrPatch()) {
                if (additionalLookupProperties.isEmpty()) {
                    templateProcessingParams.getBrandsNoAddAttrs().add(brand);
                } else {
                    templateProcessingParams.getBrandsWithAddAttrs().add(brand);
                    templateProcessingParams.getAddAttrs().addAll(additionalLookupProperties.get(0).getValues());
                }
            }
            for (String locale : locales) {
                String recordUniqueKey = brand + additionalLookupPropertiesUniqueKey + locale;
                result.computeIfAbsent(recordUniqueKey, k -> {
                    RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
                            new RichContentDynamicNotificationTemplate();
                    richContentDynamicNotificationTemplate.setNotificationId(notificationId);
                    richContentDynamicNotificationTemplate.setId(UUID.randomUUID().toString());
                    richContentDynamicNotificationTemplate.setBrand(brand);
                    richContentDynamicNotificationTemplate.setLocale(
                            Locale.forLanguageTag(locale.replace("_", "-")).toString());
                    richContentDynamicNotificationTemplate.setAttachments(
                            new ArrayList<>(attachments.get(recordUniqueKey)));
                    richContentDynamicNotificationTemplate.setAdditionalLookupProperties(additionalLookupProperties);
                    richContentDynamicNotificationTemplate.setLastUpdateTime(now().toString());
                    Writer writer = getWriter(zipContent, all, recordUniqueKey);
                    richContentDynamicNotificationTemplate.setHtml(writer.toString());
                    addCustomPlaceHolder(richContentDynamicNotificationTemplate,
                            richContentDynamicNotificationTemplate.getHtml());
                    return richContentDynamicNotificationTemplate;
                });
            }

        }
        return result;
    }

    /**
     * Get Writer.
     *
     * @param zipContent zipContent
     * @param all all
     * @param recordUniqueKey recordUniqueKey
     * @return Writer
     */
    private @NotNull Writer getWriter(ZipFile zipContent,
                                      Map<String, Map<String, String>> all,
                                      String recordUniqueKey) {
        Map<String, String> recordKeyToLocaleAttributes = all.get(recordUniqueKey);
        Writer writer = new StringWriter();
        Template m = Mustache.compiler().escapeHTML(false).compile(new StringReader(Objects.requireNonNull(
                convertZipEntryToString(zipContent,
                        recordKeyToLocaleAttributes.get(REFERENCE_HTML_ATTRIBUTE_NAME)))));
        recordKeyToLocaleAttributes.remove(REFERENCE_HTML_ATTRIBUTE_NAME);
        m.execute(recordKeyToLocaleAttributes, writer);
        return writer;
    }

    /**
     * isImageInHtml.
     *
     * @param currentAttributeValue currentAttributeValue
     * @param html html
     * @return boolean
     */
    private boolean isImageInHtml(String currentAttributeValue, String html) {
        if (html != null) {
            return html.contains("{{" + currentAttributeValue + "}}");
        }
        return false;
    }

    /**
     * Extract html for brand.
     *
     * @param brand brand
     * @param brandRecords brandRecords
     * @param headersRecord headersRecord
     * @param locales locales
     * @param zipContent zipContent
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @return Map of String and String
     */
    private Map<String, String> extractHtmlForBrand(String brand, List<CSVRecord> brandRecords, CSVRecord headersRecord,
                                                    String[] locales,
                                                    ZipFile zipContent, int numOfAdditionalLookupProperties) {
        Map<String, String> htmlMap = new HashMap<>();
        List<CSVRecord> csvRecords = brandRecords.stream()
            .filter(rec -> rec.get(ATTRIBUTE_COLUMN + numOfAdditionalLookupProperties)
                .equals(REFERENCE_HTML_ATTRIBUTE_NAME))
            .toList();
        for (CSVRecord csvRecord : csvRecords) {
            int count = 0;
            List<AdditionalLookupProperty> additionalLookupProperties =
                createAdditionalLookupProperties(csvRecord, headersRecord,
                    ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX, numOfAdditionalLookupProperties);
            String additionalLookupPropertiesUniqueKey =
                getAdditionalLookupPropertiesValuesSignature(additionalLookupProperties);
            for (String locale : locales) {
                String recordKey = brand + additionalLookupPropertiesUniqueKey + locale;
                String currentAttributeValue =
                    csvRecord.get(ATTRIBUTE_VALUE_COLUMN + numOfAdditionalLookupProperties + count);
                if (StringUtils.isNotEmpty(currentAttributeValue)) {
                    String htmlText = convertZipEntryToString(zipContent, currentAttributeValue);
                    htmlMap.put(recordKey, htmlText);
                }
                count++;
            }
        }
        return htmlMap;
    }

    /**
     * Save rich html notification template.
     *
     * @param richContentDynamicNotificationTemplates richContentDynamicNotificationTemplates
     */
    private void saveRichHtmlNotificationTemplate(
        List<RichContentDynamicNotificationTemplate> richContentDynamicNotificationTemplates) {
        String notificationId = richContentDynamicNotificationTemplates.get(0).getNotificationId();
        List<RichContentDynamicNotificationTemplate> entities = richContentDynamicNotificationTemplateDao
            .findByNotificationIds(Collections.singletonList(notificationId));
        richContentDynamicNotificationTemplateDao
            .deleteByIds(entities.stream().map(RichContentDynamicNotificationTemplate::getId).toArray(String[]::new));
        richContentDynamicNotificationTemplateDao
            .saveAll(richContentDynamicNotificationTemplates.toArray(new RichContentDynamicNotificationTemplate[] {}));
    }

    /**
     * Save rich template by id locale brand.
     *
     * @param richContentDynamicNotificationTemplates richContentDynamicNotificationTemplates
     * @param templateProcessingParams templateProcessingParams
     */
    private void saveRichTemplateByIdLocaleBrand(
        List<RichContentDynamicNotificationTemplate> richContentDynamicNotificationTemplates,
        TemplateProcessingParams templateProcessingParams) {
        String notificationId = richContentDynamicNotificationTemplates.get(0).getNotificationId();
        Set<String> brands =
            richContentDynamicNotificationTemplates.stream().map(RichContentDynamicNotificationTemplate::getBrand)
                .map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> localeSet =
            richContentDynamicNotificationTemplates.stream().map(RichContentDynamicNotificationTemplate::getLocale)
                .collect(Collectors.toSet());
        Set<Locale> locales = new HashSet<>();
        localeSet.forEach(locale -> {
            locales.add(Locale.forLanguageTag(locale.replace('_', '-')));
        });
        List<RichContentDynamicNotificationTemplate> entities;
        LOGGER.info("Importing templates for notificationId {} , brands {},locales {}",
            notificationId, brands, locales);
        if (templateProcessingParams.isAdditionalAttrPatch()) {
            LOGGER.debug(
                "Patch update of templates for notificationId {} , brands {},locales {}, templateProcessingParams {}",
                notificationId, brands, locales, templateProcessingParams);
            entities = richContentDynamicNotificationTemplateDao
                .findByNotificationIdsBrandsLocalesNoAddAttrs(notificationId,
                    templateProcessingParams.getBrandsNoAddAttrs(), locales);
            entities.addAll(filterTemplatesWithMultipleAddAttrs(richContentDynamicNotificationTemplateDao
                .findByNotificationIdsBrandsLocalesAddAttrs(notificationId,
                    templateProcessingParams.getBrandsWithAddAttrs(),
                    locales, templateProcessingParams.getPropertyName(), templateProcessingParams.getAddAttrs())));
        } else {
            entities = richContentDynamicNotificationTemplateDao
                .findByNotificationIdsBrandsLocalesNoAddAttrs(notificationId, brands, locales);
        }

        richContentDynamicNotificationTemplateDao
            .deleteByIds(entities.stream().map(RichContentDynamicNotificationTemplate::getId).toArray(String[]::new));
        richContentDynamicNotificationTemplateDao
            .saveAll(richContentDynamicNotificationTemplates.toArray(new RichContentDynamicNotificationTemplate[] {}));
    }

    /**
     * Convert zip entry to string.
     *
     * @param zipFile zipFile
     * @param relativePath relativePath
     * @return String
     * @throws NullPointerException NullPointerException
     */
    String convertZipEntryToString(ZipFile zipFile, String relativePath) throws NullPointerException {


        ZipEntry zipEntry = zipFile.getEntry(relativePath);
        if (zipEntry == null) { // HTML file is missing. This error was
            // already added to the error aggregator
            // list
            return null;
        }
        try (InputStream is = zipFile.getInputStream(zipEntry);
            BufferedReader  br = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    byte[] extractImage(ZipFile zipFile, String relativePath) {
        try {
            ZipEntry zipEntry = zipFile.getEntry(relativePath);
            InputStream is = zipFile.getInputStream(zipEntry);
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            throw new InvalidImageException(
                Collections.singletonList(
                    NotificationCenterError.RICH_HTML_INPUT_INVALID_IMAGE.toMessage(relativePath)));
        }
    }

    /**
     * Convert CSVRecord to list of values.
     *
     * @param record The CSV record
     *
     * @param skip   Number of first columns to skip
     *
     * @return List of the record values
     */
    private Collection<? extends String> csvRecordToList(CSVRecord rec, int skip) {
        List<String> values = new ArrayList<>();
        if (rec == null || rec.size() <= skip) {
            return values;
        }

        Iterator<String> it = rec.iterator();
        for (int i = 0; i < skip; i++) {
            it.next();
        }

        while (it.hasNext()) {
            String currentValue = it.next().trim();
            if (!StringUtils.isEmpty(currentValue)) {
                values.add(currentValue);
            }
        }
        return values;
    }

    /**
     * Get rich html export headers.
     */
    public static String[] getRichHtmlExportHeaders() {
        String[] richHtmlExportHeaders = new String[RICH_HTML_HEADERS.length];
        System.arraycopy(RICH_HTML_HEADERS, 0, richHtmlExportHeaders, 0, RICH_HTML_HEADERS.length);
        return richHtmlExportHeaders;
    }
}
