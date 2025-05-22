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

package org.eclipse.ecsp.notification.entities;

import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.util.List;

/**
 * RetryRecordEntity class.
 */
public class RetryRecordEntity extends AbstractIgniteEntity {

    private List<RetryRecord> retryRecordList;

    /**
     * Get retryRecordList.
     *
     * @return retryRecordList
     */
    public List<RetryRecord> getRetryRecordList() {
        return retryRecordList;
    }

    /**
     * Set retryRecordList.
     *
     * @param retryRecordList retryRecordList
     */
    public void setRetryRecordList(List<RetryRecord> retryRecordList) {
        this.retryRecordList = retryRecordList;
    }

}
