#!/bin/sh

#
# /*
#  *
#  * ******************************************************************************
#  *
#  *  Copyright (c) 2023-24 Harman International
#  *
#  *
#  *
#  *  Licensed under the Apache License, Version 2.0 (the "License");
#  *
#  *  you may not use this file except in compliance with the License.
#  *
#  *  You may obtain a copy of the License at
#  *
#  *
#  *
#  *  http://www.apache.org/licenses/LICENSE-2.0
#  *
#  **
#  *  Unless required by applicable law or agreed to in writing, software
#  *
#  *  distributed under the License is distributed on an "AS IS" BASIS,
#  *
#  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  *
#  *  See the License for the specific language governing permissions and
#  *
#  *  limitations under the License.
#  *
#  *
#  *
#  *  SPDX-License-Identifier: Apache-2.0
#  *
#  *  *******************************************************************************
#  *
#  */
#

echo "Starting NOTIFICATION Config Utility"

echo "Java Options:" ${JVM_OPTS}

echo "Logging Level:" ${LOG_LEVEL}


exec java ${JVM_OPTS} -cp /notification/jar/notification-config-utility.jar org.eclipse.ecsp.platform.notification.utility.NotificationConfigUtility