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

/**
 * Class to enumerate the properties.
 */
public class NotificationProperty {

    private NotificationProperty() {

    }

    /** Property to specify whether the streaming app is running on lambda or not. */
    public static final String AWS_LAMBDA_EXECUTION_PROP = "vehicle.notification.aws.lambda.execution";
    /** Notification config collection name. */
    public static final String NOTIFICATION_CONFIG_COLLECTION_NAME = "notification.config.colln.name";
    /** Notification DAO class name. */
    public static final String NOTIFICATION_DAO_CLASSNAME = "vehicle.notification.dao.class";
    /** AWS region. */
    public static final String AWS_REGION = "aws.region";
    /** Email from address. */
    public static final String EMAIL_FROM = "email.from";
    /** Email subject. */
    public static final String EMAIL_SUBJECT = "email.subject";
    /** Email SMTP user. */
    public static final String EMAIL_SMTP_USER = "email.smtp.user";
    /** Email SMTP password. */
    public static final String EMAIL_SMTP_PASSWORD = "email.smtp.password";  //NOSONAR
    /** Email SMTP host. */
    public static final String EMAIL_SMTP_HOST = "email.smtp.host";
    /** MQTT broker URL. */
    public static final String MQTT_BROKER_URL = "mqtt.broker.url";
    /** MQTT topic namespace. */
    public static final String MQTT_TOPIC_NAMESPACE = "mqtt.topic.namespace";
    /** MQTT topic tenant. */
    public static final String MQTT_TOPIC_TENANT = "mqtt.topic.tenant";
    /** MQTT topic environment. */
    public static final String MQTT_TOPIC_ENV = "mqtt.topic.env";
    /** MQTT topic separator. */
    public static final String MQTT_TOPIC_SEPARATOR = "mqtt.topic.separator";
    /** MQTT config QoS. */
    public static final String MQTT_CONFIG_QOS = "mqtt.config.qos";
    /** MQTT user name. */
    public static final String MQTT_USER_NAME = "mqtt.user.name";
    /** MQTT user password. */
    public static final String MQTT_USER_PASSWORD = "mqtt.user.password";  //NOSONAR
    /** Browser enable default. */
    public static final String BROWSER_ENABLE_DEFAULT = "browser.enable.default";
    /** Service class. */
    public static final String SERVICE_CLASS = "service.class";
    /** AWS SNS endpoint name. */
    public static final String AWS_SNS_ENDPOINT_NAME = "aws.sns.endpoint.name";
    /** AWS SES endpoint name. */
    public static final String AWS_SES_ENDPOINT_NAME = "aws.ses.endpoint.name";
    /** AWS SNS topic ARN prefix. */
    public static final String AWS_SNS_TOPIC_ARN_PREFIX = "aws.sns.topic.arn.prefix";
    /** AWS SNS push app name. */
    public static final String AWS_SNS_PUSH_APP_NAME = "aws.sns.push.app.name";
    /** AWS SNS iOS push platform name. */
    public static final String AWS_SNS_IOS_PUSH_PLATFORM_NAME = "aws.sns.ios.push.platform.name";
    /** AWS SNS push certificate key filename. */
    public static final String AWS_SNS_PUSH_CERT_KEY_FILENAME = "aws.sns.push.cert.key.filename";
    /** AWS SNS push private key filename. */
    public static final String AWS_SNS_PUSH_PRIVATE_KEY_FILENAME = "aws.sns.push.private.key.filename";
    /** Device token collection name. */
    public static final String DEVICE_TOKEN_COLLECTION_NAME = "device.token.collection.name";
    /** MongoDB database name. */
    public static final String MONGODB_DATABASE = "mongodb.name";
    /** Resource bundle collection name. */
    public static final String RESOURCE_BUNDLE_COLLECTION_NAME = "resource.bundle.collection.name";
    /** Source topic name. */
    public static final String SOURCE_TOPIC = "source.topic.name";
    /** Sink topic name. */
    public static final String SINK_TOPIC = "sink.topic.name";
    /** Android server API key filename. */
    public static final String ANDROID_SERVER_API_KEY_FILENAME = "android.apikey.filename";
    /** Android message time to live. */
    public static final String ANDROID_MSG_TTL = "time_to_live";
    /** WSO2 tenant suffix. */
    public static final String WSO2_TENANT_SUFFIX = "wso2.tenant.suffix";
    /** Email bounce bloom filter count. */
    public static final String EMAIL_BOUNCE_BLOOM_FILTER_COUNT = "email.bounce.bloom.filter.count";
    /** Alert deduplication store. */
    public static final String ALERT_DEDUP_STORE = "alert.deduplicator.key.store";
    /** Alert deduplication store TTL in seconds. */
    public static final String ALERT_DEDUP_STORE_TTL_SECS = "alert.deduplicator.key.store.ttl.in.seconds";
    /** AWS SES bounce handler enable. */
    public static final String AWS_SES_BOUCE_HANDLER_ENABLE = "aws.ses.bounce.handler.enable";
    /** AWS SES bounce handler store. */
    public static final String AWS_SES_BOUCE_HANDLER_STORE = "aws.ses.bounce.handler.key.store";
    /** AWS SES bounce handler store TTL in seconds. */
    public static final String AWS_SES_BOUCE_HANDLER_STORE_TTL_SECS = "aws.ses.bounce.handler.key.store.ttl.in.seconds";
    /** AWS SES bounce handler frequency in minutes. */
    public static final String AWS_SES_BOUNCE_HANDLER_FREQUENCY = "aws.ses.bounce.handler.frequency.mins";
    /** AWS SES bounce queue. */
    public static final String AWS_SES_BOUNCE_QUEUE = "aws.ses.bounce.queue";
    /** Duplicate alerts bloom filter insert count. */
    public static final String DUPLICATE_ALERTS_BLOOM_FILTER_INSERT_COUNT = "dedup.bloom.filter.insert.count";
    /** Campaign store. */
    public static final String CAMPAIGN_STORE = "campaign.key.store";
    /** Campaign store TTL in seconds. */
    public static final String CAMPAIGN_STORE_TTL_SECS = "campaign.key.store.ttl.in.seconds";
    /** Store user. */
    public static final String STORE_USER = "store.user";
    /** Deduplicate alerts. */
    public static final String DEDUP_ALERTS = "dedup.alerts";
    /** Deduplication interval in milliseconds. */
    public static final String DEDUP_INTERVAL_MS = "dedup.interval.ms";
    /** Notification settings collection name. */
    public static final String NOTIFICATION_SETTINGS_COLLN_NAME = "notification.settings.colln.name";
    /** Alerts collection name. */
    public static final String ALERTS_COLLECTION_NAME = "alerts.colln.name";
    /** User alerts collection name. */
    public static final String USER_ALERTS_COLLECTION_NAME = "user.alerts.colln.name";
    /** Filtered event IDs. */
    public static final String FILTERED_EVENT_IDS = "filtered.event.ids";
    /** Scheduler source topic name. */
    public static final String SCHEDULER_SOURCE_TOPIC = "scheduler.source.topic.name";

