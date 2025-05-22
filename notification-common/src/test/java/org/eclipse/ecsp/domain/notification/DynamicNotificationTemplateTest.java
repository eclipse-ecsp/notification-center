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

import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.junit.Test;

/**
 * DynamicNotificationTemplateTest class.
 */
public class DynamicNotificationTemplateTest {

    DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();

    @Test
    public void testgetId() {
        dynamicNotificationTemplate.getId();
    }

    @Test
    public void testsetId() {
        dynamicNotificationTemplate.setId("");
    }

    @Test
    public void testgetNotificationId() {
        dynamicNotificationTemplate.getNotificationId();
    }

    @Test
    public void testsetNotificationId() {
        dynamicNotificationTemplate.setNotificationId("");
    }

    @Test
    public void testgetNotificationShortName() {
        dynamicNotificationTemplate.getNotificationShortName();
    }

    @Test
    public void testsetNotificationShortName() {
        dynamicNotificationTemplate.setNotificationShortName("");
    }

    @Test
    public void testgetNotificationLongName() {
        dynamicNotificationTemplate.getNotificationLongName();
    }

    @Test
    public void testsetNotificationLongName() {
        dynamicNotificationTemplate.setNotificationLongName("");
    }

    @Test
    public void testgetLocale() {
        dynamicNotificationTemplate.getLocale();
    }

    @Test
    public void testsetLocale() {
        dynamicNotificationTemplate.setLocale("en-US");
    }

    @Test
    public void testgetBrand() {
        dynamicNotificationTemplate.getBrand();
    }

    @Test
    public void testsetBrand() {
        dynamicNotificationTemplate.setBrand(null);
    }

    @Test
    public void testgetCustomPlaceholders() {
        dynamicNotificationTemplate.getCustomPlaceholders();
    }

    @Test
    public void testsetCustomPlaceholders() {
        dynamicNotificationTemplate.setCustomPlaceholders(null);
    }

    @Test
    public void testgetChannelTemplates() {
        dynamicNotificationTemplate.getChannelTemplates();
    }

    @Test
    public void testsetChannelTemplates() {
        dynamicNotificationTemplate.setChannelTemplates(null);
    }

    @Test
    public void testgetAdditionalLookupProperties() {
        dynamicNotificationTemplate.getAdditionalLookupProperties();
    }

    @Test
    public void testsetAdditionalLookupProperties() {
        dynamicNotificationTemplate.setAdditionalLookupProperties(null);
    }

    @Test
    public void testaddAttributeToChannel() throws Exception {

        dynamicNotificationTemplate.addAttributeToChannel("channel", "dummy", dynamicNotificationTemplate);
    }

    @Test(expected = Exception.class)
    public void testaddAttributeToChannelException() throws Exception {

        dynamicNotificationTemplate.addAttributeToChannel("", "dummy", dynamicNotificationTemplate);
    }


}
