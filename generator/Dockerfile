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
COPY --from=builder /app/target/generator-*.jar /app/generator.jar

# Environment variables (MUST be set at runtime)
ENV CURRENCY_LAYER_API_KEY=unset
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/generator.jar"]