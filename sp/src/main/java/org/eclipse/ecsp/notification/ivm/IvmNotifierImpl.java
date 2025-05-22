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

package org.eclipse.ecsp.notification.ivm;

import com.jayway.jsonpath.PathNotFoundException;
import org.apache.kafka.streams.processor.api.Record;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.idgen.MessageIdGenerator;
import org.eclipse.ecsp.domain.DeviceMessageFailureEventDataV1_0;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.IVMFeedbackData_V1;
import org.eclipse.ecsp.domain.notification.IVMNotifierResponse;
import org.eclipse.ecsp.domain.notification.IVMRequest;
import org.eclipse.ecsp.domain.notification.LanguageString;
import org.eclipse.ecsp.domain.notification.NotificationCreationRequest;
import org.eclipse.ecsp.domain.notification.NotificationErrorCode;
import org.eclipse.ecsp.domain.notification.VehicleMessageAckData;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionAckData;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionAckData.DispositionResponseEnum;
import org.eclipse.ecsp.domain.notification.VehicleMessageDispositionPublishData;
import org.eclipse.ecsp.domain.notification.VehicleMessagePublishData;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.domain.notification.exceptions.NoSuchEntityException;
import org.eclipse.ecsp.domain.notification.utils.VehicleService;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.eclipse.ecsp.notification.dao.IVMRequestDAO;
import org.eclipse.ecsp.notification.entities.IVMTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.feedback.NotificationFeedbackHandler;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.ecsp.domain.notification.commons.EventID.VEHICLE_MESSAGE_PUBLISH;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.CAMPAIGN_DATE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.CAMPAIGN_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.COUNTRY_CODE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.FILE_NAME;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.HARMAN_ID;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;


