FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /tmp
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25
WORKDIR /tmp
COPY --from=builder /tmp/target/seMethods-0.1.0.4-jar-with-dependencies.jar /tmp/seMethods.jar
ENTRYPOINT ["java", "-jar", "seMethods.jar", "db:3306"]