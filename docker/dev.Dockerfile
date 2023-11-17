FROM maven:3-eclipse-temurin-21 as builder

ARG GIT_PACKAGE_TOKEN
ARG GIT_PACKAGE_USERNAME

ENV GIT_PACKAGE_TOKEN=${GIT_PACKAGE_TOKEN}
ENV GIT_PACKAGE_USERNAME=${GIT_PACKAGE_USERNAME}

COPY pom.xml /home/app/
COPY docker/settings.xml /root/.m2/settings.xml

COPY src /home/app/src
RUN --mount=type=cache,target=/root/.m2/repository mvn -f /home/app/pom.xml clean package -Dmaven.test.skip=true -Dmaven.gitcommitid.skip=true


FROM eclipse-temurin:21-jre-jammy

ARG APPLICATION=idporten-user-service
RUN mkdir /var/log/${APPLICATION}
RUN mkdir /usr/local/webapps
WORKDIR /usr/local/webapps

COPY --from=builder /home/app/target/${APPLICATION}-0.0.1-SNAPSHOT.jar application.jar

ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 7080