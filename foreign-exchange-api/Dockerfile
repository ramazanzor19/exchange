# Build stage
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/target/foreign-exchange-api-*.jar /app/foreign-exchange-api.jar


EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/foreign-exchange-api.jar"]