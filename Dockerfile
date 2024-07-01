FROM alpine:3.20.1

## Install OpenJDK 17
RUN apk add --no-cache openjdk17

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} usermanagement-0-0-1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/usermanagement-0-0-1-SNAPSHOT.jar"]