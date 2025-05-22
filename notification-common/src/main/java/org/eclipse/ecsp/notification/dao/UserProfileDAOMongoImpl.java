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

import com.amazonaws.encryptionsdk.exception.AwsCryptoException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.DecoderException;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Locale;

/**
 * UserProfileDAOMongoImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Profile("!ignite-user-profile")
@Repository
@Slf4j
public class UserProfileDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, UserProfile>
    implements UserProfileDAO {

    @Autowired
    private EncryptDecryptInterface encryptDecryptInterface;

    /**
     * Init method.
     */
    @PostConstruct
    public void initInfo() {
        log.info("Initiated UserProfileDAOMongoImpl. The application will use Notification Center UserProfile table.");
    }

    /**
     * Update nick name.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @param nickName  the nick name
     * @return the user profile
     */
    @Override
    public UserProfile updateNickName(String userId, String vehicleId, String nickName) {

        UserProfile userProfile = findById(userId);
        if (null == userProfile) {
            userProfile = new UserProfile(userId);
            userProfile.addNickName(new NickName(nickName, vehicleId));
        } else {
            userProfile.addNickName(new NickName(nickName, vehicleId));
        }
        userProfile.setLastModifiedTime(System.currentTimeMillis());
        return save(userProfile);
    }

    /**
     * Update consent.
     *
     * @param userId  the user id
     * @param consent the consent
     * @return the user profile
     */
    @Override
    public UserProfile updateConsent(String userId, boolean consent) {

        UserProfile userProfile = findById(userId);
        if (null == userProfile) {
            userProfile = new UserProfile(userId);
            userProfile.setLocale(Locale.US);
        }
        userProfile.setConsent(consent);
        userProfile.setLastModifiedTime(System.currentTimeMillis());
        return save(userProfile);
    }

    /**
     * Remove nick names.
     *
     * @param userId    the user id
     * @param vehicleId the vehicle id
     * @return the user profile
     */
    @Override
    public UserProfile removeNickNames(String userId, String vehicleId) {
        UserProfile userProfile = findById(userId);
        if (userProfile != null) {
            userProfile.removeNickNames(vehicleId);
            userProfile.setLastModifiedTime(System.currentTimeMillis());
            return save(userProfile);
        }
        return null;
    }

    /**
     * Save method.
     *
     * @param userProfile userprofile
     *
     * @return userprofile
     */
    @Override
    public UserProfile save(UserProfile userProfile) {
        encryptUserProfile(userProfile);
        return decryptUserProfile(super.save(userProfile));
    }

    /**
     * Find by id method.
     *
     * @param id id
     *
     * @return userprofile
     */
    @Override
    public UserProfile findById(String id) {
        UserProfile profile = super.findById(id);
        if (profile != null) {
            return decryptUserProfile(profile);
        }
        return null;
    }

    /**
     * encryptUserProfile method.
     *
     * @param userProfile userprofile
     */
    public void encryptUserProfile(UserProfile userProfile) {
        if (null != userProfile.getFirstName()) {
            userProfile.setFirstName(encryptDecryptInterface.encrypt(userProfile.getFirstName()));
        }
        if (null != userProfile.getDefaultEmail()) {
            userProfile.setDefaultEmail(encryptDecryptInterface.encrypt(userProfile.getDefaultEmail()));
        }
        if (null != userProfile.getDefaultPhoneNumber()) {
            userProfile.setDefaultPhoneNumber(encryptDecryptInterface.encrypt(userProfile.getDefaultPhoneNumber()));
        }
        if (null != userProfile.getLastName()) {
            userProfile.setLastName(encryptDecryptInterface.encrypt(userProfile.getLastName()));
        }
    }

    /**
     * decryptUserProfile method.
     *
     * @param userProfile userprofile.
     *
     * @return decrypted userprofile
     */
    public UserProfile decryptUserProfile(UserProfile userProfile) {
        if (null != userProfile.getFirstName()) {
            userProfile.setFirstName(encryptDecryptInterface.decrypt(userProfile.getFirstName()));
        }
        if (null != userProfile.getDefaultEmail()) {
            userProfile.setDefaultEmail(encryptDecryptInterface.decrypt(userProfile.getDefaultEmail()));
        }
        if (null != userProfile.getDefaultPhoneNumber()) {
            userProfile.setDefaultPhoneNumber(encryptDecryptInterface.decrypt(userProfile.getDefaultPhoneNumber()));
        }
        if (null != userProfile.getLastName()) {
            try {
                userProfile.setLastName(encryptDecryptInterface.decrypt(userProfile.getLastName()));
            } catch (AwsCryptoException e) {
                // Last name wasn't encrypted in DB in previous versions.
                // For old user profiles, lastName can't be decrypted so leave it as is
            } catch (DecoderException e) {
                // Handle Base64 decoding issues
                // Reported in WI 697893
                //The reason is in older version of aws encryption sdk
                // base64 class was used from jdk
                // while in latest it is being used from bountycastle.
                log.error("Failed to decode Base64 string for lastName: {}", userProfile.getLastName(), e);
                // Last name wasn't encrypted in DB in previous versions.
                // For old user profiles, lastName can't be decrypted so leave it as is


            }
        }
        return userProfile;
    }

}