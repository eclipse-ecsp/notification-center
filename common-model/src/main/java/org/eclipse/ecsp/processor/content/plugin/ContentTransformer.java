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

package org.eclipse.ecsp.processor.content.plugin;

import org.eclipse.ecsp.processor.content.dto.ContentProcessingContextDto;
import org.pf4j.ExtensionPoint;

import java.util.function.BiFunction;

/**
 * ContentTransformer interface.
 */
public interface ContentTransformer extends BiFunction<ContentProcessingContextDto, String, String>, ExtensionPoint {

    /**
     * This is the identifier of the function which will be applied to the content.
     * When content placeholder looks like this: [[TRANSFORMER ID|TRANSFORMED CONTENT]],
     * the first value is this particular transformer id.
     *
     * @return unique transformer id.
     */
    String getId();


    /**
     * in case of exception or timeout on the "apply" method,
     * the fallback will be used to provide the missing value.
     *
     * @param contextDto contextDto
     *
     * @param input content to be tranformed
     *
     * @return String tranformed content
     */
    default String fallback(ContentProcessingContextDto contextDto, String input) {
        return input;
    }
}
