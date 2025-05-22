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
import org.eclipse.ecsp.analytics.stream.base.utils.ObjectUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * This class is used to read the alerts msg from the resource bundle. All the
 * alerts resource bundle files are specified in src/main/resource folder
 */
public class ResourceBundleClient implements NotificationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleClient.class);

    /**
     * id is the Locale. fieldName is key and collectionName would be the
     * basebundle name.
     */
    @Override
    public String getFieldValueWithPrimaryKey(String id, String fieldName, String baseBundleName) {
        ObjectUtils.requireNonEmpty(id, "Locale cannot be null or empty.");
        ObjectUtils.requireNonEmpty(baseBundleName, "Base bundle name cannot be null or empty.");
        ObjectUtils.requireNonEmpty(fieldName, "Field name cannot be null or empty.");

        ResourceBundle bundle = ResourceBundle.getBundle(baseBundleName, new Locale(id));
        String fieldValue = bundle.getString(fieldName);
        if (null != fieldValue) {
            LOGGER.debug("Received value {} for the key {}", fieldValue, fieldName);
        }
        return fieldValue;
    }

    /**
     * Method to initialize the ResourceBundleReader.
     */
    @Override
    public void init(Properties prop) {
        LOGGER.debug("ResourceBundleReader initialized.");

    }

    /**
     * Method to insert multiple documents in a collection.
     */
    @Override
    public void insertMultiDocument(String data, String collectionName)
            throws JsonParseException, JsonMappingException, IOException {
        LOGGER.error("Method insertMultiDocument is not implemented for ResourceBundleReader class.");
    }

    /**
     * Method to insert single document in a collection.
     */
    @Override
    public void insertSingleDocument(Map<String, Object> data, String collectionName) {
        LOGGER.error("Method insertSingleDocument is not implemented for ResourceBundleReader class.");

    }

    /**
     * Method to delete single document in a collection.
     */
    @Override
    public void deleteSingleDocument(String id, String collectionName) {
        LOGGER.error("Method deleteSingleDocument is not implemented for ResourceBundleReader class.");

    }

    /**
     * Method to fetch specific field data based on filter on field name and field
     * value.
     */
    @Override
    public List<String> getFieldValuesByField(String fieldName, String fieldValue, String collectionName,
                                              String fieldNameValuesToBeFetched) {
        LOGGER.error("Method getFieldValuesByField is not implemented for ResourceBundleReader class.");
        return new ArrayList<>();
    }

    /**
     * Method to get id field.
     *
     * @return id field
     */
    @Override
    public String getIdField() {
        LOGGER.error("ResourceBundleClient doesn't support id field. Returning null.");
        return null;
    }

    /**
     * Method to insert alert in alerts history collection.
     *
     * @param alert    AlertsHistoryInfo
     * @param tableName String
     */
    @Override
    public void insertAlert(AlertsHistoryInfo alert, String tableName) {
        LOGGER.error("ResourceBundleClient doesn't support insertAlert method.");
    }

}
