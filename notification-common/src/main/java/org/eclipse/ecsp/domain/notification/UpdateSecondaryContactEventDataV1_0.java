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

/**
 *  UpdateSecondaryContactEventDataV1_0 event data class.
 */
@SuppressWarnings("checkstyle:TypeName")
@EventMapping(id = EventID.UPDATE_SECONDARY_CONTACT, version = Version.V1_0)
public class UpdateSecondaryContactEventDataV1_0 extends AbstractEventData {
    private static final long serialVersionUID = 1L;
    private SecondaryContact secondaryContact;

    /**
     * Constructor for UpdateSecondaryContactEventDataV1_0.
     *
     * @param secondaryContact SecondaryContact
     */
    public UpdateSecondaryContactEventDataV1_0(SecondaryContact secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    /**
     * Getter for SecondaryContact.
     *
     * @return secondarycontact
     */
    public SecondaryContact getSecondaryContact() {
        return secondaryContact;
    }

    /**
     * Setter for SecondaryContact.
     *
     * @param secondaryContact the new value
     */
    public void setSecondaryContact(SecondaryContact secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    /**
     * Constructor for UpdateSecondaryContactEventDataV1_0.
     */
    public UpdateSecondaryContactEventDataV1_0() {
    }

}
