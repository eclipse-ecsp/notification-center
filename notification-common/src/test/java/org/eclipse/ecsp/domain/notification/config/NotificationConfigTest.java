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

package org.eclipse.ecsp.domain.notification.config;

import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.ApiPushChannel;
import org.eclipse.ecsp.domain.notification.Channel;
import org.eclipse.ecsp.domain.notification.EmailChannel;
import org.eclipse.ecsp.domain.notification.PushChannel;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NotificationConfigTest class.
 */
public class NotificationConfigTest {

    @Test
    public void defaultNotificationConfigWhenBrandSpecified() {
        NotificationConfigRequest request = new NotificationConfigRequest();
        request.setBrand("brand1");

        NotificationConfig config = NotificationConfig.defaultNotificationConfig(request, "default");
        Assert.assertEquals("brand1", config.getBrand());
    }

    @Test
    public void addChannel() {
        NotificationConfigRequest request = new NotificationConfigRequest();
        request.setBrand("brand1");
        NotificationConfig config = NotificationConfig.defaultNotificationConfig(request, "default");
        config.addChannel(null);
    }

    @Test
    public void addAllIfAbsentnotNull() {
        NotificationConfigRequest request = new NotificationConfigRequest();
        request.setBrand("brand1");
        NotificationConfig config = NotificationConfig.defaultNotificationConfig(request, "default");
        EmailChannel ech = new EmailChannel();
        List<Channel> newChannels = new ArrayList<Channel>();
        newChannels.add(ech);
        config.addAllIfAbsent(newChannels);
        SmsChannel sms = new SmsChannel();
        newChannels.add(sms);
        config.addAllIfAbsent(newChannels);
    }

    @Test
    public void addAllIfAbsent() {
        NotificationConfigRequest request = new NotificationConfigRequest();
        request.setBrand("brand1");
        NotificationConfig config = NotificationConfig.defaultNotificationConfig(request, "default");
        config.addAllIfAbsent(null);
    }

    @Test
    public void addHashcode() {
        NotificationConfigRequest request = new NotificationConfigRequest();
        request.setBrand("brand1");
        NotificationConfig config = NotificationConfig.defaultNotificationConfig(request, "default");
        config.hashCode();
        config.equals(config);
        config.toString();
    }

    @Test
    public void defaultNotificationConfigWhenBrandIsEmpty() {
        NotificationConfigRequest request = new NotificationConfigRequest();

        NotificationConfig config = NotificationConfig.defaultNotificationConfig(request, "default");
        Assert.assertEquals("default", config.getBrand());
    }

    @Test
    public void testDiff() {
        List<Channel> channels = new ArrayList<>();
        channels.add(new PushChannel("apns", "test", Arrays.asList("839238", "63929")));
        channels.add(new EmailChannel(Arrays.asList("123@abc.com", "abc@xyz.com")));
        channels.add(new SmsChannel(Arrays.asList("12345", "456789")));
        NotificationConfig firstConfig = new NotificationConfig();

        firstConfig.setChannels(channels);

        List<Channel> newChannels = new ArrayList<>();
        newChannels.add(new PushChannel("apns", "test", Arrays.asList("839238", "824872")));
        newChannels.add(new EmailChannel(Arrays.asList("123@abc.com", "456@xyz.com")));
        newChannels.add(new SmsChannel(Arrays.asList("321987", "456789")));
        NotificationConfig secondConfig = new NotificationConfig();

        secondConfig.setChannels(newChannels);

        List<Channel> deletions = new ArrayList<>();
        List<Channel> additions = new ArrayList<>();
        firstConfig.diffChannels(secondConfig, deletions, additions);
        Assert.assertEquals(
            Arrays.asList(new PushChannel("apns", "test", Arrays.asList("63929")),
                new EmailChannel(Arrays.asList("abc@xyz.com")),
                new SmsChannel(Arrays.asList("12345"))),
            deletions);
        Assert.assertEquals(
            Arrays.asList(new PushChannel("apns", "test", Arrays.asList("824872")),
                new EmailChannel(Arrays.asList("456@xyz.com")),
                new SmsChannel(Arrays.asList("321987"))),
            additions);
    }