    /** Host separator. */
    public static final String HOST_SEPARATOR = ",";
    /** Field separator. */
    public static final String FIELD_SEPARATOR = ":";
    /** Primary key name. */
    public static final String PRIMARY_KEY_NAME = "_id";
    /** Missing constant. */
    public static final String MISSING = " Missing ";
    /** Config constant. */
    public static final String CONFIG = " config ";

    // Vault settings
    /** MongoDB vault enabled. */
    public static final String IS_MONGO_VAULT_ENABLED = "mongodb.vault.enabled";
    /** Secrets vault enabled. */
    public static final String IS_SECRETS_VAULT_ENABLED = "secrets.vault.enabled";
    /** Vault server IP address. */
    public static final String VAULT_SERVER_IP_ADDRESS = "vault.server.ipaddress";
    /** Vault server port number. */
    public static final String VAULT_SERVER_PORT = "vault.server.port.number";
    /** Vault environment. */
    public static final String VAULT_ENVIRONMENT = "vault.environment";
    /** Vault secret folder. */
    public static final String VAULT_SECRET_FOLDER = "vault.secret.folder";

    // Vault API keys
    /** MongoDB vault username key. */
    public static final String MONGO_VAULT_USERNAME_KEY = "username";
    /** MongoDB vault password key. */
    public static final String MONGO_VAULT_PASS_KEY = "password";
    /** MongoDB vault lease duration. */
    public static final String MONGO_VAULT_LEASE_DURATION = "lease_duration";

    /** Check user PDID association. */
    public static final String CHECK_USER_PDID_ASSOCIATION = "check.user.pdid.association";
    /** Default channel types. */
    public static final String DEFAULT_CHANNEL_TYPES = "default.channel.types";
    /** Supported channels. */
    public static final String CHANNELS_SUPPORTED = "channels.supported";
    /** IVM response acknowledgment topic. */
    public static final String IVM_RESPONSE_ACK_TOPIC = "ivm.ack.topic";

    /** Account created event. */
    public static final String ACCOUNT_CREATED = "accountCreated";
    /** Account registered event. */
    public static final String ACCOUNT_REGISTERED = "accountRegistered";
    /** Account updated event. */
    public static final String ACCOUNT_UPDATED = "accountUpdated";
    /** Account deleted event. */
    public static final String ACCOUNT_DELETED = "accountDeleted";
}