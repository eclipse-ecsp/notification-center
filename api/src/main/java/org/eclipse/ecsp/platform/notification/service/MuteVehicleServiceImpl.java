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

package org.eclipse.ecsp.platform.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.notification.dao.MuteVehicleDAO;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.MuteVehicleDto;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mute Notification Configuration service.
 */
@Service
@Slf4j
public class MuteVehicleServiceImpl implements MuteVehicleService {

    private final MuteVehicleDAO muteVehicleDao;

    private final CoreVehicleProfileClient vehicleService;

    private final NotificationGroupingDAO notificationGroupingDao;

    @Value("${enable.ignite.vehicle.profile:true}")
    private boolean igniteVehicleProfile;

    /**
     * Parameterized c'tor.
     *
     * @param muteVehicleDao {@link MuteVehicleDAO}
     */
    @Autowired
    public MuteVehicleServiceImpl(MuteVehicleDAO muteVehicleDao, CoreVehicleProfileClient vehicleService,
                                  NotificationGroupingDAO notificationGroupingDao) {
        this.muteVehicleDao = muteVehicleDao;
        this.vehicleService = vehicleService;
        this.notificationGroupingDao = notificationGroupingDao;
    }

    /**
     * Create or Update Mute Notification configuration after
     * validating input params like start time, end time,vehicle Id.
     *
     * @param muteVehicleDto Instance of {@link MuteVehicleDto}
     * @return {@link MuteVehicle} model object
     */
    @Override
    public MuteVehicle createMuteConfig(MuteVehicleDto muteVehicleDto) {
        performMuteConfigValidation(muteVehicleDto);
        MuteVehicle muteVehicle = MuteVehicleDto.to(muteVehicleDto);

        IgniteCriteria criteria = new IgniteCriteria("_id", Operator.EQ, muteVehicleDto.getVehicleId());
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup(criteria);
        IgniteQuery query = new IgniteQuery(criteriaGroup);
        muteVehicleDao.upsert(query, muteVehicle);
        return muteVehicle;
    }

    /**
     * Find mute notification config based on Vehicle Id.
     *
     * @param vehicleId VehicleId a.k.a VIN
     *
     * @return {@link MuteVehicle} model object
     */
    @Override
    public MuteVehicle getMuteConfigById(String vehicleId) {
        MuteVehicle muteVehicle = muteVehicleDao.findById(vehicleId);
        if (muteVehicle == null) {
            throw new NotFoundException(
                Collections.singletonList(NotificationCenterError.MUTE_CONFIG_VEHICLE_ID_DOES_NOT_EXIST.toMessage()));
        }
        return muteVehicle;
    }

    /**
     * Delete mute notification config based on Vehicle Id.
     *
     * @param vehicleId Vehicle Id a.k.a VIN
     */
    @Override
    public void deleteMuteConfigById(String vehicleId) {
        boolean deleteStatus = muteVehicleDao.deleteById(vehicleId);
        if (!deleteStatus) {
            throw new NotFoundException(
                Collections.singletonList(NotificationCenterError.MUTE_CONFIG_VEHICLE_ID_DOES_NOT_EXIST.toMessage()));
        }
    }

    /**
     * Perform mute config validations.
     *
     * @param muteVehicleDto {@link MuteVehicleDto} model object
     */
    private void performMuteConfigValidation(MuteVehicleDto muteVehicleDto) {
        validateVehicleId(muteVehicleDto.getVehicleId());
        validateGroups(muteVehicleDto.getGroups());
        validateStartAndEndTime(muteVehicleDto.getStartTime(), muteVehicleDto.getEndTime());
    }

    /**
     * Vehicle Id should not be empty of null.
     *
     * @param vehicleId Vehicle Id a.k.a VIN
     */
    private void validateVehicleId(String vehicleId) {
        try {
            Optional<String> vehicleProfile = vehicleService.getVehicleProfileJSON(vehicleId, igniteVehicleProfile);
            if (!vehicleProfile.isPresent()) {
                log.error(String.format("Vehicle ID %s does not exist", vehicleId));
                throw new InvalidInputException(Collections.singletonList(
                    NotificationCenterError.MUTE_CONFIG_VEHICLE_ID_DOES_NOT_EXIST.toMessage()));
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.error(String.format("Vehicle ID %s does not exist", vehicleId));
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.MUTE_CONFIG_VEHICLE_ID_DOES_NOT_EXIST.toMessage()));
        } catch (Exception e) {
            log.error("Failed getting vehicle profile", e);
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.MUTE_CONFIG_FAILURE_GETTING_VEHICLE.toMessage()));
        }
    }

    /**
     * Validate groups.
     *
     * @param groups Set of groups
     */
    private void validateGroups(Set<String> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }

        List<NotificationGrouping> notificationGroupings = notificationGroupingDao.findByGroups(groups);
        if (CollectionUtils.isEmpty(notificationGroupings)
            || notificationGroupings.stream().map(NotificationGrouping::getGroup).distinct().count() < groups.size()) {
            groups.removeAll(
                notificationGroupings.stream().map(NotificationGrouping::getGroup).collect(Collectors.toSet()));
            log.error(String.format("The following group[s] don't exist: %s", groups));
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.MUTE_CONFIG_INVALID_GROUP.toMessage()));
        }
    }

    /**
     * Mute config end time should not be <= startTime.
     *
     * @param startTime Mute Notification config start time
     *
     * @param endTime   Mute Notification config end time
     */
    private void validateEndTime(Long startTime, Long endTime) {
        if (startTime != null && endTime != null && endTime <= startTime) {
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.MUTE_CONFIG_INVALID_END_TIME.toMessage()));
        }
    }

    /**
     * Mute config start time and end time should be null ot in valid iso format.
     *
     * @param startTime Mute Notification config start time
     *
     * @param endTime   Mute Notification config end time
     */
    private void validateStartAndEndTime(String startTime, String endTime) {
        Long start;
        Long end;

        try {
            start = MuteVehicleDto.getTimestampFromIsoStringDate(startTime);
            end = MuteVehicleDto.getTimestampFromIsoStringDate(endTime);
        } catch (Exception e) {
            throw new InvalidInputException(
                Collections.singletonList(NotificationCenterError.MUTE_CONFIG_INVALID_TIME.toMessage()));
        }

        validateEndTime(start, end);
    }
}
