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

package org.eclipse.ecsp.platform.notification.utility.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.notification.BaseTemplate;
import org.eclipse.ecsp.notification.config.NotificationConfigRequest;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.entities.NotificationTemplateConfig;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.platform.notification.utility.utils.Constants;
import org.eclipse.ecsp.platform.notification.utility.utils.Utils;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NotificationConfigUpdateService class.
 *
 * @author MBadoni
 */
@Service
public class NotificationConfigUpdateService {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(NotificationConfigUpdateService.class);

    @Autowired
    private RestTemplate restTemplate;


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(Include.NON_EMPTY).setSerializationInclusion(Include.NON_NULL)
        .addMixIn(BaseTemplate.class, BaseTemplateMixIn.class);

    private Map<String, String> processedRequest = new HashMap<>();

    private Map<String, String> failedRequest = new HashMap<>();


    @Value("${config.file.path}")
    private String configFilePath;

    @Value("${notification.config.update.url}")
    private String notificationConfigUpdateUrl;

    @Value("${notification.grouping.update.url}")
    private String notificationGroupingUpdateUrl;

    @Value("${notification.template.update.url}")
    private String notificationTemplateUpdateUrl;

    @Value("${notification.template.config.update.url}")
    private String notificationTemplateConfigUpdateUrl;

    @Value("${notification.grouping.delete.url}")
    private String notificationGroupingDeleteUrl;

    @Value("${notification.template.delete.url}")
    private String notificationTemplateDeleteUrl;

    @Value("${region}")
    private String region;

    @Value("${service}")
    private String service;

    /**
     * processConfigUpdate start method.
     */
    public void processConfigUpdate() {
        LOGGER.info("Notification config update job started");
        readConfigFileAndProcess();
        LOGGER.info(
            "Job Summary - Failed config request count: {}, Successful config request count: {},"
                + " Failed config request Details: {}",
            failedRequest.size(), processedRequest.size(), new JSONObject(failedRequest).toString());
    }

