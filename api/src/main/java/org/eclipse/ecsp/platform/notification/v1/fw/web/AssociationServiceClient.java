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

package org.eclipse.ecsp.platform.notification.v1.fw.web;

import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.eclipse.ecsp.platform.notification.v1.common.Constants;
import org.eclipse.ecsp.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * AssociationServiceClient class.
 *
 * @author NeKumar
 */
@Service
@Profile("!test")
public class AssociationServiceClient {

    private static final String USER_ASSOCIATIONS = "/user/associations/";

    /**
     * Logger Ref.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AssociationServiceClient.class);

    /**
     * association Service Base URL.
     */
    @Value("${device.association.base.url}")
    private String baseUrl;

    /**
     * association Service API version.
     */
    @Value("${device.association.base.url.version}")
    private String baseVersion;



    /**
     * Rest Template Ref.
     */
    @Autowired
    @Qualifier("servicesCommonRestTemplate")
    private RestTemplate restTemplate;

    /**
     * association service Url.
     */
    private String associationServiceUrl;

    /**
     * Get devices for the user.
     *
     * @param userId the user ID
     *
     * @return - List of Associated devices to a user
     *
     * @throws Exception if failed to fetch devices
     */
    @SuppressWarnings("unchecked")
    public Optional<List<DeviceAssociation>> getDevices(String userId) throws IOException, UnknownError {

        try {
            ResponseEntity<String> response = restTemplate.exchange(associationServiceUrl, HttpMethod.GET,
                new HttpEntity<>(createHeaders(userId)), String.class);
            List<DeviceAssociation> devices = null;
            if (response.getBody() != null) {
                devices = JsonUtils.parseJsonAsList(response.getBody(), DeviceAssociation.class);
            }

            LOGGER.debug("AssociationServiceClient associated devices list size: {}",
                (devices != null ? devices.size() : 0));

            assert devices != null;
            return Optional.of(devices);

        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == (HttpStatus.UNAUTHORIZED)) {
                LOGGER.warn("Association api returned {} error.",
                    (HttpStatus.UNAUTHORIZED).value());
                throw new AuthenticationException("Authentication exception to retrieve user information");
            } else if (ex.getStatusCode() == (HttpStatus.FORBIDDEN)) {
                LOGGER.warn("Association api returned {} error. ",
                    (HttpStatus.UNAUTHORIZED).value());
                throw new AuthorizationException("Unauthorized to retreive user association");
            } else {
                LOGGER.warn("Associated service response code: {}", ex.getStatusCode());
                throw new UnknownError("Unknown exception while retrieving user associations");
            }

        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * createHeaders request headers.
     *
     * @param userId userId
     *
     * @return - HttpHeaders to hit association service
     */
    private HttpHeaders createHeaders(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.USER_ID, userId);
        headers.add(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        return headers;
    }

    /**
     * creates Association Service URL.
     */
    @PostConstruct
    private void getAssociationUrl() {
        associationServiceUrl = baseUrl + Constants.URL_SEPARATOR + baseVersion + USER_ASSOCIATIONS;
    }

    /**
     * Set association service URL.
     *
     * @param associationServiceUrl association service URL
     */
    void setAssociationServiceUrl(String associationServiceUrl) {
        this.associationServiceUrl = associationServiceUrl;
    }
}
