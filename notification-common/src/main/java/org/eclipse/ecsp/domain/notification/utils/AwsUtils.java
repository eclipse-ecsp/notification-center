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

package org.eclipse.ecsp.domain.notification.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * AWSUtils class.
 */
public class AwsUtils {
    /**
     * getCredentialProvider method.
     *
     * @param props aws properties
     *
     * @return AWSCredentialsProviderChain
     */

    private AwsUtils()
      {}

    /**
     * Get credentials provider.
     *
     * @param props props
     *
     * @return AWSCredentialsProviderChain
     */
    public static AWSCredentialsProviderChain getCredentialProvider(Properties props) {

        AWSCredentialsProviderChain cred = new AWSCredentialsProviderChain(getCredentialsProviderList(props));
        return cred;
    }

    /**
     * getCredentialsProviderListmethod.
     *
     * @param props aws properties
     *
     * @return List of AWSCredentialsProvider
     */
    public static List<AWSCredentialsProvider> getCredentialsProviderList(Properties props) {
        List<AWSCredentialsProvider> providerList = new ArrayList<AWSCredentialsProvider>();
        providerList.add(new ClasspathPropertiesFileCredentialsProvider());
        providerList.add(InstanceProfileCredentialsProvider.getInstance());

        String accessKey = props.getProperty("aws.access.key");
        String secretKey = props.getProperty("aws.secret.key");

        if (!StringUtils.isEmpty(accessKey) && !StringUtils.isEmpty(secretKey)) {
            providerList.add(getCredentialsProvider(accessKey, secretKey));

        }

        return providerList;

    }

    /**
     * getCredentialsProvider method.
     *
     * @param props aws properties
     *
     * @return AWSCredentialsProvider
     */
    public static AWSCredentialsProvider getCredentialsProvider(Properties props) {
        String accessKey = props.getProperty("aws.access.key");
        String secretKey = props.getProperty("aws.secret.key");
        return getCredentialsProvider(accessKey, secretKey);

    }

    /**
     * getCredentialsProvider with given access and secretkey.
     *
     * @param accessKey String
     *
     * @param secretKey String
     *
     * @return AWSCredentialsProvider
     */
    public static AWSCredentialsProvider getCredentialsProvider(String accessKey, String secretKey) {
        AWSCredentialsProvider provider = null;
        if (!StringUtils.isEmpty(accessKey) && !StringUtils.isEmpty(secretKey)) {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            provider = new AWSStaticCredentialsProvider(credentials);

        }
        return provider;
    }
}
