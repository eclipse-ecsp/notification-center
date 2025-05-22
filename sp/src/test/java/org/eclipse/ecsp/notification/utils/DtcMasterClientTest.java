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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;


/**
 * DTCMasterClientTest.
 */
public class DtcMasterClientTest {

    @InjectMocks
    DtcMasterClient dtcMasterClient;

    @Mock
    private RestTemplate restTemplate;

    private MemoryAppender memoryAppender;

    /**
     * set up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Logger logger = (Logger) LoggerFactory.getLogger(DtcMasterClient.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void getDtcListSuccess() {

        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("{\"OP11\":{\"description\":\"a\"},\"OP12\":{\"description\":\"b\"}}", HttpStatus.OK);
        ReflectionTestUtils.setField(dtcMasterClient, "dtcMasterServiceUrl", "https://stam.com");
        doReturn(responseEntity).when(restTemplate)
            .exchange(anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<String>>any());

        String[] result = dtcMasterClient.getDtcList(null);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
    }

    @Test
    public void getDtcListParseFailure() {
        memoryAppender.reset();
        ResponseEntity<String> responseEntity =
            new ResponseEntity<>("[{\"OP11\":{\"description\":\"a\"},\"OP12\":{\"description\":\"b\"}}", HttpStatus.OK);
        ReflectionTestUtils.setField(dtcMasterClient, "dtcMasterServiceUrl", "https://stam.com");
        doReturn(responseEntity).when(restTemplate)
            .exchange(anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<String>>any());

        String[] result = dtcMasterClient.getDtcList(null);
        assertNotNull(result);
        assertEquals(1, memoryAppender.search("Exception occurred due to", Level.ERROR).size());
    }

    @Test
    public void getDtcListRestFailure() {
        memoryAppender.reset();
        ReflectionTestUtils.setField(dtcMasterClient, "dtcMasterServiceUrl", "https://stam.com");
        doThrow(new RuntimeException("rest failure")).when(restTemplate)
            .exchange(anyString(), any(HttpMethod.class), any(), ArgumentMatchers.<Class<String>>any());

        String[] result = dtcMasterClient.getDtcList(null);
        assertNull(result);
        assertEquals(1,
            memoryAppender.search("Unable to retrieve the response from DTCMasterClient due to", Level.ERROR).size());
    }

}