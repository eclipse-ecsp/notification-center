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

package org.eclipse.ecsp.platform.notification.marketing;

import org.eclipse.ecsp.domain.notification.MarketingName;
import org.eclipse.ecsp.notification.dao.MarketingNameDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * MarketingService class.
 */
@Service
public class MarketingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketingService.class);

    @Autowired
    private MarketingNameDao marketingNameDao;

    /**
     * Update marketing names.
     *
     * @param marketingNameList the marketing name list
     * @return the boolean
     */
    boolean updateMarketingName(List<MarketingName> marketingNameList) {
        boolean isUpdated = false;
        try {
            marketingNameList.forEach(this::creatMarketingName);
            isUpdated = true;
        } catch (Exception e) {
            LOGGER.error("Error while patching marketing Names data ", e);
        }
        return isUpdated;
    }

    /**
     * Creat marketing name.
     *
     * @param marketingName the marketing name
     */
    private void creatMarketingName(MarketingName marketingName) {
        LOGGER.debug("Upserting marketing name for brandName {} model {} ", marketingName.getBrandName(),
            marketingName.getModel());
        String model = (!StringUtils.isEmpty(marketingName.getModel())) ? marketingName.getModel().toLowerCase() : null;
        marketingName.setModel(model);
        String brand = marketingName.getBrandName().toLowerCase();
        marketingName.setBrandName(brand);
        List<MarketingName> marketingNameList =
            marketingNameDao.findByMakeAndModel(marketingName.getBrandName(), marketingName.getModel(), false);
        if (CollectionUtils.isEmpty(marketingNameList)) {
            marketingName.setId(UUID.randomUUID().toString());
            marketingNameDao.save(marketingName);
        } else {
            marketingNameList.get(0).setMarketingName(marketingName.getMarketingName());
            marketingNameDao.update(marketingNameList.get(0));
        }

    }
}
