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

package org.eclipse.ecsp.notification.processors.transformers;

import org.eclipse.ecsp.processor.content.dto.ContentProcessingContextDto;
import org.eclipse.ecsp.processor.content.plugin.ContentTransformer;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SM: Date Format in Notification. As part of contentTransformer.
 * plugin, this class is used to transform the date from given source date
 * format to target date format.

 * NOTE: Strict checks are done while parsing date based on provided format.
 * Incase of failure, date is returned without any format change.

 * USAGE: * [[formatDate|SOURCE_DATE_FORMAT|TARGET_DATE_FORMAT|[$.Data.DATE_FIELD_NAME]]]

 * Listing few examples below: 1> [[formatDate|yyyy-MM-dd|dd-MM-yyyy|[$.Data.purchaseDate]]]
 * purchaseDate="2021-06-22

 * 2>[[formatDate|yyyy-MM-dd ss:mm:HH|dd-MM-yyyy HH:mm:ss|[$.Data.purchaseDate]]] purchaseDate="2021-06-22 00:30:10"
 * 3>[[formatDate|MM-dd-yyyy HH:mm:ss Z|dd-MM-yyyy HH:mm:ss Z|[$.Data.purchaseDate]]]
 * purchaseDate="02-06-2021 00:30:10 +0000"

 * 4>[[formatDate|MM-dd-yyyy HH:mm:ss z|dd-MM-yyyy HH:mm:ss
 * z|[$.Data.purchaseDate]]] purchaseDate="02-06-2021 00:30:10 UTC"
 *
 * @author MaKumari
 */

@Extension
@Component
public class DateFormatTransformer implements ContentTransformer {

    private static final int TWO = 2;
    private static final  String ID = "formatDate";
    private static final  String DELIMITER = "\\|";
    private static IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DateFormatTransformer.class);

    /**
     * This method is used to transform the date from given source date format to target date format.
     *
     * @param context the given context
     * @param content the given content
     * @return the transformed date
     */
    @Override
    public String apply(ContentProcessingContextDto context, String content) {

        LOGGER.info("Inside the DateFormatTransformer ,contentToTransform {}, context {}", content, context);
        String[] resolvedFields = content.split(DELIMITER);
        DateTimeFormatter sourceFormatter = null;
        DateTimeFormatter targetFormatter = null;
        try {
            sourceFormatter = DateTimeFormatter.ofPattern(resolvedFields[0]);
            targetFormatter = DateTimeFormatter.ofPattern(resolvedFields[1]);
            return transformDate(resolvedFields[TWO], sourceFormatter, targetFormatter);
        } catch (Exception e) {
            LOGGER.error("Error occurred while formatting the date. Returning the date without any format change.", e);
        }
        return resolvedFields[TWO];
    }

    /**
     * This method is used to transform the date from given source date format to target date format.
     *
     * @param dateString the given date string
     * @param sourceFormatter the given source formatter
     * @param targetFormatter the given target formatter
     * @return the transformed date
     */
    private String transformDate(String dateString, DateTimeFormatter sourceFormatter,
                                 DateTimeFormatter targetFormatter) {
        try {
            return LocalDate.parse(dateString, sourceFormatter).format(targetFormatter);
        } catch (Exception e) {
            LOGGER.error("Error parsing input date as LocaleDate. Continuing to parse as LocaleDateTime", e);
        }
        try {
            return LocalDateTime.parse(dateString, sourceFormatter).format(targetFormatter);
        } catch (Exception e) {
            LOGGER.error("Error parsing input date as LocaleTimeDate. Continuing to parse as ZonedDateTime", e);
        }
        try {
            return ZonedDateTime.parse(dateString, sourceFormatter).format(targetFormatter);
        } catch (Exception e) {
            LOGGER.error("Error parsing input date as ZonedDateTime. Returning the date without any format change.", e);
        }
        return dateString;
    }

    /**
     * This method is used to get the id.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return ID;
    }

}