    /**
     * readConfigFileAndProcess method.
     */
    private void readConfigFileAndProcess() {
        try (Stream<Path> paths = Files.list(Paths.get(configFilePath))
            .filter(path -> path.toString().endsWith(Constants.JSON_EXTN))) {
            paths.forEach(path -> {
                String fileName = path.getFileName().toString();
                try {
                    LOGGER.info("Reading notification config file: {}", fileName);
                    if (fileName.endsWith(Constants.NOTIFICATION_CONFIG_FILE)) {
                        List<NotificationConfigRequest> notificationConfigRequest = readJsonFile(path,
                            new TypeReference<List<NotificationConfigRequest>>() {
                            });
                        updateNotificationConfig(fileName, notificationConfigRequest);
                    } else if (fileName.endsWith(Constants.NOTIFICATION_GROUPING_CONFIG_FILE)) {
                        List<NotificationGrouping> notificationGroupings = readJsonFile(path,
                            new TypeReference<List<NotificationGrouping>>() {
                            });
                        updateNotificationGrouping(fileName, notificationGroupings);
                    } else if (fileName.endsWith(Constants.NOTIFICATION_TEMPLATE_FILE)) {
                        List<NotificationTemplate> notificationTemplates = readJsonFile(path,
                            new TypeReference<List<NotificationTemplate>>() {
                            });
                        updateNotificationTemplate(fileName, notificationTemplates);
                    } else if (fileName.endsWith(Constants.NOTIFICATION_TEMPLATE_CONFIG_FILE)) {
                        List<NotificationTemplateConfig> notificationTemplateConfigs = readJsonFile(path,
                            new TypeReference<List<NotificationTemplateConfig>>() {
                            });
                        updateNotificationTemplatesConfigs(fileName, notificationTemplateConfigs);
                    }
                    LOGGER.info("Successfully processed {} config file", fileName);
                } catch (IOException e) {
                    String errorMessage = String.format("Failed to read notification config file %s with error %s ",
                        fileName, e.getMessage());
                    LOGGER.error("Exception while reading notification config files: {}", errorMessage, e);
                    failedRequest.put(fileName, errorMessage);
                } catch (Exception e) {
                    String errorMessage =
                        String.format("Failed to processing notification config file %s with error %s ",
                            fileName, e.getMessage());
                    LOGGER.error("Exception while processing notification config files: {}", errorMessage, e);
                    failedRequest.put(fileName, errorMessage);
                }
            });
        } catch (IOException e) {
            String errorMessage = String.format("Invalid notification config file path %s", configFilePath);
            LOGGER.error("Exception while notification accessing the config path: {}", errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * readJsonFile method.
     *
     * @param path path
     * @param typeReference typeReference
     * @param <T> T
     * @return T
     * @throws IOException IOException
     */
    private <T> T readJsonFile(Path path, TypeReference<T> typeReference) throws IOException {
        return OBJECT_MAPPER.readValue(
            Files.readString(path), typeReference);
    }

    /**
     * updateNotificationTemplatesConfigs method.
     *
     * @param notificationTemplateConfigFileName notificationTemplateConfigFileName
     * @param notificationTemplateConfigs notificationTemplateConfigs
     */
    private void updateNotificationTemplatesConfigs(String notificationTemplateConfigFileName,
                                                    List<NotificationTemplateConfig> notificationTemplateConfigs) {

        try {
            HttpEntity<List<NotificationTemplateConfig>> request =
                new HttpEntity<>(notificationTemplateConfigs, createHttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(notificationTemplateConfigUpdateUrl,
                HttpMethod.POST, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                processedRequest.put(notificationTemplateConfigFileName,
                    Utils.prepareSuccessResponse(response.getStatusCodeValue()));
                LOGGER.info("Default notification template configs create/update request status {}",
                    response.getStatusCodeValue());
            }
        } catch (RestClientResponseException ex) {
            String errorMsg = Utils.prepareErrorResponse(ex);
            failedRequest.put(notificationTemplateConfigFileName, errorMsg);
            LOGGER.error(errorMsg, ex);
        }
    }

    /**
     * updateNotificationTemplate method.
     *
     * @param notificationTemplateFileName notificationTemplateFileName
     * @param notificationTemplates notificationTemplates
     */
    private void updateNotificationTemplate(String notificationTemplateFileName,
                                            List<NotificationTemplate> notificationTemplates) {
        try {
            notificationTemplates = notificationTemplates.stream()
                .filter(nt -> deleteExistingNotificationTemplate(nt, notificationTemplateFileName))
                .collect(Collectors.toList());
            if (notificationTemplates.isEmpty()) {
                LOGGER.info("Default notification templates no record to process");
                return;
            }

            MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
            messageConverter.setPrettyPrint(false);
            messageConverter.setObjectMapper(OBJECT_MAPPER);
            restTemplate.getMessageConverters()
                .removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
            restTemplate.getMessageConverters().add(messageConverter);
            HttpEntity<List<NotificationTemplate>> request =
                new HttpEntity<>(notificationTemplates, createHttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(notificationTemplateUpdateUrl, HttpMethod.POST,
                request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                processedRequest.put(notificationTemplateFileName,
                    Utils.prepareSuccessResponse(response.getStatusCodeValue()));
                LOGGER.info("Default notification templates create/update request status {}",
                    response.getStatusCodeValue());
            }
        } catch (RestClientResponseException ex) {
            String errorMsg = Utils.prepareErrorResponse(ex);
            failedRequest.put(notificationTemplateFileName, errorMsg);
            LOGGER.error(errorMsg, ex);
        }
    }

    /**
     * updateNotificationGrouping method.
     *
     * @param notificationGroupingFileName notificationGroupingFileName
     * @param notificationGroupings notificationGroupings
     */
    private void updateNotificationGrouping(String notificationGroupingFileName,
                                            List<NotificationGrouping> notificationGroupings) {
        try {
            notificationGroupings =
                notificationGroupings.stream().filter(ng -> deleteExistingGroup(ng, notificationGroupingFileName))
                    .collect(Collectors.toList());
            if (notificationGroupings.isEmpty()) {
                LOGGER.info("Default notification grouping no record to process");
                return;
            }
            HttpEntity<List<NotificationGrouping>> request = new HttpEntity<>(notificationGroupings,
                createHttpHeaders());
            ResponseEntity<String> response = restTemplate.exchange(notificationGroupingUpdateUrl, HttpMethod.POST,
                request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                processedRequest.put(notificationGroupingFileName,
                    Utils.prepareSuccessResponse(response.getStatusCodeValue()));
                LOGGER.info("Default notification grouping create/update request status {}",
                    response.getStatusCodeValue());
            }
        } catch (RestClientResponseException ex) {
            String errorMsg = Utils.prepareErrorResponse(ex);
            failedRequest.put(notificationGroupingFileName, errorMsg);
            LOGGER.error(errorMsg, ex);
        }
    }

    /**
     * updateNotificationConfig method.
     *
     * @param notificationConfigFileName notificationConfigFileName
     * @param notificationConfigRequest notificationConfigRequest
     */
    private void updateNotificationConfig(String notificationConfigFileName,
                                          List<NotificationConfigRequest> notificationConfigRequest) {
        try {
            MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
            messageConverter.setPrettyPrint(false);
            messageConverter.setObjectMapper(OBJECT_MAPPER);
            restTemplate.getMessageConverters()
                .removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
            restTemplate.getMessageConverters().add(messageConverter);
            ResponseEntity<String> response = restTemplate.exchange(notificationConfigUpdateUrl, HttpMethod.PATCH,
                new HttpEntity<>(notificationConfigRequest, createHttpHeaders()), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                processedRequest.put(notificationConfigFileName,
                    Utils.prepareSuccessResponse(response.getStatusCodeValue()));
                LOGGER.info("Default notification config create/update request status {}",
                    response.getStatusCodeValue());
            }

        } catch (RestClientResponseException ex) {
            String errorMsg = Utils.prepareErrorResponse(ex);
            failedRequest.put(notificationConfigFileName, errorMsg);
            LOGGER.error(errorMsg, ex);
        }
    }

    /**
     * createHttpHeaders method.
     *
     * @return HttpHeaders
     */
    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Constants.REQUEST_ID, Constants.UTILITY_REQUEST_ID);
        return headers;
    }

    /**
     * delete existing group before update.
     *
     * @param notificationGroup group
     *
     * @param notificationGroupingFileName file
     *
     * @return boolean success or failure
     */
    public boolean deleteExistingGroup(NotificationGrouping notificationGroup, String notificationGroupingFileName) {
        boolean deleted = false;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        Map<String, Object> map = new HashMap<>();
        String notificationId = notificationGroup.getNotificationId();
        String serviceName = notificationGroup.getService();
        String group = notificationGroup.getGroup();
        LOGGER.info(" Delete existing group before reimporting notificationId {} group {} service {}",
            notificationId, group, serviceName);
        map.put(Constants.GROUP, group);
        map.put(Constants.NOTIFICATION_ID, notificationId);
        map.put(Constants.SERVICE, serviceName);
        try {
            restTemplate.exchange(notificationGroupingDeleteUrl, HttpMethod.DELETE, httpEntity, Void.class, map);
            LOGGER.info(
                " Deleted existing NotificationGrouping before reimporting, notificationId {} group {} service {}",
                notificationId, group, serviceName);
            deleted = true;
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
                LOGGER.info("NotificationGroup for notificationId {} doesn't exists", notificationId);
                deleted = true;
            } else {
                String errorMsg = Utils.prepareErrorResponse(ex);
                String key = String.format("%s_%s_%s_%s_delete", notificationGroupingFileName, notificationId, group,
                    serviceName);
                failedRequest.put(key, errorMsg);
                LOGGER.error("Exception while deleting NotifiicationGroup, notificationId {}", notificationId, ex);
            }
        }
        return deleted;
    }

    /**
     * delete existing notification template before update.
     *
     * @param notificationTemplate template
     *
     * @param notificationConfigFileName file
     *
     * @return boolean success or failure
     */
    private boolean deleteExistingNotificationTemplate(NotificationTemplate notificationTemplate,
                                                       String notificationConfigFileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Constants.REQUEST_ID, Constants.UTILITY_REQUEST_ID);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        Map<String, Object> map = new HashMap<>();
        String notificationId = notificationTemplate.getNotificationId();

        String locale = notificationTemplate.getLocale().toString();
        LOGGER.info(" Delete existing NotificationTemplate before reimporting, notificationId {} brand {} locale {}",
            notificationId, notificationTemplate.getBrand(), locale);
        boolean deleted = false;
        if (null != notificationTemplate.getBrand()
            && !Constants.DEFAULT_BRAND.equalsIgnoreCase(notificationTemplate.getBrand())) {
            String key = String.format("%s_%s_%s_%s_delete", notificationConfigFileName, notificationId,
                notificationTemplate.getBrand(), locale);
            failedRequest.put(key, Constants.NON_DEFAULT_NOTIFICATION_TEMPLATE_CAN_NOT_PROCESSED);
            LOGGER.error("Notification Template with notification id {} brand {} {}", notificationId,
                notificationTemplate.getBrand(), Constants.NON_DEFAULT_NOTIFICATION_TEMPLATE_CAN_NOT_PROCESSED);
            return deleted;
        }
        String brand = Constants.DEFAULT_BRAND;
        map.put(Constants.NOTIFICATION_ID, notificationId);
        map.put(Constants.BRAND, brand);
        map.put(Constants.LOCALE, locale);
        try {
            restTemplate.exchange(notificationTemplateDeleteUrl, HttpMethod.DELETE, httpEntity, Void.class, map);
            LOGGER.info(
                " Deleted existing NotificationTemplate before reimporting notificationId {} brand {} locale {}",
                notificationId, brand, locale);
            deleted = true;
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() == HttpStatus.BAD_REQUEST.value()) {
                LOGGER.info("NotificationTemplate for notificationId {} doesn't exists", notificationId);
                deleted = true;
            } else {
                String errorMsg = Utils.prepareErrorResponse(ex);
                String key =
                    String.format("%s_%s_%s_%s_delete", notificationConfigFileName, notificationId, brand, locale);
                failedRequest.put(key, errorMsg);
                LOGGER.error("Exception while deleting NotifiicationTemplate, notificationId {}", notificationId, ex);
            }

        }
        return deleted;
    }

}
