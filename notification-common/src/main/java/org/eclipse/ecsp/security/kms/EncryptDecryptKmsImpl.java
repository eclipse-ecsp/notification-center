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

package org.eclipse.ecsp.security.kms;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.CryptoAlgorithm;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.eclipse.ecsp.security.EncryptionDecryptionException;
import org.eclipse.ecsp.security.utils.MessageEnum;
import org.eclipse.ecsp.security.utils.SecurityConstants;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;


/**
 * Utility class to help handle encryption decryption for AWS KMS(Key Management
 * Service).
 *
 * @author ansarkar
 *
 */
@Profile("!test")
@Component
@ConditionalOnProperty(name = "cloud.service.provider", havingValue = "aws")
public class EncryptDecryptKmsImpl implements EncryptDecryptInterface {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(EncryptDecryptKmsImpl.class);

    @Value("${ignite.kmsArn}")
    private String kmsArn;

    private AwsCrypto crypto;

    private KmsMasterKeyProvider kmsProvider;

    private Map<String, String> context;

    /**
     * Initializes the AWS KMS encryption/decryption components.
     *
     * <p>This method sets up the {@link AwsCrypto} instance with a specific encryption algorithm
     * and commitment policy, configures the {@link KmsMasterKeyProvider} using the provided KMS ARN,
     * and prepares the encryption context for integrity protection.
     * It is automatically invoked after dependency injection is complete.
     */
    @PostConstruct
    public void init() {

        // Instantiate the SDK
        //crypto = new AwsCrypto();
        // AES 256 bit encryption with HKDF and SHA is being used as encryption
        // algorithm
        //crypto.setEncryptionAlgorithm(CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384);
        crypto = AwsCrypto.builder()
                .withCommitmentPolicy(CommitmentPolicy.ForbidEncryptAllowDecrypt)
                .withEncryptionAlgorithm(CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
                .build();
        // Set up the KmsMasterKeyProvider
        kmsProvider = KmsMasterKeyProvider.builder()
                .buildStrict(kmsArn);

        // Setup encryption context to protect integrity
        context = Collections.singletonMap(SecurityConstants.KMS_CONTEXT_KEY, SecurityConstants.KMS_CONTEXT_VALUE);
        LOGGER.info("EncryptDecryptKmsImpl initialization completed");
    }

    /**
     * Encrypts the given plain text using AWS KMS with the configured encryption context.
     *
     * @param plainText the plain text string to encrypt
     * @return the encrypted cipher text as a string
     * @throws EncryptionDecryptionException if encryption fails
     */
    public String encrypt(String plainText) {
        final String ciphertext = crypto.encryptString(kmsProvider, plainText, context).getResult();
        LOGGER.info("EncryptDecryptKmsImpl encryption completed");
        return ciphertext;
    }

    /**
     * Decrypts the given cipher text using AWS KMS and validates the encryption context and KMS ARN.
     *
     * @param cipherText the encrypted cipher text to decrypt
     * @return the decrypted plain text string
     * @throws EncryptionDecryptionException if decryption fails or validation checks do not pass
     */
    public String decrypt(String cipherText) {
        final CryptoResult<String, KmsMasterKey> decryptResult = crypto.decryptString(kmsProvider, cipherText);
        // Before returning the plaintext, verify that the customer master key
        // that was used in the encryption operation was the one supplied to the
        // master key provider.
        if (!decryptResult.getMasterKeyIds().get(0).equals(kmsArn)) {
            LOGGER.error("{}", MessageEnum.WRONG_KMS_ARN.value());
            throw new EncryptionDecryptionException(MessageEnum.WRONG_KMS_ARN.value());
        }
        // Also, verify that the encryption context in the result contains the
        // encryption context supplied to the encryptString method. Because the
        // SDK can add values to the encryption context, don't require that
        // the entire context matches.
        for (final Map.Entry<String, String> e : context.entrySet()) {
            if (!e.getValue().equals(decryptResult.getEncryptionContext().get(e.getKey()))) {
                LOGGER.error("{}", MessageEnum.WRONG_ENCRYPTION_CONTEXT.value());
                throw new EncryptionDecryptionException(MessageEnum.WRONG_ENCRYPTION_CONTEXT.value());
            }
        }
        String plainText = decryptResult.getResult();
        LOGGER.info("EncryptDecryptKmsImpl decryption completed");
        return plainText;
    }

}
