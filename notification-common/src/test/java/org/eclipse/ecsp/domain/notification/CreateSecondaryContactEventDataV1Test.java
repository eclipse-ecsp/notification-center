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

import org.junit.Test;

/**
 * CreateSecondaryContactEventDataV1Test class.
 */
public class CreateSecondaryContactEventDataV1Test {

    CreateSecondaryContactEventDataV1_0 cs = new CreateSecondaryContactEventDataV1_0();
    CreateSecondaryContactEventDataV1_0 cs2 = new CreateSecondaryContactEventDataV1_0(null);
    DeleteSecondaryContactEventDataV1_0 ds = new DeleteSecondaryContactEventDataV1_0();
    DeleteSecondaryContactEventDataV1_0 ds2 = new DeleteSecondaryContactEventDataV1_0(null);

    DisAssociationDataV1_0 dasd = new DisAssociationDataV1_0();

    UpdateSecondaryContactEventDataV1_0 usec = new UpdateSecondaryContactEventDataV1_0();
    UpdateSecondaryContactEventDataV1_0 usec2 = new UpdateSecondaryContactEventDataV1_0(null);


    @Test
    public void testgetset() {
        cs.getSecondaryContact();
        cs.setSecondaryContact(null);
        ds2.getSecondaryContact();
        ds2.setSecondaryContact(null);
        dasd.getPdId();
        dasd.setPdId(null);
        dasd.getUserId();
        dasd.setUserId(null);
        usec.getSecondaryContact();
        usec.setSecondaryContact(null);
    }
}
