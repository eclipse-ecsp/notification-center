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

package org.eclipse.ecsp.notification.aws.pinpoint;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.services.pinpoint.AmazonPinpoint;
import com.amazonaws.services.pinpoint.AmazonPinpointClientBuilder;
import com.amazonaws.services.pinpoint.model.AmazonPinpointException;
import com.amazonaws.services.pinpoint.model.ChannelType;
import com.amazonaws.services.pinpoint.model.DeleteEndpointRequest;
import com.amazonaws.services.pinpoint.model.DeleteEndpointResult;
import com.amazonaws.services.pinpoint.model.EndpointMessageResult;
import com.amazonaws.services.pinpoint.model.EndpointRequest;
import com.amazonaws.services.pinpoint.model.EndpointResponse;
import com.amazonaws.services.pinpoint.model.EndpointUser;
import com.amazonaws.services.pinpoint.model.GetEndpointRequest;
import com.amazonaws.services.pinpoint.model.GetEndpointResult;
import com.amazonaws.services.pinpoint.model.NumberValidateRequest;
import com.amazonaws.services.pinpoint.model.PhoneNumberValidateRequest;
import com.amazonaws.services.pinpoint.model.PhoneNumberValidateResult;
import com.amazonaws.services.pinpoint.model.SendMessagesRequest;
import com.amazonaws.services.pinpoint.model.SendMessagesResult;
import com.amazonaws.services.pinpoint.model.UpdateEndpointRequest;
import com.amazonaws.services.pinpoint.model.UpdateEndpointResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceAsyncClientBuilder;
import com.codahale.metrics.MetricRegistry;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.EndpointResult;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.utils.AwsUtils;
import org.eclipse.ecsp.notification.adaptor.AbstractChannelNotifier;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.dao.AmazonPinpointEndpointDAO;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.entities.AmazonPinpointEndpoint;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.eclipse.ecsp.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

/**
 * AmazonPinpointNotifier abstraction.
 *
 * @author MaKumari
 */
