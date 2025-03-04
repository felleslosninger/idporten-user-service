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

* JDK 21
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

| Key                                                | Default value | Description                                     |
|----------------------------------------------------|---------------|-------------------------------------------------|



### Required vault values and kubernetes config. Environment variables:
* DATASOURCE_PASSWORD (password to idporten_user database for user user_service)
* API_KEY (api-key for /login access)



