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

package org.eclipse.ecsp.notification.user.profile;

import org.eclipse.ecsp.domain.notification.CreateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.DeleteSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.NickName;
import org.eclipse.ecsp.domain.notification.NotificationUserProfileEventDataV1_0;
import org.eclipse.ecsp.domain.notification.SecondaryContact;
import org.eclipse.ecsp.domain.notification.UpdateSecondaryContactEventDataV1_0;
import org.eclipse.ecsp.domain.notification.UserProfile;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.notification.config.NotificationConfigDAO;
import org.eclipse.ecsp.notification.dao.SecondaryContactDAO;
import org.eclipse.ecsp.notification.dao.UserProfileDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * UserProfileServiceTest.
 */
public class UserProfileServiceTest {

    @InjectMocks
    UserProfileService userProfileService;

    @Mock
    private UserProfileDAO userProfileDao;

    @Mock
    private SecondaryContactDAO secondaryContactDao;

    @Mock
    private NotificationConfigDAO notificationConfigDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deleteUserProfile_success() {
        doReturn(true).when(userProfileDao).deleteById("dummyId");
        userProfileService.deleteUserProfile("dummyId");
        verify(userProfileDao, times(1)).deleteById("dummyId");
    }

    @Test
    public void deleteUserVehicleNickNames_success() {
        doReturn(new UserProfile()).when(userProfileDao).removeNickNames("userId", "vechileId");
        userProfileService.deleteUserVehicleNickNames("userId", "vechileId");
        verify(userProfileDao, times(1)).removeNickNames("userId", "vechileId");

    }

    @Test
    public void updateSecondaryContact_success() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        UpdateSecondaryContactEventDataV1_0 updateSecondaryContactEventData = new UpdateSecondaryContactEventDataV1_0();
        updateSecondaryContactEventData.setSecondaryContact(new SecondaryContact());
        igniteEvent.setEventData(updateSecondaryContactEventData);

        doReturn(true).when(secondaryContactDao).update(any());
        userProfileService.updateSecondaryContact(igniteEvent);
        verify(secondaryContactDao, times(1)).update(any());
    }

    @Test
    public void createSecondaryContact_success() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        CreateSecondaryContactEventDataV1_0 createSecondaryContactEventData = new CreateSecondaryContactEventDataV1_0();
        createSecondaryContactEventData.setSecondaryContact(new SecondaryContact());
        igniteEvent.setEventData(createSecondaryContactEventData);

        doReturn(new SecondaryContact()).when(secondaryContactDao).save(any());
        userProfileService.createSecondaryContact(igniteEvent);
        verify(secondaryContactDao, times(1)).save(any());
    }

    @Test
    public void deleteSecondaryContact_success() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        DeleteSecondaryContactEventDataV1_0 deleteSecondaryContactEventData = new DeleteSecondaryContactEventDataV1_0();
        deleteSecondaryContactEventData.setSecondaryContact(new SecondaryContact());
        igniteEvent.setEventData(deleteSecondaryContactEventData);

        doReturn(true).when(secondaryContactDao).deleteById(any());
        userProfileService.deleteSecondaryContact(igniteEvent);
        verify(secondaryContactDao, times(1)).deleteById(any());
    }

    @Test
    public void updateUserProfile_noUpdate() {
        IgniteEventImpl igniteEvent = new IgniteEventImpl();

        NotificationUserProfileEventDataV1_0 notificationUserProfileEventData =
            new NotificationUserProfileEventDataV1_0();
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("dummyUserId");
        notificationUserProfileEventData.setUserProfile(userProfile);
        igniteEvent.setEventData(notificationUserProfileEventData);

        doReturn(userProfile).when(userProfileDao).findById("dummyUserId");
        userProfileService.updateUserProfile(igniteEvent);
        verify(userProfileDao, times(1)).findById("dummyUserId");
    }

    @Test
    public void updateUserProfile_updateFields() {
        UserProfile userProfile = createUserProfile();

        doReturn(userProfile).when(userProfileDao).findById("dummyUserId");
        doReturn(userProfile).when(userProfileDao).save(userProfile);
        UserProfile actualUserProfile = userProfileService.updateUserProfile(userProfile);
        compareUserProfile(userProfile, actualUserProfile);
    }

    @Test
    public void updateUserProfile_userNotExists() {

        UserProfile userProfile = createUserProfile();

        doReturn(null).when(userProfileDao).findById("dummyUserId");
        doReturn(userProfile).when(userProfileDao).save(userProfile);
        UserProfile actualUserProfile = userProfileService.updateUserProfile(userProfile);
        compareUserProfile(userProfile, actualUserProfile);
    }

    private void compareUserProfile(UserProfile userProfile, UserProfile actualUserProfile) {
        assertEquals(userProfile.getFirstName(), actualUserProfile.getFirstName());
        assertEquals(userProfile.getLastName(), actualUserProfile.getLastName());
        assertEquals(userProfile.getDefaultEmail(), actualUserProfile.getDefaultEmail());
        assertEquals(userProfile.getDefaultPhoneNumber(), actualUserProfile.getDefaultPhoneNumber());
        assertEquals(userProfile.getLocale(), actualUserProfile.getLocale());
        assertEquals(userProfile.getCustomAttributes(), actualUserProfile.getCustomAttributes());
        assertEquals(userProfile.getNickNames(), actualUserProfile.getNickNames());
    }


    private UserProfile createUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("dummyUserId");
        userProfile.setFirstName("dummyFirstName");
        userProfile.setLastName("dummyLastName");
        userProfile.setDefaultEmail("dummyDefaultEmail");
        userProfile.setDefaultPhoneNumber("dummyDefaultPh");
        userProfile.setLocale(Locale.forLanguageTag("en_US"));
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("dummykey", "DummyValue");
        userProfile.setCustomAttributes(customAttributes);
        Set<NickName> nickNames = new HashSet<>();
        nickNames.add(new NickName("dummyNickName", "dummyVehicleId"));
        userProfile.setNickNames(nickNames);
        return userProfile;
    }

}