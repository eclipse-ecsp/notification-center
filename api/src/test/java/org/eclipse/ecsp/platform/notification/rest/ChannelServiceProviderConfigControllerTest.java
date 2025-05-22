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

package org.eclipse.ecsp.platform.notification.rest;

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.ChannelServiceProviderConfig;
import org.eclipse.ecsp.platform.notification.service.ChannelServiceProviderConfigService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.mockito.Mockito.when;

/**
 * ChannelServiceProviderConfigControllerTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ChannelServiceProviderConfigControllerTest {

    @InjectMocks
    ChannelServiceProviderConfigController controller;

    @Mock
    ChannelServiceProviderConfigService configServiceImpl;

    @Test
    public void updateNotificationStaticConfigsTest() {

        ChannelServiceProviderConfig config = new ChannelServiceProviderConfig();
        config.setRegion("emea");
        config.setChannelType(ChannelType.EMAIL);
        config.setNotificationId("notificationId1");
        config.setServiceProvider("ses");
        when(configServiceImpl.configure(Mockito.anyList())).thenReturn(true);

        controller.updateNotificationStaticConfigs(null, null, null, Collections.singletonList(config));

        when(configServiceImpl.configure(Mockito.anyList())).thenReturn(false);

        Assert.assertNotNull(controller.updateNotificationStaticConfigs(
                null, null, null, Collections.singletonList(config)));

    }
}

