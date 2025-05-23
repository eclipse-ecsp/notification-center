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

package org.eclipse.ecsp.configuration;

import org.eclipse.ecsp.platform.notification.exceptions.InvalidUserIdInput;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidVehicleIdInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.ecsp.platform.notification.v1.utils.ResponseMsgConstants.UNSUPPORTED_API_VERSION_MSG;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * ExceptionsAdviser class.
 */
@ControllerAdvice
public class ExceptionsAdviser {

    /**
     * Handles requests to unmapped endpoints (404 errors).
     *
     * <p>
     * To enable this exception, set the Spring flag: <code>spring.mvc.throw-exception-if-no-handler-found=true</code>
     * and add <code>@EnableWebMvc</code> to your configuration.
     * </p>
     *
     * @param ex the exception thrown when no handler is found
     * @return a response entity with a not found status and error message
     */
    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<Object> handleNotFoundError(NoHandlerFoundException ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", UNSUPPORTED_API_VERSION_MSG);
        return new ResponseEntity<>(map, NOT_FOUND);
    }

    /**
     * Handle InvalidUserIdInput exception.
     *
     * @param ex InvalidUserIdInput
     * @return ResponseEntity
     */
    @ExceptionHandler(InvalidUserIdInput.class)
    public ResponseEntity<Object> invalidUserIdException(InvalidUserIdInput ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle InvalidVehicleIdInput exception.
     *
     * @param ex InvalidVehicleIdInput
     * @return ResponseEntity
     */
    @ExceptionHandler(InvalidVehicleIdInput.class)
    public ResponseEntity<Object> invalidVehicleIdException(InvalidVehicleIdInput ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
