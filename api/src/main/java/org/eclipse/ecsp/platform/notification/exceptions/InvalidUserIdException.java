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

import lombok.experimental.FieldDefaults;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;

import java.util.Collection;

import static lombok.AccessLevel.PRIVATE;
import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.INVALID_USERID_EXCEPTION;

/**
 * InvalidUserIdException class.
 */
@FieldDefaults(level = PRIVATE)
public class InvalidUserIdException extends NotificationCenterExceptionBase {

    /**
     * InvalidUserIdException class constructor.
     */
    public InvalidUserIdException(Collection<ResponseWrapper.Message> errors) {
        super(INVALID_USERID_EXCEPTION.getMessage(), errors);
        this.setCode(INVALID_USERID_EXCEPTION.getCode());
        this.setReason(INVALID_USERID_EXCEPTION.getReason());
    }
}
