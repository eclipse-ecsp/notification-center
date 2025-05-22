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

import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.ChannelNotifierRegistry;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.config.NotificationSettingsInfo.NotificationData;
import org.eclipse.ecsp.notification.dao.NotificationSettingsInfoDAO;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NotificationConfigControlService class.
 */
@Service
public class NotificationConfigControlService {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationConfigControlService.class);
    @Autowired
    private NotificationConfigDAO notificationConfigDao;

    @Autowired
    private NotificationSettingsInfoDAO notificationSettingsInfoDao;

    @Autowired
    private NotificationEncryptionServiceImpl encryptionDecryptionService;

    @Autowired
    private SecondaryContactDAO secondaryContactDao;

    @Autowired
    private UserProfileDAO userProfileDao;

    private ChannelNotifierRegistry notifierRegistry;

    /**
     * update existing config.
     *
     * @param notificationConfigEvent IgniteEvent
     */
    public void patchUpdateConfig(IgniteEvent notificationConfigEvent) {
        LOGGER.debug(notificationConfigEvent, "Updating with new notification config");
        NotificationSettingDataV1_0 configData = (NotificationSettingDataV1_0) notificationConfigEvent.getEventData();
        NotificationConfig sample = configData.getNotificationConfigs().get(0);
        List<NotificationConfig> originalConfigs = notificationConfigDao.findByUserVehicleContactId(sample.getUserId(),
                sample.getVehicleId(), sample.getContactId());
        Map<String, NotificationConfig> originalConfigsByGroup = new HashMap<>();
        originalConfigs.forEach(c -> originalConfigsByGroup.put(c.getGroup(), c));
        Set<Channel> existingChannels = new HashSet<>();
        originalConfigs.forEach(c -> existingChannels.addAll(c.getFlattenedChannels()));
        Set<Channel> newChannels = new HashSet<>();
        // add for new groupings and update existing
        for (NotificationConfig nc : configData.getNotificationConfigs()) {
            NotificationConfig original = originalConfigsByGroup.get(nc.getGroup());
            if (original == null) {
                ObjectId id = new ObjectId();
                nc.setId(id.toString());
                notificationConfigDao.save(nc);
                LOGGER.info(notificationConfigEvent, "Saved notification config for userId {}", nc.getUserId());
            } else {
                original.patch(nc);
                notificationConfigDao.update(original);
                LOGGER.info(notificationConfigEvent, "Updated notification config for userId {}", nc.getUserId());
            }
            newChannels.addAll(nc.getFlattenedChannels());
        }
        Set<Channel> deletions = new HashSet<>(existingChannels);
        deletions.removeAll(newChannels);
        LOGGER.debug(notificationConfigEvent, "Deletions are {}", deletions);
        newChannels.removeAll(existingChannels);
        LOGGER.debug(notificationConfigEvent, "Additions are {}", newChannels);
        configData.getNotificationConfigs().forEach(this::processEachConfig);
        updateSettingsInfo(notificationConfigEvent, configData);
    }


    /**
     * process each config.
     *
     * @param config NotificationConfig
     */
    private void processEachConfig(NotificationConfig config) {
        LOGGER.debug("Adding default phones for config {}", config);

        SmsChannel smsChannel = config.getChannel(ChannelType.SMS);
        if (smsChannel != null && smsChannel.getEnabled() && smsChannel.getPhones() == null) {
            String userId = config.getUserId();
            String contactId = config.getContactId();
            LOGGER.debug("Adding default phones SMS with contact id {} and user id {} ", contactId, userId);
            String phone = null;
            phone = getPhoneNum(contactId, userId, phone);
            if (phone != null) {
                smsChannel.setPhones(Collections.singletonList(phone));
            }
        }
    }

    /**
     * get phone number.
     *
     * @param contactId String
     * @param userId    String
     * @param phone     String
     * @return phone
     */
    private String getPhoneNum(String contactId, String userId, String phone) {
        if ("self".equals(contactId)) {
            UserProfile userprofile = userProfileDao.findById(userId);
            if (userprofile != null) {
                phone = userprofile.getDefaultPhoneNumber();
            }
        } else {
            SecondaryContact secondaryContact = secondaryContactDao.findById(contactId);
            if (secondaryContact != null) {
                phone = secondaryContact.getPhoneNumber();
            }
        }
        return phone;
    }

    /**
     * update settings info.
     *
     * @param notificationConfigEvent IgniteEvent
     * @param configData              NotificationSettingDataV1_0
     */
    private void updateSettingsInfo(IgniteEvent notificationConfigEvent,
                                    NotificationSettingDataV1_0 configData) {
        NotificationSettingsInfo settingsInfo = new NotificationSettingsInfo();
        settingsInfo.setEventID(notificationConfigEvent.getEventId());
        settingsInfo.setTimestamp(notificationConfigEvent.getTimestamp());
        settingsInfo.setTimezone(notificationConfigEvent.getTimezone());
        settingsInfo.setVersion(notificationConfigEvent.getVersion().getValue());
        NotificationData data = new NotificationData();
        data.setNotificationConfigs(configData.getNotificationConfigs());
        settingsInfo.setData(data);
        setupChannel(settingsInfo);
    }

    /**
     * get notification config dao.
     *
     * @return NotificationConfigDAO
     */
    public NotificationConfigDAO getNotificationConfigDao() {
        return notificationConfigDao;
    }

    /**
     * set notification config dao.
     *
     * @param notificationConfigDao NotificationConfigDAO
     */
    public void setNotificationConfigDao(NotificationConfigDAO notificationConfigDao) {
        this.notificationConfigDao = notificationConfigDao;
    }

    /**
     * get notification settings info dao.
     *
     * @return NotificationSettingsInfoDAO
     */
    public NotificationSettingsInfoDAO getNotificationSettingsInfoDao() {
        return notificationSettingsInfoDao;
    }

    /**
     * set notification settings info dao.
     *
     * @param notificationSettingsInfoDao NotificationSettingsInfoDAO
     */
    void setNotificationSettingsInfoDao(NotificationSettingsInfoDAO notificationSettingsInfoDao) {
        this.notificationSettingsInfoDao = notificationSettingsInfoDao;
    }

    /**
     * Setup channel.
     *
     * @param settingsInfo NotificationSettingsInfo
     */
    private void setupChannel(NotificationSettingsInfo settingsInfo) {
        for (NotificationConfig config : settingsInfo.getData().getNotificationConfigs()) {
            for (Channel channel : config.getEnabledChannels()) {
                LOGGER.debug("Setting up channel {} ", channel);
                Map<String, ChannelNotifier> svcProviderMap =
                        notifierRegistry.getAllchannelNotifiers(channel.getChannelType());
                for (Map.Entry<String, ChannelNotifier> entry : svcProviderMap.entrySet()) {
                    ChannelNotifier channelNotifier = entry.getValue();
                    settingsInfo.addChannelResponse(channelNotifier.setupChannel(config));
                }
            }
        }
        notificationSettingsInfoDao.save(settingsInfo);
    }

    /**
     * get notifier registry.
     *
     * @param notifierRegistry ChannelNotifierRegistry
     */
    public void setNotifierRegistry(ChannelNotifierRegistry notifierRegistry) {
        this.notifierRegistry = notifierRegistry;
    }

    /**
     * Encrypt Notification Config.
     *
     * @param notificationConfig NotificationConfig
     */
    public void encryptNotificationConfig(NotificationConfig notificationConfig) {
        encryptionDecryptionService.encryptNotificationConfig(notificationConfig);
    }

    /**
     * Decrypt Notification Config.
     *
     * @param notificationConfig NotificationConfig
     */
    public void decryptNotificationConfig(NotificationConfig notificationConfig) {
        encryptionDecryptionService.decryptNotificationConfig(notificationConfig);
    }

}
