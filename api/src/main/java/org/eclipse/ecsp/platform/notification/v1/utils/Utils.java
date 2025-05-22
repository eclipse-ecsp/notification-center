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

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Utils java.
 */
public class Utils {

    private Utils()
      {}

    /** Default pagination size. */
    public static final String DEFAULT_PAGINATION_SIZE = "600";
    /** Default pagination page. */
    public static final String DEFAULT_PAGINATION_PAGE = "1";

    /**
     * Validation message.
     */
    public static final String VALIDATION_MESSAGE = "received either null or empty";

    private static final String VALID_EMAIL =
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String VALID_SMS = "^\\+?[0-9]{10,13}$";
    private static final String VALID_PORTAL = "^[a-zA-Z0-9\\/]+\\/notification$";

    /**
     * isValidEmail method.
     *
     * @param sting sting
     * @return boolean
     */
    public static boolean isValidEmail(String sting) {
        return isMatches(sting, VALID_EMAIL);
    }

    /**
     * isValidSms method.
     *
     * @param sting sting
     * @return boolean
     */
    public static boolean isValidSms(String sting) {
        return isMatches(sting, VALID_SMS);
    }

    /**
     * isValidPortal method.
     *
     * @param sting sting
     * @return boolean
     */
    public static boolean isValidPortal(String sting) {
        return isMatches(sting, VALID_PORTAL);
    }

    /**
     *  Is Valid Pottern.
     *
     * @param sting sting
     * @param pattern pattern
     * @return boolean
     */
    private static boolean isMatches(String sting, String pattern) {
        return sting != null && pattern != null && sting.matches(pattern);
    }

    /**
     * Generate Random Id.
     *
     * @param field field
     * @return string
     */
    public static String generateRandomId(String field) {
        return StringUtils.isBlank(field) ? UUID.randomUUID().toString() : field;
    }

}