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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.notification.utils.DynamoMapperRepository;
import org.eclipse.ecsp.notification.utils.MemoryAppender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * DynamoDbClientTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class DynamoDbClientTest {

    public static final String ORDERS = "orders";
    @InjectMocks
    DynamoDbClient dynamoDbClient;

    @Mock
    DynamoDB dynamoDb;

    private MemoryAppender memoryAppender;

    @Captor
    private ArgumentCaptor<AlertsHistoryInfo> alertsHistoryInfoArgumentCaptor;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    /**
     * set up.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Logger logger = (Logger) LoggerFactory.getLogger(DynamoDbClient.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    public void getFieldValueWithPrimaryKeySuccess() {

        Table tableMock = mock(Table.class);
        Item item = new Item().with("id", "12345");

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(item).when(tableMock).getItem(any(GetItemSpec.class));
        String result = dynamoDbClient.getFieldValueWithPrimaryKey("12345", "id", ORDERS);
        assertEquals("12345", result);
    }

    @Test
    public void getFieldValueWithPrimaryKeyNull() {

        Table tableMock = mock(Table.class);

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(null).when(tableMock).getItem(any(GetItemSpec.class));
        String result = dynamoDbClient.getFieldValueWithPrimaryKey("12345", "id", ORDERS);
        assertNull(result);
    }

    @Test
    public void getFieldValueWithPrimaryKeyEmptyParam() {

        RuntimeException thrown =
            assertThrows(RuntimeException.class,
                () -> dynamoDbClient.getFieldValueWithPrimaryKey("12345", "id", ""));
        assertEquals("Table name cannot be null or empty.", thrown.getMessage());
    }

    @Test
    public void getFieldValueWithPrimaryKeyFailure() {
        memoryAppender.reset();
        Table tableMock = mock(Table.class);

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doThrow(new RuntimeException("failure")).when(tableMock).getItem(any(GetItemSpec.class));
        String result = dynamoDbClient.getFieldValueWithPrimaryKey("12345", "id", ORDERS);
        assertNull(result);
        assertEquals(1, memoryAppender.search("Error msg while getting item:", Level.ERROR).size());
    }

    @Test
    public void insertMultiDocument() {
        memoryAppender.reset();
        dynamoDbClient.insertMultiDocument("data", ORDERS);
        assertEquals(1, memoryAppender.search("Method insertMultiDocument is not implemented for DynamoDbClient class.",
            Level.ERROR).size());
    }

    @Test
    public void insertAlertEmptyParam() {

        RuntimeException thrown =
            assertThrows(RuntimeException.class,
                () -> dynamoDbClient.insertAlert(null, ""));
        assertEquals("Table name cannot be null or empty.", thrown.getMessage());
    }

    @Test
    public void insertAlertSuccess() {
        memoryAppender.reset();

        DynamoMapperRepository mapperRepo = mock(DynamoMapperRepository.class);

        DynamoDBMapper dynamoDbMapper = mock(DynamoDBMapper.class);
        doReturn(dynamoDbMapper).when(mapperRepo).getMapper(any(), any());
        doNothing().when(dynamoDbMapper).save(alertsHistoryInfoArgumentCaptor.capture());
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();
        alertsHistoryInfo.setId("12345");

        ReflectionTestUtils.setField(dynamoDbClient, "mapperRepo", mapperRepo);

        dynamoDbClient.insertAlert(alertsHistoryInfo, ORDERS);
        verify(dynamoDbMapper, times(1)).save(any());
        assertEquals(1, memoryAppender.search("Successfully put the record ", Level.DEBUG).size());
        assertEquals("12345", alertsHistoryInfoArgumentCaptor.getValue().getId());
    }


    @Test
    public void insertAlertNoId() {
        memoryAppender.reset();

        DynamoMapperRepository mapperRepo = mock(DynamoMapperRepository.class);

        DynamoDBMapper dynamoDbMapper = mock(DynamoDBMapper.class);
        doReturn(dynamoDbMapper).when(mapperRepo).getMapper(any(), any());
        doNothing().when(dynamoDbMapper).save(alertsHistoryInfoArgumentCaptor.capture());
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        ReflectionTestUtils.setField(dynamoDbClient, "mapperRepo", mapperRepo);

        dynamoDbClient.insertAlert(alertsHistoryInfo, ORDERS);
        verify(dynamoDbMapper, times(1)).save(any());
        assertEquals(1, memoryAppender.search("Successfully put the record ", Level.DEBUG).size());
        assertNotNull(alertsHistoryInfoArgumentCaptor.getValue().getId());
    }

    @Test
    public void insertAlertFailure() {
        memoryAppender.reset();

        DynamoMapperRepository mapperRepo = mock(DynamoMapperRepository.class);

        DynamoDBMapper dynamoDbMapper = mock(DynamoDBMapper.class);
        doReturn(dynamoDbMapper).when(mapperRepo).getMapper(any(), any());
        doThrow(new RuntimeException("save error")).when(dynamoDbMapper).save(any());
        AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

        ReflectionTestUtils.setField(dynamoDbClient, "mapperRepo", mapperRepo);

        dynamoDbClient.insertAlert(alertsHistoryInfo, ORDERS);
        assertEquals(1, memoryAppender.search("Error msg while putting item", Level.ERROR).size());
    }

    @Test
    public void insertSingleDocumentEmptyParam() {

        RuntimeException thrown =
            assertThrows(RuntimeException.class,
                () -> dynamoDbClient.insertSingleDocument(new HashMap<>(), ""));
        assertEquals("Table name cannot be null or empty.", thrown.getMessage());
    }

    @Test
    public void insertSingleDocumentSuccess() {
        memoryAppender.reset();
        Table tableMock = mock(Table.class);

        List<KeySchemaElement> keySchemaElements = new ArrayList<>();
        KeySchemaElement keySchemaElement = new KeySchemaElement("id", "String");
        keySchemaElements.add(keySchemaElement);
        TableDescription tableDescription = new TableDescription().withKeySchema(keySchemaElements);

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(tableDescription).when(tableMock).describe();
        doReturn(null).when(tableMock).putItem(any(Item.class));
        Map<String, Object> data = new HashMap<>();
        data.put("id", "00001");
        data.put("amount", 1);
        data.put("price", 5.00);
        dynamoDbClient.insertSingleDocument(data, ORDERS);
        verify(tableMock, times(1)).putItem(any(Item.class));
        assertEquals(1, memoryAppender.search("Successfully put the record ", Level.DEBUG).size());
    }

    @Test
    public void insertSingleDocumentWithoutId() {
        memoryAppender.reset();
        Table tableMock = mock(Table.class);

        List<KeySchemaElement> keySchemaElements = new ArrayList<>();
        KeySchemaElement keySchemaElement = new KeySchemaElement("id", "String");
        keySchemaElements.add(keySchemaElement);
        TableDescription tableDescription = new TableDescription().withKeySchema(keySchemaElements);

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(tableDescription).when(tableMock).describe();
        doReturn(null).when(tableMock).putItem(any(Item.class));
        Map<String, Object> data = new HashMap<>();
        data.put("amount", 1);
        data.put("price", 5.00);
        dynamoDbClient.insertSingleDocument(data, ORDERS);
        verify(tableMock, times(1)).putItem(itemArgumentCaptor.capture());
        assertEquals(1, memoryAppender.search("Successfully put the record ", Level.DEBUG).size());
        assertNotNull(itemArgumentCaptor.getValue().get("id"));
    }

    @Test
    public void insertSingleDocumentEmptyScheme() {

        Table tableMock = mock(Table.class);

        List<KeySchemaElement> keySchemaElements = new ArrayList<>();
        TableDescription tableDescription = new TableDescription().withKeySchema(keySchemaElements);

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(tableDescription).when(tableMock).describe();
        doReturn(null).when(tableMock).putItem(any(Item.class));
        Map<String, Object> data = new HashMap<>();
        data.put("id", "00001");
        data.put("amount", 1);
        data.put("price", 5.00);

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class,
                () -> dynamoDbClient.insertSingleDocument(data, ORDERS));
        assertEquals("Table " + ORDERS + " doesn't have any schema elements.", thrown.getMessage());
    }

    @Test
    public void insertSingleDocumentKeyWithNullValue() {

        Table tableMock = mock(Table.class);

        List<KeySchemaElement> keySchemaElements = new ArrayList<>();
        KeySchemaElement keySchemaElement1 = new KeySchemaElement("id", "String");
        keySchemaElements.add(keySchemaElement1);
        KeySchemaElement keySchemaElement2 = new KeySchemaElement("amount", "int");
        keySchemaElements.add(keySchemaElement2);
        TableDescription tableDescription = new TableDescription().withKeySchema(keySchemaElements);

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(tableDescription).when(tableMock).describe();
        doReturn(null).when(tableMock).putItem(any(Item.class));
        Map<String, Object> data = new HashMap<>();
        data.put("id", "00001");
        data.put("amount", null);
        data.put("price", 5.00);

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class,
                () -> dynamoDbClient.insertSingleDocument(data, ORDERS));
        assertEquals("Null value received for the primary key element: amount", thrown.getMessage());
    }

    @Test
    public void insertSingleDocumentFailure() {
        memoryAppender.reset();
        Table tableMock = mock(Table.class);

        List<KeySchemaElement> keySchemaElements = new ArrayList<>();
        KeySchemaElement keySchemaElement = new KeySchemaElement("id", "String");
        keySchemaElements.add(keySchemaElement);
        TableDescription tableDescription = new TableDescription().withKeySchema(keySchemaElements);

        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(tableDescription).when(tableMock).describe();
        doThrow(new RuntimeException("put failed")).when(tableMock).putItem(any(Item.class));
        Map<String, Object> data = new HashMap<>();
        data.put("id", "00001");
        data.put("amount", 1);
        data.put("price", 5.00);
        dynamoDbClient.insertSingleDocument(data, ORDERS);
        assertEquals(1, memoryAppender.search("Error msg while putting item:", Level.ERROR).size());
    }

    @Test
    public void deleteSingleDocumentEmptyParam() {

        RuntimeException thrown =
            assertThrows(RuntimeException.class,
                () -> dynamoDbClient.deleteSingleDocument("12345", ""));
        assertEquals("Table name cannot be null or empty.", thrown.getMessage());
    }

    @Test
    public void deleteSingleDocumentSuccess() {
        memoryAppender.reset();
        Table tableMock = mock(Table.class);
        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(null).when(tableMock).deleteItem(any(DeleteItemSpec.class));
        dynamoDbClient.deleteSingleDocument("12345", ORDERS);
        assertEquals(1, memoryAppender.search("Successfully deleted the record with primary key", Level.DEBUG).size());
        verify(tableMock, times(1)).deleteItem(any(DeleteItemSpec.class));
    }

    @Test
    public void deleteSingleDocumentFailure() {
        memoryAppender.reset();
        Table tableMock = mock(Table.class);
        doReturn(tableMock).when(dynamoDb).getTable(any());
        doThrow(new RuntimeException("fail delete")).when(tableMock).deleteItem(any(DeleteItemSpec.class));
        dynamoDbClient.deleteSingleDocument("12345", ORDERS);
        assertEquals(1, memoryAppender.search("Error msg while deleting item:", Level.ERROR).size());
    }

    @Test
    public void getFieldValuesByFieldEmptyParam() {

        RuntimeException thrown =
            assertThrows(RuntimeException.class,
                () -> dynamoDbClient.getFieldValuesByField("id", "12345", "", "amount"));
        assertEquals("Table name cannot be null or empty.", thrown.getMessage());
    }

    @Test
    public void getFieldValuesByFieldSuccess() {
        Table tableMock = mock(Table.class);
        Index indexMock = mock(Index.class);
        ItemCollection<QueryOutcome> itemsMock = mock(ItemCollection.class);
        Item itemMock = mock(Item.class);
        IteratorSupport iteratorSupportMock = mock(IteratorSupport.class);
        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(indexMock).when(tableMock).getIndex(any());
        doReturn(itemsMock).when(indexMock).query(any(QuerySpec.class));
        doReturn(iteratorSupportMock).when(itemsMock).iterator();
        doReturn(true).doReturn(false).when(iteratorSupportMock).hasNext();
        doReturn(itemMock).when(iteratorSupportMock).next();
        doReturn("id").when(itemMock).getString(any());

        List<String> result = dynamoDbClient.getFieldValuesByField("id", "12345", ORDERS, "amount");
        assertEquals(1, result.size());
        assertEquals("id", result.get(0));
    }

    @Test
    public void getFieldValuesByFieldFailure() {
        memoryAppender.reset();
        Table tableMock = mock(Table.class);
        Index indexMock = mock(Index.class);
        doReturn(tableMock).when(dynamoDb).getTable(any());
        doReturn(indexMock).when(tableMock).getIndex(any());
        doThrow(new RuntimeException("failure")).when(indexMock).query(any(QuerySpec.class));
        dynamoDbClient.getFieldValuesByField("id", "12345", ORDERS, "amount");
        assertEquals(1, memoryAppender.search("Error msg while getting item:", Level.ERROR).size());
    }

    @Test
    public void getIdField() {
        assertEquals("id", dynamoDbClient.getIdField());
    }
}