public abstract class AmazonPinpointNotifier extends AbstractChannelNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonPinpointNotifier.class);
    private AmazonPinpoint client;
    @Value("${aws.pinpoint.application.id}")
    private String appId;
    @Autowired
    private AmazonPinpointEndpointDAO pinpointEndpointDao;
    @Value("${aws.pinpoint.cross.account.enabled: false}")
    private boolean pinpointCrossAccountEnabled;
    @Value("${aws.cross.account.enabled: false}")
    private boolean isCrossAccountEnabled;
    @Value("${aws.cross.account.session.name}")
    private String crossRoleSessionName;
    @Value("${aws.cross.account.token.expiry.sec: 3600}")
    private String durationInSec;
    @Value("${aws.cross.account.arn.role}")
    private String crossArnRole;
    private STSAssumeRoleSessionCredentialsProvider stsAssumeRoleSessionCredentialsProvider;

    /**
     * Incase of cross account, temporary credentials are fetched from assumed
     * role in different aws account.
     *
     * @return AWSCredentialsProvider
     */
    private AWSCredentialsProvider loadCredentials(String awsRegion) {
        AWSCredentialsProviderChain credProviderChain = new AWSCredentialsProviderChain(
                AwsUtils.getCredentialsProviderList(getProperties()));
        if (isCrossAccountEnabled) {
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceAsyncClientBuilder.standard()
                    .withCredentials(credProviderChain)
                    .withRegion(awsRegion)
                    .build();
            STSAssumeRoleSessionCredentialsProvider.Builder builder =
                    new STSAssumeRoleSessionCredentialsProvider.Builder(crossArnRole,
                            crossRoleSessionName).withRoleSessionDurationSeconds(Integer.parseInt(durationInSec))
                            .withStsClient(stsClient);
            stsAssumeRoleSessionCredentialsProvider = builder.build();
            LOGGER.info("Initialised cross account credentials provider {} for AmazonPinpointNotifier",
                    stsAssumeRoleSessionCredentialsProvider);
            return stsAssumeRoleSessionCredentialsProvider;
        }
        return credProviderChain;
    }

    /**
     * Initialises AmazonPinpointNotifier.
     *
     * @param properties Properties
     * @param metricRegistry MetricRegistry
     * @param notificationDao NotificationDao
     */
    @Override
    public void init(Properties properties, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        super.init(properties, metricRegistry, notificationDao);
        AWSCredentialsProviderChain credProviderChain = new AWSCredentialsProviderChain(
                AwsUtils.getCredentialsProviderList(properties));
        boolean isRunningOnLambda = Boolean
                .parseBoolean(properties.getProperty(NotificationProperty.AWS_LAMBDA_EXECUTION_PROP, "false"));
        String awsRegion = properties.getProperty(NotificationProperty.AWS_REGION);
        if (isRunningOnLambda) {
            client = AmazonPinpointClientBuilder.standard().withRegion(awsRegion).build();
        } else {
            if (pinpointCrossAccountEnabled) {
                client = AmazonPinpointClientBuilder.standard()
                        .withCredentials(loadCredentials(awsRegion))
                        .withRegion(awsRegion)
                        .build();
            } else {
                client = AmazonPinpointClientBuilder.standard()
                        .withCredentials(credProviderChain)
                        .withRegion(awsRegion)
                        .build();
            }
        }
    }

    /**
     * shutdown method.
     */
    @PreDestroy
    public void shutdown() {
        if (ObjectUtils.isNotEmpty(client)) {
            client.shutdown();
        }
        if (ObjectUtils.isNotEmpty(stsAssumeRoleSessionCredentialsProvider)) {
            stsAssumeRoleSessionCredentialsProvider.close();
        }
    }

    /**
     * Creates endpoint request payload for endpoint creation.
     *
     * @param userId String
     * @param channelType ChannelType
     * @param address String
     * @return EndpointRequest
     */
    private EndpointRequest createEndpointRequestPayload(String userId, ChannelType channelType, String address) {
        EndpointUser user = new EndpointUser().withUserId(userId);
        return new EndpointRequest().withAddress(address)
                .withChannelType(channelType.toString())
                .withRequestId(UUID.randomUUID().toString())
                .withUser(user);
    }

    /**
     * Update(s) amazon pinpoint endpoint.
     *
     * @param userId String
     * @param channelType ChannelType
     * @param address String
     * @return String
     */
    protected String updateEndpoint(String userId, ChannelType channelType, String address) {
        String endpointId = UUID.randomUUID().toString();
        try {
            EndpointRequest endpointRequest = createEndpointRequestPayload(userId, channelType, address);
            UpdateEndpointRequest updateEndpointRequest =
                    new UpdateEndpointRequest().withApplicationId(appId).withEndpointId(endpointId)
                            .withEndpointRequest(endpointRequest);
            LOGGER.info("UpdateEndpointRequest: requestId: {} userId: {} channelType: {} endpointId: {}",
                    endpointRequest.getRequestId(),
                    userId, channelType, updateEndpointRequest.getEndpointId());
            UpdateEndpointResult updateEndpointResult = client.updateEndpoint(updateEndpointRequest);
            LOGGER.info("UpdateEndpointResult: {} for requestId: {} userId: {} channelType: {} endpointId:{} ",
                    updateEndpointResult,
                    endpointRequest.getRequestId(), userId, channelType, updateEndpointRequest.getEndpointId());
        } catch (AmazonPinpointException e) {
            LOGGER.error("Exception occurred while updating endpoint for userId: {} endpointId: {} exception: {}",
                    userId, endpointId,
                    e.getMessage());
            endpointId = null;
        }
        return endpointId;
    }

    /**
     * GET endpoint details for an endpointId.
     *
     * @param endpointId String
     * @return EndpointResponse
     */
    protected EndpointResponse getEndpoint(String endpointId) {
        EndpointResponse endpointResponse = null;
        try {
            GetEndpointRequest getEndpointRequest =
                    new GetEndpointRequest().withApplicationId(appId).withEndpointId(endpointId);
            LOGGER.info("GetEndpointRequest: {}", getEndpointRequest);
            GetEndpointResult getEndpointResult = client.getEndpoint(getEndpointRequest);
            endpointResponse = getEndpointResult.getEndpointResponse();
            LOGGER.info("GetEndpointResponse: {}", endpointResponse.getId());
        } catch (AmazonPinpointException e) {
            LOGGER.error("Exception occurred while doing get endpoint: {}", e.getMessage());
        }
        return endpointResponse;
    }

    /**
     * Deletes an endpointId.
     *
     * @param endpointId String
     * @return endpointResponse
     */
    protected EndpointResponse deleteEndpoint(String endpointId) {
        EndpointResponse endpointResponse = null;
        try {
            DeleteEndpointRequest deleteEndpointRequest =
                    new DeleteEndpointRequest().withApplicationId(appId).withEndpointId(endpointId);
            LOGGER.info("DeleteEndpointRequest: {}", deleteEndpointRequest);
            DeleteEndpointResult deleteEndpointResult = client.deleteEndpoint(deleteEndpointRequest);
            endpointResponse = deleteEndpointResult.getEndpointResponse();
            LOGGER.info("DeleteEndpointResponse: {}", endpointResponse);
        } catch (AmazonPinpointException e) {
            LOGGER.error("Exception occurred while deleting endpoint:{} ", e.getMessage());
        }
        return endpointResponse;
    }

    /**
     * Updates amazon pinpoint endpoint(s).
     *
     * @param userId String
     * @param channelType ChannelType
     * @param addressList List of addresses

     * @return map
     */
    protected Map<String, String> updateEndpoints(String userId, ChannelType channelType, List<String> addressList) {

        List<String> validAddressList = validateAddressList(channelType, addressList);

        AmazonPinpointEndpoint endpointDocument = pinpointEndpointDao.findById(userId);
        endpointDocument = ObjectUtils.isNotEmpty(endpointDocument) ? endpointDocument : new AmazonPinpointEndpoint();
        Map<String, Map<String, String>> endpointsMap = (ObjectUtils.isNotEmpty(endpointDocument)
                && ObjectUtils.isNotEmpty(endpointDocument.getEndpoints())) ? endpointDocument.getEndpoints()
                : new HashMap<>();
        String channelTypeString = channelType.toString();
        Map<String, String> channelEndpointsMap = (ObjectUtils.isNotEmpty(endpointsMap)
                && ObjectUtils.isNotEmpty(endpointsMap.get(channelTypeString))) ? endpointsMap.get(channelTypeString)
                : new HashMap<>();

        // Create(s) new endpoint(s) for address(es).
        List<String> createAddrList = validAddressList.stream()
                .filter(address -> !channelEndpointsMap.containsKey(address))
                .toList();
        if (CollectionUtils.isEmpty(createAddrList)) {
            LOGGER.info(
                    "Endpoint(s) are already available for "
                            +
                            "userId: {} channelType: {}. Hence skipping endpoint(s) creation",
                    userId,
                    channelTypeString);
            return channelEndpointsMap;
        }

        LOGGER.info("Creating new endpoints for userId: {} channelType: {}", userId, channelType);
        for (String address : createAddrList) {
            try {
                String endpointId = updateEndpoint(userId, channelType, address);
                if (StringUtils.isEmpty(endpointId)) {
                    continue;
                }
                EndpointResponse endpointResponse = getEndpoint(endpointId);
                if (ObjectUtils.isNotEmpty(endpointResponse)) {
                    channelEndpointsMap.put(address, endpointId);
                }
            } catch (AmazonPinpointException e) {
                LOGGER.error("Exception occurred while creating endpoint(s) {} ", e.getMessage());
            }
        }

        endpointsMap.put(channelTypeString, channelEndpointsMap);
        endpointDocument.setEndpoints(endpointsMap);
        endpointDocument.setUserId(userId);
        LOGGER.debug(
                "Updated info for userId: {}  channelType:{} endpointDocument:{} ", userId, channelTypeString,
                endpointDocument);
        pinpointEndpointDao.save(endpointDocument);
        return channelEndpointsMap;
    }


    /**
     * Create(s) pinpoint endpoint(s) for address for userId and secondary
     * contact(s).
     *
     * @param notificationConfig NotificationConfig
     * @param channelType ChannelType
     * @param addressList list of address
     * @return map of address and corresponding endpoint for lookup.
     */
    protected Map<String, String> updateEndpoints(NotificationConfig notificationConfig,
                                                  com.amazonaws.services.pinpoint.model.ChannelType channelType,
                                                  List<String> addressList) {

        Map<String, Map<ChannelType, List<String>>> usersMap = new HashMap<>();
        Map<ChannelType, List<String>> channelMap = null;
        List<String> address = null;


        String userId = "self".equalsIgnoreCase(notificationConfig.getContactId()) ? notificationConfig.getUserId() :
                notificationConfig.getContactId();
        channelMap = (usersMap.containsKey(userId)) ? usersMap.get(userId) : new HashMap<>();
        switch (channelType) {
            case SMS:
                addPhNum(notificationConfig, channelMap);
                break;
            case EMAIL:
                addEmailAddress(notificationConfig, addressList, channelMap);
                break;
            default:
                LOGGER.warn("Invalid channel type provided:{} ", channelType);
                break;
        }
        usersMap.put(userId, channelMap);

        Map<String, String> channelEndpointsMap = new HashMap<>();
        for (Entry<String, Map<ChannelType, List<String>>> userInfo : usersMap.entrySet()) {
            for (Entry<ChannelType, List<String>> channelEndpoints : userInfo.getValue().entrySet()) {
                String usrId = userInfo.getKey();
                updateEndpoints(usrId, channelEndpoints.getKey(),
                        channelEndpoints.getValue())
                        .forEach((k, v) -> channelEndpointsMap.putIfAbsent(k, v));
            }
        }
        return channelEndpointsMap;
    }

    /**
     * Add email address.
     *
     * @param notificationConfig NotificationConfig
     * @param addressList List of addresses
     * @param channelMap Map
     */
    private void addEmailAddress(NotificationConfig notificationConfig, List<String> addressList,
                                 Map<ChannelType, List<String>> channelMap) {
        List<String> address;
        EmailChannel emailChannel =
                notificationConfig.getChannel(org.eclipse.ecsp.domain.notification.commons.ChannelType.EMAIL);
        if (ObjectUtils.isNotEmpty(emailChannel) && emailChannel.getEnabled()) {
            List<String> emailList =
                    emailChannel.getEmails().stream().filter(addr -> addressList.contains(addr))
                            .toList();
            address = ObjectUtils.isNotEmpty(channelMap) ? channelMap.get(ChannelType.EMAIL) :
                    new ArrayList<String>();
            address.addAll(emailList);
            channelMap.put(ChannelType.EMAIL, address);
        }
    }

    /**
     * Add phone number.
     *
     * @param notificationConfig NotificationConfig
     * @param channelMap Map
     */
    private void addPhNum(NotificationConfig notificationConfig, Map<ChannelType, List<String>> channelMap) {
        List<String> address;
        SmsChannel smsChannel =
                notificationConfig.getChannel(org.eclipse.ecsp.domain.notification.commons.ChannelType.SMS);
        if (ObjectUtils.isNotEmpty(smsChannel) && smsChannel.getEnabled()
                &&
                !CollectionUtils.isEmpty(smsChannel.getPhones())) {
            address =
                    ObjectUtils.isNotEmpty(channelMap)
                            ? channelMap.get(ChannelType.SMS) : new ArrayList<String>();
            address.addAll(smsChannel.getPhones());
            channelMap.put(ChannelType.SMS, address);
        }
    }

    /**
     * Returns endpointId for given userId channel type and address.
     *
     * @param channelType String
     * @param userId String
     * @param address String
     * @return String
     */
    protected String getEndpointIdUserChannelAddr(String channelType, String userId, String address) {
        AmazonPinpointEndpoint endpointDocument = pinpointEndpointDao.findById(userId);
        if (ObjectUtils.isNotEmpty(endpointDocument) && ObjectUtils.isNotEmpty(endpointDocument.getEndpoints())
                && ObjectUtils.isNotEmpty(endpointDocument.getEndpoints().get(channelType))) {
            return endpointDocument.getEndpoints().get(channelType).get(address);
        }
        return null;
    }

    /**
     * Prepares Channel Response.
     *
     * @param result Map
     * @return Map
     */
    protected Map<String, EndpointResult> getDeliveryStatus(Map<String, EndpointMessageResult> result) {
        Map<String, EndpointResult> status = new HashMap<>();
        result.forEach((k, v) -> {
            EndpointResult endpointResult = new EndpointResult();
            endpointResult.setAddress(v.getAddress());
            endpointResult.setDeliveryStatus(v.getDeliveryStatus());
            endpointResult.setMessageId(v.getMessageId());
            endpointResult.setStatusCode(v.getStatusCode());
            endpointResult.setStatusMessage(v.getStatusMessage());
            endpointResult.setUpdatedToken(v.getUpdatedToken());
            status.put(k, endpointResult);
        });
        return status;
    }

    /**
     * Validate addresses.
     *
     * @param channelType ChannelType
     * @param addressList List of addresses
     * @return List
     */
    protected List<String> validateAddressList(ChannelType channelType, List<String> addressList) {
        if (channelType.equals(ChannelType.SMS)) {
            return validatePhoneNumer(addressList);
        }
        return addressList;
    }

    /**
     * Validates phone number(s).
     *
     * @param phoneNumberList list of String
     * @return list of String
     */
    protected List<String> validatePhoneNumer(List<String> phoneNumberList) {
        List<String> validPhoneNumberList = new ArrayList<>();
        for (String phoneNumber : phoneNumberList) {
            PhoneNumberValidateRequest phoneNumberValidateRequest = new PhoneNumberValidateRequest();
            NumberValidateRequest numberValidateRequest = new NumberValidateRequest();
            numberValidateRequest.setPhoneNumber(phoneNumber);
            phoneNumberValidateRequest.setNumberValidateRequest(numberValidateRequest);
            try {
                PhoneNumberValidateResult phoneNumberValidateResult =
                        client.phoneNumberValidate(phoneNumberValidateRequest);
                if (phoneNumberValidateResult.getNumberValidateResponse().getPhoneType().equals("INVALID")) {
                    String maskedPh = Utils.maskString(phoneNumber);
                    LOGGER.error("Invalid phone number provided: {}", maskedPh);
                } else {
                    validPhoneNumberList.add(phoneNumber);
                }
            } catch (AmazonPinpointException e) {
                String maskedPh = Utils.maskString(phoneNumber);
                LOGGER.error("Exception occurred while validating phoneNumber {} : {}", maskedPh,
                        e.getMessage());
            }
        }
        return validPhoneNumberList;
    }

    /**
     * Sends pinpoint message.
     *
     * @param pinpointRequest SendMessagesRequest
     * @return SendMessagesResult
     */
    protected SendMessagesResult sendMessage(SendMessagesRequest pinpointRequest) {
        return client.sendMessages(pinpointRequest);
    }
}