/**
 * IvmNotifierImpl class.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IvmNotifierImpl extends IvmNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(IvmNotifierImpl.class);
    private static final String SVC_PROVIDER = "IVM:DEFAULT";
    private StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt;
    @Autowired
    private AlertsHistoryDao alertsHistoryDao;
    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;
    @Autowired
    private IVMRequestDAO ivmRequestDao;
    @Autowired
    @Qualifier("globalMessageIdGenerator")
    private MessageIdGenerator msgIdGen;
    @Value("${" + NotificationProperty.IVM_RESPONSE_ACK_TOPIC + "}")
    private String ivmResponseAckTopic;
    @Autowired
    private VehicleService vehicleService;
    @Value("${ivm.event.ttl.enabled:false}")
    private boolean ivmEventTtlEnabled;
    @Value("${ivm.event.device.delivery.cutoff.ms:604800000}")
    private long ivmEventDeviceDeliveryCutoffMillis;

    /**
     * Setup Channel.
     *
     * @param notificationConfig Channel configuration
     * @return Channel Response
     */
    @Override
    public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
        return new IVMNotifierResponse(notificationConfig.getUserId(), null, null);
    }

    /**
     * Destroy Channel.
     *
     * @param userId    User Id
     * @param eventData Event Data
     * @return Channel Response
     */
    @Override
    public ChannelResponse destroyChannel(String userId, String eventData) {
        // Nothing to do
        return null;
    }

    /**
     * Publish message to vehicle.
     *
     * @param alertInfo Alerts Info
     * @return Channel Response
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alertInfo) {
        boolean isCampaignMessage = VEHICLE_MESSAGE_PUBLISH.equals(alertInfo.getEventID());
        String userId = alertInfo.getAlertsData().getUserProfile() != null
            ?
            alertInfo.getAlertsData().getUserProfile().getUserId() : null;
        LOGGER.debug("alertInfo.getIgniteEvent()  {}", alertInfo.getIgniteEvent());
        Map<String, Object> ivmRequestParams = alertInfo.getAlertsData().any();
        VehicleMessagePublishData publishData;

        if (isCampaignMessage) {
            publishData = (VehicleMessagePublishData) alertInfo.getIgniteEvent().getEventData();
        } else {
            publishData = getVehicleMessagePublishDataFromTemplate(alertInfo, ivmRequestParams);
        }
        publishData.setVin(alertInfo.getIgniteEvent().getVehicleId());
        IVMRequest ivmRequest = new IVMRequest();
        ivmRequest.setNotificationId(publishData.getNotificationId());
        NotificationCreationRequest request = getNotificationCreationRequest(alertInfo, publishData);
        boolean entitlementFailed = false;

        try {
            checkEntitlementServices(request);
        } catch (AuthorizationException ae) {
            entitlementFailed = true;
            LOGGER.error("Authorization Exception {} :: entitlementFlag = {}", ae, true);
        } catch (NoSuchEntityException nsee) {
            entitlementFailed = true;
            LOGGER.error("NoSuchEntityException {} :: entitlementFlag = {}", nsee, true);
        } catch (PathNotFoundException pne) {
            entitlementFailed = true;
            LOGGER.error("PathNotFoundException {} :: entitlementFlag = {}", pne, true);
        }

        IVMNotifierResponse ivmResponse = new IVMNotifierResponse(userId, alertInfo.getPdid());
        IgniteEventImpl igniteEvent = getIgniteEvent(alertInfo);
        if (entitlementFailed) {
            return getIvmFailedEntitlementResponse(alertInfo, ivmResponse, igniteEvent, ivmRequestParams, publishData);
        }

        publishData.setUserId(null);
        publishData.setCampaignId(null);
        igniteEvent.setEventData(publishData);
        if (publishData.getVehicleMessageID() == 0) {
            publishData.setVehicleMessageID(Integer.parseInt(igniteEvent.getMessageId()));
        }

        if (ivmEventTtlEnabled) {

            LOGGER.debug("TTL enabled {} and cutoff set is {}", ivmEventTtlEnabled, ivmEventDeviceDeliveryCutoffMillis);

            igniteEvent.setDeviceDeliveryCutoff(System.currentTimeMillis() + ivmEventDeviceDeliveryCutoffMillis);
        }
        LOGGER.debug("IVM Vehicle publish event {}", igniteEvent);
        IgniteStringKey key = new IgniteStringKey();
        key.setKey(alertInfo.getIgniteEvent().getVehicleId());
        LOGGER.debug("key {}", key);

        try {
            LOGGER.info("Publishing message {} to vehicle {}", alertInfo.getAlertsData(),
                alertInfo.getIgniteEvent().getVehicleId());
            ctxt.forward(new Record<>(key, igniteEvent, System.currentTimeMillis()));

            updateIvmRequestAndResponse(alertInfo, ivmRequest, igniteEvent, ivmRequestParams, ivmResponse);
        } catch (Exception e) {
            LOGGER.error("Failed sending notification via IvmNotifierImpl", e);
            ivmResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
        }

        return ivmResponse;
    }

    /**
     * Get Notification Creation Request.
     *
     * @param alertInfo   Alerts Info
     * @param publishData Publish Data
     * @return Notification Creation Request
     */
    @NotNull
    private NotificationCreationRequest getNotificationCreationRequest(AlertsInfo alertInfo,
                                                                       VehicleMessagePublishData publishData) {
        NotificationCreationRequest request = new NotificationCreationRequest();
        request.setNotificationId(publishData.getNotificationId());
        request.setVehicleId(alertInfo.getIgniteEvent().getVehicleId());
        request.setRequestId(alertInfo.getIgniteEvent().getRequestId());
        return request;
    }

    /**
     * Update IVM Request and Response.
     *
     * @param alertInfo         Alerts Info
     * @param ivmRequest        IVM Request
     * @param igniteEvent       Ignite Event
     * @param ivmRequestParams  IVM Request Params
     * @param ivmResponse       IVM Response
     */
    private void updateIvmRequestAndResponse(AlertsInfo alertInfo, IVMRequest ivmRequest,
                                             IgniteEventImpl igniteEvent, Map<String, Object> ivmRequestParams,
                                             IVMNotifierResponse ivmResponse) {
        ivmRequest.setRequestId(alertInfo.getIgniteEvent().getRequestId());
        ivmRequest.setMessageId(igniteEvent.getMessageId());
        ivmRequest.setVehicleId(alertInfo.getIgniteEvent().getVehicleId());
        ivmRequest.setSessionId(alertInfo.getIgniteEvent().getBizTransactionId());
        ivmRequest.setCampaignDate((String) ivmRequestParams.get(CAMPAIGN_DATE));
        ivmRequest.setCampaignId((String) ivmRequestParams.get(CAMPAIGN_ID));
        ivmRequest.setFileName((String) ivmRequestParams.get(FILE_NAME));
        ivmRequest.setHarmanId((String) ivmRequestParams.get(HARMAN_ID));
        ivmRequest.setCountryCode((String) ivmRequestParams.get(COUNTRY_CODE));
        ivmRequestDao.save(ivmRequest);

        ivmResponse.setStatus(NOTIFICATION_STATUS_SUCCESS);
        IVMTemplate ivmTemplate = new IVMTemplate();
        ivmTemplate.setBody(alertInfo.getNotificationTemplate().getIvmTemplate().getBody());
        ivmTemplate.setTitle(alertInfo.getNotificationTemplate().getIvmTemplate().getTitle());
        ivmResponse.setTemplate(ivmTemplate);
    }

    /**
     * Get IVM Failed Entitlement Response.
     *
     * @param alertInfo         Alerts Info
     * @param ivmResponse       IVM Response
     * @param igniteEvent       Ignite Event
     * @param ivmRequestParams  IVM Request Params
     * @param publishData       Publish Data
     * @return IVM Notifier Response
     */
    @NotNull
    private IVMNotifierResponse getIvmFailedEntitlementResponse(AlertsInfo alertInfo, IVMNotifierResponse ivmResponse,
                                                                IgniteEventImpl igniteEvent,
                                                                Map<String, Object> ivmRequestParams,
                                                                VehicleMessagePublishData publishData) {
        IgniteEventImpl ivmFeedbackEvent = new IgniteEventImpl();
        ivmFeedbackEvent.setEventId(EventID.IVM_FEEDBACK);
        ivmFeedbackEvent.setBizTransactionId(igniteEvent.getBizTransactionId());
        ivmFeedbackEvent.setRequestId(igniteEvent.getRequestId());
        ivmFeedbackEvent.setTimestamp(igniteEvent.getTimestamp());
        ivmFeedbackEvent.setTimezone(igniteEvent.getTimezone());
        ivmFeedbackEvent.setVehicleId(igniteEvent.getVehicleId());
        ivmFeedbackEvent.setVersion(Version.V1_0);

        IVMFeedbackData_V1 ivmFeedbackData = new IVMFeedbackData_V1();
        ivmFeedbackData.setCampaignDate((String) ivmRequestParams.get(CAMPAIGN_DATE));
        ivmFeedbackData.setCampaignId((String) ivmRequestParams.get(CAMPAIGN_ID));
        ivmFeedbackData.setFileName((String) ivmRequestParams.get(FILE_NAME));
        ivmFeedbackData.setHarmanId((String) ivmRequestParams.get(HARMAN_ID));
        ivmFeedbackData.setCountryCode((String) ivmRequestParams.get(COUNTRY_CODE));
        ivmFeedbackData.setNotificationId(publishData.getNotificationId());
        ivmFeedbackData.setStatus(NotificationConstants.FAILURE);

        ivmFeedbackData.setErrorCode(NotificationErrorCode.VEHICLE_ID_NOT_PROVISIONED);
        ivmFeedbackData.setErrorDetail("Vehicle Id not provisioned");

        ivmFeedbackData.setVehicleMessageID(
            publishData.getVehicleMessageID() != 0 ? publishData.getVehicleMessageID() :
                Integer.parseInt(igniteEvent.getMessageId()));
        ivmFeedbackEvent.setEventData(ivmFeedbackData);

        ivmResponse.setStatus(NotificationConstants.FAILURE);
        ivmResponse.setErrorCode(ivmFeedbackData.getErrorCode());

        IgniteStringKey key = new IgniteStringKey();
        key.setKey(alertInfo.getIgniteEvent().getVehicleId());
        ctxt.forwardDirectly(key, ivmFeedbackEvent, ivmResponseAckTopic);
        LOGGER.debug("IVM Failure {} entitlementFailed {} ", ivmFeedbackEvent, true);
        return ivmResponse;
    }

    /**
     * Get Vehicle Message Publish Data From Template.
     *
     * @param alertInfo         Alerts Info
     * @param ivmRequestParams  IVM Request Params
     * @return Vehicle Message Publish Data
     */
    @NotNull
    private VehicleMessagePublishData getVehicleMessagePublishDataFromTemplate(AlertsInfo alertInfo,
                                                                               Map<String, Object> ivmRequestParams) {
        VehicleMessagePublishData publishData;
        LOGGER.debug("IVM doPublish regular notification");
        List<LanguageString> messages = getLanguageStringsFromTemplate(alertInfo);
        publishData = new VehicleMessagePublishData();
        publishData.setNotificationId(alertInfo.getNotificationTemplate().getNotificationId());
        publishData.setMessage(messages);
        publishData.setCampaignId((String) ivmRequestParams.get(CAMPAIGN_ID));
        publishData.setMessageTemplate(alertInfo.getNotificationTemplate().getNotificationId());
        if (ivmRequestParams.containsKey("messageType")) {
            publishData.setMessageType(
                VehicleMessagePublishData.MessageType.valueOf((String) ivmRequestParams.get("messageType")));
        }
        if (ivmRequestParams.containsKey("messageDetailType")) {
            publishData.setMessageDetailType(
                VehicleMessagePublishData.MessageDetailType.valueOf(
                    (String) ivmRequestParams.get("messageDetailType")));
        }
        if (ivmRequestParams.containsKey("messageParameters")) {
            publishData.setMessageParameters((Map<String, String>) ivmRequestParams.get("messageParameters"));
        }
        if (ivmRequestParams.containsKey("serviceMessageEventID")) {
            publishData.setServiceMessageEventID((String) ivmRequestParams.get("serviceMessageEventID"));
        }
        if (ivmRequestParams.containsKey("altPhoneNumber")) {
            publishData.setAltPhoneNumber((String) ivmRequestParams.get("altPhoneNumber"));
        }
        if (ivmRequestParams.containsKey("buttonActions")) {
            publishData.setButtonActions((List<String>) ivmRequestParams.get("buttonActions"));
        }
        if (ivmRequestParams.containsKey("callType")) {
            publishData.setCallType((String) ivmRequestParams.get("callType"));
        }
        if (ivmRequestParams.containsKey("priority")) {
            publishData.setPriority((int) ivmRequestParams.get("priority"));
        }
        if (ivmRequestParams.containsKey("additionalData")) {
            publishData.setAdditionalData((Map<String, Object>) ivmRequestParams.get("additionalData"));
        }
        LOGGER.debug("IVM doPublish final messages {}", messages);
        return publishData;
    }

    /**
     * Get Language Strings From Template.
     *
     * @param alertInfo Alerts Info
     * @return Language Strings
     */
    @NotNull
    private List<LanguageString> getLanguageStringsFromTemplate(AlertsInfo alertInfo) {
        List<LanguageString> messages = new ArrayList<>();
        if (CollectionUtils.isEmpty(alertInfo.getAllLanguageTemplates())) {
            LOGGER.debug("IVM doPublish only user locale");
            messages.add(getLanguageStringFromTemplate(alertInfo.getNotificationTemplate()));
        } else {
            LOGGER.debug("IVM doPublish all locales");
            for (NotificationTemplate notificationTemplate : alertInfo.getAllLanguageTemplates()) {
                messages.add(getLanguageStringFromTemplate(notificationTemplate));
            }
        }
        return messages;
    }

    /**
     * Get Ignite Event.
     *
     * @param alertInfo Alerts Info
     * @return Ignite Event
     */
    @NotNull
    private IgniteEventImpl getIgniteEvent(AlertsInfo alertInfo) {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setDeviceRoutable(true);
        igniteEvent.setResponseExpected(true);
        igniteEvent.setEventId(VEHICLE_MESSAGE_PUBLISH);
        igniteEvent.setVehicleId(alertInfo.getIgniteEvent().getVehicleId());
        igniteEvent.setVersion(Version.V1_0);
        igniteEvent.setBizTransactionId(alertInfo.getIgniteEvent().getBizTransactionId());
        igniteEvent.setRequestId(alertInfo.getIgniteEvent().getRequestId());
        igniteEvent.setMessageId(msgIdGen.generateUniqueMsgId(alertInfo.getIgniteEvent().getVehicleId()));
        igniteEvent.setTimestamp(alertInfo.getIgniteEvent().getTimestamp());
        return igniteEvent;
    }

    /**
     * Get Language String From Template.
     *
     * @param notificationTemplate Notification Template
     * @return Language String
     */
    private LanguageString getLanguageStringFromTemplate(NotificationTemplate notificationTemplate) {
        LanguageString languageString = new LanguageString();
        languageString.setLanguage(notificationTemplate.getLocale().toLanguageTag());
        IVMTemplate ivmTemplate = notificationTemplate.getChannelTemplate(ChannelType.IVM);
        languageString.setTitle(ivmTemplate.getTitle());
        languageString.setMessageText(ivmTemplate.getBody());
        return languageString;
    }

    /**
     * Process Ack.
     *
     * @param igniteEvent Ignite Event
     */
    @Override
    public void processAck(IgniteEvent igniteEvent) {
        String userStatus = null;
        String vehicleId = igniteEvent.getVehicleId();
        String messageId = null;
        if (EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH.equals(igniteEvent.getEventId())) {
            messageId = igniteEvent.getMessageId();
            LOGGER.debug("Sending vehicleMessageDispositionAck to vehicle {}", vehicleId);
            VehicleMessageDispositionPublishData vehicleMessageDispositionData =
                (VehicleMessageDispositionPublishData) igniteEvent
                    .getEventData();
            IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
            igniteEventImpl.setDeviceRoutable(true);
            igniteEventImpl.setEventId(EventID.VEHICLE_MESSAGE_DISPOSITION_ACK);
            igniteEventImpl.setVehicleId(vehicleId);
            igniteEventImpl.setVersion(Version.V1_0);
            igniteEventImpl.setRequestId(igniteEvent.getRequestId());
            igniteEventImpl.setMessageId(msgIdGen.generateUniqueMsgId(vehicleId));
            igniteEventImpl.setCorrelationId(messageId);
            igniteEventImpl.setBizTransactionId(igniteEvent.getBizTransactionId());
            igniteEventImpl.setTimestamp(vehicleMessageDispositionData.getMessageDisplayTimestamp());
            VehicleMessageDispositionAckData vehicleMessageDispositionAckData = new VehicleMessageDispositionAckData();
            vehicleMessageDispositionAckData.setResponse(DispositionResponseEnum.SUCCESS);
            igniteEventImpl.setEventData(vehicleMessageDispositionAckData);
            IgniteStringKey key = new IgniteStringKey();
            key.setKey(vehicleId);
            if (ivmEventTtlEnabled) {

                LOGGER.debug("TTL enabled {} and cutoff set is {}", ivmEventTtlEnabled,
                    ivmEventDeviceDeliveryCutoffMillis);

                igniteEventImpl.setDeviceDeliveryCutoff(
                    System.currentTimeMillis() + ivmEventDeviceDeliveryCutoffMillis);
            }
            LOGGER.info("Publishing vehicle message disposition data {} to {}", vehicleMessageDispositionData,
                vehicleId);
            ctxt.forward(new Record<>(key, igniteEventImpl, System.currentTimeMillis()));
            LOGGER.info("Vehicle message disposition ack data: {}", vehicleMessageDispositionAckData);
            userStatus = vehicleMessageDispositionData.getDisposition().toString();
        }

        Optional<IVMRequest> ivmRequest;
        if (EventID.DMA_FEEDBACK_EVENT.equals(igniteEvent.getEventId())) {
            DeviceMessageFailureEventDataV1_0 statusData = (DeviceMessageFailureEventDataV1_0) igniteEvent
                .getEventData();
            vehicleId = statusData.getFailedIgniteEvent().getVehicleId();
            messageId = statusData.getFailedIgniteEvent().getMessageId();
            ivmRequest = ivmRequestDao.findByVehicleIdMessageId(vehicleId, messageId);
        } else if (EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH.equals(igniteEvent.getEventId())) {
            String sessionId = igniteEvent.getBizTransactionId();
            ivmRequest = ivmRequestDao.findByVehicleIdSessionId(vehicleId, sessionId);
        } else {
            messageId = igniteEvent.getCorrelationId();
            ivmRequest = ivmRequestDao.findByVehicleIdMessageId(vehicleId, messageId);
        }
        if (ivmRequest.isPresent()) {
            processExistingIvmRequest(igniteEvent, ivmRequest, userStatus, vehicleId, messageId);
        }
    }

    /**
     * Process Existing Ivm Request.
     *
     * @param igniteEvent Ignite Event
     * @param ivmRequest  IVM Request
     * @param userStatus  User Status
     * @param vehicleId   Vehicle Id
     * @param messageId   Message Id
     */
    private void processExistingIvmRequest(IgniteEvent igniteEvent, Optional<IVMRequest> ivmRequest,
                                           String userStatus, String vehicleId, String messageId) {
        String requestId = ivmRequest.get().getRequestId();
        LOGGER.debug("Found request for {}", requestId);
        AlertsHistoryInfo alertsHistory = alertsHistoryDao.findById(requestId);
        LOGGER.debug("Updating history {}", alertsHistory);
        int channelResponseIndex = 0;
        for (ChannelResponse channelResponse : alertsHistory.getChannelResponses()) {
            if (ChannelType.IVM.equals(channelResponse.getChannelType())) {
                IVMNotifierResponse response = ((IVMNotifierResponse) channelResponse);
                if (EventID.VEHICLE_MESSAGE_DISPOSITION_PUBLISH.equals(igniteEvent.getEventId())) {
                    response.setUserStatus(userStatus);
                    VehicleMessageDispositionPublishData vehicleMessageDisposition =
                        (VehicleMessageDispositionPublishData) igniteEvent
                            .getEventData();
                    vehicleMessageDisposition.setCampaignDate(ivmRequest.get().getCampaignDate());
                    vehicleMessageDisposition.setCampaignId(ivmRequest.get().getCampaignId());
                    vehicleMessageDisposition.setFileName(ivmRequest.get().getFileName());
                    vehicleMessageDisposition.setHarmanId(ivmRequest.get().getHarmanId());
                    vehicleMessageDisposition.setCountryCode(ivmRequest.get().getCountryCode());
                    vehicleMessageDisposition.setNotificationId(ivmRequest.get().getNotificationId());
                    IgniteStringKey key = new IgniteStringKey();
                    key.setKey(vehicleId);
                    LOGGER.info("Sending VehicleMessageDispositionPublish: {}", vehicleMessageDisposition);
                    ctxt.forwardDirectly(key, igniteEvent, ivmResponseAckTopic);
                }
                if (EventID.VEHICLE_MESSAGE_ACK.equals(igniteEvent.getEventId())) {
                    VehicleMessageAckData statusData = (VehicleMessageAckData) igniteEvent.getEventData();
                    response.setDeviceStatus(statusData.getStatus().toString());
                    response.setStatus(NotificationConstants.SUCCESS);
                    response.setErrorCode(null);
                    VehicleMessageAckData vehicleMessageAck = (VehicleMessageAckData) igniteEvent.getEventData();
                    vehicleMessageAck.setCampaignDate(ivmRequest.get().getCampaignDate());
                    vehicleMessageAck.setCampaignId(ivmRequest.get().getCampaignId());
                    vehicleMessageAck.setFileName(ivmRequest.get().getFileName());
                    vehicleMessageAck.setHarmanId(ivmRequest.get().getHarmanId());
                    vehicleMessageAck.setCountryCode(ivmRequest.get().getCountryCode());
                    vehicleMessageAck.setNotificationId(ivmRequest.get().getNotificationId());
                    IgniteStringKey key = new IgniteStringKey();
                    key.setKey(vehicleId);
                    LOGGER.info("Sending VehicleMessageAck: {}", vehicleMessageAck);
                    ctxt.forwardDirectly(key, igniteEvent, ivmResponseAckTopic);
                }
                if (EventID.DMA_FEEDBACK_EVENT.equals(igniteEvent.getEventId())) {
                    processDmaFeedbackEvent(igniteEvent, ivmRequest, vehicleId, messageId, response);
                }
                // update of Alert History should be only for IVM Channel
                boolean isUpdated = alertsHistoryDao.updateChannel(requestId, channelResponseIndex, response);
                LOGGER.debug("Alert history after update: {} , {}", alertsHistory, isUpdated);
                break;
            }
            channelResponseIndex++;
        }
        NotificationFeedbackHandler.sendNotificationChannelLevelFeedback(
                alertsHistory.getPayload(), new IgniteStringKey(igniteEvent.getVehicleId()),
                igniteEvent, alertsHistory);

    }

    /**
     * Process Dma Feedback Event.
     *
     * @param igniteEvent Ignite Event
     * @param ivmRequest  IVM Request
     * @param vehicleId   Vehicle Id
     * @param messageId   Message Id
     * @param response    IVM Notifier Response
     */
    private void processDmaFeedbackEvent(IgniteEvent igniteEvent, Optional<IVMRequest> ivmRequest,
                                         String vehicleId, String messageId, IVMNotifierResponse response) {
        DeviceMessageFailureEventDataV1_0 statusData = (DeviceMessageFailureEventDataV1_0) igniteEvent
            .getEventData();
        response.setStatus(statusData.getErrorCode().toString());
        response.setErrorCode(NotificationErrorCode.DELIVERY_CHANNEL_NOT_AVAILABLE);
        IgniteEventImpl ivmFeedbackEvent = new IgniteEventImpl();
        ivmFeedbackEvent.setEventId(EventID.IVM_FEEDBACK);
        ivmFeedbackEvent.setBizTransactionId(igniteEvent.getBizTransactionId());
        ivmFeedbackEvent.setRequestId(igniteEvent.getRequestId());
        ivmFeedbackEvent.setTimestamp(igniteEvent.getTimestamp());
        ivmFeedbackEvent.setTimezone(igniteEvent.getTimezone());
        ivmFeedbackEvent.setVehicleId(vehicleId);

        IVMFeedbackData_V1 ivmFeedbackData = new IVMFeedbackData_V1();
        ivmFeedbackData.setCampaignDate(ivmRequest.get().getCampaignDate());
        ivmFeedbackData.setCampaignId(ivmRequest.get().getCampaignId());
        ivmFeedbackData.setFileName(ivmRequest.get().getFileName());
        ivmFeedbackData.setHarmanId(ivmRequest.get().getHarmanId());
        ivmFeedbackData.setCountryCode(ivmRequest.get().getCountryCode());
        ivmFeedbackData.setNotificationId(ivmRequest.get().getNotificationId());
        ivmFeedbackData.setStatus("Failure");
        ivmFeedbackData.setErrorCode(NotificationErrorCode.DELIVERY_CHANNEL_NOT_AVAILABLE);
        ivmFeedbackData.setErrorDetail(statusData.getErrorCode().toString());
        if (VEHICLE_MESSAGE_PUBLISH.equalsIgnoreCase(statusData.getFailedIgniteEvent().getEventId())) {
            VehicleMessagePublishData publishData =
                (VehicleMessagePublishData) statusData.getFailedIgniteEvent()
                    .getEventData();
            ivmFeedbackData.setVehicleMessageID(
                publishData.getVehicleMessageID() != 0 ? publishData.getVehicleMessageID()
                    : Integer.parseInt(messageId));
        } else {
            ivmFeedbackData.setVehicleMessageID(Integer.parseInt(messageId));
        }

        ivmFeedbackEvent.setEventData(ivmFeedbackData);

        IgniteStringKey key = new IgniteStringKey();
        key.setKey(vehicleId);
        ctxt.forwardDirectly(key, ivmFeedbackEvent, ivmResponseAckTopic);
    }

    /**
     * Check Entitlement Services.
     *
     * @param notificationRequest Notification Creation Request
     * @throws AuthorizationException Authorization Exception
     * @throws NoSuchEntityException  No Such Entity Exception
     */
    private void checkEntitlementServices(NotificationCreationRequest notificationRequest)
        throws AuthorizationException, NoSuchEntityException {
        List<NotificationGrouping> ngList =
            notificationGroupingDao.findByNotificationId(notificationRequest.getNotificationId());
        if (CollectionUtils.isEmpty(ngList)) {
            throw new NoSuchEntityException(
                "Notification Grouping not found for " + notificationRequest.getNotificationId());
        }
        vehicleService.validateServiceEnabled(notificationRequest.getVehicleId(), ngList);
    }

    /**
     * Set Processor Context.
     *
     * @param ctxt Stream processing context
     */
    @Override
    public void setProcessorContext(StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt) {
        this.ctxt = ctxt;
    }

    /**
     * Get Service Provider Name.
     *
     * @return Service Provider Name
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }

}