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

import org.eclipse.ecsp.domain.notification.commons.ChannelType;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.notification.dao.MuteVehicleDAO;
import org.eclipse.ecsp.notification.entities.MuteVehicle;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.grouping.NotificationGroupingDAO;
import org.eclipse.ecsp.platform.notification.dto.MuteVehicleDto;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputException;
import org.eclipse.ecsp.platform.notification.exceptions.NotFoundException;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.eclipse.ecsp.platform.notification.utils.UnitTestUtils.compareException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * MuteVehicleServiceImplTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MuteVehicleServiceImplTest {
    private static final Set<String> GROUPS = new HashSet<>(Arrays.asList("g1", "g2"));
    private static final Set<ChannelType> CHANNELS = Collections.singleton(ChannelType.API_PUSH);
    private static final String VEHICLE_ID = "VH1";
    private static final String IN_VALID_VEHICLE_ID = "VH!";

    @InjectMocks
    private MuteVehicleServiceImpl muteVehicleService;

    @Mock
    private CoreVehicleProfileClient vehicleService;

    @Mock
    private NotificationGroupingDAO notificationGroupingDao;

    @Mock
    private MuteVehicleDAO muteVehicleDao;

    @BeforeEach
    void beforeEach() {
        initMocks(this);
    }

    @Test
    public void createMuteConfigWhenVehicleIdEmpty() {
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setVehicleId("");
        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(muteVehicleDto));
        assertEquals(NotificationCenterError.INVALID_INPUT_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void createMuteConfigWhenVehicleIdNull() {
        Mockito.doThrow(HttpClientErrorException.NotFound.class).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(getMuteVehicleDto()));
        assertEquals(NotificationCenterError.INVALID_INPUT_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void createMuteConfigWhenVehicleIdNotFound() {
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setVehicleId(IN_VALID_VEHICLE_ID);
        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(muteVehicleDto));
        assertEquals(NotificationCenterError.INVALID_INPUT_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void createMuteConfigInvalidStartTime() {
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setVehicleId(VEHICLE_ID);
        muteVehicleDto.setStartTime("2020-15-20T10:15:30");

        List<NotificationGrouping> notificationGroupings = getNotificationGroupings();
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());

        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(muteVehicleDto));
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.MUTE_CONFIG_INVALID_TIME);
    }

    @Test
    public void createMuteConfigInvalidEndTime() {
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setVehicleId(VEHICLE_ID);
        muteVehicleDto.setEndTime("2020-15-20T10:15:30");

        List<NotificationGrouping> notificationGroupings = getNotificationGroupings();
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());

        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(muteVehicleDto));
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.MUTE_CONFIG_INVALID_TIME);
    }

    @Test
    public void createMuteConfigWhenEndTimeLessThanToStartTime() {
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setVehicleId(VEHICLE_ID);
        muteVehicleDto.setStartTime("2020-12-20T10:15:30");
        muteVehicleDto.setEndTime("2020-11-20T10:15:30");
        List<NotificationGrouping> notificationGroupings = getNotificationGroupings();
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());

        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(muteVehicleDto));
        compareException(thrown, NotificationCenterError.INVALID_INPUT_EXCEPTION,
            NotificationCenterError.MUTE_CONFIG_INVALID_END_TIME);
    }

    @Test
    public void createMuteConfigWhenGroupingEmpty() {
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setGroups(null);
        MuteVehicle muteConfig = muteVehicleService.createMuteConfig(muteVehicleDto);
        assertNotNull(muteConfig);
        assertEquals(VEHICLE_ID, muteConfig.getVehicleId());
    }

    @Test
    public void createMuteConfigWhenInvalidGrouping() {
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(new ArrayList<>()).when(notificationGroupingDao).findByGroups(Mockito.any());

        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(getMuteVehicleDto()));
        assertEquals(NotificationCenterError.INVALID_INPUT_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void createMuteConfigWhenGroupingIsNotMatchWithDbGrouping() {
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());

        List<NotificationGrouping> notificationGroupings = getNotificationGroupings();
        notificationGroupings.remove(1);
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());

        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(getMuteVehicleDto()));
        assertEquals(NotificationCenterError.INVALID_INPUT_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void createMuteConfigWhenGroupingPartiallyExist() {
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());

        List<NotificationGrouping> notificationGroupings = getNotificationGroupingsSameGroups();
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());

        InvalidInputException thrown =
            assertThrows(InvalidInputException.class,
                () -> muteVehicleService.createMuteConfig(getMuteVehicleDto()));
        assertEquals(NotificationCenterError.INVALID_INPUT_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void createMuteConfig() {
        List<NotificationGrouping> notificationGroupings = getNotificationGroupings();
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());
        MuteVehicle muteConfig = muteVehicleService.createMuteConfig(getMuteVehicleDto());
        assertNotNull(muteConfig);
        assertEquals(VEHICLE_ID, muteConfig.getVehicleId());
    }

    @Test
    public void createMuteConfigStartTimeIsNull() {
        List<NotificationGrouping> notificationGroupings = getNotificationGroupings();
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setStartTime(null);
        MuteVehicle muteConfig = muteVehicleService.createMuteConfig(muteVehicleDto);
        assertNotNull(muteConfig);
        assertEquals(VEHICLE_ID, muteConfig.getVehicleId());
    }

    @Test
    public void createMuteConfigEndTimeIsNull() {
        List<NotificationGrouping> notificationGroupings = getNotificationGroupings();
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).save(Mockito.any());
        Mockito.doReturn(Optional.of(VEHICLE_ID)).when(vehicleService)
            .getVehicleProfileJSON(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationGroupings).when(notificationGroupingDao).findByGroups(Mockito.any());
        MuteVehicleDto muteVehicleDto = getMuteVehicleDto();
        muteVehicleDto.setEndTime(null);
        MuteVehicle muteConfig = muteVehicleService.createMuteConfig(muteVehicleDto);
        assertNotNull(muteConfig);
        assertEquals(VEHICLE_ID, muteConfig.getVehicleId());
    }

    @Test
    public void findMuteConfigByIdWhenInvalidVehicleId() {
        Mockito.doReturn(null).when(muteVehicleDao).findById(Mockito.any());

        NotFoundException thrown =
            assertThrows(NotFoundException.class,
                () -> muteVehicleService.getMuteConfigById(VEHICLE_ID));
        Assert.assertEquals(NotificationCenterError.NOT_FOUND_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void findMuteConfigById() {
        Mockito.doReturn(getMuteVehicle()).when(muteVehicleDao).findById(Mockito.any());
        MuteVehicle muteVehicle = muteVehicleService.getMuteConfigById(VEHICLE_ID);
        assertNotNull(muteVehicle);
        assertEquals(VEHICLE_ID, muteVehicle.getVehicleId());
    }

    @Test
    public void deleteMuteConfigByIdWhenInvalidId() {
        Mockito.doReturn(false).when(muteVehicleDao).deleteById(Mockito.any());
        NotFoundException thrown =
            assertThrows(NotFoundException.class,
                () -> muteVehicleService.deleteMuteConfigById(VEHICLE_ID));
        Assert.assertEquals(NotificationCenterError.NOT_FOUND_EXCEPTION.getMessage(), thrown.getMessage());
    }

    @Test
    public void deleteMuteConfigById() {
        Mockito.doReturn(true).when(muteVehicleDao).deleteById(Mockito.any());
        Assert.assertNotNull(muteVehicleDao);
        muteVehicleService.deleteMuteConfigById(VEHICLE_ID);
    }

    private List<NotificationGrouping> getNotificationGroupings() {
        List<NotificationGrouping> notificationGroupings = new ArrayList<>();
        NotificationGrouping grouping1 = new NotificationGrouping();
        grouping1.setGroup("GRP1");
        NotificationGrouping grouping2 = new NotificationGrouping();
        grouping2.setGroup("GRP2");
        notificationGroupings.add(grouping1);
        notificationGroupings.add(grouping2);
        return notificationGroupings;
    }

    private List<NotificationGrouping> getNotificationGroupingsSameGroups() {
        List<NotificationGrouping> notificationGroupings = new ArrayList<>();
        NotificationGrouping grouping1 = new NotificationGrouping();
        grouping1.setGroup("GRP1");
        NotificationGrouping grouping2 = new NotificationGrouping();
        grouping2.setGroup("GRP1");
        notificationGroupings.add(grouping1);
        notificationGroupings.add(grouping2);
        return notificationGroupings;
    }

    private MuteVehicleDto getMuteVehicleDto() {
        MuteVehicleDto muteVehicleDto = new MuteVehicleDto();
        muteVehicleDto.setVehicleId(VEHICLE_ID);
        muteVehicleDto.setGroups(GROUPS);
        muteVehicleDto.setChannels(CHANNELS);
        muteVehicleDto.setStartTime("2020-12-20T10:15:30");
        muteVehicleDto.setEndTime("2021-12-20T10:15:30");
        return muteVehicleDto;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private MuteVehicle getMuteVehicle() {
        MuteVehicle muteVehicle = new MuteVehicle();
        muteVehicle.setVehicleId(VEHICLE_ID);
        muteVehicle.setGroups(GROUPS);
        muteVehicle.setChannels(CHANNELS);
        muteVehicle.setStartTime(1L);
        muteVehicle.setEndTime(2L);
        return muteVehicle;
    }
}
