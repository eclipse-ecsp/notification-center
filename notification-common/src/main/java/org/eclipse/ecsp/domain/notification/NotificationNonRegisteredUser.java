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

package org.eclipse.ecsp.domain.notification;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.ecsp.utils.Constants;

import java.util.List;

/**
 * NotificationNonRegisteredUser class.
 */
@Getter
@Setter
@NoArgsConstructor
public class NotificationNonRegisteredUser extends NotificationRequest {
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String notificationId;
    private String version;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private List<NonRegisteredUserData> recipients;

    @Override
    public String toString() {
        return "NotificationNonRegisteredUser [notificationId=" + notificationId + ", version=" + version
            + ", recipients=" + recipients + ", toString()=" + super.toString() + "]";
    }
}
