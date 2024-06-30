# Spring Boot User Management API and PostgreSQL (Dockerized)

Simple Spring boot application with the following characteristics:
- **PostreSQL image** available in docker container (therefore H2 not used)
- Standard **REST Controller** - **Service** - **Repository** pattern to separate concerns
- Basic Authentization used by Spring Security

## To run local Spring boot and PostgreSql image:

1) Start docker daemon

2) Start PostreSQL Image
``` bash
docker-compose build postgres-db && docker-compose up
```

3) Run Management API
   Please run `./gradlew bootRun`.

4) Application REST API is available at http://localhost:8080/users

## To Run dockerize Spring Boot and PostgreSql images:

1) Please run
``` bash
docker build --no-cache . && docker-compose up
```

No cache option:
``` bash
docker build --no-cache . && docker-compose up
```

2) Application REST API is available at http://localhost:8080/users

To check details like ports, etc. run
``` bash
docker ps
```

## Testing application

Application REST API is available at http://localhost:8080/users

REST Api yaml for test: [api.yml](src%2Fmain%2Fresources%2Fapi.yml)

Basic authorization is based on Admin
username = admin
password = password

Or a saved User in database.

User can be created using API.

### Notes:

#### Setup images option only
``` bash
docker build --no-cache .
```

#### Start Postgres image
``` bash
docker-compose up postgres-db
```

Todo: Auto generate yaml, Long id validation, More tests...