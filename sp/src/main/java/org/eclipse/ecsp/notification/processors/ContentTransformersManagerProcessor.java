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


import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.val;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.BaseTemplate;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.entities.NotificationTemplate;
import org.eclipse.ecsp.notification.processors.transformers.AlertsInfoToDtoConverter;
import org.eclipse.ecsp.notification.processors.transformers.PlaceholderDescriptor;
import org.eclipse.ecsp.notification.processors.transformers.PlaceholderResolver;
import org.eclipse.ecsp.processor.content.dto.ContentProcessingContextDto;
import org.eclipse.ecsp.processor.content.plugin.ContentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Stream.empty;

/**
 * ContentTransformersManagerProcessor class.
 */
@Component
@DependsOn("pluginManager")
@Order
public class ContentTransformersManagerProcessor implements NotificationProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTransformersManagerProcessor.class);
    private final ImmutableMap<String, ContentTransformer> transformersMap;
    private final Set<String> disabledTransformers;
    private final ContentTransformer defaultTransformer;
    @Value("${content.transformer.timeout:300}")
    private int transformerTimeoutMs;
    @Value("${content.transformer.thread.pool.count:5}")
    private int transformerPoolMax;
    @Autowired
    private PlaceholderResolver placeholderResolver;
    @Autowired
    private AlertsInfoToDtoConverter alertsInfoToDtoConverter;
    private ListeningExecutorService transformersExecutor;
    private ScheduledExecutorService timeoutScheduledExecutor;

    /**
     * ContentTransformersManagerProcessor constructor.
     *
     * @param transformers list of transformers
     * @param disabled String
     */
    public ContentTransformersManagerProcessor(@Autowired(required = false) List<ContentTransformer> transformers,
                                               @Value("${content.transformer.disabled.list:}") String disabled) {
        disabledTransformers = disabled == null ? new HashSet<>()
                : Splitter.on(",").omitEmptyStrings().trimResults().splitToList(disabled).stream()
                .collect(Collectors.toSet());

        Stream<ContentTransformer> transformersStream = transformers == null ? empty()
                : transformers.stream().filter(ct -> !disabledTransformers.contains(ct.getId()));
        transformersMap = transformersStream
                .collect(toImmutableMap(tr -> tr.getId(), tr -> tr,
                        (alreadyIn, candidate) -> {
                            LOGGER.warn("Duplicate transformer Id={} found. Using the last one!", alreadyIn.getId());
                            return candidate;
                        }));
        LOGGER.info("Number of transformers: {}", transformersMap.size());
        transformersMap.forEach((k, v) -> LOGGER.info("Loaded content transformer from plugin: {}", k));
        LOGGER.info("Disabled content transformers list: {}", disabledTransformers);
        transformersExecutor = MoreExecutors.listeningDecorator(
                Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("content-transformer-worker-%d")
                        .build()));

        timeoutScheduledExecutor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("timeout-scheduler-pool-%d")
                .build());

        defaultTransformer = new ContentTransformer() {
            @Override
            public String getId() {
                return "default";
            }

            @Override
            public String apply(ContentProcessingContextDto contextDto, String s) {
                return s;
            }
        };
    }

    /**
     * Process method.
     *
     * @param info AlertsInfo
     */
    @Override
    public void process(AlertsInfo info) {
        Set<ChannelType> availableChannelTypes = info.resolveAvailableChannelTypes();
        ContentProcessingContextDto processingContextDto = alertsInfoToDtoConverter.apply(info);
        for (NotificationTemplate template : info.getLocaleToNotificationTemplate().values()) {
            processingContextDto.setLocale(template.getLocale().toString());
            LOGGER.debug("Processing template content transformers for {} of AlertsInfo {}", template, info);
            for (ChannelType ct : availableChannelTypes) {
                BaseTemplate channelTemplate = template.getChannelTemplate(ct);
                if (channelTemplate == null) {
                    continue;
                }
                processContentFields(processingContextDto, channelTemplate);
            }
        }
    }

    /**
     * Process content fields.
     *
     * @param contextDto ContentProcessingContextDto
     * @param channelTemplate BaseTemplate
     */
    private void processContentFields(ContentProcessingContextDto contextDto, BaseTemplate channelTemplate) {
        //iterating over each content property and transforming it`s placeholders
        channelTemplate.getContentFieldsGetter().forEach((field, getter) -> {
            String content = getter.get();
            if (content == null) {
                return; //skip current iteration (continue in lambda)
            }
            val placeholderDescriptors = placeholderResolver.resolvePlaceholders(content);
            LOGGER.debug("placeholders resolved in {} at ContentField {}:{}", content, field, placeholderDescriptors);

            val transformationFutures = placeholderDescriptors.stream()
                    .map(descriptor -> transformContentAsync(contextDto, descriptor))
                    .toList();

            //foreach transformations futures, finalizing the transformation results (blocking)
            transformationFutures.forEach(this::getResult);

            String resultingContent = placeholderResolver.replacePlaceholders(placeholderDescriptors, content);
            channelTemplate.getContentFieldsSetter().get(field)
                    .accept(resultingContent);
            LOGGER.debug("Final {} after placeholder replacements is:\"{}\"", field, resultingContent);
        });
    }

    /**
     * Transform content async.
     *
     * @param contextDto ContentProcessingContextDto
     * @param descriptor PlaceholderDescriptor
     * @return PlaceholderDescriptor
     */
    private FluentFuture<PlaceholderDescriptor> transformContentAsync(ContentProcessingContextDto contextDto,
                                                                      PlaceholderDescriptor descriptor) {
        val transformer = transformersMap.getOrDefault(descriptor.getContentTransformerId(), defaultTransformer);
        ListenableFuture<Transformation> transformFuture = transformersExecutor.submit(() ->
                transformContent(contextDto, descriptor, transformer));
        FluentFuture<Transformation> transformationFuture = wrapFutureTimeout(descriptor, transformer, transformFuture);
        FluentFuture<PlaceholderDescriptor> fallbackWrapper = addFallbackHandling(contextDto, transformationFuture);
        return fallbackWrapper;
    }

    /**
     * Transform content.
     *
     * @param contextDto ContentProcessingContextDto
     * @param descriptor PlaceholderDescriptor
     * @param transformer ContentTransformer
     * @return Transformation
     */
    private Transformation transformContent(ContentProcessingContextDto contextDto, PlaceholderDescriptor descriptor,
                                            ContentTransformer transformer) {
        LOGGER.debug("Starting transformation for ContentDescriptor: {} with ContentTransformer {}", descriptor,
                transformer.getId());
        String result = transformer.apply(contextDto, descriptor.getInput());
        descriptor.setResult(result);
        LOGGER.debug("Resolved result for ContentDescriptor: {} with ContentTransformer {}", descriptor,
                transformer.getId());
        return Transformation.success(transformer, descriptor);
    }

    /**
     * Wrap future timeout.
     *
     * @param descriptor PlaceholderDescriptor
     * @param transformer ContentTransformer
     * @param transformFuture ListenableFuture
     * @return FluentFuture
     */
    private FluentFuture<Transformation> wrapFutureTimeout(PlaceholderDescriptor descriptor,
                                                           ContentTransformer transformer,
                                                           ListenableFuture<Transformation> transformFuture) {
        return
                FluentFuture.from(transformFuture)
                        .withTimeout(transformerTimeoutMs, MILLISECONDS, timeoutScheduledExecutor)
                        .catching(TimeoutException.class, e -> {
                            LOGGER.error("Timeout while waiting for transformation to finish: {}", descriptor);
                            return Transformation.failedTransformation(transformer, descriptor);
                        }, transformersExecutor)
                        .catching(Exception.class, e -> {
                            LOGGER.error("Exception while performing transformation : {}", descriptor, e);
                            return Transformation.failedTransformation(transformer, descriptor);
                        }, transformersExecutor);
    }

    /**
     * Add fallback handling.
     *
     * @param contextDto ContentProcessingContextDto
     * @param transformationFuture FluentFuture
     * @return FluentFuture
     */
    private FluentFuture<PlaceholderDescriptor> addFallbackHandling(ContentProcessingContextDto contextDto,
                                                                    FluentFuture<Transformation> transformationFuture) {
        return transformationFuture.transform(res -> {
            if (res.failed) {
                String fallbackResult = res.contentTransformer.fallback(contextDto, res.descriptor.getInput());
                return res.descriptor.setResult(fallbackResult);
            } else {
                return res.descriptor;
            }
        }, this.transformersExecutor);
    }


    /**
     * just a utility method to avoid try-catch block inside lambda, making it ugly as hell.
     */
    private PlaceholderDescriptor getResult(FluentFuture<PlaceholderDescriptor> future) {

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e.getMessage());
        }

    }


    /**
     * An inner class holding the context of the transformation.
     * The use of this class allows timeout and exception handling.
     */
    private static class Transformation {
        public final ContentTransformer contentTransformer;
        public final PlaceholderDescriptor descriptor;
        public final boolean failed;

        private Transformation(ContentTransformer contentTransformer, PlaceholderDescriptor descriptor,
                               boolean failed) {
            this.contentTransformer = contentTransformer;
            this.descriptor = descriptor;
            this.failed = failed;
        }

        public static Transformation failedTransformation(
                ContentTransformer contentTransformer, PlaceholderDescriptor descriptor) {
            return new Transformation(contentTransformer, descriptor, true);
        }

        public static Transformation success(ContentTransformer contentTransformer, PlaceholderDescriptor descriptor) {
            return new Transformation(contentTransformer, descriptor, false);
        }
    }
}
