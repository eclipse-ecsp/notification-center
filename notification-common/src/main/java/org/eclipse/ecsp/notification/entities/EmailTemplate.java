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
import org.eclipse.ecsp.domain.notification.BaseTemplate;

/**
 * EmailTemplate class.
 */
@Entity(discriminatorKey = "className", discriminator = "org.eclipse.ecsp.notification.entities.EmailTemplate")
public class EmailTemplate extends BaseTemplate {

    private String from;
    private String subject;
    @JsonIgnore
    private boolean isRichContent;

    /**
     * EmailTemplate default constructor.
     */
    public EmailTemplate() {
        super();
        contentFieldsGetter.put("subject", this::getSubject);
        contentFieldsSetter.put("subject", this::setSubject);
    }

    /**
     * This method is a getter for from.
     *
     * @return String
     */

    public String getFrom() {
        return from;
    }

    /**
     * This method is a setter for from.
     *
     * @param from : String
     */

    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * This method is a getter for subject.
     *
     * @return String
     */

    public String getSubject() {
        return subject;
    }

    /**
     * This method is a setter for subject.
     *
     * @param subject : String
     */

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "EmailTemplate [from=" + from + ", subject=" + subject + "]";
    }

    /**
     * This method is a getter for richcontent.
     *
     * @return boolean
     */
    @JsonIgnore
    public boolean isRichContent() {
        return isRichContent;
    }

    /**
     * This method is a setter for richcontent.
     *
     * @param richContent : boolean
     */

    public void setRichContent(boolean richContent) {
        isRichContent = richContent;
    }
}