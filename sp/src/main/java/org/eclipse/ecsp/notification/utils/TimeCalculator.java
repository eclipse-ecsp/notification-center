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

package org.eclipse.ecsp.notification.utils;

import org.eclipse.ecsp.domain.notification.SuppressionConfig;

import java.time.LocalDateTime;

/**
 * TimeCalculator class.
 */
public class TimeCalculator {
    private TimeCalculator() {
    }

    // returns a boolean if the given LocalDateTime object is between the start
    // LocalDateTime and the end LocalDateTime. builds the start & end
    // LocalDateTime objects for every day in the List of DayOfWeek in suppression
    // configuration. handles the case of overnight recurring quite times.


    /**
     * currentDateTimeIsInRecurringInterval method.
     *
     * @param suppression SuppressionConfig
     * @param now         LocalDateTime
     * @return boolean
     */
    public static boolean currentDateTimeIsInRecurringInterval(SuppressionConfig suppression, LocalDateTime now) {
        return !(suppression.getDays().stream().filter(day -> {
            LocalDateTime startDateTime = now.with(day).with(suppression.getStartTime());
            LocalDateTime endDateTime = now.with(day).with(suppression.getEndTime());
            boolean isOverNight = suppression.getStartTime().isAfter(suppression.getEndTime());
            if (isOverNight) {
                endDateTime = now.with(day).plusDays(1).with(suppression.getEndTime());
            }

            return now.isAfter(startDateTime) && now.isBefore(endDateTime);
        }).toList().isEmpty());
    }

    /**
     * returns a boolean if the given LocalDateTime object is between the start
     * LocalDateTime and the end LocalDateTime. builds the start and end
     * LocalDateTime objects according to the suppression configuration. More
     * specificity - according to the LocalTime and DateTime objects.
     */
    public static boolean currentDateTimeIsInInterval(SuppressionConfig suppression, LocalDateTime now) {
        LocalDateTime startDateTime = LocalDateTime.of(suppression.getStartDate(), suppression.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(suppression.getEndDate(), suppression.getEndTime());
        return now.isAfter(startDateTime) && now.isBefore(endDateTime);
    }

    /**
     * Returns TimeCalculator object.
     *
     * @return TimeCalculator
     */
    public static TimeCalculator createTimeCalculator() {
        return new TimeCalculator();
    }
}
