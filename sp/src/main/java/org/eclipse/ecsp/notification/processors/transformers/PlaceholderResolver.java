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

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlaceholderResolver class.
 */
@Component
public class PlaceholderResolver {

    private static int TWO = 2;

    private static int THREE = 3;

    private static int MINUS_ONE = -1;
    static final String DELIMITER = "\\|";
    static final String PLACEHOLDER_START = "[[";
    static final String PLACEHOLDER_END = "]]";
    static final String PLACEHOLDER_REGEX = "(\\[\\[(\\s*[a-zA-Z0-9\\-\\._]+\\s*)\\|(.*?)\\]\\])";

    /**
     * Method to resolve placeholders in a template.
     *
     * @param content String
     * @return List of resolved placeholders
     */
    public List<PlaceholderDescriptor> resolvePlaceholders(String content) {
        List<PlaceholderDescriptor> results = new LinkedList<>();

        if (content == null) {
            return results;
        }

        Pattern pattern = Pattern.compile(PLACEHOLDER_REGEX);
        Matcher matcher = pattern.matcher(content);
        String placeHolder;
        String contentTransformerId;
        String inputContent;

        while (matcher.find()) {
            placeHolder = matcher.group(1);
            contentTransformerId = matcher.group(TWO).trim();
            inputContent = matcher.group(THREE);

            results.add(new PlaceholderDescriptor(placeHolder, contentTransformerId, inputContent));
        }
        return results;
    }

    /**
     * Method to replace placeholders.
     *
     * @param descriptors List of placeholderdescriptors
     * @param content String
     * @return String
     */
    public String replacePlaceholders(List<PlaceholderDescriptor> descriptors, String content) {
        StringBuilder result = new StringBuilder(content);
        for (PlaceholderDescriptor des : descriptors) {
            replaceAll(result, des.getPlaceholder(), des.getResult());
        }
        return result.toString();
    }

    /**
     * replaceAll method.
     *
     * @param builder StringBuilder
     * @param from String
     * @param to String
     */
    public static void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != MINUS_ONE) {
            builder.replace(index, index + from.length(), to);
            index += to.length();
            index = builder.indexOf(from, index);
        }
    }
}
