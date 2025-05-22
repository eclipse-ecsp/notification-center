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

package org.eclipse.ecsp.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.notification.entities.RichContentDynamicNotificationTemplate;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.notification.config.NotificationConfig.USER_ID_FOR_DEFAULT_PREFERENCE;


/**
 * ClientChangeLog class to perform db data migration.
 * Also maintains changelog of the activity.
 */
@ChangeLog
public class ClientChangeLog {

    /**
     * NEW_DTC_PLACEHOLDER_SET.
     */
    public static final String NEW_DTC_PLACEHOLDER_SET = "[[dtc-content-transformer| [$.Data.set] ]]";
    /**
     * NEW_DTC_PLACEHOLDER_CLEARED.
     */
    public static final String NEW_DTC_PLACEHOLDER_CLEARED = "[[dtc-content-transformer| [$.Data.cleared] ]]";
    /**
     * OLD_DTC_PLACEHOLDER.
     */
    public static final String OLD_DTC_PLACEHOLDER = "[$.Data.DTC_LIST]";
    /**
     * DTC_SET_ID.
     */
    public static final String DTC_SET_ID = "DTCStored_set";
    /**
     * DTC_CLEARED_ID.
     */
    public static final String DTC_CLEARED_ID = "DTCStored_cleared";
    /**
     * NOTIFICATION_TEMPLATES.
     */
    public static final String NOTIFICATION_TEMPLATES = "notificationTemplates";
    /**
     * LOGGER.
     */
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ClientChangeLog.class);
    /**
     * NOTIFICATION_GROUPING.
     */
    private static final String NOTIFICATION_GROUPING = "notificationGrouping";
    /**
     * NOTIFICATION_GROUPING_BACKUP.
     */
    private static final String NOTIFICATION_GROUPING_BACKUP = "notificationGrouping219Backup";
    /**
     * MARKETING_NAMES.
     */
    private static final String MARKETING_NAMES = "marketingNames";
    /**
     * MARKETING_NAMES_BACKUP.
     */
    private static final String MARKETING_NAMES_BACKUP = "marketingNamesBackup";
    /**
     * NOTIFICATION_GROUPING_GROUP_TYPE_BACKUP.
     */
    private static final String NOTIFICATION_GROUPING_GROUP_TYPE_BACKUP = "notificationGrouping222Backup";
    /**
     * NOTIFICATION_BUFFER.
     */
    private static final String NOTIFICATION_BUFFER = "notificationBuffer";
    /**
     * NOTIFICATION_CONFIG.
     */
    protected static final String NOTIFICATION_CONFIG = "notificationConfigs";
    /**
     * CHANNEL_TEMPLATES.
     */
    public static final String CHANNEL_TEMPLATES = "channelTemplates";
    /**
     * RICH_CONTENT_NOTIFICATION_TEMPLATES.
     */
    public static final String RICH_CONTENT_NOTIFICATION_TEMPLATES = "richContentNotificationTemplates";
    /**
     * BODY.
     */
    public static final String BODY = "body";
    /**
     * ID_FIELD.
     */
    public static final String ID_FIELD = "_id";
    /**
     * NOTIFICATION_GROUPING_ASSOCIATION_BACKUP.
     */
    private static final String NOTIFICATION_GROUPING_ASSOCIATION_BACKUP = "notificationGrouping229Backup";
    /**
     * START_GROUP_MIGRATION.
     */
    private static final String START_GROUP_MIGRATION = "Start notificationGrouping migration";
    /**
     * EMPTY_GROUP.
     */
    private static final String EMPTY_GROUP = "{} is empty -> no need for migration";
    /**
     * FINISHED_DROP.
     */
    private static final String FINISHED_DROP = "finished notificationGrouping drop";
    /**
     * FINISHED_GROUP_MIGRATION.
     */
    private static final String FINISHED_GROUP_MIGRATION = "finished notificationGrouping migration";
    /**
     * GROUP_TYPE.
     */
    public static final String GROUP_TYPE = "groupType";


    @Value("${brand.default.value:default}")
    private String defaultBrand;

    /**
     * backup notificationgrouping collection.
     *
     * @param template template
     */
    @ChangeSet(id = "backupNotificationGrouping-2.19", order = "001", author = "Shai")
    public void backNotificationGrouping(MongoTemplate template) {
        backupCollection(template, NOTIFICATION_GROUPING_BACKUP, NOTIFICATION_GROUPING);
    }

    /**
     * notificationGrouping migration.
     *
     * @param template template
     */
    @ChangeSet(id = "updateNotificationGrouping-2.20", order = "002", author = "Shai")
    public void updateNotificationGrouping(MongoTemplate template) {
        LOGGER.info(START_GROUP_MIGRATION);
        List<Document> notificationGroupingOriginalDocuments =
            readFromCollection(template, NOTIFICATION_GROUPING_BACKUP);
        if (notificationGroupingOriginalDocuments.isEmpty()) {
            LOGGER.info(EMPTY_GROUP, NOTIFICATION_GROUPING_BACKUP);
        } else {
            template.getCollection(NOTIFICATION_GROUPING).drop();
            LOGGER.info(FINISHED_DROP);

            List<Document> migratedList = new ArrayList<>(notificationGroupingOriginalDocuments.size());
            for (Document document : notificationGroupingOriginalDocuments) {
                document.put("notificationId", document.getString("_id"));
                document.put("_id", document.getString("_id") + "_" + document.getString("group") + "_"
                    + (org.apache.commons.lang3.StringUtils.isEmpty(document.getString("service")) ? "" : document.getString("service")));
                migratedList.add(document);
            }

            template.createCollection(NOTIFICATION_GROUPING).insertMany(migratedList);
            LOGGER.info(FINISHED_GROUP_MIGRATION);
        }
    }

    /**
     * backup notificationgrouping collection.
     *
     * @param template template
     */
    @ChangeSet(id = "backupNotificationGrouping-2.22", order = "003", author = "Shai")
    public void backNotificationGroupingGroupType(MongoTemplate template) {
        backupCollection(template, NOTIFICATION_GROUPING_GROUP_TYPE_BACKUP, NOTIFICATION_GROUPING);
    }

    /**
     * notificationGrouping migration.
     *
     * @param template template
     */
    @ChangeSet(id = "updateNotificationGrouping-2.22", order = "004", author = "Maayan")
    public void updateNotificationGroupingWithGroupType(MongoTemplate template) {
        LOGGER.info(START_GROUP_MIGRATION);
        List<Document> notificationGroupingOriginalDocuments =
            readFromCollection(template, NOTIFICATION_GROUPING_GROUP_TYPE_BACKUP);
        if (notificationGroupingOriginalDocuments.isEmpty()) {
            LOGGER.info(EMPTY_GROUP, NOTIFICATION_GROUPING_GROUP_TYPE_BACKUP);
        } else {
            template.getCollection(NOTIFICATION_GROUPING).drop();
            LOGGER.info(FINISHED_DROP);

            List<Document> migratedList = new ArrayList<>(notificationGroupingOriginalDocuments.size());
            for (Document document : notificationGroupingOriginalDocuments) {
                document.put(GROUP_TYPE,
                    (org.apache.commons.lang3.StringUtils.isEmpty(document.getString(GROUP_TYPE)) ? "DEFAULT" :
                        document.getString(GROUP_TYPE)));
                migratedList.add(document);
            }

            template.createCollection(NOTIFICATION_GROUPING).insertMany(migratedList);
            LOGGER.info(FINISHED_GROUP_MIGRATION);
        }
    }

    /**
     * Notification Buffer migration.
     *
     * @param template template
     */
    @ChangeSet(id = "updateNotificationBuffer-2.24", order = "005", author = "Prashant")
    public void updateNotificationBufferWithContactId(MongoTemplate template) {
        LOGGER.info("Start Notification Buffer Updates");
        FindIterable<Document> notificationBufferRecords = template.getCollection(NOTIFICATION_BUFFER).find();
        for (Document document : notificationBufferRecords) {
            try {
                String contactId = document.getList("alertsInfo", Document.class).get(0)
                    .getEmbedded(Arrays.asList("cloneNotificationConfig", "contactId"), "self");
                document.put("contactId", contactId);
                template.save(document, NOTIFICATION_BUFFER);
            } catch (Exception e) {
                LOGGER.debug("Failed to update notificationBuffer id: {}", document.getOrDefault("_id", "null"));
            }
        }
    }

    /**
     * removeRedundantIndexNotificationBuffer.
     *
     * @param template template
     */
    @ChangeSet(id = "removeNotificationBufferIndex-2.24", order = "006", author = "Costa")
    public void removeRedundantIndexNotificationBuffer(MongoTemplate template) {
        LOGGER.info("Start Notification Buffer Update");
        MongoCollection<Document> collection = template.getCollection(NOTIFICATION_BUFFER);
        String redundantIndex = "userId_1_vehicleId_1_schedulerId_1_group_1";
        if (existsIndex(redundantIndex, collection)) {
            collection.dropIndex(redundantIndex);
        }
    }

    /**
     * NotificationCOnfigs migration.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "addBrandToDefaultConfigs-2.27", order = "007", author = "Shai")
    public void addBrandToDefaultConfigs(MongoTemplate template) {
        if (!StringUtils.hasText(defaultBrand)) {
            defaultBrand = "default";
        }
        LOGGER.info("Start NotificationConfigs Updates");

        Bson filter = BsonDocument.parse("{ \"userId\": \"" + USER_ID_FOR_DEFAULT_PREFERENCE + "\"}");
        Bson update = BsonDocument.parse("{ $set: { brand: \"" + defaultBrand + "\" }}");

        try {
            UpdateResult result = template.getCollection(NOTIFICATION_CONFIG).updateMany(filter, update);
            if (result.getMatchedCount() != result.getModifiedCount()) {
                LOGGER.warn("Updated {}/{} config records", result.getModifiedCount(), result.getMatchedCount());
            } else {
                LOGGER.info("Updated {}/{} config records", result.getModifiedCount(), result.getMatchedCount());
            }
        } catch (Exception e) {
            LOGGER.debug(String.format("Failed to update %s", NOTIFICATION_CONFIG), e);
            throw e;
        }
    }

    /**
     * DTCtemplate update.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "addDtcTemplate-2.27", order = "008", author = "Maayan")
    public void updateDtcTemplate(MongoTemplate template) {
        LOGGER.info("start DTC template update");
        updateNotificationTemplateBody(template, Filters.regex(ID_FIELD, ("^" + DTC_SET_ID)), OLD_DTC_PLACEHOLDER,
            NEW_DTC_PLACEHOLDER_SET);
        updateNotificationTemplateBody(template, Filters.regex(ID_FIELD, ("^" + DTC_CLEARED_ID)), OLD_DTC_PLACEHOLDER,
            NEW_DTC_PLACEHOLDER_CLEARED);
    }

    /**
     * migration of notification templates.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "migrateExportTemplateToFiles-2.28", order = "009", author = "Shai")
    public void migrateExportTemplateToFiles(MongoTemplate template) {
        LOGGER.info("start migrateExportTemplateToFiles");
        MongoDatabaseFactory mongoDbFactory = template.getMongoDatabaseFactory();

        MappingMongoConverter converter = new MappingMongoConverter(mongoDbFactory, new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);

        List<DynamicNotificationTemplate> notificationTemplatesList =
            template.findAll(DynamicNotificationTemplate.class,
                NOTIFICATION_TEMPLATES);
        Set<String> notificationIds =
            notificationTemplatesList.stream().map(DynamicNotificationTemplate::getNotificationId)
                .collect(Collectors.toSet());
        SpringTemplateData notificationTemplateImportedData;
        for (String notificationId : notificationIds) {
            try {
                byte[] file = MigrationUtils.getTemplateFileStream(notificationTemplatesList.stream()
                    .filter(dynamicNotificationTemplate -> dynamicNotificationTemplate.getNotificationId()
                        .equals(notificationId))
                    .toList());
                notificationTemplateImportedData = new SpringTemplateData(notificationId, file, new Date());

                mongoTemplate.save(notificationTemplateImportedData);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Richtemplate migration.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "updateLocalesInRichTemplates-2.28", order = "010", author = "Shai")
    public void updateLocalesInRichTemplates(MongoTemplate template) {
        LOGGER.info("start updateLocalesInRichTemplates");
        List<RichContentDynamicNotificationTemplate> richContentDynamicNotificationTemplateList = template
            .findAll(RichContentDynamicNotificationTemplate.class, RICH_CONTENT_NOTIFICATION_TEMPLATES);
        for (RichContentDynamicNotificationTemplate richContentDynamicNotificationTemplate
            : richContentDynamicNotificationTemplateList) {
            richContentDynamicNotificationTemplate
                .setLocale(Locale.forLanguageTag(richContentDynamicNotificationTemplate.getLocale().replace("_", "-"))
                    .toString());
            richContentDynamicNotificationTemplate.setBrand(
                richContentDynamicNotificationTemplate.getBrand().toLowerCase(Locale.ROOT));
            template.save(richContentDynamicNotificationTemplate, RICH_CONTENT_NOTIFICATION_TEMPLATES);
        }
    }

    /**
     * backup marketing names.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "backupMarketingName-2.27", order = "011", author = "Efrat")
    public void backMarketingNames(MongoTemplate template) {
        backupCollection(template, MARKETING_NAMES_BACKUP, MARKETING_NAMES);
        LOGGER.info("finished backup marketing names");
    }

    /**
     * marketing names migration.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "updateMarketingName-2.28", order = "012", author = "Efrat")
    public void updateMarketingNames(MongoTemplate template) {
        LOGGER.info("Start marketing name migration");
        FindIterable<Document> marketingNamesOriginalList = template.getCollection(MARKETING_NAMES_BACKUP).find();
        List<Document> marketingNamesOriginalDocuments = new ArrayList<>();
        for (Document document : marketingNamesOriginalList) {
            marketingNamesOriginalDocuments.add(document);
        }
        if (marketingNamesOriginalDocuments.isEmpty()) {
            LOGGER.info("marketingNamesBackup is empty -> no need for migration");
        } else {
            template.getCollection(MARKETING_NAMES).drop();
            LOGGER.info("finished marketingNames drop");
            List<Document> migratedList = new ArrayList<>(marketingNamesOriginalDocuments.size());
            for (Document document : marketingNamesOriginalDocuments) {
                document.put("brandName", document.getString("_id").toLowerCase());
                document.put("_id", UUID.randomUUID().toString());
                migratedList.add(document);
            }
            template.createCollection(MARKETING_NAMES).insertMany(migratedList);
            LOGGER.info("finished marketingNames migration");
        }
    }

    /**
     * backup notification grouping.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "backupNotificationGrouping-2.28", order = "013", author = "Arpitha")
    public void backUpNotificationGrouping(MongoTemplate template) {
        backupCollection(template, NOTIFICATION_GROUPING_ASSOCIATION_BACKUP, NOTIFICATION_GROUPING);
    }

    /**
     * notificationGrouping migration.
     *
     * @param template MongoTemplate
     */
    @ChangeSet(id = "updateNotificationGrouping-2.29", order = "014", author = "Arpitha")
    public void updateNotificationGroupingWithAssociation(MongoTemplate template) {
        LOGGER.info(START_GROUP_MIGRATION);
        List<Document> notificationGroupingOriginalDocuments =
            readFromCollection(template, NOTIFICATION_GROUPING_ASSOCIATION_BACKUP);
        if (notificationGroupingOriginalDocuments.isEmpty()) {
            LOGGER.info(EMPTY_GROUP, NOTIFICATION_GROUPING_ASSOCIATION_BACKUP);
        } else {
            template.getCollection(NOTIFICATION_GROUPING).drop();
            LOGGER.info(FINISHED_DROP);

            List<Document> migratedList = new ArrayList<>(notificationGroupingOriginalDocuments.size());
            for (Document document : notificationGroupingOriginalDocuments) {
                document.put("checkAssociation", true);
                migratedList.add(document);
            }

            template.createCollection(NOTIFICATION_GROUPING).insertMany(migratedList);
            LOGGER.info(FINISHED_GROUP_MIGRATION);
        }
    }

    /**
     * update notification template body.
     *
     * @param template MongoTemplate
     * @param dtcFilter Bson
     * @param oldBody String
     * @param newBody String
     */
    private void updateNotificationTemplateBody(MongoTemplate template, Bson dtcFilter, String oldBody,
                                                String newBody) {
        FindIterable<Document> dtcTemplateList = template.getCollection(NOTIFICATION_TEMPLATES).find(dtcFilter);
        for (Document dtcTemplate : dtcTemplateList) {
            Document channelTemplates = dtcTemplate.get(CHANNEL_TEMPLATES, Document.class);
            replaceBodyForEachChannel(oldBody, newBody, channelTemplates);
            LOGGER.info("updated dtc template: {}", dtcTemplate);
            template.save(dtcTemplate, NOTIFICATION_TEMPLATES);
        }
    }

    /**
     * replace body for each channel.
     *
     * @param oldBody String
     * @param newBody String
     * @param channelTemplates Document
     */
    private void replaceBodyForEachChannel(String oldBody, String newBody, Document channelTemplates) {
        Set<String> channels = channelTemplates.keySet().stream().filter(c -> !c.isEmpty()).collect(Collectors.toSet());
        for (String channel : channels) {
            Document document = channelTemplates.get(channel, Document.class);
            document.computeIfPresent(BODY, (b, v) -> v.toString().replace(oldBody, newBody));

        }
    }

    /**
     * backup collection.
     *
     * @param template MongoTemplate
     * @param collectionNameBackup String
     * @param collectionName String
     */
    private void backupCollection(MongoTemplate template, String collectionNameBackup, String collectionName) {
        LOGGER.info("Start collection {} backup", collectionName);
        FindIterable<Document> collectionOriginalList = template.getCollection(collectionName).find();
        List<Document> collectionOriginalDocuments = new ArrayList<>();
        for (Document document : collectionOriginalList) {
            collectionOriginalDocuments.add(document);
        }
        if (collectionOriginalDocuments.isEmpty()) {
            LOGGER.info(" {} is empty -> no need for migration", collectionName);
        } else {
            template.createCollection(collectionNameBackup).insertMany(collectionOriginalDocuments);
            LOGGER.info("finished {} backup", collectionName);
        }
    }

    /**
     * check if index exists.
     *
     * @param name String
     * @param collection MongoCollection
     * @return boolean
     */
    private boolean existsIndex(String name, MongoCollection<Document> collection) {
        for (Document doc : collection.listIndexes()) {
            if (doc.getString("name").equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * read data from collection.
     *
     * @param template MongoTemplate
     *
     * @param collectionName collection
     *
     * @return Document list
     */
    public List<Document> readFromCollection(MongoTemplate template, String collectionName) {
        FindIterable<Document> documents = template.getCollection(collectionName).find();
        List<Document> documentList = new ArrayList<>();
        for (Document document : documents) {
            documentList.add(document);
        }
        return documentList;
    }
}
