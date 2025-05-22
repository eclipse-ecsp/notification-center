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

import org.apache.commons.io.IOUtils;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * MigrationUtilsTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MigrationUtilsTest {

    /**
     * Test getTemplateFileStream.
     */
    @Test
    public void getTemplateFileStreamEmptyTemplate() {
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        byte[] data = MigrationUtils.getTemplateFileStream(dynamicNotificationTemplateList);
        assertEquals(0, data.length);
    }

    /**
     * Test getTemplateFileStream.
     */
    @Test
    public void getTemplateFileStreamSuccess() throws Exception {
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = getTemplates();
        byte[] data = MigrationUtils.getTemplateFileStream(dynamicNotificationTemplateList);
        assertNotNull(data);
        String returnData = new String(data);
        returnData = returnData.substring(1);
        assertEquals(getTemplateData(), returnData);
    }

    /**
     * getTemplates.
     */
    private List<DynamicNotificationTemplate> getTemplates() throws IOException {
        return JsonUtils.getListObjects(
            IOUtils.toString(MigrationUtilsTest.class.getResourceAsStream("/migrationTemplate.json"),
                StandardCharsets.UTF_8),
            DynamicNotificationTemplate.class);
    }

    /**
     * getTemplateData.
     */
    private String getTemplateData() {
        return "Notification ID,Notification short name,Notification long name\r\n"
            + "nshai,nshai Notification,nshai Notification long\r\n"
            + "Brand,Channel,Attribute,en_US,fr_FR\r\n"
            + "default,apipush,body,low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "default,apipush,subtitle,low fuel alert,Notification de faible consommation de carburant\r\n"
            + "default,apipush,title,low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "default,email,body,low fuel in name [$.Data.vehicleProfile.name] alert please stop at gas station,"
            + "\"alerte faible en carburant, veuillez vous arrêter à la station-service\"\r\n"
            + "default,email,from,test@test.com,test@test.com\r\n"
            + "default,email,subject,low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "default,ivm,body,low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "default,ivm,title,low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "default,portal,body,low fuel alert please stop at gas station,\"alerte faible en carburant,"
            + " veuillez vous arrêter à la station-service\"\r\n"
            + "default,push,body,low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "default,push,subtitle,low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "default,push,title,low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "default,sms,body,low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "fita,apipush,body,fita low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "fita,apipush,subtitle,fita low fuel alert,Notification de faible consommation de carburant\r\n"
            + "fita,apipush,title,fita low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "fita,email,body,fita low fuel alert please stop at gas station,\"fita alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "fita,email,from,test@test.com,test@test.com\r\n"
            + "fita,email,subject,fita fita low fuel Notification,fita Notification "
            + "de faible consommation de carburant\r\n"
            + "fita,ivm,body,fita low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "fita,ivm,title,fita low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "fita,portal,body,fita low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "fita,push,body,fita low fuel alert please stop at gas station,\"alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n"
            + "fita,push,subtitle,fita low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "fita,push,title,fita low fuel Notification,Notification de faible consommation de carburant\r\n"
            + "fita,sms,body,fita low fuel alert please stop at gas station,\"fita alerte faible en carburant, "
            + "veuillez vous arrêter à la station-service\"\r\n";
    }
}