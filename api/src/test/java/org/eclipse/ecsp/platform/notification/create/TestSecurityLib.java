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

package org.eclipse.ecsp.platform.notification.create;

import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.eclipse.ecsp.security.kms.EncryptDecryptKmsImpl;
import org.eclipse.ecsp.services.utils.VehicleProfileClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.Optional;

/**
 * TestSecurityLib class.
 */
@Configuration
@Profile("test")
public class TestSecurityLib {

    /**
     * getVault method.
     */
    @Bean
    public EncryptDecryptInterface getVault() throws Exception {
        EncryptDecryptKmsImpl lib = Mockito.mock(EncryptDecryptKmsImpl.class);
        org.mockito.Mockito.when(lib.encrypt("test@harman.com")).thenReturn("1a2b3c4d5f");
        org.mockito.Mockito.when(lib.decrypt("1a2b3c4d5f")).thenReturn("test@harman.com");
        org.mockito.Mockito.when(lib.encrypt("123456789")).thenReturn("12qqq6789");
        org.mockito.Mockito.when(lib.decrypt("12qqq6789")).thenReturn("123456789");
        org.mockito.Mockito.when(lib.encrypt("secondary_email@harman.com")).thenReturn("6g1a2b3c4d5f7h8j");
        org.mockito.Mockito.when(lib.decrypt("6g1a2b3c4d5f7h8j")).thenReturn("secondary_email@harman.comss");
        org.mockito.Mockito.when(lib.encrypt("222222222")).thenReturn("32qqq2224");
        org.mockito.Mockito.when(lib.decrypt("32qqq2224")).thenReturn("222222222");

        return lib;
    }

    /**
     * getClient.
     */
    @Bean
    public VehicleProfileClient getClient() throws IOException {
        String simulatedVehicleProfileData = IOUtils.toString(
            TestSecurityLib.class.getResourceAsStream("/SimulatedVehicleProfileDataForEntitlementTest.json"),
            "UTF-8");

        VehicleProfileClient vpc = Mockito.mock(VehicleProfileClient.class);
        Optional<String> vp = Optional.of(simulatedVehicleProfileData);
        Mockito.when(vpc.getVehicleProfileJson(Mockito.any(String.class))).thenReturn(vp);
        return vpc;
    }
}
