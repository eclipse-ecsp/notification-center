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

import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.EventMetadata.CurfewEventVersions;

/**
 * CurfewViolationFactory class.
 */
public class CurfewViolationFactory {
    private CurfewViolationFactory() {
    }

    /**
     * getField method.
     *
     * @param alert AlertsInfo
     * @return String
     */
    public static String getField(AlertsInfo alert) {
        CurfewEventVersions version = CurfewEventVersions.getVersion(alert.getVersion());
        switch (version) {
            case VER1_0:
                return new CurfewViolationFieldGetter10().getNotificationId(alert);

            case VER1_1:
                return new CurfewViolationFieldGetter11().getNotificationId(alert);

            default:
                return null;

        }


    }

    /**
     * createCurfewViolationFactory method.
     *
     * @return CurfewViolationFactory
     */
    public static CurfewViolationFactory createCurfewViolationFactory() {
        return new CurfewViolationFactory();
    }
}
