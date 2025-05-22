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
import lombok.Builder;
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
    @Builder(builderMethodName = "dataResponseWrapperBuilder")
    private DataResponseWrapper(int httpStatusCode, String requestId, Message rootResponseWrapperMessage,
                                Collection<? extends Message> errors, Collection<Message> responseWrapperMessages,
                                Long count, Collection<T> data) {
        super(httpStatusCode, requestId, rootResponseWrapperMessage, errors, responseWrapperMessages);
        this.count = toIntExact(count);
        this.data = data;
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
}
