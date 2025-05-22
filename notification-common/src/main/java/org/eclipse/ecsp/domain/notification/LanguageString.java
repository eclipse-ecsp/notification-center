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

import dev.morphia.annotations.Entity;

import java.io.Serializable;
import java.util.Map;

/**
 * LanguageString class.
 */
@Entity
public class LanguageString implements Serializable {

    private static final long serialVersionUID = -4635576482702752734L;
    private String language;
    private String messageText;
    private String title;
    private String subtitle;
    private Map<String, Object> additionalData;

    /**
     * Getter for Language.
     *
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Setter for Language.
     *
     * @param language the new value
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Getter for MessageText.
     *
     * @return messagetext
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Setter for MessageText.
     *
     * @param messageText the new value
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /**
     * Getter for Title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for Title.
     *
     * @param title the new value
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for Subtitle.
     *
     * @return subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Setter for Subtitle.
     *
     * @param subtitle the new value
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    /**
     * Setter for AdditionalData.
     *
     * @param additionalData the new value
     */
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "LanguageString [" + "language=" + language + ", messageText=" + messageText
            + ", title=" + title + ", subtitle=" + subtitle
            + ", additionalData=" + additionalData + ']';
    }
}
