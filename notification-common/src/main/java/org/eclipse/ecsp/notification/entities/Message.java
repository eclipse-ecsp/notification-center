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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message class.
 */
public class Message {

    @JsonProperty("in-app")
    private InAppMessage inAppMessage;

    @JsonProperty("push")
    private PushMessage pushMessage;

    /**
     * Message default constructor.
     */
    public Message() {
        super();
    }

    /**
     * This method is a getter for inappmessage.
     *
     * @return InAppMessage
     */

    public InAppMessage getInAppMessage() {
        return inAppMessage;
    }

    /**
     * This method is a setter for inappmessage.
     *
     * @param inAppMessage : InAppMessage
     */

    public void setInAppMessage(InAppMessage inAppMessage) {
        this.inAppMessage = inAppMessage;
    }

    /**
     * This method is a getter for pushmessage.
     *
     * @return PushMessage
     */

    public PushMessage getPushMessage() {
        return pushMessage;
    }

    /**
     * This method is a setter for pushmessage.
     *
     * @param pushMessage : PushMessage
     */

    public void setPushMessage(PushMessage pushMessage) {
        this.pushMessage = pushMessage;
    }

    @Override
    public String toString() {
        return "Message [inAppMessage=" + inAppMessage + ", pushMessage=" + pushMessage + "]";
    }
}