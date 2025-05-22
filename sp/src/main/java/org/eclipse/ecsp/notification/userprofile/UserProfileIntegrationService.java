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

package org.eclipse.ecsp.notification.userprofile;

import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.entities.IgniteEvent;

/**
 * Abstraction for User Profile Integration service.
 */
public interface UserProfileIntegrationService {
    /**
     * Process Webhook event notification.
     *
     * @param webHookEvent the given webhook event
     */
    void processWebHookNotification(IgniteEvent webHookEvent);

    /**
     * Process Real Time User Update.
     *
     * @param uid the given user id
     * @param persistUserProfile the given persist user profile flag
     * @return the user profile
     */
    UserProfile processRealTimeUserUpdate(String uid, boolean persistUserProfile);

    /**
     * Process Real Time User Update.
     *
     * @param uid the given user id
     * @param vehicleProfileAbridged the given vehicle profile abridged
     * @param persistUserProfile the given persist user profile flag
     * @return the user profile
     */
    default UserProfile processRealTimeUserUpdate(String uid, VehicleProfileAbridged vehicleProfileAbridged,
                                                  boolean persistUserProfile) {
        throw new UnsupportedOperationException();
    }

}
