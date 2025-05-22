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

package org.eclipse.ecsp.notification.client;

/**
 * IgniteCoreConstants class.
 */
public interface IgniteCoreConstants {

    String RESULTS_KEY = "results";
    String FIRST_NAME = "firstName";
    String LAST_NAME = "lastName";
    String EMAIL = "email";
    String PHONE_NUMBER = "phoneNumber";
    String LOCALE = "locale";
    String FILTER_BY_SINGLE_USERNAME_URI = "/v1/users/filter?pageNumber=0&sortOrder=DESC";
    String APPLICATION_JSON = "application/json";
    String USER_CONSENT = "notificationConsent";
    String TIMEZONE = "timeZone";
    String CORRELATION_ID = "CorrelationId";
    String WSO2_IDAM = "WSO2";
    String UIDAM = "UIDAM";
}
