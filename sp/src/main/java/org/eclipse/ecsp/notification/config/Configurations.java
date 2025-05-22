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

package org.eclipse.ecsp.notification.config;

import org.eclipse.ecsp.notification.processors.transformers.AlertsInfoToDtoConverter;
import org.eclipse.ecsp.notification.processors.transformers.BasicContextConverter;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configurations class.
 */
@Configuration
public class Configurations {
    /**
     * getJavaMailSender bean.
     *
     * @param port Integer
     * @param host String
     * @param userName String
     * @param password String
     * @param protocol String
     * @param startsslEnabled String
     * @param startsslRequired String
     * @return JavaMailSender bean
     */
    @ConditionalOnProperty(
        prefix = "spring.mail",
        name = "host")
    @Bean
    public JavaMailSender getJavaMailSender(@Value("${spring.mail.port}") int port,
                                            @Value("${spring.mail.host}") String host,
                                            @Value("${spring.mail.username}") String userName,
                                            @Value("${spring.mail.password}") String password,
                                            @Value("${spring.mail.protocol}") String protocol,
                                            @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
                                            String startsslEnabled,
                                            @Value("${spring.mail.properties.mail.smtp.starttls.required}")
                                            String startsslRequired) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(userName);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.starttls.enable", startsslEnabled);
        props.put("mail.smtp.starttls.required", startsslRequired);

        return mailSender;
    }

    /**
     * alertsInfoToDtoConverter bean.
     *
     * @return AlertsInfoToDtoConverter bean
     */
    @Bean
    @ConditionalOnMissingBean()
    public AlertsInfoToDtoConverter alertsInfoToDtoConverter() {
        return new BasicContextConverter();
    }

    /**
     * pluginManager bean.
     *
     * @return SpringPluginManager bean
     */
    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }
}
