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

package org.eclipse.ecsp.notification;

import com.codahale.metrics.MetricRegistry;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.adaptor.ChannelNotifier;
import org.eclipse.ecsp.notification.config.CachedChannelServiceProviderConfigDAO;
import org.eclipse.ecsp.notification.db.client.MongoDbClient;
import org.eclipse.ecsp.notification.db.client.NotificationDao;
import org.eclipse.ecsp.notification.ivm.IvmNotifierImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * ChannelNotifierRegistryTest.
 */
public class ChannelNotifierRegistryTest {

    @Mock
    MetricRegistry metricRegistry;
    NotificationDao notificationDao = new MongoDbClient();
    @Mock
    private CachedChannelServiceProviderConfigDAO cachedChannelServiceProviderConfigDao;

    @Before
    public void setUp() throws IOException {
    }

    @Test(expected = IllegalArgumentException.class)
    public void initTestException() {
        Properties props = new Properties();
        props.setProperty("available.channel.notifiers",
            "ivm:org.eclipse.ecsp.notification.ivm.IvmNotifierImpl,"
                + "apiPush:org.eclipse.ecsp.notification.push.ApiPushNotifierImpl,"
                + "portal:org.eclipse.ecsp.notification.browser.MqttBrowserNotifier,"
                + "push:org.eclipse.ecsp.notification.fcm.FcmNotifier,"
                + "sms:org.eclipse.ecsp.notification.aws.sns.AmazonSmsNotifier,"
                + "email:org.eclipse.ecsp.notification.aws.ses.AmazonSesNotifier");
        Set<ChannelType> channelsSupported = new HashSet<>();
        channelsSupported.add(ChannelType.API_PUSH);
        channelsSupported.add(ChannelType.EMAIL);
        ChannelNotifierRegistry notifier = new ChannelNotifierRegistry();
        notifier.init(cachedChannelServiceProviderConfigDao, props, metricRegistry, null, null, channelsSupported,
            null);
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void initTest() {
        Properties props = new Properties();
        props.setProperty("available.channel.notifiers",
            "ivm:org.eclipse.ecsp.notification.ivm.IvmNotifierImpl,"
                + "portal:org.eclipse.ecsp.notification.browser.MqttBrowserNotifier,"
                + "push:org.eclipse.ecsp.notification.fcm.FcmNotifier,"
                + "sms:org.eclipse.ecsp.notification.aws.sns.AmazonSmsNotifier,"
                + "email:org.eclipse.ecsp.notification.aws.ses.AmazonSesNotifier");
        Set<ChannelType> channelsSupported = new HashSet<>();
        channelsSupported.add(ChannelType.EMAIL);
        channelsSupported.add(ChannelType.SMS);
        channelsSupported.add(ChannelType.IVM);
        channelsSupported.add(ChannelType.MOBILE_APP_PUSH);
        channelsSupported.add(ChannelType.PORTAL);
        ChannelNotifierRegistry notifier = new ChannelNotifierRegistry();

        ApplicationContext ctx = new ApplicationContext() {

            @Override
            public Resource getResource(String location) {

                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public Resource[] getResources(String locationPattern) throws IOException {

                return null;
            }

            @Override
            public void publishEvent(Object event) {


            }

            @Override
            public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {

                return null;
            }

            @Override
            public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {

                return null;
            }

            @Override
            public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {

                return null;
            }

            @Override
            public BeanFactory getParentBeanFactory() {

                return null;
            }

            @Override
            public boolean containsLocalBean(String name) {

                return false;
            }

            @Override
            public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {

                return false;
            }

            @Override
            public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {

                return false;
            }

            @Override
            public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {

                return false;
            }

            @Override
            public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {

                return false;
            }

            @Override
            public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {

                return null;
            }

            @Override
            public Class<?> getType(String name) throws NoSuchBeanDefinitionException {

                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {

                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {

                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {

                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {

                return null;
            }

            @Override
            public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {

                return null;
            }

            @Override
            public Object getBean(String name, Object... args) throws BeansException {

                return null;
            }

            @Override
            public <T> T getBean(String name, Class<T> requiredType) throws BeansException {

                return null;
            }

            @Override
            public <T> T getBean(Class<T> requiredType) throws BeansException {

                return (T) new IvmNotifierImpl();
            }

            @Override
            public Object getBean(String name) throws BeansException {

                return null;
            }

            @Override
            public String[] getAliases(String name) {

                return null;
            }

            @Override
            public boolean containsBean(String name) {

                return false;
            }

            @Override
            public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
                throws BeansException {

                return null;
            }

            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons,
                                                     boolean allowEagerInit)
                throws BeansException {

                return null;
            }

            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {

                return null;
            }



            @Override
            public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

                return null;
            }

            @Override
            public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons,
                                                boolean allowEagerInit) {

                return null;
            }

            @Override
            public String[] getBeanNamesForType(Class<?> type) {

                return null;
            }

            @Override
            public String[] getBeanNamesForType(ResolvableType type) {

                return null;
            }

            @Override
            public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {

                return null;
            }

            @Override
            public String[] getBeanDefinitionNames() {

                return null;
            }

            @Override
            public int getBeanDefinitionCount() {

                return 0;
            }

            @Override
            public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
                    throws NoSuchBeanDefinitionException {

                return null;
            }

            @Override
            public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType,
                                                                 boolean allowFactoryBeanInit)
                throws NoSuchBeanDefinitionException {

                return null;
            }

            @Override
            public <A extends Annotation> Set<A> findAllAnnotationsOnBean(
                    String beanName, Class<A> annotationType,
                    boolean allowFactoryBeanInit)
                    throws NoSuchBeanDefinitionException {
                return Set.of();
            }

            @Override
            public boolean containsBeanDefinition(String beanName) {

                return false;
            }

            @Override
            public Environment getEnvironment() {

                return null;
            }

            @Override
            public long getStartupDate() {

                return 0;
            }

            @Override
            public ApplicationContext getParent() {

                return null;
            }

            @Override
            public String getId() {

                return null;
            }

            @Override
            public String getDisplayName() {

                return null;
            }

            @Override
            public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {

                return null;
            }

            @Override
            public String getApplicationName() {

                return null;
            }
        };
        Assertions.assertNotNull(ctx);
        notifier.init(cachedChannelServiceProviderConfigDao, props, metricRegistry, notificationDao, null,
            channelsSupported, ctx);
        Assertions.assertNotNull(ctx);

        notifier.channelNotifier(ChannelType.EMAIL);
        notifier.channelNotifier(ChannelType.SMS);
        notifier.channelNotifier(ChannelType.IVM);
        notifier.channelNotifier(ChannelType.MOBILE_APP_PUSH);
        notifier.channelNotifier(ChannelType.PORTAL);
    }

    @Test
    public void getAllchannelNotifiers() {
        ChannelNotifierRegistry notifier = new ChannelNotifierRegistry();
        Map<String, ChannelNotifier> mp = notifier.getAllchannelNotifiers(ChannelType.API_PUSH);
        Assertions.assertNull(mp);
    }

    @Test(expected = Exception.class)
    public void getChannelNotifiersById() {
        ChannelNotifierRegistry notifier = new ChannelNotifierRegistry();
        notifier.channelNotifier(ChannelType.API_PUSH, "notificationId", "EMEA");
    }

}
