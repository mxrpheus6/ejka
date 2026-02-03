FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=builder /app/target/ejka-1.0.0.jar app.jar

EXPOSE 4003

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]