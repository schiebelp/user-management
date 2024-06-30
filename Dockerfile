FROM alpine:latest

## Install OpenJDK 17
RUN apk add --no-cache openjdk17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} user-management-0-0-1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/user-management-0-0-1-SNAPSHOT.jar"]