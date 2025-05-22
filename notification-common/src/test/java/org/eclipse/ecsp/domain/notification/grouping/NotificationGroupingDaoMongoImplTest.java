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

package org.eclipse.ecsp.domain.notification.grouping;

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
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

/**
 * NotificationGroupingDaoMongoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class, DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class NotificationGroupingDaoMongoImplTest {

    @Autowired
    private NotificationGroupingDAO notificationGroupingDao;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Test
    public void testCr() {
        NotificationGrouping ng = new NotificationGrouping();
        ng.setSchemaVersion(Version.V1_0);
        ng.setGroup("ParentalControls");
        ng.setNotificationId("GeoFenceIn");
        notificationGroupingDao.save(ng);
        NotificationGrouping ng1 = notificationGroupingDao.findFirstByNotificationId(ng.getNotificationId());
        notificationGroupingDao.findFirstByNotificationId("test");
    }

    @Test
    public void testfindByMandatory() {
        NotificationGrouping ng = new NotificationGrouping();
        ng.setSchemaVersion(Version.V1_0);
        ng.setGroup("ParentalControls");
        ng.setNotificationId("GeoFenceIn");
        ng.setMandatory(false);
        notificationGroupingDao.save(ng);
        notificationGroupingDao.findByMandatory(false);
    }

    @Test
    public void testfindByGroups() {

        NotificationGrouping ng = new NotificationGrouping();
        ng.setSchemaVersion(Version.V1_0);
        ng.setGroup("ParentalControls");
        ng.setNotificationId("GeoFenceIn");
        ng.setMandatory(false);
        notificationGroupingDao.save(ng);
        List<String> grpnames = new ArrayList<String>();
        grpnames.add("ParentalControls");
        notificationGroupingDao.findByGroups(grpnames);

    }

    @Test
    public void testfindByNotificationId() {

        NotificationGrouping ng = new NotificationGrouping();
        ng.setSchemaVersion(Version.V1_0);
        ng.setGroup("ParentalControls");
        ng.setNotificationId("GeoFenceIn");
        ng.setMandatory(false);
        notificationGroupingDao.save(ng);
        List<String> grpnames = new ArrayList<String>();
        grpnames.add("ParentalControls");
        notificationGroupingDao.findByNotificationId("GeoFenceIn");
    }

    @Test
    public void testdeleteByGroupNotificationIdAndService() {

        NotificationGrouping ng = new NotificationGrouping();
        ng.setSchemaVersion(Version.V1_0);
        ng.setGroup("ParentalControls");
        ng.setNotificationId("GeoFenceIn");
        ng.setMandatory(false);
        ng.setService("test");
        notificationGroupingDao.save(ng);
        List<String> grpnames = new ArrayList<String>();
        grpnames.add("ParentalControls");
        notificationGroupingDao.deleteByGroupNotificationIdAndService("ParentalControls", "GeoFenceIn", "test");

    }
}
