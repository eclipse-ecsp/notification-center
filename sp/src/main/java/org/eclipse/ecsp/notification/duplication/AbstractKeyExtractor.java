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

package org.eclipse.ecsp.notification.duplication;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import scala.collection.mutable.StringBuilder;

/**
 * AbstractKeyExtractor class.
 */
public abstract class AbstractKeyExtractor implements KeyExtractor {

    private static double TWO = 2.0;

    private int interval;

    /**
     * AbstractKeyExtractor constructor.
     *
     * @param interval int
     */
    AbstractKeyExtractor(int interval) {
        if (interval < 0) {
            throw new IllegalArgumentException("Interval must be positive number");
        }
        if (interval == 0) {
            this.interval = 0;
        } else {
            this.interval = (int) Math.ceil(interval / TWO);
        }
    }

    /**
     * Method to extract current key.
     *
     * @param alertInfo AlertsInfo
     * @return String
     */
    @Override
    public String extractCurrentKey(AlertsInfo alertInfo) {
        StringBuilder key = new StringBuilder();
        key.append(KeyExtractor.DEDUP_KEY_PREFIX);
        key.append(getKeyCommon(alertInfo));
        key.append(UNDERSCORE);
        key.append(getCurrentTsKey(alertInfo.getTimestamp()));
        String dataQualifier = getDataQualifer(alertInfo.getAlertsData());
        if (StringUtils.isNotEmpty(dataQualifier)) {
            key.append(UNDERSCORE);
            key.append(dataQualifier);
        }
        return key.toString();
    }

    /**
     * Method to extract previous key.
     *
     * @param alertInfo AlertsInfo
     * @param hops      int
     * @return String
     */
    @Override
    public String extractPreviousKey(AlertsInfo alertInfo, int hops) {
        StringBuilder key = new StringBuilder();
        key.append(KeyExtractor.DEDUP_KEY_PREFIX);
        key.append(getKeyCommon(alertInfo));
        key.append(UNDERSCORE);
        key.append(getPreviousTsKey(alertInfo.getTimestamp(), hops));
        String dataQualifier = getDataQualifer(alertInfo.getAlertsData());
        if (StringUtils.isNotEmpty(dataQualifier)) {
            key.append(UNDERSCORE);
            key.append(dataQualifier);
        }
        return key.toString();
    }

    /**
     * Method to get data qualifier.
     *
     * @param alertsData Data
     * @return String
     */
    protected abstract String getDataQualifer(Data alertsData);

    /**
     * Method to get key common.
     *
     * @param alertInfo AlertsInfo
     * @return String
     */
    private String getKeyCommon(AlertsInfo alertInfo) {
        StringBuilder key = new StringBuilder();
        key.append(alertInfo.getPdid());
        key.append(UNDERSCORE);
        key.append(alertInfo.getEventID());
        return key.toString();
    }

    /**
     * Method to get current ts key.
     *
     * @param ts long
     * @return long
     */
    private long getCurrentTsKey(long ts) {
        if (interval == 0) {
            return ts;
        }
        return ts - (ts % interval);
    }

    /**
     * Method to get previous ts key.
     *
     * @param ts   long
     * @param hops int
     * @return long
     */
    private long getPreviousTsKey(long ts, int hops) {
        long finalTs = getCurrentTsKey(ts);
        for (int i = 1; i <= hops; i++) {
            finalTs = finalTs - interval;
        }
        return finalTs;
    }

}
