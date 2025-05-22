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

package org.eclipse.ecsp.domain.notification.commons;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.AbstractEventData;

import static lombok.AccessLevel.PRIVATE;

/**
 * NotificationLifecycleFeedbackEventData class.
 */
@Data
@Builder
@FieldDefaults(level = PRIVATE)
@EventMapping(id = EventID.NOTIFICATION_LIFECYCLE_FEEDBACK, version = Version.V1_0)
public class NotificationLifecycleFeedbackEventData extends AbstractEventData {
    String milestone;
    String campaignId;
    String vehicleId;
    String userId;
    String code;
    String message;
}
