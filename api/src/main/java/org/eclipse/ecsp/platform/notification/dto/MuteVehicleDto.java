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

package org.eclipse.ecsp.platform.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.platform.notification.v1.utils.Utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

/**
 * MuteVehicleDto contains vehicle mute details.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MuteVehicleDto {
    @NotEmpty(message = Utils.VALIDATION_MESSAGE)
    String vehicleId;
    Set<String> groups;
    Set<ChannelType> channels;
    String startTime;
    String endTime;

    /**
     * COnvert MuteVehicleDto to MuteVehicle.
     *
     * @param muteVehicleDto MuteVehicleDto object
     *
     * @return MuteVehicle object
     */
    @JsonIgnore
    public static MuteVehicle to(MuteVehicleDto muteVehicleDto) {
        MuteVehicle muteVehicle = new MuteVehicle();
        muteVehicle.setVehicleId(muteVehicleDto.getVehicleId());
        muteVehicle.setGroups(muteVehicleDto.getGroups());
        muteVehicle.setChannels(muteVehicleDto.getChannels());
        muteVehicle.setStartTime(getTimestampFromIsoStringDate(muteVehicleDto.getStartTime()));
        muteVehicle.setEndTime(getTimestampFromIsoStringDate(muteVehicleDto.getEndTime()));
        return muteVehicle;
    }

    /**
     * MuteVehicle to MuteVehicleDto converter.
     *
     * @param muteVehicle MuteVehicle
     *
     * @return MuteVehicleDto
     */
    @JsonIgnore
    public static MuteVehicleDto from(MuteVehicle muteVehicle) {
        MuteVehicleDto muteVehicleDto = new MuteVehicleDto();
        muteVehicleDto.setVehicleId(muteVehicle.getVehicleId());
        muteVehicleDto.setGroups(muteVehicle.getGroups());
        muteVehicleDto.setChannels(muteVehicle.getChannels());
        muteVehicleDto.setStartTime(getIsoStringDateFromTimestamp(muteVehicle.getStartTime()));
        muteVehicleDto.setEndTime(getIsoStringDateFromTimestamp(muteVehicle.getEndTime()));
        return muteVehicleDto;
    }

    /**
     * Get timestamp from string Date.
     *
     * @param time time string
     *
     * @return long timestamp
     */
    public static Long getTimestampFromIsoStringDate(String time) {
        return time == null ? null :
            LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC")).toInstant()
                .toEpochMilli();
    }

    /**
     * Get ISO string date from timestamp.
     *
     * @param time timestamp
     *
     * @return ISO string date
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    private static String getIsoStringDateFromTimestamp(Long time) {
        return time == null ? null :
            LocalDateTime.ofEpochSecond(time / 1000, 0, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
