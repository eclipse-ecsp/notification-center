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

import jakarta.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;
import org.eclipse.ecsp.notification.dao.NotificationTemplateImportedDataDAO;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateImportedData;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.dto.TemplateProcessingParams;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationTemplateDoesNotExistException;
import org.eclipse.ecsp.platform.notification.utils.NotificationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.addCustomPlaceHolder;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.createAdditionalLookupProperties;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.findNumOfAdditionalLookupProperties;

/**
 * Notification template service class.
 */
@Service
public class NotificationTemplateService extends BaseNotificationTemplateService {
    /**
     * Regex for sender id.
     */
    public static final String REGEX = "^[a-zA-Z0-9]{0,11}$|^/g";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationTemplateService.class);

    private static final String[] TEMPLATE_HEADERS = {"Brand", "Channel", "Attribute"};
    /**
     * Sender attribute.
     */
    public static final String SENDER = "sender";

    static final String CSV_FILE_NAME_FORMAT = "ignite_notification_template_%s.csv";

    @Value("${sender.id.default:NOTICE}")
    String defaultSenderId;

    private final DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    private final NotificationTemplateImportedDataDAO notificationTemplateImportedDataDao;

    /**
     * BODY.
     */
    public static final String BODY = "body";

    /**
     * COLUMN_TWO.
     */
    public static final int COLUMN_TWO = 2;

    /**
     * ROW_THREE.
     */
    public static final int ROW_THREE = 3;

    /**
     * Initialize the default sender id.
     */
    @PostConstruct
    public void init() {
        defaultSenderId = resolveSenderId(defaultSenderId);
    }

    /**
     * Parameterized NotificationTemplateService c'tor.
     *
     * @param dynamicNotificationTemplateDao dynamicNotificationTemplateDao
     * @param notificationTemplateImportedDataDao notificationTemplateImportedDataDao
     */
    public NotificationTemplateService(DynamicNotificationTemplateDAO dynamicNotificationTemplateDao,
                                       NotificationTemplateImportedDataDAO notificationTemplateImportedDataDao) {
        this.dynamicNotificationTemplateDao = dynamicNotificationTemplateDao;
        this.notificationTemplateImportedDataDao = notificationTemplateImportedDataDao;
    }

    /**
     * filter notification templates.
     */
    public byte[] filter(NotificationTemplateFilterDto filterDto) {
        try {
            if (!CollectionUtils.isEmpty(filterDto.getNotificationTemplateIds())) {
                validateNotificationIds(filterDto);
            }

            byte[] result = null;
            result = getBytes(filterDto, result);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * getBytes.
     *
     * @param filterDto filterDto
     * @param result result
     * @return byte[]
     */
    private byte[] getBytes(NotificationTemplateFilterDto filterDto, byte[] result) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream,
                 java.nio.charset.StandardCharsets.UTF_8)) {
            result = getZipEntryFileStream(filterDto, byteArrayOutputStream, zipOutputStream);
        } catch (Exception e) {
            LOGGER.error("error in filter template", e);
        }
        return result;
    }

    /**
     * getTemplate ExportHeaders.
     *
     * @return String set
     */
    @Override
    protected Set<String> getAllTemplateNotificationIds() {
        return notificationTemplateImportedDataDao.findAll().stream()
            .map(NotificationTemplateImportedData::getNotificationId)
            .collect(Collectors.toSet());
    }

    /**
     * getTemplate ExportHeaders.
     *
     * @param notificationId notificationId
     * @return byte[]
     */
    @Override
    protected byte[] getTemplateFile(String notificationId) {
        String encodedFile = notificationTemplateImportedDataDao.findById(notificationId).getFile();
        if (StringUtils.isNotBlank(encodedFile)) {
            return Base64.decodeBase64(encodedFile);
        }
        return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    /**
     * getZipEntry.
     *
     * @param notificationId notificationId
     * @return ZipEntry
     */
    @Override
    protected ZipEntry getZipEntry(String notificationId) {
        return new ZipEntry(String.format(CSV_FILE_NAME_FORMAT, notificationId));
    }

    /**
     * getHeadersRecordLength.
     *
     * @return int
     */
    @Override
    protected int getHeadersRecordLength() {
        return TEMPLATE_HEADERS.length;
    }

    /**
     * getTemplate ExportHeaders.
     *
     * @param filterDto filterDto
     * @return Set of String
     */
    @Override
    protected Set<String> getTemplateNotificationIds(NotificationTemplateFilterDto filterDto) {
        List<NotificationTemplateImportedData> dynamicNotificationTemplateList = notificationTemplateImportedDataDao
            .findByNotificationIds(filterDto.getNotificationTemplateIds());
        return dynamicNotificationTemplateList.stream().map(NotificationTemplateImportedData::getNotificationId)
            .collect(Collectors.toSet());
    }

    /**
     * Import notification template either incrementally or at one shot.
     *
     * @param file csv
     *
     * @param isPatchUpdate true for incremental templates
     *
     * @throws Exception if failed to import
     */
    public void importNotificationTemplate(MultipartFile file, boolean isPatchUpdate) throws Exception {
        try {
            List<CSVRecord> records = NotificationUtils.getCsvRecords(file).stream()
                .filter(rec -> !NotificationUtils.isEmptyRecord(rec)).toList();
            validateRecords(records, isPatchUpdate);
            TemplateProcessingParams templateProcessingParams = new TemplateProcessingParams();
            CSVRecord notificationDescriptionRecord = records.get(NOTIFICATION_DESCRIPTION_DATA_ROW);
            String notificationId = notificationDescriptionRecord.get(0).trim();
            List<DynamicNotificationTemplate> newTemplates =
                buildNotificationTemplates(records, notificationDescriptionRecord, templateProcessingParams);
            if (isPatchUpdate) {
                updateNotificationTemplateByIdBrandLocle(notificationId, newTemplates, templateProcessingParams);
            } else {
                updateNotificationTemplate(notificationId, newTemplates);
            }

            saveCsvFile(file, notificationId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Build notification templates.
     *
     * @param records csv records
     * @param notificationDescriptionRecord notification description record
     * @param templateProcessingParams template processing params
     * @return list of dynamic notification templates
     * @throws Exception if failed to build notification templates
     */
    private List<DynamicNotificationTemplate> buildNotificationTemplates(List<CSVRecord> records,
         CSVRecord notificationDescriptionRecord,
         TemplateProcessingParams templateProcessingParams)
        throws Exception {
        int numOfAdditionalLookupProperties =
            findNumOfAdditionalLookupProperties(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW));

        if (numOfAdditionalLookupProperties == 1) {
            templateProcessingParams.setAdditionalAttrPatch(true);
            templateProcessingParams.setPropertyName(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(1)
                .substring(ADDITIONAL_ATTRIBUTE_HEADER_PREFIX.length()));
            templateProcessingParams.setAddAttrs(new HashSet<>());
            templateProcessingParams.setBrandsNoAddAttrs(new HashSet<>());
            templateProcessingParams.setBrandsWithAddAttrs(new HashSet<>());
        }

        List<String> languages = new ArrayList<>();
        for (int i = TEMPLATE_HEADERS.length + numOfAdditionalLookupProperties;
             i < records.get(NOTIFICATION_PROPERTIES_HEADER_ROW)
                 .size(); i++) {
            languages.add(
                Locale.forLanguageTag(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(i).replace('_', '-'))
                    .toString());
        }

        List<DynamicNotificationTemplate> newTemplates = new ArrayList<>();
        for (CSVRecord rec : records.stream().skip(NOTIFICATION_PROPERTIES_HEADER_ROW + 1L)
            .toList()) {
            handleRecord(rec, notificationDescriptionRecord, records.get(NOTIFICATION_PROPERTIES_HEADER_ROW),
                languages, newTemplates,
                numOfAdditionalLookupProperties, templateProcessingParams);
        }
        return newTemplates;
    }

    /**
     * Handle record.
     *
     * @param rec csv record
     * @param notificationDescriptionRecord notification description record
     * @param headersRecord headers record
     * @param languages languages
     * @param newTemplates new templates
     * @param numOfAdditionalLookupProperties number of additional lookup properties
     * @param templateProcessingParams template processing params
     * @throws Exception if failed to handle record
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private void handleRecord(CSVRecord rec, CSVRecord notificationDescriptionRecord, CSVRecord headersRecord,
                              List<String> languages,
                              List<DynamicNotificationTemplate> newTemplates, int numOfAdditionalLookupProperties,
                              TemplateProcessingParams templateProcessingParams) throws Exception {
        String brand = rec.get(0).trim().toLowerCase();
        String channel = rec.get(1 + numOfAdditionalLookupProperties).trim().toLowerCase();
        String attribute = rec.get(2 + numOfAdditionalLookupProperties).trim().toLowerCase();
        List<AdditionalLookupProperty> additionalLookupProperties =
            createAdditionalLookupProperties(rec, headersRecord,
                ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX, numOfAdditionalLookupProperties);
        String additionalLookupPropertiesSignature =
            getAdditionalLookupPropertiesValuesSignature(additionalLookupProperties);

        if (templateProcessingParams.isAdditionalAttrPatch()) {
            if (additionalLookupProperties.isEmpty()) {
                templateProcessingParams.getBrandsNoAddAttrs().add(brand);
            } else {
                templateProcessingParams.getBrandsWithAddAttrs().add(brand);
                templateProcessingParams.getAddAttrs().addAll(additionalLookupProperties.get(0).getValues());
            }
        }
        for (int i = TEMPLATE_HEADERS.length + numOfAdditionalLookupProperties; i < rec.size(); i++) {
            String currentLanguage = languages.get(i - TEMPLATE_HEADERS.length - numOfAdditionalLookupProperties);
            Optional<DynamicNotificationTemplate> existingNotification = newTemplates.stream()
                .filter(n -> n.getBrand().equalsIgnoreCase(brand)
                    && n.getLocale().equalsIgnoreCase(currentLanguage)
                    && getAdditionalLookupPropertiesValuesSignature(n.getAdditionalLookupProperties())
                        .equals(additionalLookupPropertiesSignature))
                .findFirst();
            if (existingNotification.isPresent()) {
                updateCurrentTemplate(existingNotification.get(), rec.get(i), channel, attribute);
            } else {
                DynamicNotificationTemplate template =
                    createNewTemplate(notificationDescriptionRecord, brand, currentLanguage, channel,
                        attribute, rec.get(i), additionalLookupProperties);
                newTemplates.add(template);
            }
        }
    }

    /**
     * Create new template.
     *
     * @param notificationDescriptionRecord notification description record
     * @param brand brand
     * @param language language
     * @param channel channel
     * @param attribute attribute
     * @param attributeValue attribute value
     * @param additionalLookupProperties additional lookup properties
     * @return dynamic notification template
     * @throws Exception if failed to create new template
     */
    private DynamicNotificationTemplate createNewTemplate(CSVRecord notificationDescriptionRecord, String brand,
                                                          String language,
                                                          String channel, String attribute, String attributeValue,
                                                          List<AdditionalLookupProperty> additionalLookupProperties)
        throws Exception {
        String notificationId = notificationDescriptionRecord.get(0).trim();

        DynamicNotificationTemplate template = new DynamicNotificationTemplate();
        template.setNotificationId(notificationId);
        template.setNotificationShortName(notificationDescriptionRecord.get(1).trim());
        template.setNotificationLongName(notificationDescriptionRecord.get(COLUMN_TWO).trim());
        template.setLocale(language);
        template.setBrand(brand);
        template.setAdditionalLookupProperties(additionalLookupProperties);
        template.setId(UUID.randomUUID().toString());
        if (SENDER.equals(attribute) && channel.equals(ChannelType.SMS.getChannelType())) {
            attributeValue = resolveSenderId(attributeValue);
        }
        template.addAttributeToChannel(channel, attribute, attributeValue);
        addCustomPlaceHolder(template, attributeValue);
        return template;
    }

    /**
     * Update current template.
     *
     * @param template template
     * @param recordField record field
     * @param channel channel
     * @param attribute attribute
     * @throws Exception if failed to update current template
     */
    private void updateCurrentTemplate(DynamicNotificationTemplate template, String recordField, String channel,
                                       String attribute)
        throws Exception {
        if (SENDER.equals(attribute) && channel.equals(ChannelType.SMS.getChannelType())) {
            recordField = resolveSenderId(recordField);
        }
        template.addAttributeToChannel(channel, attribute, recordField);
        addCustomPlaceHolder(template, recordField);
    }

    private String resolveSenderId(String attributeValue) {
        if (attributeValue.isEmpty()) {
            attributeValue = defaultSenderId;
        }
        final Matcher matcher = Pattern.compile(REGEX).matcher(attributeValue);
        if (!matcher.find()) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.TEMPLATE_INVALID_SENDER_ID.toMessage()));
        }
        return matcher.group(0);
    }

    /**
     * Delete notification template.
     *
     * @param notificationId notificationId
     */
    public void deleteNotificationTemplate(String notificationId) {
        List<DynamicNotificationTemplate> notificationTemplates = dynamicNotificationTemplateDao
            .findByNotificationIds(Collections.singletonList(notificationId));
        if (CollectionUtils.isEmpty(notificationTemplates)) {
            throw new NotificationTemplateDoesNotExistException(
                Collections.singletonList(
                    NotificationCenterError.TEMPLATE_INPUT_NOTIFICATION_NOT_EXIST.toMessage(notificationId)));
        }

        dynamicNotificationTemplateDao
            .deleteByIds(notificationTemplates.stream().map(DynamicNotificationTemplate::getId).toArray(String[]::new));

        try {
            notificationTemplateImportedDataDao.deleteById(notificationId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete .csv file {} from DB.", StringUtils.normalizeSpace(notificationId), e);
        }
    }

    /**
     * Delete templates.
     *
     * @param notificationIds notificationIds
     *
     * @return notificationIds for which template not present.
     */
    public Set<String> deleteNotificationTemplates(NotificationTemplateFilterDto notificationIds) {
        List<DynamicNotificationTemplate> notificationTemplates = dynamicNotificationTemplateDao
            .findByNotificationIds(notificationIds.getNotificationTemplateIds());
        dynamicNotificationTemplateDao
            .deleteByIds(notificationTemplates.stream().map(DynamicNotificationTemplate::getId).distinct()
                .toArray(String[]::new));

        try {
            notificationTemplateImportedDataDao.deleteByIds(
                notificationIds.getNotificationTemplateIds().toArray(new String[0]));
        } catch (Exception e) {
            LOGGER.error("Failed to delete .csv file " + notificationIds.getNotificationTemplateIds() + " from DB", e);
        }

        Set<String> existingNotificationIds =
            notificationTemplates.stream().map(DynamicNotificationTemplate::getNotificationId)
                .collect(toSet());
        return notificationIds.getNotificationTemplateIds().stream().filter(n -> !existingNotificationIds.contains(n))
            .collect(toSet());
    }

    /**
     * Update notification template.
     *
     * @param notificationId notificationId
     *
     * @param notificationsDao notificationsDao
     *
     */
    private void updateNotificationTemplate(String notificationId, List<DynamicNotificationTemplate> notificationsDao) {
        List<DynamicNotificationTemplate> notificationIds = dynamicNotificationTemplateDao
            .findByNotificationIds(Collections.singletonList(notificationId));
        dynamicNotificationTemplateDao.deleteByIds(
            notificationIds.stream().map(DynamicNotificationTemplate::getId).toArray(String[]::new));
        dynamicNotificationTemplateDao.saveAll(notificationsDao.toArray(new DynamicNotificationTemplate[0]));
    }

    /**
     * Update notification template by id, brand and locale.
     *
     * @param notificationId notificationId
     * @param newTemplates newTemplates
     * @param templateProcessingParams templateProcessingParams
     */
    private void updateNotificationTemplateByIdBrandLocle(String notificationId,
                                                          List<DynamicNotificationTemplate> newTemplates,
                                                          TemplateProcessingParams templateProcessingParams) {
        Set<String> brands = newTemplates.stream().map(DynamicNotificationTemplate::getBrand).map(String::toLowerCase)
            .collect(Collectors.toSet());
        Set<String> localeSet =
            newTemplates.stream().map(DynamicNotificationTemplate::getLocale).collect(Collectors.toSet());
        Set<Locale> locales = new HashSet<>();
        localeSet.forEach(locale -> {
            locales.add(Locale.forLanguageTag(locale.replace('_', '-')));
        });
        LOGGER.debug("Importing templates for notificationId {} , brands {},locales {}",
            notificationId, brands, locales);
        List<DynamicNotificationTemplate> notificationIds;
        if (templateProcessingParams.isAdditionalAttrPatch()) {
            LOGGER.debug(
                "Patch update of templates for notificationId {} , brands {},locales {},additional attributes {} {}",
                notificationId,
                brands, locales, templateProcessingParams.getPropertyName(), templateProcessingParams.getAddAttrs());
            LOGGER.debug("TemplateProcessing params {}", templateProcessingParams);
            notificationIds = dynamicNotificationTemplateDao
                .findByNotificationIdsBrandsLocalesNoAddAttrs(notificationId,
                    templateProcessingParams.getBrandsNoAddAttrs(), locales);
            notificationIds.addAll(filterTemplatesWithMultipleAddAttrs(
                dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(notificationId,
                    templateProcessingParams.getBrandsWithAddAttrs(), locales,
                    templateProcessingParams.getPropertyName(), templateProcessingParams.getAddAttrs())));

        } else {
            notificationIds = dynamicNotificationTemplateDao
                .findByNotificationIdsBrandsLocalesNoAddAttrs(notificationId, brands, locales);

        }

        dynamicNotificationTemplateDao.deleteByIds(
            notificationIds.stream().map(DynamicNotificationTemplate::getId).toArray(String[]::new));
        dynamicNotificationTemplateDao.saveAll(
            newTemplates.toArray(new DynamicNotificationTemplate[newTemplates.size()]));
    }

    /**
     * saveCsvFile.
     *
     * @param importedFile importedFile
     * @param notificationId notificationId
     */
    private void saveCsvFile(MultipartFile importedFile, String notificationId) throws IOException {
        notificationTemplateImportedDataDao.deleteById(notificationId);
        NotificationTemplateImportedData newEntity = new NotificationTemplateImportedData(notificationId,
            Base64.encodeBase64String(IOUtils.toByteArray(importedFile.getInputStream())));
        notificationTemplateImportedDataDao.save(newEntity);
    }

    /**
     * Get all notificaiton templates.
     *
     * @return templates basic data
     */
    public List<Map<String, String>> getNotificationTemplates() {
        List<DynamicNotificationTemplate> notificationTemplateList = dynamicNotificationTemplateDao.findAll();
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map;
        Set<String> notificationIds = new HashSet<>();
        for (DynamicNotificationTemplate dynamicNotificationTemplate : notificationTemplateList) {
            if (!notificationIds.contains(dynamicNotificationTemplate.getNotificationId())) {
                map = new HashMap<>();
                map.put("notificationId", dynamicNotificationTemplate.getNotificationId());
                map.put("notificationShortName", dynamicNotificationTemplate.getNotificationShortName());
                map.put("notificationLongName", dynamicNotificationTemplate.getNotificationLongName());
                data.add(map);
                notificationIds.add(dynamicNotificationTemplate.getNotificationId());
            }
        }
        return data;
    }

    /**
     * Validate records.
     *
     * @param records csv records
     * @param isPatchUpdate true for incremental templates
     */
    private void validateRecords(List<CSVRecord> records, boolean isPatchUpdate) {

        if (records.size() < ROW_THREE) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.INPUT_NUMBER_OF_ROWS.toMessage()));
        }

        validateNotificationHeaders(records);

        validatePropertiesHeaders(records, isPatchUpdate);

        NotificationUtils.validateLanguages(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW), getHeadersRecordLength());

        validateTemplates(records);
    }

    /**
     * Validate notification headers.
     *
     * @param records csv records
     */
    private void validateNotificationHeaders(List<CSVRecord> records) {
        NotificationUtils.validateUtf8File(records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW));

        if (records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW).size() < ROW_THREE
            || !records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW).get(0).toLowerCase()
                .endsWith(NOTIFICATION_ID_HEADLINE.toLowerCase())
            || StringUtils.isEmpty(records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW).get(1))
            || StringUtils.isEmpty(records.get(NOTIFICATION_DESCRIPTION_HEADER_ROW).get(COLUMN_TWO))) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.TEMPLATE_INPUT_MISSING_HEADERS.toMessage()));
        }

        if (records.get(NOTIFICATION_DESCRIPTION_DATA_ROW).size() < ROW_THREE) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.TEMPLATE_INPUT_MISSING_HEADER_DATA.toMessage()));
        }

        if (StringUtils.isEmpty(records.get(NOTIFICATION_DESCRIPTION_DATA_ROW).get(0))) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.INPUT_MANDATORY_NOTIFICATION_ID.toMessage()));
        }
    }

    /**
     * Validate properties headers.
     *
     * @param records csv records
     * @param isPatchUpdate true for incremental templates
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private void validatePropertiesHeaders(List<CSVRecord> records, boolean isPatchUpdate) {
        int numOfAdditionalLookupProperties =
            findNumOfAdditionalLookupProperties(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW));
        if (isPatchUpdate && numOfAdditionalLookupProperties > 1) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.INPUT_NUMBER_OF_LOOKUP_PROPERTIES.toMessage()));
        }

        if (records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).size() < (4 + numOfAdditionalLookupProperties)
            || !records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(0).equalsIgnoreCase(TEMPLATE_HEADERS[0])
            || !records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(1 + numOfAdditionalLookupProperties)
                .equalsIgnoreCase(TEMPLATE_HEADERS[1])
            ||
            !records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(COLUMN_TWO + numOfAdditionalLookupProperties)
                .equalsIgnoreCase(TEMPLATE_HEADERS[COLUMN_TWO])) {
            throw new InvalidInputFileException(
                Collections.singletonList(
                    NotificationCenterError.TEMPLATE_INPUT_MISSING_PROPERTIES_HEADER.toMessage()));
        }

        for (int i = TEMPLATE_HEADERS.length + numOfAdditionalLookupProperties;
             i < records.get(NOTIFICATION_PROPERTIES_HEADER_ROW)
                 .size(); i++) {
            if (StringUtils.isEmpty(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW).get(i))) {
                throw new InvalidInputFileException(
                    Collections.singletonList(NotificationCenterError.INPUT_EMPTY_LOCALE.toMessage()));
            }
        }
    }

    /**
     * Validate templates.
     *
     * @param records csv records
     */
    private void validateTemplates(List<CSVRecord> records) {
        Map<String, List<CSVRecord>> recordsUniqueness = new HashMap<>();
        Set<String> duplicateRecords = new HashSet<>();
        List<ResponseWrapper.Message> errors = new ArrayList<>();
        int numOfAdditionalLookupProperties =
            findNumOfAdditionalLookupProperties(records.get(NOTIFICATION_PROPERTIES_HEADER_ROW));

        for (CSVRecord rec : records.stream().skip(NOTIFICATION_PROPERTIES_HEADER_ROW + 1L)
            .toList()) {
            if (rec.size() < TEMPLATE_HEADERS.length + numOfAdditionalLookupProperties) {
                throw new InvalidInputFileException(
                    Collections.singletonList(NotificationCenterError.TEMPLATE_INPUT_MISSING_DATA_FIELD.toMessage()));
            }

            String brand = rec.get(0).trim().toLowerCase();
            String channel = rec.get(1 + numOfAdditionalLookupProperties).trim().toLowerCase();
            String attribute = rec.get(COLUMN_TWO + numOfAdditionalLookupProperties).trim().toLowerCase();

            if (channel.equals(ChannelType.EMAIL.getChannelType()) && BODY.equals(attribute)) {
                validateAdditionalLookUpProperties(rec, numOfAdditionalLookupProperties, errors);
            }
            if (brand.isEmpty() || channel.isEmpty() || attribute.isEmpty()) {
                throw new InvalidInputFileException(
                    Collections.singletonList(NotificationCenterError.TEMPLATE_INPUT_EMPTY_MANDATORY_DATA.toMessage()));
            }

            String recordUniqueId = brand + "-" + channel + "-" + attribute;
            if (!NotificationUtils.isRecordUnique(rec, recordsUniqueness, recordUniqueId,
                numOfAdditionalLookupProperties, 1)) {
                duplicateRecords.add(recordUniqueId);
            }

        }

        if (!CollectionUtils.isEmpty(duplicateRecords)) {
            throw new InvalidInputFileException(Collections
                .singletonList(NotificationCenterError.TEMPLATE_INPUT_DUPLICATE_ROW_PREFIX.toMessage(
                    duplicateRecords.toString())));
        }
    }

    /**
     * Validate additional look up properties.
     *
     * @param rec csv record
     * @param numOfAdditionalLookupProperties number of additional lookup properties
     * @param errors errors
     */
    private void validateAdditionalLookUpProperties(CSVRecord rec, int numOfAdditionalLookupProperties,
                           List<ResponseWrapper.Message> errors) {
        for (int i = ROW_THREE + numOfAdditionalLookupProperties; i < rec.size(); i++) {
            String attributeValue = rec.get(i).trim();
            errors.addAll(validateDateTransformerTemplate(attributeValue));
            if (!CollectionUtils.isEmpty(errors)) {
                LOGGER.error("Validation of Date transformer template failed.");
                throw new InvalidInputFileException(errors);
            }
        }
    }

    /**
     * getTemplate ExportHeaders.
     */
    public static String[] getTemplateExportHeaders() {
        String[] templateExportHeaders = new String[TEMPLATE_HEADERS.length];
        System.arraycopy(TEMPLATE_HEADERS, 0, templateExportHeaders, 0, TEMPLATE_HEADERS.length);
        return templateExportHeaders;
    }

    /**
     * Delete templates by filter criteria.
     */
    public void deleteNotificationTemplateByIdBrandLocale(String notificationId, String brand, String locale,
                                                          String propertyName, String propertyValue) {
        List<DynamicNotificationTemplate> notificationTemplates = null;
        LOGGER.info(
            "Delete notification template for notificationId {} , brand {} locale {} property {} propertyValue {} ",
            notificationId, brand,
            locale, propertyName,
            propertyValue);
        if (StringUtils.isNotBlank(propertyName) && StringUtils.isNotBlank(propertyValue)) {
            String lookUpname = NotificationConstants.VEHICLE_DATA.concat(".").concat(propertyName);
            notificationTemplates =
                dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesAddAttrs(notificationId,
                    Collections.singleton(brand.toLowerCase()),
                    Collections.singleton(Locale.forLanguageTag(locale.replace('_', '-'))),
                    lookUpname, Collections.singleton(propertyValue.toLowerCase()));
        } else {
            notificationTemplates =
                dynamicNotificationTemplateDao.findByNotificationIdsBrandsLocalesNoAddAttrs(notificationId,
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
            dynamicNotificationTemplateDao
                .deleteByIds(
                    notificationTemplates.stream().map(DynamicNotificationTemplate::getId).toArray(String[]::new));
        }
    }

    /**
     * filterTemplatesWithMultipleAddAttrs.
     *
     * @param notificationTemplates notificationTemplates
     * @return List of DynamicNotificationTemplate
     */
    private List<DynamicNotificationTemplate> filterTemplatesWithMultipleAddAttrs(
        List<DynamicNotificationTemplate> notificationTemplates) {

        List<DynamicNotificationTemplate> deleteTemplates = new ArrayList<>();
        notificationTemplates.forEach(t -> {
            if (t.getAdditionalLookupProperties().size() <= 1) {
                deleteTemplates.add(t);
            }
        });
        return deleteTemplates;

    }

    /**
     * findTemplatesToDeleteOrUpdate.
     *
     * @param notificationTemplates notificationTemplates
     * @param propValue propValue
     * @return List of DynamicNotificationTemplate
     */
    private List<DynamicNotificationTemplate> findTemplatesToDeleteOrUpdate(
        List<DynamicNotificationTemplate> notificationTemplates, String propValue) {
        List<DynamicNotificationTemplate> deleteTemplates = new ArrayList<>();
        if (!notificationTemplates.isEmpty()) {
            deleteTemplates.addAll(notificationTemplates);
            notificationTemplates.forEach(t -> {
                if (!t.getAdditionalLookupProperties().isEmpty()
                    && t.getAdditionalLookupProperties().get(0).getValues().size() > 1) {
                    deleteTemplates.remove(t);
                    AdditionalLookupProperty prop = new AdditionalLookupProperty();
                    AdditionalLookupProperty propExist = t.getAdditionalLookupProperties().get(0);
                    prop.setName(propExist.getName());
                    prop.setOrder(propExist.getOrder());
                    propExist.getValues().remove(propValue);
                    prop.setValues(propExist.getValues());
                    Updates update = new Updates();
                    update.addFieldSet(NotificationDaoConstants.TEMPLATE_ADDITIONALLOOKUP_FIELD,
                        Collections.singletonList(prop));
                    dynamicNotificationTemplateDao.update(t.getId(), update);
                }
            });
        }

        return deleteTemplates;

    }

}
