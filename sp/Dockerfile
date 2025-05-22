FROM eclipseecsp/ecsp-base-java17:1.0.0

ARG PROJECT_JAR_VERSION
RUN echo ${PROJECT_JAR_VERSION}

RUN \
  apk update && \
  apk upgrade --no-cache && \
  apk add --no-cache postgresql-client bash openssl libgcc libstdc++ ncurses-libs

RUN apk update && apk add --no-cache gcompat


RUN echo $JAVA_HOME

COPY target/dependencies /opt/notification/jar/lib
RUN mkdir -p /opt/notification/jar/plugins
##ADD target/classes/notification-sp-${PROJECT_JAR_VERSION}-shaded.jar /opt/notification/jar/notification-sp.jar
ADD ./target/notification-sp-shaded.jar /opt/notification/jar/notification-sp.jar
ADD src/scripts/start.sh /opt/notification/bin/
ADD src/main/resources/kafka.client.keystore.jks /kafka/ssl/kafka.client.keystore.jks
ADD src/main/resources/kafka.client.truststore.jks /kafka/ssl/kafka.client.truststore.jks
ADD src/main/resources/logback.xml /notification/logback.xml

RUN chmod 755 /opt/notification/bin/start.sh
RUN mkdir -p /logs/archived
RUN chown -R 1000 /opt/notification /logs

ENTRYPOINT /opt/notification/bin/start.sh