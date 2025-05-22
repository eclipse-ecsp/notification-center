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
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.AbstractEventData;

/**
 * VehicleMessageDispositionAckData class.
 */
@EventMapping(id = EventID.VEHICLE_MESSAGE_DISPOSITION_ACK, version = Version.V1_0)
// @Converters(JsonObjectConverter.class)
public class VehicleMessageDispositionAckData extends AbstractEventData {

    private static final long serialVersionUID = -8279173288506686108L;

    private DispositionResponseEnum response;

    /**
     * DispositionResponseEnum class.
     */
    public enum DispositionResponseEnum {
        SUCCESS(NotificationConstants.SUCCESS),
        FAILURE(NotificationConstants.FAILURE),
        CUSTOM_EXTENSION(NotificationConstants.CUSTOM_EXTENSION);

        private String value;

        DispositionResponseEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    /**
     * Getter for Response.
     *
     * @return response
     */
    public DispositionResponseEnum getResponse() {
        return response;
    }

    /**
     * Setter for Response.
     *
     * @param response the new value
     */
    public void setResponse(DispositionResponseEnum response) {
        this.response = response;
    }

    /**
     * Equals method.
     *
     * @param obj Object
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * HashCode method.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * ToString method.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "VehicleMessageDispositionAckData [response=" + response + "]";
    }

}
