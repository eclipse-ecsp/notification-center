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

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;

import static org.eclipse.ecsp.changelog.ClientChangeLog.NOTIFICATION_CONFIG;
import static org.eclipse.ecsp.notification.config.NotificationConfig.USER_ID_FOR_DEFAULT_PREFERENCE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * ClientChangeLogParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class ClientChangeLogParameterizedTest {

    /**
     * LONG constant.
     */
    public static final long LONG = 2L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

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

    /**
     * setUp method.
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * ClientChangeLogParameterizedTest constructor.
     *
     * @param brand1 String
     * @param brand2 String
     * @param matchedCount long
     */
    public ClientChangeLogParameterizedTest(String brand1, String brand2, long matchedCount) {
        this.brand1 = brand1;
        this.brand2 = brand2;
        this.matchedCount = matchedCount;
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"brand1", "brand1", 1L },
                {"brand1", "brand1", LONG},
                {"", "default", LONG},

        });
    }

    private final String brand1;
    private final String brand2;
    private final long matchedCount;

    /**
     * AddBrandToDefaultConfigsWhenAllUpdated method.
     */
    @Test
    public void addBrandToDefaultConfigsWhenAllUpdated() {
        ReflectionTestUtils.setField(clientChangeLog, "defaultBrand", brand1);
        Bson filter = BsonDocument.parse("{ \"userId\": \"" + USER_ID_FOR_DEFAULT_PREFERENCE + "\"}");
        Bson update = BsonDocument.parse("{ $set: { brand: \"" + brand2 + "\" }}");
        assertNotNull(filter);
        when(mongoTemplate.getCollection(NOTIFICATION_CONFIG)).thenReturn(mongoCollection);
        when(mongoCollection.updateMany(filter, update)).thenReturn(UpdateResult.acknowledged(matchedCount, 1L, null));

        clientChangeLog.addBrandToDefaultConfigs(mongoTemplate);
        Mockito.verify(mongoCollection, Mockito.times(1)).updateMany(filter, update);
        Mockito.verifyNoMoreInteractions(mongoCollection);
    }
}