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

package org.eclipse.ecsp.domain.notification.commons;

/**
 * NotificationConstants class.
 */
public interface NotificationConstants {

    /**
     * NOTIFICATION_TOPIC.
     */
    String NOTIFICATION_TOPIC = "notification";
    /**
     * FEEDBACK_TOPIC.
     */
    String FEEDBACK_TOPIC = "feedbackTopic";
    /**
     * FEEDBACK_KEY.
     */
    String FEEDBACK_KEY = "feedbackKey";
    /**
     * MILESTONE_FEEDBACK_TOPIC.
     */
    String MILESTONE_FEEDBACK_TOPIC = "milestoneFeedbackTopic";
    /**
     * MILESTONE_FEEDBACK_KEY.
     */
    String MILESTONE_FEEDBACK_KEY = "milestoneFeedbackKey";
    /**
     * TOPIC_SEPARATOR.
     */
    String TOPIC_SEPARATOR = "/";
    /**
     * EMPTY.
     */
    String EMPTY = "";
    /**
     * UNDERSCORE.
     */
    String UNDERSCORE = "_";
    /**
     * COMMA.
     */
    String COMMA = ",";
    /**
     * DOUBLE_QUOTES.
     */
    String DOUBLE_QUOTES = "\"";
    /**
     * DEFAULT.
     */
    String DEFAULT = "default";
    /**
     * SOUND.
     */
    String SOUND = "sound";
    /**
     * ALERT.
     */
    String ALERT = "alert";
    /**
     * SPACE.
     */
    String SPACE = " ";
    String APNS = "APNS";
    String GCM = "GCM";
    String DATA = "data";
    String STATUS = "status";
    String SERVICE_DATA = "serviceData";
    String DISASSOCIATION_TOPIC = "vehicle-profile-modified-authorized-users";

    String DRY_RUN = "dry_run";
    String MESSAGE = "message";
    String PAYLOAD = "payload";
    String COLUMN = ":";
    String APS = "aps";
    String USERID = "userId";
    String VEHICLEID = "vehicleId";
    String NOTIFICATION_ID = "notificationId";
    String SESSION_ID = "SessionId";
    String PLATFORM_RESPONSE_ID = "PlatformResponseId";
    String CLIENT_REQUEST_ID = "ClientRequestId";
    String SCHEDULE_NOTIFICATION = "schedule";

    // MessageType enum values
    String RECALL_NOTICE = "RECALL_NOTICE";
    String SERVICE_NOTICE = "SERVICE_NOTICE";
    String PC_NOTIFICATION = "PC_NOTIFICATION";
    String ENROLLMENT = "ENROLLMENT";
    String SUBSCRIPTION_NOTICE = "SUBSCRIPTION_NOTICE ";
    String MARKETING = "MARKETING";
    String GENERAL = "GENERAL";
    String CUSTOM_EXTENSION = "CUSTOM_EXTENSION";

    // MessageDetailsType enum values
    String RECALL_ID = "RECALL_ID";
    String RECALL_COMPONENT = "RECALL_COMPONENT";
    String RECALL_SUMMARY = "RECALL_SUMMARY";
    String RECALL_CONSEQUENCE = "RECALL_CONSEQUENCE";
    String OWNER_NEXT_STEPS = "OWNER_NEXT_STEPS ";
    String SERVICE_DUE_DATE = "SERVICE_DUE_DATE";
    String SERVICE_SUMMARY = "SERVICE_SUMMARY";
    String SUBSCRIPTION_TO_EXPIRE = "SUBSCRIPTION_TO_EXPIRE";
    String SUBSCRIPTION_EXPIRED = "SUBSCRIPTION_EXPIRED";
    String OTHER = "OTHER";
    String SUBSCRIPTION_END_DATE = "SUBSCRIPTION_END_DATE";
    String SUBSCRIPTION_TO_RENEW = "SUBSCRIPTION_TO_RENEW";
    String SUBSCRIPTION_DAYS_TO_EXPIRE = "SUBSCRIPTION_DAYS_TO_EXPIRE";

    // MessageDispositionEnum values
    String MESSAGE_CONFIRMED_BY_OPERATOR = "MESSAGE_CONFIRMED_BY_OPERATOR";
    String MESSAGE_DISPLAY_CANCELLED = "MESSAGE_DISPLAY_CANCELLED";
    String MESSAGE_DISPLAY_CONFIRMED_CALL = "MESSAGE_DISPLAY_CONFIRMED_CALL";
    String MESSAGE_VIN_NOT_VALID = "MESSAGE_VIN_NOT_VALID";
    String MESSAGE_TIMED_OUT = "MESSAGE_TIMED_OUT ";
    String MESSAGE_LANGUAGES_NOT_SUPPORTED = "MESSAGE_LANGUAGES_NOT_SUPPORTED";
    String MESSAGE_DISPLAY_FAILED = "MESSAGE_DISPLAY_FAILED";
    String MESSAGE_AUTO_DELETE = "MESSAGE_AUTO_DELETE";
    String MESSAGE_DELETE = "MESSAGE_DELETE";

    // DispositionResponseEnum values
    String SUCCESS = "SUCCESS";
    String FAILURE = "FAILURE";

