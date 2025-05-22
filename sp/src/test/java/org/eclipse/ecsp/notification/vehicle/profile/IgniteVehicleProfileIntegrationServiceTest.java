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

package org.eclipse.ecsp.notification.vehicle.profile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ecsp.domain.VehicleAttributes;
import org.eclipse.ecsp.domain.VehicleProfile;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.domain.notification.AlertsInfo.Data;
import org.eclipse.ecsp.domain.notification.VehicleProfileAbridged;
import org.eclipse.ecsp.domain.notification.utils.CoreVehicleProfileClient;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.notification.exception.UserNotAssociatedException;
import org.eclipse.ecsp.notification.grouping.NotificationGrouping;
import org.eclipse.ecsp.notification.utils.PropertyUtils;
import org.eclipse.ecsp.services.constants.VehicleProfileAttribute;
import org.eclipse.ecsp.services.entities.VehicleProfileOnDemandAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * IgniteVehicleProfileIntegrationServiceTest.
 */
public class IgniteVehicleProfileIntegrationServiceTest {
    private static final String VIN = "vinVal";

    @InjectMocks
    private IgniteVehicleProfileIntegrationService vpService;

    @Mock
    private CoreVehicleProfileClient coreVehicleProfileClient;

    @Mock
    private PropertyUtils utils;

    @Mock
    private IgniteEvent event;

