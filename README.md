# idporten-user-service

[![Maven build status](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-maventests.yml/badge.svg)](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-maventests.yml)
[![Build image](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-buildimage.yml/badge.svg)](https://github.com/felleslosninger/idporten-user-service/actions/workflows/call-buildimage.yml)


This application is a service for managing users in ID-porten.

## Requirements

To build and run the application you need:

* JDK 17
* Maven

## Running the application locally

The application has profiles located in the [resources](src/main/resources) directory.

| Profile | Description                                    |
|---------|------------------------------------------------|

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


## allure test report in local

    mvn clean test
    mv allure-results/ target/  
    mvn io.qameta.allure:allure-maven:serve


### Required vault values and kubernetes config||


## Runtime dependencies

