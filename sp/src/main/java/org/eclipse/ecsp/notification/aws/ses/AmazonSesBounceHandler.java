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

package org.eclipse.ecsp.notification.aws.ses;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.analytics.stream.base.utils.ObjectUtils;
import org.eclipse.ecsp.domain.notification.utils.AwsUtils;
import org.eclipse.ecsp.notification.aws.ses.AmazonBounceNotification.Bounce;
import org.eclipse.ecsp.notification.aws.ses.AmazonBounceNotification.BounceMessage;
import org.eclipse.ecsp.notification.aws.ses.AmazonBounceNotification.BouncedRecipient;
import org.eclipse.ecsp.notification.key.store.KeyStore;
import org.eclipse.ecsp.notification.key.store.StoreUser;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AmazonSesBounceHandler class.
 */
public class AmazonSesBounceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonSesBounceHandler.class);
    private static final String PERMANENT_BOUNCE = "Permanent";

    private static final int TEN = 10;

    private static Properties properties;
    private final String awsRegion;
    private final BouncedEmailStore bouncedEmailStore;
    private final String awsSesBounceQueue;
    private final AWSCredentialsProviderChain credProviderChain;

    private AmazonSesBounceHandler() {
        this(properties);
    }

    /**
     * AmazonSesBounceHandler constructor.
     *
     * @param properties Properties
     */
    private AmazonSesBounceHandler(Properties properties) {

        credProviderChain = new AWSCredentialsProviderChain(AwsUtils.getCredentialsProviderList(properties));
        awsRegion = properties.getProperty(NotificationProperty.AWS_REGION);
        awsSesBounceQueue = properties.getProperty(NotificationProperty.AWS_SES_BOUNCE_QUEUE);
        ObjectUtils.requireNonEmpty(awsSesBounceQueue,
            String.format("Property %s is mandatory", NotificationProperty.AWS_SES_BOUNCE_QUEUE));
        String keyStoreClass = properties.getProperty(NotificationProperty.AWS_SES_BOUCE_HANDLER_STORE);
        ObjectUtils.requireNonEmpty(keyStoreClass,
            String.format("Property %s is mandatory", NotificationProperty.AWS_SES_BOUCE_HANDLER_STORE));
        KeyStore keyStore;
        try {
            keyStore = (KeyStore) Class.forName(keyStoreClass).getConstructor(Properties.class, StoreUser.class)
                .newInstance(properties, StoreUser.NOTIFICATION_EMAIL_BOUNCE_HANDLER);  //NOSONAR
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {

            throw new IllegalArgumentException(
                String.format("Failed to initialize %s with %s.class", NotificationProperty.AWS_SES_BOUCE_HANDLER_STORE,
                    keyStoreClass),
                e);
        }
        int timeToLiveInSeconds =
            Integer.parseInt(properties.getProperty(NotificationProperty.AWS_SES_BOUCE_HANDLER_STORE_TTL_SECS, "-1"));
        keyStore.setTtl(timeToLiveInSeconds, TimeUnit.SECONDS);
        bouncedEmailStore = new BouncedEmailStore(properties, keyStore);
        int processFrequency =
            Integer.parseInt(properties.getProperty(NotificationProperty.AWS_SES_BOUNCE_HANDLER_FREQUENCY, "60"));
        ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setName("AmazonSesBounceHandler");
                thread.setDaemon(true);
                return thread;
            });
        execService.scheduleWithFixedDelay(this::storeBouncedEmailIds, 0, processFrequency, TimeUnit.MINUTES);
    }

    /**
     * Method to store bounced email ids.
     */
    private void storeBouncedEmailIds() {

        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            .withCredentials(credProviderChain)
            .withRegion(awsRegion)
            .build();
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(awsSesBounceQueue);
        String sesQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sesQueueUrl);
        receiveMessageRequest.setMaxNumberOfMessages(TEN);
        List<Message> messages;
        int bouncedEmails = 0;
        do {
            messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            for (Message message : messages) {
                BounceMessage bounceMessage;
                try {
                    AmazonBounceNotification bounceNotification =
                        JsonUtils.bindData(message.getBody(), AmazonBounceNotification.class);
                    bounceMessage = JsonUtils.bindData(bounceNotification.getBounceMessage(), BounceMessage.class);
                } catch (IOException e) {
                    LOGGER.error("Failed to parse bounce notification.", e);
                    continue;
                }
                if (null != bounceMessage) {
                    Bounce bounce = bounceMessage.getBounce();
                    if (PERMANENT_BOUNCE.equals(bounce.getBounceType())) {
                        List<BouncedRecipient> bouncedRecipients = bounceMessage.getBounce().getBouncedRecipients();
                        bouncedRecipients.forEach(bouncedRecipient -> put(bouncedRecipient.getEmailAddress()));
                    }
                }
                String messageReceiptHandle = message.getReceiptHandle();
                sqs.deleteMessage(new DeleteMessageRequest(sesQueueUrl, messageReceiptHandle));
                bouncedEmails++;
            }
        } while (!CollectionUtils.isEmpty(messages));
        if (bouncedEmails > 0) {
            LOGGER.info("{} bounced emails deleted", bouncedEmails);
        }
    }

    /**
     * Method to put email address.
     *
     * @param emailAddress Email address
     */
    private void put(String emailAddress) {
        bouncedEmailStore.put(emailAddress);
    }

    /**
     * Method to check if email address is bounced.
     *
     * @param emailAddress Email address
     * @return boolean
     */
    public boolean bounced(String emailAddress) {
        return bouncedEmailStore.keyExists(emailAddress);
    }

    /**
     * AmazonSesBounceHandlerHolder Static class.
     */
    private static class AmazonSesBounceHandlerHolder {
        private static final AmazonSesBounceHandler HANDLER = new AmazonSesBounceHandler();

        private AmazonSesBounceHandlerHolder() {
        }
    }

    /**
     * Method to get instance.
     *
     * @param props Properties
     * @return AmazonSesBounceHandler
     */
    public static AmazonSesBounceHandler getInstance(Properties props) {
        properties = props;
        return AmazonSesBounceHandlerHolder.HANDLER;
    }
}
