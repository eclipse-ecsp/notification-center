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

package org.eclipse.ecsp.platform.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.RefreshSchedulerData;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.domain.notification.utils.NotificationConfigCommonService;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.kafka.service.KafkaService;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.config.NotificationConfigRequest;
import org.eclipse.ecsp.notification.dao.NotificationTemplateConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
import org.eclipse.ecsp.notification.dao.TokenUserMapDao;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.entities.TokenUserMap;
import org.eclipse.ecsp.notification.grouping.GroupType;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.NotificationConfigResponse;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.ChannelNotAllowedForGroupException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidContactInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidGroupException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidUserIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.KafkaException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationGroupingNotAllowedException;
import org.eclipse.ecsp.platform.notification.exceptions.ServiceNameNotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.VehicleIdNotFoundException;
import org.eclipse.ecsp.security.utils.HmacSignatureGenerator;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.utils.Utils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.ecsp.domain.Version.V1_0;
import static org.eclipse.ecsp.domain.notification.commons.EventID.REFRESH_NOTIFICATION_SCHEDULER;
import static org.eclipse.ecsp.notification.config.NotificationConfig.CONTACT_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.config.NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE;
import static org.eclipse.ecsp.notification.grouping.GroupType.DEFAULT;
import static org.eclipse.ecsp.notification.grouping.GroupType.USER_ONLY;
import static org.eclipse.ecsp.notification.grouping.GroupType.USER_VEHICLE;

/**
 * NotificationConfigServiceV1_0 class.
 */
