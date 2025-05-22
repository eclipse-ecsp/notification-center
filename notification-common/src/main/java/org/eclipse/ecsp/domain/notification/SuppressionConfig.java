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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

/**
 * SuppressionConfig class.
 */
@FieldDefaults(level = PRIVATE)
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity(useDiscriminator = false)
public class SuppressionConfig {
    /**
     * SuppressionType enum.
     * RECURRING, VACATION.
     */
    public enum SuppressionType {
        RECURRING, VACATION
    }

    static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    SuppressionType suppressionType;
    List<DayOfWeek> days;

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    LocalTime startTime;

    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    LocalTime endTime;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    LocalDate startDate;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    LocalDate endDate;

    /**
     * Defines the varied configuration options for notification suppression dates and times.
     *
     * @param suppressionType Must match {@link SuppressionType}
     * @param startTime       Must match <code>HH:mm</code>
     * @param endTime         Must match <code>HH:mm</code>
     * @param startDate       Must match ISO_LOCAL_DATE <code>yyyy-MM-dd</code>. Only for VACATION suppressionType.
     * @param endDate         Must match ISO_LOCAL_DATE <code>yyyy-MM-dd</code>. Only for VACATION suppressionType.
     * @param days            List of days for recurring configuration.
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE">ISO_LOCAL_DATE</a>
     */
    public SuppressionConfig(String suppressionType, String startTime, String endTime, String startDate, String endDate,
                             List<Integer> days) {
        this.suppressionType = SuppressionType.valueOf(suppressionType);
        this.startTime = StringUtils.isEmpty(startTime) ? null : LocalTime.parse(startTime);
        this.endTime = StringUtils.isEmpty(endTime) ? null : LocalTime.parse(endTime);
        this.startDate = StringUtils.isEmpty(startDate) ? null : LocalDate.parse(startDate);
        this.endDate = StringUtils.isEmpty(endDate) ? null : LocalDate.parse(endDate);
        this.days = days.stream().map(DayOfWeek::of).collect(Collectors.toList());
    }

    /**
     * LocalTimeSerializer class.
     */
    public static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime time, JsonGenerator gen, SerializerProvider serializerProvider)
            throws IOException {
            gen.writeString(time.format(TIME_FORMATTER));
        }
    }

    /**
     * LocalTimeDeserializer static class.
     */
    public static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return LocalTime.parse(parser.getValueAsString(), TIME_FORMATTER);
        }
    }

    /**
     * LocalDateSerializer static class.
     */
    public static class LocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate date, JsonGenerator gen, SerializerProvider serializerProvider)
            throws IOException {
            gen.writeString(date.format(DATE_FORMATTER));
        }
    }

    /**
     * LocalDateDeserializer class.
     */
    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return LocalDate.parse(parser.getValueAsString(), DATE_FORMATTER);
        }
    }
}