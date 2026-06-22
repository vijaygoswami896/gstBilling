FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN useradd -m appuser && chown -R appuser /app
USER appuser

EXPOSE 7860

ENTRYPOINT ["java", "-Xmx1g", "-Djava.net.preferIPv4Stack=true", "-Dserver.port=7860", "-jar", "app.jar"]