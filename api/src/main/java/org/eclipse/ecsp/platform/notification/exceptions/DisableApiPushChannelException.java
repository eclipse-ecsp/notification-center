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

import static org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError.NOTIFICATION_CONFIG_API_PUSH_DISABLE_NOT_ALLOWED;

/**
 * DisableApiPushChannelException class.
 */
public class DisableApiPushChannelException extends NotificationCenterExceptionBase {

    /**
     * DisableApiPushChannelException constructor.
     */
    public DisableApiPushChannelException(Collection<ResponseWrapper.Message> errors) {
        super(NOTIFICATION_CONFIG_API_PUSH_DISABLE_NOT_ALLOWED.getMessage(), errors);
        this.setCode(NOTIFICATION_CONFIG_API_PUSH_DISABLE_NOT_ALLOWED.getCode());
        this.setReason(NOTIFICATION_CONFIG_API_PUSH_DISABLE_NOT_ALLOWED.getReason());

    }
}
