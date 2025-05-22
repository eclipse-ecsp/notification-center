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

package org.eclipse.ecsp.notification.utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;

/**
 * Dynamo DB repo mapper.
 */
public class DynamoMapperRepository {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DynamoMapperRepository.class);



    /**
     * DynamoMapperRepository constructor.
     *
     * @param amazonDynamoDb AmazonDynamoDB
     */
    public DynamoMapperRepository(AmazonDynamoDB amazonDynamoDb) {

    }

    /**
     * getMapper method to get dynamo db mapper.
     *
     * @param tableName String
     * @param dynamoDbClient DynamoDbClient
     * @return DynamoDbMapper
     */
    public DynamoDBMapper getMapper(String tableName, AmazonDynamoDB dynamoDbClient) {

        LOGGER.debug("Inside DynamoDBConfig getMapper");

        DynamoDBMapperConfig dynamoDbMapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(TableNameOverride.withTableNameReplacement(tableName)).build();
        return new DynamoDBMapper(dynamoDbClient, dynamoDbMapperConfig);
    }
}
