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

package org.eclipse.ecsp.domain.notification;

import org.junit.Test;

/**
 * AmazonSESResponseTest class.
 */
public class AmazonSesResponseTest {

    AmazonSESResponse amazonSesResponse = new AmazonSESResponse();
    AmazonSESResponse amazonSesResponse2 = new AmazonSESResponse("test123");
    AmazonSESResponse amazonSesResponse3 = new AmazonSESResponse("test123", "test123");
    AmazonSESResponse amazonSesResponse4 = new AmazonSESResponse("test123", "test123", "test123");

    @Test
    public void testsetMessageId() {
        amazonSesResponse.setMessageId("test123");
    }

    @Test
    public void testgetMessageId() {
        amazonSesResponse.getMessageId();
    }

    @Test
    public void testgetProvider() {
        amazonSesResponse.getProvider();
    }

    @Test
    public void testtoString() {
        amazonSesResponse.toString();
    }

    @Test
    public void testgetAlertData() {
        amazonSesResponse.getAlertData();
    }

    @Test
    public void testsetAlertData() {
        amazonSesResponse.setAlertData(null);
    }

    @Test
    public void testsetUserId() {
        amazonSesResponse.setUserID("");
    }

    @Test
    public void testgetUserId() {
        amazonSesResponse.getUserID();
    }

    @Test
    public void testgetAlertObject() {
        amazonSesResponse.getAlertObject();
    }

    @Test
    public void testsetAlertObject() {
        amazonSesResponse.setAlertObject(null);
    }

    @Test
    public void testgetPdid() {
        amazonSesResponse.getPdid();
    }

    @Test
    public void testsetPdid() {
        amazonSesResponse.setPdid("");

    }

    @Test
    public void testgetProcessedTime() {
        amazonSesResponse.getProcessedTime();
    }

    @Test
    public void testsetProcessedTime() {
        amazonSesResponse.setProcessedTime("");
    }

    @Test
    public void testgetActualEvent() {
        amazonSesResponse.getActualEvent();
    }

    @Test
    public void testsetActualEvent() {
        amazonSesResponse.setActualEvent(amazonSesResponse);
    }

    @Test
    public void testsetStatus() {
        amazonSesResponse.setStatus("");
    }

    @Test
    public void testgetErrorCode() {
        amazonSesResponse.getErrorCode();
    }

    @Test
    public void testsetErrorCode() {
        amazonSesResponse.setErrorCode(null);
    }

    @Test
    public void testgetStatus() {
        amazonSesResponse.getStatus();
    }

    /*
     * used in api.Added from saas-api
     */
    @Test
    public void testgetDefaultMessage() {
        amazonSesResponse.getDefaultMessage();
    }

    @Test
    public void testgetTemplate() {
        amazonSesResponse.getTemplate();
    }

    @Test
    public void testsetTemplate() {
        amazonSesResponse.setTemplate(null);
    }

    @Test
    public void testsetChannelType() {
        amazonSesResponse.setChannelType(null);
    }

    @Test
    public void testgetChannelType() {
        amazonSesResponse.getChannelType();
    }

    @Test
    public void testsetDestination() {
        amazonSesResponse.setDestination("");
    }

    @Test
    public void testgetDestination() {
        amazonSesResponse.getDestination();
    }

    @Test
    public void toStringtest() {
        amazonSesResponse.toString();
    }
}
