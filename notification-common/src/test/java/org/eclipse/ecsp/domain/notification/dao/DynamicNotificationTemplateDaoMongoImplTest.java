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
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
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
 * DynamicNotificationTemplateDaoMongoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class,
    DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class DynamicNotificationTemplateDaoMongoImplTest {

    @Autowired
    DynamicNotificationTemplateDAO dynamicNotificationTemplateDaoMongoImpl;


    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Test
    public void testfindByNotificationIds() {
        DynamicNotificationTemplate d1 = new DynamicNotificationTemplate();
        d1.setNotificationId("dummyId");
        DynamicNotificationTemplate d2 = new DynamicNotificationTemplate();
        d2.setNotificationId("dummyId");

        dynamicNotificationTemplateDaoMongoImpl.save(d2);
        dynamicNotificationTemplateDaoMongoImpl.save(d1);

        List<String> notificationIds = new ArrayList<String>();
        notificationIds.add("dummyId");
        dynamicNotificationTemplateDaoMongoImpl.findByNotificationIds(notificationIds);

    }

    @Test
    public void testisNotificationIdExist() {
        DynamicNotificationTemplate d1 = new DynamicNotificationTemplate();
        d1.setNotificationId("dummyId");

        dynamicNotificationTemplateDaoMongoImpl.save(d1);

        dynamicNotificationTemplateDaoMongoImpl.isNotificationIdExist("dummyId");

    }

    @Test
    public void testfindByNotificationIdsBrandsLocalesNoAddAttrs() {
        DynamicNotificationTemplate d1 = new DynamicNotificationTemplate();
        d1.setNotificationId("dummyId");
        d1.setBrand("testbrand");
        d1.setLocale("en-US");
        dynamicNotificationTemplateDaoMongoImpl.save(d1);
        Set<String> brands = new HashSet<String>();
        brands.add("en-US");

        Set<Locale> locales = new HashSet<Locale>();
        Locale l = new Locale("en-US");
        locales.add(l);
        dynamicNotificationTemplateDaoMongoImpl.findByNotificationIdsBrandsLocalesNoAddAttrs("dummyId", brands,
            locales);
    }


    @Test
    public void testfindByNotificationIdsBrandsLocalesAddAttrs() {
        DynamicNotificationTemplate d1 = new DynamicNotificationTemplate();
        d1.setNotificationId("dummyId");
        d1.setBrand("testbrand");
        d1.setLocale("en-US");
        AdditionalLookupProperty ad = new AdditionalLookupProperty();
        ad.setName("test");
        Set<String> propertyValues = new HashSet<String>();
        propertyValues.add("test");
        ad.setValues(propertyValues);
        List<AdditionalLookupProperty> additionalLookupProperties = new ArrayList<AdditionalLookupProperty>();
        additionalLookupProperties.add(ad);
        d1.setAdditionalLookupProperties(additionalLookupProperties);
        dynamicNotificationTemplateDaoMongoImpl.save(d1);
        Set<String> brands = new HashSet<String>();
        brands.add("en-US");

        Set<Locale> locales = new HashSet<Locale>();
        Locale l = new Locale("en-US");
        locales.add(l);
        dynamicNotificationTemplateDaoMongoImpl.findByNotificationIdsBrandsLocalesAddAttrs("dummyId", brands, locales,
            "test", propertyValues);
    }


}
