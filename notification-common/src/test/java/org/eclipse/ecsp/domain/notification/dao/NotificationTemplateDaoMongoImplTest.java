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
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
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
import java.util.List;
import java.util.Locale;

/**
 * NotificationTemplateDaoMongoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class,
    DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class NotificationTemplateDaoMongoImplTest {

    @Autowired
    NotificationTemplateDAO notificationTemplateDaoMongoImpl;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();


    @Test
    public void testfindByNotificationIdAndLocale() {
        NotificationTemplate nt = new NotificationTemplate();
        Locale locale = new Locale("en-US");
        nt.setNotificationId("test");
        nt.setLocale(locale);
        notificationTemplateDaoMongoImpl.save(nt);
        notificationTemplateDaoMongoImpl.findByNotificationIdAndLocale("test", locale);
    }

    @Test
    public void testfindByNotificationIdAndLocale2() {
        NotificationTemplate nt = new NotificationTemplate();
        Locale locale = new Locale("en-US");
        nt.setNotificationId("test");
        nt.setLocale(locale);
        notificationTemplateDaoMongoImpl.save(nt);
        notificationTemplateDaoMongoImpl.findByNotificationIdAndLocale("test1", locale);
    }


    @Test
    public void testfindByNotificationIdAndBrand() {
        NotificationTemplate nt = new NotificationTemplate();
        Locale locale = new Locale("en-US");
        nt.setNotificationId("test");
        nt.setLocale(locale);
        nt.setBrand("test");
        notificationTemplateDaoMongoImpl.save(nt);
        notificationTemplateDaoMongoImpl.findByNotificationIdAndBrand("test", "test");
    }

    @Test
    public void testffindByNotificationIdLocaleAndBrand() {
        NotificationTemplate nt = new NotificationTemplate();
        Locale locale = new Locale("en-US");
        nt.setNotificationId("test");
        nt.setLocale(locale);
        nt.setBrand("test");
        notificationTemplateDaoMongoImpl.save(nt);
        notificationTemplateDaoMongoImpl.findByNotificationIdLocaleAndBrand("test1", locale, "test");
    }

    @Test
    public void findByNotificationIdLocalesAndBrands() {

        NotificationTemplate nt = new NotificationTemplate();
        Locale locale = new Locale("en-US");
        nt.setNotificationId("test");
        nt.setLocale(locale);
        nt.setBrand("test");
        notificationTemplateDaoMongoImpl.save(nt);
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(locale);
        List<String> brands = new ArrayList<String>();
        brands.add("test");
        notificationTemplateDaoMongoImpl.findByNotificationIdLocalesAndBrands("test", locales, brands);

    }

    @Test
    public void testfindByNotificationIdAndBrands() {


        NotificationTemplate nt = new NotificationTemplate();
        Locale locale = new Locale("en-US");
        nt.setNotificationId("test");
        nt.setLocale(locale);
        nt.setBrand("test");
        notificationTemplateDaoMongoImpl.save(nt);
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(locale);
        List<String> brands = new ArrayList<String>();
        brands.add("test");
        notificationTemplateDaoMongoImpl.findByNotificationIdAndBrands("test", brands);


    }
}
