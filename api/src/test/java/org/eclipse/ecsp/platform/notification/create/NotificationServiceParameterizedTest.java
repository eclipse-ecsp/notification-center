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

package org.eclipse.ecsp.platform.notification.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.domain.notification.NotificationNonRegisteredUser;
import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collection;

import static org.eclipse.ecsp.platform.notification.utils.UnitTestUtils.compareException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * NotificationServiceParameterizedTest class.
 */
@RunWith(Parameterized.class)
public class NotificationServiceParameterizedTest {
    public static final int TWO = 2;
    public static final int SIX = 6;
    public static final int FIFTEEN = 15;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private DynamicNotificationTemplateDAO dynamicnotificationtemplatedao;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * NotificationServiceParameterizedTest constructor.
     *
     * @param payload String
     * @param maxUsers int
     * @param missingCh String
     */
    public NotificationServiceParameterizedTest(String payload, int maxUsers, String missingCh) {
        this.payload = payload;
        this.maxUsers = maxUsers;
        this.missingCh = missingCh;
    }


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * data method.
     *
     * @return List
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"/non-register-user-no-channel-data.json", TWO, "1" },
                {"/non-register-user-no-channel-data2.json", SIX, "2" },
                {"/non-register-user-no-channel-data3.json", FIFTEEN, "5" },

        });

    }

    private final String payload;
    private final int maxUsers;
    private final String missingCh;

    @Test
    public void nonRegisteredUserWarningOneRecipientWithoutChannels() throws Exception {

        String requestData = IOUtils.toString(
                NotificationServiceTest.class.getResourceAsStream(payload), "UTF-8");
        NotificationNonRegisteredUser notificationNonRegisteredUser =
                mapper.readValue(requestData, NotificationNonRegisteredUser.class);

        when(dynamicnotificationtemplatedao.isNotificationIdExist(any())).thenReturn(true);
        notificationService.setMaxNonRegisterUserNotificationsPerRequest(maxUsers);

        NotificationCenterExceptionBase thrown =
                assertThrows(InvalidInputException.class,
                        () -> notificationService.
                                createNotificationForNonRegisteredUsers(
                                        notificationNonRegisteredUser, "rId",
                                "sId"),
                        "Expected to throw, but it didn't");
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
                NotificationCenterError.NON_REGISTERED_INPUT_MISSING_RECIPIENT_CHANNELS, missingCh);
    }
}
