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

import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.junit.Test;

/**
 * RichContentDynamicNotificationTemplateTest class.
 */
public class RichContentDynamicNotificationTemplateTest {

    RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate =
        new RichContentDynamicNotificationTemplate();

    @Test
    public void testgetId() {
        richContentDynamicNotificationTemplate.getId();
    }

    @Test
    public void testsetId() {
        richContentDynamicNotificationTemplate.setId("");
    }

    @Test
    public void testgetNotificationId() {
        richContentDynamicNotificationTemplate.getNotificationId();
    }

    @Test
    public void testgetBrand() {
        richContentDynamicNotificationTemplate.getBrand();
    }

    @Test
    public void testgetHtml() {
        richContentDynamicNotificationTemplate.getHtml();
    }

    @Test
    public void testgetLastUpdateTime() {
        richContentDynamicNotificationTemplate.getLastUpdateTime();
    }

    @Test
    public void testgetAttachments() {
        richContentDynamicNotificationTemplate.getAttachments();
    }

    @Test
    public void testgetAdditionalLookupProperties() {
        richContentDynamicNotificationTemplate.getAdditionalLookupProperties();
    }

    @Test
    public void testsetAdditionalLookupProperties() {
        richContentDynamicNotificationTemplate.setAdditionalLookupProperties(null);
    }

    @Test
    public void testgetCustomPlaceholders() {
        richContentDynamicNotificationTemplate.getCustomPlaceholders();
    }

    @Test
    public void testsetCustomPlaceholders() {
        richContentDynamicNotificationTemplate.setCustomPlaceholders(null);
    }

    @Test
    public void testgetLocaleAsLocale() {
        richContentDynamicNotificationTemplate.setLocale("EN-US");
        richContentDynamicNotificationTemplate.getLocaleAsLocale();
    }

    @Test
    public void testtoString() {
        richContentDynamicNotificationTemplate.toString();
    }
}
