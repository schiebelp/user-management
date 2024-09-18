# Spring Boot User Management API and PostgreSQL

Application which showcases a simple REST API to manage User information.

Each User information contains:
 - User id
 - User Name
 - Password
 - First Name
 - Last Name
 - Assigned Roles

Operations:
- POST /users : Create a new user
- DELETE /users/{id} : Delete a user by ID
- GET /users : Get all users
- GET /users/{id} : Get user by ID
- PUT /users/{id} : Update an existing user
- PATCH /users/{id} : Partially update an existing user

Documentation:
- OpenAPI documentation at http://localhost:8080/swagger-ui/index.html#/
- REST Endpoint at http://localhost:8080/users

Security:
- API is secured using Basic Auth Header in Request
- Admin or User present in database can access endpoints
- Any non registered access will be rejected

Example Unauthorized:
```json
401 Unauthorized
{
    "type": "about:blank",
    "title": "Unauthorized",
    "status": 401,
    "detail": "Full authentication is required to access this resource",
    "instance": null,
    "properties": null
}
```

Restrictions:
 - Admin can modify and view all database
 - User can modify only his records and view any User

Validation:
 - Every request is validated based on conditions:
   - validation.password.mandatory=Password is mandatory
   - validation.password.size=Password size must be between {min} and {max}
   - validation.userName.mandatory=User Name is mandatory
   - validation.userName.size=User Name size must be between {min} and {max}
   - validation.userName.regex=Username must contain only letters, numbers, underscore, or hyphen

Examples:
```json
{
    "type": "about:blank",
    "title": "Bad Request",
    "status": 400,
    "detail": "[Password is mandatory, User Name is mandatory]",
    "instance": "/api/users"
}
```
```json
{
"type": "about:blank",
"title": "Bad Request",
"status": 400,
"detail": "Invalid 'id' supplied. Should be a valid 'Integer' and ':id' isn't!",
"instance": "/api/users/:id"
}
```

Idempotency:
 - Duplicated Users cannot be registered
```json
409 Conflict
{
"type": "about:blank",
"title": "Conflict",
"status": 409,
"detail": "User already registered with given userName user1234d",
"instance": "/api/users"
}
```

Notes:
- Docker friendly
- Global Exception Handler
- PostgreSQL
- Mapstruct
- Validation annotations
- DTO hiding passwords
- Robust Unit tests
- @Nested test annotation for better readability
- TestContainers

## To run local Spring boot and PostgreSql image:

1) Start docker daemon
2) Start PostreSQL Image And Springboot
``` bash
docker-compose up -d postgres-db && ./gradlew bootRun
```

3) Application REST API is available at http://localhost:8080/users

## To Run dockerized Spring Boot and PostgreSql images:

1) Start docker daemon
2) Run both images
``` bash
docker-compose up
```
3) Application REST API is available at http://localhost:8080/users

To check details like ports, etc. run
``` bash
docker ps
```

## REST API details

- **Endpoint**: http://localhost:8080/users
- **Documentation**: http://localhost:8080/swagger-ui/index.html#/ (no security)
- **Security**: Basic Auth
  - Admin: username = admin / password = password
  - User: Saved in DB

![open-api.png](open-api.png)

### Notes:

#### Run all Tests
``` bash
./gradlew test
```

#### Run Spring boot
``` bash
./gradlew bootRun
```

#### Clean, build, test before commit
``` bash
./gradlew clean && ./gradlew build
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
- Use pagination
- toml vs version properties for versions of some framework
- use BCryptPasswordEncoderTests instead of the base class to be testable