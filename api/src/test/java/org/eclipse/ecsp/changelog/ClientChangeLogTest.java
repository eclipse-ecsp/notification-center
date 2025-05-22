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

package org.eclipse.ecsp.changelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertManyResult;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.utils.JsonUtils;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.ecsp.changelog.ClientChangeLog.DTC_CLEARED_ID;
import static org.eclipse.ecsp.changelog.ClientChangeLog.DTC_SET_ID;
import static org.eclipse.ecsp.changelog.ClientChangeLog.ID_FIELD;
import static org.eclipse.ecsp.changelog.ClientChangeLog.NOTIFICATION_CONFIG;
import static org.eclipse.ecsp.changelog.ClientChangeLog.NOTIFICATION_TEMPLATES;
import static org.eclipse.ecsp.notification.dao.NotificationDaoConstants.NOTIFICATION_BUFFER;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * ClientChangeLogTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ClientChangeLogTest {

    @InjectMocks
    private ClientChangeLog clientChangeLog;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    MongoCollection<Document> mongoCollection;

    @Mock
    FindIterable<Document> findIterable;

    @Mock
    InsertManyResult insertManyResult;

    @Mock
    MongoCursor<Document> cursor;


    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * beforeEach method.
     */
    @BeforeEach
    public void beforeEach() {
        initMocks(this);
    }

    /**
     * backNotificationGroupingEmptyCollection method.
     */
    @Test
    public void backNotificationGroupingEmptyCollection() {

        when(findIterable.iterator()).thenReturn(cursor);
        assertNotNull(cursor);
        when(cursor.hasNext()).thenReturn(false);
        when(cursor.next()).thenReturn(null);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);

        clientChangeLog.backNotificationGrouping(mongoTemplate);
    }

    /**
     * backNotificationGroupingWithCollection method.
     */
    @Test
    public void backNotificationGroupingWithCollection() {

        Document doc1 = new Document("key1", "value1");

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1);

        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.backNotificationGrouping(mongoTemplate);
    }

    /**
     * updateNotificationGroupingEmptyCollection method.
     */
    @Test
    public void updateNotificationGroupingEmptyCollection() {

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        when(cursor.next()).thenReturn(null);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.updateNotificationGrouping(mongoTemplate);
    }

    /**
     * updateNotificationGroupingWithCollection method.
     */
    @Test
    public void updateNotificationGroupingWithCollection() {

        Document doc1 = new Document("_id", "n1");
        doc1.put("group", "g1");
        doc1.put("service", "");
        Document doc2 = new Document("_id", "n2");
        doc2.put("group", "g2");
        doc2.put("service", "s2");

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1).thenReturn(doc2);

        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        doNothing().when(mongoCollection).drop();
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.updateNotificationGrouping(mongoTemplate);
    }

    /**
     * updateNotificationBufferWithContactIdField method.
     */
    @Test
    public void backNotificationGroupingGroupTypeEmptyCollection() {

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        when(cursor.next()).thenReturn(null);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.backNotificationGroupingGroupType(mongoTemplate);
    }

    /**
     * backNotificationGroupingGroupTypeWithCollection method.
     */
    @Test
    public void backNotificationGroupingGroupTypeWithCollection() {

        Document doc1 = new Document("key1", "value1");

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1);

        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.backNotificationGroupingGroupType(mongoTemplate);
    }

    /**
     * updateNotificationGroupingWithGroupTypeEmptyCollection method.
     */
    @Test
    public void updateNotificationGroupingWithGroupTypeEmptyCollection() {

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        when(cursor.next()).thenReturn(null);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.updateNotificationGroupingWithGroupType(mongoTemplate);
    }

    /**
     * updateNotificationGroupingWithGroupTypeWithCollection method.
     */
    @Test
    public void updateNotificationGroupingWithGroupTypeWithCollection() {

        Document doc1 = new Document("_id", "n1");
        doc1.put("group", "g1");
        doc1.put("service", "");
        Document doc2 = new Document("_id", "n2");
        doc2.put("group", "g2");
        doc2.put("service", "s2");
        doc2.put("groupType", "gt");

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1).thenReturn(doc2);

        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        doNothing().when(mongoCollection).drop();
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.updateNotificationGroupingWithGroupType(mongoTemplate);
    }

    /**
     * updateNotificationBufferWithContactIdField method.
     */
    @Test
    public void updateNotificationBufferWithContactIdField() {

        Document doc1 = Mockito.mock(Document.class, Mockito.RETURNS_DEEP_STUBS);

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1);
        when(doc1.getList(any(), any())).thenReturn(Arrays.asList(doc1));
        when(doc1.getEmbedded(anyList(), any())).thenReturn(doc1);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        assertNotNull(cursor);
        clientChangeLog.updateNotificationBufferWithContactId(mongoTemplate);
    }

    /**
     * updateNotificationBufferWithContactIdFieldThrowError method.
     */
    @Test
    public void updateNotificationBufferWithContactIdFieldThrowError() {

        Document doc1 = Mockito.mock(Document.class, Mockito.RETURNS_DEEP_STUBS);

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1);
        when(doc1.getList(any(), any())).thenReturn(Arrays.asList(doc1));
        when(doc1.getEmbedded(anyList(), any())).thenReturn(doc1);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        when(mongoTemplate.save(any(), any())).thenThrow(new RuntimeException("rt ex"));
        assertNotNull(cursor);
        clientChangeLog.updateNotificationBufferWithContactId(mongoTemplate);
    }

    /**
     * updateNotificationBufferWithContactIdField method.
     */
    @SneakyThrows
    @Test
    public void updateDtcTemplate() {

        Document setTemplate = null;
        try {
            setTemplate = objectMapper
                .readValue(ResourceUtils.getFile("classpath:ignite_notification_template_DTCStored_set.json"),
                    Document.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(setTemplate);
        Bson setFilter = Filters.regex(ID_FIELD, ("^" + DTC_SET_ID));
        when(mongoTemplate.getCollection(NOTIFICATION_TEMPLATES)).thenReturn(mongoCollection);
        when(mongoCollection.find(setFilter)).thenReturn(findIterable);
        assertNotNull(cursor);
        Document clearedTemplate = null;
        try {
            clearedTemplate = objectMapper
                .readValue(ResourceUtils.getFile("classpath:ignite_notification_template_DTCStored_set.json"),
                    Document.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        when(cursor.next()).thenReturn(clearedTemplate);
        Bson clearedFilter = Filters.regex(ID_FIELD, ("^" + DTC_CLEARED_ID));
        when(mongoTemplate.getCollection(NOTIFICATION_TEMPLATES).find(clearedFilter)).thenReturn(findIterable);
    }

    /**
     * updateNotificationBufferWithContactIdField method.
     */
    @Test(expected = RuntimeException.class)
    public void addBrandToDefaultConfigsWhenUpdateFailed() {
        doThrow(new RuntimeException("rt ex")).when(mongoTemplate).getCollection(NOTIFICATION_CONFIG);
        assertNotNull(mongoTemplate);
        clientChangeLog.addBrandToDefaultConfigs(mongoTemplate);
    }

    /**
     * updateNotificationBufferWithContactIdField method.
     */
    @Test
    public void removeRedundantIndexNotificationBufferWithIndex() {
        Document doc1 = new Document("_id", "n1");
        doc1.put("name", "userId_1_vehicleId_1_schedulerId_1_group_1");
        MongoCollection<Document> mockCollection = Mockito.mock(MongoCollection.class);
        assertNotNull(mongoTemplate);
        when(mongoTemplate.getCollection(NOTIFICATION_BUFFER)).thenReturn(mockCollection);
        ListIndexesIterable listIndexesIterable = Mockito.mock(ListIndexesIterable.class);
        when(listIndexesIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1);
        when(mockCollection.listIndexes()).thenReturn(listIndexesIterable);
        doNothing().when(mockCollection).dropIndex("userId_1_vehicleId_1_schedulerId_1_group_1");
        clientChangeLog.removeRedundantIndexNotificationBuffer(mongoTemplate);
        Mockito.verify(mockCollection, Mockito.times(1)).listIndexes();
        Mockito.verify(mockCollection, Mockito.times(1)).dropIndex("userId_1_vehicleId_1_schedulerId_1_group_1");
        verifyNoMoreInteractions(mockCollection);
    }

    /**
     * removeRedundantIndexNotificationBufferWithoutIndex method.
     */
    @Test
    public void removeRedundantIndexNotificationBufferWithoutIndex() {
        Document doc1 = new Document("_id", "n1");
        doc1.put("name", "no_index");
        MongoCollection<Document> mockCollection = Mockito.mock(MongoCollection.class);
        when(mongoTemplate.getCollection(NOTIFICATION_BUFFER)).thenReturn(mockCollection);
        assertNotNull(mongoTemplate);
        ListIndexesIterable listIndexesIterable = Mockito.mock(ListIndexesIterable.class);
        when(listIndexesIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1);
        when(mockCollection.listIndexes()).thenReturn(listIndexesIterable);
        clientChangeLog.removeRedundantIndexNotificationBuffer(mongoTemplate);
        Mockito.verify(mockCollection, Mockito.times(1)).listIndexes();
        Mockito.verify(mockCollection, Mockito.times(0)).dropIndex("userId_1_vehicleId_1_schedulerId_1_group_1");
        verifyNoMoreInteractions(mockCollection);

    }

    /**
     * migrateExportTemplateToFiles method.
     */
    @Test
    public void migrateExportTemplateToFiles() throws Exception {

        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = JsonUtils.getListObjects(
            IOUtils.toString(MigrationUtilsTest.class.getResourceAsStream("/migrationTemplate.json"),
                StandardCharsets.UTF_8),
            DynamicNotificationTemplate.class);

        MongoDatabaseFactory mongoDbFactory = Mockito.mock(MongoDatabaseFactory.class, Mockito.RETURNS_DEEP_STUBS);

        when(mongoTemplate.findAll(DynamicNotificationTemplate.class, NOTIFICATION_TEMPLATES)).thenReturn(
            dynamicNotificationTemplateList);
        when(mongoTemplate.getMongoDatabaseFactory()).thenReturn(mongoDbFactory);
        assertNotNull(mongoTemplate);

        clientChangeLog.migrateExportTemplateToFiles(mongoTemplate);
    }

    /**
     * backMarketingNamesCollection method.
     */
    @Test
    public void backMarketingNamesCollection() {

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        when(cursor.next()).thenReturn(null);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        assertNotNull(mongoTemplate);
        clientChangeLog.backMarketingNames(mongoTemplate);
    }

    /**
     * backMarketingNamesWithCollection method.
     */
    @Test
    public void backMarketingNamesWithCollection() {

        Document doc1 = new Document("key1", "value1");

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1);

        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        assertNotNull(mongoTemplate);
        clientChangeLog.backMarketingNames(mongoTemplate);
    }

    /**
     * updateMarketingNameEmptyCollection method.
     */
    @Test
    public void updateMarketingNameEmptyCollection() {

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);
        when(cursor.next()).thenReturn(null);
        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        assertNotNull(mongoTemplate);
        clientChangeLog.updateMarketingNames(mongoTemplate);
    }

    /**
     * updateMarketingNameWithCollection method.
     */
    @Test
    public void updateMarketingNameWithCollection() {

        Document doc1 = new Document("_id", "mn1");
        doc1.put("brandNmae", "kia");
        doc1.put("marketingName", "kiaPicanto");
        Document doc2 = new Document("_id", "mn2");
        doc2.put("brandNmae", "Deep");
        doc2.put("marketingName", "DeepAa");

        when(findIterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(doc1).thenReturn(doc2);

        when(mongoCollection.find()).thenReturn(findIterable);
        when(mongoCollection.insertMany(any())).thenReturn(insertManyResult);
        doNothing().when(mongoCollection).drop();
        when(mongoTemplate.getCollection(any())).thenReturn(mongoCollection);
        when(mongoTemplate.createCollection(anyString())).thenReturn(mongoCollection);
        assertNotNull(mongoTemplate);
        clientChangeLog.updateMarketingNames(mongoTemplate);
    }
}