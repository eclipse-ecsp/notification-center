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

package org.eclipse.ecsp.notification.entities;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import static org.eclipse.ecsp.notification.dao.NotificationDaoConstants.RICH_CONTENT_IMPORTED_DATA_COLLECTION_NAME;

/**
 * RichContentImportedData class.
 */
@Entity(value = RICH_CONTENT_IMPORTED_DATA_COLLECTION_NAME, useDiscriminator = false)
public class RichContentImportedData extends AbstractIgniteEntity {
    @Id
    private String notificationId;
    private byte[] file;

    /**
     * RichContentImportedData constructor.
     *
     * @param notificationId notificationId
     *
     * @param file file
     */
    public RichContentImportedData(String notificationId, String file) {
        super();
        this.notificationId = notificationId;
        this.file = Base64.decodeBase64(file);
    }

    /**
     * Get notificationId.
     *
     * @return notificationId
     */
    public String getNotificationId() {
        return notificationId;
    }

    // To avoid returning the mutable array reference , returning a base64
    // encoded string
    public String getFile() {
        return Base64.encodeBase64String(file);
    }

}
