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

package org.eclipse.ecsp.platform.notification.utils;

import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.PlaceholderContainer;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNonRegisteredVehicle;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidNotificationIdException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.csv.CSVFormat.RFC4180;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.CUSTOM_PLACEHOLDERS;
import static org.eclipse.ecsp.platform.notification.service.BaseNotificationTemplateService.ADDITIONAL_ATTRIBUTE_HEADER_PREFIX;
import static org.eclipse.ecsp.platform.notification.service.BaseNotificationTemplateService.UTF8_BOM;

/**
 * NotificationUtils utility class.
 */
public class NotificationUtils {


    private static final String PLACEHOLDER_REGEX = "(\\[\\$\\.Data\\." + CUSTOM_PLACEHOLDERS + "\\..*?\\])";

    private static final Set<String> VALID_LOCALES;

    static {
        VALID_LOCALES = Sets.newHashSet(
            Arrays.stream(Locale.getAvailableLocales()).map(Locale::toLanguageTag).map(String::toLowerCase)
                .toList());
        VALID_LOCALES.add("iw-il");
    }

    private NotificationUtils()
      {}
    
    /**
     * Validate all language tags in the languageTags param are valid locale.
     *
     * @param languageTags list of language tags
     * @return List
     */
    public static List<ResponseWrapper.Message> validateLocaleExistence(Collection<String> languageTags) {
        List<ResponseWrapper.Message> errors = new ArrayList<>();
        Set<String> errorLocales = languageTags.stream()
            .filter(tag -> !isValidLocale(tag))
            .collect(Collectors.toSet());

        if (!errorLocales.isEmpty()) {
            if (errorLocales.contains("")) {
                errors.add(NotificationCenterError.INPUT_EMPTY_LOCALE.toMessage(new HashSet<String>()));
                errorLocales.remove("");
            }
            if (!errorLocales.isEmpty()) {
                errors.add(NotificationCenterError.INPUT_INVALID_LOCALE.toMessage(errorLocales.toString()));
            }
        }

        return errors;
    }

    /**
     * Validate locale.
     */
    public static boolean isValidLocale(String languageTag) {
        if (StringUtils.isEmpty(languageTag)) {
            return false;
        }
        String[] langCountryCode = languageTag.replace("_", "-").split("-");
        return Arrays.asList(Locale.getISOLanguages()).contains(langCountryCode[0].toLowerCase())
            && Arrays.asList(Locale.getISOCountries()).contains(langCountryCode[1].toUpperCase());
    }

