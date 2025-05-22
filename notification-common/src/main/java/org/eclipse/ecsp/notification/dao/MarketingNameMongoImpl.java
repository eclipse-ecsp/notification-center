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

import org.eclipse.ecsp.domain.notification.MarketingName;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.eclipse.ecsp.nosqldao.Operator.EQ;

/**
 * MarketingNameMongoImpl class.
 */
@Repository
public class MarketingNameMongoImpl extends IgniteBaseDAOMongoImpl<String, MarketingName> implements MarketingNameDao {

    static final Logger LOGGER = LoggerFactory.getLogger(MarketingNameMongoImpl.class);

    /**
     * find marketing names by vehicle make and model.
     *
     * @param make make
     *
     * @param model model
     *
     * @param isForFallback boolean
     *
     * @return marketingnames
     */
    public List<MarketingName> findByMakeAndModel(String make, String model, boolean isForFallback) {
        LOGGER.debug("finding marketing name for brand {} model {} ", make, model);
        IgniteCriteria c1 = new IgniteCriteria("brandName", EQ, make);
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c1).and(new IgniteCriteria("model", EQ, model));
        IgniteQuery query = new IgniteQuery(cg);
        if (isForFallback) {
            IgniteCriteriaGroup cg1 = new IgniteCriteriaGroup(c1).and(new IgniteCriteria("model", EQ, null));
            query.or(cg1);
        }
        return super.find(query);
    }

}
