FROM eclipseecsp/ecsp-base-java17-tomcat10-apr:1.0.0

ARG API_WAR_NAME
ARG PROJECT_JAR_VERSION

RUN echo $JAVA_HOME

RUN apk update && apk add --no-cache gcompat

ADD target/notification-api-${PROJECT_JAR_VERSION}.jar /notification-api/${API_WAR_NAME}.jar
ADD src/main/resources/kafka.client.keystore.jks /kafka/ssl/kafka.client.keystore.jks
ADD src/main/resources/kafka.client.truststore.jks /kafka/ssl/kafka.client.truststore.jks
ADD src/main/resources/logback.xml /notification-api/conf/logback.xml
COPY src/scripts/* /notification-api/bin/


RUN chmod 755 /notification-api/bin/start.sh
RUN mkdir -p /logs/archived

RUN chown -R 1000 /notification-api /logs
ENTRYPOINT /bin/sh /notification-api/bin/start.sh
