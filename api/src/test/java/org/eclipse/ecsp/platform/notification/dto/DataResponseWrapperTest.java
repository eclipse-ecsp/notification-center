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

package org.eclipse.ecsp.platform.notification.dto;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataResponseWrapperTest.
 */
class DataResponseWrapperTest {

    @Test
    void testBuilderWithSingleData() {
        String data = "test";
        ResponseWrapper.Message root = ResponseWrapper.Message.of("100", "root", "Root message");
        DataResponseWrapper<String> wrapper = DataResponseWrapper.<String>dataResponseWrapperBuilder()
                .httpStatusCode(HttpStatus.OK.value())
                .requestId("req-1")
                .count(1L)
                .data(Collections.singleton(data))
                .rootResponseWrapperMessage(root)
                .build();

        assertEquals(HttpStatus.OK.value(), wrapper.getHttpStatusCode());
        assertEquals("req-1", wrapper.getRequestId());
        assertEquals(1, wrapper.getCount());
        assertEquals(Collections.singleton(data), wrapper.getData());
    }

    @Test
    void testBuilderWithMultipleData() {
        ResponseWrapper.Message root = ResponseWrapper.Message.of("100", "root", "Root message");
        List<String> dataList = Arrays.asList("a", "b");
        DataResponseWrapper<String> wrapper = DataResponseWrapper.<String>dataResponseWrapperBuilder()
                .httpStatusCode(HttpStatus.OK.value())
                .count(2L)
                .data(dataList)
                .rootResponseWrapperMessage(root)
                .build();

        assertEquals(2, wrapper.getCount());
        assertEquals(dataList, wrapper.getData());
    }

    @Test
    void testBuilderWithMessagesAndErrors() {
        ResponseWrapper.Message root = ResponseWrapper.Message.of("100", "root", "Root message");
        List<ResponseWrapper.Message> errors = Arrays.asList(
                ResponseWrapper.Message.of("200", "error1", "Error message 1"),
                ResponseWrapper.Message.of("201", "error2", "Error message 2")
        );
        List<ResponseWrapper.Message> messages = List.of(
                ResponseWrapper.Message.of("300", "info", "Info message")
        );

        DataResponseWrapper<String> wrapper = DataResponseWrapper.<String>dataResponseWrapperBuilder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .rootMessage(root)
                .errors(errors)
                .messageList(messages)
                .count(0L)
                .data(Collections.emptyList())
                .build();

        assertEquals(HttpStatus.BAD_REQUEST.value(), wrapper.getHttpStatusCode());
        assertEquals(root, wrapper.getRootMessage());
        assertEquals(errors, wrapper.getErrors());
        assertEquals(messages, wrapper.getMessageList());
    }

    @Test
    void testPrivateConstructor() throws Exception {
        // Prepare test data
        int httpStatusCode = 200;
        String requestId = "test-request-id";
        ResponseWrapper.Message rootMessage = ResponseWrapper.Message.of("100", "root", "Root message");
        List<ResponseWrapper.Message> errors = Collections.singletonList(
                ResponseWrapper.Message.of("200", "error", "Error message"));
        List<ResponseWrapper.Message> messages = Collections.singletonList(
                ResponseWrapper.Message.of("300", "info", "Info message"));
        Long count = 5L;
        List<String> data = Arrays.asList("item1", "item2", "item3", "item4", "item5");

        // Get the private constructor
        Constructor<DataResponseWrapper> constructor = DataResponseWrapper.class.getDeclaredConstructor(
                int.class, String.class, ResponseWrapper.Message.class,
                Collection.class, Collection.class, Long.class, Collection.class);
        constructor.setAccessible(true);

        // Create instance using the private constructor
        DataResponseWrapper<String> wrapper = constructor.newInstance(
                httpStatusCode, requestId, rootMessage, errors, messages, count, data);

        // Verify the object state
        assertEquals(httpStatusCode, wrapper.getHttpStatusCode());
        assertEquals(requestId, wrapper.getRequestId());
        assertEquals(rootMessage, wrapper.getRootMessage());
        assertEquals(errors, wrapper.getErrors());
        assertEquals(messages, wrapper.getMessageList());
        assertEquals(count.intValue(), wrapper.getCount());
        assertEquals(data, wrapper.getData());
    }

