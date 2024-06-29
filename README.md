# Spring Boot User Management API and PostgreSQL (Dockerized)

Simple Spring boot application with the following characteristics:
 - PostreSQL repository quickly available in docker container (therefore H2 not used)
 - 

To quickly run Spring Boot + PostgreSQL use docker commands below.

To run integration test on localhost, first Setup images and run Postgre image to have backend DB available.

## Dockerized Spring Boot + PostgreSQL

To build whole Spring boot + PostgreSQL and run using Docker images from a [docker-compose.yml](docker-compose.yml) simply run commands:

### Setup & Start images
``` bash
docker build --no-cache . && docker-compose up
```

The application should be available at
http://localhost:8080/example - no authorization

http://localhost:8080/users - basic authorization needed (admin:password or created user)

#### Setup images option only
``` bash
docker build --no-cache .
```

#### Start Postgres image
``` bash
docker-compose up postgres-db
```