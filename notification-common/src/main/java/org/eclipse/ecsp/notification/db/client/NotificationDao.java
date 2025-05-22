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

package org.eclipse.ecsp.notification.db.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * NotificationDao interface.
 */
public interface NotificationDao {

    /**
     * Returns value for a given field for a given collection.
     *
     * @param id             String
     * @param fieldName      String
     * @param collectionName String
     * @return String
     */
    public String getFieldValueWithPrimaryKey(String id, String fieldName, String collectionName);

    /**
     * Init Method.
     *
     * @param prop Properties
     */
    public void init(Properties prop);

    /**
     * Method to insert multiple documents in a collection.
     *
     * @param data           String
     * @param collectionName String
     * @throws JsonParseException   JsonParseException
     * @throws JsonMappingException JsonMappingException
     * @throws IOException          IOException
     */
    public void insertMultiDocument(String data, String collectionName)
            throws JsonParseException, JsonMappingException, IOException;

    /**
     * Method to insert single document in a collection.
     *
     * @param data          Map of String and Object
     * @param collectionName String
     */
    public void insertSingleDocument(Map<String, Object> data, String collectionName);

    /**
     * Method to delete single document in a collection.
     *
     * @param id String
     * @param collectionName String
     */
    public void deleteSingleDocument(String id, String collectionName);

    /**
     * Method to insert alert in alerts history collection.
     *
     * @param alert    AlertsHistoryInfo
     * @param tableName String
     */
    public void insertAlert(AlertsHistoryInfo alert, String tableName);

    /**
     * Fetch specific field data based on filter on field name and field value.
     *
     * @param fieldName String
     * @param fieldValue String
     * @param collectionName String
     * @param fieldNameValuesToBeFetched String
     * @return List of String
     */
    public List<String> getFieldValuesByField(String fieldName, String fieldValue, String collectionName,
                                              String fieldNameValuesToBeFetched);

    /**
     * Fetch specific field data based on filter on field name and field value.
     *
     * @param constraint Map of String and Object
     * @param collectionName String
     * @param fieldsMap Map of String and Object
     * @return List of Map of String and Object
     */
    public default  List<Map<String, Object>> getFieldsValueByFields(Map<String, Object> constraint,
                                                                    String collectionName,
                                                                    Map<String, Object> fieldsMap) {
        return new ArrayList<>();
    }

    /**
     * To return the id field name. In mongo it is _id and in dynamo it is id,
     * as in dynamo filter query doesnt support columns with underscore.
     *
     * @return String
     */
    public String getIdField();

}
