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

package org.eclipse.ecsp.domain.notification.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.notification.config.NotificationConfig.CONTACT_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.USER_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;

/**
 * NotificationConfigCommonService class.
 */
@Slf4j
@Service
public class NotificationConfigCommonService {

    private final NotificationConfigDAO notificationConfigDao;

    private final SecondaryContactDAO secondaryContactDao;

    @Value("${locale.default.value}")
    private String defaultLocale;

    @Value("${brand.default.value:default}")
    private String defaultBrand;

    /**
     * NotificationConfigCommonService constructor.
     *
     * @param notificationConfigDao notificationConfigDao
     *
     * @param secondaryContactDao secondaryContactDao
     */
    public NotificationConfigCommonService(NotificationConfigDAO notificationConfigDao,
                                           SecondaryContactDAO secondaryContactDao) {
        this.notificationConfigDao = notificationConfigDao;
        this.secondaryContactDao = secondaryContactDao;
    }

    /**
     * getAllConfigsFromDbByGroup.
     *
     * @param userId userId string
     *
     * @param vehicleId vehicleId String
     *
     * @param group group string
     *
     * @return List of NotificationConfig
     */
    public List<NotificationConfig> getAllConfigsFromDbByGroup(String userId, String vehicleId, String group) {
        List<String> fetchUserIds = new ArrayList<>();
        List<String> fetchVehicleIds = new ArrayList<>();
        populateUserIdVehicleIdPairs(fetchUserIds, fetchVehicleIds, userId, vehicleId);
        log.debug("getAllConfigsFromDbByGroup userId: {}, vehicleId: {}, group: {}", userId, vehicleId, group);
        return notificationConfigDao.findByUserVehicleGroup(fetchUserIds, fetchVehicleIds, group);
    }

    /**
     * Populate user id and vehicle id pairs.
     *
     * @param fetchUserIds fetchUserIds
     * @param fetchVehicleIds fetchVehicleIds
     * @param userId userId
     * @param vehicleId vehicleId
     */
    private void populateUserIdVehicleIdPairs(List<String> fetchUserIds, List<String> fetchVehicleIds, String userId,
                                              String vehicleId) {
        // add user id and default vehicle id
        if (!StringUtils.isEmpty(userId)) {
            fetchUserIds.add(userId);
            fetchVehicleIds.add(VEHICLE_ID_FOR_DEFAULT_PREFERENCE);


            // add user id and specific vehicle id if specific vehicle id exists
            if (!StringUtils.isEmpty(vehicleId)) {
                fetchVehicleIds.add(vehicleId);
                fetchUserIds.add(userId);
            }
        }

        // default notification config
        fetchUserIds.add(USER_ID_FOR_DEFAULT_PREFERENCE);
        fetchVehicleIds.add(VEHICLE_ID_FOR_DEFAULT_PREFERENCE);
    }

    /**
     * getSecondaryContactsConfig.
     *
     * @param userId userId
     *
     * @param vehicleId vehicleId
     *
     * @param configs configs
     *
     * @param brand brand
     *
     * @return List of NotificationConfig
     */
    public List<NotificationConfig> getSecondaryContactsConfig(String userId, String vehicleId,
                                                               List<NotificationConfig> configs, String brand) {
        List<NotificationConfig> result = new ArrayList<>();

        if (!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(vehicleId)) {
            Set<String> contactsWithConfig =
                configs.stream().map(NotificationConfig::getContactId).collect(Collectors.toSet());
            List<SecondaryContact> secondaryContactList = secondaryContactDao.getContacts(userId, vehicleId).stream()
                .filter(contact -> !contactsWithConfig.contains(contact.getContactId()))
                .collect(Collectors.toList());

            if (!secondaryContactList.isEmpty()) {
                Optional<NotificationConfig> defaultNotificationConfig = findDefaultConfigByBrand(configs, brand);
                defaultNotificationConfig.ifPresent(notificationConfig -> secondaryContactList.forEach(sc -> {
                    NotificationConfig nc = notificationConfig.deepClone();
                    nc.setContactId(sc.getContactId());
                    nc.setUserId(userId);
                    nc.setVehicleId(vehicleId);
                    nc.setLocale(hasLocale(sc.getLocale()) ? sc.getLocale().toLanguageTag() : defaultLocale);
                    result.add(nc);
                }));
            }
        }

        return result;
    }

