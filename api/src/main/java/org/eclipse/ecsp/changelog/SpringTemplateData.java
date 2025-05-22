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

package org.eclipse.ecsp.changelog;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static org.eclipse.ecsp.notification.dao.NotificationDaoConstants.NOTIFICATION_TEMPLATE_IMPORTED_DATA_COLLECTION_NAME;

/**
 * SpringTemplateData for notification template import data.
 */
@Document(collection = NOTIFICATION_TEMPLATE_IMPORTED_DATA_COLLECTION_NAME)
public class SpringTemplateData {
    @Id
    private String notificationId;
    private byte[] file;
    private Date lastUpdatedTime;

    /**
     * SpringTemplateData constructor.
     *
     * @param notificationId notificationId
     *
     * @param file file
     *
     * @param lastUpdatedTime lastUpdatedtime
     */
    public SpringTemplateData(String notificationId, byte[] file, Date lastUpdatedTime) {
        super();
        this.notificationId = notificationId;
        this.file = clone(file, 0, file.length);
        this.lastUpdatedTime = lastUpdatedTime != null ? new Date(lastUpdatedTime.getTime()) : null;
    }

    /**
     * SpringTemplateData constructor.
     */
    public SpringTemplateData() {

    }

    /**
     * Get notificationId.
     *
     * @return notificationId
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Get file.
     *
     * @return file
     */
    public byte[] getFile() {
        return file != null ? clone(file, 0, file.length) : null;
    }

    /**
     * Get lastUpdatedTime.
     *
     * @return lastUpdatedTime
     */
    public Date getLastUpdatedTime() {
        return lastUpdatedTime != null ? new Date(lastUpdatedTime.getTime()) : null;
    }

    /**
     * Clones a segment of an array of bytes.
     *
     * @param array  the array with the segment to be cloned
     * @param start  the initial position of the segment
     * @param length the length of the segment to be cloned
     * @return a new byte array filled with the elements corresponding to the specified segment
     */
    public static byte[] clone(final byte[] array, final int start, final int length) {
        final byte[] result = new byte[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

}
