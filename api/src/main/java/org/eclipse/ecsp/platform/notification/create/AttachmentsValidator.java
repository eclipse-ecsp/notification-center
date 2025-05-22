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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Spring validator for sending notifications.
 * The validator validate the payload of the request.
 * Any payload that does not contains attachments is valid.
 * Payload that contains attachments must contains a file name and content
 */
@Service
public class AttachmentsValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentsValidator.class);
    private static final String BAD_REQUEST = "400";
    private static final String ATTACHMENTS_KEY = "attachments";
    private static final int ONE_THOUSAND_TWENTY_FOUR = 1024;
    public static final String INVALID = "invalid";

    @Autowired
    private ObjectMapper mapper;

    /**
     * Max size of all attachments in MB.
     */
    @Value("${app_attachments_max-size:10}")
    private int maxSize;

    @Value("${app_attachments_max_count:5}")
    private int attachmentsMaxCount;

    @SuppressWarnings("checkstyle:ParameterName")
    @Override
    public boolean supports(Class<?> aClass) {
        return NotificationCreationRequest.class.equals(aClass);
    }

    /**
     * Validate the request.
     * The request is valid if it does not contains attachments.
     * If the request contains attachments, the attachments must contains a file name and content.
     *
     * @param request the request to validate
     * @param errors  the errors
     */
    @Override
    public void validate(Object request, Errors errors) {
        Map<String, Object> data = ((NotificationCreationRequest) request).getData();

        // Data can be sent without any attachment
        if (!data.containsKey(ATTACHMENTS_KEY)) {
            return;
        } else if (!(data.get(ATTACHMENTS_KEY) instanceof List)) {

            LOGGER.warn("Received NotificationCreationRequest with invalid attachments data");
            errors.rejectValue("data", INVALID, "attachments.is.not.collection");
            return;
        }
        List<?> attachmentsRawData = (List<?>) data.get(ATTACHMENTS_KEY);
        if (CollectionUtils.isEmpty(attachmentsRawData)) {
            return;
        }

        if (attachmentsRawData.size() > attachmentsMaxCount) {
            LOGGER.warn("Attachment validation failed, received: {}, max attachment is: {}", attachmentsRawData.size(),
                attachmentsMaxCount);
            errors.rejectValue("data", INVALID, "attachments.exceeded." + attachmentsMaxCount);
            return;
        }

        double attachmentsSize = 0;
        List<AttachmentData> attachments;
        try {

            /*
            When sending notifications with attachments, the 'client' (might be other backend)
            which sends the notification
            might send base 64 string instead of byte array, because the structure of attachment
            is generic and Jackson cant detect the real type of the content.
             */
            attachments = new ArrayList<>(attachmentsRawData.stream().map(o -> {
                try {
                    byte[] attachmentByteArray = mapper.writeValueAsBytes(o);
                    return mapper.readValue(attachmentByteArray, AttachmentData.class);
                } catch (IOException e) {
                    LOGGER.warn("Failed to read attachment");
                    Collection<ResponseWrapper.Message> error = new ArrayList<>();
                    error.add(ResponseWrapper.Message.of(BAD_REQUEST, e.getMessage(), "Failed to read attachment"));

                    throw new InvalidInputException(error);
                }
            }).toList());
        } catch (Exception e) {
            errors.rejectValue("data", INVALID, "attachment.structure.invalid");
            return;
        }

        validateAttachments(errors, attachments, attachmentsSize);
    }

    /**
     * Validate the attachments.
     *
     * @param errors          the errors
     * @param attachments     the attachments
     * @param attachmentsSize the attachments size
     */
    private void validateAttachments(Errors errors, List<AttachmentData> attachments, double attachmentsSize) {
        for (AttachmentData currentItem : attachments) {
            if (!StringUtils.hasText(currentItem.getFileName())) {
                LOGGER.warn("Validation failed, attachment file name must not be empty");
                errors.rejectValue("data", INVALID, "attachment.name.empty");
                return;
            }
            if (!StringUtils.hasText(currentItem.getMimeType())) {
                LOGGER.warn("Validation failed, attachment mime type must not be empty");
                errors.rejectValue("data", INVALID, "attachment.type.empty");
                return;
            }
            if (currentItem.getContent() == null) {
                errors.rejectValue("data", INVALID, "attachment.content.empty");
                return;
            }
            attachmentsSize += currentItem.getContent().length;
            double sizeInmb = attachmentsSize / ONE_THOUSAND_TWENTY_FOUR / ONE_THOUSAND_TWENTY_FOUR;

            if (sizeInmb > maxSize) {
                errors.rejectValue("data", INVALID, "attachment.content.exceeded." + maxSize);
                return;
            }
        }
    }

    //for unit test
    void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    //for unit test
    void setAttachmentsMaxCount(int attachmentsMaxCount) {
        this.attachmentsMaxCount = attachmentsMaxCount;
    }

    //for unit test
    void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}
