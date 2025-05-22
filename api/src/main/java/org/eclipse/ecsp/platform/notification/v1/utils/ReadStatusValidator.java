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

package org.eclipse.ecsp.platform.notification.v1.utils;

import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;

/**
 * ReadStatusValidator class.
 */
public class ReadStatusValidator {

    private ReadStatusValidator()
      {}

    private static final String ALL = "all";
    private static final String READ = "read";
    private static final String UNREAD = "unread";

    private static final String[] ALLOWED_READ_STATUS = {ALL, READ, UNREAD};

    /**
     * Validate the status.
     *
     * @param status status
     */
    public static void validate(String status) {
        for (String allowed : ALLOWED_READ_STATUS) {
            if (allowed.equalsIgnoreCase(status)) {
                return;
            }
        }
        throw new NoSuchEntityException(ResponseMsgConstants.UNSUPPORTED_API_READ_MSG);
    }

    /**
     * COnvert readstatus string.
     *
     * @param readStatus status
     *
     * @return readstatus object
     */
    public static Object convert(String readStatus) {
        if (null == readStatus || ALL.equalsIgnoreCase(readStatus)) {
            return null;
        } else if (READ.equalsIgnoreCase(readStatus)) {
            return true;
        } else if (UNREAD.equalsIgnoreCase(readStatus)) {
            return false;
        }
        throw new NoSuchEntityException(ResponseMsgConstants.UNSUPPORTED_API_READ_MSG);
    }
}
