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
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.grouping.TestSecurityLibSample;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
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
 * SecondaryContactDaoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class,
    DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class SecondaryContactDaoImplTest {

    @Autowired
    SecondaryContactDAO secondaryContactDaoImpl;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();


    @Test
    public void findById() {
        SecondaryContact secondaryContact = new SecondaryContact();
        SecondaryContact op = secondaryContactDaoImpl.save(secondaryContact);
        secondaryContactDaoImpl.findById(op.getContactId());
    }

    @Test
    public void findByIdEmail() {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setEmail("test@gmail.com");
        SecondaryContact op = secondaryContactDaoImpl.save(secondaryContact);
        secondaryContactDaoImpl.findById(op.getContactId());
    }


    @Test
    public void findByIds() {
        SecondaryContact secondaryContact = new SecondaryContact();
        SecondaryContact secondaryContact2 = new SecondaryContact();
        SecondaryContact op = secondaryContactDaoImpl.save(secondaryContact);
        SecondaryContact op2 = secondaryContactDaoImpl.save(secondaryContact2);
        List<String> decryptedSecondaryContacts = new ArrayList<String>();
        decryptedSecondaryContacts.add(op2.getContactId());
        String[] strarr = decryptedSecondaryContacts.toArray(new String[1]);
        secondaryContactDaoImpl.findByIds(strarr);
    }

    @Test
    public void getContactIds() {
        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setUserId("test");
        secondaryContact.setVehicleId("test");
        SecondaryContact op = secondaryContactDaoImpl.save(secondaryContact);
        secondaryContactDaoImpl.getContactIds("test", "test");

    }

    @Test
    public void getContacts() {

        SecondaryContact secondaryContact = new SecondaryContact();
        secondaryContact.setUserId("test");
        secondaryContact.setVehicleId("test");
        SecondaryContact secondaryContact2 = new SecondaryContact();
        secondaryContact2.setUserId("test");
        secondaryContact2.setVehicleId("test");
        SecondaryContact op = secondaryContactDaoImpl.save(secondaryContact);
        SecondaryContact op2 = secondaryContactDaoImpl.save(secondaryContact2);
        secondaryContactDaoImpl.getContacts("test", "test");

    }
}
