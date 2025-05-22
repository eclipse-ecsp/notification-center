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

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;
import org.eclipse.ecsp.utils.Constants;

import java.io.Serializable;
import java.util.Locale;

/**
 * Class SecondaryContact.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity(value = NotificationDaoConstants.SECONDARY_CONTACT_COLLECTION_NAME)
public class SecondaryContact extends AbstractIgniteEntity implements Serializable {

    private static final long serialVersionUID = 8506400548321231268L;
    @Id
    private String contactId;
    private String userId;
    @NotEmpty(message = Constants.VALIDATION_MESSAGE)
    private String contactName;
    @NotNull(message = Constants.VALIDATION_MESSAGE)
    private Locale locale;
    private String email;
    private String phoneNumber;
    private String vehicleId;
}
