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
 * DisabledTransformer1 class.
 */
@Component
@Profile("test")
public class DisabledTransformer1 implements ContentTransformer {
    @Override
    public String getId() {
        return "disabled-transformer1";
    }

    @Override
    public String apply(ContentProcessingContextDto alertsInfo, String s) {

        return "who cares, i am disabled";
    }
}