    /**
     * Validate notificationIds.
     *
     * @param notificationIds notificationIds
     *
     * @param filterDto filter
     *
     * @throws InvalidNotificationIdException exception
     */
    public static void validateNotificationIds(Set<String> notificationIds, NotificationTemplateFilterDto filterDto)
        throws InvalidNotificationIdException {
        Set<ResponseWrapper.Message> errors = new HashSet<>();
        for (String notificationId : filterDto.getNotificationTemplateIds()) {
            if (!notificationIds.contains(notificationId)) {
                errors.add(NotificationCenterError.NOTIFICATION_ID_DOES_NOT_EXIST.toMessage(notificationId));
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidNotificationIdException(errors);
        }
    }

    /**
     * check if record is empty.
     */
    public static boolean isEmptyRecord(CSVRecord rec) {
        if (rec.size() == 0) {
            return true;
        }

        for (String field : rec) {

            if (!StringUtils.isEmpty(field.trim())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Receive file, extract collection of csv records.
     *
     * @param file File to extract
     * @return Collection of {@link CSVRecord}
     */
    public static List<CSVRecord> getCsvRecords(MultipartFile file) throws IOException {
        try {
            return CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, RFC4180).getRecords();
        } catch (IOException e) {

            throw e;
        }
    }

    /**
     *Find the occurrences of additional lookup property.
     */
    public static int findNumOfAdditionalLookupProperties(CSVRecord propertiesHeadersRecord) {
        int counter = 0;
        for (int i = 0; i < propertiesHeadersRecord.size(); i++) {
            if (propertiesHeadersRecord.get(i).toLowerCase(Locale.ROOT)
                .startsWith(ADDITIONAL_ATTRIBUTE_HEADER_PREFIX)) {
                counter++;
            }
        }
        return counter;
    }

    /**
     *Create additional lookup property.
     */
    public static List<AdditionalLookupProperty> createAdditionalLookupProperties(CSVRecord rec,
                                                                                  CSVRecord headersRecord,
                                                                                  int startIndex,
                                                                                  int numOfAdditionalLookupProperties) {
        List<AdditionalLookupProperty> additionalLookupProperties = new ArrayList<>(numOfAdditionalLookupProperties);
        for (int i = 0; i < numOfAdditionalLookupProperties; i++) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(rec.get(i + startIndex))) {
                AdditionalLookupProperty additionalLookupProperty = new AdditionalLookupProperty();
                additionalLookupProperty.setName(
                    headersRecord.get(i + startIndex).substring(ADDITIONAL_ATTRIBUTE_HEADER_PREFIX.length()));
                additionalLookupProperty.setValues(
                    Sets.newHashSet(rec.get(i + startIndex).toLowerCase(Locale.ROOT).split("\\|")));
                additionalLookupProperty.setOrder((short) (i + 1));
                additionalLookupProperties.add(additionalLookupProperty);
            }
        }
        return additionalLookupProperties;
    }

    /**
     *Validate file.
     */
    public static void validateUtf8File(CSVRecord firstRow) {
        if (!firstRow.get(0).startsWith(UTF8_BOM)) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.TEMPLATE_INVALID_FORMAT.toMessage()));
        }
    }

    /**
     *Validate languages.
     */
    public static void validateLanguages(CSVRecord headerRow, int headersLength) {
        int numOfAdditionalLookupProperties = findNumOfAdditionalLookupProperties(headerRow);
        Map<String, Integer> countedLanguages =
            countLanguages(headerRow, numOfAdditionalLookupProperties, headersLength);

        if (countedLanguages.entrySet().stream().anyMatch(lang -> lang.getValue() > 1)) {
            throw new InvalidInputFileException(
                Collections.singletonList(NotificationCenterError.INPUT_DUPLICATE_LOCALE_PREFIX
                    .toMessage(
                        countedLanguages.entrySet().stream().filter(lang -> lang.getValue() > 1).toList()
                            .toString())));
        }

        List<ResponseWrapper.Message> errors = NotificationUtils.validateLocaleExistence(countedLanguages.keySet());
        if (!CollectionUtils.isEmpty(errors)) {
            throw new InvalidInputFileException(errors);
        }
    }

    /**
     *Count languages.
     */
    public static Map<String, Integer> countLanguages(CSVRecord propertiesHeaderRecord,
                                                      int numOfAdditionalLookupProperties,
                                                      int headersLength) {
        Map<String, Integer> languagesToCount = new HashMap<>(propertiesHeaderRecord.size());
        for (int i = headersLength + numOfAdditionalLookupProperties; i < propertiesHeaderRecord.size(); i++) {
            String currentLanguage = propertiesHeaderRecord.get(i).replace('_', '-').toLowerCase();
            languagesToCount.compute(currentLanguage, (k, v) -> v == null ? 1 : v + 1);
        }
        return languagesToCount;
    }

    /**
     *Check if record is unique.
     */
    public static boolean isRecordUnique(CSVRecord rec, Map<String, List<CSVRecord>> recordsUniqueness,
                                         String recordUniqueId,
                                         int numOfAdditionalLookupProperties, int propsStartIndex) {
        if (recordsUniqueness.containsKey(recordUniqueId)) {
            for (CSVRecord r : recordsUniqueness.get(recordUniqueId)) {
                if (!hasUniqueAttribute(rec, r, numOfAdditionalLookupProperties, propsStartIndex)) {
                    return false;
                }
            }
            List<CSVRecord> records = recordsUniqueness.get(recordUniqueId);
            records.add(rec);
            recordsUniqueness.put(recordUniqueId, records);
        } else {
            ArrayList<CSVRecord> records = new ArrayList<>();
            records.add(rec);
            recordsUniqueness.put(recordUniqueId, records);
        }
        return true;
    }

    /**
     *Add custom placeholder.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static void addCustomPlaceHolder(PlaceholderContainer placeholderContainer, String text) {

        List<String> placeholders = NotificationUtils.getVariables(text, PLACEHOLDER_REGEX);

        if (!CollectionUtils.isEmpty(placeholders)) {
            if (CollectionUtils.isEmpty(placeholderContainer.getCustomPlaceholders())) {
                placeholderContainer.setCustomPlaceholders(new HashSet<>());
            }
            for (String placeholder : placeholders) {
                placeholderContainer.getCustomPlaceholders()
                    .add(placeholder.substring(9 + CUSTOM_PLACEHOLDERS.length(), placeholder.length() - 1));
            }
        }
    }

    /**
     * Get placeholder variables.
     */
    public static List<String> getVariables(String msg, String regex) {
        List<String> results = new LinkedList<>();

        if (msg == null) {
            return results;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(msg);
        String placeHolderContent;

        while (matcher.find()) {
            placeHolderContent = matcher.group(1);
            results.add(placeHolderContent);
        }
        return results;
    }

    /**
     *Check if record has unique attribute.
     */
    private static boolean hasUniqueAttribute(CSVRecord record1, CSVRecord record2, int numOfAdditionalLookupProperties,
                                              int propsStartIndex) {
        for (int i = 0; i < numOfAdditionalLookupProperties; i++) {
            Set<String> oldRecordValues = new HashSet<>(Arrays.asList(record2.get(propsStartIndex + i).split("\\|")));
            Set<String> newRecordValues = new HashSet<>(Arrays.asList(record1.get(propsStartIndex + i).split("\\|")));
            if (newRecordValues.stream().noneMatch(oldRecordValues::contains)) {
                return true;
            }
        }
        return false;
    }

    /**
     *Check if vehicle is non registered.
     */
    public static boolean isNonRegisteredVehicle(NotificationCreationRequest notificationRequest) {

        Map<String, Object> data = notificationRequest.getData();
        if (data != null
            && data.containsKey(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD)
            && (Boolean) data.get(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD)) {
            return true;
        }
        return false;
    }

    /**
     *Validate vehicle existence.
     */
    public static void validateVehiclesExistence(
            VehicleService vehicleService, String vehicleId,
            boolean isNonRegisteredVehicle) {
        if (isNonRegisteredVehicle) {
            validateVehicleNotExists(vehicleService, vehicleId);
        } else {
            validateVehicleExists(vehicleService, vehicleId);
        }
    }

    /**
     *Validate vehicle not exists.
     */
    public static void validateVehicleNotExists(
            VehicleService vehicleService, String vehicleId) {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(vehicleId) && vehicleService.isVehicleExist(vehicleId)) {
            throw new InvalidNonRegisteredVehicle(Collections.emptyList());
        }
    }

    /**
     *Validate vehicle exists.
     */
    public static void validateVehicleExists(VehicleService vehicleService, String vehicleId) {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(vehicleId) && !vehicleService.isVehicleExist(vehicleId)) {
            throw new VehicleIdNotFoundException(Collections.emptyList());
        }
    }
}
