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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Notification class.
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 7186241932115310762L;
    private String title;

    private String subtitle;

    @JsonProperty(value = "body")
    private String body;

    @JsonProperty(value = "click_action")
    private String clickAction;

    private String bannerTitle;

    private String bannerDesc;

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

    /**
     * Getter for Body.
     *
     * @return body
     */
    public String getBody() {
        return body;
    }

    /**
     * Setter for Body.
     *
     * @param body the new value
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Getter for ClickAction.
     *
     * @return clickaction
     */
    public String getClickAction() {
        return clickAction;
    }

    /**
     * Setter for ClickAction.
     *
     * @param clickAction the new value
     */
    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    /**
     * Getter for BannerTitle.
     *
     * @return bannertitle
     */
    public String getBannerTitle() {
        return bannerTitle;
    }

    /**
     * Setter for BannerTitle.
     *
     * @param bannerTitle the new value
     */
    public void setBannerTitle(String bannerTitle) {
        this.bannerTitle = bannerTitle;
    }

    /**
     * Getter for BannerDesc.
     *
     * @return bannerdesc
     */
    public String getBannerDesc() {
        return bannerDesc;
    }

    /**
     * Setter for BannerDesc.
     *
     * @param bannerDesc the new value
     */
    public void setBannerDesc(String bannerDesc) {
        this.bannerDesc = bannerDesc;
    }

    @Override
    public String toString() {
        return String.format(
            "Notification [title=%s, subtitle=%s, body=%s, clickAction=%s, bannerTitle=%s, bannerDesc=%s]", title,
            subtitle, body, clickAction, bannerTitle, bannerDesc);
    }

}
