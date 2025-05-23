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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.Collections;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.lang.Math.toIntExact;
import static lombok.AccessLevel.PRIVATE;


/**
 * DataResponseWrapper for API response.
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@FieldDefaults(level = PRIVATE)
@JsonInclude(NON_EMPTY)
public class DataResponseWrapper<T> extends ResponseWrapper<T> {

    // @ApiModelProperty(name = "Number of entities returned", required = true, example = "1")
    Integer count;

    // @ApiModelProperty(name = "Data wrapper", required = true)
    Collection<T> data;

    /**
     * DataResponseWrapper.
     */
    private DataResponseWrapper(int httpStatusCode, String requestId, Message rootResponseWrapperMessage,
                                Collection<? extends Message> errors, Collection<Message> responseWrapperMessages,
                                Long count, Collection<T> data) {
        super(httpStatusCode, requestId, rootResponseWrapperMessage, errors, responseWrapperMessages);
        this.count = toIntExact(count);
        this.data = data;
    }

    private DataResponseWrapper(DataResponseWrapperBuilder<T> dataResponseWrapperBuilder) {
        super(dataResponseWrapperBuilder.httpStatusCode, dataResponseWrapperBuilder.requestId,
                dataResponseWrapperBuilder.rootMessage, dataResponseWrapperBuilder.errors,
                dataResponseWrapperBuilder.messageList);
        this.count = dataResponseWrapperBuilder.count;
        this.data = dataResponseWrapperBuilder.data;
    }

    /**
     * DataResponseWrapperBuilder.
     */
    public static <T> DataResponseWrapperBuilder<T> ok(T t) {
        return DataResponseWrapper.<T>dataResponseWrapperBuilder()
            .httpStatusCode(HttpStatus.OK.value())
            .data(Collections.singleton(t))
            .count(1L);
    }

    /**
     * DataResponseWrapperBuilder.
     */
    public static <T> DataResponseWrapperBuilder<T> ok(Collection<T> results) {
        return DataResponseWrapper.<T>dataResponseWrapperBuilder()
            .httpStatusCode(HttpStatus.OK.value())
            .data(results)
            .count((long) results.size());
    }

    /**
     * DataResponseWrapperBuilder for building the response object.
     */
    public static <T> DataResponseWrapperBuilder<T> dataResponseWrapperBuilder() {
        return new DataResponseWrapperBuilder<>();
    }

    /**
     * Builder class for constructing {@link DataResponseWrapper} instances.
     * Allows step-by-step configuration of response fields such as HTTP status, request ID, messages, errors, count, and data.
     *
     * @param <T> the type of data contained in the response
     */
    public static class DataResponseWrapperBuilder<T> {
        private int httpStatusCode;
        private String requestId;
        private Message rootMessage;
        private Collection<? extends Message> errors;
        private Collection<Message> messageList;
        private Integer count;
        private Collection<T> data;

        public DataResponseWrapperBuilder<T> httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public DataResponseWrapperBuilder<T> requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public DataResponseWrapperBuilder<T> rootMessage(Message rootMessage) {
            this.rootMessage = rootMessage;
            return this;
        }

        public DataResponseWrapperBuilder<T> errors(Collection<? extends Message> errors) {
            this.errors = errors;
            return this;
        }

        public DataResponseWrapperBuilder<T> messageList(Collection<Message> messageList) {
            this.messageList = messageList;
            return this;
        }

        public DataResponseWrapperBuilder<T> count(Long count) {
            this.count = toIntExact(count);
            return this;
        }

        public DataResponseWrapperBuilder<T> data(Collection<T> data) {
            this.data = data;
            return this;
        }

        // Custom-named builder method
        public DataResponseWrapperBuilder<T> rootResponseWrapperMessage(Message rootMessage) {
            this.rootMessage = rootMessage;
            return this;
        }

        public DataResponseWrapper<T> build() {
            return new DataResponseWrapper<>(this);
        }
    }
}
