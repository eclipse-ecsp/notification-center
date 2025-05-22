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

import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GeoFenceFieldGetter11 class.
 */
public class GeoFenceFieldGetter11 implements NotificationIdGetter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoFenceFieldGetter11.class);

    /**
     * getNotificationId method.
     *
     * @param alert AlertsInfo
     * @return String
     */
    @Override
    public String getNotificationId(AlertsInfo alert) {
        String fieldName = null;
        String positionAttribValue =
                (String) alert.getAlertsData().any().get(EventMetadata.GeoFenceAttrs.POSITION.toString());
        if (StringUtils.isNotEmpty(positionAttribValue)) {
            /*
             * Example:
             *
             * 1)geo fence breach for mobile push :GeoFence_generic_out
             * :GeoFence_valet_push_out 2)How fieldName look when the vehicle is
             * back in the geo fence :GeoFence_generic_in
             * :GeoFence_valet_push_in
             *
             */
            String typeAttribute =
                    (String) alert.getAlertsData().any().get(EventMetadata.GeoFenceAttrs.TYPE.toString());
            StringBuilder data = new StringBuilder();
            if (StringUtils.isNotEmpty(typeAttribute) && EventMetadata.GeoFenceTypes.isSupportedType(typeAttribute)) {
                fieldName = data.append(alert.getEventID()).append(NotificationUtils.UNDERSCORE).append(typeAttribute)
                        .append(NotificationUtils.UNDERSCORE)
                        .append(positionAttribValue).toString();
            } else {
                LOGGER.error("Type attribute {} in the data field is null , empty or not supported", typeAttribute);
            }

        } else {
            LOGGER.error("Position attribute {} in the data field is null or empty", positionAttribValue);
        }
        return fieldName;
    }

}