    // VehicleMessageAckData ResponseEnum values
    String MESSAGE_STAGED_FOR_DISPLAY = "MESSAGE_STAGED_FOR_DISPLAY";
    String MESSAGE_TYPE_NOT_SUPPORTED = "MESSAGE_TYPE_NOT_SUPPORTED";
    String MESSAGE_STAGING_FAILED = "MESSAGE_STAGING_FAILED";
    String MESSAGE_DESTINATION_TEMPORARILY_NOT_AVAILABLE = "MESSAGE_DESTINATION_TEMPORARILY_NOT_AVAILABLE";
    String DEFAULT_MESSAGE = "Default message is unavailable.";
    String PDID = "pdid";
    String ID = "id";
    String TIMESTAMP = "timestamp";
    String ALERT_TYPE = "alertType";
    String ALERT_MESSAGE = "alertMessage";
    String READ = "read";

    String FCM_PROVIDER = "FCM_PUSH";
    String SMTP_PROVIDER = "SMTP_EMAIL";
    String MQTT_PROVIDER = "MQTT_PORTAL";
    String API_PUSH_PROVIDER = "API_PUSH";
    String AWS_SMS_PROVIDER = "AWS_SNS_SMS";
    String AWS_PUSH_PROVIDER = "AWS_SNS_PUSH";
    String AWS_SES_PROVIDER = "AWS_SES";
    String IVM_PROVIDER = "IVM";
    String AWS_PINPOINT_PROVIDER = "PINPOINT";
    String AWS_PINPOINT_EMAIL_PROVIDER = "AWS_PINPOINT_EMAIL";
    String AWS_PINPOINT_SMS_PROVIDER = "AWS_PINPOINT_SMS";
    String ANDROID_MSG_TTL = "time_to_live";

    String FCM_RESPONSE = "fcmResponse";
    String FCM_PAYLOAD = "fcmPayload";
    String APP_PLATFORM = "appPlatform";

    String NOTIFICATION_STATUS_SUCCESS = "Success";
    String NOTIFICATION_STATUS_FAILURE = "Failure";
    String NOTIFICATION_STATUS_MISSING_DESTINATION = "MissingDestination";
    String NOTIFICATION_ACK_GROUP = "NotificationAckGroup";
    String CAMPAIGN_ID = "campaignId";
    String CAMPAIGN_DATE = "campaignDate";
    String HARMAN_ID = "harmanId";
    String FILE_NAME = "fileName";
    String COUNTRY_CODE = "countryCode";
    String NOTIFICATION_CONTACT_ID_SELF = "self";

    // Added for RTC 159393: Notification: Incorrect validation for version
    // parameter in notification API
    String VERSION = "version";
    String MESSAGE_VERSION_NUMBER_NOT_VALID = "Version is not valid.";
    String MESSAGE_VERSION_NUMBER_EMPTY = "Version must not be empty.";
    String USER_NOTIFICATION = "UserNotification";
    String BRAND = "brand";

    String NON_REGISTER_MESSAGE_ID = "NON_REGISTER_MESSAGE_ID";
    String NON_REGISTERED_FIELD_RECIPIENTS = "recipients";
    int MAX_NON_REGISTERED_USERS_PER_REQUEST_HARD_LIMIT = 1000;
    String NON_REGISTERED_FIELD_PUSH_TOKEN = "token";
    String NON_REGISTERED_FIELD_PUSH_CLIENT = "client";

    String MESSAGE_ATTACHMENTS = "attachments";
    String NOTIFICATION_CAMPAIGN_ID = "campaignId";
    String NOTIFICATION_TYPE = "NOTIFICATION";
    String REFRESH_USER = "refreshUser";

    String CUSTOM_PLACEHOLDERS = "customPlaceholders";
    String VEHICLE_DATA = "vehicleProfile";
    String VEHICLE_ATTRIBUTES_DATA = "vehicleAttributes";
    String MODEL = "vehicleProfile.model";
    String MODEL_CODE = "vehicleProfile.modelCode";
    String ADDITIONAL_ATTRIBUTE_MODEL_HEADER = "prop.vehicleProfile.model";
    String ADDITIONAL_ATTRIBUTE_MODEL_CODE_HEADER = "prop.vehicleProfile.modelCode";
    String NOTIFICATION_RETRY = "NOTIFICATIONRETRY:";
    String NON_REGISTERED_VEHICLE_FIELD = "isNonRegisteredVehicle";
    String EMERGENCY_CONTACTS = "emergencyContacts";
    String LICENSE_PLATE = "licensePlate";
    String CHANNEL_SERVICE_PROIVDER_CONFIG_COLLECTION_NAME = "channelServiceProviderConfig";
    String NC_STATIC_CONFIG_CLASS_NAME =
        "org.eclipse.ecsp.notification.db.client.CachedChannelServiceProviderConfigDaoImpl";

    String TRANSACTIONAL_MESSAGE_TYPE = "TRANSACTIONAL";
    String CHANNEL_RESPONSES_STATUS = "channelResponses.status";
    String STATUS_HISTORY_RECORD_STATUS = "statusHistoryRecordList.status";

    int REMAINING_SIZE_IN_BYTES = 20971520;

    int ALERT_STATUS_INIT_SIZE = 7;
}
