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

package org.eclipse.ecsp.domain.notification;

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.domain.notification.grouping.TestSecurityLibSample;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAOMongoImpl;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
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
import java.util.stream.Collectors;



/**
 *  RichContentDynamicNotificationTemplateDaoMongoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class, DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class RichContentDynamicNotificationTemplateDaoMongoImplTest {

    @Autowired
    RichContentDynamicNotificationTemplateDAOMongoImpl richContentDynamicNotificationTemplateDaoMongoImpl;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Test
    public void testfindByNotificationIds() {
        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            new RichContentDynamicNotificationTemplate();
        richContentDynamicNotificationTemplate.setNotificationId("test");
        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate2 =
            new RichContentDynamicNotificationTemplate();
        richContentDynamicNotificationTemplate2.setNotificationId("test");
        richContentDynamicNotificationTemplateDaoMongoImpl.save(richContentDynamicNotificationTemplate);
        richContentDynamicNotificationTemplateDaoMongoImpl.save(richContentDynamicNotificationTemplate2);
        List<String> notificationIds = new ArrayList<String>();
        notificationIds.add("test");
        richContentDynamicNotificationTemplateDaoMongoImpl.findByNotificationIds(notificationIds);
    }

    @Test
    public void testfindByNotificationIdLocaleAndBrand() {

        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            new RichContentDynamicNotificationTemplate();
        richContentDynamicNotificationTemplate.setNotificationId("test");
        richContentDynamicNotificationTemplate.setLocale("en-US");
        richContentDynamicNotificationTemplate.setBrand("fita");
        richContentDynamicNotificationTemplateDaoMongoImpl.save(richContentDynamicNotificationTemplate);
        List<String> brand = new ArrayList<String>();
        brand.add("fita");
        List<Locale> locales = new ArrayList<Locale>();
        Locale l = new Locale("en-US");
        locales.add(l);
        richContentDynamicNotificationTemplateDaoMongoImpl.findByNotificationIdLocaleAndBrand("test", l, "fita");
    }

    @Test
    public void testfindByNotificationIdLocalesAndBrands() {

        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            new RichContentDynamicNotificationTemplate();
        richContentDynamicNotificationTemplate.setNotificationId("test");
        richContentDynamicNotificationTemplate.setLocale("en-US");
        richContentDynamicNotificationTemplate.setBrand("fita");
        richContentDynamicNotificationTemplateDaoMongoImpl.save(richContentDynamicNotificationTemplate);
        List<String> brand = new ArrayList<String>();
        brand.add("fita");
        List<Locale> locales = new ArrayList<Locale>();
        Locale l = new Locale("en-US");
        locales.add(l);
        richContentDynamicNotificationTemplateDaoMongoImpl.findByNotificationIdLocalesAndBrands("test", locales, brand);
    }

    @Test
    public void testfindByNotificationIdsBrandsLocalesNoAddAttrs() {

        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            new RichContentDynamicNotificationTemplate();
        richContentDynamicNotificationTemplate.setNotificationId("test");
        richContentDynamicNotificationTemplate.setLocale("en-US");
        richContentDynamicNotificationTemplate.setBrand("fita");
        richContentDynamicNotificationTemplateDaoMongoImpl.save(richContentDynamicNotificationTemplate);
        List<String> brand = new ArrayList<String>();
        brand.add("fita");
        List<Locale> locales = new ArrayList<Locale>();
        Locale l = new Locale("en-US");
        locales.add(l);
        richContentDynamicNotificationTemplateDaoMongoImpl.findByNotificationIdsBrandsLocalesNoAddAttrs("test",
            brand.stream().collect(Collectors.toSet()), locales.stream().collect(Collectors.toSet()));

    }

    @Test
    public void testfindByNotificationIdsBrandsLocalesAddAttrs() {

        RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
            new RichContentDynamicNotificationTemplate();
        richContentDynamicNotificationTemplate.setNotificationId("test");
        richContentDynamicNotificationTemplate.setLocale("en-US");
        richContentDynamicNotificationTemplate.setBrand("fita");
        AdditionalLookupProperty additionalLookupProperty = new AdditionalLookupProperty();
        Set<String> propertyValues = new HashSet<String>();
        propertyValues.add("test");
        additionalLookupProperty.setName("test");
        additionalLookupProperty.setValues(propertyValues);
        List<AdditionalLookupProperty> ads = new ArrayList<AdditionalLookupProperty>();
        ads.add(additionalLookupProperty);
        richContentDynamicNotificationTemplate.setAdditionalLookupProperties(ads);
        richContentDynamicNotificationTemplateDaoMongoImpl.save(richContentDynamicNotificationTemplate);
        List<String> brand = new ArrayList<String>();
        brand.add("fita");
        List<Locale> locales = new ArrayList<Locale>();
        Locale l = new Locale("en-US");
        locales.add(l);
        richContentDynamicNotificationTemplateDaoMongoImpl.findByNotificationIdsBrandsLocalesAddAttrs("test",
            brand.stream().collect(Collectors.toSet()), locales.stream().collect(Collectors.toSet()), "test",
            propertyValues);

    }

}
