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

package org.eclipse.ecsp.notification.processors.transformers;

import org.eclipse.ecsp.processor.content.dto.ContentProcessingContextDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SM: Date Format in Notification.
 *
 * @author MaKumari
 */
public class DateFormatTransformerTest {

    DateFormatTransformer dateFormatTransformer;
    ContentProcessingContextDto contentProcessingContextDto;

    @Before
    public void setup() {
        contentProcessingContextDto = new ContentProcessingContextDto();
        dateFormatTransformer = new DateFormatTransformer();
    }

    @Test
    public void testyyyymmdd() {
        String content = "yyyy-MM-dd|dd-MM-yyyy|2021-06-22";
        Assert.assertEquals("22-06-2021", dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

    @Test
    public void testyyyymmddhhmmss() {
        String content = "yyyy-MM-dd-HH-mm-ss|dd-MM-yyyy-HH-mm-ss|2021-06-22-01-00-00";
        Assert.assertEquals("22-06-2021-01-00-00", dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

    @Test
    public void testyyyymmddFailure() {
        String content = "dd-MM-yyyy|yyyy-MM-dd|2021-06-22";
        Assert.assertEquals("2021-06-22", dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

    @Test
    public void testyyyymmddhhmmssFailure() {
        String content = "dd-MM-yyyy-HH-mm-ss|yyyy-MM-dd-HH-mm-ss|2021-06-22-01-00-00";
        Assert.assertEquals("2021-06-22-01-00-00", dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

    @Test
    public void testInputStringZoneFormatError() {
        String content = "MM-dd-yyyy-HH-mm-ss Z|dd-MM-yyyy-HH-mm-ss Z|02-01-2018-13-45-30 +0000";
        Assert.assertEquals("01-02-2018-13-45-30 +0000",
            dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

    @Test
    public void testInputStringZoneFormat() {
        String content = "MM-dd-yyyy HH:mm:ss Z|dd-MM-yyyy HH:mm:ss Z|02-01-2018 13:45:30 +0000";
        Assert.assertEquals("01-02-2018 13:45:30 +0000",
            dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

    @Test
    public void testInputStringZoneNameFormat() {
        String content = "MM-dd-yyyy HH:mm:ss z|dd-MM-yyyy HH:mm:ss z|02-01-2018 13:45:30 UTC";
        Assert.assertEquals("01-02-2018 13:45:30 UTC",
            dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

    @Test
    public void testInputStringZoneNameFormatErrors() throws Exception {
        String content = "MM-dd-yyyy HH:mm:ss z|dd-MM-yyyy HH:mm:ss z|02-01-2018 13:45:30 UTC";
        Assert.assertEquals("01-02-2018 13:45:30 UTC",
            dateFormatTransformer.apply(contentProcessingContextDto, content));
    }

}
