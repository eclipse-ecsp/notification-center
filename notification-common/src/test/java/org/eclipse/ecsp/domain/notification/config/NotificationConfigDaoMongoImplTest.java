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

import com.google.common.collect.Sets;
import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.SmsChannel;
import org.eclipse.ecsp.domain.notification.grouping.TestSecurityLibSample;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.config.NotificationConfig;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.nosqldao.Operator.EQ;

/**
 * NotificationConfigDaoMongoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class,
    DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class NotificationConfigDaoMongoImplTest {

    @Autowired
    private NotificationConfigDAO notificationConfigDao;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    private String three = "3";

    @Test
    public void testFindByUserVehicleAndGroup() {
        List<String> userIds =
            Arrays.asList("noname001", "noname001", NotificationConfig.USER_ID_FOR_DEFAULT_PREFERENCE);
        List<String> vehicleIds = Arrays.asList("HJKJDHS&78983DJ", NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE,
            NotificationConfig.VEHICLE_ID_FOR_DEFAULT_PREFERENCE);
        List<NotificationConfig> originalConfigs = new ArrayList<>();
        for (int i = 0; i < Integer.valueOf(three); i++) {
            NotificationConfig nc = createNotificationConfig(userIds.get(0), vehicleIds.get(0));
            originalConfigs.add(nc);
            notificationConfigDao.save(nc);
        }
        List<NotificationConfig> ncList = notificationConfigDao.findByUserVehicleGroup(userIds, vehicleIds,
            originalConfigs.get(0).getGroup());
        Assert.assertEquals(originalConfigs, ncList);
        // make sure we get back configs for all user ids requested
        Assert.assertEquals(Integer.parseInt(three),
            ncList.stream().filter(nc -> userIds.contains(nc.getUserId())).collect(Collectors.toList()).size());
        // make sure we get back configs for all vehicle ids requested
        Assert.assertEquals(Integer.parseInt(three),
            ncList.stream().filter(nc -> vehicleIds.contains(nc.getVehicleId())).collect(Collectors.toList()).size());
        Assert.assertEquals(Integer.parseInt(three),
            ncList.stream().filter(nc -> originalConfigs.get(0).getGroup().equals(nc.getGroup()))
                .collect(Collectors.toList()).size());
        Assert.assertEquals(Integer.parseInt(three),
            ncList.stream().filter(nc -> originalConfigs.get(0).getChannels().equals(nc.getChannels()))
                .collect(Collectors.toList())
                .size());
    }

    @Test
    public void testFindByUserVehicle() {
        String userId = "noname002";
        String vehicleId = "JBJKBBD29&78983DJ";
        List<NotificationConfig> originalConfigs = new ArrayList<>();
        for (int i = 0; i < Integer.valueOf(three); i++) {
            NotificationConfig nc = createNotificationConfig(userId, vehicleId);
            originalConfigs.add(nc);
            notificationConfigDao.save(nc);
        }
        List<NotificationConfig> ncList = notificationConfigDao.findByUserVehicle(userId, vehicleId);
        NotificationConfig nc = ncList.get(0);
        Assert.assertEquals(userId, nc.getUserId());
        Assert.assertEquals(vehicleId, nc.getVehicleId());
    }

    @Test
    public void testNotFoundDefaultGroups() {
        List<NotificationConfig> configEntities =
            notificationConfigDao.findDefaultByGroups(Collections.singleton("group"));
        Assert.assertTrue(CollectionUtils.isEmpty(configEntities));
    }

    @Test
    public void testFindOneDefaultByGroups() {
        final String group = "noname003";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setEnabled(true);
        notificationConfigDao.save(config);

        List<NotificationConfig> configEntities =
            notificationConfigDao.findDefaultByGroups(Collections.singleton(group));

    }

    @Test
    public void testFindTwoDefaultByGroups() {
        final String group1 = "noname004";
        NotificationConfig config1 = new NotificationConfig();
        config1.setGroup(group1);
        config1.setEnabled(true);

        final String group2 = "noname005";
        NotificationConfig config2 = new NotificationConfig();
        config2.setGroup(group2);
        config2.setEnabled(true);
        notificationConfigDao.saveAll(config1, config2);

        List<NotificationConfig> configEntities =
            notificationConfigDao.findDefaultByGroups(Sets.newHashSet(group1, group2));

    }

    @Test
    public void testNotFoundDefaultGroupsAndBrand() {
        List<NotificationConfig> configEntities =
            notificationConfigDao.findDefaultByGroupsAndBrand(Collections.singleton("group"), "brand1");
        Assert.assertTrue(CollectionUtils.isEmpty(configEntities));
    }

    @Test
    public void testFindOneDefaultByGroupsAndBrandIgnoreCase() {
        final String group = "noname003";
        final String brand = "brand1";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setBrand(brand);
        config.setEnabled(true);
        notificationConfigDao.save(config);

        List<NotificationConfig> configEntities =
            notificationConfigDao.findDefaultByGroupsAndBrand(Collections.singleton(group),
                brand.toUpperCase(Locale.ROOT));

    }

    @Test
    public void testfindByUserVehicleContactId() {
        final String group = "noname003";
        final String brand = "brand1";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setBrand(brand);
        config.setEnabled(true);
        config.setUserId("test123");
        config.setVehicleId("test123");
        config.setContactId("test123");
        notificationConfigDao.save(config);

        List<NotificationConfig> configEntities =
            notificationConfigDao.findByUserVehicleContactId("test123", "test123", "test123");
    }

    @Test
    public void testfindByUserIdAndVehicleIdAndContactIdAndGroups() {
        final String group = "noname003";
        final String brand = "brand1";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setBrand(brand);
        config.setEnabled(true);
        config.setUserId("test123");
        config.setVehicleId("test123");
        config.setContactId("test123");
        notificationConfigDao.save(config);
        Set<String> hashSet = new HashSet<String>();
        hashSet.add(group);
        List<NotificationConfig> configEntities =
            notificationConfigDao.findByUserIdAndVehicleIdAndContactIdAndGroups("test123", "test123", "test123",
                    hashSet);
    }

    @Test
    public void testdeleteConfigForContact() {
        final String group = "noname003";
        final String brand = "brand1";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setBrand(brand);
        config.setEnabled(true);
        config.setUserId("test123");
        config.setVehicleId("test123");
        config.setContactId("test123");
        notificationConfigDao.save(config);

        boolean configEntities = notificationConfigDao.deleteConfigForContact("test123", "test123");


    }

    @Test
    public void testdeleteNotificationConfigByUserAndVehicle() {
        final String group = "noname003";
        final String brand = "brand1";
        NotificationConfig config = new NotificationConfig();
        config.setGroup(group);
        config.setBrand(brand);
        config.setEnabled(true);
        config.setUserId("test123");
        config.setVehicleId("test123");
        config.setContactId("test123");
        notificationConfigDao.save(config);

        boolean configEntities = notificationConfigDao.deleteNotificationConfigByUserAndVehicle("test123", "test123");
    }

    @Ignore
    @Test
    public void testDeleteNotificationConfigByUserAndVehicle() {
        notificationConfigDao.deleteNotificationConfigByUserAndVehicle("user", "vehicle");
        IgniteCriteria c1 = new IgniteCriteria("userId", EQ, "user");
        IgniteCriteriaGroup cg = new IgniteCriteriaGroup(c1);
        cg.and(new IgniteCriteria("vehicleId", EQ, "vehicle"));
        IgniteQuery igQuery = new IgniteQuery(cg);
        Mockito.verify(notificationConfigDao, Mockito.times(1)).deleteByQuery(igQuery);
    }

    private NotificationConfig createNotificationConfig(String userId, String vehicleId) {
        NotificationConfig nc = new NotificationConfig();
        nc.setChannels(Arrays.asList(new SmsChannel(new ArrayList<String>(Arrays.asList("123456789")))));
        nc.setEnabled(true);
        nc.setGroup("ParentalControls");
        nc.setSchemaVersion(Version.V1_0);
        nc.setUserId(userId);
        nc.setVehicleId(vehicleId);
        return nc;
    }
}
