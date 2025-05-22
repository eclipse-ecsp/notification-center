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

import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.notification.dao.UserManagementToUserProfileDAO;
import org.junit.Test;

/**
 * UserManagementToUserProfileDaoTest class.
 */
public class UserManagementToUserProfileDaoTest {

    UserManagementToUserProfileDAO usmdao = new UserManagementToUserProfileDAO();


    @Test
    public void testinit() {
        usmdao.init();
    }


    @Test(expected = RuntimeException.class)
    public void testupdateNickName() {
        usmdao.updateNickName("test1", "test1", "test1");
    }

    @Test(expected = RuntimeException.class)
    public void testupdateConsent() {
        usmdao.updateConsent("test1", false);
    }

    @Test(expected = RuntimeException.class)
    public void testremoveNickNames() {
        usmdao.removeNickNames("test1", "test2");
    }


    @Test
    public void testfindById() {
        usmdao.findById(null);
    }

    //region ...not implemented methods from interface

    @Test(expected = RuntimeException.class)
    public void testsave() {
        usmdao.save(null);
    }

    @Test(expected = RuntimeException.class)
    public void testsaveAll() {
        usmdao.saveAll(null);
    }

    @Test(expected = RuntimeException.class)
    public void testfindByIds() {
        usmdao.findByIds(null);
    }

    @Test(expected = RuntimeException.class)
    public void testfindAll() {
        usmdao.findAll();
    }

    @Test(expected = RuntimeException.class)
    public void testfind() {
        usmdao.find(null);
    }

    @Test(expected = RuntimeException.class)
    public void testfindWithPagingInfo() {
        usmdao.findWithPagingInfo(null);
    }

    @Test(expected = RuntimeException.class)
    public void testdeleteById() {
        usmdao.deleteById(null);
    }

    @Test(expected = RuntimeException.class)
    public void testdeleteByIds() {
        usmdao.deleteByIds(null);
    }

    @Test(expected = RuntimeException.class)
    public void testdeleteByQuery() {
        usmdao.deleteByQuery(null);
    }

    @Test(expected = RuntimeException.class)
    public void testdeleteAll() {
        usmdao.deleteAll();
    }

    @Test(expected = RuntimeException.class)
    public void testupdate() {
        usmdao.update(null);
    }

    @Test(expected = RuntimeException.class)
    public void testupdateAll() {
        usmdao.updateAll(null);
    }

    @Test(expected = RuntimeException.class)
    public void testupdate2() {
        usmdao.update("a", null);
    }

    @Test(expected = RuntimeException.class)
    public void testupdate3() {
        usmdao.update(new IgniteQuery(), null);
    }

    @Test(expected = RuntimeException.class)
    public void testupsert() {
        usmdao.upsert(null, null);
    }

    @Test(expected = RuntimeException.class)
    public void testremoveAll() {
        usmdao.removeAll(null, null);
    }

    @Test(expected = RuntimeException.class)
    public void testdelete() {
        usmdao.delete(null);
    }

    @Test(expected = RuntimeException.class)
    public void testdistinct() {
        usmdao.distinct(null, null);
    }

    @Test(expected = RuntimeException.class)
    public void testcountByQuery() {
        usmdao.countByQuery(null);
    }

    @Test(expected = RuntimeException.class)
    public void teststreamFind() {
        usmdao.streamFind(null);
    }

    @Test(expected = RuntimeException.class)
    public void testcountAll() {
        usmdao.countAll();
    }

    @Test(expected = RuntimeException.class)
    public void teststreamFindAll() {
        usmdao.streamFindAll();
    }
}
