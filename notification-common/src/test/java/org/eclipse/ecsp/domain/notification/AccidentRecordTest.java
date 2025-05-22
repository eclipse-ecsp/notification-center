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

package org.eclipse.ecsp.domain.notification;

import org.junit.Test;

/**
 * AccidentRecordTest class.
 */
public class AccidentRecordTest {

    AccidentRecord accidentRecord = new AccidentRecord();

    private final String twentyTwo = "22";

    AlertsInfo alertsInfo = new AlertsInfo();
    AlertsInfo.Data data2 = new AlertsInfo.Data();

    /**
     * setAlertInfoException test method.
     *
     * @return void.
     */
    public AlertsInfo setAlertInfoException() {
        alertsInfo.setEventID("XYZ");
        alertsInfo.setPdid("dsadasd");
        return alertsInfo;

    }

    /**
     * setAlertInfo test method.
     *
     * @return void.
     */
    public AlertsInfo setAlertInfo() {
        alertsInfo.setEventID("Collision");
        data2.set("speed", twentyTwo);
        data2.set("latitude", twentyTwo);
        data2.set("longitude", twentyTwo);
        alertsInfo.setPdid("dsadasd");
        alertsInfo.setAlertsData(data2);
        return alertsInfo;
    }

    @Test(expected = Exception.class)
    public void testsetAletInfExcep() {
        AccidentRecord accidentRecord = new AccidentRecord(setAlertInfoException());
    }

    @Test
    public void testsetAletInf() {
        AccidentRecord accidentRecord = new AccidentRecord(setAlertInfo());
    }

    @Test
    public void testgetPdId() {
        accidentRecord.getPdId();
    }

    @Test
    public void testsetPdId() {
        accidentRecord.setPdId("test");
    }

    @Test
    public void testisEventDateSysGenerated() {
        accidentRecord.isEventDateSysGenerated();
    }

    @Test
    public void testsetEventDateSysGenerated() {
        accidentRecord.setEventDateSysGenerated(false);
    }

    @Test
    public void testgetLocation() {
        accidentRecord.getLocation();
    }

    @Test
    public void testsetLocation() {
        accidentRecord.setLocation(null);
    }

    @Test
    public void testgetRemainingSizeInBytes() {
        accidentRecord.getRemainingSizeInBytes();
    }

    @Test
    public void testsetRemainingSizeInBytes() {
        accidentRecord.setRemainingSizeInBytes(0);
    }

    @Test
    public void testgetEventDate() {
        accidentRecord.getEventDate();
    }

    @Test
    public void testsetEventDate() {
        accidentRecord.setEventDate(0);
    }

    @Test
    public void testgetLastKnownSpeed() {
        accidentRecord.getLastKnownSpeed();
    }

    @Test
    public void setLastKnownSpeed() {
        accidentRecord.setLastKnownSpeed(Double.valueOf(Integer.valueOf(twentyTwo)));
    }

    AccidentRecord.Location data = new AccidentRecord.Location(
            Double.valueOf(Integer.valueOf(twentyTwo)),
            Double.valueOf(Integer.valueOf(twentyTwo)));

    @Test
    public void testgetLatitude() {
        data.getLatitude();
    }

    @Test
    public void testsetLatitude() {
        data.setLatitude(Double.valueOf(twentyTwo));
    }

    @Test
    public void testgetLongitude() {
        data.getLongitude();
    }

    @Test
    public void testsetLongitude() {
        data.setLongitude(Double.valueOf(Integer.valueOf(twentyTwo)));
    }

    @Test
    public void testtoString() {
        data.toString();
    }

    @Test
    public void toStringtest() {

        accidentRecord.toString();
    }

}
