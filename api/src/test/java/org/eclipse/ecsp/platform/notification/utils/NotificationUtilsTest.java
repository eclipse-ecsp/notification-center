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

package org.eclipse.ecsp.platform.notification.utils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper.Message;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * NotificationUtilsTest.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class NotificationUtilsTest {

    @Test
    public void addCustomPlaceHolderNoPlaceholders() {
        DynamicNotificationTemplate template = new DynamicNotificationTemplate();
        Set<String> ph = new HashSet<>();
        template.setCustomPlaceholders(ph);
        NotificationUtils.addCustomPlaceHolder(template, "text without placeholders");
        assertTrue(CollectionUtils.isEmpty(template.getCustomPlaceholders()));
    }

    @Test
    public void addCustomPlaceHolderNullText() {
        DynamicNotificationTemplate template = new DynamicNotificationTemplate();
        Set<String> ph = new HashSet<>();
        template.setCustomPlaceholders(ph);
        NotificationUtils.addCustomPlaceHolder(template, null);
        assertTrue(CollectionUtils.isEmpty(template.getCustomPlaceholders()));
    }

    @Test
    public void addCustomPlaceHolderNullPlaceholder() {
        DynamicNotificationTemplate template = new DynamicNotificationTemplate();
        NotificationUtils.addCustomPlaceHolder(template,
            "text with 1 placeholder [$.Data.customPlaceholders.key1] and [$.Data.vehicleProfile.model]");
        assertTrue(template.getCustomPlaceholders().contains("key1"));
        assertEquals(1, template.getCustomPlaceholders().size());
    }

    @Test
    public void addCustomPlaceHolderAddPlaceholder() {
        DynamicNotificationTemplate template = new DynamicNotificationTemplate();
        Set<String> ph = new HashSet<>();
        ph.add("oldKey");
        template.setCustomPlaceholders(ph);
        NotificationUtils.addCustomPlaceHolder(template,
            "text with 1 placeholder [$.Data.customPlaceholders.key1] and [$.Data.vehicleProfile.model]");
        assertTrue(template.getCustomPlaceholders().contains("key1"));
        assertTrue(template.getCustomPlaceholders().contains("oldKey"));
        assertEquals(2, template.getCustomPlaceholders().size());
    }


    @Test
    public void addCustomPlaceHolderTwoPlaceholder() {
        DynamicNotificationTemplate template = new DynamicNotificationTemplate();
        NotificationUtils.addCustomPlaceHolder(template,
            "text with 2 placeholder [$.Data.customPlaceholders.key1] and [$.Data.customPlaceholders.key2]"
                + " and [$.Data.vehicleProfile.model]");
        assertTrue(template.getCustomPlaceholders().contains("key1"));
        assertTrue(template.getCustomPlaceholders().contains("key2"));
        assertEquals(2, template.getCustomPlaceholders().size());
    }

    @Test
    public void testIsValidLocale() {

        List<String> langtags = new ArrayList<String>();

        langtags.add("fr-fr");
        langtags.add("fr-bg");
        langtags.add("test-bg");
        langtags.add("langCode-CountryCode");
        langtags.addAll(
            Arrays.asList("en-BG", "en-HR", "en-CY", "en-EE", "en-LV", "en-LT", "en-RO", "en-Si", "en-IS", ""));

        assertTrue(NotificationUtils.isValidLocale("fr-fr"));
        assertFalse(NotificationUtils.isValidLocale("langCode-CountryCode"));
        assertFalse(NotificationUtils.isValidLocale(StringUtils.EMPTY));
        assertFalse(NotificationUtils.isValidLocale(null));

        List<Message> messageList = NotificationUtils.validateLocaleExistence(langtags);

        assertEquals(NotificationCenterError.INPUT_EMPTY_LOCALE.getMessage(), messageList.get(0).getMsg());
        assertEquals(
            NotificationCenterError.INPUT_INVALID_LOCALE.toMessage("[test-bg, langCode-CountryCode]").getMsg(),
            messageList.get(1).getMsg());
    }

}