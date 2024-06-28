FROM alpine:latest

## Install OpenJDK 17
RUN apk add --no-cache openjdk17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} user-management-1-0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/user-management-1-0-SNAPSHOT.jar"]