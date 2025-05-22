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

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;

import java.util.Locale;

/**
 * CloneNotificationTemplate class.
 */
@Entity(useDiscriminator = false)
public class CloneNotificationTemplate {

    private String id;
    private String notificationId;
    private String notificationShortName;
    private String notificationLongName;
    private Locale locale;
    private ChannelTemplates channelTemplates;

    /**
     * This is a default constructor.
     */
    public CloneNotificationTemplate() {
    }

    /**
     * This method is a getter for id.
     *
     * @return String
     */

    public String getId() {
        return id;
    }

    /**
     * This method is a setter for id.
     *
     * @param id : String
     */

    public void setId(String id) {
        this.id = id;
    }

    /**
     * This method is a getter for notificationid.
     *
     * @return String
     */

    public String getNotificationId() {
        return notificationId;
    }

    /**
     * This method is a setter for notificationid.
     *
     * @param notificationId : String
     */

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * This method is a getter for locale.
     *
     * @return Locale
     */

    public Locale getLocale() {
        return locale;
    }

    /**
     * This method is a setter for locale.
     *
     * @param locale : Locale
     */

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * This method is a getter for notificationshortname.
     *
     * @return String
     */

    public String getNotificationShortName() {
        return notificationShortName;
    }

    /**
     * This method is a setter for notificationshortname.
     *
     * @param notificationShortName : String
     */

    public void setNotificationShortName(String notificationShortName) {
        this.notificationShortName = notificationShortName;
    }

    /**
     * This method is a getter for notificationlongname.
     *
     * @return String
     */

    public String getNotificationLongName() {
        return notificationLongName;
    }

    /**
     * This method is a setter for notificationlongname.
     *
     * @param notificationLongName : String
     */

    public void setNotificationLongName(String notificationLongName) {
        this.notificationLongName = notificationLongName;
    }

    /**
     * This method is a getter for channeltemplates.
     *
     * @return ChannelTemplates
     */

    public ChannelTemplates getChannelTemplates() {
        return channelTemplates;
    }

    /**
     * This method is a setter for channeltemplates.
     *
     * @param channelTemplates : ChannelTemplates
     */

    public void setChannelTemplates(ChannelTemplates channelTemplates) {
        this.channelTemplates = channelTemplates;
    }


    /**
     * This method is a getter for emailtemplate.
     *
     * @return EmailTemplate
     */
    @JsonIgnore
    public EmailTemplate getEmailTemplate() {
        return channelTemplates.getEmail();
    }



    /**
     * This method is a getter for apipushtemplate.
     *
     * @return APIPushTemplate
     */
    @JsonIgnore
    public APIPushTemplate getApiPushTemplate() {
        return channelTemplates.getApiPush();
    }



    /**
     * This method is a getter for smstemplate.
     *
     * @return SMSTemplate
     */
    @JsonIgnore
    public SMSTemplate getSmsTemplate() {
        return channelTemplates.getSms();
    }



    /**
     * This method is a getter for ivmtemplate.
     *
     * @return IVMTemplate
     */
    @JsonIgnore
    public IVMTemplate getIvmTemplate() {
        return channelTemplates.getIvm();
    }



    /**
     * This method is a getter for pushtemplate.
     *
     * @return PushTemplate
     */
    @JsonIgnore
    public PushTemplate getPushTemplate() {
        return channelTemplates.getPush();
    }



    /**
     * This method is a getter for portaltemplate.
     *
     * @return PortalTemplate
     */
    @JsonIgnore
    public PortalTemplate getPortalTemplate() {
        return channelTemplates.getPortal();
    }

    /**
     * To String.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "NotificationTemplate [id=" + id + ", notificationId=" + notificationId + ", notificationShortName="
            + notificationShortName + ", notificationLongName=" + notificationLongName + ", locale=" + locale
            + ", channelTemplates=" + channelTemplates + "]";
    }
}