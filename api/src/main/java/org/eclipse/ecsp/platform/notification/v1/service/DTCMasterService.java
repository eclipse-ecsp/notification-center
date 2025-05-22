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

package org.eclipse.ecsp.platform.notification.v1.service;

import org.eclipse.ecsp.notification.dao.DTCMasterDAO;
import org.eclipse.ecsp.notification.entities.DTCMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * DTC Master service.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@Service
public class DTCMasterService {

    @Autowired
    private DTCMasterDAO dtcMasterRepository;

    /**
     * Get DTC code.
     *
     * @param dtcCode DTC code
     * @return DTCMaster
     */
    public DTCMaster getDtcCode(String dtcCode) {
        return dtcMasterRepository.findById(dtcCode);

    }

    /**
     * Get DTC codes.
     *
     * @param dtcCodes DTC codes
     * @return List of DTCMaster
     */
    public List<DTCMaster> getDtcCodes(Collection<String> dtcCodes) {
        return dtcMasterRepository.findByIdIn(dtcCodes);
    }
}
