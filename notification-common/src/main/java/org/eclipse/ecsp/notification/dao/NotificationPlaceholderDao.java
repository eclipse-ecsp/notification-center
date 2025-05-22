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

package org.eclipse.ecsp.notification.dao;

import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;
import org.eclipse.ecsp.notification.entities.NotificationPlaceholder;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * NotificationPlaceholderDao interface.
 */
public interface NotificationPlaceholderDao extends IgniteBaseDAO<String, NotificationPlaceholder> {
    /**
     * Delete by keys.
     *
     * @param placeholdersKeys placeholders keys
     */
    void deleteByKeys(Set<String> placeholdersKeys);

    /**
     * Find by keys.
     *
     * @param keys keys
     * @return list of NotificationPlaceholder
     */
    List<NotificationPlaceholder> findByKeys(Set<String> keys);

    /**
     * Find by keys, brands and locales.
     *
     * @param keys keys
     * @param locales locales
     * @param brands brands
     * @return list of NotificationPlaceholder
     */
    List<NotificationPlaceholder> findByKeysBrandsAndLocales(Set<String> keys, List<Locale> locales,
                                                             List<String> brands);
}
