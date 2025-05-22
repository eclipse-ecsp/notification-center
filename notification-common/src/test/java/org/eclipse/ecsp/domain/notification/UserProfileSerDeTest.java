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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

/**
 * UserProfileSerDeTest class.
 */
public class UserProfileSerDeTest {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(UserProfileSerDeTest.class);

    @Test
    public void testLocaleSerDe() throws IOException {
        UserProfile up = new UserProfile();
        up.setLocale(new Locale("en", "US"));
        ObjectMapper mapper = new ObjectMapper();
        String upString = mapper.writeValueAsString(up);
        LOGGER.info(upString);
        UserProfile reborn = (UserProfile) mapper.readValue(upString, UserProfile.class);
        Assert.assertEquals(up.getLocale(), reborn.getLocale());

    }
}
