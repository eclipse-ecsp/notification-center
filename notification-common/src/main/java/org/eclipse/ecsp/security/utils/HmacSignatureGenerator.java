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


import jakarta.xml.bind.DatatypeConverter;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SignatureException;
import java.util.Base64;

/**
 * Utility class for generating HMAC signatures using a specified algorithm.
 *
 * <p>
 * This class provides a method to compute a Base64-encoded HMAC signature for a given content
 * and secret key, supporting algorithms such as HmacSHA256 and HmacSHA1. It is typically used
 * for signing webhook payloads or other data requiring integrity and authenticity verification.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     String signature = hmacSignatureGenerator.getSignature(secretKey, content, "HmacSHA256");
 * </pre>
 *
 * @author pbehere
 */

@Component
public class HmacSignatureGenerator {

    /**
     * Calculate signature based on provided algorithm .
     *
     * @param secretKey
     *            the secret key (in string form).
     * @param content
     *            the content to create a signture for OCCS WebHooks this
     *            should be the complete, unmodified body of the post.
     * @param algorithm
     *           the algorithm to use for signing (e.g. HmacSHA256, HmacSHA1,
     * @return The Base64-encoded signature
     * @throws SignatureException
     *           if the signature generation fails.
     */
    public String getSignature(String secretKey, String content, String algorithm)
            throws SignatureException {

        try {
            // Constructs a secret key from the given byte array and algorithm
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(secretKey.getBytes()),
                    algorithm);

            // get the Mac instance for given algorithm
            Mac mac = Mac.getInstance(algorithm);

            // initialize with our key spec
            mac.init(keySpec);

            // generate the signature from the UTF-8 bytes of the content
            byte[] digest = mac.doFinal(content.getBytes("UTF-8"));
            // base64-encode the signature generated ... there's a pre-JDK-8 one
            // tucked away in javax.xml.bind. If using Java 8, use the new
            // java.util.Base64 class instead.
            return DatatypeConverter.printBase64Binary(
                    digest);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate signature: " + e.getMessage(), e);
        }
    }
}