@SuppressWarnings("checkstyle:TypeName")
@Service
@Slf4j
public class NotificationConfigServiceV1_0 {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationConfigServiceV1_0.class);
    public static final String FAILED_TO_SEND_THE_REQUEST_TO_KAFKA = "Failed to send the request to kafka";
    public static final String FAILED_TO_SEND_THE_REQUEST_TO_KAFKA_FOR_EVENT =
            "Failed to send the request to kafka for event ";

    @Value("${kafka.sink.topic}")
    private String topic;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;

    @Autowired
    private NotificationTemplateDAO notificationTemplateDao;

    @Autowired
    private NotificationTemplateConfigDAO notificationTemplateConfigDao;

    @Autowired
    private NotificationConfigDAO notificationConfigDao;

    @Autowired
    private NotificationEncryptionServiceImpl encryptionDecryptionService;

    @Autowired
    private UserProfileDAO userProfileDao;

    @Autowired
    private SecondaryContactDAO secondaryContactDao;

    @Autowired
    private HmacSignatureGenerator hmacSignatureGenerator;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private NotificationConfigCommonService notificationConfigCommonService;

    @Value("${enable.ignite.vehicle.profile:true}")
    private boolean igniteVehicleProfile;

    @Autowired
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @Value("${enable.entitlement.validation:false}")
    private boolean enableEntitlementValidation;

    @Autowired
    TokenUserMapDao tokenUserMapDao;

    @Value("${brand.default.value:default}")
    private String defaultBrand;

    private static final String BAD_REQUEST = "400";

    @SuppressWarnings("checkstyle:ConstantName")
    private static final String REGULAR_EXP = "^\\+?(\\d[- ]?){1,50}$";

    @SuppressWarnings("checkstyle:ConstantName")
    private static final String EMAIL_REGEX = "\\S{1,52}@\\S{1,52}\\.\\S{2,4}";

    private static final String INVALID_PHONE_NUMBER = "Phone number should be all digit";

    private static final String INVALID_EMAIL = "Email should be in correct format";

    private Matcher matcher;

    /**
     * ProcessNotificationPreference and forward to kafka.
     *
     * @param userId userId
     *
     * @param vehicleId vehicleId
     *
     * @param preferenceEvent preferenceEvent
     */
    public void processNotificationPreference(String userId, String vehicleId, IgniteEvent preferenceEvent) {
        LOGGER.debug("Received preference request {}", preferenceEvent);
        try {
            kafkaService.sendIgniteEvent(vehicleId != null ? vehicleId : userId, preferenceEvent, topic);
        } catch (ExecutionException e) {
            LOGGER.error(preferenceEvent, FAILED_TO_SEND_THE_REQUEST_TO_KAFKA, e);
            throw new KafkaException(FAILED_TO_SEND_THE_REQUEST_TO_KAFKA_FOR_EVENT + e.getMessage());
        }
    }

    /**
     * GetNotificationPreference for a user vehicle service.
     *
     * @param userId userId
     *
     * @param vehicleId vehicleId
     *
     * @param service service
     *
     * @return List of NotificationConfigResponse
     */
    public List<NotificationConfigResponse> getNotificationPreference(String userId, String vehicleId, String service) {
        LOGGER.debug("Getting notification config for userId {} vehicleId {}", userId, vehicleId);

        String brand = getVehicleMake(vehicleId);
        UserProfile userProfile = userProfileDao.findById(userId);
        List<NotificationConfig> notificationConfigs = new ArrayList<>();

        //get all groups relevant for this user and vehicle
        Set<String> groups = getGroupsForUser(vehicleId, service);

        //for each group get config
        groups.forEach(group -> notificationConfigs.addAll(
            getNotificationConfigByGroup(userId, vehicleId, group, brand, userProfile)));

        List<NotificationConfigResponse> response = new ArrayList<>();

        //get all contact (self + secondaries)
        Set<String> contacts =
            notificationConfigs.stream().map(NotificationConfig::getContactId).collect(Collectors.toSet());

        //for each contact build config with all groups
        contacts.forEach(contact -> {
            NotificationConfigResponse notificationConfigResponse = new NotificationConfigResponse();
            List<NotificationConfig> configs = new ArrayList<>();
            notificationConfigs.stream().filter(nc -> nc.getContactId().equals(contact)).forEach(configs::add);
            notificationConfigResponse.setPreferences(configs);
            notificationConfigResponse.setContactId(contact);
            updateUserDetails(userProfile, contact, notificationConfigResponse);
            if (notificationConfigResponse.getContactId() != null) {
                response.add(notificationConfigResponse);
            }
        });

        cleanConfigFromDuplicateData(response);

        return response;
    }

    /**
     * UpdateNotificationPreference for a user vehicle service.
     *
     * @param userProfile UserProfile
     *
     * @param contact contact
     *
     * @param notificationConfigResponse notificationConfigResponse
     *
     */
    private void updateUserDetails(UserProfile userProfile, String contact,
                                   NotificationConfigResponse notificationConfigResponse) {
        if (contact.equals("self")) {
            notificationConfigResponse.setEmail(userProfile.getDefaultEmail());
            notificationConfigResponse.setPhoneNumber(userProfile.getDefaultPhoneNumber());
            notificationConfigResponse.setLocale(userProfile.getLocale());
            notificationConfigResponse.setContactName(contact);
        } else {
            SecondaryContact secondaryContact = secondaryContactDao.findById(contact);
            if (secondaryContact != null) {
                notificationConfigResponse.setEmail(secondaryContact.getEmail());
                notificationConfigResponse.setPhoneNumber(secondaryContact.getPhoneNumber());
                notificationConfigResponse.setLocale(secondaryContact.getLocale());
                notificationConfigResponse.setContactName(secondaryContact.getContactName());
            } else {
                notificationConfigResponse.setContactId(null);
            }
        }
    }

    /**
     * cleanConfigFromDuplicateData.
     *
     *
     * @param response List of NotificationConfigResponse
     */
    private void cleanConfigFromDuplicateData(List<NotificationConfigResponse> response) {
        // ------ Delete all duplicated data since it already in the 1st level of the response ---------
        response.forEach(res -> res.getPreferences().forEach(config -> {
            config.setEmail(null);
            config.setId(null);
            config.setPhoneNumber(null);
            config.setSchemaVersion(null);
            config.setUserId(null);
            config.setVehicleId(null);
            config.setContactId(null);
            config.setBrand(null);
        }));
    }


    /**
     * GetNotificationConfigByGroup .
     *
     * @param userId userId
     *
     * @param vehicleId vehicleId
     *
     * @param group service
     *
     * @param brand brand
     *
     * @return List of NotificationConfigResponse
     */
    private List<NotificationConfig> getNotificationConfigByGroup(String userId, String vehicleId, String group,
                                                                  String brand, UserProfile userProfile) {

        List<NotificationConfig> configs =
            notificationConfigCommonService.getAllConfigsFromDbByGroup(userId, vehicleId, group);
        if (configs.isEmpty()) {
            LOGGER.info(
                "No notification config found for userId {} vehicleId {} serviceName {} Cannot process any further. "
                    + "expecting at least GENERAL/GENERAL",
                userId, vehicleId, group);
            return new ArrayList<>();
        }

        List<NotificationConfig> secondaryContactsDefaultConfigs =
            notificationConfigCommonService.getSecondaryContactsConfig(userId, vehicleId, configs, brand);
        configs.addAll(secondaryContactsDefaultConfigs);

        List<NotificationConfig> selectedConfigs =
            notificationConfigCommonService.selectNotificationConfig(configs, vehicleId, userProfile, brand);
        LOGGER.debug("selected config size {} and config {}", selectedConfigs.size(), selectedConfigs);
        if (selectedConfigs.isEmpty()) {
            LOGGER.info(
                "Could not find a notification config for userId {} vehicleId {} serviceName {} "
                    + "Fetch returned configs: {}",
                userId, vehicleId, group, configs);
            return new ArrayList<>();
        }

        selectedConfigs.forEach(config -> {
            if (NotificationConstants.GENERAL.equals(config.getUserId())) {
                config.setUserId(userId);
            }
        });

        selectedConfigs = selectedConfigs.stream()
            .filter(nc -> {
                nc.getChannels().removeIf(ch -> ChannelType.API_PUSH == ch.getChannelType());
                return !nc.getChannels().isEmpty();
            }).toList();

        return selectedConfigs;
    }

    /**
     * RefreshNotificationScheduler.
     *
     * @param vehicleId vehicleId
     *
     * @param service service
     *
     * @return groups
     */
    private Set<String> getGroupsForUser(String vehicleId, String service) {

        Set<String> groups;

        Set<NotificationGrouping> allNotificationGroupings =
            new HashSet<>(notificationGroupingDao.findByMandatory(false));

        Set<NotificationGrouping> notificationGrouping;

        if (!StringUtils.isEmpty(service)) {
            notificationGrouping = allNotificationGroupings.stream().filter(ng -> service.equals(ng.getService()))
                .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(notificationGrouping)) {
                LOGGER.error("Service {} does not exist", service);
                throw new ServiceNameNotFoundException(Collections.singletonList(
                    NotificationCenterError.NOTIFICATION_CONFIG_SERVICE_NAME_NOT_FOUND.toMessage()));
            }
        } else {
            notificationGrouping = allNotificationGroupings;
        }

        // ----- Remove preferences that are for user only or vehicle only --------
        GroupType groupType = VEHICLE_ID_FOR_DEFAULT_PREFERENCE.equals(vehicleId) ? USER_ONLY : USER_VEHICLE;
        notificationGrouping = notificationGrouping.stream()
            .filter(ng -> (ng.getGroupType().equals(groupType) || ng.getGroupType().equals(DEFAULT)))
            .collect(Collectors.toSet());

        if (enableEntitlementValidation) {
            Set<String> enabledServices =
                vehicleId.equals(VEHICLE_ID_FOR_DEFAULT_PREFERENCE) ? new HashSet<>() : getEnabledServices(vehicleId);
            groups = notificationGrouping.stream()
                .filter(ng -> !ng.isCheckEntitlement() || enabledServices.contains(ng.getService()))
                .map(NotificationGrouping::getGroup).collect(Collectors.toSet());
        } else {
            groups = notificationGrouping.stream().map(NotificationGrouping::getGroup).collect(Collectors.toSet());
        }
        return groups;
    }


    /**
     * getVehicleMake getVehicleMake.
     *
     * @param vehicleId vehicleId
     * @return Brand
     */
    private String getVehicleMake(String vehicleId) {
        if (VEHICLE_ID_FOR_DEFAULT_PREFERENCE.equals(vehicleId)) {
            return defaultBrand;
        }
        try {
            VehicleProfileAttribute[] vehicleProfileAttributes =
                new VehicleProfileAttribute[] {VehicleProfileAttribute.MAKE};
            Map<VehicleProfileAttribute, Optional<String>> attrs =
                coreVehicleProfileClient.getVehicleProfileAttributes(vehicleId, igniteVehicleProfile,
                    vehicleProfileAttributes);
            if (CollectionUtils.isEmpty(attrs)) {
                return defaultBrand;
            }
            return attrs.get(VehicleProfileAttribute.MAKE).orElse(defaultBrand);
        } catch (RuntimeException e) {
            LOGGER.error(String.format("Vehicle ID %s does not exist", vehicleId), e);
            throw new VehicleIdNotFoundException(Collections.singletonList(
                NotificationCenterError.NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND.toMessage()));
        }
    }

    /**
     * isValidContactId isValidContactId.
     *
     * @param contactId String
     * @return boolean
     */
    private boolean isValidContactId(String contactId) {
        return null != secondaryContactDao.findById(contactId);
    }

    /**
     * processUserPreference.
     *
     * @param userId userId
     *
     * @param userProfileEvent event
     */
    public void processUserPreference(String userId, IgniteEvent userProfileEvent) {
        try {
            kafkaService.sendIgniteEvent(userId, userProfileEvent, topic);
        } catch (ExecutionException e) {
            LOGGER.error(userProfileEvent, FAILED_TO_SEND_THE_REQUEST_TO_KAFKA, e);
            throw new KafkaException(FAILED_TO_SEND_THE_REQUEST_TO_KAFKA_FOR_EVENT + e.getMessage());
        }
    }

    /**
     * saveNotificationTemplates.
     *
     * @param notificationTemplatesList templates
     *
     * @return save status
     */
    public boolean saveNotificationTemplates(List<NotificationTemplate> notificationTemplatesList) {
        boolean isSaved = false;
        try {
            notificationTemplateDao.saveAll(notificationTemplatesList.toArray(new NotificationTemplate[0]));
            isSaved = true;
            LOGGER.debug("Notification templates stored in MongoDb");

        } catch (Exception e) {
            LOGGER.error("Error while saving NotificationTemplates data in MongoDb: {}", e.getMessage());
        }
        return isSaved;
    }

    /**
     * saveNotificationTemplatesConfigs.
     *
     * @param notificationTemplatesConfigs configs
     *
     * @return save status
     */
    public boolean saveNotificationTemplatesConfigs(List<NotificationTemplateConfig> notificationTemplatesConfigs) {
        boolean isSaved = false;
        try {
            notificationTemplateConfigDao
                .saveAll(notificationTemplatesConfigs.toArray(new NotificationTemplateConfig[0]));
            isSaved = true;
            LOGGER.debug("Notification template configs stored in MongoDb");

        } catch (Exception e) {
            LOGGER.error("Error while saving NotificationTemplates data in MongoDb: {}", e.getMessage());
        }
        return isSaved;
    }

    /**
     * saveNotificationGrouping.
     *
     * @param notificationTemplatesList templates
     *
     * @return save status
     */
    public boolean saveNotificationGrouping(List<NotificationGrouping> notificationTemplatesList) {
        boolean isSaved = false;
        try {
            notificationTemplatesList.forEach(ng -> ng.setId(ng.getNotificationId() + "_" + ng.getGroup() + "_"
                + (StringUtils.isEmpty(ng.getService()) ? "" : ng.getService())));
            notificationGroupingDao.saveAll(notificationTemplatesList.toArray(new NotificationGrouping[0]));
            isSaved = true;
            LOGGER.debug("Notification Grouping stored in MongoDb");
        } catch (Exception e) {
            LOGGER.error("Error while saving Notification Grouping in MongoDb", e);
        }
        return isSaved;
    }

    /**
     * Get notification grouping.
     *
     * @param group group name
     *
     * @return List of grouping
     */
    public List<NotificationGrouping> getNotificationGrouping(String group) {
        List<NotificationGrouping> groups = notificationGroupingDao.findByGroups(Collections.singleton(group));
        if (CollectionUtils.isEmpty(groups)) {
            LOGGER.error("Group {} does not exist", group);
            throw new InvalidGroupException(
                Collections.singletonList(NotificationCenterError.GROUPING_NAME_DOES_NOT_EXIST.toMessage()));
        }
        return groups;
    }

    /**
     * Delete notification grouping.
     *
     * @param group group
     *
     * @param notificationId notificationId
     *
     * @param service service
     */
    public void deleteNotificationGrouping(String group, String notificationId, String service) {
        int deleted = notificationGroupingDao.deleteByGroupNotificationIdAndService(group, notificationId, service);
        if (deleted == 0) {
            LOGGER.error("No such a record: group={}, notificationId={}, service={}]", group, notificationId, service);
            throw new InvalidGroupException(Collections.singletonList(
                NotificationCenterError.GROUPING_NOTIFICATION_ID_SERVICE_DOES_NOT_EXIST.toMessage()));
        }
    }

    /**
     * Save default config.
     *
     * @param configs configs
     *
     * @return List of configs
     */
    public List<NotificationConfig> saveDefaultNotificationConfig(List<NotificationConfig> configs) {
        List<NotificationConfig> existingConfigs = notificationConfigDao.findDefaultByGroups(configs.stream()
            .map(NotificationConfig::getGroup)
            .collect(Collectors.toSet()));
        List<NotificationConfig> configsToAdd = new ArrayList<>(configs.size());
        configs.forEach(config -> {
            if (!config.getBrand().equals(defaultBrand)) {
                verifyDefaultBrandExists(config.getGroup(), configs, existingConfigs);
            }
            Optional<NotificationConfig> existingConfig = existingConfigs.stream()
                .filter(dbConfig -> config.getBrand().equals(dbConfig.getBrand())
                    && config.getGroup().equals(dbConfig.getGroup())).findFirst();
            if (existingConfig.isPresent()) {
                NotificationConfig dbExistingConfig = existingConfig.get();
                dbExistingConfig.patch(config);
                configsToAdd.add(dbExistingConfig);
            } else {
                config.setId(new ObjectId().toString());
                configsToAdd.add(config);
            }
        });
        return notificationConfigDao.saveAll(configsToAdd.toArray(new NotificationConfig[0]));
    }

    /**
     * Verify default brand exists.
     *
     * @param group group
     * @param payloadConfigs payloadConfigs
     * @param dbConfigs dbConfigs
     */
    private void verifyDefaultBrandExists(String group, List<NotificationConfig> payloadConfigs,
                                          List<NotificationConfig> dbConfigs) {
        if (payloadConfigs.stream().anyMatch(c -> c.getGroup().equals(group) && c.getBrand().equals(defaultBrand))) {
            return;
        }
        if (dbConfigs.stream().anyMatch(c -> c.getGroup().equals(group) && c.getBrand().equals(defaultBrand))) {
            return;
        }
        throw new InvalidInputException(
            Collections.singletonList(NotificationCenterError.DEFAULT_CONFIG_FOR_BRAND_WITHOUT_DEFAULT.toMessage()));
    }

    /**
     * Save notification config.
     */
    public List<NotificationConfig> saveNotificationConfig(String userId, String vehicleId, String contactId,
                                                           List<NotificationConfig> configs) {
        List<NotificationConfig> existingConfigs =
            notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups(userId, vehicleId, contactId,
                configs.stream()
                    .map(NotificationConfig::getGroup)
                    .collect(Collectors.toSet()));
        Map<String, NotificationConfig> groupToExistingConfig = existingConfigs.stream()
            .collect(Collectors.toMap(NotificationConfig::getGroup, Function.identity()));
        List<NotificationConfig> configsToAdd = new ArrayList<>(configs.size());
        configs.forEach(config -> {
            NotificationConfig existingConfig = groupToExistingConfig.get(config.getGroup());
            if (existingConfig != null) {
                existingConfig.patch(config);
                configsToAdd.add(existingConfig);
            } else {
                ObjectId id = new ObjectId();
                config.setId(id.toString());
                configsToAdd.add(config);
            }
        });
        return notificationConfigDao.saveAll(configsToAdd.toArray(new NotificationConfig[0]));
    }

    /**
     * Validates suppression configs and removes invalid configs. This method
     * will remove invalid time configurations that don't make sense, but are in
     * a valid format. For example, setting a vacation in the past makes no
     * sense, so we remove it.<br/>
     * This will result in safer code that does not flow through our system and
     * also does not require any client-side changes.
     *
     * @param configs NotificationConfigs to be checked
     * @return Sanitized list of NotificationConfigs
     */
    public List<NotificationConfig> sanitizeConfig(List<NotificationConfig> configs) {

        return configs.stream().flatMap(notificationConfig -> {
            List<Channel> sanitizedChannels = notificationConfig.getChannels().stream().map(
                    channel -> {

                        List<SuppressionConfig> sanitizedConfigs = channel.getSuppressionConfigs().stream()
                                .filter(config -> {
                                    if (config.getSuppressionType()
                                            .equals(SuppressionConfig
                                            .SuppressionType.VACATION)) {
                                        config.setDays(null);
                                        return checkSupressionConfigTime(config);
                                    } else if (config.getSuppressionType()
                                            .equals(SuppressionConfig.SuppressionType.RECURRING)) {
                                        boolean isStartEqualEnd = config.getStartTime().equals(config.getEndTime());
                                        if (!isStartEqualEnd) {
                                            return true;
                                        }

                                        LOGGER.warn("Sanitizing invalid suppression configs. isStartEqualEnd=true");
                                        return false;
                                    }

                                    return true;
                                }).toList();
                        sanitizeSuppressionConfig(sanitizedConfigs);
                        channel.setSuppressionConfigs(sanitizedConfigs);
                        return channel;

                    }

            ).toList();
            notificationConfig.setChannels(sanitizedChannels);

            return Stream.of(notificationConfig);

        }).toList();
    }

    /**
     * Validates suppression configs time.
     *
     * @param config config
     * @return boolean
     */
    private static boolean checkSupressionConfigTime(SuppressionConfig config) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.of(config.getStartDate(), config.getStartTime());
        LocalDateTime end = LocalDateTime.of(config.getEndDate(), config.getEndTime());

        boolean isStartBeforeEnd = start.isBefore(end);
        boolean isEndInFuture = now.isBefore(end);
        if (isStartBeforeEnd && isEndInFuture) {
            return true;
        }

        LOGGER.warn(
            "Sanitizing invalid suppression configs. isStartBeforeEnd=" + isStartBeforeEnd
                + " | isEndInFuture=" + isEndInFuture);
        return false;
    }

    /**
     * Sanitize suppression config.
     *
     * @param sanitizedConfigs sanitizedConfigs
     */
    private static void sanitizeSuppressionConfig(List<SuppressionConfig> sanitizedConfigs) {
        sanitizedConfigs.forEach(suppressionConfig -> {
            if (suppressionConfig.getDays() != null) {
                suppressionConfig.setDays(
                    suppressionConfig.getDays().stream().distinct().toList());
            }
        });
    }

    /**
     * Encrypt notification config.
     *
     * @param notificationConfig notificationConfig
     */
    public void encryptNotificationConfig(NotificationConfig notificationConfig) {
        encryptionDecryptionService.encryptNotificationConfig(notificationConfig);
    }


    /**
     * Get user profile.
     *
     * @param id userId
     *
     * @return UserProfile
     */
    public UserProfile getUserProfile(String id) {
        return userProfileDao.findById(id);
    }

    /**
     * Get secondary contact.
     *
     * @param id contactId
     *
     * @return SecondaryContact
     */
    public SecondaryContact getSecondaryContact(String id) {
        return secondaryContactDao.findById(id);
    }

    /**
     * GetEnabledServices.
     *
     * @param vehicleId vehicleId
     * @return Set of services
     */
    private Set<String> getEnabledServices(String vehicleId) {
        try {
            return vehicleService.getEnabledServices(vehicleId);
        } catch (NoSuchEntityException e) {
            LOGGER.info("Provisioned Services for the vehicle {}  Not Found", vehicleId);
            return new HashSet<>();
        }
    }

    /**
     * Check if user exists.
     *
     * @param userId userId
     * @return boolean
     */
    public boolean isUserExists(String userId) {
        return !StringUtils.isEmpty(userId) && nonNull(getUserProfile(userId));
    }

    /**
     * validateInput configs.
     **/
    public void validateInput(String userId, String vehicleId, String contactId,
                              Collection<NotificationConfigRequest> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            throw new IllegalArgumentException("expecting array of configs");
        }

        boolean isUserOnlyRequest = VEHICLE_ID_FOR_DEFAULT_PREFERENCE.equals(vehicleId);
        if (!isUserExists(userId)) {
            throw new InvalidUserIdInput("User doesn't exist");
        }
        if (!isUserOnlyRequest && !vehicleService.isVehicleExist(vehicleId)) {
            throw new InvalidVehicleIdInput("Vehicle doesn't exist");
        }
        if (!contactId.equals(CONTACT_ID_FOR_DEFAULT_PREFERENCE) && !isValidContactId(contactId)) {
            throw new InvalidContactInput("Invalid contact id");
        }

        Collection<String> groupNames = new HashSet<>();
        if (!CollectionUtils.isEmpty(configs)) {
            groupNames = configs.stream().map(NotificationConfigRequest::getGroup).collect(toSet());
        }
        GroupType groupType = isUserOnlyRequest ? USER_ONLY : USER_VEHICLE;
        Collection<NotificationGrouping> groups = validateGroups(groupNames, groupType);
        if (enableEntitlementValidation && !isUserOnlyRequest) {
            validateServicesEnabled(groups, vehicleId);
        }
        validateChannels(configs, vehicleId);
    }

    /**
     * validateServicesEnabled.
     *
     * @param groupNames groupNames
     * @param expectedType expectedType
     * @return Collection of NotificationGrouping
     */
    private Collection<NotificationGrouping> validateGroups(Collection<String> groupNames, GroupType expectedType) {
        Collection<NotificationGrouping> groups = notificationGroupingDao.findByGroups(groupNames);
        if (groups == null
            || groups.stream().map(NotificationGrouping::getGroup).distinct().count() != groupNames.size()) {
            Set<String> existingGroupNames;
            if (groups != null) {
                existingGroupNames = groups.stream().map(NotificationGrouping::getGroup).collect(Collectors.toSet());
                groupNames.removeAll(existingGroupNames);
            }
            String msg = "No such group(s): " + groupNames;
            LOGGER.error(msg);
            throw new NoSuchEntityException(msg);
        } else {
            Set<String> allowedGroups = groups.stream()
                    .filter(ng -> (expectedType.equals(ng.getGroupType()) || (DEFAULT.equals(ng.getGroupType()))))
                .map(NotificationGrouping::getGroup)
                .collect(Collectors.toSet());
            if (!allowedGroups.containsAll(groupNames)) {
                LOGGER.error("Group(s) " + groupNames + " are not allowed for API of type " + expectedType);
                throw new NotificationGroupingNotAllowedException(Collections.emptyList(), groupNames.toString(),
                    expectedType.toString());
            }
        }
        return groups;
    }

    /**
     * Validate Phone Number.
     *
     * @param configRequests configRequests
     */
    private void validatePhoneNumber(Collection<NotificationConfigRequest> configRequests) {
        Pattern pattern = Pattern.compile(REGULAR_EXP);
        Collection<ResponseWrapper.Message> errors = new ArrayList<>();
        if ((configRequests.stream().anyMatch(a -> (a.getChannels() == null || a.getChannels().isEmpty())))) {
            LOGGER.debug(" Channels is Empty or null");
            return;

        }

        List<Channel> channels = configRequests.stream().flatMap(a -> a.getChannels().stream())
            .filter(c -> c.getType().equalsIgnoreCase("sms")).toList();
        if (channels.stream()
            .anyMatch(a -> (((SmsChannel) a).getPhones() == null || ((SmsChannel) a).getPhones().isEmpty()))) {
            LOGGER.debug(" Phones is Empty or null");
            return;
        }
        channels.stream().flatMap(a -> ((SmsChannel) a).getPhones().stream()).forEach(ph -> {

            matcher = pattern.matcher(ph);
            if (!matcher.matches()) {
                errors.add(ResponseWrapper.Message.of(BAD_REQUEST, ph, INVALID_PHONE_NUMBER));
                LOGGER.error("Phone number should be all digit : " + Utils.maskString(ph));
                throw new InvalidInputException(errors);
            }
        });

    }

    /**
     * Validate Email.
     *
     * @param configRequests configRequests
     */
    private void validateEmail(Collection<NotificationConfigRequest> configRequests) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Collection<ResponseWrapper.Message> errors = new ArrayList<>();
        if ((configRequests.stream().anyMatch(a -> (a.getChannels() == null || a.getChannels().isEmpty())))) {
            LOGGER.debug(" Channels is Empty or null");
            return;

        }

        List<Channel> channels = configRequests.stream().flatMap(a -> a.getChannels().stream())
            .filter(c -> c.getType().equalsIgnoreCase("email")).toList();
        if (channels.stream()
            .anyMatch(a -> (((EmailChannel) a).getEmails() == null || ((EmailChannel) a).getEmails().isEmpty()))) {
            LOGGER.debug(" Email is Empty or null");
            return;
        }
        channels.stream().flatMap(a -> ((EmailChannel) a).getEmails().stream()).forEach(em -> {

            matcher = pattern.matcher(em);
            if (!matcher.matches()) {
                errors.add(ResponseWrapper.Message.of(BAD_REQUEST, em, INVALID_EMAIL));
                LOGGER.error("Email is not in correct pattern: " + em);
                throw new InvalidInputException(errors);
            }
        });

    }

    /**
     * validateChannels.
     *
     * @param configRequests configRequests
     * @param vehicleId vehicleId
     */
    private void validateChannels(Collection<NotificationConfigRequest> configRequests, String vehicleId) {

        Set<String> groups = configRequests.stream().map(NotificationConfigRequest::getGroup).collect(toSet());
        List<NotificationConfig> allDefaultConfigs = notificationConfigDao.findDefaultByGroups(groups);
        String brand = getVehicleMake(vehicleId);
        List<NotificationConfig> defaultConfigs = new ArrayList<>();
        groups.forEach(group -> {
            Optional<NotificationConfig> optional = notificationConfigCommonService.findDefaultConfigByBrand(
                allDefaultConfigs.stream().filter(nc -> group.equals(nc.getGroup())).toList(), brand);
            optional.ifPresent(defaultConfigs::add);
        });

        if (CollectionUtils.isEmpty(defaultConfigs)) {
            return;
        }

        List<ChannelNotAllowedForGroupException> errors = new ArrayList<>();
        Map<String, NotificationConfigRequest> groupToConfigRequest =
            configRequests.stream().collect(toMap(NotificationConfigRequest::getGroup, Function.identity()));
        defaultConfigs.forEach(defaultConfig -> {
            //noinspection configRequests must contains the current group since we search DB with groups from this list
            NotificationConfigRequest configRequest = groupToConfigRequest.get(defaultConfig.getGroup());
            Set<ChannelType> configRequestChannels =
                Optional.ofNullable(configRequest.getChannels()).orElse(new ArrayList<>()).stream()
                    .map(Channel::getChannelType).collect(toSet());
            if (CollectionUtils.isEmpty(configRequestChannels)) {
                return;
            }

            if (CollectionUtils.isEmpty(defaultConfig.getChannels())
                || !defaultConfig.getChannels().stream().map(Channel::getChannelType).collect(toSet())
                    .containsAll(configRequestChannels)) {
                Set<ChannelType> notAllowedChannels = configRequestChannels.stream().filter(
                    crc -> !defaultConfig.getChannels().stream().map(Channel::getChannelType).collect(toSet())
                        .contains(crc)).collect(toSet());
                errors.add(
                    new ChannelNotAllowedForGroupException(Collections.emptyList(), notAllowedChannels.toString(),
                        configRequest.getGroup()));
            }

        });


        if (!CollectionUtils.isEmpty(errors)) {
            log.error("Validation of notification config request failed: {}", errors);
            throw new InvalidInputException(
                errors.stream().map(ChannelNotAllowedForGroupException::getErrors).flatMap(Collection::stream)
                    .collect(toList()));
        }

        validatePhoneNumber(configRequests);
        validateEmail(configRequests);
    }

    /**
     * validate contact uniqueness.
     */
    public void validateContactUniqueness(SecondaryContact secondaryContact) {
        List<String> contactIds =
            secondaryContactDao.getContactIds(secondaryContact.getUserId(), secondaryContact.getVehicleId());
        String[] ks = contactIds.toArray(new String[0]);
        List<SecondaryContact> existingContacts = secondaryContactDao.findByIds(ks);

        existingContacts.removeIf(sc -> sc.getContactId().equals(secondaryContact.getContactId()));

        if (StringUtils.isNotEmpty(secondaryContact.getContactName())
                &&
                existingContacts.stream()
                        .anyMatch(sc -> secondaryContact.getContactName().equals(sc.getContactName()))) {

            throw new InvalidContactInput(
                    "Invalid input: contact name (" + secondaryContact.getContactName() + ") already present");

        }

        if (StringUtils.isNotEmpty(secondaryContact.getEmail())
                && existingContacts.stream().anyMatch(sc -> secondaryContact.getEmail().equals(sc.getEmail()))) {
            throw new InvalidContactInput(
                    "Invalid input: contact email (" + secondaryContact.getEmail() + ") already exists");

        }

        if (StringUtils.isNotEmpty(secondaryContact.getPhoneNumber())
                && existingContacts.stream().anyMatch(sc -> secondaryContact.getPhoneNumber()
                .equals(sc.getPhoneNumber()))) {

            throw new InvalidContactInput(
                    "Invalid input: contact phone (" + secondaryContact.getPhoneNumber() + ") exists");


        }
    }

    /**
     * Validate user id and vehicle id.
     */
    public void validateUserIdAndVehicleId(String userId, String vehicleId) {
        if (!isUserExists(userId)) {
            LOGGER.debug("User Id not registered");
            throw new InvalidUserIdInput("User Id not registered");
        }

        if (!vehicleService.isVehicleExist(vehicleId)) {
            LOGGER.debug("Vehicle Id not registered");
            throw new InvalidVehicleIdInput("Vehicle Id not registered");
        }

    }

    /**
     * validate grouping.
     */
    public void validateGroupingInput(List<NotificationGrouping> notificationGroupingList) {
        notificationGroupingList.forEach(
            nc -> nc.setGroupType(nc.getGroupType() == null ? DEFAULT : nc.getGroupType()));
        List<NotificationGrouping> notificationGroupingDbList = notificationGroupingDao.findAll();
        Set<String> notificationIdList =
            notificationGroupingList.stream().map(NotificationGrouping::getNotificationId).collect(Collectors.toSet());
        notificationIdList.forEach(notificationId -> {
            //collect all records from db and input related to this notification ID
            List<NotificationGrouping> currentList =
                notificationGroupingList.stream().filter(ng -> ng.getNotificationId().equals(notificationId))
                    .collect(Collectors.toList());
            currentList.addAll(
                notificationGroupingDbList.stream().filter(ng -> ng.getNotificationId().equals(notificationId))
                    .toList());

            //check single group for notification id
            if (currentList.stream().map(NotificationGrouping::getGroup).distinct().count() > 1) {
                throw new InvalidInputException(Collections.singletonList(
                    NotificationCenterError.GROUPING_MULTI_GROUP_FOR_NOTIFICATION_ID.toMessage(notificationId)));
            }
            //check duplicate key
            if (currentList.stream().map(NotificationGrouping::getService).distinct().count() != currentList.size()) {
                throw new InvalidInputException(
                    Collections.singletonList(NotificationCenterError.GROUPING_DUPLICATE_KEY.toMessage()));
            }
        });
    }

    /**
     * validateServicesEnabled.
     *
     * @param groupList groupList
     * @param vehicleId vehicleId
     */
    private void validateServicesEnabled(Collection<NotificationGrouping> groupList, String vehicleId) {
        Set<String> enabledServices = getEnabledServices(vehicleId);
        Map<String, List<NotificationGrouping>> groupNameToGroups = getGroupNameToGroupsMap(groupList);
        groupList.forEach(group -> {
            if (isGroupNotEntitled(groupNameToGroups, group.getGroup(), enabledServices)) {
                LOGGER.error("Vehicle {} is not subscribed to group {}. Vehicle is subscribed only to {}", vehicleId,
                    group.getGroup(), enabledServices);
                throw new AuthorizationException("Vehicle is not subscribed to any of the services");
            }
        });
    }

    /**
     * getGroupNameToGroupsMap.
     *
     * @param groups groups
     * @return Map of group name to groups
     */
    private Map<String, List<NotificationGrouping>> getGroupNameToGroupsMap(Collection<NotificationGrouping> groups) {
        return groups.stream().collect(Collectors.toMap(
            NotificationGrouping::getGroup,
            g -> {
                List<NotificationGrouping> currentGroupObjects = new ArrayList<>();
                currentGroupObjects.add(g);
                return currentGroupObjects;
            },
            (oldValue, newValue) -> {
                oldValue.addAll(newValue);
                return oldValue;
            }));
    }

    /**
     * isGroupNotEntitled.
     *
     * @param groupNameToGroups groupNameToGroups
     * @param group group
     * @param enabledServices enabledServices
     * @return boolean
     */
    private boolean isGroupNotEntitled(Map<String, List<NotificationGrouping>> groupNameToGroups, String group,
                                       Set<String> enabledServices) {
        List<NotificationGrouping> currentGroups = groupNameToGroups.get(group);
        return currentGroups.stream().allMatch(NotificationGrouping::isCheckEntitlement)
            && currentGroups.stream().map(NotificationGrouping::getService).noneMatch(enabledServices::contains);
    }

    /**
     * Put a message on kafka that tells the notification sp to update the scheduler.
     * The method can be async since the it should not impact the user
     *
     * @param userId    the user of the notification config
     * @param vehicleId the id of the vehicle of the notification config
     * @param contactId the relevant config
     */
    @Async
    public void notifyNotificationConfigUpdate(String userId, String vehicleId, String contactId, Set<String> groups,
                                               String requestId) {
        IgniteEventImpl event = new IgniteEventImpl();
        event.setEventData(RefreshSchedulerData.builder().userId(userId).contactId(contactId).groups(groups).build());
        event.setEventId(REFRESH_NOTIFICATION_SCHEDULER);
        event.setVehicleId(vehicleId);
        event.setMessageId(randomUUID().toString());
        event.setRequestId(requestId);
        event.setVersion(V1_0);
        LOGGER.info("Put on kafka message: {}", event);
        try {
            kafkaService.sendIgniteEvent(userId, event, topic);
        } catch (ExecutionException e) {
            LOGGER.error("Failed to put message on kafka: {}", event);
        }
    }

    /**
     * This method deletes fcm-token from token user-Map.
     * Deletion is done if the userId in the jwt token matches the userId in mongo document.
     *
     * @param token  token
     * @param userId userId
     */
    public void deleteFcmToken(String token, String userId) {
        TokenUserMap tokenMapping = tokenUserMapDao.findById(token);
        if (ObjectUtils.isEmpty(tokenMapping)) {
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.FCM_TOKEN_DOES_NOT_EXIST.toMessage()));
        }
        if (userId.equalsIgnoreCase(tokenMapping.getUserID())) {
            tokenUserMapDao.deleteById(token);
        } else {
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.INVALID_USERID_EXCEPTION.toMessage()));
        }
    }

    // Added for test purposes
    public void setCoreVehicleProfileClient(CoreVehicleProfileClient coreVehicleProfileClient) {
        this.coreVehicleProfileClient = coreVehicleProfileClient;
    }

    /**
     * Get default config.
     *
     * @param notificationId notificationId
     * @param brand brand
     * @return NotificationConfig
     */
    public NotificationConfig getDefaultConfig(String notificationId, String brand) {
        String group = getNotificationGroupName(notificationId);
        return getNotificationConfig(group, brand).get(0);
    }

    /**
     * Get all default configs.
     *
     * @param notificationId notificationId
     * @return List of NotificationConfig
     */
    public List<NotificationConfig> getAllDefaultConfigs(String notificationId) {
        String group = getNotificationGroupName(notificationId);
        return getNotificationConfig(group, null);
    }

    /**
     * Get notification config by group.
     *
     * @param notificationId notificationId
     * @return NotificationConfig
     */
    private String getNotificationGroupName(String notificationId) {
        List<NotificationGrouping> notificationGroupings = notificationGroupingDao.findByNotificationId(notificationId);
        if (CollectionUtils.isEmpty(notificationGroupings)) {
            throw new NotFoundException(
                Collections.singletonList(NotificationCenterError.GROUPING_NOTIFICATION_ID_NOT_FOUND.toMessage()));
        }
        return notificationGroupings.get(0).getGroup();
    }

    /**
     * Get notification config.
     *
     * @param group group
     * @param brand brand
     * @return List of NotificationConfig
     */
    private List<NotificationConfig> getNotificationConfig(String group, String brand) {
        List<NotificationConfig> notificationConfigs;
        if (StringUtils.isEmpty(brand)) {
            notificationConfigs = notificationConfigDao.findDefaultByGroups(Collections.singleton(group));
        } else {
            notificationConfigs =
                notificationConfigDao.findDefaultByGroupsAndBrand(Collections.singleton(group), brand);
        }

        if (CollectionUtils.isEmpty(notificationConfigs)) {
            throw new NotFoundException(
                Collections.singletonList(NotificationCenterError.NOTIFICATION_CONFIG_NOT_FOUND.toMessage()));
        }
        return notificationConfigs;
    }
}
