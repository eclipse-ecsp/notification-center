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

package org.eclipse.ecsp.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import dev.morphia.Datastore;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.metrics.reporter.CumulativeLogger;
import org.eclipse.ecsp.analytics.stream.base.stores.HarmanPersistentKVStore;
import org.eclipse.ecsp.analytics.stream.base.stores.ObjectStateStore;
import org.eclipse.ecsp.analytics.stream.base.utils.Constants;
import org.eclipse.ecsp.analytics.stream.base.utils.ObjectUtils;
import org.eclipse.ecsp.domain.IgniteBaseException;
import org.eclipse.ecsp.domain.IgniteExceptionDataV1_1;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.AssociationDataV1_0;
import org.eclipse.ecsp.domain.notification.CampaignStatusDataV1_0;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.DisAssociationDataV1_0;
import org.eclipse.ecsp.domain.notification.NotificationSettingDataV1_0;
import org.eclipse.ecsp.domain.notification.PinDataV1_0;
import org.eclipse.ecsp.domain.notification.RefreshSchedulerData;
import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.RetryableException;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.domain.notification.utils.NotificationSettings;
import org.eclipse.ecsp.domain.notification.utils.ProcessingStatus;
import org.eclipse.ecsp.entities.AbstractIgniteEvent;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.scheduler.CreateScheduleEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleNotificationEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleOpStatusEventData;
import org.eclipse.ecsp.events.scheduler.ScheduleStatus;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.adaptor.NotificationEventFields.EventIdValues;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.config.CachedChannelServiceProviderConfigDAO;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigControlService;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.NotificationBufferDao;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.duplication.Deduplicator;
import org.eclipse.ecsp.notification.entities.BufferedAlertsInfo;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.notification.entities.NotificationSchedulerPayload;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.exception.UserProfileNotFoundException;
import org.eclipse.ecsp.notification.feedback.NotificationFeedbackHandler;
import org.eclipse.ecsp.notification.processors.AlertProcessorChain;
import org.eclipse.ecsp.notification.user.profile.UserProfileService;
import org.eclipse.ecsp.notification.userprofile.UserProfileIntegrationService;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.domain.EventID.COMPOSITE_EVENT;
import static org.eclipse.ecsp.domain.EventID.IGNITE_EXCEPTION_EVENT;
import static org.eclipse.ecsp.domain.EventID.SCHEDULE_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.EventID.SCHEDULE_OP_STATUS_EVENT;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.CANCELED;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.DONE;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.FAILED;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.READY;
import static org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status.STOPPED_BY_CONFIG;
import static org.eclipse.ecsp.domain.notification.commons.ChannelType.IVM;
import static org.eclipse.ecsp.domain.notification.commons.EventID.CAMPAIGN_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.CAMPAIGN_STATUS_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.CREATE_SECONDARY_CONTACT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.DELETE_SECONDARY_CONTACT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.DMA_FEEDBACK_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NON_REGISTERED_USER_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NOTIFICATION_SETTINGS;
import static org.eclipse.ecsp.domain.notification.commons.EventID.NOTIFICATION_USER_PROFILE;
import static org.eclipse.ecsp.domain.notification.commons.EventID.REFRESH_NOTIFICATION_SCHEDULER;
import static org.eclipse.ecsp.domain.notification.commons.EventID.UPDATE_SECONDARY_CONTACT;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_ACK;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH;
import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.DISASSOCIATION_TOPIC;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_ACK_GROUP;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_CAMPAIGN_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_TYPE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.STATUS;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.UNDERSCORE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.USERID;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Class that takes the notification events from the KAFKA, gets the user.
 * information from the PDID and sends the notification to the SNS service
 */
