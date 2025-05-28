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

package org.eclipse.ecsp.domain.notification.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.notification.client.IgniteCoreConstants;
import org.eclipse.ecsp.notification.client.IgniteCoreUserManagementClient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.APPLICATION_JSON;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.CORRELATION_ID;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.EMAIL;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.FIRST_NAME;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.LAST_NAME;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.LOCALE;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.PHONE_NUMBER;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.TIMEZONE;
import static org.eclipse.ecsp.notification.client.IgniteCoreConstants.USER_CONSENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;


/**
 * IgniteCoreUserManagementClientTest class.
 */
@Slf4j
public class IgniteCoreUserManagementClientTest {

    @InjectMocks
    IgniteCoreUserManagementClient igniteCoreUserManagementClient;

    @Mock
    RestTemplate restTemplate;

    String jsonStringUpsMisInfo = "{\n"
            +
            "  \"messages\": [\n"
            +
            "    {\n"
            +
            "      \"key\": \"success.key\",\n"
            +
            "      \"parameters\": [\n"
            +
            "        {}\n"
            +
            "      ]\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"results\": [\n"
            +
            "    {\n"
            +
            "      \"id\": \"AV-6l_XKJD9q-6ubOKC8\",\n"
            +
            "      \"userName\": \"test-user\",\n"
            +
            "      \"role\": \"VEHICLE_OWNER\",\n"
            +
            "      \"country\": \"US\",\n"
            +
            "      \"city\": \"Irvine\",\n"
            +
            "      \"phoneNumber\": \"+17535011234\",\n"
            +
            "      \"gender\": \"MALE\",\n"
            +
            "      \"birthDate\": \"1997-10-13\",\n"
            +
            "      \"email\": \"john.doe@domain.com\",\n"
            +
            "      \"devIds\": [\n"
            +
            "        \"VINaXMJFbqJTST\"\n"
            +
            "      ],\n"
            +
            "      \"address1\": \"Main Street\",\n"
            +
            "      \"address2\": \"2000\",\n"
            +
            "      \"state\": \"CA\",\n"
            +
            "      \"postalCode\": \"92614\"\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"metaData\": {\n"
            +
            "    \"count\": \"1\"\n"
            +
            "  }\n"
            +
            "}";
    String jsonStringFedMisInf = "{\n"
            +
            "  \"messages\": [\n"
            +
            "    {\n"
            +
            "      \"key\": \"success.key\",\n"
            +
            "      \"parameters\": [\n"
            +
            "        {}\n"
            +
            "      ]\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"results\": [\n"
            +
            "    {\n"
            +
            "      \"id\": \"AV-6l_XKJD9q-6ubOKC8\",\n"
            +
            "      \"userName\": \"federated_test-user\",\n"
            +
            "      \"role\": \"VEHICLE_OWNER\",\n"
            +
            "      \"country\": \"US\",\n"
            +
            "      \"city\": \"Irvine\",\n"
            +
            "      \"phoneNumber\": \"+17535011234\",\n"
            +
            "      \"gender\": \"MALE\",\n"
            +
            "      \"birthDate\": \"1997-10-13\",\n"
            +
            "      \"email\": \"john.doe@domain.com\",\n"
            +
            "      \"devIds\": [\n"
            +
            "        \"VINaXMJFbqJTST\"\n"
            +
            "      ],\n"
            +
            "      \"address1\": \"Main Street\",\n"
            +
            "      \"address2\": \"2000\",\n"
            +
            "      \"state\": \"CA\",\n"
            +
            "      \"postalCode\": \"92614\"\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"metaData\": {\n"
            +
            "    \"count\": \"1\"\n"
            +
            "  }\n"
            +
            "}";
    String jsonStringUps = "{\n"
            +
            "  \"messages\": [\n"
            +
            "    {\n"
            +
            "      \"key\": \"success.key\",\n"
            +
            "      \"parameters\": [\n"
            +
            "        {}\n"
            +
            "      ]\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"results\": [\n"
            +
            "    {\n"
            +
            "      \"id\": \"AV-6l_XKJD9q-6ubOKC8\",\n"
            +
            "      \"userName\": \"test-user\",\n"
            +
            "      \"firstName\": \"John\",\n"
            +
            "      \"role\": \"VEHICLE_OWNER\",\n"
            +
            "      \"lastName\": \"Doe\",\n"
            +
            "      \"country\": \"US\",\n"
            +
            "      \"city\": \"Irvine\",\n"
            +
            "      \"phoneNumber\": \"+17535011234\",\n"
            +
            "      \"gender\": \"MALE\",\n"
            +
            "      \"birthDate\": \"1997-10-13\",\n"
            +
            "      \"email\": \"john.doe@domain.com\",\n"
            +
            "      \"locale\": \"en_US\",\n"
            +
            "      \"devIds\": [\n"
            +
            "        \"VINaXMJFbqJTST\"\n"
            +
            "      ],\n"
            +
            "      \"address1\": \"Main Street\",\n"
            +
            "      \"address2\": \"2000\",\n"
            +
            "      \"state\": \"CA\",\n"
            +
            "      \"notificationConsent\": \"true\",\n"
            +
            "      \"timeZone\": \"GMT\",\n"
            +
            "      \"postalCode\": \"92614\"\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"metaData\": {\n"
            +
            "    \"count\": \"1\"\n"
            +
            "  }\n"
            +
            "}";

