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

import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.config.CachedChannelServiceProviderConfigDAO;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * ChannelNotifierRegistry for registering channel service providers.
 */
public class ChannelNotifierRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelNotifierRegistry.class);
    private Map<ChannelType, Map<String, ChannelNotifier>> registry = new EnumMap<>(ChannelType.class);

    private CachedChannelServiceProviderConfigDAO cachedChannelServiceProviderConfigDao;

    private Properties props;

    /**
     * ChannelNotifier constructor.
     *
     * @param channelType channelType
     *
     * @return ChannelNotifier
     */
    public ChannelNotifier channelNotifier(ChannelType channelType) {

        LOGGER.info("Proceeding with Default implementation for channelType {} ", channelType);

        Map<String, ChannelNotifier> svcProviderMap = registry.get(channelType);

        LOGGER.debug("map obtained for the channel {} ", svcProviderMap);

        switch (channelType) {
            case EMAIL:
                return svcProviderMap.get(props.getProperty("email.default.sp"));
            case SMS:
                return svcProviderMap.get(props.getProperty("sms.default.sp"));
            case IVM:
                return svcProviderMap.get(props.getProperty("ivm.default.sp"));
            case API_PUSH:
                return svcProviderMap.get(props.getProperty("apiPush.default.sp"));
            case MOBILE_APP_PUSH:
                return svcProviderMap.get(props.getProperty("push.default.sp"));
            case PORTAL:
                return svcProviderMap.get(props.getProperty("portal.default.sp"));
            default:
                LOGGER.error("Invalid Channel Type {}", channelType);
                throw new IllegalArgumentException("Invalid Channel Type " + channelType);

        }
    }

    /**
     * Get channel notifier by channel type ,notificationId and brand.
     *
     * @param channelType channelType
     *
     * @param notificationId notificationId
     *
     * @param region region
     *
     * @return channelNotifier
     */
    public ChannelNotifier channelNotifier(ChannelType channelType, String notificationId, String region) {

        LOGGER.info("notificationId {} for channel {} region {}", notificationId, channelType, region);

        Optional<String> serviceProviderInfo = Optional.empty();
        if (!(StringUtils.isEmpty(notificationId) || StringUtils.isEmpty(region))) {
            LOGGER.debug("cachedChannelServiceProviderConfigDAO {}", cachedChannelServiceProviderConfigDao);

            serviceProviderInfo = cachedChannelServiceProviderConfigDao
                    .getServiceProviderByChannelNotificationIdAndRegion(channelType, notificationId, region);
        }
        LOGGER.debug("Service Provider configured {} for channel {}", serviceProviderInfo, channelType);

        Map<String, ChannelNotifier> svcProviderMap = registry.get(channelType);

        // if entry found return specific notifier from registry
        if (serviceProviderInfo.isPresent()) {

            return (svcProviderMap.get(serviceProviderInfo.get()) != null)
                    ? svcProviderMap.get(serviceProviderInfo.get())
                    : channelNotifier(channelType);

        }
        return channelNotifier(channelType);

    }

    /**
     * Get all channel notifiers for the configured channels.
     *
     * @param channelType channelType
     *
     * @return channelnotifiers
     */
    public Map<String, ChannelNotifier> getAllchannelNotifiers(ChannelType channelType) {

        LOGGER.info("Returning all notifiers for channelType {} ", channelType);

        Map<String, ChannelNotifier> svcProviderMap = registry.get(channelType);

        LOGGER.debug("map obtained for the channel {} ", svcProviderMap);

        return svcProviderMap;

    }


    /**
     * Initialize the ChannelNotifierRegistry.
     *
     * @param channelSpConfigDao channelSpConfigDao
     *
     * @param props props
     *
     * @param metricRegistry metricRegistry
     *
     * @param notificationDao notificationDao
     *
     * @param ctxt ctxt
     *
     * @param channelsSupported channelsSupported
     *
     * @param applicationContext applicationContext
     */
    public void init(CachedChannelServiceProviderConfigDAO channelSpConfigDao, Properties props,
                     MetricRegistry metricRegistry, NotificationDao notificationDao,
                     StreamProcessingContext<IgniteKey<?>, IgniteEvent>  ctxt,
                     Set<ChannelType> channelsSupported, ApplicationContext applicationContext) {

        this.props = props;
        this.cachedChannelServiceProviderConfigDao = channelSpConfigDao;
        LOGGER.info("channelServiceProviderConfigDAO init {} ", cachedChannelServiceProviderConfigDao);

        String[] availableChannelNotifiers = (props.getProperty("available.channel.notifiers")).split(",");

        Map<String, List<String>> channelNotifiersMap = getChannelNotifiersMap(availableChannelNotifiers);

        for (ChannelType channelType : channelsSupported) {

            Map<String, ChannelNotifier> svcProviderConfigMap = new HashMap<>();

            List<String> notifierList = channelNotifiersMap.get(channelType.getChannelType());

            if (notifierList == null) {
                throw new IllegalArgumentException(
                        "No notifer present for " + channelType + " " + " in available.channel.notifiers property");
            } else {
                for (String channelNotifier : notifierList) {

                    LOGGER.debug("className {}", channelNotifier);
                    Class<?> cls;

                    try {

                        cls = this.getClass().getClassLoader().loadClass(channelNotifier); // NOSONAR

                        ChannelNotifier  notifier = (ChannelNotifier) applicationContext.getBean(cls);

                        notifier.init(props, metricRegistry, notificationDao);
                        notifier.setProcessorContext(ctxt);
                        // use sp store notifier bean
                        svcProviderConfigMap.put(notifier.getServiceProviderName(), notifier);
                    } catch (Exception e) {
                        LOGGER.error("Invalid configuration for {}."
                                        +
                                        " Could not instantiate class {} due to exception {}", channelType,
                                channelNotifier, e.getMessage());
                        throw new IllegalArgumentException("Invalid configuration for " + channelType + " "
                                + channelNotifier + "  class. Could not instantiate class" + e.getMessage());
                    }

                }
            }
            LOGGER.debug("Notifier registered for channel {} with map {}", channelType, svcProviderConfigMap);

            registry.put(channelType, svcProviderConfigMap);


        }
        LOGGER.info("ChannelRegistry :- {}", registry);
    }

    /**
     * Get channel notifiers map.
     *
     * @param availableChannelNotifiers availableChannelNotifiers
     *
     * @return channelNotifiersMap
     */
    private Map<String, List<String>> getChannelNotifiersMap(String[] availableChannelNotifiers) {
        Map<String, List<String>> channelNotifiersMap = new HashMap<>();
        List<String> notifierList = null;
        if (null != availableChannelNotifiers) {
            for (String header : availableChannelNotifiers) {
                String[] arr = header.split(":");

                if (channelNotifiersMap.get(arr[0]) == null) {
                    notifierList = new ArrayList<>();
                    notifierList.add(arr[1]);
                    channelNotifiersMap.put(arr[0], notifierList);
                } else {
                    notifierList = channelNotifiersMap.get(arr[0]);
                    notifierList.add(arr[1]);
                    channelNotifiersMap.put(arr[0], notifierList);
                }

            }
        }
        return channelNotifiersMap;
    }
}
