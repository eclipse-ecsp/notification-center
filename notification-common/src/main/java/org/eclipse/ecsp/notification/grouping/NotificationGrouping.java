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

package org.eclipse.ecsp.notification.grouping;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;
import org.eclipse.ecsp.notification.dao.NotificationDaoConstants;

/**
 * NotificationGrouping entity.
 */
@Entity(value = NotificationDaoConstants.NOTIFICATION_GROUPING_COLLECTION_NAME,
    discriminatorKey = "className",
    discriminator = "org.eclipse.ecsp.notification.grouping.NotificationGrouping")
public class NotificationGrouping extends AbstractIgniteEntity {

    @Id
    private String id;
    private String notificationId;
    private String group;
    private boolean mandatory;
    private String service;
    private boolean checkEntitlement;
    private GroupType groupType;
    private boolean checkAssociation = true;

    /**
     * Constructor.
     */
    public NotificationGrouping() {
    }

    /**
     * Constructor.
     *
     * @param notificationId notificationId
     * @param group group
     */
    public NotificationGrouping(String notificationId, String group) {
        this.notificationId = notificationId;
        this.group = group;
    }


    /**
     * Get id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get Notification Id.
     *
     * @return String
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     * Set Notification Id.
     *
     * @param notificationId notificationId
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Get group.
     *
     * @return group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set group.
     *
     * @param group group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Is mandatory.
     *
     * @return boolean
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Set mandatory.
     *
     * @param mandatory mandatory
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Get service.
     *
     * @return service
     */
    public String getService() {
        return service;
    }

    /**
     * Set service.
     *
     * @param service service
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Is check entitlement.
     *
     * @return boolean
     */
    public boolean isCheckEntitlement() {
        return checkEntitlement;
    }

    /**
     * Set check entitlement.
     *
     * @param checkEntitlement checkEntitlement
     */
    public void setCheckEntitlement(boolean checkEntitlement) {
        this.checkEntitlement = checkEntitlement;
    }

    /**
     * Get group type.
     *
     * @return groupType
     */
    public GroupType getGroupType() {
        return groupType;
    }

    /**
     * Set group type.
     *
     * @param groupType groupType
     */
    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    /**
     * Is check association.
     *
     * @return boolean
     */
    public boolean isCheckAssociation() {
        return checkAssociation;
    }

    /**
     * Set check association.
     *
     * @param checkAssociation checkAssociation
     */
    public void setCheckAssociation(boolean checkAssociation) {
        this.checkAssociation = checkAssociation;
    }

    /**
     * To String.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "NotificationGrouping [id=" + id + ", notificationId=" + notificationId + ", group=" + group
            + ", mandatory=" + mandatory + ", service="
            + service + ", checkEntitlement=" + checkEntitlement + ", checkAssociation=" + checkAssociation + "]";
    }

    /**
     * Hash code.
     *
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((notificationId == null) ? 0 : notificationId.hashCode());
        return result;
    }

    /**
     * Equals.
     *
     * @param obj obj
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotificationGrouping other = (NotificationGrouping) obj;
        if (id == null) {
            return other.id == null;
        } else {
            return id.equals(other.id);
        }
    }

}
