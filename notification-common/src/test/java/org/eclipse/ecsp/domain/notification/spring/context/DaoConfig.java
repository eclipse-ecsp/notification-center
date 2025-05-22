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

package org.eclipse.ecsp.domain.notification.spring.context;

import org.eclipse.ecsp.notification.config.NotificationConfigDAOMongoImpl;
import org.eclipse.ecsp.notification.dao.UserProfileDAOMongoImpl;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAOMongoImpl;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;

/**
 * DaoConfig class.
 */
@Configuration
@ComponentScan(basePackageClasses = {NotificationConfigDAOMongoImpl.class,
    NotificationGroupingDAOMongoImpl.class,
    UserProfileDAOMongoImpl.class}, includeFilters = {
      @ComponentScan.Filter(classes = {Repository.class}, type = FilterType.ANNOTATION)}, useDefaultFilters = false)
public class DaoConfig {

}
