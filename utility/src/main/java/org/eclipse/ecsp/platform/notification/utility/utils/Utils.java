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

package org.eclipse.ecsp.platform.notification.utility.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

/**
 * Utility class to define, common utility method used in notification-utility.
 *
 * @author MBadoni
 */
public class Utils {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(Utils.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ObjectMapper OBJECT_MAPPER_CBOR = new ObjectMapper(new CBORFactory());

    private Utils() {
    }

    /**
     * fileExists method.
     *
     * @param dirPath  directory path
     * @param fileName file name
     *
     * @return boolean
     */
    public static boolean fileExists(String dirPath, String fileName) {
        return Files.exists(Paths.get(dirPath, fileName), LinkOption.NOFOLLOW_LINKS);

    }

    /**
     * prepareErrorResponse method.
     *
     * @param ex exception
     *
     * @return errorMessage
     */
    public static String prepareErrorResponse(RestClientResponseException ex) {
        String errorMsg = null;
        JsonNode errorResponse;
        String errorMessage = null;
        try {
            if (ex.getResponseHeaders() != null
                && MediaType.APPLICATION_CBOR.equalsTypeAndSubtype(ex.getResponseHeaders().getContentType())) {
                errorResponse = OBJECT_MAPPER_CBOR.readValue(ex.getResponseBodyAsByteArray(), JsonNode.class);
                LOGGER.debug("prepare error response message content-type application/cbor");
            } else {
                errorResponse = OBJECT_MAPPER.readValue(ex.getResponseBodyAsByteArray(), JsonNode.class);
            }
            LOGGER.info("prepare error response message {}", errorResponse);
            if (errorResponse.isArray()) {
                errorMessage = errorResponse.get(0).get(Constants.ERROR_MSG).asText();
            } else {
                errorMessage = errorResponse.get(Constants.ERROR_MSG).asText();
            }
            errorMsg = String.format("Api call failed statusCode %s error message %s ", ex.getRawStatusCode(),
                errorMessage != null ? errorMessage : ex.getMostSpecificCause());
        } catch (IOException e) {
            errorMsg = String.format("Api call failed statusCode %s ,failed to parse error response message %s ",
                ex.getRawStatusCode(),
                e.getMessage());
        }
        return errorMsg;

    }

    /**
     * prepareSuccessResponse method.
     *
     * @param httpStatusCode http status code
     *
     * @return successMessage
     */
    public static String prepareSuccessResponse(int httpStatusCode) {
        return String.format("%s %s", Constants.SUCCESS_MSG, httpStatusCode);
    }

    /**
     * buildFileName method.
     *
     * @param service  service name
     * @param fileName file name
     *
     * @return fileName
     */
    public static String buildFileName(String service, String fileName) {
        return String.format("%s-%s", service, fileName);
    }
}
