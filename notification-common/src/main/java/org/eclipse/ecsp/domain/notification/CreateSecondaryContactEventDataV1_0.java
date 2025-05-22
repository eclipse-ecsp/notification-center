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
 * CreateSecondaryContactEventDataV1_0 class.
 */
@SuppressWarnings("checkstyle:TypeName")
@EventMapping(id = EventID.CREATE_SECONDARY_CONTACT, version = Version.V1_0)
public class CreateSecondaryContactEventDataV1_0 extends AbstractEventData {
    private static final long serialVersionUID = 1L;
    private SecondaryContact secondaryContact;

    /**
     * CreateSecondaryContactEventDataV1_0 Constructor.
     *
     * @param secondaryContact SecondaryContact
     */
    public CreateSecondaryContactEventDataV1_0(SecondaryContact secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    /**
     * This method is a getter for secondaryContact.
     *
     * @return SecondaryContact
     */
    public SecondaryContact getSecondaryContact() {
        return secondaryContact;
    }

    /**
     * This method is a setter for secondaryContact.
     *
     * @param secondaryContact SecondaryContact
     */
    public void setSecondaryContact(SecondaryContact secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    /**
     * CreateSecondaryContactEventDataV1_0 Constructor.
     */
    public CreateSecondaryContactEventDataV1_0() {
    }

}
