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

package org.eclipse.ecsp.notification.browser;

import org.eclipse.ecsp.notification.adaptor.AbstractChannelNotifier;
import org.eclipse.ecsp.notification.adaptor.NotificationEventFields.ChannelType;

/**
 * BrowserNotifier class.
 */
public abstract class BrowserNotifier extends AbstractChannelNotifier {
    private static final String CHANNEL_TYPE = ChannelType.BROWSER.getChannelTypeName();

    /**
     * Method to get protocol.
     *
     * @return protocol
     */
    @Override
    public String getProtocol() {
        return CHANNEL_TYPE;
    }

}
