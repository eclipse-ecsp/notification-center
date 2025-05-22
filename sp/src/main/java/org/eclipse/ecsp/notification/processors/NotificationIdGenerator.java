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
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.adaptor.NotificationUtils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * NotificationIdGenerator class.
 */
@Component
@Order(1)
public class NotificationIdGenerator implements NotificationProcessor {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationIdGenerator.class);

    /**
     * process method.
     *
     * @param alertsInfo AlertsInfo
     */
    @Override
    public void process(AlertsInfo alertsInfo) {
        Data alertsData = alertsInfo.getAlertsData();
        String notificationId = alertsInfo.getAlertsData().getNotificationId();
        if (StringUtils.isNotEmpty(notificationId)) {
            return;
        }
        alertsData.setNotificationId(getTemplateKey(alertsInfo));
    }

    /**
     * getTemplateKey method.
     *
     * @param alert AlertsInfo
     * @return String
     */
    private String getTemplateKey(AlertsInfo alert) {

        String fieldName = null;
        String eventId = alert.getEventID();

        if (eventId.equals(EventMetadata.EventID.DTC_STORED.toString())) {

            fieldName = getDtcAttr(alert, fieldName, eventId);

        } else if (eventId.equals(EventMetadata.EventID.GEOFENCE.toString())) {
            fieldName = GeoFenceFieldFactory.getField(alert);
        } else if (eventId.equals(EventMetadata.EventID.CURFEW.toString())) {
            fieldName = CurfewViolationFactory.getField(alert);
        } else if (eventId.equals(EventMetadata.EventID.DONGLE_STATUS.toString())) {
            fieldName = getDongleStatusAttr(alert, fieldName);

            /*
             * Service reminder alerts are of two types :1) Odometer based 2) Data
             * based and for both the messages are different
             *
             * For Odometer based service reminder, the mongo fieldname would be
             * ServiceReminder_OdometerReading_push (Similar for email, browser,
             * sms)
             *
             * For Date based service reminder, the mongo field name would be
             * ServiceReminder_ReminderEngine (Similar for email, browser, sms)
             */
        }    else if (eventId.equals(EventMetadata.EventID.SERVICE_REMINDER.toString())) {

            fieldName = getServiceReminder(alert, fieldName, eventId);
        } else if (eventId.equals(EventMetadata.EventID.GENERIC_NOTIFICATION_EVENT.toString())
                || eventId.equals(EventID.VEHICLE_MESSAGE_PUBLISH)
                || eventId.equals(EventMetadata.EventID.PROMOTIONAL_NOTIFICATIONS.toString())) {

            fieldName = (String) alert.getAlertsData().any().get(NotificationConstants.NOTIFICATION_ID);
            LOGGER.info("EventId: {}, NotificationId: {}", eventId, fieldName);
        } else {
            fieldName = eventId;
        }
        LOGGER.debug("Mongo field name returned for event:{} is {}", eventId, fieldName);
        return fieldName;

    }

    /**
     * getServiceReminder method.
     *
     * @param alert    AlertsInfo
     * @param fieldName String
     * @param eventId  String
     * @return String
     */
    private String getServiceReminder(AlertsInfo alert, String fieldName, String eventId) {
        String source =
                (String) alert.getAlertsData().any().get(EventMetadata.ServiceReminderAttr.SOURCE.toString());
        if (StringUtils.isNotEmpty(source)) {
            fieldName = eventId + NotificationUtils.UNDERSCORE + source;
        } else {
            LOGGER.error("Data.Source field is null or empty for service reminder alert:{}", alert);
        }
        return fieldName;
    }

    /**
     * GeoFenceFieldFactory class.
     */
    private String getDongleStatusAttr(AlertsInfo alert, String fieldName) {
        if (null != alert.getAlertsData().any().get(EventMetadata.DongleStatusAttr.STATUS.toString())) {
            String statusAttribute =
                    (String) alert.getAlertsData().any().get(EventMetadata.DongleStatusAttr.STATUS.toString());
            if (StringUtils.isNotEmpty(statusAttribute)) {
                StringBuilder data = new StringBuilder();
                fieldName = data.append(alert.getEventID())
                        .append(NotificationUtils.UNDERSCORE)
                        .append(statusAttribute).toString();
            }
        }
        return fieldName;
    }

    /**
     * GeoFenceFieldFactory class.
     */
    private String getDtcAttr(AlertsInfo alert, String fieldName, String eventId) {
        if (null != alert.getAlertsData().any().get(EventMetadata.DTCAttrs.SET.toString())) {

            fieldName = eventId + NotificationUtils.UNDERSCORE + EventMetadata.DTCAttrs.SET.toString();
        } else if (null != alert.getAlertsData().any().get(EventMetadata.DTCAttrs.CLEARED.toString())) {
            fieldName = eventId + NotificationUtils.UNDERSCORE + EventMetadata.DTCAttrs.CLEARED.toString();
        }
        return fieldName;
    }

}
