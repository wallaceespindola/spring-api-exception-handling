# Build stage
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
LABEL org.opencontainers.image.authors="Wallace Espindola <wallace.espindola@gmail.com>" \
      org.opencontainers.image.source="https://github.com/wallaceespindola/spring-api-exception-handling" \
      org.opencontainers.image.licenses="Apache-2.0"
ENV JAVA_OPTS="-Xms128m -Xmx384m"
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
