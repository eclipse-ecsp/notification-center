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

package org.eclipse.ecsp.domain.notification.config;

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.domain.notification.grouping.TestSecurityLibSample;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.config.NotificationBuffer;
import org.eclipse.ecsp.notification.dao.NotificationBufferDaoImpl;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * NotificationBufferDaoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class, DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class NotificationBufferDaoImplTest {

    @Autowired
    NotificationBufferDaoImpl notificationBufferDaoImpl;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Test
    public void testfindBySchedulerId() {
        NotificationBuffer notificationBuffer = new NotificationBuffer();
        notificationBuffer.setSchedulerId("test123");
        notificationBufferDaoImpl.save(notificationBuffer);
        notificationBufferDaoImpl.findBySchedulerId("test123");
    }

    @Test
    public void testfindByUserIdAndVehicleIdAndChannelTypeAndGroup() {
        NotificationBuffer notificationBuffer = new NotificationBuffer();
        notificationBuffer.setUserId("test123");
        notificationBuffer.setVehicleId("test123");
        notificationBuffer.setGroup("test123");
        notificationBuffer.setChannelType(null);
        notificationBufferDaoImpl.save(notificationBuffer);
        notificationBufferDaoImpl.findByUserIdAndVehicleIdAndChannelTypeAndGroup("test123", "test123", null, "test123");

    }

    @Test
    public void testfindByUserIdAndVehicleId() {

        NotificationBuffer notificationBuffer = new NotificationBuffer();
        notificationBuffer.setUserId("test123");
        notificationBuffer.setVehicleId("test123");
        notificationBuffer.setGroup("test123");
        notificationBufferDaoImpl.save(notificationBuffer);
        notificationBufferDaoImpl.findByUserIdAndVehicleId("test123", "test123");

    }

    @Test
    public void testdeleteByUserIdAndVehicleId() {
        NotificationBuffer notificationBuffer = new NotificationBuffer();
        notificationBuffer.setUserId("test123");
        notificationBuffer.setVehicleId("test123");
        notificationBuffer.setGroup("test123");
        notificationBufferDaoImpl.save(notificationBuffer);
        notificationBufferDaoImpl.deleteByUserIdAndVehicleId("test123", "test123");

    }

    @Test
    public void findByUserIdVehicleIdChannelTypeGroupContactId() {

        NotificationBuffer notificationBuffer = new NotificationBuffer();
        notificationBuffer.setUserId("test123");
        notificationBuffer.setVehicleId("test123");
        notificationBuffer.setGroup("test123");
        notificationBuffer.setChannelType(null);
        notificationBuffer.setContactId("test123");
        notificationBufferDaoImpl.save(notificationBuffer);
        notificationBufferDaoImpl.findByUserIDVehicleIDChannelTypeGroupContactId("test123", "test123", null, "test123",
            "test123");


    }

}
