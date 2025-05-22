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

import org.eclipse.ecsp.domain.notification.exceptions.AuthorizationException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * AssociationServiceClientTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AssociationServiceClientTest {

    @InjectMocks
    private AssociationServiceClient associationServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getDevicesSuccess() throws Exception {

        ResponseEntity<String> response = new ResponseEntity<>(
            "[{\"associationId\": \"aid\", \"associationStatus\": \"ASSOCIATED\", \"deviceId\": \"did\"}]",
            HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(),
            ArgumentMatchers.<Class<String>>any())).thenReturn(response);

        associationServiceClient.setAssociationServiceUrl("a/s");
        Optional<List<DeviceAssociation>> deviceAssociationListOptional = associationServiceClient.getDevices("uid");
        assertTrue(deviceAssociationListOptional.isPresent());
        List<DeviceAssociation> associationList = deviceAssociationListOptional.get();
        assertEquals(1, associationList.size());
        DeviceAssociation deviceAssociation = associationList.get(0);
        assertEquals("aid", deviceAssociation.getAssociationId());
        assertEquals("ASSOCIATED", deviceAssociation.getAssociationStatus());
        assertEquals("did", deviceAssociation.getHarmanID());
    }

    @Test
    public void getDevicesUnauthorizedException() {

        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)).when(restTemplate)
            .exchange(anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<String>>any());

        associationServiceClient.setAssociationServiceUrl("http://dd");

        AuthenticationException thrown =
            assertThrows(AuthenticationException.class,
                () -> associationServiceClient.getDevices("uid"),
                "Expected to throw, but it didn't");
        assertEquals("Authentication exception to retrieve user information", thrown.getMessage());
    }

    @Test
    public void getDevicesForbiddenException() {

        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(restTemplate)
            .exchange(anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<String>>any());

        associationServiceClient.setAssociationServiceUrl("http://dd");

        AuthorizationException thrown =
            assertThrows(AuthorizationException.class,
                () -> associationServiceClient.getDevices("uid"),
                "Expected to throw, but it didn't");
        assertEquals("Unauthorized to retreive user association", thrown.getMessage());
    }

    @Test
    public void getDevicesException() {

        doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(restTemplate)
            .exchange(anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<String>>any());

        associationServiceClient.setAssociationServiceUrl("http://dd");

        UnknownError thrown =
            assertThrows(UnknownError.class,
                () -> associationServiceClient.getDevices("uid"),
                "Expected to throw, but it didn't");
        assertEquals("Unknown exception while retrieving user associations", thrown.getMessage());
    }


    /**
     * handling NPE so not in scope.
     */
    //    @Test handling NPE so not in scope
    public void getDevicesNullException() {
        ResponseEntity<String> response = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(),
            ArgumentMatchers.<Class<String>>any())).thenReturn(response);

        associationServiceClient.setAssociationServiceUrl("http://dd");

        Exception thrown =
            assertThrows(Exception.class,
                () -> associationServiceClient.getDevices("uid"),
                "Expected to throw, but it didn't");
        assertEquals("Unknown exception while retrieving user associations", thrown.getMessage());
    }
}