    @Test
    void testOkWithCollection() {
        // Create test data
        List<String> testData = Arrays.asList("item1", "item2", "item3");

        // Call the static ok method
        DataResponseWrapper.DataResponseWrapperBuilder<String> builder = DataResponseWrapper.ok(testData);
        ResponseWrapper.Message rootMessage =
                ResponseWrapper.Message.of("201", "debug", "debug message");
        // Build the wrapper
        DataResponseWrapper<String> wrapper = builder.rootMessage(rootMessage).build();

        // Verify the properties
        assertEquals(HttpStatus.OK.value(), wrapper.getHttpStatusCode());
        assertEquals(testData, wrapper.getData());
        assertEquals(testData.size(), wrapper.getCount());
    }

    @Test
    void testOkWithAll() {
        // Create test data
        List<String> testData = Arrays.asList("item1", "item2", "item3");

        // Call the static ok method
        DataResponseWrapper.DataResponseWrapperBuilder<String> builder = DataResponseWrapper.ok(testData);
        ResponseWrapper.Message rootMessage =
                ResponseWrapper.Message.of("201", "debug", "debug message");

        List<ResponseWrapper.Message> errors = Collections.singletonList(
                ResponseWrapper.Message.of("200", "error", "Error message"));
        List<ResponseWrapper.Message> messages = Collections.singletonList(
                ResponseWrapper.Message.of("300", "info", "Info message"));

        // Build the wrapper
        DataResponseWrapper<String> wrapper = builder.rootMessage(rootMessage)
                .messageList(messages)
                .errors(errors)
                .build();

        // Verify the properties
        assertEquals(HttpStatus.OK.value(), wrapper.getHttpStatusCode());
        assertEquals(testData, wrapper.getData());
        assertEquals(testData.size(), wrapper.getCount());
    }

    @Test
    void testOkWithSingleItem() {
        // Create test data
        String testData = "singleItem";

        // Call the static ok method
        DataResponseWrapper.DataResponseWrapperBuilder<String> builder = DataResponseWrapper.ok(testData);
        ResponseWrapper.Message rootMessage =
                ResponseWrapper.Message.of("201", "debug", "debug message");
        // Build the wrapper
        DataResponseWrapper<String> wrapper = builder.rootMessage(rootMessage).build();

        // Verify the properties
        assertEquals(HttpStatus.OK.value(), wrapper.getHttpStatusCode());
        assertTrue(wrapper.getData().contains(testData));
        assertEquals(1, wrapper.getData().size());
        assertEquals(1, wrapper.getCount());
    }

    @Test
    void testMessageListMethod() {
        // Create test messages
        Collection<ResponseWrapper.Message> messages = Arrays.asList(
                ResponseWrapper.Message.of("100", "info", "Information message"),
                ResponseWrapper.Message.of("101", "warning", "Warning message")
        );

        // Create builder and call the messageList method
        ResponseWrapper.ResponseWrapperBuilder<String> builder = ResponseWrapper.<String>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .rootMessage(ResponseWrapper.Message.of("200", "success", "Operation successful"))
                .messageList(messages);

        // Build the wrapper
        ResponseWrapper<String> wrapper = builder.build();

        // Verify that the messageList was correctly set
        assertEquals(messages, wrapper.getMessageList());
        assertEquals(2, wrapper.getMessageList().size());
    }

    @Test
    void testErrorsMethod() {
        // Create test errors
        List<ResponseWrapper.ValidationError> errors = Arrays.asList(
                ResponseWrapper.ValidationError.of("400", "validation", "Field is required", "username"),
                ResponseWrapper.ValidationError.of("401", "validation", "Invalid format", "email")
        );

        // Create builder and call the errors method
        ResponseWrapper<String> wrapper = ResponseWrapper.<String>builder()
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .rootMessage(ResponseWrapper.Message.of("400", "error", "Validation failed"))
                .errors(errors)
                .build();

        // Verify that the errors was correctly set
        assertEquals(errors, wrapper.getErrors());
        assertEquals(2, wrapper.getErrors().size());

        // Check if all error elements are present
        for (ResponseWrapper.ValidationError error : errors) {
            assertTrue(wrapper.getErrors().contains(error));
        }
    }
}
