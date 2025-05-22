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

package org.eclipse.ecsp.notification.annotation;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validate version number against versions defined in.
 * org.eclipse.ecsp.domain.Version in entities project
 *
 * @author JDEHURY
 */
@Component
public class VersionValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionValidator.class);

    /**
     * This method checks if the class is supported for validation.
     *
     * @param clazz class to be validated
     * @return boolean
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return NotificationCreationRequest.class.equals(clazz);
    }

    /**
     * This method validates the version number.
     *
     * @param target object to be validated
     * @param errors errors
     */
    @Override
    public void validate(Object target, Errors errors) {
        String version = ((NotificationCreationRequest) target).getVersion();
        LOGGER.debug("Validating the version : {}", version);
        if (StringUtils.isBlank(version)) {
            errors.rejectValue(NotificationConstants.VERSION, "empty",
                NotificationConstants.MESSAGE_VERSION_NUMBER_EMPTY);
        } else {
            boolean isValidVersion = false;
            for (Version v : Version.values()) {
                if (v.getValue().equals(version)) {
                    isValidVersion = true;
                }
            }
            if (!isValidVersion) {
                errors.rejectValue(NotificationConstants.VERSION, "invalid",
                    NotificationConstants.MESSAGE_VERSION_NUMBER_NOT_VALID);
            }

        }

    }
}