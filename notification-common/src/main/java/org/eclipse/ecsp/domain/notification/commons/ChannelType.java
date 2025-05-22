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

package org.eclipse.ecsp.domain.notification.commons;

/**
 * ChannelType enum.
 */
public enum ChannelType {

    /**
     * MOBILE_APP_PUSH enum.
     */
    MOBILE_APP_PUSH("push", "application"),
    /**
     * SMS enum.
     */
    SMS("sms", "sms"),
    /**
     * EMAIL enum.
     */
    EMAIL("email", "email"),
    /**
     * API_PUSH enum.
     */
    API_PUSH("apiPush", "apiPush"),
    /**
     * IVM enum.
     */
    IVM("ivm", "ivm"),
    /**
     * PORTAL enum.
     */
    PORTAL("portal", "portal");

    private String type;
    private String protocol;

    /**
     * ChannelType constructor.
     *
     * @param type string
     * @param protocol string
     */
    private ChannelType(String type, String protocol) {
        this.type = type;
        this.protocol = protocol;
    }

    /**
     * get channel type.
     *
     * @return string
     */
    public String getChannelType() {
        return type;
    }

    /**
     * get channel type.
     *
     * @param channel string
     *
     * @return ChannelType
     */
    public static ChannelType getChannelType(String channel) {
        for (ChannelType type : ChannelType.values()) {
            if (type.getChannelType().equals(channel)) {
                return type;
            }
        }
        return null;

    }

    /**
     * get protocol.
     *
     * @return string
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * method to check if channel is supported.
     *
     * @param channel string
     *
     * @return boolean
     */
    public static boolean isChannelSupported(String channel) {

        boolean channelSupported = false;
        for (ChannelType type : ChannelType.values()) {
            if (type.getChannelType().equals(channel)) {
                channelSupported = true;
                break;
            }

        }
        return channelSupported;
    }
}
