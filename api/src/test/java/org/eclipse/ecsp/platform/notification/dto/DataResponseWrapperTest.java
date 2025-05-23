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

import java.util.Arrays;
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
}
