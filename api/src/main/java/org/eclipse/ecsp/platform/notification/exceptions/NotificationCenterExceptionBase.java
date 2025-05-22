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

package org.eclipse.ecsp.platform.notification.exceptions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;

import java.util.ArrayList;
import java.util.Collection;

import static lombok.AccessLevel.PRIVATE;

/**
 * NotificationCenterExceptionBase class.
 */
@Getter
@Setter
@FieldDefaults(level = PRIVATE)
public class NotificationCenterExceptionBase extends RuntimeException {

    String code;
    String reason;

    final transient Collection<ResponseWrapper.Message> errors;

    NotificationCenterExceptionBase(String message, Collection<ResponseWrapper.Message> errors) {
        super(message);
        this.errors = errors != null ? errors : new ArrayList<>();
    }
}
