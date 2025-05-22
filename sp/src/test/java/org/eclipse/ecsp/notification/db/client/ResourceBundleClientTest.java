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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ResourceBundleClientTest.
 */
public class ResourceBundleClientTest {


    ResourceBundleClient resourceBundleClient;

    @Before
    public void init() {
        resourceBundleClient = new ResourceBundleClient();
    }

    @Test
    public void getFieldValueWithPrimaryKey_idNull() {
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> resourceBundleClient.getFieldValueWithPrimaryKey(null, "dummyFieldName", "dummyBaseBundleName"));
        assertEquals("Locale cannot be null or empty.", thrown.getMessage());
    }

    @Test
    public void getFieldValueWithPrimaryKey_fieldNameNull() {
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> resourceBundleClient.getFieldValueWithPrimaryKey("dummyId", null, "dummyBaseBundleName"));
        assertEquals("Field name cannot be null or empty.", thrown.getMessage());
    }

    @Test
    public void getFieldValueWithPrimaryKey_baseBundleNameNull() {
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> resourceBundleClient.getFieldValueWithPrimaryKey("dummyId", "dummyFieldName", null));
        assertEquals("Base bundle name cannot be null or empty.", thrown.getMessage());
    }

    @Test(expected = Test.None.class)
    public void init_success() {
        Properties prop = new Properties();
        resourceBundleClient.init(prop);
    }

    @Test(expected = Test.None.class)
    public void insertMultiDocument_success() throws IOException {
        resourceBundleClient.insertMultiDocument("", "");
    }

    @Test(expected = Test.None.class)
    public void insertSingleDocument_success() {
        resourceBundleClient.insertSingleDocument(null, "");
    }

    @Test(expected = Test.None.class)
    public void deleteSingleDocument_success() {
        resourceBundleClient.deleteSingleDocument("", "");
    }

    @Test
    public void getFieldValuesByField_success() {
        assertTrue(resourceBundleClient.getFieldValuesByField("", "", "", "").isEmpty());
    }

    @Test
    public void getIdField_success() {
        assertNull(resourceBundleClient.getIdField());
        ;
    }

    @Test(expected = Test.None.class)
    public void insertAlert_success() {
        resourceBundleClient.insertAlert(null, "");
    }
}
