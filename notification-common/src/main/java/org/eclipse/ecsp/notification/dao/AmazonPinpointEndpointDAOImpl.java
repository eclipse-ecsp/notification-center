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

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.notification.entities.AmazonPinpointEndpoint;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DAO Impl class for user account endpoint(s).
 *
 * @author MaKumari
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Repository
public class AmazonPinpointEndpointDAOImpl extends IgniteBaseDAOMongoImpl<String, AmazonPinpointEndpoint>
    implements AmazonPinpointEndpointDAO {

    private static final  Logger LOGGER = LoggerFactory.getLogger(AmazonPinpointEndpointDAOImpl.class);

    /**
     * Initiate AmazonPinpointEndpointDAOImpl.
     */
    @PostConstruct
    public void initInfo() {
        LOGGER.info(
            "Initiated AmazonPinpointEndpointDAOImpl");
    }

    @Autowired
    private EncryptDecryptInterface encryptDecryptInterface;

    /**
     * Save AmazonPinpointEndpoint.
     *
     * @param amazonPinpointEndpoint endpoint
     * @return AmazonPinpointEndpoint
     */
    @Override
    public AmazonPinpointEndpoint save(AmazonPinpointEndpoint amazonPinpointEndpoint) {
        amazonPinpointEndpoint = decryptPinpointEndpoint(super.save(encryptPinpointEndpoint(amazonPinpointEndpoint)));
        return amazonPinpointEndpoint;
    }

    /**
     * Find AmazonPinpointEndpoint by id.
     *
     * @param id id
     * @return AmazonPinpointEndpoint
     */
    @Override
    public AmazonPinpointEndpoint findById(String id) {
        AmazonPinpointEndpoint amazonPinpointEndpoint = super.findById(id);
        if (amazonPinpointEndpoint != null) {
            return decryptPinpointEndpoint(amazonPinpointEndpoint);
        }
        return null;
    }

    /**
     * encryptPinpointEndpoint data.
     *
     * @param amazonPinpointEndpoint endpoint
     *
     * @return encrypted AmazonPinpointEndpoint
     */
    public AmazonPinpointEndpoint encryptPinpointEndpoint(AmazonPinpointEndpoint amazonPinpointEndpoint) {
        AmazonPinpointEndpoint amazonPinpointEndpointUpdated = null;
        if (ObjectUtils.isNotEmpty(amazonPinpointEndpoint)
            && ObjectUtils.isNotEmpty(amazonPinpointEndpoint.getEndpoints())) {
            amazonPinpointEndpointUpdated = new AmazonPinpointEndpoint();
            amazonPinpointEndpointUpdated.setUserId(amazonPinpointEndpoint.getUserId());
            Map<String, Map<String, String>> endpoints = new HashMap<>();
            Map<String, String> channelEndpoints = null;
            for (Entry<String, Map<String, String>> endps : amazonPinpointEndpoint.getEndpoints().entrySet()) {
                channelEndpoints = new HashMap<>();
                for (Entry<String, String> channelEndps : endps.getValue().entrySet()) {
                    String key = channelEndps.getKey();
                    String encryptedKey = encryptDecryptInterface.encrypt(key);
                    channelEndpoints.put(encryptedKey, channelEndps.getValue());
                }
                endpoints.put(endps.getKey(), channelEndpoints);
            }
            amazonPinpointEndpointUpdated.setEndpoints(endpoints);
            return amazonPinpointEndpointUpdated;
        }
        return amazonPinpointEndpoint;
    }

    /**
     * decryptPinpointEndpoint.
     *
     * @param amazonPinpointEndpoint endpoint
     *
     * @return decrypted endpoint
     */
    public AmazonPinpointEndpoint decryptPinpointEndpoint(AmazonPinpointEndpoint amazonPinpointEndpoint) {
        AmazonPinpointEndpoint amazonPinpointEndpointUpdated = null;
        if (ObjectUtils.isNotEmpty(amazonPinpointEndpoint)
            && ObjectUtils.isNotEmpty(amazonPinpointEndpoint.getEndpoints())) {
            amazonPinpointEndpointUpdated = new AmazonPinpointEndpoint();
            amazonPinpointEndpointUpdated.setUserId(amazonPinpointEndpoint.getUserId());
            Map<String, Map<String, String>> endpoints = new HashMap<>();
            Map<String, String> channelEndpoints = null;
            for (Entry<String, Map<String, String>> endps : amazonPinpointEndpoint.getEndpoints().entrySet()) {
                channelEndpoints = new HashMap<>();
                for (Entry<String, String> channelEndps : endps.getValue().entrySet()) {
                    channelEndpoints.put(encryptDecryptInterface.decrypt(channelEndps.getKey()),
                        channelEndps.getValue());
                }
                endpoints.put(endps.getKey(), channelEndpoints);
            }
            amazonPinpointEndpointUpdated.setEndpoints(endpoints);
            return amazonPinpointEndpointUpdated;
        }
        return amazonPinpointEndpoint;
    }

}
