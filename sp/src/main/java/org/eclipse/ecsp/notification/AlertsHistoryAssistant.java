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

import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.notification.dao.AlertsHistoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_CAMPAIGN_ID;

/**
 * AlertsHistoryAssistant class for alerthistory CRUD operation.
 */
@Component
public class AlertsHistoryAssistant {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertsHistoryAssistant.class);
    private String alertsColln;
    private String userAlertsColln;
    private final AlertsHistoryDao historyDao;

    /**
     * Constructor for AlertsHistoryAssistant.
     *
     * @param historyDao the given history dao
     */
    @Autowired
    public AlertsHistoryAssistant(AlertsHistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    /**
     * Initializing the collection names.
     *
     * @param alertsColumn      alerts collection name
     * @param userAlertsColumn  user alerts collection name
     */
    void init(String alertsColumn, String userAlertsColumn) {
        this.alertsColln = alertsColumn;
        this.userAlertsColln = userAlertsColumn;
    }

    /**
     * Setting Alert History Object with additional data that needs to be enriched (e.g. user-profile).
     *
     * @param alert alert info
     */
    AlertsHistoryInfo setEnrichedAlertHistory(AlertsInfo alert, AlertsHistoryInfo alertsHistory) {
        setBasicAlertHistory(alert, alertsHistory);
        String userId =
            alert.getAlertsData().getUserProfile() != null ? alert.getAlertsData().getUserProfile().getUserId() : "";
        alertsHistory.setUserId(userId);
        String group = alert.getNotificationConfig() != null ? alert.getNotificationConfig().getGroup() : "";
        alertsHistory.setGroup(group);
        String notificationLongName =
            alert.getNotificationTemplate() != null ? alert.getNotificationTemplate().getNotificationLongName() : "";
        alertsHistory.setNotificationLongName(notificationLongName);
        alertsHistory.setNotificationId(alert.getAlertsData().getNotificationId());
        return alertsHistory;
    }

    /**
     * Setting Alert History Object with basic data that dose not need to be enriched.
     * NOTE: If the History already exists from prev logic (like scheduling) it will be used as a base
     *
     * @param alert alertinfo
     */
    AlertsHistoryInfo createBasicAlertHistory(AlertsInfo alert) {
        Optional<AlertsHistoryInfo> alertsHistoryInfo = Optional.ofNullable(historyDao.findById(
            alert.getIgniteEvent().getRequestId()));
        return setBasicAlertHistory(alert, alertsHistoryInfo.orElse(new AlertsHistoryInfo()));
    }

    /**
     * Setting Alert History Object with basic data that dose not need to be enriched.
     *
     * @param alert alert info
     */
    private AlertsHistoryInfo setBasicAlertHistory(AlertsInfo alert, AlertsHistoryInfo alertsHistory) {
        String id = alert.getIgniteEvent().getRequestId();
        alertsHistory.setId(StringUtils.isEmpty(id) ? UUID.randomUUID().toString() : id);
        alertsHistory.setCreateDts(LocalDateTime.now());
        alertsHistory.setPdid(alert.getPdid());
        alertsHistory.setAlertType(alert.getEventID());
        alertsHistory.setTimestamp(alert.getTimestamp());
        alertsHistory.setPayload(alert);
        if (!ObjectUtils.isEmpty(alert.getAlertsData().getAlertDataProperties().get(NOTIFICATION_CAMPAIGN_ID))) {
            alertsHistory.setCampaignId(
                (String) alert.getAlertsData().getAlertDataProperties().get(NOTIFICATION_CAMPAIGN_ID));
        }
        return alertsHistory;
    }

    /**
     * Save alert history.
     *
     * @param userId          user id
     * @param pdid            pdid
     * @param alert           alert info
     * @param alertHistoryObj alert history object
     */
    void saveAlertHistory(String userId, String pdid, AlertsInfo alert, AlertsHistoryInfo alertHistoryObj) {
        String collection;
        if (userId != null && EventID.PIN_GENERATED.equals(alert.getEventID())) {
            alertHistoryObj.setUserId(userId);
            collection = userAlertsColln;
        } else {
            alertHistoryObj.setPdid(pdid);
            collection = alertsColln;
        }
        LOGGER.debug("Alert history collection {}", collection);
        saveAlertHistory(alertHistoryObj);
    }

    /**
     * Save alert history.
     *
     * @param alertHistoryObj alert history object
     */
    private void saveAlertHistory(AlertsHistoryInfo alertHistoryObj) {
        LOGGER.debug("At saveAlertHistory, inserting alert history object {}", alertHistoryObj);
        historyDao.save(alertHistoryObj);
    }

    /**
     * Fetch alert history by id.
     *
     * @param id alert history id
     * @return alert history object
     */
    public AlertsHistoryInfo getAlertHistory(String id) {
        LOGGER.debug("Fetching alertHistory by id {}", id);
        return historyDao.findById(id);
    }
}
