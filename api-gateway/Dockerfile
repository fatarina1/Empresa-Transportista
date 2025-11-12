# Multi-stage build: build with Maven, run with JRE
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
# Use Maven to build the project (skip tests for speed)
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
# copy fat jar produced by spring-boot-maven-plugin
COPY --from=build /workspace/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
