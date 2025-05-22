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

package org.eclipse.ecsp.notification.processors;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.notification.entities.AdditionalLookupProperty;
import org.eclipse.ecsp.notification.entities.Ranker;
import org.springframework.util.CollectionUtils;

import java.util.Locale;

/**
 * Utility class.
 */
public class Utils {


    private static final int MINUS_ONE = -1;

    private static final int TWO = 2;

    private Utils() {
    }

    /**
     * getRank method.
     *
     * @param notificationTemplate Ranker
     * @param brand               String
     * @param locale              Locale
     * @param data                String
     * @return int
     */
    static int getRank(Ranker notificationTemplate, String brand, Locale locale, String data) {
        int rank = 0;
        int attributesSize = 0;
        if (!CollectionUtils.isEmpty(notificationTemplate.getAdditionalLookupProperties())) {
            attributesSize = notificationTemplate.getAdditionalLookupProperties().size();
            for (AdditionalLookupProperty additionalLookupProperty :
                    notificationTemplate.getAdditionalLookupProperties()) {
                String name = additionalLookupProperty.getName();
                if (name.startsWith("vehicleProfile")) {
                    name = name.replace("vehicleProfile", "vehicleProfile.vehicleAttributes");
                }
                String value = getValueFromData(data, name);
                if (StringUtils.isEmpty(value) || !additionalLookupProperty.getValues().contains(value)) {
                    return MINUS_ONE;
                }
                rank += (int) Math.pow(TWO, attributesSize - additionalLookupProperty.getOrder());
            }
        }
        if (notificationTemplate.getLocaleAsLocale().equals(locale)) {
            rank += (int) Math.pow(TWO, attributesSize + 1);
        }
        if (notificationTemplate.getBrand().equals(brand)) {
            rank += (int) Math.pow(TWO, attributesSize);
        }
        return rank;
    }

    /**
     * getValueFromData method.
     *
     * @param data String
     * @param name String
     * @return String
     */
    static String getValueFromData(String data, String name) {
        try {
            return JsonPath.read(data, name).toString().toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

}
