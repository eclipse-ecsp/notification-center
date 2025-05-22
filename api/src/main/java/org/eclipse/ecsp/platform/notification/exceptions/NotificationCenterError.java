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

package org.eclipse.ecsp.platform.notification.exceptions;

import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;

import java.util.Arrays;

import static org.eclipse.ecsp.platform.notification.service.NotificationTemplateService.getTemplateExportHeaders;
import static org.eclipse.ecsp.platform.notification.service.RichContentNotificationTemplateService.getRichHtmlExportHeaders;

/**
 * NotificationCenterError codes.
 */
public enum NotificationCenterError {

    // success codes:
    NON_REGISTERED_SUCCESS("nc-0001", "send.notification.to.non.registered.users.success",
        "Send notification to non registered users success"),
    FCM_TOKEN_DELETE_SUCCESS("nc-0002", "fcm.token.delete.success", "FCM-Token deleted successfully"),
    DELETE_SCHEDULED_NOTIFICATION_SUCCESS("nc-0003", "delete.scheduled.notification.success",
        "Scheduled notification deleted successfully"),
    PLACEHOLDER_SUCCESS("nc-0004", "placeholder.success", "Placeholder success"),

    // Exception data
    INVALID_NOTIFICATION_ID_EXCEPTION("nc-1400", "invalid.notification.id", "Invalid notification id"),
    INVALID_INPUT_FILE_EXCEPTION("nc-1401", "invalid.input.file", "Invalid input file"),
    NOTIFICATION_TEMPLATE_DOES_NOT_EXIST_EXCEPTION("nc-1402", "notification.template.does.not.exist",
        "Notification template does not exist"),
    INVALID_IMAGE_EXCEPTION("nc-1403", "invalid.image", "Invalid image"),
    INVALID_INPUT_EXCEPTION("nc-1404", "invalid.input", "Invalid input"),
    GROUPING_DOES_NOT_EXIST("nc-1405", "grouping.does.not.exist", "Grouping does not exist"),
    NOT_FOUND_EXCEPTION("nc-1406", "not.found", "Not found"),

    // common errors
    NOTIFICATION_ID_DOES_NOT_EXIST("nc-10201", "notification.id.doesn't.exist", "Notification ID doesn't exist: %s"),
    INPUT_NUMBER_OF_ROWS("nc-10202", "invalid.csv.lines.number", "File must have at least 3 lines"),
    INPUT_MANDATORY_NOTIFICATION_ID("nc-10203", "invalid.csv.notification.id", "Notification ID is mandatory"),
    INPUT_EMPTY_LOCALE("nc-10204", "invalid.csv.attributes.header", "Language header can not be empty"),
    INPUT_DUPLICATE_LOCALE_PREFIX("nc-10205", "invalid.csv.duplicated.locales",
        "There are duplicate languages in the file: %s"),
    INPUT_INVALID_LOCALE("nc-10206", "invalid.csv.locales", "Invalid locale(s): %s"),
    INVALID_USERID_EXCEPTION("nc-10207", "invalid.userid", "userId constraint failed"),
    ERROR_SENDING_EVENT_EXCEPTION("nc-10208", "error.sending.event.to.kafka",
        "Error occured while sending event to kafka"),
    PLATFORM_RESPONSE_ID_NOT_FOUND("nc-10209", "platform.response.id.not.found", "Platform response id not found: %s"),
    CONTENT_TYPE_DOSE_NOT_EXIST("nc-10210", "content.type.does.not.exist",
        "Content type must be either 'status' or 'full'"),
    INPUT_NUMBER_OF_LOOKUP_PROPERTIES("nc-10211", "invalid.number.of.additional.lookup.properties",
        "File can have maximum 1 additional lookup property"),

