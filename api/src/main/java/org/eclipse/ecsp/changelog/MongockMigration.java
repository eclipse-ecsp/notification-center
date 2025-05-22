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

package org.eclipse.ecsp.changelog;

import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver;
import io.mongock.runner.springboot.MongockSpringboot;
import io.mongock.runner.springboot.base.MongockInitializingBeanRunner;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoAdminClient;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

/**
 * MongockMigration class.
 */
@Configuration
@Profile("!test")
public class MongockMigration {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(MongockMigration.class);

    @Value("${mongodb.name}")
    private String dbName;

    /**
     * Get MongoTemplate.
     *
     * @param igniteDaoMongoAdminClient IgniteDAOMongoAdminClient
     * @return MongoTemplate
     */
    @Bean
    public MongoTemplate getMongoTemplate(IgniteDAOMongoAdminClient igniteDaoMongoAdminClient) {
        return new MongoTemplate(
            new SimpleMongoClientDatabaseFactory(igniteDaoMongoAdminClient.getAdminClient(), dbName));
    }

    /**
     * mongockSpringBoot starter.
     *
     * @return MongockInitializingBeanRunner
     */
    @Bean("mongock-spring-boot")
    public MongockInitializingBeanRunner mongockSpringBoot(ApplicationContext springContext) {
        LOGGER.info("start checking mongo change log");
        return MongockSpringboot.builder()
            .setDriver(SpringDataMongoV4Driver.withDefaultLock(
                    getMongoTemplate(springContext.getBean(IgniteDAOMongoAdminClient.class))))
            .addChangeLogsScanPackage(ClientChangeLog.class.getPackage().getName())
            .setSpringContext(springContext)
            .buildInitializingBeanRunner();
    }
}