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

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.parser.GenericValue;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for NotificationSettings data.
 */
public class NotificationUtils {
    /** Underscore constant. */
    public static final String UNDERSCORE = "_";
    /** Comma constant. */
    public static final String COMMA = ",";
    /** Empty string constant. */
    public static final String EMPTY_STRING = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationUtils.class);

    /** Variable start string constant. */
    public static final String VARIABLE_START_STRING = "[";
    /** Variable end string constant. */
    public static final String VARIABLE_END_STRING = "]";
    private static final String TIMESTAMP = "$.Timestamp";
    private static final String TIMEZONE = "$.Timezone";
    private static final int MILLISECOND_MULTILIER = 60 * 1000;
    /** Colon constant. */
    public static final String COLON = ":";
    /** Service reminder date field constant. */
    public static final String SERVICE_REMINDER_DATE_FIELD = "$.Data.reminderDate";
    /** Service name constant. */
    public static final String SERVICE_NAME = "service.name";
    private static final String NOT_FOUND = "<NOT_FOUND>";
    private static final String PLACEHOLDER_REGEX = "\\[(\\$(\\.\\w+)*+)]";
    private static final String VEHICLE_PROFILE = "vehicleProfile";
    private static final String VEHICLEPROFILE_VEHICLEATTRIBUTES = "vehicleProfile.vehicleAttributes";

    private NotificationUtils() {
    }

    /**
     * Returns true if the value of the EventID is NotificationSettings.
     *
     * @param eventData event data
     * @return boolean
     */
    public static boolean isNotificationSettingsRequest(String eventData) {
        String eventId;
        if (null != (eventId = getEventId(eventData))) {
            return eventId.equals(NotificationEventFields.EventIdValues.NOTIFICATION_SETTINGS.getEventType());
        }
        return false;
    }

    /**
     * Returns true if the value of the eventID is Association.
     *
     * @param eventData event data
     * @return boolean
     */
    public static boolean isUserToPdidAssociationRequest(String eventData) {
        String eventId;
        if (null != (eventId = getEventId(eventData))) {
            return eventId.equals(NotificationEventFields.EventIdValues.ASSOCIATION.getEventType());
        }
        return false;

    }

    /**
     * Return true if the value of the event id DisAssociation.
     *
     * @param eventData event data
     * @return boolean
     */
    public static boolean isUserDissociationRequest(String eventData) {
        String eventId;
        if (null != (eventId = getEventId(eventData))) {
            return eventId.equals(NotificationEventFields.EventIdValues.DISASSOCIATION.getEventType());
        }
        LOGGER.error("There is not EventID field in the jsonData:{} or JSON is not proper", eventData);
        return false;

    }

    /**
     * return the value of the EventID from the given json string.
     *
     * @param eventData event data
     * @return string event id
     */
    public static String getEventId(String eventData) {
        return JsonUtils.getValueAsString(NotificationEventFields.EVENT_ID, eventData);

    }

    /**
     * Returns the user id from the Data field.
     *
     * <p>{ EventID:"Association", Data: { PDID:"1234", UserID:"xyz" } }
     *
     * @param eventData event data
     * @return string
     */
    public static String getUserId(String eventData) {
        return JsonUtils.getJsonNode(NotificationEventFields.DATA, eventData).get(NotificationEventFields.USER_ID)
                .asText();
    }

    /**
     * Returns the token id from the Data field.
     *
     * <p>{ EventID:"Association", Data: { PDID:"1234", UserID:"xyz", token":"abc"
     * } }
     *
     * @param eventData event data
     * @return string
     */
    public static String getDeviceToken(String eventData) {
        return JsonUtils.getJsonNode(NotificationEventFields.DATA, eventData).get(NotificationEventFields.TOKEN)
                .asText();
    }

    /**
     * bind data json parser.
     */
    @SuppressWarnings("unchecked")
    public static <T> T bindData(String data, Class<?> clazz) {
        try {
            return (T) JsonUtils.bindData(data, clazz);
        } catch (IOException e) {
            LOGGER.error("Error while parsing the json data {} and exception got is {}", data, e.getMessage());
        }
        return null;

    }

    /**
     * Get objects list from the json.
     */
    // @SuppressWarnings("unchecked")
    public static <T> List<T> getListObjects(String data, Class<T> clazz) {
        try {
            return JsonUtils.getListObjects(data, clazz);
        } catch (IOException e) {
            LOGGER.error("Error while parsing the json data {} and exception got is {}", data, e.getMessage());
        }
        return new ArrayList<>();

    }

    /**
     * Get the value of the key from the input.
     */
    private static List<String> getVariables(String msg) {
        List<String> results = new LinkedList<>();

        if (msg == null) {
            return results;
        }

        Pattern pattern = Pattern.compile(PLACEHOLDER_REGEX);
        Matcher matcher = pattern.matcher(msg);
        String placeHolderContent;

        while (matcher.find()) {
            placeHolderContent = matcher.group(1);
            results.add(placeHolderContent);
        }
        return results;
    }

    /**
     * Get decorated message by resolving placeholders.
     */
    public static String getDecoratedMsg(String alertPayLoad, String msg) {


        // Get all the variables enclosed between []
        List<String> variables = getVariables(msg);
        DocumentContext documentContext = JsonPath.parse(alertPayLoad);

        msg = replacePlaceHolders(alertPayLoad, msg, variables, documentContext);

        List<String> embeddedPlaceholders = getVariables(msg);
        if (!embeddedPlaceholders.isEmpty()) {
            return replacePlaceHolders(alertPayLoad, msg, embeddedPlaceholders, documentContext);
        }

        return msg;
        // return the same msg if the variables are null
    }

    /**
     * Get decorated message by resolving placeholders.
     */
    public static String getDecoratedMsg(AlertsInfo alertsInfo, String msg) {
        LOGGER.debug("getDecoratedMsg: alertPayLoad - {}, msg - {} ", alertsInfo, msg);
        return getDecoratedMsg(JsonUtils.getObjectValueAsString(alertsInfo), msg);
    }

    /**
     * Get decorated message by resolving placeholders.
     */
    private static String replacePlaceHolders(String alertPayLoad, String msg, List<String> embeddedPlaceholders,
                                              DocumentContext documentContext) {
        for (String embeddedPlaceholder : embeddedPlaceholders) {
            // getting value of embeddedPlaceholder from payload using json
            Object value = null;
            String variable = embeddedPlaceholder;
            try {
                if (variable.contains(VEHICLE_PROFILE)) {
                    variable = variable.replace(VEHICLE_PROFILE, VEHICLEPROFILE_VEHICLEATTRIBUTES);
                }
                value = documentContext.read(variable);
            } catch (Exception ex) {
                msg = StringUtils.replace(
                        msg, VARIABLE_START_STRING + embeddedPlaceholder
                                + VARIABLE_END_STRING, NOT_FOUND);
            }
            if (null != value) {
                String valueStr;
                GenericValue gv = new GenericValue(value);
                valueStr = gv.asString();
                /*
                 * If the variable we are replacing is Timestamp or
                 * Data.reminderDate, then we convert the value to YYYY-mm-dd
                 * format
                 */
                if (embeddedPlaceholder.equals(TIMESTAMP) || embeddedPlaceholder.equals(SERVICE_REMINDER_DATE_FIELD)) {
                    int timezone = (JsonPath.parse(alertPayLoad).read(TIMEZONE));
                    valueStr = getDateAsString(valueStr, timezone);
                }
                // remove the ${json.variable.name} from the string
                if (null != valueStr) {
                    msg = StringUtils.replace(msg,
                            VARIABLE_START_STRING + embeddedPlaceholder
                                    + VARIABLE_END_STRING, valueStr);
                }
            }
        }
        return msg;
    }

    /**
     * Utility method to convert the time in milliseconds to date format by
     * taking timezone into consideration.
     *
     * <p>Calendar object is used to convert the epoch timestamp as a long into a
     * Date object
     *
     * @param ts time String
     * @param tz timezone
     * @return String
     */
    private static String getDateAsString(String ts, int tz) {
        long timestamp = Long.parseLong(ts);
        long localTimeInMs = timestamp + (long) tz * MILLISECOND_MULTILIER;
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat timeStampFormatter = new SimpleDateFormat("yyyy-MM-dd");
        timeStampFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(localTimeInMs);
        return timeStampFormatter.format(cal.getTime());
    }

    /**
     * Resolve string payload to igniteevent using the tranformer.
     *
     * @param notificationPayload notificationPayload
     *
     * @param igniteEventTransformer igniteEventTransformer
     *
     * @return ignite event
     */
    public static IgniteEvent resolveEventPayload(String notificationPayload,
                                                  GenericIgniteEventTransformer igniteEventTransformer) {
        try {
            IgniteEvent igniteEvent =
                    igniteEventTransformer.fromBlob(notificationPayload.getBytes(StandardCharsets.UTF_8),
                            Optional.empty());
            Preconditions.checkNotNull(igniteEvent.getRequestId(), "%s is missing or null: %s", "requestId",
                    igniteEvent);
            return igniteEvent;
        } catch (Exception e) {

            throw new IllegalArgumentException("Error retrieving RequestId from scheduler payload:", e);
        }
    }

    /**
     * Get the retry exception.
     *
     * @param exception exception
     * @return string
     */
    public static String getRetryException(Exception exception) {
        return exception.toString().split(":")[0];
    }

}