    // notification template crud
    TEMPLATE_INPUT_MISSING_HEADERS("nc-10301", "invalid.csv.headers", "Missing one of the headers in the first line"),
    TEMPLATE_INPUT_MISSING_HEADER_DATA("nc-10302", "invalid.csv.missing.header.data",
        "Missing notification ID or name"),
    TEMPLATE_INPUT_MISSING_PROPERTIES_HEADER("nc-10303", "invalid.csv.missing.attributes.headers",
        "File must contain " + Arrays.toString(getTemplateExportHeaders()) + " and at least one language"),
    TEMPLATE_INPUT_MISSING_DATA_FIELD("nc-10304", "invalid.csv.missing.attributes.data",
        "Missing one of the fields: " + Arrays.toString(getTemplateExportHeaders())),
    TEMPLATE_INPUT_EMPTY_MANDATORY_DATA("nc-10305", "invalid.csv.empty.attributes.data",
        Arrays.toString(getTemplateExportHeaders()) + " can not be empty"),
    TEMPLATE_INPUT_DUPLICATE_ROW_PREFIX("nc-10306", "invalid.duplicate.attributes",
        Arrays.toString(getTemplateExportHeaders())
            + " have to be unique. The following record(s) are duplicate: %s"),
    TEMPLATE_INPUT_NOTIFICATION_NOT_EXIST("nc-10307", "notification.template.does.not.exist",
        "Notification template ID %s does not exist"),
    TEMPLATE_INVALID_SENDER_ID("nc-10308", "notification.template.invalid.sender.id",
        "Sender ID must be 1-11 alpha-numeric characters"),
    TEMPLATE_INVALID_FORMAT("nc-10309", "notification.template.file.invalid.format", "CSV File format should be UTF-8"),

    // rich html crud
    RICH_HTML_INPUT_MISSING_CSV_FILE("nc-10401", "invalid.input.file.content",
        "Zip file must contain exactly one csv file"),
    RICH_HTML_INPUT_MISSING_HEADERS("nc-10402", "invalid.csv.headers", "Missing notification ID header"),
    RICH_HTML_INPUT_MISSING_PROPERTIES_HEADER("nc-10403", "invalid.csv.locales.number",
        "File must contain " + Arrays.toString(getRichHtmlExportHeaders()) + " and at least one language"),
    RICH_HTML_INPUT_MISSING_DATA_FIELD("nc-10404", "invalid.csv.missing.attributes",
        "Missing one of the fields: " + Arrays.toString(getRichHtmlExportHeaders())),
    RICH_HTML_INPUT_EMPTY_MANDATORY_DATA("nc-10405", "invalid.csv.empty.attributes",
        Arrays.toString(getRichHtmlExportHeaders()) + " can not be empty"),
    RICH_HTML_INPUT_DUPLICATE_ROW_PREFIX("nc-10406", "invalid.csv.duplicate.attributes",
        Arrays.toString(getRichHtmlExportHeaders())
            + " have to be unique. The following record(s) are duplicated: %s"),
    RICH_HTML_INPUT_MISSING_REFERENCE_HTML("nc-10407", "invalid.csv.missing.html.brand.reference",
        "Missing Reference-HTML attribute for brand(s): %s"),
    RICH_HTML_INPUT_MISSING_REFERENCE_HTML_VALUE("nc-10408", "invalid.csv.missing.html.brand.and.locale.reference",
        "Missing Reference-HTML value for the following brand(s) and locale(s): %s"),
    RICH_HTML_INPUT_MISSING_FILE("nc-10409", "invalid.input.missing.file.reference", "Missing following file(s): %s"),
    RICH_HTML_INPUT_MISSING_ATTRIBUTE("nc-10410", "invalid.input.missing.placeholder",
        "%s placeholder from HTML %s is missing for brand %s"),
    RICH_HTML_INPUT_INVALID_IMAGE("nc-10411", "invalid.image", "Invalid image: %s"),
    RICH_HTML_INPUT_INVALID_FILE_TYPE("nc-10412", "invalid.file.type", "Invalid file type"),

    // non register user notifications
    NON_REGISTERED_INPUT_MISSING_RECIPIENTS("nc-10501", "invalid.input.recipients",
        "At least one recipient is mandatory"),
    NON_REGISTERED_INPUT_MAX_RECIPIENTS_EXCEEDED("nc-10502", "invalid.input.max.recipients.exceeded",
        "Max allowed %s recipient was exceeded (%s)"),
    NON_REGISTERED_INPUT_MISSING_RECIPIENT_CHANNELS("nc-10503", "invalid.input.no.channels.for.recipient",
        "%s recipients without channels"),
    NON_REGISTERED_INPUT_INVALID_LOCALE("nc-10504", "invalid.input.locale.for.recipient",
        "recipient %s invalid locale: %s"),
    NON_REGISTERED_INPUT_INVALID_EMAIL("nc-10505", "invalid.input.email.addresses",
        "Following email addresses are invalid: %s"),
    NON_REGISTERED_INPUT_INVALID_SMS("nc-10506", "invalid.input.phone.numbers",
        "Following phone numbers are invalid: %s"),
    NON_REGISTERED_INPUT_INVALID_PORTAL("nc-10507", "invalid.input.mqtt.topic", "Following topics are invalid: %s"),
    // non registered vehicle errors
    NOTIFICATION_INVALID_NON_REGISTERED_VEHICLE("nc-10508", "invalid.input.nonregistered.vehicle-id",
        "invalid nonregistered vehicle id"),

