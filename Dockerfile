FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml mvnw.cmd ./
COPY .mvn .mvn
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 execution
COPY --from=build /workspace/target/mecanica-execution-service-0.0.1-SNAPSHOT.jar app.jar
COPY newrelic.jar newrelic.yml ./
USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-jar", "/app/app.jar"]
