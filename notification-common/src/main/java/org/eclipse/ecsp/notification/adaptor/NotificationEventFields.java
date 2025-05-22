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

package org.eclipse.ecsp.notification.adaptor;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationEventFields class.
 */
public class NotificationEventFields {

    /** Event ID constant. */
    public static final String EVENT_ID = "EventID";
    /** User ID constant. */
    public static final String USER_ID = "userId";
    /** Data constant. */
    public static final String DATA = "Data";
    /** PDID constant. */
    public static final String PDID = "pdid";
    /** PDIDS constant. */
    public static final String PDIDS = "pdids";
    /** SMS constant. */
    public static final String SMS = "sms";
    /** Token constant. */
    public static final String TOKEN = "token";
    /** Channels constant. */
    public static final String CHANNELS = "channels";
    /** Channel type constant. */
    public static final String CHANNEL_TYPE = "type";
    /** Notification config constant. */
    public static final String NOTIFICATION_CONFIG = "notificationConfig";
    /** Timestamp constant. */
    public static final String TIMESTAMP = "Timestamp";
    /** Email constant. */
    public static final String EMAIL = "email";
    /** Enabled constant. */
    public static final String ENABLED = "enabled";
    /** Alert types constant. */
    public static final String ALERT_TYPES = "alertTypes";

    private NotificationEventFields() {
    }

    /**
     * Create NotificationEventFields object.
     *
     * @return NotificationEventFields object
     */
    public static NotificationEventFields createNotificationEventFields() {
        return new NotificationEventFields();
    }

    /**
     * For notification we have 3 different kinds of event id values.
     */
    public enum EventIdValues {
        NOTIFICATION_SETTINGS("NotificationSettings"),
        ASSOCIATION("VehicleAssociation"),
        DISASSOCIATION("VehicleDisAssociation");

        private String eventType;

        private EventIdValues(String eventType) {
            this.eventType = eventType;
        }

        public String getEventType() {
            return eventType;
        }

    }

    /**
     * What are the various channel types we are supporting.
     */
    public enum ChannelType {
        /**
         * These are the only supported channel types as of now.
         */
        MOBILE_APP_PUSH("push", "application"),
        SMS("sms", "sms"),
        EMAIL(NotificationEventFields.EMAIL, NotificationEventFields.EMAIL),
        API_PUSH("apiPush", "apiPush"),
        BROWSER("browser", "browser"),
        IVM("ivm", "ivm"),
        PORTAL("portal", "portal");

        private String chType;
        private String channelTypeName;

        private ChannelType(String chType, String channelName) {
            this.chType = chType;
            this.channelTypeName = channelName;
        }

        /**
         * getChType method.
         *
         * @return String
         */
        public String getChType() {
            return this.chType;
        }

        /**
         * isChannelSupported method.
         *
         * @param ch String
         * @return boolean
         */
        public static boolean isChannelSupported(String ch) {

            for (ChannelType type : ChannelType.values()) {
                if (type.getChType().equals(ch)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * getChannel method.
         *
         * @param chType String
         * @return ChannelType
         */
        public static ChannelType getChannel(String chType) {
            for (ChannelType type : ChannelType.values()) {
                if (type.getChType().equals(chType)) {
                    return type;
                }
            }
            return null;
        }

        /**
         * getChannelTypeName method.
         *
         * @return String
         */
        public String getChannelTypeName() {
            return channelTypeName;
        }
    }

    /**
     * "type": "sms", "enabled": true, "ph": "478347387", "alertTypes":.
     * ["accident"]
     */
    public enum SmsFields {
        ENABLED(NotificationEventFields.ENABLED),
        PHONE_NUM("ph"),
        ALERT_TYPE(ALERT_TYPES);

        private String fieldName;

        private SmsFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return this.fieldName;
        }
    }

    /**
     * "enabled": true, "token": "sxw8jhsd", "service": "apns/gcm",
     * "alertTypes": ["all"].
     */
    public enum DevicePushFields {
        ENABLED(NotificationEventFields.ENABLED),
        DEVICE_TOKEN(NotificationEventFields.TOKEN),
        SERVICE("service"),
        ALERT_TYPE(ALERT_TYPES),
        APP_PLATFORM("appPlatform");

        private String fieldName;

        private DevicePushFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return this.fieldName;
        }
    }

    /**
     * EmailFields enum.
     */
    public enum EmailFields {
        ENABLED(NotificationEventFields.ENABLED),
        EMAIL(NotificationEventFields.EMAIL),
        ALERT_TYPE(ALERT_TYPES);

        private String fieldName;

        private EmailFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return this.fieldName;
        }
    }

    /**
     * PortalFields enum.
     */
    public enum PortalFields {
        ENABLED(NotificationEventFields.ENABLED),
        USER("user"),
        ALERT_TYPE(ALERT_TYPES);

        private String fieldName;

        private PortalFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return this.fieldName;
        }
    }

    /**
     * Method which iterates.
     *
     * @param chType String
     */
    public static String getEndPointFieldName(String chType) {
        ChannelType type = ChannelType.getChannel(chType);
        if (type != null) {
            switch (type) {
                case MOBILE_APP_PUSH:
                    return DevicePushFields.DEVICE_TOKEN.getFieldName();
                case SMS:
                    return SmsFields.PHONE_NUM.getFieldName();
                case EMAIL:
                    return EmailFields.EMAIL.getFieldName();
                case BROWSER:
                    return PortalFields.USER.getFieldName();
                default:
                    return null;

            }
        }
        return null;

    }

    /**
     * Method which iterates.
     *
     * @param chType String
     */
    public static String getSnsType(String chType) {
        ChannelType type = ChannelType.getChannel(chType);
        return type != null ? type.getChannelTypeName() : "";
    }

    /**
     * Return the list of the channels we support.
     *
     * @return list of channels
     */
    public static List<String> getChannelsList() {
        List<String> channelList = new ArrayList<String>();

        for (ChannelType chType : ChannelType.values()) {
            channelList.add(chType.getChannelTypeName());
        }

        return channelList;
    }
}