    // Notification config
    NOTIFICATION_CONFIG_INPUT_MISSING_CONFIG_REQUEST("nc-10601", "invalid.input.config-request",
        "At least one notification config is required"),
    NOTIFICATION_CONFIG_USER_ID_NOT_FOUND("nc-10602", "invalid.input.user-id.not-found", "user id not found"),
    NOTIFICATION_CONFIG_INVALID_GROUPS("nc-10603", "invalid.input.invalid-groups", "invalid groups"),
    NOTIFICATION_CONFIG_MANDATORY_GROUP_NOT_ALLOWED("nc-10604", "invalid.input.mandatory-groups-not",
        "Notification config can not contains mandatory groups, the invalid group is: "),
    NOTIFICATION_CONFIG_API_PUSH_DISABLE_NOT_ALLOWED("nc-10606", "invalid.input.contact.api-push.disable-not-allowed",
        "Can not disable  api push channel"),
    NOTIFICATION_CONFIG_API_PUSH_SUPPRESSION_NOT_ALLOWED("nc-10607",
        "invalid.input.contact.api-push.suppress-not-allowed", "Can not suppress api push channel"),
    FCM_TOKEN_DOES_NOT_EXIST("nc-10608", "fcm.token.does.not.exist", "FCM-Token does not exist"),
    NOTIFICATION_CONFIG_NOT_FOUND("nc-10609", "config.not.found", "Config not found"),
    NOTIFICATION_CONFIG_GROUP_NOT_ALLOWED("nc-10610", "group.not.allowed.for.api",
        "The following groups: %s are not allowed for API of type %s"),
    CHANNEL_NOT_ALLOWED_FOR_GROUP("nc-10611", "channels.not.allowed.for.group",
        "Channels %s are not allowed for group %s"),
    DEFAULT_CONFIG_FOR_BRAND_WITHOUT_DEFAULT("nc-10612", "default.config.for.brand.without.default",
        "Create default config for brand without default is not allowed"),
    NOTIFICATION_CONFIG_VEHICLE_ID_NOT_FOUND("nc-10613", "invalid.input.vehicle-id.not-found", "vehicle id not found"),
    NOTIFICATION_CONFIG_SERVICE_NAME_NOT_FOUND("nc-10614", "invalid.input.service-name.not-found",
        "service name not found"),

    // grouping
    GROUPING_MULTI_GROUP_FOR_NOTIFICATION_ID("nc-10701", "invalid.input.grouping.multi.groups.for.notification",
        "More than one group associated to the same notification id (%s)"),
    GROUPING_DUPLICATE_KEY("nc-10702", "invalid.input.grouping.duplicate.key",
        "Duplicate key (notification id, group, service)"),
    GROUPING_NAME_DOES_NOT_EXIST("nc-10703", "group.doesn't.exist", "Group doesn't exist"),
    GROUPING_NOTIFICATION_ID_SERVICE_DOES_NOT_EXIST("nc-10704", "threesome.group.notificationId.service.doesn't.exist",
        "Threesome of group, notification ID and service doesn't exist"),
    GROUPING_NOTIFICATION_ID_NOT_FOUND("nc-10705", "notification.id.not.found", "Notification id not found"),

    // Scheduled notification
    SCHEDULED_NOTIFICATION_MAX_DELAY_INVALID("nc-10800", "invalid.input.value.schedule",
        "Scheduled notification can be set only for future date. Max delay is %s days. Requested delay: (%s)"),
    DELETE_SCHEDULED_NOTIFICATION_ERROR("nc-10801", "scheduled.notification.cannot.be.deleted",
        "Scheduled notification status does not allow deletion"),
    INVALID_TIME_FORMAT("nc-10802", "invalid.time.format", "Invalid time format"),

