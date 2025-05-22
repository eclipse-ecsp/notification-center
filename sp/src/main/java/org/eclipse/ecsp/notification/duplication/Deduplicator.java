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

package org.eclipse.ecsp.notification.duplication;

import org.eclipse.ecsp.analytics.stream.base.utils.ObjectUtils;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.notification.key.store.KeyStore;
import org.eclipse.ecsp.notification.key.store.StoreUser;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Deduplicator class.
 */
public class Deduplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Deduplicator.class);

    private static int TWO = 2;
    private static KeyStore keyStore;
    private Properties properties;
    private boolean isDuplicationCheckRequired;
    private DuplicateAlertStore alertStore;
    private int interval;

    /**
     * public constructor.
     *
     * @param prop Properties
     */
    public Deduplicator(Properties prop) {
        super();
        this.properties = prop;
        isDuplicationCheckRequired = Boolean
                .parseBoolean(this.properties.getProperty(NotificationProperty.DEDUP_ALERTS, "true"));

        if (isDuplicationCheckRequired) {
            this.interval = Integer.parseInt(this.properties.getProperty(NotificationProperty.DEDUP_INTERVAL_MS, "0"));
            LOGGER.debug("Alert duplication check is enabled with interval {}.", interval);
            KeyExtractorFactory.init(interval);
            String keyStoreClass = properties.getProperty(NotificationProperty.ALERT_DEDUP_STORE);
            ObjectUtils.requireNonEmpty(keyStoreClass,
                    String.format("Property %s is mandatory", NotificationProperty.ALERT_DEDUP_STORE));
            try {
                if (keyStore == null) {
                    setKeyStore(prop, keyStoreClass);
                }
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
                     | InvocationTargetException | NoSuchMethodException | SecurityException e) {

                throw new IllegalArgumentException(
                        String.format("Failed to initialize %s with %s.class", NotificationProperty.ALERT_DEDUP_STORE,
                                keyStoreClass), e);
            }
            int timeToLiveInSeconds =
                    Integer.parseInt(prop.getProperty(NotificationProperty.ALERT_DEDUP_STORE_TTL_SECS, "120"));
            keyStore.setTtl(timeToLiveInSeconds, TimeUnit.SECONDS);
            alertStore = new DuplicateAlertStore(prop, keyStore);
        }
    }

    /**
     * setKeyStore method.
     *
     * @param prop          Properties
     * @param keyStoreClass String
     * @throws InstantiationException    class
     * @throws IllegalAccessException    class
     * @throws InvocationTargetException class
     * @throws NoSuchMethodException     class
     * @throws ClassNotFoundException    class
     */
    private static void setKeyStore(Properties prop, String keyStoreClass)
            throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            ClassNotFoundException {
        keyStore = (KeyStore) Class.forName(keyStoreClass)
                .getConstructor(Properties.class, StoreUser.class) //NOSONAR
                .newInstance(prop, StoreUser.NOTIFICATION_ALERT_DEDUPLICATOR);
    }

    /**
     * filter Duplicate Alerts.
     *
     * @param alerts List of AlertsInfo.
     * @return List of AlertsInfo.
     */
    public List<AlertsInfo> filterDuplicateAlert(List<AlertsInfo> alerts) {

        if (!isDuplicationCheckRequired) {
            return alerts;
        }

        List<AlertsInfo> filteredAlerts = new ArrayList<>();
        String currentKey;
        String previousKey;
        for (AlertsInfo alert : alerts) {
            currentKey = KeyExtractorFactory.getCurrentKey(alert);
            LOGGER.debug("Current key : {}", currentKey);
            if (!keyExists(currentKey)) {
                previousKey = KeyExtractorFactory.getPreviousKey(alert, 1);
                LOGGER.debug("Previous key hop by 1: {}", previousKey);
                if (!keyExists(previousKey)) {
                    checkPrevKey(alert, currentKey, filteredAlerts, previousKey);
                } else {
                    LOGGER.info("Filtered duplicate alert for {} due to previous key {} 1st hop", currentKey,
                            previousKey);
                }
            } else {
                LOGGER.info("Filtered duplicate alert for {}", currentKey);
            }
        }
        return filteredAlerts;
    }

    /**
     * checkPrevKey method.
     *
     * @param alert          AlertsInfo
     * @param currentKey     String
     * @param filteredAlerts List of AlertsInfo
     * @param previousKey    String
     */
    private void checkPrevKey(AlertsInfo alert, String currentKey, List<AlertsInfo> filteredAlerts,
                              String previousKey) {
        String keyHopBy2 = KeyExtractorFactory.getPreviousKey(alert, TWO);
        LOGGER.debug("Previous key hop by 2: {}", keyHopBy2);
        if (keyExists(keyHopBy2)) {
            long previousTs = (long) keyStore.get(keyHopBy2);
            long currentTs = alert.getTimestamp();
            LOGGER.debug("Timestamps current alert {}, previous alert {} Interval {}", currentTs,
                    previousTs, interval);
            if ((currentTs - previousTs) > interval) {
                LOGGER.debug("This is not a duplicate alert on 2nd hop with {}. Falls outside interval",
                        currentKey);
                filteredAlerts.add(alert);
                alertStore.put(currentKey);
                keyStore.put(currentKey, alert.getTimestamp());
            } else {
                LOGGER.info(
                        "Filtered duplicate alert for {} due "
                                +
                                "to previous key {} on 2nd hop. Falls inside interval",
                        currentKey, previousKey);
            }
        } else {
            LOGGER.debug("This is not a duplicate alert on 2nd hop {}", currentKey);
            filteredAlerts.add(alert);
            alertStore.put(currentKey);
            keyStore.put(currentKey, alert.getTimestamp());
        }
    }

    /**
     * keyExists method.
     *
     * @param key String
     * @return boolean
     */
    private boolean keyExists(String key) {

        return alertStore.keyExists(key);
    }

    /**
     * cacheRestored method.
     *
     * @return boolean
     */
    public boolean cacheRestored() {
        return alertStore.cacheRestored();
    }
}
