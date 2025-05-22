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

package org.eclipse.ecsp.domain.notification.dao;

import com.amazonaws.services.pinpoint.model.ChannelType;
import org.eclipse.ecsp.notification.dao.AmazonPinpointEndpointDAOImpl;
import org.eclipse.ecsp.notification.entities.AmazonPinpointEndpoint;
import org.eclipse.ecsp.security.EncryptDecryptInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * AmazonPinpointEndpointDaoImplTest class.
 */
public class AmazonPinpointEndpointDaoImplTest {

    @InjectMocks
    private AmazonPinpointEndpointDAOImpl pinpointDao;

    @Mock
    private EncryptDecryptInterface encryptDecryptInterface;

    private AmazonPinpointEndpoint amazonPinpointEndpoint = null;
    private Map<String, Map<String, String>> endpoints = new HashMap<>();
    private Map<String, String> channelEndpoints = new HashMap<>();

    /**
     * setUp method.
     */
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        pinpointDao = new AmazonPinpointEndpointDAOImpl();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(pinpointDao, "encryptDecryptInterface", encryptDecryptInterface);

        Mockito.when(encryptDecryptInterface.encrypt("address1")).thenReturn("address1_encrypted");
        Mockito.when(encryptDecryptInterface.encrypt("address2")).thenReturn("address2_encrypted");
        Mockito.when(encryptDecryptInterface.encrypt("address3")).thenReturn("address3_encrypted");
        Mockito.when(encryptDecryptInterface.encrypt("address11")).thenReturn("address11_encrypted");
        Mockito.when(encryptDecryptInterface.encrypt("address22")).thenReturn("address22_encrypted");
        Mockito.when(encryptDecryptInterface.encrypt("address33")).thenReturn("address33_encrypted");
        Mockito.when(encryptDecryptInterface.decrypt("address1_encrypted")).thenReturn("address1");
        Mockito.when(encryptDecryptInterface.decrypt("address2_encrypted")).thenReturn("address2");
        Mockito.when(encryptDecryptInterface.decrypt("address3_encrypted")).thenReturn("address3");
        Mockito.when(encryptDecryptInterface.decrypt("address11_encrypted")).thenReturn("address11");
        Mockito.when(encryptDecryptInterface.decrypt("address22_encrypted")).thenReturn("address22");
        Mockito.when(encryptDecryptInterface.decrypt("address33_encrypted")).thenReturn("address33");

        amazonPinpointEndpoint = new AmazonPinpointEndpoint();
        amazonPinpointEndpoint.setUserId("userId");
        channelEndpoints.put("address1", "endpoint1");
        channelEndpoints.put("address2", "endpoint2");
        channelEndpoints.put("address3", "endpoint3");
        endpoints.put(ChannelType.SMS.toString(), channelEndpoints);
        channelEndpoints = new HashMap<>();
        channelEndpoints.put("address11", "endpoint11");
        channelEndpoints.put("address22", "endpoint22");
        channelEndpoints.put("address33", "endpoint33");
        endpoints.put(ChannelType.EMAIL.toString(), channelEndpoints);
        amazonPinpointEndpoint.setEndpoints(endpoints);
    }

    @Test(expected = Exception.class)
    public void testFindById() {
        pinpointDao.findById("id");
    }

    @Test(expected = Exception.class)
    public void testSave() {
        pinpointDao.save(amazonPinpointEndpoint);
    }

    @Test
    public void testEncryptionDecryption() {

        AmazonPinpointEndpoint encryAmazonPinpointEndpoint =
            pinpointDao.encryptPinpointEndpoint(amazonPinpointEndpoint);
        Assert.assertEquals("userId", encryAmazonPinpointEndpoint.getUserId());
        Assert.assertEquals("endpoint1",
            encryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.SMS.toString()).get("address1_encrypted"));
        Assert.assertEquals("endpoint2",
            encryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.SMS.toString()).get("address2_encrypted"));
        Assert.assertEquals("endpoint3",
            encryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.SMS.toString()).get("address3_encrypted"));
        Assert.assertEquals("endpoint11",
            encryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.EMAIL.toString()).get("address11_encrypted"));
        Assert.assertEquals("endpoint22",
            encryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.EMAIL.toString()).get("address22_encrypted"));
        Assert.assertEquals("endpoint33",
            encryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.EMAIL.toString()).get("address33_encrypted"));

        endpoints = new HashMap<>();
        channelEndpoints = new HashMap<>();
        channelEndpoints.put("address1_encrypted", "endpoint1");
        channelEndpoints.put("address2_encrypted", "endpoint2");
        channelEndpoints.put("address3_encrypted", "endpoint3");
        endpoints.put(ChannelType.SMS.toString(), channelEndpoints);
        channelEndpoints = new HashMap<>();
        channelEndpoints.put("address11_encrypted", "endpoint11");
        channelEndpoints.put("address22_encrypted", "endpoint22");
        channelEndpoints.put("address33_encrypted", "endpoint33");
        endpoints.put(ChannelType.EMAIL.toString(), channelEndpoints);
        amazonPinpointEndpoint.setEndpoints(endpoints);

        AmazonPinpointEndpoint decryAmazonPinpointEndpoint = pinpointDao.decryptPinpointEndpoint(
            amazonPinpointEndpoint);
        Assert.assertEquals("userId", decryAmazonPinpointEndpoint.getUserId());
        Assert.assertEquals("endpoint1",
            decryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.SMS.toString()).get("address1"));
        Assert.assertEquals("endpoint2",
            decryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.SMS.toString()).get("address2"));
        Assert.assertEquals("endpoint3",
            decryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.SMS.toString()).get("address3"));
        Assert.assertEquals("endpoint11",
            decryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.EMAIL.toString()).get("address11"));
        Assert.assertEquals("endpoint22",
            decryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.EMAIL.toString()).get("address22"));
        Assert.assertEquals("endpoint33",
            decryAmazonPinpointEndpoint.getEndpoints().get(ChannelType.EMAIL.toString()).get("address33"));

    }

}
