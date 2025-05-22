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

package org.eclipse.ecsp.notification.exception;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UserProfileNotFoundExceptionTest.
 */
public class UserProfileNotFoundExceptionTest {

    @Test
    public void userProfileNotFoundExceptionMsg_success() {
        UserProfileNotFoundException userProfileNotFoundException = new UserProfileNotFoundException("dummy message");
        assertEquals("dummy message", userProfileNotFoundException.getMessage());
        assertNull(userProfileNotFoundException.getCause());
    }

    @Test
    public void userProfileNotFoundExceptionMsgCause_success() {
        Throwable cause = new Throwable();
        UserProfileNotFoundException userProfileNotFoundException =
                new UserProfileNotFoundException("dummy message", cause);
        assertEquals("dummy message", userProfileNotFoundException.getMessage());
        assertEquals(userProfileNotFoundException.getCause(), cause);

    }


}