    String jsonStringGf = "{\n"
            +
            "  \"messages\": [\n"
            +
            "    {\n"
            +
            "      \"key\": \"success.key\",\n"
            +
            "      \"parameters\": [\n"
            +
            "        {}\n"
            +
            "      ]\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"results\": [\n"
            +
            "    {\n"
            +
            "      \"id\": \"AV-6l_XKJD9q-6ubOKC8\",\n"
            +
            "      \"userName\": \"federated_test-user\",\n"
            +
            "      \"firstName\": \"John\",\n"
            +
            "      \"role\": \"VEHICLE_OWNER\",\n"
            +
            "      \"lastName\": \"Doe\",\n"
            +
            "      \"country\": \"US\",\n"
            +
            "      \"city\": \"Irvine\",\n"

            +
            "      \"phoneNumber\": \"+17535011234\",\n"
            +
            "      \"gender\": \"MALE\",\n"
            +
            "      \"birthDate\": \"1997-10-13\",\n"
            +
            "      \"email\": \"john.doe@domain.com\",\n"
            +
            "      \"locale\": \"en_US\",\n"
            +
            "      \"devIds\": [\n"
            +
            "        \"VINaXMJFbqJTST\"\n"
            +
            "      ],\n"
            +
            "      \"address1\": \"Main Street\",\n"
            +
            "      \"address2\": \"2000\",\n"
            +
            "      \"state\": \"CA\",\n"
            +
            "      \"notificationConsent\": \"true\",\n"
            +
            "      \"timeZone\": \"GMT\",\n"
            +
            "      \"postalCode\": \"92614\"\n"
            +
            "    }\n"
            +
            "  ],\n"
            +
            "  \"metaData\": {\n"
            +
            "    \"count\": \"1\"\n"
            +
            "  }\n"
            +
            "}";

    String jsonStringGfUidam = "[\r\n"
            + "    {\r\n"
            + "        \"id\": \"test-user\",\r\n"
            + "        \"userName\": \"new_robot_user_1724232200@yopmail.com\",\r\n"
            + "        \"status\": \"ACTIVE\",\r\n"
            + "        \"firstName\": \"John\",\r\n"
            + "        \"lastName\": \"Doe\",\r\n"
            + "        \"country\": null,\r\n"
            + "        \"state\": null,\r\n"
            + "        \"city\": null,\r\n"
            + "        \"address1\": null,\r\n"
            + "        \"address2\": null,\r\n"
            + "        \"postalCode\": null,\r\n"
            + "        \"phoneNumber\": null,\r\n"
            + "        \"email\": \"new_robot_user_1724232200@yopmail.com\",\r\n"
            + "        \"gender\": null,\r\n"
            + "        \"birthDate\": null,\r\n"
            + "        \"locale\": \"en_US\",\r\n"
            + "        \"notificationConsent\": true,\r\n"
            + "        \"timeZone\": \"GMT\",\r\n"
            + "        \"devIds\": [],\r\n"
            + "        \"roles\": [\r\n"
            + "            \"BUSINESS_ADMIN\"\r\n"
            + "        ]\r\n"
            + "    }\r\n"
            + "]\r\n"
            + "";

