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

package org.eclipse.ecsp.notification.dao;

import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;

import java.util.List;

/**
 * SecondaryContactDAO interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface SecondaryContactDAO extends IgniteBaseDAO<String, SecondaryContact> {

    /**
     * Get contact ids.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @return the list
     */
    List<String> getContactIds(String userId, String vehicleId);

    /**
     * Get contacts.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @return the list
     */
    List<SecondaryContact> getContacts(String userId, String vehicleId);
}
