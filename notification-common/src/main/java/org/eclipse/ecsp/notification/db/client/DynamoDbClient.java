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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.ObjectUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.utils.AwsUtils;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.notification.fcm.PropertyNamesForFcm;
import org.eclipse.ecsp.notification.utils.DynamoMapperRepository;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/**
 * This class is used to read the alerts msg from the resource bundle. All the
 * alerts resource bundle files are specified in src/main/resource folder
 */
public class DynamoDbClient implements NotificationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbClient.class);

    private static final int FIVE = 5;
    private static final String ID = "id";
    /**
     * Table name cannot be null or empty.
     */
    public static final String TABLE_NAME_CANNOT_BE_NULL_OR_EMPTY = "Table name cannot be null or empty.";

    private DynamoDB dynDb;

    private static DynamoMapperRepository mapperRepo;

    private AmazonDynamoDB dynDbClient;

    /**
     * Method to get the field value with primary key.
     *
     * @param primaryKeyValue primary key value
     * @param fieldName       field name
     * @param tableName       table name
     * @return field value
     */
    @Override
    public String getFieldValueWithPrimaryKey(String primaryKeyValue, String fieldName, String tableName) {

        ObjectUtils.requireNonEmpty(tableName, TABLE_NAME_CANNOT_BE_NULL_OR_EMPTY);
        LOGGER.debug("Getting the value of the field:{} from table {}", fieldName, tableName);
        String fieldValue = null;
        Table table = dynDb.getTable(tableName);

        GetItemSpec spec = new GetItemSpec().withPrimaryKey(ID, primaryKeyValue);

        try {
            LOGGER.debug("Attempting to get the item");
            Item outcome = table.getItem(spec);

            if (null != outcome) {
                fieldValue = (String) outcome.get(fieldName);
            } else {
                LOGGER.error("Primary key value {} is not present in the table {}", primaryKeyValue, tableName);
            }

            LOGGER.debug("Value for the field :{} is {}", fieldName, fieldValue);
        } catch (Exception e) {
            LOGGER.error("Unable to get the item for the primary key {} from table {}", primaryKeyValue, tableName);
            LOGGER.error("Error msg while getting item:", e);
        } /*
         * set primary key and attributeName to null so that next queries
         * shouldn't use the previous values.
         */
        return fieldValue;

    }

    /**
     * Method to initialize the dynamo db client.
     *
     * @param props properties
     */
    @Override
    public void init(Properties props) {

        boolean isRunningOnLambda =
            Boolean.parseBoolean(props.getProperty(NotificationProperty.AWS_LAMBDA_EXECUTION_PROP, "false"));
        String signingRegion = props.getProperty("dynamodb.region");
        ObjectUtils.requireNonEmpty(signingRegion, "DynamoDB region shouldn't be null or empty");
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();

        if (isRunningOnLambda) {
            LOGGER.info("App is running on aws lambda. For configuring Dynamo only region is required.");
            ClientConfiguration config = new ClientConfiguration();
            int retry = Integer.parseInt(props.getOrDefault("dynamo.retry.count", FIVE).toString());
            config.setRetryPolicy(new RetryPolicy(null, null, retry, false));
            builder.withRegion(signingRegion).withClientConfiguration(config);
        } else {
            LOGGER.info("App is not running on aws lambda");
            String serviceEndpoint = props.getProperty("dynamodb.service.endpoint");
            ObjectUtils.requireNonEmpty(serviceEndpoint, "DynamoDB service endpoint shoudln't be null or empty");
            /*
             * creating dynamodb client for creating tables
             */
            AWSCredentialsProviderChain credProviderChain = AwsUtils.getCredentialProvider(props);
            builder.withCredentials(credProviderChain);

            EndpointConfiguration endPointConfiguration = new EndpointConfiguration(serviceEndpoint, signingRegion);
            builder.withEndpointConfiguration(endPointConfiguration);
        }
        dynDbClient = builder.build();
        DynamoMapperRepository mapRep = new DynamoMapperRepository(dynDbClient);
        setMapperRepo(mapRep);
        dynDb = new DynamoDB(dynDbClient);

        ObjectUtils.requireNonNull(dynDb, "Unable to create dynamo db client.");
        LOGGER.info("Successfully initialized the dynamo db client");
    }


    /**
     * Method to insert multiple documents.
     *
     * @param data     data
     * @param tableName table name
     */
    @Override
    public void insertMultiDocument(String data, String tableName) {
        LOGGER.error("Method insertMultiDocument is not implemented for DynamoDbClient class.");
    }

    /**
     * Method to insert alert.
     *
     * @param alertInfo alert info
     * @param tableName table name
     */
    @Override
    public void insertAlert(AlertsHistoryInfo alertInfo, String tableName) {
        ObjectUtils.requireNonEmpty(tableName, TABLE_NAME_CANNOT_BE_NULL_OR_EMPTY);
        try {
            String idValue = alertInfo.getId();
            if (StringUtils.isBlank(idValue)) {
                idValue = UUID.randomUUID().toString();
                alertInfo.setId(idValue);
            }
            mapperRepo.getMapper(tableName, dynDbClient).save(alertInfo);
            LOGGER.debug("Successfully put the record {} into table {}", alertInfo, tableName);
        } catch (Exception e) {
            LOGGER.error("Unable to put the item: {} into table: {} ", alertInfo, tableName);
            LOGGER.error("Error msg while putting item:", e);
        }
    }

    /**
     * Method to set the mapper repo.
     *
     * @param mapRep map rep
     */
    private static void setMapperRepo(DynamoMapperRepository mapRep) {
        mapperRepo = mapRep;
    }

    /**
     * Insert single document.
     *
     * @param data     data
     * @param tableName table name
     */
    @Override
    public void insertSingleDocument(Map<String, Object> data, String tableName) {
        ObjectUtils.requireNonEmpty(tableName, TABLE_NAME_CANNOT_BE_NULL_OR_EMPTY);
        LOGGER.debug("Putting record {} into table {}", data, tableName);
        Table table = dynDb.getTable(tableName);

        Optional<PrimaryKey> pk = getPrimaryKeyComponents(data, tableName);
        if (!pk.isPresent()) {
            throw new IllegalArgumentException(
                "Unable to form primary key element for the table:" + tableName + " with the data:" + data);
        }
        Item item = new Item().withPrimaryKey(pk.get());

        for (Map.Entry<String, Object> dataPoints : data.entrySet()) {

            String keyName = dataPoints.getKey();
            Object keyValue = dataPoints.getValue();
            LOGGER.debug("Adding keyName:{} and keyValue:{} to the dynamo db item", keyName, keyValue);
            item.with(keyName, keyValue);
        }
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Attempting to put the record. item to be inserted is {} ", item.toJSON());
            }
            table.putItem(item);
            LOGGER.debug("Successfully put the record {} into table {}", data, tableName);
        } catch (Exception e) {
            LOGGER.error("Unable to put the item {}  into table {}", item, tableName);
            LOGGER.error("Error msg while putting item:", e);
        }
    }

    /**
     * Method to delete single document.
     *
     * @param primaryKeyValue primary key value
     * @param tableName       table name
     */
    @Override
    public void deleteSingleDocument(String primaryKeyValue, String tableName) {
        ObjectUtils.requireNonEmpty(tableName, TABLE_NAME_CANNOT_BE_NULL_OR_EMPTY);
        LOGGER.debug("Deleting the record with primary key value:{} from table {}", primaryKeyValue, tableName);
        Table table = dynDb.getTable(tableName);
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(ID, primaryKeyValue);

        try {
            LOGGER.debug("Attempting to delete the item");
            table.deleteItem(deleteItemSpec);
            LOGGER.debug("Successfully deleted the record with primary key {} from the table {}", primaryKeyValue,
                tableName);
        } catch (Exception e) {
            LOGGER.error("Unable to delete item with primary key:{} from table {}", primaryKeyValue, tableName);
            LOGGER.error("Error msg while deleting item:", e);
        }
    }

    /**
     * Method to get the field value by field.
     *
     * @param fieldName             field name
     * @param fieldValue            field value
     * @param tableName             table name
     * @param fieldValuesToBeFetched field values to be fetched
     * @return list of string
     */
    @Override
    public List<String> getFieldValuesByField(String fieldName, String fieldValue, String tableName,
                                              String fieldValuesToBeFetched) {
        ObjectUtils.requireNonEmpty(tableName, TABLE_NAME_CANNOT_BE_NULL_OR_EMPTY);

        Table table = dynDb.getTable(tableName);
        Index index = table.getIndex(PropertyNamesForFcm.DYNAMO_NOTIFICATION_GSI_USERID);
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put(PropertyNamesForFcm.HASH + fieldName, fieldName);
        List<String> fieldValues = new ArrayList<>();
        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put(NotificationUtils.COLON + fieldName, fieldValue);
        QuerySpec querySpec = new QuerySpec()
            .withKeyConditionExpression(
                PropertyNamesForFcm.HASH + fieldName + PropertyNamesForFcm.EQUAL_TO + NotificationUtils.COLON
                        +
                    fieldName)
            .withNameMap(nameMap).withValueMap(valueMap);
        ItemCollection<QueryOutcome> items;
        Iterator<Item> iterator;
        Item item;

        try {
            items = index.query(querySpec);
            iterator = items.iterator();
            while (iterator.hasNext()) {
                item = iterator.next();
                fieldValues.add(item.getString(PropertyNamesForFcm.ID));
            }
        } catch (Exception e) {
            LOGGER.error("Unable to get the items for key {} from table {}", fieldValue, tableName);
            LOGGER.error("Error msg while getting item:", e);
        }
        return fieldValues;
    }

    /**
     * Method to get field Id.
     *
     * @return String
     */
    @Override
    public String getIdField() {
        return ID;
    }

    /**
     * Helper method that forms the primary key component for an Item to be
     * inserted.

     * First get the keyschema elements of the table which will give you the
     * list.

     * Iterate over the list of key schema element and for each key schema
     * element you get both the attribute name and key type. Based on the
     * attribute name get the value from the data. Once you have the attribute
     * name and the corresponding value create KeyAttribute object and add to
     * the primary key object.

     * if the attribute is "id", its value will not be present in data, we need
     * to generate UUID for the id attribute.
     *
     * @param data      data
     * @param tableName table name
     * @return Optional
     */
    @SuppressWarnings("null")
    private Optional<PrimaryKey> getPrimaryKeyComponents(Map<String, Object> data, String tableName) {

        LOGGER.debug("Getting table description of table:{}", tableName);

        TableDescription tableDescription = dynDb.getTable(tableName).describe();
        List<KeySchemaElement> keySchemaElements = tableDescription.getKeySchema();

        if (CollectionUtils.isEmpty(keySchemaElements)) {
            throw new IllegalArgumentException("Table " + tableName + " doesn't have any schema elements.");
        }

        PrimaryKey primaryKey = new PrimaryKey();

        for (KeySchemaElement schemaElement : keySchemaElements) {
            String attributeName = schemaElement.getAttributeName();
            String keyType = schemaElement.getKeyType();
            // get the value of the attribute name
            Object value = data.get(attributeName);
            /*
             * value can be null in case of "id" attribute. we need to generate
             * UUID for id attribute
             */

            if (null == value) {
                LOGGER.debug("Received null value for the attribute name:{}", attributeName);
                if (attributeName.equals(ID)) {
                    LOGGER.debug("Received id as primary key element, generating UUID.");
                    value = UUID.randomUUID().toString();
                } else {
                    throw new IllegalArgumentException(
                        "Null value received for the primary key element: " + attributeName);
                }
            }
            LOGGER.debug("Attribute name:{} and KeyType:{} and the value of the attribute in map is:{}", attributeName,
                keyType, value);

            KeyAttribute attribute = new KeyAttribute(attributeName, value);
            /*
             * Let's remove this attribute name from the map
             */
            data.remove(attributeName);
            primaryKey.addComponents(attribute);
        }
        LOGGER.debug("Primary key component generated:{}", primaryKey);
        return Optional.of(primaryKey);
    }
}
