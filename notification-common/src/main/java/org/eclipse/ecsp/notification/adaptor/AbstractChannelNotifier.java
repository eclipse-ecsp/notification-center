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

package org.eclipse.ecsp.notification.adaptor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.ecsp.domain.notification.AlertEventData;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;

/**
 * AbstractChannelNotifier abstract class.
 */
public abstract class AbstractChannelNotifier implements ChannelNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChannelNotifier.class);
    /**
     * Metric Registry for registering our own metrics.
     */
    private static final String TOTAL_SMS_SENT = "TotalMessageSent";
    private static final String TOTAL_GEO_FENCE_ALERT_SENT = "TotalGeoFenceAlert";
    private static final String TOTAL_DTC_ALERTS_SENT = "TotalDTCAlert";
    private static final String TOTAL_LOW_FUEL_ALERTS_SENT = "TotalLowFuelAlertsSent";
    private static final String TOTAL_OVER_SPEED_ALERTS_SENT = "TotalOverSpeedAlertsSent";
    private static final String TOTAL_CURFEW_ALERTS_SENT = "TotalCurfewAlertsSent";
    private static final String TOTAL_IDLE_ALERTS_SENT = "TotalIdleAlertsSent";
    private static final String TOTAL_TOW_ALERTS_SENT = "TotalTowAlertsSent";
    private static final String TOTAL_DONGLE_STATUS_ALERTS_SENT = "TotalDongleStatusAlertsSent";
    private static final String GEOFENCE_METRIC_ENABLE = "metrics.geofence.count.enable";
    private static final String SMS_METRIC_ENABLE = "metrics.sms.count.enable";
    private static final  String DTC_METRIC_ENABLE = "metrics.dtc.counts.enable";
    private static final String LOW_FUEL_METRIC_ENABLE = "metrics.lowfuel.count.enable";
    private static final String OVER_SPEED_METRIC_ENABLE = "metrics.overspeed.count.enable";
    private static final String CURFEW_METRIC_ENABLE = "metrics.curfew.count.enable";
    private static final String IDLING_METRIC_ENABLE = "metrics.idling.count.enable";
    private static final String TOW_METRIC_ENABLE = "metrics.tow.count.enable";
    private static final String DONGLE_STATUS_METRIC_ENABLE = "metrics.dongle.count.enable";
    /**
     * Metric already registered.
     */
    public static final String METRIC_ALREADY_REGISTERED = "Metric {} already registered.";
    // idFieldName value will be "_id" if dao is mongo and value will be "id" if
    // dao
    // is dynamo
    /**
     * Id field name.
     */
    protected String idFieldName;
    /**
     * Notification dao.
     */
    protected NotificationDao notificationDao;
    private Properties properties;
    private MetricRegistry metricRegistry;
    private long totalGeoFenceNotificationPushed;
    private long totalDtcNotificationsPushed;
    private long totalLowFuelAlertsNotificationPushed;
    private long totalSmsSent;
    private long totalOverSpeedNotificationPushed;
    private long totalCurfewAlertPushed;
    private long totalIdleAlertPushed;
    private long totalTowAlertPushed;
    private long totalDongleStatusAlertPushed;

    /**
     * Constructor.
     */
    protected AbstractChannelNotifier() {

    }

    /**
     * Method to initialize the properties, metric registry and notification dao.
     *
     * @param properties       Properties
     * @param metricRegistry    MetricRegistry
     * @param notificationDao NotificationDao
     */
    @Override
    public void init(Properties properties, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        this.properties = properties;
        this.metricRegistry = metricRegistry;
        this.notificationDao = notificationDao;
        idFieldName = this.notificationDao.getIdField();
        registerMetrics();
    }

    /**
     * Method to publish the alert.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    public final ChannelResponse publish(AlertsInfo alert) {
        ChannelResponse response = doPublish(alert);
        incrementMetricValue(alert.getEventID());
        return response;
    }

    /**
     * Method to validate the user id.
     *
     * @param channelResponse ChannelResponse
     * @param userId          String
     * @param alertsInfo      AlertsInfo
     * @return boolean
     */
    protected boolean validateUserId(ChannelResponse channelResponse, String userId, AlertsInfo alertsInfo) {
        if (userId == null
                &&
                !EventID.NON_REGISTERED_USER_NOTIFICATION_EVENT.equals(alertsInfo.getIgniteEvent().getEventId())) {
            LOGGER.error("Failed sending notification -> no user");
            channelResponse.setStatus(NOTIFICATION_STATUS_FAILURE);
            return false;
        }
        return true;
    }

    /**
     * Method to Publish the notification.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    protected abstract ChannelResponse doPublish(AlertsInfo alert);

    /**
     * Method to register the metrics.
     */
    private void registerMetrics() {
        registerTotalSmsSentMetric();
        registerTotalGeofencePublishedMetric();
        registerTotalDtcSentMetric();
        registerLowFuelAlertSenttMetric();
        registerOverSpeedAlertSenttMetric();
        registerCurfewAlertSenttMetric();
        registerIdlingAlertSenttMetric();
        registerTowAlertSenttMetric();
        registerDongleStatusAlertSentMetric();

    }

    /**
     * Method to register total geo fence alert sent metric.
     */
    private void registerTotalGeofencePublishedMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(GEOFENCE_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total geo fence alert sent");

            Gauge<Long> geoFence = () -> totalGeoFenceNotificationPushed;
            try {
                getMetricRegistry().register(TOTAL_GEO_FENCE_ALERT_SENT, geoFence);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_GEO_FENCE_ALERT_SENT);
            }

        }

    }

    /**
     * Method to register total sms sent metric.
     */
    private void registerTotalSmsSentMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(SMS_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total sms sent");

            Gauge<Long> sms = () -> totalSmsSent;

            try {
                getMetricRegistry().register(TOTAL_SMS_SENT, sms);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_SMS_SENT);
            }

        }
    }

    /**
     * Method to register total dtc alert sent metric.
     */
    private void registerTotalDtcSentMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(DTC_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total DTC alert sent");

            Gauge<Long> dtc = () -> totalDtcNotificationsPushed;

            try {
                getMetricRegistry().register(TOTAL_DTC_ALERTS_SENT, dtc);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_DTC_ALERTS_SENT);
            }
        }

    }

    /**
     * Method to register low fuel alert sent metric.
     */
    private void registerLowFuelAlertSenttMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(LOW_FUEL_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total low fuel  sent");

            Gauge<Long> lowFuel = () -> totalLowFuelAlertsNotificationPushed;

            try {
                getMetricRegistry().register(TOTAL_LOW_FUEL_ALERTS_SENT, lowFuel);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_LOW_FUEL_ALERTS_SENT);
            }
        }

    }

    /**
     * Method to get the properties.
     *
     * @return Properties
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Method to get the metric registry.
     *
     * @return MetricRegistry
     */
    public MetricRegistry getMetricRegistry() {
        return this.metricRegistry;
    }

    /**
     * Method to check register over speed alert.
     */
    private void registerOverSpeedAlertSenttMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(OVER_SPEED_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total over speed  sent");

            Gauge<Long> overSpeed = () -> totalOverSpeedNotificationPushed;

            try {
                getMetricRegistry().register(TOTAL_OVER_SPEED_ALERTS_SENT, overSpeed);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_OVER_SPEED_ALERTS_SENT);
            }
        }

    }

    /**
     * Method to check register curfew alert.
     */
    private void registerCurfewAlertSenttMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(CURFEW_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total curfew alert  sent");

            Gauge<Long> curfewAlert = () -> totalCurfewAlertPushed;

            try {
                getMetricRegistry().register(TOTAL_CURFEW_ALERTS_SENT, curfewAlert);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_CURFEW_ALERTS_SENT);
            }
        }

    }

    /**
     * Method to register number of idling alert sent.
     */
    private void registerIdlingAlertSenttMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(IDLING_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total idling alert  sent");

            Gauge<Long> idleAlert = () -> totalIdleAlertPushed;

            try {
                getMetricRegistry().register(TOTAL_IDLE_ALERTS_SENT, idleAlert);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_IDLE_ALERTS_SENT);
            }
        }

    }

    /**
     * Method to register total number of tow alerts sent .
     */
    private void registerTowAlertSenttMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(TOW_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total tow alert  sent");

            Gauge<Long> towAlert = () -> totalTowAlertPushed;

            try {
                getMetricRegistry().register(TOTAL_TOW_ALERTS_SENT, towAlert);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_TOW_ALERTS_SENT);
            }
        }

    }

    /**
     * Method to register dongle status alert.
     */
    private void registerDongleStatusAlertSentMetric() {
        if (Boolean.parseBoolean(getProperties().getProperty(DONGLE_STATUS_METRIC_ENABLE, "true"))) {
            LOGGER.info("Registering metrics to report total dongle status alert  sent");

            Gauge<Long> dongleAlert = () -> totalDongleStatusAlertPushed;

            try {
                getMetricRegistry().register(TOTAL_DONGLE_STATUS_ALERTS_SENT, dongleAlert);
            } catch (Exception e) {
                LOGGER.info(METRIC_ALREADY_REGISTERED, TOTAL_DONGLE_STATUS_ALERTS_SENT);
            }
        }

    }

    /**
     * Increment the value of the metric.
     */
    private void incrementMetricValue(String eventId) {

        if (eventId.equals(EventMetadata.EventID.COLLISION.toString())) {
            // increment the sms counter as we are going to send the sms
            totalSmsSent++;
        } else if (eventId.equals(EventMetadata.EventID.DTC_STORED.toString())) {
            // increment the dtc counter
            totalDtcNotificationsPushed++;
        } else if (eventId.equals(EventMetadata.EventID.GEOFENCE.toString())) {
            totalGeoFenceNotificationPushed++;
        } else if (eventId.equals(EventMetadata.EventID.LOW_FUEL.toString())) {
            totalLowFuelAlertsNotificationPushed++;
        } else if (eventId.equals(EventMetadata.EventID.OVER_SPEED.toString())) {
            totalOverSpeedNotificationPushed++;
        } else if (eventId.equals(EventMetadata.EventID.CURFEW.toString())) {
            totalCurfewAlertPushed++;
        } else if (eventId.equals(EventMetadata.EventID.IDLING.toString())) {
            totalIdleAlertPushed++;
        } else if (eventId.equals(EventMetadata.EventID.TOWING.toString())) {
            totalTowAlertPushed++;
        } else if (eventId.equals(EventMetadata.EventID.DONGLE_STATUS.toString())) {
            totalDongleStatusAlertPushed++;
        }

    }

    /**
     * Get the default alert data for a channel.
     *
     * @param alertMsg String
     * @return map with the default message
     */
    protected AlertEventData getDefaultAlertData(String alertMsg) {
        AlertEventData data = new AlertEventData();
        data.setDefaultMessage(alertMsg);
        return data;
    }

}
