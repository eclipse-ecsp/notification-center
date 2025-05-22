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

package org.eclipse.ecsp.notification.key.store;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * String key store.
 */
public interface KeyStore {

    /**
     * Method to put Key.
     *
     * @param key the given key
     */
    public void put(String key);

    /**
     * Method to put Key.
     *
     * @param key the given key
     * @param val the given value
     */
    public default void put(String key, Object val) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    /**
     * Method to check if Key exists.
     *
     * @param key the given key
     * @return boolean
     */
    public boolean keyExists(String key);

    /**
     * Method to check if Key exists.
     *
     * @param pattern the given pattern
     * @return Set
     */
    public Set<String> getAllKeys(String pattern);

    /**
     * Method to get Cache User.
     *
     * @return StoreUser
     */
    public StoreUser getUser();

    /**
     * Method to set TTL.
     *
     * @param ttl int
     * @param ttlTimeUnit TimeUnit
     */
    public default void setTtl(int ttl, TimeUnit ttlTimeUnit) {
    }


    /**
     * Method to get Value.
     *
     * @param key the given key
     * @return Object
     */
    public default Object get(String key) {
        throw new UnsupportedOperationException("Operation not supported");
    }
}
