FROM openjdk:17

ARG PROJECT_JAR_VERSION
RUN echo ${PROJECT_JAR_VERSION}

ADD ./target/notification-config-utility-${PROJECT_JAR_VERSION}-shaded.jar /notification/jar/notification-config-utility.jar
ADD src/scripts/startNotificationUtility.sh /notification/bin/
ADD src/main/resources/logback.xml /notification/logback.xml


RUN chmod 755 /notification/bin/startNotificationUtility.sh && \
    chown -R 1000 /notification

ENTRYPOINT ["/notification/bin/startNotificationUtility.sh"]