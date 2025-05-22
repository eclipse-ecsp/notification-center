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

package org.eclipse.ecsp.notification.config;

import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.eclipse.ecsp.security.kms.EncryptDecryptKmsImpl;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * TestSecurityLib.
 */
@Configuration
@Profile("test")
public class TestSecurityLib {
    /**
     * get vault.
     */
    @Bean
    public EncryptDecryptInterface getVault() throws Exception {
        EncryptDecryptKmsImpl lib = Mockito.mock(EncryptDecryptKmsImpl.class);
        org.mockito.Mockito.when(lib.encrypt("TestFirstName")).thenReturn("TestFirstName");
        org.mockito.Mockito.when(lib.encrypt("test@harman.com")).thenReturn("1a2b3c4d5f");
        org.mockito.Mockito.when(lib.decrypt("1a2b3c4d5f")).thenReturn("test@harman.com");
        org.mockito.Mockito.when(lib.encrypt("123456789")).thenReturn("12qqq6789");
        org.mockito.Mockito.when(lib.decrypt("12qqq6789")).thenReturn("123456789");

        org.mockito.Mockito.when(lib.encrypt("test@test.com")).thenReturn("test@test.com");
        org.mockito.Mockito.when(lib.encrypt("1231231233")).thenReturn("1231231233");
        org.mockito.Mockito.when(lib.encrypt("+391231231233")).thenReturn("+391231231233");
        org.mockito.Mockito.when(lib.decrypt("TestFirstName")).thenReturn("TestFirstName");
        org.mockito.Mockito.when(lib.decrypt("test@test.com")).thenReturn("test@test.com");
        org.mockito.Mockito.when(lib.decrypt("1231231233")).thenReturn("1231231233");
        org.mockito.Mockito.when(lib.decrypt("+391231231233")).thenReturn("+391231231233");

        org.mockito.Mockito.when(lib.encrypt("akshay.kalra@harman.com")).thenReturn("akshay.kalra@harman.com");
        org.mockito.Mockito.when(lib.encrypt("8699566750")).thenReturn("8699566750");

        return lib;
    }
}
