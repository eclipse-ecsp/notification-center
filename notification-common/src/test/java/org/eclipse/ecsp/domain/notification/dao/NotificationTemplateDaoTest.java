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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.domain.notification.grouping.TestSecurityLibSample;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

/**
 * NotificationTemplateDaoTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class,
    DaoConfig.class})
@TestPropertySource("/notificationTemplate-dao-test.properties")
public class NotificationTemplateDaoTest {

    @Autowired
    private NotificationTemplateDAO notificationTemplateDao;

    @Autowired
    private DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    @Autowired
    private RichContentDynamicNotificationTemplateDAO richContentDynamicNotificationTemplateDao;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * saveNotificationTemplate method.
     *
     */
    @Before
    public void saveNotificationTemplate() throws IOException {

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        String inputData =
            IOUtils.toString(NotificationTemplateDaoTest.class.getResourceAsStream("/notificationTemplate.json"),
                "UTF-8");
        DynamicNotificationTemplate notificationTemplate =
            mapper.readValue(inputData, DynamicNotificationTemplate.class);
        dynamicNotificationTemplateDao.save(notificationTemplate);

        inputData =
            IOUtils.toString(NotificationTemplateDaoTest.class.getResourceAsStream("/richNotificationTemplate.json"),
                "UTF-8");
        RichContentDynamicNotificationTemplate[] richTemplate =
            mapper.readValue(inputData, RichContentDynamicNotificationTemplate[].class);
        richContentDynamicNotificationTemplateDao.saveAll(richTemplate);
    }

    @Test
    public void findByNotificationIdLocaleAndBrandTest() {

        Locale locale = Locale.forLanguageTag("en-us");
        Optional<NotificationTemplate> notificationTemplate =
            notificationTemplateDao.findByNotificationIdLocaleAndBrand("GeoFence_In", locale, "Fita");
        Assert.assertTrue(notificationTemplate.isPresent());
        Assert.assertEquals(1, notificationTemplateDao.findAll().size());
        Assert.assertEquals("GeoFence_In", notificationTemplate.get().getNotificationId());
    }


    @Test
    public void findRichUnderscoreByNotificationIdLocaleAndBrandTest() {
        Locale locale = Locale.forLanguageTag("en-us");
        Optional<RichContentDynamicNotificationTemplate> deep = richContentDynamicNotificationTemplateDao
            .findByNotificationIdLocaleAndBrand("test-notification-underscore", locale, "Deep");
        Assert.assertTrue(deep.isPresent());
        Assert.assertEquals("en_US", deep.get().getLocale());
    }

    @Test
    public void findRichDashByNotificationIdLocaleAndBrandTest() {
        Locale locale = Locale.forLanguageTag("en-us");
        Optional<RichContentDynamicNotificationTemplate> deep = richContentDynamicNotificationTemplateDao
            .findByNotificationIdLocaleAndBrand("test-notification-dash", locale, "Deep");
        Assert.assertTrue(deep.isPresent());
        Assert.assertEquals("en-US", deep.get().getLocale());
    }

    @After
    public void tearDown() {
        notificationTemplateDao.deleteAll();
    }

}