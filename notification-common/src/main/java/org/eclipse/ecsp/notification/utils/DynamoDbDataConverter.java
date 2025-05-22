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

package org.eclipse.ecsp.notification.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides converter for complex objects for AlertsHistoryInfo.
 * (org.eclipse.ecsp.notification.alert.AlertsHistoryInfo) domain object.
 *
 * @author Neerajkumar
 */
public class DynamoDbDataConverter {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final DateTimeFormatter ISODDTFORMATTER = ISODateTimeFormat.dateTime().withZoneUTC();
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbDataConverter.class);

    private DynamoDbDataConverter() {
    }

    /**
     * This class is used to convert ChannelResponseConverter info data to DB
     * and fetch it back from database.
     */
    public static class ChannelResponseConverter implements DynamoDBTypeConverter<String, List<ChannelResponse>> {
        @Override
        public String convert(List<ChannelResponse> param) {
            try {
                String val = MAPPER.writeValueAsString(param);
                LOGGER.debug("ChannelResponseConverter Convert: ChannelResponse String: {} ", val);
                return val;
            } catch (JsonProcessingException e) {
                LOGGER.error("Not able to parse channel response {} data while converting to json string. Error {}",
                        param, e);
            }
            return null;
        }

        /**
         * This method is used to convert the string data back to ChannelResponse
         * object.
         *
         * @param data String
         * @return List ChannelResponse
         */
        @Override
        public List<ChannelResponse> unconvert(String data) {
            try {
                List<ChannelResponse> val = JsonUtils.getListObjects(data, ChannelResponse.class);
                LOGGER.debug("ChannelResponseConverter unconvert: ChannelResponse String: {} ", val);
                return val;
            } catch (IOException e) {
                LOGGER.error(
                        "Not able to parse json string {} while converting back to channel response object. Error {} ",
                        data, e);
            }
            return new ArrayList<ChannelResponse>();

        }

    }

    /**
     * This class is used to convert createDts data to DB and fetch it back from
     * database.
     */
    public static class DateTimeConverter implements DynamoDBTypeConverter<String, DateTime> {
        /**
         * This method is used to convert the DateTime object to string.
         *
         * @param param DateTime
         * @return String
         */
        @Override
        public String convert(DateTime param) {
            return ISODDTFORMATTER.print(param);
        }

        /**
         * This method is used to convert the string data back to DateTime object.
         *
         * @param data String
         * @return DateTime
         */
        @Override
        public DateTime unconvert(String data) {
            return ISODDTFORMATTER.parseDateTime(data);
        }

    }
}
