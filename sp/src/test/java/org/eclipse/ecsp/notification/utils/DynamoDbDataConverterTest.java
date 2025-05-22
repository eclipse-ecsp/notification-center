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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.eclipse.ecsp.domain.notification.AmazonSESResponse;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DynamoDBDataConverterTest.
 */
public class DynamoDbDataConverterTest {

    private MemoryAppender memoryAppender;

    /**
     * test setup.
     */
    @Before
    public void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(DynamoDbDataConverter.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    public void channelResponseConverterConvert() {
        List<ChannelResponse> channelResponses = new ArrayList<>();
        AmazonSESResponse sesResponse = new AmazonSESResponse();
        sesResponse.setStatus("success");
        channelResponses.add(sesResponse);
        DynamoDbDataConverter.ChannelResponseConverter obj = new DynamoDbDataConverter.ChannelResponseConverter();
        String result = obj.convert(channelResponses);
        assertTrue(result.contains("\"status\":\"success\""));
    }

    @Test
    public void channelResponseConverterUnconvertSuccess() {
        String param =
            "[{\"defaultMessage\":null,\"alertData\":null,\"alertEventData\":null,\"userID\":null,\"alertsObject\":null"
                + ",\"pdid\":null,\"processedTime\":\"1636972978603\",\"eventData\":null,\"status\":\"success\","
                + "\"errorCode\":null,\"template\":null,\"provider\":\"AWS_SES\",\"destination\":null,"
                + "\"messageId\":null}]";
        DynamoDbDataConverter.ChannelResponseConverter obj = new DynamoDbDataConverter.ChannelResponseConverter();
        List<ChannelResponse> result = obj.unconvert(param);
        assertEquals(1, result.size());
        assertEquals("success", result.get(0).getStatus());
    }

    @Test
    public void channelResponseConverterUnconvertFailure() {
        memoryAppender.reset();
        DynamoDbDataConverter.ChannelResponseConverter obj = new DynamoDbDataConverter.ChannelResponseConverter();
        List<ChannelResponse> result = obj.unconvert("dsfgd");
        assertNotNull(result);
        assertEquals(1,
            memoryAppender.search("Not able to parse json string {} while converting back to channel response object.",
                Level.ERROR).size());

    }

    @Test
    public void dateTimeConverterConvert() {
        DateTime dateTime = DateTime.parse("2018-05-04T21:00:00.000Z");
        DynamoDbDataConverter.DateTimeConverter obj = new DynamoDbDataConverter.DateTimeConverter();
        String result = obj.convert(dateTime);
        assertEquals("2018-05-04T21:00:00.000Z", result);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void dateTimeConverterUnconvert() {

        DynamoDbDataConverter.DateTimeConverter obj = new DynamoDbDataConverter.DateTimeConverter();
        DateTime result = obj.unconvert("2018-05-04T21:00:00.000Z");
        assertEquals(2018, result.getYear());
        assertEquals(5, result.getMonthOfYear());
    }
}