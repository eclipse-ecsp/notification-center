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
 * Enumeration of standard error and status messages used across the security module.
 *
 * <p>
 * This enum provides predefined message strings for common error scenarios such as
 * invalid KMS ARN, encryption context mismatches, uninitialized S3 clients, invalid DTOs,
 * invalid S3 bucket/key, and empty or null HSM passwords.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     throw new EncryptionDecryptionException(MessageEnum.WRONG_KMS_ARN.value());
 * </pre>
 *
 * @author ansarkar
 */
public enum MessageEnum {
    WRONG_KMS_ARN("Wrong kms arn"),
    WRONG_ENCRYPTION_CONTEXT("Wrong Encryption Context"),
    S3_CLIENT_UNINITITALIZED("S3 client not initiatlized"),
    INVALID_DTO("Invalid dto"),
    INVALID_BUCKET_KEY("Invalid bucket/key"),
    INVALID_HSM_PASSWORD("HSM password is empty or null");
    private String msg;

    MessageEnum(String msg) {
        this.msg = msg;
    }

    public String value() {
        return msg;
    }
}
