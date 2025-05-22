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

package org.eclipse.ecsp.notification.dao;

import org.eclipse.ecsp.domain.notification.IVMRequest;
import org.eclipse.ecsp.nosqldao.IgniteBaseDAO;

import java.util.Optional;

/**
 * IVMRequestDAO class.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface IVMRequestDAO extends IgniteBaseDAO<String, IVMRequest> {
    /**
     * Find by vehicle id message id.
     *
     * @param vehicleId vehicle id
     * @param messageId message id
     * @return IVMRequest
     */
    public Optional<IVMRequest> findByVehicleIdMessageId(String vehicleId, String messageId);

    /**
     * Find by vehicle id session id.
     *
     * @param vehicleId vehicle id
     * @param sessionId session id
     * @return IVMRequest
     */
    public Optional<IVMRequest> findByVehicleIdSessionId(String vehicleId, String sessionId);
}
