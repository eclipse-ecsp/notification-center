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

package org.eclipse.ecsp.notification;

import org.eclipse.ecsp.notification.utils.DtcMasterClient;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * test mock SAAS Api.
 */
@Configuration
public class TestMockSaaaApi {

    /**
     * get DTC list.
     */
    @Bean
    @Primary
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Profile("test")
    public DtcMasterClient getDtcList() throws Exception {
        DtcMasterClient dtcMasterClient = Mockito.mock(DtcMasterClient.class);
        String[] dtcList = {"ISO/SAE Reserved", "ISO/SAE Reserved"};
        List<String> setList = new ArrayList<String>();

        String[] setIds = {"12", "34"};
        Collections.addAll(setList, setIds);
        org.mockito.Mockito.when(dtcMasterClient.getDtcList(setList)).thenReturn(dtcList);

        return dtcMasterClient;

    }

}
