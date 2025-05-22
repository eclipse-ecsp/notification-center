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

package org.eclipse.ecsp.notification.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.notification.adaptor.AbstractChannelNotifier;
import org.eclipse.ecsp.notification.adaptor.NotificationEventFields.ChannelType;
import org.eclipse.ecsp.notification.entities.EmailAttachment;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EmailNotifier class.
 */
public abstract class EmailNotifier extends AbstractChannelNotifier {

    private static final String CHANNEL_TYPE = ChannelType.EMAIL.getChannelTypeName();
    /**
     * Mapper.
     */
    protected ObjectMapper mapper;

    /**
     * EmailNotifier Set Mapper.
     *
     * @param mapper the given mapper
     */
    @Autowired
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Get Protocol.
     *
     * @return String
     */
    @Override
    public String getProtocol() {
        return CHANNEL_TYPE;
    }

    /**
     * Get Attachments From Optional Data.
     *
     * @param obj Object
     * @return List
     */
    protected List<EmailAttachment> getAttachmentsFromOptionalData(Object obj) {
        List<EmailAttachment> emailAttachments = new ArrayList<>();
        if (obj == null) {
            return emailAttachments;
        }

        List<Object> allAttachments = (List<Object>) obj;

        for (Object attachment : allAttachments) {
            EmailAttachment emailAttachment;
            try {
                byte[] attachmentAsByteArray = mapper.writeValueAsBytes(attachment);
                emailAttachment = mapper.readValue(attachmentAsByteArray, EmailAttachment.class);
            } catch (IOException e) {

                throw new IllegalArgumentException(e.getMessage());
            }
            emailAttachments.add(emailAttachment);
        }
        return emailAttachments;
    }
}
