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

import com.codahale.metrics.MetricRegistry;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.ChannelResponse;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.db.client.NotificationDao;

import java.util.Properties;

/**
 * Adaptor for various notification provider. Currently we supports Amazon SNS
 * as sending notification
 */
public interface ChannelNotifier {

    /**
     * Publish alert.
     *
     * @param alert Alert
     * @return Channel response
     */
    public ChannelResponse publish(AlertsInfo alert);

    /**
     * Get service provider name.
     *
     * @return Service provider name
     */
    public String getServiceProviderName();

    /**
     * Set up channel. For exaple, AmazonSNS create topics.
     *
     * @param notificationConfig Channel configuration
     * @return Channel response
     */
    public ChannelResponse setupChannel(NotificationConfig notificationConfig);

    /**
     * Destroy channel. For example, AmazonSNS delete topics.
     *
     * @param userId    User id
     * @param eventData Event data
     * @return Channel response
     */
    public ChannelResponse destroyChannel(String userId, String eventData);

    /**
     * Initialize the channel notifier.
     *
     * @param notificationProps Notification properties
     * @param metricRegistry     Metric registry
     * @param notificationDao    Notification DAO
     */
    public void init(Properties notificationProps, MetricRegistry metricRegistry, NotificationDao notificationDao);

    /**
     * Get protocol.
     *
     * @return Protocol
     */
    public String getProtocol();

    /**
     * Set processor context.
     *
     * @param ctxt Stream processing context
     */
    public default void setProcessorContext(StreamProcessingContext<IgniteKey<?>, IgniteEvent> ctxt) {

    }

    /**
     * Process ack.
     *
     * @param igniteEvent Ignite event
     */
    public default void processAck(IgniteEvent igniteEvent) {

    }

}
