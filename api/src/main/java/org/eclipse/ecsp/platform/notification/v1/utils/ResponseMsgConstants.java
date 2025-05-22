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

package org.eclipse.ecsp.platform.notification.v1.utils;

import org.eclipse.ecsp.platform.notification.v1.common.Constants;

/**
 * ResponseMsgConstants class.
 */
public class ResponseMsgConstants {

    private ResponseMsgConstants() {}

    /** Invalid idling threshold message constant. */
    public static final String INVALID_IDLING_THRESHOLD_MSG = "Received invalid idling threshold";
    /** Invalid tow threshold message constant. */
    public static final String INVALID_TOW_THRESHOLD_MSG = "Received invalid tow threshold";
    /** Unsupported API version message constant. */
    public static final String UNSUPPORTED_API_VERSION_MSG = "Unsupported api version received in URI";
    /** Invalid device ID message constant. */
    public static final String INVALID_DEVICE_ID_MSG = "Received invalid device id in URI";
    /** Invalid item ID message constant. */
    public static final String INVALID_ITEM_ID_MSG = "Received invalid item id in URI";
    /** Invalid item IDs message constant. */
    public static final String INVALID_ITEM_IDS_MSG = "Received invalid item ids in URI";
    /** Invalid payload message constant. */
    public static final String INVALID_PAYLOAD_MSG = "Received invalid payload";
    /** Invalid alert since message constant. */
    public static final String INVALID_ALERT_SINCE_MSG = "Received invalid start time";
    /** Invalid location size message constant. */
    public static final String INVALID_LOCATION_SIZE_MSG = "Received invalid location size";
    /** Invalid alert until message constant. */
    public static final String INVALID_ALERT_UNTIL_MSG = "Received invalid end time";
    /** Invalid alerts specified message constant. */
    public static final String INVALID_ALERTS_SPECIFIED = "Received invalid alert types list";
    /** Invalid geofence message constant. */
    public static final String INVALID_GEOFENCE_MSG = "One or more invalid geofenceIds provided:";
    /** Invalid alert since or until specified message constant. */
    public static final String INVALID_ALERT_SINCE_OR_UNTIL_SPECIFIED =
            "Received invalid time range as start time is greater than end time.";
    /** Empty dongle detach message constant. */
    public static final String EMPTY_DONGLE_DETACH_MSG =
            "Alert on removal (alertOnDetach) is not specified but is mandatory";
    /** Empty dongle reattach message constant. */
    public static final String EMPTY_DONGLE_REATTACH_MSG =
            "Alert on attach (alertOnAttach) is not specified but is mandatory";
    /** No data found message constant. */
    public static final String NO_DATA_FOUND = "No data found for this device id.";
    /** Invalid fuel log IDs message constant. */
    public static final String INVALID_FUEL_LOG_IDS = "Invalid fuelLogIds : ";
    /** Invalid refill date message constant. */
    public static final String INVALID_REFILL_DATE =
            "Invalid refillDate. The value should be greater than zero and less than the current time in milliseconds ";
    /** Refill date missing message constant. */
    public static final String REFILL_DATE_MISSING = "refillDate is missing.";
    /** Refill value missing message constant. */
    public static final String REFILL_VALUE_MISSING = "refill value is missing";
    /** Refill value should be greater than zero message constant. */
    public static final String REFILL_VALUE_SHOULD_BE_GREATER_THAN_ZERO = "refill value should be greater than zero";
    /** Odometer value missing message constant. */
    public static final String ODOMETER_VALUE_MISSING = "odometer value is missing";
    /** Odometer value should be positive value message constant. */
    public static final String ODOMETER_VALUE_SHOULD_BE_POSITIVE_VALUE = "odometer value should be a positive number";
    /** Fuel unit price value missing message constant. */
    public static final String FUEL_UNIT_PRICE_VALUE_MISSING = "fuel unit price value is missing";
    /** Fuel unit price value should be positive value message constant. */
    public static final String FUEL_UNIT_PRICE_VALUE_SHOULD_BE_POSITIVE_VALUE =
            "fuel unit price value should be positive number";
    /** Fuel total price value missing message constant. */
    public static final String FUEL_TOTAL_PRICE_VALUE_MISSING = "fuel total price value is missing";
    /** Fuel total price value should be positive value message constant. */
    public static final String FUEL_TOTAL_PRICE_VALUE_SHOULD_BE_POSITIVE_VALUE =
            "fuel total price value should be positive number";
    /** Invalid engine status message constant. */
    public static final String INVALID_ENGINE_STATUS = "Invalid engineStatus value";
    /** Invalid doors status message constant. */
    public static final String INVALID_DOORS_STATUS = "Invalid doorsStatus value";
    /** Unsupported fuel type message constant. */
    public static final String UNSUPPORTED_FUELTYPE = "Unsupported fuelType : ";
    /** Unsupported geofence type message constant. */
    public static final String UNSUPPORTED_GEOFENCETYPE = "Unsupported geofenceType : ";
    /** Unsupported breach type message constant. */
    public static final String UNSUPPORTED_BREACHTYPE = "Unsupported breachType : ";
    /** Duplicate geofence name message constant. */
    public static final String DUPLICATE_GEOFENCE_NAME = "Geofence already exist with name : ";
    /** Device ID reference message constant. */
    public static final String DEVICE_ID_REFERRENCE = " for device ID : ";
    /** Unsupported odometer unit type message constant. */
    public static final String UNSUPPORTED_ODOMETERUNITTYPE = "Unsupported odometerUnitType : ";
    /** Unsupported fuel unit type message constant. */
    public static final String UNSUPPORTED_FUELUNITTYPE = "Unsupported fuelUnitType : ";
    /** Unsupported fuel price unit type message constant. */
    public static final String UNSUPPORTED_FUELPRICEUNITTYPE = "Unsupported fuelPriceUnitType : ";
    /** Unknown message constant. */
    public static final String UNKNOWN = "UNKNOWN";
    /** Unsupported API message constant. */
    public static final String UNSUPPORTED_API = "Unsupported api operation";
    /** Details empty message constant. */
    public static final String DETAILS_EMPTY = "details cannot be empty";
    /** Device ID mandatory message constant. */
    public static final String DEVICE_ID_MANDATORY = "deviceId is mandatory";
    /** Service date mandatory message constant. */
    public static final String SERVICE_DATE_MANDATORY = "Service date is mandatory";
    /** Future service date set message constant. */
    public static final String FUTURE_SERVICE_DATE_SET = "Cannot set future date for service date";
    /** Service station mandatory message constant. */
    public static final String SERVICE_STATION_MANDATORY = "Service station is mandatory";
    /** Service station name mandatory message constant. */
    public static final String SERVICE_STATION_NAME_MANDATORY = "Service station name is mandatory";
    /** Odometer reading mandatory message constant. */
    public static final String ODOMETER_READING_MANDATORY = "Odometer reading is mandatory";
    /** Service record ID mandatory message constant. */
    public static final String SERVICE_RECORD_ID_MANDATORY = "service record id parameter is mandatory";
    /** Service record invalid message constant. */
    public static final String SERVICE_RECORD_INVALID = "Invalid service record Id";
    /** Service record already exist for service date message constant. */
    public static final String SERVICE_RECORD_ALREADY_EXIST_FOR_SERVICEDATE =
            "Service record already exist for given service date and time. Please try to update the same";
    /** Exceed total file size message constant. */
    public static final String EXCEED_TOTAL_FILE_SIZE =
            "Please consider that the total size of document should not exceed allocated size of";
    /** Exceed total file limit message constant. */
    public static final String EXCEED_TOTAL_FILE_LIMIT =
            "Please consider that the total no of document should not exceed allocated limit of";
    /** Invalid file type message constant. */
    public static final String INVALID_FILE_TYPE = "Please upload valid file type";
    /** Invalid file name message constant. */
    public static final String INVALID_FILE_NAME = "Invalid file name";
    /** File does not exist message constant. */
    public static final String FILE_DOES_NOT_EXIST = "File does not exist, file id -";
    /** Error while copying file to temp location message constant. */
    public static final String ERROR_WHILE_COPY_FILE_TO_TEMP_LOC = "Error while copying file to temporary folder:";
    /** Copy file to temp location failed message constant. */
    public static final String COPY_FILE_TO_TEMP_LOC_FAILED = "copy file to temporary file failed";
    /** Error while uploading file message constant. */
    public static final String ERROR_WHILE_UPLOADING_FILE = "Error while uploading file:";
    /** Uploading file failed message constant. */
    public static final String UPLOADING_FILE_FAILED = "upload file failed:";
    /** No input stream for file message constant. */
    public static final String NO_INPUT_STREAM_FOR_FILE = "No input stream for file:";
    /** Either of start or end present message constant. */
    public static final String EITHER_OF_START_OR_END_PRESENT = "starttime and endtime both needs to be sent, if sent";
    /** Should not remaining size message constant. */
    public static final String SHOULD_NOT_REMAINING_SIZE =
            "Please consider that the total size of document should not exceed remaining size of";
    /** Reached max size limit message constant. */
    public static final String REACHED_MAX_SIZE_LIMIT =
            "Already reached max size. Please delete some files then try update";
    /** Reminder date must be greater than current date message constant. */
    public static final String REMINDER_DATE_MUST_BE_GREATER_THAN_CURRENTDATE =
            "Reminder date must be greater than current date";
    /** Service reminder mandatory data message constant. */
    public static final String SERVICE_REMINDER_MANDATORY_DATA =
            "Either reminderDate or reminderOdometerReading is mandatory";
    /** Invalid file message constant. */
    public static final String INVALID_FILE = "invalid file";
    /** Start timestamp must be greater than or equal to zero message constant. */
    public static final String START_TIMESTAMP_MUST_GT_ZERO = "Start timestamp must be >= 0";
    /** End timestamp must be greater than zero message constant. */
    public static final String END_TIMESTAMP_MUST_GT_ZERO = "End timestamp must be > 0";
    /** End must be greater than start message constant. */
    public static final String END_MUST_BE_GT_START = "End timestamp must be >= start timestamp";
    /** Search by ID not supported message constant. */
    public static final String SEARCH_BY_ID_NOT_SUPPORTED = "find by Id method not supported";
    /** Delete not supported message constant. */
    public static final String DELETE_NOT_SUPPORTED = "Delete method not supported";
    /** Unsupported version please use version 2 or above message constant. */
    public static final String UNSUPPORTED_VERSION_PLEASE_USE_VERSION_2_OR_ABOVE =
            "Unsupported API version please use version 2 or above";
    /** Event date mandatory message constant. */
    public static final String EVENT_DATE_MANDATORY = "Event date is mandatory";
    /** Event date wrong value message constant. */
    public static final String EVENT_DATE_WRONG_VALUE = "Event date must be > 0";
    /** Future event date set message constant. */
    public static final String FUTURE_EVENT_DATE_SET = "Cannot set future date for accident date";
    /** Service date wrong value message constant. */
    public static final String SEVICE_DATE_WRONG_VALUE = "Service date must be > 0";
    /** Accident system generated can't edit date message constant. */
    public static final String ACCIDENT_SYS_GENERATED_CANT_EDIT_DATE =
            "Cannot edit the date for system generated accident record";
    /** Accident location latitude and longitude mandatory message constant. */
    public static final String ACCIDENT_LOC_LAT_LONG_MANDATORY = "Both latitude and longitude are mandatory together";
    /** Accident record invalid message constant. */
    public static final String ACCIDENT_RECORD_INVALID = "Invalid accident record Id";
    /** Accident record not found message constant. */
    public static final String ACCIDENT_RECORD_NOTFOUND = "accident record not found";
    /** Accident record ID mandatory message constant. */
    public static final String ACCIDENT_RECORD_ID_MANDATORY = "accident record id parameter is mandatory";
    /** Accident record already exist for event date message constant. */
    public static final String ACCIDENT_RECORD_ALREADY_EXIST_FOR_EVENTDATE =
            "Accident record already exist for given event date and time. Please try to update the same";
    /** Wait till next service message constant. */
    public static final String WAIT_TILL_NEXT_SERVICE =
            "You cannot see the performance improvement details until your next service.";
    /** Mileage improvement not enough distance message constant. */
    public static final String MILEAGE_IMPROVEMENT_NOT_ENOUGH_DISTANCE =
            "Not enough distance travelled. Minimum distance required in KM is : ";
    /** Remaining distance message constant. */
    public static final String REMAINING_DISTANCE = "Remaining distance in KM is  : ";
    /** Invalid parameter value message constant. */
    public static final String INVALID_PARAM_VALUE = "Invalid parameter value : ";
    /** Fuel log one or more mandatory fields are missing message constant. */
    public static final String FUEL_LOG_ONE_OR_MORE_MANDATORY_FIELDS_ARE_MISSING =
            "one or more mandatory request fields are missing";
    /** Received empty shapes message constant. */
    public static final String RECEIVED_EMPTY_SHAPES = "Received empty shapes";
    /** Threshold range validation message constant. */
    public static final String THRESHOLD_RANGE_VALIDATION_MSG =
            "Invalid threshold : value should be between " + Constants.MIN_THRESHOLD
                    + " and " + Constants.MAX_THRESHOLD;
    /** Threshold non-numeric validation message constant. */
    public static final String THRESHOLD_NON_NUMERIC_VALIDATION_MSG = "Null or Non-numeric threshold value";
    /** Fuel type validation message constant. */
    public static final String FUEL_TYPE_VALIDATION_MSG =
            "Invalid Fuel Type sent. Valid values are in the range 0 to 23";
    /** Invalid request payload message constant. */
    public static final String INVALID_REQUEST_PAYLOAD = "Invalid Request Payload";
    /** Fuel level threshold not found message constant. */
    public static final String FUEL_LEVEL_THRSHOLD_NOT_FOUND = "Unable to find fuel level threshold for given deviceId";
    /** Fuel level threshold save error message constant. */
    public static final String FUEL_LEVEL_THRSHOLD_SAVE_ERR = "Unable to save fuel level threshold for given deviceId";
    /** Fuel level threshold delete error message constant. */
    public static final String FUEL_LEVEL_THRSHOLD_DELETE_ERR =
            "Unable to delete fuel level threshold for given deviceId";
    /** Publish fuel level error message constant. */
    public static final String PUBLISH_FUEL_LEVEL_ERR = "Unable to publish fuel level threshold for given deviceId";
    /** Disturbance alert config not found message constant. */
    public static final String DISTURBANCE_ALERT_CONFIG_NOT_FOUND =
            "Unable to find disturbance alert config for given deviceId, deviceId=";
    /** Both params empty message constant. */
    public static final String BOTH_PARAMS_EMPTY = "Both dataparams and fileparams cannot be empty";
    /** Data params empty message constant. */
    public static final String DATA_PARAMS_EMPTY = "value passed for dataparams cannot be empty";
    /** File params empty message constant. */
    public static final String FILE_PARAMS_EMPTY = "value passed for fileparams cannot be empty";
    /** Key data params empty message constant. */
    public static final String KEY_DATA_PARAMS_EMPTY = "key passed for dataparams cannot be empty";
    /** Key file params empty message constant. */
    public static final String KEY_FILE_PARAMS_EMPTY = "key passed for fileparams cannot be empty";
    /** File params with empty files message constant. */
    public static final String FILE_PARAMS_WITH_EMPTY_FILES =
            "With fileparams present, number of files passed cannot be empty";
    /** Mismatch file count file params size message constant. */
    public static final String MISMATCH_FILECOUNT_FILEPARAMSSIZE =
            "Number of files attached do not match with number of fileparams";
    /** No file match message constant. */
    public static final String NO_FILE_MATCH = "No match found in files list for fileparam value :";
    /** Empty get system param keys message constant. */
    public static final String EMPTY_GET_SYSTEMPARAM_KEYS = "systemparamkeys is empty";
    /** Empty key passed message constant. */
    public static final String EMPTY_KEY_PASSED = "One of the system param key passed is empty";
    /** Get system param keys missing in DB message constant. */
    public static final String GET_SYSTEMPARAM_KEYS_MISSING_IN_DB =
            "Please pass proper systemparamkeys .No data found for these systemparamkeys passed :";
    /** No system params found message constant. */
    public static final String NO_SYSTEM_PARAMS_FOUND = "no system parameters found";
    /** Invalid geofence ID message constant. */
    public static final String INVALID_GEOFENCE_ID_MSG = "Invalid geofenceIds provided";
    /** Invalid page size message constant. */
    public static final String INVALID_PAGE_SIZE_MSG = "Received invalid pagination size";
    /** Invalid page number message constant. */
    public static final String INVALID_PAGE_NUMBER_MSG = "Received invalid page number";
    /** Invalid size message constant. */
    public static final String INVALID_SIZE = "Received invalid number of records required per page";
    /** Invalid page or size message constant. */
    public static final String INVALID_PAGE_OR_SIZE_MSG = "Received invalid pagination number or size";
    /** Invalid page message constant. */
    public static final String INVALID_PAGE = "Received invalid page number";
    /** Unsupported API read message constant. */
    public static final String UNSUPPORTED_API_READ_MSG = "Unsupported api readstatus received in URI";
    /** Either of page size null message constant. */
    public static final String EITHER_OF_PAGE_SIZE_NULL = "page and size to be sent together when sent";
    /** Invalid next count message constant. */
    public static final String INVALID_NEXT_COUNT_MSG = "Received invalid next count value";
    /** Since greater than until message constant. */
    public static final String SINCE_GREATER_THAN_UNTIL = "until has to be greater than since";
    /** Unsupported API delete type message constant. */
    public static final String UNSUPPORTED_API_DELETE_TYPE_MSG =
            "Invalid api deletetype received in URI, Possible value: soft";
}