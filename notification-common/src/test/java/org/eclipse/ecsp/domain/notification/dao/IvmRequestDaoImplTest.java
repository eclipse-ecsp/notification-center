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
import org.eclipse.ecsp.domain.notification.IVMRequest;
import org.eclipse.ecsp.domain.notification.grouping.TestSecurityLibSample;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.dao.IVMRequestDAOImpl;
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
 * IVMRequestDaoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class, DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class IvmRequestDaoImplTest {

    @Autowired
    IVMRequestDAOImpl ivmRequestDaoImpl;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Test
    public void testfindByVehicleIdMessageId() {
        IVMRequest ir = new IVMRequest();
        ir.setVehicleId("test");
        ir.setMessageId("test");
        ivmRequestDaoImpl.save(ir);
        ivmRequestDaoImpl.findByVehicleIdMessageId("test", "test");
    }

    @Test
    public void testfindByVehicleIdSessionId() {
        IVMRequest ir = new IVMRequest();
        ir.setVehicleId("test");
        ir.setSessionId("test");
        ivmRequestDaoImpl.save(ir);
        ivmRequestDaoImpl.findByVehicleIdSessionId("test", "test");
    }

    @Test
    public void testfindByVehicleIdMessageIdnull() {

        ivmRequestDaoImpl.findByVehicleIdMessageId("test", "test");
    }

    @Test
    public void testfindByVehicleIdSessionIdnull() {

        ivmRequestDaoImpl.findByVehicleIdSessionId("test", "test");
    }
}
