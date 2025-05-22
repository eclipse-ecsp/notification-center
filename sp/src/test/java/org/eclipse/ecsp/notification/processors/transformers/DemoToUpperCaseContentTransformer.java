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
import org.eclipse.ecsp.processor.content.plugin.ContentTransformer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This is a demo implementation, which transforms the input content to upper case.
 */
@Component
@Profile("test")
public class DemoToUpperCaseContentTransformer implements ContentTransformer {

    private static final  String ID = "demo-upper-case";


    /**
     * This is the identifier of the function which will be applied to the content.
     * When content placeholder looks like this: [[demo-upper-case|yaba daba doo]],
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
     * When content placeholder looks like this: [[demo-upper-case|yaba daba doo]],
     * the content to transform value is "yaba daba doo".  The result is YABA DABA DOO
     *
     * @param alertsInfo         the event context, which may contain data required for the transformation
     * @param contentToTransform the specific value which is in the placeholder
     * @return input in uppercase
     */
    @Override
    public String apply(ContentProcessingContextDto alertsInfo, String contentToTransform) {
        return contentToTransform == null ? null : contentToTransform.toUpperCase();
    }
}