    // Mute vehicle Config
    MUTE_CONFIG_FAILURE_GETTING_VEHICLE("nc-10901", "failure.getting.vehicle",
        "Failure occurred while fetching vehicle"),
    MUTE_CONFIG_INVALID_TIME("nc-10902", "invalid.time.format", "Invalid start or end time format"),
    MUTE_CONFIG_INVALID_END_TIME("nc-10903", "invalid.end.time", "End time should not be <= start time"),
    MUTE_CONFIG_VEHICLE_ID_DOES_NOT_EXIST("nc-10904", "vehicle.id.does.not.exist", "Vehicle id does not exist"),
    MUTE_CONFIG_CREATE_SUCCESS("nc-10905", "mute.vehicle.config.created.success",
        "Mute vehicle config created successfully"),
    MUTE_CONFIG_FIND_SUCCESS("nc-10906", "mute.vehicle.config.retrieved.success",
        "Mute vehicle config retrieved successfully"),
    MUTE_CONFIG_DELETE_SUCCESS("nc-10907", "mute.vehicle.config.deleted.success",
        "Mute vehicle config deleted successfully"),
    MUTE_CONFIG_INVALID_GROUP("nc-10908", "group.does.not.exist", "Group(s) does not exist"),

    // Custom attributes placeholders
    PLACEHOLDERS_INPUT_MISSING_LINES("nc-11001", "input.missing.lines", "File contain less than 2 rows"),
    PLACEHOLDERS_INPUT_MISSING_COLUMNS("nc-11002", "input.missing.columns", "File contain less than 3 columns"),
    PLACEHOLDERS_INPUT_MISSING_HEADERS("nc-11003", "input.missing.headers",
        "Missing one of the headers in the first line"),
    PLACEHOLDERS_INPUT_MISSING_DATA_FIELD("nc-11004", "invalid.csv.missing.data", "Missing one of the fields"),
    PLACEHOLDERS_INPUT_DUPLICATE_DATA("nc-11005", "invalid.csv.duplicate.data",
        "Lookup fields have to be unique. The following record(s) are duplicated: %s"),
    PLACEHOLDERS_INPUT_INVALID_CSV("nc-11006", "placeholder.invalid.csv", "Invalid CSV file"),
    PLACEHOLDERS_NOT_FOUND("nc-11007", "placeholder.not.found", "Placeholder not found"),
    PLACEHOLDERS_WRONG_HEADERS_ORDER("nc-11008", "input.headers,order", "Headers are in wrong order"),

    // Date Format Content Transformer
    DATE_FORMATTER_CONTENT_TRANSFORMER_INVALID_TEMPLATE("nc-11009", "dateFormatter.invalid.template",
        "Invalid template: %s. Format should be [[formatDate|SOURCE_DATE_FORMAT|TARGET_DATE_FORMAT|[$.PLACEHOLDERS]]]."
            + "For eg [[formatDate|yyyy-MM-dd|dd-MM-yyyy|[$.PLACEHOLDER]]]"),
    DATE_FORMATTER_CONTENT_TRANSFORMER_INVALID_DATE_PATTERN("nc-11010", "dateFormatter.invalid.date.pattern",
        "Invalid source/target date pattern provided:  %s. Exception: %s");
    private final String code;
    private final String reason;
    private final String message;

    /**
     * NotificationCenterError constructor.
     *
     * @param code    error code
     * @param reason  error reason
     * @param message error message
     */
    NotificationCenterError(String code, String reason, String message) {
        this.code = code;
        this.reason = reason;
        this.message = message;
    }

    /**
     * Returns the error code.
     *
     * @return error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the error reason.
     *
     * @return error reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns the error message.
     *
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the error message with formatted arguments.
     *
     * @param formatParam arguments to format the message
     * @return formatted message
     */
    public String getMessage(String... formatParam) {
        return String.format(message, (Object[]) formatParam);
    }

    /**
     * Returns a message.
     *
     * @return message
     */
    public ResponseWrapper.Message toMessage() {
        return ResponseWrapper.Message.of(code, reason, message);
    }

    /**
     * Returns a message with formatted arguments.
     *
     * @param args arguments to format the message
     * @return formatted message
     */
    public ResponseWrapper.Message toMessage(Object... args) {
        return ResponseWrapper.Message.of(code, reason, String.format(message, args));
    }
}
