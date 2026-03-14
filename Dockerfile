FROM maven:4.0.0-rc-4-amazoncorretto-21 AS build

COPY src /app/src
COPY pom.xml /app

WORKDIR /app
RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine

COPY --from=build /app/target/Consultas-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]