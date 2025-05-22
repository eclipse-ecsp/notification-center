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

package org.eclipse.ecsp.notification;

import org.eclipse.ecsp.domain.notification.SuppressionConfig;
import org.eclipse.ecsp.notification.utils.TimeCalculator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertFalse;

/**
 * VehicleInfoNotificationParametriedUnitTest class.
 */
@RunWith(Parameterized.class)
public class VehicleInfoNotificationParametriedUnitTest {


    public static final int ELEVEN = 11;
    public static final int TWENTYONE = 21;
    public static final int TWENTYTHREE = 23;
    public static final int ONE = 1;
    public static final int TWENTYNINTEEN = 2019;
    private final int day;
    private final int hour;

    /**
     * VehicleInfoNotificationParametriedUnitTest constructor.
     *
     * @param day int
     * @param hour int
     */
    public VehicleInfoNotificationParametriedUnitTest(int day, int hour) {
        this.day = day;
        this.hour = hour;
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {ELEVEN, TWENTYONE},
                {ONE, TWENTYONE},
                {ELEVEN, TWENTYTHREE},

        });

    }

    /**
     * suppression case: VACATION - The suppression day is 1/1/2019 - 10/1/2019, 22:00 - 05:00
     * the current date time is 11/1/2019, 21:00
     * date time is not in interval so method should return false.
     */
    @Test
    public void dateTimeNotInInterval() {
        String vacation = SuppressionConfig.SuppressionType.VACATION.toString();
        SuppressionConfig suppressionConfig = new SuppressionConfig(vacation,
                "22:00", "05:00", "2019-01-01", "2019-01-10", Collections.emptyList());

        // Date: 11/1/2019 21:00
        LocalDateTime current = LocalDateTime.of(TWENTYNINTEEN, ONE, day, hour, 0);
        assertFalse(TimeCalculator.currentDateTimeIsInInterval(suppressionConfig, current));
    }
}
