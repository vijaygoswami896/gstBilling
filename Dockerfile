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
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /home/user/.cache && chmod -R 777 /home/user

# Default port (overridden by PORT env var at runtime, e.g. on Render)
ENV PORT=7860
EXPOSE 7860

# Use shell form so $PORT is resolved at container start, not at build time
ENTRYPOINT ["sh", "-c", "java -Xmx4g -Dserver.port=${PORT} -jar app.jar"]



## ==========================================
## Stage 1: Build the Spring Boot Application
## ==========================================
#FROM maven:3.8.5-openjdk-17 AS build
#WORKDIR /app
#
## Copy the pom.xml and source code into the container
#COPY pom.xml .
#COPY src ./src
#
## Build the application and skip running tests to speed up deployment
#RUN mvn clean package -DskipTests
#
## ==========================================
## Stage 2: Run the Spring Boot Application
## ==========================================
#FROM openjdk:17-jdk-slim
#WORKDIR /app
#
## Copy the compiled JAR file from the build stage
#COPY --from=build /app/target/*.jar app.jar
#
## Create a safe home directory for Hugging Face's user restrictions
#RUN mkdir -p /home/user/.cache && chmod -R 777 /home/user
#
## Hugging Face routes traffic strictly through port 7860
#EXPOSE 7860
#
## Execute the application, override the default port to 7860, and cap JVM memory
#ENTRYPOINT ["java", "-Xmx4g", "-Dserver.port=7860", "-jar", "app.jar"]