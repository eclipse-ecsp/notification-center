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

package org.eclipse.ecsp.notification.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * DTCMasterClient class to call DTC saas svc.
 */
@Service
public class DtcMasterClient {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DtcMasterClient.class);
    private static final String DTC_DESCRIPTION = "description";

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPropertyInclusion(Include.NON_NULL);
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${dtc.api.base.url.with.version}")
    private String dtcMasterServiceUrl;

    @Autowired
    @Qualifier("servicesCommonRestTemplate")
    private RestTemplate restTemplate;

    /**
     * DtcMasterClient constructor.
     */
    public DtcMasterClient() {
    }

    /**
     * getDtcList method to get DTC list.
     *
     * @param dtcSetOrCleared List
     * @return String array of dtc codes
     */
    public String[] getDtcList(List<String> dtcSetOrCleared) {
        String[] dtcList = null;
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(dtcSetOrCleared, null);
        try {
            LOGGER.debug("Parameters passed to DTCMasterClint are {}  and dtcMasterServiceUrl is {}",
                requestEntity.getBody(),
                dtcMasterServiceUrl);
            ResponseEntity<String> response = restTemplate.exchange(dtcMasterServiceUrl, HttpMethod.POST,
                requestEntity, String.class);
            dtcList = getDtcDescriptions(response.getBody());
        } catch (Exception ex) {
            LOGGER.error("Unable to retrieve the response from DTCMasterClient due to : ", ex);
        }
        return dtcList;
    }

    /**
     * getDtcDescriptions method to get DTC descriptions.
     *
     * @param dtcList String
     * @return String array of dtc descriptions
     */
    private String[] getDtcDescriptions(String dtcList) {

        JsonNode dtcMasterList;
        String[] dtcDescriptions = null;
        try {
            dtcMasterList = mapper.readValue(dtcList, JsonNode.class);

        } catch (IOException e) {
            LOGGER.error("Exception occurred due to : ", e);
            return new String[]{};
        }
        if (dtcMasterList != null) {
            List<JsonNode> jsonDtcDescriptions = dtcMasterList.findValues(DTC_DESCRIPTION);

            dtcDescriptions = new String[jsonDtcDescriptions.size()];
            int index = 0;
            for (JsonNode jsonDtcDescription : jsonDtcDescriptions) {
                dtcDescriptions[index] = jsonDtcDescription.asText();
                ++index;
            }
        }
        return dtcDescriptions;
    }
}
