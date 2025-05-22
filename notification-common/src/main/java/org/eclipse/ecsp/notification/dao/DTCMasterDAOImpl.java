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

package org.eclipse.ecsp.notification.dao;

import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.notification.entities.DTCMaster;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * DTCMasterDAOImple class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class DTCMasterDAOImpl extends IgniteBaseDAOMongoImpl<String, DTCMaster> implements DTCMasterDAO {

    /**
     * Find DTCMaster by id.
     *
     * @param id id
     * @return DTCMaster
     */
    @Override
    public List<DTCMaster> findByIdIn(Collection<String> id) {
        String[] ids = id.toArray(new String[id.size()]);
        return findByIds(ids);
    }

}
