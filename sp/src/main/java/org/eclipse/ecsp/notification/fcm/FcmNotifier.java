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

package org.eclipse.ecsp.notification.fcm;


import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.apache.http.HttpStatus;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertEventData;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.FCMChannelResponse;
import org.eclipse.ecsp.domain.notification.FcmPushAlertEventData;
import org.eclipse.ecsp.domain.notification.PushChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.adaptor.AbstractChannelNotifier;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.domain.notification.commons.EventID.NON_REGISTERED_USER_NOTIFICATION_EVENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_CLIENT;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NON_REGISTERED_FIELD_PUSH_TOKEN;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM_ANDROID;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM_IOS;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.APP_PLATFORM_WEB;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.BODY;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.IMAGE;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.MUTABLE_CONTENT;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.NOTIFICATION;
import static org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm.TITLE;

/**
 * FcmNotifier class.
 */
@Component
public class FcmNotifier extends AbstractChannelNotifier {

    private static int TWENTY_THOUSAND = 20000;

    private static int TEN_THOUSAND = 10000;

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmNotifier.class);
    private static final String CHANNELTYPE = ChannelType.MOBILE_APP_PUSH.getProtocol();
    private static final String SVC_PROVIDER = "PUSH:FCM";
    private String notificationEntityName;
    private String notificationDeviceFieldName;
    
    private String fcmProjectId;
    
    private String filePath;

    
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };

    private String fcmUrl = "https://fcm.googleapis.com/v1/projects/%s/messages:send";

    /**
     * Constructor.
     */
    public FcmNotifier() {
    }

    /**
     * Method creates Payload as per FCM specification. Sample payload looks
     * like : {"notification":{"sound":true,"icon":"icon","body": "This is a DTC
     * STored Event"},"data":{"payload":
     * "{\"EventID\":\"DTCStored\",\"Data\":{\"latitude\":12.9688785,
     * \"name\":\"name\",\"geofenceId\":\"58d73ff946e0fb000c78ea89\",
     * \"position\":\"in\",\"type\":\"generic\",\"longitude\":
     * \"77.6933817\"},\"Version\":\"1.0\",\"BenchMode\":1,\"Timestamp\":1486123552319,
     * \"Timezone\":330,\"pdid\":\"HUHLWSDJ0L1562\"}"
     * ,"channelIdentifier":"FCM"},"registration_ids":[
     * "dJ_LaAupV1E:APA91bGhwkpi6dpU9zVUjB3T1GYC74aFvdS_vOcDPSDuj
     * jpmnwXmYFOB3hQUkOEWpBUd5Bea8VbQvMGayLrQEbLutS425n-g6m0_OrmnPQNcEPruQaQH_
     * E8agsbf0ACrGEHRmiFPBPUM"
     * ]}
     *
     * @param payloadAlert alert info
     * @param deviceId   fcm registration token
     * @param alertMessage message
     * @return byte[]
     * @throws JSONException JSONException
     */
    private static byte[] getFcmPayload(AlertsInfo payloadAlert, String deviceId, String alertMessage)
            throws JSONException {


        // notification key value pairs - Data that is used for notification tray, when app is in background.
        JSONObject notificationObject = new JSONObject();
        notificationObject.put(BODY, alertMessage);
        String title = payloadAlert.getNotificationTemplate().getPushTemplate().getTitle();
        if (title != null) {
            notificationObject.put(TITLE, title);
        }
       
        // data key value pair - Data that is used for mobile app when it is in foreground.

        JSONObject dataObject = new JSONObject();
        dataObject.put(PropertyNamesForFcm.PAYLOAD, JsonUtils.getObjectValueAsString(payloadAlert));
        dataObject.put(PropertyNamesForFcm.CHANNEL_IDENTIFIER, PropertyNamesForFcm.FCM);
        
        //Apns

        JSONObject payloadObject = new JSONObject();
        payloadObject.put(PropertyNamesForFcm.APS, new JSONObject());
        payloadObject.put(PropertyNamesForFcm.ICON, PropertyNamesForFcm.ICON);
        payloadObject.put(PropertyNamesForFcm.SOUND, "true");
        String imageUrl = payloadAlert.getNotificationTemplate().getPushTemplate().getImage();
        if (imageUrl != null) {
            payloadObject.put(MUTABLE_CONTENT, "true");
            payloadObject.put(IMAGE, imageUrl);

        }
        JSONObject apnsObject = new JSONObject();
        apnsObject.put(PropertyNamesForFcm.PAYLOAD, payloadObject);
        JSONObject messageObject = new JSONObject();
        messageObject.put(NOTIFICATION, notificationObject);
        messageObject.put(PropertyNamesForFcm.DATA, dataObject);
        messageObject.put(PropertyNamesForFcm.APNS, apnsObject);
        messageObject.put(PropertyNamesForFcm.TOKEN, deviceId);
        JSONObject parentPayload = new JSONObject();
        parentPayload.put(PropertyNamesForFcm.MESSAGE, messageObject);

        LOGGER.debug("FCM Notification Payload : {}", parentPayload);
        return parentPayload.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Method creates Payload as per FCM specification for Android. Sample payload
     *
     * @param payloadAlert alert info
     * @param deviceId  fcm registration token
     * @param alertMessage message
     * @return byte[]
     * @throws JSONException JSONException
     */
    private static byte[] getAndroidFcmPayload(AlertsInfo payloadAlert, String deviceId, String alertMessage)
            throws JSONException {


        // data key value pair - For Android only Data object will be sent and
        // this will be handled by android app.Either app is in background or
        // foreground
        JSONObject dataObject = new JSONObject();
        dataObject.put(PropertyNamesForFcm.PAYLOAD, JsonUtils.getObjectValueAsString(payloadAlert));
        dataObject.put(PropertyNamesForFcm.CHANNEL_IDENTIFIER, PropertyNamesForFcm.FCM);
        dataObject.put(PropertyNamesForFcm.BODY, alertMessage);
        dataObject.put(PropertyNamesForFcm.ICON, PropertyNamesForFcm.ICON);
        dataObject.put(PropertyNamesForFcm.SOUND, "true");
        JSONObject messageObject = new JSONObject();
        messageObject.put(PropertyNamesForFcm.DATA, dataObject);
        messageObject.put(PropertyNamesForFcm.TOKEN, deviceId);
        JSONObject parentPayload = new JSONObject();
        parentPayload.put(PropertyNamesForFcm.MESSAGE, messageObject);

        LOGGER.debug("FCM Android Notification Payload : {}, for deviceId:{}", parentPayload, deviceId);
        return parentPayload.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Method creates Payload as per FCM specification for Web. Sample payload
     *
     * @param deviceId  fcm registration token
     * @param alertMessage message
     * @return byte[]
     * @throws JSONException JSONException
     */
    private static byte[] getWebFcmPayload(String deviceId, String alertMessage) throws JSONException {

        JSONObject messagePayload = new JSONObject();
        JSONObject dataObject = new JSONObject();
        dataObject.put(PropertyNamesForFcm.BODY, alertMessage);
        messagePayload.put(PropertyNamesForFcm.DATA, dataObject);
        messagePayload.put(PropertyNamesForFcm.TOKEN, deviceId);
        JSONObject parentPayload = new JSONObject();
        parentPayload.put(PropertyNamesForFcm.MESSAGE, messagePayload);
        LOGGER.debug("FCM WEB Notification Payload : {}, for deviceId:{}", parentPayload, deviceId);
        
        return parentPayload.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Method to convert input stream to string.
     *
     * @param inStream InputStream
     * @return String
     * @throws IOException IOException
     */
    private static String convertStreamToString(InputStream inStream) throws IOException {
        InputStreamReader inputStream = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader buffReader = new BufferedReader(inputStream);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = buffReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Method to get the protocol.
     *
     * @return String
     */
    @Override
    public String getProtocol() {
        return CHANNELTYPE;
    }

    /**
     * Init method.
     *
     * @param props       Properties
     * @param metricRegistry    MetricRegistry
     * @param notificationDao NotificationDao
     */
    @Override
    public void init(Properties props, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        super.init(props, metricRegistry, notificationDao);
        fcmProjectId = props.getProperty("fcm.project.id");
        filePath = props.getProperty("service-json.file.path");
        notificationEntityName = props.getProperty("vehicle.haa.platform.notification.entity.name");
        notificationDeviceFieldName = props.getProperty("vehicle.haa.platform.notification.field.name");
    }

    /**
     * Method to publish alert.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {

        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        FCMChannelResponse response = new FCMChannelResponse(userId);
        if (!validateUserId(response, userId, alert)) {
            return response;
        }
        LOGGER.debug("FCM pushing alert for user: {}", userId);
        String alertMsg = alert.getNotificationTemplate().getChannelTemplates().getPush().getBody();
        AlertEventData defaultData = getDefaultAlertData(alertMsg);
        String responseString;

        Map<String, String> appPlatformToRegistrationIds;
        appPlatformToRegistrationIds = getappPlatformToRegistrationIdsMap(alert, userId);
        List<String> tokens = new ArrayList<>(appPlatformToRegistrationIds.values());
        String destinationsString = tokens.stream().map(Object::toString).collect(Collectors.joining(","));
        response.setDestination(destinationsString);
        String appPlatform;
        String registrationIds;
        for (Map.Entry<String, String> entry : appPlatformToRegistrationIds.entrySet()) {
            appPlatform = entry.getKey();
            registrationIds = entry.getValue();
            byte[] postData;
            StringBuilder postDataConsolidated = new StringBuilder();
            StringBuilder responseConsolidated = new StringBuilder();
            boolean fcmSucceeded = false;
            for (String regId : registrationIds.split(PropertyNamesForFcm.COMMA)) {
                LOGGER.debug("userid :{}, registrationId:{}, appPlatform:{}", userId, registrationIds, appPlatform);

                postData = getPostData(alert, appPlatform, regId, alertMsg);
                responseString = publishFcmNotification(postData, regId);

                postDataConsolidated.append(new String(postData, Charset.defaultCharset()));
                responseConsolidated.append(responseString);

                fcmSucceeded = handleNewFcmResponse(responseString, regId, userId, appPlatform.toUpperCase(),
                        NON_REGISTERED_USER_NOTIFICATION_EVENT.equals(alert.getEventID()), fcmSucceeded);
            }

            FcmPushAlertEventData data = new FcmPushAlertEventData();
            data.setDefaultMessage(defaultData.getDefaultMessage());
            data.setFcmPayload(postDataConsolidated.toString());
            data.setFcmResponse(responseConsolidated.toString());
            data.setAppPlatform(appPlatform);
            response.setAlertData(data);
            response.setTemplate(alert.getNotificationTemplate().getChannelTemplates().getPush());
            String notificationId = alert.getNotificationTemplate().getNotificationId();
            if (fcmSucceeded) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Alert %s sent successfully to FCM",
                            notificationId));
                }
                response.setStatus(NOTIFICATION_STATUS_SUCCESS);
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(String.format("Failed publishing alert %s to FCM",
                            notificationId));
                }
                response.setStatus(NOTIFICATION_STATUS_FAILURE);
            }
        }

        return response;
    }

    /**
     * Method to get the app platform to registration ids map.
     *
     * @param alert AlertsInfo
     * @param userId String
     * @return Map
     */
    private @NotNull Map<String, String> getappPlatformToRegistrationIdsMap(AlertsInfo alert, String userId) {
        Map<String, String> appPlatformToRegistrationIds;
        if (NON_REGISTERED_USER_NOTIFICATION_EVENT.equals(alert.getEventID())) {
            appPlatformToRegistrationIds = getFcmDataFromRecipient(alert);
        } else {
            appPlatformToRegistrationIds = fetchAppPlatformToRegistrationIds(userId);
        }
        return appPlatformToRegistrationIds;
    }

    /**
     * Method to get the FCM data from recipient.
     *
     * @param alert AlertsInfo
     * @param appPlatform String
     * @param registrationId String
     * @param alertMsg String
     * @return byte[]
     */
    @NotNull
    private byte[] getPostData(AlertsInfo alert, String appPlatform, String registrationId, String alertMsg) {
        byte[] postData;
        switch (appPlatform.toUpperCase()) {
            case APP_PLATFORM_ANDROID:
                LOGGER.debug("Coming for Android");
                postData = getAndroidFcmPayload(alert, registrationId, alertMsg);
                break;
            case APP_PLATFORM_IOS:
                LOGGER.debug("Coming for IOS");
                postData = getFcmPayload(alert, registrationId, alertMsg);
                break;
            case APP_PLATFORM_WEB:
                LOGGER.debug("Coming for WEB");
                postData = getWebFcmPayload(registrationId, alertMsg);
                break;
            default:
                postData = getFcmPayload(alert, registrationId, alertMsg);
        }
        return postData;
    }

    /**
     * Method to get the FCM data from recipient.
     *
     * @param userId String
     * @return Map
     */
    @NotNull
    private Map<String, String> fetchAppPlatformToRegistrationIds(String userId) {

        Map<String, Object> constraintNameValueMap = new HashMap<>();
        constraintNameValueMap.put(PropertyNamesForFcm.USER_ID, userId);
        Map<String, Object> fieldNameValuesToBeFetched = new HashMap<>();
        fieldNameValuesToBeFetched.put(notificationDeviceFieldName, 1);
        fieldNameValuesToBeFetched.put(PropertyNamesForFcm.APP_PLATFORM, 1);
        Map<String, String> appPlatformToRegistrationIds;
        List<Map<String, Object>> documentList = notificationDao.getFieldsValueByFields(constraintNameValueMap,
                notificationEntityName, fieldNameValuesToBeFetched);
        appPlatformToRegistrationIds = getAppPlatformToRegistrationIds(documentList);
        if (CollectionUtils.isEmpty(appPlatformToRegistrationIds)) {
            String msg = "Missing token in DB for " + constraintNameValueMap.toString();
            LOGGER.error(msg);
            throw new IllegalStateException(msg);
        }
        return appPlatformToRegistrationIds;
    }

    /**
     * Method to get access token.
     *
     * @return String
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    private String getAccesstoken() throws FileNotFoundException, IOException {
        LOGGER.info("FilePath: {}", filePath);
        GoogleCredentials gc = GoogleCredentials
                .fromStream(new FileInputStream(filePath))
                .createScoped(Arrays.asList(SCOPES));
        gc.refreshIfExpired();

        AccessToken token = gc.getAccessToken();
        LOGGER.debug("token: {}", token.getTokenValue());
        return token.getTokenValue();
    }

    /**
     * Method to publish FCM notification.
     *
     * @param postData byte[]
     * @param deviceId String
     * @return String
     */
    String publishFcmNotification(byte[] postData, String deviceId) {
        int responseCode;
        String responseBody = null;
        fcmUrl = String.format(fcmUrl, fcmProjectId);
        try {
            LOGGER.info("Sending FCM Notification request to Device Id : {} to URL : {}", deviceId, fcmUrl);
            URL url = new URL(fcmUrl);
            HttpsURLConnection httpUrlConnection = (HttpsURLConnection) url.openConnection();
            httpUrlConnection.setConnectTimeout(TWENTY_THOUSAND);
            httpUrlConnection.setReadTimeout(TEN_THOUSAND);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setRequestMethod(PropertyNamesForFcm.REQUEST_METHOD_POST);
            httpUrlConnection.setRequestProperty(PropertyNamesForFcm.CONTENT_TYPE,
                    PropertyNamesForFcm.CONTENT_TYPE_APPLICATION_JSON);
            httpUrlConnection.setRequestProperty(PropertyNamesForFcm.CONTENT_LENGTH, Integer.toString(postData.length));
            httpUrlConnection.setRequestProperty(PropertyNamesForFcm.AUTHORIZATION, "Bearer " + getAccesstoken());
            OutputStream out = httpUrlConnection.getOutputStream();
            out.write(postData);
            out.close();
            responseCode = httpUrlConnection.getResponseCode();
            if (responseCode == HttpStatus.SC_OK) {
                responseBody = convertStreamToString(httpUrlConnection.getInputStream());
                LOGGER.info("FCM message sent successfully : {}", responseBody);

                // failure
            } else {
                responseBody = convertStreamToString(httpUrlConnection.getErrorStream());
                LOGGER.error("Sending FCM request failed for regId: {} response: {}", deviceId, responseBody);
            }
        } catch (IOException ioe) {
            LOGGER.error("IO Exception in sending FCM request. regId: {}", deviceId);
            ioe.printStackTrace();
        } catch (Exception e) {
            LOGGER.error("Unknown exception in sending FCM request. regId: {} {} ", deviceId, e.getMessage());
            e.printStackTrace();
        }
        return responseBody;
    }

    /**
     * Method to Setup Channel.
     *
     * @param notificationConfig Channel configuration
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
        String userId = notificationConfig.getUserId();
        String vehicleId = notificationConfig.getVehicleId();
        LOGGER.debug("FCM setting up channel for user :{}", userId);
        FCMChannelResponse fcmResponse = new FCMChannelResponse(userId, vehicleId);
        fcmResponse.setUserID(userId);
        PushChannel pushChannel = notificationConfig.getChannel(ChannelType.MOBILE_APP_PUSH);
        int startIndex = 0;
        String token = pushChannel.getDeviceTokens().get(startIndex);
        String appPlatform = pushChannel.getAppPlatform();
        Map<String, Object> tokenUserId = new HashMap<>();
        tokenUserId.put(PropertyNamesForFcm.ID, token);
        tokenUserId.put(PropertyNamesForFcm.USER_ID, userId);
        tokenUserId.put(PropertyNamesForFcm.APP_PLATFORM, appPlatform);
        if (pushChannel.getEnabled()) {
            notificationDao.deleteSingleDocument(token, notificationEntityName);
            notificationDao.insertSingleDocument(tokenUserId, notificationEntityName);
            fcmResponse.setFcmResponse(
                    String.format("setup Channel for FCM push successful for device %s and user %s", token, userId));
        } else {
            notificationDao.deleteSingleDocument(token, notificationEntityName);
            fcmResponse.setFcmResponse(String.format("Push channel not enabled for %s and user %s", token, userId));
        }
        return fcmResponse;
    }

    /**
     * Method does device token management based on the response from FCM. In
     * case of error removing token and updating if directive from FCM (new
     * Token or canonical id for device).
     *
     * @param responseString response
     * @param deviceIds      fcm registration tokens
     * @param userId         user ID
     * @param platform       platform
     * @param isNonRegisterEvent boolean
     * @return True - if returned OK, False - otherwise
     */
    public boolean handleFcmResponse(String responseString, List<String> deviceIds, String userId, String platform,
                                     boolean isNonRegisterEvent) {
        boolean isSucceeded = false;
        JsonNode node = JsonUtils.getJsonNode(PropertyNamesForFcm.FCM_RESULTS, responseString);

        if (node != null) {
            for (int i = 0; i < node.size(); i++) {
                isSucceeded = isSucceeded(deviceIds, userId, platform, isNonRegisterEvent, node, i, isSucceeded);
            }
        }

        return isSucceeded;
    }

    /**
     * handleNewFcmResponse method.
     *
     * @param responseString String
     * @param deviceId String
     * @param userId String
     * @param platform String
     * @param isNonRegisterEvent Boolean
     * @param fcmSucceeded Boolean
     * @return boolean
     */
    public boolean handleNewFcmResponse(String responseString,
                                        String deviceId, String userId, String platform,
                                        boolean isNonRegisterEvent, boolean fcmSucceeded) {

        JSONObject json = new JSONObject(responseString);

        if (json.has(PropertyNamesForFcm.NAME)) {
            fcmSucceeded = true;
        } else if (json.has(PropertyNamesForFcm.ERROR)) {
            JsonNode node = JsonUtils.getJsonNode(PropertyNamesForFcm.ERROR, responseString);

            isSucceededNew(deviceId, node);

        }

        return fcmSucceeded;

    }
    
    /**
     * isSucceededNew method.
     *
     * @param deviceId String
     * @param node JsonNode
     */
    private void isSucceededNew(String deviceId,
                                JsonNode node) {

        JsonNode errorResponseTypeArr = node.get(PropertyNamesForFcm.DETAILS);
        String errorResponseType = errorResponseTypeArr.get(0).get(PropertyNamesForFcm.ERROR_CODE).asText();

        String errorResponseMessage = node.get(PropertyNamesForFcm.MESSAGE).asText();

        if ((errorResponseType.equalsIgnoreCase(PropertyNamesForFcm.INVALID_ARGUMENT)
                && errorResponseMessage.equalsIgnoreCase(PropertyNamesForFcm.INVALID_FCM_REG_TOKEN_MSG))
                || errorResponseType.equalsIgnoreCase(PropertyNamesForFcm.UNREGISTERED)) {

            LOGGER.error("Token {} for the device {} is either invalid or not registered. Removing it.",
                    errorResponseType,
                    deviceId);
            notificationDao.deleteSingleDocument(deviceId, notificationEntityName);

        }


    }

    /**
     * isSucceeded method.
     *
     * @param deviceIds List
     * @param userId String
     * @param platform String
     * @param isNonRegisterEvent Boolean
     * @param node JsonNode
     * @param i int
     * @param isSucceeded Boolean
     * @return boolean
     */
    private boolean isSucceeded(List<String> deviceIds, String userId, String platform,
                                boolean isNonRegisterEvent, JsonNode node, int i, boolean isSucceeded) {
        String errorResponseType = null;
        String successResponseType = null;

        if (node.get(i).get(PropertyNamesForFcm.FCM_ERROR) != null) {
            errorResponseType = node.get(i).get(PropertyNamesForFcm.FCM_ERROR).asText();
        }

        if (node.get(i).get(PropertyNamesForFcm.REGISTRATION_ID) != null
                &&
                !APP_PLATFORM_WEB.equals(platform)) {
            successResponseType = node.get(i).get(PropertyNamesForFcm.REGISTRATION_ID).asText();
            isSucceeded = true;
        } else if (node.get(i).get(PropertyNamesForFcm.FCM_MESSAGE_ID) != null
                &&
                !node.get(i).has(PropertyNamesForFcm.FCM_ERROR)) {
            isSucceeded = true;
        }

        if (errorResponseType != null) {

            if (PropertyNamesForFcm.ERROR_NOT_REGISTERED.equals(errorResponseType)
                    || PropertyNamesForFcm.ERROR_INVALID_REGISTRATION.equals(errorResponseType)) {
                LOGGER.error("Token {} for the device {} is either invalid or not registered. Removing it.",
                        errorResponseType,
                        deviceIds.get(i));
                notificationDao.deleteSingleDocument(deviceIds.get(i), notificationEntityName);
            }
        } else if (successResponseType != null && !isNonRegisterEvent) {
            LOGGER.info("Received Canonical Id from FCM, removing old token {}, and inserting new one {}",
                    deviceIds.get(i),
                    successResponseType);
            notificationDao.deleteSingleDocument(deviceIds.get(i), notificationEntityName);
            Map<String, Object> tokenUserId = new HashMap<>();
            tokenUserId.put(PropertyNamesForFcm.ID, successResponseType);
            tokenUserId.put(PropertyNamesForFcm.USER_ID, userId);
            notificationDao.insertSingleDocument(tokenUserId, notificationEntityName);
        }
        return isSucceeded;
    }

    /**
     * Method to destroy channel.
     *
     * @param userId    String
     * @param eventData String
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse destroyChannel(String userId, String eventData) {
        String token = null;
        FCMChannelResponse fcmResponse = new FCMChannelResponse(userId, null, eventData);
        fcmResponse.setUserID(userId);
        try {
            token = NotificationUtils.getDeviceToken(eventData);
            LOGGER.info("FCM destroying channel for user :{} and token {}", userId, token);
            if (token != null) {
                notificationDao.deleteSingleDocument(token, notificationEntityName);
            }
            fcmResponse.setFcmResponse(
                    "Destroying Channel for FCM push successful for device " + token + " and user " + userId);
        } catch (Exception e) {
            LOGGER.error("Error while deleting token {}", token);
            LOGGER.error(e.getMessage());
        }
        return fcmResponse;
    }

    /**
     * Get Platform to Registration Ids.
     *
     * @param documents List
     * @return Map
     */
    private Map<String, String> getAppPlatformToRegistrationIds(List<Map<String, Object>> documents) {
        Map<String, String> platformToRegistrationIds = new HashMap<>();
        if (null == documents) {
            LOGGER.info("documents is null");
            return platformToRegistrationIds;
        }
        for (Map<String, Object> doc : documents) {

            String appPlatform = (String) doc.get(PropertyNamesForFcm.APP_PLATFORM);
            String registrationId = (String) doc.get(PropertyNamesForFcm.ID);

            if (null == appPlatform) {
                appPlatform = PropertyNamesForFcm.SPACE;
            }
            if (registrationId == null || registrationId.trim().isEmpty()) {
                LOGGER.info("Invalid/Empty registration id found ,Skip adding");
                continue;
            }
            String ids = platformToRegistrationIds.get(appPlatform);
            if (ids == null) {
                ids = registrationId;
            } else {
                ids = ids + PropertyNamesForFcm.COMMA + registrationId;
            }
            platformToRegistrationIds.put(appPlatform, ids);
        }
        return platformToRegistrationIds;
    }

    /**
     * Method to get FCM data from recipient.
     *
     * @param alert AlertsInfo
     * @return Map
     */
    private Map<String, String> getFcmDataFromRecipient(AlertsInfo alert) {
        Map<String, String> platformToRegistrationIds = new HashMap<>();
        if (alert.getAlertsData().getAlertDataProperties().containsKey(NON_REGISTERED_FIELD_PUSH_CLIENT)
                &&
                alert.getAlertsData().getAlertDataProperties().containsKey(NON_REGISTERED_FIELD_PUSH_TOKEN)) {
            String client =
                    alert.getAlertsData().getAlertDataProperties().get(NON_REGISTERED_FIELD_PUSH_CLIENT).toString()
                            .toUpperCase();
            String token =
                    alert.getAlertsData().getAlertDataProperties().get(NON_REGISTERED_FIELD_PUSH_TOKEN).toString();
            platformToRegistrationIds.put(client, token);
        }
        return platformToRegistrationIds;
    }

    /**
     * Method to get the service provider name.
     *
     * @return String
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }


}
