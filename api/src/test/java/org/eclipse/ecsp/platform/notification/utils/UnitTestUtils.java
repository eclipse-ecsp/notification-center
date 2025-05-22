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

package org.eclipse.ecsp.platform.notification.utils;

import org.eclipse.ecsp.platform.notification.dto.ResponseWrapper;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterError;
import org.eclipse.ecsp.platform.notification.exceptions.NotificationCenterExceptionBase;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UnitTestUtils.
 */
public class UnitTestUtils {

    private UnitTestUtils() {}

    public static void compareExceptionWithoutSubMessage(NotificationCenterExceptionBase thrown,
                                                         NotificationCenterError mainException,
                                                         NotificationCenterError subError) {
        ResponseWrapper.Message msg = thrown.getErrors().iterator().next();
        compareExceptionWithoutSubMessage(msg, thrown, mainException, subError);
    }

    /**
     * compareExceptionWithoutSubMessage.
     */
    public static void compareExceptionWithoutSubMessage(ResponseWrapper.Message msg,
                                                         NotificationCenterExceptionBase thrown,
                                                         NotificationCenterError mainException,
                                                         NotificationCenterError subError) {
        assertEquals(thrown.getMessage(), mainException.getMessage());
        assertEquals(thrown.getCode(), mainException.getCode());
        assertEquals(thrown.getReason(), mainException.getReason());
        assertEquals(msg.getCode(), subError.getCode());
        assertEquals(msg.getReason(), subError.getReason());
    }

    /**
     * compare Exception.
     */
    public static void compareException(NotificationCenterExceptionBase thrown, NotificationCenterError mainException,
                                        NotificationCenterError subError) {
        ResponseWrapper.Message msg = thrown.getErrors().iterator().next();
        compareExceptionWithoutSubMessage(msg, thrown, mainException, subError);
        assertEquals(msg.getMsg(), subError.getMessage());
    }

    /**
     * compare Exception.
     */
    public static void compareException(NotificationCenterExceptionBase thrown, NotificationCenterError mainException,
                                        NotificationCenterError subError, String... str) {
        ResponseWrapper.Message msg = thrown.getErrors().iterator().next();
        compareExceptionWithoutSubMessage(msg, thrown, mainException, subError);
        assertEquals(msg.getMsg(), subError.getMessage(str));
    }

    /**
     * compare Exception.
     */
    public static void compareException(NotificationCenterExceptionBase thrown, NotificationCenterError mainException,
                                        NotificationCenterError subError, List<String> errorList) {
        Iterator it = thrown.getErrors().iterator();
        ResponseWrapper.Message msg;
        while (it.hasNext()) {
            msg = (ResponseWrapper.Message) it.next();
            compareExceptionWithoutSubMessage(msg, thrown, mainException, subError);
            assertEquals(msg.getMsg(), subError.getMessage(getErrorFromList(msg, errorList)));
        }
    }

    /**
     * compare Exception.
     */
    public static void compareException(NotificationCenterExceptionBase thrown, NotificationCenterError mainException,
                                        List<NotificationCenterError> subErrors, List<String> errors) {
        Iterator it = thrown.getErrors().iterator();
        ResponseWrapper.Message msg;
        while (it.hasNext()) {
            msg = (ResponseWrapper.Message) it.next();
            NotificationCenterError subError = getSubErrorFromList(msg, subErrors);
            compareExceptionWithoutSubMessage(msg, thrown, mainException, subError);
            assertEquals(msg.getMsg(), subError.getMessage(getErrorFromList(msg, errors)));
        }
    }

    /**
     * compare Exception.
     */
    public static void compareExceptionMultiParams(NotificationCenterExceptionBase thrown,
                                                   NotificationCenterError mainException,
                                                   NotificationCenterError subError, List<List<String>> errorList) {
        Iterator it = thrown.getErrors().iterator();
        ResponseWrapper.Message msg;
        while (it.hasNext()) {
            msg = (ResponseWrapper.Message) it.next();
            compareExceptionWithoutSubMessage(msg, thrown, mainException, subError);
            assertEquals(msg.getMsg(),
                subError.getMessage(Objects.requireNonNull(getErrorsFromList(msg, errorList)).toArray(new String[0])));
        }
    }



    /**
     * compare Exception.
     */
    public static void compareMessage(ResponseWrapper.Message msg, NotificationCenterError subError, String... str) {
        assertEquals(msg.getMsg(), subError.getMessage(str));
        assertEquals(msg.getCode(), subError.getCode());
        assertEquals(msg.getReason(), subError.getReason());
    }

    private static String getErrorFromList(ResponseWrapper.Message msg, List<String> errorList) {
        for (String error : errorList) {
            if (msg.getMsg().contains(error)) {
                return error;
            }
        }
        return null;
    }

    private static List<String> getErrorsFromList(ResponseWrapper.Message msg, List<List<String>> errorLists) {
        for (List<String> errors : errorLists) {
            if (errors.stream().allMatch(e -> msg.getMsg().contains(e))) {
                return errors;
            }
        }
        return null;
    }

    private static NotificationCenterError getSubErrorFromList(ResponseWrapper.Message msg,
                                                               List<NotificationCenterError> subErrorList) {
        for (NotificationCenterError subError : subErrorList) {
            if (subError.getCode().contains(msg.getCode())) {
                return subError;
            }
        }
        return null;
    }
}
