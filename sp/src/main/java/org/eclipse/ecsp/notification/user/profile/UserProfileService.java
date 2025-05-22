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

package org.eclipse.ecsp.notification.user.profile;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.CreateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.DeleteSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.NotificationUserProfileEventDataV1_0;
import org.eclipse.ecsp.domain.notification.UpdateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Locale;
import java.util.Set;

/**
 * UserProfileService class to fetch user details.
 */
@Service
public class UserProfileService {
    @Autowired
    private UserProfileDAO profileDao;

    @Autowired
    private SecondaryContactDAO secondaryContactDao;

    @Autowired
    private NotificationConfigDAO notificationConfigDao;

    /**
     * Method to update user profile.
     *
     * @param igniteEvent igniteEvent
     */
    public void updateUserProfile(IgniteEvent igniteEvent) {
        UserProfile profile = ((NotificationUserProfileEventDataV1_0) igniteEvent.getEventData()).getUserProfile();
        updateUserProfile(profile);
    }

    /**
     * Method to update user profile.
     *
     * @param profile UserProfile to be updated
     * @return Updated user profile
     */
    public UserProfile updateUserProfile(UserProfile profile) {
        UserProfile userProfile = profileDao.findById(profile.getUserId());
        if (userProfile != null) {
            populateUserInfo(profile, userProfile);
        } else {
            userProfile = profile;
        }
        userProfile.setLastModifiedTime(System.currentTimeMillis());
        return profileDao.save(userProfile);
    }

    private void populateUserInfo(UserProfile profile, UserProfile userProfile) {
        String firstName = profile.getFirstName();
        if (StringUtils.isNotEmpty(firstName)) {
            userProfile.setFirstName(firstName);
        }
        String lastName = profile.getLastName();
        if (StringUtils.isNotEmpty(lastName)) {
            userProfile.setLastName(lastName);
        }
        String defaultEmail = profile.getDefaultEmail();
        if (defaultEmail != null) {
            userProfile.setDefaultEmail(defaultEmail);
        }
        String defaultPh = profile.getDefaultPhoneNumber();
        if (defaultPh != null) {
            userProfile.setDefaultPhoneNumber(defaultPh);
        }
        Locale locale = profile.getLocale();
        if (null != locale) {
            userProfile.setLocale(locale);
        }
        if (!CollectionUtils.isEmpty(profile.getCustomAttributes())) {
            userProfile.setCustomAttributes(profile.getCustomAttributes());
        }
        Set<NickName> nickNameSet = profile.getNickNames();
        if (!CollectionUtils.isEmpty(nickNameSet)) {
            userProfile.setNickNames(nickNameSet);
        }
    }

    public void createSecondaryContact(IgniteEvent igniteEvent) {
        secondaryContactDao.save(
                ((CreateSecondaryContactEventDataV1_0) igniteEvent.getEventData()).getSecondaryContact());
    }

    /**
     * Update Secondary contact details.
     *
     * @param igniteEvent event containing secondary contact details
     */
    public void updateSecondaryContact(IgniteEvent igniteEvent) {
        secondaryContactDao.update(
                ((UpdateSecondaryContactEventDataV1_0) igniteEvent.getEventData()).getSecondaryContact());
    }

    /**
     * Method to delete a secondary contact.
     *
     * @param igniteEvent event passing contact id and vin
     */
    public void deleteSecondaryContact(IgniteEvent igniteEvent) {
        String contactId =
                ((DeleteSecondaryContactEventDataV1_0) igniteEvent.getEventData()).getSecondaryContact().getContactId();
        String vehicleId =
                ((DeleteSecondaryContactEventDataV1_0) igniteEvent.getEventData()).getSecondaryContact().getVehicleId();

        secondaryContactDao
                .deleteById(contactId);
        notificationConfigDao.deleteConfigForContact(contactId, vehicleId);
    }

    public void deleteUserVehicleNickNames(String userId, String vechileId) {
        profileDao.removeNickNames(userId, vechileId);
    }

    // Added for 163635: Delete the user if user deactivated it
    public void deleteUserProfile(String uid) {
        profileDao.deleteById(uid);
    }

}