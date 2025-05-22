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

package org.eclipse.ecsp.domain.notification;

import org.eclipse.ecsp.annotations.EventMapping;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.commons.EventID;
import org.eclipse.ecsp.entities.AbstractEventData;

import java.util.Arrays;

/**
 * Eventdata for retry ignite event.
 * this class will be deprecated and
 * replaced by NotificationRetryEventDataV2 as part of
 * fix for RTC 511741 SonarQube - notification-common - Code Smells
 */
@EventMapping(id = EventID.RETRY_NOTIFICATION_EVENT, version = Version.V1_0)
@Deprecated
public class NotificationRetryEventData extends AbstractEventData {

    private static final long serialVersionUID = -3736798123313255986L;

    private RetryRecord retryRecord;

    private byte[] originalEvent;

    private String originalEventTopic;

    /**
     * Getter for RetryRecord.
     *
     * @return retryrecord
     */
    public RetryRecord getRetryRecord() {
        return retryRecord;
    }

    /**
     * Setter for RetryRecord.
     *
     * @param retryRecord the new value
     */
    public void setRetryRecord(RetryRecord retryRecord) {
        this.retryRecord = retryRecord;
    }

    /**
     * Getter for OriginalEvent.
     *
     * @return originalevent
     */
    public byte[] getOriginalEvent() {
        return originalEvent;
    }

    /**
     * Setter for OriginalEvent.
     *
     * @param originalEvent the new value
     */
    public void setOriginalEvent(byte[] originalEvent) {
        this.originalEvent = originalEvent;
    }

    /**
     * Getter for OriginalEventTopic.
     *
     * @return originaleventtopic
     */
    public String getOriginalEventTopic() {
        return originalEventTopic;
    }

    /**
     * Setter for OriginalEventTopic.
     *
     * @param originalEventTopic the new value
     */
    public void setOriginalEventTopic(String originalEventTopic) {
        this.originalEventTopic = originalEventTopic;
    }

    @Override
    public String toString() {
        return "NotificationRetryEventData [retryRecord=" + retryRecord + ", originalEvent="
            + Arrays.toString(originalEvent) + ", originalEventTopic="
            + originalEventTopic + "]";
    }
}
