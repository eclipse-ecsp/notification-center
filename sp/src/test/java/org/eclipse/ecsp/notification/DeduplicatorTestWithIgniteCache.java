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

import org.eclipse.ecsp.cache.AddScoredEntityRequest;
import org.eclipse.ecsp.cache.AddScoredStringRequest;
import org.eclipse.ecsp.cache.DeleteEntryRequest;
import org.eclipse.ecsp.cache.DeleteMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.GetEntityRequest;
import org.eclipse.ecsp.cache.GetMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.GetScoredEntitiesRequest;
import org.eclipse.ecsp.cache.GetScoredStringsRequest;
import org.eclipse.ecsp.cache.GetStringRequest;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutEntityRequest;
import org.eclipse.ecsp.cache.PutMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.EventMetadata;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.notification.duplication.Deduplicator;
import org.eclipse.ecsp.notification.duplication.KeyExtractor;
import org.eclipse.ecsp.notification.duplication.KeyExtractorFactory;
import org.eclipse.ecsp.notification.utils.ApplicationContextUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * Deduplicator test with ignite cache.
 */
public class DeduplicatorTestWithIgniteCache {

    private static final String KEY_PATTERN = KeyExtractor.DEDUP_KEY_PREFIX + "*";
    private Properties redisProperties;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        redisProperties = NotificationTestUtils.loadProperties("/deduplicator_ignite_cache.properties");
    }

    @After
    public void clearRedis() {

        KeyExtractorFactory.reset();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void testDeduplicatorWithIntervalZero() throws IOException {
        // Default interval is 0
        Deduplicator deduplicator;
        try (MockedStatic<ApplicationContextUtil> applicationContextUtilMock = Mockito.mockStatic(
            ApplicationContextUtil.class)) {
            applicationContextUtilMock.when(ApplicationContextUtil::getApplicationContext)
                .thenReturn(getAppCtx());
            deduplicator = new Deduplicator(redisProperties);
        }
        List<AlertsInfo> alerts = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            AlertsInfo geofenceAlert = new AlertsInfo();
            Data d1 = new Data();
            d1.set("id", "geofenceId");
            geofenceAlert.setAlertsData(d1);
            geofenceAlert.setEventID(EventMetadata.EventID.GEOFENCE.toString());
            geofenceAlert.setPdid("H0123");
            geofenceAlert.setTimestamp(i);
            alerts.add(geofenceAlert);
        }
        List<AlertsInfo> filteredAlerts = deduplicator.filterDuplicateAlert(alerts);
        assertEquals(10, filteredAlerts.size());
    }

    @SuppressWarnings("checkstyle:MethodLength")
    @NotNull
    private ApplicationContext getAppCtx() {
        return new ApplicationContext() {
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
                return new Resource[0];
            }

            @Override
            public Environment getEnvironment() {
                return null;
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
            public String getMessage(MessageSourceResolvable resolvable, Locale locale)
                throws NoSuchMessageException {
                return null;
            }

            @Override
            public void publishEvent(Object event) {

            }

            @Override
            public Object getBean(String name) throws BeansException {
                return null;
            }

            @Override
            public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
                return null;
            }

            @Override
            public Object getBean(String name, Object... args) throws BeansException {
                return null;
            }

            @Override
            public <T> T getBean(Class<T> requiredType) throws BeansException {
                return (T) new IgniteCache() {

                    @Override
                    public Future<String> putStringAsync(PutStringRequest request) {

                        return null;
                    }

                    @Override
                    public void putString(PutStringRequest request) {


                    }

                    @Override
                    public <T extends IgniteEntity> void putMapOfEntities(PutMapOfEntitiesRequest<T> request) {


                    }

                    @Override
                    public <T extends IgniteEntity> Future<String> putEntityAsync(
                        PutEntityRequest<T> putRequest) {

                        return null;
                    }

                    @Override
                    public <T extends IgniteEntity> void putEntity(PutEntityRequest<T> putRequest) {


                    }

                    @Override
                    public List<String> getStringsFromScoredSortedSet(GetScoredStringsRequest request) {

                        return null;
                    }

                    @Override
                    public String getString(GetStringRequest request) {

                        return null;
                    }

                    @Override
                    public String getString(String key) {

                        return null;
                    }

                    @Override
                    public <T extends IgniteEntity> Map<String, T> getMapOfEntities(
                        GetMapOfEntitiesRequest request) {

                        return null;
                    }

                    @Override
                    public <T extends IgniteEntity> Map<String, T> getKeyValuePairsForRegex(String keyRegex,
                                                                                            Optional<Boolean> arg1) {

                        return null;
                    }

                    @Override
                    public <T extends IgniteEntity> T getEntity(String key) {

                        return null;
                    }

                    @Override
                    public <T extends IgniteEntity> T getEntity(GetEntityRequest getRequest) {

                        return null;
                    }

                    @Override
                    public <T extends IgniteEntity> List<T> getEntitiesFromScoredSortedSet(
                        GetScoredEntitiesRequest request) {

                        return null;
                    }

                    @Override
                    public void deleteMapOfEntities(DeleteMapOfEntitiesRequest request) {


                    }

                    @Override
                    public Future<String> deleteAsync(DeleteEntryRequest deleteRequest) {

                        return null;
                    }

                    @Override
                    public void delete(DeleteEntryRequest deleteRequest) {


                    }

                    @Override
                    public void delete(String key) {


                    }

                    @Override
                    public Future<String> addStringToScoredSortedSetAsync(AddScoredStringRequest request) {

                        return null;
                    }

                    @Override
                    public void addStringToScoredSortedSet(AddScoredStringRequest request) {


                    }

                    @Override
                    public <T extends IgniteEntity> Future<String> addEntityToScoredSortedSetAsync(
                        AddScoredEntityRequest<T> request) {

                        return null;
                    }

                    @Override
                    public <T extends IgniteEntity> void addEntityToScoredSortedSet(
                        AddScoredEntityRequest<T> request) {


                    }
                };

            }

            @Override
            public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {

                return null;
            }

            @Override
            public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {

                return null;
            }

            @Override
            public boolean containsBean(String name) {
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
            public boolean isTypeMatch(String name, ResolvableType typeToMatch)
                throws NoSuchBeanDefinitionException {
                return false;
            }

            @Override
            public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
                return false;
            }

            @Override
            public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
                return null;
            }

            @Override
            public Class<?> getType(String name, boolean allowFactoryBeanInit)
                throws NoSuchBeanDefinitionException {
                return null;
            }

            @Override
            public String[] getAliases(String name) {
                return new String[0];
            }

            @Override
            public boolean containsBeanDefinition(String beanName) {
                return false;
            }

            @Override
            public int getBeanDefinitionCount() {
                return 0;
            }

            @Override
            public String[] getBeanDefinitionNames() {
                return new String[0];
            }

            @Override
            public String[] getBeanNamesForType(ResolvableType type) {
                return new String[0];
            }

            @Override
            public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons,
                                                boolean allowEagerInit) {
                return new String[0];
            }

            @Override
            public String[] getBeanNamesForType(Class<?> type) {
                return new String[0];
            }

            @Override
            public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons,
                                                boolean allowEagerInit) {
                return new String[0];
            }

            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
                return null;
            }

            @Override
            public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons,
                                                     boolean allowEagerInit) throws BeansException {
                return null;
            }

            @Override
            public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
                return new String[0];
            }

            @Override
            public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
                throws BeansException {
                return null;
            }

            @Override
            public <A extends Annotation> A findAnnotationOnBean(
                    String beanName, Class<A> annotationType)
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
            public BeanFactory getParentBeanFactory() {
                return null;
            }

            @Override
            public boolean containsLocalBean(String name) {
                return false;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public String getApplicationName() {
                return null;
            }

            @Override
            public String getDisplayName() {
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
            public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
                return null;
            }

        };
    }
}
