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

package org.eclipse.ecsp.domain.notification;

import dev.morphia.annotations.Entity;
import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.notification.EventMetadata.CollisionDataFieldNames;
import org.eclipse.ecsp.domain.notification.EventMetadata.EventID;
import org.eclipse.ecsp.domain.notification.commons.NotificationConstants;
import org.eclipse.ecsp.entities.AbstractIgniteEntity;

import java.util.Map;

/**
 * AccidentRecord class.
 */
@Entity(value = "accidentrecords")
public class AccidentRecord extends AbstractIgniteEntity {

    private String pdId;
    private boolean isEventDateSysGenerated;
    private int remainingSizeInBytes;

    private long eventDate;
    private Double lastKnownSpeed;
    private Location location;

    /**
     * Default constructor.
     */
    public AccidentRecord() {
        this(null);
    }

    /**
     * COnstructor for accident record.
     *
     * @param alertsInfo alertInfo details
     */
    public AccidentRecord(AlertsInfo alertsInfo) {

        /*
         * Just to make sure, if this constructor is called from somewhere, it
         * should work only for collision events
         */
        if (null != alertsInfo && !EventID.COLLISION.toString().equals(alertsInfo.getEventID())) {
            throw new RuntimeException("Accident Records cannot be created for event:" + alertsInfo.getEventID());
        }

        isEventDateSysGenerated = true;
        remainingSizeInBytes = NotificationConstants.REMAINING_SIZE_IN_BYTES;
        if (null != alertsInfo) {
            pdId = alertsInfo.getPdid();
            eventDate = alertsInfo.getTimestamp();
            Map<String, Object> data = alertsInfo.getAlertsData().any();
            if (null != data) {
                lastKnownSpeed = getDoubleValue(data.get(CollisionDataFieldNames.SPEED.toString()));
                Double latitude = getDoubleValue(data.get(CollisionDataFieldNames.LATITUDE.toString()));
                Double longitude = getDoubleValue(data.get(CollisionDataFieldNames.LONGITUDE.toString()));
                if (null != latitude && null != longitude) {
                    location = new Location(latitude, longitude);
                }
            }
        }
    }

    /**
     * Get double value.
     *
     * @param object object
     *
     * @return double value
     */
    private Double getDoubleValue(Object object) {
        if (object != null) {
            String str = object.toString();
            if (StringUtils.isNotEmpty(str)) {
                return Double.valueOf(str);
            }
        }
        return null;
    }

    /**
     * Get pdId.
     *
     * @return String pdId
     */
    public String getPdId() {
        return pdId;
    }

    /**
     * Set pdId.
     *
     * @param pdId pdId
     */
    public void setPdId(String pdId) {
        this.pdId = pdId;
    }

    /**
     * Is event date sys generated.
     *
     * @return boolean isEventDateSysGenerated
     */
    public boolean isEventDateSysGenerated() {
        return isEventDateSysGenerated;
    }

    /**
     * Set event date sys generated.
     *
     * @param isEventDateSysGenerated isEventDateSysGenerated
     */
    public void setEventDateSysGenerated(boolean isEventDateSysGenerated) {
        this.isEventDateSysGenerated = isEventDateSysGenerated;
    }

    /**
     * Get location.
     *
     * @return Location loc
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set location.
     *
     * @param location location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Get remaining size in bytes.
     *
     * @return remainingSizeInBytes int
     */
    public int getRemainingSizeInBytes() {
        return remainingSizeInBytes;
    }

    /**
     * Set remaining size in bytes.
     *
     * @param remainingSizeInBytes remainingSizeInBytes
     */
    public void setRemainingSizeInBytes(int remainingSizeInBytes) {
        this.remainingSizeInBytes = remainingSizeInBytes;
    }

    /**
     * Get event date.
     *
     * @return eventDate long
     */
    public long getEventDate() {
        return eventDate;
    }

    /**
     * Set event date.
     *
     * @param eventDate eventDate
     */
    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Get last known speed.
     *
     * @return lastKnownSpeed double
     */
    public Double getLastKnownSpeed() {
        return lastKnownSpeed;
    }

    /**
     * Set last known speed.
     *
     * @param lastKnownSpeed lastKnownSpeed
     */
    public void setLastKnownSpeed(Double lastKnownSpeed) {
        this.lastKnownSpeed = lastKnownSpeed;
    }

    /**
     * Location class.
     */
    @Entity
    public static class Location {
        private Double latitude;
        private Double longitude;

        /**
         * Default constructor.
         *
         * @param latitude  latitude
         * @param longitude longitude
         */
        public Location(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * Get latitude.
         *
         * @return latitude
         */
        public Double getLatitude() {
            return latitude;
        }

        /**
         * Set latitude.
         *
         * @param latitude latitude
         */
        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        /**
         * Get longitude.
         *
         * @return longitude double
         */
        public Double getLongitude() {
            return longitude;
        }

        /**
         * Set longitude.
         *
         * @param longitude longitude
         */
        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        /**
         * To string.
         *
         * @return string
         */
        @Override
        public String toString() {
            return "Location [latitude=" + latitude + ", longitude=" + longitude + "]";
        }
    }

    /**
     * To string.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "AccidentRecord [pdId=" + pdId + ", isEventDateSysGenerated=" + isEventDateSysGenerated
                + ", remainingSizeInBytes="
                + remainingSizeInBytes + ", eventDate=" + eventDate + ", lastKnownSpeed=" + lastKnownSpeed + "]";
    }

}
