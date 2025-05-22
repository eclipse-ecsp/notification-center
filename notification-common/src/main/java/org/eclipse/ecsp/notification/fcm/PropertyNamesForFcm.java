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

/**
 * PropertyNamesForFcm class.
 */
public final class PropertyNamesForFcm {

    /** SNS message structure constant. */
    public static final String SNS_MSG_STRUCTURE = "json";
    /** Token constant. */
    public static final String TOKEN = "token";
    /** APS constant. */
    public static final String APS = "aps";
    /** APNS constant. */
    public static final String APNS = "apns";
    /** Token ID constant. */
    public static final String TOKEN_ID = "tokenId";
    /** App name constant. */
    public static final String APP_NAME = "appname";
    /** Device ID constant. */
    public static final String DEVICE_ID = "deviceID";
    /** Success status constant. */
    public static final String SUCCESS_STATUS = "Success";
    /** Failed status constant. */
    public static final String FAILED_STATUS = "Failed";
    /** Status constant. */
    public static final String STATUS = "status";
    /** User ID constant. */
    public static final String USER_ID = "userID";
    /** Details constant. */
    public static final String DETAILS = "details";
    /** Error code constant. */
    public static final String ERROR_CODE = "errorCode";
    /** Error constant. */
    public static final String ERROR = "error";
    /** Upload timestamp constant. */
    public static final String UTS = "UploadTimeStamp";
    /** Alerts constant. */
    public static final String ALERTS = "alerts";
    /** Mongo ID constant. */
    public static final String MONGO_ID = "_id";
    /** Server upload timestamp constant. */
    public static final String STS = "serverUploadTimeStamp";
    /** Alert type constant. */
    public static final String ALERT_TYPE = "alertType";
    /** Alert attribute constant. */
    public static final String ALERT_ATTR = "alertAttribute";
    /** Collection token mapping constant. */
    public static final String COLLECTION_TOKEN_MAPPING = "notnTokenUserMap";
    /** Registration IDs constant. */
    public static final String REGISTRATION_IDS = "registration_ids";
    /** Space constant. */
    public static final String SPACE = "";
    /** Hash constant. */
    public static final String HASH = "#";
    /** ID constant. */
    public static final String ID = "_id";
    /** Equal to constant. */
    public static final String EQUAL_TO = " = ";
    /** Dynamo notification GSI user ID constant. */
    public static final String DYNAMO_NOTIFICATION_GSI_USERID = "userIDIndex";
    /** Invalid argument constant. */
    public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
    /** Unregistered constant. */
    public static final String UNREGISTERED = "UNREGISTERED";
    /** Invalid FCM registration token message constant. */
    public static final String INVALID_FCM_REG_TOKEN_MSG =
            "The registration token is not a valid FCM registration token";
    /** Notification constant. */
    public static final String NOTIFICATION = "notification";
    /** Notification ID constant. */
    public static final String NOTIFICATION_ID = "notificationId";
    /** Intended recipient constant. */
    public static final String INTENDED_REC = "intendedRecipient";
    /** Message constant. */
    public static final String MESSAGE = "message";
    /** Data constant. */
    public static final String DATA = "data";
    /** Title constant. */
    public static final String TITLE = "title";
    /** Icon constant. */
    public static final String ICON = "icon";
    /** Sound constant. */
    public static final String SOUND = "sound";
    /** Body constant. */
    public static final String BODY = "body";
    /** Payload constant. */
    public static final String PAYLOAD = "payload";
    /** FCM constant. */
    public static final String FCM = "FCM";
    /** Channel identifier constant. */
    public static final String CHANNEL_IDENTIFIER = "channelIdentifier";
    /** Name constant. */
    public static final String NAME = "name";
    /** Server timestamp constant. */
    public static final String SERVER_TIME_STAMP = "ServerTimeStamp";
    /** Alert timestamp constant. */
    public static final String ALERT_TIME_STAMP = "Timestamp";
    /** Alert timezone constant. */
    public static final String ALERT_TIME_ZONE = "Timezone";
    /** Comma constant. */
    public static final String COMMA = ",";
    /** Request method POST constant. */
    public static final String REQUEST_METHOD_POST = "POST";
    /** Content type constant. */
    public static final String CONTENT_TYPE = "Content-Type";
    /** Content length constant. */
    public static final String CONTENT_LENGTH = "Content-Length";
    /** Authorization constant. */
    public static final String AUTHORIZATION = "Authorization";
    /** Content type application JSON constant. */
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    /** FCM response constant. */
    public static final String FCM_RESPONSE = "fcmResponse";
    /** FCM payload constant. */
    public static final String FCM_PAYLOAD = "fcmPayload";
    /** FCM results constant. */
    public static final String FCM_RESULTS = "results";
    /** FCM error constant. */
    public static final String FCM_ERROR = "error";
    /** Error not registered constant. */
    public static final String ERROR_NOT_REGISTERED = "NotRegistered";
    /** Error invalid registration constant. */
    public static final String ERROR_INVALID_REGISTRATION = "InvalidRegistration";
    /** Registration ID constant. */
    public static final String REGISTRATION_ID = "registration_id";
    /** App platform constant. */
    public static final String APP_PLATFORM = "appPlatform";
    /** App platform Android constant. */
    public static final String APP_PLATFORM_ANDROID = "ANDROID";
    /** App platform iOS constant. */
    public static final String APP_PLATFORM_IOS = "IOS";
    /** App platform web constant. */
    public static final String APP_PLATFORM_WEB = "WEB";
    /** FCM message ID constant. */
    public static final String FCM_MESSAGE_ID = "message_id";
    /** Mutable content constant. */
    public static final String MUTABLE_CONTENT = "mutable_content";
    /** Content available constant. */
    public static final String CONTENT_AVAILABLE = "content_available";
    /** URL constant. */
    public static final String URL = "url";
    /** Image constant. */
    public static final String IMAGE = "image";

    /**
     * PropertyNamesForFcm.
     */
    private PropertyNamesForFcm() {
    }

    /**
     * Create PropertyNamesForFcm.
     *
     * @return PropertyNamesForFcm
     */
    public static PropertyNamesForFcm createPropertyNamesForFcm() {
        return new PropertyNamesForFcm();
    }
}
