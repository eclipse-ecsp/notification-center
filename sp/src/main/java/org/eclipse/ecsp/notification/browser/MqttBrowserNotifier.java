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

package org.eclipse.ecsp.notification.browser;

import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.domain.notification.MQTTBrowserResponse;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.processors.NotificationMessage;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.UUID;

import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_FAILURE;
import static org.eclipse.ecsp.domain.notification.commons.NotificationConstants.NOTIFICATION_STATUS_SUCCESS;

/**
 * MqttBrowserNotifier class.
 */
@Component
public class MqttBrowserNotifier extends BrowserNotifier {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MqttBrowserNotifier.class);
    private static final String SVC_PROVIDER = "PORTAL:MQTT";

    private static final int ONE_TWO_ZERO = 120;
    private static String overRiddenBrokerUrl;
    private MqttConnectOptions connOpt;
    private String brokerUrl;
    private String separator;
    private int mqttQos;

    /**
     * setOverRiddenBrokerUrl method.
     *
     * @param overRiddenBrokerUrl String
     */
    public static void setOverRiddenBrokerUrl(String overRiddenBrokerUrl) {
        MqttBrowserNotifier.overRiddenBrokerUrl = overRiddenBrokerUrl;
    }

    /**
     * Method to publish message to appropriate service.
     *
     * @param alert AlertsInfo
     * @return ChannelResponse
     */
    @Override
    protected ChannelResponse doPublish(AlertsInfo alert) {

        String topicName;
        String userId =
                alert.getAlertsData().getUserProfile() != null
                        ? alert.getAlertsData().getUserProfile().getUserId() : null;
        MQTTBrowserResponse response = new MQTTBrowserResponse(userId);
        if (!validateUserId(response, userId, alert)) {
            return response;
        }
        try {
            response.setAlertObject(alert);
            String alertMsg = alert.getNotificationTemplate().getChannelTemplates().getPortal().getBody();
            response.setAlertData(getDefaultAlertData(alertMsg));
            String message = getAlertMessage(alert);
            MqttMessage mqttMessage = new MqttMessage();
            byte[] payLoad = JsonUtils.getObjectValueAsBytes(message);
            mqttMessage.setPayload(payLoad);
            mqttMessage.setQos(mqttQos);
            MqttClient client = new MqttClient(brokerUrl, UUID.randomUUID().toString(), null);
            client.connect(connOpt);
            topicName = userId + separator + NotificationConstants.NOTIFICATION_TOPIC;
            if (StringUtils.isNotEmpty(alert.getAlertsData().getMqttTopic())) {
                topicName = alert.getAlertsData().getMqttTopic();
            }
            client.publish(topicName, mqttMessage);
            LOGGER.info("Published message {} to topic {}", message, topicName);
            client.disconnect();
            response.setStatus(NOTIFICATION_STATUS_SUCCESS);
        } catch (Exception e) {
            LOGGER.error("Failed sending notification via MqttBrowserNotifier", e);
            response.setStatus(NOTIFICATION_STATUS_FAILURE);
        }

        return response;
    }

    /**
     * getAlertMessage method.
     *
     * @param alert AlertsInfo
     * @return String
     */
    public String getAlertMessage(AlertsInfo alert) {
        NotificationMessage message = new NotificationMessage();
        message.setAlertsInfo(alert);
        message.setMessage(alert.getNotificationTemplate().getChannelTemplates().getPortal().getBody());
        return JsonUtils.getObjectValueAsString(message);
    }

    /**
     * Method to initialize.
     *
     * @param props       Properties
     * @param metricRegistry    MetricRegistry
     * @param notificationDao NotificationDao
     */
    @Override
    public void init(Properties props, MetricRegistry metricRegistry, NotificationDao notificationDao) {
        super.init(props, metricRegistry, notificationDao);
        brokerUrl = props.getProperty(NotificationProperty.MQTT_BROKER_URL);
        if (null != overRiddenBrokerUrl) {
            brokerUrl = overRiddenBrokerUrl;
        }
        separator = props.getProperty(NotificationProperty.MQTT_TOPIC_SEPARATOR);
        mqttQos = Integer.parseInt(props.getProperty(NotificationProperty.MQTT_CONFIG_QOS));
        connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(false);
        connOpt.setKeepAliveInterval(ONE_TWO_ZERO);
        String userName = props.getProperty(NotificationProperty.MQTT_USER_NAME);
        connOpt.setUserName(userName);
        String userPassword = props.getProperty(NotificationProperty.MQTT_USER_PASSWORD);
        connOpt.setPassword(userPassword.toCharArray());
    }

    /**
     * Method to setup channel.
     *
     * @param notificationConfig Channel configuration
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse setupChannel(NotificationConfig notificationConfig) {
        // Nothing to do
        return null;
    }

    /**
     * Method to destroy channel.
     *
     * @param key       String
     * @param eventData String
     * @return ChannelResponse
     */
    @Override
    public ChannelResponse destroyChannel(String key, String eventData) {
        // Nothing to do
        return null;
    }

    /**
     * Get service provider name.
     *
     * @return Service provider name
     */
    @Override
    public String getServiceProviderName() {
        return SVC_PROVIDER;
    }


}
