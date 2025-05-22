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

import org.eclipse.ecsp.domain.notification.AlertsHistoryInfo.Status;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AlertsHistoryInfoTest class.
 */
public class AlertsHistoryInfoTest {

    AlertsHistoryInfo alertsHistoryInfo = new AlertsHistoryInfo();

    private final String timeStamp = "321312";

    @Test
    public void testgetPdid() {
        alertsHistoryInfo.getPdid();
    }

    @Test
    public void testsetPdid() {
        alertsHistoryInfo.setPdid("dummyPdid");
    }

    @Test
    public void testgetUserId() {
        alertsHistoryInfo.getUserId();
    }

    @Test
    public void testsetUserId() {
        alertsHistoryInfo.setUserId("dummy");
    }

    @Test
    public void testgetAlertType() {
        alertsHistoryInfo.getAlertType();
    }

    @Test
    public void testsetAlertType() {
        alertsHistoryInfo.setAlertType("Dummy");
    }

    @Test
    public void testgetTimestamp() {
        alertsHistoryInfo.getTimestamp();
    }

    @Test
    public void testsetTimestamp() {
        alertsHistoryInfo.setTimestamp(Integer.valueOf(timeStamp));
    }

    @Test
    public void testgetPayload() {
        alertsHistoryInfo.getPayload();
    }

    @Test
    public void testsetPayload() {
        alertsHistoryInfo.setPayload(null);
    }

    @Test
    public void getChannelResponses() {
        alertsHistoryInfo.getChannelResponses();
    }

    @Test
    public void testsetChannelResponses() {
        List<ChannelResponse> ls = new ArrayList<ChannelResponse>();
        ls.add(new EmailResponse("dsadsa", "dsdsa", "dsadwqf") {

            @Override
            public String getProvider() {
                return null;
            }
        });
        alertsHistoryInfo.setChannelResponses(ls);
    }

    @Test
    public void testEventId() {

        Assert.assertEquals(Status.SCHEDULE_REQUESTED, Status.SCHEDULE_REQUESTED);
        Assert.assertEquals(Status.SCHEDULED, Status.SCHEDULED);
        Assert.assertEquals(Status.CANCELED, Status.CANCELED);
        Assert.assertEquals(Status.FAILED, Status.FAILED);
        Assert.assertEquals(Status.DONE, Status.DONE);
        Assert.assertEquals(Status.READY, Status.READY);
        Assert.assertEquals(Status.STOPPED_BY_CONFIG, Status.STOPPED_BY_CONFIG);
        Assert.assertEquals(Status.RETRY_REQUESTED, Status.RETRY_REQUESTED);
        Assert.assertEquals(Status.RETRY_SCHEDULED, Status.RETRY_SCHEDULED);
        Status.forValue("RETRY_SCHEDULED");

    }

    @Test
    public void testaddChannelResponse() {
        alertsHistoryInfo.addChannelResponse(null);
    }

    @Test
    public void testcurrentStatus() {
        alertsHistoryInfo.currentStatus();
    }

    @Test
    public void testgetCreateDts() {
        alertsHistoryInfo.getCreateDts();
    }

    @Test
    public void testsetCreateDts() {
        alertsHistoryInfo.setCreateDts(null);
    }

    @Test
    public void testsetDefaultMessage() {
        alertsHistoryInfo.setDefaultMessage("DUMMYmSG");
    }

    @Test
    public void testgetId() {
        alertsHistoryInfo.getId();
    }

    @Test
    public void testsetId() {
        alertsHistoryInfo.setId("dummyTest");
    }

    @Test
    public void testgetAlertMessage() {
        alertsHistoryInfo.getAlertMessage();
    }

    @Test
    public void testsetAlertMessage() {
        alertsHistoryInfo.setAlertMessage("dummyalertMsg");
    }

    @Test
    public void testgetGroup() {
        alertsHistoryInfo.getGroup();
    }

    @Test
    public void testsetGroup() {
        alertsHistoryInfo.setGroup("dsadsad");
    }

    @Test
    public void testgetDefaultMessage() {
        testsetChannelResponses();
        alertsHistoryInfo.getDefaultMessage();
    }

    @Test
    public void testsetRead() {
        alertsHistoryInfo.setRead(true);
    }

    @Test
    public void testisRead() {
        alertsHistoryInfo.isRead();
    }

    @Test
    public void testgetNotificationLongName() {
        alertsHistoryInfo.getNotificationLongName();
    }

    @Test
    public void testsetNotificationLongName() {
        alertsHistoryInfo.setNotificationLongName("dummyLongName");
    }

    @Test
    public void testisDeleted() {
        alertsHistoryInfo.isDeleted();
    }

    @Test
    public void testsetDeleted() {
        alertsHistoryInfo.setDeleted(false);
    }

    @Test
    public void testgetCampaignId() {
        alertsHistoryInfo.getCampaignId();
    }

    @Test
    public void testsetCampaignId() {
        alertsHistoryInfo.setCampaignId(null);
    }

    @Test
    public void testtoString() {
        alertsHistoryInfo.toString();
    }

    @Test
    public void testgetStatusHistoryRecordList() {
        alertsHistoryInfo.getStatusHistoryRecordList();
    }

    @Test
    public void testsetStatusHistoryRecordList() {
        alertsHistoryInfo.setStatusHistoryRecordList(null);
    }

    @Test
    public void testaddStatus() {
        alertsHistoryInfo.addStatus(null);
    }

    @Test
    public void test2addStatus() {
        alertsHistoryInfo.addStatus(null, null);
    }

    @Test
    public void testgetRetryRecordList() {
        alertsHistoryInfo.getRetryRecordList();
    }

    @Test
    public void testsetRetryRecordList() {
        alertsHistoryInfo.setRetryRecordList(null);
    }

    @Test
    public void testSetAndGetNotificationId() {
        alertsHistoryInfo.setNotificationId("Test_Notification_id");
        assertEquals("Test_Notification_id", alertsHistoryInfo.getNotificationId());
    }

}
