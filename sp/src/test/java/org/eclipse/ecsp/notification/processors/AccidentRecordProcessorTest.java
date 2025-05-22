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
import org.eclipse.ecsp.domain.notification.AccidentRecord;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.notification.dao.AccidentRecordDAO;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

/**
 * AccidentRecordProcessorTest class.
 */
public class AccidentRecordProcessorTest {

    private Properties properties1;
    private Properties properties2;

    {
        properties1 = new Properties();
        properties2 = new Properties();
        properties2.put(PropertyNames.LOG_PER_PDID, "true");
    }

    @InjectMocks
    AccidentRecordProcessor accidentRecordProcessor = new AccidentRecordProcessor(properties1);

    @InjectMocks
    AccidentRecordProcessor accidentRecordProcessor2 = new AccidentRecordProcessor(properties2);

    @Mock
    AccidentRecordDAO accidentRecordDao;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void accidentRecordProcessorLogPerPdIdTrue() {
        Properties properties = new Properties();
        properties.put(PropertyNames.LOG_PER_PDID, "true");
        AccidentRecordProcessor accidentRecordProcessor = new AccidentRecordProcessor(properties);
        assertTrue(accidentRecordProcessor.isLogPerPdid());
    }

    @Test
    public void accidentRecordProcessorLogPerPdIdFalse() {
        Properties properties = new Properties();
        AccidentRecordProcessor accidentRecordProcessor = new AccidentRecordProcessor(properties);
        assertFalse(accidentRecordProcessor.isLogPerPdid());
    }

    @Test
    public void processNotCollision() {

        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.CURFEW.toString());
        Assertions.assertNotNull(alertsInfo);
        accidentRecordProcessor.process(alertsInfo);
    }

    @Test
    public void processLogPerPdIdFalse() {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.COLLISION.toString());
        AccidentRecord accidentRecord = new AccidentRecord(alertsInfo);

        Mockito.doReturn(accidentRecord).when(accidentRecordDao).save(any());
        accidentRecordProcessor.process(alertsInfo);
        Mockito.verify(accidentRecordDao, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void processLogPerPdIdTrue() {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(EventMetadata.EventID.COLLISION.toString());
        AccidentRecord accidentRecord = new AccidentRecord(alertsInfo);

        Mockito.doReturn(accidentRecord).when(accidentRecordDao).save(any());
        accidentRecordProcessor2.process(alertsInfo);
        Mockito.verify(accidentRecordDao, Mockito.times(1)).save(Mockito.any());
    }
}