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
import org.eclipse.ecsp.processor.content.dto.UserProfileDto;
import org.eclipse.ecsp.processor.content.plugin.ContentTransformer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * This is a demo implementation, which returns the user name and last name.
 */
@Component
@Profile("test")
public class DemoUserNameContentTransformer implements ContentTransformer {

    private static final String ID = "demo-user-name";


    /**
     * This is the identifier of the function which will be applied to the content.
     * When content placeholder looks like this: [[demo-user-name|]],
     * the first value is this particular transformer id.
     *
     * @return unique transformer id.
     */
    @Override
    public String getId() {
        return ID;
    }


    /**
     * This is the actual transformation function.
     * If content placeholder looks like this: [[demo-user-name|]],
     * the content to transform value is "".  The result is the full name: Clark Kent
     * NOTE the empty data part, which is totally supported
     * If content placeholder looks like this: [[demo-user-name|Superman]],
     * the content to transform value is "Mad Max".  The result is the supplied name: Clark (Superman) Kent
     *
     * @param alertsInfo         the event context, which may contain data required for the transformation
     * @param contentToTransform the specific value which is in the placeholder
     * @return input in uppercase
     */
    @Override
    public String apply(ContentProcessingContextDto alertsInfo, String contentToTransform) {
        UserProfileDto userProfile = alertsInfo.getUserProfile();
        String middleName = isNullOrEmpty(contentToTransform) ? " " : " (" + contentToTransform + ") ";
        return String.format("%s%s%s", userProfile.getFirstName(), middleName, userProfile.getLastName());
    }


    /**
     * in case of exception or timeout on the "apply" method, the
     * fallback will be used to provide the missing value.
     *
     * @param alertsInfo alertsInfo
     * @param input input
     * @return String
     */
    @Override
    public String fallback(ContentProcessingContextDto alertsInfo, String input) {
        return "Momo";
    }
}
