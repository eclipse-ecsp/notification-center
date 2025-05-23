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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Collection;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;



/**
 * Response wrapper class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class ResponseWrapper<T> {
    // @ApiModelProperty(name = "Http status code", required = true, example = "200")
    //@NonNull
    int httpStatusCode;

    //  @ApiModelProperty(name = "Request id", required = true, example = "529269fb1459")
    String requestId;

    @JsonUnwrapped
    @NonNull
    Message rootMessage;

    //@ApiModelProperty(name = "List of errors")
    Collection<? extends Message> errors;

    // @ApiModelProperty(name = "List of additional messages")
    Collection<Message> messageList;

    ResponseWrapper(ResponseWrapperBuilder<T> responseWrapperBuilder) {
        this.httpStatusCode = responseWrapperBuilder.httpStatusCode;
        this.requestId = responseWrapperBuilder.requestId;
        this.rootMessage = responseWrapperBuilder.rootMessage;
        this.errors = responseWrapperBuilder.errors;
        this.messageList = responseWrapperBuilder.messageList;
    }

    /**
     * ResponseWrapperBuilder OK.
     */
    public static ResponseWrapperBuilder<Void> ok() {
        return ResponseWrapper.<Void>builder()
            .httpStatusCode(HttpStatus.OK.value());
    }

    /**
     * ResponseWrapperBuilder BAD_REQUEST.
     */
    public static ResponseWrapperBuilder<Void> badRequest() {
        return ResponseWrapper.<Void>builder()
            .httpStatusCode(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * ResponseWrapperBuilder NOT_FOUND.
     */
    public static ResponseWrapperBuilder<Void> notFound() {
        return ResponseWrapper.<Void>builder()
            .httpStatusCode(NOT_FOUND.value());
    }

    /**
     * ResponseWrapperBuilder ACCEPTED.
     */
    public static ResponseWrapperBuilder<Void> accepted() {
        return ResponseWrapper.<Void>builder()
            .httpStatusCode(ACCEPTED.value());
    }

    /**
     * ResponseWrapperBuilder INTERNAL_SERVER_ERROR.
     */
    public static ResponseWrapperBuilder<Void> internalServerError() {
        return ResponseWrapper.<Void>builder()
            .httpStatusCode(INTERNAL_SERVER_ERROR.value());
    }

    /**
     * ResponseWrapperBuilder for building the response object.
     */
    public static <T> ResponseWrapperBuilder<T> builder() {
        return new ResponseWrapperBuilder<>();
    }

    /**
     * Builder class for constructing {@link ResponseWrapper} instances.
     * Allows step-by-step configuration of response fields such as HTTP status, request ID, messages, errors, and additional messages.
     *
     * @param <T> the type of data contained in the response
     */
    public static class ResponseWrapperBuilder<T> {
        private int httpStatusCode;
        private String requestId;
        private Message rootMessage;
        private Collection<? extends Message> errors;
        private Collection<Message> messageList;

        public ResponseWrapperBuilder<T> httpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public ResponseWrapperBuilder<T> requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ResponseWrapperBuilder<T> rootMessage(Message rootMessage) {
            this.rootMessage = rootMessage;
            return this;
        }

        public ResponseWrapperBuilder<T> errors(Collection<? extends Message> errors) {
            this.errors = errors;
            return this;
        }

        public ResponseWrapperBuilder<T> messageList(Collection<Message> messageList) {
            this.messageList = messageList;
            return this;
        }

        public ResponseWrapper<T> build() {
            return new ResponseWrapper<>(this);
        }
    }

    /**
     * static Message class.
     */
    @AllArgsConstructor
    @Getter
    @Setter
    @FieldDefaults(level = PRIVATE)
    public static class Message {
        @NonNull
        String code;
        @NonNull
        String reason;
        @NonNull
        String msg;

        public static Message of(String code, String reason, String message) {
            return new Message(code, reason, message);
        }
    }

    /**
     * Static class for Validation error.
     */
    @Getter
    @FieldDefaults(level = PRIVATE)
    @JsonInclude(NON_EMPTY)
    public static class ValidationError extends Message {
        String field;

        /**
         * ValidationError.
         */
        private ValidationError(String code, String reason, String message, String field) {
            super(code, reason, message);
            this.field = field;
        }

        /**
         * ValidationError.
         */
        public static ValidationError of(String code, String reason, String message, String field) {
            return new ValidationError(code, reason, message, field);
        }
    }

}