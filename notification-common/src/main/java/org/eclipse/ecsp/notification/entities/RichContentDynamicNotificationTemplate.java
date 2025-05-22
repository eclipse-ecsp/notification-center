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

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.eclipse.ecsp.notification.dao.NotificationDaoConstants.RICH_CONTENT_NOTIFICATION_TEMPLATES_COLLECTION_NAME;

/**
 * RichContentDynamicNotificationTemplate entity.
 */
@Entity(value = RICH_CONTENT_NOTIFICATION_TEMPLATES_COLLECTION_NAME)
public class RichContentDynamicNotificationTemplate extends AbstractIgniteEntity
    implements Ranker, PlaceholderContainer {
    @Id
    private String id;
    private String notificationId;
    private String locale;
    private String brand;
    private String html;
    private String lastUpdateTime;
    private Set<String> customPlaceholders;
    private List<EmailAttachment> attachments;
    private List<AdditionalLookupProperty> additionalLookupProperties;

    /**
     * Get id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get Notification Id.
     *
     * @return String
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Set Notification Id.
     *
     * @param notificationId notificationId
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Get locale.
     *
     * @return locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Set locale.
     *
     * @param locale locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Get brand.
     *
     * @return brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Set brand.
     *
     * @param brand brand
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * Get html.
     *
     * @return html
     */
    public String getHtml() {
        return html;
    }

    /**
     * Set html.
     *
     * @param html html
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * Get lastUpdateTime.
     *
     * @return lastUpdateTime
     */
    public String getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    /**
     * Set lastUpdateTime.
     *
     * @param lastUpdateTime lastUpdateTime
     */
    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * Get attachments.
     *
     * @return attachments
     */
    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Set attachments.
     *
     * @param attachments attachments
     */
    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Get additionalLookupProperties.
     *
     * @return additionalLookupProperties
     */
    public List<AdditionalLookupProperty> getAdditionalLookupProperties() {
        return this.additionalLookupProperties;
    }

    /**
     * Set additionalLookupProperties.
     *
     * @param additionalLookupProperties additionalLookupProperties
     */
    public void setAdditionalLookupProperties(List<AdditionalLookupProperty> additionalLookupProperties) {
        this.additionalLookupProperties = additionalLookupProperties;
    }

    /**
     * Get customPlaceholders.
     *
     * @return customPlaceholders
     */
    public Set<String> getCustomPlaceholders() {
        return customPlaceholders;
    }

    /**
     * Set customPlaceholders.
     *
     * @param customPlaceholders customPlaceholders
     */
    public void setCustomPlaceholders(Set<String> customPlaceholders) {
        this.customPlaceholders = customPlaceholders;
    }

    /**
     * Get locale as Locale.
     *
     * @return Locale
     */
    @Override
    public Locale getLocaleAsLocale() {
        return Locale.forLanguageTag(locale.replace("_", "-"));
    }

    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "RichContentDynamicNotificationTemplate={id=" + id + ", notificationId=" + notificationId
            + ", locale=" + locale + ", brand=" + brand + ", html=" + html
            + ", lastUpdateTime=" + lastUpdateTime + ", attachments="
            + (attachments == null ? "[]" : Arrays.toString(attachments.toArray()))
            + ", additionalLookupProperties=" + (CollectionUtils.isEmpty(additionalLookupProperties)
            ? "[]" : Arrays.toString(additionalLookupProperties.toArray()))
            + ", customPlaceholders=" + (CollectionUtils.isEmpty(customPlaceholders)
            ? "[]" : Arrays.toString(customPlaceholders.toArray())) + ",}";
    }
}
