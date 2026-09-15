# Stage 1: Build the Java application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /tmp
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight runtime image
FROM eclipse-temurin:25
WORKDIR /tmp
COPY --from=builder /tmp/target/seMethods-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp/seMethods.jar
ENTRYPOINT ["java", "-jar", "seMethods.jar"]