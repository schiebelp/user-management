FROM alpine:3.20.1 AS build

RUN apk --no-cache --update add openjdk21-jre

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} usermanagement-0-0-1-SNAPSHOT.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/usermanagement-0-0-1-SNAPSHOT.jar"]