public class VehicleInfoNotification implements IgniteEventStreamProcessor {
    /**
     * ObjectMapper instance.
     */
    public static final ObjectMapper MAPPER;
    static final String OTHER_EVENTS = "OtherEvents";
    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleInfoNotification.class);
    private static final String NOTIFICATION_STATE_STORE = "notification-state-store";
    private static final CumulativeLogger CLOGGER = CumulativeLogger.getLogger();
    private static final String ALERTS_COUNTER = "AlertsReceived";
    private static final String MONGO_ALERTS_COUNTER = "AlertsInMongo";
    private static final String PIN = "_pin";

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    private final Set<String> filteredEventIds = new HashSet<>();
    private final Set<ChannelType> channelsSupported = new HashSet<>();
    @Autowired
    BiPredicate<IgniteEventStreamProcessor, StreamProcessingContext<IgniteKey<?>, IgniteEvent>> skipProcessorPredicate;
    private String[] notificationTopicsArray;
    private String notificationColln;
    private String alertsColln;
    private String userAlertsColln;
    private String wso2TenantSuffix;
    private String configCollection;
    private boolean counterPerPdid;
    private boolean checkUserPdidAssn;
    private ChannelNotifierRegistry channelNotifierRegistry;
    private Properties notificationProps;
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;
    private NotificationDao notificationDao;
    private CachedChannelServiceProviderConfigDAO cachedChannelServiceProviderConfigDao;
    private KeyValueStore<String, String> notificationStateStore;
    private Deduplicator deduplicator;
    private CampaignStore campaignStore;
    private Map<String[], String[]> sourceCache;
    @Autowired
    private Datastore datastore;
    @Autowired
    private AlertProcessorChain alertProcessorChain;
    @Autowired
    private NotificationConfigControlService configService;
    @Autowired
    private UserProfileService profileService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private NotificationBufferDao notificationBufferDao;
    @Autowired
    private NotificationConfigDAO notificationConfigDao;
    @Autowired
    private ScheduleNotificationAssistant scheduleNotificationAssistant;
    @Autowired
    private NotificationRetryAssistant notificationRetryAssistant;
    @Autowired
    private AlertsHistoryAssistant alertsHistoryAssistant;
    @Autowired
    private VehicleInfoNotificationNonRegisteredUser vehicleInfoNotificationNonRegisteredUser;
    @Autowired(required = false)
    private UserProfileIntegrationService userService;
    @Autowired
    private CoreVehicleProfileClient coreVehicleProfileClient;
    @Value("${enable.user.consent:false}")
    private boolean enableUserConsent;
    @Value("${locale.default.value}")
    private String defaultLocale;
    @Value("${" + PropertyNames.DLQ_REPROCESSING_ENABLED + ":false}")
    private boolean reprocessingEnabled;
    @Value("${enable.ignite.vehicle.profile:true}")
    private boolean igniteVehicleProfile;
    @Value("${notification.retry.topic}")
    private String notificationRetryTopic;

    @Value("${is.feedback.enabled}")
    private boolean isFeedBackEnabled;

    @Value("${is.default.feedback.topic.enabled}")
    private boolean isDefaultFeedbackTopicEnabled;

    @Value("${default.notification.feedback.topic}")
    private String defaultFeedbackTopic;



    private Map<String, String> eventIdMap;

    /**
     * publishes the given AlertsInfo through the given ChannelNotifier saves
     * the ChannelResponse to the given AlertsHistoryInfo object.
     */
    static void sendAlert(AlertsHistoryInfo alertHistoryObj, ChannelNotifier notifier, AlertsInfo alert,
                          IgniteEvent event) {
        ChannelResponse channelResponse = notifier.publish(alert);
        try {
            updateAlertHistory(alertHistoryObj, channelResponse);
        } finally {
            NotificationFeedbackHandler.sendNotificationSendingFeedback(alert, event, channelResponse,
                    alertHistoryObj.getId());
        }
    }

    /**
     * updates the AlertsHistoryInfo object with the given ChannelResponse.
     */
    private static void updateAlertHistory(AlertsHistoryInfo alertHistoryObj, ChannelResponse channelResponse) {
        alertHistoryObj.addChannelResponse(channelResponse);
        LOGGER.debug("DefaultAlertMessage from channel {} ", channelResponse.getDefaultMessage());
        if (StringUtils.isEmpty(alertHistoryObj.getAlertMessage())) {
            LOGGER.debug("adding DefaultMessage");
            alertHistoryObj.setAlertMessage(channelResponse.getDefaultMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ecsp.stream.base.StreamProcessor#init(org.eclipse.ecsp
     * .analytics.stream.base.StreamProcessingContext)
     */
    @Override
    public void init(StreamProcessingContext<IgniteKey<?>, IgniteEvent> spc) {
        LOGGER.info("Initializing vehicle info notification streaming app");
        this.ctxt = spc;
        deduplicator = new Deduplicator(notificationProps);
        campaignStore = new CampaignStore(notificationProps);
        channelNotifierRegistry = new ChannelNotifierRegistry();
        initNotificationDao(notificationProps);
        initCachedChannelServiceProviderConfigDao();
        getNcEventIdMap();
        LOGGER.info("Registered Beans in VehicleInfo Notification :"
                + Arrays.toString(applicationContext.getBeanDefinitionNames()));
        channelNotifierRegistry.init(this.cachedChannelServiceProviderConfigDao, notificationProps,
                spc.getMetricRegistry(), this.notificationDao, this.ctxt,
                channelsSupported, applicationContext);
        this.notificationStateStore = spc.getStateStore(NOTIFICATION_STATE_STORE);
        configService.setNotifierRegistry(channelNotifierRegistry);
        scheduleNotificationAssistant.init(ctxt);
        alertsHistoryAssistant.init(alertsColln, userAlertsColln);
        NotificationFeedbackHandler.init(ctxt, isFeedBackEnabled, isDefaultFeedbackTopicEnabled, defaultFeedbackTopic);
    }

    /**
     * Method to initialize the Configurations.
     */
    @Override
    public void initConfig(Properties props) {
        this.notificationProps = props;
        getNcEventIdMap();
        String topics = this.notificationProps.getProperty(NotificationProperty.SOURCE_TOPIC);
        ObjectUtils.requireNonEmpty(topics, "Alerts publishing topic not found.");
        notificationTopicsArray = topics.split(",");

        sourceCache = new HashMap<>();
        sourceCache.put(notificationTopicsArray, Arrays.stream(notificationTopicsArray).toArray(String[]::new));

        notificationColln = this.notificationProps.getProperty(NotificationProperty.NOTIFICATION_SETTINGS_COLLN_NAME);
        ObjectUtils.requireNonEmpty(notificationColln, "Notification Settings collection not found.");

        alertsColln = this.notificationProps.getProperty(NotificationProperty.ALERTS_COLLECTION_NAME);
        ObjectUtils.requireNonEmpty(alertsColln, "Alerts collection not found.");

        userAlertsColln = this.notificationProps.getProperty(NotificationProperty.USER_ALERTS_COLLECTION_NAME);
        ObjectUtils.requireNonEmpty(userAlertsColln, "User alerts collection not found.");

        configCollection = this.notificationProps.getProperty(NotificationProperty.NOTIFICATION_CONFIG_COLLECTION_NAME);
        ObjectUtils.requireNonEmpty(configCollection, "Notification config collection not found");

        checkUserPdidAssn = Boolean
                .parseBoolean(notificationProps.getProperty(NotificationProperty.CHECK_USER_PDID_ASSOCIATION));
        ObjectUtils.requireNonEmpty(checkUserPdidAssn, "checkUserPdidAssn Config Not found");

        scheduleNotificationAssistant.init(ctxt);
        LOGGER.debug("Source Topic Name : {}", topics);
        LOGGER.debug("Scheduler Source Topic Name : {}", scheduleNotificationAssistant.getSchedulerSourceTopic());

        wso2TenantSuffix = this.notificationProps.getProperty(NotificationProperty.WSO2_TENANT_SUFFIX, "");
        counterPerPdid = Boolean.parseBoolean(this.notificationProps.getProperty(PropertyNames.LOG_PER_PDID, "false"));

        String filter = this.notificationProps.getProperty(NotificationProperty.FILTERED_EVENT_IDS);
        if (StringUtils.isNotEmpty(filter)) {
            filteredEventIds.addAll(Arrays.asList(filter.split(Constants.COMMA)));
        }

        String channelsSupportedStr = this.notificationProps.getProperty(NotificationProperty.CHANNELS_SUPPORTED);
        if (StringUtils.isNotEmpty(channelsSupportedStr)) {
            for (String channel : channelsSupportedStr.split(Constants.COMMA)) {
                ChannelType channelType = ChannelType.getChannelType(channel);
                if (null != channelType) {
                    channelsSupported.add(channelType);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ecsp.stream.base.StreamProcessor#name()
     */
    @Override
    public String name() {
        return "VehicleInfoNotication";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ecsp.stream.base.StreamProcessor#process(java.lang.
     * Object, java.lang.Object)
     */
    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> kafkaRecord) {
        if (skipProcessorPredicate.test(this, ctxt)) { // if the target
            // processor is different
            // than this
            ctxt.forward(kafkaRecord);
            return;
        }

        process(kafkaRecord.key(), kafkaRecord.value(), this.ctxt.streamName());
    }

    /**
     * Method to call different kinds of processing based on the topic name.
     */
    public void process(IgniteKey<?> key, IgniteEvent value, String kafkaTopicName) {
        String keyString = null;
        if (key != null) {
            keyString = (String) key.getKey();
        }

        if (key == null || value == null) {
            LOGGER.error("Skipped key {} from {}.", keyString, kafkaTopicName);
            return;
        }
        LOGGER.info("Processing key {} from {}", keyString, kafkaTopicName);


        /*
         * In case DLQ Re-processing : Taking back up of the incoming payload
         * which is wrapper around the actual event to be processed and contains
         * the contextual data required during re-processing. else original
         * event
         */
        IgniteEvent originalEventOrEventToBeReprocessed = value;
        // This will not
        // work. It is
        // just new
        // reference.
        // Need deep
        // copy
        if (reprocessingEnabled && IGNITE_EXCEPTION_EVENT.equals(value.getEventId())
                && Version.V1_1.equals(value.getVersion())) {
            try {
                value = ((IgniteExceptionDataV1_1) value.getEventData()).getIgniteEvent(); // Trying
                // to
                // cast
                // to
                // the
                // expected
                // type
            } catch (Exception e) {
                throw new IgniteBaseException("Exception occurred in VehicleInfoNotification", false,
                        new RuntimeException(
                                "Event couldn't be re-processed as "
                                        +
                                        "payload is of invalid format or DLQ re-processing"
                                        +
                                        " is not enabled."), value, null);
            }
        }

        try {
            List<IgniteEvent> eventDataList;
            if (COMPOSITE_EVENT.equals(value.getEventId())) {
                eventDataList = value.getNestedEvents();
            } else {
                eventDataList = Collections.singletonList(value);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("notificationEvent matching alertPublishTopic:{}",
                        MAPPER.writeValueAsString(eventDataList));
            }
            CLOGGER.incrementByOne(ALERTS_COUNTER);
            if (counterPerPdid) {
                CLOGGER.incrementByOne(ALERTS_COUNTER + keyString);
            }

            Map<String, List<IgniteEvent>> events = filterAndGroupEvents(eventDataList, kafkaTopicName);
            handleEventsByType(key, value, keyString, originalEventOrEventToBeReprocessed, events);
        } catch (JsonProcessingException e) {
            LOGGER.error("Exception while parsing ignite events list: ", e);
        } catch (RetryableException e) {

            scheduleNotificationRetry(key, e, originalEventOrEventToBeReprocessed);

        } catch (IgniteBaseException e) {

            throw e;
        } catch (Exception e) {
            LOGGER.error(String.format("Caught an exception while processing key= %s for the topic %s", keyString,
                    kafkaTopicName), e);
        }
    }

    /**
     * Method to schedule the notification retry.
     *
     * @param key String
     * @param e RetryableException
     * @param originalEventOrEventToBeReprocessed IgniteEvent
     */
    private void scheduleNotificationRetry(IgniteKey<?> key, RetryableException e,
                                           IgniteEvent originalEventOrEventToBeReprocessed) {
        RetryRecord retryRecord = new RetryRecord(NotificationUtils.getRetryException(e), e.getMaxRetryLimit(), 0,
                e.getRetryInterval());
        IgniteEvent retryNotifEvent;
        try {
            retryNotifEvent =
                    notificationRetryAssistant.createRetryNotificationEvent(originalEventOrEventToBeReprocessed,
                            retryRecord, notificationTopicsArray[0]);
            LOGGER.info("Forwarding the retry event {} to topic {}", retryNotifEvent, notificationRetryTopic);
            ctxt.forwardDirectly(key, retryNotifEvent, notificationRetryTopic);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Unable to retry the event {}", ex.getMessage());
        }
    }

    /**
     * Method to handle the events by its type.
     *
     * @param key String
     * @param value IgniteEvent
     * @param keyString String
     * @param originalEventOrEventToBeReprocessed IgniteEvent
     * @param events Map of String, List of IgniteEvent
     * @throws JsonProcessingException JsonProcessingException
     */
    private void handleEventsByType(IgniteKey<?> key, IgniteEvent value, String keyString,
                                    IgniteEvent originalEventOrEventToBeReprocessed,
                                    Map<String, List<IgniteEvent>> events)
            throws JsonProcessingException {
        if (!isEmpty(events.get(REFRESH_NOTIFICATION_SCHEDULER))) {
            refreshNotificationScheduler(key, (IgniteEventImpl) events.get(REFRESH_NOTIFICATION_SCHEDULER).get(0));
        } else if (!isEmpty(events.get(NOTIFICATION_SETTINGS))) {
            handleNotificationSettingsEvents(key, events.get(NOTIFICATION_SETTINGS).get(0));
        } else if (!isEmpty(events.get(NOTIFICATION_USER_PROFILE))) {
            profileService.updateUserProfile(events.get(NOTIFICATION_USER_PROFILE).get(0));
        } else if (!isEmpty(events.get(CREATE_SECONDARY_CONTACT))) {
            profileService.createSecondaryContact(events.get(CREATE_SECONDARY_CONTACT).get(0));
        } else if (!isEmpty(events.get(UPDATE_SECONDARY_CONTACT))) {
            profileService.updateSecondaryContact(events.get(UPDATE_SECONDARY_CONTACT).get(0));
        } else if (!isEmpty(events.get(DELETE_SECONDARY_CONTACT))) {
            profileService.deleteSecondaryContact(events.get(DELETE_SECONDARY_CONTACT).get(0));
        } else {
            handleAllEvents(key, value, keyString, events);
        }

    }

    /**
     * Method to filter and group the events.
     *
     * @param key IgniteKey<?>
     * @param value IgniteEvent
     * @param keyString String
     * @param events Map of String, List of IgniteEvent
     * @throws JsonProcessingException JsonProcessingException
     */
    private void handleAllEvents(IgniteKey<?> key, IgniteEvent value, String keyString,
                                 Map<String, List<IgniteEvent>> events) throws JsonProcessingException {

        if (!isEmpty(events.get(SCHEDULE_OP_STATUS_EVENT))) {
            handleScheduleOpStatusEvents(key, events.get(SCHEDULE_OP_STATUS_EVENT));
        }
        if (!isEmpty(events.get(SCHEDULE_NOTIFICATION_EVENT))) {
            handleScheduleNotificationEvents(key, events.get(SCHEDULE_NOTIFICATION_EVENT));
        }
        if (!isEmpty(events.get(NOTIFICATION_ACK_GROUP))) {
            handleNotificationAckEvents(events.get(NOTIFICATION_ACK_GROUP));
        }
        if (!isEmpty(events.get(EventID.ASSOCIATION))) {
            handleAssociationEvents(key, events.get(EventID.ASSOCIATION));
        }
        if (!isEmpty(events.get(EventID.DISASSOCIATION))) {
            handleDisassociationEvents(key, events.get(EventID.DISASSOCIATION));
        }
        if (!isEmpty(events.get(VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT))) {
            processProfileChanged(value);
        }
        if (!isEmpty(events.get(OTHER_EVENTS))) {
            handleNormalAlertEvents(key, value, keyString, events.get(OTHER_EVENTS));
        }
        if (!isEmpty(events.get(EventID.PIN_GENERATED))) {
            handlePinGeneratedEvents(key, value, keyString, events.get(EventID.PIN_GENERATED));
        }
        if (!isEmpty(events.get(NON_REGISTERED_USER_NOTIFICATION_EVENT))) {
            vehicleInfoNotificationNonRegisteredUser.processNonRegisterUserEvent(key,
                    events.get(NON_REGISTERED_USER_NOTIFICATION_EVENT).get(0), channelsSupported,
                    channelNotifierRegistry, defaultLocale);
        }
        if (!isEmpty(events.get(CAMPAIGN_EVENT))) {
            handleNormalAlertEvents(key, value, keyString, events.get(CAMPAIGN_EVENT));
        }
        if (!isEmpty(events.get(CAMPAIGN_STATUS_EVENT))) {
            handleStatusEvents(events.get(CAMPAIGN_STATUS_EVENT));
        }
    }

    /**
     * Method to handle profile changed event.
     *
     * @param event IgniteEvent
     */
    private void processProfileChanged(IgniteEvent event) {
        VehicleProfileNotificationEventDataV1_1 data = (VehicleProfileNotificationEventDataV1_1) event.getEventData();
        List<VehicleProfileNotificationEventDataV1_1.ChangeDescription> changeDescriptions =
                data.getChangeDescriptions();
        for (VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription : changeDescriptions) {
            if (changeDescription.getOld() != null && changeDescription.getChanged() == null) {
                String userId = ((Map) ((List) changeDescription.getOld()).get(0)).get("userId").toString();
                String vehicleId = event.getVehicleId();
                notificationConfigDao.deleteNotificationConfigByUserAndVehicle(userId, vehicleId);
                scheduleNotificationAssistant.deleteScheduleNotifications(userId, vehicleId, event);
                profileService.deleteUserVehicleNickNames(userId, vehicleId);
            }
        }
    }

    /**
     * Method to handle refresh scheduler event.
     *
     * @param key IgniteKey<?>
     * @param igniteEvent IgniteEventImpl
     */
    private void refreshNotificationScheduler(IgniteKey<?> key, IgniteEventImpl igniteEvent) {
        String userId = ((RefreshSchedulerData) igniteEvent.getEventData()).getUserId();
        String contactId = ((RefreshSchedulerData) igniteEvent.getEventData()).getContactId();
        Set<String> groups = ((RefreshSchedulerData) igniteEvent.getEventData()).getGroups();
        LOGGER.info("Refresh scheduler for user: {}, vehicle: {}, contact: {}, and groups: {}", userId,
                igniteEvent.getVehicleId(), contactId, groups);
        List<NotificationConfig> notificationConfigs = notificationConfigDao
                .findByUserIdAndVehicleIdAndContactIdAndGroups(userId, igniteEvent.getVehicleId(), contactId, groups);
        NotificationSettingDataV1_0 notificationSettingDataV1 = new NotificationSettingDataV1_0(notificationConfigs);
        igniteEvent.setEventData(notificationSettingDataV1);
        try {
            updateScheduler(igniteEvent, key);
        } catch (JsonProcessingException e) {
            LOGGER.error("error while updating scheduler: ", e);
        }
    }

    /**
     * Method to handle notification settings event.
     *
     * @param key IgniteKey<?>
     * @param event IgniteEvent
     */
    private void handleNotificationSettingsEvents(IgniteKey<?> key, IgniteEvent event) {
        // decrypt NotificationConfig
        NotificationSettingDataV1_0 configData = (NotificationSettingDataV1_0) event.getEventData();
        configData.getNotificationConfigs().forEach(config -> configService.decryptNotificationConfig(config));
        configService.patchUpdateConfig(event);
        try {
            updateScheduler(event, key);
            LOGGER.debug("Scheduler updated successfully");
        } catch (Exception e) {
            LOGGER.error("Unable to update scheduler. Exception occurred", e);
        }
    }

    /**
     * Updates the Scheduler and NotificationBuffer when quiet period is updated.
     *
     * @throws JsonProcessingException Json Processing Exception
     */
    private void updateScheduler(IgniteEvent event, IgniteKey<?> key) throws JsonProcessingException {
        LOGGER.debug("Updating Scheduler before updating NotificationConfig");
        NotificationSettingDataV1_0 configData = (NotificationSettingDataV1_0) event.getEventData();

        for (NotificationConfig nc : configData.getNotificationConfigs()) {
            if (StringUtils.isEmpty(nc.getUserId())) {
                LOGGER.debug("UserProfile is null for the request {}", event);
                continue;
            }

            for (Channel c : nc.getChannels()) {
                NotificationBuffer nb = notificationBufferDao.findByUserIDVehicleIDChannelTypeGroupContactId(
                        nc.getUserId(), nc.getVehicleId(), c.getChannelType(), nc.getGroup(), nc.getContactId());

                if (isEmpty(c.getSuppressionConfigs()) && nb != null) {
                    // Notification buffer exists for the user per vehicle,
                    // which means that a
                    // notification is waiting to be sent, but no suppression
                    // config, therefore
                    // resent the notification
                    resendNotificationsSavedInBuffer(key, nb.getSchedulerId(), event);
                } else if (nb != null) {
                    // SuppressionConfigs were updated, check if there are any
                    // scheduler events that
                    // need to be updated and update them
                    SuppressionConfig suppression =
                            scheduleNotificationAssistant.enforceSuppression(c.getSuppressionConfigs(),
                                    nc.getUserId());
                    if (suppression == null) { // if its is not Quiet Period now
                        // then publish the saved alerts
                        resendNotificationsSavedInBuffer(key, nb.getSchedulerId(), event);
                    } else {
                        // if it is Quiet Period now then update the scheduler
                        // in NotificationBuffer
                        scheduleNotificationAssistant.updateScheduler(suppression, nc, c.getChannelType(), nb, key,
                                event, sources()[0]);
                    }
                }
            }
        }
    }

    /**
     * Method to handle schedule op status events.
     *
     * @param key IgniteKey<?>
     * @param scheduleOpStatusEvent List of IgniteEvent
     */
    private void handleScheduleOpStatusEvents(IgniteKey<?> key, List<IgniteEvent> scheduleOpStatusEvent) {
        scheduleOpStatusEvent.forEach(event -> processSchedulerOpStatusEvent(event, key));
    }

    /**
     * Method to handle scheduled notification events.
     *
     * @param key IgniteKey<?>
     * @param scheduleNotificationEvent List of IgniteEvent
     */
    private void handleScheduleNotificationEvents(IgniteKey<?> key, List<IgniteEvent> scheduleNotificationEvent) {
        for (IgniteEvent event : scheduleNotificationEvent) {
            ScheduleNotificationEventData snoozedNotificationAlert = (ScheduleNotificationEventData) event
                    .getEventData();
            String schedulerId = snoozedNotificationAlert.getScheduleIdId();
            NotificationBuffer notificationBuffer = getNotificationBuffer(schedulerId);
            if (notificationBuffer == null || schedulerId == null) {
                continue;
            }
            ChannelType channelType = notificationBuffer.getChannelType();
            for (BufferedAlertsInfo bufferedAlert : notificationBuffer.getAlertsInfo()) {
                processBufferedAlert(key, event, bufferedAlert, notificationBuffer, channelType, schedulerId);
            } // for
        } // for
    }

    /**
     * Method to process buffered alert.
     *
     * @param key IgniteKey<?>
     * @param event IgniteEvent
     * @param bufferedAlert BufferedAlertsInfo
     * @param notificationBuffer NotificationBuffer
     * @param channelType ChannelType
     * @param schedulerId String
     */
    private void processBufferedAlert(IgniteKey<?> key, IgniteEvent event,
                                      BufferedAlertsInfo bufferedAlert,
                                      NotificationBuffer notificationBuffer, ChannelType channelType,
                                      String schedulerId) {
        AlertsInfo alert = setAlertInfo(bufferedAlert);
        try {

            String vehicleId = notificationBuffer.getVehicleId();
            if (vehicleId == null) {
                // Setting default vehicle id for the case when sending
                // a notification to user
                // with no vehicle
                vehicleId = alert.getNotificationConfig().getVehicleId();
            }
            List<NotificationConfig> originalConfigs = notificationConfigDao.findByUserVehicleContactId(
                    notificationBuffer.getUserId(), vehicleId, alert.getNotificationConfig().getContactId());
            List<SuppressionConfig> suppressionConfigs = originalConfigs.stream()
                    .filter(g -> g.getGroup().equals(alert.getNotificationConfig().getGroup()))// only
                    // single
                    // notif.conf
                    // is
                    // expected
                    .findFirst().map(config -> config.getChannel(channelType).getSuppressionConfigs())
                    .orElse(Collections.emptyList());
            SuppressionConfig suppression = scheduleNotificationAssistant.enforceSuppression(suppressionConfigs,
                    notificationBuffer.getUserId());
            if (suppression == null) { // if its is not Quiet Period now
                // then publish the saved alerts
                resendNotificationsSavedInBuffer(key, schedulerId, event);
            } else { // if it is still Quiet Period, then update the
                // scheduler in NotificationBuffer
                scheduleNotificationAssistant.updateScheduler(suppression, alert.getNotificationConfig(),
                        channelType, notificationBuffer, key, event, sources()[0]);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while handling scheduled notification event " + alert.toString(),
                    e);
        }
    }

    /**
     * Method to handle Notification Ack Events.
     *
     * @param notificationAckEventList List of IgniteEvent
     */
    private void handleNotificationAckEvents(List<IgniteEvent> notificationAckEventList) {
        for (IgniteEvent event : notificationAckEventList) {
            if (EventID.DFF_FEEDBACK_EVENT.equals(event.getEventId())) {
                ChannelNotifier channelNotifier = channelNotifierRegistry.channelNotifier(ChannelType.API_PUSH);

                channelNotifier.processAck(event);
            } else if (EventID.VEHICLE_MESSAGE_ACK.equals(event.getEventId())
                    || EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH.equals(event.getEventId())
                    || EventID.DMA_FEEDBACK_EVENT.equals(event.getEventId())) {
                ChannelNotifier channelNotifier = channelNotifierRegistry.channelNotifier(IVM);

                channelNotifier.processAck(event);
            }
        }
    }

    /**
     * Method to handle association events.
     *
     * @param key IgniteKey<?>
     * @param associationEventList List of IgniteEvent
     */
    private void handleAssociationEvents(IgniteKey<?> key, List<IgniteEvent> associationEventList) {
        for (IgniteEvent event : associationEventList) {
            String userId = ((AssociationDataV1_0) event.getEventData()).getUserId();
            String actualUserId = StringUtils.removeEnd(userId, wso2TenantSuffix);
            LOGGER.debug("Received user {} Actual user {} for pdid {}", userId, actualUserId, key);
            put(key.getKey().toString(), actualUserId);
        }
    }

    /**
     * Method to handle disassociation events.
     *
     * @param key IgniteKey<?>
     * @param disAssociationEventList List of IgniteEvent
     */
    private void handleDisassociationEvents(IgniteKey<?> key, List<IgniteEvent> disAssociationEventList) {
        for (IgniteEvent event : disAssociationEventList) {
            String userId = ((DisAssociationDataV1_0) event.getEventData()).getUserId();
            String actualUserId = StringUtils.removeEnd(userId, wso2TenantSuffix);
            LOGGER.debug("Received Disassociation event for user {}, Actual user {}, for pdid {}", userId, actualUserId,
                    key);
            delete(key.getKey().toString());
            LOGGER.info("Deleted the key {} from statestore since the device is disassociated", key);
        }
    }

    /**
     * Method to handle normal alert events.
     *
     * @param key IgniteKey<?>
     * @param value IgniteEvent
     * @param pdid String
     * @param normalAlertsEventList List IgniteEvent
     * @throws JsonProcessingException JsonProcessingException
     */
    private void handleNormalAlertEvents(IgniteKey<?> key, IgniteEvent value, String pdid,
                                         List<IgniteEvent> normalAlertsEventList) throws JsonProcessingException {
        String userId = "";
        boolean isNonRegisteredVehicle = false;

        for (IgniteEvent e : normalAlertsEventList) {
            if (((AbstractIgniteEvent) e).getEventData() instanceof GenericEventData) {
                GenericEventData genericEventData = (GenericEventData) ((AbstractIgniteEvent) e).getEventData();

                if (genericEventData.getData(NotificationConstants.NON_REGISTERED_VEHICLE_FIELD).isPresent()) {
                    isNonRegisteredVehicle =
                            (Boolean) genericEventData.getData(
                                    NotificationConstants.NON_REGISTERED_VEHICLE_FIELD).get();
                }
                if (isNonRegisteredVehicle && genericEventData.getData(NotificationConstants.USERID).isPresent()) {
                    userId = (String) genericEventData.getData(NotificationConstants.USERID).get();
                }
            }
        }

        if (!isNonRegisteredVehicle && checkUserPdidAssn) {
            userId = getUserId(pdid);
            LOGGER.debug("userID from state store:{}", userId);
        }
        LOGGER.debug("Vehicle {} isNonRegisteredVehicle {} and userID {}", pdid, isNonRegisteredVehicle, userId);

        checkVehUsrAssociationAndProcessAlert(key, value, pdid, normalAlertsEventList, userId);
    }

    /**
     * Method to check vehicle user association and process alert.
     *
     * @param key IgniteKey<?>
     * @param value IgniteEvent
     * @param pdid String
     * @param normalAlertsEventList List of IgniteEvent
     * @param userId String
     * @throws JsonProcessingException JsonProcessingException
     */
    private void checkVehUsrAssociationAndProcessAlert(IgniteKey<?> key, IgniteEvent value, String pdid,
                                                       List<IgniteEvent> normalAlertsEventList,
                                                       String userId) throws JsonProcessingException {
        if (!checkUserPdidAssn || !StringUtils.isEmpty(userId)) {
            List<AlertsInfo> alertsList = NotificationUtils
                    .getListObjects(MAPPER.writeValueAsString(normalAlertsEventList), AlertsInfo.class);
            for (int i = 0; i < alertsList.size(); i++) {
                AlertsInfo alert = alertsList.get(i);
                alert.setPdid(pdid);
                if (StringUtils.isNotEmpty(userId)) {
                    alert.getAlertsData().set(USERID, userId);
                }
                AbstractIgniteEvent event = (AbstractIgniteEvent) normalAlertsEventList.get(i);
                if (StringUtils.isEmpty(event.getVehicleId())) {
                    event.setVehicleId(pdid);
                }
                alert.setIgniteEvent(event);
            }
            LOGGER.debug("alertsList: {}", alertsList);
            processAlertInfoList(alertsList, userId, pdid, key, value);
        } else {
            LOGGER.error("Couldn't find the user id for the pdid:{}. Alerts will not be send", pdid);
        }
    }

    /**
     * Method to handle status events.
     *
     * @param eventList List of IgniteEvent
     */
    private void handleStatusEvents(List<IgniteEvent> eventList) {
        LOGGER.info("handleStatusEvents eventList - {}", eventList);
        eventList.stream().forEach(event -> {
            CampaignStatusDataV1_0 campaignData = (CampaignStatusDataV1_0) event.getEventData();
            if (Objects.nonNull(campaignData) && StringUtils.isNotEmpty(campaignData.getCampaignId())
                    && NOTIFICATION_TYPE.equals(campaignData.getType())) {
                String redisKey = campaignData.getCampaignId() + UNDERSCORE + STATUS;
                campaignStore.put(redisKey, campaignData.getStatus());
                LOGGER.info("Saved status as {} in cache for campaignId:{}", campaignData.getStatus(),
                        campaignData.getCampaignId());
            }
        });
    }

    /**
     * Method to handle pin generated events.
     *
     * @param key IgniteKey<?>
     * @param value IgniteEvent
     * @param keyString String
     * @param pinGeneratedEventList List of IgniteEvent
     * @throws JsonProcessingException JsonProcessingException
     */
    private void handlePinGeneratedEvents(IgniteKey<?> key, IgniteEvent value, String keyString,
                                          List<IgniteEvent> pinGeneratedEventList) throws JsonProcessingException {
        String userId = keyString + PIN;
        List<IgniteEvent> settings = getNotificationSettingEvents(pinGeneratedEventList);
        setupNotificationService(userId, settings);
        List<AlertsInfo> alertsList = NotificationUtils.getListObjects(MAPPER.writeValueAsString(pinGeneratedEventList),
                AlertsInfo.class);
        alertsList.forEach(a -> a.setPdid(userId));
        for (int i = 0; i < pinGeneratedEventList.size(); i++) {
            alertsList.get(i).setIgniteEvent(pinGeneratedEventList.get(i));
            alertsList.get(i).getAlertsData().set(NotificationConstants.USERID, userId);
            // Logic seems wrong here
            // This line expects settings to have the same size as
            // pinGeneratedEventList.
            // Looking at the getNotificationSettingEvents method which created
            // this list, shows that it's not always true
            NotificationConfig config = ((NotificationSettingDataV1_0) settings.get(i).getEventData())
                    .getNotificationConfigs().get(0);
            config.setUserId(userId);
            alertsList.get(i).addNotificationConfig(config);
        }
        LOGGER.debug("alertsList :{}", alertsList);
        processAlertInfoList(alertsList, userId, null, key, value);
    }

    /**
     * Filter and group the events.
     *
     * @param events List of IgniteEvent
     * @param kafkaTopicName String
     * @return Map of String and List of IgniteEvent
     */
    Map<String, List<IgniteEvent>> filterAndGroupEvents(List<IgniteEvent> events, String kafkaTopicName) {
        LOGGER.info("Events Received {}", events);
        Map<String, List<IgniteEvent>> filtered = events.stream()
                .filter(event -> !filteredEventIds.contains(event.getEventId()))
                .collect(Collectors.groupingBy(event -> {
                    if (eventIdMap.get(event.getEventId()) != null) {
                        return  eventIdMap.get(event.getEventId());
                    } else if (VEHICLE_MESSAGE_ACK.equals(event.getEventId())
                            || VEHICLE_MESSAGE_DISPOSITION_PUBLISH.equals(event.getEventId())
                            || EventID.DFF_FEEDBACK_EVENT.equals(event.getEventId())
                            || DMA_FEEDBACK_EVENT.equals(event.getEventId())) {
                        return NOTIFICATION_ACK_GROUP;
                    } else if (DISASSOCIATION_TOPIC.equals(kafkaTopicName)
                            && VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT.equals(event.getEventId())) {
                        return VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT;
                    } else {
                        return OTHER_EVENTS;
                    }
                }));
        LOGGER.debug("Events to be processed {}", filtered);
        return filtered;
    }

    /**
     * Method to get notification setting events.
     *
     * @param pinEvents List of IgniteEvent
     * @return List of IgniteEvent
     */
    private List<IgniteEvent> getNotificationSettingEvents(List<IgniteEvent> pinEvents) {
        List<IgniteEvent> settings = new ArrayList<>();
        for (IgniteEvent pinEvent : pinEvents) {
            IgniteEvent setting =
                    NotificationSettings.createNotificationSettingEvent((PinDataV1_0) pinEvent.getEventData());
            if (setting != null) {
                settings.add(setting);
            }
        }
        return settings;
    }

    /**
     * Method to process alert info list.
     *
     * @param alertsList List of AlertsInfo
     * @param userId String
     * @param pdid String
     * @param igniteKey IgniteKey<?>
     * @param event IgniteEvent
     */
    @SuppressWarnings("PlaceholderCountMatchesArgumentCount")
    private void processAlertInfoList(List<AlertsInfo> alertsList, String userId, String pdid, IgniteKey<?> igniteKey,
                                      IgniteEvent event) {
        LOGGER.info("Received alert list: {}", alertsList);
        alertsList = deduplicator.filterDuplicateAlert(alertsList);
        LOGGER.debug("Remain alerts after deduplicator filtered alert list: {}",
                alertsList.stream().map(AlertsInfo::getEventID));
        for (AlertsInfo alert : alertsList) {
            AlertsHistoryInfo alertHistory = new AlertsHistoryInfo();
            try {
                alertHistory = alertsHistoryAssistant.createBasicAlertHistory(alert);
                alertHistory.addStatus(READY);
                processAlertBasedOnStatus(pdid, igniteKey, event, alert, alertHistory);
            } catch (UserProfileNotFoundException e) {
                alertHistory.addStatus(FAILED);

                setProcessingStatus(NotificationConstants.FAILURE, alertHistory, e.getMessage());
                NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(alert, igniteKey, event, alertHistory);
                throw e;

            } catch (RetryableException e) {

                RetryRecord rc = new RetryRecord(NotificationUtils.getRetryException(e), e.getMaxRetryLimit(), 0,
                        e.getRetryInterval());

                notificationRetryAssistant.updateAlertHistoryForRetry(alertHistory, rc);
                alertsHistoryAssistant.setEnrichedAlertHistory(alert, alertHistory);
                alertsHistoryAssistant.saveAlertHistory(userId, pdid, alert, alertHistory);

                throw e;

            } catch (Exception e) {

                alertHistory.addStatus(FAILED);
                LOGGER.error(MessageFormat.format("Processing alert failed : {0}", alert), e);
                setProcessingStatus(NotificationConstants.FAILURE, alertHistory, e.getMessage());
            }
            alertsHistoryAssistant.setEnrichedAlertHistory(alert, alertHistory);
            alertsHistoryAssistant.saveAlertHistory(userId, pdid, alert, alertHistory);
            LOGGER.info("Calling feedback Handler");
            NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(alert, igniteKey, event, alertHistory);

        }
    }

    /**
     * Method to process alert based on status.
     *
     * @param pdid String
     * @param key IgniteKey<?>
     * @param event IgniteEvent
     * @param alert AlertsInfo
     * @param history AlertsHistoryInfo
     */
    private void processAlertBasedOnStatus(String pdid, IgniteKey<?> key, IgniteEvent event, AlertsInfo alert,
                                           AlertsHistoryInfo history) {
        if (alert.getEventID().equalsIgnoreCase(CAMPAIGN_EVENT)
                &&
                isCanceledCampaign((String) alert.getAlertsData().any().get(NOTIFICATION_CAMPAIGN_ID))) {
            history.addStatus(CANCELED);
            LOGGER.debug("Alert processing canceled : {}", alert);
            NotificationFeedbackHandler.sendCampaignLifecycleFeedback(alert, event, key, CANCELED.name(), "nc-403",
                    "Campaign canceled by CM");
        } else {
            processAlertInfo(pdid, key, event, alert, history);
        }
    }

    /**
     * Method to process alert info.
     *
     * @param pdid String
     * @param igniteKey IgniteKey<?>
     * @param event IgniteEvent
     * @param alert AlertsInfo
     * @param alertHistoryObj AlertsHistoryInfo
     */
    private void processAlertInfo(String pdid, IgniteKey<?> igniteKey, IgniteEvent event, AlertsInfo alert,
                                  AlertsHistoryInfo alertHistoryObj) {
        alertProcessorChain.process(alert);
        setProcessingStatus(NotificationConstants.SUCCESS, alertHistoryObj, null);
        LOGGER.info("Processing alert : {}", alert.getEventID());
        alert.setNotificationConfig(alert.getNotificationConfigs().get(0));
        if (enableUserConsent && alert.getAlertsData().getUserProfile() != null
                &&
                !alert.getAlertsData().getUserProfile().isConsent()) {
            alertHistoryObj.addStatus(STOPPED_BY_CONFIG);
            LOGGER.warn("No user consent, skipped alert {}", alert);
            return;
        }
        if (!alert.getNotificationConfig().isEnabled()) {
            alertHistoryObj.addStatus(STOPPED_BY_CONFIG);
            LOGGER.warn("Not enabled for group {} skipped alert {}", alert.getNotificationConfig().getGroup(), alert);
            return;
        }
        ((AbstractIgniteEvent) alert.getIgniteEvent()).setVehicleId(pdid);
        String userProfileUserId =
                alert.getAlertsData().getUserProfile() != null ? alert.getAlertsData().getUserProfile().getUserId()
                        : null;
        LOGGER.debug("Processing alert for {} Event {}", alert.getPdid(), alert.getEventID());
        LOGGER.debug("Alert History: {} channelsSupported : {}", alertHistoryObj, channelsSupported);
        alert.getNotificationConfigs().forEach(config -> {
            processAlertPerConfig(igniteKey, event, alert, alertHistoryObj, config, userProfileUserId);
        });

        alertHistoryObj.addStatus(DONE);
        if (!isEmpty(alertHistoryObj.getChannelResponses())) {
            CLOGGER.incrementByOne(MONGO_ALERTS_COUNTER);
        }




    }

    /**
     * setProcessingStatus method.
     *
     * @param status String
     * @param alertHistoryObj AlertsHistoryInfo
     * @param errorMsg String
     */
    private void setProcessingStatus(String status, AlertsHistoryInfo alertHistoryObj, String errorMsg) {
        ProcessingStatus processingStatus = alertHistoryObj.getProcessingStatus();
        if (processingStatus == null) {
            processingStatus = new ProcessingStatus(status, errorMsg);
        } else {
            processingStatus.setStatus(status);
            processingStatus.setErrorMessage(errorMsg);
        }
        alertHistoryObj.setProcessingStatus(processingStatus);

    }

    /**
     * Method to process alert per config.
     *
     * @param igniteKey IgniteKey<?>
     * @param event IgniteEvent
     * @param alert AlertsInfo
     * @param alertHistoryObj AlertsHistoryInfo
     * @param config NotificationConfig
     * @param userProfileUserId String
     */
    private void processAlertPerConfig(IgniteKey<?> igniteKey, IgniteEvent event, AlertsInfo alert,
                                       AlertsHistoryInfo alertHistoryObj, NotificationConfig config,
                                       String userProfileUserId) {
        String contactId = config.getContactId();
        LOGGER.debug("Settings notification config for the following contact id {}", contactId);
        alert.setNotificationConfig(config);
        for (Channel channel : alert.getNotificationConfig().getEnabledChannels()) {
            sendAlertForConfiguredChannels(igniteKey, event, alert, alertHistoryObj, config,
                    userProfileUserId, channel);
        }
    }

    /**
     * Method to send alert for configured channels.
     *
     * @param igniteKey IgniteKey<?>
     * @param event IgniteEvent
     * @param alert AlertsInfo
     * @param alertHistoryObj AlertsHistoryInfo
     * @param config NotificationConfig
     * @param userProfileUserId String
     * @param channel Channel
     */
    private void sendAlertForConfiguredChannels(IgniteKey<?> igniteKey, IgniteEvent event, AlertsInfo alert,
                                                AlertsHistoryInfo alertHistoryObj, NotificationConfig config,
                                                String userProfileUserId, Channel channel) {
        String contactId = config.getContactId();
        ChannelType channelType = channel.getChannelType();
        Data data = alert.getAlertsData();
        String notificationId = data.getNotificationId();
        String region = fetchRegion(data);
        LOGGER.debug("Processing channel type {}", channelType);
        if (channelsSupported.contains(channelType)) {
            // check notification is not muted
            if (isMutedVin(alert, channelType)) {
                // not muted continue to process
                ChannelNotifier notifier =
                        channelNotifierRegistry.channelNotifier(channelType, notificationId, region);

                try {
                    // Suppression enforcement
                    SuppressionConfig suppression = scheduleNotificationAssistant
                            .enforceSuppression(channel.getSuppressionConfigs(), userProfileUserId);
                    if (suppression == null) {
                        LOGGER.debug("Quiet time not in effect, sending notification.");
                        sendAlert(alertHistoryObj, notifier, alert, event);
                    } else {
                        LOGGER.info(
                                "Quiet time IS in effect, saving "
                                        +
                                        "notification to send later. User id {} config: {}, suppression is: {}",
                                config.getUserId(), config, suppression);
                        scheduleNotificationAssistant.snoozeAlert(igniteKey, event, alert,
                                contactId, channelType, suppression, sources()[0]);
                        setSkippedChannelResponse(channelType, alertHistoryObj,
                                String.format(
                                        "Quiet Time is in effect,notification "
                                                + "to be sent later. User id %s config: %s, suppression is: %s",
                                        config.getUserId(), config.getId(), suppression));

                    }
                } catch (Exception e) {
                    LOGGER.error(MessageFormat.format(
                            "Exception occurred while publishing "
                                    +
                                    "alerts {0} to channel type {1}. Continuing publishing to other channels",
                            alert, channelType), e);
                    setSkippedChannelResponse(channelType, alertHistoryObj, e.getMessage());

                }
            } else {
                LOGGER.debug("Notification was muted by admin for vehicle {}",
                        alert.getMuteVehicle().getVehicleId());
                setSkippedChannelResponse(channelType, alertHistoryObj,
                        String.format("Notification was muted by admin for vehicle : %s",
                                alert.getMuteVehicle().getVehicleId()));


            }
        } else {
            LOGGER.debug("{} channel not Supported. Alert will not be send", channelType);
            setSkippedChannelResponse(channelType, alertHistoryObj,
                    "Channel not Supported. Alert will not be send");


        }
    }

    /**
     * setSkippedChannelResponse method.
     *
     * @param channelType ChannelType
     * @param alertHistoryObj AlertsHistoryInfo
     * @param failureReason String
     */
    private void setSkippedChannelResponse(
            ChannelType channelType, AlertsHistoryInfo alertHistoryObj, String failureReason) {

        Map<String, String> skippedChannels = alertHistoryObj.getSkippedChannels();
        skippedChannels.put(channelType.getChannelType(), failureReason);
    }

    /**
     * Method to check if the notification is muted.
     *
     * @param alert AlertsInfo
     * @param channelType ChannelType
     * @return boolean
     */
    private boolean isMutedVin(AlertsInfo alert, ChannelType channelType) {
        return alert.getMuteVehicle() == null
                || !isMuteVehicleApplied(alert.getMuteVehicle(),
                channelType, alert.getNotificationConfig());
    }

    /**
     * Method to fetch region.
     *
     * @param data Data
     * @return String
     */
    private String fetchRegion(Data data) {
        String region = NotificationConstants.EMPTY;
        if (data.getVehicleProfile() != null) {
            region =
                    data.getVehicleProfile().getSoldRegion() != null
                            ? data.getVehicleProfile().getSoldRegion() :
                            NotificationConstants.EMPTY;
        }
        return region;
    }

    /**
     * Method to check if the campaign is canceled.
     *
     * @param campaignId String
     * @return boolean
     */
    private boolean isCanceledCampaign(String campaignId) {
        String key = campaignId + UNDERSCORE + STATUS;
        return (StringUtils.isNotEmpty(campaignId)
                &&
                CANCELED.name().equalsIgnoreCase((String) campaignStore.get(key)));
    }

    /**
     * Method to check if the mute vehicle is applied.
     *
     * @param muteVehicle MuteVehicle
     * @param channelType ChannelType
     * @param notificationConfig  NotificationConfig
     * @return boolean
     */
    private boolean isMuteVehicleApplied(MuteVehicle muteVehicle, ChannelType channelType,
                                         NotificationConfig notificationConfig) {
        LOGGER.debug("isMuteVehicleApplied {} ; {}", channelType, notificationConfig.getGroup());
        return
                (CollectionUtils.isEmpty(muteVehicle.getChannels())
                        || muteVehicle.getChannels().contains(channelType))
                        &&
                        (CollectionUtils.isEmpty(muteVehicle.getGroups())
                                ||
                                muteVehicle.getGroups().contains(notificationConfig.getGroup()))
                        &&
                        isMutePeriod(muteVehicle);
    }

    /**
     * Method to check if the mute period is applied.
     *
     * @param muteVehicle MuteVehicle
     * @return boolean
     */
    private boolean isMutePeriod(MuteVehicle muteVehicle) {
        long now = System.currentTimeMillis();
        LOGGER.debug("isMutePeriod now = {}; start = {}; end = {}", now, muteVehicle.getStartTime(),
                muteVehicle.getEndTime());
        return ((muteVehicle.getStartTime() == null || muteVehicle.getStartTime() == 0
                ||
                muteVehicle.getStartTime() <= now)
                &&
                (muteVehicle.getEndTime() == null || muteVehicle.getEndTime() == 0 || muteVehicle.getEndTime() >= now));
    }

    /**
     * Saves SchedulerId and ScheluerEvent Payload to NotificationBuffer in.
     * Mongo
     */
    @SuppressWarnings("PlaceholderCountMatchesArgumentCount")
    private void processSchedulerOpStatusEvent(IgniteEvent schedulerEvent, IgniteKey<?> key) {
        ScheduleOpStatusEventData eventData = (ScheduleOpStatusEventData) schedulerEvent.getEventData();
        String scheduleId = eventData.getScheduleId();
        ScheduleStatus status = eventData.getStatus();
        boolean valid = eventData.isValid();
        ScheduleOpStatusEventData.ScheduleOpStatusErrorCode statusErrorCode = eventData.getStatusErrorCode();
        if (status == ScheduleStatus.CREATE) {
            CreateScheduleEventData scheduleEventData = (CreateScheduleEventData) eventData.getIgniteEvent()
                    .getEventData();
            String notificationPayload =
                    new String(scheduleEventData.getNotificationPayload(), Charset.defaultCharset());
            if (valid) {
                try {
                    NotificationSchedulerPayload notificationSchedulerPayload = MAPPER.readValue(notificationPayload,
                            NotificationSchedulerPayload.class);
                    NotificationBuffer buffer = scheduleNotificationAssistant.getNotificationBuffer(
                            notificationSchedulerPayload.getChannelType(), notificationSchedulerPayload.getUserID(),
                            notificationSchedulerPayload.getVehicleID(), notificationSchedulerPayload.getGroup(),
                            notificationSchedulerPayload.getContactId());
                    if (buffer != null) {
                        buffer.setSchedulerId(scheduleId);
                        notificationBufferDao.update(buffer);
                    }
                    if (notificationSchedulerPayload.isSchedulerUpdateFlag()) {
                        // schedulerUpdateFlag will be true the request was
                        // generated to update the
                        // Scheduler else False
                        IgniteEvent deleteScheduleEvent = scheduleNotificationAssistant.createDeleteScheduleEventData(
                                key, schedulerEvent, notificationSchedulerPayload.getSchedulerId());
                        ctxt.forwardDirectly(key, deleteScheduleEvent,
                                scheduleNotificationAssistant.getSchedulerSourceTopic());
                    }
                } catch (IOException e) {
                    LOGGER.error("Exception occurred while saving notification SchedulerId to buffer", e);
                }
            } else {
                LOGGER.error("Error saving ScheduledId. valid field is false {}", eventData);
            }
        } else {
            LOGGER.debug(
                    "NotificationStreamProcessor has received schedule "
                            +
                            "op status event: key={}, value={}, scheduleId={}, "
                            +
                            "status={}, valid={}, statusErrorCode={}, Ignoring the Event.",
                    key, schedulerEvent, scheduleId, status, valid, statusErrorCode);
        }
    }

    /**
     * Overloaded getNotificationBuffer method, returns a NotificationBuffer.
     * Object from mongo based on schedulerId
     */
    private NotificationBuffer getNotificationBuffer(String schedulerId) {
        LOGGER.debug("finding NotificationBuffer based on schedulerId");
        return notificationBufferDao.findBySchedulerId(schedulerId);
    }

    /**
     * Processess notifications stored in NotificationBuffer.
     *
     * @param key         IgniteKey
     * @param schedulerId String
     * @param event       IgniteEvent
     */
    @SuppressWarnings("PlaceholderCountMatchesArgumentCount")
    private void resendNotificationsSavedInBuffer(IgniteKey<?> key, String schedulerId, IgniteEvent event) {
        LOGGER.debug("Resend snoozed Notifications");
        NotificationBuffer notification = getNotificationBuffer(schedulerId);
        if (notification != null && schedulerId != null) {
            ChannelType channelType = notification.getChannelType();
            for (BufferedAlertsInfo bufferedAlert : notification.getAlertsInfo()) {
                AlertsInfo alert = setAlertInfo(bufferedAlert);
                AlertsHistoryInfo alertHistoryObj = Optional
                        .of(alertsHistoryAssistant.getAlertHistory(alert.getIgniteEvent().getRequestId()))
                        .orElse(new AlertsHistoryInfo());
                alertsHistoryAssistant.setEnrichedAlertHistory(alert, alertHistoryObj);
                LOGGER.debug("Alert History: {} ", alertHistoryObj);
                Data data = alert.getAlertsData();

                fetchRegionAndSendAlert(event, data, channelType, alertHistoryObj, alert);
                if (!isEmpty(alertHistoryObj.getChannelResponses())) {
                    alertsHistoryAssistant.saveAlertHistory(notification.getUserId(), notification.getVehicleId(),
                            alert, alertHistoryObj);
                    CLOGGER.incrementByOne(MONGO_ALERTS_COUNTER);
                }
                NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                        alert, key, event, alertHistoryObj);

            }
            // delete Entry from NotificationBuffer And delete Scheduler
            notificationBufferDao.deleteById(notification.getId());
            IgniteEvent deleteScheduleEvent = scheduleNotificationAssistant.createDeleteScheduleEventData(key, event,
                    schedulerId);
            ctxt.forwardDirectly(key, deleteScheduleEvent, scheduleNotificationAssistant.getSchedulerSourceTopic());

        } else {
            LOGGER.error("SchedulerId not found");
        }
    }

    /**
     * Method to fetch region and send alert.
     *
     * @param event           IgniteEvent
     * @param data            Data
     * @param channelType     ChannelType
     * @param alertHistoryObj AlertsHistoryInfo
     * @param alert           AlertsInfo
     */
    private void fetchRegionAndSendAlert(IgniteEvent event, Data data, ChannelType channelType,
                                         AlertsHistoryInfo alertHistoryObj, AlertsInfo alert) {
        String region = NotificationConstants.EMPTY;
        String notificationId = data.getNotificationId();
        if (data.getVehicleProfile() != null) {
            region =
                    data.getVehicleProfile().getSoldRegion() != null
                            ? data.getVehicleProfile().getSoldRegion() :
                            NotificationConstants.EMPTY;
        }
        ChannelNotifier notifier = channelNotifierRegistry.channelNotifier(channelType, notificationId, region);

        try {
            sendAlert(alertHistoryObj, notifier, alert, event);
        } catch (Exception e) {
            LOGGER.error(MessageFormat.format(
                    "Exception occurred while publishing alerts {0} "
                            +
                            "to channel type {1}. Continuing publishing to other channels",
                    alert, channelType), e);
            setSkippedChannelResponse(channelType, alertHistoryObj, e.getMessage());
        }
    }

    /**
     * Method to create alert info.
     *
     * @param buffer BufferedAlertsInfo
     * @return AlertsInfo
     */
    private AlertsInfo setAlertInfo(BufferedAlertsInfo buffer) {
        AlertsInfo alert = new AlertsInfo();
        BeanUtils.copyProperties(buffer, alert);
        NotificationConfig nc = new NotificationConfig();
        BeanUtils.copyProperties(buffer.getCloneNotificationConfig(), nc);
        alert.setNotificationConfig(nc);

        NotificationTemplate nt = new NotificationTemplate();
        BeanUtils.copyProperties(buffer.getCloneNotificationTemplate(), nt);
        alert.addNotificationTemplate(nc.getLocale(), nt);

        NotificationTemplateConfig ntc = new NotificationTemplateConfig();
        BeanUtils.copyProperties(buffer.getCloneNotificationTemplateConfig(), ntc);
        alert.setNotificationTemplateConfig(ntc);

        AlertsInfo.Data data = new AlertsInfo.Data();
        BeanUtils.copyProperties(buffer.getAlertsData(), data);
        alert.setAlertsData(data);
        return alert;
    }

    /**
     * Method to get the source topics.
     *
     * @return String[]
     */
    @Override
    public String[] sources() {
        return sourceCache.get(notificationTopicsArray);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ecsp.stream.base.StreamProcessor#punctuate(long)
     */
    @Override
    public void punctuate(long timestamp) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ecsp.stream.base.StreamProcessor#close()
     */
    @Override
    public void close() {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ecsp.stream.base.StreamProcessor#configChanged(java.util.
     * Properties)
     */
    @Override
    public void configChanged(Properties props) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ecsp.stream.base.StreamProcessor#createStateStore()
     */
    @Override
    public HarmanPersistentKVStore createStateStore() {
        LOGGER.info("Creating state store for vehicle notification component");
        return new ObjectStateStore(NOTIFICATION_STATE_STORE, true);
    }

    /**
     * if the value of EventID is "NotificationSettings" then go ahead with
     * creation of topics. In this case vehicleKey will be userID.

     * If the value of the EventID is "Association", then store the value of
     * pdid to userid mapping in the state store. In this case, vehicleKey will
     * be pdid
     */
    private void setupNotificationService(String key, List<IgniteEvent> eventDataList) {
        if (hasEventId(eventDataList)
                &&
                EventIdValues.NOTIFICATION_SETTINGS.getEventType().equals(eventDataList.get(0).getEventId())) {
            IgniteEvent igEvent = eventDataList.get(0);
            NotificationSettingDataV1_0 data = (NotificationSettingDataV1_0) igEvent.getEventData();
            NotificationConfig config = data.getNotificationConfigs().get(0);
            config.setUserId(key);
            LOGGER.debug("New set up request received for the user id {}", key);
            for (Channel channel : config.getEnabledChannels()) {
                ChannelType channelType = channel.getChannelType();
                processSupportedChannel(channel, channelType, config);
            }
        }
    }

    /**
     * Method to process supported channel.
     *
     * @param channel     Channel
     * @param channelType ChannelType
     * @param config      NotificationConfig
     */
    private void processSupportedChannel(Channel channel, ChannelType channelType, NotificationConfig config) {
        if (channelsSupported.contains(channelType)) {
            Map<String, ChannelNotifier> svcProviderMap =
                    channelNotifierRegistry.getAllchannelNotifiers(channel.getChannelType());
            for (Map.Entry<String, ChannelNotifier> entry : svcProviderMap.entrySet()) {
                ChannelNotifier channelNotifier = entry.getValue();
                try {
                    channelNotifier.setupChannel(config);
                } catch (Exception e) {
                    LOGGER.error(MessageFormat.format(
                            "Exception occurred while setting up "
                                    +
                                    "channel {0}. Continuing setting up other channels",
                            channelType), e);
                }
            }
        } else {
            LOGGER.warn("channel not supported {}", channelType);
        }
    }

    /**
     * Method to check if the event has event id.
     *
     * @param eventDataList List of IgniteEvent
     * @return boolean
     */
    private boolean hasEventId(List<IgniteEvent> eventDataList) {
        return !CollectionUtils.isEmpty(eventDataList)
                &&
                Objects.nonNull(eventDataList.get(0))
                &&
                !StringUtils.isEmpty(eventDataList.get(0).getEventId());
    }

    /**
     * Method to put in state store.
     */
    private void put(String vehicleKey, String userId) {
        LOGGER.debug("Received the user-id {}  for the pdid {}, saving this mapping in state store", userId,
                vehicleKey);
        this.notificationStateStore.put(vehicleKey, userId);

    }

    /**
     * Method to delete in state store.
     */
    private void delete(String vehicleKey) {
        LOGGER.debug("Received DELETE request to remove from state store for the pdid {}", vehicleKey);
        this.notificationStateStore.delete(vehicleKey);
    }

    /**
     * Return the userid for the given pdid from the state store.
     */
    String getUserId(String pdid) {
        String userId = this.notificationStateStore.get(pdid);
        if (StringUtils.isEmpty(userId)) {
            userId = getDeviceAssociation(pdid);
            put(pdid, userId);
        }
        return userId;
    }

    /**
     * Method to get device association.
     *
     * @param pdid String
     * @return String
     */
    private String getDeviceAssociation(String pdid) {
        try {
            Map<VehicleProfileAttribute, Optional<String>> attrs =
                    coreVehicleProfileClient.getVehicleProfileAttributes(pdid,
                            igniteVehicleProfile, VehicleProfileAttribute.USERID);
            return attrs.get(VehicleProfileAttribute.USERID).orElse(null);
        } catch (Exception e) {
            LOGGER.error("Exception from VehicleProfileClient: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Method to initialize the channel service provider config dao.
     */
    void initCachedChannelServiceProviderConfigDao() {
        String daoClassName = NotificationConstants.NC_STATIC_CONFIG_CLASS_NAME;
        ObjectUtils.requireNonEmpty(daoClassName, "DAO class name for notification cannot be null or empty.");
        try {
            Class<?> daoClass = this.getClass().getClassLoader().loadClass(daoClassName); // NOSONAR
            this.cachedChannelServiceProviderConfigDao =
                    (CachedChannelServiceProviderConfigDAO) applicationContext.getBean(daoClass);
            LOGGER.info("this.channelServiceProviderConfigDAO {}", this.cachedChannelServiceProviderConfigDao);

        } catch (Exception e) {
            LOGGER.error("Unable to instantiate DAO for channelServiceProviderConfigDAO", e);
        }
        ObjectUtils.requireNonNull(this.cachedChannelServiceProviderConfigDao,
                "Unable to instantiate DAO for channelServiceProviderConfigDAO.");


    }

    /**
     * method to get the channel service provider config.
     */
    private void getNcEventIdMap() {
        eventIdMap = new HashMap<String, String>();
        eventIdMap.put("PinGenerated", "PinGenerated");
        eventIdMap.put("refreshNotificationScheduler", "refreshNotificationScheduler");
        eventIdMap.put("VehicleAssociation", "VehicleAssociation");
        eventIdMap.put("VehicleDisAssociation", "VehicleDisAssociation");
        eventIdMap.put("NotificationSettings", "NotificationSettings");
        eventIdMap.put("NotificationUserProfile", "NotificationUserProfile");
        eventIdMap.put("CreateSecondaryContact", "CreateSecondaryContact");
        eventIdMap.put("UpdateSecondaryContact", "UpdateSecondaryContact");
        eventIdMap.put("DeleteSecondaryContact", "DeleteSecondaryContact");
        eventIdMap.put("NON_REGISTERED_USER_NOTIFICATION_EVENT", "NON_REGISTERED_USER_NOTIFICATION_EVENT");
        eventIdMap.put("CAMPAIGN_EVENT", "CAMPAIGN_EVENT");
        eventIdMap.put("CampaignStatus", "CampaignStatus");
        eventIdMap.put("ScheduleNotificationEvent", "ScheduleNotificationEvent");
        eventIdMap.put("ScheduleOpStatusEvent", "ScheduleOpStatusEvent");

    }

    /**
     * Method to initialize the notification dao.
     */
    void initNotificationDao(Properties properties) {
        String daoClassName = properties.getProperty(NotificationProperty.NOTIFICATION_DAO_CLASSNAME);
        ObjectUtils.requireNonEmpty(daoClassName, "DAO class name for notification cannot be null or empty.");
        try {
            Class<?> daoClass = this.getClass().getClassLoader().loadClass(daoClassName); // NOSONAR
            this.notificationDao = (NotificationDao) daoClass.newInstance();
        } catch (Exception e) {
            LOGGER.error("Unable to instantiate DAO for notification", e);
        }

        ObjectUtils.requireNonNull(this.notificationDao, "Unable to instantiate DAO for notification.");
        if (notificationDao instanceof MongoDbClient) {
            ((MongoDbClient) notificationDao).setDatastore(datastore);
        }
        this.notificationDao.init(properties);
    }

    /**
     * Method to persist response.
     *
     * @param response Map of String and Object
     * @param collectionName String
     */
    void persistResponse(Map<String, Object> response, String collectionName) {
        LOGGER.debug("Persisting response: {} in collection: {}", response, collectionName);
        notificationDao.insertSingleDocument(response, collectionName);
    }

    /**
     * Method to get supported channels.
     *
     * @return Set of ChannelType
     */
    Set<ChannelType> getChannelsSupported() {
        return channelsSupported;
    }
}
