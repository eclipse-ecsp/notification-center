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

package org.eclipse.ecsp.platform.notification.v1.common;

/**
 * Constants used in API module.
 *
 * @author Neerajkumar
 */
public class Constants {

    private Constants()
      {}

    /**
     * The Constant COMMA.
     */
    public static final String COMMA = ",";
    /**
     * The Constant SINGLE_QUOTE.
     */
    public static final String SINGLE_QUOTE = "'";
    /**
     * The Constant UNDER_SCORE.
     */
    public static final String UNDER_SCORE = "_";
    /**
     * The Constant DTC_CODE.
     */
    public static final String DTC_CODE = "code";
    /**
     * The Constant DTC_DESCRIPTION.
     */
    public static final String DTC_DESCRIPTION = "description";
    /**
     * The Constant DTC_CATEGORY.
     */
    public static final String DTC_CATEGORY = "category";
    /**
     * The Constant DTC_SUB_CATEGORY.
     */
    public static final String DTC_SUB_CATEGORY = "subcategory";
    /**
     * The Constant DTC_SUGGESTIONS.
     */
    public static final String DTC_SUGGESTIONS = "suggestions";
    /**
     * The Constant NOT_APPLICABLE.
     */
    public static final String NOT_APPLICABLE = "NA";
    /**
     * The Constant HOST_SEPARATOR.
     */
    public static final String HOST_SEPARATOR = ",";
    /**
     * The Constant FIELD_SEPARATOR.
     */
    public static final String FIELD_SEPARATOR = ":";
    /**
     * The Constant LAMBDA.
     */
    public static final String LAMBDA = "lambda";
    /**
     * The Constant TEST_CASE_EXECUTION_PREFIX.
     */
    public static final String TEST_CASE_EXECUTION_PREFIX = "test-";
    /**
     * The Constant SAN_PREFIX.
     */
    public static final String SAN_PREFIX = "haa-harman";
    /**
     * The Constant API_EXECUTION_ENV_PROP.
     */
    public static final String API_EXECUTION_ENV_PROP = "API_EXECUTION_ENV";
    /**
     * The Constant DYNAMO.
     */
    public static final String DYNAMO = "dynamo";
    /**
     * The Constant REPOSITORYSELECTOR.
     */
    public static final String REPOSITORYSELECTOR = "API_REPOSITORY_SELECTOR";
    /**
     * The Constant AWAITED_STATUS.
     */
    public static final String AWAITED_STATUS = "awaited";
    /**
     * The Constant DOT.
     */
    public static final String DOT = ".";
    /**
     * The Constant THUMBNAIL_POSTFIX.
     */
    public static final String THUMBNAIL_POSTFIX = "thumbnail";

    public static final String USER_ID = "user-id";
    /**
     * URL Separator Constant.
     */
    public static final String URL_SEPARATOR = "/";

    /**
     * ASSOCIATED constant.
     */
    public static final String ASSOCIATED = "ASSOCIATED";

    /**
     * Pdid Constant.
     */
    public static final String PDID = "pdid";

    /**
     * Auth Header Key Constant.
     */
    public static final String AUTH_HEADER_KEY = "Authorization";

    /**
     * APPLICATION_JSON Constant.
     */
    public static final String APPLICATION_JSON = "application/json";

    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    /**
     * CONTENT TYPE constant.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * BLANK Constant.
     */
    public static final String SPACE = " ";

    /**
     * threshold Constant.
     */
    public static final String THRESHOLD = "threshold";

    /**
     * put Constant.
     */
    public static final String PUT = "put";

    /**
     * delete Constant.
     */
    public static final String DELETE = "delete";

    /**
     * PowerPsAtRpm Constant.
     */
    public static final String POWER_PS_AT_RPM = "PowerPsAtRpm";

    /**
     * DisplacementCC Constant.
     */
    public static final String DISPLACEMENT_CC = "DisplacementCC";

    /**
     * Fuel Type Constant.
     */
    public static final String FUEL_TYPE = "FuelType";

    public static final String SERVICE_MAINTENANCE_ID = "ServiceMaintenanceId";

    /**
     * min threshold Constant.
     */
    public static final int MIN_THRESHOLD = 0;

    /**
     * max threshold Constant.
     */
    public static final int MAX_THRESHOLD = 100;

    public static final String ACTIVE = "active";

    public static final String USERNAME = "username";

    public static final int AUTH_HEADER_NUM_SPLITS = 2;

    /**
     * TRIP SUMMARY SUFFIX Constant.
     */
    public static final String TRIP_SUMMARY_SUFFIX = "TRIPSUMMARY_2";

    public static final String TRIP_COMPLETED_STATUS_CODE = "42";

    /**
     * LAT LONGS Constant.
     */
    public static final String LAT_LONGS = "latLongs";

    public static final String VEHICLE_SUMMARY_SUFFIX = "VEHICLESUMMARY_1";

    // alert payload params
    public static final String VEHICLE_PROFILE = "vehicleProfile";
    public static final String DOMAIN = "domain";
    public static final String DATA = "data";

    /**
     * TRIP_ID constant.
     */
    public static final String TRIP_ID = "tripId";
    public static final String ID = "id";

    public static final String MQTT_SERVICE_SELECTOR = "MQTT_SERVICE_SELECTOR";

    public static final String TEST = "test";

    public static final String SPRING_MULTIPART_ERROR = "the request was rejected because its size";

    public static final String DEVICES = "devices";
    public static final String CONFIG = "config";

    /**
     * MONGO DB USERNAME KEY Constant.
     */
    public static final String VAULT_MONGO_USERNAME_KEY = "username";

    /**
     * MONGO DB PASS KEY Constant.
     */
    public static final String VAULT_MONGO_PASS_KEY = "password";

    /**
     * MONGO DB LEASE DURATION Constant.
     */
    public static final String VAULT_MONGO_LEASE_DURATION = "lease_duration";

    /**
     * KAFKA CLIENT KEYSTORE PASSWORD Constant.
     */
    public static final String KAFKA_CLIENT_KEYSTORE_PASS_KEY = "kafka_client_keystore_password";

    /**
     * KAFKA CLIENT KEY PASSWORD Constant.
     */
    public static final String KAFKA_CLIENT_KEY_PASS_KEY = "kafka_client_key_password";

    /**
     * KAFKA CLIENT TRUSTSTORE PASSWORD Constant.
     */
    public static final String KAFKA_CLIENT_TRUSTSTORE_PASS_KEY = "kafka_client_truststore_password";

    /**
     * VERSION 1 Constant.
     */
    public static final String VERSION_1 = "v1";

    /**
     * VERSION 2 Constant.
     */
    public static final String VERSION_2 = "v2";
    /**
     * VERSION 3 Constant.
     */
    public static final String VERSION_3 = "v3";

    /**
     * IVM eventname.
     */
    public static final String IVM_RESPONSE = "IVMResponse";
    /**
     * IVM ACK.
     */
    public static final String IVM_ACK = "IVMAck";

}