    /**
     *<p>This method returns NotificationConfigs for primary and secondary contacts.
     * API push default channel should be merged for only primary preference means contactId is self</p>
     *
     * @param configs          config
     * @param vehicleId        vehicle id
     * @param alertUserProfile user profile
     * @param brand            brand
     * @return notification config list
     */
    public List<NotificationConfig> selectNotificationConfig(List<NotificationConfig> configs, String vehicleId,
                                                             UserProfile alertUserProfile,
                                                             String brand) {
        log.debug("Selecting notification config from: {}", configs);
        String userId = alertUserProfile != null ? alertUserProfile.getUserId() : null;
        List<NotificationConfig> userConfigs = new ArrayList<>();
        // Check vehicleId, otherwise use GENERAL for user notification
        String vehicleIdToUse = (StringUtils.isEmpty(vehicleId)) ? VEHICLE_ID_FOR_DEFAULT_PREFERENCE : vehicleId;

        // Get default config
        Optional<NotificationConfig> generalConfig = findDefaultConfigByBrand(configs, brand);
        // If default config (GENERAL / GENERAL) does not exist return empty
        if (!generalConfig.isPresent()) {
            log.error(
                "Failed to find default config for group {}, notification will not "
                    + "be sent for userId: {}, vehicleId: {}",
                configs.get(0).getGroup(), userId, vehicleIdToUse);
            return userConfigs;
        }
        NotificationConfig defaultConfig = generalConfig.get();
        log.debug("Default notification config: {}", defaultConfig);

        // Get primary contact config "self"
        if (userId != null) {
            getPrimaryContactConfig(configs, userId, vehicleIdToUse, defaultConfig, userConfigs);

            // Get Secondary contacts config - only for notification with vehicle
            if (!vehicleIdToUse.equals(VEHICLE_ID_FOR_DEFAULT_PREFERENCE)) {
                getSecondaryContactConfigs(configs, userId, vehicleIdToUse, userConfigs);
            }


            // Merge general and user config
            List<NotificationConfig> retList = userConfigs.stream().map(c -> {
                NotificationConfig defaultConfigClone = defaultConfig.deepClone();
                defaultConfigClone.setContactId(c.getContactId());
                defaultConfigClone.patch(c);
                if (!c.getContactId().equals("self")) {
                    //removing API PUSH from secondary contacts
                    defaultConfigClone.removeChannel(ChannelType.API_PUSH);
                }
                defaultConfigClone.setUserId(userId);
                defaultConfigClone.setVehicleId(vehicleIdToUse);
                defaultConfigClone.setLocale(c.getLocale());
                return defaultConfigClone;
            }).collect(Collectors.toList());

            // Add default mail and phone no.

            addDefaultMailAndPhone(alertUserProfile, retList);

            // Removing all configs of contact that do not exists
            retList.removeIf(c -> null == c.getContactId());
            return retList;
        } else {
            NotificationConfig defaultConfigClone = defaultConfig.deepClone();
            defaultConfigClone.setContactId("self");
            defaultConfigClone.setVehicleId(vehicleIdToUse);
            if (StringUtils.isEmpty(defaultConfigClone.getLocale())) {
                defaultConfigClone.setLocale(defaultLocale);
            }
            List<NotificationConfig> retList = new ArrayList<>();
            retList.add(defaultConfigClone);
            return retList;
        }
    }

    /**
     * Add default mail and phone no.
     *
     * @param alertUserProfile alertUserProfile
     * @param retList          retList
     */
    private void addDefaultMailAndPhone(UserProfile alertUserProfile, List<NotificationConfig> retList) {
        retList.forEach(config -> {
            if (config.getContactId().equals("self")) {
                setConfigEmailPhoneLocale(config, alertUserProfile.getDefaultEmail(),
                    alertUserProfile.getDefaultPhoneNumber(), alertUserProfile.getLocale());
            } else {
                String contactId = config.getContactId();
                SecondaryContact secondaryContact = secondaryContactDao.findById(contactId);
                if (secondaryContact == null) {
                    config.setContactId(null);
                } else {
                    setConfigEmailPhoneLocale(config, secondaryContact.getEmail(),
                        secondaryContact.getPhoneNumber(), secondaryContact.getLocale());
                }
            }
        });
    }

