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

import dev.morphia.annotations.AlsoLoad;
import dev.morphia.annotations.Entity;

/**
 * ChannelTemplates class contains all channel template.
 */
@Entity(useDiscriminator = false)
public class ChannelTemplates {
    private PushTemplate push;
    @AlsoLoad("apipush")
    private APIPushTemplate apiPush;
    private EmailTemplate email;
    private SMSTemplate sms;
    private IVMTemplate ivm;
    private PortalTemplate portal;

    /**
     * This is a default constructor.
     */
    public ChannelTemplates() {
        super();
    }

    /**
     * This method is a getter for push.
     *
     * @return PushTemplate
     */

    public PushTemplate getPush() {
        return push;
    }

    /**
     * This method is a setter for push.
     *
     * @param push : PushTemplate
     */

    public void setPush(PushTemplate push) {
        this.push = push;
    }

    /**
     * This method is a getter for apipush.
     *
     * @return APIPushTemplate
     */

    public APIPushTemplate getApiPush() {
        return apiPush;
    }

    /**
     * This method is a setter for apipush.
     *
     * @param apiPushTemplate : APIPushTemplate
     */

    public void setApiPush(APIPushTemplate apiPushTemplate) {
        this.apiPush = apiPushTemplate;
    }

    /**
     * This method is a getter for email.
     *
     * @return EmailTemplate
     */

    public EmailTemplate getEmail() {
        return email;
    }

    /**
     * This method is a setter for email.
     *
     * @param email : EmailTemplate
     */

    public void setEmail(EmailTemplate email) {
        this.email = email;
    }

    /**
     * This method is a getter for sms.
     *
     * @return SMSTemplate
     */

    public SMSTemplate getSms() {
        return sms;
    }

    /**
     * This method is a setter for sms.
     *
     * @param sms : SMSTemplate
     */

    public void setSms(SMSTemplate sms) {
        this.sms = sms;
    }

    /**
     * This method is a getter for ivm.
     *
     * @return IVMTemplate
     */

    public IVMTemplate getIvm() {
        return ivm;
    }

    /**
     * This method is a setter for ivm.
     *
     * @param ivm : IVMTemplate
     */

    public void setIvm(IVMTemplate ivm) {
        this.ivm = ivm;
    }

    /**
     * This method is a getter for portal.
     *
     * @return PortalTemplate
     */

    public PortalTemplate getPortal() {
        return portal;
    }

    /**
     * This method is a setter for portal.
     *
     * @param portal : PortalTemplate
     */

    public void setPortal(PortalTemplate portal) {
        this.portal = portal;
    }

    @Override
    public String toString() {
        return "ChannelTemplates [push=" + push + ", apiPush=" + apiPush + ", email=" + email
            + ", sms=" + sms + ", ivm=" + ivm + ", portal=" + portal + "]";
    }

}