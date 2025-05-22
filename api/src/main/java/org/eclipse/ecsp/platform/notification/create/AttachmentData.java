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

package org.eclipse.ecsp.platform.notification.create;

/**
 * Class used to help validate.
 * {@link org.eclipse.ecsp.domain.notification.NotificationCreationRequest NotificationCreationRequest}.
 */
public class AttachmentData {
    private String fileName;
    private byte[] content;
    private String mimeType;

    private AttachmentData() {

    }

    /**
     * Get File Name.
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set File Name.
     *
     * @param fileName file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get attachment content.
     *
     * @return content
     */
    public byte[] getContent() {
        return content != null ? clone(content, 0, content.length) : null;
    }

    /**
     * Set attachment content.
     *
     * @param content content
     */
    public void setContent(byte[] content) {
        if (content != null) {
            this.content = clone(content, 0, content.length);
        }
    }

    /**
     * Get MIME type.
     *
     * @return MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Set MIME type.
     *
     * @param mimeType MIME type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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
