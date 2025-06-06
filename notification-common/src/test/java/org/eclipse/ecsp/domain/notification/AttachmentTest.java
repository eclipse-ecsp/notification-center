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

import org.eclipse.ecsp.notification.entities.Attachment;
import org.junit.Test;

/**
 * AttachmentTest class.
 */
public class AttachmentTest {

    Attachment attchmnt = new Attachment();

    @Test
    public void testgetId() {
        attchmnt.getId();
    }

    @Test
    public void testsetId() {
        attchmnt.setId("");
    }

    @Test
    public void testgetName() {
        attchmnt.getName();
    }

    @Test
    public void testsetName() {
        attchmnt.setName("");
    }

    @Test
    public void testgetUrl() {
        attchmnt.getUrl();
    }

    @Test
    public void testsetUrl() {
        attchmnt.setUrl("");
    }

    @Test
    public void testtoString() {
        attchmnt.toString();
    }
}
