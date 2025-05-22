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

package org.eclipse.ecsp.domain.notification.grouping;

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.domain.notification.spring.context.DaoConfig;
import org.eclipse.ecsp.domain.notification.utils.NotificationEncryptionServiceImpl;
import org.eclipse.ecsp.nosqldao.spring.config.IgniteDAOMongoConfigWithProps;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * UserProfileDAOMongoImplTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@ContextConfiguration(classes = {TestSecurityLibSample.class, NotificationEncryptionServiceImpl.class,
    IgniteDAOMongoConfigWithProps.class,
    DaoConfig.class})
@TestPropertySource("/notification-dao-test.properties")
public class UserProfileDaoMongoImplTest {

    @Autowired
    private UserProfileDAO userProfileDao;

    @ClassRule
    public static EmbeddedMongoDB embeddedMongoDB = new EmbeddedMongoDB();

    @Test
    public void saveUserProfile() {
        UserProfile userProfile = new UserProfile();

        String mail = "test@harman.com";
        String phone = "123456789";
        String userId = "user123";



        userProfile.setDefaultEmail(mail);
        userProfile.setDefaultPhoneNumber(phone);
        userProfile.setUserId(userId);

        UserProfile savedUserProfile = userProfileDao.save(userProfile);

        Assert.assertTrue(mail.equals(savedUserProfile.getDefaultEmail()));
        Assert.assertTrue(phone.equals(savedUserProfile.getDefaultPhoneNumber()));

        savedUserProfile = userProfileDao.findById(userId);

        Assert.assertTrue(mail.equals(savedUserProfile.getDefaultEmail()));
        Assert.assertTrue(phone.equals(savedUserProfile.getDefaultPhoneNumber()));
        String encryptedMail = "1a2b3c4d5f";
        String encryptedPhone = "12qqq6789";
        UserProfile actualUserProfileInDb =
            userProfileDao.findAll().stream().filter(u -> u.getUserId().equals(userId)).findFirst().get();
        Assert.assertTrue(encryptedMail.equals(actualUserProfileInDb.getDefaultEmail()));
        Assert.assertTrue(encryptedPhone.equals(actualUserProfileInDb.getDefaultPhoneNumber()));

    }

    @Test
    public void testupdateNickName() {
        UserProfile userProfile = new UserProfile();
        NickName n = new NickName("test", "test");
        String mail = "test@harman.com";
        String phone = "123456789";
        String userId = "user123";

        String encryptedMail = "1a2b3c4d5f";
        String encryptedPhone = "12qqq6789";

        userProfile.setDefaultEmail(mail);
        userProfile.setDefaultPhoneNumber(phone);
        userProfile.setUserId(userId);

        UserProfile savedUserProfile = userProfileDao.save(userProfile);
        userProfileDao.updateNickName(userId, "test", "test");
        userProfileDao.updateNickName("test", "test", "test");
    }

    @Test
    public void testupdateConsent() {
        UserProfile userProfile = new UserProfile();
        NickName n = new NickName("test", "test");
        String mail = "test@harman.com";
        String phone = "123456789";
        String userId = "user123";

        String encryptedMail = "1a2b3c4d5f";
        String encryptedPhone = "12qqq6789";

        userProfile.setDefaultEmail(mail);
        userProfile.setDefaultPhoneNumber(phone);
        userProfile.setUserId(userId);

        UserProfile savedUserProfile = userProfileDao.save(userProfile);
        userProfileDao.updateConsent(userId, true);
        userProfileDao.updateConsent("test", true);
    }

    @Test
    public void testRemoveNickNames() {
        String userId = "user";
        String vehicleId = "vehicle";
        NickName nickName = new NickName();
        nickName.setNickName("nickName");
        nickName.setVehicleId(vehicleId);

        UserProfile before = new UserProfile();
        before.setUserId(userId);
        before.setLastUpdatedTime(LocalDateTime.now());
        before.setNickNames(Collections.singleton(nickName));
        userProfileDao.save(before);

        UserProfile after = userProfileDao.removeNickNames(userId, vehicleId);

        Assert.assertNull(after.getNickName(vehicleId));
        Assert.assertNotEquals(before.getLastModifiedTime(), after.getLastModifiedTime());
    }

    @Test
    public void testRemoveNickNamesUserNotFound() {

        UserProfile after = userProfileDao.removeNickNames("notExistingUser", "vehicleId");
        Assert.assertNull(after);
    }
}
