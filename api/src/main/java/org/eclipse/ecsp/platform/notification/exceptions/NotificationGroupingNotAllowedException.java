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

import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;

import java.util.Collection;

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_GROUP_NOT_ALLOWED;

/**
 * NotificationGroupingNotAllowedException class.
 */
public class NotificationGroupingNotAllowedException extends NotificationCenterExceptionBase {

    private static final long serialVersionUID = 4517299967333590269L;

    /**
     * NotificationGroupingNotAllowedException class.
     */
    public NotificationGroupingNotAllowedException(Collection<ResponseWrapper.Message> errors, String... formatParam) {
        super(NOTIFICATION_CONFIG_GROUP_NOT_ALLOWED.getMessage(formatParam), errors);
        this.setCode(NOTIFICATION_CONFIG_GROUP_NOT_ALLOWED.getCode());
        this.setReason(NOTIFICATION_CONFIG_GROUP_NOT_ALLOWED.getReason());
    }
}
