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

package org.eclipse.ecsp.domain.notification.commons;

/**
 * EventID class with constant event ids.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class EventID {
    /*
     * Event id for refreshing scheduler when user data has changed
     */
    public static final String REFRESH_NOTIFICATION_SCHEDULER = "refreshNotificationScheduler";
    /**
     * NOTIFICATION_SETTINGS.
     */
    public static final String NOTIFICATION_SETTINGS = "NotificationSettings";

    /**
     * NOTIFICATION_USER_PROFILE.
     */
    public static final String NOTIFICATION_USER_PROFILE = "NotificationUserProfile";
    /**
     * PIN_GENERATED.
     */
    public static final String PIN_GENERATED = "PinGenerated";


    /**
     * API_PUSH_NOTIFICATION.
     */
    public static final String API_PUSH_NOTIFICATION = "APIPushNotification";
    /**
     * API_PUSH_TEST_EVENT.
     */
    public static final String API_PUSH_TEST_EVENT = "ApiPushTestEvent";

    /**
     * ASSOCIATION.
     */
    public static final String ASSOCIATION = "VehicleAssociation";
    /**
     * DISASSOCIATION.
     */
    public static final String DISASSOCIATION = "VehicleDisAssociation";
    /**
     * VEHICLE_MESSAGE_ACK.
     */
    public static final String VEHICLE_MESSAGE_ACK = "VehicleMessageAck";
    /**
     * VEHICLE_MESSAGE_PUBLISH.
     */
    public static final String VEHICLE_MESSAGE_PUBLISH = "VehicleMessagePublish";
    /**
     * VEHICLE_MESSAGE_DISPOSITION_PUBLISH.
     */
    public static final String VEHICLE_MESSAGE_DISPOSITION_PUBLISH = "VehicleMessageDispositionPublish";
    /**
     * VEHICLE_MESSAGE_DISPOSITION_ACK.
     */
    public static final String VEHICLE_MESSAGE_DISPOSITION_ACK = "VehicleMessageDispositionAck";
    /**
     * DFF_FEEDBACK_EVENT.
     */
    public static final String DFF_FEEDBACK_EVENT = org.eclipse.ecsp.domain.EventID.DFF_FEEDBACK_EVENT;

    /**
     * DMA_FEEDBACK_EVENT.
     */
    public static final String DMA_FEEDBACK_EVENT = org.eclipse.ecsp.domain.EventID.DEVICEMESSAGEFAILURE;
    /**
     * CREATE_SECONDARY_CONTACT.
     */
    public static final String CREATE_SECONDARY_CONTACT = "CreateSecondaryContact";
    /**
     * UPDATE_SECONDARY_CONTACT.
     */
    public static final String UPDATE_SECONDARY_CONTACT = "UpdateSecondaryContact";
    /**
     * DELETE_SECONDARY_CONTACT.
     */
    public static final String DELETE_SECONDARY_CONTACT = "DeleteSecondaryContact";
    /**
     * IVM_FEEDBACK.
     */
    public static final String IVM_FEEDBACK = "IvmFeedback";
    /**
     * NOTIFICATION_FEEDBACK.
     */
    public static final String NOTIFICATION_FEEDBACK = "NotificationFeedback";

    /**
     * NOTIFICATION_MILESTONE_FEEDBACK.
     */
    public static final String NOTIFICATION_MILESTONE_FEEDBACK = "MilestoneEvent";
    /**
     * NOTIFICATION_LIFECYCLE_FEEDBACK.
     */
    public static final String NOTIFICATION_LIFECYCLE_FEEDBACK = "LifecycleEvent";
    /**
     * DYNAMIC_NOTIFICATION.
     */
    public static final String DYNAMIC_NOTIFICATION = "DynamicNotificationEvent";
    /**
     * VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT.
     */
    public static final String VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT =
        "VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT";
    /**
     * NON_REGISTERED_USER_NOTIFICATION_EVENT.
     */
    public static final String NON_REGISTERED_USER_NOTIFICATION_EVENT = "NON_REGISTERED_USER_NOTIFICATION_EVENT";

    /**
     * CAMPAIGN_EVENT.
     */
    public static final String CAMPAIGN_EVENT = "CAMPAIGN_EVENT";
    /**
     * CAMPAIGN_STATUS_EVENT.
     */
    public static final String CAMPAIGN_STATUS_EVENT = "CampaignStatus";

    //Schedule Notification constants
    public static final String DELETE_SCHEDULED_NOTIFICATION_COMMAND = "DeleteScheduleNotificationCommand";
    /**
     * GENERIC_NOTIFICATION_EVENT.
     */
    public static final String GENERIC_NOTIFICATION_EVENT = "GenericNotificationEvent";
    /**
     * RETRY_NOTIFICATION_EVENT.
     */
    public static final String RETRY_NOTIFICATION_EVENT = "RetryNotificationEvent";

    private EventID() {
    }
}