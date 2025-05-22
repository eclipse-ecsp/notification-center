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

import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;

/**
 * UserProfileDAO interface.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface UserProfileDAO extends IgniteBaseDAO<String, UserProfile> {
    /**
     * Update nick name.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @param NickName  the nick name
     * @return the user profile
     */
    @SuppressWarnings("checkstyle:ParameterName")
    UserProfile updateNickName(String userId, String vehicleId, String NickName);

    /**
     * Update consent.
     *
     * @param userId   the user id
     * @param consent the consent
     * @return the user profile
     */
    UserProfile updateConsent(String userId, boolean consent);

    /**
     * Remove nick names.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @return the user profile
     */
    UserProfile removeNickNames(String userId, String vehicleId);
}
