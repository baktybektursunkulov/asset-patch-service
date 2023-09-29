# maven
FROM maven:3.8.7-eclipse-temurin-8-alpine AS maven
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN mvn package

# docker
FROM eclipse-temurin:8-alpine

COPY --from=maven /usr/src/app/target/*.jar application.jar

ENTRYPOINT ["java","-jar","application.jar"]