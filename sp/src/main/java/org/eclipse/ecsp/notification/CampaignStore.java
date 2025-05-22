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

import org.eclipse.ecsp.analytics.stream.base.utils.ObjectUtils;
import org.eclipse.ecsp.notification.key.store.CachedKeyStore;
import org.eclipse.ecsp.notification.key.store.KeyStore;
import org.eclipse.ecsp.notification.key.store.StoreUser;
import org.eclipse.ecsp.notification.utils.NotificationProperty;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Keystore implementation for campaign store.
 */
public class CampaignStore implements CachedKeyStore {

    private static KeyStore keyStore;

    /**
     * CampaignStore constructor.
     *
     * @param properties properties
     */
    public CampaignStore(Properties properties) {
        String keyStoreClass = properties.getProperty(NotificationProperty.CAMPAIGN_STORE);
        ObjectUtils.requireNonEmpty(keyStoreClass,
            String.format("Property %s is mandatory", NotificationProperty.CAMPAIGN_STORE));
        try {
            if (keyStore == null) {
                setKeyStore(properties, keyStoreClass);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {

            throw new IllegalArgumentException(
                String.format("Failed to initialize %s with %s.class", NotificationProperty.CAMPAIGN_STORE,
                    keyStoreClass), e);
        }
        int timeToLiveInSeconds =
            Integer.parseInt(properties.getProperty(NotificationProperty.CAMPAIGN_STORE_TTL_SECS, "86400"));
        keyStore.setTtl(timeToLiveInSeconds, TimeUnit.SECONDS);
    }

    /**
     * setKeyStore method.
     *
     * @param properties properties
     * @param keyStoreClass keyStoreClass
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     * @throws NoSuchMethodException NoSuchMethodException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    private static void setKeyStore(Properties properties, String keyStoreClass) throws
            InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, ClassNotFoundException {
        keyStore =
                (KeyStore) Class.forName(keyStoreClass).getConstructor(Properties.class, StoreUser.class) //NOSONAR
                        .newInstance(properties, StoreUser.NOTIFICATION_DEFAULT_USER);
    }

    /**
     * get method.
     *
     * @param key key
     * @return Object
     */
    public Object get(String key) {
        return keyStore.get(key);
    }

    /**
     * put method.
     *
     * @param key key
     * @param val val
     */
    public void put(String key, Object val) {
        keyStore.put(key, val);
    }

    /**
     * put method.
     *
     * @param key key
     */
    @Override
    public void put(String key) {
        keyStore.put(key);
    }

    /**
     * keyExists method.
     *
     * @param key key
     */
    @Override
    public boolean keyExists(String key) {
        return keyStore.keyExists(key);
    }

    /**
     * Cache restored.
     *
     * @return boolean
     */
    @Override
    public boolean cacheRestored() {
        return false;
    }

    /**
     * getCacheUser method.
     *
     * @return StoreUser
     */
    @Override
    public StoreUser getCacheUser() {
        return StoreUser.NOTIFICATION_DEFAULT_USER;
    }

}