    VehicleProfileOnDemandAttribute make = new VehicleProfileOnDemandAttribute("make", "$.data.make", String.class);
    VehicleProfileOnDemandAttribute model = new VehicleProfileOnDemandAttribute("model", "$.data.model", String.class);
    VehicleProfileOnDemandAttribute modelYear =
        new VehicleProfileOnDemandAttribute("modelYear", "$.data.modelYear", String.class);
    VehicleProfileOnDemandAttribute name = new VehicleProfileOnDemandAttribute("name", "$.data.name", String.class);
    VehicleProfileOnDemandAttribute vin = new VehicleProfileOnDemandAttribute("vin", "$.data.vin", String.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessWithModelOrModelNull() {
        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();

        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.of(VIN));
        String userId = "testUser";
        String vehicleId = "MAR_1234";
        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.USERID, Optional.of(userId));
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            VehicleProfileAttribute.USERID);

        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);


        assertEquals(vh.getName(), vp.getName());
        assertEquals(vh.getModel(), vp.getModel());
        assertEquals(vh.getModelYear(), vp.getModelYear());
        assertEquals(vh.getMake(), vp.getMake());
        assertTrue(StringUtils.isEmpty(vp.getEmergencyNumber()));
        assertTrue(StringUtils.isEmpty(vp.getPlateNumber()));
        assertEquals(userId, vp.getUserId());

    }

    @Test
    public void testProcessWithModelNull() {

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.ofNullable(null));
        String userId = "testUser";
        String vehicleId = "MAR_1234";
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.USERID, Optional.of(userId));
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            VehicleProfileAttribute.USERID);

        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);

        assertEquals(vh.getName(), vp.getName());
        assertEquals(vh.getModel(), vp.getModel());
        assertEquals(vh.getModelYear(), vp.getModelYear());
        assertEquals(vh.getMake(), vp.getMake());
        assertNull(vp.getVin());
        assertTrue(StringUtils.isEmpty(vp.getEmergencyNumber()));
        assertTrue(StringUtils.isEmpty(vp.getPlateNumber()));
        assertEquals(userId, vp.getUserId());

    }

    @SuppressWarnings("checkstyle:MethodLength")
    @Test
    public void testProcessWithVehicleJsonAttributes() {

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        // create sample data
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.ofNullable(VIN));
        String userId = "testUser";
        String vehicleId = "MAR_1234";
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.USERID, Optional.of(userId));
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            VehicleProfileAttribute.USERID);

        Optional<String> json = Optional.of("{\n"
            + "    \"vehicleId\": \"T5N1AR2MM3FC619813\",\n"
            +
            "    \"vin\": \"T5N1AR2MM3FC619813\",\n"
            +
            "    \"createdOn\": \"2018-07-17T20:48:52.757Z\",\n"
            +
            "    \"updatedOn\": \"2018-07-17T20:48:52.757Z\",\n"
            +
            "    \"productionDate\": \"2018-07-17T20:17:13.711Z\",  \n"
            +
            "    \"soldRegion\":\"EMEA\",\n"
            +
            "    \"vehicleAttributes\": {\n"
            +
            "        \"baseColor\": \"White\",\n"
            +
            "        \"bodyStyle\": \"Coupe\",\n"
            +
            "        \"destinationCountry\": \"US\",\n"
            +
            "        \"engineType\": \"G\",\n"
            +
            "        \"make\": \"fita\",\n"
            +
            "        \"marketingColor\": \"Pearl White\",\n"
            +
            "        \"model\": \"Punto\",\n"
            +
            "        \"modelYear\": \"2019\",\n"
            +
            "        \"bodyType\": \"not_used_abc\",\n"
            +
            "        \"name\": \"not_used_abc\",\n"
            +
            "        \"trim\":\"XLE\",\n"
            +
            "        \"brandCode\": \"abc_specific_00\",\n"
            +
            "        \"modelCode\":\"abc_specific_0DQ\",\n"
            +
            "        \"versionCode\":\"abc_specific_12\",\n"
            +
            "        \"seriesCode\":\"abc_specific_12\",\n"
            +
            "        \"seriesSpecialCode\":\"abc_specific_12\",\n"
            +
            "        \"plantName\":\"A\"\n"
            +
            "    },\n"
            +
            "    \"saleAttributes\":{\n"
            +
            "      \"dealerCode\":\"3131\",\n"
            +
            "      \"saleDate\":\"2018-07-17T20:17:13.711Z\",\n"
            +
            "      \"eventType\":\"CCF\",\n"
            +
            "      \"eventDate\":\"2018-07-17T20:17:13.711Z\",\n"
            +
            "      \"warrantyStartDate\":\"2018-07-17T20:17:13.711Z\",\n"
            +
            "      \"marketCode\":\"EMEA\",\n"
            +
            "      \"salesChannel\":\"DEALER\",\n"
            +
            "      \"customerSegment\":\"SALES_CLUSTER\"\n"
            +
            "    },\n"
            +
            "    \"schemaVersion\": \"1.0\",\n"
            +
            "    \"abcFirstTrialDate\": \"2018-07-17T20:48:52.757Z\",\n"
            +
            "    \"abcVehicleType\": \"NON-RENTAL\",\n"
            +
            "    \"authorizedUsers\": [{\n"
            +
            "        \"createdOn\": \"2018-07-17T20:48:52.757Z\",\n"
            +
            "        \"role\": \"VEHICLE_OWNER\",\n"
            +
            "        \"updatedOn\": \"2018-07-17T20:48:52.757Z\",\n"
            +
            "        \"userId\": \"schunchu\",\n"
            +
            "        \"source\": \"IVA\",\n"
            +
            "        \"status\": \"BASIC_STAGE_1\",\n"
            +
            "        \"licensePlate\": \"Q71 9031\",\n"
            +
            "        \"emergencyContacts\":[\n"
            +
            "        {\n"
            +
            "            \"name\":\"Robert\",\n"
            +
            "            \"phone\":\"+1 2299910912\"\n"
            +
            "        }\n"
            +
            "        ],\n"
            +
            "        \"tc\": {\n"
            +
            "            \"registration\": {\n"
            +
            "                \"countryCode\": \"US\",\n"
            +
            "                \"lastAcceptedOn\": \"2018-09-14T16:37:51.357Z\",\n"
            +
            "                \"status\": \"AGREED\",\n"
            +
            "                \"version\": \"us1.0.1\"\n"
            +
            "            },\n"
            +
            "            \"activation\": {\n"
            +
            "                \"countryCode\": \"US\",\n"
            +
            "                \"lastAcceptedOn\": \"2018-09-14T16:37:51.357Z\",\n"
            +
            "                \"status\": \"AGREED\",\n"
            +
            "                \"version\": \"us1.0.1\"\n"
            +
            "            }\n"

            +
            "        }\n"
            +
            "    }],\n"
            +
            "    \"modemInfo\": {\n"
            +
            "        \"iccid\": \"123987456963\",\n"
            +
            "        \"eid\": \"123987456963\",\n"
            +
            "        \"imei\": \"12484977388\",\n"
            +
            "        \"imsi\": \"7895412789321748\",\n"
            +
            "        \"msisdn\": \"7895412789321748\"\n"
            +
            "    },\n"
            +
            "    \"events\": {\n"
            +
            "        \"nvdr\": {\n"
            +
            "            \"eventId\": \"2344234\",\n"
            +
            "            \"eventDate\": \"2018-07-17T20:17:13.711Z\"\n"
            +
            "        }\n"
            +
            "    },\n"
            +
            "    \"customParams\": {\n"
            +
            "        \"service1\": {\n"
            +
            "            \"key1\": \"value1\",\n"
            +
            "            \"key2\": \"value2\"\n"
            +
            "        }\n"
            +
            "    },\n"
            +
            "    \"vehicleArchType\": \"hu,xyz\",\n"
            +
            "    \"ecus\": {\n"
            +
            "        \"hu\": {\n"
            +
            "            \"swVersion\": \"1.58b\",\n"
            +
            "            \"hwVersion\": \"1.54\",\n"
            +
            "            \"partNumber\": \"187469231\",\n"
            +
            "            \"os\": \"Android\",\n"
            +
            "      \"screenSize\":\"5\",\n"
            +
            "            \"manufacturer\": \"Harman\",\n"
            +
            "            \"ecuType\": \"VP4\",\n"
            +
            "            \"serialNo\": \"9874265387896345\",\n"
            +
            "            \"clientId\": \"12345\",\n"
            +
            "            \"capabilities\": {\n"
            +
            "                \"services\": [{\n"
            +
            "                        \"serviceId\": \"SRTD\"\n"
            +
            "                    },\n"
            +
            "                    {\n"
            +
            "                        \"serviceId\": \"REOND\"\n"
            +
            "                    }\n"
            +
            "                ],\n"
            +
            "                \"abcApplications\": [{\n"
            +
            "                        \"serviceId\": \"SOS\",\n"
            +
            "                        \"applicationId\": \"SOS_apt_v01\",\n"
            +
            "                        \"version\": \"1.43\"\n"
            +
            "                    },\n"
            +
            "                    {\n"
            +
            "                        \"serviceId\": \"AssistApp\",\n"
            +
            "                        \"applicationId\": \"AssistApp_apt_v05\",\n"
            +
            "                        \"version\": \"1.44\"\n"
            +
            "                    }\n"
            +
            "                ]\n"
            +
            "            },\n"
            +
            "           \"provisionedServices\": {\n"
            +
            "                \"services\": [{\n"
            +
            "                    \"serviceId\": \"REON\",\n"
            +
            "                    \"applicationId\": \"REON\"\n"
            +
            "                }],\n"
            +
            "                \"abcApplications\": [{\n"
            +
            "                    \"serviceId\": \"SOS\",\n"
            +
            "                    \"applicationId\": \"SOS\",\n"
            +
            "                    \"version\": \"1.43\"\n"
            +
            "                }]\n"
            +
            "            },\n"
            +
            "            \"abcProvisionedState\": {\n"
            +
            "                \"state\": \"PENDING\",\n"
            +
            "                \"date\": \"2018-07-17T20:48:52.757Z\"\n"
            +
            "            },\n"
            +
            "            \"abcDrm\": {\n"
            +
            "                \"raw\": \"readable DRM file\",\n"
            +
            "                \"signed\": \"you_cannot_read_this_bcz_it_is_signed\",\n"
            +
            "                \"signatureType\":\"RSA2048_SHA256_RSASSA_PKCS1_V1_5\",\n"
            +
            "                \"certificateChain\":[\"-----BEGIN CERTIFICATE-----MIIC ... ps==n-----END CERTIFICATE-----"
            + "\",\"-----BEGIN CERTIFICATE-----\\\\MIIC ... AB==\\\\n-----END CERTIFICATE-----\"],\n"

            +
            "                \"createdOn\": \"2018-07-17T20:48:52.757Z\"\n"
            +
            "            },\n"
            +
            "            \"abcDrmType\": \"JAR\",\n"
            +
            "            \"abcProductId\": \"signing_key\",\n"
            +
            "            \"isNavigationPresent\": true,\n"
            +
            "            \"swComponentIdHwNumber\":\"SWM_specific_attribute\",\n"
            +
            "            \"swComponentIdHwVersion\":\"SWM_specific_attribute\",\n"
            +
            "            \"fwVersionSwNumber\":\"SWM_specific_attribute\",\n"
            +
            "            \"fwVersionSwVersion\":\"SWM_specific_attribute\"\n"
            +
            "             \n"
            +
            "         \n"
            +
            "        },\n"
            +
            "      \"xyz\": {\n"
            +
            "            \"swVersion\": \"1.58b\",\n"
            +
            "            \"hwVersion\": \"1.54\",\n"
            +
            "            \"partNumber\": \"187469231\",\n"
            +
            "            \"os\": \"Android\",\n"
            +
            "      \"screenSize\":\"5\",\n"
            +
            "            \"manufacturer\": \"Harman\",\n"
            +
            "            \"ecuType\": \"VP4\",\n"
            +
            "            \"serialNo\": \"9874265387896345\",\n"
            +
            "            \"clientId\": \"12345\",\n"
            +
            "            \"capabilities\": {\n"
            +
            "                \"services\": [{\n"
            +
            "                        \"serviceId\": \"SRTD\"\n"
            +
            "                    },\n"
            +
            "                    {\n"
            +
            "                        \"serviceId\": \"REOND\"\n"
            +
            "                    }\n"
            +
            "                ],\n"
            +
            "                \"abcApplications\": [{\n"
            +
            "                        \"serviceId\": \"SOS\",\n"
            +
            "                        \"applicationId\": \"SOS_apt_v01\",\n"
            +
            "                        \"version\": \"1.43\"\n"
            +
            "                    },\n"
            +
            "                    {\n"
            +
            "                        \"serviceId\": \"AssistApp\",\n"
            +
            "                        \"applicationId\": \"AssistApp_apt_v05\",\n"
            +
            "                        \"version\": \"1.44\"\n"
            +
            "                    }\n"
            +
            "                ]\n"
            +
            "            },\n"
            +
            "           \"provisionedServices\": {\n"
            +
            "                \"services\": [{\n"
            +
            "                    \"serviceId\": \"REON\",\n"
            +
            "                    \"applicationId\": \"REON\"\n"
            +
            "                }],\n"
            +
            "                \"abcApplications\": [{\n"
            +
            "                    \"serviceId\": \"SOS\",\n"
            +
            "                    \"applicationId\": \"SOS\",\n"
            +
            "                    \"version\": \"1.43\"\n"
            +
            "                }]\n"
            +
            "            },\n"
            +
            "            \"abcProvisionedState\": {\n"
            +
            "                \"state\": \"PENDING\",\n"
            +
            "                \"date\": \"2018-07-17T20:48:52.757Z\"\n"
            +
            "            },\n"
            +
            "            \"abcDrm\": {\n"
            +
            "                \"raw\": \"readable DRM file\",\n"
            +
            "                \"signed\": \"you_cannot_read_this_bcz_it_is_signed\",\n"
            +
            "                \"signatureType\":\"RSA2048_SHA256_RSASSA_PKCS1_V1_5\",\n"
            + "                \"certificateChain\":[\"-----BEGIN CERTIFICATE-----MIIC ... ps==n-----"
            + "END CERTIFICATE-----\",\"-----BEGIN CERTIFICATE-----\\\\MIIC ... "
            + "AB==\\\\n-----END CERTIFICATE-----\"],\n"
            + "                \"createdOn\": \"2018-07-17T20:48:52.757Z\"\n"
            + "            },\n"
            + "            \"abcDrmType\": \"JAR\",\n"
            + "            \"abcProductId\": \"signing_key\",\n"
            + "            \"isNavigationPresent\": true,\n"
            + "            \"swComponentIdHwNumber\":\"SWM_specific_attribute\",\n"
            + "            \"swComponentIdHwVersion\":\"SWM_specific_attribute\",\n"
            + "            \"fwVersionSwNumber\":\"SWM_specific_attribute\",\n"
            + "            \"fwVersionSwVersion\":\"SWM_specific_attribute\"\n"
            + "             \n"
            + "             \n"
            + "        },\n"
            + "       \"cadm2\": {\n"
            + "            \"swVersion\": \"1.58b\",\n"
            + "            \"hwVersion\": \"1.54\",\n"
            + "            \"partNumber\": \"187469231\",\n"
            + "            \"os\": \"Android\",\n"
            + "      \"screenSize\":\"5\",\n"
            + "            \"manufacturer\": \"Harman\",\n"
            + "            \"ecuType\": \"VP4\",\n"
            + "            \"serialNo\": \"9874265387896345\",\n"
            + "            \"clientId\": \"12345\",\n"
            + "            \"capabilities\": {\n"
            + "                \"services\": [{\n"
            + "                        \"serviceId\": \"SRTD\"\n"
            + "                    },\n"
            + "                    {\n"
            + "                        \"serviceId\": \"REOND\"\n"
            + "                    }\n"
            + "                ],\n"
            + "                \"abcApplications\": [{\n"
            + "                        \"serviceId\": \"SOS\",\n"
            + "                        \"applicationId\": \"SOS_apt_v01\",\n"
            + "                        \"version\": \"1.43\"\n"
            + "                    },\n"
            + "                    {\n"
            + "                        \"serviceId\": \"AssistApp\",\n"
            + "                        \"applicationId\": \"AssistApp_apt_v05\",\n"
            + "                        \"version\": \"1.44\"\n"
            + "                    }\n"
            + "                ]\n"
            + "            },\n"
            + "           \"provisionedServices\": {\n"
            + "                \"services\": [{\n"
            + "                    \"serviceId\": \"REON\",\n"
            + "                    \"applicationId\": \"REON\"\n"
            + "                }],\n"
            + "                \"abcApplications\": [{\n"
            + "                    \"serviceId\": \"SOS\",\n"
            + "                    \"applicationId\": \"SOS\",\n"
            + "                    \"version\": \"1.43\"\n"
            + "                }]\n"
            + "            },\n"
            + "            \"abcProvisionedState\": {\n"
            + "                \"state\": \"PENDING\",\n"
            + "                \"date\": \"2018-07-17T20:48:52.757Z\"\n"
            + "            },\n"
            + "            \"abcDrm\": {\n"
            + "                \"raw\": \"readable DRM file\",\n"
            + "                \"signed\": \"you_cannot_read_this_bcz_it_is_signed\",\n"
            + "                \"signatureType\":\"RSA2048_SHA256_RSASSA_PKCS1_V1_5\",\n"
            + "                \"certificateChain\":[\"-----BEGIN CERTIFICATE-----MIIC ... ps==n-----"
            + "END CERTIFICATE-----\",\"-----BEGIN CERTIFICATE-----\\\\MIIC ... AB==\\\\n"
            + "-----END CERTIFICATE-----\"],\n"
            + "                \"createdOn\": \"2018-07-17T20:48:52.757Z\"\n"
            + "            },\n"
            + "            \"abcDrmType\": \"JAR\",\n"
            + "            \"abcProductId\": \"signing_key\",\n"
            + "            \"isNavigationPresent\": true,\n"
            + "            \"swComponentIdHwNumber\":\"SWM_specific_attribute\",\n"
            + "            \"swComponentIdHwVersion\":\"SWM_specific_attribute\",\n"
            + "            \"fwVersionSwNumber\":\"SWM_specific_attribute\",\n"
            + "            \"fwVersionSwVersion\":\"SWM_specific_attribute\"\n"
            + "             \n"
            + "             \n"
            + "        }\n"
            + "},\n"
            + "  \"abcSubscribedPackages\": [{\n"
            + "        \"packageName\": \"DEFAULT\",\n"
            + "        \"startDate\": \"2018-07-17T20:17:13.711Z\",\n"
            + "        \"endDate\": \"2018-07-17T20:17:13.711Z\",\n"
            + "        \"subscriptionId\": \"1343251\",\n" + "        \"state\": \"ACTIVE\"\n" + "    }]\n" + "}");
        Mockito.doReturn(json).when(coreVehicleProfileClient).getVehicleProfileJSON(vehicleId, false);
        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);

        assertEquals(vh.getName(), vp.getName());
        assertEquals(vh.getModel(), vp.getModel());
        assertEquals(vh.getModelYear(), vp.getModelYear());
        assertEquals(vh.getMake(), vp.getMake());
        assertEquals("+1 2299910912", vp.getEmergencyNumber());
        assertEquals("Q71 9031", vp.getPlateNumber());
        assertEquals(userId, vp.getUserId());

    }

    @Test
    public void testProcessWithVehicleJsonException() {

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();

        // create sample data
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.ofNullable(VIN));
        String userId = "testUser";
        String vehicleId = "MAR_1234";
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.USERID, Optional.of(userId));
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            VehicleProfileAttribute.USERID);

        Mockito.doThrow(new RuntimeException()).when(coreVehicleProfileClient).getVehicleProfileJSON(vehicleId, false);
        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);

        assertEquals(vh.getName(), vp.getName());
        assertEquals(vh.getModel(), vp.getModel());
        assertEquals(vh.getModelYear(), vp.getModelYear());
        assertEquals(vh.getMake(), vp.getMake());
        assertEquals(null, vp.getEmergencyNumber());
        assertEquals(null, vp.getPlateNumber());
        assertEquals(userId, vp.getUserId());

    }

    @Test
    public void testProcessWithoutVehicleProfile() {

        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);

        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        VehicleProfile vehProf = new VehicleProfile();
        vehProf.setVehicleAttributes(vh);
        Mockito.when(event.getVehicleId()).thenReturn(null);
        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);
        assertNull(vp);
    }

    @Test
    public void testProcessWithoutUserAssociation() {


        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        geofenceGrouping.setCheckAssociation(false);
        alertsInfo.setNotificationGrouping(geofenceGrouping);

        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        VehicleProfile vehProf = new VehicleProfile();
        vehProf.setVehicleAttributes(vh);
        String vehicleId = "MAR_1234";
        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.ofNullable(null));
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Mockito.doThrow(new RuntimeException("The path $['data']['authorizedUsers'] is null"))
            .when(coreVehicleProfileClient)
            .getVehicleProfileAttributes(vehicleId, false,
                VehicleProfileAttribute.USERID);
        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);

        assertEquals(vh.getName(), vp.getName());
        assertEquals(vh.getModel(), vp.getModel());
        assertEquals(vh.getModelYear(), vp.getModelYear());
        assertEquals(vh.getMake(), vp.getMake());
        assertNull(vp.getVin());
        assertNull(vp.getUserId());
    }

    @Test(expected = UserNotAssociatedException.class)
    public void testProcessWithAssociationTrueNoUser() {


        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        geofenceGrouping.setCheckAssociation(true);
        alertsInfo.setNotificationGrouping(geofenceGrouping);

        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        VehicleProfile vehProf = new VehicleProfile();
        vehProf.setVehicleAttributes(vh);
        String vehicleId = "MAR_1234";
        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.ofNullable(VIN));
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Mockito.doThrow(new RuntimeException("The path $['data']['authorizedUsers'] is null"))
            .when(coreVehicleProfileClient)
            .getVehicleProfileAttributes(vehicleId, false,
                VehicleProfileAttribute.USERID);

        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);
    }

    @Test
    public void testProcessWithUserAssociation() {


        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();
        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        geofenceGrouping.setCheckAssociation(false);
        alertsInfo.setNotificationGrouping(geofenceGrouping);

        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        VehicleProfile vehProf = new VehicleProfile();
        vehProf.setVehicleAttributes(vh);
        String vehicleId = "MAR_1234";

        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);

        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.ofNullable(VIN));
        String userId = "testUser01";
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.USERID, Optional.of(userId));
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            VehicleProfileAttribute.USERID);
        vpService.getName();
        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);
        assertEquals(vh.getName(), vp.getName());
        assertEquals(vh.getModel(), vp.getModel());
        assertEquals(vh.getModelYear(), vp.getModelYear());
        assertEquals(vh.getMake(), vp.getMake());
        assertEquals(userId, vp.getUserId());
    }

    @Test
    public void testProcessWithVpException() {
        AlertsInfo alertsInfo = new AlertsInfo();
        Data data = new Data();

        data.set("id", "geofenceId");
        alertsInfo.setIgniteEvent(event);
        alertsInfo.setAlertsData(data);
        NotificationGrouping geofenceGrouping = new NotificationGrouping("GeofenceIn", "ParentalControls");
        alertsInfo.setNotificationGrouping(geofenceGrouping);
        VehicleAttributes vh = new VehicleAttributes();
        vh.setName("MARUTI");
        vh.setModel("BREEZA-MT");
        vh.setModelYear("2019");
        vh.setMake("MARUTI");
        Map<String, VehicleProfileOnDemandAttribute> attrs = new HashMap<>();
        attrs.put("make", make);
        attrs.put("model", model);
        attrs.put("modelYear", modelYear);
        attrs.put("name", name);
        attrs.put("vin", vin);
        Mockito.doReturn(attrs).when(utils).getVehicleAttributes();
        Map<String, Optional<?>> vpAttrs = new HashMap<>();
        vpAttrs.put("make", Optional.of(vh.getMake()));
        vpAttrs.put("model", Optional.of(vh.getModel()));
        vpAttrs.put("modelYear", Optional.of(vh.getModelYear()));
        vpAttrs.put("name", Optional.of(vh.getMake()));
        vpAttrs.put("vin", Optional.of(VIN));
        String userId = "testUser";
        String vehicleId = "MAR_1234";

        Mockito.when(event.getVehicleId()).thenReturn(vehicleId);
        Mockito.doReturn(vpAttrs).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            attrs.values().toArray(new VehicleProfileOnDemandAttribute[attrs.size()]));

        Map<VehicleProfileAttribute, Optional<String>> attr = new HashMap<>();
        attr.put(VehicleProfileAttribute.USERID, Optional.of(userId));
        Mockito.doReturn(attr).when(coreVehicleProfileClient).getVehicleProfileAttributes(vehicleId, false,
            VehicleProfileAttribute.USERID);

        VehicleProfileAbridged vp = vpService.getVehicleProfile(alertsInfo);


        assertEquals(vh.getName(), vp.getName());
        assertEquals(vh.getModel(), vp.getModel());
        assertEquals(vh.getModelYear(), vp.getModelYear());
        assertEquals(vh.getMake(), vp.getMake());
        assertTrue(StringUtils.isEmpty(vp.getEmergencyNumber()));
        assertTrue(StringUtils.isEmpty(vp.getPlateNumber()));
        assertEquals(userId, vp.getUserId());

    }


}
