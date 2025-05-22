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

/**
 * RequestInfo class.
 */
public class RequestInfo {
    private String requestId;
    private String sessionId;
    private String clientRequestId;

    /**
     * RequestInfo constructor.
     */
    public RequestInfo() {

    }

    /**
     * RequestInfo constructor.
     *
     * @param requestId String
     *
     * @param sessionId String
     *
     * @param clientRequestId String
     */
    public RequestInfo(String requestId, String sessionId, String clientRequestId) {
        super();
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.clientRequestId = clientRequestId;
    }

    /**
     * Getter for RequestId.
     *
     * @return requestid
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter for RequestId.
     *
     * @param requestId the new value
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter for SessionId.
     *
     * @return sessionid
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Setter for SessionId.
     *
     * @param sessionId the new value
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Getter for ClientRequestId.
     *
     * @return clientrequestid
     */
    public String getClientRequestId() {
        return clientRequestId;
    }

    /**
     * Setter for ClientRequestId.
     *
     * @param clientRequestId the new value
     */
    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }
}
