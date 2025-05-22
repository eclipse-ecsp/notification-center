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
import org.eclipse.ecsp.notification.dao.MarketingNameDao;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * MarketingServiceTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MarketingServiceTest {

    @InjectMocks
    private MarketingService marketingService;

    @Mock
    private MarketingNameDao marketingNameDao;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void updateMarketingNameSuccess() {

        MarketingName marketingName = new MarketingName();
        marketingName.setBrandName("kia");
        marketingName.setModel("kia1");
        marketingName.setMarketingName("kia2");

        Mockito.doReturn(Collections.singletonList(marketingName)).when(marketingNameDao)
            .findByMakeAndModel(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(true).when(marketingNameDao).update(Mockito.any());
        boolean result = marketingService.updateMarketingName(Collections.singletonList(marketingName));
        assertTrue(result);
    }


    @Test
    public void updateMarketingNameFailure() {

        MarketingName marketingName = new MarketingName();
        marketingName.setBrandName("kia");
        marketingName.setModel("kia1");
        marketingName.setMarketingName("kia2");

        Mockito.doReturn(Collections.singletonList(marketingName)).when(marketingNameDao)
            .findByMakeAndModel(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doThrow(new RuntimeException("error")).when(marketingNameDao).update(Mockito.any());
        boolean result = marketingService.updateMarketingName(Collections.singletonList(marketingName));
        assertFalse(result);
    }

    @Test
    public void createNewMarketingNameSuccess() {

        MarketingName marketingName = new MarketingName();

        marketingName.setBrandName("kia");
        marketingName.setMarketingName("kia2");

        List<MarketingName> marketingNames = new ArrayList<>();

        Mockito.doReturn(marketingNames).when(marketingNameDao)
            .findByMakeAndModel(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(marketingName).when(marketingNameDao).save(Mockito.any());
        boolean result = marketingService.updateMarketingName(Collections.singletonList(marketingName));
        assertTrue(result);
    }

    @Test
    public void createNewMarketingNameFailure() {

        MarketingName marketingName = new MarketingName();
        marketingName.setBrandName("kia");
        marketingName.setMarketingName("kia2");

        List<MarketingName> marketingNames = new ArrayList<>();

        Mockito.doReturn(marketingNames).when(marketingNameDao)
            .findByMakeAndModel(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doThrow(new RuntimeException("error")).when(marketingNameDao).save(Mockito.any());
        boolean result = marketingService.updateMarketingName(Collections.singletonList(marketingName));
        assertFalse(result);
    }


}