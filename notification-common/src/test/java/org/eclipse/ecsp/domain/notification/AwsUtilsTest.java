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

package org.eclipse.ecsp.domain.notification;

import org.eclipse.ecsp.domain.notification.utils.AwsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

/**
 * AWSUtilsTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/notification-dao-test.properties")
public class AwsUtilsTest {

    @Autowired
    Properties prop;


    @Test
    public void testgetCredentialProvider() {
        AwsUtils.getCredentialProvider(prop);
    }

    @Test
    public void testgetCredentialProvidernull() {
        prop.setProperty("aws.secret.key", "rwerwe");

        prop.setProperty("aws.access.key", "rwerwe");
        AwsUtils.getCredentialProvider(prop);
    }


    @Test
    public void getCredentialsProvidertest() {
        AwsUtils.getCredentialsProvider(prop);
    }

    @Test
    public void getCredentialsProvidertestnull() {
        prop.setProperty("aws.secret.key", "rwerwe");

        prop.setProperty("aws.access.key", "rwerwe");
        AwsUtils.getCredentialsProvider(prop);
    }
}
