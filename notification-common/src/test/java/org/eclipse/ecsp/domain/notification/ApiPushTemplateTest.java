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

import org.eclipse.ecsp.notification.entities.APIPushTemplate;
import org.junit.Test;

/**
 * APIPushTemplateTest.
 */
public class ApiPushTemplateTest {

    APIPushTemplate ap = new APIPushTemplate();

    @Test
    public void testgetCategory() {
        ap.getCategory();
    }

    @Test
    public void testsetCategory() {
        ap.setCategory("");
    }

    @Test
    public void testgetBannertitle() {
        ap.getBannertitle();
    }

    @Test
    public void testsetBannertitle() {
        ap.setBannertitle("");
    }

    @Test
    public void testgetBannerdesc() {
        ap.getBannerdesc();
    }

    @Test
    public void testsetBannerdesc() {
        ap.setBannerdesc("");
    }

    @Test
    public void testgetTitle() {
        ap.getTitle();
    }

    @Test
    public void testsetTitle() {
        ap.setTitle("");
    }

    @Test
    public void testgetSubtitle() {
        ap.getSubtitle();
    }

    @Test
    public void testsetSubtitle() {
        ap.setSubtitle("");
    }

    @Test
    public void testgetBody() {
        ap.getBody();
    }

    @Test
    public void testsetBody() {
        ap.setBody("");
    }

    @Test
    public void testgetContentFieldsGetter() {
        ap.getContentFieldsGetter();
    }

    @Test
    public void testgetContentFieldsSetter() {
        ap.getContentFieldsSetter();
    }

    @Test
    public void testtoString() {
        ap.toString();
    }
}
