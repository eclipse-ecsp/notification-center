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

package org.eclipse.ecsp.notification.config;

import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.eclipse.ecsp.nosqldao.Operator.EQ;
import static org.eclipse.ecsp.nosqldao.Operator.EQI;
import static org.eclipse.ecsp.nosqldao.Operator.IN;
import static org.eclipse.ecsp.notification.config.NotificationConfig.CONTACT_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.USER_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;

/**
 * NotificationConfigDAOMongoImpl class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
@FieldDefaults(level = PRIVATE)
public class NotificationConfigDAOMongoImpl extends IgniteBaseDAOMongoImpl<String, NotificationConfig>
    implements NotificationConfigDAO {

    static final Logger LOGGER = LoggerFactory.getLogger(NotificationConfigDAO.class);

    @Autowired
    NotificationEncryptionServiceImpl encryptionDecryptionService;


    /**
     * Sets the encryption and decryption service used for notification configuration.
     *
     * @param encryptionDecryptionService the {@link NotificationEncryptionServiceImpl}
     *                                    to use for encrypting and decrypting notification configs
     */
    public void setEncryptionDecryptionService(NotificationEncryptionServiceImpl encryptionDecryptionService) {
        this.encryptionDecryptionService = encryptionDecryptionService;
    }

    /**
     * find config by UserVehicleGroup.
     *
     * @param userIds userIds
     *
     * @param vehicleIds vehicleIds.
     *
     * @param group group
     *
     * @return list of NotificationConfigs
     */
    public List<NotificationConfig> findByUserVehicleGroup(List<String> userIds, List<String> vehicleIds,
                                                           String group) {
        IgniteQuery igQuery = null;
        for (int i = 0; i < userIds.size(); i++) {
            IgniteCriteria c1 = new IgniteCriteria("userId", EQ, userIds.get(i));
            IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c1);
            cg.and(new IgniteCriteria("vehicleId", EQ, vehicleIds.get(i)));
            cg.and(new IgniteCriteria("group", EQ, group));
            if (igQuery == null) {
                igQuery = new IgniteQuery(cg);
            } else {
                igQuery.or(cg);
            }
        }
        List<NotificationConfig> configs = super.find(igQuery);
        configs.forEach(config -> encryptionDecryptionService.decryptNotificationConfig(config));
        return configs;
    }


    /**
     * Find default config by groups and brand.
     *
     * @param groups group
     * @param brand brand
     * @return list of notificationConfigs.
     */
    @Override
    public List<NotificationConfig> findDefaultByGroupsAndBrand(Set<String> groups, String brand) {
        LOGGER.debug("Finding default config for groups {} and brand {}", groups, brand);
        IgniteCriteria groupCriterion = new IgniteCriteria("group", IN, groups);
        IgniteCriteria userCriterion = new IgniteCriteria("userId", EQ, USER_ID_FOR_DEFAULT_PREFERENCE);
        IgniteCriteria vehicleCriterion = new IgniteCriteria("vehicleId", EQ, VEHICLE_ID_FOR_DEFAULT_PREFERENCE);
        IgniteCriteria contactCriterion = new IgniteCriteria("contactId", EQ, CONTACT_ID_FOR_DEFAULT_PREFERENCE);
        IgniteCriteria brandCriterion = new IgniteCriteria("brand", EQI, brand);
        IgniteCriteriaGroup criteria = new IgniteCriteriaGroup(groupCriterion).and(userCriterion).and(vehicleCriterion)
            .and(contactCriterion).and(brandCriterion);
        return findByCriteria(criteria);
    }

    /**
     * Find notification config by user id and vehicle id.
     *
     * @param userId userId
     * @param vehicleId vehicleId
     * @return list of notificationConfigs.
     */
    @Override
    public List<NotificationConfig> findByUserVehicle(String userId, String vehicleId) {
        LOGGER.debug("finding notification config for userId {} vehicleId {} ", userId, vehicleId);
        IgniteCriteria c1 = new IgniteCriteria("userId", EQ, userId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c1).and(new IgniteCriteria("vehicleId", EQ, vehicleId));
        return findByCriteria(cg);
    }

    /**
     * Save notification config.
     *
     * @param notificationConfig notificationConfig
     * @return notificationConfig
     */
    @Override
    public NotificationConfig save(NotificationConfig notificationConfig) {
        encryptionDecryptionService.encryptNotificationConfig(notificationConfig);
        notificationConfig = super.save(notificationConfig);
        encryptionDecryptionService.decryptNotificationConfig(notificationConfig);
        return notificationConfig;
    }

    /**
     * Find notification config by user id, vehicle id and contact id.
     *
     * @param userId userId
     * @param vehicleId vehicleId
     * @param contactId contactId
     * @return list of notificationConfigs.
     */
    @Override
    public List<NotificationConfig> findByUserVehicleContactId(String userId, String vehicleId, String contactId) {
        LOGGER.debug("finding notification config for userId {} vehicleId {} ", userId, vehicleId);
        IgniteCriteria c1 = new IgniteCriteria("userId", EQ, userId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c1)
            .and(new IgniteCriteria("vehicleId", EQ, vehicleId))
            .and(new IgniteCriteria("contactId", EQ, contactId));
        return findByCriteria(cg);
    }

    /**
     * Find default config by groups.
     *
     * @param groups groups
     * @return list of notificationConfigs.
     */
    @Override
    public List<NotificationConfig> findDefaultByGroups(Set<String> groups) {
        LOGGER.debug("Finding default config for groups {}", groups);
        IgniteCriteria groupCriterion = new IgniteCriteria("group", IN, groups);
        IgniteCriteria userCriterion = new IgniteCriteria("userId", EQ, USER_ID_FOR_DEFAULT_PREFERENCE);
        IgniteCriteria vehicleCriterion = new IgniteCriteria("vehicleId", EQ, VEHICLE_ID_FOR_DEFAULT_PREFERENCE);
        IgniteCriteria contactCriterion = new IgniteCriteria("contactId", EQ, CONTACT_ID_FOR_DEFAULT_PREFERENCE);
        IgniteCriteriaGroup criteria = new IgniteCriteriaGroup(groupCriterion).and(userCriterion).and(vehicleCriterion)
            .and(contactCriterion);
        return findByCriteria(criteria);
    }

    /**
     * find configs by UserIdAndVehicleIdAndContactIdAndGroups.
     *
     * @param userId userId
     *
     * @param vehicleId vehicleId
     *
     * @param contactId contactId
     *
     * @param groups groups
     *
     * @return list of notificationConfigs.
     */
    public List<NotificationConfig> findByUserIdAndVehicleIdAndContactIdAndGroups(String userId, String vehicleId,
                                                                                  String contactId,
                                                                                  Set<String> groups) {
        IgniteCriteria groupCriterion = new IgniteCriteria("group", IN, groups);
        IgniteCriteria userCriterion = new IgniteCriteria("userId", EQ, userId);
        IgniteCriteria vehicleCriterion = new IgniteCriteria("vehicleId", EQ, vehicleId);
        IgniteCriteria contactCriterion = new IgniteCriteria("contactId", EQ, contactId);
        IgniteCriteriaGroup criteria = new IgniteCriteriaGroup(groupCriterion).and(userCriterion).and(vehicleCriterion)
            .and(contactCriterion);
        return findByCriteria(criteria);
    }

    /**
     * Find notification config by criteria.
     *
     * @param criteria criteria
     * @return list of notificationConfigs.
     */
    @NotNull
    private List<NotificationConfig> findByCriteria(IgniteCriteriaGroup criteria) {
        IgniteQuery query = new IgniteQuery(criteria);
        List<NotificationConfig> configs = super.find(query);
        configs.forEach(config -> encryptionDecryptionService.decryptNotificationConfig(config));
        return configs;
    }

    /**
     * Delete notification config by contact id and vehicle id.
     *
     * @param contactId contactId
     * @param vehicleId vehicleId
     * @return boolean
     */
    @Override
    public boolean deleteConfigForContact(String contactId, String vehicleId) {
        LOGGER.debug("deleteing  notification config for contactid {} and vehicleId {}", contactId, vehicleId);
        IgniteCriteria contactCriterion = new IgniteCriteria("contactId", EQ, contactId);
        IgniteCriteria vehicleCriterion = new IgniteCriteria("vehicleId", EQ, vehicleId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(contactCriterion).and(vehicleCriterion);
        IgniteQuery igQuery = new IgniteQuery(cg);
        super.find(igQuery).forEach(config -> super.deleteById(config.getId()));
        return true;
    }

    /**
     * Delete notification config by user id and vehicle id.
     *
     * @param userId userId
     * @param vehicleId vehicleId
     * @return boolean
     */
    @Override
    public boolean deleteNotificationConfigByUserAndVehicle(String userId, String vehicleId) {
        LOGGER.debug("deleteNotificationConfigByUserAndVehicle for userId {} vehicleId {} ", userId, vehicleId);
        IgniteCriteria c1 = new IgniteCriteria("userId", EQ, userId);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c1);
        cg.and(new IgniteCriteria("vehicleId", EQ, vehicleId));
        IgniteQuery igQuery = new IgniteQuery(cg);
        int count = super.deleteByQuery(igQuery);
        return count != 0;
    }
}
