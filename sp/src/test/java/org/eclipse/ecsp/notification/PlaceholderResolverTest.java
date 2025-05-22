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

package org.eclipse.ecsp.notification;

import org.eclipse.ecsp.notification.processors.transformers.PlaceholderDescriptor;
import org.eclipse.ecsp.notification.processors.transformers.PlaceholderResolver;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Placeholder resolver test.
 */
public class PlaceholderResolverTest {

    PlaceholderResolver placeholderResolver = new PlaceholderResolver();

    @Test
    public void resolvePlaceholdersSinglePlaceholderSuccess() {
        List<PlaceholderDescriptor> actual =
            placeholderResolver.resolvePlaceholders("[[tinyUrl|paypal.com/2319482384423424]]");
        PlaceholderDescriptor expected = new PlaceholderDescriptor("[[tinyUrl|paypal.com/2319482384423424]]", "tinyUrl",
            "paypal.com/2319482384423424");
        assertEquals(expected.getContentTransformerId(), actual.get(0).getContentTransformerId());
        assertEquals(expected.getPlaceholder(), actual.get(0).getPlaceholder());
        assertEquals(expected.getInput(), actual.get(0).getInput());
    }

    @Test
    public void resolvePlaceholdersSinglePlaceholderIdWithSpacesSuccess() {
        List<PlaceholderDescriptor> actual =
            placeholderResolver.resolvePlaceholders("[[   tinyUrl   |paypal.com/2319482384423424]]");
        PlaceholderDescriptor expected =
            new PlaceholderDescriptor("[[   tinyUrl   |paypal.com/2319482384423424]]", "tinyUrl",
                "paypal.com/2319482384423424");
        assertEquals(expected.getContentTransformerId(), actual.get(0).getContentTransformerId());
        assertEquals(expected.getPlaceholder(), actual.get(0).getPlaceholder());
        assertEquals(expected.getInput(), actual.get(0).getInput());
    }

    @Test
    public void resolvePlaceholdersMultiplePlaceholderSuccess() {
        List<PlaceholderDescriptor> actual = placeholderResolver.resolvePlaceholders(
            "[[tinyUrl|paypal.com/2319482384423424]] thank you [[userNameToPicture|Bob Cohen]]");
        List<PlaceholderDescriptor> expected = new LinkedList<>();
        expected.add(new PlaceholderDescriptor("[[tinyUrl|paypal.com/2319482384423424]]", "tinyUrl",
            "paypal.com/2319482384423424"));
        expected.add(new PlaceholderDescriptor("[[userNameToPicture|Bob Cohen]]", "userNameToPicture", "Bob Cohen"));
        assertEquals(expected.get(0).getContentTransformerId(), actual.get(0).getContentTransformerId());
        assertEquals(expected.get(0).getPlaceholder(), actual.get(0).getPlaceholder());
        assertEquals(expected.get(0).getInput(), actual.get(0).getInput());
        assertEquals(expected.get(1).getContentTransformerId(), actual.get(1).getContentTransformerId());
        assertEquals(expected.get(1).getPlaceholder(), actual.get(1).getPlaceholder());
        assertEquals(expected.get(1).getInput(), actual.get(1).getInput());

    }

    @Test
    public void resolvePlaceholdersReturnEmptyIncorrectParenthesis() {
        List<PlaceholderDescriptor> actual =
            placeholderResolver.resolvePlaceholders("[tinyUrl|paypal.com/2319482384423424]]");
        assertEquals(0, actual.size());
    }

    @Test
    public void resolvePlaceholdersReturnEmptyNoPipeline() {
        List<PlaceholderDescriptor> actual =
            placeholderResolver.resolvePlaceholders("[[tinyUrl paypal.com/2319482384423424]]");
        assertEquals(0, actual.size());
    }

    @Test
    public void resolvePlaceholdersNoContentSuccess() {
        List<PlaceholderDescriptor> actual = placeholderResolver.resolvePlaceholders("[[tinyUrl|]]");
        assertEquals(1, actual.size());
    }

    @Test
    public void resolvePlaceholdersReturnEmptyNoTransformer() {
        List<PlaceholderDescriptor> actual =
            placeholderResolver.resolvePlaceholders("[[|paypal.com/2319482384423424]]");
        assertEquals(0, actual.size());
    }

    @Test
    public void replacePlaceholdersSinglePlaceholderSuccess() {
        List<PlaceholderDescriptor> descriptors = new LinkedList<>();
        PlaceholderDescriptor placeholderDescriptor =
            new PlaceholderDescriptor("[[tinyUrl|paypal.com/2319482384423424]]", "tinyUrl",
                "paypal.com/2319482384423424");
        placeholderDescriptor.setResult("paypal.com/july");
        descriptors.add(placeholderDescriptor);
        String actual = placeholderResolver.replacePlaceholders(descriptors,
            "Hello Bob, the bill for July can be found in the following link: [[tinyUrl|paypal.com/2319482384423424]]");
        String expected = "Hello Bob, the bill for July can be found in the following link: paypal.com/july";
        assertEquals(expected, actual);

    }

    @Test
    public void replacePlaceholdersMultiplePlaceholderSuccess() {
        List<PlaceholderDescriptor> descriptors = new LinkedList<>();
        PlaceholderDescriptor placeholderDescriptor1 =
            new PlaceholderDescriptor("[[tinyUrl|paypal.com/2319482384423424]]", "tinyUrl",
                "paypal.com/2319482384423424");
        placeholderDescriptor1.setResult("paypal.com/july");
        PlaceholderDescriptor placeholderDescriptor2 =
            new PlaceholderDescriptor("[[userNameToPicture|Bob Cohen]]", "userNameToPicture", "Bob Cohen");
        placeholderDescriptor2.setResult("Bobby =)");
        descriptors.add(placeholderDescriptor1);
        descriptors.add(placeholderDescriptor2);
        String actual = placeholderResolver.replacePlaceholders(descriptors,
            "Hello [[userNameToPicture|Bob Cohen]], the bill for July can be found in the following link: "
                + "[[tinyUrl|paypal.com/2319482384423424]]");
        String expected = "Hello Bobby =), the bill for July can be found in the following link: paypal.com/july";
        assertEquals(expected, actual);
    }

}
