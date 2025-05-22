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
import org.eclipse.ecsp.entities.ContextAwareEventData;

/**
 * PushApiTestEventData class.
 */
@EventMapping(id = EventID.API_PUSH_TEST_EVENT, version = Version.V1_0)
public class PushApiTestEventData extends AbstractEventData implements ContextAwareEventData {

    private static final long serialVersionUID = 8220108881635243135L;

    private String user;


    /**
     * Getter for User.
     *
     * @return user
     */
    @Override
    public String getUser() {
        return user;
    }

    /**
     * Setter for User.
     *
     * @param user the new value
     */
    public void setUser(String user) {
        this.user = user;
    }

}
