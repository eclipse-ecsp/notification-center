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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.notification.dao.NotificationPlaceholderDao;
import org.eclipse.ecsp.notification.dao.NotificationPlaceholderImportedDataDao;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholder;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholderImportedData;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.utils.NotificationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.ecsp.platform.notification.service.BaseNotificationTemplateService.ADDITIONAL_ATTRIBUTE_HEADER_PREFIX;
import static org.eclipse.ecsp.platform.notification.service.BaseNotificationTemplateService.UTF8_BOM;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.createAdditionalLookupProperties;
import static org.eclipse.ecsp.platform.notification.utils.NotificationUtils.findNumOfAdditionalLookupProperties;

/**
 * PlaceholderServiceImpl class.
 */
@Service
public class PlaceholderServiceImpl implements PlaceholderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaceholderServiceImpl.class);

    static final int HEADER_ROW = 0;
    static final int KEY_COLUMN = 0;
    static final int BRAND_COLUMN = 1;
    static final int NUMBER_OF_COLUMNS_BEFORE_PROP = 2;
    private static final String[] MANDATORY_HEADERS = {"key", "brand"};

    private final NotificationPlaceholderDao notificationPlaceholderDao;
    private final NotificationPlaceholderImportedDataDao notificationPlaceholderImportedDataDao;

    /**
     * Constructor.
     *
     * @param notificationPlaceholderDao           NotificationPlaceholderDao
     * @param notificationPlaceholderImportedDataDao NotificationPlaceholderImportedDataDao
     */
    public PlaceholderServiceImpl(NotificationPlaceholderDao notificationPlaceholderDao,
                                  NotificationPlaceholderImportedDataDao notificationPlaceholderImportedDataDao) {
        this.notificationPlaceholderDao = notificationPlaceholderDao;
        this.notificationPlaceholderImportedDataDao = notificationPlaceholderImportedDataDao;
    }

    /**
     * Import placeholders.
     *
     * @param file File
     */
    @Override
    public void importPlaceholders(MultipartFile file) {

        List<CSVRecord> records;
        try {
            records = NotificationUtils.getCsvRecords(file).stream()
                .filter(rec -> !NotificationUtils.isEmptyRecord(rec)).toList();
        } catch (IOException e) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.PLACEHOLDERS_INPUT_INVALID_CSV.toMessage()));
        }
        validateInput(records);
        List<NotificationPlaceholder> notificationPlaceholders = getPlaceholders(records);
        savePlaceholders(notificationPlaceholders);
        saveCsvFile(records);
    }

    /**
     * Export placeholders.
     *
     * @param key String
     * @return byte[]
     */
    @Override
    public byte[] exportPlaceholders(String key) {
        NotificationPlaceholderImportedData placeholder = notificationPlaceholderImportedDataDao.findById(key);
        if (placeholder == null || StringUtils.isBlank(placeholder.getFile())) {
            throw new NotFoundException(
                Collections.singletonList(NotificationCenterError.PLACEHOLDERS_NOT_FOUND.toMessage()));
        }
        return Base64.decodeBase64(placeholder.getFile());
    }

    /**
     * Delete placeholder.
     *
     * @param key String
     */
    @Override
    public void deletePlaceholder(String key) {
        NotificationPlaceholderImportedData placeholderImportedData =
            notificationPlaceholderImportedDataDao.findById(key);
        List<NotificationPlaceholder> placeholders = notificationPlaceholderDao.findByKeys(Collections.singleton(key));
        if (placeholderImportedData == null && CollectionUtils.isEmpty(placeholders)) {
            throw new NotFoundException(
                Collections.singletonList(NotificationCenterError.PLACEHOLDERS_NOT_FOUND.toMessage()));
        }
        if (placeholderImportedData != null) {
            notificationPlaceholderImportedDataDao.deleteById(key);
        }
        if (!CollectionUtils.isEmpty(placeholders)) {
            notificationPlaceholderDao.deleteByKeys(Collections.singleton(key));
        }
    }

    /**
     * Validate input.
     *
     * @param records List of CSVRecord
     */
    private void validateInput(List<CSVRecord> records) {
        NotificationUtils.validateUtf8File(records.get(HEADER_ROW));
        validateMandatoryRowsAndColumns(records);
        NotificationUtils.validateLanguages(records.get(HEADER_ROW), MANDATORY_HEADERS.length);
        validateDuplicates(records);
    }

    /**
     * Validate mandatory rows and columns.
     *
     * @param records List of CSVRecord
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private void validateMandatoryRowsAndColumns(List<CSVRecord> records) {

        if (records.size() < 2) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_LINES.toMessage()));
        }

        CSVRecord headerRow = records.get(HEADER_ROW);

        if (headerRow.size() < MANDATORY_HEADERS.length + findNumOfAdditionalLookupProperties(headerRow) + 1) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_COLUMNS.toMessage()));
        }
        if (!headerRow.get(KEY_COLUMN).replace(UTF8_BOM, "").toLowerCase().equals(MANDATORY_HEADERS[KEY_COLUMN])
            || !headerRow.get(BRAND_COLUMN).toLowerCase().equals(MANDATORY_HEADERS[BRAND_COLUMN])) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_HEADERS.toMessage()));
        }

        validateHeadersOrder(headerRow);

        validateLocaleExists(headerRow);
    }

    /**
     * Validate headers order.
     *
     * @param headerRow CSVRecord
     */
    private void validateHeadersOrder(CSVRecord headerRow) {
        int propNum = findNumOfAdditionalLookupProperties(headerRow);
        for (int i = MANDATORY_HEADERS.length; i < MANDATORY_HEADERS.length + propNum; i++) {
            if (!headerRow.get(i).toLowerCase().startsWith(ADDITIONAL_ATTRIBUTE_HEADER_PREFIX.toLowerCase())) {
                throw new InvalidInputFileException(
                    Collections.singletonList(NotificationCenterError.PLACEHOLDERS_WRONG_HEADERS_ORDER.toMessage()));
            }
        }
    }

    /**
     * Validate locale exists.
     *
     * @param headerRow CSVRecord
     */
    private void validateLocaleExists(CSVRecord headerRow) {
        for (int i = MANDATORY_HEADERS.length + findNumOfAdditionalLookupProperties(headerRow); i < headerRow.size();
             i++) {
            if (StringUtils.isEmpty(headerRow.get(i))) {
                throw new InvalidInputFileException(
                    Collections.singletonList(NotificationCenterError.INPUT_EMPTY_LOCALE.toMessage()));
            }
        }
    }

    /**
     * Validate duplicates.
     *
     * @param records List of CSVRecord
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private void validateDuplicates(List<CSVRecord> records) {
        Map<String, List<CSVRecord>> recordsUniqueness = new HashMap<>();
        Set<String> duplicateRecords = new HashSet<>();
        int numOfAdditionalLookupProperties = findNumOfAdditionalLookupProperties(records.get(HEADER_ROW));
        int numOfColumns = records.get(HEADER_ROW).size();

        for (CSVRecord rec : records.stream().skip(HEADER_ROW + 1L).toList()) {
            if (rec.size() < numOfColumns) {
                throw new InvalidInputFileException(Collections.singletonList(
                    NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_DATA_FIELD.toMessage()));
            }

            String brand = rec.get(BRAND_COLUMN).trim().toLowerCase();
            String key = rec.get(KEY_COLUMN).trim();

            if (brand.isEmpty() || key.isEmpty()) {
                throw new InvalidInputFileException(Collections.singletonList(
                    NotificationCenterError.PLACEHOLDERS_INPUT_MISSING_DATA_FIELD.toMessage()));
            }

            String recordUniqueId = key + "-" + brand;
            if (!NotificationUtils.isRecordUnique(rec, recordsUniqueness, recordUniqueId,
                numOfAdditionalLookupProperties, 2)) {
                duplicateRecords.add(recordUniqueId);
            }
        }

        if (!CollectionUtils.isEmpty(duplicateRecords)) {
            throw new InvalidInputFileException(Collections.singletonList(
                NotificationCenterError.PLACEHOLDERS_INPUT_DUPLICATE_DATA.toMessage(duplicateRecords.toString())));
        }
    }

    /**
     * Get placeholders.
     *
     * @param records List of CSVRecord
     * @return List of NotificationPlaceholder
     */
    private List<NotificationPlaceholder> getPlaceholders(List<CSVRecord> records) {

        CSVRecord headersRecord = records.get(HEADER_ROW);
        int numOfAdditionalLookupProperties = findNumOfAdditionalLookupProperties(headersRecord);

        List<String> languages = new ArrayList<>();
        for (int i = NUMBER_OF_COLUMNS_BEFORE_PROP + numOfAdditionalLookupProperties; i < headersRecord.size(); i++) {
            languages.add(Locale.forLanguageTag(headersRecord.get(i).replace('_', '-')).toString());
        }

        List<NotificationPlaceholder> placeholders = new ArrayList<>();
        for (CSVRecord rec : records.stream().skip(HEADER_ROW + 1L).toList()) {
            handleRecord(rec, headersRecord, languages, placeholders, numOfAdditionalLookupProperties);
        }
        return placeholders;
    }

    /**
     * Handle record.
     *
     * @param rec                        CSVRecord
     * @param headersRecord              CSVRecord
     * @param languages                  List of String
     * @param placeholders               List of NotificationPlaceholder
     * @param numOfAdditionalLookupProperties int
     */
    private void handleRecord(CSVRecord rec, CSVRecord headersRecord, List<String> languages,
                              List<NotificationPlaceholder> placeholders, int numOfAdditionalLookupProperties) {

        for (int i = 0; i < languages.size(); i++) {
            NotificationPlaceholder notificationPlaceholder = new NotificationPlaceholder();
            notificationPlaceholder.setKey(rec.get(KEY_COLUMN));
            notificationPlaceholder.setBrand(rec.get(BRAND_COLUMN).toLowerCase());
            notificationPlaceholder.setAdditionalLookupProperties(
                createAdditionalLookupProperties(rec, headersRecord, NUMBER_OF_COLUMNS_BEFORE_PROP,
                    numOfAdditionalLookupProperties));
            notificationPlaceholder.setLocale(Locale.forLanguageTag(languages.get(i).replace("_", "-")));
            String value = rec.get(NUMBER_OF_COLUMNS_BEFORE_PROP + numOfAdditionalLookupProperties + i);
            notificationPlaceholder.setValue(StringUtils.isEmpty(value) ? "" : value);
            placeholders.add(notificationPlaceholder);
        }
    }

    /**
     * Save placeholders.
     *
     * @param notificationPlaceholders List of NotificationPlaceholder
     */
    private void savePlaceholders(List<NotificationPlaceholder> notificationPlaceholders) {
        Set<String> placeholdersKeys =
            notificationPlaceholders.stream().map(NotificationPlaceholder::getKey).collect(Collectors.toSet());
        notificationPlaceholderDao.deleteByKeys(placeholdersKeys);
        notificationPlaceholderDao.saveAll(notificationPlaceholders.toArray(new NotificationPlaceholder[0]));
    }

    /**
     * Save CSV file.
     *
     * @param records List of CSVRecord
     */
    private void saveCsvFile(List<CSVRecord> records) {
        Set<String> keys = records.stream().skip(HEADER_ROW + 1L).map(rec -> rec.get(KEY_COLUMN)).collect(toSet());
        for (String key : keys) {
            List<CSVRecord> newList = new ArrayList<>();
            newList.add(records.get(HEADER_ROW));
            newList.addAll(
                records.stream().filter(rec -> rec.get(KEY_COLUMN).equals(key)).toList());
            try {
                byte[] file = getCsvFileStream(newList);
                NotificationPlaceholderImportedData notificationPlaceholderImportedData =
                    new NotificationPlaceholderImportedData(key,
                        Base64.encodeBase64String(file));
                notificationPlaceholderImportedDataDao.save(notificationPlaceholderImportedData);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Get CSV file stream.
     *
     * @param records List of CSVRecord
     * @return byte[]
     */
    private byte[] getCsvFileStream(List<CSVRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new byte[]{};
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            OutputStreamWriter outputStreamWriter =
                new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8.newEncoder());

            CSVPrinter csvPrinter = new CSVPrinter(outputStreamWriter, CSVFormat.DEFAULT);
            for (CSVRecord rec : records) {
                csvPrinter.printRecord(rec);
            }

            csvPrinter.flush();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();

            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            LOGGER.error("error in converting data to save to DB", e);
        }
        return new byte[]{};
    }
}

