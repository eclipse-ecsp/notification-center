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
import org.apache.commons.codec.binary.Base64;

/**
 * EmailAttachment class.
 */
@Entity(useDiscriminator = false)
public class EmailAttachment {
    private String fileName;
    private byte[] content;
    private String mimeType;
    private boolean inline;

    /**
     * EmailAttachment constructor.
     */
    public EmailAttachment() {
        super();
    }

    /**
     * EmailAttachment constructor.
     *
     * @param fileName filename
     *
     * @param content attachment content
     *
     * @param mimeType contenttype
     *
     * @param inline boolean
     */
    public EmailAttachment(String fileName, String content, String mimeType, boolean inline) {
        super();
        this.fileName = fileName;
        this.content = Base64.decodeBase64(content);
        this.mimeType = mimeType;
        this.inline = inline;
    }

    /**
     * This method is a getter for filename.
     *
     * @return String
     */

    public String getFileName() {
        return fileName;
    }

    // To avoid returning the mutable array reference , returning a base64
    // encoded string

    /**
     * This method is a getter for content.
     *
     * @return String
     */
    public String getContent() {
        return Base64.encodeBase64String(content);
    }

    /**
     * This method is a getter for mimetype.
     *
     * @return String
     */

    public String getMimeType() {
        return mimeType;
    }

    /**
     * This method is a getter for inline.
     *
     * @return boolean
     */
    public boolean isInline() {
        return inline;
    }

}