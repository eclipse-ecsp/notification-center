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

package org.eclipse.ecsp.notification.processors;

import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.entities.VehicleProfileOnDemandAttribute;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * CoreVehicleProfileClientTest class.
 */
@Configuration
@Profile("test")
public class CoreVehicleProfileClientTestConfig {

    /**
     * getClient method.
     *
     * @return CoreVehicleProfileClient
     */
    @Bean
    @Primary
    public CoreVehicleProfileClient getClient() {

        Map<VehicleProfileAttribute, Optional<String>> attrs = new HashMap<VehicleProfileAttribute, Optional<String>>();

        attrs.put(VehicleProfileAttribute.MAKE, Optional.of("fita"));
        attrs.put(VehicleProfileAttribute.MODEL, Optional.of("Punto"));
        attrs.put(VehicleProfileAttribute.MODEL_YEAR, Optional.of("2019"));
        attrs.put(VehicleProfileAttribute.NAME, Optional.of("HUXOIDDN4HUN18"));
        attrs.put(VehicleProfileAttribute.USERID, Optional.of("HUXOIDDN4HUN18"));

        CoreVehicleProfileClient coreVehicleProfileClient = Mockito.mock(CoreVehicleProfileClient.class);
        Optional<String> data = Optional.of("FORD_F-150_2001");
        Mockito.when(
            coreVehicleProfileClient.getVehicleProfileAttributes(Mockito.any(String.class), Mockito.anyBoolean(),
                Mockito.any(VehicleProfileAttribute.class))).thenReturn(attrs);
        // Mockito.when(vpc.getVehicleProfileAttributesWithClientId(Mockito.any(String.class),Mockito.any(Boolean.class)
        // ,Mockito.any(VehicleProfileAttribute.class))).thenReturn(attrs);
        Map<String, Optional<?>> attr = new HashMap<>();

        attr.put("make", Optional.of("fita"));
        attr.put("model", Optional.of("Punto"));
        attr.put("modelYear", Optional.of("2019"));
        attr.put("name", Optional.of("HUXOIDDN4HUN18"));
        attr.put("vin", Optional.of("HUXOIDDN4HUN18"));

        Mockito.when(
            coreVehicleProfileClient.getVehicleProfileAttributes(Mockito.any(String.class), Mockito.anyBoolean(),
                Mockito.any(VehicleProfileOnDemandAttribute.class))).thenReturn(attr);
        return coreVehicleProfileClient;
    }

}