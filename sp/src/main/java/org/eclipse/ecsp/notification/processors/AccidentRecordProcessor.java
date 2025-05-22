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

package org.eclipse.ecsp.notification.processors;

import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.metrics.reporter.CumulativeLogger;
import org.eclipse.ecsp.domain.notification.AccidentRecord;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.EventMetadata.EventID;
import org.eclipse.ecsp.notification.dao.AccidentRecordDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * AccidentRecordProcessor class.
 */
@Component
@Order(8)
public class AccidentRecordProcessor implements NotificationProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccidentRecordProcessor.class);
    private static final CumulativeLogger CLOGGER = CumulativeLogger.getLogger();
    private static final String ACCIDENT_RECORD = "AccidentRecord";
    private boolean logPerPdid;

    @Autowired
    private AccidentRecordDAO accidentRecordDao;

    /**
     * Public constructor.
     *
     * @param properties Properties
     */
    public AccidentRecordProcessor(Properties properties) {
        logPerPdid = Boolean.parseBoolean(properties.getProperty(PropertyNames.LOG_PER_PDID, "false"));
    }

    /**
     * Process method.
     *
     * @param alert AlertsInfo
     */
    @Override
    public void process(AlertsInfo alert) {
        if (EventID.COLLISION.toString().equals(alert.getEventID())) {
            AccidentRecord accidentRecord = new AccidentRecord(alert);
            persistAccidentRecord(accidentRecord);
        }
    }

    /**
     * Persist accident record.
     *
     * @param accidentRecord AccidentRecord
     */
    private void persistAccidentRecord(AccidentRecord accidentRecord) {
        accidentRecordDao.save(accidentRecord);
        CLOGGER.incrementByOne(ACCIDENT_RECORD);
        if (logPerPdid) {
            CLOGGER.incrementByOne(String.format("%s %s", ACCIDENT_RECORD, accidentRecord.getPdId()));
        }

        LOGGER.debug("Persisted accident record {}", accidentRecord);
    }

    boolean isLogPerPdid() {
        return logPerPdid;
    }
}
