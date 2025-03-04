FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app
COPY . /app

RUN mvn clean package

FROM eclipse-temurin:17
WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
