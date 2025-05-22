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

import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutEntityRequest;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.domain.notification.RetryRecord;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.notification.entities.RetryRecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RetryCacheClient class.

 * @author AMuraleedhar
 */
@Component
public class RetryCacheClient {

    @Autowired
    private IgniteCache igniteCache;

    @Value("${notification.retry.redis.ttl}")
    private long redisEntitlyTtl;

    /**
     * getRetryRecordFromCache method to fetch record from cache.
     *
     * @param requestId String
     * @return List of Retry Records
     */
    public List<RetryRecord> getRetryRecordFromCache(String requestId) {
        String key = getCacheKey(requestId);
        List<RetryRecord> recordLst = null;
        RetryRecordEntity rcEntity = igniteCache.getEntity(key);
        if (rcEntity != null) {
            recordLst = rcEntity.getRetryRecordList();
        }
        return recordLst;
    }

    /**
     * getRetryRecordForException method to fetch retry record.
     *
     * @param requestId String
     * @param name String
     * @return RetryRecord
     */
    public RetryRecord getRetryRecordForException(String requestId, String name) {
        String key = getCacheKey(requestId);
        RetryRecord rec = null;
        RetryRecordEntity rcEntity = igniteCache.getEntity(key);

        if (rcEntity != null && rcEntity.getRetryRecordList() != null && !rcEntity.getRetryRecordList().isEmpty()) {
            List<RetryRecord> recordLst = rcEntity.getRetryRecordList();
            rec =
                    recordLst.stream().filter(rc ->
                            rc.getRetryException().equalsIgnoreCase(name)).findAny().orElse(null);
        }
        return rec;
    }

    /**
     * putRetryRecord method to add retry rec to cache.
     *
     * @param requestId String
     * @param rec RetryRecord
     */
    public void putRetryRecord(String requestId, RetryRecord rec) {
        String key = getCacheKey(requestId);
        RetryRecordEntity retryEntity = igniteCache.getEntity(key);
        if (retryEntity == null) {
            RetryRecordEntity entity = new RetryRecordEntity();
            List<RetryRecord> recordLst = new ArrayList<>();
            recordLst.add(rec);
            entity.setRetryRecordList(recordLst);
            entity.setLastUpdatedTime(LocalDateTime.now());
            entity.setSchemaVersion(Version.V1_0);
            PutEntityRequest<RetryRecordEntity> putEntityRequest = new PutEntityRequest<>();
            putEntityRequest.withKey(key);
            putEntityRequest.withValue(entity);
            igniteCache.putEntity(putEntityRequest);
        } else {
            List<RetryRecord> records = retryEntity.getRetryRecordList();
            records.removeIf(rc -> rc.getRetryException().equalsIgnoreCase(rec.getRetryException()));
            records.add(rec);
            PutEntityRequest<RetryRecordEntity> entityRequest = new PutEntityRequest<>();
            entityRequest.withKey(key);
            entityRequest.withValue(retryEntity);
            entityRequest.withTtlMs(redisEntitlyTtl);
            igniteCache.putEntity(entityRequest);
        }
    }

    /**
     * deleteRetryRecord method to delete the retry record.
     *
     * @param requestId String
     * @param name String
     */
    public void deleteRetryRecord(String requestId, String name) {
        String key = getCacheKey(requestId);
        RetryRecordEntity retryEntity = igniteCache.getEntity(key);
        if (retryEntity.getRetryRecordList().size() == 1) {
            igniteCache.delete(key);
        } else {
            List<RetryRecord> records = retryEntity.getRetryRecordList();
            records.removeIf(rc -> rc.getRetryException().equalsIgnoreCase(name));
            PutEntityRequest<RetryRecordEntity> entityRequest = new PutEntityRequest<>();
            entityRequest.withKey(key);
            entityRequest.withValue(retryEntity);
            igniteCache.putEntity(entityRequest);
        }

    }

    /**
     * getCacheKey method to get the cache key.
     *
     * @param requestId String
     * @return String
     */
    private String getCacheKey(String requestId) {
        return NotificationConstants.NOTIFICATION_RETRY.concat(requestId);
    }
}
