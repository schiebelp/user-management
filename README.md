# Spring Boot User Management API and PostgreSQL (Dockerized)

Simple Spring boot application with the following characteristics:
- **PostreSQL image** available in docker container (therefore H2 not used)
- Standard **REST Controller** - **Service** - **Repository** pattern to separate concerns
- Basic Authentization used by Spring Security
- API errors in RFC-7807 format
- OpenAPI documentation at http://localhost:8080/swagger-ui/index.html#/
- REST Endpoint at http://localhost:8080/users
- Passwords are masked in log
- Unit tests are based on @Nested classes for better readability

## To run local Spring boot and PostgreSql image:

1) Start docker daemon

2) Start PostreSQL Image And Springboot
``` bash
docker-compose build postgres-db && docker-compose up -d postgres-db && ./gradlew bootRun
```

3) Application REST API is available at http://localhost:8080/users

## To Run dockerize Spring Boot and PostgreSql images:

1) Please run
``` bash
docker-compose build && docker-compose up
```

2) Application REST API is available at http://localhost:8080/users

To check details like ports, etc. run
``` bash
docker ps
```

## REST API details

Endpoint: http://localhost:8080/users
Documentation: http://localhost:8080/swagger-ui/index.html#/ (no security)
Security: Basic Auth
    - Admin: username = admin / password = password
    - User: Saved in DB

### Notes:

#### Run Spring boot
``` bash
./gradlew bootRun
```

#### Setup images option only
``` bash
docker build .
```

### Setup images option only from new source
``` bash
docker build --no-cache .
```

#### Start Postgres image
``` bash
docker-compose up postgres-db
```

Todo:
- Paginated get all users
- more tests...
- DB not up test to fail early(Caused by: org.hibernate.service.spi.ServiceException: Unable to create requested service [org.hibernate.engine.jdbc.env.spi.JdbcEnvironment] due to: Unable to determine Dialect without JDBC metadata (please set 'jakarta.persistence.jdbc.url' for common cases or 'hibernate.dialect' when a custom Dialect implementation must be provided))
- use BCryptPasswordEncoderTests instead of the base class to be testable
- Consider H2 regardles, so application can be ran without postgres running
- etc...