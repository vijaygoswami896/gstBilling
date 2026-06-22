# ==========================================
# Stage 1: Build the Spring Boot Application
# ==========================================
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Run the Spring Boot Application
# ==========================================
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /home/user/.cache && chmod -R 777 /home/user

ENV PORT=7860
EXPOSE 7860

ENTRYPOINT ["sh", "-c", "java -Xmx4g -Djava.net.preferIPv4Stack=true -Dserver.port=${PORT} -jar app.jar"]