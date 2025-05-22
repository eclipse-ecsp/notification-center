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

package org.eclipse.ecsp.platform.notification.v1.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AlertsHistoryRequestParams class.
 */
@Data
@Builder
public class AlertsHistoryRequestParams {
    private List<String> deviceIds;
    private String userId;
    private TimeIntervalInfo timeIntervalInfo;
    private PaginationInfo paginationInfo;
    private Collection<String> alertNames;
    private String readStatus;

    /**
     * Get deviceID string.
     *
     * @return deviceID
     */
    @JsonIgnore
    public String getDeviceIdAsSingle() {
        if (CollectionUtils.isEmpty(deviceIds)) {
            return null;
        }
        return deviceIds.toArray(new String[0])[0];
    }

    /**
     * Get deviceIDs.
     *
     * @return deviceIds
     */
    @JsonIgnore
    public List<String> getDeviceIds() {
        if (deviceIds == null) {
            deviceIds = new ArrayList<>();
        }
        return deviceIds;
    }
}
