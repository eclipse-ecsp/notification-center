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

package org.eclipse.ecsp.platform.notification.service;

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.ChannelServiceProviderConfig;
import org.eclipse.ecsp.notification.config.ChannelServiceProviderConfigDAO;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * ChannelServiceProviderConfigServiceImplTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ChannelServiceProviderConfigServiceImplTest {

    @InjectMocks
    private ChannelServiceProviderConfigServiceImpl service;

    @Mock
    private ChannelServiceProviderConfigDAO channelServiceProviderConfigDao;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void testUpdateConfig() {


        ChannelServiceProviderConfig config = new ChannelServiceProviderConfig();
        config.setChannelType(ChannelType.EMAIL);
        config.setNotificationId("lite_invite");
        config.setRegion("emea");
        config.setServiceProvider("ses");

        List<ChannelServiceProviderConfig> configList = new ArrayList<>();
        configList.add(config);

        assertTrue(service.configure(configList));

    }
}
