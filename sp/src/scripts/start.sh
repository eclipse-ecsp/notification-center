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

echo "Starting Notification Stream Processor"

echo "Java Options:" ${JVM_OPTS}

echo "Logging Level:" ${LOG_LEVEL}

echo "Log file location:" ${LOG_FILE_LOCATION}

echo "Jar location:" ${API_PUSH_NOTIFICATION_JAR}




if [[ -z "${APPENDER}" ]]; then
 echo "Log Appender is empty,defaulting to FILE."
fi

LOG_APPENDER="-DAPPENDER=""${APPENDER:-FILE}"

echo "Log Appender used:" "${LOG_APPENDER}"

exec java ${JVM_OPTS} ${LOG_FILE_LOCATION} ${LOG_APPENDER} -cp /opt/notification/jar/notification-sp.jar:/shared/libs/${API_PUSH_NOTIFICATION_JAR} org.eclipse.ecsp.analytics.stream.base.Launcher