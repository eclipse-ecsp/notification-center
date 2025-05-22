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

package org.eclipse.ecsp.domain.notification.dao;

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.domain.notification.grouping.TestSecurityLibSample;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.dao.NotificationPlaceholderDao;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * NotificationPlaceholderDaoMongoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class, DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class NotificationPlaceholderDaoMongoImplTest {

    @Autowired
    NotificationPlaceholderDao npdao;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Test
    public void testdeleteByKeys() {

        NotificationPlaceholder np = new NotificationPlaceholder();
        np.setKey("test");
        np.setBrand("test");
        Locale l = new Locale("en-US");
        np.setLocale(l);

        npdao.save(np);

        Set<String> placeholdersKeys = new HashSet<String>();
        placeholdersKeys.add("test");
        npdao.deleteByKeys(placeholdersKeys);
    }


    @Test
    public void testfindByKeys() {

        NotificationPlaceholder np = new NotificationPlaceholder();
        np.setKey("test");
        np.setBrand("test");
        Locale l = new Locale("en-US");
        np.setLocale(l);

        npdao.save(np);

        Set<String> placeholdersKeys = new HashSet<String>();
        placeholdersKeys.add("test");
        npdao.findByKeys(placeholdersKeys);
    }

    @Test
    public void testfindByKeysBrandsAndLocales() {

        NotificationPlaceholder np = new NotificationPlaceholder();
        np.setKey("test");
        np.setBrand("test");
        Locale l = new Locale("en-US");
        np.setLocale(l);

        npdao.save(np);

        Set<String> placeholdersKeys = new HashSet<String>();
        placeholdersKeys.add("test");

        List<String> brand = new ArrayList<String>();
        brand.add("test");


        List<Locale> locale = new ArrayList<Locale>();
        locale.add(l);

        npdao.findByKeysBrandsAndLocales(placeholdersKeys, locale, brand);

    }

}
