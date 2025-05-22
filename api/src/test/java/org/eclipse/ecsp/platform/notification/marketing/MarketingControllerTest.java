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

package org.eclipse.ecsp.platform.notification.marketing;

import org.eclipse.ecsp.domain.notification.MarketingName;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

/**
 * MarketingControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MarketingControllerTest {

    private static final String SESSION_ID = "SESSION_ID";
    private static final String CLIENT_REQUEST_ID = "CLIENT_REQUEST_ID";
    private static final String REQUEST_ID = "REQ";

    @InjectMocks
    private MarketingController marketingController;

    @Mock
    MarketingService marketingService;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void updateMarketingNamesSuccess() {

        MarketingName marketingName = new MarketingName();
        marketingName.setBrandName("kia");
        marketingName.setMarketingName("kia2");

        Mockito.doReturn(true).when(marketingService).updateMarketingName(Mockito.any());
        ResponseEntity<Void> responseEntity =
            marketingController.updateMarketingNames(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                Collections.singletonList(marketingName));
        assertEquals(OK, responseEntity.getStatusCode());
    }

    @Test
    public void updateMarketingNamesFailure() {

        MarketingName marketingName = new MarketingName();
        marketingName.setBrandName("kia");
        marketingName.setMarketingName("kia2");

        Mockito.doReturn(false).when(marketingService).updateMarketingName(Mockito.any());
        ResponseEntity<Void> responseEntity =
            marketingController.updateMarketingNames(REQUEST_ID, SESSION_ID, CLIENT_REQUEST_ID,
                Collections.singletonList(marketingName));
        assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
}