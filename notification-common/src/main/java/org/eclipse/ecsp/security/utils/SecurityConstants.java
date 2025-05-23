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

package org.eclipse.ecsp.security.utils;

/**
 * Defines security-related constants used throughout the application.
 *
 * <p>
 * This class provides constant values for cryptographic operations, key identifiers,
 * encryption context, password generation, and supported algorithms. It is not intended
 * to be instantiated.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     String contextKey = SecurityConstants.KMS_CONTEXT_KEY;
 *     int keySize = SecurityConstants.RSA_KEY_GEN_KEY_SIZE;
 * </pre>
 *
 * @author ansarkar
 */
public class SecurityConstants {
    public static final String KMS_CONTEXT_KEY = "ignite-security-key";
    public static final String KMS_CONTEXT_VALUE = "PII";
    public static final String PUB_KEY_IDENTIFIER = "PB";
    public static final String PRI_KEY_IDENTIFIER = "PR";
    public static final String EC_KEY_GEN_CURVE_NAME = "secp256r1";
    public static final int RSA_KEY_GEN_KEY_SIZE = 2048;
    public static final String ECIES = "ECIES";
    public static final String ECDH = "ECDH";
    public static final String SHA_256 = "SHA-256";
    public static final String ECDSA = "ECDH";
    public static final String AES = "AES";
    public static final String[] CSR_ALGO_LIST = {"RSA", "SHA256withECDSA", "SHA512withECDSA"};
    
    /*----- RandomPasswordGenerator Constant - START -----*/
    public static final String ALPHABET_CAPITAL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ALPHABET_SMALL = "abcdefghijklmnopqrstuvwxyz";
    public static final String NUMBERS = "0123456789";
    public static final String SYMBOLS = "!@#$%^&*_=+-/.?<>)";
    public static final String ALPHA_NUMERIC = "ALPHA_NUMERIC";
    public static final String NUMERIC = "NUMERIC";
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 25;
    public static final String PASSWORD_LENGTH_ERROR = "Provided input is not between range " + MIN_LENGTH
            + " and " + MAX_LENGTH;
    public static boolean IS_PRE_CREATED = false;
    /*----- RandomPasswordGenerator Constant - END -----*/

    private SecurityConstants() {
        // Prevent instantiation
    }
}
