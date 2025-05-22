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

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.nosqldao.IgnitePagingInfoResponse;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Updates;
import org.eclipse.ecsp.notification.client.IgniteCoreUserManagementClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * UserManagementToUserProfileDAO class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Profile("ignite-user-profile")
@Service
@Slf4j
public class UserManagementToUserProfileDAO implements UserProfileDAO {
    @Autowired
    IgniteCoreUserManagementClient igniteCoreUserManagementClient;

    @Setter(onMethod_ = {@Value("${ignite_core_federated_prefix:}")})
    String federatedPrefix;

    /**
     * init method to initialise UserManagementToUserProfileDAOImpl.
     */
    @PostConstruct
    public void init() {
        log.info("Initiated UserManagementToUserProfileDAOImpl. The application will use Ignite Core User Management.");
        if (federatedPrefix != null && !federatedPrefix.isEmpty() && !federatedPrefix.contains("_")) {
            federatedPrefix += "_";
            log.debug("Following prefix will be added {}", federatedPrefix);
        }
    }

    /**
     * Method to update user profile.
     *
     * @param userId    userId
     * @param vehicleId vehicleId
     * @param NickName  NickName
     * @return UserProfile
     */
    @SuppressWarnings("checkstyle:ParameterName")
    @Override
    public UserProfile updateNickName(String userId, String vehicleId, String NickName) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to update user profile.
     *
     * @param userId  userId
     * @param consent consent
     * @return UserProfile
     */
    @Override
    public UserProfile updateConsent(String userId, boolean consent) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to remove nick names.
     *
     * @param userId    userId
     * @param vehicleId vehicleId
     * @return UserProfile
     */
    @Override
    public UserProfile removeNickNames(String userId, String vehicleId) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to remove nick names.
     *
     * @param userId userId
     * @return UserProfile
     */
    @Override
    public UserProfile findById(String userId) {
        if (userId == null) {
            return null;
        }
        log.debug("Invoking core user management client with user {}", userId);
        UserProfile userProfile = igniteCoreUserManagementClient.getUser(userId, federatedPrefix).orElse(null);

        log.debug("Got user {}", userProfile);
        return userProfile;
    }

    //region ...not implemented methods from interface

    /**
     * Method to save user profile.
     *
     * @param userProfile userProfile
     * @return UserProfile
     */
    @Override
    public UserProfile save(UserProfile userProfile) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /*    @Override
public UserProfile save(UserProfile userProfile, String... strings) {
throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
}*/

    /**
     * Method to save all user profiles.
     *
     * @param userProfiles userProfiles
     * @return List of UserProfile
     */
    @Override
    public List<UserProfile> saveAll(UserProfile... userProfiles) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Find by ids method.
     *
     * @param strings strings
     * @return List of UserProfile
     */
    @Override
    public List<UserProfile> findByIds(String... strings) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Find by ids method.
     *
     * @return List of UserProfile
     */
    @Override
    public List<UserProfile> findAll() {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Find by ids method.
     *
     * @param igniteQuery igniteQuery
     * @return List of UserProfile
     */
    @Override
    public List<UserProfile> find(IgniteQuery igniteQuery) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Find With Paging Info method.
     *
     * @param igniteQuery igniteQuery
     * @return IgnitePagingInfoResponse UserProfile
     */
    @Override
    public IgnitePagingInfoResponse<UserProfile> findWithPagingInfo(IgniteQuery igniteQuery) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Delete by id method.
     *
     * @param s s
     * @return boolean
     */
    @Override
    public boolean deleteById(String s) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Delete by ids method.
     *
     * @param strings strings
     * @return int
     */
    @Override
    public int deleteByIds(String... strings) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Delete by query method.
     *
     * @param igniteQuery igniteQuery
     * @return int
     */
    @Override
    public int deleteByQuery(IgniteQuery igniteQuery) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Delete all method.
     *
     * @return boolean
     */
    @Override
    public boolean deleteAll() {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to update user profile.
     *
     * @param userProfile userProfile
     * @return boolean
     */
    @Override
    public boolean update(UserProfile userProfile) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to update user profile.
     *
     * @param s       s
     * @param updates updates
     * @return boolean
     */
    @Override
    public boolean update(String s, Updates updates) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to update user profile.
     *
     * @param igniteQuery igniteQuery
     * @param updates     updates
     * @return boolean
     */
    @Override
    public boolean update(IgniteQuery igniteQuery, Updates updates) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to update all user profiles.
     *
     * @param userProfiles userProfiles
     * @return boolean[]
     */
    @Override
    public boolean[] updateAll(UserProfile... userProfiles) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * upsert method.
     *
     * @param igniteQuery igniteQuery
     * @param userProfile userProfile
     * @return boolean
     */
    @Override
    public boolean upsert(IgniteQuery igniteQuery, UserProfile userProfile) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Remove all method.
     *
     * @param igniteQuery igniteQuery
     * @param updates    updates
     * @return boolean
     */
    @Override
    public boolean removeAll(IgniteQuery igniteQuery, Updates updates) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Delete method.
     *
     * @param userProfile userProfile
     * @return boolean
     */
    @Override
    public boolean delete(UserProfile userProfile) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to find distinct values.
     *
     * @param igniteQuery
     *            IgniteQuery
     * @param s
     *            field name for which to find distinct values
     * @return List of String
     */
    @Override
    public List<String> distinct(IgniteQuery igniteQuery, String s) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to count by query.
     *
     * @param igniteQuery
     *            IgniteQuery
     * @return long
     */
    @Override
    public long countByQuery(IgniteQuery igniteQuery) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to stream find.
     *
     * @param igniteQuery
     *            IgniteQuery
     * @return Flux of UserProfile
     */
    @Override
    public Flux<UserProfile> streamFind(IgniteQuery igniteQuery) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to count all.
     *
     * @return long
     */
    @Override
    public long countAll() {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to stream find all.
     *
     * @return Flux of UserProfile
     */
    @Override
    public Flux<UserProfile> streamFindAll() {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to check if collection exists.
     *
     * @param s
     *            collection name
     * @return boolean
     */
    @Override
    public boolean collectionExists(String s) {
        throw new RuntimeException("This method is not implemented for \"ignite-user-profile\" profile");
    }

    /**
     * Method to get collection name.
     *
     * @return String
     */
    @Override
    public boolean getAndUpdate(UserProfile userProfile) {
        throw new UnsupportedOperationException("This method is not implemented for \"ignite-user-profile\" profile");
    }
    //endregion
}
