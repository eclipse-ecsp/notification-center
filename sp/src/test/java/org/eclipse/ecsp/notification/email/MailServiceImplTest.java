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

package org.eclipse.ecsp.notification.email;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.ecsp.domain.notification.AlertsInfo;
import org.eclipse.ecsp.notification.utils.NotificationProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.eclipse.ecsp.domain.notification.commons.EventID.GENERIC_NOTIFICATION_EVENT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * MailServiceImplTest.
 */
public class MailServiceImplTest {
    @InjectMocks
    MailServiceImpl mailServiceImpl;
    @Mock
    JavaMailSender mailSender;
    MimeMessage mimeMessage;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        mailServiceImpl.init(getProperties());
    }

    @Test
    public void testPrepareMimeMessage() throws MessagingException, IOException {
        AlertsInfo alertsInfo = getAlertsInfo();
        mimeMessage = new MimeMessage((Session) null);
        doReturn(mimeMessage).when(mailSender).createMimeMessage();
        MimeMessageHelper mimeMessageHelper =
            mailServiceImpl.prepareMimeMessage("efrat.sadomsky@gmail.com", alertsInfo, "test");
        assertEquals("testFrom", ((InternetAddress) mimeMessageHelper.getMimeMessage().getFrom()[0]).getAddress());
        List<Address> addresses =
            Arrays.asList(mimeMessageHelper.getMimeMessage().getRecipients(MimeMessage.RecipientType.TO));
        assertEquals(1, addresses.size());
        assertEquals("efrat.sadomsky@gmail.com", ((InternetAddress) addresses.get(0)).getAddress());

    }

    @Test
    public void testSendEmail() {
        doNothing().when(mailSender).send((MimeMessage) any());
        mailServiceImpl.sendEmail((MimeMessage) any());
        verify(mailSender, times(1)).send((MimeMessage) any());

    }


    @NotNull
    private AlertsInfo getAlertsInfo() {
        AlertsInfo alertsInfo = new AlertsInfo();
        alertsInfo.setEventID(GENERIC_NOTIFICATION_EVENT);
        return alertsInfo;
    }

    @NotNull
    private Properties getProperties() {
        Properties props = new Properties();
        props.setProperty(NotificationProperty.EMAIL_FROM, "testFrom");
        props.setProperty(NotificationProperty.EMAIL_SUBJECT, "testSubject");
        return props;
    }


}
