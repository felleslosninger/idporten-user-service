# idporten-user-service

[![Maven build status](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-maventests.yml/badge.svg)](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-maventests.yml)
[![Build image](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-buildimage.yml/badge.svg)](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-buildimage.yml)


This application is a service for managing users in ID-porten with two APIs:
1. /login - for logging in users only available for idporten-login application internally in the namespace
  * in case of accidentially exposing this endpoint, it is secured by a simple api-key known to idporten-login only
  * since no TLS inside of the namespace, it has very limited security inside the namespace
2. /admin - for managing users, available externally for admin-applications secured by Maskinporten (oauth2 token)

## Requirements

To build and run the application you need:

* JDK 25
* Maven

## Running the application locally

The application has profiles located in the [resources](src/main/resources) directory.

| Profile   | Description                                                      |
|-----------|------------------------------------------------------------------|
| local-h2  | Local development with embedded H2-database                      |
| local-dev | Local development which requires a preinstalled MariaDB database |
| docker    | User docker cluster locally, run by docker-compose file          |

The application can be started with Maven:

```
mvn spring-boot:run -Dspring-boot.run.profiles=<profile>
```

## Configuration

General properties for a Spring Boot
application (https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html) has
a [default configuration](src/main/resources/application.yaml) in the application archive.


### Application features
| Key                                 | Default value | Description                                                                       |
|-------------------------------------|---------------|-----------------------------------------------------------------------------------|
| digdir.caching.enabled              | true          | Turns redis caching and streams on/off                                            |
| digdir.caching.time_to_live_in_days | true          | Sets time to live on redis keys. The property is required when caching is enabled |

### Required vault values and kubernetes config. Environment variables:
* DATASOURCE_PASSWORD (password to idporten_user database for user user_service)
* API_KEY (api-key for /login access)
* REDIS_PASSWORD (password for Redis)

## Usage of Redis Streams and caching
The application uses redis to cache users. The cache is updated on different events defined in /userservice/data/event. 
It also uses Redis streams to achieve async updating of logins in the database. This means that the user service is able
to work even if the database goes down. As long as the user is already cached, a login attempt will be successful.

This functionality is toggled by the digdir.caching.enabled property.

## Testing
The test configuration starts an embedded Redis server via the BaseRedisTest class. The server is a static member of the class, and is shared between all tests. 
The server is started before any tests are run, and stopped after all tests are run. The server is configured to use a standard port (6379).

This means that all tests that starts spring boot (annotated with @SpringBootTest) should inherit from BaseRedisTest, to ensure that the embedded Redis server is running 
during the tests. Tests that does not start spring boot (e.g. unit tests) do not need to inherit from BaseRedisTest, as they do not 
require the embedded Redis server.

The reason for this setup is that the normal way of starting embedded Redis is to implement a SmartLifeCycle bean, which starts the server when the application context is 
started. This works well for normal applications, but in our case we have a lot of tests that does not start spring boot, and we want 
to avoid starting and stopping the embedded Redis server multiple times during the test suite. Also, the constant starting and stopping of Redis
made the tests collide into each other when they were run. By using a static member of the BaseRedisTest class, we can ensure that the embedded 
Redis server is started only once, and is available for all tests that needs it.

