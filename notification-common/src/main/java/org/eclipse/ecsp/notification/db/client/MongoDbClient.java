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
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import dev.morphia.Datastore;
import org.bson.Document;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * MongoDbClient class.
 */
@Repository
public class MongoDbClient implements NotificationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbClient.class);
    private static final String ID = "_id";
    private String collectionName;

    private Properties mongoProps;
    private Datastore datastore;

    private String getCollectionName() {
        return collectionName;
    }

    private void setCollection(String collection) {
        this.collectionName = collection;
    }

    /**
     * Exposing this method for testing.
     *
     * @return MongoDatabase.
     */
    public MongoDatabase getDataBaseInstance() {
        return getMongoDb();
    }

    /**
     * Get the MongoDb instance.
     *
     * @return MongoDatabase
     */
    private MongoDatabase getMongoDb() {
        return datastore.getDatabase();
    }

    /**
     * Method to initialize the properties.
     *
     * @param props Properties
     */
    @Override
    public void init(Properties props) {
        this.mongoProps = props;

        verify();
    }

    /**
     * Method to verify the properties.
     */
    private void verify() {
        setCollection(mongoProps.getProperty(NotificationProperty.RESOURCE_BUNDLE_COLLECTION_NAME));
    }

    /**
     * Get the template msg of an alertType for a given locale.
     *
     * @param id String
     * @param fieldName String
     * @return String
     */
    public String getRecord(String id, String fieldName) {

        StringBuilder fieldValue = new StringBuilder();
        BasicDBObject whereQuery = new BasicDBObject(ID, id);
        BasicDBObject field = new BasicDBObject(fieldName, 1);

        FindIterable<Document> cursor =
                getMongoDb().getCollection(getCollectionName()).find(whereQuery).projection(field)
                        .projection(Projections.excludeId());

        cursor.forEach((Consumer<Document>) document -> fieldValue.append(document.get(fieldName)));
        return fieldValue.toString();
    }

    /**
     * Get the template msg of an alertType for a given locale for a given
     * collection name.
     *
     * @param id String
     * @param alertType String
     * @return String
     */
    public String getRecordsByCollections(String id, String alertType, String collectionName) {

        if (null != collectionName && !collectionName.isEmpty()) {
            StringBuilder jsonRecord = new StringBuilder();
            BasicDBObject whereQuery = new BasicDBObject(ID, id);
            BasicDBObject field = new BasicDBObject(alertType, 1);

            FindIterable<Document> cursor =
                    getMongoDb().getCollection(collectionName).find(whereQuery).projection(field)
                            .projection(Projections.excludeId());

            cursor.forEach((Consumer<Document>) document -> jsonRecord.append(document.get(alertType)));
            return jsonRecord.toString();
        } else {
            return getRecord(id, alertType);
        }

    }

    /**
     * Method only for testing.
     *
     * @param jsonData String
     */
    public void insertOneDocument(String jsonData) {

        Map<String, Object> object = JsonUtils.getJsonAsMap(jsonData);
        Document doc = new Document(object);
        getMongoDb().getCollection(getCollectionName()).insertOne(doc);
    }

    /**
     * Method only for testing.
     *
     * @param data Map
     * @param collectionName String
     */
    @Override
    public void insertSingleDocument(Map<String, Object> data, String collectionName) {
        Document doc = new Document(data);
        getMongoDb().getCollection(collectionName).insertOne(doc);
    }

    /**
     * Method only for testing.
     *
     * @param jsonData String
     * @param collectionName String
     * @throws JsonParseException Exception class.
     * @throws JsonMappingException Exception class.
     * @throws IOException Exception class.
     */
    @Override
    public void insertMultiDocument(String jsonData, String collectionName) throws IOException {
        @SuppressWarnings("rawtypes")
        List<Map> list = JsonUtils.getListObjects(jsonData, Map.class);
        for (@SuppressWarnings("rawtypes")
            Map obj : list) {
            @SuppressWarnings("unchecked")
            Document doc = new Document(obj);
            getMongoDb().getCollection(collectionName).insertOne(doc);

        }
    }

    /**
     * Method to get the field value with primary key.
     *
     * @param id String
     * @param fieldName String
     * @param collectionName String
     * @return String
     */
    @Override
    public String getFieldValueWithPrimaryKey(String id, String fieldName, String collectionName) {

        if (null != collectionName && !collectionName.isEmpty()) {
            StringBuilder jsonRecord = new StringBuilder();
            BasicDBObject whereQuery = new BasicDBObject(ID, id);
            BasicDBObject field = new BasicDBObject(fieldName, 1);

            FindIterable<Document> cursor =
                    getMongoDb().getCollection(collectionName).find(whereQuery).projection(field)
                            .projection(Projections.excludeId());

            cursor.forEach((Consumer<Document>) document -> jsonRecord.append(document.get(fieldName)));
            return jsonRecord.toString();
        } else {
            return getRecord(id, fieldName);
        }

    }

    /**
     * Delete the document based on the ID.
     *
     * @param id String
     * @param collectionName String
     */
    @Override
    public void deleteSingleDocument(String id, String collectionName) {

        if (null != collectionName && !collectionName.isEmpty()) {
            DeleteResult result = getMongoDb().getCollection(collectionName).deleteOne(new Document(ID, id));
            LOGGER.info("Number of records deleted from Mongo:{}", result.getDeletedCount());
        }

    }

    /**
     * Get the document based on filter criteria from a collection name.
     *
     * @param fieldName String
     * @param fieldValue String
     * @param collectionName String
     * @param fieldNameValuesToBeFetched String
     * @return List of String
     */
    @Override
    public List<String> getFieldValuesByField(String fieldName, String fieldValue, String collectionName,
                                              String fieldNameValuesToBeFetched) {
        List<Document> documents = null;
        List<String> fieldValues = new ArrayList<>();

        if (null != collectionName && !collectionName.isEmpty()) {
            BasicDBObject whereQuery = new BasicDBObject(fieldName, fieldValue);
            documents = getMongoDb().getCollection(collectionName).find(whereQuery).into(new ArrayList<Document>());
            documents.forEach(document -> fieldValues.add(document.getString(fieldNameValuesToBeFetched)));
        }
        return fieldValues;
    }

    /**
     * Get the document based on filter criteria from a collection name.
     *
     * @param constraintNameValueMap Map
     * @param collectionName String
     * @param fieldNameValuesToBeFetched Map
     * @return List of Map
     */
    @Override
    public List<Map<String, Object>> getFieldsValueByFields(Map<String, Object> constraintNameValueMap,
                                                            String collectionName,
                                                            Map<String, Object> fieldNameValuesToBeFetched) {
        List<Document> documents = null;
        if (null != collectionName && !collectionName.isEmpty()) {
            BasicDBObject whereQuery = getBasicDbObject(constraintNameValueMap);
            BasicDBObject projectionFields = getBasicDbObject(fieldNameValuesToBeFetched);
            documents = getMongoDb().getCollection(collectionName).find(whereQuery).projection(projectionFields)
                    .into(new ArrayList<Document>());
        }
        if (documents != null) {
            List<Map<String, Object>> documentList = new ArrayList<>();
            for (Document doc : documents) {
                documentList.add(doc);
            }
            return documentList;
        }
        return Collections.emptyList();

    }

    /**
     * Get the document based on filter criteria from a collection name.
     *
     * @return String
     */
    @Override
    public String getIdField() {
        return NotificationProperty.PRIMARY_KEY_NAME;
    }

    /**
     * Insert the alert into the collection.
     *
     * @param alert AlertsHistoryInfo
     * @param tableName String
     */
    @Override
    public void insertAlert(AlertsHistoryInfo alert, String tableName) {
        Map<String, Object> alertsMap = JsonUtils.getObjectAsMap(alert);
        insertSingleDocument(alertsMap, tableName);
    }

    /**
     * Method to get the BasicDBObject.
     *
     * @param map Map
     * @return BasicDBObject
     */
    private BasicDBObject getBasicDbObject(Map<String, Object> map) {
        BasicDBObject basicDbObject = new BasicDBObject();
        if (null != map) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                basicDbObject.append(entry.getKey(), entry.getValue());
            }
        }
        return basicDbObject;
    }

    /**
     * Method to set the datastore.
     *
     * @param datastore Datastore
     */
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }
}
