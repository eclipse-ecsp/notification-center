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
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.util.Map;

import static org.eclipse.ecsp.notification.dao.NotificationDaoConstants.NOTIFICATION_TEMPLATE_CONFIGS_COLLECTION_NAME;

/**
 *  NotificationTemplateConfig entity.
 */
@Entity(value = NOTIFICATION_TEMPLATE_CONFIGS_COLLECTION_NAME)
public class NotificationTemplateConfig extends AbstractIgniteEntity {
    @Id
    private String notificationId;

    @JsonProperty("apiPush")
    private Map<String, Object> apiPushConfig;

    @JsonProperty("email")
    private EmailConfig emailConfig;

    @JsonProperty("sms")
    private SmsConfig smsConfig;

    @JsonProperty("ivm")
    private IvmConfig ivmConfig;

    public NotificationTemplateConfig() {
        super();
    }

    /**
     * This method is a getter for apipushconfig.
     *
     * @return Map
     */

    public Map<String, Object> getApiPushConfig() {
        return apiPushConfig;
    }

    /**
     * This method is a setter for apipushconfig.
     *
     * @param apiPushConfig : Map
     */

    public void setApiPushConfig(Map<String, Object> apiPushConfig) {
        this.apiPushConfig = apiPushConfig;
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
     * This method is a getter for emailconfig.
     *
     * @return EmailConfig
     */

    public EmailConfig getEmailConfig() {
        return emailConfig;
    }

    /**
     * This method is a setter for emailconfig.
     *
     * @param emailConfig : EmailConfig
     */

    public void setEmailConfig(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    /**
     * This method is a getter for smsconfig.
     *
     * @return SmsConfig
     */

    public SmsConfig getSmsConfig() {
        return smsConfig;
    }

    /**
     * This method is a setter for smsconfig.
     *
     * @param smsConfig : SmsConfig
     */

    public void setSmsConfig(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    /**
     * This method is a getter for ivmconfig.
     *
     * @return IvmConfig
     */

    public IvmConfig getIvmConfig() {
        return ivmConfig;
    }

    /**
     * This method is a setter for ivmconfig.
     *
     * @param ivmConfig : IvmConfig
     */

    public void setIvmConfig(IvmConfig ivmConfig) {
        this.ivmConfig = ivmConfig;
    }

    @Override
    public String toString() {
        return "NotificationTemplateConfig [notificationId=" + notificationId + ", apiPushConfig=" + apiPushConfig
            + ", emailConfig=" + emailConfig + ", smsConfig=" + smsConfig + ", ivmConfig=" + ivmConfig + "]";
    }

}