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

package org.eclipse.ecsp.processor.content.request;

import java.util.List;

/**
 * DTCDataResponse class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class DTCDataResponse {

    private List<DTCMaster> data;

    /**
     * Get the data.
     *
     * @return data
     */
    public List<DTCMaster> getData() {
        return data;
    }

    /**
     * Set the data.
     *
     * @param data data
     */
    public void setData(List<DTCMaster> data) {
        this.data = data;
    }

    /**
     * To string.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "DTCDataResponse [data=" + data + "]";
    }

}