    @Test
    public void getFederatedUserProfileSuccessUidam() {
        MockitoAnnotations.initMocks(this);


        ArrayList<LinkedHashMap> map = null;
        try {
            map = new ObjectMapper().readValue(jsonStringGfUidam,
                    new TypeReference<ArrayList<LinkedHashMap>>() {});
        } catch (Exception e) {
            log.debug("could not create valid json for test");
            System.out.println(e.getMessage());
        }
        String requestBodyWithUserName = "{\"userNames\":[\"federated_test-user\"]}";
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON);
        headers.add(ACCEPT, APPLICATION_JSON);
        headers.add(CORRELATION_ID, "" + new Date().getTime());
        ResponseEntity mockResponseEntity = mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<
                        ArrayList<LinkedHashMap>>>any())).thenReturn(mockResponseEntity);

        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getStatusCode()).thenReturn(OK);
        igniteCoreUserManagementClient.setUri("http://u_idam-user-management.sw-platform:8080/v1/users/filter?pageNumber=0&sortOrder=DESC");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.UIDAM);
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "federated_");
        Assert.assertTrue(userProfileOptional.isPresent());
        UserProfile userProfile = userProfileOptional.get();
        Assert.assertEquals(userProfile.getUserId(), "test-user");
        Assert.assertEquals(userProfile.getFirstName(), "John");
        Assert.assertEquals(userProfile.getLastName(), "Doe");
        Assert.assertEquals(userProfile.getDefaultEmail(), "new_robot_user_1724232200@yopmail.com");
        Assert.assertEquals(userProfile.getLocale(), Locale.forLanguageTag("en-US"));
        Assert.assertEquals(userProfile.getTimeZone(), "GMT");
        Assert.assertEquals(userProfile.isConsent(), true);
    }
    
    @Test
    public void getFederatedUserProfileSuccess() {
        MockitoAnnotations.initMocks(this);
        ResponseEntity mockResponseEntity = mock(ResponseEntity.class);

        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonStringGf, HashMap.class);
        } catch (IOException e) {
            log.debug("could not create valid json for test");
        }
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Map>>any())).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getStatusCode()).thenReturn(OK);
        igniteCoreUserManagementClient.setUri("http://user-management.ignite-core:8080");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "federated_");
        Assert.assertTrue(userProfileOptional.isPresent());
        UserProfile userProfile = userProfileOptional.get();
        Assert.assertEquals(userProfile.getUserId(), "test-user");
        Assert.assertEquals(userProfile.getFirstName(), "John");
        Assert.assertEquals(userProfile.getLastName(), "Doe");
        Assert.assertEquals(userProfile.getDefaultEmail(), "john.doe@domain.com");
        Assert.assertEquals(userProfile.getLocale(), Locale.forLanguageTag("en-US"));
        Assert.assertEquals(userProfile.getTimeZone(), "GMT");
        Assert.assertEquals(userProfile.isConsent(), true);
    }

    @Test
    public void getUserProfileSuccess() {
        MockitoAnnotations.initMocks(this);
        ResponseEntity mockResponseEntity = mock(ResponseEntity.class);

        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonStringUps, HashMap.class);
        } catch (IOException e) {
            log.debug("could not create valid json for test");
        }
        getFederatedUserProfileSuccessValidation(mockResponseEntity, map);
    }

    private void getFederatedUserProfileSuccessValidation(ResponseEntity mockResponseEntity, Map<String, Object> map) {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Map>>any())).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getStatusCode()).thenReturn(OK);
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "");
        Assert.assertTrue(userProfileOptional.isPresent());
        UserProfile userProfile = userProfileOptional.get();
        Assert.assertEquals(userProfile.getUserId(), "test-user");
        Assert.assertEquals(userProfile.getFirstName(), "John");
        Assert.assertEquals(userProfile.getLastName(), "Doe");
        Assert.assertEquals(userProfile.getDefaultEmail(), "john.doe@domain.com");
        Assert.assertEquals(userProfile.getDefaultPhoneNumber(), "+17535011234");
        Assert.assertEquals(userProfile.getLocale(), Locale.forLanguageTag("en-US"));
        Assert.assertEquals(userProfile.getTimeZone(), "GMT");
        Assert.assertEquals(userProfile.isConsent(), true);
    }

    @Test
    public void getUserProfileSuccessMissingInformation() {
        MockitoAnnotations.initMocks(this);
        ResponseEntity mockResponseEntity = mock(ResponseEntity.class);

        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonStringUpsMisInfo, HashMap.class);
        } catch (IOException e) {
            log.debug("could not create valid json for test");
        }
        getUserProfileSuccessValidations(mockResponseEntity, map);

    }

    private void getUserProfileSuccessValidations(ResponseEntity mockResponseEntity, Map<String, Object> map) {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Map>>any())).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getStatusCode()).thenReturn(OK);
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);
        igniteCoreUserManagementClient.setDefaultLocale("en-US");
        igniteCoreUserManagementClient.setDefaultTimezone("GMT");
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "");
        Assert.assertTrue(userProfileOptional.isPresent());
        UserProfile userProfile = userProfileOptional.get();
        Assert.assertEquals(userProfile.getUserId(), "test-user");
        Assert.assertEquals(userProfile.getDefaultEmail(), "john.doe@domain.com");
        Assert.assertEquals(userProfile.getDefaultPhoneNumber(), "+17535011234");
        Assert.assertEquals(userProfile.getLocale(), Locale.forLanguageTag("en-US"));
        Assert.assertEquals(userProfile.getTimeZone(), "GMT");
        Assert.assertEquals(userProfile.isConsent(), false);
    }

    @Test
    public void getFederatedUserProfileSuccessMissingInformation() {
        MockitoAnnotations.initMocks(this);
        ResponseEntity mockResponseEntity = mock(ResponseEntity.class);

        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonStringFedMisInf, HashMap.class);
        } catch (IOException e) {
            log.debug("could not create valid json for test");
        }
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Map>>any())).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getStatusCode()).thenReturn(OK);
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);
        igniteCoreUserManagementClient.setDefaultLocale("en-US");
        igniteCoreUserManagementClient.setDefaultTimezone("GMT");
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "federated_");
        Assert.assertTrue(userProfileOptional.isPresent());
        UserProfile userProfile = userProfileOptional.get();
        Assert.assertEquals(userProfile.getUserId(), "test-user");
        Assert.assertEquals(userProfile.getDefaultEmail(), "john.doe@domain.com");
        Assert.assertEquals(userProfile.getDefaultPhoneNumber(), "+17535011234");
        Assert.assertEquals(userProfile.getLocale(), Locale.forLanguageTag("en-US"));
        Assert.assertEquals(userProfile.getTimeZone(), "GMT");
        Assert.assertEquals(userProfile.isConsent(), false);

    }

    @Test
    public void getUserProfileFailUserNameDoseNotExists() {
        MockitoAnnotations.initMocks(this);
        ResponseEntity mockResponseEntity = mock(ResponseEntity.class);
        String jsonString = "{\n"
                +
            "  \"messages\": [\n"
                +
            "    {\n"
                +
            "      \"key\": \"success.key\"\n"
                +
            "    }\n"
                +
            "  ],\n"
                +
            "  \"metadata\": {\n"
                +
            "    \"count\": 0\n"
                +
            "  }\n"
                +
            "}";
        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonString, HashMap.class);
        } catch (IOException e) {
            log.debug("could not create valid json for test");
        }
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Map>>any())).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getStatusCode()).thenReturn(OK);
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "");
        Assert.assertFalse(userProfileOptional.isPresent());
    }

    @Test
    public void getFederatedUserProfileFailUserNameDoseNotExists() {
        MockitoAnnotations.initMocks(this);
        ResponseEntity mockResponseEntity = mock(ResponseEntity.class);
        String jsonString = "{\n"
                +
            "  \"messages\": [\n"
                +
            "    {\n"
                +
            "      \"key\": \"success.key\"\n"
                +
            "    }\n"
                +
            "  ],\n"
                +
            "  \"metadata\": {\n"
                +
            "    \"count\": 0\n"
                +
            "  }\n"
                +
            "}";
        Map<String, Object> map = null;
        try {
            map = new ObjectMapper().readValue(jsonString, HashMap.class);
        } catch (IOException e) {
            log.debug("could not create valid json for test");
        }
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
            ArgumentMatchers.<Class<Map>>any())).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(map);
        when(mockResponseEntity.getStatusCode()).thenReturn(OK);
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "federated_");
        Assert.assertFalse(userProfileOptional.isPresent());
    }

    @Test
    public void testGetUserWhenResponseBodyIsNull() {
        // Setup
        MockitoAnnotations.initMocks(this);
        ResponseEntity<HashMap> mockResponseEntity = mock(ResponseEntity.class);

        // Configure mock to return null body
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<HashMap>>any()))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(null);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        // Set required properties
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);

        // Call method under test
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "");

        // Verify result
        Assert.assertFalse(userProfileOptional.isPresent());
    }

    @Test
    public void testGetUserWithWso2IdamWhenResponseBodyIsNull() {
        // Setup
        MockitoAnnotations.initMocks(this);
        ResponseEntity<HashMap> mockResponseEntity = mock(ResponseEntity.class);

        // Configure mock to return null body specifically for WSO2_IDAM server
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(HashMap.class)))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(null);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        // Set required properties with WSO2_IDAM server type
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.WSO2_IDAM);
        igniteCoreUserManagementClient.setDefaultLocale("en-US");
        igniteCoreUserManagementClient.setDefaultTimezone("GMT");

        // Call method under test
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "");

        // Verify result - should return empty Optional when map is null
        Assert.assertFalse("Should return empty Optional when response body is null",
                userProfileOptional.isPresent());

        // Verify the exchange method was called with expected parameters
        verify(restTemplate).exchange(
                contains("test-uri.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(HashMap.class));
    }

    @Test
    public void testGetUserWithUnsupportedIdamServer() {
        // Setup
        MockitoAnnotations.initMocks(this);

        // Set unsupported IDAM server type (neither WSO2_IDAM nor UIDAM)
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer("UNSUPPORTED_IDAM");

        // Call method under test
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "");

        // Verify result - should return empty Optional for unsupported server type
        Assert.assertFalse("Should return empty Optional for unsupported IDAM server",
                userProfileOptional.isPresent());

        // Verify that exchange method was never called
        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<?>>any());

        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<?>>any());
    }

    @Test
    public void testGetUserWithUidamServerWithNullTimezone() {
        // Setup
        MockitoAnnotations.initMocks(this);

        // Create a response with null timezone
        ArrayList<LinkedHashMap<String, Object>> responseList = new ArrayList<>();
        LinkedHashMap<String, Object> userMap = new LinkedHashMap<>();
        userMap.put(FIRST_NAME, "John");
        userMap.put(LAST_NAME, "Doe");
        userMap.put(EMAIL, "john.doe@example.com");
        userMap.put(PHONE_NUMBER, "+15551234567");
        userMap.put(USER_CONSENT, "true");
        userMap.put(LOCALE, "en_US");
        userMap.put(TIMEZONE, null); // Explicitly setting timezone to null
        responseList.add(userMap);

        // Fix: Use the proper parameterized type for the mock
        ResponseEntity<ArrayList<LinkedHashMap<String, Object>>> mockResponseEntity =
                mock(ResponseEntity.class);

        // Configure mock to return our response with correct typing
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponseEntity);

        // Fix: Use proper typing in the mock response
        when(mockResponseEntity.getBody()).thenReturn(responseList);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        // Set client properties with UIDAM server type
        String customDefaultTimezone = "America/New_York";
        igniteCoreUserManagementClient.setUri("test-uri.com");
        igniteCoreUserManagementClient.setIdamServer(IgniteCoreConstants.UIDAM);
        igniteCoreUserManagementClient.setDefaultLocale("en-US");
        igniteCoreUserManagementClient.setDefaultTimezone(customDefaultTimezone);

        // Call method under test
        Optional<UserProfile> userProfileOptional = igniteCoreUserManagementClient.getUser("test-user", "");

        // Verify result
        Assert.assertTrue("Should return a user profile", userProfileOptional.isPresent());
        UserProfile userProfile = userProfileOptional.get();
        Assert.assertEquals("Default timezone should be used when timezone is null",
                customDefaultTimezone,
                userProfile.getTimeZone());

        // Verify the exchange method was called with expected parameters
        verify(restTemplate).exchange(
                contains("test-uri.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }
}
