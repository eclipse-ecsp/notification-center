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

import org.apache.commons.csv.CSVRecord;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.platform.notification.dto.NotificationTemplateFilterDto;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.utils.NotificationUtils;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * BaseNotificationTemplateService abstract class.
 */
public abstract class BaseNotificationTemplateService {
    public static final String UTF8_BOM = "\uFEFF";
    public static final String ADDITIONAL_ATTRIBUTE_HEADER_PREFIX = "prop.";
    static final String NOTIFICATION_ID_HEADLINE = "Notification ID";
    static final int NOTIFICATION_DESCRIPTION_HEADER_ROW = 0;
    static final int NOTIFICATION_DESCRIPTION_DATA_ROW = 1;
    static final int NOTIFICATION_PROPERTIES_HEADER_ROW = 2;
    static final int ADDITIONAL_LOOKUP_PROPERTIES_START_INDEX = 1;
    static final String PLACEHOLDER_REGEX = "(\\[\\[formatDate\\|(.*?)\\]\\])";
    static final String DATE_FORMAT_DELIMITER = "\\|";

    /**
     * Validates the notification template filter DTO.
     *
     * @param filterDto filterDto
     */
    protected void validateNotificationIds(NotificationTemplateFilterDto filterDto) {
        Set<String> notificationIds = getTemplateNotificationIds(filterDto);
        NotificationUtils.validateNotificationIds(notificationIds, filterDto);
    }

    /**
     * getZipEntryFileStream.
     *
     * @param filterDto filterDto
     * @param byteArrayOutputStream byteArrayOutputStream
     * @param zipOutputStream zipOutputStream
     * @return byte[]
     * @throws IOException IOException
     */
    protected byte[] getZipEntryFileStream(NotificationTemplateFilterDto filterDto,
                                           ByteArrayOutputStream byteArrayOutputStream,
                                           ZipOutputStream zipOutputStream) throws IOException {
        Set<String> notificationIds;

        if (CollectionUtils.isEmpty(filterDto.getNotificationTemplateIds())) {
            notificationIds = getAllTemplateNotificationIds();
        } else {
            notificationIds = new HashSet<>(filterDto.getNotificationTemplateIds());
        }

        for (String notificationId : notificationIds) {
            createZipEntryFileStream(notificationId, zipOutputStream);
        }
        zipOutputStream.flush();
        zipOutputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * createZipEntryFileStream.
     *
     * @param notificationId notificationId
     * @param zipOutputStream zipOutputStream
     * @throws IOException IOException
     */
    private void createZipEntryFileStream(String notificationId, ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(getZipEntry(notificationId));
        zipOutputStream.write(getTemplateFile(notificationId));
        zipOutputStream.closeEntry();
    }

    /**
     * countLanguages.
     *
     * @param propertiesHeaderRecord propertiesHeaderRecord
     * @param numOfAdditionalLookupProperties numOfAdditionalLookupProperties
     * @return Map of String and Integer
     */
    protected Map<String, Integer> countLanguages(CSVRecord propertiesHeaderRecord,
                                                  int numOfAdditionalLookupProperties) {
        Map<String, Integer> languagesToCount = new HashMap<>(propertiesHeaderRecord.size());
        for (int i = getHeadersRecordLength() + numOfAdditionalLookupProperties; i < propertiesHeaderRecord.size();
             i++) {
            String currentLanguage = propertiesHeaderRecord.get(i).replace('_', '-').toLowerCase();
            languagesToCount.compute(currentLanguage, (k, v) -> v == null ? 1 : v + 1);
        }
        return languagesToCount;
    }

    /**
     * getAdditionalLookupPropertiesValuesSignature.
     *
     * @param additionalLookupProperties additionalLookupProperties
     * @return String
     */
    protected String getAdditionalLookupPropertiesValuesSignature(
        List<AdditionalLookupProperty> additionalLookupProperties) {
        StringBuilder additionalLookupPropertiesSign = new StringBuilder();
        for (AdditionalLookupProperty additionalLookupProperty : additionalLookupProperties) {
            additionalLookupPropertiesSign.append(
                    additionalLookupProperty.getValues().stream().sorted().collect(Collectors.joining("|")))
                .append(";");
        }
        return additionalLookupPropertiesSign.toString();
    }

    protected abstract Set<String> getTemplateNotificationIds(NotificationTemplateFilterDto filterDto);

    protected abstract Set<String> getAllTemplateNotificationIds();

    protected abstract byte[] getTemplateFile(String notificationId);

    protected abstract ZipEntry getZipEntry(String notificationId);

    protected abstract int getHeadersRecordLength();

    /**
     * SM: Date Format in Notification
     *
     * <p>Performs the DateFormat content transformer template validation.
     *
     * <p>USAGE:
     * [[formatDate|SOURCE_DATE_FORMAT|TARGET_DATE_FORMAT|[$.Data.DATE_FIELD_NAME]]]
     *
     * <p>Listing few examples below: 1>
     * [[formatDate|yyyy-MM-dd|dd-MM-yyyy|[$.Data.purchaseDate]]]
     * purchaseDate="2021-06-22
     *
     * <p>2>[[formatDate|yyyy-MM-dd ss:mm:HH|dd-MM-yyyy
     * HH:mm:ss|[$.Data.purchaseDate]]] purchaseDate="2021-06-22 00:30:10"
     *
     * <p>3>[[formatDate|MM-dd-yyyy HH:mm:ss Z|dd-MM-yyyy HH:mm:ss
     * Z|[$.Data.purchaseDate]]] purchaseDate="02-06-2021 00:30:10 +0000"
     *
     * <p>4>[[formatDate|MM-dd-yyyy HH:mm:ss z|dd-MM-yyyy HH:mm:ss
     * z|[$.Data.purchaseDate]]] purchaseDate="02-06-2021 00:30:10 UTC"
     *
     * @param html html
     *
     * @return response
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    protected Set<ResponseWrapper.Message> validateDateTransformerTemplate(String html) {
        Set<ResponseWrapper.Message> missingAttributes = new HashSet<>();
        String contentTransformerTemplate = null;
        String inputContent = null;
        String[] resolvedFields = null;
        Pattern pattern = Pattern.compile(PLACEHOLDER_REGEX);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            contentTransformerTemplate = matcher.group(1);
            inputContent = matcher.group(2).trim();
            resolvedFields = inputContent.split(DATE_FORMAT_DELIMITER);
            if (null == resolvedFields || 3 != resolvedFields.length) {
                missingAttributes.add(NotificationCenterError.DATE_FORMATTER_CONTENT_TRANSFORMER_INVALID_TEMPLATE
                    .toMessage(contentTransformerTemplate));
                continue;
            }
            try {
                DateTimeFormatter.ofPattern(resolvedFields[0]);
                DateTimeFormatter.ofPattern(resolvedFields[1]);
            } catch (Exception e) {
                missingAttributes.add(
                    NotificationCenterError.DATE_FORMATTER_CONTENT_TRANSFORMER_INVALID_DATE_PATTERN.toMessage(
                        contentTransformerTemplate, e.getMessage()));
            }

        }
        return missingAttributes;
    }
}