    @Test
    public void testPatch() {
        NotificationConfig firstConfig = new NotificationConfig();
        firstConfig.setEnabled(false);
        List<Channel> channels = new ArrayList<>();
        channels.add(new PushChannel("apns", "test", Arrays.asList("839238", "63929")));
        channels.add(new EmailChannel(Arrays.asList("123@abc.com", "abc@xyz.com")));
        channels.add(new SmsChannel(Arrays.asList("12345", "456789")));
        firstConfig.setChannels(channels);

        NotificationConfig secondConfig = new NotificationConfig();
        secondConfig.setEnabled(true);
        List<Channel> newChannels = new ArrayList<>();
        newChannels.add(new PushChannel("apns", "test", Arrays.asList("152333", "824872")));
        newChannels.add(new SmsChannel(Arrays.asList("321987")));
        secondConfig.setChannels(newChannels);

        NotificationConfig expectedConfig = secondConfig.deepClone();
        expectedConfig.getChannels().add(new EmailChannel(Arrays.asList("123@abc.com", "abc@xyz.com")));

        firstConfig.patch(secondConfig);
        expectedConfig.getChannels().forEach(c ->
            Assert.assertEquals(c,
                firstConfig.getChannels().stream().filter(fc -> c.getChannelType().equals(fc.getChannelType()))
                    .findFirst().get()));
        Assert.assertEquals(expectedConfig.getChannels().size(), firstConfig.getChannels().size());
    }

    @Test
    public void testDisableChannelExcept() {
        NotificationConfig nc1 = createNotificationConfig("test_user", "test_vehicle", "self", "ParentalControls",
            createChannels(new SmsChannel("3437634724"), new EmailChannel(Arrays.asList("test@gmail.com")),
                new ApiPushChannel()));

        nc1.getChannels().forEach(channel -> {
            channel.setEnabled(true);
        });
        nc1.disableChannelsExcept(ChannelType.EMAIL);
        Assert.assertEquals(1, nc1.getEnabledChannels().size());
        Assert.assertEquals(ChannelType.EMAIL, nc1.getEnabledChannels().get(0).getChannelType());
    }

    @Test
    public void isSimilar() {
        NotificationConfig config1 = new NotificationConfig();
        NotificationConfig config2 = new NotificationConfig();
        Assert.assertTrue(NotificationConfig.isSimilar(config1, config2));
    }

    @Test
    public void isSimilarWhenGroupIsDifferent() {
        NotificationConfig config1 = new NotificationConfig();
        config1.setGroup("group1");
        NotificationConfig config2 = new NotificationConfig();
        config1.setGroup("group2");
        Assert.assertFalse(NotificationConfig.isSimilar(config1, config2));
    }

    @Test
    public void isSimilarWhenUserIdIsDifferent() {
        NotificationConfig config1 = new NotificationConfig();
        config1.setUserId("user1");
        NotificationConfig config2 = new NotificationConfig();
        config1.setUserId("user2");
        Assert.assertFalse(NotificationConfig.isSimilar(config1, config2));
    }

    @Test
    public void isSimilarWhenVehicleIdIsDifferent() {
        NotificationConfig config1 = new NotificationConfig();
        config1.setVehicleId("vehicle1");
        NotificationConfig config2 = new NotificationConfig();
        config1.setVehicleId("vehicle2");
        Assert.assertFalse(NotificationConfig.isSimilar(config1, config2));
    }

    @Test
    public void isSimilarWhenBrandIsDifferent() {
        NotificationConfig config1 = new NotificationConfig();
        config1.setBrand("brand1");
        NotificationConfig config2 = new NotificationConfig();
        config1.setBrand("brand2");
        Assert.assertFalse(NotificationConfig.isSimilar(config1, config2));
    }

    @Test
    public void isSimilarWhenContactIdIsDifferent() {
        NotificationConfig config1 = new NotificationConfig();
        config1.setContactId("contact1");
        NotificationConfig config2 = new NotificationConfig();
        config1.setContactId("contact2");
        Assert.assertFalse(NotificationConfig.isSimilar(config1, config2));
    }

    private List<Channel> createChannels(Channel... chArray) {
        List<Channel> channels = new ArrayList<>();
        for (int i = 0; i < chArray.length; i++) {
            channels.add(chArray[i]);
        }
        return channels;
    }

    private NotificationConfig createNotificationConfig(String userId, String vehicleId, String contactId, String group,
                                                        List<Channel> channels) {
        NotificationConfig nc = new NotificationConfig();
        nc.setChannels(channels);
        nc.setEnabled(true);
        nc.setGroup(group);
        nc.setSchemaVersion(Version.V1_0);
        nc.setUserId(userId);
        nc.setVehicleId(vehicleId);
        nc.setContactId(contactId);
        return nc;
    }

}