    /**
     * Get secondary contact configs.
     *
     * @param configs        configs
     * @param userId         userId
     * @param vehicleIdToUse vehicleIdToUse
     * @param userConfigs    userConfigs
     */
    private void getSecondaryContactConfigs(List<NotificationConfig> configs,
                                            String userId, String vehicleIdToUse,
                                            List<NotificationConfig> userConfigs) {
        List<NotificationConfig> secondaryConfigs = configs.stream()
            .filter(nc -> userId.equals(nc.getUserId()) && vehicleIdToUse.equals(nc.getVehicleId())
                && !"self".equals(nc.getContactId()))
            .collect(Collectors.toList());
        log.debug("Selected secondary notification config : {}", secondaryConfigs);
        userConfigs.addAll(secondaryConfigs);
    }

    /**
     * Get primary contact config.
     *
     * @param configs        configs
     * @param userId         userId
     * @param vehicleIdToUse vehicleIdToUse
     * @param defaultConfig  defaultConfig
     * @param userConfigs    userConfigs
     */
    private void getPrimaryContactConfig(List<NotificationConfig> configs,
                                         String userId, String vehicleIdToUse,
                                         NotificationConfig defaultConfig,
                                         List<NotificationConfig> userConfigs) {
        Optional<NotificationConfig> primaryConfig = Optional.of(configs.stream()
            .filter(nc -> userId.equals(nc.getUserId()) && vehicleIdToUse.equals(nc.getVehicleId())
                && CONTACT_ID_FOR_DEFAULT_PREFERENCE.equals(nc.getContactId()))
            .findFirst().orElseGet(() -> {
                log.debug("User config not found for user {} and vehicle {}, using default config", userId,
                        vehicleIdToUse);
                NotificationConfig primaryConfigFromDefault = defaultConfig.deepClone();
                primaryConfigFromDefault.setContactId("self");
                primaryConfigFromDefault.setUserId(userId);
                primaryConfigFromDefault.setVehicleId(vehicleIdToUse);
                return primaryConfigFromDefault;
            }));
        userConfigs.add(primaryConfig.get());
    }

    /**
     * findDefaultConfigByBrand.
     *
     * @param configs configs
     *
     * @param brand brand
     *
     * @return Optional NotificationConfig
     */
    public Optional<NotificationConfig> findDefaultConfigByBrand(List<NotificationConfig> configs, String brand) {
        Optional<NotificationConfig> brandDefaultConfig = configs.stream().filter(
            nc -> USER_ID_FOR_DEFAULT_PREFERENCE.equals(nc.getUserId())
                && brand.equalsIgnoreCase(nc.getBrand())).findFirst();
        if (brandDefaultConfig.isPresent() || defaultBrand.equals(brand)) {
            return brandDefaultConfig;
        }
        return configs.stream().filter(nc -> USER_ID_FOR_DEFAULT_PREFERENCE.equals(nc.getUserId())
            && defaultBrand.equals(nc.getBrand())).findFirst();
    }

    /**
     * Has locale.
     *
     * @param locale locale
     * @return boolean
     */
    private boolean hasLocale(Locale locale) {
        return locale != null && !StringUtils.isEmpty(locale.toLanguageTag());
    }

    /**
     * Set config email phone locale.
     *
     * @param notificationConfig notificationConfig
     * @param email email
     * @param phone phone
     * @param locale locale
     */
    private void setConfigEmailPhoneLocale(NotificationConfig notificationConfig, String email, String phone,
                                           Locale locale) {
        EmailChannel emailChannel = notificationConfig.getChannel(ChannelType.EMAIL);
        if (emailChannel != null && emailChannel.getEnabled() && CollectionUtils.isEmpty(emailChannel.getEmails())
            && StringUtils.isNotEmpty(email)) {
            emailChannel.setEmails(new ArrayList<>(Collections.singletonList(email)));
        }

        SmsChannel smsChannel = notificationConfig.getChannel(ChannelType.SMS);
        if (smsChannel != null && smsChannel.getEnabled() && CollectionUtils.isEmpty(smsChannel.getPhones())
            && StringUtils.isNotEmpty(phone)) {
            smsChannel.setPhones(new ArrayList<>(Collections.singletonList(phone)));
        }

        notificationConfig.setLocale(hasLocale(locale) ? locale.toLanguageTag() : defaultLocale);
    }
}
