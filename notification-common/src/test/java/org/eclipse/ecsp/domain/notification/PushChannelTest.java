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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * PushChannelTest class.
 */
public class PushChannelTest {


    PushChannel pc = new PushChannel("test", "Test", new ArrayList<String>());
    PushChannel pc2 = new PushChannel();

    EmailChannel ec = new EmailChannel();
    EmailChannel ec2 = new EmailChannel(new ArrayList<String>());

    SmsChannel smsc = new SmsChannel();
    SmsChannel sms2 = new SmsChannel(new ArrayList<String>());

    @Test
    public void testhas() {
        pc.hashCode();
        pc.equals(null);
        pc.equals(pc);
        pc.equals(EmailChannel.class);
        pc.equals(new PushChannel());
        pc2.equals(new PushChannel());
        pc.setAppPlatform(null);
        pc2.equals(pc);
    }


    @Test
    public void etstrequiresSetup() {
        pc.requiresSetup();

    }

    @Test
    public void testflatten() {

        List<String> ls = new ArrayList<String>();
        ls.add("dsd");
        pc.setDeviceTokens(ls);
        pc.flatten();

        pc2.flatten();
    }

    @Test
    public void testhashemail() {
        ec.hashCode();
        List<String> ls = new ArrayList<String>();
        ls.add("dsd@sdsa.com");
        ec2.setEmails(ls);
        ec2.hashCode();
    }

    @Test
    public void testeqemail() {
        ec.hashCode();
        ec2.hashCode();
        ec.equals(new PushChannel());
        ec.equals(new EmailChannel());
        List<String> ls = new ArrayList<String>();
        ls.add("dsd@sdsa.com");
        ec2.setEmails(ls);
        ec2.equals(new EmailChannel());
        ec.equals(null);
    }

    @Test
    public void tstemailrequiresSetup() {
        ec.requiresSetup();

    }

    @Test
    public void testflattenemail() {


        List<String> ls = new ArrayList<String>();
        ls.add("dsd@sdsa.com");
        ec2.setEmails(ls);
        ec.flatten();
        ec2.flatten();
    }

    @Test
    public void tstsmsrequiresSetup() {
        smsc.requiresSetup();

    }

    @Test
    public void testflattensms() {


        List<String> ls = new ArrayList<String>();
        ls.add("dsd@sdsa.com");
        sms2.setPhones(ls);
        sms2.hashCode();
        smsc.flatten();
        sms2.flatten();
    }
}
