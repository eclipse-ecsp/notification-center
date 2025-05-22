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

package org.eclipse.ecsp.notification.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

import static java.util.Locale.forLanguageTag;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.APPLICATION_JSON;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.CORRELATION_ID;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.EMAIL;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.FILTER_BY_SINGLE_USERNAME_URI;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.FIRST_NAME;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.LAST_NAME;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.LOCALE;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.PHONE_NUMBER;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.RESULTS_KEY;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.TIMEZONE;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.USER_CONSENT;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;

/**
 * IgniteCoreUserManagementClient class.
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Profile("ignite-user-profile")
public class IgniteCoreUserManagementClient {

    @Value("${ignite_user_management_url}")
    private String uri;

    @Value("${idam_server:" + IgniteCoreConstants.UIDAM + "}")
    private String idamServer;

    @Value("${locale_default_value:en-US}")
    private String defaultLocale;

    @Value("${timezone_default_value:GMT}")
    private String defaultTimezone;

    private RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(@Qualifier("servicesCommonRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @param idamServer the idamServer to set
     */
    public void setIdamServer(String idamServer) {
        this.idamServer = idamServer;
    }

    /**
     * @param defaultLocale the defaultLocale to set
     */
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * @param defaultTimezone the defaultTimezone to set
     */
    public void setDefaultTimezone(String defaultTimezone) {
        this.defaultTimezone = defaultTimezone;
    }

    /**
     * getUser from core user management.
     *
     * @param userName username
     *
     * @param prefix prefix
     *
     * @return Optional userprofile
     */
    public Optional<UserProfile> getUser(String userName, String prefix) {
        log.debug("Retrieving user {} from core user management {}", userName, uri);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON);
        headers.add(ACCEPT, APPLICATION_JSON);
        headers.add(CORRELATION_ID, "" + new Date().getTime());
        HttpStatus statusCode = null;
        String requestBodyWithUserName = String.format("{\"userNames\":[\"%s\"]}", prefix + userName);
        try {
            if (idamServer.equalsIgnoreCase(IgniteCoreConstants.WSO2_IDAM)) {
                ResponseEntity<HashMap> usersListRepresentation = restTemplate
                        .exchange(uri + FILTER_BY_SINGLE_USERNAME_URI,
                                POST,
                                new HttpEntity<>(requestBodyWithUserName, headers),
                                HashMap.class);

                statusCode = (HttpStatus) usersListRepresentation.getStatusCode();
                UserProfile userProfile;
                HashMap<String, Object> map = usersListRepresentation.getBody();
                if (map != null) {
                    userProfile = convertResultToUserProfile(map, userName);
                    return Optional.of(userProfile);
                }
            } else if (idamServer.equalsIgnoreCase(IgniteCoreConstants.UIDAM)) {
                ResponseEntity<ArrayList<LinkedHashMap>> usersListRepresentation = restTemplate
                        .exchange(uri + FILTER_BY_SINGLE_USERNAME_URI,
                                POST,
                                new HttpEntity<>(requestBodyWithUserName, headers),
                                new ParameterizedTypeReference<ArrayList<LinkedHashMap>>() {

                                });

                statusCode = (HttpStatus) usersListRepresentation.getStatusCode();
                UserProfile userProfile;
                ArrayList<LinkedHashMap> map = usersListRepresentation.getBody();
                if (map != null) {
                    userProfile = convertResultToUserProfileUidam(map, userName);
                    return Optional.of(userProfile);
                }
            }
        } catch (Exception e) {
            if (HttpStatus.OK.equals(statusCode)) {
                log.error("User " + userName + " does not exists {}", e.getMessage());
            } else {
                log.error("Exception: while trying to retrieve user: " + userName + " from ignite core {}",
                    e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * convertResultToUserProfile.
     *
     * @param usersListRepresentation usersListRepresentation
     * @param userName userName
     *
     * @return UserProfile
     */
    private UserProfile convertResultToUserProfileUidam(
            ArrayList<LinkedHashMap> usersListRepresentation, String userName) {
        LinkedHashMap results = usersListRepresentation.get(0);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userName);
        log.debug("Converting User from UIDAM-User-Management: {}", results.toString());
        if (results.get(FIRST_NAME) != null) {
            userProfile.setFirstName((String) results.get(FIRST_NAME));
        }
        if (results.get(LAST_NAME) != null) {
            userProfile.setLastName((String) results.get(LAST_NAME));
        }
        if (results.get(EMAIL) != null) {
            userProfile.setDefaultEmail((String) results.get(EMAIL));
        }
        if (results.get(PHONE_NUMBER) != null) {
            userProfile.setDefaultPhoneNumber((String) results.get(PHONE_NUMBER));
        }

        boolean userConsent = Boolean.parseBoolean(String.valueOf(results.get(USER_CONSENT)));
        userProfile.setConsent(userConsent);

        String timezone = defaultString((String) results.get(TIMEZONE), defaultTimezone);
        userProfile.setTimeZone(timezone);

        String locale = defaultString((String) results.get(LOCALE), defaultLocale);
        userProfile.setLocale(forLanguageTag(locale.replace("_", "-")));

        return userProfile;
    }

    /**
     * convertResultToUserProfile.
     *
     * @param usersListRepresentation usersListRepresentation
     * @param userName userName
     *
     * @return UserProfile
     */
    private UserProfile convertResultToUserProfile(
            HashMap<String, Object> usersListRepresentation, String userName) {
        LinkedHashMap results = ((ArrayList<LinkedHashMap>) usersListRepresentation.get(RESULTS_KEY)).get(0);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userName);
        log.debug("Converting User from User-Management: {}", results.toString());
        if (results.get(FIRST_NAME) != null) {
            userProfile.setFirstName((String) results.get(FIRST_NAME));
        }
        if (results.get(LAST_NAME) != null) {
            userProfile.setLastName((String) results.get(LAST_NAME));
        }
        if (results.get(EMAIL) != null) {
            userProfile.setDefaultEmail((String) results.get(EMAIL));
        }
        if (results.get(PHONE_NUMBER) != null) {
            userProfile.setDefaultPhoneNumber((String) results.get(PHONE_NUMBER));
        }

        boolean userConsent = Boolean.parseBoolean(String.valueOf(results.get(USER_CONSENT)));
        userProfile.setConsent(userConsent);

        String timezone = defaultString((String) results.get(TIMEZONE), defaultTimezone);
        userProfile.setTimeZone(timezone);

        String locale = defaultString((String) results.get(LOCALE), defaultLocale);
        userProfile.setLocale(forLanguageTag(locale.replace("_", "-")));

        return userProfile;
    }

}
